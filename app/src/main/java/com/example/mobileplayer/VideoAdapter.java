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
 * Created by lenovo on 2017/1/23.
 */

public class VideoAdapter extends BaseAdapter {

    private final Context context;
    private ArrayList<MediaInfo> mediaInfos;
    private Utils utils;

    public VideoAdapter(Context context,ArrayList<MediaInfo> mediaInfos){
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
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //View view;
        ViewHodlers viewHodlers;
        if(convertView==null){
            convertView = View.inflate(context,R.layout.video_list_item,null);
            viewHodlers = new ViewHodlers();
            viewHodlers.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHodlers.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHodlers.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            viewHodlers.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
            convertView.setTag(viewHodlers);
        }else{
            viewHodlers = (ViewHodlers) convertView.getTag();
        }

        MediaInfo mediaInfo = mediaInfos.get(position);
        viewHodlers.tv_name.setText(mediaInfo.getName());
        viewHodlers.tv_size.setText(Formatter.formatFileSize(context,mediaInfo.getSize()));
        viewHodlers.tv_time.setText(utils.stringForTime((int) mediaInfo.getDuration()));
        return convertView;

    }
    static class ViewHodlers{
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_time;
        TextView tv_size;
    }
}