package com.growing.castscreen.settingData;

/**
 * File: AppData.java
 * Author: ejiang
 * Version: V100R001C01
 * Create: 2017-06-21 15:34
 */

public class AppData {
    private String serverIp;
    private int serverProt = -1;

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerProt() {
        return serverProt;
    }

    public void setServerProt(int serverProt) {
        this.serverProt = serverProt;
    }
}
