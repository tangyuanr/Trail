package com.example.kevin.trail;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class hikeActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnMyLocationButtonClickListener,
        IHeartRateReciever{

    private static final String TAG = "hikeActivity";
    Route route = null;
    Attempt attempt = null;
    activityHelper HikingHelper;
    private Button startStopButton;
    private Button resetTrailButton;
    private TextView routeNameTextView;
    private TextView loggingText;
    private TextView totalDistanceHikedTextView;
    private boolean logging = false;
    private final String activityType = "Hiking";
    private Switch showSelectedRoute;
    private Switch showPreviousRoute;
    private float totalDistanceHiked;
    DBHandler dbHandler = new DBHandler(this);
    GoogleMap googleMAP;
    SupportMapFragment mapFrag;
    LocationRequest locrequest;
    GoogleApiClient googleAPIclient;
    Polyline selectedRoute = null;
    Polyline previousTrail = null;
    boolean followUser = true;
    Location lastLocation;
    ArrayList<Location> locationArray = new ArrayList<Location>();

    private int heartRate;
    protected TextView hrTextView=null;
    private HRSensorHandler hrHandler;
    protected Button sensorReconnect=null;
    protected FloatingActionButton sensorHelp=null;

    TextView timerTextViewL;
    long startTime= 0;
    int noti_id=1;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            timerTextViewL.setText("Time elapsed: " + String.format("%d:%02d", minutes, seconds));
            notificationOp(noti_id, String.format("%d:%02d", minutes, seconds), String.format("%.2f", HikingHelper.getTotalDistance()));
            timerHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_hike);
        hrTextView=(TextView)findViewById(R.id.heartRateText);
        hrHandler=new HRSensorHandler(this);
        sensorReconnect=(Button)findViewById(R.id.hrReconnectHike);
        sensorHelp=(FloatingActionButton)findViewById(R.id.hrReconectHelp);

        startStopButton = (Button) findViewById(R.id.startStopHiking);
        resetTrailButton = (Button) findViewById(R.id.resetTrail);
        resetTrailButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ArrayList<Location> location = new ArrayList<Location>() {{
                    add(lastLocation);
                }};
                previousTrail.remove();
                previousTrail = addPolyLine(location, "PreviousTrail");
            }
        });
        showPreviousRoute = (Switch) findViewById(R.id.showPreviousRoute);
        showPreviousRoute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    previousTrail.setVisible(true);
                } else {
                    previousTrail.setVisible(false);
                }
            }
        });

        loggingText = (TextView) findViewById(R.id.loggingText);
        routeNameTextView = (TextView) findViewById(R.id.routeNameHiking);
        totalDistanceHikedTextView = (TextView) findViewById(R.id.distanceTravelled);
        Intent receivedIntent = getIntent();    //retrieve the intent that was sent to check if it has a Route object
        if (receivedIntent.hasExtra("route")) {  //if the intent has a route object
            route = (Route) receivedIntent.getSerializableExtra("route");
            routeNameTextView.setText(route.getRouteName());
            showSelectedRoute = (Switch) findViewById(R.id.showSelectedRoute);
            showSelectedRoute.setVisibility(View.VISIBLE);
            showSelectedRoute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        selectedRoute.setVisible(true);
                    } else {
                        selectedRoute.setVisible(false);
                    }
                }
            });
            Log.d(TAG, "Route object received by hikeActivity");
        }
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        timerTextViewL = (TextView) findViewById(R.id.timerTextView);

        startStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!logging) {
                    logging = true;
                    HikingHelper = new activityHelper(hikeActivity.this, 1);
                    HikingHelper.startActivity(null); //start logging samples
                    connectClicked();
                    startTime = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnable, 0);
                    startStopButton.setText("Stop logging");
                    loggingText.setVisibility(View.VISIBLE);
                    startUpdateStatsThread();
                } else {
                    confirmDialog();
                }
            }
        });

        //handles reconnection to heart rate sensor
        sensorReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectClicked();
            }
        });
        sensorHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorHelpDialog();
            }
        });
    }

    private void confirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Stop logging route")
                .setMessage("Are you sure you want to stop logging your route?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        HikingHelper.stopActivity();
                        logging = false;
                        disconnectClicked();
                        timerHandler.removeCallbacks(timerRunnable);
                        startStopButton.setText("Start logging");
                        loggingText.setVisibility(View.INVISIBLE);
                        NewRouteDialog();
                        long timelastSample = HikingHelper.getTimeLastsample();    //get final stats for display
                        float FinalDistance = HikingHelper.getTotalDistance();    //get final stats for display
                        showStatsDialog(timelastSample, FinalDistance);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void notificationOp(int id, String time, String distance){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Trail : Hiking");
        builder.setContentText("Time: " + timerTextViewL.getText()+ ", Distance : " + HikingHelper.getTotalDistance());
        NotificationManager NM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NM.notify(id,builder.build());
    }



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
                        String inputRouteName = input.getText().toString();
                        boolean routeNameExists = dbHandler.doesRouteNameExist(inputRouteName);
                        if (!(routeNameExists || inputRouteName.isEmpty())) {
                            //the user wants to save the route. it obviously means he wants to save the attempt with it as well. so we need to build both objects.
                            int totaltime = (int) HikingHelper.getTimeLastsample() / 1000;
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd_HHmm");//added start time so that attempts made on the same day can be differentiated in historyActivity
                            String currentDateandTime = sdf.format(new Date());
                            //instantiating a new route object with the constructor for the case in which we have no rowID yet
                            route = new Route(inputRouteName, activityType, HikingHelper.getTotalDistance(), totaltime, currentDateandTime, HikingHelper.getCoordinatesFileName());
                            dbHandler.addRoute(route);
                            Log.d(TAG, "Route object added to ROUTE_TABLE");
                            attempt=new Attempt(route, totaltime, currentDateandTime, route.getSnapshotURL());
                            dbHandler.addAttempt(attempt); //adding the attempt
                            Log.d(TAG, "Attempt object built and added to database");
                            dialog.dismiss();
                        } else if (routeNameExists) {
                            Toast.makeText(hikeActivity.this, "Route name already exists", Toast.LENGTH_SHORT).show();
                        } else if (inputRouteName.isEmpty()) {
                            Toast.makeText(hikeActivity.this, "Route name cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    private Polyline addPolyLine(ArrayList<Location> routeCoordinates, String typeOfPolyLine) {
        ArrayList<LatLng> routeCoordinatesLatLng = new ArrayList<>();
        for (int i = 0; i < routeCoordinates.size(); i++) {
            routeCoordinatesLatLng.add(new LatLng(routeCoordinates.get(i).getLatitude(), routeCoordinates.get(i).getLongitude()));
        }
        if (typeOfPolyLine.equals("SelectedRoute")) {
            return googleMAP.addPolyline(new PolylineOptions().addAll(routeCoordinatesLatLng).width(8).color(Color.BLUE));
        } else if (typeOfPolyLine.equals("PreviousTrail")) {
            return googleMAP.addPolyline(new PolylineOptions().addAll(routeCoordinatesLatLng).width(8).color(Color.RED));
        } else {
            return null;
        }
    }


    private void showStatsDialog(long timeLastSample, double FinalDistance) {

        long time = timeLastSample / (60000); //in minutes
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Hike ended");
        alertDialog.setMessage("You have hiked " + String.format("%.2f", FinalDistance) + " km in " + (time) + " minutes.\n");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


//    @Override
//    public void onPause() {
//        super.onPause();
//        if (googleAPIclient != null) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(googleAPIclient, this);
//        }
//    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMAP = googleMap;
        googleMAP.setPadding(0, 400, 0, 0);
        googleMAP.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        googleMAP.setOnCameraIdleListener(this);
        googleMAP.setOnCameraMoveStartedListener(this);
        googleMAP.setOnCameraMoveListener(this);
        googleMAP.setOnCameraMoveCanceledListener(this);
        googleMAP.setOnMyLocationButtonClickListener(this);
        buildGoogleApiClient();
        googleMAP.setMyLocationEnabled(true);
        if (!(route == null)) {
            selectedRoute = addPolyLine(route.buildLocationArray(), "SelectedRoute");
        }
        previousTrail = addPolyLine(locationArray, "PreviousTrail");
    }

    protected synchronized void buildGoogleApiClient() {
        googleAPIclient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleAPIclient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        locrequest = new LocationRequest();
        locrequest.setInterval(1000);
        locrequest.setFastestInterval(1000);
        locrequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleAPIclient, locrequest, this);
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleAPIclient);
        if (lastLocation != null) {
            locationArray.add(lastLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        if(lastLocation == null) {lastLocation = location;}
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (followUser) {
            googleMAP.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
        }
        Log.d(TAG, String.valueOf(location.getAccuracy()));
        if (location.getAccuracy() < 15 && lastLocation.distanceTo(location) > 10) {
            List<LatLng> points = previousTrail.getPoints();
            points.add(latLng);
            previousTrail.setPoints(points);
            lastLocation = location;
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        followUser = true;
        return false;
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            //Log.d(TAG, "GESTURE LISTENER");
            followUser = false;
        } else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION) { //Google documentation: indicates that the API has moved the camera in response to a non-gesture user action, such as tapping the zoom button, tapping the My Location button, or clicking a marker.
            //Log.d(TAG, "NONGESTURE LISTENER");
        } else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION) {
            //Log.d(TAG, "DEVELOPER_ANIMATION LISTENER");
        }
    }

    @Override
    public void onCameraMove() {
        //Log.d(TAG, "ONCAMERAMOVE");
    }

    @Override
    public void onCameraMoveCanceled() {
        //Log.d(TAG, "ONCAMERAMOVECANCELED");
    }

    @Override
    public void onCameraIdle() {
        //Log.d(TAG, "ONCAMERAIDLE");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (logging)// if logging is still true
        {
            HikingHelper.stopActivity();
            disconnectClicked();//disconnect from HxM
        }
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
            hrHandler.Connect();
            hrHandler.setReciver(hikeActivity.this);
            //make sensor reconnection buttons disappear if connection is established
            sensorReconnect.setVisibility(View.INVISIBLE);
            sensorHelp.setVisibility(View.INVISIBLE);
        }catch (RuntimeException e) {
            hrTextView.setText("error connecting to HxM");
            sensorReconnect.setVisibility(View.VISIBLE);
            sensorHelp.setVisibility(View.VISIBLE);
        }
    }

    //disconnect with HxM HR Sensor
    private void disconnectClicked(){
        try{
            hrHandler.setReciver(null);
            hrHandler.Disconnect();
            sensorReconnect.setVisibility(View.INVISIBLE);
            sensorHelp.setVisibility(View.INVISIBLE);
        }catch (RuntimeException e){
            hrTextView.setText("error disconnecting from HxM"+e.getMessage());//never encountered so far, put exception message here to debug
        }
    }
    final Handler newHandler=new Handler(){
        public void handleMessage(Message msg){
            heartRate=msg.getData().getInt("HeartRate");
            hrTextView.setText(Integer.toString(heartRate));
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
    //thread that receives distance updates from the service
    private void startUpdateStatsThread() {
        Thread th = new Thread(new Runnable() {

            public void run() {
                while (logging == true) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "TOTAL DISTANCE " + String.valueOf(HikingHelper.getTotalDistance()));
                            totalDistanceHikedTextView.setText("Distance traveled: " + String.format("%.2f", HikingHelper.getTotalDistance()) + " km");
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

}
