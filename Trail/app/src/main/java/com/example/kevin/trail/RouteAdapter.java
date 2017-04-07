package com.example.kevin.trail;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Ezekiel on 3/15/2017.
 * Adapter that connects the Route objects to the listview
 */

public class RouteAdapter extends BaseAdapter {
    private ArrayList<Route> routeArray;
    private LayoutInflater layoutInflater;
    private Context context;

    public RouteAdapter(Context context, ArrayList<Route> routeArray) {
        this.routeArray = routeArray;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        super.unregisterDataSetObserver(observer);
    }

    @Override
    public int getCount() {
        return routeArray.size();
    }

    @Override
    public Object getItem(int position) {
        return routeArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_routemanager, null);
            holder = new ViewHolder();
            holder.distanceView = (TextView) convertView.findViewById(R.id.routeDistancerouteManager);
            holder.CityOrBestTimeView = (TextView) convertView.findViewById(R.id.CityOrBestTimeRouteManager);
            holder.routeNameView = (TextView) convertView.findViewById(R.id.routeNamerouteManager);
            holder.routeSnapshotView = (ImageView) convertView.findViewById(R.id.routeSnapShot);
            holder.BestTimeTextView = (TextView) convertView.findViewById(R.id.BestTimeText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String routeName = routeArray.get(position).getRouteName();
        holder.routeNameView.setText(routeName);
        float distance = routeArray.get(position).getTotalDistance();
        holder.distanceView.setText(String.format("%.1f", distance) + " km");

        String activityType = routeArray.get(position).getActivityType();
        if(activityType.equals("Hiking")) {
            holder.BestTimeTextView.setVisibility(View.GONE);
            holder.CityOrBestTimeView.setText(routeArray.get(position).getLocality());
        }
        else {
            long bt = routeArray.get(position).getBestTime();
            TimeZone tz = TimeZone.getTimeZone("UTC");
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            df.setTimeZone(tz);
            String time = df.format(new Date(bt*1000));
            DateTime datebesttime = routeArray.get(position).getDateBestTimeAsDateTIme();
            DateTimeFormatter fmt1 = DateTimeFormat.forPattern("MM/dd/yyyy");
            holder.CityOrBestTimeView.setText(time);
        }

        Picasso.with(context).load(routeArray.get(position).getStaticAPIURL(context, 195, 140)).fit().into(holder.routeSnapshotView);

        return convertView;
    }

    static class ViewHolder {
        TextView CityOrBestTimeView;
        TextView distanceView;
        TextView routeNameView;
        TextView BestTimeTextView;
        ImageView routeSnapshotView;
    }
}