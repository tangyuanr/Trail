package com.example.kevin.trail;

import android.Manifest;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;

import static java.util.Collections.singleton;

public class MainActivity extends AppCompatActivity implements IHeartRateReciever{

    private sharedPreferenceHelper sharedPreferenceHelper;
    protected Button timerButtonlink = null;
    protected Button hikeButtonlink = null;
    protected Button bikeButtonlink = null;
    protected Button historyButtonlink = null;
    protected Button runButtonlink = null;
    protected Button mapButtonlink = null;
    protected Button sensorButtonlink=null;
    protected Button infoButtonlink = null;
    protected Button graphButton = null;
    DBHandler dbhandler;

    public static int heartRate;
    public static HRSensorHandler hrHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        sharedPreferenceHelper = new sharedPreferenceHelper(MainActivity.this);

        hrHandler=new HRSensorHandler(this);
        try {
            hrHandler.Connect();
            hrHandler.setReciver(MainActivity.this);
        }
        catch(RuntimeException e){
            Log.d("MainActivity", e.getMessage());
        }

        infoButtonlink = (Button) findViewById(R.id.profileButton);
        //timerButtonlink = (Button) findViewById(R.id.timerButton);
        hikeButtonlink = (Button) findViewById(R.id.hikingButton);
        bikeButtonlink = (Button) findViewById(R.id.bikingButton);
        historyButtonlink = (Button) findViewById(R.id.historyButton);
        runButtonlink = (Button) findViewById(R.id.runningButton);
        //mapButtonlink = (Button) findViewById(R.id.mapButton);
        graphButton = (Button) findViewById(R.id.statsButton);
        //sensorButtonlink = (Button) findViewById(R.id.sensorButton);
        dbhandler = new DBHandler(this);

        infoButtonlink.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                goToinfoActivity();
            }
        });

        hikeButtonlink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if (dbhandler.isRouteTableEmpty("Hiking")) {
                    Intent intent = new Intent(MainActivity.this, loggerActivity.class);
                    intent.putExtra("activityType", "Hiking");
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, routeManager.class);
                    intent.putExtra("activityType", "Hiking");
                    startActivity(intent);
                }

            }
        });


        graphButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, graphActivity.class);
                startActivity(intent);
            }
        });

        historyButtonlink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                goToHistoryActivity();
            }
        });
    /*    mapButtonlink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                goToMapActivity();
            }
        });*/

        runButtonlink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if (dbhandler.isRouteTableEmpty("Running")) {
                    Intent intent = new Intent(MainActivity.this, loggerActivity.class);
                    intent.putExtra("activityType", "Running");
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, routeManager.class);
                    intent.putExtra("activityType", "Running");
                    startActivity(intent);
                    ;
                }

            }
        });

        bikeButtonlink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if (dbhandler.isRouteTableEmpty("Biking")) {
                    Intent intent = new Intent(MainActivity.this, loggerActivity.class);
                    intent.putExtra("activityType", "Biking");
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, routeManager.class);
                    intent.putExtra("activityType", "Biking");
                    startActivity(intent);
                    ;
                }

            }
        });

     /*   sensorButtonlink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                goToSensorActivity();
            }
        });*/

        checkPermissions();
    }

    protected void onStart() {
        super.onStart();
        String gender = sharedPreferenceHelper.getProfileGender();
        String weight = sharedPreferenceHelper.getProfileWeight();
        if (gender == "" || weight == "") {
            goToinfoActivity();
        }
    }

    protected void onResume(){
        super.onResume();
        String gender = sharedPreferenceHelper.getProfileGender();
        String weight = sharedPreferenceHelper.getProfileWeight();
        if (gender == "" || weight == "") {
            goToinfoActivity();
        }
    }

    public void onBackPressed(){
        super.onBackPressed();
        String gender = sharedPreferenceHelper.getProfileGender();
        String weight = sharedPreferenceHelper.getProfileWeight();
        String age = sharedPreferenceHelper.getProfileAge();
        if (gender.length()<0 || weight.length() < 0 || age.length()<0){
        }
    }

    void goToinfoActivity(){
        Intent intent = new Intent(MainActivity.this, infoActivity.class);
        startActivity(intent);
    }

    void goToTimerActivity() {
        Intent intent = new Intent(MainActivity.this, timerActivity.class);
        startActivity(intent);
    }

    void goToHistoryActivity() {
        Intent intent = new Intent(MainActivity.this, historyActivity.class);
        startActivity(intent);
    }


    void goToSensorActivity(){
        Intent intent = new Intent(MainActivity.this, HRActivity.class);
        startActivity(intent);
    }
    void goToMapActivity() {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
    }

    private void checkPermissions() {
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(MainActivity.this,
                new PermissionsResultAction() {
                    @Override
                    public void onGranted() {

                    }

                    @Override
                    public void onDenied(String permission) {

                    }
                });
    }

    @Override
    public void heartRateReceived(int heartRate){
        Message msg=new Message();
        msg.getData().putInt("HeartRate", heartRate);
        newHandler.sendMessage(msg);
    }
    final Handler newHandler=new Handler(){
        public void handleMessage(Message msg){
            heartRate=msg.getData().getInt("HeartRate");
        }
    };
}