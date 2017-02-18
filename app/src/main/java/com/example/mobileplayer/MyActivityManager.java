package com.example.mobileplayer;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.Application;

public class MyActivityManager extends Application {

    private List<Activity> list = new ArrayList<Activity>();
 
    private static MyActivityManager mam;

    private MyActivityManager() {

    }

    public static MyActivityManager getInstance() {
        if (null == mam) {
        	mam = new MyActivityManager();
        }
        return mam;
    }

   
    public void addActivity(Activity activity) {
        list.add(activity);
    }

    public void exit() {
        for (int i=0; i < list.size(); i++) {
        	list.get(i).finish();
        }
        System.exit(0);
    }
    
}