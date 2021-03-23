package com.example.medicationtracker;

import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.Check_User.check_user;
import static com.example.medicationtracker.Clients.clients;
import static com.example.medicationtracker.CreateSignatureView.create_signature_view;
import static com.example.medicationtracker.DatepickerToDate.datepicker_to_date;
import static com.example.medicationtracker.Datetime_To_Long.datetime_to_long;
import static com.example.medicationtracker.MainActivity.admin_mode;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.MainActivity.gson;
import static com.example.medicationtracker.MainActivity.prescriptions_db;
import static com.example.medicationtracker.MainActivity.signature_size;
import static com.example.medicationtracker.MainActivity.unweighted_params;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.MainActivity.weighted_params;
import static com.example.medicationtracker.TimepickerToTime.timepicker_to_time;
import static com.example.medicationtracker.UI_Message.ui_message;
import static com.example.medicationtracker.Wipe.wipe;

public class CountSign2 {

    public static void count_sign_2(MainActivity Activity, List<Map<String, Object>> entries, byte[] sign_1, Map<String, Object> staff_1) {

        wipe(Activity, "Second Staff Sign Off On Count", () -> count_sign_2(Activity, entries, sign_1, staff_1));
        LinearLayout scroll_child = Activity.scroll_child;
        ScrollView scroll = Activity.scroll;
        LinearLayout screen = Activity.screen;

        int client_id = (int) entries.get(0).get("client_id");

        String text = (String) clients_db.getObject("name", new String[]{"id="+client_id});

        for (int i = 0; i < entries.size(); i++) {
            Map<String, Object> entry = entries.get(i);
            if ((int) entry.get("client_id") != client_id){
                client_id = (int) entry.get("client_id");
                text += "\n\n" + clients_db.getObject("name", new String[]{"id="+client_id});
            }
            text += "\n\n" + entry.get("drug") + "\nCurrent Count: " + entry.get("new_count");
            if (entry.get("method").equals("MISCOUNT")){
                text += "\nMISCOUNT";
            }
        }

        TextView summary = new TextView(Activity);
        summary.setText(text);
        scroll_child.addView(summary);

        final SignatureView signature = create_signature_view(Activity);

        if (admin_mode){
            CheckBox manual = new CheckBox(Activity);
            manual.setText("Manual Date And Time Entry");

            manual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signature.refresh_size(scroll, unweighted_params, weighted_params, signature_size);
                    if (!manual.isChecked()) {
                        scroll_child.removeView(scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1));
                        scroll_child.removeView(scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1));
                    }
                    else {

                        DatePicker date = new DatePicker(Activity);

                        TimePicker time = new TimePicker(Activity);

                        scroll_child.addView(date, scroll_child.indexOfChild(manual) + 1);
                        scroll_child.addView(time, scroll_child.indexOfChild(date) + 1);
                    }
                }
            });
            scroll_child.addView(manual);
        }

        Button confirm = new Button(Activity);
        confirm.setText("Second Staff Sign Off");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText pass = new EditText(Activity);
                pass.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
                pass.setHint("Enter Password");
                warning.setNegativeButton("Cancel", null);
                warning.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Map<String, Object> staff_2 = check_user(pass.getText().toString());

                        if (staff_2 == null){
                            warning.setView(null);
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("OK", null);
                            warning.setTitle("Invalid password");
                            warning.setMessage("That password does not match the password of any current staff members.");
                            warning.show();
                        }
                        else {
                            String staff_sign_2 = gson.toJson(signature.getBytes());
                            String staff_sign_1 = gson.toJson(sign_1);

                            long time = System.currentTimeMillis();

                            boolean double_check = false;

                            if (admin_mode){
                                CheckBox manual = (CheckBox) scroll_child.getChildAt(scroll_child.indexOfChild(summary) + 1);
                                if (manual.isChecked()){
                                    if (!staff_2.get("class").equals("admin") && !staff_1.get("class").equals("admin")){
                                        warning.setNegativeButton("", null);
                                        warning.setView(null);
                                        warning.setPositiveButton("OK", null);
                                        warning.setTitle("No Admins Present");
                                        warning.setMessage("A staff member with administrative privileges must be present to manually enter dates and times.");
                                        warning.show();
                                        return;
                                    }

                                    double_check = true;

                                    DatePicker date_picker = (DatePicker) scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1);

                                    TimePicker time_picker = (TimePicker) scroll_child.getChildAt(scroll_child.indexOfChild(date_picker) + 1);

                                    String day = datepicker_to_date(date_picker) + " " + timepicker_to_time(time_picker);

                                    time = datetime_to_long(day);
                                }
                            }

                            for (int i = 0; i < entries.size(); i++){
                                Map<String, Object> entry = entries.get(i);

                                entry.put("datetime", time);
                                entry.put("staff_signature_1", staff_sign_1);
                                entry.put("staff_signature_2", staff_sign_2);
                                entry.put("staff_1", staff_1.get("id"));
                                entry.put("staff_2", staff_2.get("id"));
                                entry.put("staff_present", staff_1.get("name") + " and " + staff_2.get("name"));

                                if (double_check){
                                    List <String> notes = gson.fromJson((String)entry.get("notes"), new TypeToken<List<String>>(){}.getType());
                                    notes.add(0, "MANUAL DATE/TIME ENTRY");
                                    entry.put("notes", gson.toJson(notes));
                                }

                                entries_db.addRow(entry);

                                if (entry.get("method").equals("MISCOUNT")){
                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("count", entry.get("new_count"));
                                    prescriptions_db.update(updates, new String[]{"id="+entry.get("prescription_id")});
                                }

                            }
                            for (int i = 0; i < entries.size() + 3; i++){
                                history.remove(0);
                            }
                            ui_message(Activity, "Medication log updated: Counts confirmed for controlled medications.");
                            clients(Activity);
                        }
                    }
                });
                warning.setTitle("Password Required");
                warning.setView(pass);
                warning.setMessage("Please enter your password to proceed.");
                warning.show();

            }
        });
        final Button clear = new Button(Activity);
        clear.setText("Clear Canvas");
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signature.ClearCanvas();
            }
        });

        LinearLayout buttons = new LinearLayout(Activity);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.addView(clear);
        buttons.addView(confirm);
        confirm.setLayoutParams(weighted_params);

        screen.addView(signature);
        screen.addView(buttons);
    }

}
