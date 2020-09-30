package com.example.cbtaid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResponseActivity extends MainActivity {
    public Thought thought;
    public Map<String,String> details;
    public int cycles;
    public Set<String> responseset;
    public List<String> responselist;
    public List<String> keylist;
    public TextView cope;
    public TextView prompt;
    public LinearLayout beliefs;
    public Spinner rating;
    public List<String> errorlist;
    public List<String> errors;
    public List<String> finished1;
    public List<Integer> finished2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);
        getSupportActionBar().setTitle("Rational Responses");
        thought = (Thought) getIntent().getSerializableExtra("thought");
        details = new HashMap<>();
        details.put("Overgeneralization", " can be countered by looking for factors that distinguish the past, present, and future scenarios from each other.");
        details.put("Mental Filter", " can be countered by examining all details of a situation whether positive, negative, or neutral.");
        details.put("Personalization", " can be countered by looking for alternative sources of responsibility.");
        details.put("Jumping To Conclusions", " can be countered by looking for factors that distinguish the past, present, and future scenarios from each other.");
        details.put("Mind Reading", " can be countered by looking for evidence to support or refute your assumptions.");
        details.put("Fortune Telling", " can be countered by looking for evidence to support or refute your assumptions.");
        details.put("Catastrophizing", " can be countered by looking for evidence to support or refute your assumptions.");
        details.put("All Or None Thinking", " can be countered by learning to use a continuum for evaluating circumstances.");
        details.put("Emotional Reasoning", " can be countered by identifying the emotional language and separating objective facts from emotional beliefs.");
        details.put("Should Statements", " can be countered by recognizing these statements represent preferences, not vital needs.");
        details.put("Emotional Labeling", " can be countered by identifying the emotional language and separating objective facts from emotional beliefs.");
        details.put("Minimization/Magnification", " can be countered by examining all details of a situation whether positive, negative, or neutral.");

        finished1 = new ArrayList<>();
        finished2 = new ArrayList<>();

        cycles = 0;
        errors = new ArrayList<>();
        Set<String> errorset = new HashSet<>();
        errorlist = new ArrayList<>();
        for (int i = 0; i < thought.emotions.size(); i++){
            errorset.addAll(thought.emotions.get(i).errors);
        }
        errorlist.addAll(errorset);

        responseset = new HashSet<>();
        responselist = new ArrayList<>();
        for (int i = 0; i < errorset.size(); i++){
            responseset.add(details.get(errorlist.get(i)));
        }
        responselist.addAll(responseset);
        keylist = new ArrayList<>();
        keylist.addAll(details.keySet());

        ArrayAdapter<Integer> ratingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList(0,1,2,3,4,5,6,7,8,9,10));
        ratingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rating = findViewById(R.id.rating);
        rating.setAdapter(ratingAdapter);

        cope = findViewById(R.id.cope);

        cope.setText(prompt(responselist.get(cycles)) + responselist.get(cycles));

        prompt = findViewById(R.id.prompt);
        prompt.setText("Below are the beliefs that you identified as containing " + prompt2() + ".");

        List<String> thoughtset = new ArrayList<>();
        for (int i = 0; i < thought.emotions.size(); i++){
            List<String> tmp = new ArrayList<>();
            tmp.addAll(thought.emotions.get(i).errors);
            for (int j = 0; j < tmp.size(); j++){
                if (details.get(tmp.get(j)).equals(responselist.get(cycles))){
                    thoughtset.add(thought.emotions.get(i).belief);
                    break;
                }
            }
        }

        beliefs = findViewById(R.id.beliefs);

        for (int i = 0; i < thoughtset.size(); i++){
            TextView tmp = new TextView(this);
            tmp.setText(thoughtset.get(i));
            tmp.setTextColor(0xFF000000);
            beliefs.addView(tmp);
        }


    }

    public String prompt2(){

        if (errors.size() == 1){
            return errors.get(0);
        }
        else if (errors.size() == 2){
            return (errors.get(0) + " and/or " + errors.get(1));
        }
        else {
            String errorstring = "";
            for (int i = 0; i < errors.size() - 2; i++) {
                errorstring += (errors.get(i) + ", ");
            }
            errorstring += (errors.get(errors.size() - 2) + ", and/or " + errors.get(errors.size() - 1));
            return errorstring;
        }
    }

    public String prompt(String response){
        errors.clear();
        for (int i = 0; i < keylist.size(); i++){
            if (details.get(keylist.get(i)) == response && errorlist.contains(keylist.get(i))){
                errors.add(keylist.get(i));
            }
        }

        if (errors.size() == 1){
            return errors.get(0);
        }
        else if (errors.size() == 2){
            return (errors.get(0) + " and " + errors.get(1));
        }
        else {
            String errorstring = "";
            for (int i = 0; i < errors.size() - 2; i++) {
                errorstring += (errors.get(i) + ", ");
            }
            errorstring += (errors.get(errors.size() - 2) + ", and " + errors.get(errors.size() - 1));
            return errorstring;
        }
    }

    public void next(View v){
        EditText response = findViewById(R.id.response);
        if (response.getText().toString().length() > 0) {
            String tmp = response.getText().toString();
            int strength = (int) rating.getSelectedItem();
            finished1.add(tmp);
            finished2.add(strength);
            response.setText("");
            cycles++;
        }
        else {
            Toast.makeText(getApplicationContext(), "Please input both a rational response and a rating.", Toast.LENGTH_SHORT).show();
        }
        if (cycles == responseset.size()){
            cycles--;
            for (int i = 0; i < responselist.size(); i++){
                errors.clear();
                for (int j = 0; j < keylist.size(); j++){
                    if (details.get(keylist.get(j)) == responselist.get(i) && errorlist.contains(keylist.get(j))){
                        errors.add(keylist.get(j));
                    }
                }
                for (int j = 0; j < thought.emotions.size(); j++){
                    for (int k = 0; k < errors.size(); k++){
                        if (thought.emotions.get(j).errors.contains(errors.get(k))){
                            thought.emotions.get(j).responses.put(finished1.get(i), finished2.get(i));
                            break;
                        }
                    }

                }
            }
            for (int i = 0; i < thought.emotions.size(); i++){
                Log.e(thought.emotions.get(i).emotion, String.valueOf(thought.emotions.get(i).responses));
            }
            Intent intent = new Intent(getApplicationContext(), RethinkActivity.class);
            intent.putExtra("thought", thought);
            startActivity(intent);
        }
        else{
            cope.setText(prompt(responselist.get(cycles)) + responselist.get(cycles));
            prompt.setText("Below are the beliefs that you identified as containing " + prompt2() + ".");
            List<String> thoughtset = new ArrayList<>();
            for (int i = 0; i < thought.emotions.size(); i++){
                List<String> tmp = new ArrayList<>();
                tmp.addAll(thought.emotions.get(i).errors);
                for (int j = 0; j < tmp.size(); j++){
                    if (details.get(tmp.get(j)).equals(responselist.get(cycles))){
                        thoughtset.add(thought.emotions.get(i).belief);
                        break;
                    }
                }
            }
            beliefs.removeAllViews();
            for (int i = 0; i < thoughtset.size(); i++){
                TextView tmp = new TextView(this);
                tmp.setText(thoughtset.get(i));
                tmp.setTextColor(0xFF000000);
                beliefs.addView(tmp);
            }
        }
    }

    public void onBackPressed(){
        if (cycles > 0){
            cycles--;
            cope.setText(prompt(responselist.get(cycles)) + responselist.get(cycles));
            prompt.setText("Below are the beliefs that you identified as containing " + prompt2() + ".");
            List<String> thoughtset = new ArrayList<>();
            for (int i = 0; i < thought.emotions.size(); i++){
                List<String> tmp = new ArrayList<>();
                tmp.addAll(thought.emotions.get(i).errors);
                for (int j = 0; j < tmp.size(); j++){
                    if (details.get(tmp.get(j)).equals(responselist.get(cycles))){
                        thoughtset.add(thought.emotions.get(i).belief);
                        break;
                    }
                }
            }
            beliefs.removeAllViews();
            EditText response = findViewById(R.id.response);
            response.setText("");
            for (int i = 0; i < thoughtset.size(); i++){
                TextView tmp = new TextView(this);
                tmp.setText(thoughtset.get(i));
                tmp.setTextColor(0xFF000000);
                beliefs.addView(tmp);
            }
            finished1.remove(finished1.size()-1);
            finished2.remove(finished2.size()-1);
        }
        else{
            int saved = getIntent().getIntExtra("cycles", 0);
            Intent intent = new Intent(getApplicationContext(), ErrorsActivity.class);
            intent.putExtra("cycles", saved);
            intent.putExtra("thought", thought);
            startActivity(intent);
        }
    }
}

