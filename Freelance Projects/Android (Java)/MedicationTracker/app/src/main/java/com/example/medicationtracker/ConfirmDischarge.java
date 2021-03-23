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
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.Check_User.check_user;
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
import static com.example.medicationtracker.TimepickerToTime.timepicker_to_time;
import static com.example.medicationtracker.UI_Message.ui_message;
import static com.example.medicationtracker.Wipe.wipe;

public class ConfirmDischarge {
    public static void confirm_discharge(MainActivity Activity, int client_id, String name){
        wipe(Activity, "Confirm Discharge of " + name, () -> confirm_discharge(Activity, client_id, name));
        LinearLayout scroll_child = Activity.scroll_child;
        ScrollView scroll = Activity.scroll;
        LinearLayout screen = Activity.screen;

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
                warning.setPositiveButton("Discharge Client", new DialogInterface.OnClickListener() {
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


                        List<String> notes = new ArrayList<>();

                        if (admin_mode){
                            CheckBox manual = (CheckBox) scroll_child.getChildAt(0);
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

                                notes.add("MANUAL DATE/TIME ENTRY");
                            }
                        }

                        List<Map<String,Object>> prescriptions = prescriptions_db.getRows(
                                new String[]{"id", "name", "count"}, new String[]{"active=1", "client_id="+client_id}, null, false);

                        for (int i = 0; i < prescriptions.size(); i++) {
                            Map<String, Object> prescription = prescriptions.get(i);

                            Map<String, Object> entry = create_entry(client_id, (int)prescription.get("id"),
                                    (String)prescription.get("name"), (Float)prescription.get("count"), null,
                                    null, time, false, false, 0,
                                    "CLIENT DISCHARGED", null, (int)staff.get("id"), gson.toJson(staff_sign), null,
                                    null, (String)staff.get("name"), notes);

                            entries_db.addRow(entry);

                            prescription.put("end", time);
                            prescription.put("active", false);
                            prescription.put("count", null);
                            prescriptions_db.update(prescription, new String[]{"id="+prescription.get("id")});
                        }

                        Map<String, Object> client = new HashMap<>();
                        client.put("active", false);
                        client.put("discharge", time);

                        clients_db.update(client, new String[]{"id="+client_id});

                        for (int i = 0; i < 2; i++){
                            history.remove(0);
                        }

                        ui_message(Activity, "Databases updated: client was discharged, all medications discontinued.");
                        prescriptions(Activity, clients_db.getSingleRow(null, new String[]{"id="+client_id}));
                    }
                });
                warning.setTitle("Confirm Discharge of " + name);
                warning.setMessage("Are you sure you want to mark " + name + " as discharged? The client's medication history will still be saved. If you " +
                        "wish to proceed, please enter your password.");
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
