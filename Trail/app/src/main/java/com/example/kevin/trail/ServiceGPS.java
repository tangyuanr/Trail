package com.example.kevin.trail;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
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
    private int samplingTime = 10000; //in milliseconds
    Subscription subscription;

    @Override
    public void onCreate() {
        super.onCreate();
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
                    String string = String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude());
                    saveText(string);
                }
            });

        } else {
            Toast.makeText(this, "GPS Disabled", Toast.LENGTH_SHORT).show();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        subscription.unsubscribe();
        super.onDestroy();
    }

    public class LocalService extends Binder {

        ServiceGPS getGPSService() {
            return ServiceGPS.this;
        }

    }

    private void saveText(String string) {

        try {
            //open file for writing
            OutputStreamWriter out = new OutputStreamWriter(openFileOutput("SERVICE_GPS_"+String.valueOf(samplingTime/1000)+"_SECONDS.txt", this.MODE_APPEND));
            out.write(string);
            out.write('\n');
            out.close();
            Toast.makeText(this, "ADDED 1 ENTRY", Toast.LENGTH_SHORT).show(); //display for debug purposes

        } catch (java.io.IOException e) {
            //if caught
            Toast.makeText(this, "Problem", Toast.LENGTH_SHORT).show();
        }
    }

}

