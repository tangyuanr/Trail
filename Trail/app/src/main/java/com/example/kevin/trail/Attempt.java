package com.example.kevin.trail;

/**
 * Created by Ezekiel.
 * Attempt object
 * It has:
 * a Route member:  I have made the decision to include the Route as a member of Attempt so that we can easily know which Route does an attempt
 *                  belong to without always having to write extra code to fetch the Route from the database.
 *                  it makes the code easier to debug and protects from careless coding.
 * int totalTimeTaken: the total time of the attempt in seconds
 * String date of attempt: the date, YYMMDD format
 * String fileNameStaticMapScreenshot: this is empty and not used for the moment but I am making provision for it just in case
 */


public class Attempt {

    private Route route; //the route object it is associated with. i am enforcing this for clarity
    private int totalTimeTaken; //in seconds
    private String dateOfAttempt; //YYMMDD
    private String fileNameStaticMapScreenshot;
    String Routename;
    String Activitytype;

    //a route must be provided to the constructor (i.e. no attempt without an associated route)
    public Attempt(Route route, int totalTimeTaken, String dateOfAttempt, String URL) {
        this.route = route;
        this.totalTimeTaken = totalTimeTaken;
        this.dateOfAttempt = dateOfAttempt;
        this.fileNameStaticMapScreenshot=URL;
    }///Ateempts for data table
    public Attempt(String activitytype,int totaltime,String dateofattempt,String Routename, String mapscreenshot){
        this.Activitytype = activitytype;
        this.totalTimeTaken = totaltime;
        this.dateOfAttempt = dateofattempt;
        this.Routename = Routename;
        this.fileNameStaticMapScreenshot = mapscreenshot;

    }

    public String getActivityType() {
        return Activitytype;
    }
    public String getRouteName(){return Routename;  }

    public String getFileNameStaticMapScreenshot() {
        return fileNameStaticMapScreenshot;
    }

    public Route getRoute() {
        return route;
    }


    public int getTotalTimeTaken() {
        return totalTimeTaken;
    }


    public String getDateOfAttempt() {
        return dateOfAttempt;
    }


}
