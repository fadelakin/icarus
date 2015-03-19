package com.fisheradelakin.icarus;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.List;

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

        /*if(ActivityRecognitionResult.hasResult(intent)) {
            // get the update
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            // log the update
            Log.i(TAG, result.toString());

            // get the most probable activity from the list of activities in the update
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();

            // get the confidence percentage for the most probable activity
            int confidence  = mostProbableActivity.getConfidence();

            // get the type of activity
            int activityType = mostProbableActivity.getType();
            mostProbableActivity.getVersionCode();

            localIntent.putExtra(Constants.ACTIVITY_EXTRA, activityType);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

            Log.d(TAG, "activity: " + Constants.getActivityString(getApplicationContext(), activityType));

            if(confidence >= 50) {
                String mode = Constants.getActivityString(getApplicationContext(), activityType);
                if(activityType == DetectedActivity.ON_FOOT) {
                    DetectedActivity betterActivity = walkingOrRunning(result.getProbableActivities());

                    if(null != betterActivity)
                        mode = Constants.getActivityString(getApplicationContext(), betterActivity.getType());
                }
            }
        }*/


        // Log each activity
        Log.i(TAG, "activities detected");
        for(DetectedActivity da: detectedActivities) {
            Log.i(TAG, Constants.getActivityString(getApplicationContext(), da.getType()) + " " + da.getConfidence() + "%");
        }

        // Broadcast the list of detected activities
        localIntent.putExtra(Constants.ACTIVITY_EXTRA, detectedActivities);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private DetectedActivity walkingOrRunning(List<DetectedActivity> probableActivites) {
        DetectedActivity myActivity = null;
        int confidence = 0;
        for(DetectedActivity activity : probableActivites) {
            Log.i(TAG, Constants.getActivityString(getApplicationContext(), activity.getType()) + " " + activity.getConfidence() + "%");
            if(activity.getType() != DetectedActivity.RUNNING && activity.getType() != DetectedActivity.WALKING)
                continue;

            if (activity.getConfidence() > confidence)
                myActivity = activity;
        }

        return myActivity;
    }
}
