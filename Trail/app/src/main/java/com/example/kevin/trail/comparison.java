package com.example.kevin.trail;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class comparison extends AppCompatActivity {
    EditText    distancechange , timechange, speedchange , pacechange,title;
    DBHandler db = new DBHandler(this);
    ArrayList<Route> myroute;
    ArrayList<Attempt> myattempt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparison);

        DBHandler db = new DBHandler(getApplicationContext());
        initializer();
        fetching();

    }

    public void initializer(){
        title = (EditText) findViewById(R.id.title);
        title.setFocusable(false);
       // recentdistance = (EditText) findViewById(R.id.recentdistance);
       // recentdistance.setFocusable(false);
       // previousdistance = (EditText) findViewById(R.id.previousdistance);
       // previousdistance.setFocusable(false);
        distancechange = (EditText) findViewById(R.id.distancechange);
        distancechange.setFocusable(false);
       // recenttime = (EditText) findViewById(R.id.recenttime);
       // recenttime.setFocusable(false);
       // previoustime = (EditText) findViewById(R.id.previoustime);
       // previoustime.setFocusable(false);
        timechange = (EditText) findViewById(R.id.timechange);
        timechange.setFocusable(false);
        speedchange = (EditText) findViewById(R.id.speedchange);
        speedchange.setFocusable(false);
       // recentspeed = (EditText) findViewById(R.id.recentspeed);
       // recentspeed.setFocusable(false);
       // speedchange = (EditText) findViewById(R.id.speedchange);
       // speedchange.setFocusable(false);
       // previouspace = (EditText) findViewById(R.id.previouspace);
       // previouspace.setFocusable(false);
       // recentpace = (EditText) findViewById(R.id.recentpace);
       // recentpace.setFocusable(false);
        pacechange = (EditText) findViewById(R.id.pacechange);
        pacechange.setFocusable(false);
    }

    public void fetching(){
        if(!(db.getAttempts("").isEmpty())) {
            if(db.getAttempts("").get(db.getAttempts("").size()-1).getActivityType().equals("Running"))   {                 ///checking if latest activity is running

                myattempt = db.getAttempts("Running");

            }
            if(db.getAttempts("").get(db.getAttempts("").size()-1).getActivityType().equals("Hiking"))   {                 ///checking if latest activity is running

                myattempt = db.getAttempts("Hiking");

            }
            if(db.getAttempts("").get(db.getAttempts("").size()-1).getActivityType().equals("Biking"))   {                 ///checking if latest activity is running

                myattempt = db.getAttempts("Biking");

            }
            if (myattempt != null) {
                if (myattempt.size() >= 2) {
                    ///comparing attempt
                  Route recentattemptroute =db.getRoute(myattempt.get(myattempt.size()-1).getRouteName());
                    Route previousattemptroute =db.getRoute(myattempt.get(myattempt.size()-2).getRouteName());
                    if((recentattemptroute!=null)||(previousattemptroute!=null)) {
                        double recentdistances = recentattemptroute.getTotalDistance();
                        double previousdistances = previousattemptroute.getTotalDistance();
                        double distancechanges = recentdistances - previousdistances;
                        // distancechange.setText("" + ((myroute.get(myroute.size() - 1).getTotalDistance()) - (myroute.get(myroute.size() - 2).getTotalDistance())));
                        float recenttimes = (recentattemptroute.getBestTime()) / 60;
                        float previoustimes = (previousattemptroute.getBestTime()) / 60;
                        String timechanges = ((recentattemptroute.getBestTime()) - (previousattemptroute.getBestTime())) / 60 + " min";
                        String timechangez = (-1 * ((recentattemptroute.getBestTime()) - (previousattemptroute.getBestTime())) / 60) + " min";
                        double recentspeeds = ((recentattemptroute.getTotalDistance()) * (1000)) / ((recentattemptroute.getBestTime()));
                        double previousspeeds = ((previousattemptroute.getTotalDistance()) * (1000)) / ((previousattemptroute.getBestTime()));
                        double speedchanges = recentspeeds - previousspeeds;
                        double previouspaces = (previousattemptroute.getBestTime()) / ((60) * previousattemptroute.getTotalDistance());//min per km
                        double recentpaces = (recentattemptroute.getBestTime()) / ((60) * recentattemptroute.getTotalDistance());//min per km
                        double pacechanges = recentpaces - previouspaces;

                        title.setText("Comparing " + myattempt.get(myattempt.size() - 1).getRouteName() + " with " + myattempt.get(myattempt.size() - 2).getRouteName());
                        if (recentdistances > previousdistances) {
                            distancechange.setText("You covered " + distancechanges * 1000 + " m more than previous session");
                        } else if (recentdistances < previousdistances) {
                            distancechange.setText("You covered " + (-1 * distancechanges) * 1000 + " m less than previous session");
                        } else {
                            distancechange.setText("You covered same distance as previous session");
                        }

                        if (recenttimes > previoustimes) {
                            timechange.setText("You spend " + timechanges + " more than previous session");
                        } else if (recenttimes < previoustimes) {
                            timechange.setText("You spend " + timechangez + " less than previous session");
                        } else {
                            timechange.setText("You spend same time as previous session");
                        }

                        if (recentspeeds > previousspeeds) {
                            speedchange.setText("Your speed was " + speedchanges + " m/s more than previous session");
                        } else if (recentspeeds < previousspeeds) {
                            speedchange.setText("Your speed was " + (-1 * speedchanges) + " m/s less than previous session");
                        } else if ((previoustimes == 0) && (recenttimes == 0)) { ///checking for divide by zero exception
                            speedchange.setText("Speed comparison is invalid");

                        } else if ((previoustimes == 0)) { ///checking for divide by zero exception
                            speedchange.setText("Your speed was " + recentspeeds + " m/s more than previous session");

                        } else if ((recenttimes == 0)) { ///checking for divide by zero exception
                            speedchange.setText("Your speed was " + previousspeeds + " m/s less than previous session");

                        } else {
                            speedchange.setText("Your speed was  same as previous session");
                        }

                        if (recentpaces > previouspaces) {
                            pacechange.setText("Your pace was " + pacechanges + " min/km more than previous session");
                        } else if (recentpaces < previouspaces) {
                            pacechange.setText("Your pace was " + (-1 * pacechanges) + " min/km less than previous session");
                        } else if ((previousdistances == 0) && (recentdistances == 0)) { ///checking for divide by zero exception
                            pacechange.setText("pace comparison is invalid");

                        } else if ((previousdistances == 0)) { ///checking for divide by zero exception
                            pacechange.setText("Your pace was " + recentpaces + " min/km more than previous session");

                        } else if ((recentdistances == 0)) { ///checking for divide by zero exception
                            pacechange.setText("Your speed was " + previouspaces + " min/km less than previous session");

                        } else {
                            pacechange.setText("Your pace was  same as previous session");
                        }


                    }
                }


            }





        }









    }
}
