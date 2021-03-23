package com.example.medicationtracker;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.gson.Gson;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static com.example.medicationtracker.Base.base;
import static com.example.medicationtracker.Check_Folders.check_folders;
import static com.example.medicationtracker.DatabaseMaps.database_maps;
import static com.example.medicationtracker.Help.help;
import static com.example.medicationtracker.Home.home;
import static com.example.medicationtracker.Setup.setup;

public class MainActivity extends AppCompatActivity {

    public ScrollView scroll;

    public LinearLayout scroll_child;

    public LinearLayout screen;

    public static SharedPreferences prefs;

    public static SharedPreferences.Editor editor;

    public static int signature_size;

    public static Gson gson;

    public static AlertDialog.Builder warning;

    public static boolean admin_mode;

    public static boolean staff_mode;

    public static List<Runnable> history;

    public static DatabaseHelper clients_db;

    public static DatabaseHelper prescriptions_db;

    public static DatabaseHelper entries_db;

    public static DatabaseHelper presets_db;

    public static String master_key;

    public static LinearLayout.LayoutParams weighted_params;

    public static LinearLayout.LayoutParams unweighted_params;

    public static File folder;

    public static boolean record_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        screen = findViewById(R.id.screen);
        scroll = new ScrollView(this);
        scroll_child = new LinearLayout(this);
        scroll_child.setOrientation(LinearLayout.VERTICAL);
        screen.addView(scroll);
        scroll.addView(scroll_child);

        weighted_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        unweighted_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);

        prefs = getApplicationContext().getSharedPreferences("prefs", 0);
        editor = prefs.edit();
        gson = new Gson();
        warning = new AlertDialog.Builder(this);
        history = new ArrayList<>();
        gson = new Gson();
        signature_size = 300;

        if (!check_folders(this)){
            UI_Message.ui_message(this, "Error: Necessary folders not present and could not be generated");
        }

        Map<String, String> client_map = database_maps("clients");

        Map<String, String> prescriptions_map = database_maps("prescriptions");

        Map<String, String> entries_map = database_maps("entries");

        Map<String, String> presets_map = database_maps("presets");

        /*clients_db = new DatabaseHelper(getApplicationContext(),
                "clients",
                "clients",
                client_map);

        prescriptions_db = new DatabaseHelper(getApplicationContext(),
                "prescriptions",
                "prescriptions",
                prescriptions_map);

        entries_db = new DatabaseHelper(getApplicationContext(),
                "entries",
                "entries",
                entries_map);*/

        clients_db = new DatabaseHelper(getApplicationContext(),
                "MedTracker",
                "clients",
                client_map);

        prescriptions_db = new DatabaseHelper(getApplicationContext(),
                "MedTracker",
                "prescriptions",
                prescriptions_map);

        entries_db = new DatabaseHelper(getApplicationContext(),
                "MedTracker",
                "entries",
                entries_map);

        presets_db = new DatabaseHelper(getApplicationContext(),
                "MedTracker",
                "presets",
                presets_map);


        master_key = "secretcode102694";
        record_password = prefs.getBoolean("record_password", true);
        boolean setup_complete = prefs.getBoolean("setup_complete", false);

        if (setup_complete){
            home(this);
        }
        else{
            setup(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_buttons, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {
        /*if (admin_mode || (record_password && staff_mode)){
            warning.setView(null);
            warning.setNegativeButton("Cancel", null);
            warning.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    history.clear();
                    home(MainActivity.this);
                }
            });
            warning.setTitle("Return to home screen?");
            warning.setMessage("Returning to the home screen will delete your history (pressing the back button will not return you to previous functions.) You will have " +
                    "to use your password to access these functions again.");
            warning.show();
        }
        else {
            home(this);
        }*/
        if (menu.getTitle().equals("home")){
            base(this);
        }
        else if (menu.getTitle().equals("logout")){
            if (admin_mode || (record_password && staff_mode)){
                warning.setView(null);
                warning.setNegativeButton("Cancel", null);
                warning.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        history.clear();
                        home(MainActivity.this);
                    }
                });
                warning.setTitle("Log Out?");
                warning.setMessage("Logging out will delete your history (pressing the back button will not return you to previous functions.) You will have " +
                        "to use your password to access these functions again.");
                warning.show();
            }
            else {
                home(this);
            }
        }
        else {
            help(this);
        }
        Log.e("title", menu.getTitle().toString());

        return true;
    }

    public ImageView signature(byte[] signature){
        Bitmap staff_bmp = BitmapFactory.decodeByteArray(signature, 0, signature.length);
        ImageView sign = new ImageView(getApplicationContext());
        sign.setImageBitmap(Bitmap.createScaledBitmap(staff_bmp, getResources().getDisplayMetrics().widthPixels, signature_size, false));
        return sign;
    }

    @Override
    public void onBackPressed() {
        if (history.size() >= 2){
            Runnable function = history.get(1);
            history.remove(1);
            history.remove(0);
            runOnUiThread(function);
        }
        else{
            super.onBackPressed();
        }
    }
}