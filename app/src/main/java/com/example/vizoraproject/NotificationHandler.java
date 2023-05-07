package com.example.vizoraproject;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.vizoraproject.AddReportActivity;
import com.example.vizoraproject.R;

public class NotificationHandler {
    private static final int NOTIFICATION_ID = 0;
    private Context mContext;
    private NotificationManager mManager;
    private static final String CHANNEL_ID = "vizora_notification_channel";


    public NotificationHandler(Context context) {
        this.mContext = context;
        this.mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannel();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                "Vízóra lejelntés app",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.RED);
        channel.setDescription("Notificaton from vizora application.");
        this.mManager.createNotificationChannel(channel);
    }
    public void send(String message){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext,CHANNEL_ID)
                .setContentTitle("Vízóra")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notif_water);
        this.mManager.notify(NOTIFICATION_ID,builder.build());

    }
    public void cancel(){
        this.mManager.cancel(NOTIFICATION_ID);
    }
}
