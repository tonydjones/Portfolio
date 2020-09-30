package com.example.tapalong;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class choiceActivity extends MainActivity {
    public String title;
    public String location;
    public String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);
        getSupportActionBar().setTitle("Song Statistics");
        title = getIntent().getStringExtra("title");
        data = getIntent().getStringExtra("data");
        location = getIntent().getStringExtra("location");
        Gson gson = new Gson();
        List<List<Integer>> info = gson.fromJson(data, new TypeToken<List<List<Integer>>>(){}.getType());
        List<Integer> stats = info.get(0);
        int difficulty = stats.get(1);
        int score = stats.get(2);
        int combo = stats.get(3);
        TextView title_view = findViewById(R.id.title);
        TextView score_view = findViewById(R.id.score);
        TextView combo_view = findViewById(R.id.combo);
        TextView difficulty_view = findViewById(R.id.difficulty);
        title_view.setText(title);
        score_view.setText("Top Score\n" + score);
        combo_view.setText("Highest Combo\n" + combo);
        difficulty_view.setText("Difficulty\n" + difficulty);
    }

    public void play(View view){
        Intent intent = new Intent(getApplicationContext(), playActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("data", data);
        intent.putExtra("location", location);

        startActivity(intent);
    }

    public void delete(View view){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("pref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(location);
        editor.apply();

        Intent intent = new Intent(getApplicationContext(), tapActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), tapActivity.class);
        startActivity(intent);
    }
}
