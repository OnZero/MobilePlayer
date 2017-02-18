package com.example.mobileplayer;

import android.content.Context;

/**
 * Created by lenovo on 2017/1/29.
 * 转换像素  适配手机屏幕
 */

public class DensityUtil {
    public static int dip2px(Context context,float dpValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue*scale+0.5f);
    }
    public static int px2dip(Context context,float pxValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue/scale+0.5f);
    }
}
