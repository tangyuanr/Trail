package com.example.kevin.trail;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class timerActivity extends AppCompatActivity {

    TextView timerTextViewL;
    TextView recordedTextViewL;
    long startTime= 0;
    long pauseTime= 0;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis/1000);
            int minutes = seconds/60;
            seconds = seconds % 60;

            timerTextViewL.setText(String.format("%d:%02d", minutes, seconds));

            timerHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        timerTextViewL = (TextView) findViewById(R.id.timerTextView);

        Button button = (Button) findViewById(R.id.startB);
        recordedTextViewL = (TextView) findViewById(R.id.recordedTextView);

        button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                Button button = (Button) v;
                if (button.getText().equals("stop")) {
                    timerHandler.removeCallbacks(timerRunnable);
                    recordedTextViewL.setText(timerTextViewL.getText());
                    //timerTextViewL.setText("0:00");
                    pauseTime = System.currentTimeMillis();
                    button.setText("resume");
                } /*else if (button.getText().equals("resume")){
                    pauseTime = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnable,0);
                    //pauseTime = System.currentTimeMillis();
                    button.setText("stop");
                } */else{
                    startTime = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnable,0);
                    button.setText("stop");
                }
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
        //pauseTime = System.currentTimeMillis();

        //timerHandler.removeCallbacks(timerRunnable);
        Button start = (Button) findViewById(R.id.startB);
        start.setText("start");
    }

    public void onResume(){
        super.onResume();

        //timerHandler.post(timerRunnable);
    }
}
