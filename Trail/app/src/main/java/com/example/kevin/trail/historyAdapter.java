package com.example.kevin.trail;

import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by JY on 2017-04-08.
 * adapted from Andre's code in RouteAdapter.java and its related layouts
 */

public class historyAdapter extends BaseAdapter {

    //members
    private HashMap<String, List<String>> listContent;
    private ArrayList<String> title;
    private Context context;
    private LayoutInflater inflater;

    private String directory=Trail.getAppContext().getFilesDir() + "/";

    //constructor
    public historyAdapter(Context aContext, HashMap<String, List<String>> aContent){
        this.listContent=aContent;
        this.context=aContext;
        title=new ArrayList<>(listContent.keySet());
        inflater=LayoutInflater.from(context);
    }

    @Override
    public Object getItem(int position){return title.get(position);}

    @Override
    public long getItemId(int position){return position;}

    @Override
    public int getCount(){return title.size();}

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        super.unregisterDataSetObserver(observer);
    }

    public View getView(int position, View convertView, ViewGroup parent){
        /*******************COPIED FROM ANDRE'S ROUTEADAPTER.JAVA********************/
        TextViewSetter setter=new TextViewSetter();
        if (convertView==null){
            convertView=inflater.inflate(R.layout.history_list, null);
            setter.date=(TextView)convertView.findViewById(R.id.dateText);
            setter.activity=(TextView)convertView.findViewById(R.id.activityTypeText);
            setter.time=(TextView)convertView.findViewById(R.id.totalTimeText);
            setter.routeName=(TextView)convertView.findViewById(R.id.routeNameText);
            setter.distance=(TextView)convertView.findViewById(R.id.totalDistanceText);
            setter.hr=(TextView)convertView.findViewById(R.id.avgHRText);
            setter.calories=(TextView)convertView.findViewById(R.id.caloriesText);
            setter.snapshot=(ImageView)convertView.findViewById(R.id.historySnapshot);
            convertView.setTag(setter);
        }
        else
            setter=(TextViewSetter)convertView.getTag();

        /*******************************END OF COPYING****************************/

        //setting content
        //the hashmap's key is the date, values are ACTIVITY, ROUTE NAME, TOTAL TIME, TOTAL DISTANCE, AVERAGE HR, CALORIES strictly in this order
        String placeholder=dateReformat(title.get(position));
        setter.date.setText(placeholder);
        Log.d("HISTORYADAPTER", placeholder);
        placeholder=listContent.get(title.get(position)).get(0);//activity type
        setter.activity.setText(placeholder);
        Log.d("HISTORYADAPTER", placeholder);
        placeholder=listContent.get(title.get(position)).get(1);//route name
        setter.routeName.setText(placeholder);
        Log.d("HISTORYADAPTER", placeholder);
        placeholder=listContent.get(title.get(position)).get(2);//total time
        setter.time.setText(placeholder);
        Log.d("HISTORYADAPTER", placeholder);
        placeholder=listContent.get(title.get(position)).get(3);//total distance
        setter.distance.setText(placeholder);
        Log.d("HISTORYADAPTER", placeholder);
        placeholder=listContent.get(title.get(position)).get(4);//average heart rate
        setter.hr.setText(placeholder);
        Log.d("HISTORYADAPTER", placeholder);
        placeholder=listContent.get(title.get(position)).get(5);//calories burnt
        setter.calories.setText(placeholder);
        Log.d("HISTORYADAPTER", placeholder);


        /**************************DISPLAY SNAPSHOT*****************************************/
        String attemptDate=title.get(position).substring(6);//cut out the DATE: part
        String imageFileName=listContent.get(title.get(position)).get(6);//image filename
        String fulldirectory=directory+imageFileName;

        //handle image download outside sport activity here
        //if there was no internet when the attempt was created,
        File imagefile=new File(fulldirectory);
        if (!imagefile.exists()){
            if (!isNetworkAvailable()){
                noNetworkDialog();
            }
            //if there is indeed internet connection, download the snapshot image
            else{
                String coordinatesFile=listContent.get(title.get(position)).get(7);//coordinates filename
                //build url by first building a dummy Route object to use getStaticAPIURL method
                Route route = new Route("dummy route name", "dummy activity type", 0, 0, "dummy time", coordinatesFile, "dummy locality", "dummy filename");
                Log.d("historyActivity", "dummy route created, with coordinates filename: "+coordinatesFile);
                String url=route.getStaticAPIURL(context, 225, 140);
                //download the image to internal storage
                imageDownload(context, url, imageFileName);

                try {
                    setter.snapshot.setImageBitmap(BitmapFactory.decodeFile(fulldirectory));
                    setter.snapshot.setScaleType(ImageView.ScaleType.FIT_XY);
                }catch (RuntimeException e){
                    Log.d("historyActivity-ImgView", e.getMessage());
                }
            }
        }

        try {
            setter.snapshot.setImageBitmap(BitmapFactory.decodeFile(fulldirectory));
            setter.snapshot.setScaleType(ImageView.ScaleType.FIT_XY);
        }catch (RuntimeException e){
            Log.d("historyActivity-ImgView", e.getMessage());
        }
        //TODO do something when image is still not loaded
        if (null==setter.snapshot.getDrawable()) {
            try {
                setter.snapshot.setImageBitmap(BitmapFactory.decodeFile(fulldirectory));
                setter.snapshot.setScaleType(ImageView.ScaleType.FIT_XY);
            }catch (RuntimeException e){
                Log.d("historyActivity-ImgView", e.getMessage());
            }
        }
        //so many image loading calls...idk which one finally did the trick, but thank god it did



        return convertView;
    }


    private String dateReformat(String date){
        //the date returned by DBHandler.getcontent is in the format of YYYYMMDD_HHMM
        date=date.substring(6);
        Log.d("HISTORYADAPTER", "date: " +date);
        String year=date.substring(0, 4);
        String month=date.substring(4, 6);
        String day=date.substring(6, 8);
        String hour=date.substring(9, 11);
        String minute=date.substring(11, 13);

        Log.d("HISTORYADAPTER", "month: "+month);
        month= new DateFormatSymbols().getMonths()[Integer.parseInt(month)-1];

        String result=month+" "+day+" "+year+", "+hour+":"+minute;
        return result;
    }

    // Check if there's internet connection
    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo=connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo!=null&&activeNetworkInfo.isConnected();
    }

    //dialog that pops out when user tries to see a non-cached snapshot without internet
    private void noNetworkDialog() {
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


    //like a struct but in java??
    private class TextViewSetter{
        TextView date;
        TextView activity;
        TextView routeName;
        TextView time;
        TextView distance;
        TextView hr;
        TextView calories;
        ImageView snapshot;
    }

}
