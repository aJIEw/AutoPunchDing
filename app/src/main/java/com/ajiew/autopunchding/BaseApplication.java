package com.ajiew.autopunchding;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

/**
 * author: aaron.chen
 * created on: 2018/9/6 17:22
 * description:
 */
public class BaseApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        context = base;
    }

    public static Context getContext() {
        return context;
    }
}
