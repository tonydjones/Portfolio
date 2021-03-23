package com.example.medicationtracker;

import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.ClientConfirm.client_confirm;
import static com.example.medicationtracker.ConfirmMultipleDiscontinue.confirm_multiple_discontinue;
import static com.example.medicationtracker.ConfirmRefill.confirm_refill;
import static com.example.medicationtracker.ConfirmUpdates.confirm_updates;
import static com.example.medicationtracker.Create_Entry.create_entry;
import static com.example.medicationtracker.Create_Prescription.create_prescription;
import static com.example.medicationtracker.DateToLong.date_to_long;
import static com.example.medicationtracker.DatepickerData.datepicker_data;
import static com.example.medicationtracker.DatepickerToDate.datepicker_to_date;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.Med_Log.log;
import static com.example.medicationtracker.Long_To_Date.long_to_date;
import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.MainActivity.prescriptions_db;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.MainActivity.weighted_params;
import static com.example.medicationtracker.MedAdherence.med_adherence;
import static com.example.medicationtracker.NextDay.next_day;
import static com.example.medicationtracker.Wipe.wipe;

public class Details {
    public static void details(MainActivity Activity, Map<String, Object> prescription, Map<String, String> packet) {

        wipe(Activity, prescription.get("name") + " Details", () -> details(Activity, prescription, packet));
        LinearLayout scroll_child = Activity.scroll_child;

        String text = (String) prescription.get("drug");
        text += "\n" + prescription.get("dose");
        text += "\n" + prescription.get("instructions");

        if ((int) prescription.get("as_needed") == 1) {
            text += "\nTo be taken as needed";
        }

        boolean controlled = false;
        Float stash = null;
        if ((int) prescription.get("controlled") == 1) {
            controlled = true;
            text += "\nCONTROLLED";
            if ((int)prescription.get("active") == 1) {
                stash = (float) prescription.get("count");
                text += "\nCurrent Count: " + prescription.get("count");
            }
        }

        if (prescription.get("dose_max") != null) {
            text += "\nMaximum Dose: " + prescription.get("dose_max");
        }

        if (prescription.get("daily_max") != null) {
            text += "\nDaily Maximum: " + prescription.get("daily_max");
        }

        long date = System.currentTimeMillis();
        String date_string = long_to_date(date);
        long datelong = date_to_long(date_string);
        long endlong = date_to_long(next_day(date_string, 1));
        List<Object> taken = entries_db.getSingleColumn("change",
                new String[]{"method='TOOK MEDS'",
                        "prescription_id="+prescription.get("id"),
                        "datetime>="+datelong,
                        "datetime<"+endlong},
                null, false);

        float taken_today = 0;
        for (int j = 0; j < taken.size(); j++){
            taken_today += (Float) taken.get(j);
        }
        text += "\nTaken Today: " + taken_today;

        if (prescription.get("indication") != null) {
            text += "\nIndication: " + prescription.get("indication");
        }

        if (prescription.get("prescriber") != null) {
            text += "\nPrescriber: " + prescription.get("prescriber");
        }

        if (prescription.get("pharmacy") != null) {
            text += "\nPharmacy: " + prescription.get("pharmacy");
        }

        text += "\nThis prescription was started on " + long_to_date((long) prescription.get("start"));

        if ((int)prescription.get("active") != 1) {
            text += "\nThis prescription was discontinued on " + long_to_date((long) prescription.get("end"));
        }

        if ((int)prescription.get("edits") > 0) {
            text += "\nThis prescription's information has been edited " + prescription.get("edits") + " time";
            if ((int)prescription.get("edits") > 1){
                text += "s";
            }
        }

        TextView information = new TextView(Activity);
        information.setText(text);
        scroll_child.addView(information);


        Button med_history = new Button(Activity);
        med_history.setText("View Medication Log");
        med_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log(Activity, prescription);
            }
        });
        scroll_child.addView(med_history);
        boolean finalControlled = controlled;

        if ((int) prescription.get("active") == 1){
            Button take = new Button(Activity);
            take.setText("Take Medication");
            take.setTag(false);
            float finalTaken_today = taken_today;
            take.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((Boolean) take.getTag() == false) {
                        take.setTag(true);

                        EditText current_count = new EditText(Activity);
                        current_count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        final float[] count = {0};
                        current_count.setText(String.valueOf(count[0]));

                        Button add = new Button(Activity);
                        add.setText("Add");
                        add.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!finalControlled || Float.parseFloat(current_count.getText().toString()) <= (float) prescription.get("count") - 1) {
                                    current_count.setText(String.valueOf(Float.parseFloat(current_count.getText().toString()) + 1));
                                }
                                else {
                                    current_count.setText(String.valueOf((float) prescription.get("count")));
                                }
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
                        subtract.setLayoutParams(weighted_params);

                        LinearLayout buttons = new LinearLayout(Activity);
                        buttons.setOrientation(LinearLayout.HORIZONTAL);
                        buttons.addView(subtract);
                        buttons.addView(add);

                        EditText notes = new EditText(Activity);
                        notes.setHint("Notes");

                        Button process = new Button(Activity);
                        process.setText("Confirm Taking Medication");
                        process.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String count_string = current_count.getText().toString();
                                if (count_string.length() > 0 && Float.parseFloat(count_string) > 0) {

                                    if (finalControlled && Float.parseFloat(count_string) > (float) prescription.get("count")) {
                                        warning.setView(null);
                                        warning.setNegativeButton("", null);
                                        warning.setPositiveButton("OK", null);
                                        warning.setTitle("Invalid Count");
                                        warning.setMessage("The client does not have " + count_string + " " + prescription.get("name"));
                                        warning.show();
                                        return;
                                    }

                                    Map<Integer, Float> maxes = new HashMap<>();
                                    float count = Float.parseFloat(count_string);

                                    Float dose_max = null;
                                    if (prescription.get("dose_max") != null){
                                        dose_max = (float) prescription.get("dose_max");
                                    }

                                    Float daily_max = null;
                                    if (prescription.get("daily_max") != null){
                                        daily_max = (float) prescription.get("daily_max");
                                    }

                                    maxes.put((int)prescription.get("id"), daily_max);

                                    final boolean[] dose_override = {false};
                                    final boolean[] daily_override = {false};

                                    String drug = (String)prescription.get("name");

                                    int script_id = (int) prescription.get("id");
                                    List<String> note = new ArrayList<>();
                                    if (notes.getText().toString().length() > 0) {
                                        note.add(notes.getText().toString());
                                    }

                                    List<String> finalNote = note;
                                    Float finalDaily_max = daily_max;
                                    if (dose_max != null && count > dose_max){
                                        warning.setMessage("Taking " + count + " " +
                                                drug + " is more than the maximum dose of " + dose_max);
                                        warning.setTitle("Taking too many " + drug);

                                        warning.setPositiveButton("Override Dose Limit", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dose_override[0] = true;
                                                if (finalDaily_max != null && count + finalTaken_today > finalDaily_max){
                                                    warning.setMessage("Taking " + count + " " +
                                                            drug + " will put the client over the daily limit of " +
                                                            finalDaily_max);
                                                    warning.setTitle("Over the daily limit for " + drug);
                                                    warning.setPositiveButton("Override Daily Limit", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            daily_override[0] = true;
                                                            Map<String,Object> entry = create_entry((int)prescription.get("client_id"), script_id, drug,
                                                                    null, count, null,
                                                                    null, dose_override[0], daily_override[0], 0, "TOOK MEDS",
                                                                    null, null, null, null, null,null, finalNote);

                                                            Map<String, String> new_packet = new HashMap<>();
                                                            new_packet.put("count", current_count.getText().toString());
                                                            new_packet.put("note", notes.getText().toString());
                                                            new_packet.put("method", "take");
                                                            history.remove(0);
                                                            history.add(0, () -> details(Activity, prescription, new_packet));

                                                            client_confirm(Activity, new ArrayList<>(Arrays.asList(entry)), maxes);
                                                        }
                                                    });
                                                    warning.setNegativeButton("Cancel", null);
                                                    warning.setView(null);
                                                    warning.show();
                                                    return;
                                                }
                                                Map<String,Object> entry = create_entry((int)prescription.get("client_id"), script_id, drug, null, count, null,
                                                        null, dose_override[0], daily_override[0], 0, "TOOK MEDS",
                                                        null, null, null, null, null,null, finalNote);

                                                Map<String, String> new_packet = new HashMap<>();
                                                new_packet.put("count", current_count.getText().toString());
                                                new_packet.put("note", notes.getText().toString());
                                                new_packet.put("method", "take");
                                                history.remove(0);
                                                history.add(0, () -> details(Activity, prescription, new_packet));

                                                client_confirm(Activity, new ArrayList<>(Arrays.asList(entry)), maxes);
                                            }
                                        });
                                        warning.setNegativeButton("Cancel", null);
                                        warning.setView(null);
                                        warning.show();
                                        return;
                                    }
                                    if (finalDaily_max != null && count + finalTaken_today > finalDaily_max){
                                        warning.setMessage("Taking " + count + " " +
                                                drug + " will put the client over the daily limit of " +
                                                finalDaily_max);
                                        warning.setTitle("Over the daily limit for " + drug);
                                        warning.setView(null);
                                        warning.setPositiveButton("Override Daily Limit", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                daily_override[0] = true;
                                                Map<String,Object> entry = create_entry((int)prescription.get("client_id"), script_id, drug, null, count, null,
                                                        null, dose_override[0], daily_override[0], 0, "TOOK MEDS",
                                                        null, null, null, null, null,null, finalNote);

                                                Map<String, String> new_packet = new HashMap<>();
                                                new_packet.put("count", current_count.getText().toString());
                                                new_packet.put("note", notes.getText().toString());
                                                new_packet.put("method", "take");
                                                history.remove(0);
                                                history.add(0, () -> details(Activity, prescription, new_packet));

                                                client_confirm(Activity, new ArrayList<>(Arrays.asList(entry)), maxes);
                                            }
                                        });
                                        warning.setNegativeButton("Cancel", null);
                                        warning.show();
                                        return;
                                    }

                                    Map<String,Object> entry = create_entry((int)prescription.get("client_id"), script_id, drug, null, count, null,
                                            null, dose_override[0], daily_override[0], 0, "TOOK MEDS",
                                            null, null, null, null, null, null, finalNote);

                                    Map<String, String> new_packet = new HashMap<>();
                                    new_packet.put("count", current_count.getText().toString());
                                    new_packet.put("note", notes.getText().toString());
                                    new_packet.put("method", "take");
                                    history.remove(0);
                                    history.add(0, () -> details(Activity, prescription, new_packet));

                                    client_confirm(Activity, new ArrayList<>(Arrays.asList(entry)), maxes);
                                }
                                else{
                                    warning.setView(null);
                                    warning.setNegativeButton("", null);
                                    warning.setTitle("Invalid Count");
                                    warning.setMessage("You must input a valid amount of medication to take.");
                                    warning.setPositiveButton("OK", null);
                                    warning.show();
                                }
                            }
                        });

                        scroll_child.addView(current_count, scroll_child.indexOfChild(take) + 1);
                        scroll_child.addView(buttons, scroll_child.indexOfChild(take) + 2);
                        scroll_child.addView(notes, scroll_child.indexOfChild(take) + 3);
                        scroll_child.addView(process, scroll_child.indexOfChild(take) + 4);

                    }
                    else {
                        take.setTag(false);
                        for (int i = 0; i < 4; i++){
                            scroll_child.removeViewAt(scroll_child.indexOfChild(take) + 1);
                        }
                    }
                }
            });
            scroll_child.addView(take);

            Button update = new Button(Activity);
            update.setText("Update Prescription");
            update.setTag(false);
            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!((Boolean) update.getTag())) {

                        CheckBox as_needed = new CheckBox(Activity);
                        as_needed.setText("Take As Needed");

                        if ((int)prescription.get("as_needed") == 1) {
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

                        Button confirm = new Button(Activity);
                        confirm.setText("Confirm Prescription Update");
                        confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                List<String> keys = Arrays.asList("as_needed", "dose", "dose_max", "daily_max", "instructions", "indication", "prescriber", "pharmacy");

                                Float max_dose = null;
                                if (dose_max.getText().toString().length() > 0) {
                                    max_dose = Float.parseFloat(dose_max.getText().toString());
                                }

                                Float max_daily = null;
                                if (daily_max.getText().toString().length() > 0) {
                                    max_daily = Float.parseFloat(daily_max.getText().toString());
                                }

                                boolean prn = false;
                                if (as_needed.isChecked()) {
                                    prn = true;
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

                                String instruction = instructions.getText().toString();

                                if (instruction.length() == 0){
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
                                        instruction,
                                        prn,
                                        finalControlled,
                                        (Float)prescription.get("count"),
                                        reason, doctor, pharm, null
                                );

                                if ((int)prescription.get("as_needed") == 1 && !as_needed.isChecked()){
                                    Map<String, Object> prescription_copy = prescriptions_db.getSingleRow(null, new String[]{"id="+prescription.get("id")});
                                    prescription_copy.put("as_needed", true);

                                    Map<String, String> new_packet = new HashMap<>();
                                    new_packet.put("method", "update");
                                    new_packet.put("as_needed", String.valueOf(prn));
                                    new_packet.put("instructions", instruction);
                                    new_packet.put("dose_max", dose_max.getText().toString());
                                    new_packet.put("daily_max", daily_max.getText().toString());
                                    new_packet.put("indication", indication.getText().toString());
                                    new_packet.put("prescriber", prescriber.getText().toString());
                                    new_packet.put("pharmacy", pharmacy.getText().toString());
                                    history.remove(0);
                                    history.add(0, () -> details(Activity, prescription, new_packet));

                                    confirm_updates(Activity, (int)prescription.get("client_id"), Arrays.asList(prescription_copy),
                                            Arrays.asList(new_prescription), keys);

                                }
                                else if ((int)prescription.get("as_needed") == 0 && as_needed.isChecked()){
                                    Map<String, Object> prescription_copy = prescriptions_db.getSingleRow(null, new String[]{"id="+prescription.get("id")});
                                    prescription_copy.put("as_needed", false);

                                    Map<String, String> new_packet = new HashMap<>();
                                    new_packet.put("method", "update");
                                    new_packet.put("as_needed", String.valueOf(prn));
                                    new_packet.put("instructions", instruction);
                                    new_packet.put("dose_max", dose_max.getText().toString());
                                    new_packet.put("daily_max", daily_max.getText().toString());
                                    new_packet.put("indication", indication.getText().toString());
                                    new_packet.put("prescriber", prescriber.getText().toString());
                                    new_packet.put("pharmacy", pharmacy.getText().toString());
                                    history.remove(0);
                                    history.add(0, () -> details(Activity, prescription, new_packet));

                                    confirm_updates(Activity, (int)prescription.get("client_id"), Arrays.asList(prescription_copy),
                                            Arrays.asList(new_prescription), keys);

                                }
                                else {
                                    for (int j = 1; j < keys.size(); j++){
                                        if (new_prescription.get(keys.get(j)) == null){
                                            if (prescription.get(keys.get(j)) != null){
                                                Map<String, Object> prescription_copy = prescriptions_db.getSingleRow(null, new String[]{"id="+prescription.get("id")});
                                                prescription_copy.put("as_needed", (int)prescription.get("as_needed") == 1);

                                                Map<String, String> new_packet = new HashMap<>();
                                                new_packet.put("method", "update");
                                                new_packet.put("as_needed", String.valueOf(prn));
                                                new_packet.put("instructions", instruction);
                                                new_packet.put("dose_max", dose_max.getText().toString());
                                                new_packet.put("daily_max", daily_max.getText().toString());
                                                new_packet.put("indication", indication.getText().toString());
                                                new_packet.put("prescriber", prescriber.getText().toString());
                                                new_packet.put("pharmacy", pharmacy.getText().toString());
                                                history.remove(0);
                                                history.add(0, () -> details(Activity, prescription, new_packet));

                                                confirm_updates(Activity, (int)prescription.get("client_id"), Arrays.asList(prescription_copy),
                                                        Arrays.asList(new_prescription), keys);
                                                break;
                                            }
                                        }
                                        else if (prescription.get(keys.get(j)) == null && new_prescription.get(keys.get(j)) != null){
                                            Map<String, Object> prescription_copy = prescriptions_db.getSingleRow(null, new String[]{"id="+prescription.get("id")});
                                            prescription_copy.put("as_needed", (int)prescription.get("as_needed") == 1);

                                            Map<String, String> new_packet = new HashMap<>();
                                            new_packet.put("method", "update");
                                            new_packet.put("as_needed", String.valueOf(prn));
                                            new_packet.put("instructions", instruction);
                                            new_packet.put("dose_max", dose_max.getText().toString());
                                            new_packet.put("daily_max", daily_max.getText().toString());
                                            new_packet.put("indication", indication.getText().toString());
                                            new_packet.put("prescriber", prescriber.getText().toString());
                                            new_packet.put("pharmacy", pharmacy.getText().toString());
                                            history.remove(0);
                                            history.add(0, () -> details(Activity, prescription, new_packet));

                                            confirm_updates(Activity, (int)prescription.get("client_id"), Arrays.asList(prescription_copy),
                                                    Arrays.asList(new_prescription), keys);

                                            break;
                                        }
                                        else if (!new_prescription.get(keys.get(j)).equals(prescription.get(keys.get(j)))){
                                            Map<String, Object> prescription_copy = prescriptions_db.getSingleRow(null, new String[]{"id="+prescription.get("id")});
                                            prescription_copy.put("as_needed", (int)prescription.get("as_needed") == 1);

                                            Map<String, String> new_packet = new HashMap<>();
                                            new_packet.put("method", "update");
                                            new_packet.put("as_needed", String.valueOf(prn));
                                            new_packet.put("instructions", instruction);
                                            new_packet.put("dose_max", dose_max.getText().toString());
                                            new_packet.put("daily_max", daily_max.getText().toString());
                                            new_packet.put("indication", indication.getText().toString());
                                            new_packet.put("prescriber", prescriber.getText().toString());
                                            new_packet.put("pharmacy", pharmacy.getText().toString());
                                            history.remove(0);
                                            history.add(0, () -> details(Activity, prescription, new_packet));

                                            confirm_updates(Activity, (int)prescription.get("client_id"), Arrays.asList(prescription_copy),
                                                    Arrays.asList(new_prescription), keys);

                                            break;
                                        }
                                    }
                                }
                            }
                        });

                        update.setTag(true);
                        scroll_child.addView(as_needed, scroll_child.indexOfChild(update) + 1);
                        scroll_child.addView(instructions, scroll_child.indexOfChild(as_needed) + 1);
                        scroll_child.addView(dose_max, scroll_child.indexOfChild(instructions) + 1);
                        scroll_child.addView(daily_max, scroll_child.indexOfChild(dose_max) + 1);
                        scroll_child.addView(indication, scroll_child.indexOfChild(daily_max) + 1);
                        scroll_child.addView(prescriber, scroll_child.indexOfChild(indication) + 1);
                        scroll_child.addView(pharmacy, scroll_child.indexOfChild(prescriber) + 1);
                        scroll_child.addView(confirm, scroll_child.indexOfChild(pharmacy) + 1);
                    } else {
                        update.setTag(false);
                        for (int i = 0; i < 8; i++){
                            scroll_child.removeViewAt(scroll_child.indexOfChild(update) + 1);
                        }
                    }
                }
            });
            scroll_child.addView(update);

            Float finalStash = stash;
            Button refill = new Button(Activity);
            refill.setText("Refill Medication");
            refill.setTag(false);
            refill.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((Boolean) refill.getTag() == false) {
                        refill.setTag(true);

                        EditText current_count = new EditText(Activity);
                        current_count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        current_count.setHint("Refill Amount");

                        Button confirm = new Button(Activity);
                        confirm.setText("Confirm Medication Refill");
                        confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (finalControlled && current_count.getText().toString().length() == 0){
                                    warning.setView(null);
                                    warning.setNegativeButton("", null);
                                    warning.setTitle("Missing Count");
                                    warning.setMessage("Refills of controlled medications must be counted.");
                                    warning.setPositiveButton("OK", null);
                                    warning.show();
                                }
                                else {
                                    Float count = null;

                                    if (current_count.getText().toString().length() > 0){
                                        if (Float.parseFloat(current_count.getText().toString()) == 0){
                                            warning.setView(null);
                                            warning.setNegativeButton("", null);
                                            warning.setTitle("Invalid Count");
                                            warning.setMessage("0 is not a valid amount of medication to refill.");
                                            warning.setPositiveButton("OK", null);
                                            warning.show();
                                            return;
                                        }
                                        else {
                                            count = Float.parseFloat(current_count.getText().toString());
                                        }
                                    }

                                    Map<String, Object> entry = create_entry((int)prescription.get("client_id"), (int)prescription.get("id"),
                                            (String) prescription.get("name"), null, count, null, null,
                                            false, false, 0, "REFILL", null,
                                            null, null, null, null, null, new ArrayList<>());
                                    Map<String, String> new_packet = new HashMap<>();
                                    new_packet.put("count", current_count.getText().toString());
                                    new_packet.put("method", "refill");
                                    history.remove(0);
                                    history.add(0, () -> details(Activity, prescription, new_packet));
                                    confirm_refill(Activity, Arrays.asList(entry));
                                }
                            }
                        });

                        scroll_child.addView(current_count, scroll_child.indexOfChild(refill) + 1);
                        scroll_child.addView(confirm, scroll_child.indexOfChild(refill) + 2);

                    }
                    else {
                        refill.setTag(false);
                        scroll_child.removeViewAt(scroll_child.indexOfChild(refill) + 1);
                        scroll_child.removeViewAt(scroll_child.indexOfChild(refill) + 1);
                    }
                }
            });
            scroll_child.addView(refill);

            Button discontinue = new Button(Activity);
            discontinue.setText("Discontinue Prescription");
            discontinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, Object> entry = create_entry((int)prescription.get("client_id"), (int)prescription.get("id"),
                            (String) prescription.get("name"), finalStash, null, null, null,
                            false, false, 0, "PRESCRIPTION DISCONTINUED", null,
                            null, null, null, null, null, new ArrayList<>());

                    confirm_multiple_discontinue(Activity, new ArrayList<>(Arrays.asList(entry)));
                }
            });
            scroll_child.addView(discontinue);

            if (packet != null && !packet.get("method").equals("adherence")){
                if (packet.get("method").equals("take")){
                    take.callOnClick();
                    ((EditText)scroll_child.getChildAt(scroll_child.indexOfChild(take) + 1)).setText(packet.get("count"));
                    ((EditText)scroll_child.getChildAt(scroll_child.indexOfChild(take) + 3)).setText(packet.get("note"));
                }
                else if (packet.get("method").equals("update")){
                    update.callOnClick();

                    if(packet.get("as_needed").equals("true")){
                        ((CheckBox)scroll_child.getChildAt(scroll_child.indexOfChild(update) + 1)).setChecked(true);
                    }
                    else {
                        ((CheckBox)scroll_child.getChildAt(scroll_child.indexOfChild(update) + 1)).setChecked(false);
                    }

                    ((EditText)scroll_child.getChildAt(scroll_child.indexOfChild(update) + 2)).setText(packet.get("instructions"));
                    ((EditText)scroll_child.getChildAt(scroll_child.indexOfChild(update) + 3)).setText(packet.get("dose_max"));
                    ((EditText)scroll_child.getChildAt(scroll_child.indexOfChild(update) + 4)).setText(packet.get("daily_max"));
                    ((EditText)scroll_child.getChildAt(scroll_child.indexOfChild(update) + 5)).setText(packet.get("indication"));
                    ((EditText)scroll_child.getChildAt(scroll_child.indexOfChild(update) + 6)).setText(packet.get("prescriber"));
                    ((EditText)scroll_child.getChildAt(scroll_child.indexOfChild(update) + 7)).setText(packet.get("pharmacy"));
                }
                else if (packet.get("method").equals("refill")){
                    refill.callOnClick();
                    ((EditText)scroll_child.getChildAt(scroll_child.indexOfChild(refill) + 1)).setText(packet.get("count"));
                }
            }
        }

        Button adherence = new Button(Activity);
        adherence.setText("Check Prescription Adherence");
        adherence.setTag(false);
        DecimalFormat df = new DecimalFormat("###.##");
        adherence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(boolean)adherence.getTag()){
                    adherence.setTag(true);

                    TextView start = new TextView(Activity);
                    start.setText("Start Date");
                    scroll_child.addView(start);

                    DatePicker start_date = new DatePicker(Activity);
                    start_date.setMinDate((long)prescription.get("start"));
                    scroll_child.addView(start_date);

                    TextView end = new TextView(Activity);
                    end.setText("End Date");
                    scroll_child.addView(end);

                    DatePicker end_date = new DatePicker(Activity);
                    end_date.setMinDate((long)prescription.get("start"));
                    scroll_child.addView(end_date);

                    if ((int)prescription.get("active") == 0){
                        start_date.setMaxDate((long)prescription.get("end"));
                        end_date.setMaxDate((long)prescription.get("end"));
                    }
                    else {
                        end_date.setMaxDate(System.currentTimeMillis());
                        start_date.setMaxDate(System.currentTimeMillis());
                    }

                    Button result = new Button(Activity);
                    result.setText("Survey Says...");

                    Button calculate = new Button(Activity);
                    calculate.setText("Calculate Adherence");
                    scroll_child.addView(calculate);
                    scroll_child.addView(result);
                    calculate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String start_string = datepicker_to_date(start_date);
                            String end_string = datepicker_to_date(end_date);

                            long start_long = date_to_long(start_string);
                            long end_long = date_to_long(next_day(end_string ,1));

                            if (end_string.equals(long_to_date(System.currentTimeMillis()))){
                                end_long = date_to_long(end_string);
                                end_string = next_day(end_string, -1);
                            }

                            if (start_long >= end_long){
                                warning.setView(null);
                                warning.setNegativeButton("", null);
                                warning.setPositiveButton("Try Again", null);
                                warning.setTitle("Invalid Date Range");
                                warning.setMessage("The date range you entered is invalid.");
                                warning.show();
                                return;
                            }

                            String text = "";

                            List<Map<String, Object>> entries = entries_db.getRows(new String[]{"id", "change", "dose_override", "daily_override", "datetime"},
                                    new String[]{"prescription_id=" + prescription.get("id"), "method='TOOK MEDS'", "datetime>=" + start_long, "datetime<" + end_long},
                                    new String[]{"datetime", "ASC"}, false);

                            if ((long) prescription.get("start") > start_long) {
                                start_string = next_day(long_to_date((long) prescription.get("start")), 1);
                                start_long = date_to_long(start_string);
                            }

                            float numerator = 0;
                            int daily_override = 0;
                            int dose_override = 0;
                            int days = 1;

                            while (!start_string.equals(end_string)) {
                                days++;
                                start_string = next_day(start_string, 1);
                            }

                            String last_override_day = "";
                            for (int j = 0; j < entries.size(); j++) {
                                Map<String, Object> entry = entries.get(j);

                                numerator += (float) entry.get("change");

                                if ((int) entry.get("dose_override") == 1) {
                                    dose_override++;
                                }

                                if ((int)entry.get("daily_override") == 1 && !long_to_date((long)entry.get("datetime")).equals(last_override_day)){
                                    daily_override++;
                                    last_override_day = long_to_date((long)entry.get("datetime"));
                                }
                            }

                            if (prescription.get("daily_max") == null){
                                text += "No Adherence Guidelines";
                            }
                            else if ((int)prescription.get("as_needed") == 0) {
                                text += df.format(100 * numerator / (days * (float) prescription.get("daily_max"))) + "% adherence";
                            }
                            else {
                                text += "As Needed";
                            }

                            if (dose_override > 0) {
                                text += " | Exceeded dose limit " + dose_override + " time";
                                if (dose_override > 1) {
                                    text += "s";
                                }
                            }
                            if (daily_override > 0) {
                                text += " | Exceeded daily limit " + daily_override + " time";
                                if (daily_override > 1) {
                                    text += "s";
                                }
                            }

                            result.setText(text);

                            long finalTemp_start = start_long;
                            long finalTemp_end = end_long;

                            String finalText = text;
                            result.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Map<String, String> new_packet = new HashMap<>();
                                    new_packet.put("start", String.valueOf(finalTemp_start));
                                    new_packet.put("start_long", String.valueOf(date_to_long(datepicker_to_date(start_date))));
                                    new_packet.put("end", String.valueOf(finalTemp_end));

                                    Map<String, Integer> start_map = datepicker_data(start_date);
                                    Map<String, Integer> end_map = datepicker_data(end_date);

                                    new_packet.put("start_year", String.valueOf(start_map.get("year")));
                                    new_packet.put("start_month", String.valueOf(start_map.get("month")));
                                    new_packet.put("start_day", String.valueOf(start_map.get("day")));

                                    new_packet.put("end_year", String.valueOf(end_map.get("year")));
                                    new_packet.put("end_month", String.valueOf(end_map.get("month")));
                                    new_packet.put("end_day", String.valueOf(end_map.get("day")));
                                    new_packet.put("text", finalText);

                                    new_packet.put("method", "adherence");

                                    history.remove(0);
                                    history.add(0, () -> details(Activity, prescription, new_packet));

                                    med_adherence(Activity, entries, (String) prescription.get("name"), finalTemp_start, finalTemp_end, prescription);
                                }
                            });
                        }
                    });
                }
                else{
                    adherence.setTag(false);
                    for (int i = 0; i < 6; i++){
                        scroll_child.removeViewAt(scroll_child.indexOfChild(adherence) + 1);
                    }
                }

            }
        });
        scroll_child.addView(adherence);

        if (packet != null && packet.get("method").equals("adherence")){
            adherence.callOnClick();
            DatePicker start = (DatePicker)scroll_child.getChildAt(scroll_child.indexOfChild(adherence) + 2);
            DatePicker end = (DatePicker)scroll_child.getChildAt(scroll_child.indexOfChild(adherence) + 4);
            Button survey = (Button)scroll_child.getChildAt(scroll_child.indexOfChild(adherence) + 6);
            start.updateDate(Integer.parseInt(packet.get("start_year")), Integer.parseInt(packet.get("start_month")), Integer.parseInt(packet.get("start_day")));
            end.updateDate(Integer.parseInt(packet.get("end_year")), Integer.parseInt(packet.get("end_month")), Integer.parseInt(packet.get("end_day")));
            survey.setText(packet.get("text"));
            survey.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long entry_long = Long.parseLong(packet.get("start_long"));
                    long start_long = Long.parseLong(packet.get("start"));
                    long end_long = Long.parseLong(packet.get("end"));

                    List<Map<String, Object>> entries = entries_db.getRows(new String[]{"id", "change", "dose_override", "daily_override", "datetime"},
                            new String[]{"prescription_id=" + prescription.get("id"), "method='TOOK MEDS'", "datetime>=" + entry_long, "datetime<" + end_long},
                            new String[]{"datetime", "ASC"}, false);

                    med_adherence(Activity, entries, (String) prescription.get("name"), start_long, end_long, prescription);
                }
            });
        }
    }
}
