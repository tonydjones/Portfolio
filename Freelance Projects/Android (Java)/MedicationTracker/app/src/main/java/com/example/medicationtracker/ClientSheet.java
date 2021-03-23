package com.example.medicationtracker;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.Long_To_Date.long_to_date;

public class ClientSheet {
    public static Sheet client_sheet(Workbook workbook, String sheet_name, List<Map<String, Object>> client_data){

        Sheet clients = workbook.createSheet(sheet_name);
        List<String> clientNames = Arrays.asList("name", "active", "admit", "discharge", "edits");

        Row headers = clients.createRow(0);
        for (int i = 0; i < clientNames.size(); i++){
            headers.createCell(i).setCellValue(clientNames.get(i));
        }

        for(int  i=0; i < client_data.size(); i++){
            Map<String, Object> client = client_data.get(i);

            if ((int) client.get("active") == 1) {
                client.put("active", "Active");
                client.put("discharge", "N/A");
            } else {
                client.put("active", "Discharged");
                client.put("discharge", long_to_date((long) client.get("discharge")));
            }
            client.put("admit", long_to_date((long) client.get("admit")));

            Row row = clients.createRow(i + 1);

            for (int j = 0; j < clientNames.size(); j++) {
                Object object = client.get(clientNames.get(j));
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
        return clients;
    }
}
