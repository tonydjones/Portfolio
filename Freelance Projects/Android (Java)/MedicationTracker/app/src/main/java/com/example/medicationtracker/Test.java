package com.example.medicationtracker;

import android.os.Environment;
import android.util.Log;

public class Test {
    public static void test(MainActivity Activity){
        CustomizedExceptionHandler handler = new CustomizedExceptionHandler(
                Environment.getExternalStorageDirectory().getPath());
        Thread.setDefaultUncaughtExceptionHandler(handler);
        handler.write_to_log("Testing testing 123");
        Integer test = null;
        test += 1;
    }
}
