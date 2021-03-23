package com.example.medicationtracker;

import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.CleanEntries.clean_entries;
import static com.example.medicationtracker.DateToLong.date_to_long;
import static com.example.medicationtracker.Long_To_Date.long_to_date;
import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.MainActivity.gson;
import static com.example.medicationtracker.MainActivity.prescriptions_db;
import static com.example.medicationtracker.MainActivity.presets_db;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.MainActivity.weighted_params;
import static com.example.medicationtracker.NextDay.next_day;
import static com.example.medicationtracker.Wipe.wipe;

public class NewPreset {
    public static void new_preset(MainActivity Activity, int client_id, Map<Integer, Map<String, String>> packet, String name, Integer id){
        wipe(Activity, "Make a Preset", () -> new_preset(Activity, client_id, packet, name, id));
        LinearLayout scroll_child = Activity.scroll_child;

        CheckBox def = new CheckBox(Activity);
        def.setText("Use default name for this preset.");
        scroll_child.addView(def);

        def.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (def.isChecked()){
                    scroll_child.removeViewAt(0);
                }
                else {
                    EditText enter_name = new EditText(Activity);
                    enter_name.setHint("Preset Name");
                    scroll_child.addView(enter_name, 0);

                    if (name != null){
                        enter_name.setText(name);
                    }
                }
            }
        });

        if (name != null){
            def.callOnClick();
        }
        else {
            def.setChecked(true);
        }

        List<Map<String, Object>> prescriptions = prescriptions_db.getRows(
                new String[]{"name", "id", "dose_max", "daily_max", "controlled", "count", "instructions"},
                new String[]{"client_id=" + client_id, "active=1"},
                new String[]{"name", "ASC"}, false);

        for (int i = 0; i < prescriptions.size(); i++) {
            Map<String, Object> script = prescriptions.get(i);

            final Button current_script = new Button(Activity);
            current_script.setText((String) script.get("name"));
            current_script.setTag(false);
            int finalI = i;
            current_script.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!((Boolean) current_script.getTag())) {
                        current_script.setTag(true);

                        TextView instructions = new TextView(Activity);
                        String text = (String) script.get("instructions");

                        boolean controlled = ((int) script.get("controlled") == 1);

                        EditText current_count = new EditText(Activity);
                        current_count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        final float[] count = {0};
                        current_count.setText(String.valueOf(count[0]));

                        if (script.get("dose_max") != null) {
                            text += "\nMaximum Dose: " + (float) script.get("dose_max");
                            instructions.setTag(script.get("dose_max"));
                        }

                        if (script.get("daily_max") != null) {
                            text += "\nDaily Maximum: " + (float) script.get("daily_max");
                            current_count.setTag(script.get("daily_max"));
                        }

                        instructions.setText(text);

                        Button add = new Button(Activity);
                        add.setText("Add");
                        add.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!controlled || Float.parseFloat(current_count.getText().toString()) <= (float) script.get("count") - 1) {
                                    current_count.setText(String.valueOf(Float.parseFloat(current_count.getText().toString()) + 1));
                                }
                                else {
                                    current_count.setText(String.valueOf((float) script.get("count")));
                                }
                            }
                        });
                        add.setLayoutParams(weighted_params);

                        Button subtract = new Button(Activity);
                        subtract.setText("Subtract");
                        subtract.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (Float.parseFloat(current_count.getText().toString()) >= 1) {
                                    current_count.setText(String.valueOf(Float.parseFloat(current_count.getText().toString()) - 1));
                                }
                                else {
                                    current_count.setText(String.valueOf(0));
                                }
                            }
                        });
                        subtract.setTag(script.get("id"));
                        subtract.setLayoutParams(weighted_params);

                        LinearLayout buttons = new LinearLayout(Activity);
                        buttons.setOrientation(LinearLayout.HORIZONTAL);
                        buttons.addView(subtract);
                        buttons.addView(add);



                        EditText notes = new EditText(Activity);
                        notes.setHint("Notes");
                        notes.setTag(finalI);

                        scroll_child.addView(instructions, scroll_child.indexOfChild(current_script) + 1);
                        scroll_child.addView(current_count, scroll_child.indexOfChild(current_script) + 2);
                        scroll_child.addView(buttons, scroll_child.indexOfChild(current_script) + 3);
                        scroll_child.addView(notes, scroll_child.indexOfChild(current_script) + 4);

                    }
                    else {
                        current_script.setTag(false);
                        for (int i = 0; i < 4; i++){
                            scroll_child.removeViewAt(scroll_child.indexOfChild(current_script) + 1);
                        }
                    }
                }
            });
            scroll_child.addView(current_script);

            if (packet != null && packet.get(script.get("id")) != null){
                current_script.callOnClick();
                ((EditText) scroll_child.getChildAt(scroll_child.indexOfChild(current_script) + 2)).setText(packet.get(script.get("id")).get("count"));
                ((EditText) scroll_child.getChildAt(scroll_child.indexOfChild(current_script) + 4)).setText(packet.get(script.get("id")).get("notes"));
            }
        }

        Button confirm = new Button(Activity);
        confirm.setText("Confirm Preset");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean taken = false;

                Map<Integer, Map<String, String>> new_packet = new HashMap<>();

                for (int i = 0; i < scroll_child.getChildCount(); i++) {
                    if (scroll_child.getChildAt(i).getTag() != null && (Boolean) scroll_child.getChildAt(i).getTag()) {
                        String count = ((EditText) scroll_child.getChildAt(i + 2)).getText().toString();
                        if (count.length() > 0 && Float.parseFloat(count) > 0) {
                            taken = true;

                            Map<String, String> mini_packet = new HashMap<>();
                            mini_packet.put("count", count);
                            mini_packet.put("notes", ((EditText) scroll_child.getChildAt(i + 4)).getText().toString());
                            int id = (int)((LinearLayout)scroll_child.getChildAt(i + 3)).getChildAt(0).getTag();
                            new_packet.put(id, mini_packet);

                            i += 4;
                        }
                        else {
                            warning.setView(null);
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("OK", null);
                            warning.setTitle("Invalid Medication Amount");
                            Button button = (Button) scroll_child.getChildAt(i);
                            warning.setMessage("You must input a valid amount of " + button.getText());
                            warning.show();
                            return;
                        }
                    }
                }
                if (taken) {
                    Map<String,Object> preset = new HashMap<>();
                    preset.put("client_id", client_id);
                    preset.put("preset", gson.toJson(new_packet));
                    List<Object> preset_names = null;
                    if (id != null){
                        preset_names = presets_db.getSingleColumn("name", new String[]{"client_id="+client_id, "id!="+id}, null, false);
                    }
                    else {
                        preset_names = presets_db.getSingleColumn("name", new String[]{"client_id="+client_id}, null, false);
                    }

                    if (def.isChecked()){
                        int counter = 1;
                        while(preset_names.contains("PRESET " + counter)){
                            counter += 1;
                        }
                        preset.put("name", "PRESET " + counter);
                    }
                    else{
                        String name = ((EditText)scroll_child.getChildAt(0)).getText().toString().toUpperCase();
                        if (name.length() == 0){
                            warning.setView(null);
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("OK", null);
                            warning.setTitle("Invalid Preset Name");
                            warning.setMessage("You must input a name for the preset.");
                            warning.show();
                            return;
                        }
                        if (preset_names.contains(name)){
                            warning.setView(null);
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("OK", null);
                            warning.setTitle("Invalid Preset Name");
                            warning.setMessage("The client already has a preset with that name. Presets must have unique names.");
                            warning.show();
                            return;
                        }
                        preset.put("name", name);
                    }

                    if (packet == null){
                        presets_db.addRow(preset);
                    }
                    else{
                        presets_db.update(preset, new String[]{"id="+id});
                    }
                    Activity.onBackPressed();
                }

            }
        });
        scroll_child.addView(confirm);
    }
}
