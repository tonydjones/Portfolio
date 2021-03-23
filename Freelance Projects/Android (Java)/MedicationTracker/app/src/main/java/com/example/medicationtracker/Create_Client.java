package com.example.medicationtracker;

import java.util.HashMap;
import java.util.Map;

public class Create_Client {
    public static Map<String, Object> create_client(String name, Long time, String type, String password){
        Map<String, Object> client = new HashMap<>();
        client.put("name", name.toUpperCase());
        client.put("active", true);
        client.put("admit", time);
        client.put("edits", 0);
        client.put("class", type);
        client.put("password", password);

        return client;
    }
}
