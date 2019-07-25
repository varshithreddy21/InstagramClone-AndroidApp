package com.gmrj.thenetwork;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
public static final String CHANNEL_1_ID="channel";
public static final String CHANNEL_2_ID="channel2";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotifications();
    }

    private void createNotifications() {
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O){
            NotificationChannel channel=new NotificationChannel(
                    CHANNEL_1_ID,
                    "channel1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("channel 1");
            NotificationChannel channe2=new NotificationChannel(
                    CHANNEL_2_ID,
                    "channel2",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("channel 2");
            NotificationManager manager=getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            manager.createNotificationChannel(channe2);
        }
    }
}
