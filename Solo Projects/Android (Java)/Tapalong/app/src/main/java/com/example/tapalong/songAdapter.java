package com.example.tapalong;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class songAdapter extends RecyclerView.Adapter<songAdapter.songViewHolder> {
    public static class songViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout containerView;
        public TextView nameTextView;

        public songViewHolder(View view) {
            super(view);
            this.containerView = view.findViewById(R.id.song_view);
            this.nameTextView = view.findViewById(R.id.song_name);

            this.containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Song song = (Song) containerView.getTag();
                    Intent intent = new Intent(context, difficultyActivity.class);
                    intent.putExtra("title", song.title);
                    intent.putExtra("location", song.location);

                    context.startActivity(intent);
                }
            });
        }
    }

    public List<Song> songs = new ArrayList<>();

    public void loadSongs(List songs, String location) {
        File folder = new File(location);
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()){
                loadSongs(songs, file.getAbsolutePath());
            }
            else if (file.getName().endsWith(".mp3")){
                songs.add(new Song(file.getAbsolutePath(), file.getName()));
            }
        }
        Collections.sort(songs);

    }

    @NonNull
    @Override
    public songViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_view, parent, false);

        return new songViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull songViewHolder holder, int position) {
        Song current = songs.get(position);
        holder.containerView.setTag(current);
        holder.nameTextView.setText(current.title);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void reload() {
        List<String> locations = new ArrayList<>();
        locations.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        locations.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath());
        songs.clear();
        for (int i = 0; i < locations.size(); i++) {
            loadSongs(songs, locations.get(i));
            notifyDataSetChanged();
        }
    }

    songAdapter() {
        reload();
    }
}
