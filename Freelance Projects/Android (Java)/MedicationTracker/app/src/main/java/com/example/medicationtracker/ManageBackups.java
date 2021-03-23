package com.example.medicationtracker;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.Check_Password.check_password;
import static com.example.medicationtracker.Create_Client.create_client;
import static com.example.medicationtracker.DatabaseMaps.database_maps;
import static com.example.medicationtracker.LongToDatetime.long_to_datetime;
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
import static com.example.medicationtracker.Wipe.wipe;

public class ManageBackups {
    public static void manage_backups(MainActivity Activity){
        wipe(Activity, "Manage Backup Data", () -> manage_backups(Activity));
        List<Long> backup_times =  gson.fromJson(prefs.getString("backup_times", gson.toJson(new ArrayList<>())), new TypeToken<List<Long>>(){}.getType());
        Map<Long, String> backup_names =  gson.fromJson(prefs.getString("backup_names", gson.toJson(new HashMap<>())), new TypeToken<Map<Long, String> >(){}.getType());
        LinearLayout scroll_child = Activity.scroll_child;

        for (int i = 0; i < backup_times.size(); i++){
            Button backup = new Button(Activity);
            long time = backup_times.get(i);
            if (backup_names.get(time) != null){
                backup.setText(backup_names.get(time) + "\n" + long_to_datetime(time));
            }
            else {
                backup.setText(long_to_datetime(time));
            }

            int finalI = i;
            backup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText pass = new EditText(Activity);
                    pass.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    pass.setHint("Enter Admin Password");
                    warning.setView(pass);
                    warning.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            warning.setNeutralButton("", null);
                        }
                    });
                    warning.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            warning.setNeutralButton("", null);
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

                                Context context = Activity;

                                context.deleteDatabase(client_backup.delete_table());
                                /*context.deleteDatabase(prescription_backup.delete_table());
                                context.deleteDatabase(entry_backup.delete_table());*/

                                scroll_child.removeView(backup);
                                backup_times.remove(finalI);
                                editor.putString("backup_times", gson.toJson(backup_times));
                                editor.apply();
                                ui_message(Activity, "Data backup at timestamp " + long_to_datetime(time) + " successfully deleted.");
                            }
                        }
                    });
                    warning.setPositiveButton("Restore", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            warning.setNeutralButton("", null);
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

                                clients_db.delete_single_constraint("class='client'");
                                prescriptions_db.reboot();
                                entries_db.reboot();

                                List<Map<String, Object>> clients = client_backup.getRows(null, null, null, false);
                                List<Map<String, Object>> prescriptions = prescription_backup.getRows(null, null, null, false);
                                List<Map<String, Object>> entries = entry_backup.getRows(null, null, null, false);
                                List<Map<String, Object>> presets = preset_backup.getRows(null, null, null, false);

                                for (int i = 0; i < clients.size(); i++){
                                    clients_db.addRow(clients.get(i));
                                }
                                for (int i = 0; i < prescriptions.size(); i++){
                                    prescriptions_db.addRow(prescriptions.get(i));
                                }
                                for (int i = 0; i < entries.size(); i++){
                                    entries_db.addRow(entries.get(i));
                                }
                                for (int i = 0; i < presets.size(); i++){
                                    presets_db.addRow(presets.get(i));
                                }

                                ui_message(Activity, "Data backup at timestamp " + long_to_datetime(time) + " successfully restored.");

                            }
                        }
                    });
                    warning.setTitle("Restore or Delete?");
                    warning.setMessage("Would you like to restore all data to how it was at this save point, or would you like to delete this backup from the archive? Please enter your password to confirm. You must be an administrator to perform either of these actions.");
                    warning.show();
                }
            });
            scroll_child.addView(backup);
        }
    }
}
