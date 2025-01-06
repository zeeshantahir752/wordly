package com.yesserly.wordly.utils.game;

import static com.yesserly.wordly.utils.Config.CHANNEL_ID;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.yesserly.wordly.R;
import com.yesserly.wordly.utils.SharedPreferencesHelper;
import com.yesserly.wordly.views.MainActivity;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AlarmReceiver extends BroadcastReceiver {

    @Inject
    SharedPreferencesHelper mSharedPrefs;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mSharedPrefs.isNotificationsEnabled()) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            Intent notificationIntent = new Intent(context, MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent in = PendingIntent.getActivity(context, 0,
                    notificationIntent, 0);

            //Build Notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.launcher)
                    .setContentTitle(context.getString(R.string.notification_title))
                    .setContentText(context.getString(R.string.notification_content))
                    .setContentIntent(in)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            Notification notification = builder.build();
            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.defaults |= Notification.DEFAULT_VIBRATE;

            notificationManager.notify(1, notification);
        }
    }
}
