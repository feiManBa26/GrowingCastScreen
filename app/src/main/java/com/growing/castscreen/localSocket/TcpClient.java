package com.growing.castscreen.localSocket;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * File: TcpClient.java
 * Author: ejiang
 * Version: V100R001C01
 * Create: 2017-06-21 14:23
 */

public class TcpClient implements LClient {
    private Socket mSocket;
    private SocketIOCallback mSocketIOCallback;
    private DataOutputStream mDataOutputStream;
    private DataInputStream mDataInputStream;

    @ClientState
    private int connectType; //连接状态

    public TcpClient(SocketIOCallback socketIOCallback) {
        this.mSocketIOCallback = socketIOCallback;
    }

    @Override
    public void connect(String ip, int port) {
        try {
            /**
             * 1.判断socket是否为空
             * 2.判断socket是否是连接状态
             */
            if (isConnected()) {
                disConnect(false);
            }
            connectType = STATE_CONNECTING; //正在连接
            int connectNum = 0;
            boolean isConnect = false;
            while (connectNum < 5) {
                connectNum++;
                try {
                    mSocket = new Socket();
//                    mSocket.setKeepAlive(true);
//                    mSocket.setSoTimeout(2 * 3 * 60 * 1000);//inputStream read 超时时间
//                    mSocket.setTcpNoDelay(true);
                    mSocket.connect(new InetSocketAddress(ip, port));
                    if (mSocket.isConnected()) {
                        mDataInputStream = new DataInputStream(mSocket.getInputStream());
                        mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());
                    }
                    isConnect = true;
                    connectType = STATE_CONNECTED;
                    break; //连接成功--拿到输入输出流
                } catch (IOException e) {
                    e.printStackTrace();
                    Thread.sleep(500);
                }
            }

            if (isConnect) {
                //连接成功--读取当前数据
                readerData();
            } else {
                //重新连接失败后提示用户
                connectType = STATE_CONNECT_FAILED;
            }
        } catch (Exception e) {
            e.printStackTrace();
            //重新连接失败后提示用户
        }
    }

    /**
     * 读取当前socket连接中服务端发送的数据
     */
    private void readerData() {
        while (isConnected()) {
            String str = null;
            try {
                byte[] bytes = new byte[1024];
                int read = mDataInputStream.read(bytes);
                str = new String(bytes, 0, read, "UTF-8");
                if (mSocketIOCallback != null) {
                    mSocketIOCallback.onReceive(str);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            break;
        }
    }

    /**
     * 关闭输入流
     */
    private void closeDataInputStream(DataInputStream dataInputStream) throws IOException {
        if (dataInputStream != null) {
            dataInputStream.close();
        }
    }

    /**
     * 关闭输出流
     */
    private void closeDataOutputStream(DataOutputStream dataOutputStream) throws IOException {
        if (dataOutputStream != null) {
            dataOutputStream.close();
        }
    }

    @Override
    public synchronized void disConnect(boolean needReconnect) {
        if (mSocket != null) {
            try {
                closeDataInputStream(mDataInputStream);
                closeDataOutputStream(mDataOutputStream);
                mSocket.shutdownOutput();
                mSocket.shutdownInput();
                mSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        connectType = STATE_DISCONNECT; //断开连接
    }

    @Override
    public boolean isConnected() {
        return mSocket != null && mSocket.isConnected();
    }

    @Override
    public void send(byte[] bytes, ISendCallBack callback) { //发送数据
        synchronized (TcpClient.class) {
            if (isConnected()) {
                try {
                    mDataOutputStream.write(bytes);
                    mDataOutputStream.flush();
                } catch (IOException e) {
                    callback.onFailed(e);
                    disConnect(true);
                    e.printStackTrace();
                }
            } else {
                callback.onFailed(new Exception("socket is not connected"));
                disConnect(true);
            }
        }

    }

    @Override
    public void sendStr(String strData) {
        synchronized (TcpClient.class) {
            if (isConnected()) {
                try {
                    mDataOutputStream.writeUTF(strData);
                    mDataOutputStream.flush();
                } catch (IOException e) {
                    disConnect(true);
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void send(byte[] bytes) {
        synchronized (TcpClient.class) {
            if (isConnected()) {
                try {
                    mDataOutputStream.write(bytes, 0, bytes.length);
                    mDataOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                disConnect(true);
            }
        }
    }

    @Override
    public int getConnectState() {
        return connectType;
    }
}
