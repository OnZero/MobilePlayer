package com.example.mobileplayer;

import java.io.Serializable;

/**
 * Created by lenovo on 2017/1/24.
 */

public class MusicInfo implements Serializable {
    private String title;
    private String artist;
    private long size;
    private long duration;
    private String path;
    private String album_id;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "MusicInfo{" +
                "title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", size=" + size +
                ", duration=" + duration +
                ", path='" + path + '\'' +
                ", album_id='" + album_id + '\'' +
                '}';
    }

    public String getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(String album_id) {
        this.album_id = album_id;
    }
}
