package com.dwin.common_app.util;

import android.content.Context;
import android.content.SharedPreferences;

public final class SpUtils {
    public static final int INVALID_INT = -1;
    public static final String INVALID_STRING = "";

    private static final String NAME = "demo";
    private static final String STARTUP_APP_KEY = "startup_app";
    private static final String STARTUP_APP_DELAY_TIME_KEY = "startup_app_delay_time";

    private Context mContext;

    private SpUtils(Context context) {
        mContext = context.getApplicationContext();
    }

    private static class SingletonHolder {
        private static SpUtils INSTANCE;

        private static SpUtils getInstance(Context context) {
            if (INSTANCE == null) {
                synchronized (SingletonHolder.class) {
                    if (INSTANCE == null) {
                        INSTANCE = new SpUtils(context);
                    }
                }
            }
            return INSTANCE;
        }
    }

    public static SpUtils getInstance(Context context) {
        return SingletonHolder.getInstance(context);
    }

    public void setStartupApp(String packageName) {
        putString(STARTUP_APP_KEY, packageName);
    }

    public String getStartupApp() {
        return getSp().getString(STARTUP_APP_KEY, INVALID_STRING);
    }

    public void setStartupAppDelayTime(int time) {
        putInt(STARTUP_APP_DELAY_TIME_KEY, time);
    }

    public int getStartupAppDelayTime() {
        return getSp().getInt(STARTUP_APP_DELAY_TIME_KEY, INVALID_INT);
    }

    private void putInt(String key, int value) {
        getSp().edit().putInt(key, value).apply();
    }

    private void putString(String key, String value) {
        getSp().edit().putString(key, value).apply();
    }

    private SharedPreferences getSp() {
        return mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }
}
