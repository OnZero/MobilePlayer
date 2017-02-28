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

import com.romainpiel.titanic.library.Titanic;
import com.romainpiel.titanic.library.TitanicTextView;

import java.util.ArrayList;

/**
 * Created by lenovo on 2017/1/23.
 */

public class VideoFragments extends Fragment implements AdapterView.OnItemClickListener{
    private Context context;
    private View view;
    private ListView lv_video_list;
    private TextView tv_novideo;
    //private ProgressBar pb_loading;
    private Titanic titanic;
    private ArrayList<MediaInfo> mediaInfos;
    private int MSG=0;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==MSG){
                if(mediaInfos!=null&&mediaInfos.size()>0){
                    lv_video_list.setAdapter(new VideoAdapter(context,mediaInfos));
                    tv_novideo.setVisibility(View.GONE);
                }else{
                    tv_novideo.setVisibility(View.VISIBLE);
                }
                //pb_loading.setVisibility(View.GONE);
                titanic.cancel();
                titanic_loading.setVisibility(View.GONE);
            }
        }
    };
    private TitanicTextView titanic_loading;


    public VideoFragments(Context context){
        this.context=context;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.video_list, null);
        mediaInfos = new ArrayList<>();
        findView();
        getVideoList();
        return view;
    }

    private void getVideoList() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                SystemClock.sleep(1000);
                String[] videoInfo={MediaStore.Video.Media.DISPLAY_NAME,
                                    MediaStore.Video.Media.DURATION,
                                    MediaStore.Video.Media.SIZE,
                                    MediaStore.Video.Media.DATA};
                ContentResolver resolver = context.getContentResolver();
                Cursor cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,videoInfo,null,null,null);
                if(cursor!=null&&cursor.getCount()>0){
                    while (cursor.moveToNext()){
                        MediaInfo info = new MediaInfo();
                        info.setName(cursor.getString(0));
                        info.setDuration(cursor.getLong(1));
                        info.setSize(cursor.getLong(2));
                        info.setPath(cursor.getString(3));
                        mediaInfos.add(info);
                    }
                    cursor.close();
                }
                //数据准备完成
                handler.sendEmptyMessage(MSG);
            }
        }.start();
    }

    private void findView() {
        lv_video_list = (ListView) view.findViewById(R.id.lv_video_list);
        tv_novideo = (TextView) view.findViewById(R.id.tv_novideo);
        //pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);
        titanic = new Titanic();
        titanic_loading = (TitanicTextView) view.findViewById(R.id.titanic_loading);
        titanic.start(titanic_loading);
        lv_video_list.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //MediaInfo mediaInfo = mediaInfos.get(position);
        Intent intent=new Intent(context,VideoPlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("videolist",mediaInfos);
        intent.putExtras(bundle);
        intent.putExtra("position",position);
        context.startActivity(intent);

    }

}
