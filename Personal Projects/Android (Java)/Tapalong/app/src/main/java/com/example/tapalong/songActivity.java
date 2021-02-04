package com.example.tapalong;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;

public class songActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        getSupportActionBar().setTitle("Songs");
        recyclerView = findViewById(R.id.song_view);
        layoutManager = new LinearLayoutManager(this);
        songAdapter = new songAdapter();

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(songAdapter);

    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

}
