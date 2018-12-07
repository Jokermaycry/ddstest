package com.aispeech.dui.dds.demo;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aispeech.dui.dds.DDS;
import com.aispeech.dui.dds.DDSConfig;
import com.aispeech.dui.dds.agent.ASREngine;
import com.aispeech.dui.dds.agent.MessageObserver;
import com.aispeech.dui.dds.demo.bean.MessageBean;
import com.aispeech.dui.dds.demo.bean.slotsbean;
import com.aispeech.dui.dds.demo.recorder.AudioFileFunc;
import com.aispeech.dui.dds.demo.ui.adapter.DialogAdapter;
import com.aispeech.dui.dds.demo.recorder.AudioRecordFunc;
import com.aispeech.dui.dds.demo.util.Jiaoyutongutil;
import com.aispeech.dui.dds.demo.util.Zhishiyuanutil;
import com.aispeech.dui.dds.demo.widget.InputField;
import com.aispeech.dui.dds.exceptions.DDSNotInitCompleteException;
import com.aispeech.dui.dds.update.DDSUpdateListener;
import com.aispeech.dui.dds.utils.PrefUtil;
import com.aispeech.dui.dsk.duiwidget.CommandObserver;
import com.alibaba.fastjson.JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service implements InputField.Listener {
    LinearLayout mFloatLayout;
    WindowManager.LayoutParams wmParams;
    WindowManager mWindowManager;
    ImageButton mFloatView;
    private boolean isRecord = false;// 设置正在录制的状态

    private int bufferSizeInBytes = 0;
    TextView tv;
    Zhishiyuanutil z;
    private InputField inputField;
    private RecyclerView mRecyclerView;
    private DialogAdapter mDialogAdapter;  // 各种UI控件的实现在DialogAdapter类里
    private LinkedList<MessageBean> mMessageList;
    private Handler mHandler = new Handler();
    private DuiMessageObserver mMessageObserver;
    MyReceiver receiver;
    private boolean isActivityShowing = false;
    private Dialog dialog;
    public static final String TAG = "wangweiminghhh";
    String value;
    String rawvalue;
    String input;
    String text;
    byte[] data=null;
    String result;
     Handler handler=new Handler();


    @Override
    public void onCreate() {
        super.onCreate();
               createFloatView();
               onstart();
               onresume();
                initaas();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;
        onresume();


        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 语音引擎
     */
    private  void  initaas()
    {

        ASREngine asrEngine = DDS.getInstance().getAgent().getASREngine();
       final AudioRecordFunc ar=new AudioRecordFunc();
        try {
            asrEngine.startListening(new ASREngine.Callback() {
                @Override
                public void beginningOfSpeech() {

                }

                @Override
                public void bufferReceived(byte[] buffer) {

                    Log.e(TAG, "用户说话的音频数据");
//                    Log.e(TAG, "用户说话的音频数据"+String.valueOf(buffer.length));

//                    try {
//                        Log.d(TAG, "onMicClicked" );
//
//                        DDS.getInstance().getAgent().avatarPress();
//                    } catch (DDSNotInitCompleteException e) {
//                        e.printStackTrace();
//                    }
                }

                @Override
                public void endOfSpeech() {

                    Log.e(TAG, "停止了对话");
//                    try {
//                        Log.d(TAG, "onMicClicked" );
//
//                        DDS.getInstance().getAgent().avatarRelease();
//                    } catch (DDSNotInitCompleteException e) {
//                        e.printStackTrace();
//                    }
                }

                @Override
                public void partialResults(String results) {

                }

                @Override
                public void finalResults(String results) {
                    result=results;
                    JSONObject jsonData = null;
                    try {
                        jsonData = new JSONObject(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                     text = jsonData.optString("text");
                    new Thread(){
                        public void run(){

                            handler.post(runnableUi);
                        }
                    }.start();
                }

                @Override
                public void error(String error) {

                }

                @Override
                public void rmsChanged(float rmsdB) {

                }
            });
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
    }
    Runnable   runnableUi=new  Runnable(){
        @Override
        public void run() {

            mRecyclerView.setVisibility(View.VISIBLE);
            AlphaAnimation alp = new AlphaAnimation(1.0f, 0.0f);
            alp.setDuration(10000);
            mRecyclerView.setAnimation(alp);
            alp.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    mRecyclerView.setVisibility(View.INVISIBLE);
                }
            });

            //更新界面
//            tv.setText(text);
//            tv.setVisibility(View.VISIBLE);
//            AlphaAnimation alp = new AlphaAnimation(1.0f, 0.0f);
//            alp.setDuration(3000);
//            tv.setAnimation(alp);
//            alp.setAnimationListener(new Animation.AnimationListener() {
//                public void onAnimationStart(Animation animation) {
//                }
//
//                public void onAnimationRepeat(Animation animation) {
//                }
//
//                public void onAnimationEnd(Animation animation) {
//                    tv.setVisibility(View.INVISIBLE);
//                }
//            });
            //mWindowManager.updateViewLayout(tv,wmParams);
        }

    };


    private void createFloatView()
      {
                 wmParams = new WindowManager.LayoutParams();

                 mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);

                 wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                 wmParams.format = PixelFormat.RGBA_8888;

                 wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

                 wmParams.gravity = Gravity.CENTER | Gravity.BOTTOM;

                 wmParams.x = 0;
                 wmParams.y = 0;

                 wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                 wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

                 LayoutInflater inflater = LayoutInflater.from(getApplication());
                 mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_layout, null);

                 mWindowManager.addView(mFloatLayout, wmParams);


                 mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                         View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                                 .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));


                  IntentFilter filter = new IntentFilter();
                  filter.addAction("ddsdemo.intent.action.init_complete");
                  receiver = new MyReceiver();
                  registerReceiver(receiver, filter);

