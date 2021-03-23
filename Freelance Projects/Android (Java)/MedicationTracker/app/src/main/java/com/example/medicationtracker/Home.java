package com.example.medicationtracker;

import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import static com.example.medicationtracker.Admin.admin;
import static com.example.medicationtracker.Auto_Reset.auto_reset;
import static com.example.medicationtracker.Base.base;
import static com.example.medicationtracker.Help.help;
import static com.example.medicationtracker.MainActivity.record_password;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.MainActivity.admin_mode;
import static com.example.medicationtracker.MainActivity.staff_mode;
import static com.example.medicationtracker.Records.records;
import static com.example.medicationtracker.Show.show;
import static com.example.medicationtracker.Test.test;
import static com.example.medicationtracker.UI_Message.ui_message;
import static com.example.medicationtracker.Wipe.wipe;

public class Home {
    public static void home(MainActivity Activity) {

        wipe(Activity, "Medication Tracker", () -> home(Activity));
        LinearLayout scroll_child = Activity.scroll_child;

        admin_mode = false;
        staff_mode = false;

        Button reset = new Button(Activity);

        reset.setText("Generate Sample Clients");
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                warning.setNegativeButton("Cancel", null);
                warning.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        auto_reset();
                        ui_message(Activity, "Sample clients successfully generated.");
                    }
                });
                warning.setTitle("Confirm Regenerating Sample Clients");
                warning.setMessage("Did you misclick you big dummy? Are you sure you want to reset the sample clients?");
                warning.setView(null);
                warning.show();
            }
        });

        /*Button meds = new Button(Activity);
        Button admin = new Button(Activity);
        meds.setText("Records");
        admin.setText("Admin");

        meds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                records(Activity);
            }
        });

        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                admin(Activity);
            }
        });*/

        Button base = new Button(Activity);
        base.setText("Log In");
        base.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                base(Activity);
            }
        });
        scroll_child.addView(base);

        Button test = new Button(Activity);
        test.setText("Test New Feature");
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test(Activity);
            }
        });

        Button help = new Button(Activity);
        help.setText("Tutorials And Help");
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                help(Activity);
            }
        });

        /*scroll_child.addView(meds);
        scroll_child.addView(admin);*/

        scroll_child.addView(reset);
        scroll_child.addView(help);
        //scroll_child.addView(test);

        Button show = new Button(Activity);
        show.setText("Show");
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show(Activity);
            }
        });
        //scroll_child.addView(show);
    }
}
