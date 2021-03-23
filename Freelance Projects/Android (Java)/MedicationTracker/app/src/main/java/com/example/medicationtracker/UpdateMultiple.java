package com.example.medicationtracker;

import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.ConfirmUpdates.confirm_updates;
import static com.example.medicationtracker.Create_Prescription.create_prescription;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.MainActivity.prescriptions_db;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.Wipe.wipe;

public class UpdateMultiple {
    public static void update_multiple(MainActivity Activity, int client_id, Map<Integer, Map<String, Object>> packet) {
        wipe(Activity, "Update Prescriptions", () -> update_multiple(Activity, client_id, packet));
        LinearLayout scroll_child = Activity.scroll_child;

        List<Map<String, Object>> prescriptions = prescriptions_db.getRows(null,
                new String[]{"client_id="+client_id, "active=1"},
                new String[]{"name", "ASC"}, false);

        for (int i = 0; i < prescriptions.size(); i++) {
            Map<String, Object> prescription = prescriptions.get(i);

            if ((int)prescription.get("as_needed") == 1) {
                prescription.put("as_needed", true);
            }
            else{
                prescription.put("as_needed", false);
            }

            final Button current_script = new Button(Activity);
            current_script.setText((String)prescription.get("name"));
            current_script.setTag(false);
            current_script.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!((Boolean) current_script.getTag())) {

                        CheckBox as_needed = new CheckBox(Activity);
                        as_needed.setText("Take As Needed");

                        if ((boolean)prescription.get("as_needed")) {
                            as_needed.setChecked(true);
                        }

                        final EditText instructions = new EditText(Activity);
                        instructions.setText((String)prescription.get("instructions"));
                        instructions.setHint("Instructions");

                        final EditText dose_max = new EditText(Activity);
                        if (prescription.get("dose_max") != null) {
                            dose_max.setText(prescription.get("dose_max").toString());
                        }
                        dose_max.setHint("Maximum Dose");
                        dose_max.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                        final EditText daily_max = new EditText(Activity);
                        if (prescription.get("daily_max") != null) {
                            daily_max.setText(prescription.get("daily_max").toString());
                        }
                        daily_max.setHint("Daily Maximum");
                        daily_max.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                        final EditText indication = new EditText(Activity);
                        if (prescription.get("indication") != null) {
                            indication.setText((String)prescription.get("indication"));
                        }
                        indication.setHint("Indication");

                        final EditText prescriber = new EditText(Activity);
                        if (prescription.get("prescriber")!= null) {
                            prescriber.setText((String)prescription.get("prescriber"));
                        }
                        prescriber.setHint("Prescriber");

                        final EditText pharmacy = new EditText(Activity);
                        if (prescription.get("pharmacy") != null) {
                            pharmacy.setText((String)prescription.get("pharmacy"));
                        }
                        pharmacy.setHint("Pharmacy");

                        current_script.setTag(true);
                        scroll_child.addView(as_needed, scroll_child.indexOfChild(current_script) + 1);
                        scroll_child.addView(instructions, scroll_child.indexOfChild(as_needed) + 1);
                        scroll_child.addView(dose_max, scroll_child.indexOfChild(instructions) + 1);
                        scroll_child.addView(daily_max, scroll_child.indexOfChild(dose_max) + 1);
                        scroll_child.addView(indication, scroll_child.indexOfChild(daily_max) + 1);
                        scroll_child.addView(prescriber, scroll_child.indexOfChild(indication) + 1);
                        scroll_child.addView(pharmacy, scroll_child.indexOfChild(prescriber) + 1);
                    } else {
                        current_script.setTag(false);
                        for (int i = 0; i < 7; i++){
                            scroll_child.removeViewAt(scroll_child.indexOfChild(current_script) + 1);
                        }
                    }
                }
            });
            scroll_child.addView(current_script);
            if (packet != null && packet.get(prescription.get("id")) != null){
                current_script.callOnClick();
                Map<String, Object> packet_script = packet.get(prescription.get("id"));

                if ((boolean)packet_script.get("as_needed")){
                    ((CheckBox) scroll_child.getChildAt(scroll_child.indexOfChild(current_script) + 1)).setChecked(true);
                }
                else{
                    ((CheckBox) scroll_child.getChildAt(scroll_child.indexOfChild(current_script) + 1)).setChecked(false);
                }

                ((EditText)scroll_child.getChildAt(scroll_child.indexOfChild(current_script) + 2)).setText((String)packet_script.get("instructions"));

                if (packet_script.get("dose_max") != null){
                    ((EditText) scroll_child.getChildAt(scroll_child.indexOfChild(current_script) + 3)).setText(String.valueOf(packet_script.get("dose_max")));
                }

                if (packet_script.get("daily_max") != null){
                    ((EditText) scroll_child.getChildAt(scroll_child.indexOfChild(current_script) + 4)).setText(String.valueOf(packet_script.get("daily_max")));
                }

                if (packet_script.get("indication") != null){
                    ((EditText) scroll_child.getChildAt(scroll_child.indexOfChild(current_script) + 5)).setText((String) packet_script.get("indication"));
                }

                if (packet_script.get("prescriber") != null){
                    ((EditText) scroll_child.getChildAt(scroll_child.indexOfChild(current_script) + 6)).setText((String) packet_script.get("prescriber"));
                }

                if (packet_script.get("pharmacy") != null){
                    ((EditText) scroll_child.getChildAt(scroll_child.indexOfChild(current_script) + 7)).setText((String) packet_script.get("pharmacy"));
                }
            }
        }

        final Button change = new Button(Activity);
        change.setText("Save Updates");
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Map<String, Object>> new_prescriptions = new ArrayList<>();
                List<Map<String, Object>> old_prescriptions = new ArrayList<>();
                List<String> keys = Arrays.asList("dose", "dose_max", "daily_max", "instructions", "as_needed", "indication", "prescriber", "pharmacy");
                Map<Integer, Map<String, Object>> new_packet = new HashMap<>();

                int add = 0;
                for (int i = 0; i < prescriptions.size(); i++) {
                    if (scroll_child.getChildAt(i + add).getTag() != null && (Boolean) scroll_child.getChildAt(i + add).getTag()) {
                        Map<String, Object> prescription = prescriptions.get(i);

                        Float max_dose = null;
                        EditText dose_max = (EditText) scroll_child.getChildAt(i + add + 3);
                        if (dose_max.getText().toString().length() > 0) {
                            max_dose = Float.parseFloat(dose_max.getText().toString());
                        }

                        Float max_daily = null;
                        EditText daily_max = (EditText) scroll_child.getChildAt(i + add + 4);
                        if (daily_max.getText().toString().length() > 0) {
                            max_daily = Float.parseFloat(daily_max.getText().toString());
                        }

                        boolean prn = false;
                        CheckBox as_needed = (CheckBox) scroll_child.getChildAt(i + add + 1);
                        if (as_needed.isChecked()) {
                            prn = true;
                        }

                        String reason = null;
                        EditText indication = (EditText) scroll_child.getChildAt(i + add + 5);
                        if (indication.getText().toString().length() > 0) {
                            reason = indication.getText().toString();
                        }

                        String doctor = null;
                        EditText prescriber = (EditText) scroll_child.getChildAt(i + add + 6);
                        if (prescriber.getText().toString().length() > 0) {
                            doctor = prescriber.getText().toString();
                        }

                        String pharm = null;
                        EditText pharmacy = (EditText) scroll_child.getChildAt(i + add + 7);
                        if (pharmacy.getText().toString().length() > 0) {
                            pharm = pharmacy.getText().toString();
                        }

                        String instructions = ((EditText)scroll_child.getChildAt(i + add + 2)).getText().toString();


                        if (instructions.length() == 0){
                            warning.setView(null);
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("Try Again", null);
                            warning.setTitle("Invalid Instructions");
                            warning.setMessage("Instructions is a required field and cannot be left blank");
                            warning.show();
                            return;
                        }


                        Map<String, Object> new_prescription = create_prescription(
                                (int) prescription.get("client_id"),
                                (String) prescription.get("drug"),
                                (String) prescription.get("dose"),
                                max_dose,
                                max_daily,
                                instructions,
                                prn,
                                (int)prescription.get("controlled") == 1,
                                (Float)prescription.get("count"),
                                reason, doctor, pharm, null
                        );

                        for (int j = 0; j < keys.size(); j++){
                            if (new_prescription.get(keys.get(j)) == null){
                                if (prescription.get(keys.get(j)) != null){
                                    new_prescriptions.add(new_prescription);
                                    old_prescriptions.add(prescription);
                                    new_packet.put((int) prescription.get("id"), new_prescription);
                                    break;
                                }
                            }
                            else if (prescription.get(keys.get(j)) == null){
                                if (new_prescription.get(keys.get(j)) != null){
                                    new_prescriptions.add(new_prescription);
                                    old_prescriptions.add(prescription);
                                    new_packet.put((int) prescription.get("id"), new_prescription);
                                    break;
                                }
                            }
                            else if (!new_prescription.get(keys.get(j)).equals(prescription.get(keys.get(j)))){
                                new_prescriptions.add(new_prescription);
                                old_prescriptions.add(prescription);
                                new_packet.put((int) prescription.get("id"), new_prescription);
                                break;
                            }
                        }
                        add += 7;
                    }
                }
                if (new_prescriptions.size() > 0){
                    history.remove(0);
                    history.add(0, () -> update_multiple(Activity, client_id, new_packet));
                    confirm_updates(Activity, client_id, old_prescriptions, new_prescriptions, keys);
                }

            }
        });
        scroll_child.addView(change);
    }
}
