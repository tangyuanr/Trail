package com.example.kevin.trail;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class infoActivity extends AppCompatActivity {

    protected EditText genderEditText;                //Declare variables
    protected EditText ageEditText;
    protected EditText weightEditNum;

    private sharedPreferenceHelper sharedPreferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        sharedPreferenceHelper = new sharedPreferenceHelper(infoActivity.this);
        genderEditText = (EditText) findViewById(R.id.genderEditText);
        ageEditText = (EditText) findViewById(R.id.ageEditText);
        weightEditNum = (EditText) findViewById(R.id.weightEditNum);
        final Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {                                                  //Operation of save Button

                String gender = genderEditText.getText().toString();                                // Retrieves String input at EditText and convert into String output to use with functions.
                String age = ageEditText.getText().toString();
                String weight = weightEditNum.getText().toString();

                int lbs = Integer.parseInt(weight);
                int ageint = Integer.parseInt(age);                                             //Convert string input into integer in order to manipulate with integers and operators.
                if (ageint < 12 || ageint > 99) {
                    Toast failtoast = Toast.makeText(getApplicationContext(), "Profile not saved due to age range 12-99, please try again", Toast.LENGTH_LONG);  //Create a save failed message
                    failtoast.show();                                                                                                     // show the saved toast message
                } else if(lbs <50 || lbs > 300) {
                    Toast failtoast = Toast.makeText(getApplicationContext(), "Profile not saved due to lbs range 50-300, please try again", Toast.LENGTH_LONG);
                    failtoast.show();
                } else {
                    sharedPreferenceHelper.saveProfileGender(gender);                                                                          //Save the profile information into the localstorage
                    sharedPreferenceHelper.saveProfileAge(age);
                    sharedPreferenceHelper.saveProfileWeight(weight);

                    Toast savetoast = Toast.makeText(getApplicationContext(), "Profile Saved", Toast.LENGTH_LONG);                         //create successful save message
                    savetoast.show();                                                                                                      //show the saved toast message
                }
            }

        });

    }

    protected void onStart() { //What to display at onStart when application starts.
        super.onStart();

        Log.d("profile", "onStart() started");
        if (genderEditText.getText() == null) {

        } else {
            Log.d("profile", "entered else");
            genderEditText.setText(sharedPreferenceHelper.getProfileGender());
            ageEditText.setText(sharedPreferenceHelper.getProfileAge());
            weightEditNum.setText(sharedPreferenceHelper.getProfileWeight());
        }
    }
}