//          ActionBar actionBar = getSupportActionBar();
//          actionBar.setDisplayHomeAsUpEnabled(true);
//          ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
//                  ActionBar.LayoutParams.MATCH_PARENT,
//                  ActionBar.LayoutParams.MATCH_PARENT,
//                  Gravity.CENTER);
          LayoutInflater inflater1 = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
          View titleView = inflater1.inflate(R.layout.action_bar_title, null);
//          actionBar.setCustomView(titleView, lp);
//
//          actionBar.setDisplayShowHomeEnabled(false);
//          actionBar.setDisplayShowTitleEnabled(false);
//          actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//          actionBar.setDisplayShowCustomEnabled(true);
           tv=(TextView)mFloatLayout.findViewById(R.id.tv);
          inputField = (InputField) mFloatLayout.findViewById(R.id.input_field);
          inputField.setListener(this);

          mRecyclerView = (RecyclerView) mFloatLayout.findViewById(R.id.recycler_view);
          mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

          mMessageList = new LinkedList<>();

          mDialogAdapter = new DialogAdapter(mMessageList);
          mRecyclerView.setAdapter(mDialogAdapter);

          mMessageObserver = new DuiMessageObserver();
//        initregister();
          DDS.getInstance().getAgent().subscribe(new String[]{"navi.poi.search","test.command.one","navi.route","xiaoqin.search.vdsou","xiaoqin.search.allvideo","xiaoqin.search.hot","qinjian.control.closeAirconditioner","qinjian.control.openlcok", "qinjian.control.closelock","qinjian.control.openIntercalation","qinjian.control.closewindow","qinjian.control.openProjector","qinjian.control.closeIntercalation"}, commandObserver);

