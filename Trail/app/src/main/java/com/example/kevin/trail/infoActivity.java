package com.example.kevin.trail;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
    Toolbar toolbar;

    private sharedPreferenceHelper sharedPreferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        toolbar=(Toolbar)findViewById(R.id.InfoActionBar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Profile");
            toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));

            final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
            upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }

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

                //Convert string input into integer in order to manipulate with integers and operators.
                if (age.length() > 0 && weight.length() > 0 && gender.length() > 0) {
                    int ageint = Integer.parseInt(age);
                    int lbs = Integer.parseInt(weight);
                    if (ageint < 12 || ageint > 99) {
                        Toast failtoast = Toast.makeText(getApplicationContext(), "Profile not saved due to invalid input in the age field, please try again", Toast.LENGTH_LONG);  //Create a save failed message
                        failtoast.show();                                                                                                     // show the saved toast message
                    } else if (lbs < 50 || lbs > 450) {
                        Toast failtoast = Toast.makeText(getApplicationContext(), "Profile not saved due to invalid input in the weight field, please try again", Toast.LENGTH_LONG);
                        failtoast.show();
                    } else {
                        sharedPreferenceHelper.saveProfileGender(gender);                                                                          //Save the profile information into the localstorage
                        sharedPreferenceHelper.saveProfileAge(age);
                        sharedPreferenceHelper.saveProfileWeight(weight);

                        Toast savetoast = Toast.makeText(getApplicationContext(), "Profile Saved", Toast.LENGTH_LONG);                         //create successful save message
                        savetoast.show();

                        goTomainActivity();
                        //show the saved toast message
                    }
                } else {
                    Toast failtoast = Toast.makeText(getApplicationContext(), "Please fill in missing field(s) to proceed", Toast.LENGTH_LONG);
                    failtoast.show();
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

    public void onBackPressed(){
        super.onBackPressed();
        String gender = sharedPreferenceHelper.getProfileGender();
        String weight = sharedPreferenceHelper.getProfileWeight();
        String age = sharedPreferenceHelper.getProfileAge();
        if (gender.length()<0 || weight.length() < 0 || age.length()<0){
        }
    }

    void goTomainActivity() {
        Intent intent = new Intent(infoActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(menuItem);
    }
}