package com.example.medicationtracker;

import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.Confirm.confirm;
import static com.example.medicationtracker.CreateSignatureView.create_signature_view;
import static com.example.medicationtracker.Long_To_Date.long_to_date;
import static com.example.medicationtracker.MainActivity.admin_mode;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.gson;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.MainActivity.weighted_params;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.Wipe.wipe;

public class ClientConfirm {
    public static void client_confirm(MainActivity Activity, List<Map<String,Object>> entries, Map<Integer, Float> maxes) {
        wipe(Activity, "Client Confirm Medication Entries", () -> client_confirm(Activity, entries ,maxes));

        LinearLayout scroll_child = Activity.scroll_child;
        LinearLayout screen = Activity.screen;

        String text = "";

        for (int i = 0; i < entries.size(); i++) {

            Map<String,Object> entry = entries.get(i);

            text += "\n\n" + entries.get(i).get("drug");
            text += "\nAmount Taken: " + entries.get(i).get("change");

            if ((boolean)entry.get("dose_override")){
                text += "\nMAXIMUM DOSE OVERRIDE";
            }

            if ((boolean) entry.get("daily_override")){
                text += "\nDAILY MAXIMUM OVERRIDE";
            }

            List<String> note = gson.fromJson((String) entry.get("notes"), new TypeToken<List<String>>(){}.getType());
            for (int j = 0; j < note.size(); j++) {
                text += "\n" + note.get(j);
            }

        }

        TextView info = new TextView(Activity);
        info.setText(text.substring(2));
        scroll_child.addView(info);


        final SignatureView signature = create_signature_view(Activity);

        final Button confirm = new Button(Activity);
        confirm.setText("Client Signoff");

        if (admin_mode){
            CheckBox skip = new CheckBox(Activity);
            skip.setText("Client is not present");
            screen.addView(skip);

            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (skip.isChecked()){
                        confirm(Activity, entries, null, maxes);
                    }
                    else {
                        byte[] sign = signature.getBytes();
                        confirm(Activity, entries, sign, maxes);
                    }
                }
            });
        }
        else {
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    byte[] sign = signature.getBytes();
                    confirm(Activity, entries, sign, maxes);
                }
            });
        }

        final Button clear = new Button(Activity);
        clear.setText("Clear Canvas");
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signature.ClearCanvas();
            }
        });

        LinearLayout buttons = new LinearLayout(Activity);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.addView(clear);
        buttons.addView(confirm);
        confirm.setLayoutParams(weighted_params);

        screen.addView(signature);
        screen.addView(buttons);
    }
}
