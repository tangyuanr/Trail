package com.example.kevin.trail;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class comparison extends AppCompatActivity {
    EditText recentdistance , previousdistance , distancechange , recenttime , previoustime , timechange, previousspeed, recentspeed , speedchange , previouspace , recentpace , pacechanges;
    DBHandler db = new DBHandler(this);
    ArrayList<Route> myroute;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparison);

        DBHandler db = new DBHandler(getApplicationContext());
        initializer();
        fetching();

    }

    public void initializer(){
        recentdistance = (EditText) findViewById(R.id.recentdistance);
        previousdistance = (EditText) findViewById(R.id.previousdistance);
      //  distancechange = (EditText) findViewById(R.id.distancechange);
        recenttime = (EditText) findViewById(R.id.recenttime);
        previoustime = (EditText) findViewById(R.id.previoustime);
      //  timechange = (EditText) findViewById(R.id.timechange);
        previousspeed = (EditText) findViewById(R.id.previousspeed);
        recentspeed = (EditText) findViewById(R.id.recentspeed);
        speedchange = (EditText) findViewById(R.id.speedchange);
        previouspace = (EditText) findViewById(R.id.previouspace);
        recentpace = (EditText) findViewById(R.id.recentpace);
        pacechanges = (EditText) findViewById(R.id.pacechanges);
    }

    public void fetching(){
        if(!(db.getRoutes("").isEmpty())) {
            if(db.getRoutes("").get(db.getRoutes("").size()-1).getActivityType().equals("Running"))   {                 ///checking if latest activity is running

                myroute = db.getRoutes("Running");

            }
            if(db.getRoutes("").get(db.getRoutes("").size()-1).getActivityType().equals("Hiking"))   {                 ///checking if latest activity is running

                myroute = db.getRoutes("Hiking");

            }
            if(db.getRoutes("").get(db.getRoutes("").size()-1).getActivityType().equals("Biking"))   {                 ///checking if latest activity is running

                myroute = db.getRoutes("Biking");

            }
            if (myroute != null) {
                if (myroute.size() >= 2) {
                    recentdistance.setText("" + myroute.get(myroute.size() - 1).getTotalDistance()+"km");
                    previousdistance.setText("" + myroute.get(myroute.size() - 2).getTotalDistance()+"km");
                   // distancechange.setText("" + ((myroute.get(myroute.size() - 1).getTotalDistance()) - (myroute.get(myroute.size() - 2).getTotalDistance())));
                    recenttime.setText("" + myroute.get(myroute.size() - 1).getBestTime() +"s");
                    previoustime.setText("" + myroute.get(myroute.size() - 2).getBestTime()+"s");
                   // timechange.setText("" + ((myroute.get(myroute.size() - 1).getBestTime()) - (myroute.get(myroute.size() - 2).getTotalDistance())));

                    recentspeed.setText("" + ((myroute.get(myroute.size() - 1).getTotalDistance())*(1000)) / ((myroute.get(myroute.size() - 1).getBestTime()))+"m/s");
                    previousspeed.setText("" + ((myroute.get(myroute.size() - 2).getTotalDistance())*(1000)) / ((myroute.get(myroute.size() - 2).getBestTime()))+"m/s");
                    speedchange.setText("" + (((((myroute.get(myroute.size() - 1).getTotalDistance())*(1000)) / ((myroute.get(myroute.size() - 1).getBestTime())))) - (((myroute.get(myroute.size() - 2).getTotalDistance())*(1000)) / ((myroute.get(myroute.size() - 2).getBestTime()))))+"m/s");
                    previouspace.setText("" + (myroute.get(myroute.size() - 2).getBestTime()) / ((60000) * myroute.get(myroute.size() - 2).getTotalDistance())+"min/km");//min per km
                    recentpace.setText("" + (myroute.get(myroute.size() - 1).getBestTime()) / ((60000) * myroute.get(myroute.size() - 1).getTotalDistance())+"min/km");//min per km
                    pacechanges.setText("" + ((myroute.get(myroute.size() - 1).getBestTime()) / ((60000) * myroute.get(myroute.size() - 1).getTotalDistance()) - ((myroute.get(myroute.size() - 2).getBestTime()) / ((60000) * myroute.get(myroute.size() - 2).getTotalDistance())))+"min/km");
                }


            }

        }









    }
}
