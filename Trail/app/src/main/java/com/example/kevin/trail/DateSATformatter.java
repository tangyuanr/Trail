package com.example.kevin.trail;

import android.content.Context;

import com.jjoe64.graphview.DefaultLabelFormatter;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateSATformatter extends DefaultLabelFormatter {
    /**
     * the date format that will convert
     * the unix timestamp to string
     */
    protected final DateFormat mDateFormat;

    /**
     * calendar to avoid creating new date objects
     */
    protected final Calendar mCalendar;

    /**
     * create the formatter with the Android default date format to convert
     * the x-values.
     *
     * @param context the application context
     */
    public  DateSATformatter(Context context) {
        mDateFormat = android.text.format.DateFormat.getDateFormat(context);
        mCalendar = Calendar.getInstance();
    }

    /**
     * create the formatter with your own custom
     * date format to convert the x-values.
     *
     * @param context the application context
     * @param dateFormat custom date format
     */
    public  DateSATformatter(Context context, DateFormat dateFormat) {
        mDateFormat = dateFormat;
        mCalendar = Calendar.getInstance();
    }



    /**
     * formats the x-values as date string.
     *
     * @param value raw value
     * @param isValueX true if it's a x value, otherwise false
     * @return value converted to string
     */
    @Override
    public  String formatLabel(double value, boolean isValueX) {
        if (isValueX) {
            // format as date
            mCalendar.setTimeInMillis((long) value);
            int day_of_week = mCalendar.get(Calendar.DAY_OF_WEEK);
            return getDayOfWeek(day_of_week);
        } else {
            return String.format("%.1f", value);
        }
    }

    private String getDayOfWeek(int value) {
        String day = "";
        switch (value) {
            case 1:
                day = "Sun";
                break;
            case 2:
                day = "Mon";
                break;
            case 3:
                day = "Tue";
                break;
            case 4:
                day = "Wed";
                break;
            case 5:
                day = "Thu";
                break;
            case 6:
                day = "Fri";
                break;
            case 7:
                day = "Sat";
                break;
        }
        return day;
    }

}