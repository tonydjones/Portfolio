package com.example.medicationtracker;

import android.util.Pair;

import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.example.medicationtracker.MainActivity.folder;

public class SaveXLS {
    public static Pair<Boolean, String> save_xls(Workbook workbook, String file_name){
        String fileName = file_name + ".xls";


        boolean value = false;
        File file = new File(folder, fileName);
        int count = 1;
        while (file.exists()){
            fileName = file_name + " (" + count + ")";
            count++;
            file = new File(folder, fileName + ".xls");
        }
        try {
            value = file.createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Pair<>(value, fileName);
    }
}
