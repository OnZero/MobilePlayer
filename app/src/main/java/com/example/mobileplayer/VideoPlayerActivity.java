package com.example.mobileplayer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.VideoView;


public class VideoPlayerActivity extends Activity implements MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,View.OnClickListener ,SeekBar.OnSeekBarChangeListener,MediaPlayer.OnInfoListener{

    private VideoView videoview;
    private Uri uri;//播放地址
    private LinearLayout ll_video_top;
    private TextView tv_name,tv_netspeed;
    private ImageView iv_dianchi;
    private TextView tv_systemtime;
    private LinearLayout ll_video_controler,ll_buffer;
    private TextView tv_currentposition;
    private SeekBar sb_progress;
    private TextView tv_endtime;
    private ImageButton ib_video_play;
    private ImageButton ib_video_pause;
    private final int PROGRESS=0; //区分handler
    private Utils utils;
    private MyReceiver myReceiver;
    private ArrayList<MediaInfo> mediaItems; //传递过来的视频列表
    private int position; //位置
    private ImageButton ib_video_rewin;
    private ImageButton ib_video_recon;
    private GestureDetector detector;
    private boolean isShowController=false;
    private final int HIDECONTROLLER=1;
    private final int SHOWNETSPEED = 2;
    private AudioManager am;
    private int currentVoice;
    private int maxVoice;
    private SeekBar sb_voice;
    private float startX;
    private float startY;
    private float touchRang;
    private int tVol;
    private int screenwidth;
    private int screenheight;
    private LinearLayout ll_voice;
    /**
     * 播放位置*/
    private int currentPosition;
    private float endX;
    private float endY;
    //是否是网络视频
    private boolean isNetUri;