//          Intent intent = new Intent(getApplicationContext(), BackgroundService.class);
//          startService(intent);



     }
    private CommandObserver commandObserver = new CommandObserver() {
        @Override
        public void onCall(final String command, final String data) {
            Log.e("wangweimingcommandr",command);
            Log.e("wangweimingdata",data);
            try {
                //获取input
                JSONObject jsonData = new JSONObject(data);
                String nlu = jsonData.optString("nlu");
                //获取semantics
                JSONObject jsonData1 = new JSONObject(nlu);
                String semantics=jsonData1.optString("semantics");
                System.out.println("wangweiming_semantics" + semantics);
                //获取request
                JSONObject jsonData2 = new JSONObject(semantics);
                String request=jsonData2.optString("request");
                System.out.println("wangweiming_request" + request);

                //获取slots
                JSONObject jsonData3 = new JSONObject(request);
                String slots=jsonData3.optString("slots");
                System.out.println("wangweiming_slots" + slots);

                //获取rawvalue
                List<slotsbean> temp;
                temp = JSON.parseArray(slots, slotsbean.class);

//                for(int i=0;i<temp.size();i++)
//                {
//
//                     String name=temp.get(i).getName();
//                     if(name.equals("应用软件")){
//                         value=temp.get(i).getValue();
//                     }
//
//                }
                value=temp.get(0).getValue();
                rawvalue=temp.get(0).getRawvalue();
                Log.e("frank",value);
                Log.e("frank",rawvalue);
                input=value;


            }
            catch (Exception e){}

            if(command.equals("qinjian.control.closeAirconditioner"))
            {
                Jiaoyutongutil jyt = new Jiaoyutongutil(getApplicationContext());
                jyt.gotoapp(input);
            }
            else if(command.equals("xiaoqin.search.vdsou"))
            {
                Intent intent = new Intent();
                ComponentName comp=new ComponentName("com.zhongqin.wx.xiaoqinvideo","com.zhongqin.wx.xiaoqinvideo.main.MainActivity");
                intent.setComponent(comp);
                intent.setAction("xiaoqin.intent.action.VIDEO_SEARCH");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("command", command);
                intent.putExtra("data", value);
                startActivity(intent);
            }
            else if(command.equals("xiaoqin.search.allvideo"))
            {
                Intent intent = new Intent();
                ComponentName comp=new ComponentName("com.zhongqin.wx.xiaoqinvideo","com.zhongqin.wx.xiaoqinvideo.main.MainActivity");
                intent.setComponent(comp);
                intent.setAction("xiaoqin.intent.action.VIDEO_SEARCH");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("command", command);
                intent.putExtra("data", value);
                startActivity(intent);
            }
            else if(command.equals("xiaoqin.search.hot"))
            {
                Intent intent = new Intent();
                ComponentName comp=new ComponentName("com.zhongqin.wx.xiaoqinvideo","com.zhongqin.wx.xiaoqinvideo.main.MainActivity");
                intent.setComponent(comp);
                intent.setAction("xiaoqin.intent.action.VIDEO_SEARCH");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("command", command);
                intent.putExtra("data", value);
                startActivity(intent);
            }
            //
            else if(command.equals("qinjian.control.openProjector"))//分享直播到微信orqq
            {
                    z=new Zhishiyuanutil(getApplicationContext());
                    z.dosomething(0x115,rawvalue);
            }
            else if(command.equals("qinjian.control.closeIntercalation"))//切换
            {
                                z=new Zhishiyuanutil(getApplicationContext());
                    z.dosomething(0x003,rawvalue);
            }
            else if(command.equals("qinjian.control.openIntercalation")) //搜索课程
            {
                z=new Zhishiyuanutil(getApplicationContext());
                    z.dosomething(0x002,rawvalue);
            }
            else if(command.equals("qinjian.control.closelock"))//退出
            {
                z=new Zhishiyuanutil(getApplicationContext());
                    z.dosomething(0x007);

            }
            else if(command.equals("qinjian.control.openlcok"))//动作
            {
                z=new Zhishiyuanutil(getApplicationContext());
                    z.dosomething(0x003,"我的录制");
            }
            else if(command.equals("qinjian.control.closewindow"))//分享到微信orqq
            {
                z=new Zhishiyuanutil(getApplicationContext());
                    z.dosomething(0x010,rawvalue);
            }

        }
    };
    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String name = intent.getAction();
            if (name.equals("ddsdemo.intent.action.init_complete")) {
                inputField.getAvatarView().go();
               // sendHiMessage();
                enableWakeup();
            }
            if (name.equals("sayhello")) {
                inputField.getAvatarView().go();
                //sendHiMessage();
                enableWakeup();

            }
        }
    }
    public void onstart(){
        isActivityShowing = true;
        try {
            DDS.getInstance().getUpdater().update(ddsUpdateListener);
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
        DDS.getInstance().getAgent().subscribe("sys.resource.updated", resourceUpdatedMessageObserver);
    }
    public void onresume()
    {
        DDS.getInstance().getAgent().subscribe(new String[]{
                "sys.resource.updated",
                "sys.dialog.state",
                "context.output.text",
                "context.input.text",
                "avatar.silence",
                "avatar.listening",
                "avatar.understanding",
                "avatar.speaking",
                "context.widget.content",
                "context.widget.list",
                "context.widget.web",
                "context.widget.media"}, mMessageObserver);

        inputField.getAvatarView().go();
       // sendHiMessage();
        enableWakeup();
        
    }

//    @Override
//    protected void onStart() {
//        isActivityShowing = true;
//        try {
//            DDS.getInstance().getUpdater().update(ddsUpdateListener);
//        } catch (DDSNotInitCompleteException e) {
//            e.printStackTrace();
//        }
//        DDS.getInstance().getAgent().subscribe("sys.resource.updated", resourceUpdatedMessageObserver);
//        super.onStart();
//    }
//    @Override
//    protected void onStop() {
//        AILog.d(TAG, "onStop() " + this.hashCode());
//        isActivityShowing = false;
//        if (dialog != null) {
//            dialog.dismiss();
//        }
//        DDS.getInstance().getAgent().unSubscribe(resourceUpdatedMessageObserver);
//        super.onStop();
//    }
//
//
//    @Override
//    protected void onResume() {
//        DDS.getInstance().getAgent().subscribe(new String[]{
//                "sys.resource.updated",
//                "sys.dialog.state",
//                "context.output.text",
//                "context.input.text",
//                "avatar.silence",
//                "avatar.listening",
//                "avatar.understanding",
//                "avatar.speaking",
//                "context.widget.content",
//                "context.widget.list",
//                "context.widget.web",
//                "context.widget.media"}, mMessageObserver);
//
//        inputField.getAvatarView().go();
//        sendHiMessage();
//        enableWakeup();
//
//        super.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        DDS.getInstance().getAgent().unSubscribe(mMessageObserver);
//
//        inputField.toIdle();
//        disableWakeup();
//        super.onPause();
//    }

    @Override
         public void onDestroy()
        {
             // TODO Auto-generated method stub
           super.onDestroy();
            unregisterReceiver(receiver);
            inputField.destroy();
            stopService();
              if(mFloatLayout != null)
                  {
                       mWindowManager.removeView(mFloatLayout);
                  }
            }
    private void stopService() {
        Intent intent = new Intent(BackgroundService.this, DDSService.class);
        stopService(intent);
    }

    void enableWakeup() {
        try {
            DDS.getInstance().getAgent().getWakeupEngine().enableWakeup();
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
    }

    void disableWakeup() {
        try {
            DDS.getInstance().getAgent().stopDialog();
            DDS.getInstance().getAgent().getWakeupEngine().disableWakeup();
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
    }

    public void sendHiMessage() {
        String[] wakeupWords = new String[0];
        String minorWakeupWord = null;
        try {
            wakeupWords = DDS.getInstance().getAgent().getWakeupEngine().getWakeupWords();
            minorWakeupWord = DDS.getInstance().getAgent().getWakeupEngine().getMinorWakeupWord();
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
        String hiStr = "";
        if (wakeupWords != null && minorWakeupWord != null) {
            hiStr = getString(R.string.hi_str2, wakeupWords[0], minorWakeupWord);
        } else if (wakeupWords != null && wakeupWords.length == 2) {
            hiStr = getString(R.string.hi_str2, wakeupWords[0], wakeupWords[1]);
        } else if (wakeupWords != null && wakeupWords.length > 0) {
            hiStr = getString(R.string.hi_str, wakeupWords[0]);
        }
        if (!TextUtils.isEmpty(hiStr)) {
            MessageBean bean = new MessageBean();
            bean.setText(hiStr);
            bean.setType(MessageBean.TYPE_OUTPUT);
            mMessageList.add(bean);
            mDialogAdapter.notifyItemInserted(mMessageList.size());
            mRecyclerView.smoothScrollToPosition(mMessageList.size());
        }
    }
    public void notifyItemInserted() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mMessageList.size()>=10){
                    mMessageList.poll();
                }
                mDialogAdapter.notifyDataSetChanged();
                mRecyclerView.smoothScrollToPosition(mMessageList.size());
            }
        });
    }
    @Override
    public boolean onMicClicked() {
        try {
            Log.d(TAG, "onMicClicked" );

            DDS.getInstance().getAgent().avatarClick();
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
        return false;
    }

    class DuiMessageObserver implements MessageObserver {

        private boolean isFirstVar = true;
        private boolean hasvar = false;
        @Override
        public void onMessage(String message, String data) {
            Log.d(TAG, "message : " + message + " data : " + data);
            MessageBean bean = null;
            switch (message) {
                case "context.output.text":
                    bean = new MessageBean();
                    String txt = "";
                    try {
                        JSONObject jo = new JSONObject(data);
                        txt = jo.optString("text", "");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    bean.setText(txt);
//                    bean.setType(MessageBean.TYPE_OUTPUT);
//                    mMessageList.add(bean);
//                    notifyItemInserted();
                    break;
                case "context.input.text":
                    mMessageList.clear();
                    bean = new MessageBean();

                    try {
                        JSONObject jo = new JSONObject(data);
                        if (jo.has("var")) {
                            String var = jo.optString("var", "");
                            if (isFirstVar) {
                                isFirstVar = false;
                                hasvar = true;
                                bean.setText(var);
                                bean.setType(MessageBean.TYPE_INPUT);
                                mMessageList.add(bean);
                                notifyItemInserted();
                            } else {
                                mMessageList.pollLast();
                                bean.setText(var);
                                bean.setType(MessageBean.TYPE_INPUT);
                                mMessageList.add(bean);
                                notifyItemInserted();
                            }
                        }
                        if (jo.has("text")) {
                            if(hasvar) {
                                mMessageList.pollLast();
                                hasvar = false;
                                isFirstVar = true;
                            }
                            String text = jo.optString("text", "");
                            bean.setText(text);
                            bean.setType(MessageBean.TYPE_INPUT);
                            mMessageList.add(bean);
                            notifyItemInserted();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "context.widget.content":
                    bean = new MessageBean();
                    try {
                        JSONObject jo = new JSONObject(data);
                        String title = jo.optString("title", "");
                        String subTitle = jo.optString("subTitle", "");
                        String imgUrl = jo.optString("imageUrl", "");
                        bean.setTitle(title);
                        bean.setSubTitle(subTitle);
                        bean.setImgUrl(imgUrl);
                        bean.setType(MessageBean.TYPE_WIDGET_CONTENT);
                        mMessageList.add(bean);
                        notifyItemInserted();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "context.widget.list":
                    bean = new MessageBean();
                    try {
                        JSONObject jo = new JSONObject(data);
                        JSONArray array = jo.optJSONArray("content");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.optJSONObject(i);
                            String title = object.optString("title", "");
                            String subTitle = object.optString("subTitle", "");
                            MessageBean b = new MessageBean();
                            b.setTitle(title);
                            b.setSubTitle(subTitle);
                            bean.addMessageBean(b);
                        }
                        int currentPage = jo.optInt("currentPage");
                        bean.setCurrentPage(currentPage);
                        bean.setType(MessageBean.TYPE_WIDGET_LIST);
                        mMessageList.add(bean);
                        notifyItemInserted();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "context.widget.web":
                    bean = new MessageBean();
                    try {
                        JSONObject jo = new JSONObject(data);
                        String url = jo.optString("url");
                        bean.setUrl(url);
                        bean.setType(MessageBean.TYPE_WIDGET_WEB);
                        mMessageList.add(bean);
                        notifyItemInserted();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "avatar.silence":
                    inputField.toIdle();
                    initaas();
                    break;
                case "avatar.listening":
                    inputField.toListen();
                    break;
                case "avatar.understanding":
                    inputField.toRecognize();
                    break;
                case "avatar.speaking":
                    inputField.toSpeak();
//                    initaas();
                    break;
                case "sys.dialog.state":
                    DialogAdapter.mState = data;
                    break;
                case "sys.resource.updated":
                    try {
                        DDS.getInstance().getUpdater().update(ddsUpdateListener);
                    } catch (DDSNotInitCompleteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }




    private DDSUpdateListener ddsUpdateListener = new DDSUpdateListener() {
        @Override
        public void onUpdateFound(String detail) {
            final String str = detail;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "UPDATE finished : " );
                }
            });

            try {
                DDS.getInstance().getAgent().getTTSEngine().speak("发现新版本,正在为您更新", 1);
            } catch (DDSNotInitCompleteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpdateFinish() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //showUpdateFinishedDialog();
                    Log.e(TAG, "UPDATE finished : " );
                }
            });

            try {
                DDS.getInstance().getAgent().getTTSEngine().speak("更新成功", 1);
            } catch (DDSNotInitCompleteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDownloadProgress(float progress) {
            Log.d(TAG, "onDownloadProgress :" + progress);
        }

        @Override
        public void onError(int what, String error) {
            String productId = PrefUtil.getString(getApplicationContext(), DDSConfig.K_PRODUCT_ID);
            if (what == 70319) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "UPDATE ing : " );
                    }
                });

            } else {
                Log.e(TAG, "UPDATE ERROR : " + error);
            }
        }

        @Override
        public void onUpgrade(String version) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //showApkUpdateDialog();
                }
            });

        }
    };

    protected void showNewVersionDialog(final String info) {
        if (!isActivityShowing) {
            return;
        }
        if (null != dialog) {
            dialog.dismiss();
            dialog = null;
        }
        dialog = new ProgressDialog(this);
        dialog.setCancelable(true);
        dialog.setTitle("发现新的更新资源, 正在为您更新...");
        ((ProgressDialog) dialog).setMessage(info);
        ((ProgressDialog) dialog).setProgress(0);
        dialog.show();
    }

    protected void showProductNeedUpdateDialog() {
        if (!isActivityShowing) {
            return;
        }
        if (null != dialog) {
            dialog.dismiss();
            dialog = null;
        }
        dialog = new AlertDialog.Builder(this).setCancelable(false).setTitle(R.string
                .update_product_title)
                .setMessage(R.string.update_product_message).setPositiveButton(R.string.update_product_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton(R.string.update_product_cancel, new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                        System.exit(0);
                    }
                }).create();
        dialog.show();
    }

    protected void showApkUpdateDialog() {
        if (!isActivityShowing) {
            return;
        }
        if (null != dialog) {
            dialog.dismiss();
            dialog = null;
        }
        dialog = new AlertDialog.Builder(this).setCancelable(false).setTitle(R.string
                .update_sdk_title)
                .setMessage(R.string.update_sdk_message).setPositiveButton(R.string.update_sdk_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton(R.string.update_sdk_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                        System.exit(0);
                    }
                }).create();
        dialog.show();
    }

    protected void showUpdateFinishedDialog() {
        if (!isActivityShowing) {
            return;
        }
        if (null != dialog) {
            dialog.dismiss();
            dialog = null;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("资源加载成功");

        dialog = builder.create();
        dialog.show();

        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                dialog.dismiss();
                t.cancel();
            }
        }, 2000);
    }

    private MessageObserver resourceUpdatedMessageObserver = new MessageObserver() {
        @Override
        public void onMessage(String message, String data) {
            try {
                DDS.getInstance().getUpdater().update(ddsUpdateListener);
            } catch (DDSNotInitCompleteException e) {
                e.printStackTrace();
            }
        }
    };
}
