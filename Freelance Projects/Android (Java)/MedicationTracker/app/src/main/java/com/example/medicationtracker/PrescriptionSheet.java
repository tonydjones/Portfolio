package com.example.medicationtracker;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.DateToLong.date_to_long;
import static com.example.medicationtracker.Long_To_Date.long_to_date;
import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.NextDay.next_day;

public class PrescriptionSheet {
    public static Sheet prescription_sheet(Workbook workbook, String sheet_name, List<Map<String, Object>> prescription_data, String start, String end){

        Sheet prescriptions = workbook.createSheet(sheet_name);
        List<String> prescription_headers = new ArrayList<>(Arrays.asList("drug", "dose", "instructions", "as_needed",
                "controlled", "count", "dose_max", "daily_max", "indication", "prescriber", "pharmacy", "active",
                "start", "end", "edits"));

        String start_string = null;
        String end_string = null;
        Long start_long = null;
        Long end_long = null;
        DecimalFormat df = null;
        if (start != null){
            prescription_headers.addAll(Arrays.asList("adherence", "dose_override", "daily_override"));
            start_string = start;
            end_string = end;

            start_long = date_to_long(start_string);
            end_long = date_to_long(next_day(end_string ,1));

            df = new DecimalFormat("###.##");

            if (end_string.equals(long_to_date(System.currentTimeMillis()))){
                end_long = date_to_long(end_string);
            }
        }

        Row headers = prescriptions.createRow(0);
        for (int i = 0; i < prescription_headers.size(); i++){
            headers.createCell(i).setCellValue(prescription_headers.get(i));
        }

        for(int  i=0; i < prescription_data.size(); i++) {

            Map<String, Object> prescription = prescription_data.get(i);

            if (start != null){

                if (prescription.get("daily_max") == null && prescription.get("dose_max") == null) {
                    prescription.put("adherence", "No Adherence Guidelines");
                    prescription.put("daily_override", "No Daily Guidelines");
                    prescription.put("dose_override", "No Dose Guidelines");
                }
                else{

                    long temp_start = start_long;
                    long temp_end = end_long;

                    String first = start_string;
                    String last = end_string;

                    if ((int)prescription.get("active") == 1) {
                        if (last.equals(long_to_date(System.currentTimeMillis()))){
                            last = next_day(end_string, -1);
                        }
                    }
                    else {
                        if ((long) prescription.get("end") < end_long){
                            temp_end = date_to_long(long_to_date((long) prescription.get("end")));
                            last = next_day(long_to_date((long) prescription.get("end")), -1);
                        }
                    }

                    if ((long) prescription.get("start") > start_long){
                        first = next_day(long_to_date((long) prescription.get("start")), 1);
                        temp_start = date_to_long(first);
                    }

                    if (temp_start >= temp_end){
                        prescription.put("adherence", "Not Enough Data");
                        if (prescription.get("daily_max") == null) {
                            prescription.put("daily_override", "No Daily Guidelines");
                        }
                        else {
                            prescription.put("daily_override", 0);
                        }
                        if (prescription.get("dose_max") == null){
                            prescription.put("dose_override", "No Dose Guidelines");
                        }
                        else {
                            prescription.put("dose_override", 0);
                        }
                    }
                    else {
                        List<Map<String, Object>> entries = entries_db.getRows(new String[]{"id", "change", "dose_override", "daily_override", "datetime"},
                                new String[]{"prescription_id="+prescription.get("id"), "method='TOOK MEDS'", "datetime>="+temp_start, "datetime<"+temp_end},
                                new String[]{"datetime", "ASC"}, false);

                        float numerator = 0;
                        int daily_override = 0;
                        int dose_override = 0;
                        int days = 1;

                        while (!first.equals(last)){
                            days++;
                            first = next_day(first, 1);
                        }

                        String last_override_day = "";
                        for (int j = 0; j < entries.size(); j++){
                            Map<String, Object> entry = entries.get(j);

                            numerator += (float)entry.get("change");

                            if ((int)entry.get("dose_override") == 1){
                                dose_override++;
                            }

                            if ((int)entry.get("daily_override") == 1 && !long_to_date((long)entry.get("datetime")).equals(last_override_day)){
                                daily_override++;
                                last_override_day = long_to_date((long)entry.get("datetime"));
                            }
                        }

                        if (prescription.get("daily_max") == null){
                            prescription.put("adherence", "No Adherence Guidelines");
                            prescription.put("daily_override", "No Daily Guidelines");
                        }
                        else if ((int)prescription.get("as_needed") == 0) {
                            prescription.put("adherence", df.format(100 * numerator / (days * (float) prescription.get("daily_max"))) + "%");
                            prescription.put("daily_override", daily_override);
                        }
                        else {
                            prescription.put("adherence", "As Needed");
                            prescription.put("daily_override", daily_override);
                        }

                        if (prescription.get("dose_max") == null){
                            prescription.put("dose_override", "No Dose Guidelines");
                        }
                        else {
                            prescription.put("dose_override", dose_override);
                        }
                    }
                }
            }

            if ((int) prescription.get("as_needed") == 1) {
                prescription.put("as_needed", "Yes");
            } else {
                prescription.put("as_needed", "No");
            }
            if ((int) prescription.get("controlled") == 1) {
                prescription.put("controlled", "Yes");
            } else {
                prescription.put("controlled", "No");
            }

            if ((int) prescription.get("active") == 1) {
                prescription.put("active", "Active");
                prescription.put("end", "N/A");
            } else {
                prescription.put("active", "Discontinued");
                prescription.put("end", long_to_date((long) prescription.get("end")));
            }
            prescription.put("start", long_to_date((long) prescription.get("start")));

            Row row = prescriptions.createRow(i + 1);
            for (int j = 0; j < prescription_headers.size(); j++) {
                Object object = prescription.get(prescription_headers.get(j));
                if (object == null){
                    row.createCell(j).setCellValue("N/A");
                }
                else {
                    Class<?> type = object.getClass();
                    if (type == String.class) {
                        row.createCell(j).setCellValue((String) object);
                    } else if (type == Float.class) {
                        row.createCell(j).setCellValue((Float) object);
                    } else if (type == Integer.class) {
                        row.createCell(j).setCellValue((Integer) object);
                    }
                }

            }
        }
        return prescriptions;
    }
}
