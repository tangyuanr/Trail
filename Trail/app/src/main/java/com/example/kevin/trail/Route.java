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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Andre & Jiayin
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
    private String snapshotURL;

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
//    public Route(long rowID, String routeName, String activityType, float totalDistance, int bestTime, String dateBestTime, String filename_coordinates) {
//        this.routeName = routeName;
//        this.activityType = activityType;
//        this.totalDistance = totalDistance;
//        this.bestTime = bestTime;
//        this.dateBestTime = dateBestTime;
//        this.filename_coordinates = filename_coordinates;
//        this.rowID = rowID;
//    }


    public String getRouteName() {
        return routeName;
    }

    public String getActivityType() {
        return activityType;
    }

    public float getTotalDistance() {
        return round(totalDistance,1);
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

    public String getSnapshotURL(){return snapshotURL;}

    //implementing toString so that a listview adapter can call it on a Route object. We can build the string here and return it to the listview.
    @Override
    public String toString() {
        //converting seconds to hours,minutes string. copied-pasted from http://stackoverflow.com/questions/6118922/convert-seconds-value-to-hours-minutes-seconds
        int hours = bestTime / 3600;
        int minutes = (bestTime % 3600) / 60;
        String bestTimeString = String.format("%02dh %02dm", hours, minutes);
        Log.d("Best time string: ", bestTimeString);
        if(activityType.equals("Running")) {
            return routeName + "\n Best time:\n" + bestTimeString + "\n on " + formatDate(dateBestTime) + "\n Distance: " + String.format("%.1f", totalDistance) + " km";
        }
        else if(activityType.equals("Hiking")) {
            return routeName + "\n Best time:\n" + bestTimeString + "\n on " + formatDate(dateBestTime) +"\n Distance: " + String.format("%.1f", totalDistance) + " km";
        }
        else if(activityType.equals("Biking")) {
            return routeName + "\n Best time:\n" + bestTimeString + "\n on " + formatDate(dateBestTime) + "\n Distance: " + String.format("%.1f", totalDistance) + " km";
        }
        else {return null;}
    }

    public String spinnerString() {
        return "test";
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_DOWN);
        return bd.floatValue();
    }

    private static String formatDate(String YYYYMMDD) {
        DateFormat inputformat = new SimpleDateFormat("yyyyMMdd");
        Date inputdate = null;
        try {
            inputdate = inputformat.parse(YYYYMMDD);
        } catch(ParseException e) {}
        DateFormat outputformat = new SimpleDateFormat("yyyy/MM/dd");
        String output = outputformat.format(inputdate);
        return output;
    }

    public ArrayList<Location> buildLocationArray() {
        ArrayList<Location> arrayLocation = new ArrayList<>();
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
                    Location location = new Location("");
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
        for (int i = 0; i < locationArrayList.size(); i++) {
            double latitude = locationArrayList.get(i).getLatitude();
            double longitude = locationArrayList.get(i).getLongitude();
            url += latitude + "," + longitude + "|";
        }
        url = url.substring(0, url.length() - 1);
        url += "&sensor=false";
        Log.d("getStaticAPIURL:",url);
        this.snapshotURL=url;
        return url;
        // this URL can get quite large so we need to look into ways to reduce it. For the moment I am taking every other element to half the number of points.
        // downloading the image and storing its filename in the SQLite database is probably better.
    }

}
