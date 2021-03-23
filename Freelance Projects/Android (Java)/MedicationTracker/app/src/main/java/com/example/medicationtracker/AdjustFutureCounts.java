package com.example.medicationtracker;

import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.MainActivity.prescriptions_db;

public class AdjustFutureCounts {
    public static void adjust_future_counts(int prescription_id, float count_change, long datetime){
        List<Map<String, Object>> old_entries = entries_db.getRows(new String[]{"change", "id", "method", "old_count", "new_count", "edits"},
                new String[]{"datetime>"+datetime, "prescription_id="+prescription_id},
                new String[]{"datetime", "ASC"}, false);
        boolean broken = false;
        for (int i = 0; i < old_entries.size(); i++){
            Map<String, Object> entry = old_entries.get(i);
            if (entry.get("method").equals("REFILL") || entry.get("method").equals("TOOK MEDS")){
                entry.put("old_count", (float)entry.get("old_count") + count_change);
                entry.put("new_count", (float)entry.get("new_count") + count_change);
            }
            else if (entry.get("method").equals("CLIENT DISCHARGED") || entry.get("method").equals("PRESCRIPTION DISCONTINUED") || entry.get("method").equals("DISCONTINUED DUE TO UPDATE")){
                entry.put("old_count", (float)entry.get("old_count") + count_change);
            }
            else if (entry.get("method").equals("COUNT")){
                entry.put("old_count", (float)entry.get("old_count") + count_change);
                entry.put("change", -1 * count_change);
                entry.put("method", "MISCOUNT");
                entry.put("edits", (int)entry.get("edits") + 1);
                entries_db.update(entry, new String[]{"id="+(int)entry.get("id")});
                broken = true;
                break;
            }
            else if (entry.get("method").equals("MISCOUNT")){
                entry.put("old_count", (float)entry.get("old_count") + count_change);
                entry.put("change", -1 * count_change + (float)entry.get("change"));
                if ((float) entry.get("old_count") == (float) entry.get("new_count")){
                    entry.put("method", "COUNT");
                }
                entry.put("edits", (int)entry.get("edits") + 1);
                entries_db.update(entry, new String[]{"id="+(int)entry.get("id")});
                broken = true;
                break;
            }
            entry.put("edits", (int)entry.get("edits") + 1);
            entries_db.update(entry, new String[]{"id="+(int)entry.get("id")});
        }

        if (!broken){
            Map<String, Object> prescription = prescriptions_db.getSingleRow(new String[]{"count"}, new String[]{"id="+prescription_id});
            prescription.put("count", (float)prescription.get("count") + count_change);
            prescriptions_db.update(prescription, new String[]{"id="+prescription_id});
        }
    }
}
