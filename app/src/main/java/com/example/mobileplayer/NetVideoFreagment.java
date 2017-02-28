package com.example.mobileplayer;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;

/**
 * Created by lenovo on 2017/2/10.
 */

public class NetVideoFreagment extends Fragment implements AdapterView.OnItemClickListener {
    private Context context;
    private View view;
    private ListView lv_netvideo_list;
    private TextView mTv_noNet;
    private ProgressBar pb_loading;
    private ArrayList<MediaInfo> mediaInfos;
    private NetVideoAdapter netVideoAdapter;

    public NetVideoFreagment(Context context){
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.netvideo_list, null);
        //x.view().inject(NetVideoFreagment.this,view);
        findView();
        String saveVideoCacheJson = CacheUtils.getVideoCache(context,Constants.NET_URL);
        if(!TextUtils.isEmpty(saveVideoCacheJson)){
            processData(saveVideoCacheJson);
        }
        getMusicList();
        return view;
    }

    private void findView() {
        lv_netvideo_list = (ListView) view.findViewById(R.id.lv_netvideo_list);
        mTv_noNet = (TextView) view.findViewById(R.id.tv_nonet);
        pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);
        lv_netvideo_list.setOnItemClickListener(this);
    }

    //联网请求
    private void getMusicList() {
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            //联网成功
            @Override
            public void onSuccess(String result) {
                CacheUtils.putVideoCache(context,Constants.NET_URL,result);
                processData(result);
            }
            //联网失败
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                showData();
                Toast.makeText(context,"联网失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }
            //联网完成
            @Override
            public void onFinished() {

            }
        });
    }

    private void processData(String result) {
        mediaInfos = parseJson(result);
        //设置适配器
        showData();
    }

    private void showData(){
        if(mediaInfos !=null&&mediaInfos.size()>0){
            mTv_noNet.setVisibility(View.GONE);
            netVideoAdapter = new NetVideoAdapter(context,mediaInfos,NetVideoFreagment.this);
            lv_netvideo_list.setAdapter(netVideoAdapter);
        }else{
            mTv_noNet.setVisibility(View.VISIBLE);
        }
        pb_loading.setVisibility(View.GONE);
    }

    //解析Json
    private ArrayList<MediaInfo> parseJson(String result) {
        ArrayList<MediaInfo> mediaitems = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(result);
            //获得json数组
            JSONArray jsonArray = jsonObject.optJSONArray("trailers");
            if(jsonArray!=null && jsonArray.length()>0){
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObjectItems = (JSONObject) jsonArray.get(i);
                    if(jsonObjectItems !=null){
                        MediaInfo mediaInfo = new MediaInfo();
                        String movieName = jsonObjectItems.optString("movieName");
                        mediaInfo.setName(movieName);
                        String videoTitle = jsonObjectItems.optString("videoTitle");
                        mediaInfo.setDesc(videoTitle);
                        String imageUrl = jsonObjectItems.optString("coverImg");
                        mediaInfo.setImageUrl(imageUrl);
                        String hightUrl = jsonObjectItems.optString("hightUrl");
                        mediaInfo.setPath(hightUrl);
                        //添加数据到集合中
                        mediaitems.add(mediaInfo);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mediaitems;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent=new Intent(context,VideoPlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("videolist",mediaInfos);
        intent.putExtras(bundle);
        intent.putExtra("position",position);
        context.startActivity(intent);
    }
}
