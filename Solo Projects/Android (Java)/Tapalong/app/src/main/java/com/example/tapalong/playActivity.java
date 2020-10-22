package com.example.tapalong;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static java.lang.Long.compare;

public class playActivity extends MainActivity {
    //Public variables, only if required for multiple functions
    public float height;
    public int difficulty;
    public List<List<Integer>> recording;
    public String location;
    public MediaPlayer song;
    public List<Integer> info;
    public int currentscore;
    public int currentcombo;
    public Gson gson = new Gson();
    public List<List<Integer>> buttonstream;
    public List<List<Integer>> buttonmap;
    public float tapheight;
    public float moveY;
    public LinearLayout.LayoutParams tap;
    public LinearLayout.LayoutParams tapmargin;
    public int maxcombo;
    public String newdata;
    public LinearInterpolator interpolator;
    public Button[] buttons;
    public Animation[] animations;
    public int[] timing;
    public RelativeLayout[] lanes;
    public int[][] switches;
    public Button[] inputs;
    public RelativeLayout[] views;
    public int adjusted;
    public Handler handler;
    public Handler activator;
    public Handler main;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //view setup, default layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        currentcombo = 0;
        currentscore = 0;
        getSupportActionBar().setTitle("Score: " + currentscore);
        getSupportActionBar().setSubtitle("Combo: " + currentcombo);

        //getting screen height
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        height = displaymetrics.heightPixels;

        //getting standard animation distance
        moveY = height * 11 / 10;
        interpolator = new LinearInterpolator();

