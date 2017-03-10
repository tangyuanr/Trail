package com.example.kevin.trail;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import static android.content.Context.BIND_AUTO_CREATE;
import com.example.kevin.trail.ServiceGPS.LocalService;

/**
 * Created by Ezekiel on 3/9/2017.
 * Wrapper class that tries to abstract away ServiceGPS so that it's easier to use in the future.
 */

public class activityHelper {

    private int mActivityType;  //0 for running..variable to keep track of what activity. useless for now but could be useful later on.
    private Context context;
    private ServiceGPS serviceGPS;
    private boolean mBounded;
    private Intent intent;
    //private Intent intent_message;
    double totalDistance = 0;
    double pace = 0;
    private static final String TAG = "activityHelper";

    public activityHelper(Context context, int activityType) {
        mActivityType = activityType;
        this.context = context;
    }

    public void startActivity() {
        intent = new Intent(context, ServiceGPS.class);
        context.startService(intent);
        context.bindService(intent, mConnection, BIND_AUTO_CREATE);
        context.registerReceiver(broadcastReceiver, new IntentFilter(ServiceGPS.BROADCAST_ACTION));

    }

    public void stopActivity() {
        context.unbindService(mConnection);
        context.stopService(intent);

    }



    public double getTotalDistance() {
        return totalDistance;
    }

    public double getPace() {return pace;}


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
        totalDistance = intent.getDoubleExtra("distance", 1);
        pace = intent.getDoubleExtra("pace", 0);
        Log.d(TAG, String.valueOf(totalDistance)+"data received");
    }


}
