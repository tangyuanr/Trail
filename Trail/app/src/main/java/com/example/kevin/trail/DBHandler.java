package com.example.kevin.trail;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Andre & Jiayin
 * Logs GPS coordinates and writes them to a file
 * This runs as a service and can be called from any activity.
 */


public class DBHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION=12;
    private static final String DATABASE_NAME="healthData";
    private boolean isEmpty=true;



    long id=0;
    private final String TAG="DBHandler";

    public DBHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // runner table content
    private static final String TABLE_ROUTES="ROUTES";
    private static final String TABLE_ATTEMPTS="Attempts";
    private static final String ROUTE_ID="RouteID";
    private static final String ATTEMPT_ID="AttemptID";
    private static final String ACTIVITY_TYPE="ActivityType";
    private static final String ROUTE_DISTANCE="RouteDistance"; //in km
    private static final String ROUTE_NAME="RouteName";
    private static final String BEST_TIME="BestTime"; //in seconds
    private static final String DATE_OF_BEST_TIME = "DateOfBestTime"; //YYYYMMDD
    private static final String FILENAME_COORDINATES = "FileNameCoordinates";
    private static final String TOTAL_TIME = "TotalTime"; //in seconds
    private static final String DATE_OF_ATTEMPT = "DateOfAttempt"; //yyyyMMdd_HHmm
    private static final String MAP_SCREENSHOT = "LinkToMapScreenshot";
    private static final String AVG_HR="AverageHeartRate";//in BMP
    private static final String CALORIES="CaloriesBurnt";//in KCal
    private static final String TOTAL_DISTANCE="TotalDistance";//in km
    private static final String IMAGEFILENAME="ImageFileName";//ending in .jpg

    @Override
    public void onCreate(SQLiteDatabase db){

        String CREATE_ROUTE_TABLE="CREATE TABLE "+TABLE_ROUTES + " ("+ROUTE_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+ROUTE_NAME+" TEXT, "
                + ACTIVITY_TYPE + " TEXT, " + ROUTE_DISTANCE + " REAL, " + BEST_TIME + " INT, " + DATE_OF_BEST_TIME + " TEXT, " + FILENAME_COORDINATES + " TEXT)";
        Log.e(TAG, CREATE_ROUTE_TABLE);
        db.execSQL(CREATE_ROUTE_TABLE);
        String CREATE_ATTEMPTS_TABLE="CREATE TABLE "+TABLE_ATTEMPTS+" ("+ATTEMPT_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+ACTIVITY_TYPE+" TEXT, "+TOTAL_DISTANCE+" TEXT, "+TOTAL_TIME + " INT, "
                + DATE_OF_ATTEMPT + " TEXT, " + ROUTE_NAME + " TEXT, " +AVG_HR +" INT, " + CALORIES + " INT, "+ IMAGEFILENAME+" TEXT, "+MAP_SCREENSHOT + " TEXT)";
        Log.e(TAG, CREATE_ATTEMPTS_TABLE);
        db.execSQL(CREATE_ATTEMPTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_ROUTES);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_ATTEMPTS);
        onCreate(db);
    }


    //builds an arraylist of Route objects and returns it
    public ArrayList<Route> getRoutes(String activity) { //
        ArrayList<Route> routesList = new ArrayList<>();
        String query;
        if(activity.equals("")) {    //getRoutes("") will select all the routes
            query = "SELECT * FROM " + TABLE_ROUTES;
            Log.d(TAG, query);
        }
        else {  //ex: when we call getRoutes("Running") to only display a specific type;
            query = "SELECT * FROM " + TABLE_ROUTES + " WHERE " + ACTIVITY_TYPE + " IN ('" + activity + "')";
            Log.d(TAG, query);
        }
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor routesCursor = db.rawQuery(query, null);
        try {
            while (routesCursor.moveToNext()) {
                String nameOfRoute = routesCursor.getString(1);
                String activityType = routesCursor.getString(2);
                float totalDistance = routesCursor.getFloat(3);
                long bestTime = routesCursor.getLong(4);
                String dateBestTime = routesCursor.getString(5);
                String filename_coordinates = routesCursor.getString(6);
                Route route = new Route(nameOfRoute, activityType, totalDistance, bestTime, dateBestTime, filename_coordinates);
                routesList.add(route);
            }
        } finally {
            routesCursor.close();
        }
        db.close();
        return routesList;
    }


    //builds an arraylist of attempts and return it
    public ArrayList<Attempt> getAttempts(String activity) { //
        ArrayList<Attempt> attemptsList = new ArrayList<>();
        String query;
        if(activity.equals("")) {    //getAttempts("") will select all the routes
            query = "SELECT * FROM " + TABLE_ATTEMPTS;
            Log.d(TAG, query);
        }
        else {  //ex: when we call getRoutes("Running") to only display a specific type;
            query = "SELECT * FROM " + TABLE_ATTEMPTS + " WHERE " + ACTIVITY_TYPE + " IN ('" + activity + "')";
            Log.d(TAG, query);
        }
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor attemptsCursor = db.rawQuery(query, null);
        try {
            while (attemptsCursor.moveToNext()) {
                String activityType = attemptsCursor.getString(1);
                int totalTime = attemptsCursor.getInt(attemptsCursor.getColumnIndex(TOTAL_TIME));
                String date = attemptsCursor.getString(attemptsCursor.getColumnIndex(DATE_OF_ATTEMPT)); // yyyyMMdd_HHmm
                String routeName = attemptsCursor.getString(attemptsCursor.getColumnIndex(ROUTE_NAME));
                String snapshotURL = attemptsCursor.getString(attemptsCursor.getColumnIndex(MAP_SCREENSHOT));
                String distance=attemptsCursor.getString(attemptsCursor.getColumnIndex(TOTAL_DISTANCE));
                String avgHR=attemptsCursor.getString(attemptsCursor.getColumnIndex(AVG_HR));
                String calories=attemptsCursor.getString(attemptsCursor.getColumnIndex(CALORIES));
                String imageDir=attemptsCursor.getString(attemptsCursor.getColumnIndex(IMAGEFILENAME));
                Route route = getRoute(routeName);
                Attempt attempt = new Attempt(route, totalTime, Float.parseFloat(distance), date, snapshotURL, Integer.parseInt(avgHR), Integer.parseInt(calories), imageDir);
                attemptsList.add(attempt);
            }
        } finally {
            attemptsCursor.close();
        }
        db.close();
        return attemptsList;
    }

    public ArrayList<Attempt> getAttemptsFromRouteName(String routename) {
        ArrayList<Attempt> attemptsList = new ArrayList<>();
        String query;
        query = "SELECT * FROM " + TABLE_ATTEMPTS + " WHERE " + ROUTE_NAME + " IN ('" + routename + "')";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor attemptsCursor = db.rawQuery(query, null);
        try {
            while (attemptsCursor.moveToNext()) {
                String activityType = attemptsCursor.getString(1);
                int totalTime = attemptsCursor.getInt(attemptsCursor.getColumnIndex(TOTAL_TIME));
                String date = attemptsCursor.getString(attemptsCursor.getColumnIndex(DATE_OF_ATTEMPT)); // yyyyMMdd_HHmm
                String routeName = attemptsCursor.getString(attemptsCursor.getColumnIndex(ROUTE_NAME));
                String snapshotURL = attemptsCursor.getString(attemptsCursor.getColumnIndex(MAP_SCREENSHOT));
                String distance=attemptsCursor.getString(attemptsCursor.getColumnIndex(TOTAL_DISTANCE));
                String avgHR=attemptsCursor.getString(attemptsCursor.getColumnIndex(AVG_HR));
                String calories=attemptsCursor.getString(attemptsCursor.getColumnIndex(CALORIES));
                String imageDir=attemptsCursor.getString(attemptsCursor.getColumnIndex(IMAGEFILENAME));
                Route route = getRoute(routeName);
                Attempt attempt = new Attempt(route, totalTime, Float.parseFloat(distance), date, snapshotURL, Integer.parseInt(avgHR), Integer.parseInt(calories), imageDir);
                attemptsList.add(attempt);
            }
        } finally {
            attemptsCursor.close();
        }
        db.close();
        return attemptsList;

    }

    //overload getAttempts. fetches all attempts whose date begin with date YYMMDD
    public ArrayList<Attempt> getAttemptsByDate(String date, String activity) { //
        ArrayList<Attempt> attemptsList = new ArrayList<>();
        String query;
        if(activity.equals("")) {    //getAttempts("") will select all the routes
            query = "SELECT * FROM " + TABLE_ATTEMPTS + " WHERE " + DATE_OF_ATTEMPT + " LIKE '" + date + "%'";
            Log.d(TAG, query);
        }
        else {  //ex: when we call getRoutes("Running") to only display a specific type;
            query = "SELECT * FROM " + TABLE_ATTEMPTS + " WHERE " + ACTIVITY_TYPE + " IN ('" + activity + "')";
            Log.d(TAG, query);
        }
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor attemptsCursor = db.rawQuery(query, null);
        try {
            while (attemptsCursor.moveToNext()) {
                String activityType = attemptsCursor.getString(1);
                int totalTime = attemptsCursor.getInt(attemptsCursor.getColumnIndex(TOTAL_TIME));
                String dateAttempt = attemptsCursor.getString(attemptsCursor.getColumnIndex(DATE_OF_ATTEMPT)); // yyyyMMdd_HHmm
                String routeName = attemptsCursor.getString(attemptsCursor.getColumnIndex(ROUTE_NAME));
                String snapshotURL = attemptsCursor.getString(attemptsCursor.getColumnIndex(MAP_SCREENSHOT));
                float distance=attemptsCursor.getFloat(attemptsCursor.getColumnIndex(TOTAL_DISTANCE));
                String avgHR=attemptsCursor.getString(attemptsCursor.getColumnIndex(AVG_HR));
                String calories=attemptsCursor.getString(attemptsCursor.getColumnIndex(CALORIES));
                String imageDir=attemptsCursor.getString(attemptsCursor.getColumnIndex(IMAGEFILENAME));
                Route route = getRoute(routeName);
                Attempt attempt = new Attempt(route, totalTime, distance, dateAttempt, snapshotURL, Integer.parseInt(avgHR), Integer.parseInt(calories), imageDir);
                attemptsList.add(attempt);
            }
        } finally {
            attemptsCursor.close();
        }
        db.close();
        return attemptsList;
    }







    //add new Route to Route table
    public void addRoute(Route route) {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(ROUTE_NAME, route.getRouteName());//filename of .txt file containing coordinates
        contentValues.put(ACTIVITY_TYPE, route.getActivityType());
        contentValues.put(ROUTE_DISTANCE, route.getTotalDistance());
        contentValues.put(BEST_TIME, route.getBestTime());
        contentValues.put(DATE_OF_BEST_TIME, route.getDateBestTime());
        contentValues.put(FILENAME_COORDINATES, route.getFilename_coordinates());
        db.insert(TABLE_ROUTES, null, contentValues);  //keeping track of the addedID so that we can call addAttempt for the correct RouteID
        db.close();
    }

    public void deleteRoute(String routeName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ROUTES, ROUTE_NAME +  "='" + routeName + "'", null);
        db.delete(TABLE_ATTEMPTS, ROUTE_NAME +  "='" + routeName + "'", null);
        db.close();
    }

    //fetch one route
    public Route getRoute(String routeName) {
        Route route = null;
        String query = "SELECT * FROM " + TABLE_ROUTES +" WHERE " + ROUTE_NAME + " ='" + routeName +"'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor routeCursor = db.rawQuery(query, null);
        try {
            while (routeCursor.moveToNext()) {
                String nameOfRoute = routeCursor.getString(1);
                String activityType = routeCursor.getString(2);
                float totalDistance = routeCursor.getFloat(3);
                int bestTime = routeCursor.getInt(4);
                String dateBestTime = routeCursor.getString(5);
                String filename_coordinates = routeCursor.getString(6);
                route = new Route(nameOfRoute, activityType, totalDistance, bestTime, dateBestTime, filename_coordinates);
            }
        } finally {
            routeCursor.close();
        }
        db.close();
        return route;

    }

    //adding an attempt to the attempt table
    public void addAttempt(Attempt attempt) {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(ACTIVITY_TYPE, attempt.getRoute().getActivityType());
        contentValues.put(TOTAL_TIME, attempt.getTotalTimeTaken());
        contentValues.put(DATE_OF_ATTEMPT, attempt.getDateOfAttemptAsString());
        contentValues.put(MAP_SCREENSHOT,attempt.getFileNameStaticMapScreenshot());
        contentValues.put(ROUTE_NAME, attempt.getRoute().getRouteName());
        contentValues.put(AVG_HR, attempt.getAverageHeartRate());
        contentValues.put(CALORIES, attempt.getCaloriesBurnt());
        contentValues.put(TOTAL_DISTANCE, attempt.getTotalDistance());
        contentValues.put(IMAGEFILENAME, attempt.getImagefilename());
        db.insert(TABLE_ATTEMPTS, null, contentValues);
        db.close();
        compareBestTime(attempt);
    }

    public DateTime getStartingDate() {
        String query = "SELECT * FROM " + TABLE_ATTEMPTS + " ORDER BY ROWID ASC LIMIT 1"; //select first row
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor attemptCursor = db.rawQuery(query, null);
        attemptCursor.moveToNext();
        String date = attemptCursor.getString(attemptCursor.getColumnIndex(DATE_OF_ATTEMPT)); // yyyyMMdd_HHmm
        attemptCursor.close();
        db.close();
        return convertStringToDateTime(date);
    }

    public DateTime convertStringToDateTime(String date) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd_HHmm");
        return formatter.parseDateTime(date);
    }

    public ArrayList<Attempt> getAttempts(DateTime date1, DateTime date2, String activityType) {
        ArrayList<Attempt> attemptsList = getAttempts(activityType);
        ArrayList<Attempt> finalList = new ArrayList<Attempt>();

        if(activityType.equals("")) {
            for (int i = 0; i < attemptsList.size(); i++) {
                DateTime dateOfAttempt = convertStringToDateTime(attemptsList.get(i).getDateOfAttemptAsString());
                if(dateOfAttempt.isAfter(date1.minusMinutes(10)) && dateOfAttempt.isBefore(date2.plusMinutes(10))) {
                    finalList.add(attemptsList.get(i));
                }
            }
        }
        return finalList;
    }

    //compares the current record best time for the given route and if the one of the current attempt is better, update the best time of the route table
    private void compareBestTime(Attempt attempt) {
        int totalTime = attempt.getTotalTimeTaken(); //in seconds
        String routeName = attempt.getRoute().getRouteName();
        String query = "SELECT * FROM " + TABLE_ROUTES +" WHERE " + ROUTE_NAME + " ='" + routeName +"'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getInt(4) >= totalTime) {
                query = "UPDATE " + TABLE_ROUTES + " SET " + BEST_TIME + "=" + totalTime + " WHERE " + ROUTE_NAME + " ='" + routeName +"'";
                db.execSQL(query);
                query = "UPDATE " + TABLE_ROUTES + " SET " + DATE_OF_BEST_TIME + "='" + attempt.getDateOfAttemptAsString() + "' WHERE " + ROUTE_NAME + " ='" + routeName +"'";
                Log.d(TAG, "Best time updated");
            }
        }
        cursor.close();
        db.close();
    }

    // this checks if the Route table is empty. I
    public boolean isRouteTableEmpty(String activityType){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " +TABLE_ROUTES + " WHERE " + ACTIVITY_TYPE + " ='" + activityType + "'", null);
        if(cursor != null){
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            if(count > 0){
                return false;
            }
            cursor.close();
        }
        return true;
    }

    //overload for all activity types
    public boolean isRouteTableEmpty(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " +TABLE_ROUTES, null);
        if(cursor != null){
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            if(count > 0){
                return false;
            }
            cursor.close();
        }
        return true;
    }

    public boolean doesRouteNameExist(String routeName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_ROUTES + " WHERE " + ROUTE_NAME + " ='" + routeName + "'";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return true; //yes, it exists
        } else {
            return false; //no, it does not exist
        }
    }

    //get general info of all attempts from ATTEMPTS table
    //data will be read in descending order, so that the display order goes from most recent to oldest
    public HashMap<String, List<String>> getContent(){
        HashMap<String, List<String>> expandList = new HashMap<String, List<String>>();
        String placeholder;

        SQLiteDatabase db=this.getReadableDatabase();
        String selectQuery = "SELECT * FROM "+TABLE_ATTEMPTS;
        Log.e(TAG, selectQuery);

        Cursor cursor=db.rawQuery(selectQuery,null);

        //read data in reverse order
        if (cursor.moveToLast()) {
            while (true) {
                List<String> dataHolder = new ArrayList<String>();

                placeholder = cursor.getString(cursor.getColumnIndex(ACTIVITY_TYPE));
                Log.d(TAG, "read ACTIVITY TYPE from TABLE ATTEMPTS: " + placeholder);
                dataHolder.add("ACTIVITY: " + placeholder);

                placeholder = cursor.getString(cursor.getColumnIndex(ROUTE_NAME));
                Log.d(TAG, "read ROUTE NAME from TABLE ATTEMPTS: " + placeholder);
                dataHolder.add("ROUTE:      " + placeholder);

                placeholder = cursor.getString(cursor.getColumnIndex(TOTAL_TIME));
                Log.d(TAG, "read TOTAL TIME from TABLE ATTEMPTS: " + timeReformat(placeholder));
                dataHolder.add("TOTAL TIME: " + timeReformat(placeholder));

                placeholder = cursor.getString(cursor.getColumnIndex(TOTAL_DISTANCE));
                Log.d(TAG, "read TOTAL DISTANCE from TABLE ATTEMPTS: " + placeholder);
                dataHolder.add("TOTAL DISTANCE: " + placeholder+"KM");

                placeholder = cursor.getString(cursor.getColumnIndex(AVG_HR));
                Log.d(TAG, "read AVERAGE HEART RATE from TABLE ATTEMPTS: " + placeholder);
                dataHolder.add("AVERAGE HEART RAGE:      " + placeholder + "BMP");

                placeholder = cursor.getString(cursor.getColumnIndex(CALORIES));
                Log.d(TAG, "read CALORIES BURNT from TABLE ATTEMPTS: " + placeholder);
                dataHolder.add("CALORIES BURNT:      " + placeholder+"KCAL");

                dataHolder.add("See map snapshot");//click on this item to see the snapshot of map

                //when adding row data is finished, name hashmap key with date
                placeholder = cursor.getString(cursor.getColumnIndex(DATE_OF_ATTEMPT));
                Log.d(TAG, "read DATE OF ATTEMPT from TABLE ATTEMPTS");
                expandList.put("DATE: "+placeholder, dataHolder);

                if (cursor.isFirst()) {
                    cursor.close();
                    db.close();
                    break;
                }

                cursor.moveToPrevious();
            }
        }


        return expandList;
    }

    private String timeReformat(String timeInSecond){
        int time=Integer.parseInt(timeInSecond);
        int seconds = time%60;
        int minutes = time/60;
        minutes=minutes%60;
        int hours=time/3600;

        String formatted=hours+" Hour "+minutes+" Min "+seconds+" Sec";
        return formatted;
    }

    public String getImageFileName(String savedDate){
        SQLiteDatabase db=this.getReadableDatabase();
        String query="SELECT * FROM "+TABLE_ATTEMPTS + " WHERE "+DATE_OF_ATTEMPT+" = '"+savedDate+"'";
        Cursor cursor=db.rawQuery(query, null);
        if (cursor.moveToFirst())
            return cursor.getString(cursor.getColumnIndex(IMAGEFILENAME));
        else
            return null;
    }

    public String getFilenameCoordinates(String savedDate){
        SQLiteDatabase db=this.getReadableDatabase();
        String query="SELECT * FROM "+TABLE_ATTEMPTS + " WHERE "+DATE_OF_ATTEMPT+" = '"+savedDate+"'";
        Cursor cursor=db.rawQuery(query, null);

        String routeName;
        if (cursor.moveToFirst())
            routeName= cursor.getString(cursor.getColumnIndex(ROUTE_NAME));
        else
            return null;

        query="SELECT * FROM " + TABLE_ROUTES+" WHERE "+ROUTE_NAME+" = '"+routeName+"'";
        cursor=db.rawQuery(query, null);

        if (cursor.moveToFirst())
            return cursor.getString(cursor.getColumnIndex(FILENAME_COORDINATES));
        else
            return null;
    }

}
