package com.fisheradelakin.icarus;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

/**
 * Created by Fisher on 3/19/15.
 */
public class DetectedActivityIntentService extends IntentService {

    protected static final String TAG = "activityDetectionServ";

    // required constructor. call super intent service with name for a worker thread
    public DetectedActivityIntentService() {
        // use the tag to name the worker thread
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    // handle incoming intents.
    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION);

        // get the list of the probable activities associated with the current state of the device.
        // each activity is associated with a confidence level which is between 0 and 100
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

        DetectedActivity mostProbable = result.getMostProbableActivity();
        Log.i(TAG, "most probable");
        Log.i(TAG, Constants.getActivityString(getApplicationContext(), mostProbable.getType()) + " " + mostProbable.getConfidence() + "%");


        // Log each activity
        Log.i(TAG, "activities detected");
        for(DetectedActivity da: detectedActivities) {
            Log.i(TAG, Constants.getActivityString(getApplicationContext(), da.getType()) + " " + da.getConfidence() + "%");
        }

        // Broadcast the list of detected activities
        localIntent.putExtra(Constants.ACTIVITY_EXTRA, detectedActivities);
        localIntent.putExtra(Constants.ACTIVITY_EXTRA + "2", mostProbable);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
