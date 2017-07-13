package com.growing.castscreen.localSocket;


import com.growing.castscreen.pushHelper.UploadFileStatusInterFace;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
                disConnect();
            }
            connectType = STATE_CONNECTING; //正在连接
            int connectNum = 0;
            boolean isConnect = false;
            while (connectNum < 2) {
                connectNum++;
                try {
                    mSocket = new Socket();
//                    mSocket.setKeepAlive(true);
                    mSocket.setSoTimeout(5 * 1000);//inputStream read 超时时间
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
                    connectType = STATE_CONNECT_FAILED;
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
            connectType = STATE_CONNECT_FAILED;
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
    public synchronized boolean disConnect() {
        if (mSocket != null) {
            try {
//                mSocket.shutdownOutput();
//                mSocket.shutdownInput();
                closeDataInputStream(mDataInputStream);
                closeDataOutputStream(mDataOutputStream);
                mSocket = null;
                connectType = STATE_DISCONNECT; //断开连接
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
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
                    disConnect();
                    e.printStackTrace();
                }
            } else {
                callback.onFailed(new Exception("socket is not connected"));
                disConnect();
            }
        }

    }

    @Override
    public void send(int type) {
        if (mSocket != null) {
            synchronized (TcpClient.class) {
                if (isConnected()) {
                    try {
                        mDataOutputStream.writeInt(type);
                        mDataOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void sendStr(String strData) {
        if (mSocket != null) {
            synchronized (TcpClient.class) {
                if (isConnected()) {
                    try {
                        mDataOutputStream.writeInt(1);
                        mDataOutputStream.flush();
                        byte[] bytes = strData.getBytes("UTF-8");
                        mDataOutputStream.write(bytes);
                        mDataOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    @Override
    public void sendFile(InputStream inputStream, String fileName) throws IOException {
        if (fileName == null || inputStream == null) return;
        if (mSocket != null) {
            synchronized (TcpClient.class) {
                if (isConnected()) {
                    mDataOutputStream.writeInt(1);
                    mDataOutputStream.writeUTF(fileName);
                    mDataOutputStream.writeLong(inputStream.available());
                    int available = inputStream.available();
                    int length = 0;
                    byte[] bytes = new byte[1024];
                    long progress = 0;
                    while ((length = inputStream.read(bytes, 0, bytes.length)) != -1) {
                        mDataOutputStream.write(bytes, 0, length);
                        mDataOutputStream.flush();
                        progress += length;
                        System.out.println("| " + (100 * progress / available) + "% |");
                    }
                }
            }
        }

    }

    @Override
    public void sendLocalFile(String filePath, UploadFileStatusInterFace interFace) {
        if (filePath == null || filePath.isEmpty()) return;
        File file = new File(filePath);
        String fileName = file.getName();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            if (inputStream == null) return;
            if (mSocket != null) {
                synchronized (TcpClient.class) {
                    if (isConnected()) {
                        mDataOutputStream.writeInt(0);
                        mDataOutputStream.writeUTF(fileName);
                        mDataOutputStream.writeLong(inputStream.available());
                        int available = inputStream.available();
                        int length = 0;
                        byte[] bytes = new byte[1024];
                        long progress = 0;
                        while ((length = inputStream.read(bytes, 0, bytes.length)) != -1) {
                            mDataOutputStream.write(bytes, 0, length);
                            mDataOutputStream.flush();
                            progress += length;
                            System.out.println("| " + (100 * progress / available) + "% |");
                        }
                        inputStream.close();
                        if (interFace != null) {
                            interFace.uploadComplete();
                        }
                    }
                }
            }
        } catch (IOException e) {
            if (interFace != null) {
                interFace.uploadError(e);
            }
        }
    }

    @Override
    public int getConnectState() {
        return connectType;
    }
}
