package com.example.kevin.trail;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
    public static final int GPS_PERMISSION_FINE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Trail trail = ((Trail)getApplicationContext());     //used to access global variable to check if permissions are right
        checkAndAskPermissions(trail);
        trail.setGPSStatus(true);
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
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {


                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GPS_PERMISSION_FINE  );


                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case GPS_PERMISSION_FINE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                   

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
