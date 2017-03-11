package com.example.kevin.trail;

import android.app.Application;

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
