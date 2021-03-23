package com.example.medicationtracker;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.Check_Password.check_password;
import static com.example.medicationtracker.DatabaseMaps.database_maps;
import static com.example.medicationtracker.LongToDatetime.long_to_datetime;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.editor;
import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.MainActivity.gson;
import static com.example.medicationtracker.MainActivity.prefs;
import static com.example.medicationtracker.MainActivity.prescriptions_db;
import static com.example.medicationtracker.MainActivity.presets_db;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.NewPreset.new_preset;
import static com.example.medicationtracker.UI_Message.ui_message;
import static com.example.medicationtracker.Wipe.wipe;

public class Presets {
    public static void presets(MainActivity Activity, int client){
        wipe(Activity, "Manage Presets", () -> presets(Activity, client));
        LinearLayout scroll_child = Activity.scroll_child;

        Button add = new Button(Activity);
        add.setText("Make A New Preset");
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new_preset(Activity, client, null, null, null);
            }
        });
        scroll_child.addView(add);

        List<Map<String, Object>> presets = presets_db.getRows(null, new String[]{"client_id=" + client}, null, false);

        if (presets.size() > 0){
            TextView current = new TextView(Activity);
            current.setText("Current Presets");
            scroll_child.addView(current);
        }

        for (int i = 0; i < presets.size(); i++){

            Button backup = new Button(Activity);
            backup.setText((String)presets.get(i).get("name"));

            int finalI = i;
            backup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    warning.setView(null);
                    warning.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            warning.setNeutralButton("", null);
                        }
                    });
                    warning.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            warning.setNeutralButton("", null);
                            presets_db.delete_single_constraint("id="+presets.get(finalI).get("id"));
                            scroll_child.removeView(backup);
                        }
                    });
                    warning.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            warning.setNeutralButton("", null);
                            Map<Integer, Map<String, String>> packet = gson.fromJson((String)presets.get(finalI).get("preset"), new TypeToken<Map<Integer, Map<String, String>>>(){}.getType());
                            new_preset(Activity, client, packet, (String)presets.get(finalI).get("name"), (int)presets.get(finalI).get("id"));
                        }
                    });
                    warning.setTitle("Edit or Delete?");
                    warning.setMessage("Would you like to delete this preset, or edit this preset?");
                    warning.show();
                }
            });
            scroll_child.addView(backup);
        }
    }
}
