package com.example.medicationtracker;

import android.widget.TimePicker;

public class TimepickerToTime {
    public static String timepicker_to_time(TimePicker clock){
        String hour;
        if (clock.getHour() < 10) {
            hour = "0" + clock.getHour();
        } else {
            hour = String.valueOf(clock.getHour());
        }

        String minute;
        if (clock.getMinute() < 10) {
            minute = "0" + clock.getMinute();
        } else {
            minute = String.valueOf(clock.getMinute());
        }

        return hour + ":" + minute;
    }
}
