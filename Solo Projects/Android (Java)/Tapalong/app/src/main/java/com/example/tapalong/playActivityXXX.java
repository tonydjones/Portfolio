package com.example.tapalong;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
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

import java.util.ArrayList;
import java.util.List;

import static java.lang.Long.compare;

public class playActivityXXX extends MainActivity {
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        height = displaymetrics.heightPixels;
        moveY = height * 12 / 10;
        tapheight = height / 10;
        interpolator = new LinearInterpolator();
        tap = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) tapheight);
        tapmargin = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        String data = getIntent().getStringExtra("data");
        recording = gson.fromJson(data, new TypeToken<List<List<Integer>>>(){}.getType());
        info = recording.get(0);
        location = getIntent().getStringExtra("location");
        difficulty = info.get(1);
        currentcombo = 0;
        currentscore = 0;
        getSupportActionBar().setTitle("Score: " + currentscore);
        getSupportActionBar().setSubtitle("Combo: " + currentcombo);
        buttonmap = buttonmap(recording);
        buttonstream = buttonstream(recording);
        LinearLayout ll2 = findViewById(R.id.game_view);
        for (int i = 1; i < difficulty + 1; i++){
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
            Button button = new Button(this);
            button.setLayoutParams(p);
            RelativeLayout view = new RelativeLayout(this);
            view.setLayoutParams(tapmargin);
            view.setId(i);
            view.setBackgroundColor(0x00FFFFFF);
            ll2.addView(view);
            final int finalI = i;
            LinearLayout rl = findViewById(R.id.button_view);
            button.setTag(0x81000000, false);
            button.setTag(0x82000000,false);
            button.setId(i + difficulty);
            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(final View v, final MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN){
                        if ((boolean) v.getTag(0x81000000)){
                            right(finalI);
                        }
                        else{
                            wrong(finalI);
                        }
                        if ((boolean) v.getTag(0x82000000)) {
                            new Thread() {
                                @Override
                                public void run() {
                                    while ((boolean) v.getTag(0x82000000) && event.getAction() == MotionEvent.ACTION_DOWN) {
                                        currentscore += currentcombo;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                getSupportActionBar().setTitle("Score: " + currentscore);
                                            }
                                        });
                                        delayAction(100);
                                    }
                                    findViewById(finalI).setBackgroundColor(0x00FFFFFF);
                                }
                            }.start();
                        }
                        return true;
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        v.setTag(0x82000000, false);
                        findViewById(finalI).setBackgroundColor(0x00FFFFFF);
                    }
                    return false;
                }
            });
            rl.addView(button);
        }
        song = MediaPlayer.create(this, Uri.parse(location));
        song.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
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
    }

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

    public List<List<Integer>> buttonmap(List<List<Integer>> thing){
        List<List<Integer>> product = clone(thing);
        for (int i = 1; i < product.size(); i++){
            if (product.get(i).get(1) == 1){
                int press = product.get(i).get(2);
                for (int j = i + 1; j < product.size(); j++){
                    if (product.get(j).get(0).equals(product.get(i).get(0))){
                        int release = product.get(j).get(2);
                        product.get(i).set(2, press - 175);
                        if (release - press < 500){
                            product.get(j).set(2, press + 175);
                            product.get(i).add(0);
                        }
                        else{
                            product.get(i).add(1);
                        }
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

    @Override
    public void onWindowFocusChanged(boolean hasfocus) {
        super.onWindowFocusChanged(hasfocus);
        if (hasfocus) {
            new Thread() {
                @Override
                public void run() {
                    new Thread() {
                        @Override
                        public void run() {
                            delayAction(2500);
                            game();
                        }
                    }.start();
                    stream();
                }
            }.start();
        }
    }

    public void toggleoff(final int i){
        if ((boolean) findViewById(i).getTag(0x81000000)){
            wrong(i - difficulty);
            findViewById(i).setTag(0x81000000, false);
            new Thread(){
                @Override
                public void run(){
                    delayAction(250);
                    findViewById(i - difficulty).setBackgroundColor(0x00000000);
                }
            }.start();
        }
        findViewById(i).setTag(0x82000000, false);
    }

    public void toggleon(int i){
        findViewById(i).setTag(0x81000000, true);
    }

    public void togglehold(int i){
        findViewById(i).setTag(0x81000000, true);
        findViewById(i).setTag(0x82000000, true);
    }

    public void stream(){
        long start = SystemClock.elapsedRealtime();
        for (int i = 1; i < buttonstream.size(); i++){
            int tmp = buttonstream.get(i).get(2);
            if (buttonstream.get(i).size() == 3){
                final int finalI1 = i;
                while (timer(start) < tmp){
                }
                generate(buttonstream.get(finalI1).get(0));
            }
            else{
                final int finalI = i;
                while (timer(start) < tmp){
                }
                generatehold(buttonstream.get(finalI));
            }


        }

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

    public void generate(Integer data){
        final Button button = new Button(this);
        button.setLayoutParams(tap);
        button.setY(-1 * tapheight);
        final FrameLayout mylane = findViewById(data);
        final Animation move = new TranslateAnimation(0, 0, 0, moveY);
        move.setDuration(3000);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mylane.addView(button);
                button.startAnimation(move);
            }
        });
    }

    public void generatehold(List<Integer> data){
        final Button button = new Button(this);
        int multiplier = (int) (tapheight * data.get(3) / 250);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, multiplier);
        button.setLayoutParams(p);
        final RelativeLayout mylane = findViewById(data.get(0));
        final RelativeLayout newlane = new RelativeLayout(this);
        newlane.setBackgroundColor(0x00000000);
        LinearLayout.LayoutParams q = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        q.setMargins(0, -1 * multiplier,0,0);
        newlane.setLayoutParams(q);
        final Animation holdmove = new TranslateAnimation(0, 0, 0, (height * 11 / 10) + multiplier);
        holdmove.setDuration((long) ((multiplier / tapheight) * 250) + 2750);
        holdmove.setInterpolator(interpolator);
        holdmove.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mylane.removeView(newlane);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mylane.addView(newlane);
                newlane.addView(button);
                button.startAnimation(holdmove);
            }
        });

    }

    public void game() {
        song.start();
        long start = SystemClock.elapsedRealtime();
        for (int i = 1; i < buttonmap.size(); i++) {
            int tmp = buttonmap.get(i).get(2);
            int toggle = buttonmap.get(i).get(0) + difficulty;
            if (buttonmap.get(i).get(1) == 1) {
                if (buttonmap.get(i).get(3) == 1){
                    while (timer(start) < tmp) {
                    }
                    togglehold(toggle);
                }
                else {
                    while (timer(start) < tmp) {
                    }
                    toggleon(toggle);
                }
            }
            else{
                while (timer(start) < tmp) {
                }
                toggleoff(toggle);
            }
        }
    }

    public void delayAction(long milliseconds){
        long a = SystemClock.elapsedRealtime();
        long[] arr = {0};
        arr[0] = Math.subtractExact(SystemClock.elapsedRealtime(), a);
        while(compare(arr[0], milliseconds) < 0){
            arr[0] = Math.subtractExact(SystemClock.elapsedRealtime(), a);
        }
    }

    public String process(){
        recording.set(0, info);
        String json = gson.toJson(recording);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("pref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(location, json);
        editor.apply();
        return json;
    }

    public int timer(long start){
        return  (int) Math.subtractExact(SystemClock.elapsedRealtime(), start);
    }

    public void wrong(int i){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSupportActionBar().setSubtitle("Combo: " + 0);
            }
        });
        findViewById(i).setBackgroundColor(0x7DFF0000);
        if (currentcombo > maxcombo){
            maxcombo = currentcombo;
        }
        currentcombo = 0;
    }

    public void right(int i){
        findViewById(i).setBackgroundColor(0x7D00FF00);
        findViewById(i + difficulty).setTag(0x81000000, false);
        currentcombo += 1;
        currentscore += 10 * currentcombo;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSupportActionBar().setTitle("Score: " + currentscore);
                getSupportActionBar().setSubtitle("Combo: " + currentcombo);
            }
        });
    }


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
