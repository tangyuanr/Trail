package com.example.kevin.trail;

import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
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
import com.squareup.picasso.Target;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileOutputStream;
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
    private boolean noInternetDialog = false;

    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo=connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo!=null&&activeNetworkInfo.isConnected();
    }

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

    private void noNetworkDialog() {
        noInternetDialog = true;
        AlertDialog helpDialog = new AlertDialog.Builder(context).create();
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

        //Picasso.with(context).load(routeArray.get(position).getStaticAPIURL(context, 195, 140)).fit().into(holder.routeSnapshotView);



        String directory=context.getApplicationContext().getFilesDir() + "/";
        String imageFileName=routeArray.get(position).getImagefilename();//get the image filename in date.jpg format
        String fulldirectory=directory+imageFileName;//get complete path to image

        //handle image download outside sport activity here
        //if there was no internet when the attempt was created,
        File imagefile=new File(fulldirectory);
        if (!imagefile.exists()){
            if (!isNetworkAvailable() && !noInternetDialog){
                noNetworkDialog();
            }
            //if there is indeed internet connection, download the snapshot image
            else{
                String coordinatesFile=routeArray.get(position).getFilename_coordinates();
                //build url by first building a dummy Route object to use getStaticAPIURL method
                Route route = new Route("dummy route name", "dummy activity type", 0, 0, "dummy time", coordinatesFile, "dummy locality", "dummy filename");
                String url=route.getStaticAPIURL(context, 225, 140);
                //download the image to internal storage
                imageDownload(context, url, imageFileName);
            }
        }

        try {
            holder.routeSnapshotView.setImageBitmap(BitmapFactory.decodeFile(fulldirectory));
            holder.routeSnapshotView.setScaleType(ImageView.ScaleType.FIT_XY);
        }catch (RuntimeException e){
            Log.d("routeadapter-ImgView", e.getMessage());
            if(!noInternetDialog) {
                noNetworkDialog();
            }
        }

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

