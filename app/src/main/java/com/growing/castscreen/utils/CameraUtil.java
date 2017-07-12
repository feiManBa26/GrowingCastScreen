package com.growing.castscreen.utils;

import android.hardware.Camera;

/**
 * 相机开启检测工具类
 */
public class CameraUtil {

    /**
     * @param
     * @return
     * @author miao
     * @createTime 2017年2月10日
     * @lastModify 2017年2月10日
     */
    public static boolean isCameraCanUse() {
        boolean canUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            canUse = false;
        }
        if (canUse) {
            if (mCamera != null)
                mCamera.release();
            mCamera = null;
        }
        return canUse;
    }
}
