package com.example.kevin.trail;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Created by JY on 2017-03-22.
 */

public class BTBroadcastReceiver extends BroadcastReceiver {

    public static void Register(Context context, BTBroadcastReceiver reciver){
		/*Sending a message to android that we are going to initiate a pairing request*/
        IntentFilter filter = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
        /*Registering a new BTBroadcast receiver from the Main Activity context with pairing request event*/
        context.registerReceiver(reciver, filter);
    }

    public static void Register(Context context){
        Register(context, new BTBroadcastReceiver());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Log.d("BTIntent", intent.getAction());
        String address = intent.getExtras().get("android.bluetooth.device.extra.DEVICE").toString();
        Log.d("BTIntent address", address);
        Log.d("BTIntent variant", intent.getExtras().get("android.bluetooth.device.extra.PAIRING_VARIANT").toString());
        BluetoothDevice device = adapter.getRemoteDevice(address);
        BTPinSetter.SetPin(device, "1234");
    }
}
