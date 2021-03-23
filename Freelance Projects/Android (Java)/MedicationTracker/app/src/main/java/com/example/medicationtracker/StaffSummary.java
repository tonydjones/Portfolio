package com.example.medicationtracker;

import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import static com.example.medicationtracker.ChangeUserPassword.change_user_password;
import static com.example.medicationtracker.CheckAdmin.check_admin;
import static com.example.medicationtracker.Check_Password.check_password;
import static com.example.medicationtracker.History.history;
import static com.example.medicationtracker.LongToDatetime.long_to_datetime;
import static com.example.medicationtracker.Long_To_Date.long_to_date;
import static com.example.medicationtracker.MainActivity.admin_mode;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.Staff.staff;
import static com.example.medicationtracker.UI_Message.ui_message;
import static com.example.medicationtracker.Wipe.wipe;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.MainActivity.history;

public class StaffSummary {

    public static void staff_summary(MainActivity Activity, Map<String, Object> client) {

        wipe(Activity, (String) client.get("name"), () -> staff_summary(Activity, client));
        LinearLayout scroll_child = Activity.scroll_child;


        String client_string = client.get("name") + " started on " + long_to_date((long) client.get("admit"));
        if ((int)client.get("active") == 0){
            client_string += "\n" + client.get("name") + " was deactivated on " + long_to_date((long) client.get("discharge"));
        }


        TextView admit_date = new TextView(Activity);
        admit_date.setText(client_string);
        scroll_child.addView(admit_date);

        Button see_entries = new Button(Activity);
        see_entries.setText("View History");
        see_entries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                history(Activity, (int)client.get("id"));
            }
        });
        scroll_child.addView(see_entries);

        Button change_password = null;

        Button forgot_password = null;

        if ((int)client.get("active") == 1){

            change_password = new Button(Activity);

            forgot_password = new Button(Activity);

            change_password.setText("Change Password");
            change_password.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    change_user_password(Activity, client, false);
                }
            });
            scroll_child.addView(change_password);

            forgot_password.setText("Forgot Password");
            forgot_password.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    change_user_password(Activity, client, true);
                }
            });
            scroll_child.addView(forgot_password);
        }

        if (admin_mode){

            if ((int)client.get("active") == 1){
                Button remove_admin = new Button(Activity);
                remove_admin.setText("Revoke Admin Privileges");

                Button admin = new Button(Activity);
                admin.setText("Grant Admin Privileges");
                admin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText pass = new EditText(Activity);
                        pass.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        warning.setNegativeButton("Cancel", null);
                        warning.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!check_password(pass.getText().toString(), "admin")){
                                    warning.setView(null);
                                    warning.setNegativeButton("", null);
                                    warning.setPositiveButton("OK", null);
                                    warning.setTitle("Invalid password");
                                    warning.setMessage("That password does not match the password of any current administrators. Only administrators can grant admin privileges.");
                                    warning.show();
                                }
                                else {
                                    scroll_child.addView(remove_admin, scroll_child.indexOfChild(admin));
                                    scroll_child.removeView(admin);


                                    client.put("class", "admin");
                                    clients_db.update(client, new String[]{"id="+client.get("id")});
                                    ui_message(Activity, client.get("name") + " was granted administrative privileges.");
                                }
                            }
                        });
                        warning.setTitle("Confirm granting admin privileges");
                        warning.setView(pass);
                        warning.setMessage("Please enter your password to confirm granting this staff member administrative privileges.");
                        warning.show();
                    }
                });

                remove_admin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!check_admin((int)client.get("id"))){
                            warning.setView(null);
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("OK", null);
                            warning.setTitle("Cannot revoke the final admin");
                            warning.setMessage("This application requires at least one administrator in order to function. Please give at least one other staff member " +
                                    "administrative privileges before revoking this staff member's privileges.");
                            warning.show();
                            return;
                        }
                        EditText pass = new EditText(Activity);
                        pass.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        warning.setNegativeButton("Cancel", null);
                        warning.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (!check_password(pass.getText().toString(), "admin")){
                                    warning.setView(null);
                                    warning.setNegativeButton("", null);
                                    warning.setPositiveButton("OK", null);
                                    warning.setTitle("Invalid password");
                                    warning.setMessage("That password does not match the password of any current administrators. Only administrators can revoke admin privileges.");
                                    warning.show();
                                }
                                else {
                                    scroll_child.addView(admin, scroll_child.indexOfChild(remove_admin));
                                    scroll_child.removeView(remove_admin);

                                    client.put("class", "staff");
                                    clients_db.update(client, new String[]{"id="+client.get("id")});
                                    ui_message(Activity, client.get("name") + " no longer has administrative privileges.");
                                }
                            }
                        });
                        warning.setTitle("Confirm revoking admin privileges");
                        warning.setView(pass);
                        warning.setMessage("Please enter your password to confirm revoking this staff member's administrative privileges.");
                        warning.show();
                    }
                });

                if (client.get("class").equals("staff")){
                    scroll_child.addView(admin);
                }
                else{
                    scroll_child.addView(remove_admin);
                }

                Button delete = new Button(Activity);
                delete.setText("Deactivate Staff Member");
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!check_admin((int)client.get("id"))){
                            warning.setView(null);
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("OK", null);
                            warning.setTitle("Cannot deactivate the final admin");
                            warning.setMessage("This application requires at least one active administrator in order to function. Please give at least one other staff member " +
                                    "administrative privileges before deactivating this staff member.");
                            warning.show();
                            return;
                        }
                        EditText pass = new EditText(Activity);
                        pass.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        pass.setHint("Enter Admin Password");
                        warning.setNegativeButton("Cancel", null);
                        warning.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (!check_password(pass.getText().toString(), "admin")){
                                    warning.setView(null);
                                    warning.setNegativeButton("", null);
                                    warning.setPositiveButton("OK", null);
                                    warning.setTitle("Invalid password");
                                    warning.setMessage("That password does not match the password of any current administrators. Only administrators can deactivate staff members.");
                                    warning.show();
                                }
                                else {
                                    client.put("active", 0);
                                    client.put("discharge", System.currentTimeMillis());
                                    clients_db.update(client, new String[]{"id="+client.get("id")});
                                    scroll_child.removeViewAt(2);
                                    scroll_child.removeViewAt(2);
                                    scroll_child.removeViewAt(2);

                                    delete.setText("Delete Staff Member");
                                    delete.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {/*
                                            if (!check_admin((int)client.get("id"))){
                                                warning.setView(null);
                                                warning.setNegativeButton("", null);
                                                warning.setPositiveButton("OK", null);
                                                warning.setTitle("Cannot delete the final admin");
                                                warning.setMessage("This application requires at least one administrator in order to function. Please give at least one other staff member " +
                                                        "administrative privileges before deleting this staff member.");
                                                warning.show();
                                                return;
                                            }*/
                                            EditText pass = new EditText(Activity);
                                            pass.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                            pass.setHint("Enter Admin Password");
                                            warning.setNegativeButton("Cancel", null);
                                            warning.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    if (!check_password(pass.getText().toString(), "admin")){
                                                        warning.setView(null);
                                                        warning.setNegativeButton("", null);
                                                        warning.setPositiveButton("OK", null);
                                                        warning.setTitle("Invalid password");
                                                        warning.setMessage("That password does not match the password of any current administrators. Only administrators can delete staff members.");
                                                        warning.show();
                                                    }
                                                    else {
                                                        clients_db.delete_single_constraint("id="+client.get("id"));
                                                        history.remove(0);
                                                        history.remove(0);
                                                        ui_message(Activity, "Records for " + client.get("name") + " have been successfully deleted.");
                                                        staff(Activity);
                                                    }
                                                }
                                            });
                                            warning.setTitle("Confirm Staff Deletion");
                                            warning.setView(pass);
                                            warning.setMessage("Please enter your password to confirm deleting this staff member from the database. The staff member's name will remain viewable in client records where relevant, but " +
                                                    "the entries will no longer be gathered for viewing in staff records.");
                                            warning.show();
                                        }
                                    });

                                    ui_message(Activity, client.get("name") + " is no longer an active staff member.");
                                }
                            }
                        });
                        warning.setTitle("Confirm Staff Deactivation");
                        warning.setView(pass);
                        warning.setMessage("Please enter your password to confirm deactivating this staff member. Their history will still be viewable, but they will not be able to access or make new records.");
                        warning.show();
                    }
                });
                scroll_child.addView(delete);
            }
            else{
                Button delete = new Button(Activity);
                delete.setText("Delete Staff Member");
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*if (!check_admin((int)client.get("id"))){
                            warning.setView(null);
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("OK", null);
                            warning.setTitle("Cannot delete the final admin");
                            warning.setMessage("This application requires at least one administrator in order to function. Please give at least one other staff member " +
                                    "administrative privileges before deleting this staff member.");
                            warning.show();
                            return;
                        }*/
                        EditText pass = new EditText(Activity);
                        pass.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        pass.setHint("Enter Admin Password");
                        warning.setNegativeButton("Cancel", null);
                        warning.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (!check_password(pass.getText().toString(), "admin")){
                                    warning.setView(null);
                                    warning.setNegativeButton("", null);
                                    warning.setPositiveButton("OK", null);
                                    warning.setTitle("Invalid password");
                                    warning.setMessage("That password does not match the password of any current administrators. Only administrators can delete staff members.");
                                    warning.show();
                                }
                                else {
                                    clients_db.delete_single_constraint("id="+client.get("id"));
                                    history.remove(0);
                                    history.remove(0);
                                    ui_message(Activity, "Records for " + client.get("name") + " have been successfully deleted.");
                                    staff(Activity);
                                }
                            }
                        });
                        warning.setTitle("Confirm Staff Deletion");
                        warning.setView(pass);
                        warning.setMessage("Please enter your password to confirm deleting this staff member from the database. The staff member's name will remain viewable in client records where relevant, but " +
                                "the entries will no longer be gathered for viewing in staff records.");
                        warning.show();
                    }
                });
                scroll_child.addView(delete);
            }
        }
    }
}
