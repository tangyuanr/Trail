package com.example.kevin.trail;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import com.google.android.gms.location.LocationListener;

import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Created by Ezekiel on 3/9/2017.
 * Logs GPS coordinates and writes them to a file
 * This runs as a service and can be called from any activity.
 */

public class ServiceGPS extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    LocationRequest locationRequestQuery;
    GoogleApiClient googleApi;
    Location currentLocation;
    private final IBinder mBinder = new LocalService();
    private int SUBSAMPLINGPERIOD = 1000; //in milliseconds
    private final int numberOfSamplePerAverage = 10;
    private int EFFECTIVESAMPLINGPERIOD;
    private static final String TAG = "SERVICEGPS";
    Intent intent_sender;
    public static final String BROADCAST_ACTION = "STATS";
    private final Handler handler = new Handler();
    int counter = 0;
    private float totalDistance = 0;
    private int sample = 0;
    private double averageLatitude = 0;
    private double averageLongitude = 0;
    double totalLongitude = 0;
    double totalLatitude = 0;
    private Location averageLocation;
    private Location previousLocation;
    private float previousDistance = 0;
    private long tStart = 0;
    private long tSample = 0;
    private double pace = 0;
    ArrayList<Location> coordinatesArray = new ArrayList<>();


    @Override
    public void onCreate() {
        super.onCreate();
        locationRequestQuery = new LocationRequest();
        locationRequestQuery.setInterval(SUBSAMPLINGPERIOD);
        locationRequestQuery.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        googleApi = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        intent_sender = new Intent(BROADCAST_ACTION);
        //configure output filename
        EFFECTIVESAMPLINGPERIOD = SUBSAMPLINGPERIOD * numberOfSamplePerAverage;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateandTime = sdf.format(new Date());
        Log.d(TAG, "getting current date and time: " + currentDateandTime);
        filename = String.valueOf(EFFECTIVESAMPLINGPERIOD / 1000) + currentDateandTime + ".TXT";
        Log.d(TAG, "forming output filename: " + filename);


    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting ServiceGPS service");
        googleApi.connect();
        tStart = System.currentTimeMillis();    //start time, used to return the total time elapsed between 1st and last sample
        handler.removeCallbacks(sendUpdates);
        handler.postDelayed(sendUpdates, 5000); // 10 seconds
        return START_NOT_STICKY;
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(googleApi, locationRequestQuery, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }



    @Override
    public void onLocationChanged(Location location) {
        //Log.d(TAG, "onLocationChanged googleAPI");
        currentLocation = location;

        //if we have enough subsamples to get an average sample
        if (counter > numberOfSamplePerAverage - 1) {
            averageLongitude = totalLongitude / (numberOfSamplePerAverage);
            averageLatitude = totalLatitude / (numberOfSamplePerAverage);
            averageLocation = new Location("");
            averageLocation.setLatitude(averageLatitude);
            averageLocation.setLongitude(averageLongitude);

            //if this is not fhte 1st sample
            if (!(sample == 0)) {
                previousDistance = totalDistance;
                float latestDistance = (previousLocation.distanceTo(averageLocation))/1000; //in km
                Log.d(TAG, "latest distance "+latestDistance);
                if(latestDistance > 0.0025) {totalDistance += latestDistance;} //if not, user is probably standing still. doing this to prevent accumulation of small errors.
                pace = calculatePace(previousDistance, totalDistance, EFFECTIVESAMPLINGPERIOD / 1000);
                Log.d(TAG, "totalDistance "+totalDistance);
                Log.d(TAG, "pace "+pace);
            }

            tSample = System.currentTimeMillis() - tStart;  //time of last sample
            String string = String.valueOf(round(averageLocation.getLatitude(),5)) + "," + String.valueOf(round(averageLocation.getLongitude(),5));
            //String string = String.valueOf(averageLocation.getLatitude()) + "," + String.valueOf(averageLocation.getLongitude()) + "," + totalDistance + "," + String.valueOf(pace) + "," + tSample;
            saveText(string);
            coordinatesArray.add(averageLocation);  //building a dynamic arraylist of Location objects so that we can send it out if anything needs it.
            Log.d(TAG, "saved sample"+string);
            counter = 0;
            sample++;
            totalLongitude = 0;
            totalLatitude = 0;
            previousLocation = averageLocation;
        } else {

            totalLongitude += currentLocation.getLongitude();
            totalLatitude += currentLocation.getLatitude();
            counter++;
        }

    }

    private ArrayList<Location> getArrayCoordinates() {
        return coordinatesArray;
    }


    public class LocalService extends Binder {

        public ServiceGPS getServerInstance() {
            return ServiceGPS.this;
        }

    }

    //broadcasting updates
    private Runnable sendUpdates = new Runnable() {
        public void run() {
            packageUpdates();
            handler.postDelayed(this, 10000); // 5 seconds
        }
    };

    private void packageUpdates() {

        intent_sender.putExtra("distance", totalDistance);
        intent_sender.putExtra("pace", pace);
        intent_sender.putExtra("currentLatitude", averageLatitude);
        intent_sender.putExtra("currentLongitude", averageLongitude);
        intent_sender.putExtra("time_of_last_sample", tSample);
        intent_sender.putExtra("number of samples", sample);
        sendBroadcast(intent_sender);
        Log.d(TAG, "entered PackageUpdates");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private static String filename;
    public String getFilename() {
        Log.d(TAG, "returned filename: " + filename);
        return filename;
    }


    private void saveText(String string) {

        try {
            //open file for writing
            //filename should be generated dynamically once we figure out implementation of route managing
            OutputStreamWriter out = new OutputStreamWriter(openFileOutput(filename, MODE_APPEND));
            out.write(string);
            out.write('\n');
            out.close();
            Toast.makeText(this, "ADDED 1 ENTRY", Toast.LENGTH_SHORT).show();

        } catch (java.io.IOException e) {
            //if caught
            Toast.makeText(this, "Problem", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(sendUpdates);
        googleApi.disconnect();
        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();   //displaying this for debug purposes
        super.onDestroy();
    }

    private double calculatePace(float previousDist, float totalDist, int samplingPeriod) {
        return 1/(((totalDist-previousDist)/samplingPeriod)*60); //minutes per km
    }

    //copied-pasted from http://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


}