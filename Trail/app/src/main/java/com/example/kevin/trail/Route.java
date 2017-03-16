package com.example.kevin.trail;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by admin on 3/15/2017.
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


    @Override
    public String toString() {
        return routeName + "\n Best time:";
    }
}
