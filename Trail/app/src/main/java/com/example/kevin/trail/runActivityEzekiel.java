package com.example.kevin.trail;

import android.content.Intent;
import android.icu.text.DecimalFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class runActivityEzekiel extends AppCompatActivity {
    private boolean logging = false;
    activityHelper RunningHelper;
    TextView totalDistance;
    TextView latestPace;
    private static final String TAG = "runActivityEzekiel";

    /**
     * Created by Ezekiel on 3/9/2017.
     * Example activity that calls the ServiceGPS service.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runezekiel);

        Button startStopButton = (Button) findViewById(R.id.StartStop);
        totalDistance = (TextView) findViewById(R.id.totalDistance);
        latestPace = (TextView) findViewById(R.id.latestpace);
        final Intent intent = new Intent(this, ServiceGPS.class);
        RunningHelper = new activityHelper(runActivityEzekiel.this,0); //instantiate a running helper object, the int parameter is the type of activity. 0 for running.

        startStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!logging) {

                    RunningHelper.startActivity(); //when the user clicks start, running activity (activity in the traditional sense, not android sense) starts and data starts being collected.
                    logging = true; //boolean so that the same button acts as an on/off toggle
                    Toast.makeText(runActivityEzekiel.this, "You've started running",Toast.LENGTH_SHORT).show();
                    startUpdateStatsThread(); //start the thread that receives updates from the service
                }
                else{
                    RunningHelper.stopActivity();
                    logging = false;
                    Toast.makeText(runActivityEzekiel.this, "You've stopped running",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    //thread that receives distance updates from the service
    private void startUpdateStatsThread() {
        Thread th = new Thread(new Runnable() {

            public void run() {
                while (logging == true) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            totalDistance.setText(String.format("%.2f", RunningHelper.getTotalDistance()));
                            latestPace.setText(String.format("%.2f", RunningHelper.getPace()));
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
