package com.example.cbtaid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class DetailActivity extends MainActivity {
    public Thought thought;
    public Map<String, String> details;
    public int cycles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setTitle(getIntent().getStringExtra("error"));
        thought = (Thought) getIntent().getSerializableExtra("thought");
        cycles = getIntent().getIntExtra("cycle",0);
        details = new HashMap<>();
        details.put("Overgeneralization", "Overgeneralization is the pattern of thinking that if something is true in one case, it is always true.");
        details.put("Mental Filter", "A mental filter is when you notice negative details more frequently than positive or neutral ones.");
        details.put("Personalization", "Personalization is when you believe you are responsible for all failures and other negative events.");
        details.put("Jumping To Conclusions", "Jumping to conclusions is thinking that if something has been true in the past, then it is always true.");
        details.put("Mind Reading", "Mind reading occurs when you assume what others think or assume that you know what the outcome of a situation will be without checking.");
        details.put("Fortune Telling", "Fortune telling is when you make predictions without evidence.");
        details.put("Catastrophizing", "Catastrophizing occurs when you believe the worst case scenario is certain to occur.");
        details.put("All Or None Thinking", "All or none thinking is believing that things are either black or white, all or none, good or bad.");
        details.put("Emotional Reasoning", "Emotional reasoning is believing that your emotions equal facts. If you feel it, then it must be true.");
        details.put("Should Statements", "Should statements are indicative of judgment. You judge how things should, must, or ought to be, and this can be " +
                "directed at yourself, others, or a situation at large. If things are not as expected, you feel guilty or resentful.");
        details.put("Emotional Labeling", "Emotional labeling occurs when you use extreme emotional terms to describe yourself, events, or other people.");
        details.put("Minimization/Magnification", "Minimization/magnification occurs when you minimize positive factors and maximize negative" +
                "factors. Positive events count less than negative events, failure count more than successes, and good traits are less important than bad traits.");
        TextView detail = findViewById(R.id.details);
        detail.setText(details.get(getIntent().getStringExtra("error")));
    }

    @SuppressLint("ShowToast")
    public void select(View v){
        thought.emotions.get(cycles).errors.add(getIntent().getStringExtra("error"));
        Intent intent = new Intent(getApplicationContext(), ErrorsActivity.class);
        intent.putExtra("cycles", cycles);
        intent.putExtra("thought", thought);
        startActivity(intent);
    }

    public void deselect(View v){
        thought.emotions.get(cycles).errors.remove(getIntent().getStringExtra("error"));
        Intent intent = new Intent(getApplicationContext(), ErrorsActivity.class);
        intent.putExtra("cycles", cycles);
        intent.putExtra("thought", thought);
        startActivity(intent);
    }
}
