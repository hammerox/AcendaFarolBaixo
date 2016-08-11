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
    private SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat formatDate = new SimpleDateFormat("dd/mm - HH:mm");
    private String[] debugEmailAddress = new String[]{"EMAIL_ADDRESS"};

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

        sendEmailLog();

        Log.d(LOG_TAG, "DetectorService STOPPED");
    }


    public void playNotification() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
            mNotifyUser = false;

            mTextLog.append("USER NOTIFIED")
                    .append("\n");
            Log.d(LOG_TAG, "User notified");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void resetNotificationFlag() {
        if (!mNotifyUser) {
            mNotifyUser = true;

            mTextLog.append("FLAG RESET")
                    .append("\n");

            Log.d(LOG_TAG, "User is now prone to receive notification");
        }
    }


    public void showLog(DetectedActivity detectedActivity) {
        long timeDiff = 0L;
        mTimeNow = System.currentTimeMillis();

        if (mLastTime != 0L) {
            timeDiff = (mTimeNow - mLastTime) / DateUtils.SECOND_IN_MILLIS;
        }

        mTextLog.append(formatTime.format(mTimeNow))
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


    public void sendEmailLog() {
        try {
            mTimeNow = System.currentTimeMillis();

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL  , debugEmailAddress);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "LOG " + formatDate.format(mTimeNow));
            emailIntent.putExtra(Intent.EXTRA_TEXT   , mTextLog.toString());

            Intent sendIntent = Intent.createChooser(emailIntent, "Send e-mail with...");
            sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            getApplicationContext().startActivity(sendIntent);

            Log.d(LOG_TAG, "Finished sending email...");
        }
        catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }

}
