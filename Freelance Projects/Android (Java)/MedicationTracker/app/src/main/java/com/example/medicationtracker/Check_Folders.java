package com.example.medicationtracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

import static com.example.medicationtracker.MainActivity.folder;

public class Check_Folders {

    public static boolean check_folders(MainActivity Activity){
        if (ContextCompat.checkSelfPermission(Activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(Activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        }

        String externalStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
        File outputDirectory = new File(externalStorage + File.separator + "Documents" );
        folder = new File(outputDirectory, "Medication Tracker");

        if(folder.exists()){
            return true;
        }
        else{
            if(!outputDirectory.exists()){
                outputDirectory.mkdir();
            }
            folder.mkdir();
            if(folder.exists()){
                return true;
            }
        }
        return false;
    }
}
