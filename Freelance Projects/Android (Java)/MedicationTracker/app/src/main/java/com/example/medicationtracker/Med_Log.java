package com.example.medicationtracker;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.LongToDatetime.long_to_datetime;
import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.MainActivity.gson;
import static com.example.medicationtracker.Wipe.wipe;

public class Med_Log {
    public static void log(MainActivity Activity, Map<String, Object> prescription) {
        wipe(Activity, prescription.get("name") + " Log", () -> log(Activity, prescription));
        LinearLayout scroll_child = Activity.scroll_child;


        List<Map<String, Object>> entries = entries_db.getRows(null, new String[]{"prescription_id="+prescription.get("id")}, new String[]{"datetime", "DESC"}, false);

        List<String> other_methods = Arrays.asList("CLIENT DISCHARGED", "COUNT", "MISCOUNT", "DISCONTINUED DUE TO UPDATE", "PRESCRIPTION DISCONTINUED");
        for (int i = 0; i < entries.size(); i++) {
            Map<String, Object> entry = entries.get(i);
            TextView information = new TextView(Activity);
            String text = "";
            text += long_to_datetime((long) entry.get("datetime"));
            text += "\nStaff Present: " + entry.get("staff_present");
            //text += "\n" + entry.get("drug");
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

            if ((int) entry.get("dose_override") == 1) {
                text += "\nMAXIMUM DOSE OVERRIDE";
            }

            if ((int) entry.get("daily_override") == 1) {
                text += "\nDAILY MAXIMUM OVERRIDE";
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

            information.setText(text + "\n");
            scroll_child.addView(information);
        }
    }
}
