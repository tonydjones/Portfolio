package com.example.medicationtracker;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.LongsToRange.longs_to_range;
import static com.example.medicationtracker.MainActivity.prescriptions_db;

public class UpdatePrescriptions {
    public static Pair<List<Map<String, Object>>, List<String>> update_prescriptions(int client_id, boolean add_all){

        List<Map<String, Object>> current_prescriptions =  prescriptions_db.getRows(new String[]{"id", "name", "start", "end", "active"},
                new String[]{"client_id="+client_id}, new String[]{"active", "DESC", "name", "ASC"}, false);

        List<String> current_names = new ArrayList<>();

        if(add_all){
            current_names.add("All Prescriptions");
        }

        for (int i = 0; i < current_prescriptions.size(); i++){
            Map<String, Object> prescription = current_prescriptions.get(i);
            String prescription_string = (String)prescription.get("name");
            if ((int) prescription.get("active") == 0){
                prescription_string += longs_to_range((long)prescription.get("start"), (long)prescription.get("end"));
            }
            current_names.add(prescription_string);
        }

        if (current_names.size() == 0){
            current_names.add("There are no prescriptions to edit");
        }

        return new Pair<>(current_prescriptions, current_names);
    }
}
