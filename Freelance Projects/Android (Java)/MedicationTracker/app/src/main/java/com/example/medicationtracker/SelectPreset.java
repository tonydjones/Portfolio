package com.example.medicationtracker;

import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.MainActivity.gson;
import static com.example.medicationtracker.MainActivity.presets_db;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.NewPreset.new_preset;
import static com.example.medicationtracker.TakeMeds.take_meds;
import static com.example.medicationtracker.Wipe.wipe;

public class SelectPreset {
    public static void select_preset(MainActivity Activity, int client, List<Map<String, Object>> presets){
        wipe(Activity, "Manage Presets", () -> select_preset(Activity, client, presets));
        LinearLayout scroll_child = Activity.scroll_child;

        Button add = new Button(Activity);
        add.setText("Manual Entry");
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                take_meds(Activity, client, null);
            }
        });
        scroll_child.addView(add);

        TextView current = new TextView(Activity);
        current.setText("Current Presets");
        scroll_child.addView(current);

        for (int i = 0; i < presets.size(); i++){

            Button backup = new Button(Activity);
            backup.setText((String)presets.get(i).get("name"));

            int finalI = i;
            backup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    take_meds(Activity, client, gson.fromJson(((String)presets.get(finalI).get("preset")), new TypeToken<Map<Integer, Map<String, String>>>(){}.getType()));
                }
            });
            scroll_child.addView(backup);
        }
    }
}
