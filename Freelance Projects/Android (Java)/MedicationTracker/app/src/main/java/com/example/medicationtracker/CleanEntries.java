package com.example.medicationtracker;

import android.app.Activity;
import android.content.DialogInterface;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.ClientConfirm.client_confirm;
import static com.example.medicationtracker.Create_Entry.create_entry;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.TakeMeds.take_meds;

public class CleanEntries {

    public static void clean_entries(MainActivity Activity, int client_id, int position, List<Map<String, Object>> entries, Map<Integer, Float> maxes, Map<Integer, Map<String, String>> packet) {

        LinearLayout scroll_child = Activity.scroll_child;

        for (int i = position; i < scroll_child.getChildCount(); i++) {
            if (scroll_child.getChildAt(i).getTag() != null && (Boolean) scroll_child.getChildAt(i).getTag()) {
                String count_string = ((EditText) scroll_child.getChildAt(i + 2)).getText().toString();
                if (count_string.length() > 0 && Float.parseFloat(count_string) > 0) {
                    float count = Float.parseFloat(count_string);
                    float taken_today = (Float) ((LinearLayout)scroll_child.getChildAt(i + 3)).getChildAt(1).getTag();
                    Float dose_max = (Float) (scroll_child.getChildAt(i + 1).getTag());
                    Float daily_max = (Float) (scroll_child.getChildAt(i + 2).getTag());

                    final boolean[] dose_override = {false};
                    final boolean[] daily_override = {false};

                    String drug = (String)((Button) scroll_child.getChildAt(i)).getText();
                    EditText notes = (EditText) scroll_child.getChildAt(i + 4);
                    int script_id = (int) ((LinearLayout)scroll_child.getChildAt(i + 3)).getChildAt(0).getTag();
                    List<String> note = new ArrayList<>();
                    if (notes.getText().toString().length() > 0) {
                        note.add(notes.getText().toString());
                    }

                    maxes.put(script_id, daily_max);

                    if (dose_max != null && count > dose_max){
                        warning.setView(null);
                        warning.setMessage("Taking " + count + " " +
                                drug + " is more than the maximum dose of " + dose_max);
                        warning.setTitle("Taking too many " + drug);
                        int finalI = i;
                        List<String> finalNote = note;
                        warning.setPositiveButton("Override Dose Limit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dose_override[0] = true;
                                if (daily_max != null && count + taken_today > daily_max){
                                    warning.setView(null);
                                    warning.setMessage("Taking " + count + " " +
                                            drug + " will put the client over the daily limit of " +
                                            daily_max);
                                    warning.setTitle("Over the daily limit for " + drug);
                                    warning.setPositiveButton("Override Daily Limit", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            daily_override[0] = true;
                                            Map<String,Object> entry = create_entry(client_id, script_id, drug, null, count, null,
                                                    null, dose_override[0], daily_override[0], 0, "TOOK MEDS",
                                                    null, null, null, null, null,null, finalNote);
                                            entries.add(entry);
                                            Map<String, String> mini_packet = new HashMap<>();
                                            mini_packet.put("count", String.valueOf(count));
                                            mini_packet.put("notes", notes.getText().toString());
                                            packet.put(script_id, mini_packet);
                                            clean_entries(Activity, client_id, finalI + 5, entries, maxes, packet);
                                        }
                                    });
                                    warning.setNegativeButton("Cancel", null);
                                    warning.show();
                                    return;
                                }
                                Map<String,Object> entry = create_entry(client_id, script_id, drug, null, count, null,
                                        null, dose_override[0], daily_override[0], 0, "TOOK MEDS",
                                        null, null, null, null, null,null, finalNote);
                                entries.add(entry);
                                Map<String, String> mini_packet = new HashMap<>();
                                mini_packet.put("count", String.valueOf(count));
                                mini_packet.put("notes", notes.getText().toString());
                                packet.put(script_id, mini_packet);
                                clean_entries(Activity, client_id, finalI + 5, entries, maxes, packet);
                            }
                        });
                        warning.setNegativeButton("Cancel", null);
                        warning.show();
                        return;
                    }
                    else if (daily_max != null && count + taken_today > daily_max){
                        int finalI = i;
                        List<String> finalNote = note;
                        warning.setView(null);
                        warning.setMessage("Taking " + count + " " +
                                drug + " will put the client over the daily limit of " +
                                daily_max);
                        warning.setTitle("Over the daily limit for " + drug);
                        warning.setPositiveButton("Override Daily Limit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                daily_override[0] = true;
                                Map<String,Object> entry = create_entry(client_id, script_id, drug, null, count, null,
                                        null, dose_override[0], daily_override[0], 0, "TOOK MEDS",
                                        null, null, null, null, null, null, finalNote);
                                entries.add(entry);
                                Map<String, String> mini_packet = new HashMap<>();
                                mini_packet.put("count", String.valueOf(count));
                                mini_packet.put("notes", notes.getText().toString());
                                packet.put(script_id, mini_packet);
                                clean_entries(Activity, client_id, finalI + 5, entries, maxes, packet);
                            }
                        });
                        warning.setNegativeButton("Cancel", null);
                        warning.show();
                        return;
                    }

                    Map<String,Object> entry = create_entry(client_id, script_id, drug, null, count, null,
                            null, dose_override[0], daily_override[0], 0, "TOOK MEDS",
                            null, null, null, null, null, null, note);
                    entries.add(entry);

                    Map<String, String> mini_packet = new HashMap<>();
                    mini_packet.put("count", String.valueOf(count));
                    mini_packet.put("notes", notes.getText().toString());
                    packet.put(script_id, mini_packet);
                }
                i+=4;
            }
        }
        history.remove(0);
        history.add(0, () -> take_meds(Activity, client_id, packet));
        client_confirm(Activity, entries, maxes);
    }
}
