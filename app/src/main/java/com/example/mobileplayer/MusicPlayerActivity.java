package com.example.mobileplayer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MusicPlayerActivity extends Activity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener{
    private int position;
    private IMusicPlayerService service;
    private ImageButton ib_playandpause;
    private ImageButton ib_audio_pre;
    private ImageButton ib_audio_next;
    private ImageButton iv_music_back;
    private TextView tv_audio_name;
    private TextView tv_audio_artist;
    private TextView tv_audio_current;
    private SeekBar sb_audio_progress;
    private TextView tv_audio_endtime;
    private ImageButton ib_audio_playmode;
    private MyReceiver myReceiver;
    private final int PROGRESS = 1;
    private final int SHOW_LYRIC=2;
    private Utils utils;
    private boolean notification;
    //歌词
    private ShowLryicView showLyricView;
    //频谱
    private BaseVisualizerView baseVisualizerView;
    private Visualizer mVisualizer;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case PROGRESS:
                    try {
                        int currentPosition = service.getCurrentPosition();
                        sb_audio_progress.setProgress(currentPosition);
                        tv_audio_current.setText(utils.stringForTime(currentPosition));
                        tv_audio_endtime.setText(utils.stringForTime(service.getDuration()));
                        handler.removeMessages(PROGRESS);
                        handler.sendEmptyMessageDelayed(PROGRESS,500);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case SHOW_LYRIC:
                    try {
                        int currentPosition = service.getCurrentPosition();
                        showLyricView.setShowNextLyric(currentPosition);
                        handler.removeMessages(SHOW_LYRIC);
                        handler.sendEmptyMessage(SHOW_LYRIC);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };
    private ServiceConnection con = new ServiceConnection() {
        /***
         * 当连接成功后调用***/
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            service = IMusicPlayerService.Stub.asInterface(iBinder);
            if(service!=null){
                try {
                    if(!notification){
                        service.openAudio(position);
                        /*int currentPosition = CacheUtils.getMusicPositionCache(MusicPlayerActivity.this,"currentPosition");
                        if(position !=currentPosition){

                            CacheUtils.putMusicPositionCache(MusicPlayerActivity.this,"currentPosition",position);
                        }else{
                            showLyric();
                            showViewData();
                            checkPlayMode();
                            setupVisualizerFxAndUi();
                        }*/
                    }else{
                        showViewData();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        /***
         * 当断开连接后调用**/
        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                if(service!=null) {
                    service.stop();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_music_player);
        initView();
        getData();
        bindAndStartService();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setupVisualizerFxAndUi();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //释放频谱
        if(mVisualizer!=null){
            mVisualizer.release();
        }
    }

    @Override
    protected void onDestroy() {
        //System.out.println("onDestroy+++++++++++++++++++++++++++++++++++++++++++++");
        handler.removeCallbacks(null);
        //注销广播
        if(myReceiver!=null){
            unregisterReceiver(myReceiver);
            myReceiver=null;
        }
        //解绑服务
        if(con !=null){
            unbindService(con);
        }
        super.onDestroy();
    }

    private void bindAndStartService() {
        Intent intent = new Intent(this,MusicPlayerService.class);
        intent.setAction("com.example.mobileplayer.OPENAUDIO");
        bindService(intent,con, Context.BIND_AUTO_CREATE);
        startService(intent); //防止多次调用bindAndStartService
    }

    private void getData() {
        notification = getIntent().getBooleanExtra("Notification",false);
        if(!notification){
            position = getIntent().getIntExtra("position",0);
        }
    }

    private void initView() {
        MyActivityManager.getInstance().addActivity(this);
        ib_playandpause = (ImageButton) findViewById(R.id.ib_playandpause);
        ib_audio_pre = (ImageButton) findViewById(R.id.ib_audio_pre);
        ib_audio_next = (ImageButton) findViewById(R.id.ib_audio_next);
        iv_music_back = (ImageButton) findViewById(R.id.iv_music_back);
        tv_audio_name = (TextView) findViewById(R.id.tv_audio_name);
        tv_audio_artist = (TextView) findViewById(R.id.tv_audio_artist);
        tv_audio_current = (TextView) findViewById(R.id.tv_audio_current);
        sb_audio_progress = (SeekBar) findViewById(R.id.sb_audio_progress);
        tv_audio_endtime = (TextView) findViewById(R.id.tv_audio_endtime);
        ib_audio_playmode = (ImageButton) findViewById(R.id.ib_audio_playmode);
        showLyricView = (ShowLryicView) findViewById(R.id.showLyricView);
        baseVisualizerView = (BaseVisualizerView) findViewById(R.id.baseVisualizerView);
        utils = new Utils();
        ib_playandpause.setOnClickListener(this);
        ib_audio_next.setOnClickListener(this);
        ib_audio_pre.setOnClickListener(this);
        iv_music_back.setOnClickListener(this);
        ib_audio_playmode.setOnClickListener(this);
        showLyricView.setOnClickListener(this);
        baseVisualizerView.setOnClickListener(this);
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicPlayerService.OPENAUDIO);
        registerReceiver(myReceiver, intentFilter);
        sb_audio_progress.setOnSeekBarChangeListener(this);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ib_playandpause:
                try {
                    if(service!=null){
                        if(service.isPlaying()){
                            service.pause();
                            ib_playandpause.setImageResource(R.drawable.ib_audio_play_selector);
                        }else{
                            service.start();
                            ib_playandpause.setImageResource(R.drawable.ib_audio_pause_selector);
                        }
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.ib_audio_playmode:
                setPlayMode();
                break;
            case R.id.ib_audio_pre:
                if(service!=null){
                    try {
                        service.pre();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.ib_audio_next:
                if(service!=null){
                    try {
                        service.next();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.iv_music_back:
                setMainIntent();
                break;
            case R.id.showLyricView:
                showLyricView.setVisibility(View.GONE);
                baseVisualizerView.setVisibility(View.VISIBLE);
                break;
            case R.id.baseVisualizerView:
                baseVisualizerView.setVisibility(View.GONE);
                showLyricView.setVisibility(View.VISIBLE);
                break;
        }
    }


    private void setMainIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //改变模式
    private void setPlayMode() {
        try {
            int playmode = service.getPlayMode();
            if(playmode == MusicPlayerService.REPEAT_NORMAL){
                playmode = MusicPlayerService.REPEAT_SINGLE;
            }else if(playmode == MusicPlayerService.REPEAT_SINGLE){
                playmode = MusicPlayerService.REPEAT_ALL;
            }else if(playmode == MusicPlayerService.REPEAT_ALL){
                playmode = MusicPlayerService.REPEAT_NORMAL;
            }else{
                playmode = MusicPlayerService.REPEAT_NORMAL;
            }
            service.setPlayMode(playmode);
            showPlayMode();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //校验模式
    private void checkPlayMode() {
        try {
            int playmode = service.getPlayMode();
            if(playmode == MusicPlayerService.REPEAT_NORMAL){
                ib_audio_playmode.setImageResource(R.drawable.ib_playmode_normal_selector);
            }else if(playmode == MusicPlayerService.REPEAT_SINGLE){
                ib_audio_playmode.setImageResource(R.drawable.ib_playmode_single_selector);
            }else if(playmode == MusicPlayerService.REPEAT_ALL){
                ib_audio_playmode.setImageResource(R.drawable.ib_playmode_all_selector);
            }else{
                ib_audio_playmode.setImageResource(R.drawable.ib_playmode_normal_selector);
            }
            if(service.isPlaying()){
                ib_playandpause.setImageResource(R.drawable.ib_audio_pause_selector);
            }else{
                ib_playandpause.setImageResource(R.drawable.ib_audio_play_selector);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //显示模式图片
    private void showPlayMode() {
        try {
            int playmode = service.getPlayMode();
            if(playmode == MusicPlayerService.REPEAT_NORMAL){
                ib_audio_playmode.setImageResource(R.drawable.ib_playmode_normal_selector);
                Toast.makeText(this,"顺序播放",Toast.LENGTH_LONG).show();
            }else if(playmode == MusicPlayerService.REPEAT_SINGLE){
                ib_audio_playmode.setImageResource(R.drawable.ib_playmode_single_selector);
                Toast.makeText(this,"单曲循环",Toast.LENGTH_LONG).show();
            }else if(playmode == MusicPlayerService.REPEAT_ALL){
                ib_audio_playmode.setImageResource(R.drawable.ib_playmode_all_selector);
                Toast.makeText(this,"列表循环",Toast.LENGTH_LONG).show();
            }else{
                ib_audio_playmode.setImageResource(R.drawable.ib_playmode_normal_selector);
                Toast.makeText(this,"顺序播放",Toast.LENGTH_LONG).show();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser){
            try {
                service.seekTo(progress);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    //设置频谱
    private void setupVisualizerFxAndUi(){
        try {
            int audioSessionid = service.getAudioSessionId();
            if(mVisualizer != null){
                mVisualizer = null;
            }
            mVisualizer = new Visualizer(audioSessionid);
            /**在调用setCaptureSize()函数之前不先设置setEnabled()值为false
             * 容易报java.lang.IllegalStateException: setCaptureSize() called in wrong state: 2**/
            mVisualizer.setEnabled(false);
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            baseVisualizerView.setVisualizer(mVisualizer);
            mVisualizer.setEnabled(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    class MyReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            //开启歌词同步
            showLyric();
            showViewData();
            checkPlayMode();
            setupVisualizerFxAndUi();
        }
    }

    //显示歌词
    private void showLyric() {
        LyricUtils lyricUtils = new LyricUtils();
        try {
            String path = service.getAudioPath();
            path = path.substring(0,path.lastIndexOf("."));
            File file = new File(path+".lrc");
            if(!file.exists()){
                file = new File(path+".txt");
            }
            lyricUtils.readLyricFile(file);

            showLyricView.setLyrics(lyricUtils.getLyrics());


        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if(lyricUtils.isExistsLyric()){
            handler.sendEmptyMessage(SHOW_LYRIC);
        }

    }

    private void showViewData() {
        try {
            tv_audio_artist.setText(service.getArtist());
            tv_audio_name.setText(service.getName());
            sb_audio_progress.setMax(service.getDuration());
            handler.sendEmptyMessage(PROGRESS);


        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
