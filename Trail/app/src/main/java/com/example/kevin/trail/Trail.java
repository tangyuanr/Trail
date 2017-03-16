package com.example.kevin.trail;

import android.app.Application;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;


/**
 * Created by Ezekiel
 * Application class to hold global variables
 */

public class Trail extends Application {

    private boolean isGPSEnabled = false;



    public boolean getGPSStatus() {
        return isGPSEnabled;
    }

    public void setGPSStatus(boolean bool) {
        isGPSEnabled = bool;
    }


}


