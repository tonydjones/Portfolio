package com.example.medicationtracker;

import android.widget.Toast;

public class UI_Message {
    public static void ui_message(MainActivity Activity, String message){
        Toast.makeText(Activity, message,
                Toast.LENGTH_LONG).show();
    }
}
