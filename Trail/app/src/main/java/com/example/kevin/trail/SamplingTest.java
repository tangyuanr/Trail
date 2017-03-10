package com.example.kevin.trail;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Subscription;
import rx.functions.Action1;

public class SamplingTest extends AppCompatActivity {
    private boolean logging = false;
    activityHelper RunningHelper;
    TextView totalDistance;
    private static final String TAG = "SamplingTest";

    /**
     * Created by Ezekiel on 3/9/2017.
     * Example activity that calls the ServiceGPS service.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sampling_test);

        Button startStopButton = (Button) findViewById(R.id.StartStop);
        totalDistance = (TextView) findViewById(R.id.totalDistance);
        final Intent intent = new Intent(this, ServiceGPS.class);
        RunningHelper = new activityHelper(SamplingTest.this,0); //instantiate a running helper object, the int parameter is the type of activity. 0 for running.

        startStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!logging) {

                    RunningHelper.startActivity(); //when the user clicks start, running activity (activity in the traditional sense, not android sense) starts and data starts being collected.
                    logging = true; //boolean so that the same button acts as an on/off toggle
                    Toast.makeText(SamplingTest.this, "LOGGING STARTED",Toast.LENGTH_SHORT).show();
                    startDistanceThread(); //start the thread that receives distance updates from the service
                }
                else{
                    RunningHelper.stopActivity();
                    logging = false;
                }

            }
        });
    }

    //thread that receives distance updates from the service
    private void startDistanceThread() {
        Thread th = new Thread(new Runnable() {

            public void run() {
                while (logging == true) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            double distance = RunningHelper.getTotalDistance();
                            Log.d(TAG, String.valueOf(distance)+" entered runOnUiThread");
                            totalDistance.setText(Double.toString(distance));
                        }
                    });
                    try {
                        Thread.sleep(5000);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        th.start();
    }
}
