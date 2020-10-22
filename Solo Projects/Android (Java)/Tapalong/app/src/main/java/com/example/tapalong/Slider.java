package com.example.tapalong;

import android.view.animation.Animation;
import android.widget.Button;

public class Slider {
    public Button button;
    public Animation animation;

    Slider(Button button, Animation animation){
        this.button = button;
        this.animation = animation;
    }
}
