package com.example.cbtaid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Welcome to CBT Aid");

    }

    public void input(View v){
        Thought thought = new Thought();
        Log.e("date", thought.date.toString());
        Intent intent = new Intent(getApplicationContext(), SituationActivity.class);
        intent.putExtra("thought", thought);
        startActivity(intent);
    }

    public void review(View v){
        Intent intent = new Intent(getApplicationContext(), ReviewActivity.class);
        startActivity(intent);
    }

}
