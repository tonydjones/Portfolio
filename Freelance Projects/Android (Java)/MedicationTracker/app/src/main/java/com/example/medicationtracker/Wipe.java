package com.example.medicationtracker;

import static com.example.medicationtracker.MainActivity.history;

public class Wipe {
    public static void wipe(MainActivity Activity, String title, Runnable page) {

        Activity.getSupportActionBar().setTitle(title);

        Activity.screen.removeAllViews();
        Activity.screen.addView(Activity.scroll);
        Activity.scroll_child.removeAllViews();

        if (page != null){
            history.add(0, page);
            if (history.size() > 100){
                history.remove(100);
            }
        }
    }
}