    private TextView tv_loadingspeed;
    private LinearLayout ll_loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Vitamio.initialize(this);
        setContentView(R.layout.activity_video_player);
        findView();//初始化控件
        initData();
        videoview.setOnPreparedListener(this);
        videoview.setOnErrorListener(this);
        videoview.setOnCompletionListener(this);
        videoview.setOnInfoListener(this);
        getData();
        setData();
    }

    private void setData() {
        if(mediaItems!=null && mediaItems.size()>0){
            MediaInfo mediaInfo = mediaItems.get(position);
            tv_name.setText(mediaInfo.getName());
            isNetUri = utils.isNetUri(mediaInfo.getPath());
            videoview.setVideoPath(mediaInfo.getPath());
        }else if(uri !=null){
            tv_name.setText(uri.toString());
            isNetUri = utils.isNetUri(uri.toString());
            videoview.setVideoURI(uri);
        }else{
            Toast.makeText(getApplicationContext(),"VideoPath Is Null",Toast.LENGTH_SHORT).show();
        }
        setButtonState();
    }

    private void getData() {
        uri = getIntent().getData();
        mediaItems = (ArrayList<MediaInfo>) getIntent().getSerializableExtra("videolist");
        position = getIntent().getIntExtra("position", 0);
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        //home
        videoview.seekTo(currentPosition);
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoview.pause();
        currentPosition = (int) videoview.getCurrentPosition();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(null);
        //先释放父类资源  可防止子类调用父类出现空指针错误
        if(myReceiver!=null){
            unregisterReceiver(myReceiver);
            myReceiver=null;
        }
        super.onDestroy();
    }

    private void initData() {
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);//监听电量变化
        registerReceiver(myReceiver,intentFilter);
        //获取音量
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVoice = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVoice = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        sb_voice.setMax(maxVoice);
        sb_voice.setProgress(currentVoice);
    }

    //视频监听卡
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what){
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                ll_buffer.setVisibility(View.VISIBLE);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                ll_buffer.setVisibility(View.GONE);
                break;
        }
        return true;
    }

    //获取电量
    class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level",0);
            setBattery(level);
        }
    }

    //设置电量显示
    private void setBattery(int level) {
        if(level<=0){
            iv_dianchi.setImageResource(R.drawable.ic_battery_0);
        }else if(level<=10){
            iv_dianchi.setImageResource(R.drawable.ic_battery_10);
        }else if(level<=20){
            iv_dianchi.setImageResource(R.drawable.ic_battery_20);
        }else if(level<=40){
            iv_dianchi.setImageResource(R.drawable.ic_battery_40);
        }else if(level<=60){
            iv_dianchi.setImageResource(R.drawable.ic_battery_60);
        }else if(level<=80){
            iv_dianchi.setImageResource(R.drawable.ic_battery_80);
        }else if(level<=100){
            iv_dianchi.setImageResource(R.drawable.ic_battery_100);
        }else{
            iv_dianchi.setImageResource(R.drawable.ic_battery_100);
        }
    }

    private void findView() {
        MyActivityManager.getInstance().addActivity(this);
        videoview = (VideoView) findViewById(R.id.surface_view);
        ll_video_top = (LinearLayout) findViewById(R.id.ll_video_top);
        ll_buffer = (LinearLayout) findViewById(R.id.ll_buffer);
        tv_name = (TextView) findViewById(R.id.tv_vtm_videoname);
        iv_dianchi = (ImageView) findViewById(R.id.iv_dianchi);
        tv_netspeed = (TextView) findViewById(R.id.tv_netspeed);
        tv_systemtime = (TextView) findViewById(R.id.tv_systemtime);
        ll_video_controler = (LinearLayout) findViewById(R.id.ll_video_controler);
        tv_currentposition = (TextView) findViewById(R.id.tv_currentposition);
        sb_progress = (SeekBar) findViewById(R.id.sb_progress);
        tv_endtime = (TextView) findViewById(R.id.tv_endtime);
        ib_video_play = (ImageButton) findViewById(R.id.ib_video_play);
        ib_video_pause = (ImageButton) findViewById(R.id.ib_video_pause);
        ib_video_rewin = (ImageButton) findViewById(R.id.ib_video_rewin);
        ib_video_recon = (ImageButton) findViewById(R.id.ib_video_recon);
        sb_voice = (SeekBar) findViewById(R.id.sb_voice);
        tv_loadingspeed = (TextView) findViewById(R.id.tv_loadingspeed);
        ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
        utils = new Utils();
        ib_video_play.setOnClickListener(this);
        ib_video_pause.setOnClickListener(this);
        ib_video_rewin.setOnClickListener(this);
        ib_video_recon.setOnClickListener(this);
        sb_progress.setOnSeekBarChangeListener(this);//播放进度
        sb_voice.setOnSeekBarChangeListener(this);
        //手势
        detector = new GestureDetector(this,new MySimpleOnGestureListener());
        screenwidth = getWindowManager().getDefaultDisplay().getWidth();
        screenheight = getWindowManager().getDefaultDisplay().getHeight();
        ll_voice = (LinearLayout) findViewById(R.id.ll_voice);
        handler.sendEmptyMessage(SHOWNETSPEED);
    }


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case PROGRESS:
                    int currentPosition = (int) videoview.getCurrentPosition();
                    sb_progress.setProgress(currentPosition);
                    tv_currentposition.setText(utils.stringForTime(currentPosition));
                    tv_systemtime.setText(getSystemTime());
                    if(isNetUri){
                        int bufferPercentage = videoview.getBufferPercentage();
                        int totalBuffer = bufferPercentage * sb_progress.getMax();
                        int secondaryProgress = totalBuffer/100;
                        sb_progress.setSecondaryProgress(secondaryProgress);
                    }else{
                        sb_progress.setSecondaryProgress(0);
                    }
                    removeMessages(PROGRESS);
                    sendEmptyMessageDelayed(PROGRESS,500);
                    break;
                case HIDECONTROLLER:
                    //
                    hideMediaController();
                    break;
                case SHOWNETSPEED:
                    //显示网速
                    String netspeed = utils.getNetSpeed(VideoPlayerActivity.this);
                    tv_netspeed.setText(netspeed);
                    tv_loadingspeed.setText("加载中。。。"+netspeed);
                    removeMessages(SHOWNETSPEED);
                    sendEmptyMessageDelayed(SHOWNETSPEED,2000);
                    break;
            }
        }
    };

    //获取系统时间
    private String getSystemTime() {
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //准备播放
        videoview.start();
        int duration = (int) videoview.getDuration();
        sb_progress.setMax(duration);
        tv_endtime.setText(utils.stringForTime(duration));
        handler.sendEmptyMessage(PROGRESS);
        hideMediaController();
        ll_loading.setVisibility(View.GONE);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //播放出错
        Toast.makeText(this,"播放出错！",Toast.LENGTH_LONG).show();
        finish();
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //播放完成
        playPreOrNextVideo(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ib_video_play:
                setPlayStage(true);
                break;
            case R.id.ib_video_pause:
                setPlayStage(false);
                break;
            case R.id.ib_video_rewin:
                playPreOrNextVideo(false);
                break;
            case R.id.ib_video_recon:
                playPreOrNextVideo(true);
                break;
        }
        handler.removeMessages(HIDECONTROLLER);
        handler.sendEmptyMessageDelayed(HIDECONTROLLER,4000);
    }

    private void playPreOrNextVideo(boolean isNext) {
        if(isNext){
            if(mediaItems!=null&&mediaItems.size()>0){
                position++;
                if(position<mediaItems.size()){
                    ll_loading.setVisibility(View.VISIBLE);
                    MediaInfo mediaInfo = mediaItems.get(position);
                    tv_name.setText(mediaInfo.getName());
                    isNetUri = utils.isNetUri(mediaInfo.getPath());
                    videoview.setVideoPath(mediaInfo.getPath());
                    setButtonState();
                }
            }else if(uri!=null){
                setButtonState();
            }
        }else{
            if(mediaItems!=null&&mediaItems.size()>0){
                position--;
                if(position>=0){
                    ll_loading.setVisibility(View.VISIBLE);
                    MediaInfo mediaInfo = mediaItems.get(position);
                    tv_name.setText(mediaInfo.getName());
                    isNetUri = utils.isNetUri(mediaInfo.getPath());
                    videoview.setVideoPath(mediaInfo.getPath());
                    setButtonState();
                }
            }else if(uri!=null){
                setButtonState();
            }
        }

    }

    private void setButtonState() {
        if(mediaItems!=null&&mediaItems.size()>0){
            if(mediaItems.size()==1){
                //列表中只有一个
                setEnable(false);
            }else if(mediaItems.size()==2){
                //列表中有两个  区分播放位置
                if(position==0){
                    ib_video_rewin.setEnabled(false);
                    ib_video_recon.setEnabled(true);
                }else if(position==mediaItems.size()-1){
                    ib_video_recon.setEnabled(false);
                    ib_video_rewin.setEnabled(true);
                }
            }else{
                if(position==0){
                    ib_video_rewin.setEnabled(false);
                }else if(position==mediaItems.size()-1){
                    ib_video_recon.setEnabled(false);
                }else{
                    setEnable(true);
                }
            }
        }else if(uri!=null){
            //
            setEnable(false);
        }
    }

    private void setEnable(boolean isEnable){
        if(isEnable){
            ib_video_rewin.setEnabled(true);
            ib_video_recon.setEnabled(true);
        }else{
            ib_video_rewin.setEnabled(false);
            ib_video_recon.setEnabled(false);
        }

    }

    private void setPlayStage(boolean b) {
        if(b){
            videoview.start();
            ib_video_play.setVisibility(View.GONE);
            ib_video_pause.setVisibility(View.VISIBLE);
        }else{
            videoview.pause();
            ib_video_play.setVisibility(View.VISIBLE);
            ib_video_pause.setVisibility(View.GONE);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(seekBar==sb_voice){
            if(fromUser){
                updataVoice(progress);
            }
        }else if(seekBar==sb_progress){
            if(fromUser){
                videoview.seekTo(progress);
            }
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        handler.removeMessages(HIDECONTROLLER);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        handler.sendEmptyMessageDelayed(HIDECONTROLLER,4000);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                tVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                touchRang = screenheight;
                handler.removeMessages(HIDECONTROLLER);
                break;
            case MotionEvent.ACTION_MOVE:
                endX = event.getX();
                endY = event.getY();
                if(endX < screenwidth/2){
                    //调节亮度
                    final double FLING_MIN_DISTANCE = 0.5;
                    final double FLING_MIN_VELOCITY = 0.5;
                    if(startY - endY > FLING_MIN_DISTANCE && Math.abs(startY - endY)> FLING_MIN_VELOCITY){
                        setBrightness(20);
                    }
                    if(startY - endY < FLING_MIN_DISTANCE && Math.abs(startY - endY)> FLING_MIN_VELOCITY){
                        setBrightness(-20);
                    }
                }else{
                    //调节音量
                    float delta = (startY - event.getY())/touchRang*maxVoice;
                    int voice = (int) Math.min(Math.max(delta+tVol,0),maxVoice);
                    if(delta != 0){
                        updataVoice(voice);
                    }
                }

                if(startX  - endX > 20){

                }

                break;
            case MotionEvent.ACTION_UP:
                handler.sendEmptyMessageDelayed(HIDECONTROLLER,4000);
                break;
        }
        return super.onTouchEvent(event);
    }

    /**设置亮度**/
    private void setBrightness(float i) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = lp.screenBrightness + i / 255.0f;
        if(lp.screenBrightness > 1 ){
            lp.screenBrightness = 1;
        }else if(lp.screenBrightness < 0.1){
            lp.screenBrightness = (float) 0.1;
        }
        getWindow().setAttributes(lp);
    }

    private void isShowMediaController(){
            ll_video_top.setVisibility(View.VISIBLE);
            ll_video_controler.setVisibility(View.VISIBLE);
            sb_voice.setVisibility(View.VISIBLE);
            ll_voice.setVisibility(View.VISIBLE);
            isShowController=false;
    }
    private void hideMediaController(){
        ll_video_top.setVisibility(View.GONE);
        ll_video_controler.setVisibility(View.GONE);
        sb_voice.setVisibility(View.GONE);
        ll_voice.setVisibility(View.GONE);
        isShowController=true;
    }

    private void updataVoice(int progress) {
        am.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
        sb_voice.setProgress(progress);
        currentVoice=progress;
    }

    //手势识别器
    class MySimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            //单击
            if(isShowController){
                isShowMediaController();
                handler.sendEmptyMessageDelayed(HIDECONTROLLER,4000);
            }else{
                hideMediaController();
                handler.removeMessages(HIDECONTROLLER);
            }
            return super.onSingleTapConfirmed(e);
        }
    }

    /**
     * 监听物理音量键**/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            currentVoice--;
            updataVoice(currentVoice);
            handler.removeMessages(HIDECONTROLLER);
            handler.sendEmptyMessageDelayed(HIDECONTROLLER,4000);
            return true;
        }else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            currentVoice++;
            updataVoice(currentVoice);
            handler.removeMessages(HIDECONTROLLER);
            handler.sendEmptyMessageDelayed(HIDECONTROLLER,4000);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
