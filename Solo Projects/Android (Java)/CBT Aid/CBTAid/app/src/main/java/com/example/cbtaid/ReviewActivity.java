package com.example.cbtaid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ReviewActivity extends MainActivity {
    public SharedPreferences pref;
    public List<Thought> entries;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        getSupportActionBar().setTitle("Previous Entries");
        pref = getApplicationContext().getSharedPreferences("thoughts", 0);
        entries = new ArrayList<>();
        load(entries, pref);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZZZ");
        LinearLayout thoughts = findViewById(R.id.thoughts);
        for (int i = 0; i < entries.size(); i++){
            Button text = new Button(this);
            text.setText(simpleDateFormat.getDateTimeInstance().format(entries.get(i).date));
            final int finalI = i;
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), RecapActivity.class);
                    intent.putExtra("thought", entries.get(finalI));
                    startActivity(intent);
                }
            });
            thoughts.addView(text);
        }
    }

    public void load(List entries, SharedPreferences pref) {
        Gson gson = new Gson();
        Map<String, String> preferences = (Map<String, String>) pref.getAll();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZZZ");
        for (Map.Entry<String, String> entry: preferences.entrySet()) {
            Thought thought = gson.fromJson(entry.getValue(), new TypeToken<Thought>(){}.getType());
            entries.add(thought);
        }
        Collections.sort(entries);
    }

}
