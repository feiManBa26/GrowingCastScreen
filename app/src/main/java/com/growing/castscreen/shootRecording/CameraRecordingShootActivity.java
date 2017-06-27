package com.growing.castscreen.shootRecording;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.growing.castscreen.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * File: CameraRecordingShootActivity.java
 * Author: ejiang
 * Version: V100R001C01
 * Create: 2017-06-27 17:01
 */

public class CameraRecordingShootActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    @BindView(R.id.surfaceView)
    SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceViewHolder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_shoot_recording);
        ButterKnife.bind(this);
        mSurfaceViewHolder = mSurfaceView.getHolder();
        mSurfaceViewHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
