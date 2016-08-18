package com.hammerox.android.acendaofarolbaixo;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;


/**
 * Created by Mauricio on 18-Aug-16.
 */
public class SpeechService extends Service
        implements TextToSpeech.OnInitListener {


    private TextToSpeech mTextToSpeech;
    private String mSpeech;
    private int mSpeechInterval = 3000; // 2 seconds by default, can be changed later
    private Handler mSpeechHandler;
    private Runnable mSpeechRunnable;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mSpeech = getString(R.string.pref_speech_default);
        mTextToSpeech = new TextToSpeech(this, this);

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSpeech();
        super.onTaskRemoved(rootIntent);
    }


    @Override
    public void onDestroy() {
        stopSpeech();
        super.onDestroy();
    }

    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {

            int result = mTextToSpeech.setLanguage(new Locale("pt", "br"));

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                mSpeechHandler = new Handler();
                mSpeechRunnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            playSpeech();

                            // Wait for it to start speaking
                            while (!mTextToSpeech.isSpeaking()) {}
                            // Wait for it to finish the speech
                            while (mTextToSpeech.isSpeaking()) {}
                        } finally {
                            // 100% guarantee that this always happens, even if
                            // your update method throws an exception
                            mSpeechHandler.postDelayed(mSpeechRunnable, mSpeechInterval);
                        }
                    }
                };
                mSpeechRunnable.run();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    public void playSpeech() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsGreater21(mSpeech);
        } else {
            ttsUnder20(mSpeech);
        }

        Log.d(DetectorService.LOG_TAG, "User notified");
    }


    public void stopSpeech() {
        if(mTextToSpeech !=null){
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
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
