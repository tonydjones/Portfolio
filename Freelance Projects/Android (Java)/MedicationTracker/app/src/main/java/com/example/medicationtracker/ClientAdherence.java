package com.example.medicationtracker;

import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.DateToLong.date_to_long;
import static com.example.medicationtracker.DatepickerData.datepicker_data;
import static com.example.medicationtracker.DatepickerToDate.datepicker_to_date;
import static com.example.medicationtracker.Long_To_Date.long_to_date;
import static com.example.medicationtracker.LongsToRange.longs_to_range;
import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.MainActivity.prescriptions_db;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.MedAdherence.med_adherence;
import static com.example.medicationtracker.NextDay.next_day;
import static com.example.medicationtracker.Wipe.wipe;

public class ClientAdherence {
    public static void client_adherence(MainActivity Activity, int client_id, long start_date, Map<String, Object> packet) {
        wipe(Activity, "Overall Medication Adherence", () -> client_adherence(Activity, client_id, start_date, packet));
        LinearLayout scroll_child = Activity.scroll_child;

        DecimalFormat df = new DecimalFormat("###.##");

        TextView start_prompt = new TextView(Activity);
        start_prompt.setText("Start Date");
        scroll_child.addView(start_prompt);

        DatePicker start = new DatePicker(Activity);
        start.setMaxDate(System.currentTimeMillis());
        start.setMinDate(start_date);
        scroll_child.addView(start);

        TextView end_prompt = new TextView(Activity);
        end_prompt.setText("End Date");
        scroll_child.addView(end_prompt);

        DatePicker end = new DatePicker(Activity);
        end.setMaxDate(System.currentTimeMillis());
        end.setMinDate(start_date);
        scroll_child.addView(end);

        TextView active = new TextView(Activity);
        active.setText("Active Prescriptions");

        TextView discontinued = new TextView(Activity);
        discontinued.setText("Discontinued Prescriptions");

        List<Map<String, Object>> prescriptions = prescriptions_db.getRows(new String[]{"id", "name", "start", "end", "as_needed", "dose_max", "daily_max", "active", "instructions"},
                new String[]{"client_id="+client_id}, new String[]{"name", "ASC"}, false);

        Button calculate = new Button(Activity);
        calculate.setText("Calculate Adherence");
        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, Object> new_packet = new HashMap<>();
                new_packet.put("start", datepicker_data(start));
                new_packet.put("end", datepicker_data(end));

                String start_string = datepicker_to_date(start);
                String end_string = datepicker_to_date(end);


                long start_long = date_to_long(start_string);
                long end_long = date_to_long(next_day(end_string ,1));

                if (end_string.equals(long_to_date(System.currentTimeMillis()))){
                    end_long = date_to_long(end_string);
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

                int add = 1;
                int dc_add = 1;

                for (int i = 0; i < prescriptions.size(); i++) {

                    Map<String, Object> prescription = prescriptions.get(i);

                    long temp_start = start_long;
                    long temp_end = end_long;

                    String first = start_string;
                    String last = end_string;

                    Button script;
                    if ((int)prescription.get("active") == 1) {
                        script = (Button) scroll_child.getChildAt(scroll_child.indexOfChild(active) + add);
                        add++;
                        if (last.equals(long_to_date(System.currentTimeMillis()))){
                            last = next_day(end_string, -1);
                        }
                    }
                    else {
                        script = (Button) scroll_child.getChildAt(scroll_child.indexOfChild(discontinued) + dc_add);
                        dc_add++;
                        if ((long) prescription.get("end") < end_long){
                            temp_end = date_to_long(long_to_date((long) prescription.get("end")));
                            last = next_day(long_to_date((long) prescription.get("end")), -1);
                        }
                    }

                    String text = (String) prescription.get("name");

                    if ((int) prescription.get("active") == 0) {
                        text += longs_to_range((long)prescription.get("start"), (long)prescription.get("end"));
                    }

                    List<Map<String, Object>> entries = entries_db.getRows(new String[]{"id", "change", "dose_override", "daily_override", "datetime"},
                            new String[]{"prescription_id="+prescription.get("id"), "method='TOOK MEDS'", "datetime>="+temp_start, "datetime<"+temp_end},
                            new String[]{"datetime", "ASC"}, false);

                    if ((long) prescription.get("start") > start_long){
                        first = next_day(long_to_date((long) prescription.get("start")), 1);
                        temp_start = date_to_long(first);
                    }

                    if (temp_start >= temp_end){
                        text += ": Not Enough Data";
                        script.setText(text);
                        script.setOnClickListener(null);
                        continue;
                    }

                    float numerator = 0;
                    int daily_override = 0;
                    int dose_override = 0;
                    int days = 1;

                    while (!first.equals(last)){
                        days++;
                        first = next_day(first, 1);
                    }

                    String last_override_day = "";
                    for (int j = 0; j < entries.size(); j++){
                        Map<String, Object> entry = entries.get(j);

                        numerator += (float)entry.get("change");

                        if ((int)entry.get("dose_override") == 1){
                            dose_override++;
                        }

                        if ((int)entry.get("daily_override") == 1 && !long_to_date((long)entry.get("datetime")).equals(last_override_day)){
                            daily_override++;
                            last_override_day = long_to_date((long)entry.get("datetime"));
                        }
                    }

                    if (prescription.get("daily_max") == null){
                        text += ": No Adherence Guidelines";
                    }
                    else if ((int)prescription.get("as_needed") == 0) {
                        text += ": " + df.format(100 * numerator / (days * (float) prescription.get("daily_max"))) + "% adherence";
                    }
                    else {
                        text += ": As Needed";
                    }

                    if (prescription.get("dose_max") == null){
                        prescription.put("dose_override", "No Dose Guidelines");
                    }
                    else {
                        prescription.put("dose_override", dose_override);
                    }

                    if (dose_override > 0) {
                        text += " | Exceeded dose limit " + dose_override + " time";
                        if (dose_override > 1){
                            text += "s";
                        }
                    }
                    if (daily_override > 0) {
                        text += " | Exceeded daily limit " + daily_override + " time";
                        if (daily_override > 1){
                            text += "s";
                        }
                    }

                    script.setText(text);

                    long finalTemp_start = temp_start;
                    long finalTemp_end = temp_end;
                    script.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            history.remove(0);
                            history.add(0, () -> client_adherence(Activity, client_id, start_date, new_packet));
                            med_adherence(Activity, entries, (String) prescription.get("name"), finalTemp_start, finalTemp_end, prescription);
                        }
                    });

