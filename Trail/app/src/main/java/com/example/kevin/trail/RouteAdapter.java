package com.example.kevin.trail;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Ezekiel on 3/15/2017.
 * Adapter that connects the Route objects to the listview
 */

public class RouteAdapter extends ArrayAdapter<Route> {
    private Context context;
    private LayoutInflater inflater;

    private ArrayList<Route> routes;

    public RouteAdapter(Context context, ArrayList<Route> routes) {
        super(context, R.layout.listview_item_image, routes);

        this.context = context;
        this.routes = routes;

        inflater = LayoutInflater.from(context);
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.listview_item_image, parent, false);
        }


        final ImageView imageView = (ImageView) convertView.findViewById(R.id.imageViewtest);

        Picasso.with(context).load(routes.get(position).getStaticAPIURL(context, 250, 130)).networkPolicy(NetworkPolicy.OFFLINE).fit().into(imageView, new Callback() {

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                Picasso.with(context).load(routes.get(position).getStaticAPIURL(context, 250, 130)).fit().into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(context).load(routes.get(position).getStaticAPIURL(context, 250, 130)).error(R.drawable.ic_deletebutton).into(imageView);
                    }
                });
            }
        });

        TextView routeName = (TextView) convertView.findViewById(R.id.textViewtest);
        routeName.setText(routes.get(position).toString());

        return convertView;
    }
}