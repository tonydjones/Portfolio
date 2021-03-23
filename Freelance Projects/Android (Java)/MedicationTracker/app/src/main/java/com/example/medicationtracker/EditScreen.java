package com.example.medicationtracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.AdjustFutureCounts.adjust_future_counts;
import static com.example.medicationtracker.Check_Password.check_password;
import static com.example.medicationtracker.Create_Client.create_client;
import static com.example.medicationtracker.Create_Entry.create_entry;
import static com.example.medicationtracker.Create_Prescription.create_prescription;
import static com.example.medicationtracker.DateToLong.date_to_long;
import static com.example.medicationtracker.DatepickerToDate.datepicker_to_date;
import static com.example.medicationtracker.Datetime_To_Long.datetime_to_long;
import static com.example.medicationtracker.LongToDatetime.long_to_datetime;
import static com.example.medicationtracker.LongToFullDate.long_to_full_date;
import static com.example.medicationtracker.Long_To_Date.long_to_date;
import static com.example.medicationtracker.LongsToRange.longs_to_range;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.MainActivity.gson;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.MainActivity.prescriptions_db;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.MainActivity.weighted_params;
import static com.example.medicationtracker.NextDay.next_day;
import static com.example.medicationtracker.Staff.staff;
import static com.example.medicationtracker.TimepickerToTime.timepicker_to_time;
import static com.example.medicationtracker.UI_Message.ui_message;
import static com.example.medicationtracker.Wipe.wipe;
import static com.example.medicationtracker.Edit.edit;
import static com.example.medicationtracker.EditEntries.edit_entries;

