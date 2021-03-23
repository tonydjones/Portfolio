package com.example.medicationtracker;

import android.widget.DatePicker;

import java.util.HashMap;
import java.util.Map;

public class DatepickerData {
    public static Map<String, Integer> datepicker_data(DatePicker datepicker){
        Map<String, Integer> result = new HashMap<>();
        result.put("day", datepicker.getDayOfMonth());
        result.put("month", datepicker.getMonth());
        result.put("year", datepicker.getYear());

        return result;
    }
}
