package com.growing.castscreen.localSocket;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import static com.growing.castscreen.base.BaseApplication.getAppData;

/**
 * File: PhoneSocketThread.java
 * Author: ejiang
 * Version: V100R001C01
 * Create: 2017-06-21 15:59
 */

public class PhoneSocketThread extends Thread {
    private Socket mSocket;
    OutputStream outputStream;
    BufferedOutputStream bufferedOutputStream;
    PrintWriter printWriter;

    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    public PhoneSocketThread(Socket socket) {
        super(PhoneSocketThread.class.getName());
        this.mSocket = socket;
    }

    @Override
    public void run() {
        try {
            mSocket = new Socket(getAppData().getServerIp(), getAppData().getServerProt());
            mSocket.setKeepAlive(true);
            mSocket.setSoTimeout(10 * 1000); //连接超时时间设置位1分钟
            outputStream = mSocket.getOutputStream();
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            printWriter = new PrintWriter(bufferedOutputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendData(String str) {

        if (printWriter != null) {
            printWriter.write(str);
            printWriter.flush();
        }
    }


    public void endStop() throws IOException {
        if (mSocket != null) mSocket.close();
        if (outputStream != null) outputStream.close();
        if (bufferedOutputStream != null) bufferedOutputStream.close();
        if (printWriter != null) printWriter.close();
    }
}
