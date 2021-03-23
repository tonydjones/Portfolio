package com.example.medicationtracker;

import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.DateToLong.date_to_long;
import static com.example.medicationtracker.Long_To_Date.long_to_date;
import static com.example.medicationtracker.CleanEntries.clean_entries;
import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.MainActivity.prescriptions_db;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.MainActivity.weighted_params;
import static com.example.medicationtracker.NextDay.next_day;
import static com.example.medicationtracker.Wipe.wipe;

public class TakeMeds {
    public static void take_meds(MainActivity Activity, int client_id, Map<Integer, Map<String, String>> packet) {

        wipe(Activity, "Take Meds", () -> take_meds(Activity, client_id, packet));
        LinearLayout scroll_child = Activity.scroll_child;

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

                        if (controlled) {
                            text += "\nCount: " + (float) script.get("count");
                        }

                        long date = System.currentTimeMillis();
                        String date_string = long_to_date(date);
                        long datelong = date_to_long(date_string);
                        long endlong = date_to_long(next_day(date_string, 1));
                        List<Object> taken = entries_db.getSingleColumn("change",
                                new String[]{"method='TOOK MEDS'",
                                        "prescription_id="+script.get("id"),
                                        "datetime>="+datelong,
                                        "datetime<"+endlong},
                                null, false);

                        float taken_today = 0;
                        for (int j = 0; j < taken.size(); j++){
                            taken_today += (Float) taken.get(j);
                        }

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

                        text += "\nTaken Today: " + taken_today;

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
                        add.setTag(taken_today);
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
        confirm.setText("Confirm Logged Meds");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean taken = false;
                for (int i = 0; i < scroll_child.getChildCount(); i++) {
                    if (scroll_child.getChildAt(i).getTag() != null && (Boolean) scroll_child.getChildAt(i).getTag()) {
                        String count = ((EditText) scroll_child.getChildAt(i + 2)).getText().toString();
                        if (count.length() > 0 && Float.parseFloat(count) > 0) {
                            taken = true;
                            i += 4;
                        }
                        else {
                            warning.setView(null);
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("OK", null);
                            warning.setTitle("Invalid Medication Amount");
                            Button button = (Button) scroll_child.getChildAt(i);
                            warning.setMessage("You must input a valid amount of " + button.getText() + " to take.");
                            warning.show();
                            return;
                        }
                    }
                }
                if (taken) {
                    clean_entries(Activity, client_id, 0, new ArrayList<>(), new HashMap<>(), new HashMap<>());
                }

            }
        });
        scroll_child.addView(confirm);
    }
}
