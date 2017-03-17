package com.example.kevin.trail;

import android.os.Handler;

import zephyr.android.HxMBT.ConnectListenerImpl;

/**
 * Created by JY on 2017-03-17.
 * used sample code from HxM Example Android Project
 */

public class HRSensorConnector extends ConnectListenerImpl{

    private Handler handler;
    private int HEART_RATE=0;

    public HRSensorConnector(Handler handler, Handler newHandler){
        super(handler, null);

    }
}
