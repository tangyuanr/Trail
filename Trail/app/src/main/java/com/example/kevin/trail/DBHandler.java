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
    private static final String MASTER_TABLE_RUN="MasterRunning";
    private static final String TABLE_RUN_ATTEMPTS="RunAttempts";
    private static final String ROUTE_ID="RouteID";
    private static final String RUN_ATTEMPT_ID="RunAttemptID";
    private static final String RUN_ROUTE_NAME="RouteName";
    private static final String STATIC_MAP_URL="StaticMapURL";
    private static final String RUN_DATA="CoordinateDataFile"; //may be useless
    private static final String RUN_DISTANCE="TotalDistance";
    private static final String RUN_TIME="TotalRunTime";

    @Override
    public void onCreate(SQLiteDatabase db){

        //MASTER_RUN_TABLE holds the routeID (autoincrement row id), the route name and the URL to google static map API. downloading the image and storing the filename of the image is better.
        String CREATE_MASTER_RUN_TABLE="CREATE TABLE "+MASTER_TABLE_RUN+" ("+ROUTE_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+RUN_ROUTE_NAME+" TEXT, "+STATIC_MAP_URL + " TEXT)";
        Log.e(TAG, CREATE_MASTER_RUN_TABLE);
        db.execSQL(CREATE_MASTER_RUN_TABLE);
        //TABLE_RUN_ATTEMPTS holds a record of every attempt.
        //1st column is the ID of the attempt (autoincrement), the 2nd column is the ID of the route it is associated with (should write some code to enforce sqlite foreign key)
        //3rd column is the total time of the attempt, 4th time is the distance, 5th time is the filename of the data
        //the Distance should probably be stored in the MASTER_TABLE_RUN instead. what do you think?
        //keeping the data coordinates for every attempt is not needed, so we should probably remove this column.
        //instead we could keep one file for a specific route and hold its name in the MASTER_RUN_TABLE
        String CREATE_RUNS_TABLE="CREATE TABLE "+TABLE_RUN_ATTEMPTS+" ("+RUN_ATTEMPT_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+ROUTE_ID+" INT, "+RUN_TIME + " REAL, "
                + RUN_DISTANCE + " REAL, " + RUN_DATA + " TEXT)";
        Log.e(TAG, CREATE_RUNS_TABLE);
        db.execSQL(CREATE_RUNS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS "+MASTER_TABLE_RUN);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_RUN_ATTEMPTS);
        onCreate(db);
    }


    //builds an arraylist of Route objects and returns it
    public ArrayList<Route> getAllRoutes() {
        ArrayList<Route> routesList = new ArrayList<>();
        String query = "SELECT * FROM " + MASTER_TABLE_RUN;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor routesCursor = db.rawQuery(query, null);
        try {
            while (routesCursor.moveToNext()) {
                String nameOfRoute = routesCursor.getString(1);
                String staticMapURL = routesCursor.getString(2);
                Route route = new Route(nameOfRoute, staticMapURL);
                routesList.add(route);
            }
        } finally {
            routesCursor.close();
        }
        db.close();
        return routesList;
    }

    //setter
    //add running record
//    public void addRecord(String filename, String distance, String time){
//        SQLiteDatabase db=this.getWritableDatabase();
//        //link data to corresponding column
//        ContentValues contentValues=new ContentValues();
//        contentValues.put(RUN_DATA,filename);//filename of .txt file containing coordinates
//        contentValues.put(RUN_DISTANCE,distance);
//        contentValues.put(RUN_TIME,time);
//        //insert row into table
//        id=db.insert(TABLE_RUN,null,contentValues);
//        Log.d(TAG, "RUNNING record added into database");
//
//        db.close();
//    }

    //add new Route to Master table
    public long addMasterRoute(String routeName, String url) {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(RUN_ROUTE_NAME, routeName);//filename of .txt file containing coordinates
        contentValues.put(STATIC_MAP_URL, url);
        long addedID = db.insert(MASTER_TABLE_RUN, null, contentValues);  //keeping track of the addedID so that we can call addRunAttempt for the correct RouteID
        db.close();
        return addedID;
    }

    //adding an attempt to the attempt table
    public void addRunAttempt(int RouteID, int totalTimeMinutes, float totalDistanceKM, String filename) {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(ROUTE_ID, RouteID);
        contentValues.put(RUN_TIME, totalTimeMinutes);
        contentValues.put(RUN_DISTANCE, totalDistanceKM);
        contentValues.put(RUN_DATA,filename);
        long addedID = db.insert(TABLE_RUN_ATTEMPTS, null, contentValues);
        db.close();
    }

    // this checks if the Master table is empty. I made this so that in the MainActivity, when the Running button is clicked, it calls SelectRouteRunning if it's not empty or directly Running activity if this table is empty.
    public boolean isMasterTableEmpty(){

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " +MASTER_TABLE_RUN, null);

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

    // content generator for listview display
    /*public HashMap<String, List<String>> getContent(){
        HashMap<String, List<String>> expandList=new HashMap<String, List<String>>();
        String placeholder;

        SQLiteDatabase db=this.getReadableDatabase();
        String selectQuery="SELECT * FROM "+TABLE_RUN;
        Log.e(TAG, selectQuery);

        Cursor cursorRead=db.rawQuery(selectQuery,null);
        if (cursorRead!=null)
            cursorRead.moveToFirst();
        //loop through all rows
        if (cursorRead.moveToFirst()) {
//            do {
//                //for each row, put each column data into list item
//                List<String> dataHolder = new ArrayList<String>();
//                dataHolder.add(cursor.getString(cursor.getColumnIndex(RUN_DATA)));//get coordinates filename
//                dataHolder.add(cursor.getString(cursor.getColumnIndex(RUN_DISTANCE)));// get total run distance
//                dataHolder.add(cursor.getString(cursor.getColumnIndex(RUN_TIME)));// get total run time
//                //when adding row data is finished, name list with run ID
//                expandList.put(cursor.getString(cursor.getColumnIndex(RUN_ID)), dataHolder);
//            } while (cursor.moveToNext());
            while (false==cursorRead.isAfterLast()){
                List<String> dataHolder = new ArrayList<String>();
                placeholder=cursorRead.getString(cursorRead.getColumnIndex(RUN_DATA));//get coordinates filename
                Log.d(TAG, "read RUN DATA from RUN TABLE: "+placeholder);
                dataHolder.add(placeholder);
                placeholder=cursorRead.getString(cursorRead.getColumnIndex(RUN_DISTANCE));// get total run distance
                Log.d(TAG, "read RUN DISTANCE from RUN TABLE: "+placeholder);
                dataHolder.add(placeholder);
                placeholder=cursorRead.getString(cursorRead.getColumnIndex(RUN_TIME));// get total run time
                Log.d(TAG, "read RUN TIME from RUN TABLE: "+placeholder);
                dataHolder.add(placeholder);
                //when adding row data is finished, name key with ID
                placeholder=cursorRead.getString(cursorRead.getColumnIndex(RUN_ID));
                Log.d(TAG, "RUN ID: "+placeholder);
                expandList.put(placeholder, dataHolder);

                //next row
                cursorRead.moveToNext();
            }
        }
        cursorRead.close();
        db.close();

        return expandList;
    }*/

    //row number getter
  /*  public int getRowNumber(){
        SQLiteDatabase db=this.getReadableDatabase();
        int numRows=(int) DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM "+TABLE_RUN, null);
        return numRows;
    }*/

    // getters for...future...use??? honestly the data can also be extracted from the content generator above, which
    // probably consumes less memory since it won't need to establish connection with database
    /*public String getRunData(String ID){
        SQLiteDatabase db=this.getReadableDatabase();
        //setup query sentence to retrieve row based on ID
        String selectQuery="SELECT * FROM "+TABLE_RUN+" WHERE "+RUN_ID+"="+ID;
        Cursor cursor=db.rawQuery(selectQuery,null);
        cursor.moveToFirst();
        //placeholder string
        String runData;
        //get filename from column RUN_DATA
        runData=cursor.getString(cursor.getColumnIndex(RUN_DATA));
        Log.d(TAG, "retrieved coordinate data filename: "+runData);
        return runData;
    }

    public String getRunDistance(String ID){
        SQLiteDatabase db=this.getReadableDatabase();
        //setup query sentence to retrieve row based on ID
        String selectQuery="SELECT * FROM "+TABLE_RUN+" WHERE "+RUN_ID+"="+ID;
        Cursor cursor=db.rawQuery(selectQuery,null);
        cursor.moveToFirst();
        //placeholder string
        String runDistance;
        //get filename from column RUN_DISTANCE
        runDistance=cursor.getString(cursor.getColumnIndex(RUN_DISTANCE));
        Log.d(TAG, "retrieved total RUN distance: "+runDistance);

        return runDistance;
    }

    public String getRunTime(String ID){
        SQLiteDatabase db=this.getReadableDatabase();
        //setup query sentence to retrieve row based on ID
        String selectQuery="SELECT * FROM "+TABLE_RUN+" WHERE "+RUN_ID+"="+ID;
        Cursor cursor=db.rawQuery(selectQuery,null);
        cursor.moveToFirst();
        //placeholder string
        String runTime;
        //get filename from column RUN_TIME
        runTime=cursor.getString(cursor.getColumnIndex(RUN_TIME));
        Log.d(TAG, "retrieved total RUN time: "+runTime);

        return runTime;
    }*/
}
