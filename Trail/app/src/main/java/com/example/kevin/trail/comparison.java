package com.example.kevin.trail;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class comparison extends AppCompatActivity {
    EditText    distancechange , timechange, speedchange , pacechange,title,currentdistance,currenttime,currentpace,currentspeed;
    double previouspaces;
    double recentspeeds;
    double previousspeeds;
    double recentpaces;
    DBHandler db = new DBHandler(this);
    ArrayList<Route> myattemptroute =new ArrayList<Route>();
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
  //      title = (EditText) findViewById(R.id.titles);
  //      title.setFocusable(false);
       // recentdistance = (EditText) findViewById(R.id.recentdistance);
       // recentdistance.setFocusable(false);
       // previousdistance = (EditText) findViewById(R.id.previousdistance);
       // previousdistance.setFocusable(false);
        currentdistance = (EditText) findViewById((R.id.currentdistance));
        currenttime = (EditText) findViewById(R.id.currenttime);
        currentpace = (EditText) findViewById(R.id.currentpace);
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
        currentspeed = (EditText)findViewById(R.id.currentspeed);
        currentspeed.setFocusable(false);
        currentdistance.setFocusable(false);
        currenttime.setFocusable(false);
        currentpace.setFocusable(false);
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
            if((db.getAttempts("").get(db.getAttempts("").size()-1).getActivityType().equals("Running") ))  {                 ///checking if latest activity is running

                myattempt = db.getAttempts("Running");


            }
            if((db.getAttempts("").get(db.getAttempts("").size()-1).getActivityType().equals("Hiking") ))  {                 ///checking if latest activity is running

                myattempt = db.getAttempts("Hiking");


            }




                if (myattempt != null) {
                if (myattempt.size() >= 2) {




                    ///comparing attempt
                  Attempt recentattempt =myattempt.get(myattempt.size()-1);
                    Attempt previousattempt =myattempt.get(myattempt.size()-2);
                    if((recentattempt!=null)||(previousattempt!=null)) {
                        double recentdistances = ((recentattempt.getTotalDistance()) * (1000));
                        double previousdistances =((previousattempt.getTotalDistance()) * (1000));
                        double distancechanges = recentdistances - previousdistances;
                        recentdistances=recentdistances*1000;           ////rounding off to 6 decimal places
                        recentdistances = Math.round(recentdistances);
                        recentdistances = recentdistances/1000;


                        distancechanges=distancechanges*1000;           ////rounding off to 6 decimal places
                        distancechanges = Math.round(distancechanges);
                        distancechanges = distancechanges/1000;
                        distancechanges=distancechanges*1000;           ////rounding off to 6 decimal places
                        distancechanges = Math.round(distancechanges);
                        distancechanges = distancechanges/1000;
                        // distancechange.setText("" + ((myroute.get(myroute.size() - 1).getTotalDistance()) - (myroute.get(myroute.size() - 2).getTotalDistance())));
                        float recenttimes = (recentattempt.getTotalTimeTaken())  ;
                        recenttimes=recenttimes*1000;           ////rounding off to 6 decimal places
                        recenttimes = Math.round(recenttimes);
                        recenttimes = recenttimes/1000;
                        float previoustimes = (previousattempt.getTotalTimeTaken()) ;
                        float timechanges = ((recentattempt.getTotalTimeTaken()) - (previousattempt.getTotalTimeTaken()))  ;
                        float timechangez = (-1 * ((recentattempt.getTotalTimeTaken()) - (previousattempt.getTotalTimeTaken())) );
                        if(recentattempt.getTotalTimeTaken()!=0){
                         recentspeeds = ((recentattempt.getTotalDistance()) * (1000)) / ((recentattempt.getTotalTimeTaken()));
                        }else{
                            recentspeeds = 0;
                        }
                        if(previousattempt.getTotalTimeTaken()!=0){
                         previousspeeds = ((previousattempt.getTotalDistance()) * (1000)) / ((previousattempt.getTotalTimeTaken()));
                        }else{
                            previousspeeds=0;
                        }
                        recentspeeds=recentspeeds*1000;           ////rounding off to 6 decimal places
                        recentspeeds = Math.round(recentspeeds);
                        recentspeeds = recentspeeds/1000;
                        double speedchanges = recentspeeds - previousspeeds;
                        speedchanges=speedchanges*1000;////rounding off to 6 decimal places
                        speedchanges = Math.round(speedchanges);
                        speedchanges = speedchanges/1000;////rounding off to 6 decimal places
                        if(previousattempt.getTotalDistance()!=0) {
                             previouspaces = (previousattempt.getTotalTimeTaken()) / ((60) * previousattempt.getTotalDistance());
                        }  else{
                              previouspaces = 0;
                        }                                                                                                         //min per km
                        if(recentattempt.getTotalDistance()!=0) {
                             recentpaces = (recentattempt.getTotalTimeTaken()) / ((60) * recentattempt.getTotalDistance());//min per km
                        }else{
                             recentpaces = 0;
                        }
                        double pacechanges = recentpaces - previouspaces;
                        pacechanges=pacechanges*100;      ////rounding off to 6 decimal places
                        pacechanges = Math.round(pacechanges);
                        pacechanges = pacechanges/100;////rounding off to 6 decimal places
                        recentpaces=recentpaces*1000;           ////rounding off to 6 decimal places
                        recentpaces = Math.round(recentpaces);
                        recentpaces = recentpaces/1000;

                     //   title.setText("Comparing at date :"+myattempt.get(myattempt.size() - 1).getDate().substring(0,4) +"-"+myattempt.get(myattempt.size() - 1).getDate().substring(4,6)+"-"+myattempt.get(myattempt.size() - 1).getDate().substring(6,8) );
                        if (distancechanges>0) {
                            distancechange.setText("You covered " +distancechanges  + " m more distance");
                        } else if (distancechanges<0) {
                            distancechange.setText("You covered " + -1* distancechanges + " m less distance");
                        } else {
                            distancechange.setText("You covered same distance");
                        }

                        if (recenttimes > previoustimes) {
                            timechange.setText("You spend " + (int)timechanges/60+" min "+ timechanges%60+" s more ");
                        } else if (recenttimes < previoustimes) {
                            timechange.setText("You spend "+ (int)timechanges/60+" min "+ timechanges%60+" s less");
                        } else {
                            timechange.setText("You spend same time as previous session");
                        }

                        if (recentspeeds > previousspeeds) {
                            speedchange.setText("Your speed was " + speedchanges + " m/s more ");
                        } else if (recentspeeds < previousspeeds) {
                            speedchange.setText("Your speed was " + (-1 * (speedchanges)) + " m/s less ");
                        } else if ((previoustimes == 0) || (recenttimes == 0)) { ///checking for divide by zero exception
                            speedchange.setText("Speed comparison is invalid");

                        }  else {
                            speedchange.setText("Your speed was  same as previous session");
                        }

                        if (recentpaces > previouspaces) {
                            pacechange.setText("Your pace was " + pacechanges + " min/km more ");
                        } else if (recentpaces < previouspaces) {
                            pacechange.setText("Your pace was " + (-1 * pacechanges) + " min/km less ");
                        } else if ((previousdistances == 0) || (recentdistances == 0)) { ///checking for divide by zero exception
                            pacechange.setText("pace comparison is invalid");

                        }else {
                            pacechange.setText("Your pace was  same as previous session");
                        }
                        currentdistance.setText("Distance :"+recentdistances+" m");
                        currenttime.setText("Time: "+(int)recenttimes/60+" min "+recenttimes%60 +" s");
                        currentpace.setText("Pace: "+recentpaces+" min/km");
                        currentspeed.setText("Speed: "+recentspeeds+" m/s");

                    }
                }


            }





        }









    }
}
