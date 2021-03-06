package com.fisheradelakin.icarus;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import info.hoang8f.widget.FButton;

// TODO: REFACTOR
// TODO: Add reboot reciever

public class MainActivity extends ActionBarActivity implements ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status> {

    protected static final String TAG = "activity-recognition";

    protected ActivityDetectionBroadcastReceiver mBroadcastReciever;
    protected GoogleApiClient mGoogleApiClient;
    private PendingIntent mActivityDetectionPendingIntent;

    @Bind(R.id.stopButton) FButton stopButton;

    protected SharedPreferences mSharedPreferences;

    private ArrayList<DetectedActivity> mDetectedActivities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // shared preferences
        mSharedPreferences = getSharedPreferences("settings", 0);

        // enable/disable buttons
        if(mSharedPreferences.getBoolean("isRunning", false)) {
            //disableButton(startButton);
            Log.i(TAG, "Icarus is already running");
        } else {
            disableButton(stopButton);
        }

        // get receiver for broadcasts from ActivityDetectionIntentService
        mBroadcastReciever = new ActivityDetectionBroadcastReceiver();

        // reuse the value of mDetectedActivities from the bundle if possible.
        // if mDetectedActivities is not stored in the bundle, populate it with DetectedActivity objects whose confidence is set to 0.
        if(savedInstanceState != null && savedInstanceState.containsKey(Constants.DETECTED_ACTIVITIES)) {
            mDetectedActivities = (ArrayList<DetectedActivity>) savedInstanceState.getSerializable(Constants.DETECTED_ACTIVITIES);
        } else {
            mDetectedActivities = new ArrayList<>();

            // set the confidence level of each monitored activity to zero.
            for(int i = 0; i < Constants.MONITORED_ACTIVITIES.length; i++) {
                mDetectedActivities.add(new DetectedActivity(Constants.MONITORED_ACTIVITIES[i], 0));
            }
        }

        // kick off the request to build GoogleApiClient
        buildGoogleApiClient();

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReciever, new IntentFilter(Constants.BROADCAST_ACTION));
    }

    // builds a GoogleApiClient
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();
    }

    private void startDetectingActivity() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
        }
        mSharedPreferences.edit().putBoolean("isRunning", true).apply();
        //disableButton(startButton);
        enableButton(stopButton);
        showStartDialog(); // this should only be called once.

        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent()).setResultCallback(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register the broadcast receiver that informs this activity of the detected activity object
        // sent by the intent service
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReciever, new IntentFilter(Constants.BROADCAST_ACTION));
    }

    @Override
    protected void onDestroy() {
        // unregister the broadcast receiver that was registered during onResume()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReciever);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // unregister the broadcast receiver that was registered during onResume()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReciever);
        super.onPause();
    }

    // onclick implementation for stop button
    @OnClick(R.id.stopButton)
    public void stop(View view) {
        mSharedPreferences.edit().putBoolean("isRunning", false).apply();
        Log.i(TAG, "Stopping");
        disableButton(stopButton);

        // remove all activity updates for the pending intent that was used to request activity updates
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,
                getActivityDetectionPendingIntent()).setResultCallback(this);
    }

    // gets a pending intent to be sent for each activity detection.
    private PendingIntent getActivityDetectionPendingIntent() {
        // reuse the pendingIntent if we already have it.
        if(mActivityDetectionPendingIntent != null) {
            return mActivityDetectionPendingIntent;
        }

        Intent intent = new Intent(this, DetectedActivityIntentService.class);

        // use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling requestActivityUpdates() and removeActivityUpdates()
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void showStartDialog() {
        if(mSharedPreferences.getBoolean("Seen", true)) {
            Log.i(TAG, "User has seen the dialog");
        } else {
            Log.i(TAG, "User has not seen the dialog");
            mSharedPreferences.edit().putBoolean("Seen", true);
            mSharedPreferences.edit().apply();
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Done!")
                    .setMessage(getString(R.string.start_dialog_message)).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            }).setCancelable(false).show();
        }
    }

    private void disableButton(FButton button) {
        button.setEnabled(false);
        button.setButtonColor(getResources().getColor(R.color.gray_disabled));
        button.setShadowColor(getResources().getColor(R.color.gray_disabled_shadow));
        button.setTextColor(getResources().getColor(R.color.text_button_disabled));
    }

    private void enableButton(FButton button) {
        button.setEnabled(true);
        button.setButtonColor(getResources().getColor(R.color.blue_enabled));
        button.setShadowColor(getResources().getColor(R.color.blue_enabled_shadow));
        button.setTextColor(getResources().getColor(R.color.text_button_enabled));
    }

    // when a google api client object successfully connects
    @Override
    public void onConnected(Bundle bundle) {
        startDetectingActivity();
        Log.i(TAG, "Connected to GoogleApiClient");
    }

    @Override
    public void onConnectionSuspended(int i) {
        // the connection to google play services was lost for some reason.
        // call connect() to attempt to re-establish the connection
        Log.i(TAG, "connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // lol. refer to javadoc for error codes that might be returned for connectionResult
        Log.i(TAG, "connection failed: connectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onResult(Status status) {
        if(status.isSuccess()) {
            // toggle the status of the activity updates requested and save in shared preferences.
            boolean requestingUpdates = !getUpdatesRequestedState();
            setUpdatesRequestedState(requestingUpdates);

            //Toast.makeText(this, getString(requestingUpdates ? R.string.activity_updates_added : R.string.activity_updates_removed), Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "Error adding or removing activity detection: " + status.getStatusMessage());
        }
    }

    // set boolean in shared preferences that tracks whether we are requesting activity updates
    private void setUpdatesRequestedState(boolean requestingUpdates) {
        getSharedPreferencesInstance().edit().putBoolean(Constants.ACTIVITY_UPDATES_REQUESTED_KEY, requestingUpdates).commit();
    }

    // retrieve the boolean from shared preferences that tracks whether we are requesting activity updates
    private boolean getUpdatesRequestedState() {
        return getSharedPreferencesInstance().getBoolean(Constants.ACTIVITY_UPDATES_REQUESTED_KEY, false);
    }

    // retrieve shared preferences object used to store or read values in this app
    private SharedPreferences getSharedPreferencesInstance() {
        return getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
    }

    // store the list of detected activities in the bundle
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(Constants.DETECTED_ACTIVITIES, mDetectedActivities);
        super.onSaveInstanceState(savedInstanceState);
    }

    // process list of recently detected activities and update the list of objects
    protected static void updateActivities(ArrayList<DetectedActivity> detectedActivities) {
        HashMap<Integer, Integer> detectedActivitiesMap = new HashMap<>();
        for(DetectedActivity activity : detectedActivities) {
            detectedActivitiesMap.put(activity.getType(), activity.getConfidence());
        }

        // every time a new activity is detected, reset the confidence level of all activities that we monitor.
        ArrayList<DetectedActivity> tempList = new ArrayList<DetectedActivity>();
        for(int i = 0; i < Constants.MONITORED_ACTIVITIES.length; i++) {
            int confidence = detectedActivitiesMap.containsKey(Constants.MONITORED_ACTIVITIES[i]) ? detectedActivitiesMap.get(Constants.MONITORED_ACTIVITIES[i]) : 0;
            tempList.add(new DetectedActivity(Constants.MONITORED_ACTIVITIES[i], confidence));
        }

    }

    protected static void updateDetectedActivitiesList(ArrayList<DetectedActivity> updatedActivities) {
        updateActivities(updatedActivities);
    }


}
