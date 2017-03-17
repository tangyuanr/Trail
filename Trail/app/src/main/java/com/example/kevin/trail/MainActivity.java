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

    protected Button timerButtonlink = null;
    protected Button hikeButtonlink = null;
    protected Button bikeButtonlink = null;
    protected Button historyButtonlink = null;
    protected Button runButtonlink = null;
    DBHandler dbhandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Trail trail = ((Trail) getApplicationContext());     //used to access global variable to check if permissions are right
        checkAndAskPermissions(trail);
        timerButtonlink = (Button) findViewById(R.id.timerButton);
        hikeButtonlink = (Button) findViewById(R.id.hikeButton);
        bikeButtonlink = (Button) findViewById(R.id.bikeButton);
        historyButtonlink = (Button) findViewById(R.id.historyButton);
        runButtonlink = (Button) findViewById(R.id.runButton);
        dbhandler = new DBHandler(this);


        timerButtonlink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                goToTimerActivity();
            }
        });

        hikeButtonlink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                goToHikeActivity();
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

        runButtonlink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (dbhandler.isMasterTableEmpty()) {goToRunningActivity();}
                else {goToSelectRouteActivity();}
            }
        });
    }

    void goToTimerActivity() {
        Intent intent = new Intent(MainActivity.this, timerActivity.class);
        startActivity(intent);
    }

    void goToHikeActivity() {
        Intent intent = new Intent(MainActivity.this, hikeActivity.class);
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

    void goToSelectRouteActivity() {
        Intent intent = new Intent(MainActivity.this, SelectRouteRunning.class);
        startActivity(intent);
    }

    void goToRunningActivity() {
        Intent intent = new Intent(MainActivity.this, runActivity.class);
        startActivity(intent);
    }

    //methods that checks and asks for permissions and set the global variable trail.GPSStatus


    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


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