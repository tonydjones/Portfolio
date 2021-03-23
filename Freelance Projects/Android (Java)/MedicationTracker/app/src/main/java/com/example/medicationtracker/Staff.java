package com.example.medicationtracker;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import static com.example.medicationtracker.MainActivity.admin_mode;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.NewStaff.new_staff;
import static com.example.medicationtracker.StaffSummary.staff_summary;
import static com.example.medicationtracker.Wipe.wipe;

public class Staff {
    public static void staff(MainActivity Activity){
        wipe(Activity, "Current Staff", () -> staff(Activity));
        LinearLayout scroll_child = Activity.scroll_child;

        if (admin_mode){
            Button new_client = new Button(Activity);
            new_client.setText("Add A New Staff Member");
            new_client.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new_staff(Activity);
                }
            });
            scroll_child.addView(new_client);
        }

        ArrayList<Map<String, Object>> clients = clients_db.getRows(null, new String[]{"class IN ('admin','staff') AND active=1"}, new String[]{"name", "ASC"}, false);

        if (clients.size() > 0){
            TextView current = new TextView(Activity);
            current.setText("Current Staff");
            scroll_child.addView(current);

            for (int i = 0; i < clients.size(); i++) {
                Map<String, Object> client = clients.get(i);
                Button current_client = new Button(Activity);
                current_client.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        staff_summary(Activity, client);
                    }
                });
                current_client.setText((String) client.get("name"));
                scroll_child.addView(current_client);
            }
        }

        clients = clients_db.getRows(null, new String[]{"class IN ('admin','staff') AND active=0"}, new String[]{"name", "ASC"}, false);
        if (clients.size() > 0){
            TextView former = new TextView(Activity);
            former.setText("former Staff");
            scroll_child.addView(former);

            for (int i = 0; i < clients.size(); i++) {
                Map<String, Object> client = clients.get(i);
                Button current_client = new Button(Activity);
                current_client.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        staff_summary(Activity, client);
                    }
                });
                current_client.setText((String) client.get("name"));
                scroll_child.addView(current_client);
            }
        }
    }
}
