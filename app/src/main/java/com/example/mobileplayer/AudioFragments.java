package com.example.mobileplayer;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by lenovo on 2017/1/23.
 */

public class AudioFragments extends Fragment implements AdapterView.OnItemClickListener{
    private Context context;
    private ArrayList<MusicInfo> mediaInfos;
    private View view;
    private int AUDIOMSG=1;
    private ListView lv_audio_list;
    private TextView tv_audio_novideo;
    private ProgressBar pb_audio_loading;

    public AudioFragments(Context context){
        this.context=context;
    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==AUDIOMSG){
                if(mediaInfos!=null&&mediaInfos.size()>0){
                    lv_audio_list.setAdapter(new MusicAdapter(context,mediaInfos));
                    tv_audio_novideo.setVisibility(View.GONE);
                }else{
                    tv_audio_novideo.setVisibility(View.VISIBLE);
                }
                pb_audio_loading.setVisibility(View.GONE);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.audio_list, null);
        mediaInfos = new ArrayList<>();
        findView();
        getMusicList();
        return view;
    }

    private void getMusicList() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                //SystemClock.sleep(2000);
                String[] musicInfo={MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.DATA};
                ContentResolver resolver = context.getContentResolver();
                Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,musicInfo,null,null,null);
                if(cursor!=null&&cursor.getCount()>0){
                    while (cursor.moveToNext()){
                        MusicInfo info = new MusicInfo();
                        info.setTitle(cursor.getString(0));
                        info.setArtist(cursor.getString(1));
                        info.setSize(cursor.getLong(2));
                        info.setDuration(cursor.getLong(3));
                        info.setPath(cursor.getString(4));
                        mediaInfos.add(info);
                    }
                    cursor.close();
                }
                //数据准备完成
                handler.sendEmptyMessage(AUDIOMSG);
            }
        }.start();
    }

    private void findView() {
        lv_audio_list = (ListView) view.findViewById(R.id.lv_audio_list);
        tv_audio_novideo = (TextView) view.findViewById(R.id.tv_audio_novideo);
        pb_audio_loading = (ProgressBar) view.findViewById(R.id.pb_audio_loading);
        lv_audio_list.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent=new Intent(context,MusicPlayerActivity.class);
        //Bundle bundle = new Bundle();
        //bundle.putSerializable("audiolist",mediaInfos);
        //intent.putExtras(bundle);
        intent.putExtra("position",position);
        context.startActivity(intent);
    }
}
