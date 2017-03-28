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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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


public class hikeActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, IHeartRateReciever {
    private static final String TAG = "hikeActivity";
    Route route = null;
    Attempt attempt = null;
    activityHelper HikingHelper;
    private Button startStopButton;
    private TextView routeNameTextView;
    private boolean logging = false;
    private final String activityType = "Hiking";
    DBHandler dbHandler = new DBHandler(this);
    GoogleMap googleMAP;
    SupportMapFragment mapFrag;
    LocationRequest locrequest;
    GoogleApiClient googleAPIclient;

    Polyline routeLine=null;
    boolean firstSample=true;
    ArrayList<LatLng> locationArray=new ArrayList<LatLng>();

    private int heartRate;
    protected TextView hrTextView=null;
    private HRSensorHandler hrHandler;

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
            timerTextViewL.setText(String.format("%d:%02d", minutes, seconds));
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

        startStopButton = (Button) findViewById(R.id.startStopHiking);
        routeNameTextView = (TextView) findViewById(R.id.routeNameHiking);
        hrTextView=(TextView)findViewById(R.id.heartRateText);
        hrHandler=new HRSensorHandler(this);
        Intent receivedIntent = getIntent();    //retrieve the intent that was sent to check if it has a Route object
        if (receivedIntent.hasExtra("route")) {  //if the intent has a route object
            route = (Route) receivedIntent.getSerializableExtra("route");
            routeNameTextView.setText(route.getRouteName());
            Log.d(TAG, "Route object received by hikeActivity");
        }
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        timerTextViewL = (TextView) findViewById(R.id.timerTextView);

        startStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!logging) {
                    logging = true;
                    HikingHelper = new activityHelper(hikeActivity.this, 1);
                    HikingHelper.startActivity(null);
                    connectClicked();
                    startTime = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnable, 0);
                    startStopButton.setText("Stop logging");
                }
                else {
                    disconnectClicked();
                    NewRouteDialog();
                    logging = false;
                    timerHandler.removeCallbacks(timerRunnable);
                    startStopButton.setText("Start logging route");

                }

            }
        });
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save Route?");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //the user wants to save the route. it obviously means he wants to save the attempt with it as well. so we need to build both objects.
                int totaltime = (int)HikingHelper.getTimeLastsample()/1000;
                String inputRouteName = input.getText().toString();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String currentDateandTime = sdf.format(new Date());
                route = new Route(inputRouteName, activityType, HikingHelper.getTotalDistance(), totaltime, currentDateandTime, HikingHelper.getCoordinatesFileName() );
                long addedID = dbHandler.addRoute(route);
                HikingHelper.stopActivity();
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

    private void addPolyLine() {

        ArrayList<Location> routeCoordinates = route.buildLocationArray();
        ArrayList<LatLng> routeCoordinatesLatLng = new ArrayList<>();
        for (int i = 0; i < routeCoordinates.size(); i++) {
            routeCoordinatesLatLng.add(new LatLng(routeCoordinates.get(i).getLatitude(), routeCoordinates.get(i).getLongitude()));
        }
        googleMAP.addPolyline(new PolylineOptions().addAll(routeCoordinatesLatLng).width(8).color(Color.BLUE));
    }


    @Override
    public void onPause() {
        super.onPause();
        if (googleAPIclient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleAPIclient, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMAP = googleMap;
        googleMAP.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        buildGoogleApiClient();
        googleMAP.setMyLocationEnabled(true);
        if(!(route==null)) {addPolyLine();}

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

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        locationArray.add(latLng);
        if (!firstSample){
            routeLine.remove();
        }
        routeLine=googleMAP.addPolyline(new PolylineOptions().addAll(locationArray).width(8).color(Color.RED));

        googleMAP.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
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
        }catch (RuntimeException e) {
            hrTextView.setText("error connecting to HxM");
        }
    }

    //disconnect with HxM HR Sensor
    private void disconnectClicked(){
        try{
            hrHandler.setReciver(null);
            hrHandler.Disconnect();
        }catch (RuntimeException e){
            hrTextView.setText("error disconnecting from HxM");
        }
    }
    final Handler newHandler=new Handler(){
        public void handleMessage(Message msg){
            heartRate=msg.getData().getInt("HeartRate");
            hrTextView.setText(Integer.toString(heartRate));
        }
    };

}
