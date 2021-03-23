package com.example.medicationtracker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateToLong {
    public static Long date_to_long(String date) {
        SimpleDateFormat f = new SimpleDateFormat("MM/dd/yy");
        Date d = null;
        try {
            d = f.parse(date);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return d.getTime();
    }
}