        //setting up standard tap size
        tapheight = height / 10;
        tap = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) tapheight);

        //setting up lane parameters
        tapmargin = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);

        //getting song data
        String data = getIntent().getStringExtra("data");
        recording = gson.fromJson(data, new TypeToken<List<List<Integer>>>(){}.getType());
        location = getIntent().getStringExtra("location");

        //getting miscellaneous info (difficulty and current top score/combo
        info = recording.get(0);
        difficulty = info.get(1);

        //getting calibration calculation
        int calibrate = getApplicationContext().getSharedPreferences("cal", 0).getInt("calibration",0);
        adjusted = 1800 - calibrate;

        //Processing song data for activators and animations
        buttonmap = buttonmap(recording);
        buttonstream = buttonstream(recording);

        //setting up views and arrays for buttons and lanes
        LinearLayout rl = findViewById(R.id.button_view);
        LinearLayout ll2 = findViewById(R.id.game_view);
        inputs = new Button[difficulty];
        views = new RelativeLayout[difficulty];

        //making multiple lanes and buttons
        for (int i = 1; i < difficulty + 1; i++){
            final int finalI = i;

            //Making lanes
            final RelativeLayout view = new RelativeLayout(this);
            view.setLayoutParams(tapmargin);
            view.setId(i);
            view.setBackgroundColor(0x00FFFFFF);
            ll2.addView(view);
            views[i-1] = view;

            //making inputs
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
            Button button = new Button(this);
            button.setLayoutParams(p);
            button.setTag(0x81000000, false);
            button.setTag(0x82000000,false);
            button.setId(i + difficulty);
            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(final View v, final MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if ((boolean) v.getTag(0x81000000)) {
                            v.setTag(0x81000000, false);
                            currentcombo += 1;
                            currentscore += 10 * currentcombo;
                            view.setBackgroundColor(0x7D00FF00);
                            getSupportActionBar().setTitle("Score: " + currentscore);
                            getSupportActionBar().setSubtitle("Combo: " + currentcombo);
                            if ((boolean) v.getTag(0x82000000)) {
                                main.postDelayed(points(v, event, view), 100);
                            }
                        }
                        else {
                            if (currentcombo > maxcombo) {
                                maxcombo = currentcombo;
                            }
                            currentcombo = 0;
                            view.setBackgroundColor(0x7DFF0000);
                            getSupportActionBar().setSubtitle("Combo: " + currentcombo);
                        }
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        v.setTag(0x82000000, false);
                        view.setBackgroundColor(0x00FFFFFF);
                    }
                    return false;
                }
            });
            rl.addView(button);
            inputs[i-1] = button;

        }

        //generate all prompts and animations here.
        buttons = new Button[buttonstream.size()];
        animations = new Animation[buttonstream.size()];
        timing = new int[buttonstream.size()];
        lanes = new RelativeLayout[buttonstream.size()];

        for (int i = 1; i < buttonstream.size(); i++){
            if (buttonstream.get(i).size() == 3){
                generate(buttonstream.get(i).get(0), i);
                timing [i] = buttonstream.get(i).get(2);
            }
            else{
                generatehold(buttonstream.get(i), i);
                timing [i] = buttonstream.get(i).get(2);
            }
        }

        //generate activator array
        switches = new int[buttonmap.size()][4];
        for (int i = 0; i < buttonmap.size(); i++){
            for (int j = 0; j < 4; j++){
                switches[i][j] = buttonmap.get(i).get(j);
            }
        }

        //Set up the song
        song = MediaPlayer.create(this, Uri.parse(location));
        song.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.release();
                int update = 0;
                int score = info.get(2);
                int combo = info.get(3);
                if (currentscore > score){
                    info.set(2, currentscore);
                    update++;
                }
                if (currentcombo > maxcombo){
                    maxcombo = currentcombo;
                }
                if (maxcombo > combo){
                    info.set(3, maxcombo);
                    update++;
                }
                if (update > 0){
                    newdata = process();
                }
                else{
                    newdata = getIntent().getStringExtra("data");
                }
                Intent intent = new Intent(getApplicationContext(), resultsActivity.class);
                intent.putExtra("title", getIntent().getStringExtra("title"));
                intent.putExtra("data", newdata);
                intent.putExtra("location", location);
                intent.putExtra("score", currentscore);
                intent.putExtra("combo", maxcombo);

                startActivity(intent);
            }
        });

        //set up handlers
        main = new Handler();
        activator = new Handler();
        handler = new Handler();

    }

    //After preparations made, window changes to start game. Begin thread management here.
    @Override
    public void onWindowFocusChanged(boolean hasfocus) {
        super.onWindowFocusChanged(hasfocus);
        if (hasfocus) {

            main.postDelayed(play(), adjusted);
            setup();

        }
    }

    public void setup(){
        long start = SystemClock.elapsedRealtime();
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

    public Runnable play(){
        return new Runnable() {
            @Override
            public void run() {
                song.start();
                activators();

            }
        };
    }

    //This thread manages the user inputs, determines when inputs are allowed
    public void activators() {
        long init = SystemClock.elapsedRealtime();
        for (int i = 1; i < switches.length; i++) {
            if (switches[i][1] == 1) {
                int tmp = switches[i][2];
                Button button = inputs[switches[i][0] - 1];
                if (switches[i][3] == 1){
                    long diff = Math.subtractExact(SystemClock.elapsedRealtime(), init);
                    activator.postDelayed(togglehold(button), tmp - diff);
                }
                else {
                    long diff = Math.subtractExact(SystemClock.elapsedRealtime(), init);
                    activator.postDelayed(toggleon(button), tmp - diff);
                }
            }
            else{
                int tmp = switches[i][2];
                Button button = inputs[switches[i][0] - 1];
                int toggle = switches[i][0] - 1;
                long diff = Math.subtractExact(SystemClock.elapsedRealtime(), init);
                activator.postDelayed(toggleoff(button, toggle), tmp - diff);
            }
        }
    }

    public Runnable toggleon(final Button button){
        return new Runnable() {
            @Override
            public void run() {
                button.setTag(0x81000000, true);
            }
        };
    }

    public Runnable togglehold(final Button button){
        return new Runnable() {
            @Override
            public void run() {
                button.setTag(0x81000000, true);
                button.setTag(0x82000000, true);
            }
        };
    }

    //deactivates inputs, checks if button was pressed at the right time
    public Runnable toggleoff(final Button button, final int i){
        return new Runnable() {
            @Override
            public void run() {
                if ((boolean) button.getTag(0x81000000)){
                    if (currentcombo > maxcombo){
                        maxcombo = currentcombo;
                    }
                    currentcombo = 0;
                    button.setTag(0x81000000, false);
                    button.setTag(0x82000000, false);
                    RelativeLayout view = views[i];
                    view.setBackgroundColor(0x7DFF0000);
                    getSupportActionBar().setSubtitle("Combo: " + currentcombo);
                    activator.postDelayed(reset(view) , 125);
                }
                else{
                    button.setTag(0x82000000, false);
                    views[i].setBackgroundColor(0x00000000);
                }
            }
        };
    }

    public Runnable reset(final View view){
        return new Runnable() {
            @Override
            public void run() {
                view.setBackgroundColor(0x00000000);
            }
        };
    }

    //Saves data to shared preferences if achieved a new high score or combo record
    public String process(){
        recording.set(0, info);
        String json = gson.toJson(recording);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("pref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(location, json);
        editor.apply();
        return json;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //HELPER FUNCTIONS
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //This function produces a runnable that will recursively add points during a held note, every 100 ms
    public Runnable points(final View button, final MotionEvent event, final View view){
        return new Runnable() {
            @Override
            public void run() {
                if ((boolean) button.getTag(0x82000000) && event.getAction() == MotionEvent.ACTION_DOWN){
                    currentscore += currentcombo;
                    getSupportActionBar().setTitle("Score: " + currentscore);
                    main.postDelayed(points(button, event, view), 100);
                }
                else{
                    view.setBackgroundColor(0x00FFFFFF);
                }
            }
        };
    }


    //for cloning lists without affecting the original contents
    public List clone(List thing){
        List clone = new ArrayList();
        for (int i = 0; i < thing.size(); i++){
            if (thing.get(i) instanceof List){
                clone.add(clone((List) thing.get(i)));
            }
            else{
                clone.add(thing.get(i));
            }
        }
        return clone;
    }

    //for generating the lists to create prompts and animations
    public List<List<Integer>> buttonstream(List<List<Integer>> thing){
        List<List<Integer>> product = clone(thing);
        for (int i = 1; i < product.size(); i++){
            if (product.get(i).get(1) == 1){
                int press = product.get(i).get(2);
                for (int j = i + 1; j < product.size(); j++){
                    if (product.get(j).get(0).equals(product.get(i).get(0))){
                        int release = product.get(j).get(2);
                        if (release - press > 500){
                            product.get(i).add(release - press);
                        }
                        product.remove(j);
                        break;
                    }
                }
            }
        }
        return product;
    }

    //generates the activators list
    public List<List<Integer>> buttonmap(List<List<Integer>> thing){
        List<List<Integer>> product = clone(thing);
        for (int i = 1; i < product.size(); i++){
            if (product.get(i).get(1) == 1){
                int press = product.get(i).get(2);
                for (int j = i + 1; j < product.size(); j++){
                    if (product.get(j).get(0).equals(product.get(i).get(0))){
                        int release = product.get(j).get(2);
                        product.get(i).set(2, press - 225);
                        if (release - press < 500){
                            product.get(j).set(2, press + 225);
                            product.get(i).add(0);
                        }
                        else{
                            product.get(i).add(1);
                        }
                        product.get(j).add(0);
                        break;
                    }
                }
            }
        }
        for (int i = 1; i < product.size(); i++){
            if (product.get(i).get(1) == 0){
                int release = product.get(i).get(2);
                for (int j = i + 1; j < product.size(); j++){
                    if (product.get(j).get(0).equals(product.get(i).get(0))){
                        int press = product.get(j).get(2);
                        if (press < release){
                            int avg = (press + release) / 2;
                            product.get(j).set(2, avg);
                            product.get(i).set(2, avg + 1);
                        }
                        break;
                    }
                }
            }
        }
        return product;
    }


    //Stops song when app stops
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

    //produces prompts and adds them to proper lane above the screen
    public void generate(Integer data, int i){
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

    //produces long notes and adds them out of sight
    public void generatehold(List<Integer> data, int i){
        final Button button = new Button(this);
        float multiplier = (tapheight * data.get(3) / 180);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) multiplier);
        button.setLayoutParams(p);
        button.setY(-1 * multiplier);
        final RelativeLayout mylane = findViewById(data.get(0));
        Animation holdmove = new TranslateAnimation(0, 0, 0, (height + multiplier));
        holdmove.setDuration((long) ((multiplier / tapheight) * 180) + 1800);
        holdmove.setInterpolator(interpolator);
        holdmove.setAnimationListener(new Animation.AnimationListener() {
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
        animations[i] = holdmove;
        lanes[i] = mylane;

        lanes[i].addView(buttons[i]);
    }

    //pause a thread. try to replace with something better
    public void delayAction(long milliseconds){
        long a = SystemClock.elapsedRealtime();
        long[] arr = {0};
        arr[0] = Math.subtractExact(SystemClock.elapsedRealtime(), a);
        while(compare(arr[0], milliseconds) < 0){
            arr[0] = Math.subtractExact(SystemClock.elapsedRealtime(), a);
        }
    }

    //Pauses a thread until past a certain point since an initial timepoint
    public int timer(long start){
        return  (int) Math.subtractExact(SystemClock.elapsedRealtime(), start);
    }


    //pressing the back button takes you back to the choice screen to play or delete the tap
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), choiceActivity.class);
        intent.putExtra("title", getIntent().getStringExtra("title"));
        intent.putExtra("data", getIntent().getStringExtra("data"));
        intent.putExtra("location", location);

        startActivity(intent);
    }

}
