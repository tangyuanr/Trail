package com.example.kevin.trail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by Kevin on 2017-03-25.
 */

public class sharedPreferenceHelper {
    private static SharedPreferences sharedPreferences;

    public sharedPreferenceHelper(Context context){
        sharedPreferences = context.getSharedPreferences("ProfilePreference",Context.MODE_PRIVATE);

    }

    public static void saveProfileGender (String gender){                           //Save name into SharedPreferences.
        SharedPreferences.Editor nameEditor = sharedPreferences.edit();
        nameEditor.putString("profileGender", gender);
        nameEditor.apply();
    }

    public static void saveProfileAge(String age){                              //Save age into local storage
        SharedPreferences.Editor ageEditor = sharedPreferences.edit();
        ageEditor.putString("profileAge", age);
        ageEditor.commit();

    }

    public static void saveProfileWeight(String weight){                                //Save id into local storage
        SharedPreferences.Editor idEditor = sharedPreferences.edit();
        idEditor.putString("profileWeight", weight);
        idEditor.apply();
    }

    public String getProfileAge(){                                              //Return age that was saved in localstorage
        return sharedPreferences.getString("profileAge", "");
    }

    public String getProfileGender(){                                             //Return name saved in local storage
        return sharedPreferences.getString("profileGender", "");
    }

    public String getProfileWeight(){                                               //Return id saved in local storage
        return sharedPreferences.getString("profileWeight", "");
    } //Return id saved in local storage
}
