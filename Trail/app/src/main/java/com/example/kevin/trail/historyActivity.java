package com.example.kevin.trail;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class historyActivity extends AppCompatActivity {
    ListView listView;
    historyAdapter adapter;
    HashMap<String, List<String>> detail=new HashMap<String, List<String>>();
    Toolbar toolbar;

    DBHandler dbHandler=new DBHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        detail=dbHandler.getContent();
        adapter=new historyAdapter(this, detail);

        listView=(ListView)findViewById(R.id.attemptsList);
        listView.setAdapter(adapter);

        toolbar=(Toolbar)findViewById(R.id.historyActionBar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("History");
        }
    }

    //action bar related methods:
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(menuItem);
    }
}
