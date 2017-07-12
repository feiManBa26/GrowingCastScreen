package com.growing.castscreen.localSocket;

import android.support.annotation.IntDef;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * File: LClient.java
 * Author: ejiang
 * Version: V100R001C01
 * Create: 2017-06-22 13:16
 */

public interface LClient {

    /**
     * 创建socket连接，并准备接收数据。
     *
     * @param ip
     * @param port
     */
    void connect(String ip, int port);

    /**
     * 断开连接。
     */
    boolean disConnect();

    boolean isConnected();

    void send(byte[] bytes, ISendCallBack callback);

    void send(int type);

    void sendStr(String strData);

    void send(byte[] bytes);

    void sendFile(InputStream inputStream, String fileName) throws IOException;


    /**
     * @return 返回socket连接状态
     */
    @ClientState
    int getConnectState();

    /**
     * 正在连接
     */
    int STATE_CONNECTING = 0x0001;

    /**
     * 已连接
     */
    int STATE_CONNECTED = 0x0002;
    /**
     * 连接失败
     */
    int STATE_CONNECT_FAILED = 0x0003;
    /**
     * 已断开连接
     */
    int STATE_DISCONNECT = 0x0004;

    @IntDef({STATE_CONNECTING, STATE_CONNECTED, STATE_CONNECT_FAILED, STATE_DISCONNECT})
    @Retention(RetentionPolicy.SOURCE)
    @interface ClientState {
    }

}
