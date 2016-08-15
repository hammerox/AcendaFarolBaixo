package com.hammerox.android.acendaofarolbaixo;

import android.content.Context;
import android.util.Log;

/**
 * Created by Mauricio on 14-Aug-16.
 */
public class PrefManager {

    public static final String FILE_NAME = "preferences";
    public static final String LOG_TAG = PrefManager.class.getName();
    public static final String START_ON_BOOT = "START_ON_BOOT";


    public static void saveBoolean(Context context, String key, boolean value) {
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(key, value)
                .apply();
        sendLog(key, String.valueOf(value));
    }


    public static boolean loadBoolean(Context context, String key) {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
                .getBoolean(key, true);
    }


    private static void sendLog(String key, String value) {
        Log.v(LOG_TAG, key + " is " + value);
    }

}
