package com.example.medicationtracker;

import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.MainActivity.entries_db;

public class Swap_Prescriptions {

    public static void swap_prescriptions(int old_id, Float old_dose_max, Float old_daily_max, int new_id, Float new_dose_max, Float new_daily_max, long datetime, String name){

        List<Map<String, Object>> old_entries = null;
        if (new_id == old_id){
            old_entries = entries_db.getRows(new String[]{"change", "id", "method", "datetime"},
                    new String[]{"prescription_id="+old_id},
                    new String[]{"datetime", "ASC"}, false);

        }
        else {
            old_entries = entries_db.getRows(new String[]{"change", "id", "method", "datetime", "drug"},
                    new String[]{"datetime>"+datetime, "prescription_id="+old_id},
                    new String[]{"datetime", "ASC"}, false);
        }
        String day_string = "";
        float day_count = 0;
        for (int i = 0; i < old_entries.size(); i++){
            Map<String, Object> entry = old_entries.get(i);
            entry.put("prescription_id", new_id);
            entry.put("drug", name);
            if (entry.get("method").equals("TOOK MEDS")){
                if (new_dose_max == null){
                    entry.put("dose_override", false);
                }
                else if (new_dose_max != old_dose_max){
                    if ((float) entry.get("change") > new_dose_max){
                        entry.put("dose_override", true);
                    }
                    else{
                        entry.put("dose_override", false);
                    }
                }
                if (new_daily_max == null){
                    entry.put("daily_override", false);
                }
                else if (new_daily_max != old_daily_max){
                    if (!Long_To_Date.long_to_date((long)entry.get("datetime")).equals(day_string)){
                        day_count = 0;
                        day_string = Long_To_Date.long_to_date((long)entry.get("datetime"));
                    }

                    day_count += (float) entry.get("change");

                    if (day_count > new_daily_max){
                        entry.put("daily_override", true);
                    }
                    else{
                        entry.put("daily_override", false);
                    }
                }
            }
            entries_db.update(entry, new String[]{"id="+(int)entry.get("id")});
        }
    }
}
