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

    DBHandler dbhandler = new DBHandler(this);
    ArrayList<Route> listOfRoutes = new ArrayList<Route>();
    private int routeID = -1;
    ListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_example_listview);


        listOfRoutes = dbhandler.getAllRoutes();
        listview = (ListView) findViewById(R.id.usage_example_listview);
        listview.setAdapter(new RouteAdapter(SelectRouteRunning.this, listOfRoutes));

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SelectRouteRunning.this, runActivity.class);
                intent.putExtra("ROUTEID", (int)id);
                startActivity(intent);
            }
        });

        Button newRouteButton = new Button(this);
        newRouteButton.setText("New Route");
        listview.addFooterView(newRouteButton);
        newRouteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(SelectRouteRunning.this, runActivity.class);
                intent.putExtra("ROUTEID", (int)-1);     //-1 for new route
                startActivity(intent);
            }
        });





    }

}
