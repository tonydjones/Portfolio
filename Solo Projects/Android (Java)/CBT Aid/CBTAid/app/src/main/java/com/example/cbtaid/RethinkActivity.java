package com.example.cbtaid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RethinkActivity extends MainActivity {
    public Thought thought;
    public LinearLayout emotions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rethink);
        getSupportActionBar().setTitle("Revisit Your Emotions");
        thought = (Thought) getIntent().getSerializableExtra("thought");

        emotions = findViewById(R.id.emotions);
        for (int i = 0; i < thought.emotions.size(); i++){
            LinearLayout line = new LinearLayout(this);
            TextView original = new TextView(this);
            TextView emotion = new TextView(this);
            emotion.setText(thought.emotions.get(i).emotion);
            int strength = thought.emotions.get(i).rating;
            Gson gson = new Gson();
            original.setText(gson.toJson(strength) + " / 10");
            emotion.setTextColor(0xFF000000);
            original.setTextColor(0xFF000000);
            emotion.setGravity(Gravity.CENTER);
            original.setGravity(Gravity.CENTER);
            Spinner rating = new Spinner(this);

            ArrayAdapter<Integer> ratingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList(0,1,2,3,4,5,6,7,8,9,10));
            ratingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            LinearLayout.LayoutParams text = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 3);
            LinearLayout.LayoutParams choice = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            emotion.setLayoutParams(text);
            rating.setLayoutParams(choice);
            original.setLayoutParams(choice);
            rating.setAdapter(ratingAdapter);
            emotions.addView(line);
            line.addView(emotion);
            line.addView(original);
            line.addView(rating);

        }
    }

    public void next(View v){
        for (int i = 0; i < emotions.getChildCount(); i++){
            LinearLayout entry = (LinearLayout) emotions.getChildAt(i);
            Spinner rating = (Spinner) entry.getChildAt(2);
            thought.emotions.get(i).rethink = (Integer) rating.getSelectedItem();
            Log.e(thought.emotions.get(i).emotion, String.valueOf(thought.emotions.get(i).rethink));
        }
        Intent intent = new Intent(getApplicationContext(), SummaryActivity.class);
        intent.putExtra("thought", thought);
        startActivity(intent);
    }


}
