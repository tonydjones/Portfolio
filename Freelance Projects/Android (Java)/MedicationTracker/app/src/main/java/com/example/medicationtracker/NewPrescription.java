package com.example.medicationtracker;

import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.AddMultiplePrescriptions.add_multiple_prescriptions;
import static com.example.medicationtracker.Create_Prescription.create_prescription;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.Wipe.wipe;
import static com.example.medicationtracker.MainActivity.history;


public class NewPrescription {
    public static void new_prescription(MainActivity Activity, String client_name, Integer client_id, List<Map<String, Object>> prescriptions, String pass, Map<String, Object> packet){
        List<Map<String, Object>> prescription_copy = new ArrayList<>();
        prescription_copy.addAll(prescriptions);
        LinearLayout scroll_child = Activity.scroll_child;

        wipe(Activity, "Add A New Prescription", () -> new_prescription(Activity, client_name, client_id, prescription_copy, pass, packet));

        EditText name = new EditText(Activity);
        name.setHint("Medication Name");
        scroll_child.addView(name);

        EditText dose = new EditText(Activity);
        dose.setHint("Dosage");
        scroll_child.addView(dose);

        EditText instructions = new EditText(Activity);
        instructions.setHint("Prescription Instructions");
        scroll_child.addView(instructions);

        CheckBox as_needed = new CheckBox(Activity);
        as_needed.setText("Take As Needed");
        scroll_child.addView(as_needed);

        CheckBox controlled = new CheckBox(Activity);
        controlled.setText("Controlled");

        /*controlled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!controlled.isChecked()) {
                    scroll_child.removeView(scroll_child.getChildAt(scroll_child.indexOfChild(controlled) + 1));
                } else {
                    EditText current_count = new EditText(Activity);
                    current_count.setHint("Count");
                    current_count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    scroll_child.addView(current_count, scroll_child.indexOfChild(controlled) + 1);
                }
            }
        });*/
        scroll_child.addView(controlled);

        EditText current_count = new EditText(Activity);
        current_count.setHint("Count");
        current_count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        scroll_child.addView(current_count, scroll_child.indexOfChild(controlled) + 1);

        EditText dose_max = new EditText(Activity);
        dose_max.setHint("Max At A Time");
        dose_max.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        scroll_child.addView(dose_max);

        EditText daily_max = new EditText(Activity);
        daily_max.setHint("Max Per Day");
        daily_max.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        scroll_child.addView(daily_max);

        EditText indication = new EditText(Activity);
        indication.setHint("Indication");
        scroll_child.addView(indication);

        EditText prescriber = new EditText(Activity);
        prescriber.setHint("Prescriber");
        scroll_child.addView(prescriber);

        EditText pharmacy = new EditText(Activity);
        pharmacy.setHint("Pharmacy");
        scroll_child.addView(pharmacy);

        if (packet != null){
            name.setText((String)packet.get("drug"));
            dose.setText((String)packet.get("dose"));
            instructions.setText((String)packet.get("instructions"));

            if((boolean) packet.get("as_needed")){
                as_needed.setChecked(true);
            }
            if((boolean) packet.get("controlled")){
                controlled.setChecked(true);
            }
            if (packet.get("count") != null){
                current_count.setText(String.valueOf(packet.get("count")));
            }
            if(packet.get("dose_max") != null){
                dose_max.setText(String.valueOf(packet.get("dose_max")));
            }
            if(packet.get("daily_max") != null){
                daily_max.setText(String.valueOf(packet.get("daily_max")));
            }
            if (packet.get("indication") != null){
                indication.setText((String)packet.get("indication"));
            }
            if (packet.get("prescriber") != null){
                prescriber.setText((String)packet.get("prescriber"));
            }
            if (packet.get("pharmacy") != null){
                pharmacy.setText((String)packet.get("pharmacy"));
            }
        }

        Button add = new Button(Activity);
        add.setText("Add Prescription");
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText().toString().length() > 0 && dose.getText().toString().length() > 0 && instructions.getText().toString().length() > 0) {

                    Float max_dose = null;
                    if (dose_max.getText().toString().length() > 0) {
                        max_dose = Float.parseFloat(dose_max.getText().toString());
                        if (max_dose == 0){
                            warning.setView(null);
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("OK", null);
                            warning.setTitle("Invalid Dose Limit");
                            warning.setMessage("0 is not a valid dose limit for a medication.");
                            warning.show();
                            return;
                        }
                    }

                    Float max_daily = null;
                    if (daily_max.getText().toString().length() > 0) {
                        max_daily = Float.parseFloat(daily_max.getText().toString());
                        if (max_daily == 0){
                            warning.setView(null);
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("OK", null);
                            warning.setTitle("Invalid Daily Limit");
                            warning.setMessage("0 is not a valid daily limit for a medication.");
                            warning.show();
                            return;
                        }
                    }

                    Boolean prn = false;
                    if (as_needed.isChecked()) {
                        prn = true;
                    }

                    Boolean control = false;
                    Float count = null;
                    if (controlled.isChecked()) {
                        control = true;
                        EditText count_input = (EditText) scroll_child.getChildAt(scroll_child.indexOfChild(controlled) + 1);
                        if (count_input.getText().toString().length() > 0) {
                            count = Float.parseFloat(count_input.getText().toString());
                        } else {
                            warning.setView(null);
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("OK", null);
                            warning.setTitle("Invalid Count");
                            warning.setMessage("You must enter a valid count for controlled medications");
                            warning.show();
                            return;
                        }
                    }
                    else {
                        EditText count_input = (EditText) scroll_child.getChildAt(scroll_child.indexOfChild(controlled) + 1);
                        if (count_input.getText().toString().length() > 0) {
                            count = Float.parseFloat(count_input.getText().toString());
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

                    Map<String, Object> script = create_prescription(client_id, name.getText().toString(),
                            dose.getText().toString(), max_dose, max_daily, instructions.getText().toString(),
                            prn, control, count, reason, doctor, pharm, null);

                    prescriptions.add(script);
                    history.remove(0);
                    history.remove(0);
                    add_multiple_prescriptions(Activity, client_name, client_id, prescriptions, pass);
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
        scroll_child.addView(add);
    }
}
