package com.example.cbtaid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ContentView;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ErrorsActivity extends MainActivity {
    public Thought thought;
    public TextView belief;
    public int cycles;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_errors);
        getSupportActionBar().setTitle("Look For Cognitive Errors");
        thought = (Thought) getIntent().getSerializableExtra("thought");

        belief = findViewById(R.id.belief);

        cycles = getIntent().getIntExtra("cycles",0);
        LinearLayout errorlist = findViewById(R.id.errorlist);
        for(int i=1; i<errorlist.getChildCount(); i++) {
            LinearLayout nextChild = (LinearLayout) errorlist.getChildAt(i);
            TextView error = (TextView) nextChild.getChildAt(0);
            if (thought.emotions.get(cycles).errors.contains(error.getText())) {
                highlight(error);
            }
            else{
                nextChild.setTag(null);
                nextChild.setBackgroundColor(0xFFFFFFFF);
                error.setTextColor(0xFF000000);
            }
        }

        belief.setText(thought.emotions.get(cycles).belief);


    }

    public void next(View v){
        LinearLayout errorlist = findViewById(R.id.errorlist);
        for(int i=1; i<errorlist.getChildCount(); i++) {
            LinearLayout nextChild = (LinearLayout) errorlist.getChildAt(i);
            TextView error = (TextView) nextChild.getChildAt(0);
            if (nextChild.getTag() == null){
                continue;
            }
            else{
                nextChild.setTag(null);
                nextChild.setBackgroundColor(0xFFFFFFFF);
                error.setTextColor(0xFF000000);
            }
        }
        cycles++;
        if (cycles >= thought.emotions.size()){
            cycles--;
            int errors = 0;
            for (int i = 0; i < thought.emotions.size(); i++){
                errors += thought.emotions.get(i).errors.size();
            }
            if (errors == 0){
                Intent intent = new Intent(getApplicationContext(), RethinkActivity.class);
                intent.putExtra("thought", thought);
                intent.putExtra("activity", "errors");
                startActivity(intent);
            }
            else {
                for (int i = 0; i < thought.emotions.size(); i++){
                    Log.e("errors", thought.emotions.get(i).errors.toString());
                }

                Intent intent = new Intent(getApplicationContext(), ResponseActivity.class);
                intent.putExtra("thought", thought);
                intent.putExtra("cycles", cycles);
                startActivity(intent);
            }
        }
        else{
            for(int i=1; i<errorlist.getChildCount(); i++) {
                LinearLayout nextChild = (LinearLayout) errorlist.getChildAt(i);
                TextView error = (TextView) nextChild.getChildAt(0);
                if (thought.emotions.get(cycles).errors.contains(error.getText())) {
                    highlight(error);
                }
            }
            belief.setText(thought.emotions.get(cycles).belief);
        }
    }

    public void details(View v){
        LinearLayout parent = (LinearLayout) v.getParent();
        TextView selector = (TextView) parent.getChildAt(0);
        Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
        intent.putExtra("cycle", cycles);
        intent.putExtra("thought", thought);
        intent.putExtra("error", selector.getText());
        startActivity(intent);
    }

    public void highlight(View v){
        TextView tv = (TextView) v;
        LinearLayout parent = (LinearLayout) v.getParent();
        if (parent.getTag() == null){
            parent.setTag(true);
            parent.setBackgroundColor(0xFF0000FF);
            tv.setTextColor(0xFFFFFFFF);
            thought.emotions.get(cycles).errors.add((String) ((TextView) v).getText());
        }
        else{
            parent.setTag(null);
            parent.setBackgroundColor(0xFFFFFFFF);
            tv.setTextColor(0xFF000000);
            thought.emotions.get(cycles).errors.remove(((TextView) v).getText());

        }
    }


    public void onBackPressed(){
        if (cycles > 0){
            cycles--;
            LinearLayout errorlist = findViewById(R.id.errorlist);
            for(int i=1; i<errorlist.getChildCount(); i++) {
                LinearLayout nextChild = (LinearLayout) errorlist.getChildAt(i);
                TextView error = (TextView) nextChild.getChildAt(0);
                if (thought.emotions.get(cycles).errors.contains(error.getText())) {
                    highlight(error);
                }
                else{
                    nextChild.setTag(null);
                    nextChild.setBackgroundColor(0xFFFFFFFF);
                    error.setTextColor(0xFF000000);
                }
            }
            belief.setText(thought.emotions.get(cycles).belief);
        }
        else{
            Intent intent = new Intent(getApplicationContext(), BeliefsActivity.class);
            intent.putExtra("thought", thought);
            intent.putExtra("cycles", (thought.emotions.size()-1));
            startActivity(intent);
        }
    }
}
