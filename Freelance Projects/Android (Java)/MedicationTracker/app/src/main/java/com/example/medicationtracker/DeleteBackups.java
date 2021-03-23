package com.example.medicationtracker;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.LongToDatetime.long_to_datetime;
import static com.example.medicationtracker.MainActivity.gson;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.MainActivity.prefs;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.ManageBackups.manage_backups;
import static com.example.medicationtracker.SetupBackup.setup_backup;
import static com.example.medicationtracker.Wipe.wipe;

public class DeleteBackups {
    public static void delete_backups(MainActivity Activity, List<Integer> packet) {
        wipe(Activity, "Delete Old Backup Data", () -> delete_backups(Activity, packet));
        List<Long> backup_times = gson.fromJson(prefs.getString("backup_times", gson.toJson(new ArrayList<>())), new TypeToken<List<Long>>() {
        }.getType());
        Map<Long, String> backup_names = gson.fromJson(prefs.getString("backup_names", gson.toJson(new HashMap<>())), new TypeToken<Map<Long, String>>() {
        }.getType());
        LinearLayout scroll_child = Activity.scroll_child;

        for (int i = 0; i < backup_times.size(); i++) {
            Button backup = new Button(Activity);
            long time = backup_times.get(i);
            if (backup_names.get(time) != null) {
                backup.setText(backup_names.get(time) + "\n" + long_to_datetime(time));
            } else {
                backup.setText(long_to_datetime(time));
            }

            backup.setTag(false);
            backup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!(boolean)backup.getTag()){
                        backup.setTag(true);
                        backup.setBackgroundColor(Color.parseColor("#FF0000"));
                    }
                    else {
                        backup.setTag(false);
                        backup.setBackgroundResource(android.R.drawable.btn_default);

                    }
                }
            });
            scroll_child.addView(backup);

            if (packet != null && packet.contains(i)){
                backup.callOnClick();
            }
        }

        Button delete = new Button(Activity);
        delete.setText("Delete Selected Backups");
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Integer> delete_list = new ArrayList<>();
                for (int i = 0; i < backup_times.size(); i++){
                    if ((boolean)scroll_child.getChildAt(i).getTag()){
                        delete_list.add(i);
                    }
                }
                if (delete_list.size() == 0){
                    warning.setView(null);
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("OK", null);
                    warning.setTitle("No Backup Selected For Deletion");
                    warning.setMessage("You must select at least one of your past manual backups for deletion.");
                    warning.show();
                    return;
                }
                history.remove(0);
                history.add(0, () -> delete_backups(Activity, delete_list));
                setup_backup(Activity, delete_list);
            }
        });
        scroll_child.addView(delete);
    }
}
