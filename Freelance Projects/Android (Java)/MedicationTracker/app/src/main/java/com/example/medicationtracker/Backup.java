package com.example.medicationtracker;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.Admin.admin;
import static com.example.medicationtracker.Check_Password.check_password;
import static com.example.medicationtracker.Create_Client.create_client;
import static com.example.medicationtracker.DatabaseMaps.database_maps;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.editor;
import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.MainActivity.gson;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.MainActivity.prefs;
import static com.example.medicationtracker.MainActivity.prescriptions_db;
import static com.example.medicationtracker.MainActivity.presets_db;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.Staff.staff;
import static com.example.medicationtracker.UI_Message.ui_message;

public class Backup {
    public static void backup(MainActivity Activity, String name, List<Integer> delete_list){
        EditText pass = new EditText(Activity);
        pass.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pass.setHint("Enter Admin Password");
        warning.setNegativeButton("Cancel", null);
        warning.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!check_password(pass.getText().toString(), "admin")){
                    pass.setText("");
                    warning.setView(null);
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("OK", null);
                    warning.setTitle("Invalid password");
                    warning.setMessage("That password does not match the password of any current administrators.");
                    warning.show();
                }
                else {
                    long time = System.currentTimeMillis();

                    Map<String, String> client_map = database_maps("clients");

                    Map<String, String> prescriptions_map = database_maps("prescriptions");

                    Map<String, String> entries_map = database_maps("entries");

                    DatabaseHelper client_backup = new DatabaseHelper(Activity,
                            "MedTracker_" + time,
                            "clients",
                            client_map);


                    DatabaseHelper prescription_backup = new DatabaseHelper(Activity,
                            "MedTracker_" + time,
                            "prescriptions",
                            prescriptions_map);

                    DatabaseHelper entry_backup = new DatabaseHelper(Activity,
                            "MedTracker_" + time,
                            "entries",
                            entries_map);

                    DatabaseHelper preset_backup = new DatabaseHelper(Activity,
                            "MedTracker_" + time,
                            "presets",
                            database_maps("presets"));

                    List<Map<String, Object>> clients = clients_db.getRows(null, new String[]{"class='client'"}, null, false);
                    List<Map<String, Object>> prescriptions = prescriptions_db.getRows(null, null, null, false);
                    List<Map<String, Object>> entries = entries_db.getRows(null, null, null, false);
                    List<Map<String, Object>> presets = presets_db.getRows(null, null, null, false);

                    for (int i = 0; i < clients.size(); i++){
                        client_backup.addRow(clients.get(i));
                    }
                    for (int i = 0; i < prescriptions.size(); i++){
                        prescription_backup.addRow(prescriptions.get(i));
                    }
                    for (int i = 0; i < entries.size(); i++){
                        entry_backup.addRow(entries.get(i));
                    }
                    for (int i = 0; i < presets.size(); i++){
                        preset_backup.addRow(presets.get(i));
                    }

                    List<Long> backup_times =  gson.fromJson(prefs.getString("backup_times", gson.toJson(new ArrayList<>())), new TypeToken<List<Long>>(){}.getType());
                    backup_times.add(time);
                    Map<Long, String> backup_names =  gson.fromJson(prefs.getString("backup_names", gson.toJson(new HashMap<>())), new TypeToken<Map<Long, String> >(){}.getType());

                    if (name != null){
                        backup_names.put(time, name);
                    }

                    String message = "Data was successfully saved";

                    if (delete_list != null){
                        message += " and selected old backups were deleted.";
                        for (int i = delete_list.size() - 1; i >= 0; i--){
                            int index = delete_list.get(i);
                            long delete_time = backup_times.get(index);

                            DatabaseHelper delete_client_backup = new DatabaseHelper(Activity,
                                    "MedTracker_" + delete_time,
                                    "clients",
                                    client_map);

                            DatabaseHelper delete_prescription_backup = new DatabaseHelper(Activity,
                                    "MedTracker_" + delete_time,
                                    "prescriptions",
                                    prescriptions_map);

                            DatabaseHelper delete_entry_backup = new DatabaseHelper(Activity,
                                    "MedTracker_" + delete_time,
                                    "entries",
                                    entries_map);

                            Context context = Activity;

                            context.deleteDatabase(delete_client_backup.delete_table());
                            /*context.deleteDatabase(delete_prescription_backup.delete_table());
                            context.deleteDatabase(delete_entry_backup.delete_table());*/

                            backup_times.remove(index);
                            backup_names.remove(delete_time);
                        }
                    }

                    editor.putString("backup_names", gson.toJson(backup_names));
                    editor.putString("backup_times", gson.toJson(backup_times));
                    editor.apply();

                    history.remove(0);
                    history.remove(0);
                    if (delete_list != null){
                        history.remove(0);
                    }
                    admin(Activity);

                    ui_message(Activity, message);

                }
            }
        });
        warning.setView(pass);
        warning.setTitle("Confirm data backup");
        warning.setMessage("Are you sure you want to save a copy of the current databases? Enter your password to confirm.");
        warning.show();
    }
}
