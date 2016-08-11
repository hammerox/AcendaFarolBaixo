package com.hammerox.android.acendaofarolbaixo;

import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;

import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

/**
 * Created by Mauricio on 10-Aug-16.
 */
public class DetectorService extends Service implements OnActivityUpdatedListener {

    private boolean mNotifyUser;
    private StringBuilder mTextLog;
    private long mTimeNow;
    private long mLastTime;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    public static final String LOG_TAG = "onActivityUpdated";

    @Override
    public void onCreate() {
        super.onCreate();

        // Start string builder
        mTextLog = new StringBuilder();
        mNotifyUser = true;

        SmartLocation smartLocation = new SmartLocation.Builder(this).logging(true).build();
        smartLocation.activity().start(this);
        Log.d(LOG_TAG, "DetectorService RUNNING");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onActivityUpdated(DetectedActivity detectedActivity) {
        if (detectedActivity != null) {
            showLog(detectedActivity);
            alarmAlgorithm(detectedActivity);
        } else {
//            mTextView.setText("Null activity");
            Log.d(LOG_TAG, "Null activity");
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        SmartLocation.with(this).activity().stop();
        Log.d(LOG_TAG, "DetectorService STOPPED");
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


    public void showLog(DetectedActivity detectedActivity) {
        long timeDiff = 0L;
        mTimeNow = System.currentTimeMillis();

        if (mLastTime != 0L) {
            timeDiff = (mTimeNow - mLastTime) / DateUtils.SECOND_IN_MILLIS;
        }

        mTextLog.append(sdf.format(mTimeNow))
                .append(" - ")
                .append(detectedActivity.toString().substring(23))
                .append(" - ")
                .append(timeDiff)
                .append("\n");
//        mTextView.setText(mTextLog.toString());
        Log.d(LOG_TAG, detectedActivity.toString() + " seconds: " + timeDiff);

        mLastTime = mTimeNow;
    }


    public void alarmAlgorithm(DetectedActivity detectedActivity) {
        int activityType = detectedActivity.getType();

        if (detectedActivity.getConfidence() == 100) {
            if (activityType == DetectedActivity.IN_VEHICLE) {
                if (mNotifyUser) playNotification();
            } else if (activityType == DetectedActivity.STILL || activityType == DetectedActivity.TILTING) {
                // Do nothing
            } else {
                resetNotificationFlag();
            }
        }
    }

}
