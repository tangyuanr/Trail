package com.example.kevin.trail;

import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class historyActivity extends AppCompatActivity {
    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    protected ImageView snapshot;
    List<String> titles;
    HashMap<String, List<String>> detail=new HashMap<String, List<String>>();

    private String directory=Trail.getAppContext().getFilesDir() + "/";

    DBHandler dbHandler=new DBHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        expandableListView=(ExpandableListView)findViewById(R.id.historyListView);

        detail=dbHandler.getContent();
        titles=new ArrayList<String>(detail.keySet());
        expandableListAdapter=new ExpandableListAdapter(this, titles, detail);
        expandableListView.setAdapter(expandableListAdapter);
        snapshot=(ImageView)findViewById(R.id.snapshotView);

        snapshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snapshot.setVisibility(View.INVISIBLE);//when user clicks on image, the image disappears
            }
        });

        // at expand
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        titles.get(groupPosition) + " List Expanded.",
                        Toast.LENGTH_SHORT).show();
            }
        });
        // at collapse
        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        titles.get(groupPosition) + " List Collapsed.",
                        Toast.LENGTH_SHORT).show();

            }
        });

        //display for child item
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                //display map snapshot when user clicks on "SEE MAP SNAPSHOT" child item
                String attemptDate;
                String imageFileName;

                String clickedItem=detail.get(titles.get(groupPosition)).get(childPosition);
                if (clickedItem.startsWith("See"))//the child item content is "See map snapshot"
                {
                    attemptDate = titles.get(groupPosition).substring(6);//cut the "DATE: " part of the string
                    imageFileName=dbHandler.getImageFileName(attemptDate);//get the image filename in date.jpg format
                    imageFileName=directory+imageFileName;//get complete path to image

                    snapshot.setImageBitmap(BitmapFactory.decodeFile(imageFileName));
                    snapshot.setVisibility(View.VISIBLE);
                    snapshot.bringToFront();
                }

//                Toast.makeText(
//                        getApplicationContext(),
//                        titles.get(groupPosition)
//                                + " -> "
//                                + detail.get(
//                                titles.get(groupPosition)).get(
//                                childPosition), Toast.LENGTH_SHORT
//                ).show();
                return false;
            }
        });


    }


}
