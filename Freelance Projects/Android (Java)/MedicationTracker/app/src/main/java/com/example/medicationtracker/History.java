package com.example.medicationtracker;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.LongToDatetime.long_to_datetime;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.MainActivity.gson;
import static com.example.medicationtracker.Wipe.wipe;

public class History {
    public static void history(MainActivity Activity, int client_id) {
        wipe(Activity, "Medication History", () -> history(Activity, client_id));
        LinearLayout scroll_child = Activity.scroll_child;

        List<Object> entries;

        boolean staff = false;

        if (clients_db.getObject("class", new String[]{"id="+client_id}).equals("client")){
            entries = entries_db.getSingleColumn(
                    "datetime",
                    new String[]{"client_id=" + client_id},
                    "DESC", true
            );
        }
        else {
            staff = true;
            entries = entries_db.getSingleColumn(
                    "datetime",
                    new String[]{"staff_1=" + client_id + " OR staff_2="+client_id},
                    "DESC", true
            );
        }

        boolean finalStaff = staff;
        List<String> other_methods = Arrays.asList("CLIENT DISCHARGED", "COUNT", "MISCOUNT", "DISCONTINUED DUE TO UPDATE", "PRESCRIPTION DISCONTINUED");

        for (int i = 0; i < entries.size(); i++) {

            long entry_time = (long)entries.get(i);

            List <Object> methods;
            if (finalStaff){
                methods = entries_db.getSingleColumn(
                        "method",
                        new String[]{"(staff_1=" + client_id + " OR staff_2="+client_id+")", "datetime="+entry_time},
                        "ASC", true
                );
            }
            else {
                methods = entries_db.getSingleColumn(
                        "method",
                        new String[]{"client_id=" + client_id, "datetime="+entry_time},
                        "ASC", true
                );
            }
            String method_string = "";
            for (int j = 0; j < methods.size(); j++){
                method_string += " | " + methods.get(j);
            }

            Button button = new Button(Activity);
            button.setText(long_to_datetime(entry_time) + method_string);
            button.setTag(false);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!(Boolean) button.getTag()) {
                        button.setTag(true);

                        List<Map<String, Object>> time_entries;

                        TextView info = new TextView(Activity);
                        String text = "";

                        String attending = "";

                        if (finalStaff){
                            time_entries = entries_db.getRows(
                                    new String[]{"prescription_id", "drug", "datetime", "old_count", "change", "new_count", "dose_override", "daily_override", "edits", "method", "notes", "client_id"},
                                    new String[]{"datetime=" + entry_time, "(staff_1=" + client_id + " OR staff_2="+client_id + ")"},
                                    new String[]{"client_id", "ASC", "prescription_id", "ASC"}, false
                            );
                        }
                        else {
                            time_entries = entries_db.getRows(
                                    new String[]{"prescription_id", "drug", "datetime", "old_count", "change", "new_count", "dose_override", "daily_override", "edits", "method", "notes", "staff_present"},
                                    new String[]{"datetime=" + entry_time, "client_id="+client_id},
                                    new String[]{"staff_present", "ASC", "prescription_id", "ASC"}, false
                            );
                        }

                        for (int i = 0; i < time_entries.size(); i++) {

                            Map<String, Object> entry = time_entries.get(i);

                            if (finalStaff && !clients_db.getObject("name", new String[]{"id="+time_entries.get(i).get("client_id")}).equals(attending)){
                                attending = (String)clients_db.getObject("name", new String[]{"id="+time_entries.get(i).get("client_id")});
                                text += "\n\n\nClient: " + attending;
                            }
                            else if (!finalStaff && !time_entries.get(i).get("staff_present").equals(attending)){
                                attending = (String)time_entries.get(i).get("staff_present");
                                text += "\n\n\nStaff Present: " + attending;
                            }

                            text += "\n\n" + entry.get("drug");

                            String method = (String)entry.get("method");

                            text += "\n" + method;

                            if (entry.get("old_count") != null) {
                                if (method.equals("COUNT") || method.equals("MISCOUNT")) {
                                    text += "\nExpected Count: " + entry.get("old_count");
                                }
                                else if (other_methods.contains(method)) {
                                    text += "\nRemaining Count: " + entry.get("old_count");
                                }
                                else {
                                    text += "\nPrevious Count: " + entry.get("old_count");
                                }
                            }

                            if (entry.get("change") != null) {
                                if (method.equals("TOOK MEDS")) {
                                    text += "\nChange: -" + entry.get("change");
                                }
                                else if (!method.equals("COUNT") && !method.equals("MISCOUNT")) {
                                    text += "\nChange: +" + entry.get("change");
                                }
                                else {
                                    text += "\nDiscrepancy: " + entry.get("change");
                                }
                            }

                            if (entry.get("new_count") != null) {
                                if (method.equals("COUNT") || method.equals("MISCOUNT")) {
                                    text += "\nActual Count: " + entry.get("new_count");
                                }
                                else {
                                    text += "\nNew Count: " + entry.get("new_count");
                                }
                            }


                            if ((int)entry.get("dose_override") == 1) {
                                text += "\nDOSE OVERRIDE";
                            }
                            if ((int)entry.get("daily_override") == 1) {
                                text += "\nDAILY OVERRIDE";
                            }

                            if ((int) entry.get("edits") > 0) {
                                text += "\nEDITED " + entry.get("edits") + " TIME";
                                if ((int) entry.get("edits") > 1){
                                    text += "S";
                                }
                            }

                            List<String> notes = gson.fromJson((String) entry.get("notes"), new TypeToken<List<String>>(){}.getType());
                            for (int j = 0; j < notes.size(); j++) {
                                text += "\n" + notes.get(j);
                            }

                        }
                        info.setText(text.substring(3));
                        scroll_child.addView(info, scroll_child.indexOfChild(button) + 1);

                    }
                    else {
                        button.setTag(false);
                        scroll_child.removeViewAt(scroll_child.indexOfChild(button) + 1);
                    }
                }
            });
            scroll_child.addView(button);
        }
    }
}
