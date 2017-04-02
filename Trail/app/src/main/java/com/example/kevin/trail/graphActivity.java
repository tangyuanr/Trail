package com.example.kevin.trail;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.Series;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;

import static com.example.kevin.trail.R.id.graph;

/**
 * Created by Andre & Jiayin
 */
public class graphActivity extends AppCompatActivity {

    DBHandler dbhandler = new DBHandler(this);
    private String selectedShowingSpinner;
    private TextView sinceTextView;
    private Spinner spinnerShowing;
    private TextView selectedPoint;
    private TextView showingTextView;
    private GraphView graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        sinceTextView = (TextView) findViewById(R.id.sinceText);
        showingTextView = (TextView) findViewById(R.id.showing);
        spinnerShowing = (Spinner) findViewById(R.id.showingSpinner);
        graph = (GraphView) findViewById(R.id.graph);
        selectedPoint = (TextView) findViewById(R.id.selectedPoint);

        if(!(dbhandler.isRouteTableEmpty())) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.showingspinner_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerShowing.setAdapter(adapter);
            selectedShowingSpinner = spinnerShowing.getSelectedItem().toString();
            spinnerShowing.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedShowingSpinner = parent.getItemAtPosition(position).toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    //
                }
            });
            sinceTextView.setText(initializeSinceText());
            initializeGraph();
        }
        else {
            graph.setVisibility(View.GONE);
            spinnerShowing.setVisibility(View.GONE);
            showingTextView.setVisibility(View.GONE);
            sinceTextView.setText("There is no data to display. Get your lazy ass moving.");
        }
    }

    private void initializeGraph() {
        DateTime today = new DateTime();
        DateTime firstDayOfInterval = new DateTime().minusDays(7);


        ArrayList<Attempt> attemptsList = dbhandler.getAttempts(firstDayOfInterval, today, "");
        ArrayList<dayWhenSomethingWasDone> daysanddistance = calculateKMTravelledInDay(attemptsList);
        ArrayList<dayWhenSomethingWasDone> lastweek = format7days(daysanddistance);




        int numberOf = lastweek.size();
        DataPoint[] datapoints = new DataPoint[lastweek.size()];

        for (int i = 0; i < lastweek.size(); i++) {
            datapoints[i] = new DataPoint(lastweek.get(i).getDateTime().toDate(), lastweek.get(i).getDistance());
        }
        if (numberOf > 1) {
            LineGraphSeries<DataPoint> series_line = new LineGraphSeries<>(datapoints);
            graph.addSeries(series_line);
            graph.getGridLabelRenderer().setLabelFormatter(new DateSATformatter(this));
            graph.getGridLabelRenderer().setNumHorizontalLabels(numberOf);
            graph.getViewport().setMinX(lastweek.get(0).getDateTime().toDate().getTime());
            graph.getViewport().setMaxX(lastweek.get(numberOf - 1).getDateTime().toDate().getTime());
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getGridLabelRenderer().setHumanRounding(false);
            graph.getGridLabelRenderer().setHorizontalAxisTitle("");
            graph.getGridLabelRenderer().setVerticalAxisTitle("distance travelled (km)");

            PointsGraphSeries<DataPoint> series_points = new PointsGraphSeries<>(datapoints);
            series_points.setSize(7);
            graph.addSeries(series_points);

            series_points.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    long epoch = (long) dataPoint.getX();
                    DateTime date = new DateTime(epoch);
                    String str = "On " + DateTimeToString(date) +", you have moved " +  String.format("%.2f", dataPoint.getY()) + " kilometers.";
                    selectedPoint.setText(str);
                }
            });


        } else {
            graph.setVisibility(View.GONE);
            sinceTextView.append("\n\nYou must have at least 2 days worth of data to use Trail's graphing feature.");
        }
    }

    private ArrayList<dayWhenSomethingWasDone> format7days(ArrayList<dayWhenSomethingWasDone> daysdistance) {

        int numberOf = daysdistance.size();
        DateTime lastDay = daysdistance.get(numberOf - 1).getDateTime(); //last day (today)
        DateTime firstDay = lastDay.minusDays(6); //first day one week ago)

        ArrayList<dayWhenSomethingWasDone> last7days = new ArrayList<>();
        for(int i = 0; i < 7; i++) {
            last7days.add(new dayWhenSomethingWasDone(firstDay.plusDays(i),0));
        }
        for(int i =0; i < numberOf; i++) {
            for(int j = 0; j < 7; j++) {
                if(daysdistance.get(i).getDateTime().getDayOfWeek() == last7days.get(j).getDateTime().getDayOfWeek()) {
                    float distance = daysdistance.get(i).getDistance();
                    last7days.get(j).setDistance(distance);
                }
            }
        }
        return last7days;
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_DOWN);
        return bd.floatValue();
    }



    private ArrayList<dayWhenSomethingWasDone> calculateKMTravelledInDay(ArrayList<Attempt> attemptsList) {

        ArrayList<dayWhenSomethingWasDone> daywhensthdone = new ArrayList<>();
        float totalDistance = 0;
        for (int i = 0; i < attemptsList.size(); i++) {
            if (i == 0) {
                totalDistance += attemptsList.get(0).getRoute().getTotalDistance();
            } else {
                int comparator = DateTimeComparator.getDateOnlyInstance().compare(attemptsList.get(i).getDateofAttemptAsDateTime(), attemptsList.get(i - 1).getDateofAttemptAsDateTime());
                if (comparator == 0) {
                    totalDistance += attemptsList.get(i).getRoute().getTotalDistance();
                } else {
                    daywhensthdone.add(new dayWhenSomethingWasDone(attemptsList.get(i - 1).getDateofAttemptAsDateTime(), totalDistance));
                    totalDistance = attemptsList.get(i).getRoute().getTotalDistance();
                }
            }
            if (i == attemptsList.size() - 1) {
                daywhensthdone.add(new dayWhenSomethingWasDone(attemptsList.get(i).getDateofAttemptAsDateTime(), totalDistance));
            }
        }
        return daywhensthdone;
    }

    private class dayWhenSomethingWasDone {

        private DateTime date;
        private float distance;

        public dayWhenSomethingWasDone(DateTime date, float distance) {
            this.date = date;
            this.distance = distance;
        }

        public DateTime getDateTime() {
            return date;
        }

        public void setDateTime(DateTime date) {
            this.date = date;
        }

        public float getDistance() {
            return distance;
        }

        public void setDistance(float distance) {
            this.distance = distance;
        }
    }


    private String initializeSinceText() {
        String sinceText;
        String dayString = " days";
        DateTime FirstDateAccordingToSpinner = getFirstDateAccordingToSpinner(selectedShowingSpinner);
        DateTime today = new DateTime();
        int difference = Days.daysBetween(FirstDateAccordingToSpinner.toLocalDate(), today.toLocalDate()).getDays() + 1;
        if (difference == 1) {
            dayString = " day";
        }
        ArrayList<Attempt> attemptsList = dbhandler.getAttempts(FirstDateAccordingToSpinner, today, "");
        float totalDistance = totalKilometersFromAttemptsArray(attemptsList);
        sinceText = "Since " + DateTimeToString(FirstDateAccordingToSpinner) + ", you have moved " + String.format("%.2f", totalDistance) + " kilometers in "
                + String.valueOf(difference) + dayString + " spread over " + String.valueOf(attemptsList.size()) + " sessions."
                + " It means that on average, ever since that time, you have moved " + String.format("%.2f", totalDistance / difference) + " kilometers per day.";
        return sinceText;
    }

    private String DateTimeToString(DateTime dt) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("d MMMM, yyyy");
        return dt.toString(fmt);
    }

    private float totalKilometersFromAttemptsArray(ArrayList<Attempt> attemptsList) {

        float totalKM = 0;

        for (int i = 0; i < attemptsList.size(); i++) {
            totalKM += attemptsList.get(i).getRoute().getTotalDistance();
        }

        return totalKM;
    }

    private DateTime getFirstDateAccordingToSpinner(String show) {

        DateTime date = null;

        switch (show) {
            case "all-time stats":
                date = dbhandler.getStartingDate();
                break;
            case "stats for the last 7 days":
                date = new DateTime().minusDays(7);
                break;
            case "stats for the last month":
                date = new DateTime().minusMonths(1);
                break;
            case "stats for the last 3 months":
                date = new DateTime().minusMonths(3);
                break;
            case "stats for the last 6 months":
                date = new DateTime().minusMonths(6);
                break;
            case "stats for the last year":
                date = new DateTime().minusYears(1);
                break;
        }
        return date;
    }
}