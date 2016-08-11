package com.hammerox.android.acendaofarolbaixo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;

import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

public class MainActivity extends AppCompatActivity implements OnActivityUpdatedListener {

    private boolean mNotifyUser;
    private StringBuilder mTextLog;
    private TextView mTextView;
    private long mTimeNow;
    private long mLastTime;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    private static final int LOCATION_PERMISSION_ID = 1001;
    private static final String LOG_TAG = "onActivityUpdated";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Keep the screen always on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // bind textviews
        mTextView = (TextView) findViewById(R.id.textview_activity);

        // Start string builder
        mTextLog = new StringBuilder();

        showLast();
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


    @Override
    public void onActivityUpdated(DetectedActivity detectedActivity) {
        long timeDiff = 0L;
        mTimeNow = System.currentTimeMillis();

        if (detectedActivity != null) {
            if (mLastTime != 0L) {
                timeDiff = (mTimeNow - mLastTime) / DateUtils.SECOND_IN_MILLIS;
            }

            mTextLog.append(sdf.format(mTimeNow))
                    .append(" - ")
                    .append(detectedActivity.toString().substring(23))
                    .append(" - ")
                    .append(timeDiff)
                    .append("\n");
            mTextView.setText(mTextLog.toString());
            Log.d(LOG_TAG, detectedActivity.toString() + " seconds: " + timeDiff);

            mLastTime = mTimeNow;

            int activityType = detectedActivity.getType();

            if (detectedActivity.getConfidence() == 100) {
                switch (activityType) {
                    // IN_VEHICLE, play notification
                    case 0:
                        if (mNotifyUser) playNotification();
                        break;
                    // STILL and TILTING, do nothing
                    case 3:
                    case 5:
                        break;
                    // Everything else, reset notification flag
                    default:
                        resetNotificationFlag();
                        break;
                }
            }
        } else {
            mTextView.setText("Null activity");
            Log.d(LOG_TAG, "Null activity");
        }
    }


    public void playNotification() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
            mNotifyUser = false;
            Log.d(LOG_TAG, "User notified");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void resetNotificationFlag() {
        if (!mNotifyUser) {
            mNotifyUser = true;
            Log.d(LOG_TAG, "User is now prone to receive notification");
        }
    }


    public void onToggleClicked(View view) {
        boolean on = ((ToggleButton) view).isChecked();

        if (on) {
            // Enable detector
            // Location permission not granted
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
                return;
            }
            startLocation();
        } else {
            // Disable detector
            stopLocation();
        }
    }


    private void startLocation() {
        SmartLocation smartLocation = new SmartLocation.Builder(this).logging(true).build();
        smartLocation.activity().start(this);
    }


    private void stopLocation() {
        SmartLocation.with(this).activity().stop();
        mTextView.setText("Activity Recognition stopped!");
    }
}
