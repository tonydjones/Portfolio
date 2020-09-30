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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Long.compare;

public class CalibrateActivity extends MainActivity {
    public float height;
    public float width;
    public int difficulty;
    public List<List<Integer>> recording;
    public String location;
    public List<Integer> info;
    public Gson gson = new Gson();
    public List<List<Integer>> buttonstream;
    public float tapheight;
    public float moveY;
    public LinearLayout.LayoutParams tap;
    public LinearLayout.LayoutParams tapmargin;
    public LinearInterpolator interpolator;
    public Button[] buttons;
    public Animation[] animations;
    public int[] timing;
    public RelativeLayout[] lanes;
    public long start;
    public List<Long> calibration;
    public List<Integer> original;
    public Handler handler;
    public Handler activator;
    public Handler main;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        height = displaymetrics.heightPixels;
        width = displaymetrics.widthPixels;
        moveY = height * 11 / 10;
        tapheight = height / 10;
        interpolator = new LinearInterpolator();
        tap = new LinearLayout.LayoutParams((int) (width / difficulty), (int) tapheight);
        tapmargin = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("cal", 0);
        String data = pref.getString("setup", null);
        if (data == null){
            Toast.makeText(this,"Calibration data not found", Toast.LENGTH_SHORT);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
        recording = gson.fromJson(data, new TypeToken<List<List<Integer>>>(){}.getType());
        info = recording.get(0);
        location = getIntent().getStringExtra("location");
        difficulty = info.get(1);
        tap = new LinearLayout.LayoutParams((int) (width / difficulty), (int) tapheight);
        getSupportActionBar().hide();
        buttonstream = buttonstream(recording);
        LinearLayout ll2 = findViewById(R.id.game_view);
        calibration = new LinkedList<>();
        for (int i = 1; i < difficulty + 1; i++) {
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
            Button button = new Button(this);
            button.setLayoutParams(p);
            final RelativeLayout view = new RelativeLayout(this);
            view.setLayoutParams(tapmargin);
            view.setId(i);
            view.setBackgroundColor(0x00FFFFFF);
            ll2.addView(view);
            LinearLayout rl = findViewById(R.id.button_view);
            button.setTag(0x81000000, false);
            button.setTag(0x82000000, false);
            button.setId(i + difficulty);
            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(final View v, final MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        calibration.add(SystemClock.elapsedRealtime());
                        view.setBackgroundColor(0x7D00FF00);
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        view.setBackgroundColor(0x00000000);
                    }
                    return false;
                }
            });
            rl.addView(button);
        }

        //generate all buttons and animations here.
        buttons = new Button[buttonstream.size()];
        animations = new Animation[buttonstream.size()];
        timing = new int[buttonstream.size()];
        lanes = new RelativeLayout[buttonstream.size()];

        for (int i = 1; i < buttonstream.size(); i++) {
            if (buttonstream.get(i).size() == 3) {
                generate(buttonstream.get(i).get(0), i);
                timing[i] = buttonstream.get(i).get(2);
            }
        }

        handler = new Handler();

    }

    public List clone(List thing) {
        List clone = new ArrayList();
        for (int i = 0; i < thing.size(); i++) {
            if (thing.get(i) instanceof List) {
                clone.add(clone((List) thing.get(i)));
            } else {
                clone.add(thing.get(i));
            }
        }
        return clone;
    }

    public List<List<Integer>> buttonstream(List<List<Integer>> thing) {
        original = new ArrayList<>();
        List<List<Integer>> product = clone(thing);
        for (int i = 1; i < product.size(); i++) {
            if (product.get(i).get(1) == 1) {
                for (int j = i + 1; j < product.size(); j++) {
                    if (product.get(j).get(0).equals(product.get(i).get(0))) {
                        product.remove(j);
                        original.add(product.get(i).get(2));
                        break;
                    }
                }
            }
        }
        return product;
    }

    @Override
    public void onWindowFocusChanged(boolean hasfocus) {
        super.onWindowFocusChanged(hasfocus);
        if (hasfocus) {
            setup();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    process();
                }
            }, 18000);
        }
    }


    public void setup(){
        start = SystemClock.elapsedRealtime();
        for (int i = 1; i < timing.length; i++){
            long diff = Math.subtractExact(SystemClock.elapsedRealtime(), start);
            handler.postDelayed(animate(i), timing[i] - diff);
        }
    }

    public Runnable animate(final int i){
        return new Runnable() {
            @Override
            public void run() {
                buttons[i].startAnimation(animations[i]);
            }
        };
    }



    public void generate(Integer data, int i) {
        final Button button = new Button(this);
        button.setLayoutParams(tap);
        button.setY(-1 * tapheight);
        final RelativeLayout mylane = findViewById(data);
        Animation move = new TranslateAnimation(0, 0, 0, moveY);
        move.setDuration(1980);
        move.setInterpolator(interpolator);
        move.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mylane.removeView(button);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        buttons[i] = button;
        animations[i] = move;
        lanes[i] = mylane;

        lanes[i].addView(buttons[i]);
    }


    public void process() {
        List<Integer> calibrate = new ArrayList<>();
        for (int i = 0; i < calibration.size(); i++){
            calibrate.add((int) Math.subtractExact(calibration.get(i), start));
        }
        if (calibrate.size() == original.size()){
            int pause = 0;
            for (int i = 1; i < calibrate.size(); i++){
                pause += (calibrate.get(i) - original.get(i));
            }
            Log.e("original", String.valueOf(original));
            Log.e("calibrate", String.valueOf(calibrate));
            pause = 1800 - (pause / calibrate.size());
            SharedPreferences pref = getApplicationContext().getSharedPreferences("cal", 0);
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt("calibration", pause);
            editor.apply();
            Log.e("pause", String.valueOf(pause));
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "Incorrect number of calibration inputs.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), CalibrateActivity.class);
            startActivity(intent);
        }


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
