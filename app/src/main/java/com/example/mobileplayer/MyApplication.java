package com.example.mobileplayer;

import android.app.Application;

import org.xutils.x;

/**
 * Created by lenovo on 2017/2/10.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG);
    }
}
