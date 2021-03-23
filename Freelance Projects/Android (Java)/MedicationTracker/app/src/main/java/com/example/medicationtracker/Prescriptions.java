package com.example.medicationtracker;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.ClientAdherence.client_adherence;
import static com.example.medicationtracker.ConfirmDischarge.confirm_discharge;
import static com.example.medicationtracker.Details.details;
import static com.example.medicationtracker.DiscontinueMultiple.discontinue_multiple;
import static com.example.medicationtracker.History.history;
import static com.example.medicationtracker.Long_To_Date.long_to_date;
import static com.example.medicationtracker.LongsToRange.longs_to_range;
import static com.example.medicationtracker.MainActivity.prescriptions_db;
import static com.example.medicationtracker.MainActivity.presets_db;
import static com.example.medicationtracker.Presets.presets;
import static com.example.medicationtracker.Refill.refill;
import static com.example.medicationtracker.SelectPreset.select_preset;
import static com.example.medicationtracker.TakeMeds.take_meds;
import static com.example.medicationtracker.AddMultiplePrescriptions.add_multiple_prescriptions;
import static com.example.medicationtracker.UpdateMultiple.update_multiple;
import static com.example.medicationtracker.Wipe.wipe;

public class Prescriptions {

    public static void prescriptions(MainActivity Activity, Map<String, Object> client) {


        boolean active = ((int)client.get("active") == 1);

        wipe(Activity, (String) client.get("name"), () -> prescriptions(Activity, client));
        LinearLayout scroll_child = Activity.scroll_child;


        String client_string = "This client was admitted on " + long_to_date((long) client.get("admit"));

        List<Map<String, Object>> prescriptions = prescriptions_db.getRows(
                new String[]{"name", "start", "end", "id", "active"},
                new String[]{"client_id="+client.get("id")},
                new String[]{"name", "ASC", "start", "DESC"}, false);

        if (!active) {
            client_string += "\nThis client was discharged on " + long_to_date((Long)client.get("discharge"));
        }

        if ((int)client.get("edits") > 0) {
            client_string += "\nThis client's information has been edited " + client.get("edits") + " time";
            if ((int)client.get("edits") > 1){
                client_string += "s";
            }
        }

        TextView admit_date = new TextView(Activity);
        admit_date.setText(client_string);
        scroll_child.addView(admit_date);

        if (active) {
            if (prescriptions.size() > 0) {
                Button take = new Button(Activity);
                take.setText("Take Medications");
                take.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<Map<String, Object>> presets = presets_db.getRows(null, new String[]{"client_id=" + client.get("id")}, null, false);

                        if (presets.size() > 0){
                            select_preset(Activity, (int) client.get("id"), presets);
                        }
                        else{
                            take_meds(Activity, (int) client.get("id"), null);
                        }
                    }
                });
                scroll_child.addView(take);

                Button presets = new Button(Activity);
                presets.setText("Manage Presets");
                presets.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        presets(Activity, (int)client.get("id"));
                    }
                });
                scroll_child.addView(presets);
            }

            Button new_script = new Button(Activity);
            new_script.setText("Add New Prescriptions");
            new_script.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    add_multiple_prescriptions(Activity, null, (int) client.get("id"), new ArrayList<>(), null);
                }
            });
            scroll_child.addView(new_script);

            if (prescriptions.size() > 0) {
                Button refill = new Button(Activity);
                refill.setText("Refill Prescriptions");
                refill.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refill(Activity, (int)client.get("id"), null);
                    }
                });
                scroll_child.addView(refill);

                Button update = new Button(Activity);
                update.setText("Update Prescriptions");
                update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        update_multiple(Activity, (int)client.get("id"), null);
                    }
                });
                scroll_child.addView(update);

                Button discontinue = new Button(Activity);
                discontinue.setText("Discontinue Prescriptions");
                discontinue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        discontinue_multiple(Activity, (int)client.get("id"), null);
                    }
                });
                scroll_child.addView(discontinue);
            }
        }

        if (prescriptions.size() > 0) {
            Button history = new Button(Activity);
            history.setText("View Medication Times");
            history.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    history(Activity, (int)client.get("id"));
                }
            });
            scroll_child.addView(history);

            Button adherence = new Button(Activity);
            adherence.setText("View Medication Adherence");
            adherence.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    client_adherence(Activity, (int)client.get("id"), (long)client.get("admit"), null);
                }
            });
            scroll_child.addView(adherence);
        }


        if (active){

            Button change_password = new Button(Activity);
            change_password.setText("Change Password");
            change_password.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //change_user_password(client);
                }
            });
            //scroll_child.addView(change_password);

            Button discharge = new Button(Activity);
            discharge.setText("Discharge Client");
            discharge.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirm_discharge(Activity, (int) client.get("id"), (String) client.get("name"));
                }
            });
            scroll_child.addView(discharge);
        }


        if (prescriptions.size() > 0) {
            TextView scripts = new TextView(Activity);
            scripts.setText("Active Prescriptions");
            scroll_child.addView(scripts);

            TextView dc = new TextView(Activity);
            dc.setText("Discontinued Prescriptions");
            scroll_child.addView(dc);

            int position = scroll_child.indexOfChild(scripts) + 1;
            int trigger = 0;

            for (int i = 0; i < prescriptions.size(); i++) {
                Map<String, Object> script = prescriptions.get(i);
                Button current_script = new Button(Activity);
                current_script.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Map<String, Object> new_script = prescriptions_db.getSingleRow(null, new String[]{"id="+script.get("id")});
                        details(Activity, new_script, null);
                    }
                });

                if ((int)script.get("active") == 0) {
                    current_script.setText(script.get("name") + longs_to_range((long) script.get("start"), (long) script.get("end")));
                    scroll_child.addView(current_script);
                    trigger++;
                }
                else {
                    current_script.setText((String)script.get("name"));
                    scroll_child.addView(current_script, position);
                    position++;
                }
            }

            if (position == scroll_child.indexOfChild(scripts) + 1){
                scroll_child.removeView(scripts);
            }

            if (trigger == 0){
                scroll_child.removeView(dc);
            }

        }


    }
}
