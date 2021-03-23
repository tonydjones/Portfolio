package com.example.medicationtracker;

import java.util.ArrayList;
import java.util.Map;

import static com.example.medicationtracker.MainActivity.clients_db;

public class CheckAdmin {
    public static boolean check_admin(int id){
        Object admins = clients_db.getObject("id",
                new String[]{"id!="+id, "class='admin'"});
        if (admins != null){
            return true;
        }
        else {
            return false;
        }
    }
}
