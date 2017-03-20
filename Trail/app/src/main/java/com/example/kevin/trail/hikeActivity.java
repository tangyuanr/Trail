package com.example.kevin.trail;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;


public class hikeActivity extends AppCompatActivity {
    private static final String TAG = "hikeActivity";
    Route route = null;
    Attempt attempt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hike);
        Intent receivedIntent = getIntent();    //retrieve the intent that was sent to check if it has a Route object
        if (receivedIntent.hasExtra("route")) {  //if the intent has a route object
            route = (Route) receivedIntent.getSerializableExtra("route");
            Log.d(TAG, "Route object received by hikeActivity");
        }
    }
}