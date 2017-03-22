package com.example.kevin.trail;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import zephyr.android.HxMBT.BTClient;
import zephyr.android.HxMBT.ConnectedEvent;
import zephyr.android.HxMBT.ConnectedListener;
import zephyr.android.HxMBT.ZephyrPacketArgs;
import zephyr.android.HxMBT.ZephyrPacketEvent;
import zephyr.android.HxMBT.ZephyrPacketListener;
import zephyr.android.HxMBT.ZephyrProtocol;

/**
 * Created by JY on 2017-03-22.
 */

public class HRSensorHandler implements ZephyrPacketListener, ConnectedListener<BTClient> {
    private BTClient _bt;
    private int HR_SPD_DIST_PACKET =0x26;
    private IHeartRateReciever mReciver = null;

    public HRSensorHandler(Context context)
    {
        context = context.getApplicationContext();

        BTBroadcastReceiver.Register(context);
        BTBondReceiver.Register(context);
    }

    public void Connect()
    {
        if (_bt != null)
            throw new RuntimeException("Already connected");

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter.getState() == BluetoothAdapter.STATE_TURNING_ON)
            throw new RuntimeException("Bluethooth start not completed");

        if (adapter.getState() != BluetoothAdapter.STATE_ON)
            throw new RuntimeException("Bluethooth is not active");

        boolean pairedDeviceFound = false;

        for (BluetoothDevice pairedDevice : adapter.getBondedDevices())
        {
            if (pairedDevice.getName().startsWith("HXM"))
            {
                pairedDeviceFound = true;
                _bt = new BTClient(adapter, pairedDevice.getAddress());

                if(_bt.IsConnected())
                    break;

                _bt = null;
            }
        }

        if (!pairedDeviceFound)
            throw new RuntimeException("No paired device found");

        if(_bt == null)
            throw new RuntimeException("No connected device found");

        _bt.addConnectedEventListener(this);
        _bt.start();
    }

    public void Disconnect()
    {
        if (_bt == null)
            throw new RuntimeException("Not connected");

        _bt.removeConnectedEventListener(this);
        _bt.Close();
        _bt = null;
    }

    @Override
    public void Connected(ConnectedEvent<BTClient> eventArgs) {
        System.out.println(String.format("Connected to BioHarness %s.", eventArgs.getSource().getDevice().getName()));
        ZephyrProtocol protocol = new ZephyrProtocol(eventArgs.getSource().getComms());
        protocol.addZephyrPacketEventListener(this);
    }

    @Override
    public void ReceivedPacket(ZephyrPacketEvent eventArgs) {
        ZephyrPacketArgs msg = eventArgs.getPacket();

        if (HR_SPD_DIST_PACKET==msg.getMsgID())
        {

            byte [] data = msg.getBytes();
            int heartRate = (int) data[9] & 0xFF;

            if (mReciver != null)
                mReciver.heartRateReceived(heartRate);
        }
    }

    public void setReciver(IHeartRateReciever mReciver) {
        this.mReciver = mReciver;
    }
}
