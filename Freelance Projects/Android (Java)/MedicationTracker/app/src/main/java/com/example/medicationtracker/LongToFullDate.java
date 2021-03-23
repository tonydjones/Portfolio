package com.example.medicationtracker;

import java.text.SimpleDateFormat;

public class LongToFullDate {
    public static int[] long_to_full_date(Long date) {
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy/MM/dd/HH/mm");
        String[] split = df2.format(date).split("/");
        int[] mo_day_yr = new int[5];
        for (int i = 0; i < split.length; i++){
            mo_day_yr[i] = Integer.parseInt(split[i]);
        }

        return mo_day_yr;
    }
}
