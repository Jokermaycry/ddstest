package com.aispeech.dui.dds.demo.util;

import android.content.Context;
import android.content.Intent;

public class Jiaoyutongutil {

    public Context mContext;
    public String need;
    public Jiaoyutongutil(Context context)
    {
        this.mContext=context;
    }
    public void gotoapp(String input)
    {
//        try {
//            if(input.indexOf("我要搜索") != -1)
//            {
//                need=input.substring(4);
//                System.out.println("需要的字符串"+need);
//            }
//            else if(input.indexOf("我要看") != -1)
//            {
//                need=input.substring(3);
//                System.out.println("需要的字符串"+need);
//            }
//            else
//            {
//                need=input;
//                System.out.println("需要的字符串"+need);
//            }
//        }
//        catch (Exception e){}


        try {
            Intent intent1 = new Intent();
            intent1.setAction("com.lianhelive.search");
            intent1.putExtra("search",input);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent1);
        }
        catch (Exception e) {}

    }
}
