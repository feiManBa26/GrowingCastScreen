package com.growing.castscreen.camera;

import android.content.Context;
import android.hardware.Camera;

import com.growing.castscreen.base.BaseApplication;

/**
 * File: CameraHelper.java
 * Author: ejiang
 * 相机帮助类--参数设置
 * Version: V100R001C01
 * Create: 2017-06-27 17:29
 */

public class CameraHelper {
    private Context mContext;
    private Camera mCamera;

    public CameraHelper(Context context) {
        mContext = context;
    }

    /**
     * 获取相机对象
     */
    public void getCamera() {
        //判断权限
        if (isCameraCanUse()) {
            Camera mCamera = Camera.open();
            Camera.Parameters mCameraParameters = mCamera.getParameters();


        } else {
            BaseApplication.showTost("请打开应用的视像头权限！");
        }
    }

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
