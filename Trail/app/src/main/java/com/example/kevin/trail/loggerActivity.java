package com.example.kevin.trail;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Andre & Jiayin
 */

public class loggerActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMapLongClickListener,
        IHeartRateReciever {

    private static final String TAG = "loggerActivity";
    Route route = null;
    Attempt attempt = null;
    activityHelper activityhelper;
    private Button startStopButton;
    private Button resetTrailButton;
    private TextView loggingText;
    private TextView totalDistanceTravelledTextView;
    private boolean logging = false;
    private String activityType = null;
    private Switch showSelectedRoute;
    private Switch showPreviousRoute;
    private float totalDistanceHiked;
    DBHandler dbHandler = new DBHandler(this);
    GoogleMap googleMAP;
    SupportMapFragment mapFrag;
    LocationRequest locrequest;
    String routeOrAttempt;
    GoogleApiClient googleAPIclient;
    Polyline selectedRoute = null;
    Polyline previousTrail = null;
    boolean followUser = true;
    Location lastLocation;
    ArrayList<Location> locationArray = new ArrayList<Location>();
    private String imagefilename;
    private float totalDistanceGoogleAPI = 0;
    private long lastTimeForSpeed = 0;
    private int totalBMP = 0;
    private int counter = 0;
    private long counterStandingStill = 0;
    private float speedGoogleApi;
    protected TextView hrTextView = null;
    //private HRSensorHandler hrHandler;
    protected Button sensorReconnect = null;
    protected FloatingActionButton sensorHelp = null;
    private sharedPreferenceHelper sharedPref;
    private double totalCaloriesBurnt = 0;
    protected TextView caloriesTxtView = null;
    protected TextView paceOrSpeedText = null;
    private Button toMap;
    private RelativeLayout mapHeadLayout;
    private Button toStats;
    private RelativeLayout headerLayout;
    private long millis;
    private long oldTimeForSpeed = 0;
    private long newTimeForSpeed = 0;

    TextView timerTextViewL;
    long startTime = 0;
    int noti_id = 1;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            timerTextViewL.setText("Time elapsed: " + String.format("%d:%02d", minutes, seconds));
            notificationOp(noti_id, String.format("%d:%02d", minutes, seconds), String.format("%.2f", totalDistanceGoogleAPI));
            timerHandler.postDelayed(this, 500);
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_logger);
        hrTextView = (TextView) findViewById(R.id.heartRateText);
        //hrHandler=new HRSensorHandler(this);
        sensorReconnect = (Button) findViewById(R.id.hrReconnectHike);
        sensorHelp = (FloatingActionButton) findViewById(R.id.hrReconectHelp);
        sharedPref = new sharedPreferenceHelper(loggerActivity.this);
        caloriesTxtView = (TextView) findViewById(R.id.caloriesTextView);


        //action bar
        Toolbar loggerToolbar = (Toolbar) findViewById(R.id.loggerActionBar);
        setSupportActionBar(loggerToolbar);
        headerLayout = (RelativeLayout) findViewById(R.id.relativeLayout2);
        mapHeadLayout = (RelativeLayout) findViewById(R.id.relativeLayout3);
        //UI ENHANCEMENT OBJECTS
        toMap = (Button) findViewById(R.id.statsToMapButton);
        toStats = (Button) findViewById(R.id.backToStats);
        TextView paceOrSpeedLabel = (TextView) findViewById(R.id.paceOrSpeedLabel);
        paceOrSpeedText = (TextView) findViewById(R.id.paceOrSpeedText);


        startStopButton = (Button) findViewById(R.id.startStop);
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
                if (isChecked && mapHeadLayout.getVisibility() == View.VISIBLE) {
                    previousTrail.setVisible(true);
                } else {
                    previousTrail.setVisible(false);
                }
            }
        });

        loggingText = (TextView) findViewById(R.id.loggingText);
        totalDistanceTravelledTextView = (TextView) findViewById(R.id.distanceTravelled);
        Intent receivedIntent = getIntent();    //retrieve the intent that was sent to check if it has a Route object
        if (receivedIntent.hasExtra("activityType")) {
            activityType = receivedIntent.getStringExtra("activityType");
        }


        showSelectedRoute = (Switch) findViewById(R.id.showSelectedRoute);
        if (receivedIntent.hasExtra("route")) {  //if the intent has a route object
            route = (Route) receivedIntent.getSerializableExtra("route");
            routeOrAttempt = "attempt";
            getSupportActionBar().setTitle(activityType + " on route: " + route.getRouteName());
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
            Log.d(TAG, "Route object received by loggerActivity");
        } else {
            routeOrAttempt = "route";
        }

        /*
        *
        * UI related
        *
        * */

        //set toolbar color according to activity type
        if ("Hiking".equals(activityType)) {
            loggerToolbar.setBackgroundColor(Color.parseColor("#66cc66"));//set toolbar color
            headerLayout.setBackgroundColor(Color.parseColor("#409c5e"));//set background color
            mapHeadLayout.setBackgroundColor(Color.parseColor("#409c5e"));
            if (!receivedIntent.hasExtra("route"))
                getSupportActionBar().setTitle("Hiking");//set toolbar title
        } else if ("Running".equals(activityType)) {
            loggerToolbar.setBackgroundColor(Color.parseColor("#ff6d92"));
            headerLayout.setBackgroundColor(Color.parseColor("#ffb1c5"));
            mapHeadLayout.setBackgroundColor(Color.parseColor("#ffb1c5"));
            if (!receivedIntent.hasExtra("route"))
                getSupportActionBar().setTitle("Running");

            paceOrSpeedLabel.setText("Pace: ");
            paceOrSpeedLabel.setVisibility(View.VISIBLE);
            paceOrSpeedText.setText("--");
            paceOrSpeedText.setVisibility(View.VISIBLE);
        } else if ("Biking".equals(activityType)) {
            loggerToolbar.setBackgroundColor(Color.parseColor("#99ccff"));
            headerLayout.setBackgroundColor(Color.parseColor("#bfdfff"));
            mapHeadLayout.setBackgroundColor(Color.parseColor("#bfdfff"));
            if (!receivedIntent.hasExtra("route"))
                getSupportActionBar().setTitle("Biking");

            paceOrSpeedLabel.setText("Speed: ");
            paceOrSpeedLabel.setVisibility(View.VISIBLE);
            paceOrSpeedText.setText("0 km/h");
            paceOrSpeedText.setVisibility(View.VISIBLE);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        toMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOnline()) {
                    headerLayout.setVisibility(View.INVISIBLE);
                    //display layer 2
                    mapHeadLayout.setVisibility(View.VISIBLE);
                    showSelectedRoute.setVisibility(View.VISIBLE);
                    resetTrailButton.setVisibility(View.VISIBLE);
                    toStats.setVisibility(View.VISIBLE);
                }
                else {
                    Toast.makeText(loggerActivity.this, "Trail needs internet connection to display the map.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        toStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hide layer 2, display layer 1
                mapHeadLayout.setVisibility(View.INVISIBLE);
                showSelectedRoute.setVisibility(View.INVISIBLE);
                resetTrailButton.setVisibility(View.INVISIBLE);
                toStats.setVisibility(View.INVISIBLE);
                headerLayout.setVisibility(View.VISIBLE);
            }
        });

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        timerTextViewL = (TextView) findViewById(R.id.timerTextView);

        startStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!logging) {

                    PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(loggerActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, new PermissionsResultAction() {

                                @Override
                                public void onGranted() {

                                    logging = true;
                                    totalDistanceGoogleAPI = 0;
                                    totalDistanceTravelledTextView.setText("0.00 km");
                                    oldTimeForSpeed = System.currentTimeMillis();
                                    newTimeForSpeed = 0;
                                    speedGoogleApi = 0;
                                    activityhelper = new activityHelper(loggerActivity.this, activityType);
                                    activityhelper.startActivity(null); //start logging samples
                                    connectClicked();
                                    startTime = System.currentTimeMillis();
                                    timerHandler.postDelayed(timerRunnable, 0);
                                    startStopButton.setText("Stop logging");
                                    loggingText.setVisibility(View.VISIBLE);
                                    startUpdateStatsThread();

                                    if (MainActivity.heartRate == 0) {
                                        sensorReconnect.setVisibility(View.VISIBLE);
                                        sensorHelp.setVisibility(View.VISIBLE);
                                    }
                                }

                                @Override
                                public void onDenied(String permission) {
                                    Toast.makeText(loggerActivity.this, "Trail must have location permissions.", Toast.LENGTH_SHORT).show();
                                }
                            });

                } else {
                    confirmDialog();
                }
            }
        });


        //handles reconnection to heart rate sensor
        sensorReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MainActivity.hrHandler.Connect();
                    MainActivity.hrHandler.setReciver(loggerActivity.this);
                    //make sensor reconnection buttons disappear if connection is established
                    sensorReconnect.setVisibility(View.INVISIBLE);
                    sensorHelp.setVisibility(View.INVISIBLE);

                } catch (RuntimeException e) {
                    hrTextView.setText("error connecting to HxM");
                    sensorReconnect.setVisibility(View.VISIBLE);
                    sensorHelp.setVisibility(View.VISIBLE);
                }
            }
        });
        sensorHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorHelpDialog();
            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
    }

    private void confirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder
                .setTitle("Stop logging " + routeOrAttempt)
                .setMessage("Are you sure you want to stop logging your " + routeOrAttempt + "?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        activityhelper.stopActivity();
                        //disconnectClicked();
                        logging = false;
                        timerHandler.removeCallbacks(timerRunnable);
                        startStopButton.setText("Start logging");
                        loggingText.setVisibility(View.INVISIBLE);
                        if(activityhelper.getCurrentNumberOfSamples() > 0) {
                            if (!(route == null)) {
                                SaveAttemptDialog();

                            } else {
                                NewRouteDialog();
                            }
                            showStatsDialog(millis, totalDistanceGoogleAPI);
                        }
                        else {
                            Toast.makeText(loggerActivity.this, "Logging cancelled", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void notificationOp(int id, String time, String distance) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Trail : " + activityType);
        builder.setContentText("Time: " + timerTextViewL.getText() + ", Distance : " + totalDistanceGoogleAPI);
        NotificationManager NM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NM.notify(id, builder.build());
    }

    private void SaveAttemptDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Save attempt?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int totaltime = (int) activityhelper.getTimeLastsample() / 1000;
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd_HHmm");//added start time so that attempts made on the same day can be differentiated in historyActivity
                String currentDateandTime = sdf.format(new Date());
                String snapshotURL = route.getStaticAPIURL(loggerActivity.this, 250, 250);
                imagefilename = sdf.format(new Date()) + ".JPEG";
                imageDownload(loggerActivity.this, snapshotURL, imagefilename);
                attempt = new Attempt(route, totaltime, totalDistanceGoogleAPI, currentDateandTime, snapshotURL, totalBMP / counter, (int) totalCaloriesBurnt, imagefilename);
                dbHandler.addAttempt(attempt); //save the attempt to the database
                Log.d(TAG, "Attempt added to the database");
                Toast.makeText(loggerActivity.this, "Attempt saved to existing route.", Toast.LENGTH_SHORT).show();
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


    private void NewRouteDialog() {
        final EditText input = new EditText(this);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(input)
                .setTitle("Enter route name:")
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();
        dialog.setCancelable(false);//prevent the dialog from being closed by outside touch
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button buttonSave = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                buttonSave.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String inputRouteName = input.getText().toString();
                        if (inputRouteName.matches("^.*[^a-zA-Z0-9 ].*$")) {
                            Toast.makeText(loggerActivity.this, "The route name can only contain alphanumeric characters", Toast.LENGTH_SHORT).show();
                        } else {
                            boolean routeNameExists = dbHandler.doesRouteNameExist(inputRouteName);
                            if (!(routeNameExists || inputRouteName.isEmpty())) {
                                //the user wants to save the route. it obviously means he wants to save the attempt with it as well. so we need to build both objects.
                                int totaltime = (int) activityhelper.getTimeLastsample() / 1000;
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd_HHmm");//added start time so that attempts made on the same day can be differentiated in historyActivity
                                String currentDateandTime = sdf.format(new Date());
                                String locality = getLocality();
                                Route route_dummy = new Route("dummy route name", "dummy activity type", 0, 0, "dummy time", activityhelper.getCoordinatesFileName(), "dummy locality", "dummy filename");
                                String snapshotURL = route_dummy.getStaticAPIURL(loggerActivity.this, 225, 140);
                                imagefilename = sdf.format(new Date()) + ".JPEG";
                                imageDownload(loggerActivity.this, snapshotURL, imagefilename);
                                //instantiating a new route object with the constructor for the case in which we have no rowID yet
                                route = new Route(inputRouteName, activityType, totalDistanceGoogleAPI, totaltime, currentDateandTime, activityhelper.getCoordinatesFileName(), locality, imagefilename);
                                dbHandler.addRoute(route);
                                Log.d(TAG, "Route object added to ROUTE_TABLE");
                                attempt = new Attempt(route, totaltime, totalDistanceGoogleAPI, currentDateandTime, snapshotURL, totalBMP / counter, (int) totalCaloriesBurnt, imagefilename);
                                dbHandler.addAttempt(attempt); //adding the attempt
                                Log.d(TAG, "Attempt object built and added to database");
                                Toast.makeText(loggerActivity.this, "New route saved", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else if (routeNameExists) {
                                Toast.makeText(loggerActivity.this, "Route name already exists", Toast.LENGTH_SHORT).show();
                            } else if (inputRouteName.isEmpty()) {
                                Toast.makeText(loggerActivity.this, "Route name cannot be empty", Toast.LENGTH_SHORT).show();
                            }
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

    private String getLocality() {
        Geocoder geocoder;
        List<Address> addresses;
        String city;
        String province;
        String locality = "";
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
            city = addresses.get(0).getLocality();
            if(city==null) {
                city = "Montreal";
            }
            province = addresses.get(0).getAdminArea();
            locality = city + ", " + province;
        }
        catch(IOException ex) {
        }
        return locality;
    }


    private void showStatsDialog(long timeLastSample, double FinalDistance) {

        String time = String.format("%2d minutes and %2d seconds", TimeUnit.MILLISECONDS.toMinutes(millis), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        String string = null;
        switch (activityType) {
            case "Running":
                string = "Run";
                break;
            case "Hiking":
                string = "Hike";
                break;
            case "Biking":
                string = "Biking session";
                break;
        }
        alertDialog.setTitle(string + " ended");
        switch (activityType) {
            case "Running":
                string = "runned";
                break;
            case "Hiking":
                string = "hiked";
                break;
            case "Biking":
                string = "biked";
                break;
        }
        alertDialog.setMessage("You have " + string + " " + String.format("%.2f", FinalDistance) + " km in " + time);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


    @Override
    public void onMapLongClick(LatLng latLng) {

    }

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

        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(loggerActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, new PermissionsResultAction() {

                    @Override
                    public void onGranted() {
                        googleMAP.setMyLocationEnabled(true);
                    }

                    @Override
                    public void onDenied(String permission) {
                    }
                });

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

        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(loggerActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, new PermissionsResultAction() {

                    @Override
                    public void onGranted() {
                        getLocationUpdates();
                        if (lastLocation != null) {
                            locationArray.add(lastLocation);
                        }
                    }

                    @Override
                    public void onDenied(String permission) {
                    }
                });

        }

    private void getLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleAPIclient, locrequest, this);
    }



        @Override
        public void onConnectionSuspended ( int i){
        }

        @Override
        public void onConnectionFailed (ConnectionResult connectionResult){
        }

        @Override
        public void onLocationChanged (Location location){
            if (lastLocation == null) {
                lastLocation = location;
            }
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (followUser) {
                googleMAP.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
            }

            Log.d(TAG, String.valueOf(location.getAccuracy()));
            Log.d(TAG, "DISTANCE TO: "+ String.valueOf(lastLocation.distanceTo(location)));
            if (lastLocation.distanceTo(location) > 10) {
                List<LatLng> points = previousTrail.getPoints();
                points.add(latLng);
                previousTrail.setPoints(points);
                float distance = (lastLocation.distanceTo(location))/1000;
                newTimeForSpeed = System.currentTimeMillis();
                speedGoogleApi = (float) 3600*(distance / ((newTimeForSpeed/1000) - (oldTimeForSpeed/1000)));
                totalDistanceGoogleAPI += distance;
                lastLocation = location;
                oldTimeForSpeed = newTimeForSpeed;
            }

            if(!isOnline() && (mapHeadLayout.getVisibility() == View.VISIBLE)) {
                mapHeadLayout.setVisibility(View.INVISIBLE);
                showSelectedRoute.setVisibility(View.INVISIBLE);
                resetTrailButton.setVisibility(View.INVISIBLE);
                toStats.setVisibility(View.INVISIBLE);
                headerLayout.setVisibility(View.VISIBLE);
                Toast.makeText(loggerActivity.this, "Trail needs internet connection to display the map.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public boolean onMyLocationButtonClick () {
            followUser = true;
            return false;
        }

        @Override
        public void onCameraMoveStarted ( int reason){
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
        public void onCameraMove () {
            //Log.d(TAG, "ONCAMERAMOVE");
        }

        @Override
        public void onCameraMoveCanceled () {
            //Log.d(TAG, "ONCAMERAMOVECANCELED");
        }

        @Override
        public void onCameraIdle () {
            //Log.d(TAG, "ONCAMERAIDLE");
        }

        @Override
        protected void onDestroy () {
            googleAPIclient.disconnect();
            super.onDestroy();
            if (logging)// if logging is still true
            {
                activityhelper.stopActivity();
                //disconnectClicked();//disconnect from HxM
            }
        }

    @Override
    protected void onStop() {
        googleAPIclient.disconnect();
        super.onStop();
    }

    @Override
        public void heartRateReceived ( int heartRate){
            Message msg = new Message();
            msg.getData().putInt("HeartRate", heartRate);
            newHandler.sendMessage(msg);
        }

        //connect with HxM HR Sensor

    private void connectClicked() {
        try {
            //hrHandler.Connect();
            MainActivity.hrHandler.setReciver(loggerActivity.this);
            //make sensor reconnection buttons disappear if connection is established
            sensorReconnect.setVisibility(View.INVISIBLE);
            sensorHelp.setVisibility(View.INVISIBLE);
        } catch (RuntimeException e) {
            hrTextView.setText("error connecting to HxM");
            sensorReconnect.setVisibility(View.VISIBLE);
            sensorHelp.setVisibility(View.VISIBLE);
        }
    }

    //disconnect with HxM HR Sensor
    private void disconnectClicked() {
        try {
            //hrHandler.setReciver(null);
            //hrHandler.Disconnect();
            sensorReconnect.setVisibility(View.INVISIBLE);
            sensorHelp.setVisibility(View.INVISIBLE);
        } catch (RuntimeException e) {
            hrTextView.setText("error disconnecting from HxM" + e.getMessage());//never encountered so far, put exception message here to debug
            sensorReconnect.setVisibility(View.INVISIBLE);
            sensorHelp.setVisibility(View.INVISIBLE);
        }
    }

    final Handler newHandler = new Handler() {
        public void handleMessage(Message msg) {
            MainActivity.heartRate = msg.getData().getInt("HeartRate");
            String holder=MainActivity.heartRate + " BPM";
            hrTextView.setText(holder);
        }
    };

    //dialog that pops out when user clicks on the floating help button
    private void sensorHelpDialog() {
        AlertDialog helpDialog = new AlertDialog.Builder(this).create();
        helpDialog.setTitle("Trouble using HxM?");
        helpDialog.setMessage("Check if your Zephyr HxM is paired with your phone via bluetooth\n" +
                "Make sure your HxM is charged\n" +
                "Make sure the probes on the strap are placed onto your chest\n" +
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
                    try {
                        Thread.sleep(2000);
                        totalBMP += MainActivity.heartRate;
                        counter++;
                        totalCaloriesBurnt += caloriesCalculator(MainActivity.heartRate);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Log.d(TAG, "TOTAL DISTANCE " + String.valueOf(activityhelper.getTotalDistance()));
                            totalDistanceTravelledTextView.setText(String.format("%.2f", totalDistanceGoogleAPI) + " km");



                            float speed = speedGoogleApi;
                            if(activityType.equals("Biking")) {
                                if(speed < 1) {
                                    paceOrSpeedText.setText("0 km/h");
                                }
                                else{
                                    if(paceOrSpeedText.getText().toString().equals(String.format("%.1f", speed) + " km/h")) {
                                        counterStandingStill++;

                                        if(counterStandingStill > 10) {
                                            paceOrSpeedText.setText("0 km/h");
                                            speedGoogleApi = 0;
                                        }
                                    }
                                    else {
                                        paceOrSpeedText.setText(String.format("%.1f", speed) + " km/h");
                                        counterStandingStill = 0;
                                    }
                                }
                            }
                            else if(activityType.equals("Running")) {
                                if(speed > 1) { //km/h
                                    float pace = 60/speed; //min/km

                                    if(paceOrSpeedText.getText().toString().equals(String.format("%.1f", pace) + " min/km")) {
                                        counterStandingStill++;

                                        if(counterStandingStill > 10) {
                                            paceOrSpeedText.setText("--");
                                            speedGoogleApi = 0;
                                        }
                                    } else {
                                        paceOrSpeedText.setText(String.format("%.1f", pace) + " min/km");
                                        counterStandingStill = 0;
                                    }
                                }
                                else {
                                    paceOrSpeedText.setText("--");
                                }
                            }

                            //paceOrSpeedText.setText(String.format("%.2f", activityhelper.getTotalDistance() / 0.14));//0.14minutes=5s
                            caloriesTxtView.setText(String.format("%.2f", totalCaloriesBurnt) + " KCal");
                            if (MainActivity.heartRate == 0) {
                                sensorReconnect.setVisibility(View.VISIBLE);
                                sensorHelp.setVisibility(View.VISIBLE);
                            } else {
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

    //calculate calories based on heart rate, called inside the thread at every 5 second
    private double caloriesCalculator(int HR) {
        double age = Double.parseDouble(sharedPref.getProfileAge());
        String gender = sharedPref.getProfileGender();
        double weight = Double.parseDouble(sharedPref.getProfileWeight());
        double LB_to_KG = 0.453592;
        double DURATION = (double) 5 / 60;//in minute

        double calories = 0;

        //calories formula for female
        if (gender.equals("m") || gender.equals("M")) {
            double age_factor = age * 0.2017;
            double weight_factor = weight * LB_to_KG * 0.1988;
            double HR_factor = HR * 0.6309;
            calories = age_factor - weight_factor + HR_factor - 55.0969;
            calories = calories * DURATION / 4.184;
            Log.e(TAG, "Weight: " + weight + ", Age: " + age + ", Gender: " + gender + ". With heart rate " + HR + ", calories calculated: " + calories);
        } else if (gender.equals("f") || gender.equals("F")) {
            double age_factor = age * 0.074;
            double weight_factor = weight * LB_to_KG * 0.1263;
            double HR_factor = HR * 0.4472;
            calories = (age_factor - weight_factor + HR_factor - 20.4022) * DURATION / 4.184;
            Log.e(TAG, "Weight: " + weight + ", Age: " + age + ", Gender: " + gender + ". With heart rate " + HR + ", calories calculated: " + calories);
        }

        if (calories < 0 || MainActivity.heartRate<40)
            calories = 0;


        Log.e(TAG, "END OF METHOD: gender " + gender + ", age: " + age + " , weight: " + weight + ", calories: " + calories);
        return calories;
    }

    //map snapshot saving
    public void imageDownload(Context context, String url, String filename) {
        Picasso.with(context)
                .load(url)
                .into(getTarget(filename));
    }

    public Target getTarget(final String filename) {
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String path = Trail.getAppContext().getFilesDir() + "/";
                        File file = new File(path + filename);

                        try {
                            file.createNewFile();
                            FileOutputStream ostream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                            ostream.flush();
                            ostream.close();

                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                }).start();
            }

            @Override
            public void onBitmapFailed(Drawable errordrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeholderdrawable) {
            }
        };
        return target;
    }

    //action bar related methods:
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(menuItem);
    }

}
