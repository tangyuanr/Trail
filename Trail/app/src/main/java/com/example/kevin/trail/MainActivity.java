package com.example.kevin.trail;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    protected Button runButtonlink =null;
    protected Button hikeButtonlink =null;
    protected Button bikeButtonlink =null;
    protected Button gpsButtonlink=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        runButtonlink = (Button) findViewById(R.id.runButton);
        hikeButtonlink = (Button) findViewById(R.id.hikeButton);
        bikeButtonlink = (Button) findViewById(R.id.bikeButton);
        gpsButtonlink=(Button) findViewById(R.id.gpsButton);

        runButtonlink.setOnClickListener(new View.OnClickListener() {
         public void onClick(View view){
             goToRunActivity();
         }
        });

        hikeButtonlink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                goToHikeActivity();
            }
        });

        bikeButtonlink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                goToBikeActivity();
            }
        });

        gpsButtonlink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                goToGPSActivity();
            }
        });
    }

    void goToRunActivity(){
        Intent intent = new Intent(MainActivity.this, runActivity.class);
        startActivity(intent);
    }
    void goToHikeActivity(){
        Intent intent = new Intent(MainActivity.this, hikeActivity.class);
        startActivity(intent);
    }

    void goToBikeActivity(){
        Intent intent = new Intent(MainActivity.this, bikeActivity.class);
        startActivity(intent);
    }

    void goToGPSActivity(){
        Intent intent=new Intent(MainActivity.this, gpsActivity.class);
        startActivity(intent);
    }

}
