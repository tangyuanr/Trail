package com.example.kevin.trail;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.intentfilter.androidpermissions.PermissionManager;

import static java.util.Collections.singleton;

public class MainActivity extends AppCompatActivity {

    private sharedPreferenceHelper sharedPreferenceHelper;
    protected Button timerButtonlink = null;
    protected Button hikeButtonlink = null;
    protected Button bikeButtonlink = null;
    protected Button historyButtonlink = null;
    protected Button runButtonlink = null;
    protected Button mapButtonlink = null;
    protected Button sensorButtonlink=null;
    protected Button infoButtonlink = null;
    DBHandler dbhandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Trail trail = ((Trail) getApplicationContext());     //used to access global variable to check if permissions are right
        checkAndAskPermissions(trail);
        sharedPreferenceHelper = new sharedPreferenceHelper(MainActivity.this);

        infoButtonlink = (Button) findViewById(R.id.infoB);
            timerButtonlink = (Button) findViewById(R.id.timerButton);
            hikeButtonlink = (Button) findViewById(R.id.hikeButton);
            bikeButtonlink = (Button) findViewById(R.id.bikeButton);
            historyButtonlink = (Button) findViewById(R.id.historyButton);
            runButtonlink = (Button) findViewById(R.id.runButton);
            mapButtonlink = (Button) findViewById(R.id.mapButton);
            sensorButtonlink = (Button) findViewById(R.id.sensorButton);
            dbhandler = new DBHandler(this);

            infoButtonlink.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    goToinfoActivity();
                }
            });

            timerButtonlink.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    goToTimerActivity();
                }
            });

            hikeButtonlink.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (dbhandler.isRouteTableEmpty("Hiking")) {
                        Intent intent = new Intent(MainActivity.this, hikeActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(MainActivity.this, SelectRouteRunning.class);
                        intent.putExtra("activityType", "Hiking");
                        startActivity(intent);
                    }
                }
            });

            bikeButtonlink.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    goToBikeActivity();
                }
            });

            historyButtonlink.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    goToHistoryActivity();
                }
            });
            mapButtonlink.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    goToMapActivity();
                }
            });

            runButtonlink.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (dbhandler.isRouteTableEmpty("Running")) {
                        Intent intent = new Intent(MainActivity.this, runActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(MainActivity.this, SelectRouteRunning.class);
                        intent.putExtra("activityType", "Running");
                        startActivity(intent);
                        ;
                    }
                }
            });

            sensorButtonlink.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    goToSensorActivity();
                }
            });
        }

        protected void onStart() {
            super.onStart();
            String gender = sharedPreferenceHelper.getProfileGender();
            String weight = sharedPreferenceHelper.getProfileWeight();
            if (gender == "" || weight == "") {
                goToinfoActivity();
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

    void goToBikeActivity() {
        Intent intent = new Intent(MainActivity.this, bikeActivity.class);
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

    //methods that checks and asks for permissions and set the global variable trail.GPSStatus
    void checkAndAskPermissions(final Trail trail) {

        PermissionManager permissionManager = PermissionManager.getInstance(this);
        permissionManager.checkPermissions(singleton(Manifest.permission.ACCESS_FINE_LOCATION), new PermissionManager.PermissionRequestListener() {
            @Override
            public void onPermissionGranted() {
                trail.setGPSStatus(true);
            }

            @Override
            public void onPermissionDenied() {
                trail.setGPSStatus(false);
            }
        });
    }
}