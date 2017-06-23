package com.growing.castscreen.pushHelper;

import android.support.annotation.NonNull;

import com.growing.castscreen.localSocket.ISendCallBack;
import com.growing.castscreen.localSocket.LClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * File: ClientPushHelper.java
 * Author: ejiang
 * Version: V100R001C01
 * Create: 2017-06-23 14:40
 * 客户端socket传输帮助类
 */

public class ClientPushHelper {
    private static LClient mClient; //传输回调接口
    private static ClientPushHelper mClientPushHelper;
    private static final String PUSH_TYPE = "UTF-8";

    public synchronized static ClientPushHelper getInterest(LClient lClient) {
        if (mClientPushHelper == null) {
            mClientPushHelper = new ClientPushHelper();
            if (mClient == null) {
                mClientPushHelper.mClient = lClient;
            }
        }
        return mClientPushHelper;
    }

    /**
     * 推送字符串
     *
     * @param strInformation
     * @param callback
     * @throws UnsupportedEncodingException
     */
    private void pushStr(@NonNull String strInformation, ISendCallBack callback)
            throws UnsupportedEncodingException {
        if (strInformation == null) return;
        //推送类型字符串
        String type = TransferCommandTypes.SendString.ordinal() + "";
        byte[] bytes = type.getBytes(PUSH_TYPE);
        int length = bytes.length;
        mClient.send(bytes, callback);
        //推送字符串字符流
        byte[] strInformationBytes = strInformation.getBytes(PUSH_TYPE);
        String informationLong = strInformationBytes.length + "";
        byte[] informationLongBytes = informationLong.getBytes(PUSH_TYPE);
        mClient.send(informationLongBytes, callback);
        mClient.send(strInformationBytes, callback);
    }


    /**
     * 推送文件--单个文件
     * @param filePath
     * @param callBack
     * @throws IOException
     *
     */
    private void pushFile(String filePath, ISendCallBack callBack) throws IOException {
        //创建 file对象
        File file = new File(filePath);
        if (file.exists()) {
            //如果存在
            String fileName = file.getName();
            file.length();
            InputStream inputStream = new FileInputStream(file);//文件输入流
            subcontractingPushFile(callBack, fileName, inputStream);

        }
    }

    /**
     * 分包传输
     * @param callBack
     * @param fileName
     * @param inputStream
     * 发送文件步骤：1.文件名 2.文件大小 3.包的大小 4.包的数量 5.循环字节流
     * @throws IOException
     */
    private void subcontractingPushFile(ISendCallBack callBack, String fileName, InputStream inputStream) throws IOException {
        //获取文件输入流的字节长度
        int available = inputStream.available();

        //文件名称字符流
        byte[] fileNameBytes = fileName.getBytes(PUSH_TYPE);
        mClient.send(fileNameBytes, callBack);

        //文件长度字符流
        String fileLengthStr = available + "";
        byte[] fileLengthBytes = fileLengthStr.getBytes(PUSH_TYPE);
        mClient.send(fileLengthBytes, callBack);

        //包大小字节流
        String packageLength = 1024 + "";
        byte[] packageLengthBytes = packageLength.getBytes(PUSH_TYPE);
        mClient.send(packageLengthBytes, callBack);

        //>=1024
        if (available > 1024) {
            int endLength = available % 1024;
            if (endLength != 0) {
                int fileLength = available - endLength; //取余
                int fileNum = fileLength / 1024; //包的倍数

                int filePackageNum = fileNum + 1; //包的数量
                String filePackageNumStr = filePackageNum + "";
                byte[] filePackageNumStrBytes = filePackageNumStr.getBytes("UTF-8");
                mClient.send(filePackageNumStrBytes, callBack);

                //发送数据包--循环字节流
                for (int i = 0; i < filePackageNum; i++) {
                    byte[] bytes = null;
                    if (i == filePackageNum - 1) {
                        bytes = new byte[fileLength];
                    } else {
                        bytes = new byte[1024];
                    }
                    inputStream.read(bytes);
                    mClient.send(bytes, callBack);
                }
            } else {
                int fileNum = available / 1024; //包的倍数--包的数量
                String filePackageNumStr = fileNum + "";
                byte[] filePackageNumStrBytes = filePackageNumStr.getBytes("UTF-8");
                mClient.send(filePackageNumStrBytes, callBack);
                //发送数据包--循环字节流
                for (int i = 0; i < fileNum; i++) {
                    byte[] bytes = null;
                    bytes = new byte[1024];
                    inputStream.read(bytes);
                    mClient.send(bytes, callBack);
                }
            }
        } else {
            //文件字符流
            byte[] bytes = new byte[available];
            inputStream.read(bytes);
            mClient.send(bytes, callBack);
        }
    }

}
