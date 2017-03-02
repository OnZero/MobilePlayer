package com.example.mobileplayer;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by lenovo on 2017/1/27.
 */

public class CacheUtils {
    //缓存音乐播放模式
    public static void putPlayMode(Context context,String key,int values){
        SharedPreferences sharedPreferences = context.getSharedPreferences("mobileplayer", context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(key,values).commit();
    }
    //获取音乐播放模式
    public static int getPlayMode(Context context,String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences("mobileplayer", context.MODE_PRIVATE);
        return sharedPreferences.getInt(key,MusicPlayerService.REPEAT_NORMAL);
    }
    //缓存在线视频数据
    public static void putVideoCache(Context context,String key,String videoCache){
        SharedPreferences sharedPreferences = context.getSharedPreferences("mobileplayer", context.MODE_PRIVATE);
        sharedPreferences.edit().putString(key,videoCache).commit();
    }
    //得到已缓存的视频数据
    public static String getVideoCache(Context context,String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences("mobileplayer", context.MODE_PRIVATE);
        return sharedPreferences.getString(key,"");
    }

    //缓存当前所播放的position
    public static void putMusicPositionCache(Context context,String key,int currentPosition){
        SharedPreferences sharedPreferences = context.getSharedPreferences("mobileplayer", context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(key,currentPosition).commit();
    }
    //获取上一次缓存的position
    public static int getMusicPositionCache(Context context,String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences("mobileplayer", context.MODE_PRIVATE);
        return sharedPreferences.getInt(key,0);
    }

    //缓存当前所播放的position
    public static void putShowLyricCache(Context context,String key,boolean isShowLyric){
        SharedPreferences sharedPreferences = context.getSharedPreferences("mobileplayer", context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(key,isShowLyric).commit();
    }
    //获取上一次缓存的position
    public static boolean getShowLyricCache(Context context,String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences("mobileplayer", context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key,false);
    }


}
