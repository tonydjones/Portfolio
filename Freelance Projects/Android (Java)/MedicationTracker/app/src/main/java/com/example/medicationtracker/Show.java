package com.example.medicationtracker;

import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.Wipe.wipe;
import static com.example.medicationtracker.MainActivity.clients_db;

public class Show {
    public static void show(MainActivity Activity){
        wipe(Activity, "Display staff", () -> show(Activity));
        LinearLayout scroll_child = Activity.scroll_child;

        List<Map<String, Object>> staff = clients_db.getRows(null, new String[]{"class IN ('staff','admin')"}, null, false);
        String text = "";
        for (int i = 0; i < staff.size(); i++){
            text += "\n\n" + staff.get(i).get("name");
            text += "\n" + staff.get(i).get("password");
            text += "\n" + staff.get(i).get("class");
        }

        TextView display = new TextView(Activity);
        display.setText(text);
        scroll_child.addView(display);
    }
}
