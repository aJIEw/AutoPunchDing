package com.ajiew.autopunchding;

import android.app.IntentService;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ajiew.autopunchding.event.PunchFinishedEvent;
import com.ajiew.autopunchding.event.PunchType;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class PunchService extends IntentService {

    /**
     * 钉钉包名
     */
    private static final String DD_PACKAGE_NAME = "com.alibaba.android.rimet";

    private OutputStream os;

    private PunchType punchType;
    private PowerManager powerManager;
    private KeyguardManager keyguardManager;

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
        SystemClock.sleep(5000);

        // 打开钉钉
        startAppLauncher(DD_PACKAGE_NAME);
        SystemClock.sleep(5000);

        // 点击中间菜单
        clickXY("700", "2325");
        SystemClock.sleep(5000);

        // 点击考勤打卡
        clickXY("540", "1800");
        SystemClock.sleep(10000);

        // 点击打卡按钮
        clickXY("700", "1550");
        SystemClock.sleep(5000);

        // 点击拍照按钮
        clickXY("710", "2280");
        SystemClock.sleep(8000);

        // 点击 OK 按钮
        clickXY("710", "2281");
        SystemClock.sleep(5000);

        // 退出钉钉
        stopApp(DD_PACKAGE_NAME);

        // 打卡应用本田
        startAppLauncher(getPackageName());
        SystemClock.sleep(3000);

        // 发送打卡完成事件
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date());
        EventBus.getDefault().post(new PunchFinishedEvent(punchType, currentTime));
        Log.d(this.getClass().getSimpleName(), "onHandleIntent: punch finished");

        // 关闭通道
        try {
            if (os != null) {
                os.close();
            }
            os = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                return true;
            }
            if (hourOfDay == 17 && minute == 10) {
                punchType = PunchType.CLOCK_OUT;
                return true;
            }
        } else {
            if (hourOfDay == 8 && minute == 25) {
                punchType = PunchType.CLOCK_IN;
                return true;
            }
            if (hourOfDay == 17 && minute == 10) {
                punchType = PunchType.CLOCK_OUT;
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
     * 滑动屏幕
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    private void swipe(String x1, String y1, String x2, String y2) {
        String cmd = String.format("input swipe %s %s %s %s \n", x1, y1, x2, y2);
        exec(cmd);
    }

    /**
     * 启动应用
     */
    private void startAppLauncher(String packageName) {
        Intent intent = this.getPackageManager().getLaunchIntentForPackage(packageName);
        startActivity(intent);
    }

    /**
     * 点击
     *
     * @param x
     * @param y
     */
    public void clickXY(String x, String y) {
        Log.d(this.getClass().getSimpleName(), "clickXY: " + x + ", " + y);
        String cmd = String.format("input tap %s %s \n", x, y);
        exec(cmd);
        SystemClock.sleep(1000);
    }

    /**
     * 执行ADB命令：input tap 125 340
     *
     * @param cmd adb 命令
     */
    public final void exec(String cmd) {
        try {
            if (os == null) {
                os = Runtime.getRuntime().exec("su").getOutputStream();
            }
            os.write(cmd.getBytes());
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 强退应用
     *
     * @param packageName
     */
    private void stopApp(String packageName) {
        String cmd = "am force-stop " + packageName + " \n";
        exec(cmd);
    }

    /**
     * 强退服务
     *
     * @param fullServiceName 完整的服务名，包含包名 e.g. package.name/service.name
     */
    private void stopService(String fullServiceName) {
        String cmd = "adb shell am stopservice " + fullServiceName + " \n";
        exec(cmd);
    }

}