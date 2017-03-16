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

    public activityHelper(Context context, int routeID, int activityType) { //constructor called if route already exists
        mActivityType = activityType;
        this.context = context;
        this.routeID = routeID;
    }

    public long addNewRoute(String name) {
        String url = getStaticAPIURL();
        new DownloadImage().execute(url);
        return dbHandler.addMasterRoute(name, url);
    }




    private void saveImage(Bitmap b, String imageName) {
        FileOutputStream foStream;
        try {
            foStream = context.openFileOutput(imageName, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.PNG, 100, foStream);
            foStream.close();
        } catch (Exception e) {
            Log.d("saveImage", "Exception 2, Something went wrong!");
            e.printStackTrace();
        }
    }

    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        private String TAG = "DownloadImage";
        private Bitmap downloadImageBitmap(String sUrl) {
            Bitmap bitmap = null;
            try {
                InputStream inputStream = new URL(sUrl).openStream();   // Download Image from URL
                bitmap = BitmapFactory.decodeStream(inputStream);       // Decode Bitmap
                inputStream.close();
            } catch (Exception e) {
                Log.d(TAG, "Exception 1, Something went wrong!");
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return downloadImageBitmap(params[0]);
        }

        protected void onPostExecute(Bitmap result) {
            saveImage(result, routeName);
        }
    }


    private void addRunAttempt(int routeID) {
        dbHandler.addRunAttempt(routeID, 500, 500, "gay.txt"); //not implemented yet
    }

    private String getStaticAPIURL() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        float density  = displayMetrics.density;
        ArrayList<Location> locationArrayList = serviceGPS.getArrayCoordinates();
        String url = "http://maps.googleapis.com/maps/api/staticmap?size="+(int)(250*density)+"x"+(int)(150*density)+"&path=";
        for (int i  = 0; i < locationArrayList.size(); i++) {
            double latitude = locationArrayList.get(i).getLatitude();
            double longitude = locationArrayList.get(i).getLongitude();
            url += locationArrayList.get(i).getLatitude() + "," + longitude + "|";
        }
        url = url.substring(0,url.length()-1);
        url += "&sensor=false";
        return url;
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
        Log.d(TAG, String.valueOf(totalDistance)+" data received "+tLastSample);
    }


}
