package com.example.mobileplayer;

import android.app.Fragment;
import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.picasso.Picasso;

import org.xutils.x;

import java.util.ArrayList;

/**
 * Created by lenovo on 2017/1/23.
 */

public class NetVideoAdapter extends BaseAdapter {

    private final Context context;
    private ArrayList<MediaInfo> mediaInfos;
    private Fragment netVideoFragment;

    public NetVideoAdapter(Context context, ArrayList<MediaInfo> mediaInfos,Fragment fragment){
        this.context=context;
        this.mediaInfos=mediaInfos;
        this.netVideoFragment = fragment;
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
        //★★★★ Bug 用viewHolder会使图片无法显示或者崩溃
        /*ViewHodlers viewHodlers;
        if(convertView==null){
            convertView = View.inflate(context,R.layout.netvideo_list_item,null);
            viewHodlers = new ViewHodlers();
            viewHodlers.iv_videoimage = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHodlers.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHodlers.tv_desc = (TextView) convertView.findViewById(R.id.tv_desc);
            convertView.setTag(viewHodlers);
        }else{
            viewHodlers = (ViewHodlers) convertView.getTag();
        }

        MediaInfo mediaInfo = mediaInfos.get(position);
        viewHodlers.tv_name.setText(mediaInfo.getName());
        viewHodlers.tv_desc.setText(mediaInfo.getDesc());
        //x.image().bind(viewHodlers.iv_videoimage,mediaInfo.getImageUrl());
        System.out.println("ImageUrl++++++++"+mediaInfo.getImageUrl());
        /*Glide.with(context).load(mediaInfo.getImageUrl())
                .placeholder(R.drawable.video_default_icon)
                .error(R.drawable.video_default_icon)
                .into(viewHodlers.iv_videoimage);
        Picasso.with(context).load(mediaInfo.getImageUrl())
                .placeholder(R.drawable.video_default_icon)
                .error(R.drawable.video_default_icon)
                .into(viewHodlers.iv_videoimage);*/
        View view;
        if(convertView==null){
            view = View.inflate(context,R.layout.netvideo_list_item,null);
        }else{
            view = convertView;
        }
        MediaInfo mediaInfo = mediaInfos.get(position);
        ImageView iv_videoimage = (ImageView) view.findViewById(R.id.iv_videoimage);
        TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
        TextView tv_desc = (TextView) view.findViewById(R.id.tv_desc);
        //x.image().bind(iv_videoimage,mediaInfo.getImageUrl());
        tv_name.setText(mediaInfo.getName());
        tv_desc.setText(mediaInfo.getDesc());
        Picasso.with(context).load(mediaInfo.getImageUrl()).placeholder(R.drawable.video_default_icon).error(R.drawable.video_default_icon).into(iv_videoimage);
        return view;

    }
    /*static class ViewHodlers{
        ImageView iv_videoimage;
        TextView tv_name;
        TextView tv_desc;
    }*/
}