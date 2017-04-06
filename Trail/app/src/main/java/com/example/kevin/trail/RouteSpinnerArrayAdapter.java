package com.example.kevin.trail;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by admin on 4/5/2017.
 */

public class RouteSpinnerArrayAdapter extends ArrayAdapter<Route>{

    private Context context;
    private ArrayList<Route> arrayRoutes;

    public RouteSpinnerArrayAdapter(Context context, int textViewResourceId, ArrayList<Route> arrayRoutes) {
        super(context, textViewResourceId, arrayRoutes);
        this.context = context;
        this.arrayRoutes = arrayRoutes;
    }

    public int getCount(){
        return arrayRoutes.size();
    }

    public Route getItem(int position){
        return arrayRoutes.get(position);
    }

    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView label = new TextView(context);
        label.setTextColor(Color.BLACK);
        label.setTextSize(20);
        String activityType = null;
        switch(arrayRoutes.get(position).getActivityType()) {
            case "Hiking":
                activityType = "Hiking";
                break;
            case "Biking":
                activityType = "Biking";
                break;
            case "Running":
                activityType = "Running";

        }

        label.setText(activityType + " - " + arrayRoutes.get(position).getRouteName());
        return label;
    }
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView label = new TextView(context);
        label.setTextColor(Color.BLACK);
        label.setTextSize(20);
        String activityType = null;
        switch(arrayRoutes.get(position).getActivityType()) {
            case "Hiking":
                activityType = "Hiking";
                break;
            case "Biking":
                activityType = "Biking";
                break;
            case "Running":
                activityType = "Running";

        }
        label.setText(activityType + " - " + arrayRoutes.get(position).getRouteName());

        return label;
    }
}