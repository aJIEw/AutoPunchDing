package com.ajiew.autopunchding;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoStartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent punchIntent = new Intent(context, PunchService.class);
        context.startService(punchIntent);
    }
}
