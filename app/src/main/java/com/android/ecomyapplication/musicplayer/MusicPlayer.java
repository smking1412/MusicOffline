package com.android.ecomyapplication.musicplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.android.ecomyapplication.model.Song;

import java.io.IOException;

public class MusicPlayer implements MediaPlayer.OnCompletionListener {


    private MediaPlayer player;
    private final Context context;
    private MusicPlayerCompletionListener onMusicCompletionListener;
    private static final String TAG = "Music Player";


    public MusicPlayer(Context context){
        this.context = context;
    }


    public boolean isPlaying(){
        return player != null && player.isPlaying();
    }

    public boolean isPaused(){
        return player != null && !player.isPlaying();
    }

    public boolean isStopped(){
        return player == null;
    }

    public void setSong(Song song) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
        boolean wasPlaying = isPlaying();

        if (player == null) {
            player = new MediaPlayer();
            player.setOnCompletionListener(this);
        }else {
            player.reset();
        }

        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setDataSource(context, song.getURI());
        player.prepare();

        if (wasPlaying)
            player.start();

        Log.d(TAG, "Changed song to: " + song.getTitle());
    }

    public void play(){
        if (player == null)
            throw new IllegalStateException("Must call setSong() before calling play()");

        player.start();
    }

    public void pause(){
        if(isPlaying()){
            player.pause();
            Log.d(TAG, "Music paused");
        }
    }

    public void stop(){
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        Log.d(TAG, "Music Stopped");
    }

    public void setOnCompletionListener(MusicPlayerCompletionListener listener){
        onMusicCompletionListener = listener;
    }


    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        try {
            onMusicCompletionListener.onMusicCompletion();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
