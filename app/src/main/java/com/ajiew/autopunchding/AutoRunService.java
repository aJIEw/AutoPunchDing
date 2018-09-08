package com.ajiew.autopunchding;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

/**
 * 保证此服务能在后台一直运行
 */
public class AutoRunService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(new AutoStartReceiver(), filter);

        Notification.Builder builder = new Notification.Builder(this.getApplicationContext())
                .setContentTitle("AutoPunchDing")
                .setContentText("Day Day Up!")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setWhen(System.currentTimeMillis());
        startForeground(101, builder.build());

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
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(new AutoStartReceiver());
        stopForeground(true);
    }

}
