package com.example.medicationtracker;

import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;

import static com.example.medicationtracker.Check_Password.check_password;
import static com.example.medicationtracker.MainActivity.admin_mode;
import static com.example.medicationtracker.MainActivity.staff_mode;
import static com.example.medicationtracker.MainActivity.warning;

public class Admin_Password {
    public static void admin_password(MainActivity Activity, Runnable function, String type){
        EditText pass = new EditText(Activity);
        pass.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pass.setHint("Enter Password");
        warning.setNegativeButton("Cancel", null);
        warning.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!check_password(pass.getText().toString(), type)){
                    pass.setText("");
                    warning.setView(null);
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("OK", null);
                    warning.setTitle("Invalid password");
                    if (type.equals("admin")){
                        warning.setMessage("That password does not match the password of any current administrators.");
                    }
                    else{
                        warning.setMessage("That password does not match the password of any current staff members.");
                    }
                    warning.show();
                }
                else {
                    if (type.equals("admin")){
                        admin_mode = true;
                    }
                    else{
                        staff_mode = true;
                    }
                    Activity.runOnUiThread(function);
                }
            }
        });
        warning.setTitle("Password Required");
        warning.setView(pass);

        if (type.equals("admin")){
            warning.setMessage("Please enter your password to access the administrative functions.");
        }
        else{
            warning.setMessage("Please enter your password to access the staff functions.");
        }
        warning.show();
    }
}
