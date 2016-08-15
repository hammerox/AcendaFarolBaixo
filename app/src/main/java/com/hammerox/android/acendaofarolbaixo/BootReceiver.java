package com.hammerox.android.acendaofarolbaixo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Mauricio on 14-Aug-16.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String fileName = context.getString(R.string.pref_file_name);
        String bootKey = context.getString(R.string.pref_boot_key);

        boolean startOnBoot = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
                .getBoolean(bootKey, true);

        if (startOnBoot) {
            // Enable detector
            Intent startIntent = new Intent(context, DetectorService.class);
            context.startService(startIntent);
        }
    }

}
