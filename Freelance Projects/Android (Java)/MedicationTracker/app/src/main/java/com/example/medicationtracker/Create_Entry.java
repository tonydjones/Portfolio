package com.example.medicationtracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.MainActivity.gson;

public class Create_Entry {
    public static Map<String, Object> create_entry(Integer client_id, int prescription_id, String drug, Float old_count, Float count, Float new_count, Long time, Boolean dose_override,
                                            Boolean daily_override, Integer edits, String method,
                                            String client_signature, Integer staff_1, String staff_signature_1, Integer staff_2, String staff_signature_2,
                                            String staff, List<String> notes){

        Map<String, Object> entries_map = new HashMap<>();
        entries_map.put("client_id", client_id);
        entries_map.put("prescription_id", prescription_id);
        entries_map.put("drug", drug);
        entries_map.put("datetime", time);
        entries_map.put("old_count", old_count);
        entries_map.put("change", count);
        entries_map.put("new_count", new_count);
        entries_map.put("dose_override", dose_override);
        entries_map.put("daily_override", daily_override);
        entries_map.put("edits", edits);
        entries_map.put("method", method);
        entries_map.put("client_signature", client_signature);
        entries_map.put("staff_1", staff_1);
        entries_map.put("staff_signature_1", staff_signature_1);
        entries_map.put("staff_2", staff_2);
        entries_map.put("staff_signature_2", staff_signature_2);
        entries_map.put("staff_present", staff);
        entries_map.put("notes", gson.toJson(notes));


        return entries_map;
    }
}
