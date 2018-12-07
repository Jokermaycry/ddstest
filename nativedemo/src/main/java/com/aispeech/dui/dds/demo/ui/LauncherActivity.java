package com.aispeech.dui.dds.demo.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.aispeech.ailog.AILog;
import com.aispeech.dui.dds.DDS;
import com.aispeech.dui.dds.demo.DDSService;
import com.aispeech.dui.dds.demo.R;
import com.aispeech.dui.dds.demo.BackgroundService;

import com.aispeech.dui.dds.demo.wangxi.QinListener;
import com.aispeech.dui.dds.demo.wangxi.QinRecorder;
import com.aispeech.dui.dds.demo.wangxi.XiaoqinRecorder;
import com.aispeech.dui.dds.exceptions.DDSNotInitCompleteException;

public class LauncherActivity extends Activity {
    Button inside,outside,speek;

    private static final String TAG = "LauncherActivity";

    private AlertDialog dialog;
    MyThread my=new MyThread();
    boolean tag=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        inside=(Button)findViewById(R.id.inside);
        outside=(Button)findViewById(R.id.outside);

        inside.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getApplicationContext(), DDSService.class);
                stopService(i);
                i.setAction("start_inside");
                startService(i);
                Intent i1 = new Intent(getApplicationContext(), BackgroundService.class);
                i1.setAction("mother");
                startService(i1);

            }
        });
        outside.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                Intent i = new Intent(getApplicationContext(), DDSService.class);
                stopService(i);
                i.setAction("start_outside");
                startService(i);


            }
        });


        Intent i = new Intent(this, DDSService.class);
        i.setAction("start_inside");
        startService(i);

        my.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("ddsdemo.intent.action.auth_success");
        intentFilter.addAction("ddsdemo.intent.action.auth_failed");
        registerReceiver(authReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(authReceiver);
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        finish();
    }
class MyThread extends Thread
{
    @Override
    public void run() {

        super.run();
        if(tag) {
            checkDDSReady();
        }

    }
}



    public void checkDDSReady() {
        while (true) {
            if (DDS.getInstance().getInitStatus() == DDS.INIT_COMPLETE_FULL ||
                    DDS.getInstance().getInitStatus() == DDS.INIT_COMPLETE_NOT_FULL) {
                try {
                    if (DDS.getInstance().isAuthSuccess()) {
                        gotoMainActivity();
                        break;
                    } else {
                        showDoAuthDialog();
                    }
                } catch (DDSNotInitCompleteException e) {
                    e.printStackTrace();
                }
                break;
            } else {
                Log.e("wangweiming", "waiting  init complete finish...");
            }
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void gotoMainActivity() {
//        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//        startActivity(intent);
        Intent intent = new Intent(getApplicationContext(), BackgroundService.class);

        startService(intent);
        finish();
    }

    private void showDoAuthDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(LauncherActivity.this);
                builder.setMessage("未授权");
                builder.setPositiveButton("做一次授权", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            DDS.getInstance().doAuth();
                        } catch (DDSNotInitCompleteException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        finish();
                    }
                });

                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                dialog = builder.create();
                dialog.show();
            }
        });
    }

    private BroadcastReceiver authReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), "ddsdemo.intent.action.auth_success")) {
                gotoMainActivity();
            } else if (TextUtils.equals(intent.getAction(), "ddsdemo.intent.action.auth_failed")) {
                showDoAuthDialog();
            }
        }
    };
}
