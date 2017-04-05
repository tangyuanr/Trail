package com.example.kevin.trail;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import static android.content.Context.BIND_AUTO_CREATE;
import com.example.kevin.trail.ServiceGPS.LocalService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Ezekiel on 3/9/2017.
 * Wrapper class that tries to abstract away ServiceGPS so that it's easier to use in the future.
 */

public class activityHelper {

    private String mActivityType;
    private Context context;
    private ServiceGPS serviceGPS;
    private Intent intent;
    private double averageLatitude;
    private double averageLongitude;
    private int sample;
    private float totalDistance = 0;
    private float speed = 0;
    private long tLastSample = 0;
    boolean mBounded;
    Route route;

    private static final String TAG = "activityHelper";

    public activityHelper(Context context, String activityType) {
        mActivityType = activityType;
        this.context = context;
    }

    public void startActivity(Route route) {
        this.route = route;
        intent = new Intent(context, ServiceGPS.class);
        context.startService(intent);
        context.bindService(intent, mConnection, BIND_AUTO_CREATE);
        context.registerReceiver(broadcastReceiver, new IntentFilter(ServiceGPS.BROADCAST_ACTION));

    }


    public void stopActivity() {

        context.unregisterReceiver(broadcastReceiver);
        context.unbindService(mConnection);
        context.stopService(intent);
    }

//    public Attempt getAttempt() {
//        return new Attempt(route, (int) tLastSample/1000, getDate(), route.getSnapshotURL()); //tLastSample is in milliseconds so we divide by 1000
//    }

    private String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");//added start time so that attempts made on the same day can be differentiated in historyActivity
        String currentDateandTime = sdf.format(new Date());
        Log.d(TAG, "getting current date and time: start of Attempt");
        return currentDateandTime;
    }

    public float getTotalDistance() {
        return totalDistance;
    }

    public float getSpeed() {return speed;}


    public long getTimeLastsample() {return tLastSample;}   //milliseconds
    public int getCurrentNumberOfSamples() {return sample;}


    ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            mBounded = false;
            serviceGPS = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            mBounded = true;
            LocalService mLocalBinder = (LocalService)service;
            serviceGPS = mLocalBinder.getServerInstance();
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateData(intent);
        }
    };

    private void updateData(Intent intent) {
        totalDistance = intent.getFloatExtra("distance", 1);
        speed = intent.getFloatExtra("speed", 0);
        tLastSample = intent.getLongExtra("time_of_last_sample",0);
        sample = intent.getIntExtra("number of samples", 0);
        averageLatitude = intent.getDoubleExtra("currentLatitude", 0);
        averageLongitude = intent.getDoubleExtra("currentLongitude", 0);
        Log.d(TAG, String.valueOf(totalDistance)+" data received "+tLastSample);
    }

    public String getCoordinatesFileName() {
        return serviceGPS.getFilename();
    }


}
