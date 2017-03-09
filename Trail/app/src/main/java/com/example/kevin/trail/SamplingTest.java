package com.example.kevin.trail;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Subscription;
import rx.functions.Action1;

public class SamplingTest extends AppCompatActivity {
    private boolean logging = false;

    /**
     * Created by Ezekiel on 3/9/2017.
     * Activity that calls the ServiceGPS service. Used for sampling testing I'm doing today.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sampling_test);

        Button startStopButton = (Button) findViewById(R.id.StartStop);
        final Intent intent = new Intent(this, ServiceGPS.class);

        startStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!logging) {
                    startService(intent);
                    logging = true;
                    Toast.makeText(SamplingTest.this, "LOGGING STARTED",Toast.LENGTH_SHORT).show();
                }
                else{
                    stopService(intent);
                    logging = false;
                    Toast.makeText(SamplingTest.this, "LOGGING STOPPED",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
