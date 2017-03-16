package com.example.kevin.trail;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import static android.content.Context.BIND_AUTO_CREATE;
import com.example.kevin.trail.ServiceGPS.LocalService;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

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
    private int sample;
    //private Intent intent_message;
    double totalDistance = 0;
    double pace = 0;
    private long tLastSample = 0;
    int routeID = -1; //route does not exist in DB
    String routeName = "";
    DBHandler dbHandler;

    private static final String TAG = "activityHelper";

    public activityHelper(Context context, int activityType) {
        mActivityType = activityType;
        this.context = context;
        dbHandler = new DBHandler(context);
    }

    //I thought we may need this but I seems like not, so for the moment this is never called.
    public activityHelper(Context context, int routeID, int activityType) { //constructor called if route already exists
        mActivityType = activityType;
        this.context = context;
        this.routeID = routeID;
    }

    //Adding new route to the MASTER_TABLE_RUN table. I am storing the entire URL in the database. This will have to be improved as URLs have a character limit.
    //We can try converting the URL to a shorturl with a shortURL service. Read this for example: https://www.learn2crack.com/2014/01/android-using-goo-gl-url-shortener-api.html
    //Otherwise we can download the map image on the spot and save it on disk and keep track of the image name in the table, instead of the URL itself.
    //I have not had time to look more into this for now.
    //String name is the name of the Route that the user has inputed.
    //This method is called by the Running(or other) activity, when the user inputs a name in the dialog.
    public long addNewRoute(String name) {
        String url = getStaticAPIURL();
        return dbHandler.addMasterRoute(name, url);
    }

    //if the user wants to, he can save the attempt in the attempt table. the int routeID parameter is used to know to which route the attempt is associated.
    //I have not implemented anything about this yet but It should take no more than 30 minutes.
    private void addRunAttempt(int routeID) {
        dbHandler.addRunAttempt(routeID, 500, 500, "gay.txt"); //not implemented yet
    }

    //this builds up the URL to the static map screenshot, from the ArrayList of location objects returned by serviceGPS.
    private String getStaticAPIURL() {
        //I have used these to figure out what the resolution of the requested image should be.
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        float density  = displayMetrics.density;

        //250*density and 150*density are used to calculate the image dimensions in pixels that need to be request.
        //250 and 150 are values I have estimated from my phone's listview dimensions in dp. there is probably another way to do it, I don't know how this kind of thing is usually done.
        ArrayList<Location> locationArrayList = serviceGPS.getArrayCoordinates();
        String url = "http://maps.googleapis.com/maps/api/staticmap?size="+(int)(250*density)+"x"+(int)(150*density)+"&path=";
        for (int i  = 0; i < locationArrayList.size(); i += 2) { //every other element
            double latitude = locationArrayList.get(i).getLatitude();
            double longitude = locationArrayList.get(i).getLongitude();
            url += latitude + "," + longitude + "|";
        }
        url = url.substring(0,url.length()-1);
        url += "&sensor=false";
        return url;
        // this URL can get quite large so we need to look into ways to reduce it. For the moment I am taking every other element to half the number of points.
        // downloading the image and storing its filename in the SQLite database is probably better.
    }



    public void startActivity() {
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



    public double getTotalDistance() {
        return totalDistance;
    }

    public double getPace() {return pace;}

    //method to return pace formatted properly
    //1st version: returning the final average pace
    public String getFinalAveragePaceFormatted() {
        double finalPace =  tLastSample/(60000*totalDistance); //min per km
        if((finalPace > 30) || Double.isNaN(finalPace) || finalPace == 0 ) {
            return "--";
        }
        else {return String.format("%.2f", finalPace);}
    }

    //version for returning recent pace
    public String getPaceFormatted() {
        if((pace > 30) || Double.isNaN(pace) || pace == 0 ) {
            return "--";
        }
        else {return String.format("%.2f", pace);}
    }

    public long getTimeLastsample() {return tLastSample;}
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
        totalDistance = intent.getDoubleExtra("distance", 1);
        pace = intent.getDoubleExtra("pace", 0);
        tLastSample = intent.getLongExtra("time_of_last_sample",0);
        sample = intent.getIntExtra("number of samples", 0);
        Log.d(TAG, String.valueOf(totalDistance)+" data received "+tLastSample);
    }


}
