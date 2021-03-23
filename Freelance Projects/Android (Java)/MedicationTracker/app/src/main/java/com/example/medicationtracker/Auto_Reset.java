package com.example.medicationtracker;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.Create_Client.create_client;
import static com.example.medicationtracker.Create_Entry.create_entry;
import static com.example.medicationtracker.Create_Prescription.create_prescription;
import static com.example.medicationtracker.Datetime_To_Long.datetime_to_long;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.MainActivity.prescriptions_db;
import static com.example.medicationtracker.MainActivity.presets_db;
import static com.example.medicationtracker.Swap_Prescriptions.swap_prescriptions;

public class Auto_Reset {
    public static void auto_reset(){
        long time = datetime_to_long("02/01/21 01:00");

        List<String> names = Arrays.asList("CHARLIE CLIENT", "DANTE DEMO", "ERIC EXAMPLE", "MATTHEW MODEL", "PATRICK PATIENT");

        String ids = "(";
        for (int i = 0; i < names.size(); i++){
            ids += "'" + names.get(i) + "', ";
        }
        ids = ids.substring(0, ids.length() - 2) + ")";

        List<Object> auto_id = clients_db.getSingleColumn("id", new String[]{"name IN "+ids, "admit="+time},
                null, false);

        for (int i = 0; i < auto_id.size(); i++){
            int client = (int) auto_id.get(i);
            clients_db.delete_single_constraint("id="+client);
            prescriptions_db.delete_single_constraint("client_id="+client);
            entries_db.delete_single_constraint("client_id="+client);
            presets_db.delete_single_constraint("client_id="+client);
        }

        List<Long> entry_times = new ArrayList<>();
        entry_times.add(datetime_to_long("02/02/21 02:00"));
        entry_times.add(datetime_to_long("02/03/21 02:00"));
        entry_times.add(datetime_to_long("02/04/21 02:00"));
        entry_times.add(datetime_to_long("02/05/21 02:00"));
        entry_times.add(datetime_to_long("02/06/21 02:00"));
        entry_times.add(datetime_to_long("02/07/21 02:00"));
        entry_times.add(datetime_to_long("02/08/21 02:00"));
        entry_times.add(datetime_to_long("02/09/21 02:00"));
        entry_times.add(datetime_to_long("02/02/21 03:00"));
        entry_times.add(datetime_to_long("02/03/21 03:00"));
        entry_times.add(datetime_to_long("02/04/21 03:00"));
        entry_times.add(datetime_to_long("02/05/21 03:00"));
        entry_times.add(datetime_to_long("02/06/21 03:00"));
        entry_times.add(datetime_to_long("02/07/21 03:00"));
        entry_times.add(datetime_to_long("02/08/21 03:00"));
        entry_times.add(datetime_to_long("02/09/21 03:00"));
        entry_times.add(datetime_to_long("02/02/21 04:00"));
        entry_times.add(datetime_to_long("02/03/21 04:00"));
        entry_times.add(datetime_to_long("02/04/21 04:00"));
        entry_times.add(datetime_to_long("02/05/21 04:00"));
        entry_times.add(datetime_to_long("02/06/21 04:00"));
        entry_times.add(datetime_to_long("02/07/21 04:00"));
        entry_times.add(datetime_to_long("02/08/21 04:00"));
        entry_times.add(datetime_to_long("02/09/21 04:00"));

        for (int i = 0; i < names.size(); i++){

            Map<String, Object> client = create_client(names.get(i), time, "client", null);

            int client_id = clients_db.addRow(client);

            Map<String, Object> suboxone = create_prescription(client_id, "Suboxone", "8/2 MG", (float)1.0, (float)2.0,
                    "Take 1 strip 2 times daily", false, true, (float)60, "Withdrawal",
                    "Dr. Smith", "CVS", time);

            Map<String, Object> gabapentin = create_prescription(client_id, "Gabapentin", "800 MG", (float)1.0, (float)2.0,
                    "Take 1 tab up to 2 times daily as needed for pain", true, true, (float)60, "Pain",
                    "Dr. Gabriella", "Walgreens", time);

            Map<String, Object> hydroxyzine = create_prescription(client_id, "Hydroxyzine", "25 MG", (float)1.0, (float)2.0,
                    "Take 1 tab up to 2 times daily as needed for anxiety", true, false, null, "Anxiety",
                    "Dr. Hector", "BMC", time);

            Map<String, Object> sertraline = create_prescription(client_id, "Sertraline", "100 MG", (float)1.0, (float)2.0,
                    "Take 1 tab twice daily", false, false, null, "Depression",
                    "Dr. Salzman", "MGH", time);

            List<Map<String, Object>> prescriptions = new ArrayList<>();

            prescriptions.add(gabapentin);
            prescriptions.add(sertraline);
            prescriptions.add(hydroxyzine);
            prescriptions.add(suboxone);

            for (int j = 0; j < prescriptions.size(); j++){
                Map<String, Object> prescription = prescriptions.get(j);

                int id = prescriptions_db.addRow(prescription);

                String drug = (String) prescription.get("name");
                Float count = (Float) prescription.get("count");

                entries_db.addRow(create_entry(client_id, id, drug, (float)0, count, count, time, false, false, 0, "INTAKE",
                        null, null, null, null,null, "auto_reset", new ArrayList<>()));

                for (int k = 0; k < entry_times.size(); k++){

                    if (i == 0 && k < 16){
                        Float taken = (float) 1;
                        Float sub = null;
                        if (count != null){
                            sub = count - taken;
                        }
                        entries_db.addRow(create_entry(client_id, id, drug, count, taken, sub, entry_times.get(k), false, false, 0, "TOOK MEDS",
                                null, null, null, null,null, "auto_reset", new ArrayList<>()));
                        if (count != null){
                            count -= taken;
                        }
                    }
                    else if (i == 1 && k < 8){
                        Float taken = (float) 1;
                        Float sub = null;
                        if (count != null){
                            sub = count - taken;
                        }
                        entries_db.addRow(create_entry(client_id, id, drug, count, taken, sub, entry_times.get(k), false, false, 0, "TOOK MEDS",
                                null, null, null, null,null, "auto_reset", new ArrayList<>()));
                        if (count != null){
                            count -= taken;
                        }
                    }
                    else if (i == 2 && k < 8){
                        Float taken = (float) 2;
                        Float sub = null;
                        if (count != null){
                            sub = count - taken;
                        }
                        entries_db.addRow(create_entry(client_id, id, drug, count, taken, sub, entry_times.get(k), false, false, 0, "TOOK MEDS",
                                null, null, null, null,null, "auto_reset", new ArrayList<>()));
                        if (count != null){
                            count -= taken;
                        }
                    }
                    else if (i == 3){
                        Float taken = (float) 1;
                        Float sub = null;
                        if (count != null){
                            sub = count - taken;
                        }
                        entries_db.addRow(create_entry(client_id, id, drug, count, taken, sub, entry_times.get(k), false, false, 0, "TOOK MEDS",
                                null, null, null, null,null, "auto_reset", new ArrayList<>()));
                        if (count != null){
                            count -= taken;
                        }
                    }
                    else if (i == 4 && k < 16){
                        Float taken = (float) 2;
                        Float sub = null;
                        if (count != null){
                            sub = count - taken;
                        }
                        entries_db.addRow(create_entry(client_id, id, drug, count, taken, sub, entry_times.get(k), false, false, 0, "TOOK MEDS",
                                null, null, null, null, null, "auto_reset", new ArrayList<>()));
                        if (count != null){
                            count -= taken;
                        }
                    }
                }
                prescription.put("count", count);
                prescriptions_db.update(prescription, new String[]{"id="+id});

                swap_prescriptions(id, (float) -1, (float) -1, id, (float)prescription.get("dose_max"),
                        (float)prescription.get("daily_max"), time, (String)prescription.get("name"));

            }
        }
    }
}
