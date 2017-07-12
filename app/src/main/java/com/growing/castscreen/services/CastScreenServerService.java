package com.growing.castscreen.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * File: CastScreenServerService.java
 * Author: ejiang
 * Version: V100R001C01
 * 投屏后台服务
 * Create: 2017-07-12 11:47
 */

public class CastScreenServerService extends Service {
    private static CastScreenServerService castScreenServerService;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;

    public static MediaProjection getMediaProjection() {
        return castScreenServerService.mMediaProjection;
    }

    public static void setMediaProjection(MediaProjection mediaProjection) {
        castScreenServerService.mMediaProjection = mediaProjection;
    }

    public static MediaProjectionManager getmMediaProjectionManager() {
        return castScreenServerService.mMediaProjectionManager == null ? null : castScreenServerService.mMediaProjectionManager;
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, CastScreenServerService.class).setAction("castScreenServerServices");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
