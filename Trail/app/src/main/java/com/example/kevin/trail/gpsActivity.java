package com.example.kevin.trail;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class gpsActivity extends AppCompatActivity {
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION=11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION=12;

    //GPS managers
    private LocationManager myLocationManager;
    private LocationListener myLocationListener;
    private Location myLocation;
    private String provider;

    //layout
    protected Button gpsReadButton=null;
    protected TextView coordText=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        // initialize layout objects
        gpsReadButton=(Button)findViewById(R.id.readLocationButton);
        coordText=(TextView)findViewById(R.id.locationText);

        getLocation();

        //object listener
        gpsReadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                //display last known location onto textview
                coordText.setText("Lat: "+myLocation.getLatitude()+" Lng: "+myLocation.getLongitude());
            }
        });
    }

    //getting coordinates
    protected void getLocation(){
        myLocationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        provider=myLocationManager.GPS_PROVIDER;

        myLocationListener = new LocationListener() {
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onLocationChanged(Location location) {
                myLocation=location;
            }
        };
        // permission check
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            myLocation=myLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        else{
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_ACCESS_FINE_LOCATION);
                myLocation=myLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
        // location data reading
        if (myLocation==null){
            myLocationManager.requestLocationUpdates(provider, 1000,0,myLocationListener);
        }
    }
}
