package com.example.medicationtracker;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import static com.example.medicationtracker.Admin_Password.admin_password;
import static com.example.medicationtracker.Clients.clients;
import static com.example.medicationtracker.MainActivity.record_password;
import static com.example.medicationtracker.MainActivity.staff_mode;
import static com.example.medicationtracker.Staff.staff;
import static com.example.medicationtracker.Wipe.wipe;

public class Help {
    public static void help(MainActivity Activity){

        wipe(Activity,"Help and Tutorials", () -> help(Activity));

        LinearLayout scroll_child = Activity.scroll_child;

        TextView clients = new TextView(Activity);
        clients.setText("Please review the below tutorials as necessary. If you cannot find your answer, or to report an app crash or glitch, please contact tdjones@alum.mit.edu");
        scroll_child.addView(clients);

        Button new_client = new Button(Activity);
        new_client.setText("How To Add A New Client");
        scroll_child.addView(new_client);
        new_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
