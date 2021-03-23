package com.example.medicationtracker;

import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Map;

import static com.example.medicationtracker.Create_Client.create_client;
import static com.example.medicationtracker.Home.home;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.editor;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.Wipe.wipe;

public class Setup {
    public static void setup(MainActivity Activity){

        wipe(Activity, "Welcome", null);
        LinearLayout scroll_child = Activity.scroll_child;

        TextView directions = new TextView(Activity);
        directions.setText("Welcome to the Medication Tracker. Let's begin by setting up the first Admin Account. This action should be performed by " +
                "the highest-ranked staff member who will be actively involved in record-keeping.");
        scroll_child.addView(directions);

        EditText name = new EditText(Activity);
        name.setHint("Full Name");
        scroll_child.addView(name);

        EditText pass = new EditText(Activity);
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText confirm = new EditText(Activity);
        confirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pass.setHint("Create Password");
        confirm.setHint("Confirm Password");
        scroll_child.addView(pass);
        scroll_child.addView(confirm);

        Button submit = new Button(Activity);
        submit.setText("Continue");
        scroll_child.addView(submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pass.getText().toString().equals(confirm.getText().toString())) {
                    pass.setText("");
                    confirm.setText("");
                    warning.setView(null);
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("Try Again", null);
                    warning.setTitle("Invalid passwords");
                    warning.setMessage("The passwords must match");
                    warning.show();
                } else if (pass.getText().toString().length() <= 0) {
                    pass.setText("");
                    confirm.setText("");
                    warning.setView(null);
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("OK", null);
                    warning.setTitle("Invalid Password");
                    warning.setMessage("You must enter a password");
                    warning.show();
                }
                else if (name.getText().toString().length() <= 0) {
                    pass.setText("");
                    confirm.setText("");
                    warning.setView(null);
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("OK", null);
                    warning.setTitle("Invalid Name");
                    warning.setMessage("You must enter a name");
                    warning.show();
                }
                else {
                    Map<String, Object> admin = create_client(name.getText().toString(), System.currentTimeMillis(), "admin", pass.getText().toString());
                    clients_db.addRow(admin);
                    editor.putBoolean("setup_complete", true);
                    editor.apply();
                    home(Activity);
                }
            }
        });
    }
}
