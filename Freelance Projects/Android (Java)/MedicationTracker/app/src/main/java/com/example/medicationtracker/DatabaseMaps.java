package com.example.medicationtracker;

import java.util.HashMap;
import java.util.Map;

public class DatabaseMaps {
    public static Map<String, String> database_maps(String type){

        Map<String, String> map = new HashMap<>();

        if (type.equals("clients")){
            map.put("name", "TINYTEXT");
            map.put("admit", "BIGINT");
            map.put("active", "BOOLEAN");
            map.put("discharge", "BIGINT");
            map.put("edits", "TINYINT");
            map.put("class", "TINYTEXT");
            map.put("password", "TINYTEXT");
        }
        else if (type.equals("prescriptions")){

            map.put("client_id", "INT");
            map.put("drug", "TINYTEXT");
            map.put("dose", "TINYTEXT");
            map.put("dose_max", "FLOAT");
            map.put("daily_max", "FLOAT");
            map.put("instructions", "TINYTEXT");
            map.put("as_needed", "BOOLEAN");
            map.put("controlled", "BOOLEAN");
            map.put("count", "FLOAT");
            map.put("indication", "TINYTEXT");
            map.put("prescriber", "TINYTEXT");
            map.put("pharmacy", "TINYTEXT");
            map.put("start", "BIGINT");
            map.put("end", "BIGINT");
            map.put("active", "BOOLEAN");
            map.put("name", "TINYTEXT");
            map.put("edits", "TINYINT");
        }
        else if (type.equals("entries")){
            map.put("client_id", "INT");
            map.put("prescription_id", "INT");
            map.put("drug", "TINYTEXT");
            map.put("datetime", "BIGINT");
            map.put("old_count", "FLOAT");
            map.put("change", "FLOAT");
            map.put("new_count", "FLOAT");
            map.put("dose_override", "BOOLEAN");
            map.put("daily_override", "BOOLEAN");
            map.put("edits", "TINYINT");
            map.put("method", "TINYTEXT");
            map.put("client_signature", "LONGTEXT");
            map.put("staff_1", "TINYINT");
            map.put("staff_signature_1", "LONGTEXT");
            map.put("staff_2", "TINYINT");
            map.put("staff_signature_2", "LONGTEXT");
            map.put("staff_present", "TINYTEXT");
            map.put("notes", "LONGTEXT");
        }
        else if (type.equals("presets")){
            map.put("client_id", "INT");
            map.put("name", "TINYTEXT");
            map.put("preset", "INT");
        }
        return map;
    }
}
