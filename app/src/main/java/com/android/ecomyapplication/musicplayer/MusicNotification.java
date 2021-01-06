package com.android.ecomyapplication.musicplayer;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;

import com.android.ecomyapplication.R;
import com.android.ecomyapplication.service.MusicService;
import com.android.ecomyapplication.view.WidgetProvider;

import androidx.core.app.NotificationCompat;

public class MusicNotification {

    NotificationCompat.Builder builder;
    private Notification notification;
    private final NotificationCompat.Action playPauseAction;
    private final NotificationManager manager;
    private final int notificationID;
    static final String NOTIFICATION_CHANNEL_ID = "121";

    public MusicNotification(Context context, int notificationID, String title, String artist) {
        this.notificationID = notificationID;
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        playPauseAction = new NotificationCompat.Action(R.drawable.pause, null, WidgetProvider.getPendingIntent(context, WidgetProvider.ACTION_PLAY_PAUSE));

        Intent notificationIntent = new Intent(context, MusicService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "music";
            String description = "music info";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setLightColor(R.color.colorPrimary);

            manager.createNotificationChannel(channel);
        }

        builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);


        builder.addAction(R.drawable.ic_prev, null, WidgetProvider.getPendingIntent(context, WidgetProvider.ACTION_PREVIOUS))
                .addAction(playPauseAction)
                .addAction(R.drawable.ic_next, null, WidgetProvider.getPendingIntent(context, WidgetProvider.ACTION_NEXT))
                .addAction(R.drawable.stop, null, WidgetProvider.getPendingIntent(context, WidgetProvider.ACTION_STOP));
        builder.setPriority(NotificationCompat.PRIORITY_MAX);


        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.music_widget_icon)
                .setTicker(title)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(artist);

        notification= builder.build();

    }


    public Notification getNotification() {
        return notification;
    }

    public void update(String title, String artist, boolean isPlaying) {
        builder.setContentTitle(title)
                .setContentText(artist)
                .setWhen(System.currentTimeMillis());

        if (isPlaying)
            playPauseAction.icon = R.drawable.pause;
        else
            playPauseAction.icon = R.drawable.play;
        notification = builder.build();
        manager.notify(notificationID, notification);
    }
}
