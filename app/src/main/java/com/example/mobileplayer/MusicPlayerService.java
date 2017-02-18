package com.example.mobileplayer;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by lenovo on 2017/1/25.
 */

public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener,MediaPlayer.OnCompletionListener,MediaPlayer.OnErrorListener {
    private ArrayList<MusicInfo> mediaInfos;
    private int position;
    private android.media.MediaPlayer mediaPlayer;
    private MusicInfo musicInfo;
    public static final String OPENAUDIO="com.example.mobileplayer.OPENAUDIO";
    //顺序播放
    public static final int REPEAT_NORMAL=1;
    //单曲循环
    public static final int REPEAT_SINGLE=2;
    //全部循环
    public static final int REPEAT_ALL=3;
    private int playmode = REPEAT_NORMAL;

    @Override
    public void onCreate() {
        super.onCreate();
        playmode = CacheUtils.getPlayMode(this,"playmode");
        //服务启动后 加载音乐列表
        getDataFromLocal();
    }

    private void getDataFromLocal() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                String[] musicInfo={MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.DATA};
                mediaInfos = new ArrayList<MusicInfo>();
                ContentResolver resolver = getContentResolver();
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
            }
        }.start();
    }

    private IMusicPlayerService.Stub stub = new IMusicPlayerService.Stub() {
        MusicPlayerService service = MusicPlayerService.this;
        @Override
        public void openAudio(int position) throws RemoteException {
            service.openAudio(position);
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void start() throws RemoteException {
            service.start();
        }

        @Override
        public void pause() throws RemoteException {
            service.pause();
        }

        @Override
        public void stop() throws RemoteException {
            service.stop();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return service.getCurrentPosition();
        }

        @Override
        public int getDuration() throws RemoteException {
            return service.getDuration();
        }

        @Override
        public String getArtist() throws RemoteException {
            return service.getArtist();
        }

        @Override
        public String getName() throws RemoteException {
            return service.getName();
        }

        @Override
        public String getAudioPath() throws RemoteException {
            return service.getAudioPath();
        }

        @Override
        public void next() throws RemoteException {
            service.next();
        }

        @Override
        public void pre() throws RemoteException {
            service.pre();
        }

        @Override
        public void setPlayMode(int playMode) throws RemoteException {
            service.setPlayMode(playMode);
        }

        @Override
        public int getPlayMode() throws RemoteException {
            return service.getPlayMode();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return service.isPlaying();
        }

        @Override
        public void seekTo(int position) throws RemoteException {
            mediaPlayer.seekTo(position);
        }
    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    private void openAudio(int position){
        this.position = position;
        if(mediaInfos!=null&&mediaInfos.size()>0){
            musicInfo = mediaInfos.get(position);
            if(mediaPlayer !=null){
                mediaPlayer.reset();
            }
            mediaPlayer = new android.media.MediaPlayer();
            try {
                mediaPlayer.setDataSource(musicInfo.getPath());
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setOnCompletionListener(this);
                mediaPlayer.setOnErrorListener(this);
                mediaPlayer.prepareAsync();
                if(playmode == MusicPlayerService.REPEAT_SINGLE){
                    mediaPlayer.setLooping(true);
                }else{
                    mediaPlayer.setLooping(false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(MusicPlayerService.this,"Loading",Toast.LENGTH_LONG).show();
        }
    }
    private NotificationManager manager;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void start(){
        mediaPlayer.start();
        //开启状态栏
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this,MusicPlayerActivity.class);
        intent.putExtra("Notification",true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this).setSmallIcon(R.drawable.ic_notification).setContentTitle("MobilePlayer")
                .setContentText("正在播放:"+getName()).setContentIntent(pendingIntent).build();
        manager.notify(1,notification);
    }

    private void pause(){
        mediaPlayer.pause();
        manager.cancel(1);
    }

    private void stop(){}

    private int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }

    private int getDuration(){return mediaPlayer.getDuration();}

    private String getArtist(){return musicInfo.getArtist();}

    private String getName(){return musicInfo.getTitle();}

    private String getAudioPath(){return musicInfo.getPath();}

    private void next(){
        setNextPosition();
        openNextAudio();
    }
    //根据下标播放
    private void openNextAudio() {
        int playmode = getPlayMode();
        if(playmode == MusicPlayerService.REPEAT_NORMAL){
            if(position < mediaInfos.size()){
                openAudio(position);
            }else{
                Toast.makeText(this,"已经是最后一首了喔",Toast.LENGTH_LONG).show();
                position = mediaInfos.size()-1;
            }
        }else if(playmode == MusicPlayerService.REPEAT_SINGLE){
            openAudio(position);
        }else if(playmode == MusicPlayerService.REPEAT_ALL){
            openAudio(position);
        }else{
            if(position < mediaInfos.size()){
                openAudio(position);
            }else{
                position = mediaInfos.size()-1;
            }
        }
    }
    //根据播放模式播放下一个音乐
    private void setNextPosition() {
        int playmode = getPlayMode();
        if(playmode == MusicPlayerService.REPEAT_NORMAL){
            position++;
        }else if(playmode == MusicPlayerService.REPEAT_SINGLE){
            position++;
            if(position >mediaInfos.size()){
                position=0;
            }
        }else if(playmode == MusicPlayerService.REPEAT_ALL){
            position++;
            if(position >=mediaInfos.size()){
                position=0;
            }
        }else{
            position++;
        }
    }

    private void pre(){
        setPrePosition();
        openPreAudio();
    }

    private void openPreAudio(){
        int playmode = getPlayMode();
        if(playmode == MusicPlayerService.REPEAT_NORMAL){
            if(position >=0){
                openAudio(position);
            }else{
                Toast.makeText(this,"这已经是第一首了",Toast.LENGTH_SHORT).show();
                position = 0;
            }
        }else if(playmode == MusicPlayerService.REPEAT_SINGLE){
            openAudio(position);
        }else if(playmode == MusicPlayerService.REPEAT_ALL){
            openAudio(position);
        }else{
            if(position >=0){
                openAudio(position);
            }else{
                position = 0;
            }
        }
    }

    private void setPrePosition() {
        int playmode = getPlayMode();
        if(playmode == MusicPlayerService.REPEAT_NORMAL){
            position--;
        }else if(playmode == MusicPlayerService.REPEAT_SINGLE){
            position--;
            if(position <0){
                position=mediaInfos.size()-1;
            }
        }else if(playmode == MusicPlayerService.REPEAT_ALL){
            position--;
            if(position <0){
                position=mediaInfos.size()-1;
            }
        }else{
            position--;
        }
    }

    private void setPlayMode(int playMode){
        this.playmode=playMode;
        CacheUtils.putPlayMode(this,"playmode",playMode);
        if(playmode == MusicPlayerService.REPEAT_SINGLE){
            mediaPlayer.setLooping(true);
        }else{
            mediaPlayer.setLooping(false);
        }
    }

    private int getPlayMode(){
        return playmode;
    }

    private boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onPrepared(MediaPlayer mp) {
        notifyChange(OPENAUDIO);
        start();
    }

    //发广播
    private void notifyChange(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        next();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        next();
        Toast.makeText(this,"歌曲出错",Toast.LENGTH_LONG).show();
        return false;
    }
}
