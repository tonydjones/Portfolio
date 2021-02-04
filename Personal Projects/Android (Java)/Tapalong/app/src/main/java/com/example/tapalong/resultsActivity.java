package com.example.tapalong;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class resultsActivity extends MainActivity{
    public String title;
    public String location;
    public String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        getSupportActionBar().setTitle("Results");
        title = getIntent().getStringExtra("title");
        data = getIntent().getStringExtra("data");
        location = getIntent().getStringExtra("location");
        int score = getIntent().getIntExtra("score", 0);
        int combo = getIntent().getIntExtra("combo", 0);
        Gson gson = new Gson();
        List<List<Integer>> info = gson.fromJson(data, new TypeToken<List<List<Integer>>>(){}.getType());
        List<Integer> stats = info.get(0);
        int topscore = stats.get(2);
        int topcombo = stats.get(3);
        TextView title_view = findViewById(R.id.title);
        TextView score_view = findViewById(R.id.score);
        TextView combo_view = findViewById(R.id.combo);
        title_view.setText(title);
        if (score == topscore){
            score_view.setText("New High Score!\n" + score);
        }
        else {
            score_view.setText("Score\n" + score);
        }
        if (combo == topcombo){
            combo_view.setText("New Combo Record!\n" + combo);
        }
        else {
            combo_view.setText("Highest Combo\n" + combo);
        }
    }

    public void play(View view){
        Intent intent = new Intent(getApplicationContext(), playActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("data", data);
        intent.putExtra("location", location);

        startActivity(intent);
    }

    public void taps(View view){
        Intent intent = new Intent(getApplicationContext(), tapActivity.class);
        startActivity(intent);
    }

    public void home(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), tapActivity.class);
        startActivity(intent);
    }
}

