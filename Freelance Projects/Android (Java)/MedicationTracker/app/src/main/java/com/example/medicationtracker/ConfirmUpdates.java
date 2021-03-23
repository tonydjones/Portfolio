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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.Check_User.check_user;
import static com.example.medicationtracker.CountSign2.count_sign_2;
import static com.example.medicationtracker.CreateSignatureView.create_signature_view;
import static com.example.medicationtracker.Create_Entry.create_entry;
import static com.example.medicationtracker.DatepickerToDate.datepicker_to_date;
import static com.example.medicationtracker.Datetime_To_Long.datetime_to_long;
import static com.example.medicationtracker.MainActivity.admin_mode;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.MainActivity.gson;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.MainActivity.prescriptions_db;
import static com.example.medicationtracker.MainActivity.signature_size;
import static com.example.medicationtracker.MainActivity.unweighted_params;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.MainActivity.weighted_params;
import static com.example.medicationtracker.Prescriptions.prescriptions;
import static com.example.medicationtracker.Swap_Prescriptions.swap_prescriptions;
import static com.example.medicationtracker.TimepickerToTime.timepicker_to_time;
import static com.example.medicationtracker.UI_Message.ui_message;
import static com.example.medicationtracker.Wipe.wipe;

public class ConfirmUpdates {
    public static void confirm_updates(MainActivity Activity, int client_id, List<Map<String, Object>> old_prescriptions, List<Map<String, Object>> new_prescriptions, List<String> keys) {
        wipe(Activity, "Confirm Medication Updates", () -> confirm_updates(Activity, client_id, old_prescriptions, new_prescriptions, keys));

        LinearLayout scroll_child = Activity.scroll_child;
        ScrollView scroll = Activity.scroll;
        LinearLayout screen = Activity.screen;

        String text = "";
        List<String> notes = new ArrayList<>();
        for (int i = 0; i < new_prescriptions.size(); i++) {

            String note = "";

            Map<String, Object> new_script = new_prescriptions.get(i);
            Map<String, Object> old_script = old_prescriptions.get(i);

            text += "\n\n" + old_script.get("name");

            for (int j = 0; j < keys.size(); j++){
                String key = keys.get(j);

                if (new_script.get(key) == null){
                    if (old_script.get(key) != null){
                        note += "\nRemoving " + key + ": \"" + old_script.get(key) + "\"";
                    }
                }
                else if (old_script.get(key) == null && new_script.get(key) != null){
                    note += "\nAdding " + key + ": \"" + new_script.get(key) + "\"";                }

                else if (!new_script.get(key).equals(old_script.get(key))){
                    note += "\nChanging " + key + " from \"" + old_script.get(key) +
                            "\" to \"" + new_script.get(key) + "\"";
                }
            }

            text += note;
            notes.add(note.substring(1));
        }

        TextView entry = new TextView(Activity);
        entry.setText(text.substring(2));
        scroll_child.addView(entry);

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
        confirm.setText("Staff Signoff");
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

                        Map<String, Object> staff = check_user(pass.getText().toString());

                        if (staff == null){
                            warning.setView(null);
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("OK", null);
                            warning.setTitle("Invalid password");
                            warning.setMessage("That password does not match the password of any current staff members.");
                            warning.show();
                            return;
                        }

                        byte[] staff_sign = signature.getBytes();
                        long time = System.currentTimeMillis();

                        boolean double_check = false;
                        if (admin_mode){
                            CheckBox manual = (CheckBox) scroll_child.getChildAt(scroll_child.indexOfChild(entry) + 1);
                            if (manual.isChecked()){

                                if (!staff.get("class").equals("admin")){
                                    warning.setView(null);
                                    warning.setNegativeButton("", null);
                                    warning.setPositiveButton("OK", null);
                                    warning.setTitle("No administrators present");
                                    warning.setMessage("You must be an administrator to enter dates and times manually.");
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

                        for (int i = 0; i < new_prescriptions.size(); i++){

                            Map<String, Object> new_script = new_prescriptions.get(i);
                            new_script.put("start", time);

                            Map<String, Object> old_script = old_prescriptions.get(i);
                            old_script.put("end", time);
                            old_script.put("active", false);

                            List<String> note = new ArrayList<>();
                            note.add(notes.get(i));

                            int new_id = prescriptions_db.addRow(new_script);

                            Float count = (Float)new_script.get("count");

                            if (double_check){
                                note.add(0, "MANUAL DATE/TIME ENTRY");
                                Float old_dose = null;
                                Float old_daily = null;
                                Float new_dose = null;
                                Float new_daily = null;
                                if (old_script.get("dose_max") != null){
                                    old_dose = (float)old_script.get("dose_max");
                                }
                                if (old_script.get("daily_max") != null){
                                    old_daily = (float)old_script.get("daily_max");
                                }
                                if (new_script.get("dose_max") != null){
                                    new_dose = (float)new_script.get("dose_max");
                                }
                                if (new_script.get("daily_max") != null){
                                    new_daily = (float)new_script.get("daily_max");
                                }

                                count = (float) entries_db.getRows(new String[]{"new_count"}, new String[]{"prescription_id="+old_script.get("id"), "datetime<"+time},
                                        new String[]{"datetime", "DESC"}, false).get(0).get("new_count");

                                swap_prescriptions((int)old_script.get("id"), old_dose, old_daily,
                                        new_id, new_dose, new_daily, time, (String)new_script.get("name"));

                            }

                            Float starting_count = null;
                            if (count != null){
                                starting_count = (float)0;
                            }

                            Map<String,Object> new_entry = create_entry(client_id,
                                    new_id, (String)new_script.get("name"), starting_count, count, count,
                                    time, false, false, 0,
                                    "UPDATED PRESCRIPTION STARTED", null, (int)staff.get("id"), gson.toJson(staff_sign), null,
                                    null, (String)staff.get("name"), note);

                            Map<String,Object> old_entry = create_entry(client_id,
                                    (int)old_script.get("id"), (String)old_script.get("name"), count, null,
                                    null, time, false, false, 0,
                                    "DISCONTINUED DUE TO UPDATE", null, (int)staff.get("id"),
                                    gson.toJson(staff_sign), null,null, (String)staff.get("name"), note);

                            entries_db.addRow(new_entry);
                            entries_db.addRow(old_entry);
                            old_script.put("count", null);
                            prescriptions_db.update(old_script, new String[]{"id="+old_script.get("id")});
                        }
                        for (int i = 0; i < 3; i++){
                            history.remove(0);
                        }
                        ui_message(Activity, "Medication log updated: Prescriptions successfully updated.");
                        prescriptions(Activity, clients_db.getSingleRow(null, new String[]{"id="+client_id}));
                    }
                });
                warning.setTitle("Password Required");
                warning.setView(pass);
                warning.setMessage("Please note that making these changes will discontinue the current prescriptions and start new prescriptions. The data will still be saved. " +
                        "If you are sure you want to continue, please enter your password to proceed.");
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
