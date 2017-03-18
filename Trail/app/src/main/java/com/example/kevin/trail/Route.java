package com.example.kevin.trail;


import android.content.Context;
import android.location.Location;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Ezekiel.
 * Route object.
 * It has:
 * String routeName
 * String activityType: "Running", "Biking", etc.
 * float totalDistance: the distance of the route in KM
 * bestTime and dateBestTime: the best time recorded for that specific route, and the date at which it occured
 * String filename_coordinates: the name of the coordinates file
 * long rowID: the ID of its row in the ROUTE table
 *
 * It has 2 constructors:
 * one for when the rowID is not known (for example, when the user selects new route and we dont know the ID until a request to add it to the DB is made
 * one for when it is an existing route with a known rowID.
 */

public class Route implements Serializable { //needed to be able to pass it between android activities.
    private String routeName;
    private String activityType;
    private float totalDistance; //in KM
    private int bestTime; //in seconds
    private String dateBestTime; //YYMMDD
    private String filename_coordinates;
    private long rowID = 0;

    //constructor called when row ID is not known yet. typically before adding it to the database.
    public Route(String routeName, String activityType, float totalDistance, int bestTime, String dateBestTime, String filename_coordinates) {
        this.routeName = routeName;
        this.activityType = activityType;
        this.totalDistance = totalDistance;
        this.bestTime = bestTime;
        this.dateBestTime = dateBestTime;
        this.filename_coordinates = filename_coordinates;
    }

    //constructor called when rowID is known. typically when it already exists in the database and user has selected it.
    public Route(long rowID, String routeName, String activityType, float totalDistance, int bestTime, String dateBestTime, String filename_coordinates) {
        this.routeName = routeName;
        this.activityType = activityType;
        this.totalDistance = totalDistance;
        this.bestTime = bestTime;
        this.dateBestTime = dateBestTime;
        this.filename_coordinates = filename_coordinates;
        this.rowID = rowID;
    }

    public long getRowID() {
        return this.rowID;
    }

    public void setRowID(long rowID) {
        this.rowID = rowID;
    }

    public String getRouteName() {
        return routeName;
    }

    public String getActivityType() {
        return activityType;
    }

    public float getTotalDistance() {
        return totalDistance;
    }

    public int getBestTime() {
        return bestTime;
    }

    public String getDateBestTime() {
        return dateBestTime;
    }

    public String getFilename_coordinates() {
        return filename_coordinates;
    }

    //implementing toString so that a listview adapter can call it on a Route object. We can build the string here and return it to the listview.
    //For example, we can implement some DBHandler function to figure out what the best time is for a specific route, and concatenate it to the string being returned there.
    @Override
    public String toString() {
        return routeName + "\n Best time:"; //we can build up the returned string there.
    }

    private ArrayList<Location> buildLocationArray() {
        ArrayList<Location> arrayLocation = new ArrayList<>();
        Location location = new Location("");
        FileInputStream is;
        BufferedReader reader;
        String path = Trail.getAppContext().getFilesDir() + "/" + filename_coordinates;
        final File file = new File(path);
        if (file.exists()) {
            try {
                is = new FileInputStream(file);
                reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();
                while (line != null) {
                    List<String> coordinatesList = Arrays.asList(line.split(","));
                    location.setLatitude(Double.parseDouble(coordinatesList.get(0)));
                    location.setLongitude(Double.parseDouble(coordinatesList.get(1)));
                    arrayLocation.add(location);
                    line = reader.readLine();
                }
            } catch (FileNotFoundException e) {
                Log.d("Route","filenoexist");

            } catch (IOException e) {
                Log.d("Route","ioexception");
            }

        }
        return arrayLocation;
    }

    public String getStaticAPIURL(Context context, int widthDP, int heightDP) {

        //These are to figure out what the resolution of the requested image should be.
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        float density = displayMetrics.density;

        //widthDP*density and heightDP*density are used to calculate the image dimensions in pixels that need to be requested
        ArrayList<Location> locationArrayList = buildLocationArray();
        String url = "http://maps.googleapis.com/maps/api/staticmap?size=" + (int) (widthDP * density) + "x" + (int) (heightDP * density) + "&path=";
        for (int i = 0; i < locationArrayList.size(); i += 2) { //every other element
            double latitude = locationArrayList.get(i).getLatitude();
            double longitude = locationArrayList.get(i).getLongitude();
            url += latitude + "," + longitude + "|";
        }
        url = url.substring(0, url.length() - 1);
        url += "&sensor=false";
        return url;
        // this URL can get quite large so we need to look into ways to reduce it. For the moment I am taking every other element to half the number of points.
        // downloading the image and storing its filename in the SQLite database is probably better.
    }

}
