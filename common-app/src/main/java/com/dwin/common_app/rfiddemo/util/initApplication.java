package com.dwin.common_app.rfiddemo.util;

import android.app.Application;


public class initApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtils.init(this);
    }

}
