package com.example.kevin.trail;

import android.app.Application;
import android.content.Context;


/**
 * Created by Ezekiel
 * Application class to hold global variables
 */

public class Trail extends Application {

    private boolean isGPSEnabled = false;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        Trail.context = getApplicationContext();
    }

    public boolean getGPSStatus() {
        return isGPSEnabled;
    }

    public void setGPSStatus(boolean bool) {
        isGPSEnabled = bool;
    }

    public static Context getAppContext() {
        return Trail.context;
    }


}


