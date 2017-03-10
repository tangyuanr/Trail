package com.example.kevin.trail;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.intentfilter.androidpermissions.PermissionManager;

import static java.util.Collections.singleton;

public class MainActivity extends AppCompatActivity {

    protected Button runButtonlink =null;
    protected Button hikeButtonlink =null;
    protected Button bikeButtonlink =null;
    protected Button gpsButtonlink=null;
    protected Button runningEzekielButton =null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Trail trail = ((Trail)getApplicationContext());     //used to access global variable to check if permissions are right
        checkAndAskPermissions(trail);
        runButtonlink = (Button) findViewById(R.id.runButton);
        hikeButtonlink = (Button) findViewById(R.id.hikeButton);
        bikeButtonlink = (Button) findViewById(R.id.bikeButton);
        gpsButtonlink=(Button) findViewById(R.id.gpsButton);
        runningEzekielButton = (Button) findViewById(R.id.runningEzekielButton);



        runButtonlink.setOnClickListener(new View.OnClickListener() {
         public void onClick(View view){
             goToRunActivity();
         }
        });

        hikeButtonlink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                goToHikeActivity();
            }
        });

        bikeButtonlink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                goToBikeActivity();
            }
        });

        gpsButtonlink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                goToGPSActivity();
            }
        });

        runningEzekielButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                goToRunningEzekielActivity();
            }
        });
    }

    void goToRunActivity(){
        Intent intent = new Intent(MainActivity.this, runActivity.class);
        startActivity(intent);
    }
    void goToHikeActivity(){
        Intent intent = new Intent(MainActivity.this, hikeActivity.class);
        startActivity(intent);
    }

    void goToBikeActivity(){
        Intent intent = new Intent(MainActivity.this, bikeActivity.class);
        startActivity(intent);
    }

    void goToGPSActivity(){
        Intent intent=new Intent(MainActivity.this, gpsActivity.class);
        startActivity(intent);
    }

    void goToRunningEzekielActivity(){
        Intent intent = new Intent(MainActivity.this, runActivityEzekiel.class);
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
