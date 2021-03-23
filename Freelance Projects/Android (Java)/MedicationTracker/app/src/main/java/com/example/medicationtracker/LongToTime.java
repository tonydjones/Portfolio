package com.example.medicationtracker;

import java.text.SimpleDateFormat;

public class LongToTime {
    public static String long_to_time(Long date) {
        SimpleDateFormat df2 = new SimpleDateFormat("HH:mm");
        return df2.format(date);
    }
}
