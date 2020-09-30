package com.example.cbtaid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Map;

public class SituationActivity extends MainActivity {
    public Thought thought;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_situation);
        getSupportActionBar().setTitle("Explain The Situation");
        thought = (Thought) getIntent().getSerializableExtra("thought");
    }

    @SuppressLint("ShowToast")
    public void next(View v){
        EditText situation = findViewById(R.id.situation);
        if (situation.getText().length() != 0) {
            thought.situation = situation.getText().toString();
            Log.e("situation", thought.situation);
            Intent intent = new Intent(getApplicationContext(), EmotionsActivity.class);
            intent.putExtra("thought", thought);
            startActivity(intent);
        }
        else{
            Toast.makeText(getApplicationContext(), "Please describe your situation.", Toast.LENGTH_SHORT).show();
        }
    }
}
