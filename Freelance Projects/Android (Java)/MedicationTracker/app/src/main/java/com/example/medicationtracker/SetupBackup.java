package com.example.medicationtracker;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.Admin.admin;
import static com.example.medicationtracker.Backup.backup;
import static com.example.medicationtracker.MainActivity.gson;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.MainActivity.prefs;
import static com.example.medicationtracker.ManageBackups.manage_backups;
import static com.example.medicationtracker.Wipe.wipe;

public class SetupBackup {
    public static void setup_backup(MainActivity Activity, List<Integer> delete_list){
        wipe(Activity, "Set up Backup", () -> setup_backup(Activity, delete_list));
        LinearLayout scroll_child = Activity.scroll_child;

        CheckBox def = new CheckBox(Activity);
        def.setChecked(true);
        def.setText("Use default name for this backup.");
        scroll_child.addView(def);

        def.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (def.isChecked()){
                    scroll_child.removeViewAt(0);
                }
                else {
                    EditText name = new EditText(Activity);
                    name.setHint("Backup Name");
                    scroll_child.addView(name, 0);
                }
            }
        });

        Button save = new Button(Activity);
        save.setText("Save Backup");
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!def.isChecked() && ((EditText)scroll_child.getChildAt(0)).getText().toString().length() > 0){
                    backup(Activity, ((EditText)scroll_child.getChildAt(0)).getText().toString(), delete_list);
                }
                else {
                    backup(Activity, null, delete_list);
                }
            }
        });
        scroll_child.addView(save);
    }
}
