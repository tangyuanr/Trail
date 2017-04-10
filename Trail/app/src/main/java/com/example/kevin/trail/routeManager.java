package com.example.kevin.trail;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.roughike.bottombar.TabSelectionInterceptor;

import java.util.ArrayList;

/**
 * Created by Ezekiel
 * Basic Route Selecter
 * Highest priority feature to add: a delete feature
 */


public class routeManager extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    DBHandler dbhandler = new DBHandler(this);  //instantiate and initialize dbHandler
    ArrayList<Route> listOfRoutes = new ArrayList<Route>();     //list of Route objects that will contain all the routes
    ArrayList<Route> allroutes = new ArrayList<>();
    ListView listview;
    private static final String TAG = "routeManager";
    private String activityType;
    protected Toolbar routeManagerToolbar;
    private ActionMenuView amvMenu;
    MenuItem deleteButton;
    private Button newroutebutton;
    String routeNameSelected;
    private GoogleApiClient googleAPIclient = null;
    private Location currentLocation = null;
    private RouteAdapter listviewadapter;
    BottomBar bottomBar;




    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        if (currentLocation == null) {
            currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleAPIclient);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routemanager);
        listview = (ListView) findViewById(R.id.listviewroutes);

        Intent receivedIntent = getIntent();
        if(receivedIntent.hasExtra("activityType")) {
            activityType = (String) receivedIntent.getSerializableExtra("activityType");
        }


        allroutes= dbhandler.getRoutes(activityType);    //get all the routes stored in the route table, activity type "Running". this is for the ActivityType column in the database.
        listOfRoutes = allroutes;
        listviewadapter = new RouteAdapter(routeManager.this, listOfRoutes);
        listview.setAdapter(listviewadapter); // custom routeAdapter to bind the data to the listview

        if (googleAPIclient == null) {
            googleAPIclient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        }


        bottomBar = (BottomBar) findViewById(R.id.bottomBarRouteManager);
        bottomBar.setTabSelectionInterceptor(new TabSelectionInterceptor() {
            @Override
            public boolean shouldInterceptTabSelection(@IdRes int oldTabId, @IdRes int newTabId) {
                if (newTabId == R.id.tab_Nearby && (currentLocation == null)) {
                    Toast.makeText(routeManager.this, "Cannot get current location.", Toast.LENGTH_SHORT).show();
                    return true;
                }

                return false;
            }
        });
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_NewRoute) {
                    Log.d(TAG, "User selects new route");
                    Intent intent = new Intent(routeManager.this, loggerActivity.class);
                    intent.putExtra("activityType", activityType);
                    startActivity(intent);
                }

                if (tabId == R.id.tab_AllRoutes) {
                    listOfRoutes = allroutes;
                    listviewadapter = new RouteAdapter(routeManager.this, listOfRoutes);
                    listview.setAdapter(listviewadapter);
                }

                if (tabId == R.id.tab_Nearby) {

                    if(!(currentLocation == null)) {
                        listOfRoutes = getNearbyRoutes(activityType, allroutes);
                        listviewadapter = new RouteAdapter(routeManager.this, listOfRoutes);
                        listview.setAdapter(listviewadapter);
                    }
                    else {
                        Toast.makeText(routeManager.this, "Cannot get current location.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });




        //actionbar
        routeManagerToolbar=(Toolbar)findViewById(R.id.routeManagerActionBar);
        setSupportActionBar(routeManagerToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        amvMenu=(ActionMenuView)routeManagerToolbar.findViewById(R.id.amvMenu);
        amvMenu.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem menuItem){
                return onOptionsItemSelected(menuItem);
            }
        });
        routeManagerToolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));

        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);

        getSupportActionBar().setHomeAsUpIndicator(upArrow);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_selectroute, amvMenu.getMenu());
        //menuActionBar = menu;
        deleteButton = amvMenu.getMenu().findItem(R.id.deleteButton);
        return true;

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (android.R.id.home==item.getItemId()){
            Log.d("routeManager", "home clicked");
            finish();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ROUTEMANAGER", "ENTERED ONDESTROY");
    }


    public void onDeleteAction(MenuItem mi) {
        if(!(routeNameSelected==null)) {
            dbhandler.deleteRoute(routeNameSelected);
            deleteButton.setVisible(false);
            for(int i = 0; i < allroutes.size(); i++) {
                if(routeNameSelected.equals(allroutes.get(i).getRouteName())) {
                    allroutes.remove(i);
                }
            }
            for(int i = 0; i < listOfRoutes.size(); i++) {
                if(routeNameSelected.equals(listOfRoutes.get(i).getRouteName())) {
                    listOfRoutes.remove(i);
                }
            }

            listviewadapter = new RouteAdapter(routeManager.this, listOfRoutes);
            listview.setAdapter(listviewadapter);
            routeNameSelected = null;
            Toast.makeText(routeManager.this, "Route deleted", Toast.LENGTH_SHORT).show();
        }
    }


    //assume currentLocation is not null. null case needs to be handled before calling this method.
    private ArrayList<Route> getNearbyRoutes (String activityType, ArrayList<Route> allroutes) {
        ArrayList<Route> nearbyRoute = new ArrayList<>();
        int DISTANCE_THRESHOLD = 0;

        switch (activityType) {
            case "Running":
                DISTANCE_THRESHOLD = 1000; //max distance in meters from current location to be considered "nearby"
                break;
            case "Hiking":
                DISTANCE_THRESHOLD = 10000; //max distance in meters from current location to be considered "nearby"
                break;
            case "Biking":
                DISTANCE_THRESHOLD = 3000; //max distance in meters from current location to be considered "nearby"
                break;
        }

        for (int i = 0; i < allroutes.size(); i++) {
            float distance = currentLocation.distanceTo(allroutes.get(i).buildLocationArray().get(0));
            if(distance < DISTANCE_THRESHOLD) {
                nearbyRoute.add(allroutes.get(i));
            }
        }
        return nearbyRoute;
    }


    @Override
    protected void onStop() {
        googleAPIclient.disconnect();
        super.onStop();
        Log.d("ROUTEMANAGER", "ONSTOP");
    }

    @Override
    protected void onStart() {
        googleAPIclient.connect();
        super.onStart();


        //this is called when an item in the listview has been clicked (when the user chooses a route)
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Route selectedRoute = (Route) parent.getAdapter().getItem(position);
                Intent intent = new Intent(routeManager.this, loggerActivity.class);
                Log.d(TAG, selectedRoute.getRouteName());
                intent.putExtra("route", selectedRoute);//serializable object that can be passed in intents
                intent.putExtra("activityType", selectedRoute.getActivityType());
                startActivity(intent);
            }
        });

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteButton.setVisible(true);
                routeNameSelected = ((Route) parent.getAdapter().getItem(position)).getRouteName();
                return true;
            }
        });

    }

    @Override
    protected void onResume() {
        allroutes= dbhandler.getRoutes(activityType);
        listviewadapter = new RouteAdapter(routeManager.this, allroutes);
        listview.setAdapter(listviewadapter);
        bottomBar.selectTabAtPosition(0);
        super.onResume();
    }


}
