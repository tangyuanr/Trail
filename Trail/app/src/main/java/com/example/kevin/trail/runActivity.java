package com.example.kevin.trail;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.DecimalFormat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class runActivity extends AppCompatActivity {
    private boolean logging = false;
    activityHelper RunningHelper;
    TextView totalDistance;
    TextView latestPace;
    private static final String TAG = "runActivity";
    ArrayList<Route> listOfRoutes = new ArrayList<Route>();
    private String inputRouteName = "";
    private boolean isNewRoute = true;
    private int routeID = -1; //initialize this to -1. (don't remove it)

    /*
    * modified by JY on 3/11/2017
    * add info (coordinate data filename, total time, tital distance) into database
    */
    DBHandler dbHandler = new DBHandler(this);
    ServiceGPS servicegps = new ServiceGPS();


    /**
     * Created by Ezekiel on 3/9/2017.
     * Example activity that calls the ServiceGPS service.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        //get the intent sent from SelectRouteRunning activity. we need to know what the user has selected.
        //the first item in the list has position 0, the second has position 1, etc. the "-1" in blue is the default value (needed by this method)
        routeID = getIntent().getIntExtra("ROUTEID",-1);

        Button startStopButton = (Button) findViewById(R.id.StartStop);
        totalDistance = (TextView) findViewById(R.id.totalDistance);
        latestPace = (TextView) findViewById(R.id.latestpace);
        final Intent intent = new Intent(this, ServiceGPS.class);
        RunningHelper = new activityHelper(runActivity.this, 0); //instantiate a running helper object, the int parameter is the type of activity. 0 for running.




        startStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!logging) {
                    //TODO implement timer with start/stop button

                    RunningHelper.startActivity(); //when the user clicks start, running activity (activity in the traditional sense, not android sense) starts and data starts being collected.
                    logging = true; //boolean so that the same button acts as an on/off toggle
                    Toast.makeText(runActivity.this, "You've started running", Toast.LENGTH_SHORT).show();
                    startUpdateStatsThread(); //start the thread that receives updates from the service
                } else {

                    long timelastSample = RunningHelper.getTimeLastsample();    //get final stats for display
                    double FinalDistance = RunningHelper.getTotalDistance();    //get final stats for display
                    RunningHelper.stopActivity();
                    showDialog(timelastSample, FinalDistance);  //show stats dialog

                    //here we determine if we need to ask the user if he wants to save the route
                    //if the user has selected "New Route" and there have been more than 5 samples collected, ask the user if he wants to save
                    if ((routeID == -1) && (RunningHelper.getCurrentNumberOfSamples() > 2)) {
                        NewRouteDialog();
                    }
                    logging = false;
                    //Toast.makeText(runActivity.this, "You've stopped running",Toast.LENGTH_SHORT).show();
                    //append stuff to the database
                    //dbHandler.addRecord(servicegps.getFilename(), String.valueOf(RunningHelper.getTotalDistance()), "some total time");
                }

            }
        });
    }

    //dialog that asks the user if he wants to save the route
    private void NewRouteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save Route?");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputRouteName = input.getText().toString();
                RunningHelper.addNewRoute(inputRouteName);  //activityHelper method that adds the New Route

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        builder.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (logging)// if logging is still true
            RunningHelper.stopActivity();
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
                            latestPace.setText(RunningHelper.getPaceFormatted());
                        }
                    });
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        th.start();
    }

    //dialog that displays final stats
    private void showDialog(long timeLastSample, double FinalDistance) {

        long time = timeLastSample / (60000); //in minutes
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Run ended");
        alertDialog.setMessage("You have runned " + String.format("%.2f", FinalDistance) + " km in " + (time) + " minutes.\n"
                + "Average pace: " + RunningHelper.getFinalAveragePaceFormatted() + " min/km.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
