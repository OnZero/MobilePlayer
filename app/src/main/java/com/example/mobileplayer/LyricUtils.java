package com.example.mobileplayer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by lenovo on 2017/1/28.
 * 解析歌词
 */

public class LyricUtils {
    /**
     * 返回解析好的歌词**/
    public ArrayList<Lyric> getLyrics() {
        return lyrics;
    }

    private ArrayList<Lyric> lyrics;

    public boolean isExistsLyric() {
        return isExistsLyric;
    }

    //是否存在歌词
    private boolean isExistsLyric =false;

    public void readLyricFile(File file){
        if(file == null || !file.exists()){
            //歌词文件不存在
            lyrics = null;
            isExistsLyric = false;
        }else{
            try {
                isExistsLyric = true;
                lyrics = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),getCharset(file)));
                String line = "";
                while ((line = reader.readLine())!=null){
                    line = parsedLyric(line);
                }
                reader.close();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //排序
            Collections.sort(lyrics, new Comparator<Lyric>() {
                @Override
                public int compare(Lyric o1, Lyric o2) {
                    if(o1.getTimePoint()<o2.getTimePoint()){
                        return -1;
                    }else if(o1.getTimePoint()>o2.getTimePoint()){
                        return 1;
                    }else{
                        return 0;
                    }
                }
            });
            //计算每句歌词高亮的时间
            for(int i=0;i<lyrics.size();i++){
                Lyric oneLyric = lyrics.get(i);
                if(i+1<lyrics.size()){
                    Lyric twoLyric = lyrics.get(i+1);
                    oneLyric.setSleepTime(twoLyric.getTimePoint()-oneLyric.getTimePoint());
                }
            }
        }
    }

    /**
     * 解析单句**/
    private String parsedLyric(String line) {
        int pos1 = line.indexOf("[");
        int pos2 = line.indexOf("]");
        if(pos1 == 0 &&pos2!=-1){
            long[] times = new long[getCountTag(line)];

            String strTime = line.substring(pos1+1,pos2);
            times[0] = strTimeToLongTime(strTime);

            String context = line;
            int i = 1;
            while (pos1 == 0 && pos2 !=-1){
                context = context.substring(pos2+1);
                pos1 = context.indexOf("[");
                pos2 = context.indexOf("]");
                if(pos2 != -1){
                    strTime = context.substring(pos1+1,pos2);
                    times[i] = strTimeToLongTime(strTime);
                    if(times[i] == -1){
                        return "";
                    }
                    i++;
                }
            }
            /**
             * 添加时间和文本到集合中**/
            Lyric lyric = new Lyric();
            for(int j=0;j<times.length;j++){
                if(times[j]!=0){
                    lyric.setContent(context);
                    lyric.setTimePoint(times[j]);
                    lyrics.add(lyric);
                    lyric = new Lyric();
                }
            }
            return context;
        }
        return "";
    }

    /**
     * 判断文件编码
     * @param file 文件
     * @return 编码：GBK,UTF-8,UTF-16LE
     */
    public String getCharset(File file) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1)
                return charset;
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE
                    && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF
                    && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8";
                checked = true;
            }
            bis.reset();
            if (!checked) {
                int loc = 0;
                while ((read = bis.read()) != -1) {
                    loc++;
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF)
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF)
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return charset;
    }

    /**
     * 把String转换成long类型**/
    private long strTimeToLongTime(String strTime) {
        long result = -1;
        try{
            String[] s1 = strTime.split(":");
            String[] s2 = s1[1].split("\\.");
            //分
            long min = Long.parseLong(s1[0]);
            //秒
            long second = Long.parseLong(s2[0]);
            //毫秒
            long mil = Long.parseLong(s2[1]);
            result = min*60*1000 + second*1000 + mil*10;
        }catch (Exception e){
            e.printStackTrace();
            result = -1;
        }
        return result;
    }

    /**判断有多少句歌词**/
    private int getCountTag(String line) {
        int result=-1;
        String[] left = line.split("\\[");
        String[] right = line.split("\\]");
        if(left.length==0&&right.length==0){
            result =1;
        }else if(left.length > right.length){
            result = left.length;
        }else{
            result = right.length;
        }
        return result;
    }
}
