package com.example.medicationtracker;

import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.ConfirmMultipleDiscontinue.confirm_multiple_discontinue;
import static com.example.medicationtracker.Create_Entry.create_entry;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.MainActivity.prescriptions_db;
import static com.example.medicationtracker.Wipe.wipe;

public class DiscontinueMultiple {
    public static void discontinue_multiple(MainActivity Activity, int client_id, List<Integer> packet) {
        wipe(Activity, "Discontinue Prescriptions", () -> discontinue_multiple(Activity, client_id, packet));
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
                        current_count.setEnabled(false);
                        current_count.setHint(script.get("name") + " will be discontinued");

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
            if (packet != null && packet.contains(script.get("id"))){
                current_script.callOnClick();
            }
        }
        Button confirm = new Button(Activity);
        confirm.setText("Confirm Discontinued Meds");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int add = 0;
                List<Map<String, Object>> entries = new ArrayList<>();
                List<Integer> new_packet = new ArrayList<>();
                for (int i = 0; i < prescriptions.size(); i++) {
                    if (scroll_child.getChildAt(i + add).getTag() != null && (Boolean) scroll_child.getChildAt(add + i).getTag()) {
                        Float count = null;
                        if (prescriptions.get(i).get("count") != null){
                            count = (float) prescriptions.get(i).get("count");
                        }

                        Map<String, Object> entry = create_entry(client_id, (int)prescriptions.get(i).get("id"),
                                ((Button)scroll_child.getChildAt(i + add)).getText().toString(), count, null, null, null,
                                false, false, 0, "PRESCRIPTION DISCONTINUED", null,
                                null, null, null, null, null, new ArrayList<>());
                        entries.add(entry);
                        add++;

                        new_packet.add((int)prescriptions.get(i).get("id"));
                    }
                }
                if (entries.size() > 0){
                    history.remove(0);
                    history.add(0, () -> discontinue_multiple(Activity, client_id, new_packet));
                    confirm_multiple_discontinue(Activity, entries);
                }
            }
        });
        scroll_child.addView(confirm);
    }
}
