package com.example.medicationtracker;

import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.gson;
import static com.example.medicationtracker.MainActivity.master_key;

public class Check_Password {
    public static boolean check_password(String password, String type){
        if (password.equals(master_key)){
            return true;
        }

        //String user = (String) clients_db.getObject("class",
                //new String[]{"password='"+password+"'"});

        Map<String, Object> user_map = clients_db.manual_query("SELECT class FROM clients WHERE password=? AND active=1", new String[]{password});
        if (user_map == null){
            return false;
        }

        String user = (String) user_map.get("class");

        if (user == null){
            return false;
        }
        else if (user.equals(type)){
            return true;
        }
        else if (user.equals("admin") && type.equals("staff")){
            return true;
        }
        return false;
    }
}
