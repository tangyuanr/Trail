package com.example.kevin.trail;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by JY on 2017-03-22.
 */

public class BTBondReceiver extends BroadcastReceiver {
    public static void Register(Context context, BTBondReceiver reciver){
        // Registering the BTBondReceiver in the application that the status of the receiver has changed to Paired
        IntentFilter filter = new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED");
        context.registerReceiver(reciver, filter);
    }

    public static void Register(Context context){
        Register(context, new BTBondReceiver());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Bundle b = intent.getExtras();
        BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
        Log.d("Bond state", "BOND_STATED = " + device.getBondState());
    }
}
