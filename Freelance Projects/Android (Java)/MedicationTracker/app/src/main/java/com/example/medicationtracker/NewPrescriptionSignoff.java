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
import static com.example.medicationtracker.Create_Client.create_client;
import static com.example.medicationtracker.Create_Entry.create_entry;
import static com.example.medicationtracker.DateToLong.date_to_long;
import static com.example.medicationtracker.DatepickerToDate.datepicker_to_date;
import static com.example.medicationtracker.Datetime_To_Long.datetime_to_long;
import static com.example.medicationtracker.Long_To_Date.long_to_date;
import static com.example.medicationtracker.MainActivity.admin_mode;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.MainActivity.gson;
import static com.example.medicationtracker.MainActivity.prescriptions_db;
import static com.example.medicationtracker.MainActivity.signature_size;
import static com.example.medicationtracker.MainActivity.unweighted_params;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.MainActivity.weighted_params;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.NextDay.next_day;
import static com.example.medicationtracker.Prescriptions.prescriptions;
import static com.example.medicationtracker.TimepickerToTime.timepicker_to_time;
import static com.example.medicationtracker.UI_Message.ui_message;
import static com.example.medicationtracker.Wipe.wipe;

public class NewPrescriptionSignoff {
    public static void new_prescription_signoff(MainActivity Activity, String name, Integer client_id, List<Map<String, Object>> scripts, String password) {
        List<Map<String, Object>> script_copy = new ArrayList<>();
        script_copy.addAll(scripts);
        LinearLayout scroll_child = Activity.scroll_child;
        ScrollView scroll = Activity.scroll;
        LinearLayout screen = Activity.screen;

        wipe(Activity, "New Prescriptions Sign Off", () -> new_prescription_signoff(Activity, name, client_id, script_copy, password));


        String text = "";

        for (int i = 0; i < scripts.size(); i++) {
            Map<String, Object> prescription = scripts.get(i);

            text += "\n\n" + prescription.get("drug");
            text += "\n" + prescription.get("dose");
            text += "\n" + prescription.get("instructions");

            if ((boolean) prescription.get("as_needed")) {
                text += "\nTo be taken as needed";
            }

            if ((boolean) prescription.get("controlled")) {
                text += "\nCONTROLLED";
            }

            if (prescription.get("count") != null) {
                text += "\nStarting Count: " + prescription.get("count");
            }

            if (prescription.get("dose_max") != null) {
                text += "\nMaximum Dose: " + prescription.get("dose_max");
            }

            if (prescription.get("daily_max") != null) {
                text += "\nDaily Maximum: " + prescription.get("daily_max");
            }

            if (prescription.get("indication") != null) {
                text += "\nIndication: " + prescription.get("indication");
            }

            if (prescription.get("prescriber") != null) {
                text += "\nPrescriber: " + prescription.get("prescriber");
            }

            if (prescription.get("pharmacy") != null) {
                text += "\nPharmacy: " + prescription.get("pharmacy");
            }
        }

        TextView prescriptions = new TextView(Activity);
        prescriptions.setText(text.substring(2));
        scroll_child.addView(prescriptions);

        final SignatureView signature = create_signature_view(Activity);
        LinearLayout buttons = new LinearLayout(Activity);

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
                        Map<String, Object> admin = check_user(pass.getText().toString());

                        if (admin == null){
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

                        List<String> notes = new ArrayList<>();

                        if (admin_mode){
                            CheckBox manual = (CheckBox) scroll_child.getChildAt(scroll_child.indexOfChild(prescriptions) + 1);
                            if (manual.isChecked()){

                                if (!admin.get("class").equals("admin")){
                                    warning.setView(null);
                                    warning.setNegativeButton("", null);
                                    warning.setPositiveButton("OK", null);
                                    warning.setTitle("No Admin Present");
                                    warning.setMessage("A staff member with administrative privileges must be present to manually enter dates and times.");
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

                        int new_client_id = 0;

                        String method = "PRESCRIPTION STARTED";

                        if (name != null){

                            Map<String, Object> client = create_client(name, time, "client", password);

                            new_client_id = clients_db.addRow(client);

                            method = "INTAKE";
                        }
                        else{
                            new_client_id = client_id;
                        }

                        for (int i = 0; i < scripts.size(); i++){
                            Map<String, Object> prescription = scripts.get(i);
                            prescription.put("start", time);
                            prescription.put("client_id", new_client_id);

                            Float count = null;
                            Float start = null;
                            if (prescription.get("count") != null){
                                count = (float) prescription.get("count");
                                start = (float)0;
                            }

                            if (!(boolean)prescription.get("controlled")){
                                prescription.put("count", null);
                            }

                            int id = prescriptions_db.addRow(prescription);

                            Map<String, Object> entry = create_entry(new_client_id, id, (String) prescription.get("name"),
                                    start, count, count, time, false, false, 0, method,
                                    null, (int)admin.get("id"), staff_sign, null, null, (String)admin.get("name"), notes);

                            entries_db.addRow(entry);
                        }
                        for (int i = 0; i < (scripts.size() * 2) + 3; i++){
                            history.remove(0);
                        }

                        if (name != null){
                            history.remove(0);
                            ui_message(Activity, "Client added to database with new prescriptions.");
                            clients(Activity);
                        }
                        else {
                            ui_message(Activity, "New prescriptions added to database.");
                            prescriptions(Activity, clients_db.getSingleRow(null, new String[]{"id="+client_id}));
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

        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.addView(clear);
        buttons.addView(confirm);
        confirm.setLayoutParams(weighted_params);

        screen.addView(signature);
        screen.addView(buttons);
    }
}
