package com.fisheradelakin.icarus;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Fisher on 3/20/15.
 */
public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {

    // TODO: add boot receiver code here
    // TODO: > boot receiver works. it just crashes.

    protected static final String TAG = "activity-detection-response-receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        ArrayList<DetectedActivity> updatedActivities =
                intent.getParcelableArrayListExtra(Constants.ACTIVITY_EXTRA);
        DetectedActivity upAct = intent.getParcelableExtra(Constants.ACTIVITY_EXTRA + "2");
        String act = Constants.getActivityString(context, upAct.getType());

        SharedPreferences prefs = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String previous = prefs.getString("vehicle", null);

        if(previous == null) {
            previous = "hi";
        }
        // BUG: It sends a notification when you're walking. :/
        if(previous.equals(context.getResources().getString(R.string.in_vehicle)) && upAct.getType() == 2 && upAct.getConfidence() >= 50) {
            NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_small_icon)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                    .setTicker("Check your car!")
                    .setContentTitle(context.getResources().getString(R.string.app_name))
                    .setContentText(getRandomNotificationMessage(context))
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setOnlyAlertOnce(true)
                    .setVibrate(new long[] { 0, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000 });
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(1, nBuilder.build());
        }

        SharedPreferences.Editor editor = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE).edit();
        if(act.equals(context.getString(R.string.in_vehicle))) {
            editor.putString("vehicle", context.getString(R.string.in_vehicle));
            editor.apply();
        } else {
            editor.putString("activity", act);
            editor.apply();
        }

        MainActivity.updateDetectedActivitiesList(updatedActivities);
    }

    public String getRandomNotificationMessage(Context context) {
        String[] messages = context.getResources().getStringArray(R.array.notification_messages);
        return messages[new Random().nextInt(messages.length)];
    }
}
