package com.hammerox.android.acendaofarolbaixo;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

/**
 * Created by Mauricio on 10-Aug-16.
 */
public class DetectorService extends Service
        implements OnActivityUpdatedListener {

    private TextToSpeech mTextToSpeech;
    private boolean mNotifyUser;
    private StringBuilder mTextLog;
    private long mTimeNow;
    private long mLastTime;
    private String mSpeech = "Atenção: Acenda o farol baixo";
    private SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat formatDate = new SimpleDateFormat("dd/mm - HH:mm");
    private String[] debugEmailAddress = new String[]{"EMAIL_ADDRESS"};

    public static final String LOG_TAG = "onActivityUpdated";
    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();

        // Run as foreground
        runAsForeground();

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

        // Stop detector
        SmartLocation.with(this).activity().stop();

        // Remove TextToSpeech
        if(mTextToSpeech !=null){
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }

        // Send log
        sendEmailLog();

        stopForeground(true);
        Log.d(LOG_TAG, "DetectorService STOPPED");
    }


    public void launchAlarm() {
        // Play notification
        playNotification();

        // Launch alarm screen
        Intent alarmIntent = new Intent(this, AlarmActivity.class);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(alarmIntent);
    }


    public void playNotification() {
        try {
//            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
//            r.play();

            mTextToSpeech = new TextToSpeech(this, speakSpeech());
            mNotifyUser = false;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public TextToSpeech.OnInitListener speakSpeech() {
        return new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {

                    int result = mTextToSpeech.setLanguage(new Locale("pt", "br"));

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    } else {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ttsGreater21(mSpeech);
                        } else {
                            ttsUnder20(mSpeech);
                        }

                        // Wait for it to speak
                        while (!mTextToSpeech.isSpeaking()) {}
                        // Wait until he finishes speaking
                        while (mTextToSpeech.isSpeaking()) {}

                        mTextLog.append("USER NOTIFIED")
                                .append("\n");
                        Log.d(LOG_TAG, "User notified");
                    }

                } else {
                    Log.e("TTS", "Initilization Failed!");
                }

                // Remove listener after its use
                if(mTextToSpeech !=null){
                    mTextToSpeech.stop();
                    mTextToSpeech.shutdown();
                }
            }
        };
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
            switch (activityType) {
                case DetectedActivity.IN_VEHICLE:
                    if (mNotifyUser) launchAlarm();
                    break;
                case DetectedActivity.STILL:
                    if (mNotifyUser) launchAlarm();
                    break;
                case DetectedActivity.TILTING:
                    // Do nothing
                    break;
                default:
                    resetNotificationFlag();
                    break;
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


    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId=this.hashCode() + "";
        mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

}
