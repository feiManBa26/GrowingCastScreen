package com.growing.castscreen.localSocket;

/**
 * socket 连接状态
 * Created by jiantao on 2017/3/4.
 */
public interface SocketIOCallback {

    void onConnect(LClient transceiver);

    void onDisconnect();

    void onConnectFailed(Exception ex);

    void onReceive(String data);
}
