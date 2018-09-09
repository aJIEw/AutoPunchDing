package com.ajiew.autopunchding.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ajiew.autopunchding.broadcast.AutoStartReceiver;
import com.ajiew.autopunchding.R;
import com.ajiew.autopunchding.broadcast.PunchReceiver;

/**
 * author: aaron.chen
 * created on: 2018/9/9 15:21
 * description: Make sure this will keep running
 */
public class KeepRunningService extends Service{

    private PunchReceiver receiver;

    @Override
    public void onCreate() {
        super.onCreate();

        receiver = new PunchReceiver();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Notification.Builder builder = new Notification.Builder(this.getApplicationContext())
                .setContentTitle("AutoPunchDing")
                .setContentText("Day Day Up!")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setWhen(System.currentTimeMillis());
        startForeground(101, builder.build());

        // 闹钟也会发送这个广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(receiver, filter);

        // 每 5 分钟跑一次
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int time = 5 * 60 * 1000;
        long triggerAtTime = System.currentTimeMillis() + time;
        PendingIntent pi = PendingIntent.getBroadcast(this, 0,
                new Intent(this, AutoStartReceiver.class), 0);
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtTime, pi);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                manager.setExact(AlarmManager.RTC_WAKEUP, triggerAtTime, pi);
            } else {
                manager.set(AlarmManager.RTC_WAKEUP, triggerAtTime, pi);
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopForeground(true);
        unregisterReceiver(receiver);
    }
}
