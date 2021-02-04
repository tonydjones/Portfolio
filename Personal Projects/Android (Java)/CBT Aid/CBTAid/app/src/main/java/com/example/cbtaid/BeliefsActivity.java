package com.example.cbtaid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeliefsActivity extends MainActivity {
    public Thought thought;
    public int cycles;
    public Spinner rating;
    public TextView prompt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beliefs);
        getSupportActionBar().setTitle("Describe Your Beliefs");
        thought = (Thought) getIntent().getSerializableExtra("thought");
        cycles = getIntent().getIntExtra("cycles", 0);

        ArrayAdapter<Integer> ratingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList(1,2,3,4,5,6,7,8,9,10));
        ratingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rating = findViewById(R.id.rating);
        rating.setAdapter(ratingAdapter);
        prompt = findViewById(R.id.prompt);
        prompt.setText("Please describe the belief you hold or experience that causes you to feel " + thought.emotions.get(cycles).emotion + " at an intensity of " +
                thought.emotions.get(cycles).rating + "/10.");
    }

    public void next(View v){
        thought.emotions.get(cycles).errors.clear();
        EditText belief = findViewById(R.id.belief);
        if (belief.getText().toString().length() > 0) {
            if (cycles < thought.emotions.size()){
                thought.emotions.get(cycles).belief = belief.getText().toString();
                thought.emotions.get(cycles).beliefrating = (int) rating.getSelectedItem();
                cycles++;
            }
            if (cycles >= thought.emotions.size()) {
                cycles--;
                for (int i = 0; i < thought.emotions.size(); i++){
                    Log.e("Beliefs", thought.emotions.get(i).belief);
                }
                Intent intent = new Intent(getApplicationContext(), ErrorsActivity.class);
                intent.putExtra("thought", thought);
                startActivity(intent);
            }
            else {
                belief.setText("");
                prompt.setText("Please describe the belief you hold or experience that causes you to feel " + thought.emotions.get(cycles).emotion + " at an intensity of " +
                        thought.emotions.get(cycles).rating + "/10.");
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Please input both a belief and a rating.", Toast.LENGTH_SHORT).show();
        }

    }

    public void onBackPressed(){
        if (cycles > 0){
            cycles--;
            EditText belief = findViewById(R.id.belief);
            belief.setText("");
            prompt.setText("Please describe the belief you hold or experience that causes you to feel " + thought.emotions.get(cycles).emotion + " at an intensity of " +
                    thought.emotions.get(cycles).rating + "/10.");
        }
        else{
            super.onBackPressed();
        }

    }
}
