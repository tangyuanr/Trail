package com.example.kevin.trail;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;

import java.util.ArrayList;

public class SelectRouteRunning extends AppCompatActivity {

    DBHandler dbhandler = new DBHandler(this);  //instantiate and initialize dbHandler
    ArrayList<Route> listOfRoutes = new ArrayList<Route>();     //list of Route objects that will contain all the routes
    private int routeID = -1;
    ListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_example_listview);


        listOfRoutes = dbhandler.getAllRoutes();    //get all the routes stored in the MASTER_TABLE_RUN database
        listview = (ListView) findViewById(R.id.usage_example_listview);
        listview.setAdapter(new RouteAdapter(SelectRouteRunning.this, listOfRoutes));


        //this is called when an item in the listview has been clicked (when the user chooses a route)
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SelectRouteRunning.this, runActivity.class);
                //building the intent to pass the int. the 1st row (1st route) returns an int = 0. 2nd route returns an int of 1, etc. we need to send this to the RunActivity.
                intent.putExtra("ROUTEID", (int)id);
                startActivity(intent);
            }
        });

        //this inflates the "New Route" button at the end of the list
        Button newRouteButton = new Button(this);
        newRouteButton.setText("New Route");
        listview.addFooterView(newRouteButton);
        newRouteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(SelectRouteRunning.this, runActivity.class);
                intent.putExtra("ROUTEID", routeID);     //routeID has been initialized to -1, and this value of ROUTEID will be understood by Running Activity to be "New Route".
                startActivity(intent);
            }
        });





    }

}
