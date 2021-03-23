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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.AdjustFutureCounts.adjust_future_counts;
import static com.example.medicationtracker.Check_User.check_user;
import static com.example.medicationtracker.Clients.clients;
import static com.example.medicationtracker.CreateSignatureView.create_signature_view;
import static com.example.medicationtracker.DateToLong.date_to_long;
import static com.example.medicationtracker.DatepickerToDate.datepicker_to_date;
import static com.example.medicationtracker.Datetime_To_Long.datetime_to_long;
import static com.example.medicationtracker.Long_To_Date.long_to_date;
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
import static com.example.medicationtracker.NextDay.next_day;
import static com.example.medicationtracker.Prescriptions.prescriptions;
import static com.example.medicationtracker.TimepickerToTime.timepicker_to_time;
import static com.example.medicationtracker.UI_Message.ui_message;
import static com.example.medicationtracker.Wipe.wipe;

public class Confirm {
    public static void confirm(MainActivity Activity, List<Map<String,Object>> entries, byte[] client_sign, Map<Integer, Float> maxes) {
        wipe(Activity, "Staff Confirm Medication Entries", () -> confirm(Activity, entries, client_sign, maxes));
        LinearLayout scroll_child = Activity.scroll_child;
        ScrollView scroll = Activity.scroll;
        LinearLayout screen = Activity.screen;

        String text = "";

        for (int i = 0; i < entries.size(); i++) {

            Map<String,Object> entry = entries.get(i);

            text += "\n\n" + entries.get(i).get("drug");
            text += "\nAmount Taken: " + entries.get(i).get("change");

            if ((boolean)entry.get("dose_override")){
                text += "\nMAXIMUM DOSE OVERRIDE";
            }

            if ((boolean) entry.get("daily_override")){
                text += "\nDAILY MAXIMUM OVERRIDE";
            }

            List<String> note = gson.fromJson((String) entry.get("notes"), new TypeToken<List<String>>(){}.getType());
            for (int j = 0; j < note.size(); j++) {
                text += "\n" + note.get(j);
            }

        }

        TextView info = new TextView(Activity);
        info.setText(text.substring(2));
        scroll_child.addView(info);

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

                        String staff_sign = gson.toJson(signature.getBytes());

                        String client_signature;

                        if (client_sign == null){
                            if (!staff.get("class").equals("admin")){
                                warning.setView(null);
                                warning.setNegativeButton("", null);
                                warning.setPositiveButton("OK", null);
                                warning.setTitle("No administrators present");
                                warning.setMessage("You must be an administrator to record a client taking medications when the client is not present.");
                                warning.show();
                                return;
                            }
                            else {
                                client_signature = null;
                            }
                        }
                        else {
                            client_signature = gson.toJson(client_sign);
                        }

                        long time = System.currentTimeMillis();

                        CheckBox manual = null;

                        boolean double_check = false;
                        Map<Integer, Float> counts = new HashMap<>();

                        if (admin_mode){
                            manual = (CheckBox) scroll_child.getChildAt(scroll_child.indexOfChild(info) + 1);
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

                                DatePicker date_picker = (DatePicker) scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1);

                                TimePicker time_picker = (TimePicker) scroll_child.getChildAt(scroll_child.indexOfChild(date_picker) + 1);

                                String day = datepicker_to_date(date_picker) + " " + timepicker_to_time(time_picker);

                                time = datetime_to_long(day);

                                double_check = true;
                                String date = long_to_date(time);
                                long start = date_to_long(date);
                                long end = date_to_long(next_day(date, 1));
                                String ids = "(";
                                for (int i = 0; i < entries.size(); i++){
                                    ids += "" + entries.get(i).get("prescription_id") + ", ";
                                    counts.put((int)entries.get(i).get("prescription_id"), (float)0);
                                }
                                ids = ids.substring(0, ids.length() - 2) + ")";
                                List<Map<String, Object>> taken = entries_db.getRows(
                                        new String[]{"id", "change", "prescription_id"},
                                        new String[]{"method='TOOK MEDS'", "datetime>="+start, "datetime<"+end, "prescription_id IN "+ids},
                                        new String[]{"prescription_id", "ASC"}, false
                                );
                                for (int i = 0; i < taken.size(); i++){
                                    Map<String, Object> entry = taken.get(i);
                                    counts.put((int)entry.get("prescription_id"), counts.get(entry.get("prescription_id")) + (float)entry.get("change"));
                                }
                            }
                        }

                        for (int i = 0; i < entries.size(); i++) {
                            Map<String, Object> entry = entries.get(i);
                            entry.put("client_signature", client_signature);
                            entry.put("staff_signature_1", staff_sign);
                            entry.put("staff_1", staff.get("id"));
                            entry.put("staff_present", staff.get("name"));
                            entry.put("datetime", time);
                            Map<String, Object> prescription = prescriptions_db.getSingleRow(new String[]{"controlled", "count"}, new String[]{"id="+entry.get("prescription_id")});
                            if (double_check && maxes.get(entry.get("prescription_id")) != null){
                                if (counts.get(entry.get("prescription_id")) + (float) entry.get("change") > maxes.get(entry.get("prescription_id"))){
                                    entry.put("daily_override", true);
                                }
                                List <String> notes = gson.fromJson((String)entry.get("notes"), new TypeToken<List<String>>(){}.getType());
                                notes.add(0, "MANUAL DATE/TIME ENTRY");
                                entry.put("notes", gson.toJson(notes));

                                if ((int) prescription.get("controlled") == 1){

                                    long previous_time = (long) entries_db.getObject("MAX([datetime]) AS datetime",
                                            new String[]{"prescription_id="+entry.get("prescription_id"), "datetime<"+time});
                                    float previous_count = (float) entries_db.getObject("new_count",
                                            new String[]{"prescription_id="+entry.get("prescription_id"), "datetime="+previous_time});

                                    entry.put("old_count", previous_count);
                                    entry.put("new_count", previous_count - (float) entry.get("change"));

                                    adjust_future_counts((int)entry.get("prescription_id"), -1 * (float) entry.get("change"), time);
                                }
                            }

                            else if ((int) prescription.get("controlled") == 1){
                                float new_count = (float) prescription.get("count") - (float) entry.get("change");
                                entry.put("old_count", prescription.get("count"));
                                entry.put("new_count", new_count);
                                prescription.put("count", new_count);
                                prescriptions_db.update(prescription, new String[]{"id="+entry.get("prescription_id")});
                            }
                            entries_db.addRow(entry);
                        }

                        for (int i = 0; i < 4; i++){
                            history.remove(0);
                        }
                        Map<String, Object> client = clients_db.getSingleRow(null, new String[]{"id="+entries.get(0).get("client_id")});
                        ui_message(Activity, "Medication log updated: Client took meds");
                        prescriptions(Activity, client);
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
