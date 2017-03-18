package com.example.kevin.trail;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import zephyr.android.HxMBT.BTClient;
import zephyr.android.HxMBT.ConnectListenerImpl;
import zephyr.android.HxMBT.ConnectedEvent;
import zephyr.android.HxMBT.ZephyrPacketArgs;
import zephyr.android.HxMBT.ZephyrPacketEvent;
import zephyr.android.HxMBT.ZephyrPacketListener;
import zephyr.android.HxMBT.ZephyrProtocol;

/**
 * Created by JY on 2017-03-17.
 * using sample code from HxM Example Android Project
 */

public class HRSensorConnector extends ConnectListenerImpl{

    private Handler oldHandler;
    private Handler newHandler;
    private int HEART_RATE=0x100;
    private int HR_DIST_SPEED_PACKET=0x26;
    private HRSpeedDistPacketInfo SENSOR_PACKET=new HRSpeedDistPacketInfo();
    String TAG="HRSensorConnector";

    public HRSensorConnector(Handler handler, Handler _newHandler){
        super(handler, null);
        oldHandler=handler;
        newHandler=_newHandler;
    }

    public void Connected(ConnectedEvent<BTClient> eventArgs){
        Log.d(TAG, "Start to establish connection with HeartRate Sensor");
        ZephyrProtocol protocol = new ZephyrProtocol(eventArgs.getSource().getComms());
        protocol.addZephyrPacketEventListener(new ZephyrPacketListener() {
            @Override
            public void ReceivedPacket(ZephyrPacketEvent zephyrPacketEvent) {
                ZephyrPacketArgs zephyrPacketArgs=zephyrPacketEvent.getPacket();

                if (HR_DIST_SPEED_PACKET==zephyrPacketArgs.getMsgID()){
                    byte [] DataArray=zephyrPacketArgs.getBytes();
                    //get heart rate here
                    HEART_RATE=SENSOR_PACKET.GetHeartRate(DataArray);
                    Log.d(TAG, "Receiving Heart Rate data");

                    Message msg =newHandler.obtainMessage(HEART_RATE);
                    Bundle bundle=new Bundle();
                    bundle.putString("Heart Rate", String.valueOf(HEART_RATE));
                    msg.setData(bundle);
                    newHandler.sendMessage(msg);
                    Log.d(TAG, "Heart Rate is "+HEART_RATE);
                }
            }
        });
    }
}
