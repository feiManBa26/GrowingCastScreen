package com.growing.castscreen;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.growing.castscreen.base.BaseAppCommpatActivity;
import com.growing.castscreen.localSocket.LClient;
import com.growing.castscreen.services.CastScreenServices;
import com.growing.castscreen.utils.CommonUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.growing.castscreen.CaptureActivity.RESULT_CODE_QR_SCAN;
import static com.growing.castscreen.base.BaseApplication.getAppData;


public class MainActivity extends BaseAppCommpatActivity {
    private static final String TAG = MainActivity.class.getName();

    private static final int CASTSCREEN_TYPE = 100; //二维码录制回调

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.imageView2)
    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //初始化toolbar
        initToolBar();

        //获取wifi服务
//        @SuppressLint("WifiManagerLeak")
//        WifiManager wifiManager = (WifiManager) getSystemService(getApplicationContext().WIFI_SERVICE);
//        //判断wifi是否开启
//        if (!wifiManager.isWifiEnabled()) {
//            wifiManager.setWifiEnabled(true);
//        }
//        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//        int ipAddress = wifiInfo.getIpAddress();
//        String ip = intToIp(ipAddress);
//        Log.i(TAG, "onCreate: " + ip);

    }

    private String intToIp(int i) {

        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    private void initToolBar() {
        mToolbar.setNavigationIcon(R.mipmap.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setTitle("手机互联");
        mToolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @OnClick(R.id.imageView2)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView2:
                /**
                 * 1.判断权限是否开启
                 * 1.判断当前应用是否开启相机权限
                 */
                requestPermission(new String[]{Manifest.permission.CAMERA}, 0x00011);
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
                                    try {
                                        InputStream inputStream = getResources().getAssets().open("Android开发艺术探索.pdf");
                                        LClient lClient = CastScreenServices.getmLClient();
                                        lClient.connect(getAppData().getServerIp(), getAppData().getServerProt());
//                                        lClient.sendFile(inputStream, "Android开发艺术探索.pdf");
                                        lClient.sendStr("你是一朵小菊花");
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }
                    }
                    break;
            }
        }

    }

    public enum TransferCommandTypes {
        SendFile,
        SendString,
        Screen
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
                if (CommonUtil.isCameraCanUse()) {
                    Intent intent = new Intent(this, CaptureActivity.class);
                    startActivityForResult(intent, CASTSCREEN_TYPE);
                } else {
                    Toast.makeText(this, "请打开此应用的摄像头权限！", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
