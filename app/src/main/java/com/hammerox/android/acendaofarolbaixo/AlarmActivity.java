package com.hammerox.android.acendaofarolbaixo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class AlarmActivity extends AppCompatActivity {

    @BindView(R.id.alarm_icon) ImageView alarmIcon;

    private int mInterval = 1000; // 1 seconds by default, can be changed later
    private Handler mHandler;
    private Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                YoYo.with(Techniques.Flash).playOn(findViewById(R.id.alarm_icon));
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
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

        mHandler = new Handler();
        startAnimation();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAnimation();
    }

    @OnClick(R.id.alarm_okay)
    public void onOkayClick(Button button) {
        finish();
    }


    @OnClick(R.id.alarm_sleep)
    public void onSleepClick(Button button) {
        Toast.makeText(this, "Sorry, ainda em construção...", Toast.LENGTH_LONG).show();
    }


    @OnClick(R.id.alarm_config)
    public void onConfigClick(Button button) {
        Toast.makeText(this, "Sorry, ainda em construção...", Toast.LENGTH_LONG).show();
    }


    void startAnimation() {
        mStatusChecker.run();
    }


    void stopAnimation() {
        mHandler.removeCallbacks(mStatusChecker);
    }
}
