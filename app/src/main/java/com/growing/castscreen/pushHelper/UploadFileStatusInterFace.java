package com.growing.castscreen.pushHelper;

/**
 * File: UploadFileStatusInterFace.java
 * Author: ejiang
 * Version: V100R001C01
 * Create: 2017-07-13 9:27
 */

public abstract interface UploadFileStatusInterFace {
    void uploadComplete();

    void uploadError(Exception e);
}
