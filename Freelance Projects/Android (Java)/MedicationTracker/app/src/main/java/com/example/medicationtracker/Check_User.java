package com.example.medicationtracker;

import java.util.HashMap;
import java.util.Map;

import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.master_key;

public class Check_User {
    public static Map<String, Object> check_user(String password){
        if (password.equals(master_key)){
            Map<String, Object> admin = new HashMap<>();
            admin.put("name", "MASTER KEY");
            admin.put("id", -1);
            admin.put("class", "admin");
            return admin;
        }

        //return clients_db.getSingleRow(null,
          //      new String[]{"password='" + password + "'", "class IN ('admin','staff')"});

        return clients_db.manual_query("SELECT * FROM clients WHERE password=? AND class IN ('admin','staff') AND active=1", new String[]{password});

    }
}
