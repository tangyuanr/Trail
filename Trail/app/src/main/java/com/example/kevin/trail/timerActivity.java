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
    long resumeTime= 0;
    long totalrecordedMillis = 0;
    long resumeMillis= 0;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            totalrecordedMillis += resumeMillis;
//            System.out.println("total= " + totalrecordedMillis / 1000);
            if (pauseTime == 0) {
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                timerTextViewL.setText(String.format("%d:%02d", minutes, seconds));

                timerHandler.postDelayed(this, 500);
            }else {
                resumeMillis = pauseTime - startTime;
                totalrecordedMillis =resumeMillis;
//                System.out.println("In runnable, pauseTime = " + pauseTime + "");
                long millis = System.currentTimeMillis() + totalrecordedMillis - resumeTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                timerTextViewL.setText(String.format("%d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 500);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        timerTextViewL = (TextView) findViewById(R.id.timerTextView);

        final Button restButton= (Button) findViewById(R.id.restartB);
        Button button = (Button) findViewById(R.id.startB);
        recordedTextViewL = (TextView) findViewById(R.id.recordedTextView);

        button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                Button button = (Button) v;
                if (button.getText().equals("stop")){
                    pauseTime += System.currentTimeMillis();
//                    System.out.println("pauseTimeMillis= " + pauseTime);
//                    System.out.println("timer paused at= " + (pauseTime-startTime)/1000);
                    timerHandler.removeCallbacks(timerRunnable);
                    recordedTextViewL.setText(timerTextViewL.getText());
                    button.setText("resume");
                }  else if (button.getText().equals("resume")){
                    timerHandler.removeCallbacks(timerRunnable);
                    onResume();
                }else if (button.getText().equals("Start")){
//                    System.out.println("CurrentTimemillis= " + System.currentTimeMillis());
                    startTime = System.currentTimeMillis();
//                    System.out.println("startTimemillis= " + startTime);
                    timerHandler.postDelayed(timerRunnable, 0);
                    button.setText("stop");
                }
            }
        });

        restButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Button leftb = (Button) findViewById(R.id.startB);
                timerHandler.removeCallbacks(timerRunnable);
                startTime = System.currentTimeMillis();
                pauseTime = 0;
                resumeTime = 0;
                totalrecordedMillis = 0;
//                System.out.println("startTimeMillis= " + startTime);
                timerHandler.postDelayed(timerRunnable,0);
                leftb.setText("stop");
                recordedTextViewL.setText("0:00");

            }

        });
    }

    @Override
    public void onPause(){
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
        Button start = (Button) findViewById(R.id.startB);
        start.setText("resume");
    }

    public void onResume(){
        super.onResume();
        Button resume = (Button) findViewById(R.id.startB);
        if (resume.getText().equals("resume")) {
            if (resumeTime==0) {
                timerHandler.removeCallbacks(timerRunnable);
                resumeTime = System.currentTimeMillis();
//                System.out.println("CurrenttimeMillis= " + System.currentTimeMillis());
//                System.out.println("resumeTimeMillis= " + resumeTime);
//                System.out.println("pausetimeMillis= " + pauseTime);

                timerHandler.postDelayed(timerRunnable, 0);
//            timerHandler.postAtTime(timerRunnable,resumeTime);
                resume.setText("stop");
            }else {
                timerHandler.removeCallbacks(timerRunnable);
                startTime += resumeTime;
                resumeTime = System.currentTimeMillis();
//                System.out.println("CurrenttimeMillis= " + System.currentTimeMillis());
//                System.out.println("resumeTimeMillis= " + resumeTime);
//                System.out.println("pausetimeMillis= " + pauseTime);

                timerHandler.postDelayed(timerRunnable, 0);
//            timerHandler.postAtTime(timerRunnable,resumeTime);
                resume.setText("stop");

            }
        }
    }
}
