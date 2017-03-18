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

    private int mActivityType;  //0 for running..variable to keep track of what activity. useless for now but could be useful later on.
    private Context context;
    private ServiceGPS serviceGPS;
    private Intent intent;
    private int sample;
    float totalDistance = 0;
    double pace = 0;
    private long tLastSample = 0;
    boolean mBounded;
    Route route;

    private static final String TAG = "activityHelper";

    public activityHelper(Context context, int activityType) {
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

    public Attempt getAttempt() {
        return new Attempt(route, (int) tLastSample/1000, getDate()); //tLastSample is in milliseconds so we divide by 1000
    }

    private String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String currentDateandTime = sdf.format(new Date());
        return currentDateandTime;
    }

    public float getTotalDistance() {
        return totalDistance;
    }


    //method to return pace formatted properly
    //version for returning recent pace
    public String getPaceFormatted() {
        if((pace > 30) || Double.isNaN(pace) || pace == 0 ) {
            return "--";
        }
        else {return String.format("%.2f", pace);}
    }
    //version for the final stats dialog
    public String getFinalAveragePaceFormatted() {
        double pace =  tLastSample/(60000*totalDistance); //min per km
        if((pace > 30) || Double.isNaN(pace) || pace == 0 ) {
            return "--";
        }
        else {return String.format("%.2f", pace);}
    }

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
        pace = intent.getDoubleExtra("pace", 0);
        tLastSample = intent.getLongExtra("time_of_last_sample",0);
        sample = intent.getIntExtra("number of samples", 0);
        Log.d(TAG, String.valueOf(totalDistance)+" data received "+tLastSample);
    }

    public String getCoordinatesFileName() {
        return serviceGPS.getFilename();
    }


}
