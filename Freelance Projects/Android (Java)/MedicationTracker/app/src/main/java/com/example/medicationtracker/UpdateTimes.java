package com.example.medicationtracker;

import android.util.Pair;
import android.widget.DatePicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.DateToLong.date_to_long;
import static com.example.medicationtracker.DatepickerToDate.datepicker_to_date;
import static com.example.medicationtracker.LongToTime.long_to_time;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.MainActivity.prescriptions_db;
import static com.example.medicationtracker.NextDay.next_day;

public class UpdateTimes {
    public static Pair<List<Long>, List<String>> update_times(int id, boolean single_script, DatePicker calendar){

        String date = datepicker_to_date(calendar);

        long datetime = date_to_long(date);
        long endtime = date_to_long(next_day(date, 1));

        List<Map<String, Object>> time_maps;

        if (single_script){
            time_maps = entries_db.getRows(new String[]{"datetime", "method"},
                    new String[]{"datetime>="+datetime, "datetime<"+endtime, "prescription_id="+id},
                    new String[]{"datetime", "ASC"}, false);
        }
        else{
            time_maps = entries_db.getRows(new String[]{"datetime", "method"},
                    new String[]{"client_id="+id, "datetime>="+datetime, "datetime<"+endtime},
                    new String[]{"datetime", "ASC"}, true);
        }

        List<Long> datetime_list = new ArrayList<>();
        List<String> time_strings = new ArrayList<>();

        if (time_maps.size() == 0){
            String misc;
            if (!single_script){
                misc = (String) clients_db.getObject("name", new String[]{"id="+id});
            }
            else {
                misc = (String) prescriptions_db.getObject("name", new String[]{"id="+id});
            }
            time_strings.add("No entries for " + misc + " on " + date);
        }
        else {
            for (int i = 0; i < time_maps.size(); i++){
                datetime_list.add((long)time_maps.get(i).get("datetime"));
                time_strings.add(long_to_time((long)time_maps.get(i).get("datetime")) + " " + (String)time_maps.get(i).get("method"));
            }
        }
        return new Pair<>(datetime_list, time_strings);
    }
}
