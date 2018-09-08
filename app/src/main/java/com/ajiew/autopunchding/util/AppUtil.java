package com.ajiew.autopunchding.util;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

/**
 * author: aaron.chen
 * created on: 2018/9/8 10:00
 * description:
 */
public class AppUtil {

    private static OutputStream os;

    private AppUtil() {
    }

    /**
     * 强退应用
     *
     * @param packageName
     */
    public static void stopApp(String packageName) {
        String cmd = "am force-stop " + packageName + " \n";
        exec(cmd);
    }

    /**
     * 强退服务
     *
     * @param fullServiceName 完整的服务名，包含包名 e.g. package.name/service.name
     */
    public static void stopService(String fullServiceName) {
        String cmd = "adb shell am stopservice " + fullServiceName + " \n";
        exec(cmd);
    }

    /**
     * 滑动屏幕
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    public static void swipe(String x1, String y1, String x2, String y2) {
        String cmd = String.format("input swipe %s %s %s %s \n", x1, y1, x2, y2);
        exec(cmd);
    }

    /**
     * 点击
     *
     * @param x
     * @param y
     */
    public static void clickXY(String x, String y) {
        Log.d(AppUtil.class.getSimpleName(), "clickXY: " + x + ", " + y);
        String cmd = String.format("input tap %s %s \n", x, y);
        exec(cmd);
    }

    /**
     * 执行 ADB 命令
     *
     * @param cmd adb 命令
     */
    public static void exec(String cmd) {
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
     * 关闭通道，执行完命令记得调用该方法。
     */
    public static void close() {
        try {
            if (os != null) {
                os.close();
            }
            os = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
