package com.example.medicationtracker;

import java.text.SimpleDateFormat;

public class Long_To_Date {
    public static String long_to_date(Long date) {
        SimpleDateFormat df2 = new SimpleDateFormat("MM/dd/yy");
        return df2.format(date);
    }
}
