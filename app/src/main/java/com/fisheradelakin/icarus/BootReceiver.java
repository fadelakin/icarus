package com.fisheradelakin.icarus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving

        // an Intent broadcast.
        //Intent i = new Intent("com.fisheradelakin.icarus.DetectedActivityIntentService");
        //i.setClass(context, DetectedActivityIntentService.class);
        //context.startService(i);

        Intent i = new Intent(context, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
