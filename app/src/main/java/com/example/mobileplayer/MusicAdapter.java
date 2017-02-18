package com.example.mobileplayer;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by lenovo on 2017/1/24.
 */

public class MusicAdapter extends BaseAdapter {
    private final Context context;
    private ArrayList<MusicInfo> mediaInfos;
    private Utils utils;

    public MusicAdapter(Context context,ArrayList<MusicInfo> mediaInfos){
        this.context=context;
        this.mediaInfos=mediaInfos;
        utils = new Utils();
    }
    @Override
    public int getCount() {
        return mediaInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Hodler hodler;
        if(convertView == null){
            convertView = View.inflate(context,R.layout.audio_list_item,null);
            hodler = new Hodler();
            hodler.iv_audio_icon = (ImageView) convertView.findViewById(R.id.iv_audio_icon);
            hodler.tv_audio_title = (TextView) convertView.findViewById(R.id.tv_audio_name);
            hodler.tv_audio_artist = (TextView) convertView.findViewById(R.id.tv_audio_artist);
            hodler.tv_audio_size = (TextView) convertView.findViewById(R.id.tv_audio_size);
            hodler.tv_audio_time = (TextView) convertView.findViewById(R.id.tv_audio_time);
            convertView.setTag(hodler);
        }else{
            hodler = (Hodler) convertView.getTag();
        }
        MusicInfo musicInfo = mediaInfos.get(position);
        hodler.tv_audio_title.setText(musicInfo.getTitle());
        hodler.tv_audio_artist.setText(musicInfo.getArtist());
        hodler.tv_audio_size.setText(Formatter.formatFileSize(context,musicInfo.getSize()));
        hodler.tv_audio_time.setText(utils.stringForTime((int) musicInfo.getDuration()));
        return convertView;
    }
    static class Hodler{
        ImageView iv_audio_icon;
        TextView tv_audio_title;
        TextView tv_audio_artist;
        TextView tv_audio_time;
        TextView tv_audio_size;
    }
}
