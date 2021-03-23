package com.example.medicationtracker;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import static com.example.medicationtracker.Admin_Password.admin_password;
import static com.example.medicationtracker.Clients.clients;
import static com.example.medicationtracker.MainActivity.admin_mode;
import static com.example.medicationtracker.MainActivity.record_password;
import static com.example.medicationtracker.MainActivity.staff_mode;
import static com.example.medicationtracker.Staff.staff;
import static com.example.medicationtracker.Wipe.wipe;

public class Records {
    public static void records(MainActivity Activity){

        if (record_password && !staff_mode){
            admin_password(Activity, () -> records(Activity), "staff");
            return;
        }

        wipe(Activity,"Records", () -> records(Activity));
        LinearLayout scroll_child = Activity.scroll_child;

        Button view_client = new Button(Activity);
        view_client.setText("Clients");
        view_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clients(Activity);
            }
        });
        scroll_child.addView(view_client);

        Button staff = new Button(Activity);
        staff.setText("Staff");
        staff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                staff(Activity);
            }
        });
        scroll_child.addView(staff);
    }
}
