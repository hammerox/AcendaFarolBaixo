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

        boolean startOnBoot = PrefManager.loadBoolean(context, PrefManager.START_ON_BOOT);

        if (startOnBoot) {
            // Enable detector
            Intent startIntent = new Intent(context, DetectorService.class);
            context.startService(startIntent);
        }
    }

}
