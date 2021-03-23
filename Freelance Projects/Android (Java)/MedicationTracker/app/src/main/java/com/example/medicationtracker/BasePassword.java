package com.example.medicationtracker;

import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;

import static com.example.medicationtracker.Check_Password.check_password;
import static com.example.medicationtracker.MainActivity.admin_mode;
import static com.example.medicationtracker.MainActivity.staff_mode;
import static com.example.medicationtracker.MainActivity.warning;

public class BasePassword {
    public static void base_password(MainActivity Activity, Runnable function){
        EditText pass = new EditText(Activity);
        pass.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pass.setHint("Enter Password");
        warning.setNegativeButton("Cancel", null);
        warning.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (!check_password(pass.getText().toString(), "staff")){
                    warning.setView(null);
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("OK", null);
                    warning.setTitle("Invalid password");
                    warning.setMessage("That password does not match the password of any current staff members.");
                    warning.show();
                }
                else if (check_password(pass.getText().toString(), "admin")){
                    admin_mode = true;
                    Activity.runOnUiThread(function);
                }
                else {
                    staff_mode = true;
                    Activity.runOnUiThread(function);
                }
            }
        });
        warning.setTitle("Password Required");
        warning.setView(pass);
        warning.setMessage("Please enter your password.");
        warning.show();
    }
}
