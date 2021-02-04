package com.example.tapalong;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Long.compare;


public class recordActivity extends MainActivity {
    public Integer difficulty;
    public List<Long> recording;
    public String location;
    public MediaPlayer song;
    public TextView countdown;
    public LinearLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        getSupportActionBar().hide();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        difficulty = getIntent().getIntExtra("difficulty", 3);
        location = getIntent().getStringExtra("location");
        ll = findViewById(R.id.button_view);
        recording = new LinkedList<>();
        for (int i = 1; i < difficulty + 1; i++){
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
            final Button button = new Button(this);
            button.setLayoutParams(p);
            final RelativeLayout view = new RelativeLayout(this);
            view.setLayoutParams(p);
            view.setId(i+difficulty);
            final int finalI = i;
            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        recording.add(SystemClock.elapsedRealtime());
                        recording.add((long) finalI);
                        recording.add((long) 1);
                        view.setBackgroundColor(0x7D00FF00);
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        recording.add(SystemClock.elapsedRealtime());
                        recording.add((long) finalI);
                        recording.add((long) 0);
                        view.setBackgroundColor(0x00000000);
                    }
                    return true;
                }
            });
            button.setEnabled(false);
            button.setId(i);
            ll.addView(button);
            LinearLayout flash = findViewById(R.id.flash_view);
            flash.addView(view);
        }
        song = MediaPlayer.create(this, Uri.parse(location));
        song.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.release();
                process(recording);
            }
        });
        countdown = findViewById(R.id.countdown_view);
        countdown.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/3);
        countdown.setText("3");

    }

    @Override
    public void onWindowFocusChanged(boolean hasfocus) {
        super.onWindowFocusChanged(hasfocus);
        if(hasfocus) {
            Handler handler = new Handler();
            Runnable count = new Runnable() {
                @Override
                public void run() {
                    if (countdown.getText().equals("3")){
                        countdown.setText("2");
                    }
                    else if (countdown.getText().equals("2")){
                        countdown.setText("1");
                    }
                    else if (countdown.getText().equals("1")){
                        countdown.setText("GO");
                        game();
                    }
                    else if (countdown.getText().equals("GO")){
                        countdown.setText("");
                    }
                }
            };
            handler.postDelayed(count, 1000);
            handler.postDelayed(count, 2000);
            handler.postDelayed(count, 3000);
            handler.postDelayed(count, 4000);
            }

        }


    public void game(){
        long start = SystemClock.elapsedRealtime();
        recording.add(start);
        for (int i = 1; i < difficulty + 1; i ++){
            findViewById(i).setEnabled(true);
        }
        song.start();
    }


    @Override
    protected void onStop(){
        super.onStop();
        try {
            song.stop();
            song.release();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void process(List<Long> data){
        long init = data.get(0);
        List<List> product = new ArrayList<>();
        product.add(new ArrayList<Integer>());
        product.get(0).add(0);
        product.get(0).add(difficulty);
        product.get(0).add(0);
        product.get(0).add(0);
        for (int i = 1; i < data.size(); i = i + 3 ){
            List<Integer> tmp = new ArrayList<>();
            tmp.add(data.get(i + 1).intValue());
            tmp.add(data.get(i + 2).intValue());
            tmp.add((int) Math.subtractExact(data.get(i), init));
            product.add(tmp);
        }
        Gson gson = new Gson();
        String json = gson.toJson(product);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("pref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(location, json);
        editor.apply();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), songActivity.class);
        startActivity(intent);
    }

}
