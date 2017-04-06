package com.example.kevin.trail;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by admin on 4/5/2017.
 */

public class AttemptsListAdapter extends BaseAdapter {
    private ArrayList<Attempt> attemptArray;
    private LayoutInflater layoutInflater;
    private Context context;
    private long besttime;

    public AttemptsListAdapter(Context context, ArrayList<Attempt> attemptArray, long BestTime) {
        this.attemptArray = attemptArray;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.besttime = BestTime;
    }

    @Override
    public int getCount() {
        return attemptArray.size();
    }

    @Override
    public Object getItem(int position) {
        return attemptArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_attempts_comparison, null);
            holder = new ViewHolder();
            holder.dateView = (TextView) convertView.findViewById(R.id.dateOfAttempt);
            holder.totalTimeView = (TextView) convertView.findViewById(R.id.TotalTime);
            holder.AverageSpeedOrPaceView = (TextView) convertView.findViewById(R.id.AverageSpeedOrPace);
            holder.trophyIV = (ImageView) convertView.findViewById(R.id.trophyImageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DateTime date = attemptArray.get(position).getDateofAttemptAsDateTime();
        DateTimeFormatter fmt1 = DateTimeFormat.forPattern("MMMM d, yyyy");
        DateTimeFormatter fmt2 = DateTimeFormat.forPattern("HH:mm");
        holder.dateView.setText(fmt1.print(date) + " at " + fmt2.print(date));
        long totalTimetaken = attemptArray.get(position).getTotalTimeTaken();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(tz);
        String time = df.format(new Date(totalTimetaken*1000));
        holder.totalTimeView.setText("Duration: " + time);

        String activityType = attemptArray.get(position).getRoute().getActivityType();
        float totaldistance = attemptArray.get(position).getRoute().getTotalDistance();
        float speed = 3600*totaldistance / (totalTimetaken); //km/h
        if(activityType.equals("Biking")) {
            holder.AverageSpeedOrPaceView.setText("Average speed: " + String.format("%.1f", speed) + " km/h");
        }
        else if(activityType.equals("Running") && !(speed < 1)) {
            float pace = 60/speed;
            holder.AverageSpeedOrPaceView.setText("Average pace: " + String.format("%.1f", pace) + " min/km");
        }
        else {
            holder.AverageSpeedOrPaceView.setVisibility(View.GONE);
        }



        if(besttime == totalTimetaken) {
            Drawable myDrawable = context.getResources().getDrawable(R.drawable.trophy);
            holder.trophyIV.setImageDrawable(myDrawable);
        }



        return convertView;
    }

    static class ViewHolder {
        TextView dateView;
        TextView totalTimeView;
        TextView AverageSpeedOrPaceView;
        ImageView trophyIV;
    }
}