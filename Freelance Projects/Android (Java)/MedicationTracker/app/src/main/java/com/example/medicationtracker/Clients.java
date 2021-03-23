package com.example.medicationtracker;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.prescriptions_db;
import static com.example.medicationtracker.Count.count;
import static com.example.medicationtracker.NewClient.new_client;
import static com.example.medicationtracker.Prescriptions.prescriptions;
import static com.example.medicationtracker.Wipe.wipe;

public class Clients {
    public static void clients(MainActivity Activity){

        wipe(Activity, "Current Clients", () -> clients(Activity));
        LinearLayout scroll_child = Activity.scroll_child;

        Button med_count = new Button(Activity);
        med_count.setText("Med Count");
        med_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Map<String, Object>> prescriptions = prescriptions_db.getRows(
                        new String[]{"id", "name", "client_id", "count", "prescriber", "pharmacy", "instructions"},
                        new String[]{"active=1", "controlled=1", "count>0"},
                        new String[]{"client_id", "ASC", "id", "ASC"}, false
                );
                count(Activity, prescriptions, new ArrayList<>(), null);
            }
        });
        scroll_child.addView(med_count);

        Button new_client = new Button(Activity);
        new_client.setText("Add A New Client");
        new_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new_client(Activity, null);
            }
        });
        scroll_child.addView(new_client);

        TextView current = new TextView(Activity);
        current.setText("Current Clients");
        scroll_child.addView(current);

        TextView discharged = new TextView(Activity);
        discharged.setText("Discharged Clients");
        scroll_child.addView(discharged);

        ArrayList<Map<String, Object>> clients = clients_db.getRows(null, new String[]{"class='client'"}, new String[]{"name", "ASC"}, false);

        int position = scroll_child.indexOfChild(current) + 1;
        int trigger = 0;

        for (int i = 0; i < clients.size(); i++) {
            Map<String, Object> client = clients.get(i);
            Button current_client = new Button(Activity);
            current_client.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, Object> current = clients_db.getSingleRow(null, new String[]{"id="+client.get("id")});
                    prescriptions(Activity, current);
                }
            });


            if ((int) client.get("active") == 0) {
                current_client.setText(client.get("name") + LongsToRange.longs_to_range((long)client.get("admit"), (long)client.get("discharge")));

                scroll_child.addView(current_client);
                trigger++;
            }
            else {
                current_client.setText((String) client.get("name"));
                scroll_child.addView(current_client, position);
                position++;
            }
        }

        if (position == scroll_child.indexOfChild(current) + 1){
            scroll_child.removeView(current);
            scroll_child.removeView(med_count);
            if (trigger == 0){
                scroll_child.removeView(discharged);
            }
        }
        else if (trigger == 0){
            scroll_child.removeView(discharged);
        }
    }
}
