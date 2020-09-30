package com.example.cbtaid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EmotionsActivity extends MainActivity {
    public Thought thought;
    public LinearLayout.LayoutParams params;
    public SharedPreferences pref;
    public Gson gson;
    public Spinner previous;
    public Spinner novelrating;
    public Spinner previousrating;
    public SharedPreferences.Editor editor;
    public Map<String, Integer> history;
    public List<String> added;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emotions);
        getSupportActionBar().setTitle("Evaluate your Emotions");
        thought = (Thought) getIntent().getSerializableExtra("thought");
        params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        pref = getApplicationContext().getSharedPreferences("pref",0);
        editor = pref.edit();
        String temphistory = pref.getString("history", "{}");
        gson = new Gson();
        history = gson.fromJson(temphistory,  new TypeToken<Map<String, Integer>>(){}.getType());
        List<String> keys = new ArrayList<>();
        keys.addAll(history.keySet());

        for (int i = 0; i < keys.size() - 10; i++){
            int smallest = history.get(keys.get(0));
            String remove = keys.get(0);
            for (int j = 1; j < keys.size(); j++){
                if (history.get(keys.get(j)) < smallest){
                    smallest = history.get(keys.get(j));
                    remove = keys.get(j);
                }
            }
            keys.remove(remove);
        }

        ArrayAdapter<String> emotionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, keys);
        emotionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        previous = findViewById(R.id.previous);
        previous.setAdapter(emotionsAdapter);

        ArrayAdapter<Integer> ratingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList(1,2,3,4,5,6,7,8,9,10));
        ratingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        novelrating = findViewById(R.id.novelrating);
        previousrating = findViewById(R.id.previousrating);
        novelrating.setAdapter(ratingAdapter);
        previousrating.setAdapter(ratingAdapter);

        added = new ArrayList<>();
    }

    @SuppressLint("ShowToast")
    public void addnovel(View v){
        EditText novel = findViewById(R.id.novel);
        if (novel.getText().toString().length() > 0) {
            String tmp = novel.getText().toString().toLowerCase();
            final String text = tmp.substring(0, 1).toUpperCase() + tmp.substring(1);
            if (! added.contains(text)) {
                added.add(text);
                final LinearLayout emotion_view = findViewById(R.id.emotions);
                final LinearLayout emotions = new LinearLayout(this);
                emotion_view.addView(emotions);
                final Emotion current = new Emotion(text, (Integer) novelrating.getSelectedItem());
                thought.emotions.add(current);
                TextView emotion = new TextView(this);
                emotion.setLayoutParams(params);
                emotion.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                emotion.setText(text);
                TextView strength = new TextView(this);
                strength.setLayoutParams(params);
                strength.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                strength.setText(Integer.toString((int) novelrating.getSelectedItem()) + " / 10");
                strength.setTextColor(0xFF000000);
                emotion.setTextColor(0xFF000000);
                Button button = new Button(this);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        emotion_view.removeView(emotions);
                        if (history.get(text) == 1){
                            history.remove(text);
                        }
                        else{
                            history.put(text, history.get(text) - 1);
                        }
                        thought.emotions.remove(current);
                        added.remove(text);
                    }
                });
                button.setLayoutParams(params);
                button.setText("Delete");
                emotions.addView(emotion);
                emotions.addView(strength);
                emotions.addView(button);
                if (history.keySet().contains(text)){
                    history.put(text, history.get(text) + 1);
                }
                else{
                    history.put(text, 1);
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "That emotion has already been documented.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "Please input both an emotion and a rating.", Toast.LENGTH_SHORT).show();
        }
        novel.setText("");
    }

    @SuppressLint("ShowToast")
    public void addprevious(View v){
        if (previous.getSelectedItem() != null) {
            final String text = previous.getSelectedItem().toString();
            if (!added.contains(previous.getSelectedItem().toString())) {
                added.add(text);
                final LinearLayout emotion_view = findViewById(R.id.emotions);
                final LinearLayout emotions = new LinearLayout(this);
                emotion_view.addView(emotions);
                final Emotion current = new Emotion(previous.getSelectedItem().toString(), (Integer) previousrating.getSelectedItem());
                thought.emotions.add(current);
                TextView emotion = new TextView(this);
                emotion.setLayoutParams(params);
                emotion.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                emotion.setText(previous.getSelectedItem().toString());
                TextView strength = new TextView(this);
                strength.setLayoutParams(params);
                strength.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                strength.setText(previousrating.getSelectedItem().toString() + " / 10");
                strength.setTextColor(0xFF000000);
                emotion.setTextColor(0xFF000000);
                Button button = new Button(this);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        emotion_view.removeView(emotions);
                        thought.emotions.remove(current);
                        if (history.get(text) == 1) {
                            history.remove(text);
                        } else {
                            history.put(text, history.get(text) - 1);
                        }
                        added.remove(text);
                    }
                });
                history.put(text, history.get(text) + 1);
                button.setLayoutParams(params);
                button.setText("Delete");
                emotions.addView(emotion);
                emotions.addView(strength);
                emotions.addView(button);
            } else {
                Toast.makeText(getApplicationContext(), "That emotion has already been documented.", Toast.LENGTH_SHORT).show();
            }
        }
            else{
            Toast.makeText(getApplicationContext(), "Please input both an emotion and a rating.", Toast.LENGTH_SHORT).show();
        }
    }

    public void next(View v) {
        if (thought.emotions.size() > 0) {
            editor.putString("history", gson.toJson(history)).apply();
            for (int i = 0; i < thought.emotions.size(); i++){
                Log.e(thought.emotions.get(i).emotion, String.valueOf(thought.emotions.get(i).rating));
            }
            Intent intent = new Intent(getApplicationContext(), BeliefsActivity.class);
            intent.putExtra("thought", thought);
            startActivity(intent);
        }
        else{
            Toast.makeText(getApplicationContext(), "Please document at least one emotion.", Toast.LENGTH_SHORT).show();
        }
    }
}