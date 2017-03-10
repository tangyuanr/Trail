package com.example.kevin.trail;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;

import java.io.OutputStreamWriter;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Subscription;
import rx.functions.Action1;


/**
 * Created by Ezekiel on 3/9/2017.
 * Logs GPS coordinates and writes them to a file
 * This runs as a service and can be called from any activity.
 */

public class ServiceGPS extends Service {

    private final IBinder mBinder = new LocalService();
    private int samplingTime = 20000; //in milliseconds
    Subscription subscription;
    private double totalDistance = 0;
    private double previousDistance = 0;
    private double pace = 0;
    int sample = 0;
    private double currentLatitude = 0;
    private double currentLongitude = 0;
    private double previousLatitude = 0;
    private double previousLongitude = 0;
    private static final String TAG = "ServiceGPS";
    public static final String BROADCAST_ACTION = "STATS";
    private final Handler handler = new Handler();
    Intent intent_sender;


    public double getTotalDistance() {
        return totalDistance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        intent_sender = new Intent(BROADCAST_ACTION);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Trail trail = ((Trail) getApplicationContext());

        if (trail.getGPSStatus()) {
            LocationRequest request = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(samplingTime);
            ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(this);
            subscription = locationProvider.getUpdatedLocation(request).subscribe(new Action1<Location>() {
                @Override
                public void call(Location location) {
                    //if it's the first sample
                    if (sample == 0) {

                        previousLongitude = location.getLongitude();
                        previousLatitude = location.getLatitude();
                        currentLatitude = previousLatitude;
                        currentLongitude = previousLongitude;

                    } else {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();
                        previousDistance = totalDistance;
                        totalDistance += calculateDistance(previousLatitude, previousLongitude, currentLatitude, currentLongitude);
                        pace = calculatePace(previousDistance,totalDistance,samplingTime/1000);
                        previousLatitude = currentLatitude;
                        previousLongitude = currentLongitude;
                    }
                    sample++;
                    String string = String.valueOf(currentLatitude) + "," + String.valueOf(currentLongitude) + "," + String.valueOf(totalDistance) + "," + String.valueOf(pace);
                    saveText(string);
                }
            });

        } else {
            //Toast.makeText(this, "GPS Disabled", Toast.LENGTH_SHORT).show();
        }

        handler.removeCallbacks(sendUpdates);
        handler.postDelayed(sendUpdates, 10000); // 10 seconds

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        subscription.unsubscribe();
        //Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();   //displaying this for debug purposes
        super.onDestroy();
    }

    public class LocalService extends Binder {

        public ServiceGPS getServerInstance() {
            return ServiceGPS.this;
        }
    }


    private void saveText(String string) {

        try {
            //open file for writing
            //filename should be generated dynamically once we figure out implementation of route managing
            OutputStreamWriter out = new OutputStreamWriter(openFileOutput(String.valueOf(samplingTime / 1000) + "_DATAx.txt", this.MODE_APPEND));
            out.write(string);
            out.write('\n');
            out.close();
            //Toast.makeText(this, "ADDED 1 ENTRY", Toast.LENGTH_SHORT).show();

        } catch (java.io.IOException e) {
            //if caught
            Toast.makeText(this, "Problem", Toast.LENGTH_SHORT).show();
        }
    }

    //broadcasting updates 
    private Runnable sendUpdates = new Runnable() {
        public void run() {
            packageUpdates();
            handler.postDelayed(this, 20000); // 5 seconds
        }
    };

    private void packageUpdates() {

        intent_sender.putExtra("distance", totalDistance);
        intent_sender.putExtra("pace", pace);
        sendBroadcast(intent_sender);
        Log.d(TAG, "entered PackageUpdates");
    }

    private double calculatePace(double previousDist, double totalDist, int samplingPeriod) {
        return 1/(((totalDist-previousDist)/samplingPeriod)*60); //minutes per km
    }


    //methods to calculate the distance between two sets of longitude-latitude coordinates. copied-pasted from http://stackoverflow.com/a/6981955
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }
    
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


}