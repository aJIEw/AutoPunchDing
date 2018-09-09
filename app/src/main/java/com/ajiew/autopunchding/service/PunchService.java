package com.ajiew.autopunchding.service;

import android.app.IntentService;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ajiew.autopunchding.event.PunchFinishedEvent;
import com.ajiew.autopunchding.event.PunchType;
import com.ajiew.autopunchding.util.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static com.ajiew.autopunchding.util.AppUtil.clickXY;
import static com.ajiew.autopunchding.util.AppUtil.close;
import static com.ajiew.autopunchding.util.AppUtil.stopApp;
import static com.ajiew.autopunchding.util.AppUtil.swipe;

public class PunchService extends IntentService {

    /**
     * 钉钉包名
     */
    public static final String DD_PACKAGE_NAME = "com.alibaba.android.rimet";

    private PunchType punchType;
    private PowerManager powerManager;
    private KeyguardManager keyguardManager;
    private String punchPositionY;

    Handler handler = new Handler(Looper.getMainLooper());

    public PunchService() {
        super("PunchService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
    }

    /**
     * 处理打卡，利用 adb 命令点击屏幕完成打卡
     * 不同屏幕坐标位置不同，可以在开发者选项中开启查看屏幕坐标：Developer options -> Input -> Pointer location
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (!timeForPunch()) {
            stopSelf();
            return;
        }

        Log.d(this.getClass().getSimpleName(), "onHandleIntent: start punching...");

        // 唤醒屏幕
        wakeUp();
        // 上滑解锁
        swipe("720", "2320", "720", "1320");
        SystemClock.sleep(1000);

        // 输入 PIN 码解锁
        inputPinIfNeeded();
        SystemClock.sleep(3000);

        showToast("打开钉钉");
        startAppLauncher(DD_PACKAGE_NAME);
        SystemClock.sleep(10000);

        showToast("点击中间菜单");
        clickXY("700", "2325");
        SystemClock.sleep(5000);

        showToast("点击考勤打卡");
        clickXY("540", "1800");
        SystemClock.sleep(10000);

        showToast("点击打卡");
        clickXY("700", punchPositionY);
        SystemClock.sleep(5000);

        showToast("点击拍照");
        clickXY("710", "2280");
        SystemClock.sleep(8000);

        showToast("点击 OK");
        clickXY("710", "2281");
        SystemClock.sleep(5000);

        startAppLauncher(getPackageName());

        // 更新 UI
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date());
        EventBus.getDefault().post(new PunchFinishedEvent(punchType, currentTime));
        Log.d(this.getClass().getSimpleName(), "onHandleIntent: punch finished");

        close();

        stopSelf();
    }

    /**
     * 检查是否是打卡时间
     *
     * @return true for punching time
     */
    private boolean timeForPunch() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.CHINA);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        if (dayOfWeek == Calendar.SUNDAY) {
            return false;
        } else if (dayOfWeek == Calendar.SATURDAY) {
            if (hourOfDay == 9 && minute == 55) {
                punchType = PunchType.CLOCK_IN;
                punchPositionY = "920";
                return true;
            }
            if (hourOfDay == 17 && minute == 10) {
                punchType = PunchType.CLOCK_OUT;
                punchPositionY = "1550";
                return true;
            }
        } else {
            if (hourOfDay == 8 && minute == 25) {
                punchType = PunchType.CLOCK_IN;
                punchPositionY = "920";
                return true;
            }
            if (hourOfDay == 17 && minute == 10) {
                punchType = PunchType.CLOCK_OUT;
                punchPositionY = "1550";
                return true;
            }
        }

        return false;
    }

    /**
     * 唤醒屏幕
     */
    private void wakeUp() {
        boolean screenOn = powerManager.isScreenOn();
        if (!screenOn) {
            PowerManager.WakeLock wl = powerManager.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            wl.acquire(10000);
            wl.release();
        }

        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");
        keyguardLock.reenableKeyguard();
        keyguardLock.disableKeyguard();
    }

    /**
     * 如果需要的话输入解锁码
     */
    private void inputPinIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (keyguardManager.isDeviceLocked()) {
                clickXY("315", "1305");
                clickXY("715", "2115");
                clickXY("715", "1305");
                clickXY("315", "1600");
                clickXY("1105", "2115");
            }
        }
    }

    /**
     * 启动应用
     */
    private void startAppLauncher(String packageName) {
        Intent intent = this.getPackageManager().getLaunchIntentForPackage(packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * 显示 Toast 消息
     */
    private void showToast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ToastUtil.showToast(text);
            }
        });
    }
}