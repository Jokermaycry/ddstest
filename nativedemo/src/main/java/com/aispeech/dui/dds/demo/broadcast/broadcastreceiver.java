package com.aispeech.dui.dds.demo.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.aispeech.dui.dds.DDS;
import com.aispeech.dui.dds.demo.BackgroundService;
import com.aispeech.dui.dds.demo.DDSService;
import com.aispeech.dui.dds.exceptions.DDSNotInitCompleteException;

public class broadcastreceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {


        if ("send.voice.buffer.ing.tag".equals(intent.getAction())) {
            context.sendBroadcast(new Intent("sayhello"));

            byte[] temp=intent.getByteArrayExtra("buffer");
            //Log.e("wangweiming", ""+temp.length);

            DDS.getInstance().getAgent().feedPcm(temp);




        }


        if ("send.voice.buffer.end.tag".equals(intent.getAction())) {

            byte[] temp=intent.getByteArrayExtra("data");
            Log.e("wangweiming", "说话结束");
            try {
                DDS.getInstance().getAgent().getWakeupEngine().enableWakeup();
            } catch (DDSNotInitCompleteException e) {
                e.printStackTrace();
            }
            DDS.getInstance().getAgent().feedPcm(temp);
        }

        if ("zhishiyuan.intent.action.ASKRECORD".equals(intent.getAction())) {
            Log.e("wangweiming", "ASKRECORD");

//            Intent i =new Intent();
//            i.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
//            i.setAction("zhishiyuan.intent.action.PERMIT");
//            context.sendBroadcast(i);

            Intent i1 = new Intent(context, DDSService.class);
            context.stopService(i1);
            i1.setAction("start_outside");
            context.startService(i1);
        }

        if ("zhishiyuan.intent.action.RECORDFINISHED".equals(intent.getAction())) {
            Log.e("wangweiming", "RECORDFINISHED");
            Intent i = new Intent(context, DDSService.class);
            context.stopService(i);
            i.setAction("start_inside");
            context.startService(i);


        }
        if ("ddsdemo.intent.action.init_complete".equals(intent.getAction())) {
            Log.e("wangweiming", "init_complete");
//            Intent i = new Intent(context, DDSService.class);
//            context.stopService(i);
//            i.setAction("start_outside");
//            context.startService(i);
            try {
                Thread.sleep(2000);
            }
            catch (Exception e){}
            Intent i =new Intent();
            i.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            i.setAction("zhishiyuan.intent.action.PERMIT");
            context.sendBroadcast(i);
        }


    }
}
