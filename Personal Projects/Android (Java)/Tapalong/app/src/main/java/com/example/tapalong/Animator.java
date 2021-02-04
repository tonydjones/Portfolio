package com.example.tapalong;

import android.os.SystemClock;
import android.util.Log;
import android.view.animation.Animation;
import android.widget.Button;

public class Animator implements Runnable {
    public Button button;
    public Animation animation;
    public long start;

    Animator(Button button, Animation animation, long start){
        this.button = button;
        this.animation = animation;
        this.start = start;

    }

    @Override
    public void run() {
        button.startAnimation(animation);
    }
}
