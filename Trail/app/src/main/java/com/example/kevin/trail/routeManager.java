package com.example.kevin.trail;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Ezekiel
 * Basic Route Selecter
 * Highest priority feature to add: a delete feature
 */


public class routeManager extends AppCompatActivity {

    DBHandler dbhandler = new DBHandler(this);  //instantiate and initialize dbHandler
    ArrayList<Route> listOfRoutes = new ArrayList<Route>();     //list of Route objects that will contain all the routes
    ListView listview;
    private static final String TAG = "routeManager";
    private String activityType;
    protected Toolbar routeManagerToolbar;
    private ActionMenuView amvMenu;
    MenuItem deleteButton;
    String routeNameSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_example_listview);
        listview = (ListView) findViewById(R.id.usage_example_listview);

        Intent receivedIntent = getIntent();
        if(receivedIntent.hasExtra("activityType")) {
            activityType = (String) receivedIntent.getSerializableExtra("activityType");
        }

        //this inflates the "New Route" button at the end of the list
        Button newRouteButton = new Button(this);
        newRouteButton.setText("New Route");
        listview.addFooterView(newRouteButton);
        newRouteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "User selects new route");
                Intent intent = new Intent(routeManager.this, loggerActivity.class);
                intent.putExtra("activityType", activityType);
                startActivity(intent);
            }
        });

       // actionBar = getSupportActionBar();
        //actionBar.show();
        routeManagerToolbar=(Toolbar)findViewById(R.id.routeManagerActionBar);
        setSupportActionBar(routeManagerToolbar);
        amvMenu=(ActionMenuView)routeManagerToolbar.findViewById(R.id.amvMenu);
        amvMenu.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem menuItem){
                return onOptionsItemSelected(menuItem);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_selectroute, amvMenu.getMenu());
        //menuActionBar = menu;
        deleteButton = amvMenu.getMenu().findItem(R.id.deleteButton);
        return true;

    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item){
//        if (R.id.deleteButton==item.getItemId()){
//
//        }
//        return super.onOptionsItemSelected(item);
//    }

    public void onDeleteAction(MenuItem mi) {
        if(!(routeNameSelected==null)) {
            dbhandler.deleteRoute(routeNameSelected);
            routeNameSelected = null;
            deleteButton.setVisible(false);
            onStart();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


        listOfRoutes = dbhandler.getRoutes(activityType);    //get all the routes stored in the route table, activity type "Running". this is for the ActivityType column in the database.
        listview.setAdapter(new RouteAdapter(routeManager.this, listOfRoutes)); // custom routeAdapter to bind the data to the listview


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


//    private Class<?> getClassIntent(String activity) {
//        if(activity.equals("Running")) {return runActivity.class;}
//        else if(activity.equals("Hiking")) {return loggerActivity.class;}
//        else {return null;}
//    }


}
