package com.ajiew.autopunchding;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutoStartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null &&
                (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
                        intent.getAction().equals(Intent.ACTION_TIME_TICK))) {
            context.startService(new Intent(context, PunchService.class));
        } else {
            Log.w(this.getClass().getSimpleName(), "onReceive: false alarm");
        }
    }
}
