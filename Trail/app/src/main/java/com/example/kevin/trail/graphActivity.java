package com.example.kevin.trail;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.Series;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;


/**
 * Created by Andre & Jiayin
 */
public class graphActivity extends AppCompatActivity {

    DBHandler dbhandler = new DBHandler(this);
    private String selectedShowingSpinner;
    private TextView sinceTextView;
    private Spinner spinnerShowing;
    private Spinner selectRouteSpinner;
    private TextView selectedPoint;
    private TextView showingTextView;
    private GraphView graph;
    private GraphView HRgraph;
    private GraphView CALgraph;
    private TextView hrGraphText;
    private TextView calGraphText;
    private BottomBar mBottomBar;
    protected Toolbar graphToolbar;
    private LinearLayout graphLayout;
    private LinearLayout spinnerLayout;
    private LinearLayout progressPane;
    private SpinnerAdapter selectedroutespinneradapter;
    private ImageView imageviewRoute;
    private TextView totalDistance;
    private TextView bestTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);


        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        graphLayout = (LinearLayout) findViewById(R.id.graphLayout);
        spinnerLayout = (LinearLayout) findViewById(R.id.spinnerLayout);
        progressPane = (LinearLayout) findViewById(R.id.progressPane);
        imageviewRoute = (ImageView) findViewById(R.id.imageViewRoute);
        totalDistance = (TextView) findViewById(R.id.totaldistanceRoute);
        bestTime = (TextView) findViewById(R.id.besttime);
        progressPane.setVisibility(View.GONE);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_Graphs) {
                    graphLayout.setVisibility(View.VISIBLE);
                    spinnerLayout.setVisibility(View.VISIBLE);
                    progressPane.setVisibility(View.INVISIBLE);
                }

                if (tabId == R.id.tab_Progress) {
                    graphLayout.setVisibility(View.GONE);
                    spinnerLayout.setVisibility(View.GONE);
                    progressPane.setVisibility(View.VISIBLE);
                }
            }
        });

        sinceTextView = (TextView) findViewById(R.id.sinceText);
        showingTextView = (TextView) findViewById(R.id.showing);
        spinnerShowing = (Spinner) findViewById(R.id.showingSpinner);
        graph = (GraphView) findViewById(R.id.graph);
        HRgraph=(GraphView)findViewById(R.id.hrGraph);
        hrGraphText=(TextView)findViewById(R.id.hrGraphInfo);
        CALgraph=(GraphView)findViewById(R.id.calGraph);
        calGraphText=(TextView)findViewById(R.id.caloriesGraphInfo) ;
        selectedPoint = (TextView) findViewById(R.id.selectedPoint);

        // selectedRoute spinner dynamic fill
        selectRouteSpinner = (Spinner) findViewById(R.id.selectRouteSpinner);
        ArrayList<Route> routes = dbhandler.getRoutes("");
        selectedroutespinneradapter = new RouteSpinnerArrayAdapter(graphActivity.this, android.R.layout.simple_spinner_dropdown_item, routes);
        selectRouteSpinner.setAdapter(selectedroutespinneradapter);
        selectRouteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Route route = (Route) selectedroutespinneradapter.getItem(position);
                generateAttemptTableForGivenRoute(route.getRouteName());
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapter) {  }
        });




        if(!(dbhandler.isRouteTableEmpty())) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.showingspinner_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerShowing.setAdapter(adapter);
            selectedShowingSpinner = spinnerShowing.getSelectedItem().toString();
            spinnerShowing.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedShowingSpinner = parent.getItemAtPosition(position).toString();
                    generateSinceText();
                    generateGraph();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    //
                }
            });

            generateSinceText();
            generateGraph();
            generateAttemptTableForGivenRoute("");
        }
        else {
            graph.setVisibility(View.GONE);
            spinnerShowing.setVisibility(View.GONE);
            showingTextView.setVisibility(View.GONE);
            HRgraph.setVisibility(View.GONE);
            hrGraphText.setVisibility(View.GONE);
            CALgraph.setVisibility(View.GONE);
            calGraphText.setVisibility(View.GONE);
            sinceTextView.setText("There is no data to display.");
            bottomBar.setVisibility(View.GONE);
        }



        //actionbar
        graphToolbar=(Toolbar)findViewById(R.id.graphToolbar);
        setSupportActionBar(graphToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        graphToolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));

        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ArrayList<dayWhenSomethingWasDone> ArrayDistancePerDate(int DAYS_BACK) {
        ArrayList<dayWhenSomethingWasDone> daysanddistance = new ArrayList<>();
        DateTime firstDayOfInterval = new DateTime().minusDays(DAYS_BACK);
        for(int i = 0; i < DAYS_BACK + 1; i++) {
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
            DateTime date = firstDayOfInterval.plusDays(i);
            String dateString = date.toString(fmt);
            ArrayList<Attempt> attemptsforoneday = dbhandler.getAttemptsByDate(dateString, "");
            float totalDistance = 0;
            int HR=0;
            int count=0;
            double calories=0;
            if (!(attemptsforoneday.isEmpty())) {
                for (int j = 0; j < attemptsforoneday.size(); j++) {
                    totalDistance += attemptsforoneday.get(j).getTotalDistance();
                    int hr=(int)attemptsforoneday.get(j).getAverageHeartRate();
                    if (hr!=0){
                        HR+=hr;
                        count++;
                    }
                    calories+=attemptsforoneday.get(j).getCaloriesBurnt();
                }
            }
            if (count!=0)
                HR=HR/count;
            daysanddistance.add(new dayWhenSomethingWasDone(date,totalDistance, HR, calories));
            Log.d("GRAPH", "HR added: "+HR);
        }
        return daysanddistance;
    }

    private void generateAttemptTableForGivenRoute(String routeName) {

        if(routeName.equals("")) {
            routeName = dbhandler.getRoutes("").get(0).getRouteName();
        }
        ArrayList<Attempt> attemptsList = dbhandler.getAttemptsFromRouteName(routeName);
        final ListView listview1 = (ListView) findViewById(R.id.custom_list);
        Route route = attemptsList.get(0).getRoute();
        long bt = route.getBestTime();

        listview1.setAdapter(new AttemptsListAdapter(this, attemptsList, bt));
        listview1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Object object = listview1.getItemAtPosition(position);
                Attempt attempt = (Attempt) object;
                Toast.makeText(graphActivity.this, "Selected attempt", Toast.LENGTH_LONG).show();
            }
        });


        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        float density  = getResources().getDisplayMetrics().density;
        float dpHeight = outMetrics.heightPixels / density;
        float dpWidth  = outMetrics.widthPixels / density;


        Picasso.with(graphActivity.this).load(route.getStaticAPIURL(graphActivity.this, (int)dpWidth, 150)).fit().into(imageviewRoute);
        float distance = route.getTotalDistance();
        totalDistance.setText("Distance: " + String.format("%.2f", distance) + " km");
        String activityType = route.getActivityType();
        if(activityType.equals("Running") || activityType.equals("Biking")) {
            TimeZone tz = TimeZone.getTimeZone("UTC");
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            df.setTimeZone(tz);
            String time = df.format(new Date(bt*1000));
            DateTime datebesttime = route.getDateBestTimeAsDateTIme();
            DateTimeFormatter fmt1 = DateTimeFormat.forPattern("MMMM d, yyyy");
            bestTime.setText("Best time: " + time + " on " + fmt1.print(datebesttime));
        }
    }




    private ArrayList<dayWhenSomethingWasDone> ArrayDistancePerDate() {
        ArrayList<dayWhenSomethingWasDone> daysanddistance = new ArrayList<>();
        DateTime firstDayOfInterval = dbhandler.getStartingDate();
        DateTime today = new DateTime();
        int difference = Days.daysBetween(firstDayOfInterval.toLocalDate(), today.toLocalDate()).getDays() + 1;
        for(int i = 0; i < difference; i++) {
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
            DateTime date = firstDayOfInterval.plusDays(i);
            String dateString = date.toString(fmt);
            ArrayList<Attempt> attemptsforoneday = dbhandler.getAttemptsByDate(dateString, "");
            float totalDistance = 0;
            int HR=0;
            int count=0;
            double calories=0;
            if (!(attemptsforoneday.isEmpty())) {
                for (int j = 0; j < attemptsforoneday.size(); j++) {
                    totalDistance += attemptsforoneday.get(j).getTotalDistance();
                    int hr=(int)attemptsforoneday.get(j).getAverageHeartRate();
                    if (hr!=0){
                        HR+=hr;
                        count++;
                    }
                    calories+=attemptsforoneday.get(j).getCaloriesBurnt();
                }
            }
            if (count!=0)
                HR=HR/count;
            daysanddistance.add(new dayWhenSomethingWasDone(date,totalDistance, HR, calories));
            Log.d("GRAPH", "HR added: "+HR);
        }
        return daysanddistance;
    }

    private void generateGraph() {
        int DAYS_BACK = 0;
        switch (selectedShowingSpinner) {
            case "stats for the last 7 days":
                DAYS_BACK = 6;
                break;
            case "stats for the last month":
                DAYS_BACK = 30;
                break;
            case "stats for the last 3 months":
                DAYS_BACK = 90;
                break;
            case "stats for the last 6 months":
                DAYS_BACK = 180;
                break;
            case "stats for the last year":
                DAYS_BACK = 365;
                break;
        }

        ArrayList<dayWhenSomethingWasDone> daysanddistance = ArrayDistancePerDate(DAYS_BACK);
        int numberOf = daysanddistance.size();

        DataPoint[] datapoints = new DataPoint[numberOf];
        DataPoint[] hrDataPoints=new DataPoint[numberOf];
        DataPoint[] calDataPoints=new DataPoint[numberOf];
        for (int i = 0; i < numberOf; i++) {
            datapoints[i] = new DataPoint(daysanddistance.get(i).getDateTime().toDate(), daysanddistance.get(i).getDistance());
            hrDataPoints[i]=new DataPoint(daysanddistance.get(i).getDateTime().toDate(), daysanddistance.get(i).getHR());
            calDataPoints[i]=new DataPoint(daysanddistance.get(i).getDateTime().toDate(), daysanddistance.get(i).getCalories());
        }
        /**************************HR GRAPH*******************************************/
        //compute data points: all HR information for all attempts



        if (numberOf > 1) {
            LineGraphSeries<DataPoint> series_line = new LineGraphSeries<>(datapoints);
            //series_line.setColor(Color.GREEN);
            graph.addSeries(series_line);
            graph.getGridLabelRenderer().setLabelFormatter(new DateSATformatter(this));
            graph.getGridLabelRenderer().setNumHorizontalLabels(numberOf);
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getGridLabelRenderer().setHumanRounding(false);
            graph.getGridLabelRenderer().setHorizontalAxisTitle("");
            graph.getGridLabelRenderer().setVerticalAxisTitle("distance travelled (km)");

            PointsGraphSeries<DataPoint> series_points = new PointsGraphSeries<>(datapoints);
            series_points.setSize(7);
            //series_points.setColor(Color.GREEN);
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

            /********************HR GRAPH*******************************/
            LineGraphSeries<DataPoint> hr_series=new LineGraphSeries<>(hrDataPoints);
            hr_series.setColor(Color.parseColor("#3CA508"));
            HRgraph.addSeries(hr_series);
            HRgraph.getGridLabelRenderer().setLabelFormatter(new DateSATformatter(graphActivity.this));
            HRgraph.getGridLabelRenderer().setHumanRounding(false);
            HRgraph.getGridLabelRenderer().setVerticalAxisTitle("Average heart rate (bpm)");
            HRgraph.getGridLabelRenderer().setTextSize(25);
            HRgraph.getGridLabelRenderer().setHorizontalAxisTitle("");
            HRgraph.getGridLabelRenderer().setNumHorizontalLabels(numberOf);


            /*******************CALORIES GRAPH*********************/
            BarGraphSeries<DataPoint> cal_series=new BarGraphSeries<>(calDataPoints);
            cal_series.setDrawValuesOnTop(true);
            cal_series.setValuesOnTopColor(Color.parseColor("#FF9933"));
            cal_series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
                @Override
                public int get(DataPoint data) {
                    return Color.parseColor("#FF9966");
                }
            });
            cal_series.setValuesOnTopSize(50);

            CALgraph.addSeries(cal_series);
            CALgraph.getGridLabelRenderer().setLabelFormatter(new DateSATformatter(graphActivity.this));
            CALgraph.getGridLabelRenderer().setHumanRounding(false);
            CALgraph.getGridLabelRenderer().setVerticalAxisTitle("Calories burnt (Cal)");
            CALgraph.getGridLabelRenderer().setTextSize(25);
            CALgraph.getGridLabelRenderer().setHorizontalAxisTitle("");
            CALgraph.getGridLabelRenderer().setNumHorizontalLabels(numberOf);




        } else {
            graph.setVisibility(View.GONE);
            sinceTextView.append("\n\nYou must have at least 2 days worth of data to use Trail's graphing feature.");
        }
    }

    private class dayWhenSomethingWasDone {

        private DateTime date;
        private float distance;
        private int HR;
        private double calories;

        public dayWhenSomethingWasDone(DateTime date, float distance, int heartrate, double cal) {
            this.date = date;
            this.distance = distance;
            this.HR=heartrate;
            this.calories=cal;
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

        public void setHR(int hr){this.HR=hr;}

        public int getHR(){return HR;}

        public double getCalories(){return calories;}
    }


    private void generateSinceText() {

        DateTime date = null;
        ArrayList<dayWhenSomethingWasDone> DistanceDate = new ArrayList<>();
        String sinceText;
        int DAYS_BACK = 0;
        switch (selectedShowingSpinner) {
            case "stats for the last 7 days":
                date = new DateTime().minusDays(6);
                DAYS_BACK = 6;
                break;
            case "stats for the last month":
                date = new DateTime().minusMonths(1);
                DAYS_BACK = 30;
                break;
            case "stats for the last 3 months":
                date = new DateTime().minusMonths(3);
                DAYS_BACK = 90;
                break;
            case "stats for the last 6 months":
                date = new DateTime().minusMonths(6);
                DAYS_BACK = 180;
                break;
            case "stats for the last year":
                date = new DateTime().minusYears(1);
                DAYS_BACK = 365;
                break;
        }


        DistanceDate = ArrayDistancePerDate(DAYS_BACK);

        float totalDistanceMoved = totalKilometersFromdaysArray(DistanceDate);
        sinceText = "Since " + DateTimeToString(date) + ", you have moved " + String.format("%.1f", totalDistanceMoved) + " kilometers in " + String.valueOf(DistanceDate.size()) + " days." +
        " It means that on average, you have moved " + String.format("%.1f", totalDistanceMoved / DistanceDate.size()) + " kilometers per day.";
        sinceTextView.setText(sinceText);
    }



    private String DateTimeToString(DateTime dt) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("d MMMM, yyyy");
        return dt.toString(fmt);
    }

    private float totalKilometersFromdaysArray(ArrayList<dayWhenSomethingWasDone> daysSthdone) {
        float totalKM = 0;
        for (int i = 0; i < daysSthdone.size(); i++) {
            totalKM += daysSthdone.get(i).getDistance();
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


    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_DOWN);
        return bd.floatValue();
    }

}