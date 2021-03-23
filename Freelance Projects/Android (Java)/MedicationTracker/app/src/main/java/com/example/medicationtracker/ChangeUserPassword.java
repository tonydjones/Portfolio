package com.example.medicationtracker;

import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Map;

import static com.example.medicationtracker.Check_Password.check_password;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.StaffSummary.staff_summary;
import static com.example.medicationtracker.UI_Message.ui_message;
import static com.example.medicationtracker.Wipe.wipe;

public class ChangeUserPassword {

    public static void change_user_password(MainActivity Activity, Map<String, Object> user, boolean forgot){
        wipe(Activity, "Change Your Password", () -> change_user_password(Activity, user, forgot));
        LinearLayout scroll_child = Activity.scroll_child;

        EditText current = new EditText(Activity);
        current.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText pass = new EditText(Activity);
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText confirm = new EditText(Activity);
        confirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        if (forgot){
            current.setHint("Admin Password");
            TextView prompt = new TextView(Activity);
            prompt.setText("You need the password of an administrator in order to verify resetting your password.");
            scroll_child.addView(prompt);
        }
        else{
            current.setHint("Enter Current Password");
        }

        pass.setHint("Create New Password");
        confirm.setHint("Confirm New Password");

        Button submit = new Button(Activity);
        submit.setText("Save New Password");
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pass.getText().toString().equals(confirm.getText().toString())) {
                    current.setText("");
                    pass.setText("");
                    confirm.setText("");
                    warning.setView(null);
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("Try Again", null);
                    warning.setTitle("Invalid Passwords");
                    warning.setMessage("The new passwords must match");
                    warning.show();
                } else if (pass.getText().toString().length() <= 0) {
                    current.setText("");
                    pass.setText("");
                    confirm.setText("");
                    warning.setView(null);
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("OK", null);
                    warning.setTitle("Invalid Password");
                    warning.setMessage("You must enter a new password");
                    warning.show();
                } else if (!forgot && !current.getText().toString().equals(user.get("password"))) {
                    current.setText("");
                    pass.setText("");
                    confirm.setText("");
                    warning.setView(null);
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("Try Again", null);
                    warning.setTitle("Incorrect Password");
                    warning.setMessage("The current password you entered is incorrect");
                    warning.show();
                } else if (forgot && !check_password(current.getText().toString(), "admin")) {
                    current.setText("");
                    pass.setText("");
                    confirm.setText("");
                    warning.setView(null);
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("Try Again", null);
                    warning.setTitle("Invalid Admin Password");
                    warning.setMessage("The administrator password you entered does not match any current administrators.");
                    warning.show();
                } else {
                    user.put("password", pass.getText().toString());
                    clients_db.update(user, new String[]{"id="+user.get("id")});
                    history.remove(0);
                    history.remove(0);
                    staff_summary(Activity, user);
                    ui_message(Activity, "Password successfully changed.");
                }
            }
        });

        scroll_child.addView(current);
        scroll_child.addView(pass);
        scroll_child.addView(confirm);
        scroll_child.addView(submit);
    }
}
