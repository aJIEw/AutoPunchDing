package com.ajiew.autopunchding.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ajiew.autopunchding.service.PunchService;

/**
 * author: aaron.chen
 * created on: 2018/9/9 15:33
 * description:
 */
public class PunchReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_TIME_TICK))
            context.startService(new Intent(context, PunchService.class));
    }
}
