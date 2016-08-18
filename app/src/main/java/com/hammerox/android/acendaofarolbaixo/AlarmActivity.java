package com.hammerox.android.acendaofarolbaixo;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class AlarmActivity extends AppCompatActivity{

    @BindView(R.id.alarm_icon) ImageView alarmIcon;
    @BindString(R.string.pref_file_name) String fileName;
    @BindString(R.string.pref_alarm_type_key) String alarmTypeKey;

    private int alarmTypeValue;
    private boolean isToAlarm = false;
    private boolean isToSpeech = false;
    private Uri mAlarmSound;
    private Ringtone mRingtone;
    private int mAnimationInterval = 1000; // 1 seconds by default, can be changed later
    private Handler mAnimationHandler;
    private Runnable mAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                YoYo.with(Techniques.Flash).playOn(findViewById(R.id.alarm_icon));
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mAnimationHandler.postDelayed(mAnimationRunnable, mAnimationInterval);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_alarm);
        ButterKnife.bind(this);

        alarmTypeValue = Integer.valueOf(getSharedPreferences(fileName, Context.MODE_PRIVATE)
                .getString(alarmTypeKey, "1"));

        switch (alarmTypeValue) {
            case 1:
                isToAlarm = true;
                playAlarmSound();
                break;
            case 2:
                isToSpeech = true;
                Intent intent = new Intent(this, SpeechService.class);
                startService(intent);
                break;
        }

        mAnimationHandler = new Handler();
        startAnimation();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        // Do nothing
    }


    @OnClick(R.id.alarm_okay)
    public void onOkayClick(Button button) {
        finish();
        stopEverything();
    }


    @OnClick(R.id.alarm_sleep)
    public void onSleepClick(Button button) {
        Toast.makeText(this, "Sorry, ainda em construção...", Toast.LENGTH_LONG).show();
    }


    @OnClick(R.id.alarm_config)
    public void onConfigClick(Button button) {
        Toast.makeText(this, "Sorry, ainda em construção...", Toast.LENGTH_LONG).show();
    }


    public void playAlarmSound() {
        mAlarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mRingtone = RingtoneManager.getRingtone(getApplicationContext(), mAlarmSound);
        try {
            mRingtone.play();
            Log.d(DetectorService.LOG_TAG, "User notified");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void stopAlarmSound() {
        try {
            mRingtone.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    void startAnimation() {
        mAnimationRunnable.run();
    }


    void stopAnimation() {
        mAnimationHandler.removeCallbacks(mAnimationRunnable);
    }


    private void stopEverything() {
        stopAnimation();

        switch (alarmTypeValue) {
            case 1:
                stopAlarmSound();
                break;
            case 2:
                Intent intent = new Intent(this, SpeechService.class);
                stopService(intent);
                break;
        }
    }

}
