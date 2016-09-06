package com.hammerox.android.acendaofarolbaixo.others;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;
import com.hammerox.android.acendaofarolbaixo.R;
import com.hammerox.android.acendaofarolbaixo.activities.AlarmActivity;
import com.hammerox.android.acendaofarolbaixo.activities.MainActivity;

import java.util.Date;

import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

/**
 * Created by Mauricio on 10-Aug-16.
 */
public class DetectorService extends Service
        implements OnActivityUpdatedListener {

    public static final String IS_TESTING_KEY = "isTesting";
    public static final String LOG_TAG = "onActivityUpdated";
    public static final int NOTIFICATION_ID = 1;
    public static final long TIME_STILL_LIMIT = 600000; // 10 minutes

    private boolean alarmIsOn = true;
    private boolean timerIsOn = false;
    private Date mTimeNow = new Date();
    private Date mTimeInstance = new Date();


    @Override
    public void onCreate() {
        super.onCreate();

        // Run as foreground
        runAsForeground();

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
            Log.d(LOG_TAG, detectedActivity.toString());
            int activityType = detectedActivity.getType();

            if (!alarmIsOn) {
                if (timerIsOn) {
                    if (timeIsUp()) {
                        resetAlarmAndTimer();
                    }
                }
            }

            if (detectedActivity.getConfidence() == 100) {
                switch (activityType) {
                    case DetectedActivity.IN_VEHICLE:
                        if (alarmIsOn) {
                            launchAlarm();
                            turnAlarmOff();
                        }
                        if (timerIsOn) stopTimer();
                        break;
                    case DetectedActivity.STILL:
                        if (!alarmIsOn) {
                            if (!timerIsOn) startTimer();
                        }
                        break;
                    case DetectedActivity.TILTING:
                        // Do nothing
                        break;
                    default:
                        if (!alarmIsOn) resetAlarmAndTimer();
                        break;
                }
            }

        } else {
            Log.d(LOG_TAG, "Null activity");
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop detector
        SmartLocation.with(this).activity().stop();

        stopForeground(true);
        Log.d(LOG_TAG, "DetectorService STOPPED");
    }


    public void launchAlarm() {
        // Launch alarm screen
        Intent alarmIntent = new Intent(this, AlarmActivity.class);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtra(IS_TESTING_KEY, false);
        startActivity(alarmIntent);

        // Save date and time
        FileManager.insertDateAndTime(this);
    }


    private void runAsForeground(){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent= PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_beam)
                .setContentTitle("Acenda o Farol Baixo")
                .setContentText("Detector LIGADO. Clique para desligar")
                .setContentIntent(pendingIntent).build();

        startForeground(NOTIFICATION_ID, notification);
    }


    private void startTimer() {
        timerIsOn = true;
        mTimeInstance.setTime(System.currentTimeMillis());
        Log.d(LOG_TAG, "Timer STARTED");
    }


    private void stopTimer() {
        timerIsOn = false;
        Log.d(LOG_TAG, "Timer STOPPED");
    }


    private boolean timeIsUp() {
        mTimeNow.setTime(System.currentTimeMillis());
        long timeDiff = mTimeNow.getTime() - mTimeInstance.getTime();

        int seconds = (int) (timeDiff / 1000);
        int minutes = seconds / 60;
        seconds     = seconds % 60;

        Log.d(LOG_TAG, "Timer: " + String.format("%d:%02d", minutes, seconds));
        return timeDiff >= TIME_STILL_LIMIT;
    }


    private void resetAlarmAndTimer() {
        alarmIsOn = true;
        if (timerIsOn) stopTimer();
        Log.d(LOG_TAG, "User is now prone to receive notification");
    }


    private void turnAlarmOff() {
        alarmIsOn = false;
        if (timerIsOn) stopTimer();
    }

}
