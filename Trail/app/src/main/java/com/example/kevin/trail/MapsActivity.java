package com.example.kevin.trail;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Route route;



    DBHandler db = new DBHandler(this);
    ArrayList<Route> routes;
    Polyline line;

    PolylineOptions option;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        DBHandler db = new DBHandler(getApplicationContext());


     //   route = new Route();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
      //  LatLng sydney = new LatLng(-34, 151);
      //  mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
      //  mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);







        //Toast.makeText(MapsActivity.this,myLongitude+ " , " +myLatitude, Toast.LENGTH_LONG).show();


        ArrayList<Location> latestRouteLocation =null;  /////contains array of coordinate of latest route
             ////fetching the latest route name

                if(db.getRoutes("").size()!=0) {

                    latestRouteLocation = db.getRoutes("").get(db.getRoutes("").size() - 1).buildLocationArray();
                    Toast.makeText(MapsActivity.this, "last route fetched", Toast.LENGTH_LONG).show();
                }

        ArrayList<LatLng> coordList = new ArrayList<LatLng>();

// Adding points to ArrayList
        if(latestRouteLocation!=null){
            for (int i =0;i<latestRouteLocation.size();i++) {

                coordList.add(new LatLng(latestRouteLocation.get(i).getLatitude(), latestRouteLocation.get(i).getLongitude())); ///creating a latlng object from location



            }                                                             ///creating a polyline

        }
        if(coordList.size()!=0) {
            mMap.addMarker(new MarkerOptions().position(coordList.get(0)).title("route starting point "));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(coordList.get(0)));
        }


        option = new PolylineOptions();
        //creating polyline with existing latlng list
        option.addAll(coordList);
        option.width(5)
                .color(Color.RED);

        mMap.addPolyline(option);
























    }
}
