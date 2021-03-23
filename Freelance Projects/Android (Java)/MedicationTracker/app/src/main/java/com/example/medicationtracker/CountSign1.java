package com.example.medicationtracker;

import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.Check_User.check_user;
import static com.example.medicationtracker.CountSign2.count_sign_2;
import static com.example.medicationtracker.CreateSignatureView.create_signature_view;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.MainActivity.weighted_params;
import static com.example.medicationtracker.Wipe.wipe;

public class CountSign1 {
    public static void count_sign_1(MainActivity Activity, List<Map<String, Object>> entries) {

        wipe(Activity, "First Staff Sign Off On Count", () -> count_sign_1(Activity, entries));
        LinearLayout scroll_child = Activity.scroll_child;
        LinearLayout screen = Activity.screen;

        int client_id = (int) entries.get(0).get("client_id");

        String text = (String) clients_db.getObject("name", new String[]{"id="+client_id});

        for (int i = 0; i < entries.size(); i++) {
            Map<String, Object> entry = entries.get(i);
            if ((int) entry.get("client_id") != client_id){
                client_id = (int) entry.get("client_id");
                text += "\n\n" + clients_db.getObject("name", new String[]{"id="+client_id});;
            }
            text += "\n\n" + entry.get("drug") + "\nCurrent Count: " + entry.get("new_count");
            if (entry.get("method").equals("MISCOUNT")){
                text += "\nMISCOUNT";
            }
        }

        TextView summary = new TextView(Activity);
        summary.setText(text);
        scroll_child.addView(summary);

        SignatureView signature = create_signature_view(Activity);

        Button confirm = new Button(Activity);
        confirm.setText("First Staff Sign Off");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText pass = new EditText(Activity);
                pass.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
                pass.setHint("Enter Password");
                warning.setNegativeButton("Cancel", null);
                warning.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Map<String, Object> staff = check_user(pass.getText().toString());

                        if (staff == null){
                            warning.setView(null);
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("OK", null);
                            warning.setTitle("Invalid password");
                            warning.setMessage("That password does not match the password of any current staff members.");
                            warning.show();
                        }
                        else {
                            byte[] staff_sign = signature.getBytes();
                            count_sign_2(Activity, entries, staff_sign, staff);
                        }
                    }
                });
                warning.setTitle("Password Required");
                warning.setView(pass);
                warning.setMessage("Please enter your password to proceed.");
                warning.show();

            }
        });
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
