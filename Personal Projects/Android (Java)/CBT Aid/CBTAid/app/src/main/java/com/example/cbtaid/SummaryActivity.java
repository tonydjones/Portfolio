package com.example.cbtaid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class SummaryActivity extends MainActivity {
    public Thought thought;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        getSupportActionBar().setTitle("Dysfunctional Thought Summary");
        thought = (Thought) getIntent().getSerializableExtra("thought");
        LinearLayout summary = findViewById(R.id.summary);
        TextView date = findViewById(R.id.date);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZZZ");
        date.setText(simpleDateFormat.getDateTimeInstance().format(thought.date));
        TextView situation = findViewById(R.id.situation);
        situation.setText(thought.situation);
        situation.setTextColor(0xFF000000);
        date.setTextColor(0xFF000000);
        situation.setGravity(Gravity.CENTER);
        date.setGravity(Gravity.CENTER);
        for (int i = 0; i < thought.emotions.size(); i++) {
            Emotion current = thought.emotions.get(i);
            LinearLayout row1 = new LinearLayout(this);
            TextView column1 = new TextView(this);
            TextView column2 = new TextView(this);
            column1.setText("Emotion");
            column2.setText("Initial Intensity");
            column1.setTextColor(0xFF000000);
            column2.setTextColor(0xFF000000);
            column1.setGravity(Gravity.CENTER);
            column2.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            column2.setLayoutParams(params);
            column1.setLayoutParams(params);
            row1.setGravity(Gravity.CENTER);
            summary.addView(row1);
            row1.addView(column1);
            row1.addView(column2);

            LinearLayout initial = new LinearLayout(this);
            TextView emotion = new TextView(this);
            TextView rating = new TextView(this);
            emotion.setText(current.emotion);
            rating.setText(current.rating + " / 10");
            emotion.setTextColor(0xFF000000);
            rating.setTextColor(0xFF000000);
            emotion.setGravity(Gravity.CENTER);
            rating.setGravity(Gravity.CENTER);
            initial.setGravity(Gravity.CENTER);
            rating.setLayoutParams(params);
            emotion.setLayoutParams(params);
            summary.addView(initial);
            initial.addView(emotion);
            initial.addView(rating);

            LinearLayout.LayoutParams wide = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 4);

            LinearLayout row2 = new LinearLayout(this);
            TextView column3 = new TextView(this);
            TextView column4 = new TextView(this);
            column3.setText("Preceding Belief");
            column3.setTextColor(0xFF000000);
            column4.setText("Strength of Belief");
            column4.setTextColor(0xFF000000);
            column3.setLayoutParams(wide);
            column4.setLayoutParams(params);
            column3.setGravity(Gravity.CENTER);
            column4.setGravity(Gravity.CENTER);
            row2.setGravity(Gravity.CENTER);
            summary.addView(row2);
            row2.addView(column3);
            row2.addView(column4);

            LinearLayout beliefrow = new LinearLayout(this);
            TextView belief = new TextView(this);
            TextView beliefrating = new TextView(this);
            belief.setText(current.belief);
            belief.setTextColor(0xFF000000);
            beliefrating.setText(String.valueOf(current.beliefrating) + " / 10");
            beliefrating.setTextColor(0xFF000000);
            belief.setLayoutParams(wide);
            beliefrating.setLayoutParams(params);
            belief.setGravity(Gravity.CENTER);
            beliefrating.setGravity(Gravity.CENTER);
            beliefrow.setGravity(Gravity.CENTER);
            summary.addView(beliefrow);
            beliefrow.addView(belief);
            beliefrow.addView(beliefrating);

            TextView row3 = new TextView(this);
            row3.setText("Cognitive Errors");
            row3.setTextColor(0xFF000000);
            row3.setGravity(Gravity.CENTER);
            summary.addView(row3);

            List<String> errorlist = new ArrayList<>();
            errorlist.addAll(current.errors);
            for (int j = 0; j < current.errors.size(); j++){
                TextView error = new TextView(this);
                error.setText(errorlist.get(j));
                error.setTextColor(0xFF000000);
                error.setGravity(Gravity.CENTER);
                summary.addView(error);
            }

            List<String> responselist = new ArrayList<>();
            responselist.addAll(current.responses.keySet());

            if (current.responses.size() > 0) {
                LinearLayout row4 = new LinearLayout(this);
                TextView column5 = new TextView(this);
                TextView column6 = new TextView(this);
                if (current.responses.size() > 1) {
                    column5.setText("Rational Responses");
                }
                else {
                    column5.setText("Rational Response");
                }
                column6.setText("Belief in Response");
                column5.setTextColor(0xFF000000);
                column5.setGravity(Gravity.CENTER);
                column5.setLayoutParams(wide);
                column6.setTextColor(0xFF000000);
                column6.setGravity(Gravity.CENTER);
                row4.setGravity(Gravity.CENTER);
                column6.setLayoutParams(params);
                summary.addView(row4);
                row4.addView(column5);
                row4.addView(column6);
            }

            for (int j = 0; j < current.responses.size(); j++){
                LinearLayout line = new LinearLayout(this);
                TextView response = new TextView(this);
                TextView strength = new TextView(this);
                response.setText(responselist.get(j));
                strength.setText(current.responses.get(responselist.get(j)) + " / 10");
                response.setTextColor(0xFF000000);
                response.setGravity(Gravity.CENTER);
                response.setLayoutParams(wide);
                strength.setTextColor(0xFF000000);
                strength.setGravity(Gravity.CENTER);
                line.setGravity(Gravity.CENTER);
                strength.setLayoutParams(params);
                summary.addView(line);
                line.addView(response);
                line.addView(strength);
            }

            LinearLayout row5 = new LinearLayout(this);
            TextView column7 = new TextView(this);
            TextView column8 = new TextView(this);
            column7.setText("Emotion");
            column8.setText("Reevaluated Intensity");
            column7.setTextColor(0xFF000000);
            column8.setTextColor(0xFF000000);
            column7.setGravity(Gravity.CENTER);
            column8.setGravity(Gravity.CENTER);
            row5.setGravity(Gravity.CENTER);
            column8.setLayoutParams(params);
            column7.setLayoutParams(params);
            summary.addView(row5);
            row5.addView(column7);
            row5.addView(column8);

            TextView feeling = new TextView(this);
            feeling.setText(current.emotion);
            feeling.setTextColor(0xFF000000);
            feeling.setGravity(Gravity.CENTER);
            feeling.setLayoutParams(params);
            LinearLayout review = new LinearLayout(this);
            TextView rethink = new TextView(this);
            rethink.setText(String.valueOf(current.rethink) + " / 10");
            rethink.setTextColor(0xFF000000);
            rethink.setGravity(Gravity.CENTER);
            review.setGravity(Gravity.CENTER);
            rethink.setLayoutParams(params);
            summary.addView(review);
            review.addView(feeling);
            review.addView(rethink);

        }
    }

    public void next(View v){
        Log.e("summary", "finished, good job!");
        SharedPreferences pref = getApplicationContext().getSharedPreferences("thoughts", 0);
        Gson gson = new Gson();
        pref.edit().putString(String.valueOf(thought.date), gson.toJson(thought)).apply();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        }
    }

