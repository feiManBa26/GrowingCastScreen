package com.growing.castscreen.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * File: WifiConnectionUtils.java
 * Author: ejiang
 * Version: V100R001C01
 * wifi 连接检测工具类
 * 获取wifi ip地址 设定端口号
 * Create: 2017-07-12 14:12
 */

public class WifiConnectionUtils {


    public static boolean isWifiConnection(Context context) {
        if (context == null) return false;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            return true;
        } else {
            return false;
        }
    }

    public static void openWifi(Context context) {
        if (context == null) return;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
    }


    public static String getWifiIp(Context context) {
        if (context == null) return null;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        int address = connectionInfo.getIpAddress();
        return intToIp(address).toString();
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }
}
