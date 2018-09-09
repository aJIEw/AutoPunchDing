package com.ajiew.autopunchding.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ajiew.autopunchding.service.KeepRunningService;

public class AutoStartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent punchIntent = new Intent(context, KeepRunningService.class);
        context.startService(punchIntent);
    }
}
