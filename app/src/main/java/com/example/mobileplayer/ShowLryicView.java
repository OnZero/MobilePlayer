package com.example.mobileplayer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by lenovo on 2017/1/28.
 */

public class ShowLryicView extends TextView {

    //歌词列表
    private ArrayList<Lyric> lyrics;
    private Paint paint;
    private Paint whitepaint;
    //歌词索引
    private int index;
    //行高
    private float textHeight;

    private int width;
    private int height;
    private float currentPosition;
    //高亮显示时间
    private float sleepTime;
    //时间戳
    private float timePoint;

    public void setLyrics(ArrayList<Lyric> lyrics) {
        this.lyrics = lyrics;
    }
    public ShowLryicView(Context context) {
        this(context,null);
    }

    public ShowLryicView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ShowLryicView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    private void initView(Context context) {
        //转换像素
        textHeight = DensityUtil.dip2px(context,20);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(DensityUtil.dip2px(context,20));
        //抗锯齿
        paint.setAntiAlias(true);
        //文字居中
        paint.setTextAlign(Paint.Align.CENTER);

        whitepaint = new Paint();
        whitepaint.setColor(Color.WHITE);
        whitepaint.setTextSize(DensityUtil.dip2px(context,20));
        whitepaint.setAntiAlias(true);
        whitepaint.setTextAlign(Paint.Align.CENTER);
        lyrics = new ArrayList<>();
        /**
         * 假歌词**/
        /*Lyric lyric = new Lyric();
        for(int i=0;i<1000;i++){
            lyric.setTimePoint(1000*i);
            lyric.setSleepTime(1500*i);
            lyric.setContent("asdsadas"+i);
            lyrics.add(lyric);
            lyric = new Lyric();
        }*/
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /**
         * 不加try捕捉，会有：从有歌词的歌曲跳到下一曲没歌词的歌曲时会出现空指针错误**/
        try {
            if(lyrics.size()>0&&lyrics!=null){

                /**
                 * 推移歌词*/
                float plush= 0;
                if(sleepTime==0){
                    plush = 0;
                }else{
                    plush = textHeight + ((currentPosition - timePoint)/sleepTime)*textHeight;
                }
                canvas.translate(0,-plush);

                //绘制歌词
                /**
                 * 歌词当前句**/
                String currentText = lyrics.get(index).getContent();
                canvas.drawText(currentText,width/2,height/2,paint);
                /**
                 * 歌词前半部**/
                float tempY = height/2;
                for(int i=index-1;i>=0;i--){
                    String preContext = lyrics.get(i).getContent();
                    tempY = tempY - textHeight;
                    if(tempY<0){
                        break;
                    }
                    canvas.drawText(preContext,width/2,tempY,whitepaint);
                }
                /**
                 * 歌词后半部**/
                tempY = height/2;
                for(int i=index+1;i<lyrics.size();i++){
                    String nextContext = lyrics.get(i).getContent();
                    tempY = tempY + textHeight;
                    if(tempY > height){
                        break;
                    }
                    canvas.drawText(nextContext,width/2,tempY,whitepaint);
                }

            }else{
                canvas.drawText("没有发现歌词",width/2,height/2,paint);
            }
        }catch (Exception e){
            e.printStackTrace();
            canvas.drawText("没有发现歌词",width/2,height/2,paint);
        }

    }

    /**
     * 根据播放位置高亮歌词**/
    public void setShowNextLyric(int currentPosition) {
        this.currentPosition = currentPosition;
        if(lyrics == null || lyrics.size()==0)
            return;
        for(int i=1;i<lyrics.size();i++){
            if(currentPosition<lyrics.get(i).getTimePoint()){
                int tempIndex = i-1;
                if(currentPosition >= lyrics.get(tempIndex).getTimePoint()){
                    index = tempIndex;
                    sleepTime = lyrics.get(index).getSleepTime();
                    timePoint = lyrics.get(index).getTimePoint();
                }
            }
        }
        //重绘
        invalidate();
    }
}
