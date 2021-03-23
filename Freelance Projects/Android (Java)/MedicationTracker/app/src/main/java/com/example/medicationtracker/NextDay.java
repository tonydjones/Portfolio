package com.example.medicationtracker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NextDay {
    public static String next_day(String date, int days) {
        SimpleDateFormat df2 = new SimpleDateFormat("MM/dd/yy");

        Calendar calendar = Calendar.getInstance();
        try{
            calendar.setTime(df2.parse(date));
            calendar.add(Calendar.DATE, days);
        }
        catch(ParseException e){
            e.printStackTrace();
        }

        return df2.format(calendar.getTime());
    }
}
