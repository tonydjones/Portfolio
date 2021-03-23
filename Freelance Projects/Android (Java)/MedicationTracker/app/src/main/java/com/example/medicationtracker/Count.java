package com.example.medicationtracker;

import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.Clients.clients;
import static com.example.medicationtracker.CountSign1.count_sign_1;
import static com.example.medicationtracker.LongToDatetime.long_to_datetime;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.MainActivity.gson;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.Wipe.wipe;
import static com.example.medicationtracker.Create_Entry.create_entry;

public class Count {
    public static void count(MainActivity Activity, List<Map<String, Object>> prescriptions, List<Map<String, Object>> entries, Map<String, Object> packet) {

        if (prescriptions.size() == 0){
            if (entries.size() == 0){
                return;
            }
            else{
                count_sign_1(Activity, entries);
                return;
            }

        }

        List<Map<String, Object>> prescriptions_copy = new ArrayList<>();
        List<Map<String, Object>> entries_copy = new ArrayList<>();

        prescriptions_copy.addAll(prescriptions);
        entries_copy.addAll(entries);

        wipe(Activity, "Controlled Medication Count", () -> count(Activity, prescriptions_copy, entries_copy, null));
        LinearLayout scroll_child = Activity.scroll_child;

        Map<String, Object> prescription = prescriptions.get(0);

        String name = (String) clients_db.getObject("name", new String[]{"id=" + prescription.get("client_id")});
        String text = name;
        text += "\n" + prescription.get("name");
        text += "\nExpected Count: " + prescription.get("count");
        TextView med = new TextView(Activity);
        med.setText(text);
        scroll_child.addView(med);

        List<String> notes = new ArrayList<>();

        final Boolean[] miscount = {false};

        Button right = new Button(Activity);
        right.setText("Count is accurate");
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map <String, Object> entry = create_entry((int) prescription.get("client_id"), (int) prescription.get("id"), (String) prescription.get("name"),
                        (float) prescription.get("count"), (float)0, (float) prescription.get("count"), null, false, false, 0, "COUNT",
                        null, null, null, null, null, null, new ArrayList<>());
                entries.add(entry);

                prescriptions.remove(0);
                count(Activity, prescriptions, entries, null);
            }
        });
        scroll_child.addView(right);

        Button wrong = new Button(Activity);
        wrong.setText("Count is not accurate");
        wrong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!miscount[0]) {
                    miscount[0] = true;

                    String text = "";

                    long datetime = (long)(entries_db.getObject("MAX([datetime]) AS datetime",
                            new String[]{"prescription_id="+prescription.get("id"),
                                    "method IN ('COUNT', 'MISCOUNT', 'INTAKE', 'PRESCRIPTION STARTED', 'UPDATED PRESCRIPTION STARTED')"}));

                    List<Map<String, Object>> temp_entries = entries_db.getRows(null,
                            new String[]{"prescription_id="+prescription.get("id"), "datetime>="+datetime},
                            new String[]{"datetime", "DESC"}, false);

                    if (temp_entries.size() > 0){
                        text += "Here are the entries for this prescription since the last count:\n";
                        List<String> other_methods = Arrays.asList("CLIENT DISCHARGED", "COUNT", "MISCOUNT", "DISCONTINUED DUE TO UPDATE", "PRESCRIPTION DISCONTINUED");
                        for (int i = 0; i < temp_entries.size(); i++) {
                            Map<String, Object> entry = temp_entries.get(i);
                            TextView information = new TextView(Activity);
                            text += "\n" + long_to_datetime((long) entry.get("datetime"));
                            text += "\n" + entry.get("drug");
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
                                else if (!method.equals("COUNT") && !method.equals("MISCOUNT") && entry.get("change") != null) {
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

                            if ((int) entry.get("dose_override") == 1) {
                                text += "\nMAXIMUM DOSE OVERRIDE";
                            }

                            if ((int) entry.get("daily_override") == 1) {
                                text += "\nDAILY MAXIMUM OVERRIDE";
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

                            text += "\n";
                        }
                        text += "\n";
                    }

                    text += "Please enter the actual count and a note about how and why the count is inaccurate. Fill out an incident report if the discrepancy cannot be accounted for.";

                    TextView prompt = new TextView(Activity);
                    prompt.setText(text);
                    scroll_child.addView(prompt);

                    EditText count = new EditText(Activity);
                    count.setHint("New Count");
                    count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    scroll_child.addView(count);

                    EditText note = new EditText(Activity);
                    note.setHint("Note");
                    scroll_child.addView(note);

                    if (packet != null){
                        count.setText(String.valueOf(packet.get("new_count")));
                        note.setText(((List<String>)gson.fromJson((String) packet.get("notes"), new TypeToken<List<String>>(){}.getType())).get(0));
                    }

                    Button submit = new Button(Activity);
                    submit.setText("Submit Changes");
                    submit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (count.getText().toString().length() == 0){
                                warning.setView(null);
                                warning.setNegativeButton("", null);
                                warning.setPositiveButton("OK", null);
                                warning.setTitle("Invalid Count");
                                warning.setMessage("You must enter the current count if it is different than expected");
                                warning.show();

                            }
                            else if (Float.parseFloat(count.getText().toString()) == (float) prescription.get("count")) {
                                warning.setView(null);
                                warning.setNegativeButton("", null);
                                warning.setPositiveButton("OK", null);
                                warning.setTitle("Invalid Count");
                                warning.setMessage("In order to enter a miscount, the count you submit must be different from the expected count.");
                                warning.show();
                            }
                            else{
                                notes.add(note.getText().toString());

                                Map <String, Object> entry = create_entry((int) prescription.get("client_id"), (int) prescription.get("id"), (String) prescription.get("name"),
                                        (float) prescription.get("count"), Float.parseFloat(count.getText().toString()) - (float) prescription.get("count"),
                                        Float.parseFloat(count.getText().toString()),
                                        null, false, false, 0, "MISCOUNT",
                                        null, null, null, null, null, null, notes);
                                entries.add(entry);


                                prescriptions.remove(0);
                                history.remove(0);
                                history.add(0, () -> count(Activity, prescriptions_copy, entries_copy, entry));
                                count(Activity, prescriptions, entries, null);
                            }
                        }
                    });
                    scroll_child.addView(submit);
                }
            }
        });
        scroll_child.addView(wrong);
        if (packet != null){
            wrong.callOnClick();
        }
    }
}
