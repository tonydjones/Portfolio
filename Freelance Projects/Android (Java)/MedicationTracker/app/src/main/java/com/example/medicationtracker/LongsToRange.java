package com.example.medicationtracker;

import java.text.SimpleDateFormat;

public class LongsToRange {
    public static String longs_to_range(Long start, Long end) {
        SimpleDateFormat df2 = new SimpleDateFormat("MM/dd/yy");
        return " " + df2.format(start) + " - " + df2.format(end);
    }
}
