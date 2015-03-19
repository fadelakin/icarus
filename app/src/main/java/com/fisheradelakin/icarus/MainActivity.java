package com.fisheradelakin.icarus;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import info.hoang8f.widget.FButton;


public class MainActivity extends ActionBarActivity {

    @InjectView(R.id.startButton) FButton startButton;
    @InjectView(R.id.stopButton) FButton stopButton;

    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        // shared preferences
        mSharedPreferences = getSharedPreferences("settings", 0);

        // enable/disable buttons
        if(mSharedPreferences.getBoolean("isRunning", false)) {
            disableButton(startButton);
        } else {
            disableButton(stopButton);
        }
    }

    // onclick implementation for start button
    @OnClick(R.id.startButton)
    public void start(View view) {
        Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show();
        mSharedPreferences.edit().putBoolean("isRunning", true).apply();
        disableButton(startButton);
        enableButton(stopButton);
    }

    // onclick implementation for stop button
    @OnClick(R.id.stopButton)
    public void stop(View view) {
        Toast.makeText(this, "Stop", Toast.LENGTH_SHORT).show();
        mSharedPreferences.edit().putBoolean("isRunning", false).apply();
        disableButton(stopButton);
        enableButton(startButton);
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


}
