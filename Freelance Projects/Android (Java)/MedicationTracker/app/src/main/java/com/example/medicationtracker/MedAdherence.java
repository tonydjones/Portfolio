package com.example.medicationtracker;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.LongToTime.long_to_time;
import static com.example.medicationtracker.Long_To_Date.long_to_date;
import static com.example.medicationtracker.NextDay.next_day;
import static com.example.medicationtracker.Wipe.wipe;

public class MedAdherence {
    public static void med_adherence(MainActivity Activity, List<Map<String, Object>> entries, String name, long start, long end, Map<String, Object> prescription) {

        wipe(Activity, name + " Adherence Summary", () -> med_adherence(Activity, entries, name, start, end, prescription));
        LinearLayout scroll_child = Activity.scroll_child;

        String start_date = long_to_date(start);

        String end_date = long_to_date(end);

        int i = 0;

        while (!start_date.equals(end_date)){

            String text = "";

            float taken = 0;

            while (i < entries.size() && long_to_date((long)entries.get(i).get("datetime")).equals(start_date)){
                Map<String, Object> entry = entries.get(i);
                text += "\nTook " + entry.get("change") + " at " + long_to_time((long)entry.get("datetime"));
                taken += (float) entry.get("change");
                i++;
            }

            if (text.length() > 0){
                text = "\n" + text;
            }

            if (prescription.get("daily_max") != null){
                text = prescription.get("instructions") + "\n\nTook " + taken + " / " + prescription.get("daily_max") + text;
            }
            else {
                text = prescription.get("instructions") + "\n\nTook " + taken + text;
            }

            Button current_date = new Button(Activity);
            current_date.setText(start_date);
            current_date.setTag(false);
            String finalText = text;
            current_date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((Boolean) current_date.getTag()) {
                        current_date.setTag(false);
                        scroll_child.removeViewAt(scroll_child.indexOfChild(current_date) + 1);
                    } else {
                        current_date.setTag(true);

                        TextView details = new TextView(Activity);
                        details.setText(finalText);

                        scroll_child.addView(details, scroll_child.indexOfChild(current_date) + 1);
                    }
                }
            });
            scroll_child.addView(current_date);

            start_date = next_day(start_date, 1);

        }
    }
}
