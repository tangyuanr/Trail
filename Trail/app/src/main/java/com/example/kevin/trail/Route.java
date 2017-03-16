package com.example.kevin.trail;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ezekiel.
 * Route object that holds its name, its URL to google map static, and the image name if eventually  we choose to download it instead of keeping track of URLs.
 */

public class Route implements Parcelable {
    private String routeName = "";
    private String imgURL = "";
    private String imageName = "";

    public Route(String routeName, String mapURL) {
        this.routeName = routeName;
        this.imgURL = mapURL;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    //implementing toString so that a listview adapter can call it on a Route object. We can build the string here and return it to the listview.
    //For example, we can implement some DBHandler function to figure out what the best time is for a specific route, and concatenate it to the string being returned there.
    @Override
    public String toString() {
        return routeName + "\n Best time:"; //we can build up the returned string there.
    }


    //some methods that need to be implemented to use this in a Fragment.
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(routeName);
        dest.writeString(imgURL);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Route createFromParcel(Parcel in) {
            return new Route(in);
        }

        public Route[] newArray(int size) {
            return new Route[size];
        }
    };

    public Route(Parcel in) {
        routeName = in.readString();
        imgURL = in.readString();
    }


}
