package com.example.medicationtracker;

import java.text.SimpleDateFormat;

public class LongToDatetime {
    public static String long_to_datetime(Long date) {
        SimpleDateFormat df2 = new SimpleDateFormat("MM/dd/yy HH:mm");
        return df2.format(date);
    }
}
