package com.example.kevin.trail;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by JY on 2017-03-11.
 */

public class DBHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION=6;
    private static final String DATABASE_NAME="healthData";



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
    private static final String DATE_OF_ATTEMPT = "DateOfAttempt"; //in seconds
    private static final String MAP_SCREENSHOT = "LinkToMapScreenshot";

    @Override
    public void onCreate(SQLiteDatabase db){

        String CREATE_ROUTE_TABLE="CREATE TABLE "+TABLE_ROUTES + " ("+ROUTE_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+ROUTE_NAME+" TEXT, "
                + ACTIVITY_TYPE + " TEXT, " + ROUTE_DISTANCE + " REAL, " + BEST_TIME + " INT, " + DATE_OF_BEST_TIME + " TEXT, " + FILENAME_COORDINATES + " TEXT)";
        Log.e(TAG, CREATE_ROUTE_TABLE);
        db.execSQL(CREATE_ROUTE_TABLE);
        String CREATE_ATTEMPTS_TABLE="CREATE TABLE "+TABLE_ATTEMPTS+" ("+ATTEMPT_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+ROUTE_ID+" INT, "+TOTAL_TIME + " INT, "
                + DATE_OF_ATTEMPT + " TEXT, " + MAP_SCREENSHOT + " TEXT)";
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
        if(activity == "") {    //getRoutes("") will select all the routes
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
                int bestTime = routesCursor.getInt(4);
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

    //add new Route to Route table
    public long addRoute(Route route) {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(ROUTE_NAME, route.getRouteName());//filename of .txt file containing coordinates
        contentValues.put(ACTIVITY_TYPE, route.getActivityType());
        contentValues.put(ROUTE_DISTANCE, route.getTotalDistance());
        contentValues.put(BEST_TIME, route.getBestTime());
        contentValues.put(DATE_OF_BEST_TIME, route.getDateBestTime());
        contentValues.put(FILENAME_COORDINATES, route.getFilename_coordinates());
        long addedID = db.insert(TABLE_ROUTES, null, contentValues);  //keeping track of the addedID so that we can call addAttempt for the correct RouteID
        db.close();
        return addedID;
    }

    //fetch one route
    public Route getRoute(long rowid) {
        Route route = null;
        String query = "SELECT * FROM " + TABLE_ROUTES +" WHERE  rowid = " + rowid;
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
                route = new Route(rowid, nameOfRoute, activityType, totalDistance, bestTime, dateBestTime, filename_coordinates);
            }
        } finally {
            routeCursor.close();
        }
        db.close();
        return route;

    }

    //adding an attempt to the attempt table
    public long addAttempt(Attempt attempt) {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(ROUTE_ID, attempt.getRoute().getRowID());
        contentValues.put(TOTAL_TIME, attempt.getTotalTimeTaken());
        contentValues.put(DATE_OF_ATTEMPT, attempt.getDateOfAttempt());
        contentValues.put(MAP_SCREENSHOT,attempt.getFileNameStaticMapScreenshot());
        long addedID = db.insert(TABLE_ATTEMPTS, null, contentValues);
        db.close();
        compareBestTime(attempt);
        return addedID;
    }

    //compares the current record best time for the given route and if the one of the current attempt is better, update the best time of the route table
    private void compareBestTime(Attempt attempt) {
        int totalTime = attempt.getTotalTimeTaken(); //in seconds
        long rowID = attempt.getRoute().getRowID(); //id of the row
        String query = "SELECT * FROM " + TABLE_ROUTES + " WHERE  rowid = " + rowID;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getInt(4) >= totalTime) {
                query = "UPDATE " + TABLE_ROUTES + " SET " + BEST_TIME + "=" + totalTime + " WHERE rowid = " + rowID;
                db.execSQL(query);
                query = "UPDATE " + TABLE_ROUTES + " SET " + DATE_OF_BEST_TIME + "='" + attempt.getDateOfAttempt() + "' WHERE rowid = " + rowID;
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

}
