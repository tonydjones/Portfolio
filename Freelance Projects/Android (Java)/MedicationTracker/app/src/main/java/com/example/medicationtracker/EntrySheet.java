package com.example.medicationtracker;

import com.google.gson.reflect.TypeToken;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.InsertSignature.insert_signature;
import static com.example.medicationtracker.LongToDatetime.long_to_datetime;
import static com.example.medicationtracker.MainActivity.gson;

public class EntrySheet {
    public static Sheet entry_sheet(Workbook workbook, String sheet_name, List<Map<String, Object>> entry_data){

        Sheet entries = workbook.createSheet(sheet_name.replace("/", "-"));
        List<String> entry_headers = Arrays.asList("drug", "method", "old_count", "change", "new_count", "datetime", "staff_present", "notes",
                "dose_override", "daily_override", "client_signature", "staff_signature_1", "staff_signature_2", "edits");

        List<String> exclude = Arrays.asList("client_signature", "staff_signature_1", "staff_signature_2");

        List<String> other_methods = Arrays.asList("INTAKE", "REFILL", "UPDATED PRESCRIPTION STARTED", "PRESCRIPTION STARTED");

        CreationHelper helper = workbook.getCreationHelper();

        Drawing drawing = entries.createDrawingPatriarch();

        Row headers = entries.createRow(0);
        for (int i = 0; i < entry_headers.size(); i++){
            headers.createCell(i).setCellValue(entry_headers.get(i));
        }

        for(int  i=0; i < entry_data.size(); i++){
            Map<String, Object> entry = entry_data.get(i);

            entry.put("datetime", long_to_datetime((long)entry.get("datetime")));
            List<String> notes = gson.fromJson((String)entry.get("notes"), new TypeToken<List<String>>(){}.getType());
            String note_string = "";
            for (int j = 0; j < notes.size(); j++){
                if (j == 0){
                    note_string += notes.get(j);
                }
                else{
                    note_string += "\n" + notes.get(j);
                }
            }

            entry.put("notes", note_string);

            if ((int) entry.get("dose_override") == 1) {
                entry.put("dose_override", "OVERRIDE");
            } else {
                entry.put("dose_override", "No");
            }
            if ((int) entry.get("daily_override") == 1) {
                entry.put("daily_override", "OVERRIDE");
            } else {
                entry.put("daily_override", "No");
            }

            if (entry.get("change") == null){
                entry.put("change", "N/A");
            }
            if (entry.get("old_count") == null){
                entry.put("old_count", "N/A");
            }
            if (entry.get("new_count") == null){
                entry.put("new_count", "N/A");
            }
            else if (entry.get("method").equals("TOOK MEDS")) {
                entry.put("change", "-" + entry.get("change"));
            } else if (other_methods.contains(entry.get("method"))) {
                entry.put("change", "+" + entry.get("change"));
            }

            Row row = entries.createRow(i + 1);

            for (int j = 0; j < entry_headers.size(); j++) {
                if (!exclude.contains(entry_headers.get(j))){
                    Object object = entry.get(entry_headers.get(j));
                    Class<?> type = object.getClass();
                    if (type == String.class) {
                        row.createCell(j).setCellValue((String) object);
                    } else if (type == Float.class) {
                        row.createCell(j).setCellValue((Float) object);
                    } else if (type == Integer.class) {
                        row.createCell(j).setCellValue((Integer) object);
                    }
                }
                else{
                    if (entry.get(entry_headers.get(j)) == null){
                        if (entry.get("method").equals("TOOK MEDS") && entry_headers.get(j).equals("client_signature")){
                            row.createCell(j).setCellValue("Client was not present when this entry was recorded.");
                        }
                        else {
                            row.createCell(j).setCellValue("N/A");
                        }

                    }
                    else {
                        row.createCell(j);
                        insert_signature(workbook, helper, drawing,
                                gson.fromJson((String) entry.get(entry_headers.get(j)), new TypeToken<byte[]>(){}.getType()), i+1, j);
                    }
                }
            }
        }
        return entries;
    }
}
