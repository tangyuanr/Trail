package com.example.kevin.trail;


import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class runActivity extends AppCompatActivity implements IHeartRateReciever{
    private boolean logging = false;
    activityHelper RunningHelper;
    TextView totalDistance;
    TextView latestPace;
    private static final String TAG = "runActivity";
    private String inputRouteName = "";
    Route route = null;
    Attempt attempt = null;
    private String activityType = "Running";
    private String imagefilename;

    DBHandler dbHandler = new DBHandler(this);

    protected TextView hrTextView=null;
    //private int heartRate=0; //using the static heart rate from mainActivity now
    //HRSensorHandler hrHandler;
    private int totalBPM=0;
    private int counter=0;
    protected Button sensorReconnect=null;
    protected FloatingActionButton sensorHelp=null;
    private sharedPreferenceHelper sharedPref;
    private double totalCaloriesBurnt=0;
    protected TextView caloriesTextView=null;

    TextView timerTextViewL;
    long startTime= 0;

    int noti_id = 1;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                timerTextViewL.setText(String.format("%d:%02d", minutes, seconds));
            notificationOp(noti_id, String.format("%d:%02d", minutes, seconds), RunningHelper.getPaceFormatted(), String.format("%.2f", RunningHelper.getTotalDistance()));
                timerHandler.postDelayed(this, 500);
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        Intent receivedIntent = getIntent();    //retrieve the intent that was sent to check if it has a Route object
        if(receivedIntent.hasExtra("route")) {  //if the intent has a route object
            route = (Route) receivedIntent.getSerializableExtra("route");
            Log.d(TAG, "Route object received by runActivity");
        }

        Button startStopButton = (Button) findViewById(R.id.StartStop);
        hrTextView=(TextView)findViewById(R.id.hrText);
        totalDistance = (TextView) findViewById(R.id.totalDistance);
        latestPace = (TextView) findViewById(R.id.latestpace);;
        RunningHelper = new activityHelper(runActivity.this, 0); //instantiate a running helper object, the int parameter is the type of activity. 0 for running.
        //hrHandler=new HRSensorHandler(this);
        sensorReconnect=(Button)findViewById(R.id.HRreconnect);
        sensorHelp=(FloatingActionButton)findViewById(R.id.floatingHelp);
        sharedPref=new sharedPreferenceHelper(runActivity.this);
        caloriesTextView=(TextView)findViewById(R.id.caloriesText);

        timerTextViewL = (TextView) findViewById(R.id.timerTextView);
        //final Button restButton= (Button) findViewById(R.id.restartB);

        startStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!logging) {
                    RunningHelper.startActivity(route); //when the user clicks start, running activity (activity in the traditional sense, not android sense) starts and data starts being collected.
                    Log.d(TAG, "RunningHelper.startActivity(route) called");
                    logging = true; //boolean so that the same button acts as an on/off toggle

                    //make hrHandler send heart rate messages here
                    MainActivity.hrHandler.setReciver(runActivity.this);

                    Toast.makeText(runActivity.this, "You've started running", Toast.LENGTH_SHORT).show();
                    startUpdateStatsThread(); //start the thread that receives updates from the service
                } else {
                    if (RunningHelper.getCurrentNumberOfSamples() < 1) {
                        logging = false;
                        RunningHelper.stopActivity();
                    } else {
                        if (!(route == null)) {  //if Route is not null, it means a route was sent was sent by SelectRouteRunning
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
                            String snapshotURL=route.getStaticAPIURL(runActivity.this, 250, 250);
                            imageDownload(runActivity.this, snapshotURL);
                            attempt = new Attempt(route, (int) RunningHelper.getTimeLastsample() / 1000, RunningHelper.getTotalDistance(), sdf.format(new Date()), snapshotURL, totalBPM/counter, (int)totalCaloriesBurnt, imagefilename);
                            Log.d(TAG, "Route is not null. Attempt object built.");
                            SaveAttemptDialog();    //prompt the user if he wants to save the attempt
                        } else {  //else, Route is null and the user selected New Route, so we need to ask the user to give the new route a name
                            NewRouteDialog();
                        }
                        long timelastSample = RunningHelper.getTimeLastsample();    //get final stats for display
                        float FinalDistance = RunningHelper.getTotalDistance();    //get final stats for display
                        RunningHelper.stopActivity();
                        showStatsDialog(timelastSample, FinalDistance);  //show stats dialog
                        logging = false;
                    }
                    //disconnectClicked();//disconnect from HxM sensor
                    sensorReconnect.setVisibility(View.INVISIBLE);
                    sensorHelp.setVisibility(View.INVISIBLE);
                }

                Button  startStopButton= (Button) v;
                if (startStopButton.getText().equals("stop")){
                    timerHandler.removeCallbacks(timerRunnable);
                    startStopButton.setText("Start");
                }else if (startStopButton.getText().equals("Start")){
                    startTime = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnable, 0);
                    startStopButton.setText("stop");
                }
            }
        });

        //handles sensor reconnection
        sensorReconnect.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                try{
                    MainActivity.hrHandler.Connect();
                    MainActivity.hrHandler.setReciver(runActivity.this);
                    sensorReconnect.setVisibility(View.INVISIBLE);
                    sensorHelp.setVisibility(View.INVISIBLE);
                }catch(RuntimeException e){
                    hrTextView.setText("error connecting to HxM");
                    sensorReconnect.setVisibility(View.VISIBLE);
                    sensorHelp.setVisibility(View.VISIBLE);
                }
            }
        });
        sensorHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorHelpDialog();//display help to adjust sensor so that connection can be established
            }
        });

    }

    //to stop both sensors activity when user quits prematurely
    @Override
    protected void onDestroy(){
        super.onDestroy();
        //if still logging
        if (logging) {
            RunningHelper.stopActivity();//stop GPS
            //disconnectClicked();//stop heart rate monitor
        }
    }

    public void notificationOp(int id, String time, String pace, String distance){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Trail : Running");
        builder.setContentText("Time: " + time+ ", Pace: "+ pace + ", Distance : " + distance);
        NotificationManager NM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NM.notify(id,builder.build());
    }
    private void SaveAttemptDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save attempt?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");//added start time so that attempts made on the same day can be differentiated in historyActivity
                String currentDateandTime = sdf.format(new Date());
                String snapshotURL=route.getStaticAPIURL(runActivity.this, 250, 250);
                imageDownload(runActivity.this, snapshotURL);
                attempt = new Attempt(route, (int) RunningHelper.getTimeLastsample() / 1000, RunningHelper.getTotalDistance(), currentDateandTime, snapshotURL, totalBPM/counter, (int)totalCaloriesBurnt, imagefilename);

                dbHandler.addAttempt(attempt); //save the attempt to the database
                Log.d(TAG, "Attempt added to the database");
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }






    //dialog that asks the user if he wants to save the route
    private void NewRouteDialog() {
        final EditText input = new EditText(this);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(input)
                .setTitle("Enter route name:")
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button buttonSave = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                buttonSave.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        inputRouteName = input.getText().toString();
                        boolean routeNameExists = dbHandler.doesRouteNameExist(inputRouteName);
                        if(!(routeNameExists || inputRouteName.isEmpty())) {
                            //the user wants to save the route. it obviously means he wants to save the attempt with it as well. so we need to build both objects.
                            int totaltime = (int) RunningHelper.getTimeLastsample() / 1000;
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");//added start time so that attempts made on the same day can be differentiated in historyActivity
                            String currentDateandTime = sdf.format(new Date());
                            //instantiating a new route object with the constructor for the case in which we have no rowID yet
                            route = new Route(inputRouteName, activityType, RunningHelper.getTotalDistance(), totaltime, currentDateandTime, RunningHelper.getCoordinatesFileName());
                            dbHandler.addRoute(route);  //add the New Route to the database and get the rowID of the route that was added
                            Log.d(TAG, "Route object added to ROUTE_TABLE");

                            String snapshotURL=route.getStaticAPIURL(runActivity.this, 250, 250);
                            imageDownload(runActivity.this, snapshotURL);
                            attempt = new Attempt(route, totaltime, RunningHelper.getTotalDistance(), currentDateandTime, snapshotURL, totalBPM/counter, (int)totalCaloriesBurnt, imagefilename);
                            dbHandler.addAttempt(attempt); //adding the attempt
                            Log.d(TAG, "Attempt object built and added to database");
                            dialog.dismiss();
                        } else if(routeNameExists) {
                            Toast.makeText(runActivity.this, "Route name already exists", Toast.LENGTH_SHORT).show();
                        }
                        else if(inputRouteName.isEmpty()) {
                            Toast.makeText(runActivity.this, "Route name cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        dialog.show();
    }


    //thread that receives distance updates from the service
    private void startUpdateStatsThread() {
        Thread th = new Thread(new Runnable() {

            public void run() {
                while (logging == true) {
                    try {
                        Thread.sleep(5000);
                        totalBPM+=MainActivity.heartRate;//so now it's incrementing at every 5 seconds
                        counter++;
                        totalCaloriesBurnt+=caloriesCalculator(MainActivity.heartRate);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            totalDistance.setText(String.format("%.2f", RunningHelper.getTotalDistance()));
                            latestPace.setText(RunningHelper.getPaceFormatted());
                            caloriesTextView.setText(String.format("%.2f",totalCaloriesBurnt));
                            if (MainActivity.heartRate==0){
                                sensorReconnect.setVisibility(View.VISIBLE);
                                sensorHelp.setVisibility(View.VISIBLE);
                            }
                            else {
                                sensorReconnect.setVisibility(View.INVISIBLE);
                                sensorHelp.setVisibility(View.INVISIBLE);
                            }
                        }
                    });

                }
            }
        });
        th.start();
    }

    //dialog that displays final stats
    private void showStatsDialog(long timeLastSample, double FinalDistance) {

        long time = timeLastSample / (60000); //in minutes
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Run ended");
        alertDialog.setMessage("You have runned " + String.format("%.2f", FinalDistance) + " km in " + (time) + " minutes.\n"
                + "Average pace: " + RunningHelper.getFinalAveragePaceFormatted() + " min/km.\n"+
                "Average heart rage: "+totalBPM/counter+" BPM\n"+
                "Total calories burnt: "+totalCaloriesBurnt+" KCal");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


    @Override
    public void heartRateReceived(int heartRate){
        Message msg=new Message();
        msg.getData().putInt("HeartRate", heartRate);
        newHandler.sendMessage(msg);
    }
    //connect with HxM HR Sensor
    private void connectClicked(){
        try{

            //hrHandler.Connect();
            //hrHandler.setReciver(runActivity.this);
            sensorReconnect.setVisibility(View.INVISIBLE);
            sensorHelp.setVisibility(View.INVISIBLE);
        }catch (RuntimeException e) {
            hrTextView.setText("Error connecting to HxM");
            sensorReconnect.setVisibility(View.VISIBLE);
            sensorHelp.setVisibility(View.VISIBLE);
        }
    }

    //disconnect with HxM HR Sensor
    private void disconnectClicked(){
        try{
            //hrHandler.setReciver(null);
            //hrHandler.Disconnect();
            sensorReconnect.setVisibility(View.INVISIBLE);
            sensorHelp.setVisibility(View.INVISIBLE);
        }catch (RuntimeException e){
            hrTextView.setText("error disconnecting from HxM: "+e.getMessage());//never had problem disconnecting from it so far...but just in case there is, the exception message is for debugging
        }
    }

    final Handler newHandler=new Handler(){
        public void handleMessage(Message msg){
            MainActivity.heartRate=msg.getData().getInt("HeartRate");
            hrTextView.setText(Integer.toString(MainActivity.heartRate));
        }
    };

    //dialog that pops out when user clicks on the floating help button
    private void sensorHelpDialog() {
        AlertDialog helpDialog = new AlertDialog.Builder(this).create();
        helpDialog.setTitle("Trouble using HxM?");
        helpDialog.setMessage("Check if your Zephyr HxM is paired with your phone via bluetooth\n"+
                "Make sure your HxM is charged\n"+
                "Make sure the probes on the strap are placed onto your chest\n"+
                "Moisten the strap probes with a bit of water"
        );
        helpDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        helpDialog.show();
    }

    //calculate calories based on heart rate, called inside the thread at every 5 second
    private double caloriesCalculator(int HR){
        double age=Double.parseDouble(sharedPref.getProfileAge());
        String gender=sharedPref.getProfileGender();
        double weight=Double.parseDouble(sharedPref.getProfileWeight());
        double LB_to_KG=0.453592;
        double DURATION = (double)5/60;//in minute

        double calories=0;

        //calories formula for male
        if (gender.equals("m")||gender.equals("M")){
            double age_factor=age*0.2017;
            double weight_factor=weight*LB_to_KG*0.1988;
            double HR_factor=HR*0.6309;
            calories = age_factor+weight_factor+HR_factor - 55.0969;
            calories = calories * DURATION / 4.184;
            Log.e(TAG, "Weight: "+weight+", Age: "+age+", Gender: "+gender+". With heart rate "+HR+", calories calculated: "+calories);
        }
        //calories formula for female
        else if (gender.equals("f")||gender.equals("F")){
            double age_factor=age*0.074;
            double weight_factor=weight*LB_to_KG*0.1263;
            double HR_factor=HR*0.4472;
            calories = (age_factor+ weight_factor + HR_factor - 20.4022) * DURATION /4.184;
            Log.e(TAG, "Weight: "+weight+", Age: "+age+", Gender: "+gender+". With heart rate "+HR+", calories calculated: "+calories);
        }

        if (calories<0)
            calories=0;

        return calories;
    }

    //map snapshot saving
    public void imageDownload(Context context, String url){
        Picasso.with(context)
                .load(url)
                .into(getTarget(url));
    }
    private Target getTarget(final String url){
        Target target = new Target(){
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from){
                new Thread(new Runnable(){
                    @Override
                    public void run(){
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd_HHmm");//added start time so that attempts made on the same day can be differentiated in historyActivity
                        final String currentDateandTime = sdf.format(new Date());

                        String path = Trail.getAppContext().getFilesDir() + "/";
                        String imageName=currentDateandTime+".JPEG";
                        imagefilename=imageName;
                        File file=new File(path+imagefilename);

                        try{
                            file.createNewFile();
                            FileOutputStream ostream=new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                            ostream.flush();
                            ostream.close();

                        }catch (Exception e){
                            Log.e(TAG, e.getMessage());
                        }
                    }
                }).start();
            }
            @Override
            public void onBitmapFailed(Drawable errordrawable){}
            @Override
            public void onPrepareLoad(Drawable placeholderdrawable){}
        };
        return target;
    }
}