public class EditScreen {
    public static void edit_screen(MainActivity Activity, List<Map<String, Object>> object, String type){

        wipe(Activity, "Edit " + type, () -> edit_screen(Activity, object, type));
        LinearLayout scroll_child = Activity.scroll_child;

        Button edit = new Button(Activity);
        edit.setText("Save Edits");

        if (type.equals("Client")) {

            Map<String, Object> client = object.get(0);

            EditText name = new EditText(Activity);
            name.setText((String)client.get("name"));
            name.setHint("Full Name");
            scroll_child.addView(name);

            CheckBox active = new CheckBox(Activity);
            active.setText("Active");
            scroll_child.addView(active);

            CheckBox change = new CheckBox(Activity);
            change.setText("Change Admission Timestamp: " + long_to_datetime((long)client.get("admit")));
            scroll_child.addView(change);

            CheckBox change_discharge = new CheckBox(Activity);
            change_discharge.setText("Manually Input Discharge Timestamp");

            int[] yr_mo_day_hr_min = long_to_full_date((long)client.get("admit"));

            DatePicker admit_date = new DatePicker(Activity);
            admit_date.setMaxDate(System.currentTimeMillis());
            admit_date.updateDate(yr_mo_day_hr_min[0], yr_mo_day_hr_min[1] - 1, yr_mo_day_hr_min[2]);

            TimePicker admit_time = new TimePicker(Activity);
            admit_time.setHour(yr_mo_day_hr_min[3]);
            admit_time.setMinute(yr_mo_day_hr_min[4]);

            DatePicker discharge_date = new DatePicker(Activity);
            discharge_date.setMaxDate(System.currentTimeMillis());
            TimePicker discharge_time = new TimePicker(Activity);

            if ((int)client.get("active") == 1){
                active.setChecked(true);
            }
            else {
                change_discharge.setText("Change Discharge Timestamp: " + long_to_datetime((long)client.get("discharge")));
                scroll_child.addView(change_discharge);

                yr_mo_day_hr_min = long_to_full_date((long)client.get("discharge"));

                discharge_date.updateDate(yr_mo_day_hr_min[0], yr_mo_day_hr_min[1] - 1, yr_mo_day_hr_min[2]);

                discharge_time.setHour(yr_mo_day_hr_min[3]);
                discharge_time.setMinute(yr_mo_day_hr_min[4]);
            }

            active.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (active.isChecked()){
                        scroll_child.removeView(change_discharge);
                        scroll_child.removeView(discharge_date);
                        scroll_child.removeView(discharge_time);
                    }
                    else{
                        scroll_child.addView(change_discharge, scroll_child.indexOfChild(edit));
                        if (change_discharge.isChecked()){
                            scroll_child.addView(discharge_date, scroll_child.indexOfChild(change_discharge) + 1);
                            scroll_child.addView(discharge_time, scroll_child.indexOfChild(change_discharge) + 2);
                        }
                    }
                }
            });

            change_discharge.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!change_discharge.isChecked()){
                        scroll_child.removeView(discharge_date);
                        scroll_child.removeView(discharge_time);
                    }
                    else{
                        scroll_child.addView(discharge_date, scroll_child.indexOfChild(change_discharge) + 1);
                        scroll_child.addView(discharge_time, scroll_child.indexOfChild(change_discharge) + 2);
                    }
                }
            });

            change.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!change.isChecked()){
                        scroll_child.removeView(admit_date);
                        scroll_child.removeView(admit_time);
                    }
                    else{
                        scroll_child.addView(admit_date, scroll_child.indexOfChild(change) + 1);
                        scroll_child.addView(admit_time, scroll_child.indexOfChild(admit_date) + 1);
                    }
                }
            });

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (name.getText().toString().length() > 0){
                        EditText pass = new EditText(Activity);
                        pass.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        pass.setHint("Enter Admin Password");
                        warning.setNegativeButton("Cancel", null);
                        warning.setPositiveButton("Edit Data", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!check_password(pass.getText().toString(), "admin")){
                                    pass.setText("");
                                    warning.setView(null);
                                    warning.setNegativeButton("", null);
                                    warning.setPositiveButton("OK", null);
                                    warning.setTitle("Invalid password");
                                    warning.setMessage("That password does not match the password of any current administrators.");
                                    warning.show();
                                }
                                else {
                                    long time = (long)client.get("admit");
                                    if (change.isChecked()){
                                        String day = datepicker_to_date(admit_date) + " " + timepicker_to_time(admit_time);
                                        time = datetime_to_long(day);
                                    }

                                    Map<String, Object> revised = create_client(name.getText().toString(), time, "client", (String) client.get("password"));

                                    if (!active.isChecked()){
                                        revised.put("active", false);
                                        long dc_time = System.currentTimeMillis();
                                        if (change_discharge.isChecked()){
                                            String day = datepicker_to_date(discharge_date) + " " + timepicker_to_time(discharge_time);
                                            dc_time = datetime_to_long(day);
                                        }
                                        else if ((int)client.get("active") == 0){
                                            dc_time = (long) client.get("discharge");
                                        }
                                        revised.put("discharge", dc_time);
                                    }
                                    revised.put("edits", (int) client.get("edits") + 1);

                                    clients_db.update(revised, new String[]{"id="+client.get("id")});

                                    List<Map<String, Object>> prescriptions = prescriptions_db.getRows(new String[]{"start", "end", "edits", "id", "active", "count"},
                                            new String[]{"client_id="+client.get("id")}, null, false);

                                    for (int i = 0; i < prescriptions.size(); i++){
                                        boolean edited = false;
                                        Map<String, Object> prescription = prescriptions.get(i);
                                        List<Map<String, Object>> entries = new ArrayList<>();
                                        if (change.isChecked() && (long)prescription.get("start") == (long)client.get("admit")){
                                            prescription.put("start", time);
                                            edited = true;
                                            Map<String, Object> entry = entries_db.getSingleRow(new String[]{"edits", "id"},
                                                    new String[]{"prescription_id="+prescription.get("id"), "method='INTAKE'"});
                                            entry.put("datetime", time);
                                            entry.put("edits", (int)entry.get("edits") + 1);
                                            entries.add(entry);
                                        }
                                        if (!active.isChecked() && change_discharge.isChecked() && (int)prescription.get("active") == 0 &&
                                                (long)prescription.get("end") == (long)client.get("discharge")){
                                            prescription.put("end", revised.get("discharge"));
                                            edited = true;
                                            Map<String, Object> entry = entries_db.getSingleRow(new String[]{"edits", "id"},
                                                    new String[]{"prescription_id="+prescription.get("id"), "method='CLIENT DISCHARGED'"});
                                            entry.put("datetime", revised.get("discharge"));
                                            entry.put("edits", (int)entry.get("edits") + 1);
                                            entries.add(entry);
                                        }
                                        if (active.isChecked() && (int)client.get("active") == 0 && (long)prescription.get("end") == (long)client.get("discharge")){
                                            prescription.put("active", true);
                                            prescription.put("end", null);
                                            prescription.put("count", entries_db.getObject("old_count", new String[]{"prescription_id="+prescription.get("id"), "method='CLIENT DISCHARGED'"}));
                                            edited = true;
                                            entries_db.deleteRows(new String[]{"prescription_id="+prescription.get("id"), "method='CLIENT DISCHARGED'"});
                                        }
                                        if (!active.isChecked() && (int)client.get("active") == 1 && (int)prescription.get("active") == 1){
                                            prescription.put("active", false);
                                            prescription.put("end", revised.get("discharge"));
                                            edited = true;
                                            Float count = null;
                                            if (prescription.get("count") != null){
                                                count = (float)prescription.get("count");
                                            }
                                            Map<String, Object> entry = create_entry((int)client.get("id"), (int)prescription.get("id"), (String)prescription.get("name"), count,
                                                    null, null, (long)revised.get("discharge"), false, false, 1, "CLIENT DISCHARGED",
                                                    null, null, null, null, null,null, Arrays.asList("Client was discharged through editing function by Admin."));
                                            entries_db.addRow(entry);
                                        }
                                        if (edited){
                                            prescription.put("edits", (int) prescription.get("edits") + 1);
                                            prescriptions_db.update(prescription, new String[]{"id="+prescription.get("id")});
                                            for (int j = 0; j < entries.size(); j++){
                                                entries_db.update(entries.get(j), new String[]{"id="+entries.get(j).get("id")});
                                            }
                                        }
                                    }

                                    /*history.remove(0);
                                    history.remove(0);
                                    edit(Activity);*/
                                    ui_message(Activity, "The client's information was successfully edited.");
                                    Activity.onBackPressed();
                                }
                            }
                        });
                        warning.setTitle("Confirm Data Change");
                        warning.setView(pass);
                        warning.setMessage("Are you sure you want to make these changes to the client's data? You must be an administrator to do this. Please enter your password to confirm.");
                        warning.show();
                    }
                    else{
                        warning.setView(null);
                        warning.setNegativeButton("", null);
                        warning.setPositiveButton("OK", null);
                        warning.setTitle("Invalid Name");
                        warning.setMessage("You must enter a valid name for the client");
                        warning.show();
                    }
                }
            });
            scroll_child.addView(edit);

        }
        else if (type.equals("Prescription")) {
            Map<String, Object> prescription = object.get(0);

            final EditText name = new EditText(Activity);
            name.setText((String)prescription.get("drug"));
            name.setHint("Drug");
            scroll_child.addView(name);

            final EditText dose = new EditText(Activity);
            dose.setText((String)prescription.get("dose"));
            dose.setHint("Dose");
            scroll_child.addView(dose);

            final EditText instructions = new EditText(Activity);
            instructions.setText((String)prescription.get("instructions"));
            instructions.setHint("Instructions");
            scroll_child.addView(instructions);

            CheckBox as_needed = new CheckBox(Activity);
            as_needed.setText("Take As Needed");
            if ((int)prescription.get("as_needed") == 1) {
                as_needed.setChecked(true);
            }
            scroll_child.addView(as_needed);

            CheckBox controlled = new CheckBox(Activity);
            controlled.setText("Controlled");
            scroll_child.addView(controlled);

            EditText current_count = new EditText(Activity);
            current_count.setHint("Count");
            current_count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

            if ((int)prescription.get("controlled") == 1) {
                controlled.setChecked(true);
                if ((int)prescription.get("active") == 1){
                    current_count.setText(prescription.get("count").toString());
                    scroll_child.addView(current_count);
                }
                else {
                    float count = (float) entries_db.getObject("old_count", new String[]{"prescription_id="+prescription.get("id"), "datetime="+prescription.get("end")});
                    current_count.setText(String.valueOf(count));
                }
            }

            CheckBox active = new CheckBox(Activity);

            controlled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!controlled.isChecked()){
                        scroll_child.removeView(current_count);
                    }
                    else if (active.isChecked()){
                        scroll_child.addView(current_count, scroll_child.indexOfChild(controlled) + 1);
                    }
                }
            });

            final EditText dose_max = new EditText(Activity);
            if (prescription.get("dose_max") != null) {
                dose_max.setText(prescription.get("dose_max").toString());
            }
            dose_max.setHint("Maximum Dose");
            dose_max.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            scroll_child.addView(dose_max);

            final EditText daily_max = new EditText(Activity);
            if (prescription.get("daily_max") != null) {
                daily_max.setText(prescription.get("daily_max").toString());
            }
            daily_max.setHint("Daily Maximum");
            daily_max.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            scroll_child.addView(daily_max);

            final EditText indication = new EditText(Activity);
            if (prescription.get("indication") != null) {
                indication.setText((String)prescription.get("indication"));
            }
            indication.setHint("Indication");
            scroll_child.addView(indication);

            final EditText prescriber = new EditText(Activity);
            if (prescription.get("prescriber")!= null) {
                prescriber.setText((String)prescription.get("prescriber"));
            }
            prescriber.setHint("Prescriber");
            scroll_child.addView(prescriber);

            final EditText pharmacy = new EditText(Activity);
            if (prescription.get("pharmacy") != null) {
                pharmacy.setText((String)prescription.get("pharmacy"));
            }
            pharmacy.setHint("Pharmacy");
            scroll_child.addView(pharmacy);

            active.setText("Active");
            scroll_child.addView(active);

            CheckBox change = new CheckBox(Activity);
            change.setText("Change Prescription Start Timestamp: " + long_to_datetime((long)prescription.get("start")));
            scroll_child.addView(change);

            CheckBox change_discharge = new CheckBox(Activity);
            change_discharge.setText("Manually Input Discontinuation Timestamp");

            int[] yr_mo_day_hr_min = long_to_full_date((long)prescription.get("start"));

            DatePicker admit_date = new DatePicker(Activity);
            admit_date.updateDate(yr_mo_day_hr_min[0], yr_mo_day_hr_min[1] - 1, yr_mo_day_hr_min[2]);

            TimePicker admit_time = new TimePicker(Activity);
            admit_time.setHour(yr_mo_day_hr_min[3]);
            admit_time.setMinute(yr_mo_day_hr_min[4]);

            DatePicker discharge_date = new DatePicker(Activity);
            TimePicker discharge_time = new TimePicker(Activity);

            if ((int)prescription.get("active") == 1){
                active.setChecked(true);
            }
            else {
                change_discharge.setText("Change Discontinuation Timestamp: " + long_to_datetime((long)prescription.get("end")));
                scroll_child.addView(change_discharge);

                yr_mo_day_hr_min = long_to_full_date((long)prescription.get("end"));

                discharge_date.updateDate(yr_mo_day_hr_min[0], yr_mo_day_hr_min[1] - 1, yr_mo_day_hr_min[2]);

                discharge_time.setHour(yr_mo_day_hr_min[3]);
                discharge_time.setMinute(yr_mo_day_hr_min[4]);
            }

            active.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (active.isChecked()){
                        scroll_child.removeView(change_discharge);
                        scroll_child.removeView(discharge_date);
                        scroll_child.removeView(discharge_time);
                        if (controlled.isChecked()){
                            scroll_child.addView(current_count, scroll_child.indexOfChild(controlled) + 1);
                        }
                    }
                    else{
                        scroll_child.addView(change_discharge, scroll_child.indexOfChild(edit));
                        if (change_discharge.isChecked()){
                            scroll_child.addView(discharge_date, scroll_child.indexOfChild(change_discharge) + 1);
                            scroll_child.addView(discharge_time, scroll_child.indexOfChild(change_discharge) + 2);
                        }
                        scroll_child.removeView(current_count);
                    }
                }
            });

            change_discharge.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!change_discharge.isChecked()){
                        scroll_child.removeView(discharge_date);
                        scroll_child.removeView(discharge_time);
                    }
                    else{
                        scroll_child.addView(discharge_date, scroll_child.indexOfChild(change_discharge) + 1);
                        scroll_child.addView(discharge_time, scroll_child.indexOfChild(change_discharge) + 2);
                    }
                }
            });

            change.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!change.isChecked()){
                        scroll_child.removeView(admit_date);
                        scroll_child.removeView(admit_time);
                    }
                    else{
                        scroll_child.addView(admit_date, scroll_child.indexOfChild(change) + 1);
                        scroll_child.addView(admit_time, scroll_child.indexOfChild(admit_date) + 1);
                    }
                }
            });

            scroll_child.addView(edit);
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (name.getText().toString().length() > 0 && dose.getText().toString().length() > 0 && instructions.getText().toString().length() > 0) {

                        EditText pass = new EditText(Activity);
                        pass.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        pass.setHint("Enter Admin Password");
                        warning.setNegativeButton("Cancel", null);
                        warning.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!check_password(pass.getText().toString(), "admin")){
                                    pass.setText("");
                                    warning.setView(null);
                                    warning.setNegativeButton("", null);
                                    warning.setPositiveButton("OK", null);
                                    warning.setTitle("Invalid password");
                                    warning.setMessage("That password does not match the password of any current administrators.");
                                    warning.show();
                                }
                                else {
                                    Float max_dose = null;
                                    if (dose_max.getText().toString().length() > 0) {
                                        max_dose = Float.parseFloat(dose_max.getText().toString());
                                    }

                                    Float max_daily = null;
                                    if (daily_max.getText().toString().length() > 0) {
                                        max_daily = Float.parseFloat(daily_max.getText().toString());

                                    }

                                    Boolean prn = false;
                                    if (as_needed.isChecked()) {
                                        prn = true;
                                    }

                                    Boolean control = false;
                                    Float count = null;
                                    if (controlled.isChecked()) {
                                        control = true;
                                        if (active.isChecked() && current_count.getText().toString().length() > 0) {
                                            count = Float.parseFloat(current_count.getText().toString());
                                        } else if (active.isChecked()){
                                            warning.setView(null);
                                            warning.setNegativeButton("", null);
                                            warning.setPositiveButton("OK", null);
                                            warning.setTitle("Invalid Count");
                                            warning.setMessage("You must enter a valid count for controlled medications");
                                            warning.show();
                                            return;
                                        }
                                        else if ((int)prescription.get("active") == 1){
                                            count = (float) prescription.get("count");
                                        }
                                    }

                                    String reason = null;
                                    if (indication.getText().toString().length() > 0) {
                                        reason = indication.getText().toString();
                                    }

                                    String doctor = null;
                                    if (prescriber.getText().toString().length() > 0) {
                                        doctor = prescriber.getText().toString();
                                    }

                                    String pharm = null;
                                    if (pharmacy.getText().toString().length() > 0) {
                                        pharm = pharmacy.getText().toString();
                                    }

                                    long time = (long)prescription.get("start");
                                    if (change.isChecked()){
                                        String day = datepicker_to_date(admit_date) + " " + timepicker_to_time(admit_time);
                                        time = datetime_to_long(day);
                                    }

                                    Map<String, Object> revised = create_prescription((int)prescription.get("client_id"), name.getText().toString(),
                                            dose.getText().toString(), max_dose, max_daily, instructions.getText().toString(),
                                            prn, control, count, reason, doctor, pharm, time);

                                    if (!active.isChecked()){
                                        revised.put("active", false);
                                        long dc_time = System.currentTimeMillis();
                                        if (change_discharge.isChecked()){
                                            String day = datepicker_to_date(discharge_date) + " " + timepicker_to_time(discharge_time);
                                            dc_time = datetime_to_long(day);
                                        }
                                        else if ((int)prescription.get("active") == 0){
                                            dc_time = (long) prescription.get("end");
                                        }
                                        revised.put("end", dc_time);
                                    }
                                    revised.put("edits", (int) prescription.get("edits") + 1);

                                    prescriptions_db.update(revised, new String[]{"id="+prescription.get("id")});

                                    Float new_dose = null;
                                    Float new_daily = null;
                                    boolean check_dose = false;
                                    boolean check_daily = false;

                                    if (revised.get("dose_max") != null){
                                        new_dose = (float)revised.get("dose_max");
                                        if (prescription.get("dose_max") == null || (float)revised.get("dose_max") != (float)prescription.get("dose_max")){
                                            check_dose = true;
                                        }
                                    }
                                    if (revised.get("daily_max") != null){
                                        new_daily = (float)revised.get("daily_max");
                                        if (prescription.get("daily_max") == null || (float)revised.get("daily_max") != (float)prescription.get("daily_max")){
                                            check_daily = true;
                                        }
                                    }

                                    Long start_time = null;
                                    if (change.isChecked()){
                                        start_time = time;
                                    }

                                    Boolean active_bool = null;
                                    Long end_time = null;
                                    if (!active.isChecked() && (int)prescription.get("active") == 1){
                                        active_bool = false;
                                        end_time = (long)revised.get("end");
                                    }
                                    else if (active.isChecked() && (int)prescription.get("active") == 0){
                                        active_bool = true;
                                    }

                                    if (!active.isChecked() && change_discharge.isChecked()){
                                        end_time = (long)revised.get("end");
                                    }

                                    Boolean switch_controlled = false;
                                    if (control && (int)prescription.get("controlled") == 0){
                                        switch_controlled = true;
                                    }

                                    Float real_count = count;
                                    Float change = null;
                                    Float old_count = null;
                                    Long count_time = null;
                                    boolean reactivate = false;
                                    if (control && count != null  && (int)prescription.get("controlled") == 1){
                                        if ((int)prescription.get("active") == 1){
                                            if (count != (float)prescription.get("count")){
                                                old_count = (float)prescription.get("count");
                                                change = real_count - old_count;
                                                if (active.isChecked()){
                                                    count_time = System.currentTimeMillis();
                                                }
                                                else {
                                                    count_time = (long)revised.get("end");
                                                }
                                            }
                                        }
                                        else {
                                            float previous_count = (float) entries_db.getObject("old_count", new String[]{"prescription_id="+prescription.get("id"), "datetime="+prescription.get("end")});
                                            if (count != previous_count){
                                                old_count = previous_count;
                                                change = real_count - old_count;
                                                if (active.isChecked()){
                                                    count_time = System.currentTimeMillis();
                                                }
                                                else {
                                                    count_time = (long)revised.get("end");
                                                }
                                                reactivate = true;
                                            }
                                        }
                                    }

                                    if (count_time == null && switch_controlled){
                                        if (active.isChecked()){
                                            count_time = System.currentTimeMillis();
                                        }
                                        else {
                                            count_time = (long)revised.get("end");
                                        }
                                    }

                                    edit_entries((int)prescription.get("client_id"), (int)prescription.get("id"), new_dose, new_daily,
                                            start_time, end_time, count_time, (String)revised.get("name"), active_bool, switch_controlled,
                                            real_count, change, old_count, check_dose, check_daily, reactivate);

                                    /*history.remove(0);
                                    history.remove(0);
                                    ui_message(Activity, "The prescription was successfully edited.");
                                    edit(Activity);*/
                                    ui_message(Activity, "The prescription was successfully edited.");
                                    Activity.onBackPressed();
                                }
                            }
                        });
                        warning.setTitle("Confirm Data Change");
                        warning.setView(pass);
                        warning.setMessage("Are you sure you want to make these changes to the prescription? You must be an administrator to do this. Please enter your password to confirm.");
                        warning.show();
                    }
                    else{
                        warning.setView(null);
                        warning.setNegativeButton("", null);
                        warning.setPositiveButton("OK", null);
                        warning.setTitle("Missing Information");
                        warning.setMessage("Drug, dose, and instructions are required fields");
                        warning.show();
                    }
                }
            });
        }

        else {

            List<String> other_methods = Arrays.asList("CLIENT DISCHARGED", "COUNT", "MISCOUNT", "DISCONTINUED DUE TO UPDATE", "PRESCRIPTION DISCONTINUED");

            for (int i = 0; i < object.size(); i++) {
                Map<String, Object> entry = object.get(i);

                Button button = new Button(Activity);

                if (entry.get("method").equals("UPDATED PRESCRIPTION STARTED")){
                    button.setText(entry.get("drug") + " (Updated)");
                }
                else if (entry.get("method").equals("DISCONTINUED DUE TO UPDATE")){
                    button.setText(entry.get("drug") + " (Discontinued)");
                }
                else {
                    button.setText((String)entry.get("drug"));
                }
                button.setTag(false);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!(Boolean) button.getTag()) {
                            button.setTag(true);

                            LinearLayout stuff = new LinearLayout(Activity);
                            stuff.setOrientation(LinearLayout.VERTICAL);
                            scroll_child.addView(stuff, scroll_child.indexOfChild(button) + 1);

                            TextView info = new TextView(Activity);
                            String text = (String)entry.get("drug");

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
                            info.setText(text);
                            stuff.addView(info);

                            CheckBox delete = new CheckBox(Activity);
                            delete.setText("Delete This Entry");
                            stuff.addView(delete);

                            if (entry.get("method").equals("TOOK MEDS") || entry.get("method").equals("REFILL") || entry.get("method").equals("INTAKE") ||
                                    entry.get("method").equals("PRESCRIPTION STARTED") || entry.get("method").equals("UPDATED PRESCRIPTION STARTED")){
                                CheckBox change = new CheckBox(Activity);
                                if (entry.get("method").equals("TOOK MEDS")){
                                    change.setText("Change Taken Amount");
                                }
                                else if (entry.get("method").equals("REFILL")){
                                    change.setText("Change Refilled Amount");
                                }
                                else {
                                    change.setText("Change Starting Amount");
                                }
                                change.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (change.isChecked()){

                                            EditText count = new EditText(Activity);
                                            count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                                            if (entry.get("method").equals("TOOK MEDS")){
                                                count.setHint("Taken Amount");
                                            }
                                            else if (entry.get("method").equals("REFILL")){
                                                count.setHint("Refilled Amount");
                                            }
                                            else {
                                                count.setHint("Starting Amount");
                                            }
                                            if (entry.get("change") != null){
                                                count.setText(String.valueOf((float)entry.get("change")));
                                            }
                                            stuff.addView(count);
                                        }
                                        else {
                                            stuff.removeViewAt(stuff.indexOfChild(change) + 1);
                                        }

                                    }
                                });
                                stuff.addView(change);
                            }
                            else if (entry.get("method").equals("COUNT") || entry.get("method").equals("MISCOUNT")){
                                CheckBox change = new CheckBox(Activity);
                                change.setText("Change Counted Amount");
                                change.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (change.isChecked()){

                                            EditText count = new EditText(Activity);
                                            count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                                            count.setHint("Counted Amount");
                                            count.setText(String.valueOf((float)entry.get("new_count")));
                                            stuff.addView(count);
                                        }
                                        else {
                                            stuff.removeViewAt(stuff.indexOfChild(change) + 1);
                                        }

                                    }
                                });
                                stuff.addView(change);
                            }
                        }
                        else {
                            button.setTag(false);
                            scroll_child.removeViewAt(scroll_child.indexOfChild(button) + 1);
                        }
                    }
                });
                scroll_child.addView(button);
            }

            CheckBox add_new = new CheckBox(Activity);
            List<Map<String, Object>> possible = null;

            if (type.equals("Entry Group") && (object.get(0).get("method").equals("TOOK MEDS") || object.get(0).get("method").equals("REFILL"))){

                possible = prescriptions_db.getRows(null,
                        new String[]{"client_id="+object.get(0).get("client_id")}, new String[]{"name", "ASC"}, false);
                List<Integer> ids = new ArrayList<>();
                for (int i = 0; i < object.size(); i++){
                    ids.add((Integer)object.get(i).get("prescription_id"));
                }
                for (int i = possible.size() - 1; i > -1; i--){
                    if (ids.contains(possible.get(i).get("id")) ||
                            (long) possible.get(i).get("start") > (long)object.get(0).get("datetime") ||
                            ((int)possible.get(i).get("active") == 0 && (long) possible.get(i).get("end") < (long)object.get(0).get("datetime"))){
                        possible.remove(i);
                    }
                }
                if (possible.size() > 0){
                    add_new.setText("Add new Entries to this entry group");
                    scroll_child.addView(add_new);
                    List<Map<String, Object>> finalPossible1 = possible;
                    add_new.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (add_new.isChecked()){
                                LinearLayout stuff = new LinearLayout(Activity);
                                stuff.setOrientation(LinearLayout.VERTICAL);
                                scroll_child.addView(stuff, scroll_child.indexOfChild(add_new) + 1);
                                for (int i = 0; i < finalPossible1.size(); i++){
                                    Map<String, Object> prescription = finalPossible1.get(i);
                                    Button script = new Button(Activity);
                                    script.setTag(false);
                                    if ((int)prescription.get("active") == 1){
                                        script.setText((String)prescription.get("name"));
                                    }
                                    else {
                                        script.setText(prescription.get("name") + longs_to_range((long) prescription.get("start"), (long) prescription.get("end")));
                                    }
                                    stuff.addView(script);
                                    int finalI = i;
                                    script.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (!(boolean)script.getTag()){
                                                script.setTag(true);
                                                if (object.get(0).get("method").equals("TOOK MEDS")){

                                                    EditText current_count = new EditText(Activity);
                                                    current_count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                                                    current_count.setText(String.valueOf(0));

                                                    Button add = new Button(Activity);
                                                    add.setText("Add");
                                                    add.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            current_count.setText(String.valueOf(Float.parseFloat(current_count.getText().toString()) + 1));
                                                        }
                                                    });
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
                                                    subtract.setTag(prescription.get("id"));
                                                    subtract.setLayoutParams(weighted_params);

                                                    LinearLayout buttons = new LinearLayout(Activity);
                                                    buttons.setOrientation(LinearLayout.HORIZONTAL);
                                                    buttons.addView(subtract);
                                                    buttons.addView(add);

                                                    EditText notes = new EditText(Activity);
                                                    notes.setHint("Notes");
                                                    notes.setTag(finalI);

                                                    stuff.addView(current_count, stuff.indexOfChild(script) + 1);
                                                    stuff.addView(buttons, stuff.indexOfChild(script) + 2);
                                                    stuff.addView(notes, stuff.indexOfChild(script) + 3);
                                                }
                                                else {
                                                    EditText current_count = new EditText(Activity);
                                                    current_count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                                                    current_count.setHint("Refill Amount");

                                                    current_count.setTag((int) prescription.get("controlled") == 1);
                                                    stuff.addView(current_count, stuff.indexOfChild(script) + 1);
                                                }
                                            }
                                            else {
                                                script.setTag(false);
                                                stuff.removeViewAt(stuff.indexOfChild(script) + 1);
                                                if (object.get(0).get("method").equals("TOOK MEDS")){
                                                    stuff.removeViewAt(stuff.indexOfChild(script) + 1);
                                                    stuff.removeViewAt(stuff.indexOfChild(script) + 1);
                                                }
                                            }

                                        }
                                    });
                                }
                            }
                            else{
                                scroll_child.removeViewAt(scroll_child.indexOfChild(add_new) + 1);
                            }
                        }
                    });
                }

            }


            int[] yr_mo_day_hr_min = long_to_full_date((long)object.get(0).get("datetime"));

            DatePicker admit_date = new DatePicker(Activity);
            admit_date.setMaxDate(System.currentTimeMillis());
            admit_date.updateDate(yr_mo_day_hr_min[0], yr_mo_day_hr_min[1] - 1, yr_mo_day_hr_min[2]);

            TimePicker admit_time = new TimePicker(Activity);
            admit_time.setHour(yr_mo_day_hr_min[3]);
            admit_time.setMinute(yr_mo_day_hr_min[4]);

            CheckBox change = new CheckBox(Activity);
            change.setText("Change Entry Timestamp: " + long_to_datetime((long)object.get(0).get("datetime")));
            scroll_child.addView(change);

            change.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!change.isChecked()){
                        scroll_child.removeView(admit_date);
                        scroll_child.removeView(admit_time);
                    }
                    else{
                        scroll_child.addView(admit_date, scroll_child.indexOfChild(change) + 1);
                        scroll_child.addView(admit_time, scroll_child.indexOfChild(admit_date) + 1);
                    }
                }
            });

            scroll_child.addView(edit);
            List<Map<String, Object>> finalPossible = possible;
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText pass = new EditText(Activity);
                    pass.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    pass.setHint("Enter Admin Password");
                    warning.setNegativeButton("Cancel", null);
                    warning.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!check_password(pass.getText().toString(), "admin")){
                                pass.setText("");
                                warning.setView(null);
                                warning.setNegativeButton("", null);
                                warning.setPositiveButton("OK", null);
                                warning.setTitle("Invalid password");
                                warning.setMessage("That password does not match the password of any current administrators.");
                                warning.show();
                            }
                            else {
                                int add = 0;
                                Long time = null;
                                if (change.isChecked()){
                                    String day = datepicker_to_date(admit_date) + " " + timepicker_to_time(admit_time);
                                    time = datetime_to_long(day);
                                }
                                else {
                                    time = (long)object.get(0).get("datetime");
                                }
                                for (int i = 0; i < object.size(); i++){
                                    Map<String, Object> entry = object.get(i);
                                    boolean edited = false;
                                    if (change.isChecked()){
                                        entry.put("datetime", time);
                                        edited = true;
                                    }
                                    if ((Boolean)scroll_child.getChildAt(i + add).getTag()){
                                        add++;
                                        LinearLayout stuff = (LinearLayout) scroll_child.getChildAt(i + add);
                                        CheckBox delete = (CheckBox)stuff.getChildAt(1);
                                        if (delete.isChecked()){
                                            if ((int)prescriptions_db.getObject("controlled", new String[]{"id="+entry.get("prescription_id")}) == 1){
                                                if (entry.get("method").equals("TOOK MEDS")){
                                                    adjust_future_counts((int)entry.get("prescription_id"), (float)entry.get("change"), (long)entry.get("datetime"));
                                                }
                                                else if (entry.get("change") != null){
                                                    adjust_future_counts((int)entry.get("prescription_id"), -1 * (float)entry.get("change"), (long)entry.get("datetime"));
                                                }
                                            }
                                            entries_db.delete_single_constraint("id="+entry.get("id"));
                                            continue;
                                        }
                                        else if (entry.get("method").equals("TOOK MEDS") || entry.get("method").equals("REFILL") || entry.get("method").equals("INTAKE") ||
                                                entry.get("method").equals("PRESCRIPTION STARTED") || entry.get("method").equals("UPDATED PRESCRIPTION STARTED")){
                                            CheckBox change = (CheckBox)stuff.getChildAt(2);
                                            if (change.isChecked()){
                                                EditText count = (EditText) stuff.getChildAt(3);
                                                Float updated_count = null;
                                                if (count.getText().toString().length() > 0){
                                                    updated_count = Float.parseFloat(count.getText().toString());
                                                    if (updated_count <= 0){
                                                        warning.setView(null);
                                                        warning.setNegativeButton("", null);
                                                        warning.setPositiveButton("OK", null);
                                                        warning.setTitle("Invalid Count for " + entry.get("drug"));
                                                        warning.setMessage(updated_count + " is not a valid amount for taking meds, refilling meds, or starting a prescription. This entry will not be altered.");
                                                        warning.show();
                                                        continue;
                                                    }
                                                }
                                                else if ((int)prescriptions_db.getObject("controlled", new String[]{"id="+entry.get("prescription_id")}) == 1){
                                                    warning.setView(null);
                                                    warning.setNegativeButton("", null);
                                                    warning.setPositiveButton("OK", null);
                                                    warning.setTitle("Invalid Count for " + entry.get("drug"));
                                                    warning.setMessage("A valid amount must be input for controlled medications. This entry will not be altered.");
                                                    warning.show();
                                                    continue;
                                                }
                                                else if (entry.get("method").equals("TOOK MEDS")){
                                                    warning.setView(null);
                                                    warning.setNegativeButton("", null);
                                                    warning.setPositiveButton("OK", null);
                                                    warning.setTitle("Invalid Count for " + entry.get("drug"));
                                                    warning.setMessage("A valid amount must be input for taking medications. This entry will not be altered.");
                                                    warning.show();
                                                    continue;
                                                }

                                                Float difference = null;
                                                if (entry.get("change") != null){
                                                    difference = updated_count - (float) entry.get("change");
                                                }
                                                entry.put("change", updated_count);
                                                if ((int)prescriptions_db.getObject("controlled", new String[]{"id="+entry.get("prescription_id")}) == 1){
                                                    if (entry.get("method").equals("TOOK MEDS")){
                                                        entry.put("new_count", (float)entry.get("new_count") - difference);
                                                        adjust_future_counts((int)entry.get("prescription_id"), -1 * difference, (long)entry.get("datetime"));
                                                    }
                                                    else {
                                                        entry.put("new_count", (float)entry.get("new_count") + difference);
                                                        adjust_future_counts((int)entry.get("prescription_id"), difference, (long)entry.get("datetime"));
                                                    }
                                                }
                                                edited = true;
                                            }
                                        }
                                        else if (entry.get("method").equals("COUNT") || entry.get("method").equals("MISCOUNT")){
                                            CheckBox change = (CheckBox)stuff.getChildAt(2);
                                            if (change.isChecked()){
                                                EditText count = (EditText) stuff.getChildAt(3);
                                                Float updated_count = null;
                                                if (count.getText().toString().length() > 0){
                                                    updated_count = Float.parseFloat(count.getText().toString());
                                                    if (updated_count <= 0){
                                                        warning.setView(null);
                                                        warning.setNegativeButton("", null);
                                                        warning.setPositiveButton("OK", null);
                                                        warning.setTitle("Invalid Count for " + entry.get("drug"));
                                                        warning.setMessage(updated_count + " is not a valid amount for counting meds. This entry will not be altered.");
                                                        warning.show();
                                                        continue;
                                                    }
                                                }
                                                else{
                                                    warning.setView(null);
                                                    warning.setNegativeButton("", null);
                                                    warning.setPositiveButton("OK", null);
                                                    warning.setTitle("Invalid Count for " + entry.get("drug"));
                                                    warning.setMessage("A valid amount must be input for counting medications. This entry will not be altered.");
                                                    warning.show();
                                                    continue;
                                                }

                                                Float difference = updated_count - (float) entry.get("new_count");
                                                entry.put("new_count", updated_count);
                                                entry.put("change", (float)entry.get("change") + difference);
                                                if ((float)entry.get("change") == 0){
                                                    entry.put("method", "COUNT");
                                                }
                                                else{
                                                    entry.put("method", "MISCOUNT");
                                                }
                                                adjust_future_counts((int)entry.get("prescription_id"), difference, (long)entry.get("datetime"));
                                                edited = true;
                                            }
                                        }
                                    }
                                    if (edited){
                                        entry.put("edits", (int)entry.get("edits") + 1);
                                        entries_db.update(entry, new String[]{"id="+entry.get("id")});
                                    }
                                }

                                if (add_new.isChecked()){
                                    LinearLayout stuff = (LinearLayout)scroll_child.getChildAt(scroll_child.indexOfChild(add_new) + 1);
                                    add = 0;
                                    for (int i = 0; i < finalPossible.size(); i++){
                                        if ((Boolean)stuff.getChildAt(i + add).getTag()){
                                            add++;
                                            int text_location = i + add;
                                            if (object.get(0).get("method").equals("TOOK MEDS")){
                                                add += 2;
                                            }
                                            EditText count = (EditText) stuff.getChildAt(text_location);
                                            Float updated_count = null;
                                            if (count.getText().toString().length() > 0){
                                                updated_count = Float.parseFloat(count.getText().toString());
                                                if (updated_count <= 0){
                                                    warning.setView(null);
                                                    warning.setNegativeButton("", null);
                                                    warning.setPositiveButton("OK", null);
                                                    warning.setTitle("Invalid Count for " + finalPossible.get(i).get("name"));
                                                    warning.setMessage(updated_count + " is not a valid amount for taking meds, or refilling meds. This entry will not be added.");
                                                    warning.show();
                                                    continue;
                                                }
                                            }
                                            else if ((int)finalPossible.get(i).get("controlled") == 1){
                                                warning.setView(null);
                                                warning.setNegativeButton("", null);
                                                warning.setPositiveButton("OK", null);
                                                warning.setTitle("Invalid Count for " + finalPossible.get(i).get("name"));
                                                warning.setMessage("A valid amount must be input for controlled medications. This entry will not be added.");
                                                warning.show();
                                                continue;
                                            }
                                            else if (object.get(0).get("method").equals("TOOK MEDS")){
                                                warning.setView(null);
                                                warning.setNegativeButton("", null);
                                                warning.setPositiveButton("OK", null);
                                                warning.setTitle("Invalid Count for " + finalPossible.get(i).get("name"));
                                                warning.setMessage("A valid amount must be input for taking medications. This entry will not be added.");
                                                warning.show();
                                                continue;
                                            }
                                            Float old_count = null;
                                            Float new_count = null;

                                            if ((int)finalPossible.get(i).get("controlled") == 1){
                                                long previous_time = (long) entries_db.getObject("MAX([datetime]) AS datetime",
                                                        new String[]{"prescription_id="+(int)finalPossible.get(i).get("id"), "datetime<"+time});
                                                old_count = (float) entries_db.getObject("new_count",
                                                        new String[]{"prescription_id="+(int)finalPossible.get(i).get("id"), "datetime="+previous_time});
                                                if (object.get(0).get("method").equals("TOOK MEDS")){
                                                    new_count = old_count - updated_count;
                                                }
                                                else {
                                                    new_count = old_count + updated_count;
                                                }
                                            }

                                            boolean dose_override = false;
                                            boolean daily_override = false;
                                            if (object.get(0).get("method").equals("TOOK MEDS")){
                                                if (finalPossible.get(i).get("dose_max") != null && (float)finalPossible.get(i).get("dose_max") < updated_count){
                                                    dose_override = true;
                                                }
                                                if (finalPossible.get(i).get("daily_max") != null){
                                                    String date = long_to_date(time);
                                                    long start = date_to_long(date);
                                                    long end = date_to_long(next_day(date, 1));

                                                    List<Map<String, Object>> taken = entries_db.getRows(
                                                            new String[]{"id", "change", "prescription_id"},
                                                            new String[]{"method='TOOK MEDS'", "datetime>="+start, "datetime<"+end, "prescription_id="+finalPossible.get(i).get("id")},
                                                            null, false
                                                    );
                                                    float daily_count = 0;
                                                    for (int j = 0; j < taken.size(); j++){
                                                        daily_count += (float)taken.get(j).get("change");
                                                    }
                                                    if (daily_count + updated_count > (float)finalPossible.get(i).get("daily_max")){
                                                        daily_override = true;
                                                    }
                                                }
                                            }

                                            List<String> notes = new ArrayList<>();
                                            notes.add("This entry was added through the editing function by Admin");
                                            if (object.get(0).get("method").equals("TOOK MEDS")){
                                                EditText note = (EditText) stuff.getChildAt(i + add);
                                                if (note.getText().toString().length() > 0){
                                                    notes.add(note.getText().toString());
                                                }
                                            }
                                            Map<String, Object> entry = create_entry((int)finalPossible.get(i).get("client_id"), (int)finalPossible.get(i).get("id"),
                                                    (String)finalPossible.get(i).get("name"), old_count, updated_count, new_count, time, dose_override, daily_override, 1,
                                                    (String)object.get(0).get("method"), null, null,null, null, null, null, notes);

                                            entries_db.addRow(entry);
                                            if ((int)finalPossible.get(i).get("controlled") == 1){
                                                if (object.get(0).get("method").equals("TOOK MEDS")){
                                                    adjust_future_counts((int)finalPossible.get(i).get("id"), -1 * updated_count, time);
                                                }
                                                else {
                                                    adjust_future_counts((int)finalPossible.get(i).get("id"), updated_count, time);
                                                }
                                            }
                                        }
                                    }
                                }
                                /*history.remove(0);
                                history.remove(0);
                                ui_message(Activity, "The relevant entries have been successfully edited.");
                                edit(Activity);*/
                                ui_message(Activity, "The relevant entries have been successfully edited.");
                                Activity.onBackPressed();
                            }
                        }
                    });
                    warning.setTitle("Confirm Data Change");
                    warning.setView(pass);
                    warning.setMessage("Are you sure you want to make these changes? You must be an administrator to do this. Please enter your password to confirm.");
                    warning.show();
                }

            });

        }
    }
}
