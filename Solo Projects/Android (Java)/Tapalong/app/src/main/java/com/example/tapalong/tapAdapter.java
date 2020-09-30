package com.example.tapalong;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class tapAdapter extends RecyclerView.Adapter<tapAdapter.tapViewHolder> {
    public SharedPreferences pref;

    public static class tapViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout containerView;
        public TextView nameTextView;

        public tapViewHolder(@NonNull View view) {
            super(view);
            this.containerView = view.findViewById(R.id.tap_view);
            this.nameTextView = view.findViewById(R.id.tap_name);

            this.containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Tap tap = (Tap) containerView.getTag();
                    Intent intent = new Intent(context, choiceActivity.class);
                    intent.putExtra("title", tap.title);
                    intent.putExtra("data", tap.data);
                    intent.putExtra("location", tap.location);

                    context.startActivity(intent);
                }
            });
        }

    }

    public List<Tap> taps = new ArrayList<>();

    public void loadTaps(List taps, SharedPreferences pref) {
        Map<String, String> preferences = (Map<String, String>) pref.getAll();
            for (Map.Entry<String, String> entry: preferences.entrySet()) {
                String location = entry.getKey();
                String string = entry.getValue();
                String title = (new File(location)).getName();
                taps.add(new Tap(title, location, string));
            }
        Collections.sort(taps);
        }

        @NonNull
    @Override
    public tapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.tap_view, parent, false);

            return new tapAdapter.tapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull tapAdapter.tapViewHolder holder, int position) {
        Tap current = taps.get(position);
        holder.containerView.setTag(current);
        holder.nameTextView.setText(current.title);
    }


    @Override
    public int getItemCount() {
        return taps.size();
    }

    public void reload() {
        taps.clear();
        loadTaps(taps, pref);
        notifyDataSetChanged();
    }

    tapAdapter(Context context) {
        pref = context.getSharedPreferences("pref", 0);
        //pref.edit().clear().commit();
        reload();
    }
}
