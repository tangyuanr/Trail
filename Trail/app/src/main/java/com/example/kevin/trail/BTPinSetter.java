package com.example.kevin.trail;

import android.bluetooth.BluetoothDevice;

import java.lang.reflect.Method;

/**
 * Created by JY on 2017-03-22.
 */

public class BTPinSetter {
    public static void SetPin(BluetoothDevice device, String pin){
        try {
            Method convertPinToBytesMethod = BluetoothDevice.class.getMethod("convertPinToBytes", new Class[] {String.class} );
            byte[] pinBytes = (byte[])convertPinToBytesMethod.invoke(device, "1234");
            Method setPinMethod = device.getClass().getMethod("setPin", new Class [] {pinBytes.getClass()});
            setPinMethod.invoke(device, pinBytes);
        } catch (Exception e1) {
            throw new RuntimeException("Could not set pin for bluethooth device");
        }
    }
}
