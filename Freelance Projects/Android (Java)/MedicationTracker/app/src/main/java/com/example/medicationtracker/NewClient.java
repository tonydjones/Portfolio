package com.example.medicationtracker;

import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;

import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.AddMultiplePrescriptions.add_multiple_prescriptions;
import static com.example.medicationtracker.Wipe.wipe;

public class NewClient {
    public static void new_client(MainActivity Activity, String packet){
        wipe(Activity, "Add A New Client", () -> new_client(Activity, packet));
        LinearLayout scroll_child = Activity.scroll_child;

        EditText name = new EditText(Activity);
        name.setHint("Full Name");
        scroll_child.addView(name);

        if (packet != null){
            name.setText(packet);
        }

        EditText pass = new EditText(Activity);
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText confirm = new EditText(Activity);
        confirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pass.setHint("Create Password");
        confirm.setHint("Confirm Password");

        CheckBox def = new CheckBox(Activity);
        def.setText("Use default password.");
        def.setChecked(true);
        def.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (def.isChecked()){
                    scroll_child.removeView(pass);
                    scroll_child.removeView(confirm);
                }
                else {
                    scroll_child.addView(pass, 1);
                    scroll_child.addView(confirm, 2);
                }
            }
        });

        Button add = new Button(Activity);
        add.setText("Add Client");
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (name.getText().toString().length() <= 0) {
                    pass.setText("");
                    confirm.setText("");
                    warning.setView(null);
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("OK", null);
                    warning.setTitle("Invalid Name");
                    warning.setMessage("You must enter a name");
                    warning.show();
                }

                else if (!def.isChecked()){
                    if (!pass.getText().toString().equals(confirm.getText().toString())){
                        pass.setText("");
                        confirm.setText("");
                        warning.setView(null);
                        warning.setNegativeButton("", null);
                        warning.setPositiveButton("Try Again", null);
                        warning.setTitle("Invalid passwords");
                        warning.setMessage("The passwords must match");
                        warning.show();
                    }
                    else if (pass.getText().toString().length() <= 0) {
                        pass.setText("");
                        confirm.setText("");
                        warning.setView(null);
                        warning.setNegativeButton("", null);
                        warning.setPositiveButton("OK", null);
                        warning.setTitle("Invalid Password");
                        warning.setMessage("You must enter a password");
                        warning.show();
                    }
                    else {
                        add_multiple_prescriptions(Activity, name.getText().toString(), null, new ArrayList<>(), pass.getText().toString());
                    }
                }

                else {
                    history.remove(0);
                    history.add(0, () -> new_client(Activity, name.getText().toString()));
                    add_multiple_prescriptions(Activity, name.getText().toString(), null, new ArrayList<>(), null);
                }
            }
        });
        //scroll_child.addView(def);
        scroll_child.addView(add);
    }
}
