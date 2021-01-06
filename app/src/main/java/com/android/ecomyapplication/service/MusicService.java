package com.android.ecomyapplication.service;


import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import com.android.ecomyapplication.R;
import com.android.ecomyapplication.data.MusicLoader;
import com.android.ecomyapplication.model.Song;
import com.android.ecomyapplication.musicplayer.MusicNotification;
import com.android.ecomyapplication.musicplayer.MusicPlayer;
import com.android.ecomyapplication.musicplayer.MusicPlayerCompletionListener;
import com.android.ecomyapplication.view.WidgetProvider;

import java.io.IOException;


public class MusicService extends Service implements MusicPlayerCompletionListener {
    private static final String TAG = "Music Service";
    private static final int ONGOING_NOTIFICATION_ID = 1;
    private MusicPlayer player;
    private MusicNotification mNotification;

    @Override
    public void onCreate() {
        player = new MusicPlayer(this);
        player.setOnCompletionListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction() != null ? intent.getAction() : "";

        try {
            if (action.equals(WidgetProvider.ACTION_PLAY_PAUSE)) {
                if (player.isPlaying())
                    pauseMusic();
                else {
                    playMusic();
                }
            } else if (action.equals(WidgetProvider.ACTION_STOP)) {
                stopMusic();
            } else if (action.equals(WidgetProvider.ACTION_NEXT)) {
                nextSong();
            } else if (action.equals(WidgetProvider.ACTION_PREVIOUS)) {
                previousSong();
            } else if (action.equals(WidgetProvider.ACTION_JUMP_TO)) {
                Song song = (Song) intent.getExtras().get("song");
                jumpTo(song);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        if (player != null && !player.isStopped()) {
            stopMusic();
        }
        super.onDestroy();
    }

    private void updateUI(String title, String artist, String duration, Boolean isPlaying) {
        // Update widget

        RemoteViews remoteViews = WidgetProvider.getRemoteViews(this);

        if (title != null && artist != null && duration != null) {
            remoteViews.setViewVisibility(R.id.layout_header, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.top_icon, View.GONE);
            remoteViews.setTextViewText(R.id.textViewTitle, title);
            remoteViews.setTextViewText(R.id.textViewArtist, artist);
            remoteViews.setTextViewText(R.id.textViewDuration, duration);
        } else {
            remoteViews.setViewVisibility(R.id.layout_header, View.GONE);
            remoteViews.setViewVisibility(R.id.top_icon, View.VISIBLE);

        }


        if (isPlaying != null) {
            if (isPlaying)
                remoteViews.setImageViewResource(R.id.button_play_pause, R.drawable.pause);
            else
                remoteViews.setImageViewResource(R.id.button_play_pause, R.drawable.play);
        }

        ComponentName thisWidget = new ComponentName(this, WidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.updateAppWidget(thisWidget, remoteViews);

        if (artist != null && title != null) {
            if (mNotification == null) {
                mNotification = new MusicNotification(this, ONGOING_NOTIFICATION_ID, title, artist);
                startForeground(ONGOING_NOTIFICATION_ID, mNotification.getNotification());
            } else {
                boolean isPlayingUnboxed = isPlaying != null ? isPlaying : false;
                mNotification.update(title, artist, isPlayingUnboxed);
            }
        } else {
            stopForeground(true);
        }
    }


    private void playMusic() throws IOException {
        Log.d(TAG, "PLAY");
        Song song = MusicLoader.getInstance(this).getCurrent();

        if (player.isPaused()) {
            player.play();
        } else {
            player.setSong(song);
            player.play();
        }

        updateUI(song.getTitle(), song.getArtist(), song.getDurationStr(), true);
        Log.i("Music Service", "Playing: " + song.getTitle());
    }


    private void pauseMusic() {
        Log.d(TAG, "PAUSE");
        if (player.isPlaying()) {
            Song song = MusicLoader.getInstance(this).getCurrent();
            updateUI(song.getTitle(), song.getArtist(), song.getDurationStr(), false);

            player.pause();
        }
    }


    private void stopMusic() {
        player.stop();
        updateUI(null, null, null, false);
        MusicLoader.getInstance(this).close();
    }

    private void nextSong() throws IOException {
        if (player != null) {
            Song nextSong = MusicLoader.getInstance(this).getNext();
            player.setSong(nextSong);

            updateUI(nextSong.getTitle(), nextSong.getArtist(), nextSong.getDurationStr(), player.isPlaying());
        }
    }

    private void previousSong() throws IOException {
        Log.d(TAG, "PREVIOUS SONG");

        if (player != null) {
            Song prevSong = MusicLoader.getInstance(this).getPrevious();
            player.setSong(prevSong);

            updateUI(prevSong.getTitle(), prevSong.getArtist(), prevSong.getDurationStr(), player.isPlaying());
        }
    }

    private void jumpTo(Song song) throws IOException {
        MusicLoader.getInstance(this).jumpTo(song);
        playMusic();
    }

    @Override
    public void onMusicCompletion() throws IOException {
        nextSong();
        player.play();
        Song song = MusicLoader.getInstance(this).getCurrent();
        updateUI(song.getTitle(), song.getArtist(), song.getDurationStr(), true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
