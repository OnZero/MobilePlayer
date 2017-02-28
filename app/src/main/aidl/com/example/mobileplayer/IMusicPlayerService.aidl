// IMusicPlayerService.aidl
package com.example.mobileplayer;

// Declare any non-default types here with import statements

interface IMusicPlayerService {
        void openAudio(int position);

        void start();

        void pause();

        void stop();

        int getCurrentPosition();

        int getDuration();

        String getArtist();

        String getAlbum_id();

        String getName();

        String getAudioPath();

        void next();

        void pre();

        void setPlayMode(int playMode);

        int getPlayMode();

        boolean isPlaying();

        void seekTo(int position);

        int getAudioSessionId();
}
