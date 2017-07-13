package com.growing.castscreen.base;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.widget.Toast;

import com.growing.castscreen.base.local.PreferencesHelper;
import com.growing.castscreen.services.CastScreenServices;


/**
 * File: BaseApplication.java
 * Author: ejiang
 * Version: V100R001C01
 * Create: 2017-06-21 15:33
 */

public class BaseApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private static BaseApplication mApplication;
    private AppData mAppData;
    private PreferencesHelper mPreferencesHelper;
    private static final String TAG = BaseApplication.class.getName();
    private boolean isRun;

    public static boolean isRun() {
        return mApplication.isRun;
    }

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
        registerActivityLifecycleCallbacks(this);
    }

    public static void showTost(String msg) {
        if (msg == null) return;
        Toast.makeText(mApplication.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

        isRun = true;
    }

    @Override
    public void onActivityStarted(Activity activity) {

        isRun = true;
    }

    @Override
    public void onActivityResumed(Activity activity) {

        isRun = true;
    }

    @Override
    public void onActivityPaused(Activity activity) {

        isRun = false;
    }

    @Override
    public void onActivityStopped(Activity activity) {

        isRun = false;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        isRun = false;
    }

    @Override
    public void onActivityDestroyed(Activity activity) {

        isRun = false;
    }
}
