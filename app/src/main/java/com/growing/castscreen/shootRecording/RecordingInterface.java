package com.growing.castscreen.shootRecording;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.IOException;
import java.util.Timer;

/**
 * File: RecordingInterface.java
 * Author: ejiang
 * (录制视频相关方法集成类)
 * 1.初始化
 * 2.开始录制
 * 3.暂停
 * 4.继续
 * 5.停止释放资源
 * Version: V100R001C01
 * Create: 2017-07-10 16:18
 */

public class RecordingInterface implements MediaRecorder.OnErrorListener {
    private static RecordingInterface anInterface;
    private MediaRecorder mMediaRecorder;
    private Timer mTimer;
    private int mRecordMaxTime; //一次拍摄最长时间
    private Camera mCamera;
    private File mVecordFile;

    private RecordingInterface() {
    }

    /**
     * 抛出 视频地址
     *
     * @return
     */
    public File getVecordFile() {
        return mVecordFile;
    }

    public synchronized static RecordingInterface getInstence() {
        if (anInterface == null) {
            anInterface = new RecordingInterface();
        }
        return anInterface;
    }

    /**
     * 初始化录制视频选项
     *
     * @param mCamera
     * @param mSurfaceHolder
     */
    private void initRecording(Camera mCamera, SurfaceHolder mSurfaceHolder) throws IOException {
        this.mCamera = mCamera;
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.reset();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setOnErrorListener(this);
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);// 视频源
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 音频源
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);// 视频输出格式
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);// 音频格式
        mMediaRecorder.setVideoSize(1280, 720);// 设置分辨率：
        // mMediaRecorder.setVideoFrameRate(16);// 这个我把它去掉了，感觉没什么用
        mMediaRecorder.setVideoEncodingBitRate(1 * 1024 * 512);// 设置帧频率，然后就清晰了
        mMediaRecorder.setOrientationHint(90);// 输出旋转90度，保持竖屏录制
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);// 视频录制格式
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
     * @param mCamera
     * @param mSurfaceHolder
     * @return
     */
    public void startRecording(Camera mCamera, SurfaceHolder mSurfaceHolder) {
        if (mCamera == null || mSurfaceHolder == null) return;
        try {
            createRecordDir();
            initRecording(mCamera, mSurfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
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
        // 创建文件
        long timeMillis = System.currentTimeMillis();
        try {
            mVecordFile = File.createTempFile(timeMillis + "", ".mp4", vecordDir);//mp4格式
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
            mMediaRecorder.setPreviewDisplay(null);
            try {
                mMediaRecorder.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mMediaRecorder.setOnErrorListener(null);
            try {
                mMediaRecorder.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mMediaRecorder = null;
        //释放资源
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onError(MediaRecorder recorder, int i, int i1) {
    }
}
