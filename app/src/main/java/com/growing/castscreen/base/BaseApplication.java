package com.growing.castscreen.base;

import android.app.Application;
import android.widget.Toast;

import com.growing.castscreen.base.local.PreferencesHelper;
import com.growing.castscreen.services.CastScreenServices;


/**
 * File: BaseApplication.java
 * Author: ejiang
 * Version: V100R001C01
 * Create: 2017-06-21 15:33
 */

public class BaseApplication extends Application {
    private static BaseApplication mApplication;
    private AppData mAppData;
    private PreferencesHelper mPreferencesHelper;

    public static BaseApplication getmApplication() {
        return mApplication;
    }

    public static PreferencesHelper getPreferencesHelper() {
        return mApplication.mPreferencesHelper;
    }

    public static AppData getAppData() {
        return mApplication.mAppData;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        mAppData = new AppData(mApplication);
        startService(CastScreenServices.getIntent(this));
        mPreferencesHelper = new PreferencesHelper(mApplication);

    }

    public static void showTost(String msg) {
        if (msg == null) return;
        Toast.makeText(mApplication.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

}
