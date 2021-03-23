package com.example.medicationtracker;

import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import static com.example.medicationtracker.Check_Password.check_password;
import static com.example.medicationtracker.Create_Client.create_client;
import static com.example.medicationtracker.LongToDatetime.long_to_datetime;
import static com.example.medicationtracker.MainActivity.admin_mode;
import static com.example.medicationtracker.MainActivity.staff_mode;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.Staff.staff;
import static com.example.medicationtracker.UI_Message.ui_message;
import static com.example.medicationtracker.Wipe.wipe;

public class NewStaff {
    public static void new_staff(MainActivity Activity){
        wipe(Activity, "Add A New Staff Member", () -> new_staff(Activity));
        LinearLayout scroll_child = Activity.scroll_child;

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

        CheckBox admin = new CheckBox(Activity);
        admin.setText("Grant this user admin privileges.");
        scroll_child.addView(admin);

        Button add = new Button(Activity);
        add.setText("Add Staff Member");
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

                else if (!pass.getText().toString().equals(confirm.getText().toString())){
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
                else if (check_password(pass.getText().toString(), "staff")) {
                    pass.setText("");
                    confirm.setText("");
                    warning.setView(null);
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("OK", null);
                    warning.setTitle("Invalid Password");
                    warning.setMessage("Your password must be unique");
                    warning.show();
                }
                else {
                    EditText pw = new EditText(Activity);
                    pw.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    pw.setHint("Enter Admin Password");
                    warning.setNegativeButton("Cancel", null);
                    warning.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!check_password(pw.getText().toString(), "admin")){
                                pw.setText("");
                                warning.setView(null);
                                warning.setNegativeButton("", null);
                                warning.setPositiveButton("OK", null);
                                warning.setTitle("Invalid password");
                                warning.setMessage("That password does not match the password of any current administrators.");
                                warning.show();
                            }
                            else {
                                if (admin.isChecked()){
                                    clients_db.addRow(create_client(name.getText().toString(), System.currentTimeMillis(), "admin", pass.getText().toString()));
                                }
                                else{
                                    clients_db.addRow(create_client(name.getText().toString(), System.currentTimeMillis(), "staff", pass.getText().toString()));
                                }
                                history.remove(0);
                                history.remove(0);
                                staff(Activity);
                                ui_message(Activity, name.getText().toString() + " was successfully added as a staff member.");
                            }
                        }
                    });
                    warning.setTitle("Password Required");
                    warning.setView(pw);
                    warning.setMessage("Please enter your password to add this new staff member. Please note you must be an administrator to do this.");
                    warning.show();
                }
            }
        });
        scroll_child.addView(add);
    }
}
