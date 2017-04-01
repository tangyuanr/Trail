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
                  Attempt recentattempt =myattempt.get(myattempt.size()-1);
                    Attempt previousattempt =myattempt.get(myattempt.size()-2);
                    if((recentattempt!=null)||(previousattempt!=null)) {
                        double recentdistances = recentattempt.getTotalDistance();
                        double previousdistances = previousattempt.getTotalDistance();
                        double distancechanges = recentdistances - previousdistances;
                        distancechanges=distancechanges*100000;           ////rounding off to 6 decimal places
                        distancechanges = Math.round(distancechanges);
                        distancechanges = distancechanges/1000000;
                        // distancechange.setText("" + ((myroute.get(myroute.size() - 1).getTotalDistance()) - (myroute.get(myroute.size() - 2).getTotalDistance())));
                        float recenttimes = (recentattempt.getTotalTimeTaken()) / 60;
                        float previoustimes = (previousattempt.getTotalTimeTaken()) / 60;
                        String timechanges = ((recentattempt.getTotalTimeTaken()) - (previousattempt.getTotalTimeTaken())) / 60 + " min";
                        String timechangez = (-1 * ((recentattempt.getTotalTimeTaken()) - (previousattempt.getTotalTimeTaken())) / 60) + " min";
                        double recentspeeds = ((recentattempt.getTotalDistance()) * (1000)) / ((recentattempt.getTotalTimeTaken()));
                        double previousspeeds = ((previousattempt.getTotalDistance()) * (1000)) / ((previousattempt.getTotalTimeTaken()));
                        double speedchanges = recentspeeds - previousspeeds;
                        speedchanges=speedchanges*100000;////rounding off to 6 decimal places
                        speedchanges = Math.round(speedchanges);
                        speedchanges = speedchanges/1000000;////rounding off to 6 decimal places
                        double previouspaces = (previousattempt.getTotalTimeTaken()) / ((60) * previousattempt.getTotalDistance());//min per km
                        double recentpaces = (recentattempt.getTotalTimeTaken()) / ((60) * recentattempt.getTotalDistance());//min per km
                        double pacechanges = recentpaces - previouspaces;
                        pacechanges=pacechanges*100000;      ////rounding off to 6 decimal places
                        pacechanges = Math.round(pacechanges);
                        pacechanges = pacechanges/1000000;////rounding off to 6 decimal places

                        title.setText("Comparing " + myattempt.get(myattempt.size() - 1).getRouteName() + " at date :"+myattempt.get(myattempt.size() - 1).getDate().substring(0,4) +"-"+myattempt.get(myattempt.size() - 1).getDate().substring(4,6)+"-"+myattempt.get(myattempt.size() - 1).getDate().substring(6,8) );
                        if (recentdistances > previousdistances) {
                            distancechange.setText("You covered " +distancechanges  + " m more distance");
                        } else if (recentdistances < previousdistances) {
                            distancechange.setText("You covered " +  distancechanges + " m less distance");
                        } else {
                            distancechange.setText("You covered same distance");
                        }

                        if (recenttimes > previoustimes) {
                            timechange.setText("You spend " + timechanges + " more ");
                        } else if (recenttimes < previoustimes) {
                            timechange.setText("You spend " + timechangez + " more");
                        } else {
                            timechange.setText("You spend same time as previous session");
                        }

                        if (recentspeeds > previousspeeds) {
                            speedchange.setText("Your speed was " + speedchanges + " m/s more ");
                        } else if (recentspeeds < previousspeeds) {
                            speedchange.setText("Your speed was " + (-1 * (speedchanges)) + " m/s less ");
                        } else if ((previoustimes == 0) && (recenttimes == 0)) { ///checking for divide by zero exception
                            speedchange.setText("Speed comparison is invalid");

                        } else if ((previoustimes == 0)) { ///checking for divide by zero exception
                            speedchange.setText("Your speed was " + recentspeeds + " m/s more ");

                        } else if ((recenttimes == 0)) { ///checking for divide by zero exception
                            speedchange.setText("Your speed was " + previousspeeds + " m/s less ");

                        } else {
                            speedchange.setText("Your speed was  same as previous session");
                        }

                        if (recentpaces > previouspaces) {
                            pacechange.setText("Your pace was " + pacechanges + " min/km more ");
                        } else if (recentpaces < previouspaces) {
                            pacechange.setText("Your pace was " + (-1 * pacechanges) + " min/km less ");
                        } else if ((previousdistances == 0) && (recentdistances == 0)) { ///checking for divide by zero exception
                            pacechange.setText("pace comparison is invalid");

                        } else if ((previousdistances == 0)) { ///checking for divide by zero exception
                            pacechange.setText("Your pace was " + recentpaces + " min/km more ");

                        } else if ((recentdistances == 0)) { ///checking for divide by zero exception
                            pacechange.setText("Your speed was " + previouspaces + " min/km less ");

                        } else {
                            pacechange.setText("Your pace was  same as previous session");
                        }


                    }
                }


            }





        }









    }
}
