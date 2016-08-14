package com.hammerox.android.acendaofarolbaixo;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.location.DetectedActivity;

import io.nlopez.smartlocation.SmartLocation;


public class MainActivity extends AppCompatActivity {

    private TextView mTextView;
    private ToggleButton mToggleButton;
    private String mServiceName = DetectorService.class.getName();
    private ActivityManager mActivityManager;

    private static final int LOCATION_PERMISSION_ID = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Keep the screen always on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // bind views
        mTextView = (TextView) findViewById(R.id.textview_activity);
        mToggleButton = (ToggleButton) findViewById(R.id.toggle_detector);

        showLast();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Find DetectorService running on background
        // If found, toggle button to ON. If not, toggle to OFF
        if (findService(mServiceName)) {
            Log.v(DetectorService.LOG_TAG, "Found Service running");
            mToggleButton.setChecked(true);
        } else {
            mToggleButton.setChecked(false);
        }
    }

    private void showLast() {
        DetectedActivity detectedActivity = SmartLocation.with(this).activity().getLastActivity();
        if (detectedActivity != null) {
            mTextView.setText(
                    String.format("[From Cache] Activity %s with %d%% confidence",
                            detectedActivity.toString(),
                            detectedActivity.getConfidence())
            );
        }
    }


    public void onToggleClicked(View view) {
        boolean on = ((ToggleButton) view).isChecked();

        if (on) {
            // Location permission not granted
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
                return;
            }

            // Enable detector
            Intent intent = new Intent(this, DetectorService.class);
            startService(intent);
            mTextView.setText("Activity Recognition started!");
        } else {
            // Disable detector
            Intent intent = new Intent(this, DetectorService.class);
            stopService(intent);
            mTextView.setText("Activity Recognition stopped!");
        }
    }


    private boolean findService(String serviceName) {
        if (mActivityManager == null) {
            mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        }

        // Search
        for (ActivityManager.RunningServiceInfo service : mActivityManager.getRunningServices(Integer.MAX_VALUE)) {
            // Check
            if (serviceName.equals(service.service.getClassName())) {
                // If found, return true
                return true;
            }
        }

        // If not found, return false
        return false;
    }
}
