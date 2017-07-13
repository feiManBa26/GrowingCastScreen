package com.growing.castscreen.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.growing.castscreen.MainActivity;
import com.growing.castscreen.base.BaseApplication;

/**
 * File: WifiRecevier.java
 * Author: ejiang
 * Version: V100R001C01
 * Create: 2017-07-12 17:14
 */

public class WifiRecevier extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case WifiManager.RSSI_CHANGED_ACTION:
                break;
            case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                System.out.println("网络状态改变");
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    System.out.println("wifi网络连接断开");
                    if(BaseApplication.isRun()){
                        BaseApplication.getmApplication().startActivity(new Intent(BaseApplication.getmApplication(), MainActivity.class).
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    //获取当前wifi名称
                    System.out.println("连接到网络 " + wifiInfo.getSSID());
                }
                break;
            case WifiManager.WIFI_STATE_CHANGED_ACTION:
                int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
                if (wifistate == WifiManager.WIFI_STATE_DISABLED) {
                    System.out.println("系统关闭wifi");
                } else if (wifistate == WifiManager.WIFI_STATE_ENABLED) {
                    System.out.println("系统开启wifi");
                }
                break;

        }
    }
}
