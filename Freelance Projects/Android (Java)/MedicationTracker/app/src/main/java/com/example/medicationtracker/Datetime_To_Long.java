package com.example.medicationtracker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Datetime_To_Long {
    public static Long datetime_to_long(String datetime) {
        SimpleDateFormat f = new SimpleDateFormat("MM/dd/yy HH:mm");
        Date d = null;
        try {
            d = f.parse(datetime);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return d.getTime();
    }
}
