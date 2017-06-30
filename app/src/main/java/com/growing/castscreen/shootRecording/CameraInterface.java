package com.growing.castscreen.shootRecording;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import com.growing.castscreen.base.BaseApplication;
import com.growing.castscreen.utils.CamParaUtil;
import com.growing.castscreen.utils.FileUtil;
import com.growing.castscreen.utils.ImageUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * File: CameraInterface.java
 * Author: ejiang
 * Version: V100R001C01
 * Create: 2017-06-28 13:53
 */

public class CameraInterface {
    private static final String TAG = CameraInterface.class.getName();
    private static CameraInterface mCameraInterface;
    private boolean isPreviewing = false;
    private float mPreviwRate = -1f;
    private Camera mCamera;
    private Camera.Parameters mParams;
    private String mFilePath; //拍照缓存文件地址
    private Bitmap mRotaBitmap;
    private CamOpenOverCallback mCallback;
    private int cameraId; //摄像头

    private CameraInterface() {
    }

    public interface CamOpenOverCallback {
        public void cameraHasOpened();

        public void getCameraBitmap(Bitmap bitmap);
    }

    public synchronized static CameraInterface getInstence() {
        if (mCameraInterface == null) {
            mCameraInterface = new CameraInterface();
        }
        return mCameraInterface;
    }

    /**
     * 打开相机
     *
     * @param cameraId 相机前后摄像头0后摄像头1前摄像头
     * @param callback
     */
    public void doOpenCamera(int cameraId, CamOpenOverCallback callback) {
        try {
            Log.i(TAG, "doOpenCamera: open-----------");
            mCamera = Camera.open(cameraId);
            Log.i(TAG, "doOpenCamera: open--------------over");
            callback.cameraHasOpened();
            this.cameraId = cameraId;
            this.mCallback = callback;
        } catch (Exception e) {
            e.printStackTrace();
            BaseApplication.showTost("请打开应用的相机权限！");
        }
    }

    /**
     * 开启预览模式
     */
    public void doStartPreview(Context context, SurfaceHolder holder, float previewRate) {
        Log.i(TAG, "doStartPreview: isStartPreview");
        if (isPreviewing) {
            mCamera.stopPreview();
            return;
        }

        if (mCamera != null) {
            mParams = mCamera.getParameters();

            mParams.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式
            CamParaUtil.getInstance().printSupportPictureSize(mParams);
            CamParaUtil.getInstance().printSupportPreviewSize(mParams);
            //设置PreviewSize和PictureSize
            Camera.Size pictureSize = CamParaUtil.getInstance().getPropPictureSize(
                    mParams.getSupportedPictureSizes(), previewRate, 1080);
            mParams.setPictureSize(pictureSize.width, pictureSize.height);
            Camera.Size previewSize = CamParaUtil.getInstance().getPropPreviewSize(
                    mParams.getSupportedPreviewSizes(), previewRate, 1080);
            mParams.setPreviewSize(previewSize.width, previewSize.height);

//            mCamera.setDisplayOrientation(90);
            followScreenOrientation(context, mCamera);

            CamParaUtil.getInstance().printSupportFocusMode(mParams);
            List<String> focusModes = mParams.getSupportedFocusModes();
            if (focusModes.contains("auto")) {
                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            mCamera.setParameters(mParams);

            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();//开启预览
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            isPreviewing = true;
            mPreviwRate = previewRate;

            mParams = mCamera.getParameters(); //重新get一次
            Log.i(TAG, "最终设置:PreviewSize--With = " + mParams.getPreviewSize().width
                    + "Height = " + mParams.getPreviewSize().height);
            Log.i(TAG, "最终设置:PictureSize--With = " + mParams.getPictureSize().width
                    + "Height = " + mParams.getPictureSize().height);
        }
    }

    private void followScreenOrientation(Context context, Camera camera) {
        final int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            camera.setDisplayOrientation(180);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            camera.setDisplayOrientation(90);
        }
    }

    /**
     * 停止预览
     */
    public void doStopCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            isPreviewing = false;
            mPreviwRate = -1f;
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 拍照
     */
    public void doTakePicture() {
        if (isPreviewing && (mCamera != null)) {
            mCamera.takePicture(mShutterCallback, null, mJpegPictureCallback);
        }
    }

    /*为了实现拍照的快门声音及拍照保存照片需要下面三个回调变量*/
    Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback()
            //快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
    {
        public void onShutter() {
            // TODO Auto-generated method stub
            Log.i(TAG, "myShutterCallback:onShutter...");
        }
    };
    Camera.PictureCallback mRawCallback = new Camera.PictureCallback()
            // 拍摄的未压缩原数据的回调,可以为null
    {

        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            Log.i(TAG, "myRawCallback:onPictureTaken...");

        }
    };
    Camera.PictureCallback mJpegPictureCallback = new Camera.PictureCallback()
            //对jpeg图像数据的回调,最重要的一个回调
    {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i(TAG, "myJpegCallback:onPictureTaken...");
            Bitmap b = null;
            if (null != data) {
                b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
                mCamera.stopPreview();
                isPreviewing = false;
            }
            //保存图片到sdcard
            if (null != b) {
                //设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。
                //图片竟然不能旋转了，故这里要旋转下
                mRotaBitmap = ImageUtil.getRotateBitmap(b, cameraId == 0 ? 90.0f : -90.0f);
                if (mCallback != null) {
                    mCallback.getCameraBitmap(mRotaBitmap);
                }
                mFilePath = FileUtil.saveBitmap(mRotaBitmap);
            }
            //拍摄完成后-- 选择 上传--是否重拍--删除之前拍摄照片 --重新拍摄

        }
    };


    /**
     * 再次进入预览--是否删除之前拍摄(重拍)
     * 1.保存图片必须成功 拿到保存图片位置
     *
     * @param isRemake
     */
    public void oneMoreTimeEnterCamera(boolean isRemake) {
        if (isRemake) {
            if (mFilePath != null && mFilePath.length() > 0) {
                File file = new File(mFilePath);
                if (file.exists()) {
                    file.delete();
                }
            }
            mCamera.startPreview();
            isPreviewing = true;
        } else {
            mCamera.startPreview();
            isPreviewing = true;
        }
    }

}
