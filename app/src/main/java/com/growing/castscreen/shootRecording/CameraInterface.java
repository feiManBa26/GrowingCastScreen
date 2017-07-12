package com.growing.castscreen.shootRecording;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
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

public class CameraInterface implements MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener,
        MediaPlayer.OnCompletionListener {
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
    private MediaRecorder mMediaRecorder;
    private File mVecordFile;
    private SurfaceHolder mSurfaceHolder;
    private MediaPlayer mPlayer;

    private CameraInterface() {
    }

    private File getVecordFile() {
        return mVecordFile;
    }

    public void doStopMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    @Override
    public void onInfo(MediaRecorder recorder, int i, int i1) {

    }

    @Override
    public void onCompletion(MediaPlayer player) {
        PlayRecordingMedia();
    }

    public interface CamOpenOverCallback {
        void cameraHasOpened();

        void getCameraBitmap(Bitmap bitmap);
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

    private void openCamera() {
        try {
            Log.i(TAG, "doOpenCamera: open-----------");
            mCamera = Camera.open();
            Log.i(TAG, "doOpenCamera: open--------------over");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 开启预览模式
     */
    public void doStartPreview(Context context, SurfaceHolder holder, float previewRate) {
        Log.i(TAG, "doStartPreview: isStartPreview");
        if (holder == null) return;
        if (mCamera == null) {
            openCamera();
        }
        this.mSurfaceHolder = holder;
        this.mPreviwRate = previewRate;
        mParams = mCamera.getParameters();
        mParams.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式
        CamParaUtil.getInstance().printSupportPictureSize(mParams);
        CamParaUtil.getInstance().printSupportPreviewSize(mParams);
        //设置PreviewSize和PictureSize
        Camera.Size pictureSize = CamParaUtil.getInstance().getPropPictureSize(
                mParams.getSupportedPictureSizes(), mPreviwRate, 800);
        mParams.setPictureSize(pictureSize.width, pictureSize.height);
        Camera.Size previewSize = CamParaUtil.getInstance().getPropPreviewSize(
                mParams.getSupportedPreviewSizes(), mPreviwRate, 800);
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
            e.printStackTrace();
        }
        mParams = mCamera.getParameters(); //重新get一次
        Log.i(TAG, "最终设置:PreviewSize--With = " + mParams.getPreviewSize().width
                + "Height = " + mParams.getPreviewSize().height);
        Log.i(TAG, "最终设置:PictureSize--With = " + mParams.getPictureSize().width
                + "Height = " + mParams.getPictureSize().height);
    }


    /**
     * 设置相机旋转角度
     *
     * @param context
     * @param camera
     */
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
        if (mCamera != null) {
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

    //=========================================录制视频=============================================

    /**
     * 初始化录制视频选项
     *
     * @param mCamera
     * @param mSurfaceHolder
     */
    private void initRecording(Camera mCamera, SurfaceHolder mSurfaceHolder) throws IOException {
        this.mCamera = mCamera;
        this.mSurfaceHolder = mSurfaceHolder;
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.reset();
        if (mCamera != null)
            mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setOnErrorListener(this);
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);// 视频源
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 音频源
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);// 视频输出格式
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);// 音频格式
        mMediaRecorder.setVideoSize(640, 480);// 设置分辨率
        // mMediaRecorder.setVideoFrameRate(16);// 这个我把它去掉了，感觉没什么用
        mMediaRecorder.setVideoEncodingBitRate(10 * 1024 * 512);// 设置帧频率，然后就清晰了
        mMediaRecorder.setOrientationHint(90);// 输出旋转90度，保持竖屏录制
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);// 视频录制格式
        // mediaRecorder.setMaxDuration(Constant.MAXVEDIOTIME * 1000);
        mMediaRecorder.setOutputFile(mVecordFile.getAbsolutePath());
        mMediaRecorder.prepare();
        try {
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 开始录制
     *
     * @return
     */
    public void startRecording() {
        if (mSurfaceHolder == null) return;
        try {
            /*创建本地缓存路径*/
            createRecordDir();
            /*初始化相机*/
            initCamera();
            initRecording(mCamera, mSurfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化录制摄像头
     *
     * @throws IOException
     * @author lip
     * @date 2015-3-16
     */
    private void initCamera() throws IOException {
        if (mCamera != null) {
            freeCameraResource();
        }
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
            freeCameraResource();
        }
        if (mCamera == null)
            return;
        setCameraParams();
        mCamera.setDisplayOrientation(90);
        mCamera.setPreviewDisplay(mSurfaceHolder);
        mCamera.startPreview();
        mCamera.unlock();
    }

    /**
     * 设置摄像头为竖屏
     *
     * @author lip
     * @date 2015-3-16
     */
    private void setCameraParams() {
        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            params.set("orientation", "portrait");
            mCamera.setParameters(params);
        }
    }

    /**
     * 释放摄像头资源
     *
     * @author liuyinjun
     * @date 2015-2-5
     */
    private void freeCameraResource() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();//停止捕获并将预览画面绘制到曲面上，并重置相机以进行将来调用startPreview（）。
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 设置录制视频保存路径
     *
     * @return
     */
    private void createRecordDir() {
        File sampleDir = new File(Environment.getExternalStorageDirectory() + File.separator + "RecordVideo/");
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        File vecordDir = sampleDir;
        try {
            mVecordFile = File.createTempFile("recording" + "", ".mp4", vecordDir);//mp4格式
            Log.d("Path:", mVecordFile.getAbsolutePath());
        } catch (IOException e) {
        }
    }

    /**
     * 停止录制视频
     */
    public void stopRecording() {
        //停止录制
        if (mMediaRecorder != null) {
            // 设置后不会崩
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setOnInfoListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            try {
                mMediaRecorder.stop();
                mMediaRecorder.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mMediaRecorder = null;
        //释放资源
        freeCameraResource();
    }

    @Override
    public void onError(MediaRecorder recorder, int i, int i1) {
        Log.i(TAG, "onError:  " + recorder.toString() + "       " + i + " " + i1);
    }

    //=====================================循环录制的视频=========================================

    /**
     * 循环播放录制的视频
     */
    public void PlayRecordingMedia() {
        if (mSurfaceHolder == null) return;

        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }
        try {
            mPlayer.reset();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //设置需要播放的视频
            String path = getVecordFile().getAbsolutePath();
            if (path != null && path.length() > 0) {
                mPlayer.setDataSource(path);
                mPlayer.setOnCompletionListener(this);
                //把视频画面输出到SurfaceView
                mPlayer.setDisplay(mSurfaceHolder);
                mPlayer.prepare();
                mPlayer.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂停播放 释放资源
     */
    public void closeMediaRes() {
        if (mPlayer == null) return;
        mPlayer.stop();
        mPlayer.setDisplay(null);
        mPlayer = null;
    }
}
