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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.Check_User.check_user;
import static com.example.medicationtracker.CreateSignatureView.create_signature_view;
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
import static com.example.medicationtracker.TimepickerToTime.timepicker_to_time;
import static com.example.medicationtracker.UI_Message.ui_message;
import static com.example.medicationtracker.Wipe.wipe;

public class ConfirmMultipleDiscontinue {
    public static void confirm_multiple_discontinue(MainActivity Activity, List<Map<String, Object>> entries) {
        wipe(Activity, "Confirm Medication Discontinuation", () -> confirm_multiple_discontinue(Activity, entries));
        LinearLayout scroll_child = Activity.scroll_child;
        ScrollView scroll = Activity.scroll;
        LinearLayout screen = Activity.screen;

        String text = (String) entries.get(0).get("drug");

        if (entries.get(0).get("change") != null){
            text += "\nAmount Remaining: " + entries.get(0).get("change");
        }

        for (int i = 1; i < entries.size(); i++) {

            Map<String,Object> entry = entries.get(i);

            text += "\n\n" + entry.get("drug");
            if (entry.get("change") != null){
                text += "\nAmount Remaining: " + entry.get("change");
            }
        }

        TextView info = new TextView(Activity);
        info.setText(text);
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
                warning.setView(pass);
                warning.setNegativeButton("Cancel", null);
                warning.setPositiveButton("Discontinue Prescriptions", new DialogInterface.OnClickListener() {
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

                        long time = System.currentTimeMillis();

                        boolean double_check = false;

                        if (admin_mode){
                            CheckBox manual = (CheckBox) scroll_child.getChildAt(scroll_child.indexOfChild(info) + 1);
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

                        for (int i = 0; i < entries.size(); i++) {
                            Map<String, Object> entry = entries.get(i);
                            entry.put("staff_signature_1", staff_sign);
                            entry.put("staff_1", staff.get("id"));
                            entry.put("staff_present", staff.get("name"));
                            entry.put("datetime", time);
                            if (double_check){
                                entry.put("notes", gson.toJson(Arrays.asList("MANUAL DATE/TIME ENTRY")));

                                if ((int)prescriptions_db.getObject("controlled", new String[]{"id="+entry.get("prescription_id")}) == 1){
                                    long previous_time = (long) entries_db.getObject("MAX([datetime]) AS datetime",
                                            new String[]{"prescription_id="+entry.get("prescription_id"), "datetime<"+time});
                                    float previous_count = (float) entries_db.getObject("new_count",
                                            new String[]{"prescription_id="+entry.get("prescription_id"), "datetime="+previous_time});

                                    entry.put("old_count", previous_count);
                                }
                            }
                            entries_db.addRow(entry);

                            Map<String, Object> prescription = new HashMap<>();
                            prescription.put("end", time);
                            prescription.put("active", false);
                            prescription.put("count", null);
                            prescriptions_db.update(prescription, new String[]{"id="+entry.get("prescription_id")});
                        }

                        for (int i = 0; i < 3; i++){
                            history.remove(0);
                        }
                        Map<String, Object> client = clients_db.getSingleRow(null, new String[]{"id="+entries.get(0).get("client_id")});
                        ui_message(Activity, "Medication log updated: Prescriptions discontinued.");
                        prescriptions(Activity, client);
                    }
                });
                warning.setTitle("Discontinuing Prescriptions");
                warning.setMessage("Please note this action will discontinue the selected prescriptions. The data will still be saved. If you wish to proceed, " +
                        "please enter your password.");
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
