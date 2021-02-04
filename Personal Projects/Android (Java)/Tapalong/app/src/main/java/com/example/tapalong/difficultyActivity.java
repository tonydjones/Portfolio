package com.example.tapalong;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Dimension;

import java.util.ArrayList;


public class difficultyActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty);
        getSupportActionBar().setTitle("Choose Difficulty");
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
    }

    public void three(View view){
        record(3);
    }

    public void four(View view){
        record(4);
    }

    public void five(View view){
        record(5);
    }

    public void six(View view){
        record(6);
    }

    public void record(int difficulty) {
        Intent intent = new Intent(getApplicationContext(), recordActivity.class);
        intent.putExtra("difficulty", difficulty);
        intent.putExtra("title", getIntent().getStringExtra("title"));
        intent.putExtra("location", getIntent().getStringExtra("location"));
        startActivity(intent);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), songActivity.class);
        startActivity(intent);
    }

}
