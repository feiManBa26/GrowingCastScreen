package com.growing.castscreen;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.growing.castscreen.base.BaseAppCommpatActivity;
import com.growing.castscreen.base.BaseApplication;
import com.growing.castscreen.localSocket.LClient;
import com.growing.castscreen.services.CastScreenServices;
import com.growing.castscreen.shootRecording.CameraRecordingShootActivity;
import com.growing.castscreen.utils.TypeOperating;
import com.growing.castscreen.utils.WifiConnectionUtils;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.growing.castscreen.CaptureActivity.RESULT_CODE_QR_SCAN;
import static com.growing.castscreen.base.BaseApplication.getAppData;
import static com.growing.castscreen.localSocket.LClient.STATE_CONNECTED;
import static com.growing.castscreen.localSocket.LClient.STATE_CONNECTING;
import static com.growing.castscreen.localSocket.LClient.STATE_CONNECT_FAILED;
import static com.growing.castscreen.localSocket.LClient.STATE_DISCONNECT;


public class MainActivity extends BaseAppCommpatActivity {
    private static final String TAG = MainActivity.class.getName();
    private static final int CASTSCREEN_TYPE = 100; //二维码录制回调

    @BindView(R.id.re_cast_screen_success_ui)
    LinearLayout mReCastScreenSuccessUi;
    @BindView(R.id.re_cast_screen_ui)
    RelativeLayout mReCastScreenStartUi;
    @BindView(R.id.btn_disconnect)
    Button btnDisconnect;
    @BindView(R.id.txt_camera)
    TextView txtCamera;
    @BindView(R.id.txt_cast_screen)
    TextView txtCastScreen;
    @BindView(R.id.txt_local_album)
    TextView txtLocalAlbum;
    @BindView(R.id.txt_remote_control)
    TextView txtRemoteControl;
    @BindView(R.id.re_cast_screen)
    RelativeLayout reCastScreen;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initToolBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!WifiConnectionUtils.isWifiConnection(BaseApplication.getmApplication())) {
            showDialogMsg("启用改功能需要wifi联网是否开启wifi连接？", TypeOperating.TYPE_WIFI_OPEN);
        }
    }

    private void initToolBar() {
        mToolbar.setNavigationIcon(R.mipmap.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setTitle("手机互联");
        mToolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CastScreenServices.getmLClient().isConnected()) {
                    showDialogMsg("是否断开连接？", TypeOperating.TYPE_SOCKET_ISCLIENT);
                } else {
                    finish();
                }
            }
        });
    }

    @OnClick({R.id.re_cast_screen, R.id.btn_disconnect,
            R.id.txt_camera, R.id.txt_cast_screen,
            R.id.txt_local_album,
            R.id.txt_remote_control})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.re_cast_screen:
                /**
                 * 1.判断权限是否开启
                 * 1.判断当前应用是否开启相机权限
                 */
                requestPermission(new String[]{Manifest.permission.CAMERA}, 0x00011);
                break;
            case R.id.txt_camera:
                Log.i(TAG, "onClick: 照相机");
                startActivity(CameraRecordingShootActivity.getIntent(this));
                break;
            case R.id.txt_cast_screen:
                Log.i(TAG, "onClick: 投屏连接");
                startActivity(CastScreenActivity.getIntent(this));
                break;
            case R.id.txt_local_album:
                Log.i(TAG, "onClick: 本地相册");
                break;
            case R.id.txt_remote_control:
                Log.i(TAG, "onClick: 功能遥控");
                break;
            case R.id.btn_disconnect:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        LClient lClient1 = CastScreenServices.getmLClient();
                        if (lClient1.disConnect()) {
                            mHandler.sendEmptyMessage(2);
                        }
                    }
                }).start();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CODE_QR_SCAN) {
            switch (requestCode) {
                case CASTSCREEN_TYPE:
                    Bundle bundle = data.getExtras();
                    String scanResult = bundle.getString("qr_scan_result");
                    //将扫描出的信息显示出来
                    Log.i(TAG, "onActivityResult: 扫描二维码回调信息：" + scanResult);
                    if (scanResult != null && scanResult.length() > 0) {
                        int i = scanResult.indexOf(":");
                        if (i != -1) {
                            String pcIp = scanResult.substring(0, i);
                            String pcPcot = scanResult.substring(i + 1, scanResult.length());
                            Log.i(TAG, "onClick: " + pcIp);
                            Log.i(TAG, "onClick: " + pcPcot);
                            getAppData().setServerIp(pcIp);
                            getAppData().setServerProt(Integer.parseInt(pcPcot));
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    mHandler.sendEmptyMessage(3);
                                    LClient lClient = CastScreenServices.getmLClient();
                                    lClient.connect(getAppData().getServerIp(), getAppData().getServerProt());
                                    int connectState = lClient.getConnectState();
                                    switch (connectState) {
                                        case STATE_CONNECTING: //正在连接
                                            break;
                                        case STATE_CONNECTED: //连接成功
                                            Message message = new Message();
                                            message.what = 0;
                                            mHandler.sendMessage(message);
                                            break;
                                        case STATE_CONNECT_FAILED: //连接失败
                                            Message message1 = new Message();
                                            message1.what = 1;
                                            mHandler.sendMessage(message1);
                                            break;
                                        case STATE_DISCONNECT: //已断开连接
                                            break;
                                    }
                                }
                            }).start();
                        }
                    }
                    break;
            }
        }
    }

    private Handler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        WeakReference<Context> mReference;

        public MyHandler(Context context) {
            mReference = new WeakReference<Context>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = (MainActivity) mReference.get();
            switch (msg.what) {
                case 0: //连接成功
                    activity.closeProgressDialog();
                    activity.mReCastScreenStartUi.setVisibility(View.INVISIBLE);
                    activity.mReCastScreenSuccessUi.setVisibility(View.VISIBLE);
                    break;
                case 1: //连接失败
                    Log.i(TAG, "handleMessage: 连接失败");
                    activity.closeProgressDialog();
                    activity.showDialogMsg("连接失败请检查~", TypeOperating.TYPE_CONNECTION);
                    break;
                case 2:
                    activity.mReCastScreenStartUi.setVisibility(View.VISIBLE);
                    activity.mReCastScreenSuccessUi.setVisibility(View.INVISIBLE);
                    break;
                case 3:
                    activity.showProgressDialog("正在验证连接请稍后");
                    break;
            }

        }
    }


    public static byte[] intToBytes(int value) {
        byte[] byte_src = new byte[4];
        byte_src[3] = (byte) ((value & 0xFF000000) >> 24);
        byte_src[2] = (byte) ((value & 0x00FF0000) >> 16);
        byte_src[1] = (byte) ((value & 0x0000FF00) >> 8);
        byte_src[0] = (byte) ((value & 0x000000FF));
        return byte_src;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void permissionSuccess(int requestCode) {
        super.permissionSuccess(requestCode);
        switch (requestCode) {
            case 0x00011:
                Intent intent = new Intent(this, CaptureActivity.class);
                this.startActivityForResult(intent, CASTSCREEN_TYPE);
                break;
        }
    }

    @Override
    public void goType(@TypeOperating.typeOperating int type) {
        switch (type) {
            case TypeOperating.TYPE_WIFI_OPEN:
                WifiConnectionUtils.openWifi(BaseApplication.getmApplication());
                break;
            case TypeOperating.TYPE_SOCKET_ISCLIENT:
                CastScreenServices.getmLClient().disConnect();
                finish();
                break;
        }
    }
}
