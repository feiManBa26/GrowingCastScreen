package com.growing.castscreen.base;

import android.app.Application;

import com.growing.castscreen.services.CastScreenServices;
import com.growing.castscreen.settingData.AppData;

/**
 * File: BaseApplication.java
 * Author: ejiang
 * Version: V100R001C01
 * Create: 2017-06-21 15:33
 */

public class BaseApplication extends Application {
    private static BaseApplication mApplication;
    private AppData mAppData;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        mAppData = new AppData();
        startService(CastScreenServices.getIntent(this));
    }

    public static AppData getAppData() {
        return mApplication.mAppData;
    }
}
