package com.growing.castscreen.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.growing.castscreen.localSocket.LClient;
import com.growing.castscreen.localSocket.SocketIOCallback;
import com.growing.castscreen.localSocket.TcpClient;

/**
 * File: CastScreenServices.java
 * Author: ejiang
 * 1.开启后台服务：(连接：Socket--客户端发送信息到服务端/serverSocket：发送录屏信息到服务端)
 * Version: V100R001C01
 * Create: 2017-06-21 13:42
 */

public class CastScreenServices extends Service {
    private static final String TAG = CastScreenServices.class.getName();
    private static CastScreenServices mCastScreenServices;

    public static LClient getmLClient() {
        return mCastScreenServices.mLClient;
    }

    public LClient mLClient;

    public static Intent getIntent(Context context) {
        return new Intent(context, CastScreenServices.class).setAction("castScreenServices");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        this.mCastScreenServices = this;
        mLClient = new TcpClient(new SocketIOCallback() {
            @Override
            public void onConnect(LClient transceiver) {

            }

            @Override
            public void onDisconnect() {

            }

            @Override
            public void onConnectFailed(Exception ex) {

            }

            @Override
            public void onReceive(String data) {
                Log.i("TAG", "onReceive: data:服务端发送的信息：" + data);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLClient != null) {
            mLClient.disConnect();
            mLClient = null;
        }
    }
}
