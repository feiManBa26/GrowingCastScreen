package com.growing.castscreen.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.view.WindowManager;

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
     * 1.检测手机有几个摄像头
     * 2. int cameras = Camera.getNumberOfCameras();
     *
     * @param openCameraId --相机参数Id 0 后摄像头 1 前摄像头自拍
     */
    public Camera getCamera(int openCameraId) {
        //判断权限
        try {
            Camera mCamera = Camera.open(openCameraId);
//            Camera.Parameters mCameraParameters = mCamera.getParameters();
//            mCameraParameters.setPictureFormat(PixelFormat.JPEG); //设置图片格式
//            mCameraParameters.setPreviewSize(getWindowWidth(), getWindowHeigth()); //设置屏幕的大小
//            mCameraParameters.setPictureSize(getWindowMetricsWidth(), getWindowMetricsHeight()); //设置图片的分辨率
//            mCamera.setParameters(mCameraParameters);
            return mCamera;
        } catch (Exception e) {
            e.printStackTrace();
            BaseApplication.showTost("请打开应用的视像头权限！");
        }
        return null;
    }

    /**
     * 屏幕的宽
     *
     * @return
     */
    private int getWindowWidth() {
        WindowManager windowManager = (WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE);
        int width = windowManager.getDefaultDisplay().getWidth();
        return width;
    }

    /**
     * 屏幕的高
     *
     * @return
     */
    private int getWindowHeigth() {
        WindowManager windowManager = (WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE);
        int height = windowManager.getDefaultDisplay().getHeight();
        return height;
    }

    /**
     * 获取屏幕的分辨率 宽
     *
     * @return
     */
    private int getWindowMetricsWidth() {
        return mContext.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 获取屏幕的分辨率 高
     *
     * @return
     */
    private int getWindowMetricsHeight() {
        return mContext.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 设置相机预览方向
     *
     * @param context
     * @param camera
     */
    public void followScreenOrientation(Context context, Camera camera) {
        final int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            camera.setDisplayOrientation(180);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            camera.setDisplayOrientation(90);
        }
    }
}
