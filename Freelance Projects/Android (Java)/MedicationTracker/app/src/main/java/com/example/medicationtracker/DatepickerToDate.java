package com.example.medicationtracker;

import android.widget.DatePicker;

public class DatepickerToDate {
    public static String datepicker_to_date(DatePicker calendar){
        String day;
        if (calendar.getDayOfMonth() < 10) {
            day = "0" + calendar.getDayOfMonth();
        } else {
            day = String.valueOf(calendar.getDayOfMonth());
        }

        String month;
        if (calendar.getMonth() + 1 < 10) {
            month = "0" + (calendar.getMonth() + 1);
        } else {
            month = String.valueOf(calendar.getMonth() + 1);
        }

        String start_year_string = String.valueOf(calendar.getYear()).substring(String.valueOf(calendar.getYear()).length() - 2);

        return month + "/" + day + "/" + start_year_string;
    }
}
