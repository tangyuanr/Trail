package com.example.kevin.trail;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import zephyr.android.HxMBT.BTClient;

public class HRActivity extends AppCompatActivity implements IHeartRateReciever{

    private HRSensorHandler hrHandler;
    private TextView hrText=null;
    private TextView connectionStatus=null;
    private Button connectButton=null;
    private Button disconnectButton=null;


    String TAG="HRActivity";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hr);

        hrText=(TextView)findViewById(R.id.hrView);
        connectionStatus=(TextView)findViewById(R.id.connectStatus);
        connectButton=(Button)findViewById(R.id.hrConnectButton);
        disconnectButton=(Button)findViewById(R.id.hrDisconnectButton);

        hrHandler=new HRSensorHandler(this);

        //first disable the DISCONNECT button when connection is not even established yet
        disconnectButton.setEnabled(false);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectClicked();
            }
        });

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectClicked();
            }
        });
    }

    @Override
    public void heartRateReceived(int heartRate){
        Message msg=new Message();
        msg.getData().putInt("HeartRate", heartRate);
        newHandler.sendMessage(msg);
    }

    private void connectClicked(){
        try{
            connectionStatus.setText("Connecting...");
            hrHandler.Connect();
            connectionStatus.setText("Connected to HxM");
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
            hrHandler.setReciver(HRActivity.this);
        }catch (RuntimeException e) {
            connectionStatus.setText(e.getMessage());
        }
    }

    private void disconnectClicked(){
        try{
            hrHandler.setReciver(null);
            hrHandler.Disconnect();
        }catch (RuntimeException e){
            connectionStatus.setText(e.getMessage());
        }
        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);
        hrText.setText("Heart rate will appear here");
    }

    final Handler newHandler=new Handler(){
        public void handleMessage(Message msg){
            int heartRate=msg.getData().getInt("HeartRate");
            hrText.setText(Integer.toString(heartRate));
        }
    };
}
