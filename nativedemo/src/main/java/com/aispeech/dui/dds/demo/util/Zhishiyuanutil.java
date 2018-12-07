package com.aispeech.dui.dds.demo.util;

import android.content.Context;
import android.content.Intent;

public class Zhishiyuanutil {
    public Context mContext;
    public Zhishiyuanutil(Context context)
    {
        this.mContext=context;
    }
    public void dosomething(int cmd)
    {
        Intent intent = new Intent();
        intent.setAction("com.sewise.tv.android.action");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("cmd", cmd);
        mContext.startActivity(intent);
    }
    public void dosomething(int cmd,String data)
    {
        Intent intent = new Intent();
        intent.setAction("com.sewise.tv.android.action");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("cmd", cmd);
        intent.putExtra("data", data);
        mContext.startActivity(intent);
    }
}