                    Map<String, Object> mini_packet = new HashMap<>();
                    mini_packet.put("text", text);
                    mini_packet.put("start", temp_start);
                    mini_packet.put("start_long", start_long);
                    mini_packet.put("end", temp_end);

                    new_packet.put(String.valueOf(prescription.get("id")), mini_packet);
                }
            }
        });

        scroll_child.addView(calculate);

        scroll_child.addView(active);

        scroll_child.addView(discontinued);

        int position = scroll_child.indexOfChild(active) + 1;
        int trigger = 0;

        for (int i = 0; i < prescriptions.size(); i++) {
            Button script = new Button(Activity);
            Map<String, Object> prescription = prescriptions.get(i);
            String text = (String)prescription.get("name");
            if ((int) prescription.get("active") == 0) {
                if (packet != null){
                    Map<String, Object> mini_packet = (Map<String, Object>)packet.get(String.valueOf(prescription.get("id")));
                    script.setText((String)mini_packet.get("text"));
                    script.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            long temp_start = (long)mini_packet.get("start");
                            long temp_end = (long)mini_packet.get("end");
                            long start_long = (long)mini_packet.get("start_long");
                            List<Map<String, Object>> entries = entries_db.getRows(new String[]{"id", "change", "dose_override", "daily_override", "datetime"},
                                    new String[]{"prescription_id="+prescription.get("id"), "method='TOOK MEDS'", "datetime>="+start_long, "datetime<"+temp_end},
                                    new String[]{"datetime", "ASC"}, false);
                            med_adherence(Activity, entries, (String) prescription.get("name"), temp_start, temp_end, prescription);
                        }
                    });
                }
                else {
                    text += longs_to_range((long)prescription.get("start"), (long)prescription.get("end"));
                    script.setText(text);
                }

                scroll_child.addView(script);
                trigger++;
            } else {
                if (packet != null){
                    Map<String, Object> mini_packet = (Map<String, Object>)packet.get(String.valueOf(prescription.get("id")));
                    script.setText((String)mini_packet.get("text"));
                    script.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            long temp_start = (long)mini_packet.get("start");
                            long temp_end = (long)mini_packet.get("end");
                            long start_long = (long)mini_packet.get("start_long");
                            List<Map<String, Object>> entries = entries_db.getRows(new String[]{"id", "change", "dose_override", "daily_override", "datetime"},
                                    new String[]{"prescription_id="+prescription.get("id"), "method='TOOK MEDS'", "datetime>="+start_long, "datetime<"+temp_end},
                                    new String[]{"datetime", "ASC"}, false);
                            med_adherence(Activity, entries, (String) prescription.get("name"), temp_start, temp_end, prescription);
                        }
                    });
                }
                else {
                    script.setText(text);
                }
                scroll_child.addView(script, position);
                position++;
            }
        }

        if (position == scroll_child.indexOfChild(active) + 1){
            scroll_child.removeView(active);
        }
        if (trigger == 0){
            scroll_child.removeView(discontinued);
        }

        if (packet != null){
            start.updateDate(((Map<String, Integer>)packet.get("start")).get("year"), ((Map<String, Integer>)packet.get("start")).get("month"), ((Map<String, Integer>)packet.get("start")).get("day"));
            end.updateDate(((Map<String, Integer>)packet.get("end")).get("year"), ((Map<String, Integer>)packet.get("end")).get("month"), ((Map<String, Integer>)packet.get("end")).get("day"));
        }
    }
}
