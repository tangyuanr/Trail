package com.example.kevin.trail;

import android.app.Application;
import android.content.Context;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

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
        Picasso.Builder picassoBUILDER = new Picasso.Builder(this);
        picassoBUILDER.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso build = picassoBUILDER.build();
        build.setIndicatorsEnabled(true);
        build.setLoggingEnabled(true);
        Picasso.setSingletonInstance(build);

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


