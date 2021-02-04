package com.example.tapalong;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Long.compare;

public class SetupActivity extends MainActivity {
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
        difficulty = 1;
        try {
            AssetFileDescriptor afd = getAssets().openFd("metronome15.mp3");
            song = new MediaPlayer();
            song.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
        } catch (IOException e) {
            Toast.makeText(this, "Calibration file not found", Toast.LENGTH_SHORT);
            Intent intent = new Intent (getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
        ll = findViewById(R.id.button_view);
        recording = new LinkedList<>();
        for (int i = 1; i < difficulty + 1; i++){
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
            final Button button = new Button(this);
            button.setLayoutParams(p);
            final int finalI = i;
            final RelativeLayout view = new RelativeLayout(this);
            view.setLayoutParams(p);
            view.setId(i+difficulty);
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
        song.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.release();
                process(recording);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(), CalibrateActivity.class);
                        startActivity(intent);
                    }
                }, 1000);
            }
        });
        try {
            song.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        SharedPreferences pref = getApplicationContext().getSharedPreferences("cal", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("setup", json);
        editor.apply();
    }

}
