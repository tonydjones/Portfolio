package com.example.medicationtracker;

import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.Create_Entry.create_entry;
import static com.example.medicationtracker.Long_To_Date.long_to_date;
import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.MainActivity.gson;
import static com.example.medicationtracker.MainActivity.prescriptions_db;

public class EditEntries {
    public static void edit_entries(int client_id, int id, Float new_dose_max, Float new_daily_max, Long start, Long end, Long count_time, String name,
                             Boolean active, Boolean switch_controlled, Float real_count, Float change, Float old_count, boolean check_dose, boolean check_daily, boolean reactivate_controlled){

        List<Map<String, Object>> old_entries = entries_db.getRows(new String[]{"change", "id", "method", "datetime", "drug", "dose_override", "daily_override", "edits", "notes"},
                new String[]{"prescription_id="+id},
                new String[]{"datetime", "DESC"}, false);

        List<String> end_methods = Arrays.asList("CLIENT DISCHARGED", "PRESCRIPTION DISCONTINUED", "DISCONTINUED DUE TO UPDATE");
        List<String> start_methods = Arrays.asList("INTAKE", "UPDATED PRESCRIPTION STARTED", "PRESCRIPTION STARTED");

        String day_string = "";
        float day_count = 0;

        if (switch_controlled){
            entries_db.addRow(create_entry(client_id, id, name, null, null, real_count, count_time, false, false, 1,
                    "COUNT", null, null, null, null, null, null,
                    Arrays.asList("Prescription was set to 'controlled' and count was adjusted through editing function by Admin.")));
        }
        else if (reactivate_controlled){
            entries_db.addRow(create_entry(client_id, id, name, null, null, real_count, count_time, false, false, 1,
                    "COUNT", null, null, null, null, null, null,
                    Arrays.asList("Controlled prescription was reactivated and count was set through editing function by Admin.")));
        }
        else if (change != null){
            entries_db.addRow(create_entry(client_id, id, name, old_count, change, real_count, count_time, false, false, 1,
                    "MISCOUNT", null, null, null, null, null, null,
                    Arrays.asList("Prescription count was adjusted through editing function by Admin.")));
        }

        if (active != null && !active){
            entries_db.addRow(create_entry(client_id, id, name, real_count, null, null, end, false, false, 1,
                    "PRESCRIPTION DISCONTINUED", null, null, null, null, null, null,
                    Arrays.asList("Prescription was discontinued through editing function by Admin.")));
        }

        for (int i = 0; i < old_entries.size(); i++){
            Map<String, Object> entry = old_entries.get(i);
            if (end_methods.contains(entry.get("method")) && active != null && active){
                if (!reactivate_controlled){
                    Map<String, Object> script = new HashMap<>();
                    script.put("count", real_count);
                    prescriptions_db.update(script, new String[]{"id="+id});
                    entries_db.delete_single_constraint("id="+entry.get("id"));
                }
                else {
                    reactivate_controlled = false;
                }
            }
            else {
                boolean edited = false;
                if (!entry.get("drug").equals(name)){
                    entry.put("drug", name);
                    edited = true;
                }
                if (entry.get("method").equals("TOOK MEDS")){
                    if (check_dose){
                        if ((int)entry.get("dose_override") == 1){
                            if (new_dose_max == null || (float) entry.get("change") <= new_dose_max){
                                entry.put("dose_override", false);
                                edited = true;
                            }
                        }
                        else if (new_dose_max != null && (float) entry.get("change") > new_dose_max){
                            entry.put("dose_override", true);
                            edited = true;
                        }
                    }


                    if (check_daily){
                        if (!long_to_date((long)entry.get("datetime")).equals(day_string)){
                            day_count = 0;
                            day_string = long_to_date((long)entry.get("datetime"));
                        }
                        day_count += (float) entry.get("change");

                        if ((int)entry.get("daily_override") == 1){
                            if (new_daily_max == null || day_count <= new_daily_max){
                                entry.put("daily_override", false);
                                edited = true;
                            }
                        }
                        else if (new_daily_max != null && day_count > new_daily_max){
                            entry.put("daily_override", true);
                            edited = true;
                        }
                    }
                }
                else if (end_methods.contains(entry.get("method")) && end != null){
                    entry.put("datetime", end);
                    edited = true;
                }
                else if (start_methods.contains(entry.get("method")) && start != null){
                    entry.put("datetime", start);
                    edited = true;
                }
                else if (entry.get("method").equals("COUNT")){
                    List<String> notes = gson.fromJson((String) entry.get("notes"), new TypeToken<List<String>>(){}.getType());
                    if (notes.get(0).equals("Controlled prescription was reactivated and count was set through editing function by Admin.")){
                        reactivate_controlled = true;
                    }
                }
                if (edited){
                    entry.put("edits", (int) entry.get("edits") + 1);
                }
                entries_db.update(entry, new String[]{"id="+(int)entry.get("id")});
            }
        }
    }
}
