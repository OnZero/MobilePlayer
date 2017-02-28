package com.example.mobileplayer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
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
import android.widget.RelativeLayout;
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

    //private RelativeLayout root_layout;
    private VideoView videoview;
    private Uri uri;//播放地址
    private LinearLayout ll_video_top;
    private TextView tv_name,tv_netspeed,tv_gesture_volume,tv_gesture_bright,tv_gesture_progress;
    private ImageView iv_dianchi,iv_gesture_volume,iv_gesture_bright,iv_gesture_progress;
    private TextView tv_systemtime;
    private LinearLayout ll_video_controler,ll_buffer,gesture_volume_layout,gesture_bright_layout,gesture_progress_layout;
    private TextView tv_currentposition;
    private SeekBar sb_progress;
    private TextView tv_endtime;
    private ImageButton ib_video_play;
    private ImageButton ib_video_pause;
    private final int PROGRESS=0; //播放进度消息
    private Utils utils;
    private MyReceiver myReceiver;
    private ArrayList<MediaInfo> mediaItems; //传递过来的视频列表
    private int position; //位置
    private ImageButton ib_video_rewin;
    private ImageButton ib_video_recon;
    private GestureDetector detector; //手势识别
    private boolean isShowController=false;
    private final int HIDECONTROLLER=1; //隐藏控制条
    private final int SHOWNETSPEED = 2;
    private AudioManager am;
    //当前音量
    private int currentVoice;
    //最大音量
    private int maxVoice;
    private int screenwidth; //屏幕宽度
    private int screenheight; //屏幕高度
    /**
     * 播放位置*/
    private int currentPosition;
    //是否是网络视频
    private boolean isNetUri;

    private TextView tv_loadingspeed;
    private LinearLayout ll_loading;
    private boolean firstScroll = false;
    /**
     * 手势辨别**/
    private int GESTURE_FLAG = 10;
    private int GESTURE_MODIFY_PROGRESS = 11;
    private int GESTURE_MODIFY_VOLUME = 12;
    private int GESTURE_MODIFY_BRIGHT = 13;

    private static final float STEP_PROGRESS = 2f;// 设定进度滑动时的步长，避免每次滑动都改变，导致改变过快
    private static final float STEP_VOLUME = 2f;// 协调音量滑动时的步长，避免每次滑动都改变，导致改变过快

    private float mBrightness = -1f; // 亮度

    private int playingTime,videoTotalTime;

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
        //内部调用
        if(mediaItems!=null && mediaItems.size()>0){
            MediaInfo mediaInfo = mediaItems.get(position);
            tv_name.setText(mediaInfo.getName());
            isNetUri = utils.isNetUri(mediaInfo.getPath());
            videoview.setVideoPath(mediaInfo.getPath());

        }else if(uri !=null){
            //外部调用
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
        //root_layout = (RelativeLayout) findViewById(R.id.root_layout);
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
        tv_loadingspeed = (TextView) findViewById(R.id.tv_loadingspeed);
        ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
        gesture_volume_layout = (LinearLayout) findViewById(R.id.gesture_volume_layout);
        gesture_bright_layout = (LinearLayout) findViewById(R.id.gesture_bright_layout);
        gesture_progress_layout = (LinearLayout) findViewById(R.id.gesture_progress_layout);
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //音量图标
        iv_gesture_volume = (ImageView) findViewById(R.id.iv_gesture_volume);
        //亮度图标
        iv_gesture_bright = (ImageView) findViewById(R.id.iv_gesture_bright);
        //滑动快进快退图标
        iv_gesture_progress = (ImageView) findViewById(R.id.iv_gesture_progress);
        //滑动音量文字显示
        tv_gesture_volume = (TextView) findViewById(R.id.tv_gesture_volume);
        //滑动亮度文字显示
        tv_gesture_bright = (TextView) findViewById(R.id.tv_gesture_bright);
        //滑动进度文字显示
        tv_gesture_progress = (TextView) findViewById(R.id.tv_gesture_progress);
        utils = new Utils();
        ib_video_play.setOnClickListener(this);
        ib_video_pause.setOnClickListener(this);
        ib_video_rewin.setOnClickListener(this);
        ib_video_recon.setOnClickListener(this);
        sb_progress.setOnSeekBarChangeListener(this);//播放进度
        //手势
        detector = new GestureDetector(this,new MySimpleOnGestureListener());
        screenwidth = getWindowManager().getDefaultDisplay().getWidth();
        screenheight = getWindowManager().getDefaultDisplay().getHeight();
        handler.sendEmptyMessage(SHOWNETSPEED);
        //隐藏滑动显示
        gesture_volume_layout.setVisibility(View.GONE);
        gesture_bright_layout.setVisibility(View.GONE);
        gesture_progress_layout.setVisibility(View.GONE);
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

    //设置播放状态
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
        if(seekBar==sb_progress){
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
        if(event.getAction() == MotionEvent.ACTION_UP){
            GESTURE_FLAG = 0;
            //隐藏音量、亮度和进度的布局
            gesture_volume_layout.setVisibility(View.GONE);
            gesture_bright_layout.setVisibility(View.GONE);
            gesture_progress_layout.setVisibility(View.GONE);
        }
        return detector.onTouchEvent(event);
    }


    private void isShowMediaController(){
            ll_video_top.setVisibility(View.VISIBLE);
            ll_video_controler.setVisibility(View.VISIBLE);
            isShowController=false;
    }
    private void hideMediaController(){
        ll_video_top.setVisibility(View.GONE);
        ll_video_controler.setVisibility(View.GONE);
        isShowController=true;
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
        //手指双击时触发
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if(videoview.isPlaying()){
                setPlayStage(false);
            }else{
                setPlayStage(true);
            }
            return super.onDoubleTap(e);
        }

        //手指一按下触发
        @Override
        public boolean onDown(MotionEvent e) {
            //手指第一次触碰到屏幕
            firstScroll = true;
            handler.removeMessages(HIDECONTROLLER);
            playingTime = (int) videoview.getCurrentPosition();
            videoTotalTime = (int) videoview.getDuration();
            return super.onDown(e);
        }
        //手指在屏幕上滑动触发
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float mOldX = e1.getX(), mOldY = e1.getY();
            int y = (int) e2.getRawY();
            if(firstScroll){
                /**
                 * 避免X轴和Y周的滑动操作混乱**/
                if(Math.abs(distanceX)>=Math.abs(distanceY)){
                    //此处修改视频的进度
                    gesture_progress_layout.setVisibility(View.VISIBLE);
                    gesture_volume_layout.setVisibility(View.GONE);
                    gesture_bright_layout.setVisibility(View.GONE);
                    GESTURE_FLAG = GESTURE_MODIFY_PROGRESS;
                }else{
                    //此处修改音量或者是亮度
                    if (mOldX > screenwidth * 3.0 / 5) {// 音量
                        gesture_volume_layout.setVisibility(View.VISIBLE);
                        gesture_bright_layout.setVisibility(View.GONE);
                        gesture_progress_layout.setVisibility(View.GONE);
                        GESTURE_FLAG = GESTURE_MODIFY_VOLUME;
                    } else if (mOldX < screenwidth * 2.0 / 5) {// 亮度
                        gesture_bright_layout.setVisibility(View.VISIBLE);
                        gesture_volume_layout.setVisibility(View.GONE);
                        gesture_progress_layout.setVisibility(View.GONE);
                        GESTURE_FLAG = GESTURE_MODIFY_BRIGHT;
                    }
                }
            }

            if(GESTURE_FLAG == GESTURE_MODIFY_PROGRESS){
                //改变进度
                // distanceX=lastScrollPositionX-currentScrollPositionX，因此为正时是快进
                if (Math.abs(distanceX) > Math.abs(distanceY)) {// 横向移动大于纵向移动
                    if (distanceX >= DensityUtil.dip2px(VideoPlayerActivity.this, STEP_PROGRESS)) {// 快退，用步长控制改变速度，可微调
                        iv_gesture_progress.setImageResource(R.drawable.iv_back);
                        if (playingTime > 3000) {// 避免为负
                            playingTime -= 3000;// scroll方法执行一次快退3秒
                        } else {
                            playingTime = 0;
                        }
                    } else if (distanceX <= -DensityUtil.dip2px(VideoPlayerActivity.this, STEP_PROGRESS)) {// 快进
                        iv_gesture_progress.setImageResource(R.drawable.iv_fast);
                        if (playingTime < videoTotalTime - 16000) {// 避免超过总时长
                            playingTime += 3000;// scroll执行一次快进3秒
                        } else {
                            playingTime = videoTotalTime - 10000;
                        }
                    }
                    if (playingTime < 0) {
                        playingTime = 0;
                    }
                    videoview.seekTo(playingTime);
                    tv_gesture_progress.setText(utils.stringForTime(playingTime) + "/" + utils.stringForTime(videoTotalTime));
                }
            }else if(GESTURE_FLAG == GESTURE_MODIFY_VOLUME){
                //音量
                currentVoice = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                if(Math.abs(distanceY) > Math.abs(distanceX)){
                    if (distanceY >= DensityUtil.dip2px(VideoPlayerActivity.this, STEP_VOLUME)){
                        if(currentVoice < maxVoice){
                            currentVoice++;
                        }
                        iv_gesture_volume.setImageResource(R.drawable.ic_volume);
                    }else if (distanceY <= -DensityUtil.dip2px(VideoPlayerActivity.this, STEP_VOLUME)) {// 音量调小
                        if (currentVoice > 0) {
                            currentVoice--;
                            if (currentVoice == 0) {// 静音，设定静音独有的图片
                                iv_gesture_volume.setImageResource(R.drawable.ic_volume_off);
                            }
                        }
                    }
                    int percentage = (currentVoice * 100) / maxVoice;
                    tv_gesture_volume.setText(percentage+"%");
                    am.setStreamVolume(AudioManager.STREAM_MUSIC,currentVoice, 0);
                }
            }else if(GESTURE_FLAG == GESTURE_MODIFY_BRIGHT){
                //亮度
                iv_gesture_bright.setImageResource(R.drawable.ic_brightness);
                if (mBrightness < 0) {
                    mBrightness = getWindow().getAttributes().screenBrightness;
                    if (mBrightness <= 0.00f)
                        mBrightness = 0.50f;
                    if (mBrightness < 0.01f)
                        mBrightness = 0.01f;
                }
                WindowManager.LayoutParams lpa = getWindow().getAttributes();
                lpa.screenBrightness = mBrightness + (mOldY - y) / screenheight;
                if (lpa.screenBrightness > 1.0f)
                    lpa.screenBrightness = 1.0f;
                else if (lpa.screenBrightness < 0.01f)
                    lpa.screenBrightness = 0.01f;
                getWindow().setAttributes(lpa);
                tv_gesture_bright.setText((int) (lpa.screenBrightness * 100) + "%");
            }
            firstScroll = false;
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
        //手指触摸松开时触摸
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            handler.sendEmptyMessageDelayed(HIDECONTROLLER,4000);
            return super.onSingleTapUp(e);
        }
    }

    /**
     * 监听物理音量键**/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
}
