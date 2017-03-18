package com.example.kevin.trail;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Ezekiel
 * Basic Route Selecter
 * Highest priority feature to add: a delete feature
 */


public class SelectRouteRunning extends AppCompatActivity {

    DBHandler dbhandler = new DBHandler(this);  //instantiate and initialize dbHandler
    ArrayList<Route> listOfRoutes = new ArrayList<Route>();     //list of Route objects that will contain all the routes
    ListView listview;
    private static final String TAG = "SelectRouteRunning";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_example_listview);


        listOfRoutes = dbhandler.getRoutes("Running");    //get all the routes stored in the route table, activity type "Running". this is for the ActivityType column in the database.
        listview = (ListView) findViewById(R.id.usage_example_listview);
        listview.setAdapter(new RouteAdapter(SelectRouteRunning.this, listOfRoutes)); // custom routeAdapter to bind the data to the listview


        //this is called when an item in the listview has been clicked (when the user chooses a route)
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "User selects row #" + Long.toString(id));
                Intent intent = new Intent(SelectRouteRunning.this, runActivity.class);
                Route selectedRoute = dbhandler.getRoute((int)id+1); //fetch the Route object from the database, build the object so that we can send it to the runActivity
                intent.putExtra("route", selectedRoute);    //serializable object that can be passed in intents
                startActivity(intent);
            }
        });

        //this inflates the "New Route" button at the end of the list
        Button newRouteButton = new Button(this);
        newRouteButton.setText("New Route");
        listview.addFooterView(newRouteButton);
        newRouteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "User selects new route");
                Intent intent = new Intent(SelectRouteRunning.this, runActivity.class);
                startActivity(intent);
            }
        });





    }

}
