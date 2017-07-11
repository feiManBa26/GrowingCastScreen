package com.growing.castscreen.shootRecording;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.growing.castscreen.R;
import com.growing.castscreen.camera.CameraHelper;
import com.growing.castscreen.utils.DisplayUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.growing.castscreen.R.id.img_recording_btn;

/**
 * File: CameraRecordingShootActivity.java
 * Author: ejiang
 * Version: V100R001C01
 * Create: 2017-06-27 17:01
 */

public class CameraRecordingShootActivity extends AppCompatActivity implements CameraInterface.CamOpenOverCallback {

    @BindView(R.id.surfaceView)
    CameraSurfaceView mSurfaceView;
    //------关闭/切换摄像头
    @BindView(R.id.imageView)
    ImageView mimgClose;
    @BindView(R.id.imageView2)
    ImageView mimgSwith;
    //------录制/拍摄/拍摄结果展示---------
    @BindView(R.id.re_camera)
    RelativeLayout mreCamera;
    @BindView(R.id.img_camera)
    ImageView imgCamera;
    @BindView(R.id.img_recording)
    ImageView imgRecording;
    @BindView(R.id.img_shoot)
    ImageView imgShootCover;
    //---------上传/重新拍摄/拍摄结果展示-----
    @BindView(R.id.re_camera_shoot)
    RelativeLayout mReCameraUpFile;
    @BindView(R.id.img_upload_file)
    ImageView imgUploadFile;
    @BindView(R.id.txt_remake)
    TextView txtRemake;
    //---------录制/录制时间/开始录制/结束录制-----------
    @BindView(R.id.re_recording)
    RelativeLayout mReRecoding;
    @BindView(R.id.ll_recording_time)
    LinearLayout llRecoridngTime;
    @BindView(R.id.txt_recoding_time)
    TextView txtRecodingTime;
    @BindView(img_recording_btn)
    ImageView imgReRecording;
    @BindView(R.id.img_go_back_camera)
    ImageView imgGoBackCamera;

