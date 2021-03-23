package com.example.medicationtracker;

import java.util.HashMap;
import java.util.Map;

public class Create_Prescription {
    public static Map<String, Object> create_prescription(Integer client_id, String drug, String dose, Float dose_max, Float daily_max, String instructions,
                                                   Boolean as_needed, Boolean controlled, Float count, String indication, String prescriber,
                                                   String pharmacy, Long time){
        drug = drug.toUpperCase();
        dose = dose.toUpperCase();

        Map<String, Object> prescription = new HashMap<>();
        prescription.put("client_id", client_id);
        prescription.put("drug", drug);
        prescription.put("dose", dose);
        prescription.put("dose_max", dose_max);
        prescription.put("daily_max", daily_max);
        prescription.put("instructions", instructions);
        prescription.put("as_needed", as_needed);
        prescription.put("controlled", controlled);
        prescription.put("count", count);
        prescription.put("indication", indication);
        prescription.put("prescriber", prescriber);
        prescription.put("pharmacy", pharmacy);
        prescription.put("start", time);
        prescription.put("active", true);
        prescription.put("name", drug + " " + dose);
        prescription.put("edits", 0);

        return prescription;
    }
}
