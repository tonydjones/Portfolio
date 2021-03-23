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

import static com.example.medicationtracker.Admin_Password.admin_password;
import static com.example.medicationtracker.Backup.backup;
import static com.example.medicationtracker.Check_Password.check_password;
import static com.example.medicationtracker.Clients.clients;
import static com.example.medicationtracker.DatabaseMaps.database_maps;
import static com.example.medicationtracker.Delete.delete;
import static com.example.medicationtracker.DeleteBackups.delete_backups;
import static com.example.medicationtracker.Edit.edit;
import static com.example.medicationtracker.MainActivity.admin_mode;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.editor;
import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.MainActivity.gson;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.MainActivity.prefs;
import static com.example.medicationtracker.MainActivity.prescriptions_db;
import static com.example.medicationtracker.MainActivity.record_password;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.ManageBackups.manage_backups;
import static com.example.medicationtracker.Setup.setup;
import static com.example.medicationtracker.SetupBackup.setup_backup;
import static com.example.medicationtracker.Spreadsheets.spreadsheets;
import static com.example.medicationtracker.Staff.staff;
import static com.example.medicationtracker.UI_Message.ui_message;
import static com.example.medicationtracker.Wipe.wipe;

public class Admin {
    public static void admin(MainActivity Activity) {

        if (!admin_mode){
            admin_password(Activity, () -> admin(Activity), "admin");
            return;
        }

        wipe(Activity, "Admin", () -> admin(Activity));
        LinearLayout scroll_child = Activity.scroll_child;

        Button change_pass = new Button(Activity);
        if (record_password) {
            change_pass.setText("Remove Password Protection On Staff Functions");
        } else {
            change_pass.setText("Require Password Protection On Staff Functions");
        }
        change_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (record_password) {
                    record_password = false;
                    editor.putBoolean("record_password", false);
                    editor.apply();
                    change_pass.setText("Require Password Protection On Staff Functions");
                } else {
                    record_password = true;
                    editor.putBoolean("record_password", true);
                    editor.apply();
                    change_pass.setText("Remove Password Protection On Staff Functions");
                }
            }
        });
        //scroll_child.addView(change_pass);

        Button view_client = new Button(Activity);
        view_client.setText("Clients");
        view_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clients(Activity);
            }
        });
        scroll_child.addView(view_client);

        Button staff = new Button(Activity);
        staff.setText("Staff");
        staff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                staff(Activity);
            }
        });
        scroll_child.addView(staff);

        Button spreadsheets = new Button(Activity);
        spreadsheets.setText("Generate Spreadsheets");
        spreadsheets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spreadsheets(Activity);
            }
        });
        scroll_child.addView(spreadsheets);

        Button edit = new Button(Activity);
        edit.setText("Edit Data");
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit(Activity, null);
            }
        });
        scroll_child.addView(edit);

        Button backup = new Button(Activity);
        backup.setText("Back Up Data");
        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<Long> backup_times =  gson.fromJson(prefs.getString("backup_times", gson.toJson(new ArrayList<>())), new TypeToken<List<Long>>(){}.getType());
                if (backup_times.size() >= 5){
                    warning.setView(null);
                    warning.setNegativeButton("Cancel", null);
                    warning.setTitle("Too Many Backups");
                    warning.setMessage("You are only allowed to have 5 manual backups saved. You will need to delete an old backup to make a new one. Would you like to manage your backups?");
                    warning.setPositiveButton("Manage", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            delete_backups(Activity, null);
                        }
                    });
                    warning.show();
                }
                else{
                    setup_backup(Activity, null);
                }
            }
        });
        scroll_child.addView(backup);

        Button restore = new Button(Activity);
        restore.setText("Manage Backup Data");
        restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manage_backups(Activity);
            }
        });
        scroll_child.addView(restore);

        Button delete = new Button(Activity);
        delete.setText("Delete Data");
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete(Activity);
            }
        });
        scroll_child.addView(delete);

        Button reset = new Button(Activity);
        reset.setText("Reset App");
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                warning.setNegativeButton("Cancel", null);
                warning.setView(null);
                warning.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText pass = new EditText(Activity);
                        pass.setHint("Enter Password");
                        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        warning.setView(pass);
                        warning.setNegativeButton("Stop! This is a mistake!", null);
                        warning.setPositiveButton("YES! DO IT! DO IT!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!check_password(pass.getText().toString(), "admin")){
                                    warning.setNegativeButton("", null);
                                    warning.setPositiveButton("Drat! Foiled again!", null);
                                    warning.setTitle("Dodged a bullet there...");
                                    warning.setView(null);
                                    warning.setMessage("Looks like you don't know the admin password... phew! The data is safe from lunatics like you...");
                                    warning.show();
                                    return;
                                }

                                clients_db.reboot();
                                prescriptions_db.reboot();
                                entries_db.reboot();


                                List<Long> backup_times =  gson.fromJson(prefs.getString("backup_times", gson.toJson(new ArrayList<>())), new TypeToken<List<Long>>(){}.getType());

                                Map<String, String> client_map = database_maps("clients");

                                Map<String, String> prescriptions_map = database_maps("prescriptions");

                                Map<String, String> entries_map = database_maps("entries");

                                for (int i = 0; i < backup_times.size(); i++){
                                    long time = backup_times.get(i);

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
                                }
                                history.clear();
                                editor.clear().apply();
                                record_password = true;
                                ui_message(Activity, "The application was reset and all data was deleted. Hope you're happy with yourself, you monster.");
                                setup(Activity);
                            }
                        });
                        warning.setTitle("SERIOUSLY, ARE YOU SURE?");
                        warning.setMessage("This ain't no game, kid! There's no going back once you click that button! You're gonna delete everything! You're " +
                                "one button away from a total data apocalypse! This ain't no joke! Make sure you know what you're doing!");
                        warning.show();
                    }
                });
                warning.setTitle("Are you sure?");
                warning.setMessage("Resetting the application will essentially mimic an uninstall and reinstall of the application. This will " +
                        "delete all current staff, clients, prescriptions, and entries from the databases. This will also " +
                        "remove all passwords and delete all backups. None of this data can be recovered. Previously created spreadsheets will still " +
                        "be in your device files. Are you sure you want to proceed?");
                warning.show();
            }
        });
        scroll_child.addView(reset);
    }
}
