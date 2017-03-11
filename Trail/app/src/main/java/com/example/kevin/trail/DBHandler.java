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
    private static final int DATABASE_VERSION=1;
    private static final String DATABASE_NAME="healthData";



    long id=0;
    private final String TAG="DBHandler";

    public DBHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // runner table content
    private static final String TABLE_RUN="Running";
    private static final String RUN_ID="RunID";
    private static final String RUN_DATA="CoordinateDataFile";
    private static final String RUN_DISTANCE="TotalDistance";
    private static final String RUN_TIME="TotalRunTime";

    @Override
    public void onCreate(SQLiteDatabase db){
        String CREATE_RUN_TABLE="CREATE TABLE "+TABLE_RUN+" ("+RUN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+RUN_DATA+" TEXT, "+RUN_DISTANCE + " TEXT, "+RUN_TIME+" TEXT)";
        Log.e(TAG, CREATE_RUN_TABLE);
        db.execSQL(CREATE_RUN_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_RUN);

        onCreate(db);
    }

    //setter
    //add running record
    public void addRecord(String filename, String distance, String time){
        SQLiteDatabase db=this.getWritableDatabase();
        //link data to corresponding column
        ContentValues contentValues=new ContentValues();
        contentValues.put(RUN_DATA,filename);//filename of .txt file containing coordinates
        contentValues.put(RUN_DISTANCE,distance);
        contentValues.put(RUN_TIME,time);
        //insert row into table
        id=db.insert(TABLE_RUN,null,contentValues);
        Log.d(TAG, "RUNNING record added into database");

        db.close();
    }

    // content generator for listview display
    public HashMap<String, List<String>> getContent(){
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
    }

    //row number getter
    public int getRowNumber(){
        SQLiteDatabase db=this.getReadableDatabase();
        int numRows=(int) DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM "+TABLE_RUN, null);
        return numRows;
    }

    // getters for...future...use??? honestly the data can also be extracted from the content generator above, which
    // probably consumes less memory since it won't need to establish connection with database
    public String getRunData(String ID){
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
    }
}
