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
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
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
                Log.d("historyActivity", "clicked on snapshot ImageView");
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
                    String fulldirectory=directory+imageFileName;//get complete path to image

                    //handle image download outside sport activity here
                    //if there was no internet when the attempt was created,
                    File imagefile=new File(fulldirectory);
                    if (!imagefile.exists()){
                        if (!isNetworkAvailable()){
                            noNetworkDialog();
                        }
                        //if there is indeed internet connection, download the snapshot image
                        else{
                            String coordinatesFile=dbHandler.getFilenameCoordinates(attemptDate);
                            //build url by first building a dummy Route object to use getStaticAPIURL method
                            Route route = new Route("dummy route name", "dummy activity type", 0, 0, "dummy time", coordinatesFile, "dummy locality", "dummy filename");
                            Log.d("historyActivity", "dummy route created, with coordinates filename: "+coordinatesFile);
                            String url=route.getStaticAPIURL(historyActivity.this, 250, 250);
                            //download the image to internal storage
                            imageDownload(historyActivity.this, url, imageFileName);
                        }
                    }

                    try {
                        snapshot.setImageBitmap(BitmapFactory.decodeFile(fulldirectory));
                    }catch (RuntimeException e){
                        Log.d("historyActivity-ImgView", e.getMessage());
                        noNetworkDialog();
                    }
                    //display the snapshot only when the image is successfully loaded
                    if (null!=snapshot.getDrawable()) {
                        snapshot.setVisibility(View.VISIBLE);
                        snapshot.bringToFront();
                    }
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

    // Check if there's internet connection
    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo=connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo!=null&&activeNetworkInfo.isConnected();
    }

    //dialog that pops out when user tries to see a non-cached snapshot without internet
    private void noNetworkDialog() {
        AlertDialog helpDialog = new AlertDialog.Builder(this).create();
        helpDialog.setTitle("Snapshot has not been saved");
        helpDialog.setMessage("Uh oh! Looks like this snapshot has not been saved :/\n"+
                "\nYou can see it once you're connected to the Internet!"
        );
        helpDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        helpDialog.show();
    }

    //map snapshot saving
    public void imageDownload(Context context, String url, String filename){
        Picasso.with(context)
                .load(url)
                .into(getTarget(filename));
    }

    public Target getTarget(final String filename){
        Target target = new Target(){
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from){
                new Thread(new Runnable(){
                    @Override
                    public void run(){
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd_HHmm");//added start time so that attempts made on the same day can be differentiated in historyActivity
                        final String currentDateandTime = sdf.format(new Date());

                        String path = Trail.getAppContext().getFilesDir() + "/";
                        File file=new File(path+filename);

                        try{
                            file.createNewFile();
                            FileOutputStream ostream=new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                            ostream.flush();
                            ostream.close();

                        }catch (Exception e){
                            Log.e("getTarget", e.getMessage());
                        }
                    }
                }).start();
            }
            @Override
            public void onBitmapFailed(Drawable errordrawable){}
            @Override
            public void onPrepareLoad(Drawable placeholderdrawable){}
        };
        return target;
    }


}
