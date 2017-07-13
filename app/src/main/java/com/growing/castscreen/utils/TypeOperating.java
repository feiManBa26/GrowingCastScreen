package com.growing.castscreen.utils;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * File: TypeOperating.java
 * Author: ejiang
 * Version: V100R001C01
 * Create: 2017-07-12 15:16
 */

public class TypeOperating {
    public static final int TYPE_WIFI_OPEN = 0;
    public static final int TYPE_CONNECTION = 1;
    public static final int TYPE_SOCKET_ISCLIENT = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_WIFI_OPEN, TYPE_CONNECTION,TYPE_SOCKET_ISCLIENT})
    public @interface typeOperating {

    }
}