    private Camera mCamera;
    private boolean isShoot = false;
    private CameraHelper mHelper;
    private static final String TAG = CameraRecordingShootActivity.class.getName();
    private SurfaceHolder mHolder;
    float previewRate = -1;
    private boolean isDetectionCamera = false; //默认后置摄像头
    private boolean isRecording = false; //录制视频切换开关
    private MediaPlayer mPlayer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doOpenCamera(0);
        setContentView(R.layout.activity_shoot_recording);
        ButterKnife.bind(this);
        initViewPreview();
        //判断摄像头是否显示切换按钮
        if (!detectionCameraNum()) {
            mimgSwith.setVisibility(View.INVISIBLE);
        } else {
            mimgSwith.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化摄像机
     */
    private void doOpenCamera(final int cameraId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                CameraInterface.getInstence().doOpenCamera(cameraId, CameraRecordingShootActivity.this);
            }
        }).start();
    }

    /**
     * 检测相机摄像头数量
     * camera 摄像头数量 0 后置摄像头 1 前置摄像头 (注: 后置双摄像头暂不考虑)
     *
     * @return
     */
    private boolean detectionCameraNum() {
        int cameras = Camera.getNumberOfCameras();

        if (cameras >= 2) {
            return true;
        }
        return false;
    }

    @OnClick({R.id.imageView, R.id.imageView2, R.id.img_shoot, R.id.img_recording, R.id.img_camera
            , img_recording_btn, R.id.txt_remake, R.id.img_go_back_camera})
    public void OnClick(View view) {
        switch (view.getId()) {
            case R.id.img_camera: //拍摄
                showCameraEndUi();
                CameraInterface.getInstence().doTakePicture();
                break;
            case R.id.imageView: //关闭拍摄界面
                CameraRecordingShootActivity.this.finish();
                break;
            case R.id.imageView2: //切换摄像头
                CameraInterface.getInstence().doStopCamera();
                if (!isDetectionCamera) {
                    doOpenCamera(1);
                } else {
                    doOpenCamera(0);
                }
                isDetectionCamera = !isDetectionCamera;
                break;
            case R.id.img_recording: //显示录制页面
                clickRecording();
                break;
            case R.id.txt_remake: //重新拍摄
                resetCameraUi();
                CameraInterface.getInstence().oneMoreTimeEnterCamera(true);
                break;
            case R.id.img_go_back_camera: //录像切换回拍摄页面
                clickGoBackCameraUI();
                CameraInterface.getInstence().closeMediaRes();
                CameraInterface.getInstence().doStartPreview(this, mSurfaceView.getHolder());
                break;
            case R.id.img_recording_btn: //开始录制视频
                Log.i(TAG, "OnClick: 开始录制视频~");
                //初始化录制视频--资源
                //暂停录制视频--释放资源
                if (!isRecording) {
                    CameraInterface.getInstence().closeMediaRes();
                    CameraInterface.getInstence().startRecording();
                    imgReRecording.setImageResource(R.drawable.castscreen_shoot_shooting_end);
                    imgGoBackCamera.setVisibility(View.INVISIBLE);
                } else {
                    imgReRecording.setImageResource(R.drawable.castscreen_shoot_shooting_start);
                    CameraInterface.getInstence().stopRecording();
                    imgGoBackCamera.setVisibility(View.VISIBLE);
                    CameraInterface.getInstence().PlayRecordingMedia();
                }
                isRecording = !isRecording;
                break;
        }
    }

    @Override
    protected void onPause() {
        //先判断是否正在播放
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        }
        super.onPause();
    }


    /**
     * 初始化surfaceView配置
     */
    private void initViewPreview() {
        ViewGroup.LayoutParams params = mSurfaceView.getLayoutParams();
        Point point = DisplayUtils.getScreenMetrics(this);
        params.height = point.y;
        params.width = point.x;
        mSurfaceView.setLayoutParams(params);
        previewRate = DisplayUtils.getScreenRate(this); //默认全屏的比例预览
    }


    @Override
    public void cameraHasOpened() {
        SurfaceHolder holder = mSurfaceView.getHolder();
        CameraInterface.getInstence().doStartPreview(this, holder);
    }

    @Override
    public void getCameraBitmap(Bitmap bitmap) {
        if (bitmap == null) return;
        imgShootCover.setImageBitmap(bitmap);
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, CameraRecordingShootActivity.class);
    }


    /**
     * 点击拍摄--拍摄完毕后显示的Ui
     */
    private void showCameraEndUi() {
        //隐藏拍摄按钮
        imgCamera.setVisibility(View.INVISIBLE);
        //隐藏录制按钮
        imgRecording.setVisibility(View.INVISIBLE);
        //显示录制结果按钮
        imgShootCover.setVisibility(View.VISIBLE);
        //显示上传--重拍按钮
        mReCameraUpFile.setVisibility(View.VISIBLE);
        //隐藏切换摄像头按钮
        mimgSwith.setVisibility(View.INVISIBLE);
    }


    /**
     * 重新拍摄显示的Ui
     * 上传完毕显示的Ui
     */
    private void resetCameraUi() {
        //显示拍摄按钮
        imgCamera.setVisibility(View.VISIBLE);
        //显示录制按钮
        imgRecording.setVisibility(View.VISIBLE);
        //显示录制结果
        imgShootCover.setVisibility(View.VISIBLE);
        //隐藏上传--重拍按钮
        mReCameraUpFile.setVisibility(View.INVISIBLE);
        //显示切换摄像头按钮
        mimgSwith.setVisibility(View.VISIBLE);
    }

    /**
     * 点击上传显示的Ui--上传开始
     */


    /**
     * 点击录制显示的Ui
     */
    private void clickRecording() {
        //隐藏拍摄UI
        mreCamera.setVisibility(View.INVISIBLE);
        //隐藏拍摄完成页面
        mReCameraUpFile.setVisibility(View.INVISIBLE);
        //显示录制页面
        mReRecoding.setVisibility(View.VISIBLE);
        //隐藏切换摄像头页面
        mimgSwith.setVisibility(View.INVISIBLE);
    }

    /**
     * 回退到拍摄UI
     */
    private void clickGoBackCameraUI() {
        //显示拍摄Ui
        mreCamera.setVisibility(View.VISIBLE);
        //隐藏拍摄完成页面
        mReCameraUpFile.setVisibility(View.INVISIBLE);
        //隐藏录制页面
        mReRecoding.setVisibility(View.INVISIBLE);
        //显示切换摄像头页面
        mimgSwith.setVisibility(View.VISIBLE);
    }
}
