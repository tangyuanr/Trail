package com.example.kevin.trail;

import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
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
import java.util.ArrayList;
import java.util.Date;


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
    private BottomBar mBottomBar;
    private LinearLayout graphLayout;
    private LinearLayout spinnerLayout;
    private LinearLayout progressPane;
    private SpinnerAdapter selectedroutespinneradapter;
    private ImageView imageviewRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);


        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        graphLayout = (LinearLayout) findViewById(R.id.graphLayout);
        spinnerLayout = (LinearLayout) findViewById(R.id.spinnerLayout);
        progressPane = (LinearLayout) findViewById(R.id.progressPane);
        imageviewRoute = (ImageView) findViewById(R.id.imageViewRoute);
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
        }
        else {
            graph.setVisibility(View.GONE);
            spinnerShowing.setVisibility(View.GONE);
            showingTextView.setVisibility(View.GONE);
            sinceTextView.setText("There is no data to display. Get your lazy ass moving.");
        }

        generateAttemptTableForGivenRoute("goud");
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
            if (!(attemptsforoneday.isEmpty())) {
                for (int j = 0; j < attemptsforoneday.size(); j++) {
                    totalDistance += attemptsforoneday.get(j).getTotalDistance();
                }
            }
            daysanddistance.add(new dayWhenSomethingWasDone(date,totalDistance));
        }
        return daysanddistance;
    }

    private void generateAttemptTableForGivenRoute(String routeName) {

        ArrayList<Attempt> attemptsList = dbhandler.getAttemptsFromRouteName(routeName);
        final ListView listview1 = (ListView) findViewById(R.id.custom_list);
        listview1.setAdapter(new AttemptsListAdapter(this, attemptsList));
        listview1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Object object = listview1.getItemAtPosition(position);
                Attempt attempt = (Attempt) object;
                Toast.makeText(graphActivity.this, "Selected attempt", Toast.LENGTH_LONG).show();
            }
        });

        //Picasso.with(graphActivity.this).load(attemptsList.get(0).getRoute().getStaticAPIURL(graphActivity.this, 400, 200)).into(imageviewRoute);
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
            if (!(attemptsforoneday.isEmpty())) {
                for (int j = 0; j < attemptsforoneday.size(); j++) {
                    totalDistance += attemptsforoneday.get(j).getTotalDistance();
                }
            }
            daysanddistance.add(new dayWhenSomethingWasDone(date,totalDistance));
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
        for (int i = 0; i < numberOf; i++) {
            datapoints[i] = new DataPoint(daysanddistance.get(i).getDateTime().toDate(), daysanddistance.get(i).getDistance());
        }

        if (numberOf > 1) {
            LineGraphSeries<DataPoint> series_line = new LineGraphSeries<>(datapoints);
            graph.addSeries(series_line);
            graph.getGridLabelRenderer().setLabelFormatter(new DateSATformatter(this));
            graph.getGridLabelRenderer().setNumHorizontalLabels(numberOf);
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