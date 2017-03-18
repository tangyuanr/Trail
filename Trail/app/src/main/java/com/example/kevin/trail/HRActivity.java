package com.example.kevin.trail;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import zephyr.android.HxMBT.BTClient;

public class HRActivity extends AppCompatActivity {

    BluetoothAdapter adapter=null;
    BTClient btClient;
    HRSensorConnector connector;
    private final int HEART_RATE=0x100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hr);

        //send bluetooth pairing request to android
        IntentFilter filter=new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
        //Registering a new BTBroadcast receiver from the Main Activity context with pairing request event
        this.getApplicationContext().registerReceiver(new BTBroadcastReceiver(), filter);
        // Registering the BTBondReceiver in the application that the status of the receiver has changed to Paired
        IntentFilter filter2 = new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED");
        this.getApplicationContext().registerReceiver(new BTBondReceiver(), filter2);


        //connect to layout objects
        TextView textView = (TextView)findViewById(R.id.connectStatus);
        final String errorText="NO CONNECTION TO HxM!";
        textView.setText(errorText);

        Button connectButton = (Button)findViewById(R.id.hrConnectButton);
        if (connectButton!=null){
            connectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String MacID = "00:07:80:0E:9F:F4";
                    adapter=BluetoothAdapter.getDefaultAdapter();

                    Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
                    if (pairedDevices.size() > 0)
                    {
                        for (BluetoothDevice device : pairedDevices)
                        {
                            if (device.getName().startsWith("HXM"))
                            {
                                BluetoothDevice btDevice = device;
                                MacID = btDevice.getAddress();
                                break;
                            }
                        }
                    }

                    BluetoothDevice device =adapter.getRemoteDevice(MacID);
                    String deviceName=device.getName();
                    btClient=new BTClient(adapter, MacID);
                    connector = new HRSensorConnector(newHandler, newHandler);
                    btClient.addConnectedEventListener(connector);


                    //if connected, displays connection status
                    if (btClient.IsConnected()){
                        btClient.start();
                        TextView textView1=(TextView)findViewById(R.id.connectStatus);
                        String ErrorText="Connected to HxM device "+deviceName;
                        textView1.setText(ErrorText);
                    }
                    else
                    {
                        TextView tv = (TextView) findViewById(R.id.connectStatus);
                        String ErrorText  = "Unable to Connect !";
                        tv.setText(ErrorText);
                    }
                }
            });
        }

        //handle disconnection here
        Button disconnectButton=(Button)findViewById(R.id.hrDisconnectButton);
        if (disconnectButton!=null){
            disconnectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tv = (TextView)findViewById(R.id.connectStatus);
                    String ErrorText = "Disconnected from HxM";
                    tv.setText(ErrorText);

                    btClient.removeConnectedEventListener(connector);
                    btClient.Close();
                }
            });
        }
    }

    //implement BTBroadcastReceiver
    private class BTBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive (Context context, Intent intent){
            Bundle bundle = intent.getExtras();
            try{
                BluetoothDevice device = adapter.getRemoteDevice(bundle.get("android.bluetooth.device.extra.DEVICE").toString());
                Method method = BluetoothDevice.class.getMethod("convertPinToBytes", new Class[] {String.class} );
                byte[] pin = (byte[])method.invoke(device, "1234");
                method = device.getClass().getMethod("setPin", new Class [] {pin.getClass()});
                Object result = method.invoke(device, pin);
                Log.d("BTtest", result.toString());
            }catch (NoSuchMethodException e){
                e.printStackTrace();
            }catch (IllegalAccessException e){
                e.printStackTrace();
            }catch (InvocationTargetException e){
                e.printStackTrace();
            }
        }
    }

    //implement BTBondReceiver
    private class BTBondReceiver extends BroadcastReceiver{
        @Override
        public void onReceive (Context context, Intent intent){
            Bundle bundle=intent.getExtras();
            BluetoothDevice device = adapter.getRemoteDevice(bundle.get("android.bluetooth.device.extra.DEVICE").toString());
        }
    }

    final Handler newHandler=new Handler(){
        public void handleMessage(Message msg){
            TextView tv;
            switch (msg.what){
                //read heart rate here
                case HEART_RATE:
                    String hrInfo=msg.getData().getString("HeartRate");
                    tv=(TextView)findViewById(R.id.hrView);
                    if (tv!=null) tv.setText(hrInfo);
                    break;
            }
        }
    };
}
