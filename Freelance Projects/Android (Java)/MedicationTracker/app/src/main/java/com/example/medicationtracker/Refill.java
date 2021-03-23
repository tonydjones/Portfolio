package com.example.medicationtracker;

import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.ConfirmRefill.confirm_refill;
import static com.example.medicationtracker.Create_Entry.create_entry;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.MainActivity.prescriptions_db;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.Wipe.wipe;

public class Refill {
    public static void refill(MainActivity Activity, int client_id, Map<Integer, String> packet) {
        wipe(Activity, "Log Refills", () -> refill(Activity, client_id, packet));
        LinearLayout scroll_child = Activity.scroll_child;

        List<Map<String, Object>> prescriptions = prescriptions_db.getRows(
                new String[]{"name", "id", "controlled", "count"},
                new String[]{"client_id=" + client_id, "active=1"},
                new String[]{"name", "ASC"}, false);

        for (int i = 0; i < prescriptions.size(); i++) {
            Map<String, Object> script = prescriptions.get(i);

            final Button current_script = new Button(Activity);
            current_script.setText((String) script.get("name"));
            current_script.setTag(false);
            current_script.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!((Boolean) current_script.getTag())) {
                        current_script.setTag(true);

                        EditText current_count = new EditText(Activity);
                        current_count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        current_count.setHint("Refill Amount");

                        current_count.setTag((int) script.get("controlled") == 1);
                        scroll_child.addView(current_count, scroll_child.indexOfChild(current_script) + 1);

                    }
                    else {
                        current_script.setTag(false);
                        scroll_child.removeViewAt(scroll_child.indexOfChild(current_script) + 1);
                    }
                }
            });
            scroll_child.addView(current_script);
            if (packet != null && packet.get(script.get("id")) != null){
                current_script.callOnClick();
                ((EditText) scroll_child.getChildAt(scroll_child.indexOfChild(current_script) + 1)).setText(packet.get(script.get("id")));
            }
        }
        Button confirm = new Button(Activity);
        confirm.setText("Confirm Refilled Meds");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int add = 0;
                List<Map<String, Object>> entries = new ArrayList<>();
                Map<Integer, String> new_packet = new HashMap<>();
                for (int i = 0; i < prescriptions.size(); i++) {
                    if (scroll_child.getChildAt(i + add).getTag() != null && (Boolean) scroll_child.getChildAt(add + i).getTag()) {

                        if (((EditText) scroll_child.getChildAt(i + add + 1)).getText().toString().length() == 0){

                            if ((Boolean)((scroll_child.getChildAt(i + add + 1)).getTag())) {
                                warning.setView(null);
                                warning.setNegativeButton("", null);
                                warning.setPositiveButton("OK", null);
                                warning.setTitle("No Refill Count Given For " + ((Button)scroll_child.getChildAt(i + add)).getText().toString());
                                warning.setMessage("All controlled medications must be counted when being refilled.");
                                warning.show();
                                return;
                            }
                        }
                        else if (Float.parseFloat(((EditText)scroll_child.getChildAt(i + add + 1)).getText().toString()) == 0) {
                            warning.setView(null);
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("OK", null);
                            warning.setTitle("Invalid Refill Count Given For " + ((Button)scroll_child.getChildAt(i)).getText().toString());
                            warning.setMessage("0 is not a valid amount for a refill.");
                            warning.show();
                            return;
                        }

                        Float count = null;
                        if (((EditText)scroll_child.getChildAt(i + add + 1)).getText().toString().length() > 0){
                            count = Float.parseFloat(((EditText)scroll_child.getChildAt(i + add + 1)).getText().toString());
                        }

                        Map<String, Object> entry;
                        if ((int)prescriptions.get(i).get("controlled") == 1){
                            entry = create_entry(client_id, (int)prescriptions.get(i).get("id"),
                                    ((Button)scroll_child.getChildAt(i + add)).getText().toString(), (float)prescriptions.get(i).get("count"),
                                    count, (float)prescriptions.get(i).get("count") + count, null,
                                    false, false, 0, "REFILL", null,
                                    null, null, null, null ,null, new ArrayList<>());
                        }
                        else {
                            entry = create_entry(client_id, (int)prescriptions.get(i).get("id"),
                                    ((Button)scroll_child.getChildAt(i + add)).getText().toString(), null,
                                    count, null, null,
                                    false, false, 0, "REFILL", null,
                                    null, null, null, null, null, new ArrayList<>());
                        }
                        entries.add(entry);
                        new_packet.put((int)prescriptions.get(i).get("id"), ((EditText)scroll_child.getChildAt(i + add + 1)).getText().toString());
                        add++;
                    }
                }
                if (entries.size() > 0){
                    history.remove(0);
                    history.add(0, () -> refill(Activity, client_id, new_packet));
                    confirm_refill(Activity, entries);
                }
            }
        });
        scroll_child.addView(confirm);
    }
}
