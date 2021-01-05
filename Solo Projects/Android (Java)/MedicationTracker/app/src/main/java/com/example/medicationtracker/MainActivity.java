package com.example.medicationtracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.sl.usermodel.Line;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.Inet4Address;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public ScrollView scroll;

    public LinearLayout scroll_child;

    public LinearLayout screen;

    public LinearLayout scroll_parent;

    public SharedPreferences prefs;

    public String password;

    public String admin_password;

    public String backup_date;

    public SharedPreferences.Editor editor;

    public Integer signature_size;

    public Gson gson;

    public AlertDialog.Builder warning;

    public Boolean admin_mode;

    public List<Runnable> history;

    public DatabaseHelper clients_db;

    public DatabaseHelper prescriptions_db;

    public DatabaseHelper entries_db;

    public String code;

    public LinearLayout.LayoutParams weighted_params;

    public LinearLayout.LayoutParams unweighted_params;

    public File folder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        screen = findViewById(R.id.screen);
        scroll = new ScrollView(this);
        scroll_child = new LinearLayout(this);
        scroll_parent = new LinearLayout(this);
        scroll_child.setOrientation(LinearLayout.VERTICAL);
        scroll_parent.setOrientation(LinearLayout.VERTICAL);

        weighted_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        unweighted_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);

        screen.addView(scroll);
        scroll.addView(scroll_child);

        prefs = getApplicationContext().getSharedPreferences("prefs", 0);
        editor = prefs.edit();
        gson = new Gson();
        warning = new AlertDialog.Builder(this);
        password = prefs.getString("password", null);
        admin_password = prefs.getString("admin_password", null);
        backup_date = prefs.getString("backup_date", null);
        history = new ArrayList<>();
        gson = new Gson();
        signature_size = 300;

        if (!check_folders()){
            Toast.makeText(getApplicationContext(), "Error: Necessary folders not present and could not be generated",
                    Toast.LENGTH_LONG).show();
        }

        Map<String, String> client_map = new HashMap<>();
        client_map.put("name", "TINYTEXT");
        client_map.put("admit", "BIGINT");
        client_map.put("active", "BOOLEAN");
        client_map.put("discharge", "BIGINT");
        client_map.put("edits", "TINYINT");
        clients_db = new DatabaseHelper(getApplicationContext(),
                "clients",
                "clients",
                client_map);

        code = "secretcode102694";

        Map<String, String> prescriptions_map = new HashMap<>();
        prescriptions_map.put("client_id", "INT");
        prescriptions_map.put("drug", "TINYTEXT");
        prescriptions_map.put("dose", "TINYTEXT");
        prescriptions_map.put("dose_max", "FLOAT");
        prescriptions_map.put("daily_max", "FLOAT");
        prescriptions_map.put("instructions", "TINYTEXT");
        prescriptions_map.put("as_needed", "BOOLEAN");
        prescriptions_map.put("controlled", "BOOLEAN");
        prescriptions_map.put("count", "FLOAT");
        prescriptions_map.put("indication", "TINYTEXT");
        prescriptions_map.put("prescriber", "TINYTEXT");
        prescriptions_map.put("pharmacy", "TINYTEXT");
        prescriptions_map.put("start", "BIGINT");
        prescriptions_map.put("end", "BIGINT");
        prescriptions_map.put("active", "BOOLEAN");
        prescriptions_map.put("name", "TINYTEXT");
        prescriptions_map.put("edits", "TINYINT");

        Map<String, String> entries_map = new HashMap<>();
        entries_map.put("client_id", "INT");
        entries_map.put("prescription_id", "INT");
        entries_map.put("drug", "TINYTEXT");
        entries_map.put("datetime", "BIGINT");
        entries_map.put("old_count", "FLOAT");
        entries_map.put("change", "FLOAT");
        entries_map.put("new_count", "FLOAT");
        entries_map.put("dose_override", "BOOLEAN");
        entries_map.put("daily_override", "BOOLEAN");
        entries_map.put("edits", "TINYINT");
        entries_map.put("method", "TINYTEXT");
        entries_map.put("client_signature", "LONGTEXT");
        entries_map.put("staff_signature_1", "LONGTEXT");
        entries_map.put("staff_signature_2", "LONGTEXT");
        entries_map.put("notes", "LONGTEXT");


        prescriptions_db = new DatabaseHelper(getApplicationContext(),
                "prescriptions",
                "prescriptions",
                prescriptions_map);

        entries_db = new DatabaseHelper(getApplicationContext(),
                "entries",
                "entries",
                entries_map);

        update();

        home();
    }

    public void update(){

        List<String> columnNames = clients_db.get_column_names();

        if (!columnNames.contains("edits")){
            SQLiteDatabase clients = clients_db.getWritableDatabase();
            clients.execSQL("ALTER TABLE clients ADD COLUMN edits TINYINT");
            clients.close();
        }

        List<Map<String, Object>> client_data = clients_db.getRows(null, null, null, false);
        for (int i = 0; i < client_data.size(); i++){
            if (client_data.get(i).get("edits") == null){
                client_data.get(i).put("edits", 0);
                clients_db.update(client_data.get(i), new String[]{"id=" + client_data.get(i).get("id")});
            }
        }

        columnNames = prescriptions_db.get_column_names();

        if (!columnNames.contains("edits")){
            SQLiteDatabase clients = prescriptions_db.getWritableDatabase();
            clients.execSQL("ALTER TABLE prescriptions ADD COLUMN edits TINYINT");
            clients.close();
        }

        List<Map<String, Object>> prescription_data = prescriptions_db.getRows(null, null, null, false);
        for (int i = 0; i < prescription_data.size(); i++){
            if (prescription_data.get(i).get("edits") == null){
                prescription_data.get(i).put("edits", 0);
                prescriptions_db.update(prescription_data.get(i), new String[]{"id=" + prescription_data.get(i).get("id")});
            }
        }

        columnNames = entries_db.get_column_names();

        if (!columnNames.contains("old_count")){
            SQLiteDatabase clients = entries_db.getWritableDatabase();
            clients.execSQL("ALTER TABLE entries ADD COLUMN old_count FLOAT");
            clients.close();
        }

        if (!columnNames.contains("new_count")){
            SQLiteDatabase clients = entries_db.getWritableDatabase();
            clients.execSQL("ALTER TABLE entries ADD COLUMN new_count FLOAT");
            clients.close();
        }

        if (columnNames.contains("count") && !columnNames.contains("change")){
            SQLiteDatabase clients = entries_db.getWritableDatabase();
            clients.execSQL("ALTER TABLE entries ADD COLUMN change FLOAT");
            clients.execSQL("UPDATE entries SET change = count");
            clients.close();

            List<Map<String, Object>> entries = entries_db.getRows(new String[]{"count", "change", "method", "drug", "id"}, null, null, false);
            boolean match = true;

            for (int i = 0; i < entries.size(); i++){
                Log.wtf(gson.toJson(entries.get(i).get("count")), gson.toJson(entries.get(i).get("change")));
                if (entries.get(i).get("count") == null && entries.get(i).get("change") == null){
                    continue;
                }
                else if (entries.get(i).get("count") == null && entries.get(i).get("change") != null){
                    match = false;
                    break;
                }
                else if (entries.get(i).get("count") != null && entries.get(i).get("change") == null){
                    match = false;
                    break;
                }
                else if ((float)entries.get(i).get("count") != (float)entries.get(i).get("change")){
                    if (entries.get(i).get("method").equals("COUNT") || !entries.get(i).get("method").equals("MISCOUNT")){
                        Log.wtf(gson.toJson(entries.get(i).get("method")), gson.toJson(entries.get(i).get("drug")));
                        Map<String, Object> entry = new HashMap<>();
                        entry.put("count", entries.get(i).get("change"));
                        entries_db.update(entry, new String[]{"id="+entries.get(i).get("id")});
                    }
                    else{
                        match = false;
                        break;
                    }
                }
            }
            if (match){
                Toast.makeText(getApplicationContext(), "it matches!",
                        Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getApplicationContext(), "it doesn't match!",
                        Toast.LENGTH_LONG).show();
            }

            for (int i = 0; i < prescription_data.size(); i++){
                Map<String,Object> prescription = prescription_data.get(i);
                if ((int)prescription.get("controlled") == 1){
                    List<Map<String, Object>> entries_data = entries_db.getRows(null, new String[]{"prescription_id="+prescription.get("id")},
                            new String[]{"datetime", "ASC"}, false);
                    Float count = (float)0;
                    for (int j = 0; j < entries_data.size(); j++){
                        Map<String,Object> entry = entries_data.get(j);
                        if (entry.get("old_count") == null || entry.get("new_count") == null || entry.get("change") == null){
                            if (entry.get("method").equals("TOOK MEDS")){
                                entry.put("old_count", count);
                                count -= (float)entry.get("change");
                                entry.put("new_count", count);
                            }
                            else if (entry.get("method").equals("REFILL")){
                                entry.put("old_count", count);
                                count += (float)entry.get("change");
                                entry.put("new_count", count);
                            }
                            else if (entry.get("method").equals("INTAKE") || entry.get("method").equals("PRESCRIPTION STARTED") ||
                                    entry.get("method").equals("UPDATED PRESCRIPTION STARTED")){
                                if (entry.get("change") == null){
                                    break;
                                }
                                entry.put("old_count", 0);
                                entry.put("new_count", entry.get("change"));
                                count = (float) entry.get("change");
                            }
                            else if (entry.get("method").equals("COUNT")){
                                entry.put("old_count", count);
                                entry.put("new_count", count);
                                entry.put("change", 0);
                            }
                            else if (entry.get("method").equals("MISCOUNT")){
                                entry.put("old_count", count);
                                entry.put("new_count", entry.get("change"));
                                Float temp = (float) entry.get("change");
                                entry.put("change", (float)entry.get("change") - count);
                                count = temp;
                            }
                            else{
                                entry.put("old_count", entry.get("change"));
                                entry.put("change", null);
                                entry.put("new_count", null);
                            }
                            entries_db.update(entry, new String[]{"id=" + entry.get("id")});
                        }
                    }
                }
            }
        }

    }

    public void test(){

       // wipe("Test Page", this::test);

        Workbook workbook = new HSSFWorkbook();

        List<Map<String, Object>> client_data = clients_db.getRows(null, null, new String[]{"name", "ASC", "admit", "ASC"}, false);

        Sheet clients = client_sheet(workbook, "Clients", client_data);

        String previous_client = "";
        int client_counter = 1;

        for (int i = 0; i < client_data.size(); i++){
            List<Map<String, Object>> prescription_data = prescriptions_db.getRows(null,
                    new String[]{"client_id="+client_data.get(i).get("id")},
                    new String[]{"name", "ASC", "start", "DESC"}, false);

            String sheet_name = client_data.get(i).get("name") + " Prescriptions";

            if (sheet_name.equals(previous_client)){
                sheet_name += " (" + client_counter + ")";
                client_counter++;
            }
            else {
                previous_client = sheet_name;
                client_counter = 1;
            }

            Sheet prescriptions = prescription_sheet(workbook,
                    sheet_name, prescription_data, "12/01/20", "12/1020");

            String previous_prescription = "";
            int prescription_counter = 1;

            for (int j = 0; j < prescription_data.size(); j++){
                List<Map<String, Object>> entry_data = entries_db.getRows(null,
                        new String[]{"prescription_id="+prescription_data.get(j).get("id")},
                        new String[]{"datetime", "ASC"}, false);

                Map<String, Object> prescription = prescription_data.get(j);

                String entry_sheet_name = client_data.get(i).get("name") + " " + prescription.get("name");

                if (entry_sheet_name.equals(previous_prescription)){
                    entry_sheet_name += " (" + prescription_counter + ")";
                    prescription_counter++;
                }
                else {
                    previous_prescription = entry_sheet_name;
                    prescription_counter = 1;
                }

                Sheet entries = entry_sheet(workbook, entry_sheet_name, entry_data);
            }
        }

        save_xls(workbook, "Testing");

    }

    public void test2(){
        wipe("Test Page", this::test2);

        String text = "";

        for (int i = 1; i < 6; i++) {
            text += "\nTest Line " + i;
        }

        TextView info = new TextView(this);
        info.setText(text.substring(1));
        scroll_child.addView(info);

        final SignatureView signature = create_signature_view();

        final Button confirm = new Button(this);
        confirm.setText("Client Signoff");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signature.ClearCanvas();
            }
        });

        final Button clear = new Button(this);
        clear.setText("Clear Canvas");
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signature.ClearCanvas();
            }
        });

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.addView(clear);
        buttons.addView(confirm);
        confirm.setLayoutParams(weighted_params);

        screen.addView(signature);
        screen.addView(buttons);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_buttons, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {
        home();
        return true;
    }

    public void home() {

        wipe("Medication Tracker", this::home);

        admin_mode = false;

        Button meds = new Button(this);
        Button admin = new Button(this);
        Button reset = new Button(this);
        reset.setText("Reset App");
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                warning.setNegativeButton("Cancel", null);
                warning.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        auto_reset();
                    }
                });
                warning.setTitle("Confirm Reset");
                warning.setMessage("Did you misclick you big dummy? Are you sure you want to reset the test clients?");
                warning.show();
            }
        });

        meds.setText("Staff");
        admin.setText("Admin");
        meds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (password != null) {
                    password();
                } else {
                    clients();
                }

            }
        });
        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                admin();
            }
        });

        Button test = new Button(this);
        test.setText("Test New Feature");
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //test();
            }
        });

        Button test2 = new Button(this);
        test2.setText("Test New Feature 2");
        test2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //test2();
            }
        });

        scroll_child.addView(meds);
        scroll_child.addView(admin);
        scroll_child.addView(reset);
        scroll_child.addView(test);
        scroll_child.addView(test2);
    }

    public void password() {
        wipe("Enter Password", null);
        EditText pass = new EditText(this);
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pass.setHint("Enter Password");
        Button submit = new Button(this);
        submit.setText("Submit Password");
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pass.getText().toString().equals(password) || (code != null && pass.getText().toString().equals(code))) {
                    clients();
                } else {
                    pass.setText("");
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("Try Again", null);
                    warning.setTitle("Incorrect Password");
                    warning.setMessage("The password you entered is incorrect");
                    warning.show();
                }
            }
        });
        scroll_child.addView(pass);
        scroll_child.addView(submit);
    }

    public void admin_password() {
        wipe("Enter Password", null);
        EditText pass = new EditText(this);
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pass.setHint("Enter Admin Password");
        Button submit = new Button(this);
        submit.setText("Submit Password");
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pass.getText().toString().equals(admin_password) || (code != null && pass.getText().toString().equals(code))) {
                    admin_mode = true;
                    admin();
                } else {
                    pass.setText("");
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("Try Again", null);
                    warning.setTitle("Incorrect Password");
                    warning.setMessage("The password you entered is incorrect");
                    warning.show();
                }
            }
        });
        scroll_child.addView(pass);
        scroll_child.addView(submit);
    }

    public void admin() {
        if (admin_password != null && !admin_mode){
            admin_password();
            return;
        }

        wipe("Admin", this::admin);
        admin_mode = true;

        Button change_pass = new Button(this);
        change_pass.setText("Set Password");
        change_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (password == null) {
                    make_password();
                } else {
                    change_password();
                }
            }
        });
        scroll_child.addView(change_pass);

        Button admin_pass = new Button(this);
        admin_pass.setText("Set Admin Password");
        admin_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (admin_password == null) {
                    make_admin_password();
                } else {
                    change_admin_password();
                }
            }
        });
        scroll_child.addView(admin_pass);

        Button view_client = new Button(this);
        view_client.setText("Clients");
        view_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clients();
            }
        });
        scroll_child.addView(view_client);

        Button spreadsheets = new Button(this);
        spreadsheets.setText("Generate Spreadsheets");
        spreadsheets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spreadsheets();
            }
        });
        scroll_child.addView(spreadsheets);

        Button edit = new Button(this);
        edit.setText("Edit Data");
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit();
            }
        });
        scroll_child.addView(edit);

        Button backup = new Button(this);
        backup.setText("Back Up Data");
        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                warning.setNegativeButton("Cancel", null);
                warning.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        long time = System.currentTimeMillis();
                        Map<String, String> client_map = new HashMap<>();
                        client_map.put("name", "TINYTEXT");
                        client_map.put("admit", "BIGINT");
                        client_map.put("active", "BOOLEAN");
                        client_map.put("discharge", "BIGINT");
                        client_map.put("edits", "TINYINT");

                        DatabaseHelper client_backup = new DatabaseHelper(getApplicationContext(),
                                "clients_" + time,
                                "clients_" + time,
                                client_map);


                        Map<String, String> prescriptions_map = new HashMap<>();
                        prescriptions_map.put("client_id", "INT");
                        prescriptions_map.put("drug", "TINYTEXT");
                        prescriptions_map.put("dose", "TINYTEXT");
                        prescriptions_map.put("dose_max", "FLOAT");
                        prescriptions_map.put("daily_max", "FLOAT");
                        prescriptions_map.put("instructions", "TINYTEXT");
                        prescriptions_map.put("as_needed", "BOOLEAN");
                        prescriptions_map.put("controlled", "BOOLEAN");
                        prescriptions_map.put("count", "FLOAT");
                        prescriptions_map.put("indication", "TINYTEXT");
                        prescriptions_map.put("prescriber", "TINYTEXT");
                        prescriptions_map.put("pharmacy", "TINYTEXT");
                        prescriptions_map.put("start", "BIGINT");
                        prescriptions_map.put("end", "BIGINT");
                        prescriptions_map.put("active", "BOOLEAN");
                        prescriptions_map.put("name", "TINYTEXT");
                        prescriptions_map.put("edits", "TINYINT");

                        Map<String, String> entries_map = new HashMap<>();
                        entries_map.put("client_id", "INT");
                        entries_map.put("prescription_id", "INT");
                        entries_map.put("drug", "TINYTEXT");
                        entries_map.put("datetime", "BIGINT");
                        entries_map.put("old_count", "FLOAT");
                        entries_map.put("change", "FLOAT");
                        entries_map.put("new_count", "FLOAT");
                        entries_map.put("dose_override", "BOOLEAN");
                        entries_map.put("daily_override", "BOOLEAN");
                        entries_map.put("edits", "TINYINT");
                        entries_map.put("method", "TINYTEXT");
                        entries_map.put("client_signature", "LONGTEXT");
                        entries_map.put("staff_signature_1", "LONGTEXT");
                        entries_map.put("staff_signature_2", "LONGTEXT");
                        entries_map.put("notes", "LONGTEXT");


                        DatabaseHelper prescription_backup = new DatabaseHelper(getApplicationContext(),
                                "prescriptions_" + time,
                                "prescriptions_" + time,
                                prescriptions_map);

                        DatabaseHelper entry_backup = new DatabaseHelper(getApplicationContext(),
                                "entries_" + time,
                                "entries_" + time,
                                entries_map);

                        List<Map<String, Object>> clients = clients_db.getRows(null, null, null, false);
                        List<Map<String, Object>> prescriptions = prescriptions_db.getRows(null, null, null, false);
                        List<Map<String, Object>> entries = entries_db.getRows(null, null, null, false);

                        for (int i = 0; i < clients.size(); i++){
                            client_backup.addRow(clients.get(i));
                        }
                        for (int i = 0; i < prescriptions.size(); i++){
                            prescription_backup.addRow(prescriptions.get(i));
                        }
                        for (int i = 0; i < entries.size(); i++){
                            entry_backup.addRow(entries.get(i));
                        }

                        List<Long> backup_times =  gson.fromJson(prefs.getString("backup_times", gson.toJson(new ArrayList<>())), new TypeToken<List<Long>>(){}.getType());
                        backup_times.add(time);
                        editor.putString("backup_times", gson.toJson(backup_times));
                        editor.apply();
                        Toast.makeText(getApplicationContext(), "Data was saved.",
                                Toast.LENGTH_LONG).show();
                    }
                });
                warning.setTitle("Confirm data backup");
                warning.setMessage("Are you sure you want to save a copy of the current databases?");
                warning.show();
            }
        });
        scroll_child.addView(backup);

        Button restore = new Button(this);
        restore.setText("Manage Backup Data");
        restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manage();
            }
        });
        scroll_child.addView(restore);

        Button delete = new Button(this);
        delete.setText("Delete Data");
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
            }
        });
        scroll_child.addView(delete);
    }

    public void make_password() {
        wipe("Create Your Password", this::make_password);
        EditText admin_pass = new EditText(this);
        if (admin_password != null) {
            admin_pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            admin_pass.setHint("Enter Admin Password");
            scroll_child.addView(admin_pass);
        }
        EditText pass = new EditText(this);
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText confirm = new EditText(this);
        confirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pass.setHint("Create Password");
        confirm.setHint("Confirm Password");
        Button submit = new Button(this);
        submit.setText("Save Password");
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pass.getText().toString().equals(confirm.getText().toString())) {
                    admin_pass.setText("");
                    pass.setText("");
                    confirm.setText("");
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("Try Again", null);
                    warning.setTitle("Invalid passwords");
                    warning.setMessage("The passwords must match!");
                    warning.show();
                } else if (pass.getText().toString().length() <= 0) {
                    admin_pass.setText("");
                    pass.setText("");
                    confirm.setText("");
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("OK", null);
                    warning.setTitle("Invalid Password");
                    warning.setMessage("You must enter a password");
                    warning.show();
                } else if (admin_password != null && !admin_pass.getText().toString().equals(admin_password) && (code == null || !admin_pass.getText().toString().equals(code))) {
                    admin_pass.setText("");
                    pass.setText("");
                    confirm.setText("");
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("Try Again", null);
                    warning.setTitle("Incorrect Admin Password");
                    warning.setMessage("The admin password you entered was incorrect");
                    warning.show();
                } else {
                    editor.putString("password", pass.getText().toString());
                    editor.apply();
                    password = pass.getText().toString();
                    admin();
                }
            }
        });
        scroll_child.addView(pass);
        scroll_child.addView(confirm);
        scroll_child.addView(submit);
    }

    public void change_password() {
        wipe("Change Your Password", this::change_password);
        EditText current = new EditText(this);
        current.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText pass = new EditText(this);
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText confirm = new EditText(this);
        confirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        if (admin_password != null) {
            current.setHint("Enter Admin Password");
        } else {
            current.setHint("Enter Current Password");
        }
        pass.setHint("Create New Password");
        confirm.setHint("Confirm New Password");

        Button submit = new Button(this);
        submit.setText("Save Password");
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pass.getText().toString().equals(confirm.getText().toString())) {
                    current.setText("");
                    pass.setText("");
                    confirm.setText("");
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("Try Again", null);
                    warning.setTitle("Invalid Passwords");
                    warning.setMessage("The new passwords must match");
                    warning.show();
                } else if (pass.getText().toString().length() <= 0) {
                    current.setText("");
                    pass.setText("");
                    confirm.setText("");
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("OK", null);
                    warning.setTitle("Invalid Password");
                    warning.setMessage("You must enter a new password");
                    warning.show();
                } else if (admin_password == null && !current.getText().toString().equals(password) && (code == null || !current.getText().toString().equals(code))) {
                    current.setText("");
                    pass.setText("");
                    confirm.setText("");
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("Try Again", null);
                    warning.setTitle("Incorrect Password");
                    warning.setMessage("The password you entered is incorrect");
                    warning.show();
                } else if (admin_password != null && !current.getText().toString().equals(admin_password) && (code == null || !current.getText().toString().equals(code))) {
                    current.setText("");
                    pass.setText("");
                    confirm.setText("");
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("Try Again", null);
                    warning.setTitle("Incorrect Admin Password");
                    warning.setMessage("The admin password you entered is incorrect");
                    warning.show();
                } else {
                    editor.putString("password", pass.getText().toString());
                    editor.apply();
                    password = pass.getText().toString();
                    admin();
                }
            }
        });

        Button remove = new Button(this);
        remove.setText("Remove Password");
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (admin_password == null && !current.getText().toString().equals(password) && (code == null || !current.getText().toString().equals(code))) {
                    current.setText("");
                    pass.setText("");
                    confirm.setText("");
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("Try Again", null);
                    warning.setTitle("Incorrect Password");
                    warning.setMessage("The password you entered is incorrect");
                    warning.show();
                } else if (admin_password != null && !current.getText().toString().equals(admin_password) && (code == null || !current.getText().toString().equals(code))) {
                    current.setText("");
                    pass.setText("");
                    confirm.setText("");
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("Try Again", null);
                    warning.setTitle("Incorrect Admin Password");
                    warning.setMessage("The admin password you entered is incorrect");
                    warning.show();
                } else {
                    password = null;
                    editor.putString("password", null);
                    editor.apply();
                    admin();
                }
            }
        });

        scroll_child.addView(current);
        scroll_child.addView(pass);
        scroll_child.addView(confirm);
        scroll_child.addView(submit);
        scroll_child.addView(remove);
    }

    public void make_admin_password() {
        wipe("Create Your Admin Password", this::make_admin_password);
        EditText admin_pass = new EditText(this);
        if (password != null) {
            admin_pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            admin_pass.setHint("Enter Password");
            scroll_child.addView(admin_pass);
        }
        EditText pass = new EditText(this);
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText confirm = new EditText(this);
        confirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pass.setHint("Create Admin Password");
        confirm.setHint("Confirm Admin Password");
        Button submit = new Button(this);
        submit.setText("Save Password");
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pass.getText().toString().equals(confirm.getText().toString())) {
                    admin_pass.setText("");
                    pass.setText("");
                    confirm.setText("");
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("Try Again", null);
                    warning.setTitle("Invalid passwords");
                    warning.setMessage("The passwords must match");
                    warning.show();
                } else if (pass.getText().toString().length() <= 0) {
                    admin_pass.setText("");
                    pass.setText("");
                    confirm.setText("");
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("OK", null);
                    warning.setTitle("Invalid Password");
                    warning.setMessage("You must enter a password");
                    warning.show();
                } else if (password != null && !admin_pass.getText().toString().equals(password) && (code == null || !admin_pass.getText().toString().equals(code))) {
                    admin_pass.setText("");
                    pass.setText("");
                    confirm.setText("");
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("Try Again", null);
                    warning.setTitle("Incorrect Admin Password");
                    warning.setMessage("The password you entered was incorrect");
                    warning.show();
                } else {
                    editor.putString("admin_password", pass.getText().toString());
                    editor.apply();
                    admin_password = pass.getText().toString();
                    admin();
                }
            }
        });
        scroll_child.addView(pass);
        scroll_child.addView(confirm);
        scroll_child.addView(submit);
    }

    public void change_admin_password() {
        wipe("Change Your Admin Password", this::change_admin_password);
        EditText current = new EditText(this);
        current.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText pass = new EditText(this);
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText confirm = new EditText(this);
        confirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        current.setHint("Enter Current Admin Password");
        pass.setHint("Create New Admin Password");
        confirm.setHint("Confirm New Admin Password");

        Button submit = new Button(this);
        submit.setText("Save Password");
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pass.getText().toString().equals(confirm.getText().toString())) {
                    current.setText("");
                    pass.setText("");
                    confirm.setText("");
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("Try Again", null);
                    warning.setTitle("Invalid Passwords");
                    warning.setMessage("The new passwords must match");
                    warning.show();
                } else if (pass.getText().toString().length() <= 0) {
                    current.setText("");
                    pass.setText("");
                    confirm.setText("");
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("OK", null);
                    warning.setTitle("Invalid Password");
                    warning.setMessage("You must enter a new password");
                    warning.show();
                } else if (!current.getText().toString().equals(admin_password) && (code == null || !current.getText().toString().equals(code))) {
                    current.setText("");
                    pass.setText("");
                    confirm.setText("");
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("Try Again", null);
                    warning.setTitle("Incorrect Admin Password");
                    warning.setMessage("The admin password you entered is incorrect");
                    warning.show();
                } else {
                    admin_password = pass.getText().toString();
                    editor.putString("admin_password", pass.getText().toString());
                    editor.apply();
                    admin();
                }
            }
        });

        Button remove = new Button(this);
        remove.setText("Remove Password");
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (admin_password == null && !current.getText().toString().equals(password) && (code == null || !current.getText().toString().equals(code))){
                    current.setText("");
                    pass.setText("");
                    confirm.setText("");
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("Try Again", null);
                    warning.setTitle("Incorrect Password");
                    warning.setMessage("The password you entered is incorrect");
                    warning.show();
                } else if (admin_password != null && !current.getText().toString().equals(admin_password) && (code == null || !current.getText().toString().equals(code))) {
                    current.setText("");
                    pass.setText("");
                    confirm.setText("");
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("Try Again", null);
                    warning.setTitle("Incorrect Admin Password");
                    warning.setMessage("The admin password you entered is incorrect");
                    warning.show();
                } else {
                    admin_password = null;
                    editor.putString("admin_password", null);
                    editor.apply();
                    admin();
                }
            }
        });

        scroll_child.addView(current);
        scroll_child.addView(pass);
        scroll_child.addView(confirm);
        scroll_child.addView(submit);
        scroll_child.addView(remove);
    }

    public void clients(){
        wipe("Current Clients", this::clients);

        Button count = new Button(this);
        count.setText("Med Count");
        count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Map<String, Object>> prescriptions = prescriptions_db.getRows(
                        new String[]{"id", "name", "client_id", "count", "prescriber", "pharmacy", "instructions"},
                        new String[]{"active=1", "controlled=1", "count>0"},
                        new String[]{"client_id", "ASC", "id", "ASC"}, false
                );
                count(prescriptions, new ArrayList<>());
            }
        });
        scroll_child.addView(count);

        Button new_client = new Button(this);
        new_client.setText("Add A New Client");
        new_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new_client();
            }
        });
        scroll_child.addView(new_client);

        TextView current = new TextView(this);
        current.setText("Current Clients");
        scroll_child.addView(current);

        TextView discharged = new TextView(this);
        discharged.setText("Discharged Clients");
        scroll_child.addView(discharged);

        ArrayList<Map<String, Object>> clients = clients_db.getRows(null, null, new String[]{"name", "ASC"}, false);

        int position = scroll_child.indexOfChild(current) + 1;
        int trigger = 0;

        for (int i = 0; i < clients.size(); i++) {
            Map<String, Object> client = clients.get(i);
            Button current_client = new Button(this);
            current_client.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, Object> current = clients_db.getSingleRow(null, new String[]{"id="+client.get("id")});
                    prescriptions(current);
                }
            });


            if ((int) client.get("active") == 0) {
                current_client.setText(client.get("name") + longs_to_range((long)client.get("admit"), (long)client.get("discharge")));

                scroll_child.addView(current_client);
                trigger++;
            }
            else {
                current_client.setText((String) client.get("name"));
                scroll_child.addView(current_client, position);
                position++;
            }
        }

        if (position == scroll_child.indexOfChild(current) + 1){
            scroll_child.removeView(current);
            if (trigger == 0){
                scroll_child.removeView(discharged);
                scroll_child.removeView(count);
            }
        }
        else if (trigger == 0){
            scroll_child.removeView(discharged);
        }
    }

    public void new_client(){
        wipe("Add A New Client", this::new_client);

        EditText name = new EditText(this);
        name.setHint("Full Name");
        scroll_child.addView(name);

        Button add = new Button(this);
        add.setText("Add Client");
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText().toString().length() > 0) {
                    add_multiple_prescriptions(name.getText().toString(), null, new ArrayList<>());
                }
                else{
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("OK", null);
                    warning.setTitle("Invalid Name");
                    warning.setMessage("You must enter a valid name for the new client");
                    warning.show();
                }
            }
        });
        scroll_child.addView(add);
    }

    public void add_multiple_prescriptions(String name, Integer client_id, List<Map<String, Object>> prescriptions) {
        List<Map<String, Object>> prescription_copy = new ArrayList<>();
        prescription_copy.addAll(prescriptions);
        wipe(name + " Intake Prescriptions", () -> add_multiple_prescriptions(name, client_id, prescription_copy));

        if (prescriptions.size() > 0) {
            String text = ((String) prescriptions.get(0).get("name"));
            if (prescriptions.get(0).get("count") != null){
                text += "\nStarting Count: " + prescriptions.get(0).get("count");
            }
            for (int i = 1; i < prescriptions.size(); i++) {
                text += "\n\n" + prescriptions.get(i).get("name");
                if (prescriptions.get(i).get("count") != null){
                    text += "\nStarting Count: " + prescriptions.get(i).get("count");
                }
            }
            TextView scripts = new TextView(this);
            scripts.setText(text);
            scroll_child.addView(scripts);
        }
        Button new_script = new Button(this);
        new_script.setText("Add A New Prescription");
        new_script.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new_prescription(name, client_id, prescriptions);
            }
        });
        scroll_child.addView(new_script);

        Button done = new Button(this);
        done.setText("No More Prescriptions To Add");
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (prescriptions.size() > 0) {
                    new_prescription_signoff(name, client_id, prescriptions);
                }
                else {

                    warning.setNegativeButton("Cancel", null);
                    warning.setPositiveButton("Add Client", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            clients_db.addRow(create_client(name, System.currentTimeMillis()));
                            history.remove(0);
                            history.remove(0);
                            history.remove(0);
                            clients();
                        }
                    });
                    warning.setTitle("No Prescriptions Added");
                    warning.setMessage("Are you sure you want to add this client with no prescriptions?");
                    warning.show();
                }
            }
        });
        scroll_child.addView(done);
    }

    public void new_prescription(String client_name, Integer client_id, List<Map<String, Object>> prescriptions){
        List<Map<String, Object>> prescription_copy = new ArrayList<>();
        prescription_copy.addAll(prescriptions);

        wipe("Add A New Prescription", () -> new_prescription(client_name, client_id, prescription_copy));

        EditText name = new EditText(this);
        name.setHint("Medication Name");
        scroll_child.addView(name);

        EditText dose = new EditText(this);
        dose.setHint("Dosage");
        scroll_child.addView(dose);

        EditText instructions = new EditText(this);
        instructions.setHint("Prescription Instructions");
        scroll_child.addView(instructions);

        CheckBox as_needed = new CheckBox(this);
        as_needed.setText("Take As Needed");
        scroll_child.addView(as_needed);

        CheckBox controlled = new CheckBox(this);
        controlled.setText("Controlled");

        controlled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!controlled.isChecked()) {
                    scroll_child.removeView(scroll_child.getChildAt(scroll_child.indexOfChild(controlled) + 1));
                } else {
                    EditText current_count = new EditText(getApplicationContext());
                    current_count.setHint("Count");
                    current_count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    scroll_child.addView(current_count, scroll_child.indexOfChild(controlled) + 1);
                }
            }
        });
        scroll_child.addView(controlled);

        EditText dose_max = new EditText(this);
        dose_max.setHint("Max At A Time");
        dose_max.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        scroll_child.addView(dose_max);

        EditText daily_max = new EditText(this);
        daily_max.setHint("Max Per Day");
        daily_max.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        scroll_child.addView(daily_max);

        EditText indication = new EditText(this);
        indication.setHint("Indication");
        scroll_child.addView(indication);

        EditText prescriber = new EditText(this);
        prescriber.setHint("Prescriber");
        scroll_child.addView(prescriber);

        EditText pharmacy = new EditText(this);
        pharmacy.setHint("Pharmacy");
        scroll_child.addView(pharmacy);

        Button add = new Button(this);
        add.setText("Add Prescription");
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText().toString().length() > 0 && dose.getText().toString().length() > 0 && instructions.getText().toString().length() > 0) {

                    Float max_dose = null;
                    if (dose_max.getText().toString().length() > 0) {
                        max_dose = Float.parseFloat(dose_max.getText().toString());
                        if (max_dose == 0){
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("OK", null);
                            warning.setTitle("Invalid Dose Limit");
                            warning.setMessage("0 is not a valid dose limit for a medication.");
                            warning.show();
                            return;
                        }
                    }

                    Float max_daily = null;
                    if (daily_max.getText().toString().length() > 0) {
                        max_daily = Float.parseFloat(daily_max.getText().toString());
                        if (max_daily == 0){
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("OK", null);
                            warning.setTitle("Invalid Dose Limit");
                            warning.setMessage("0 is not a valid dose limit for a medication.");
                            warning.show();
                            return;
                        }
                    }

                    Boolean prn = false;
                    if (as_needed.isChecked()) {
                        prn = true;
                    }

                    Boolean control = false;
                    Float count = null;
                    if (controlled.isChecked()) {
                        control = true;
                        EditText count_input = (EditText) scroll_child.getChildAt(scroll_child.indexOfChild(controlled) + 1);
                        if (count_input.getText().toString().length() > 0) {
                            count = Float.parseFloat(count_input.getText().toString());
                        } else {
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("OK", null);
                            warning.setTitle("Invalid Count");
                            warning.setMessage("You must enter a valid count for controlled medications");
                            warning.show();
                            return;
                        }
                    }

                    String reason = null;
                    if (indication.getText().toString().length() > 0) {
                        reason = indication.getText().toString();
                    }

                    String doctor = null;
                    if (prescriber.getText().toString().length() > 0) {
                        doctor = prescriber.getText().toString();
                    }

                    String pharm = null;
                    if (pharmacy.getText().toString().length() > 0) {
                        pharm = pharmacy.getText().toString();
                    }

                    Map<String, Object> script = create_prescription(client_id, name.getText().toString(),
                            dose.getText().toString(), max_dose, max_daily, instructions.getText().toString(),
                            prn, control, count, reason, doctor, pharm, null);

                    prescriptions.add(script);
                    add_multiple_prescriptions(client_name, client_id, prescriptions);
                }
                else{
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("OK", null);
                    warning.setTitle("Missing Information");
                    warning.setMessage("Drug, dose, and instructions are required fields");
                    warning.show();
                }
            }
        });
        scroll_child.addView(add);
    }

    public void new_prescription_signoff(String name, Integer client_id, List<Map<String, Object>> scripts) {
        List<Map<String, Object>> script_copy = new ArrayList<>();
        script_copy.addAll(scripts);

        wipe("New Prescriptions Sign Off", () -> new_prescription_signoff(name, client_id, script_copy));


        String text = (String) scripts.get(0).get("name");
        if (scripts.get(0).get("count") != null) {
            text += "\nStarting Count: " + scripts.get(0).get("count").toString();
        }

        for (int i = 1; i < scripts.size(); i++) {
            text += "\n\n" + scripts.get(i).get("name");
            if (scripts.get(i).get("count") != null) {
                text += "\nStarting Count: " + scripts.get(i).get("count").toString();
            }
        }

        TextView prescriptions = new TextView(this);
        prescriptions.setText(text);
        scroll_child.addView(prescriptions);

        final SignatureView signature = create_signature_view();
        LinearLayout buttons = new LinearLayout(this);

        if (admin_mode){
            CheckBox manual = new CheckBox(this);
            manual.setText("Manual Date And Time Entry");

            manual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signature.refresh_size(scroll, unweighted_params, weighted_params, signature_size);

                    if (!manual.isChecked()) {

                        scroll_child.removeView(scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1));
                        scroll_child.removeView(scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1));
                    }
                    else {

                        DatePicker date = new DatePicker(getApplicationContext());

                        TimePicker time = new TimePicker(getApplicationContext());

                        scroll_child.addView(date, scroll_child.indexOfChild(manual) + 1);
                        scroll_child.addView(time, scroll_child.indexOfChild(date) + 1);
                    }

                }
            });
            scroll_child.addView(manual);
        }

        Button confirm = new Button(this);
        confirm.setText("Staff Signoff");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String staff_sign = gson.toJson(signature.getBytes());

                long time = System.currentTimeMillis();

                List<String> notes = new ArrayList<>();

                if (admin_mode){
                    CheckBox manual = (CheckBox) scroll_child.getChildAt(scroll_child.indexOfChild(prescriptions) + 1);
                    if (manual.isChecked()){

                        DatePicker date_picker = (DatePicker) scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1);

                        TimePicker time_picker = (TimePicker) scroll_child.getChildAt(scroll_child.indexOfChild(date_picker) + 1);

                        String day = datepicker_to_date(date_picker) + " " + timepicker_to_time(time_picker);

                        time = datetime_to_long(day);

                        notes.add("MANUAL DATE/TIME ENTRY");
                    }
                }

                int new_client_id = 0;

                String method = "PRESCRIPTION STARTED";

                if (name != null){
                    Map<String, Object> client = create_client(name, time);

                    new_client_id = clients_db.addRow(client);

                    method = "INTAKE";
                }
                else{
                    new_client_id = client_id;
                }

                for (int i = 0; i < scripts.size(); i++){
                    Map<String, Object> prescription = scripts.get(i);
                    prescription.put("start", time);
                    prescription.put("client_id", new_client_id);

                    int id = prescriptions_db.addRow(prescription);

                    Float count = null;
                    Float start = null;
                    if (prescription.get("count") != null){
                        count = (float) prescription.get("count");
                        start = (float)0;
                    }

                    Map<String, Object> entry = create_entry(new_client_id, id, (String) prescription.get("name"),
                            start, count, count, time, false, false, 0, method,
                            null, staff_sign, null, notes);

                    entries_db.addRow(entry);
                }
                for (int i = 0; i < (scripts.size() * 2) + 3; i++){
                    history.remove(0);
                }
                if (name != null){
                    history.remove(0);
                    clients();
                }
                else {
                    prescriptions(clients_db.getSingleRow(null, new String[]{"id="+client_id}));
                }

            }
        });

        final Button clear = new Button(this);
        clear.setText("Clear Canvas");
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signature.ClearCanvas();
            }
        });

        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.addView(clear);
        buttons.addView(confirm);
        confirm.setLayoutParams(weighted_params);

        screen.addView(signature);
        screen.addView(buttons);
    }

    public void count(List<Map<String, Object>> prescriptions, List<Map<String, Object>> entries) {

        if (prescriptions.size() == 0){
            if (entries.size() == 0){
                history.remove(0);
                clients();
                return;
            }
            else{
                count_sign_1(entries);
                return;
            }

        }

        List<Map<String, Object>> prescriptions_copy = new ArrayList<>();
        List<Map<String, Object>> entries_copy = new ArrayList<>();

        prescriptions_copy.addAll(prescriptions);
        entries_copy.addAll(entries);

        wipe("Controlled Medication Count", () -> count(prescriptions_copy, entries_copy));

        Map<String, Object> prescription = prescriptions.get(0);

        String name = (String) clients_db.getObject("name", new String[]{"id=" + prescription.get("client_id")});
        String text = name;
        text += "\n" + prescription.get("name");
        text += "\nExpected Count: " + prescription.get("count");
        TextView med = new TextView(this);
        med.setText(text);
        scroll_child.addView(med);

        List<String> notes = new ArrayList<>();

        final Boolean[] miscount = {false};

        Button right = new Button(this);
        right.setText("Count is accurate");
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map <String, Object> entry = create_entry((int) prescription.get("client_id"), (int) prescription.get("id"), (String) prescription.get("name"),
                        (float) prescription.get("count"), (float)0, (float) prescription.get("count"), null, false, false, 0, "COUNT",
                        null, null, null, new ArrayList<>());
                entries.add(entry);

                prescriptions.remove(0);
                count(prescriptions, entries);
            }
        });
        scroll_child.addView(right);

        Button wrong = new Button(this);
        wrong.setText("Count is not accurate");
        wrong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!miscount[0]) {
                    miscount[0] = true;

                    String text = "";

                    long datetime = (long)(entries_db.getObject("MAX([datetime]) AS datetime",
                            new String[]{"prescription_id="+prescription.get("id"),
                                    "method IN ('COUNT', 'MISCOUNT', 'INTAKE', 'PRESCRIPTION STARTED', 'UPDATED PRESCRIPTION STARTED')"}));

                    List<Map<String, Object>> temp_entries = entries_db.getRows(null,
                            new String[]{"prescription_id="+prescription.get("id"), "datetime>="+datetime},
                            new String[]{"datetime", "DESC"}, false);

                    if (temp_entries.size() > 0){
                        text += "Here are the entries for this prescription since the last count:\n";
                        List<String> other_methods = Arrays.asList("CLIENT DISCHARGED", "COUNT", "MISCOUNT", "DISCONTINUED DUE TO UPDATE", "PRESCRIPTION DISCONTINUED");
                        for (int i = 0; i < temp_entries.size(); i++) {
                            Map<String, Object> entry = temp_entries.get(i);
                            TextView information = new TextView(getApplicationContext());
                            text += "\n" + long_to_datetime((long) entry.get("datetime"));
                            text += "\n" + entry.get("drug");
                            String method = (String)entry.get("method");
                            text += "\n" + method;

                            if (entry.get("old_count") != null) {
                                if (method.equals("COUNT") || method.equals("MISCOUNT")) {
                                    text += "\nExpected Count: " + entry.get("old_count");
                                }
                                else if (other_methods.contains(method)) {
                                    text += "\nRemaining Count: " + entry.get("old_count");
                                }
                                else {
                                    text += "\nPrevious Count: " + entry.get("old_count");
                                }
                            }

                            if (entry.get("change") != null) {
                                if (method.equals("TOOK MEDS")) {
                                    text += "\nChange: -" + entry.get("change");
                                }
                                else if (!method.equals("COUNT") && !method.equals("MISCOUNT") && entry.get("change") != null) {
                                    text += "\nChange: +" + entry.get("change");
                                }
                                else {
                                    text += "\nDiscrepancy: " + entry.get("change");
                                }
                            }

                            if (entry.get("new_count") != null) {
                                if (method.equals("COUNT") || method.equals("MISCOUNT")) {
                                    text += "\nActual Count: " + entry.get("new_count");
                                }
                                else {
                                    text += "\nNew Count: " + entry.get("new_count");
                                }
                            }

                            if ((int) entry.get("dose_override") == 1) {
                                text += "\nMAXIMUM DOSE OVERRIDE";
                            }

                            if ((int) entry.get("daily_override") == 1) {
                                text += "\nDAILY MAXIMUM OVERRIDE";
                            }

                            if ((int) entry.get("edits") > 0) {
                                text += "\nEDITED " + entry.get("edits") + " TIME";
                                if ((int) entry.get("edits") > 1){
                                    text += "S";
                                }
                            }

                            List<String> notes = gson.fromJson((String) entry.get("notes"), new TypeToken<List<String>>(){}.getType());
                            for (int j = 0; j < notes.size(); j++) {
                                text += "\n" + notes.get(j);
                            }

                            text += "\n";
                        }
                        text += "\n";
                    }

                    text += "Please enter the actual count and a note about how and why the count is inaccurate.";

                    TextView prompt = new TextView(getApplicationContext());
                    prompt.setText(text);
                    scroll_child.addView(prompt);

                    EditText count = new EditText(getApplicationContext());
                    count.setHint("New Count");
                    count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    scroll_child.addView(count);

                    EditText note = new EditText(getApplicationContext());
                    note.setHint("Note");
                    scroll_child.addView(note);

                    Button submit = new Button(getApplicationContext());
                    submit.setText("Submit Changes");
                    submit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (count.getText().toString().length() > 0){
                                notes.add(note.getText().toString());

                                Map <String, Object> entry = create_entry((int) prescription.get("client_id"), (int) prescription.get("id"), (String) prescription.get("name"),
                                        (float) prescription.get("count"), Float.parseFloat(count.getText().toString()) - (float) prescription.get("count"),
                                        Float.parseFloat(count.getText().toString()),
                                        null, false, false, 0, "MISCOUNT",
                                        null, null, null, notes);
                                entries.add(entry);


                                prescriptions.remove(0);
                                count(prescriptions, entries);
                            }
                            else{
                                warning.setNegativeButton("", null);
                                warning.setPositiveButton("OK", null);
                                warning.setTitle("Invalid Count");
                                warning.setMessage("You must enter the current count if it is different than expected");
                                warning.show();
                            }
                        }
                    });
                    scroll_child.addView(submit);
                }
            }
        });
        scroll_child.addView(wrong);
    }

    public void count_sign_1(List<Map<String, Object>> entries) {

        wipe("First Staff Sign Off On Count", () -> count_sign_1(entries));

        int client_id = (int) entries.get(0).get("client_id");

        String text = (String) clients_db.getObject("name", new String[]{"id="+client_id});

        for (int i = 0; i < entries.size(); i++) {
            Map<String, Object> entry = entries.get(i);
            if ((int) entry.get("client_id") != client_id){
                client_id = (int) entry.get("client_id");
                text += "\n\n" + clients_db.getObject("name", new String[]{"id="+client_id});;
            }
            text += "\n\n" + entry.get("drug") + "\nCurrent Count: " + entry.get("new_count");
            if (entry.get("method").equals("MISCOUNT")){
                text += "\nMISCOUNT";
            }
        }

        TextView summary = new TextView(this);
        summary.setText(text);
        scroll_child.addView(summary);

        SignatureView signature = create_signature_view();

        Button confirm = new Button(this);
        confirm.setText("First Staff Sign Off");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] staff_sign = signature.getBytes();
                count_sign_2(entries, staff_sign);
            }
        });
        final Button clear = new Button(this);
        clear.setText("Clear Canvas");
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signature.ClearCanvas();
            }
        });

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.addView(clear);
        buttons.addView(confirm);
        confirm.setLayoutParams(weighted_params);

        screen.addView(signature);
        screen.addView(buttons);
    }

    public void count_sign_2(List<Map<String, Object>> entries, byte[] sign_1) {

        wipe("Second Staff Sign Off On Count", () -> count_sign_2(entries, sign_1));

        int client_id = (int) entries.get(0).get("client_id");

        String text = (String) clients_db.getObject("name", new String[]{"id="+client_id});

        for (int i = 0; i < entries.size(); i++) {
            Map<String, Object> entry = entries.get(i);
            if ((int) entry.get("client_id") != client_id){
                client_id = (int) entry.get("client_id");
                text += "\n\n" + clients_db.getObject("name", new String[]{"id="+client_id});
            }
            text += "\n\n" + entry.get("drug") + "\nCurrent Count: " + entry.get("new_count");
            if (entry.get("method").equals("MISCOUNT")){
                text += "\nMISCOUNT";
            }
        }

        TextView summary = new TextView(this);
        summary.setText(text);
        scroll_child.addView(summary);

        final SignatureView signature = create_signature_view();

        if (admin_mode){
            CheckBox manual = new CheckBox(this);
            manual.setText("Manual Date And Time Entry");

            manual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signature.refresh_size(scroll, unweighted_params, weighted_params, signature_size);
                    if (!manual.isChecked()) {
                        scroll_child.removeView(scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1));
                        scroll_child.removeView(scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1));
                    }
                    else {

                        DatePicker date = new DatePicker(getApplicationContext());

                        TimePicker time = new TimePicker(getApplicationContext());

                        scroll_child.addView(date, scroll_child.indexOfChild(manual) + 1);
                        scroll_child.addView(time, scroll_child.indexOfChild(date) + 1);
                    }
                }
            });
            scroll_child.addView(manual);
        }


        Button confirm = new Button(this);
        confirm.setText("Second Staff Sign Off");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String staff_sign_2 = gson.toJson(signature.getBytes());
                String staff_sign_1 = gson.toJson(sign_1);

                long time = System.currentTimeMillis();

                boolean double_check = false;

                if (admin_mode){
                    CheckBox manual = (CheckBox) scroll_child.getChildAt(scroll_child.indexOfChild(summary) + 1);
                    if (manual.isChecked()){

                        double_check = true;

                        DatePicker date_picker = (DatePicker) scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1);

                        TimePicker time_picker = (TimePicker) scroll_child.getChildAt(scroll_child.indexOfChild(date_picker) + 1);

                        String day = datepicker_to_date(date_picker) + " " + timepicker_to_time(time_picker);

                        time = datetime_to_long(day);
                    }
                }

                for (int i = 0; i < entries.size(); i++){
                    Map<String, Object> entry = entries.get(i);

                    entry.put("datetime", time);
                    entry.put("staff_signature_1", staff_sign_1);
                    entry.put("staff_signature_2", staff_sign_2);

                    if (double_check){
                        List <String> notes = gson.fromJson((String)entry.get("notes"), new TypeToken<List<String>>(){}.getType());
                        notes.add(0, "MANUAL DATE/TIME ENTRY");
                        entry.put("notes", gson.toJson(notes));
                    }

                    entries_db.addRow(entry);

                    if (entry.get("method").equals("MISCOUNT")){
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("count", entry.get("new_count"));
                        prescriptions_db.update(updates, new String[]{"id="+entry.get("prescription_id")});
                    }

                }
                for (int i = 0; i < entries.size() + 3; i++){
                    history.remove(0);
                }
                clients();
            }
        });
        final Button clear = new Button(this);
        clear.setText("Clear Canvas");
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signature.ClearCanvas();
            }
        });

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.addView(clear);
        buttons.addView(confirm);
        confirm.setLayoutParams(weighted_params);

        screen.addView(signature);
        screen.addView(buttons);
    }

    public void prescriptions(Map<String, Object> client) {


        boolean active = ((int)client.get("active") == 1);

        wipe((String) client.get("name"), () -> prescriptions(client));


        String client_string = "This client was admitted on " + long_to_date((long) client.get("admit"));

        List<Map<String, Object>> prescriptions = prescriptions_db.getRows(
                new String[]{"name", "start", "end", "id", "active"},
                new String[]{"client_id="+client.get("id")},
                new String[]{"name", "ASC", "start", "DESC"}, false);

        if (!active) {
            client_string += "\nThis client was discharged on " + long_to_date((Long)client.get("discharge"));
        }

        if ((int)client.get("edits") > 0) {
            client_string += "\nThis client's information has been edited " + client.get("edits") + " time";
            if ((int)client.get("edits") > 1){
                client_string += "s";
            }
        }

        TextView admit_date = new TextView(this);
        admit_date.setText(client_string);
        scroll_child.addView(admit_date);

        if (active) {
            if (prescriptions.size() > 0) {
                Button take = new Button(this);
                take.setText("Take Medications");
                take.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        take_meds((int) client.get("id"));
                    }
                });
                scroll_child.addView(take);
            }

            Button new_script = new Button(this);
            new_script.setText("Add New Prescriptions");
            new_script.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    add_multiple_prescriptions(null, (int) client.get("id"), new ArrayList<>());
                }
            });
            scroll_child.addView(new_script);

            if (prescriptions.size() > 0) {
                Button refill = new Button(this);
                refill.setText("Refill Prescriptions");
                refill.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refill((int)client.get("id"));
                    }
                });
                scroll_child.addView(refill);

                Button update = new Button(this);
                update.setText("Update Prescriptions");
                update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        update_multiple((int)client.get("id"));
                    }
                });
                scroll_child.addView(update);

                Button discontinue = new Button(this);
                discontinue.setText("Discontinue Prescriptions");
                discontinue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        discontinue_multiple((int)client.get("id"));
                    }
                });
                scroll_child.addView(discontinue);
            }
        }

        if (prescriptions.size() > 0) {
            Button history = new Button(this);
            history.setText("View Medication Times");
            history.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    history((int)client.get("id"));
                }
            });
            scroll_child.addView(history);

            Button adherence = new Button(this);
            adherence.setText("View Medication Adherence");
            adherence.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    client_adherence((int)client.get("id"), (long)client.get("admit"));
                }
            });
            scroll_child.addView(adherence);
        }


        if (active){
            Button discharge = new Button(this);
            discharge.setText("Discharge Client");
            discharge.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirm_discharge((int) client.get("id"), (String) client.get("name"));
                }
            });
            scroll_child.addView(discharge);
        }


        if (prescriptions.size() > 0) {
            TextView scripts = new TextView(this);
            scripts.setText("Active Prescriptions");
            scroll_child.addView(scripts);

            TextView dc = new TextView(this);
            dc.setText("Discontinued Prescriptions");
            scroll_child.addView(dc);

            int position = scroll_child.indexOfChild(scripts) + 1;
            int trigger = 0;

            for (int i = 0; i < prescriptions.size(); i++) {
                Map<String, Object> script = prescriptions.get(i);
                Button current_script = new Button(this);
                current_script.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Map<String, Object> new_script = prescriptions_db.getSingleRow(null, new String[]{"id="+script.get("id")});
                        details(new_script);
                    }
                });

                if ((int)script.get("active") == 0) {
                    current_script.setText(script.get("name") + longs_to_range((long) script.get("start"), (long) script.get("end")));
                    scroll_child.addView(current_script);
                    trigger++;
                }
                else {
                    current_script.setText((String)script.get("name"));
                    scroll_child.addView(current_script, position);
                    position++;
                }
            }

            if (position == scroll_child.indexOfChild(scripts) + 1){
                scroll_child.removeView(scripts);
            }

            if (trigger == 0){
                scroll_child.removeView(dc);
            }

        }


    }

    public void details(Map<String, Object> prescription) {

        wipe(prescription.get("name") + " Details", () -> details(prescription));

        String text = (String) prescription.get("drug");
        text += "\n" + prescription.get("dose");
        text += "\n" + prescription.get("instructions");

        if ((int) prescription.get("as_needed") == 1) {
            text += "\nTo be taken as needed";
        }

        boolean controlled = false;
        Float stash = null;
        if ((int) prescription.get("controlled") == 1) {
            controlled = true;
            text += "\nCONTROLLED";
            if ((int)prescription.get("active") == 1) {
                stash = (float) prescription.get("count");
                text += "\nCurrent Count: " + prescription.get("count");
            }
        }

        if (prescription.get("dose_max") != null) {
            text += "\nMaximum Dose: " + prescription.get("dose_max");
        }

        if (prescription.get("daily_max") != null) {
            text += "\nDaily Maximum: " + prescription.get("daily_max");
        }

        long date = System.currentTimeMillis();
        String date_string = long_to_date(date);
        long datelong = date_to_long(date_string);
        long endlong = date_to_long(next_day(date_string, 1));
        List<Object> taken = entries_db.getSingleColumn("change",
                new String[]{"method='TOOK MEDS'",
                        "prescription_id="+prescription.get("id"),
                        "datetime>="+datelong,
                        "datetime<"+endlong},
                null, false);

        float taken_today = 0;
        for (int j = 0; j < taken.size(); j++){
            taken_today += (Float) taken.get(j);
        }
        text += "\nTaken Today: " + taken_today;

        if (prescription.get("indication") != null) {
            text += "\nIndication: " + prescription.get("indication");
        }

        if (prescription.get("prescriber") != null) {
            text += "\nPrescriber: " + prescription.get("prescriber");
        }

        if (prescription.get("pharmacy") != null) {
            text += "\nPharmacy: " + prescription.get("pharmacy");
        }

        text += "\nThis prescription was started on " + long_to_date((long) prescription.get("start"));

        if ((int)prescription.get("active") != 1) {
            text += "\nThis prescription was discontinued on " + long_to_date((long) prescription.get("end"));
        }

        if ((int)prescription.get("edits") > 0) {
            text += "\nThis prescription's information has been edited " + prescription.get("edits") + " time";
            if ((int)prescription.get("edits") > 1){
                text += "s";
            }
        }

        TextView information = new TextView(this);
        information.setText(text);
        scroll_child.addView(information);


        Button history = new Button(this);
        history.setText("View Medication Log");
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log(prescription);
            }
        });
        scroll_child.addView(history);
        boolean finalControlled = controlled;

        if ((int) prescription.get("active") == 1){
            Button take = new Button(this);
            take.setText("Take Medication");
            take.setTag(false);
            float finalTaken_today = taken_today;
            take.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((Boolean) take.getTag() == false) {
                        take.setTag(true);

                        EditText current_count = new EditText(getApplicationContext());
                        current_count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        final float[] count = {0};
                        current_count.setText(String.valueOf(count[0]));

                        Button add = new Button(getApplicationContext());
                        add.setText("Add");
                        add.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!finalControlled || Float.parseFloat(current_count.getText().toString()) <= (float) prescription.get("count") - 1) {
                                    current_count.setText(String.valueOf(Float.parseFloat(current_count.getText().toString()) + 1));
                                }
                                else {
                                    current_count.setText(String.valueOf((float) prescription.get("count")));
                                }
                            }
                        });

                        add.setLayoutParams(weighted_params);

                        Button subtract = new Button(getApplicationContext());
                        subtract.setText("Subtract");
                        subtract.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (Float.parseFloat(current_count.getText().toString()) >= 1) {
                                    current_count.setText(String.valueOf(Float.parseFloat(current_count.getText().toString()) - 1));
                                }
                                else {
                                    current_count.setText(String.valueOf(0));
                                }
                            }
                        });
                        subtract.setLayoutParams(weighted_params);

                        LinearLayout buttons = new LinearLayout(getApplicationContext());
                        buttons.setOrientation(LinearLayout.HORIZONTAL);
                        buttons.addView(subtract);
                        buttons.addView(add);

                        EditText notes = new EditText(getApplicationContext());
                        notes.setHint("Notes");

                        Button process = new Button(getApplicationContext());
                        process.setText("Confirm Taking Medication");
                        process.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String count_string = current_count.getText().toString();
                                if (count_string.length() > 0 && Float.parseFloat(count_string) > 0) {

                                    if (finalControlled && Float.parseFloat(count_string) > (float) prescription.get("count")) {
                                        warning.setNegativeButton("", null);
                                        warning.setPositiveButton("OK", null);
                                        warning.setTitle("Invalid Count");
                                        warning.setMessage("The client does not have " + count_string + " " + prescription.get("name"));
                                        warning.show();
                                        return;
                                    }

                                    Map<Integer, Float> maxes = new HashMap<>();
                                    float count = Float.parseFloat(count_string);

                                    Float dose_max = null;
                                    if (prescription.get("dose_max") != null){
                                        dose_max = (float) prescription.get("dose_max");
                                    }

                                    Float daily_max = null;
                                    if (prescription.get("daily_max") != null){
                                        daily_max = (float) prescription.get("daily_max");
                                    }

                                    maxes.put((int)prescription.get("id"), daily_max);

                                    final boolean[] dose_override = {false};
                                    final boolean[] daily_override = {false};

                                    String drug = (String)prescription.get("name");

                                    int script_id = (int) prescription.get("id");
                                    List<String> note = new ArrayList<>();
                                    if (notes.getText().toString().length() > 0) {
                                        note.add(notes.getText().toString());
                                    }

                                    List<String> finalNote = note;
                                    Float finalDaily_max = daily_max;
                                    if (dose_max != null && count > dose_max){
                                        warning.setMessage("Taking " + count + " " +
                                                drug + " is more than the maximum dose of " + dose_max);
                                        warning.setTitle("Taking too many " + drug);

                                        warning.setPositiveButton("Override Dose Limit", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dose_override[0] = true;
                                                if (finalDaily_max != null && count + finalTaken_today > finalDaily_max){
                                                    warning.setMessage("Taking " + count + " " +
                                                            drug + " will put the client over the daily limit of " +
                                                            finalDaily_max);
                                                    warning.setTitle("Over the daily limit for " + drug);
                                                    warning.setPositiveButton("Override Daily Limit", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            daily_override[0] = true;
                                                            Map<String,Object> entry = create_entry((int)prescription.get("client_id"), script_id, drug,
                                                                    null, count, null,
                                                                    null, dose_override[0], daily_override[0], 0, "TOOK MEDS",
                                                                    null, null, null, finalNote);

                                                            client_confirm(new ArrayList<>(Arrays.asList(entry)), maxes);
                                                        }
                                                    });
                                                    warning.setNegativeButton("Cancel", null);
                                                    warning.show();
                                                    return;
                                                }
                                                Map<String,Object> entry = create_entry((int)prescription.get("client_id"), script_id, drug, null, count, null,
                                                        null, dose_override[0], daily_override[0], 0, "TOOK MEDS",
                                                        null, null, null, finalNote);

                                                client_confirm(new ArrayList<>(Arrays.asList(entry)), maxes);
                                            }
                                        });
                                        warning.setNegativeButton("Cancel", null);
                                        warning.show();
                                        return;
                                    }
                                    if (finalDaily_max != null && count + finalTaken_today > finalDaily_max){
                                        warning.setMessage("Taking " + count + " " +
                                                drug + " will put the client over the daily limit of " +
                                                finalDaily_max);
                                        warning.setTitle("Over the daily limit for " + drug);
                                        warning.setPositiveButton("Override Daily Limit", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                daily_override[0] = true;
                                                Map<String,Object> entry = create_entry((int)prescription.get("client_id"), script_id, drug, null, count, null,
                                                        null, dose_override[0], daily_override[0], 0, "TOOK MEDS",
                                                        null, null, null, finalNote);

                                                client_confirm(new ArrayList<>(Arrays.asList(entry)), maxes);
                                            }
                                        });
                                        warning.setNegativeButton("Cancel", null);
                                        warning.show();
                                        return;
                                    }

                                    Map<String,Object> entry = create_entry((int)prescription.get("client_id"), script_id, drug, null, count, null,
                                            null, dose_override[0], daily_override[0], 0, "TOOK MEDS",
                                            null, null, null, finalNote);

                                    client_confirm(new ArrayList<>(Arrays.asList(entry)), maxes);
                                }
                                else{
                                    warning.setNegativeButton("", null);
                                    warning.setTitle("Invalid Count");
                                    warning.setMessage("You must input a valid amount of medication to take.");
                                    warning.setPositiveButton("OK", null);
                                    warning.show();
                                }
                            }
                        });

                        scroll_child.addView(current_count, scroll_child.indexOfChild(take) + 1);
                        scroll_child.addView(buttons, scroll_child.indexOfChild(take) + 2);
                        scroll_child.addView(notes, scroll_child.indexOfChild(take) + 3);
                        scroll_child.addView(process, scroll_child.indexOfChild(take) + 4);

                    }
                    else {
                        take.setTag(false);
                        for (int i = 0; i < 4; i++){
                            scroll_child.removeViewAt(scroll_child.indexOfChild(take) + 1);
                        }
                    }
                }
            });
            scroll_child.addView(take);

            Button update = new Button(this);
            update.setText("Update Prescription");
            update.setTag(false);
            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!((Boolean) update.getTag())) {

                        CheckBox as_needed = new CheckBox(getApplicationContext());
                        as_needed.setText("Take As Needed");

                        if ((int)prescription.get("as_needed") == 1) {
                            as_needed.setChecked(true);
                        }


                        final EditText instructions = new EditText(getApplicationContext());
                        instructions.setText((String)prescription.get("instructions"));
                        instructions.setHint("Instructions");

                        final EditText dose_max = new EditText(getApplicationContext());
                        if (prescription.get("dose_max") != null) {
                            dose_max.setText(prescription.get("dose_max").toString());
                        }
                        dose_max.setHint("Maximum Dose");
                        dose_max.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                        final EditText daily_max = new EditText(getApplicationContext());
                        if (prescription.get("daily_max") != null) {
                            daily_max.setText(prescription.get("daily_max").toString());
                        }
                        daily_max.setHint("Daily Maximum");
                        daily_max.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                        final EditText indication = new EditText(getApplicationContext());
                        if (prescription.get("indication") != null) {
                            indication.setText((String)prescription.get("indication"));
                        }
                        indication.setHint("Indication");

                        final EditText prescriber = new EditText(getApplicationContext());
                        if (prescription.get("prescriber")!= null) {
                            prescriber.setText((String)prescription.get("prescriber"));
                        }
                        prescriber.setHint("Prescriber");

                        final EditText pharmacy = new EditText(getApplicationContext());
                        if (prescription.get("pharmacy") != null) {
                            pharmacy.setText((String)prescription.get("pharmacy"));
                        }
                        pharmacy.setHint("Pharmacy");

                        Button confirm = new Button(getApplicationContext());
                        confirm.setText("Confirm Prescription Update");
                        confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                List<String> keys = Arrays.asList("as_needed", "dose", "dose_max", "daily_max", "instructions", "indication", "prescriber", "pharmacy");

                                Float max_dose = null;
                                if (dose_max.getText().toString().length() > 0) {
                                    max_dose = Float.parseFloat(dose_max.getText().toString());
                                }

                                Float max_daily = null;
                                if (daily_max.getText().toString().length() > 0) {
                                    max_daily = Float.parseFloat(daily_max.getText().toString());
                                }

                                boolean prn = false;
                                if (as_needed.isChecked()) {
                                    prn = true;
                                }

                                String reason = null;
                                if (indication.getText().toString().length() > 0) {
                                    reason = indication.getText().toString();
                                }

                                String doctor = null;
                                if (prescriber.getText().toString().length() > 0) {
                                    doctor = prescriber.getText().toString();
                                }

                                String pharm = null;
                                if (pharmacy.getText().toString().length() > 0) {
                                    pharm = pharmacy.getText().toString();
                                }

                                String instruction = instructions.getText().toString();



                                if (instruction.length() == 0){
                                    warning.setNegativeButton("", null);
                                    warning.setPositiveButton("Try Again", null);
                                    warning.setTitle("Invalid Instructions");
                                    warning.setMessage("Instructions is a required field and cannot be left blank");
                                    warning.show();
                                    return;
                                }


                                Map<String, Object> new_prescription = create_prescription(
                                        (int) prescription.get("client_id"),
                                        (String) prescription.get("drug"),
                                        (String) prescription.get("dose"),
                                        max_dose,
                                        max_daily,
                                        instruction,
                                        prn,
                                        finalControlled,
                                        (Float)prescription.get("count"),
                                        reason, doctor, pharm, null
                                );

                                if ((int)prescription.get("as_needed") == 1 && !as_needed.isChecked()){
                                    Map<String, Object> prescription_copy = prescriptions_db.getSingleRow(null, new String[]{"id="+prescription.get("id")});
                                    prescription_copy.put("as_needed", true);
                                    confirm_updates((int)prescription.get("client_id"), Arrays.asList(prescription_copy),
                                            Arrays.asList(new_prescription), keys);

                                }
                                else if ((int)prescription.get("as_needed") == 0 && as_needed.isChecked()){
                                    Map<String, Object> prescription_copy = prescriptions_db.getSingleRow(null, new String[]{"id="+prescription.get("id")});
                                    prescription_copy.put("as_needed", false);
                                    confirm_updates((int)prescription.get("client_id"), Arrays.asList(prescription_copy),
                                            Arrays.asList(new_prescription), keys);

                                }
                                else {
                                    for (int j = 1; j < keys.size(); j++){
                                        if (new_prescription.get(keys.get(j)) == null){
                                            if (prescription.get(keys.get(j)) != null){
                                                Map<String, Object> prescription_copy = prescriptions_db.getSingleRow(null, new String[]{"id="+prescription.get("id")});
                                                prescription_copy.put("as_needed", (int)prescription.get("as_needed") == 1);
                                                confirm_updates((int)prescription.get("client_id"), Arrays.asList(prescription_copy),
                                                        Arrays.asList(new_prescription), keys);
                                                break;
                                            }
                                        }
                                        else if (prescription.get(keys.get(j)) == null && new_prescription.get(keys.get(j)) != null){
                                            Map<String, Object> prescription_copy = prescriptions_db.getSingleRow(null, new String[]{"id="+prescription.get("id")});
                                            prescription_copy.put("as_needed", (int)prescription.get("as_needed") == 1);
                                            confirm_updates((int)prescription.get("client_id"), Arrays.asList(prescription_copy),
                                                    Arrays.asList(new_prescription), keys);

                                            break;
                                        }
                                        else if (!new_prescription.get(keys.get(j)).equals(prescription.get(keys.get(j)))){
                                            Map<String, Object> prescription_copy = prescriptions_db.getSingleRow(null, new String[]{"id="+prescription.get("id")});
                                            prescription_copy.put("as_needed", (int)prescription.get("as_needed") == 1);
                                            confirm_updates((int)prescription.get("client_id"), Arrays.asList(prescription_copy),
                                                    Arrays.asList(new_prescription), keys);

                                            break;
                                        }
                                    }
                                }
                            }
                        });

                        update.setTag(true);
                        scroll_child.addView(as_needed, scroll_child.indexOfChild(update) + 1);
                        scroll_child.addView(instructions, scroll_child.indexOfChild(as_needed) + 1);
                        scroll_child.addView(dose_max, scroll_child.indexOfChild(instructions) + 1);
                        scroll_child.addView(daily_max, scroll_child.indexOfChild(dose_max) + 1);
                        scroll_child.addView(indication, scroll_child.indexOfChild(daily_max) + 1);
                        scroll_child.addView(prescriber, scroll_child.indexOfChild(indication) + 1);
                        scroll_child.addView(pharmacy, scroll_child.indexOfChild(prescriber) + 1);
                        scroll_child.addView(confirm, scroll_child.indexOfChild(pharmacy) + 1);
                    } else {
                        update.setTag(false);
                        for (int i = 0; i < 8; i++){
                            scroll_child.removeViewAt(scroll_child.indexOfChild(update) + 1);
                        }
                    }
                }
            });
            scroll_child.addView(update);

            Float finalStash = stash;
            Button refill = new Button(this);
            refill.setText("Refill Medication");
            refill.setTag(false);
            refill.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((Boolean) refill.getTag() == false) {
                        refill.setTag(true);

                        EditText current_count = new EditText(getApplicationContext());
                        current_count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        current_count.setHint("Refill Amount");

                        Button confirm = new Button(getApplicationContext());
                        confirm.setText("Confirm Medication Refill");
                        confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (finalControlled && current_count.getText().toString().length() == 0){
                                    warning.setNegativeButton("", null);
                                    warning.setTitle("Missing Count");
                                    warning.setMessage("Refills of controlled medications must be counted.");
                                    warning.setPositiveButton("OK", null);
                                    warning.show();
                                }
                                else {
                                    Float count = null;

                                    if (current_count.getText().toString().length() > 0){
                                        if (Float.parseFloat(current_count.getText().toString()) == 0){
                                            warning.setNegativeButton("", null);
                                            warning.setTitle("Invalid Count");
                                            warning.setMessage("0 is not a valid amount of medication to refill.");
                                            warning.setPositiveButton("OK", null);
                                            warning.show();
                                            return;
                                        }
                                        else {
                                            count = Float.parseFloat(current_count.getText().toString());
                                        }
                                    }

                                    Float.parseFloat(current_count.getText().toString());
                                    Map<String, Object> entry = create_entry((int)prescription.get("client_id"), (int)prescription.get("id"),
                                            (String) prescription.get("name"), null, count, null, null,
                                            false, false, 0, "REFILL", null,
                                            null, null, new ArrayList<>());
                                    confirm_refill(Arrays.asList(entry));
                                }
                            }
                        });

                        scroll_child.addView(current_count, scroll_child.indexOfChild(refill) + 1);
                        scroll_child.addView(confirm, scroll_child.indexOfChild(refill) + 2);

                    }
                    else {
                        refill.setTag(false);
                        scroll_child.removeViewAt(scroll_child.indexOfChild(refill) + 1);
                        scroll_child.removeViewAt(scroll_child.indexOfChild(refill) + 1);
                    }
                }
            });
            scroll_child.addView(refill);

            Button discontinue = new Button(this);
            discontinue.setText("Discontinue Prescription");
            discontinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, Object> entry = create_entry((int)prescription.get("client_id"), (int)prescription.get("id"),
                            (String) prescription.get("name"), finalStash, null, null, null,
                            false, false, 0, "PRESCRIPTION DISCONTINUED", null,
                            null, null, new ArrayList<>());

                    confirm_multiple_discontinue(new ArrayList<>(Arrays.asList(entry)));
                }
            });
            scroll_child.addView(discontinue);
        }

        Button adherence = new Button(this);
        adherence.setText("Check Prescription Adherence");
        adherence.setTag(false);
        DecimalFormat df = new DecimalFormat("###.##");
        adherence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(boolean)adherence.getTag()){
                    adherence.setTag(true);

                    TextView start = new TextView(getApplicationContext());
                    start.setText("Start Date");
                    scroll_child.addView(start);

                    DatePicker start_date = new DatePicker(getApplicationContext());
                    start_date.setMinDate((long)prescription.get("start"));
                    scroll_child.addView(start_date);

                    TextView end = new TextView(getApplicationContext());
                    end.setText("End Date");
                    scroll_child.addView(end);

                    DatePicker end_date = new DatePicker(getApplicationContext());
                    end_date.setMinDate((long)prescription.get("start"));
                    scroll_child.addView(end_date);

                    if ((int)prescription.get("active") == 0){
                        start_date.setMaxDate((long)prescription.get("end"));
                        end_date.setMaxDate((long)prescription.get("end"));
                    }
                    else {
                        end_date.setMaxDate(System.currentTimeMillis());
                        start_date.setMaxDate(System.currentTimeMillis());
                    }

                    Button result = new Button(getApplicationContext());
                    result.setText("Survey Says...");

                    Button calculate = new Button(getApplicationContext());
                    calculate.setText("Calculate Adherence");
                    scroll_child.addView(calculate);
                    scroll_child.addView(result);
                    calculate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String start_string = datepicker_to_date(start_date);
                            String end_string = datepicker_to_date(end_date);

                            long start_long = date_to_long(start_string);
                            long end_long = date_to_long(next_day(end_string ,1));

                            if (end_string.equals(long_to_date(System.currentTimeMillis()))){
                                end_long = date_to_long(end_string);
                                end_string = next_day(end_string, -1);
                            }

                            if (start_long >= end_long){
                                warning.setNegativeButton("", null);
                                warning.setPositiveButton("Try Again", null);
                                warning.setTitle("Invalid Date Range");
                                warning.setMessage("The date range you entered is invalid.");
                                warning.show();
                                return;
                            }

                            String text = "";

                            List<Map<String, Object>> entries = entries_db.getRows(new String[]{"id", "change", "dose_override", "daily_override", "datetime"},
                                    new String[]{"prescription_id=" + prescription.get("id"), "method='TOOK MEDS'", "datetime>=" + start_long, "datetime<" + end_long},
                                    new String[]{"datetime", "ASC"}, false);

                            if ((long) prescription.get("start") > start_long) {
                                start_string = next_day(long_to_date((long) prescription.get("start")), 1);
                                start_long = date_to_long(start_string);
                            }

                            float numerator = 0;
                            int daily_override = 0;
                            int dose_override = 0;
                            int days = 1;

                            while (!start_string.equals(end_string)) {
                                days++;
                                start_string = next_day(start_string, 1);
                            }

                            String last_override_day = "";
                            for (int j = 0; j < entries.size(); j++) {
                                Map<String, Object> entry = entries.get(j);

                                numerator += (float) entry.get("change");

                                if ((int) entry.get("dose_override") == 1) {
                                    dose_override++;
                                }

                                if ((int)entry.get("daily_override") == 1 && !long_to_date((long)entry.get("datetime")).equals(last_override_day)){
                                    daily_override++;
                                    last_override_day = long_to_date((long)entry.get("datetime"));
                                }
                            }

                            if (prescription.get("daily_max") == null){
                                text += "No Adherence Guidelines";
                            }
                            else if ((int)prescription.get("as_needed") == 0) {
                                text += df.format(100 * numerator / (days * (float) prescription.get("daily_max"))) + "% adherence";
                            }
                            else {
                                text += "As Needed";
                            }

                            if (dose_override > 0) {
                                text += " | Exceeded dose limit " + dose_override + " time";
                                if (dose_override > 1) {
                                    text += "s";
                                }
                            }
                            if (daily_override > 0) {
                                text += " | Exceeded daily limit " + daily_override + " time";
                                if (daily_override > 1) {
                                    text += "s";
                                }
                            }

                            result.setText(text);

                            long finalTemp_start = start_long;
                            long finalTemp_end = end_long;
                            result.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    med_adherence(entries, (String) prescription.get("name"), finalTemp_start, finalTemp_end, prescription);
                                }
                            });
                        }
                    });
                }
                else{
                    adherence.setTag(false);
                    for (int i = 0; i < 6; i++){
                        scroll_child.removeViewAt(scroll_child.indexOfChild(adherence) + 1);
                    }
                }

            }
        });
        scroll_child.addView(adherence);
    }

    public void log(Map<String, Object> prescription) {
        wipe(prescription.get("name") + " Log", () -> log(prescription));


        List<Map<String, Object>> entries = entries_db.getRows(null, new String[]{"prescription_id="+prescription.get("id")}, new String[]{"datetime", "DESC"}, false);

        List<String> other_methods = Arrays.asList("CLIENT DISCHARGED", "COUNT", "MISCOUNT", "DISCONTINUED DUE TO UPDATE", "PRESCRIPTION DISCONTINUED");
        for (int i = 0; i < entries.size(); i++) {
            Map<String, Object> entry = entries.get(i);
            TextView information = new TextView(this);
            String text = "";
            text += long_to_datetime((long) entry.get("datetime"));
            text += "\n" + entry.get("drug");
            String method = (String)entry.get("method");
            text += "\n" + method;

            if (entry.get("old_count") != null) {
                if (method.equals("COUNT") || method.equals("MISCOUNT")) {
                    text += "\nExpected Count: " + entry.get("old_count");
                }
                else if (other_methods.contains(method)) {
                    text += "\nRemaining Count: " + entry.get("old_count");
                }
                else {
                    text += "\nPrevious Count: " + entry.get("old_count");
                }
            }

            if (entry.get("change") != null) {
                if (method.equals("TOOK MEDS")) {
                    text += "\nChange: -" + entry.get("change");
                }
                else if (!method.equals("COUNT") && !method.equals("MISCOUNT")) {
                    text += "\nChange: +" + entry.get("change");
                }
                else {
                    text += "\nDiscrepancy: " + entry.get("change");
                }
            }

            if (entry.get("new_count") != null) {
                if (method.equals("COUNT") || method.equals("MISCOUNT")) {
                    text += "\nActual Count: " + entry.get("new_count");
                }
                else {
                    text += "\nNew Count: " + entry.get("new_count");
                }
            }

            if ((int) entry.get("dose_override") == 1) {
                text += "\nMAXIMUM DOSE OVERRIDE";
            }

            if ((int) entry.get("daily_override") == 1) {
                text += "\nDAILY MAXIMUM OVERRIDE";
            }

            if ((int) entry.get("edits") > 0) {
                text += "\nEDITED " + entry.get("edits") + " TIME";
                if ((int) entry.get("edits") > 1){
                    text += "S";
                }
            }

            List<String> notes = gson.fromJson((String) entry.get("notes"), new TypeToken<List<String>>(){}.getType());
            for (int j = 0; j < notes.size(); j++) {
                text += "\n" + notes.get(j);
            }

            information.setText(text + "\n");
            scroll_child.addView(information);
        }
    }

    public void take_meds(int client_id) {
        wipe("Take Meds", () -> take_meds(client_id));

        List<Map<String, Object>> prescriptions = prescriptions_db.getRows(
                new String[]{"name", "id", "dose_max", "daily_max", "controlled", "count", "instructions"},
                new String[]{"client_id=" + client_id, "active=1"},
                new String[]{"name", "ASC"}, false);

        for (int i = 0; i < prescriptions.size(); i++) {
            Map<String, Object> script = prescriptions.get(i);

            final Button current_script = new Button(this);
            current_script.setText((String) script.get("name"));
            current_script.setTag(false);
            int finalI = i;
            current_script.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((Boolean) current_script.getTag() == false) {
                        current_script.setTag(true);

                        TextView instructions = new TextView(getApplicationContext());
                        String text = (String) script.get("instructions");

                        boolean controlled = ((int) script.get("controlled") == 1);

                        if (controlled) {
                            text += "\nCount: " + (float) script.get("count");
                        }

                        long date = System.currentTimeMillis();
                        String date_string = long_to_date(date);
                        long datelong = date_to_long(date_string);
                        long endlong = date_to_long(next_day(date_string, 1));
                        List<Object> taken = entries_db.getSingleColumn("change",
                                new String[]{"method='TOOK MEDS'",
                                        "prescription_id="+script.get("id"),
                                        "datetime>="+datelong,
                                        "datetime<"+endlong},
                                null, false);

                        float taken_today = 0;
                        for (int j = 0; j < taken.size(); j++){
                            taken_today += (Float) taken.get(j);
                        }

                        EditText current_count = new EditText(getApplicationContext());
                        current_count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        final float[] count = {0};
                        current_count.setText(String.valueOf(count[0]));

                        if (script.get("dose_max") != null) {
                            text += "\nMaximum Dose: " + (float) script.get("dose_max");
                            instructions.setTag(script.get("dose_max"));
                        }

                        if (script.get("daily_max") != null) {
                            text += "\nDaily Maximum: " + (float) script.get("daily_max");
                            current_count.setTag(script.get("daily_max"));
                        }

                        text += "\nTaken Today: " + taken_today;

                        instructions.setText(text);

                        Button add = new Button(getApplicationContext());
                        add.setText("Add");
                        add.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!controlled || Float.parseFloat(current_count.getText().toString()) <= (float) script.get("count") - 1) {
                                    current_count.setText(String.valueOf(Float.parseFloat(current_count.getText().toString()) + 1));
                                }
                                else {
                                    current_count.setText(String.valueOf((float) script.get("count")));
                                }
                            }
                        });
                        add.setTag(taken_today);
                        add.setLayoutParams(weighted_params);

                        Button subtract = new Button(getApplicationContext());
                        subtract.setText("Subtract");
                        subtract.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (Float.parseFloat(current_count.getText().toString()) >= 1) {
                                    current_count.setText(String.valueOf(Float.parseFloat(current_count.getText().toString()) - 1));
                                }
                                else {
                                    current_count.setText(String.valueOf(0));
                                }
                            }
                        });
                        subtract.setTag(script.get("id"));
                        subtract.setLayoutParams(weighted_params);

                        LinearLayout buttons = new LinearLayout(getApplicationContext());
                        buttons.setOrientation(LinearLayout.HORIZONTAL);
                        buttons.addView(subtract);
                        buttons.addView(add);



                        EditText notes = new EditText(getApplicationContext());
                        notes.setHint("Notes");
                        notes.setTag(finalI);

                        scroll_child.addView(instructions, scroll_child.indexOfChild(current_script) + 1);
                        scroll_child.addView(current_count, scroll_child.indexOfChild(current_script) + 2);
                        scroll_child.addView(buttons, scroll_child.indexOfChild(current_script) + 3);
                        scroll_child.addView(notes, scroll_child.indexOfChild(current_script) + 4);

                    }
                    else {
                        current_script.setTag(false);
                        for (int i = 0; i < 4; i++){
                            scroll_child.removeViewAt(scroll_child.indexOfChild(current_script) + 1);
                        }
                    }
                }
            });
            scroll_child.addView(current_script);
        }
        Button confirm = new Button(this);
        confirm.setText("Confirm Logged Meds");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean taken = false;
                for (int i = 0; i < scroll_child.getChildCount(); i++) {
                    if (scroll_child.getChildAt(i).getTag() != null && (Boolean) scroll_child.getChildAt(i).getTag()) {
                        String count = ((EditText) scroll_child.getChildAt(i + 2)).getText().toString();
                        if (count.length() > 0 && Float.parseFloat(count) > 0) {
                            taken = true;
                            i += 4;
                        }
                        else {
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("OK", null);
                            warning.setTitle("Invalid Medication Amount");
                            Button button = (Button) scroll_child.getChildAt(i);
                            warning.setMessage("You must input a valid amount of " + button.getText() + " to take.");
                            warning.show();
                            return;
                        }
                    }
                }
                if (taken) {
                    clean_entries(client_id, 0, new ArrayList<>(), new HashMap<>());
                }

            }
        });
        scroll_child.addView(confirm);
    }

    public void clean_entries(int client_id, int position, List<Map<String, Object>> entries, Map<Integer, Float> maxes) {

        for (int i = position; i < scroll_child.getChildCount(); i++) {
            if (scroll_child.getChildAt(i).getTag() != null && (Boolean) scroll_child.getChildAt(i).getTag()) {
                String count_string = ((EditText) scroll_child.getChildAt(i + 2)).getText().toString();
                if (count_string.length() > 0 && Float.parseFloat(count_string) > 0) {
                    float count = Float.parseFloat(count_string);
                    float taken_today = (Float) ((LinearLayout)scroll_child.getChildAt(i + 3)).getChildAt(1).getTag();
                    Float dose_max = (Float) (scroll_child.getChildAt(i + 1).getTag());
                    Float daily_max = (Float) (scroll_child.getChildAt(i + 2).getTag());

                    final boolean[] dose_override = {false};
                    final boolean[] daily_override = {false};

                    String drug = (String)((Button) scroll_child.getChildAt(i)).getText();
                    EditText notes = (EditText) scroll_child.getChildAt(i + 4);
                    int script_id = (int) ((LinearLayout)scroll_child.getChildAt(i + 3)).getChildAt(0).getTag();
                    List<String> note = new ArrayList<>();
                    if (notes.getText().toString().length() > 0) {
                        note.add(notes.getText().toString());
                    }

                    maxes.put(script_id, daily_max);

                    if (dose_max != null && count > dose_max){
                        warning.setMessage("Taking " + count + " " +
                                drug + " is more than the maximum dose of " + dose_max);
                        warning.setTitle("Taking too many " + drug);
                        int finalI = i;
                        List<String> finalNote = note;
                        warning.setPositiveButton("Override Dose Limit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dose_override[0] = true;
                                if (daily_max != null && count + taken_today > daily_max){
                                    warning.setMessage("Taking " + count + " " +
                                            drug + " will put the client over the daily limit of " +
                                            daily_max);
                                    warning.setTitle("Over the daily limit for " + drug);
                                    warning.setPositiveButton("Override Daily Limit", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            daily_override[0] = true;
                                            Map<String,Object> entry = create_entry(client_id, script_id, drug, null, count, null,
                                                    null, dose_override[0], daily_override[0], 0, "TOOK MEDS",
                                                    null, null, null, finalNote);
                                            entries.add(entry);
                                            clean_entries(client_id, finalI + 5, entries, maxes);
                                        }
                                    });
                                    warning.setNegativeButton("Cancel", null);
                                    warning.show();
                                    return;
                                }
                                Map<String,Object> entry = create_entry(client_id, script_id, drug, null, count, null,
                                        null, dose_override[0], daily_override[0], 0, "TOOK MEDS",
                                        null, null, null, finalNote);
                                entries.add(entry);
                                clean_entries(client_id, finalI + 5, entries, maxes);
                            }
                        });
                        warning.setNegativeButton("Cancel", null);
                        warning.show();
                        return;
                    }
                    else if (daily_max != null && count + taken_today > daily_max){
                        int finalI = i;
                        List<String> finalNote = note;
                        warning.setMessage("Taking " + count + " " +
                                drug + " will put the client over the daily limit of " +
                                daily_max);
                        warning.setTitle("Over the daily limit for " + drug);
                        warning.setPositiveButton("Override Daily Limit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                daily_override[0] = true;
                                Map<String,Object> entry = create_entry(client_id, script_id, drug, null, count, null,
                                        null, dose_override[0], daily_override[0], 0, "TOOK MEDS",
                                        null, null, null, finalNote);
                                entries.add(entry);
                                clean_entries(client_id, finalI + 5, entries, maxes);
                            }
                        });
                        warning.setNegativeButton("Cancel", null);
                        warning.show();
                        return;
                    }

                    Map<String,Object> entry = create_entry(client_id, script_id, drug, null, count, null,
                            null, dose_override[0], daily_override[0], 0, "TOOK MEDS",
                            null, null, null, note);
                    entries.add(entry);
                }
                i+=4;
            }
        }
        client_confirm(entries, maxes);
    }

    public void client_confirm(List<Map<String,Object>> entries, Map<Integer, Float> maxes) {
        wipe("Client Confirm Medication Entries", () -> client_confirm(entries ,maxes));

        String text = "";

        for (int i = 0; i < entries.size(); i++) {

            Map<String,Object> entry = entries.get(i);

            text += "\n\n" + entries.get(i).get("drug");
            text += "\nAmount Taken: " + entries.get(i).get("change");

            if ((boolean)entry.get("dose_override")){
                text += "\nMAXIMUM DOSE OVERRIDE";
            }

            if ((boolean) entry.get("daily_override")){
                text += "\nDAILY MAXIMUM OVERRIDE";
            }

            List<String> note = gson.fromJson((String) entry.get("notes"), new TypeToken<List<String>>(){}.getType());
            for (int j = 0; j < note.size(); j++) {
                text += "\n" + note.get(j);
            }

        }

        TextView info = new TextView(this);
        info.setText(text.substring(2));
        scroll_child.addView(info);


        final SignatureView signature = create_signature_view();

        final Button confirm = new Button(this);
        confirm.setText("Client Signoff");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] sign = signature.getBytes();
                confirm(entries, sign, maxes);
            }
        });
        final Button clear = new Button(this);
        clear.setText("Clear Canvas");
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signature.ClearCanvas();
            }
        });

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.addView(clear);
        buttons.addView(confirm);
        confirm.setLayoutParams(weighted_params);

        screen.addView(signature);
        screen.addView(buttons);
    }

    public void confirm(List<Map<String,Object>> entries, byte[] client_sign, Map<Integer, Float> maxes) {
        wipe("Staff Confirm Medication Entries", () -> confirm(entries, client_sign, maxes));

        String text = "";

        for (int i = 0; i < entries.size(); i++) {

            Map<String,Object> entry = entries.get(i);

            text += "\n\n" + entries.get(i).get("drug");
            text += "\nAmount Taken: " + entries.get(i).get("change");

            if ((boolean)entry.get("dose_override")){
                text += "\nMAXIMUM DOSE OVERRIDE";
            }

            if ((boolean) entry.get("daily_override")){
                text += "\nDAILY MAXIMUM OVERRIDE";
            }

            List<String> note = gson.fromJson((String) entry.get("notes"), new TypeToken<List<String>>(){}.getType());
            for (int j = 0; j < note.size(); j++) {
                text += "\n" + note.get(j);
            }

        }

        TextView info = new TextView(this);
        info.setText(text.substring(2));
        scroll_child.addView(info);

        final SignatureView signature = create_signature_view();

        if (admin_mode){
            CheckBox manual = new CheckBox(this);
            manual.setText("Manual Date And Time Entry");

            manual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signature.refresh_size(scroll, unweighted_params, weighted_params, signature_size);
                    if (!manual.isChecked()) {
                        scroll_child.removeView(scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1));
                        scroll_child.removeView(scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1));
                    }
                    else {

                        DatePicker date = new DatePicker(getApplicationContext());

                        TimePicker time = new TimePicker(getApplicationContext());

                        scroll_child.addView(date, scroll_child.indexOfChild(manual) + 1);
                        scroll_child.addView(time, scroll_child.indexOfChild(date) + 1);
                    }
                }
            });
            scroll_child.addView(manual);
        }

        Button confirm = new Button(this);
        confirm.setText("Staff Signoff");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String staff_sign = gson.toJson(signature.getBytes());
                String client_signature = gson.toJson(client_sign);

                long time = System.currentTimeMillis();

                CheckBox manual = null;

                boolean double_check = false;
                Map<Integer, Float> counts = new HashMap<>();

                if (admin_mode){
                    manual = (CheckBox) scroll_child.getChildAt(scroll_child.indexOfChild(info) + 1);
                    if (manual.isChecked()){

                        DatePicker date_picker = (DatePicker) scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1);

                        TimePicker time_picker = (TimePicker) scroll_child.getChildAt(scroll_child.indexOfChild(date_picker) + 1);

                        String day = datepicker_to_date(date_picker) + " " + timepicker_to_time(time_picker);

                        time = datetime_to_long(day);

                        double_check = true;
                        String date = long_to_date(time);
                        long start = date_to_long(date);
                        long end = date_to_long(next_day(date, 1));
                        String ids = "(";
                        for (int i = 0; i < entries.size(); i++){
                            ids += "" + entries.get(i).get("prescription_id") + ", ";
                            counts.put((int)entries.get(i).get("prescription_id"), (float)0);
                        }
                        ids = ids.substring(0, ids.length() - 2) + ")";
                        List<Map<String, Object>> taken = entries_db.getRows(
                                new String[]{"id", "change", "prescription_id"},
                                new String[]{"method='TOOK MEDS'", "datetime>="+start, "datetime<"+end, "prescription_id IN "+ids},
                                new String[]{"prescription_id", "ASC"}, false
                        );
                        for (int i = 0; i < taken.size(); i++){
                            Map<String, Object> entry = taken.get(i);
                            counts.put((int)entry.get("prescription_id"), counts.get(entry.get("prescription_id")) + (float)entry.get("change"));
                        }
                    }
                }

                for (int i = 0; i < entries.size(); i++) {
                    Map<String, Object> entry = entries.get(i);
                    entry.put("client_signature", client_signature);
                    entry.put("staff_signature_1", staff_sign);
                    entry.put("datetime", time);
                    Map<String, Object> prescription = prescriptions_db.getSingleRow(new String[]{"controlled", "count"}, new String[]{"id="+entry.get("prescription_id")});
                    if (double_check){
                        if (counts.get(entry.get("prescription_id")) + (float) entry.get("change") > maxes.get(entry.get("prescription_id"))){
                            entry.put("daily_override", true);
                        }
                        List <String> notes = gson.fromJson((String)entry.get("notes"), new TypeToken<List<String>>(){}.getType());
                        notes.add(0, "MANUAL DATE/TIME ENTRY");
                        entry.put("notes", gson.toJson(notes));

                        if ((int) prescription.get("controlled") == 1){

                            long previous_time = (long) entries_db.getObject("MAX([datetime]) AS datetime",
                                    new String[]{"prescription_id="+entry.get("prescription_id"), "datetime<"+time});
                            float previous_count = (float) entries_db.getObject("new_count",
                                    new String[]{"prescription_id="+entry.get("prescription_id"), "datetime="+previous_time});

                            entry.put("old_count", previous_count);
                            entry.put("new_count", previous_count - (float) entry.get("change"));

                            adjust_future_counts((int)entry.get("prescription_id"), -1 * (float) entry.get("change"), time);
                        }
                    }

                    else if ((int) prescription.get("controlled") == 1){
                        float new_count = (float) prescription.get("count") - (float) entry.get("change");
                        entry.put("old_count", prescription.get("count"));
                        entry.put("new_count", new_count);
                        prescription.put("count", new_count);
                        prescriptions_db.update(prescription, new String[]{"id="+entry.get("prescription_id")});
                    }
                    entries_db.addRow(entry);
                }

                for (int i = 0; i < 4; i++){
                    history.remove(0);
                }
                Map<String, Object> client = clients_db.getSingleRow(null, new String[]{"id="+entries.get(0).get("client_id")});
                prescriptions(client);
            }
        });
        final Button clear = new Button(this);
        clear.setText("Clear Canvas");
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signature.ClearCanvas();
            }
        });

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.addView(clear);
        buttons.addView(confirm);
        confirm.setLayoutParams(weighted_params);

        screen.addView(signature);
        screen.addView(buttons);
    }

    public void refill(int client_id) {
        wipe("Log Refills", () -> refill(client_id));

        List<Map<String, Object>> prescriptions = prescriptions_db.getRows(
                new String[]{"name", "id", "controlled", "count"},
                new String[]{"client_id=" + client_id, "active=1"},
                new String[]{"name", "ASC"}, false);

        for (int i = 0; i < prescriptions.size(); i++) {
            Map<String, Object> script = prescriptions.get(i);

            final Button current_script = new Button(this);
            current_script.setText((String) script.get("name"));
            current_script.setTag(false);
            current_script.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((Boolean) current_script.getTag() == false) {
                        current_script.setTag(true);

                        EditText current_count = new EditText(getApplicationContext());
                        current_count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        current_count.setHint("Refill Amount");

                        current_count.setTag((int) script.get("controlled") == 1);
                        scroll_child.addView(current_count, scroll_child.indexOfChild(current_script) + 1);

                    }
                    else {
                        current_script.setTag(false);
                        scroll_child.removeViewAt(scroll_child.indexOfChild(current_script) + 1);
                    }
                }
            });
            scroll_child.addView(current_script);
        }
        Button confirm = new Button(this);
        confirm.setText("Confirm Refilled Meds");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int add = 0;
                List<Map<String, Object>> entries = new ArrayList<>();
                for (int i = 0; i < prescriptions.size(); i++) {
                    if (scroll_child.getChildAt(i + add).getTag() != null && (Boolean) scroll_child.getChildAt(add + i).getTag()) {

                        if (((EditText) scroll_child.getChildAt(i + add + 1)).getText().toString().length() == 0){

                            if ((Boolean)((scroll_child.getChildAt(i + add + 1)).getTag())) {
                                warning.setNegativeButton("", null);
                                warning.setPositiveButton("OK", null);
                                warning.setTitle("No Refill Count Given For " + ((Button)scroll_child.getChildAt(i + add)).getText().toString());
                                warning.setMessage("All controlled medications must be counted when being refilled.");
                                warning.show();
                                return;
                            }
                        }
                        else if (Float.parseFloat(((EditText)scroll_child.getChildAt(i + add + 1)).getText().toString()) == 0) {
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("OK", null);
                            warning.setTitle("Invalid Refill Count Given For " + ((Button)scroll_child.getChildAt(i)).getText().toString());
                            warning.setMessage("0 is not a valid amount for a refill.");
                            warning.show();
                            return;
                        }

                        Float count = null;
                        if (((EditText)scroll_child.getChildAt(i + add + 1)).getText().toString().length() > 0){
                            count = Float.parseFloat(((EditText)scroll_child.getChildAt(i + add + 1)).getText().toString());
                        }

                        Map<String, Object> entry;
                        if ((int)prescriptions.get(i).get("controlled") == 1){
                            entry = create_entry(client_id, (int)prescriptions.get(i).get("id"),
                                    ((Button)scroll_child.getChildAt(i + add)).getText().toString(), (float)prescriptions.get(i).get("count"),
                                    count, (float)prescriptions.get(i).get("count") + count, null,
                                    false, false, 0, "REFILL", null,
                                    null, null, new ArrayList<>());
                        }
                        else {
                            entry = create_entry(client_id, (int)prescriptions.get(i).get("id"),
                                    ((Button)scroll_child.getChildAt(i + add)).getText().toString(), null,
                                    count, null, null,
                                    false, false, 0, "REFILL", null,
                                    null, null, new ArrayList<>());
                        }
                        entries.add(entry);
                        add++;
                    }
                }
                if (entries.size() > 0){
                    confirm_refill(entries);
                }
            }
        });
        scroll_child.addView(confirm);
    }

    public void confirm_refill(List<Map<String, Object>> entries) {
        wipe("Confirm Medication Refills", () -> confirm_refill(entries));

        String text = (String) entries.get(0).get("drug");

        if (entries.get(0).get("change") != null){
            text += "\nAmount Refilled: " + entries.get(0).get("change");
        }

        for (int i = 1; i < entries.size(); i++) {

            Map<String,Object> entry = entries.get(i);

            text += "\n\n" + entry.get("drug");
            if (entry.get("change") != null){
                text += "\nAmount Refilled: " + entry.get("change");
            }
        }

        TextView info = new TextView(this);
        info.setText(text);
        scroll_child.addView(info);

        final SignatureView signature = create_signature_view();

        if (admin_mode){
            CheckBox manual = new CheckBox(this);
            manual.setText("Manual Date And Time Entry");

            manual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signature.refresh_size(scroll, unweighted_params, weighted_params, signature_size);
                    if (!manual.isChecked()) {
                        scroll_child.removeView(scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1));
                        scroll_child.removeView(scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1));
                    }
                    else {

                        DatePicker date = new DatePicker(getApplicationContext());

                        TimePicker time = new TimePicker(getApplicationContext());

                        scroll_child.addView(date, scroll_child.indexOfChild(manual) + 1);
                        scroll_child.addView(time, scroll_child.indexOfChild(date) + 1);
                    }
                }
            });
            scroll_child.addView(manual);
        }

        Button confirm = new Button(this);
        confirm.setText("Staff Signoff");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String staff_sign = gson.toJson(signature.getBytes());

                long time = System.currentTimeMillis();

                boolean double_check = false;
                if (admin_mode){
                    CheckBox manual = (CheckBox) scroll_child.getChildAt(scroll_child.indexOfChild(info) + 1);
                    if (manual.isChecked()){

                        double_check = true;

                        DatePicker date_picker = (DatePicker) scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1);

                        TimePicker time_picker = (TimePicker) scroll_child.getChildAt(scroll_child.indexOfChild(date_picker) + 1);

                        String day = datepicker_to_date(date_picker) + " " + timepicker_to_time(time_picker);

                        time = datetime_to_long(day);
                    }
                }

                for (int i = 0; i < entries.size(); i++) {
                    Map<String, Object> entry = entries.get(i);
                    entry.put("staff_signature_1", staff_sign);
                    entry.put("datetime", time);
                    Map<String, Object> prescription = prescriptions_db.getSingleRow(new String[]{"controlled", "count"}, new String[]{"id="+entry.get("prescription_id")});

                    if (double_check){
                        entry.put("notes", gson.toJson(Arrays.asList("MANUAL DATE/TIME ENTRY")));

                        if ((int) prescription.get("controlled") == 1){

                            long previous_time = (long) entries_db.getObject("MAX([datetime]) AS datetime",
                                    new String[]{"prescription_id="+entry.get("prescription_id"), "datetime<"+time});
                            float previous_count = (float) entries_db.getObject("new_count",
                                    new String[]{"prescription_id="+entry.get("prescription_id"), "datetime="+previous_time});

                            entry.put("old_count", previous_count);
                            entry.put("new_count", previous_count + (float) entry.get("change"));

                            adjust_future_counts((int)entry.get("prescription_id"), (float) entry.get("change"), time);
                        }
                    }

                    else if ((int) prescription.get("controlled") == 1){
                        float new_count = (float) prescription.get("count") + (float) entry.get("change");
                        entry.put("old_count", prescription.get("count"));
                        entry.put("new_count", new_count);
                        prescription.put("count", new_count);
                        prescriptions_db.update(prescription, new String[]{"id="+entry.get("prescription_id")});
                    }

                    entries_db.addRow(entry);
                }

                for (int i = 0; i < 3; i++){
                    history.remove(0);
                }
                Map<String, Object> client = clients_db.getSingleRow(null, new String[]{"id="+entries.get(0).get("client_id")});
                prescriptions(client);
            }
        });
        final Button clear = new Button(this);
        clear.setText("Clear Canvas");
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signature.ClearCanvas();
            }
        });

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.addView(clear);
        buttons.addView(confirm);
        confirm.setLayoutParams(weighted_params);

        screen.addView(signature);
        screen.addView(buttons);
    }

    public void update_multiple(int client_id) {
        wipe("Update Prescriptions", () -> update_multiple(client_id));

        List<Map<String, Object>> prescriptions = prescriptions_db.getRows(null,
                new String[]{"client_id="+client_id, "active=1"},
                new String[]{"name", "ASC"}, false);

        for (int i = 0; i < prescriptions.size(); i++) {
            Map<String, Object> prescription = prescriptions.get(i);

            if ((int)prescription.get("as_needed") == 1) {
                prescription.put("as_needed", true);
            }
            else{
                prescription.put("as_needed", false);
            }

            final Button current_script = new Button(this);
            current_script.setText((String)prescription.get("name"));
            current_script.setTag(false);
            current_script.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!((Boolean) current_script.getTag())) {

                        CheckBox as_needed = new CheckBox(getApplicationContext());
                        as_needed.setText("Take As Needed");

                        if ((boolean)prescription.get("as_needed")) {
                            as_needed.setChecked(true);
                        }

                        final EditText instructions = new EditText(getApplicationContext());
                        instructions.setText((String)prescription.get("instructions"));
                        instructions.setHint("Instructions");

                        final EditText dose_max = new EditText(getApplicationContext());
                        if (prescription.get("dose_max") != null) {
                            dose_max.setText(prescription.get("dose_max").toString());
                        }
                        dose_max.setHint("Maximum Dose");
                        dose_max.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                        final EditText daily_max = new EditText(getApplicationContext());
                        if (prescription.get("daily_max") != null) {
                            daily_max.setText(prescription.get("daily_max").toString());
                        }
                        daily_max.setHint("Daily Maximum");
                        daily_max.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                        final EditText indication = new EditText(getApplicationContext());
                        if (prescription.get("indication") != null) {
                            indication.setText((String)prescription.get("indication"));
                        }
                        indication.setHint("Indication");

                        final EditText prescriber = new EditText(getApplicationContext());
                        if (prescription.get("prescriber")!= null) {
                            prescriber.setText((String)prescription.get("prescriber"));
                        }
                        prescriber.setHint("Prescriber");

                        final EditText pharmacy = new EditText(getApplicationContext());
                        if (prescription.get("pharmacy") != null) {
                            pharmacy.setText((String)prescription.get("pharmacy"));
                        }
                        pharmacy.setHint("Pharmacy");

                        current_script.setTag(true);
                        scroll_child.addView(as_needed, scroll_child.indexOfChild(current_script) + 1);
                        scroll_child.addView(instructions, scroll_child.indexOfChild(as_needed) + 1);
                        scroll_child.addView(dose_max, scroll_child.indexOfChild(instructions) + 1);
                        scroll_child.addView(daily_max, scroll_child.indexOfChild(dose_max) + 1);
                        scroll_child.addView(indication, scroll_child.indexOfChild(daily_max) + 1);
                        scroll_child.addView(prescriber, scroll_child.indexOfChild(indication) + 1);
                        scroll_child.addView(pharmacy, scroll_child.indexOfChild(prescriber) + 1);
                    } else {
                        current_script.setTag(false);
                        for (int i = 0; i < 7; i++){
                            scroll_child.removeViewAt(scroll_child.indexOfChild(current_script) + 1);
                        }
                    }
                }
            });
            scroll_child.addView(current_script);
        }

        final Button change = new Button(this);
        change.setText("Save Updates");
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Map<String, Object>> new_prescriptions = new ArrayList<>();
                List<Map<String, Object>> old_prescriptions = new ArrayList<>();
                List<String> keys = Arrays.asList("dose", "dose_max", "daily_max", "instructions", "as_needed", "indication", "prescriber", "pharmacy");

                int add = 0;
                for (int i = 0; i < prescriptions.size(); i++) {
                    if (scroll_child.getChildAt(i + add).getTag() != null && (Boolean) scroll_child.getChildAt(i + add).getTag()) {
                        Map<String, Object> prescription = prescriptions.get(i);


                        Float max_dose = null;
                        EditText dose_max = (EditText) scroll_child.getChildAt(i + add + 3);
                        if (dose_max.getText().toString().length() > 0) {
                            max_dose = Float.parseFloat(dose_max.getText().toString());
                        }

                        Float max_daily = null;
                        EditText daily_max = (EditText) scroll_child.getChildAt(i + add + 4);
                        if (daily_max.getText().toString().length() > 0) {
                            max_daily = Float.parseFloat(daily_max.getText().toString());
                        }

                        boolean prn = false;
                        CheckBox as_needed = (CheckBox) scroll_child.getChildAt(i + add + 1);
                        if (as_needed.isChecked()) {
                            prn = true;
                        }

                        String reason = null;
                        EditText indication = (EditText) scroll_child.getChildAt(i + add + 5);
                        if (indication.getText().toString().length() > 0) {
                            reason = indication.getText().toString();
                        }

                        String doctor = null;
                        EditText prescriber = (EditText) scroll_child.getChildAt(i + add + 6);
                        if (prescriber.getText().toString().length() > 0) {
                            doctor = prescriber.getText().toString();
                        }

                        String pharm = null;
                        EditText pharmacy = (EditText) scroll_child.getChildAt(i + add + 7);
                        if (pharmacy.getText().toString().length() > 0) {
                            pharm = pharmacy.getText().toString();
                        }

                        String instructions = ((EditText)scroll_child.getChildAt(i + add + 2)).getText().toString();


                        if (instructions.length() == 0){
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("Try Again", null);
                            warning.setTitle("Invalid Instructions");
                            warning.setMessage("Instructions is a required field and cannot be left blank");
                            warning.show();
                            return;
                        }


                        Map<String, Object> new_prescription = create_prescription(
                                (int) prescription.get("client_id"),
                                (String) prescription.get("drug"),
                                (String) prescription.get("dose"),
                                max_dose,
                                max_daily,
                                instructions,
                                prn,
                                (int)prescription.get("controlled") == 1,
                                (Float)prescription.get("count"),
                                reason, doctor, pharm, null
                        );

                        for (int j = 0; j < keys.size(); j++){
                            if (new_prescription.get(keys.get(j)) == null){
                                if (prescription.get(keys.get(j)) != null){
                                    new_prescriptions.add(new_prescription);
                                    old_prescriptions.add(prescription);
                                    break;
                                }
                            }
                            else if (prescription.get(keys.get(j)) == null && new_prescription.get(keys.get(j)) != null){
                                new_prescriptions.add(new_prescription);
                                old_prescriptions.add(prescription);
                                break;
                            }
                            else if (!new_prescription.get(keys.get(j)).equals(prescription.get(keys.get(j)))){
                                new_prescriptions.add(new_prescription);
                                old_prescriptions.add(prescription);
                                break;
                            }
                        }
                        add += 7;
                    }
                }
                if (new_prescriptions.size() > 0){
                    confirm_updates(client_id, old_prescriptions, new_prescriptions, keys);
                }

            }
        });
        scroll_child.addView(change);
    }

    public void confirm_updates(int client_id, List<Map<String, Object>> old_prescriptions, List<Map<String, Object>> new_prescriptions, List<String> keys) {
        wipe("Confirm Medication Updates", () -> confirm_updates(client_id, old_prescriptions, new_prescriptions, keys));

        String text = "";
        List<String> notes = new ArrayList<>();
        for (int i = 0; i < new_prescriptions.size(); i++) {

            String note = "";

            Map<String, Object> new_script = new_prescriptions.get(i);
            Map<String, Object> old_script = old_prescriptions.get(i);

            text += "\n\n" + old_script.get("name");

            for (int j = 0; j < keys.size(); j++){
                String key = keys.get(j);

                if (new_script.get(key) == null){
                    if (old_script.get(key) != null){
                        note += "\nRemoving " + key + ": \"" + old_script.get(key) + "\"";
                    }
                }
                else if (old_script.get(key) == null && new_script.get(key) != null){
                    note += "\nAdding " + key + ": \"" + new_script.get(key) + "\"";                }

                else if (!new_script.get(key).equals(old_script.get(key))){
                    note += "\nChanging " + key + " from \"" + old_script.get(key) +
                            "\" to \"" + new_script.get(key) + "\"";
                }
            }

            text += note;
            notes.add(note.substring(1));
        }

        TextView entry = new TextView(this);
        entry.setText(text.substring(2));
        scroll_child.addView(entry);

        final SignatureView signature = create_signature_view();

        if (admin_mode){
            CheckBox manual = new CheckBox(this);
            manual.setText("Manual Date And Time Entry");

            manual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signature.refresh_size(scroll, unweighted_params, weighted_params, signature_size);
                    if (!manual.isChecked()) {
                        scroll_child.removeView(scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1));
                        scroll_child.removeView(scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1));
                    }
                    else {

                        DatePicker date = new DatePicker(getApplicationContext());

                        TimePicker time = new TimePicker(getApplicationContext());

                        scroll_child.addView(date, scroll_child.indexOfChild(manual) + 1);
                        scroll_child.addView(time, scroll_child.indexOfChild(date) + 1);
                    }
                }
            });
            scroll_child.addView(manual);
        }

        Button confirm = new Button(this);
        confirm.setText("Staff Signoff");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                warning.setNegativeButton("Cancel", null);
                warning.setPositiveButton("Update Prescriptions", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        byte[] staff_sign = signature.getBytes();
                        long time = System.currentTimeMillis();

                        boolean double_check = false;
                        if (admin_mode){
                            CheckBox manual = (CheckBox) scroll_child.getChildAt(scroll_child.indexOfChild(entry) + 1);
                            if (manual.isChecked()){

                                double_check = true;
                                DatePicker date_picker = (DatePicker) scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1);

                                TimePicker time_picker = (TimePicker) scroll_child.getChildAt(scroll_child.indexOfChild(date_picker) + 1);

                                String day = datepicker_to_date(date_picker) + " " + timepicker_to_time(time_picker);

                                time = datetime_to_long(day);
                            }
                        }

                        for (int i = 0; i < new_prescriptions.size(); i++){

                            Map<String, Object> new_script = new_prescriptions.get(i);
                            new_script.put("start", time);

                            Map<String, Object> old_script = old_prescriptions.get(i);
                            old_script.put("end", time);
                            old_script.put("active", false);

                            List<String> note = new ArrayList<>();
                            note.add(notes.get(i));

                            int new_id = prescriptions_db.addRow(new_script);

                            float count = (Float)new_script.get("count");

                            if (double_check){
                                note.add("MANUAL DATE/TIME ENTRY");
                                Float old_dose = null;
                                Float old_daily = null;
                                Float new_dose = null;
                                Float new_daily = null;
                                if (old_script.get("dose_max") != null){
                                    old_dose = (float)old_script.get("dose_max");
                                }
                                if (old_script.get("daily_max") != null){
                                    old_daily = (float)old_script.get("daily_max");
                                }
                                if (new_script.get("dose_max") != null){
                                    new_dose = (float)new_script.get("dose_max");
                                }
                                if (new_script.get("daily_max") != null){
                                    new_daily = (float)new_script.get("daily_max");
                                }

                                count = (float) entries_db.getRows(new String[]{"new_count"}, new String[]{"prescription_id="+old_script.get("id"), "datetime<"+time},
                                        new String[]{"datetime", "DESC"}, false).get(0).get("new_count");

                                swap_prescriptions((int)old_script.get("id"), old_dose, old_daily,
                                        new_id, new_dose, new_daily, time, (String)new_script.get("name"));

                            }

                            Map<String,Object> new_entry = create_entry(client_id,
                                    new_id, (String)new_script.get("name"), (float)0, count, count,
                                    time, false, false, 0,
                                    "UPDATED PRESCRIPTION STARTED", null, gson.toJson(staff_sign),
                                    null, note);

                            Map<String,Object> old_entry = create_entry(client_id,
                                    (int)old_script.get("id"), (String)old_script.get("name"), count, null,
                                    null, time, false, false, 0,
                                    "DISCONTINUED DUE TO UPDATE", null, gson.toJson(staff_sign), null, note);

                            entries_db.addRow(new_entry);
                            entries_db.addRow(old_entry);
                            old_script.put("count", null);
                            prescriptions_db.update(old_script, new String[]{"id="+old_script.get("id")});
                        }
                        for (int i = 0; i < 3; i++){
                            history.remove(0);
                        }
                        prescriptions(clients_db.getSingleRow(null, new String[]{"id="+client_id}));
                    }
                });
                warning.setTitle("Prescription Adjustment");
                warning.setMessage("Making these changes will discontinue the current prescriptions and start new prescriptions. The data will still be saved. Continue?");
                warning.show();
            }
        });
        final Button clear = new Button(this);
        clear.setText("Clear Canvas");
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signature.ClearCanvas();
            }
        });

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.addView(clear);
        buttons.addView(confirm);
        confirm.setLayoutParams(weighted_params);

        screen.addView(signature);
        screen.addView(buttons);
    }

    public void discontinue_multiple(int client_id) {
        wipe("Discontinue Prescriptions", () -> discontinue_multiple(client_id));

        List<Map<String, Object>> prescriptions = prescriptions_db.getRows(
                new String[]{"name", "id", "controlled", "count"},
                new String[]{"client_id=" + client_id, "active=1"},
                new String[]{"name", "ASC"}, false);

        for (int i = 0; i < prescriptions.size(); i++) {
            Map<String, Object> script = prescriptions.get(i);

            final Button current_script = new Button(this);
            current_script.setText((String) script.get("name"));
            current_script.setTag(false);
            current_script.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((Boolean) current_script.getTag() == false) {
                        current_script.setTag(true);

                        EditText current_count = new EditText(getApplicationContext());
                        current_count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        current_count.setEnabled(false);
                        current_count.setHint(script.get("name") + " will be discontinued");

                        current_count.setTag((int) script.get("controlled") == 1);
                        scroll_child.addView(current_count, scroll_child.indexOfChild(current_script) + 1);

                    }
                    else {
                        current_script.setTag(false);
                        scroll_child.removeViewAt(scroll_child.indexOfChild(current_script) + 1);
                    }
                }
            });
            scroll_child.addView(current_script);
        }
        Button confirm = new Button(this);
        confirm.setText("Confirm Discontinued Meds");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int add = 0;
                List<Map<String, Object>> entries = new ArrayList<>();
                for (int i = 0; i < prescriptions.size(); i++) {
                    if (scroll_child.getChildAt(i + add).getTag() != null && (Boolean) scroll_child.getChildAt(add + i).getTag()) {
                        Float count = null;
                        if (prescriptions.get(i).get("count") != null){
                            count = (float) prescriptions.get(i).get("count");
                        }

                        Map<String, Object> entry = create_entry(client_id, (int)prescriptions.get(i).get("id"),
                                ((Button)scroll_child.getChildAt(i + add)).getText().toString(), count, null, null, null,
                                false, false, 0, "PRESCRIPTION DISCONTINUED", null,
                                null, null, new ArrayList<>());
                        entries.add(entry);
                        add++;
                    }
                }
                if (entries.size() > 0){
                    confirm_multiple_discontinue(entries);
                }
            }
        });
        scroll_child.addView(confirm);
    }

    public void confirm_multiple_discontinue(List<Map<String, Object>> entries) {
        wipe("Confirm Medication Discontinuation", () -> confirm_multiple_discontinue(entries));

        String text = (String) entries.get(0).get("drug");

        if (entries.get(0).get("change") != null){
            text += "\nAmount Remaining: " + entries.get(0).get("change");
        }

        for (int i = 1; i < entries.size(); i++) {

            Map<String,Object> entry = entries.get(i);

            text += "\n\n" + entry.get("drug");
            if (entry.get("change") != null){
                text += "\nAmount Remaining: " + entry.get("change");
            }
        }

        TextView info = new TextView(this);
        info.setText(text);
        scroll_child.addView(info);

        final SignatureView signature = create_signature_view();

        if (admin_mode){
            CheckBox manual = new CheckBox(this);
            manual.setText("Manual Date And Time Entry");

            manual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signature.refresh_size(scroll, unweighted_params, weighted_params, signature_size);
                    if (!manual.isChecked()) {
                        scroll_child.removeView(scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1));
                        scroll_child.removeView(scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1));
                    }
                    else {

                        DatePicker date = new DatePicker(getApplicationContext());

                        TimePicker time = new TimePicker(getApplicationContext());

                        scroll_child.addView(date, scroll_child.indexOfChild(manual) + 1);
                        scroll_child.addView(time, scroll_child.indexOfChild(date) + 1);
                    }
                }
            });
            scroll_child.addView(manual);
        }

        Button confirm = new Button(this);
        confirm.setText("Staff Signoff");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                warning.setNegativeButton("Cancel", null);
                warning.setPositiveButton("Discontinue Prescriptions", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String staff_sign = gson.toJson(signature.getBytes());

                        long time = System.currentTimeMillis();

                        boolean double_check = false;

                        if (admin_mode){
                            CheckBox manual = (CheckBox) scroll_child.getChildAt(scroll_child.indexOfChild(info) + 1);
                            if (manual.isChecked()){

                                double_check = true;
                                DatePicker date_picker = (DatePicker) scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1);

                                TimePicker time_picker = (TimePicker) scroll_child.getChildAt(scroll_child.indexOfChild(date_picker) + 1);

                                String day = datepicker_to_date(date_picker) + " " + timepicker_to_time(time_picker);

                                time = datetime_to_long(day);
                            }
                        }

                        for (int i = 0; i < entries.size(); i++) {
                            Map<String, Object> entry = entries.get(i);
                            entry.put("staff_signature_1", staff_sign);
                            entry.put("datetime", time);
                            if (double_check){
                                entry.put("notes", gson.toJson(Arrays.asList("MANUAL DATE/TIME ENTRY")));

                                if ((int)prescriptions_db.getObject("controlled", new String[]{"id="+entry.get("prescription_id")}) == 1){
                                    long previous_time = (long) entries_db.getObject("MAX([datetime]) AS datetime",
                                            new String[]{"prescription_id="+entry.get("prescription_id"), "datetime<"+time});
                                    float previous_count = (float) entries_db.getObject("new_count",
                                            new String[]{"prescription_id="+entry.get("prescription_id"), "datetime="+previous_time});

                                    entry.put("old_count", previous_count);
                                }
                            }
                            entries_db.addRow(entry);

                            Map<String, Object> prescription = new HashMap<>();
                            prescription.put("end", time);
                            prescription.put("active", false);
                            prescription.put("count", null);
                            prescriptions_db.update(prescription, new String[]{"id="+entry.get("prescription_id")});
                        }

                        for (int i = 0; i < 3; i++){
                            history.remove(0);
                        }
                        Map<String, Object> client = clients_db.getSingleRow(null, new String[]{"id="+entries.get(0).get("client_id")});
                        prescriptions(client);
                    }
                });
                warning.setTitle("Discontinuing Prescriptions");
                warning.setMessage("This action will discontinue the selected prescriptions. The data will still be saved. Continue?");
                warning.show();
            }
        });
        final Button clear = new Button(this);
        clear.setText("Clear Canvas");
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signature.ClearCanvas();
            }
        });

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.addView(clear);
        buttons.addView(confirm);
        confirm.setLayoutParams(weighted_params);

        screen.addView(signature);
        screen.addView(buttons);
    }

    public void history(int client_id) {
        wipe("Medication History", () -> history(client_id));

        List<Object> entries = entries_db.getSingleColumn(
                "datetime",
                new String[]{"client_id=" + client_id},
                "DESC", true
        );

        List<String> other_methods = Arrays.asList("CLIENT DISCHARGED", "COUNT", "MISCOUNT", "DISCONTINUED DUE TO UPDATE", "PRESCRIPTION DISCONTINUED");

        for (int i = 0; i < entries.size(); i++) {

            long entry_time = (long)entries.get(i);

            Button button = new Button(this);
            button.setText(long_to_datetime(entry_time));
            button.setTag(false);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!(Boolean) button.getTag()) {
                        button.setTag(true);

                        List<Map<String, Object>> time_entries = entries_db.getRows(
                                new String[]{"prescription_id", "drug", "datetime", "old_count", "change", "new_count", "dose_override", "daily_override", "edits", "method", "notes"},
                                new String[]{"datetime=" + entry_time, "client_id="+client_id},
                                new String[]{"drug", "ASC"}, false
                        );

                        TextView info = new TextView(getApplicationContext());
                        String text = "";
                        for (int i = 0; i < time_entries.size(); i++) {
                            Map<String, Object> entry = time_entries.get(i);
                            text += "\n\n" + entry.get("drug");

                            String method = (String)entry.get("method");

                            text += "\n" + method;

                            if (entry.get("old_count") != null) {
                                if (method.equals("COUNT") || method.equals("MISCOUNT")) {
                                    text += "\nExpected Count: " + entry.get("old_count");
                                }
                                else if (other_methods.contains(method)) {
                                    text += "\nRemaining Count: " + entry.get("old_count");
                                }
                                else {
                                    text += "\nPrevious Count: " + entry.get("old_count");
                                }
                            }

                            if (entry.get("change") != null) {
                                if (method.equals("TOOK MEDS")) {
                                    text += "\nChange: -" + entry.get("change");
                                }
                                else if (!method.equals("COUNT") && !method.equals("MISCOUNT")) {
                                    text += "\nChange: +" + entry.get("change");
                                }
                                else {
                                    text += "\nDiscrepancy: " + entry.get("change");
                                }
                            }

                            if (entry.get("new_count") != null) {
                                if (method.equals("COUNT") || method.equals("MISCOUNT")) {
                                    text += "\nActual Count: " + entry.get("new_count");
                                }
                                else {
                                    text += "\nNew Count: " + entry.get("new_count");
                                }
                            }


                            if ((int)entry.get("dose_override") == 1) {
                                text += "\nDOSE OVERRIDE";
                            }
                            if ((int)entry.get("daily_override") == 1) {
                                text += "\nDAILY OVERRIDE";
                            }

                            if ((int) entry.get("edits") > 0) {
                                text += "\nEDITED " + entry.get("edits") + " TIME";
                                if ((int) entry.get("edits") > 1){
                                    text += "S";
                                }
                            }

                            List<String> notes = gson.fromJson((String) entry.get("notes"), new TypeToken<List<String>>(){}.getType());
                            for (int j = 0; j < notes.size(); j++) {
                                text += "\n" + notes.get(j);
                            }

                        }
                        info.setText(text.substring(2));
                        scroll_child.addView(info, scroll_child.indexOfChild(button) + 1);

                    }
                    else {
                        button.setTag(false);
                        scroll_child.removeViewAt(scroll_child.indexOfChild(button) + 1);
                    }
                }
            });
            scroll_child.addView(button);
        }
    }

    public void client_adherence(int client_id, long start_date) {
        wipe("Overall Medication Adherence", () -> client_adherence(client_id, start_date));

        DecimalFormat df = new DecimalFormat("###.##");

        TextView start_prompt = new TextView(this);
        start_prompt.setText("Start Date");
        scroll_child.addView(start_prompt);

        DatePicker start = new DatePicker(this);
        start.setMaxDate(System.currentTimeMillis());
        start.setMinDate(start_date);
        scroll_child.addView(start);

        TextView end_prompt = new TextView(this);
        end_prompt.setText("End Date");
        scroll_child.addView(end_prompt);

        DatePicker end = new DatePicker(this);
        end.setMaxDate(System.currentTimeMillis());
        end.setMinDate(start_date);
        scroll_child.addView(end);

        TextView active = new TextView(this);
        active.setText("Active Prescriptions");

        TextView discontinued = new TextView(this);
        discontinued.setText("Discontinued Prescriptions");

        List<Map<String, Object>> prescriptions = prescriptions_db.getRows(new String[]{"id", "name", "start", "end", "as_needed", "dose_max", "daily_max", "active", "instructions"},
                new String[]{"client_id="+client_id}, new String[]{"name", "ASC"}, false);

        Button calculate = new Button(this);
        calculate.setText("Calculate Adherence");
        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String start_string = datepicker_to_date(start);
                String end_string = datepicker_to_date(end);


                long start_long = date_to_long(start_string);
                long end_long = date_to_long(next_day(end_string ,1));

                if (end_string.equals(long_to_date(System.currentTimeMillis()))){
                    end_long = date_to_long(end_string);
                }

                if (start_long >= end_long){
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("Try Again", null);
                    warning.setTitle("Invalid Date Range");
                    warning.setMessage("The date range you entered is invalid.");
                    warning.show();
                    return;
                }

                int add = 1;
                int dc_add = 1;

                for (int i = 0; i < prescriptions.size(); i++) {

                    Map<String, Object> prescription = prescriptions.get(i);

                    long temp_start = start_long;
                    long temp_end = end_long;

                    String first = start_string;
                    String last = end_string;

                    Button script;
                    if ((int)prescription.get("active") == 1) {
                        script = (Button) scroll_child.getChildAt(scroll_child.indexOfChild(active) + add);
                        add++;
                        if (last.equals(long_to_date(System.currentTimeMillis()))){
                            last = next_day(end_string, -1);
                        }
                    }
                    else {
                        script = (Button) scroll_child.getChildAt(scroll_child.indexOfChild(discontinued) + dc_add);
                        dc_add++;
                        if ((long) prescription.get("end") < end_long){
                            temp_end = date_to_long(long_to_date((long) prescription.get("end")));
                            last = next_day(long_to_date((long) prescription.get("end")), -1);
                        }
                    }

                    String text = (String) prescription.get("name");

                    if ((int) prescription.get("active") == 0) {
                        text += longs_to_range((long)prescription.get("start"), (long)prescription.get("end"));
                    }

                    List<Map<String, Object>> entries = entries_db.getRows(new String[]{"id", "change", "dose_override", "daily_override", "datetime"},
                            new String[]{"prescription_id="+prescription.get("id"), "method='TOOK MEDS'", "datetime>="+temp_start, "datetime<"+temp_end},
                            new String[]{"datetime", "ASC"}, false);

                    if ((long) prescription.get("start") > start_long){
                        first = next_day(long_to_date((long) prescription.get("start")), 1);
                        temp_start = date_to_long(first);
                    }

                    if (temp_start >= temp_end){
                        text += ": Not Enough Data";
                        script.setText(text);
                        script.setOnClickListener(null);
                        continue;
                    }

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
                        text += ": No Adherence Guidelines";
                    }
                    else if ((int)prescription.get("as_needed") == 0) {
                        text += ": " + df.format(100 * numerator / (days * (float) prescription.get("daily_max"))) + "% adherence";
                    }
                    else {
                        text += ": As Needed";
                    }

                    if (prescription.get("dose_max") == null){
                        prescription.put("dose_override", "No Dose Guidelines");
                    }
                    else {
                        prescription.put("dose_override", dose_override);
                    }

                    if (dose_override > 0) {
                        text += " | Exceeded dose limit " + dose_override + " time";
                        if (dose_override > 1){
                            text += "s";
                        }
                    }
                    if (daily_override > 0) {
                        text += " | Exceeded daily limit " + daily_override + " time";
                        if (daily_override > 1){
                            text += "s";
                        }
                    }

                    script.setText(text);

                    long finalTemp_start = temp_start;
                    long finalTemp_end = temp_end;
                    script.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            med_adherence(entries, (String) prescription.get("name"), finalTemp_start, finalTemp_end, prescription);
                        }
                    });
                }
            }
        });

        scroll_child.addView(calculate);

        scroll_child.addView(active);

        scroll_child.addView(discontinued);

        int position = scroll_child.indexOfChild(active) + 1;
        int trigger = 0;

        for (int i = 0; i < prescriptions.size(); i++) {
            Button script = new Button(this);
            Map<String, Object> prescription = prescriptions.get(i);
            String text = (String)prescription.get("name");
            if ((int) prescription.get("active") == 0) {
                text += longs_to_range((long)prescription.get("start"), (long)prescription.get("end"));
                script.setText(text);
                scroll_child.addView(script);
                trigger++;
            } else {
                script.setText(text);
                scroll_child.addView(script, position);
                position++;
            }
        }

        if (position == scroll_child.indexOfChild(active) + 1){
            scroll_child.removeView(active);
        }
        if (trigger == 0){
            scroll_child.removeView(discontinued);
        }
    }

    public void med_adherence(List<Map<String, Object>> entries, String name, long start, long end, Map<String, Object> prescription) {

        wipe(name + " Adherence Summary", () -> med_adherence(entries, name, start, end, prescription));

        String start_date = long_to_date(start);

        String end_date = long_to_date(end);

        int i = 0;

        while (!start_date.equals(end_date)){

            String text = "";

            float taken = 0;

            while (i < entries.size() && long_to_date((long)entries.get(i).get("datetime")).equals(start_date)){
                Map<String, Object> entry = entries.get(i);
                text += "\nTook " + entry.get("change") + " at " + long_to_time((long)entry.get("datetime"));
                taken += (float) entry.get("change");
                i++;
            }

            if (text.length() > 0){
                text = "\n" + text;
            }

            if (prescription.get("daily_max") != null){
                text = prescription.get("instructions") + "\n\nTook " + taken + " / " + prescription.get("daily_max") + text;
            }
            else {
                text = prescription.get("instructions") + "\n\nTook " + taken + text;
            }

            Button current_date = new Button(this);
            current_date.setText(start_date);
            current_date.setTag(false);
            String finalText = text;
            current_date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((Boolean) current_date.getTag()) {
                        current_date.setTag(false);
                        scroll_child.removeViewAt(scroll_child.indexOfChild(current_date) + 1);
                    } else {
                        current_date.setTag(true);

                        TextView details = new TextView(getApplicationContext());
                        details.setText(finalText);

                        scroll_child.addView(details, scroll_child.indexOfChild(current_date) + 1);
                    }
                }
            });
            scroll_child.addView(current_date);

            start_date = next_day(start_date, 1);

        }
    }

    public void confirm_discharge(int client_id, String name){
        wipe("Confirm Discharge of " + name, () -> confirm_discharge(client_id, name));

        final SignatureView signature = create_signature_view();

        if (admin_mode){
            CheckBox manual = new CheckBox(this);
            manual.setText("Manual Date And Time Entry");

            manual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signature.refresh_size(scroll, unweighted_params, weighted_params, signature_size);
                    if (!manual.isChecked()) {
                        scroll_child.removeView(scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1));
                        scroll_child.removeView(scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1));
                    }
                    else {

                        DatePicker date = new DatePicker(getApplicationContext());

                        TimePicker time = new TimePicker(getApplicationContext());

                        scroll_child.addView(date, scroll_child.indexOfChild(manual) + 1);
                        scroll_child.addView(time, scroll_child.indexOfChild(date) + 1);
                    }
                }
            });
            scroll_child.addView(manual);
        }

        Button confirm = new Button(this);
        confirm.setText("Staff Signoff");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                warning.setNegativeButton("Cancel", null);
                warning.setPositiveButton("Discharge Client", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        byte[] staff_sign = signature.getBytes();

                        long time = System.currentTimeMillis();


                        List<String> notes = new ArrayList<>();

                        if (admin_mode){
                            CheckBox manual = (CheckBox) scroll_child.getChildAt(0);
                            if (manual.isChecked()){


                                DatePicker date_picker = (DatePicker) scroll_child.getChildAt(scroll_child.indexOfChild(manual) + 1);

                                TimePicker time_picker = (TimePicker) scroll_child.getChildAt(scroll_child.indexOfChild(date_picker) + 1);

                                String day = datepicker_to_date(date_picker) + " " + timepicker_to_time(time_picker);

                                time = datetime_to_long(day);

                                notes.add("MANUAL DATE/TIME ENTRY");
                            }
                        }

                        List<Map<String,Object>> prescriptions = prescriptions_db.getRows(
                                new String[]{"id", "name", "count"}, new String[]{"active=1", "client_id="+client_id}, null, false);

                        for (int i = 0; i < prescriptions.size(); i++) {
                            Map<String, Object> prescription = prescriptions.get(i);

                            Map<String, Object> entry = create_entry(client_id, (int)prescription.get("id"),
                                    (String)prescription.get("name"), (Float)prescription.get("count"), null,
                                    null, time, false, false, 0,
                                    "CLIENT DISCHARGED", null, gson.toJson(staff_sign), null, notes);

                            entries_db.addRow(entry);

                            prescription.put("end", time);
                            prescription.put("active", false);
                            prescription.put("count", null);
                            prescriptions_db.update(prescription, new String[]{"id="+prescription.get("id")});
                        }

                        Map<String, Object> client = new HashMap<>();
                        client.put("active", false);
                        client.put("discharge", time);

                        clients_db.update(client, new String[]{"id="+client_id});

                        for (int i = 0; i < 2; i++){
                            history.remove(0);
                        }

                        prescriptions(clients_db.getSingleRow(null, new String[]{"id="+client_id}));
                    }
                });
                warning.setTitle("Confirm Discharge of " + name);
                warning.setMessage("Are you sure you want to mark " + name + " as discharged? The client's medication history will still be saved.");
                warning.show();
            }
        });
        final Button clear = new Button(this);
        clear.setText("Clear Canvas");
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signature.ClearCanvas();
            }
        });

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.addView(clear);
        buttons.addView(confirm);
        confirm.setLayoutParams(weighted_params);

        screen.addView(signature);
        screen.addView(buttons);
    }

    public void spreadsheets() {

        wipe("Create Spreadsheets", this::spreadsheets);

        final List<Map<String, Object>>[] client_list = new List[]{clients_db.getRows(null,
                null, new String[]{"active", "DESC", "name", "ASC", "admit", "ASC"}, false)};

        final List<String>[] client_names = new List[]{new ArrayList<>()};
        client_names[0].add("All Clients");
        for (int i = 0; i < client_list[0].size(); i++){
            Map<String, Object> client = client_list[0].get(i);
            if ((int)client.get("active") == 1){
                client_names[0].add((String) client.get("name"));
            }
            else {
                client_names[0].add(client.get("name") + longs_to_range((long)client.get("admit"), (long)client.get("discharge")));
            }
        }

        final List<Map<String, Object>>[] prescription_list = new List[]{new ArrayList<>()};
        final List<String>[] prescription_names = new List[]{new ArrayList<>()};
        prescription_names[0].add("All Prescriptions");
        prescription_names[0].add("Controlled Medications");

        List<String> date_list = new ArrayList<>();
        date_list.add("All Dates");
        date_list.add("Date Range");
        date_list.add("Single Date");

        int[] chosen_client = {0};
        int[] chosen_prescription = {0};
        String[] chosen_date = {"All Dates"};

        Spinner client_picker = new Spinner(this);
        Spinner prescription_picker = new Spinner(this);
        Spinner date_picker = new Spinner(this);

        Button delete = new Button(this);

        DatePicker single_date = new DatePicker(getApplicationContext());
        DatePicker end_date = new DatePicker(getApplicationContext());

        single_date.setMaxDate(System.currentTimeMillis());
        end_date.setMaxDate(System.currentTimeMillis());

        TextView start = new TextView(getApplicationContext());
        start.setText("Start Date");

        TextView end = new TextView(getApplicationContext());
        end.setText("End Date");

        CheckBox all = new CheckBox(this);
        all.setText("All Data");
        all.setChecked(true);

        CheckBox clients = new CheckBox(this);
        clients.setText("Clients");
        clients.setChecked(true);

        CheckBox prescriptions = new CheckBox(this);
        prescriptions.setText("Prescriptions");
        prescriptions.setChecked(true);

        CheckBox entries = new CheckBox(this);
        entries.setText("Entries");
        entries.setChecked(true);

        CheckBox adherence = new CheckBox(this);
        adherence.setText("Include Adherence Data in Prescription Spreadsheet");
        adherence.setChecked(true);

        ArrayAdapter<String> clientAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, client_names[0]);
        clientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        client_picker.setAdapter(clientAdapter);

        ArrayAdapter<String> prescriptionAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, prescription_names[0]);
        prescriptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prescription_picker.setAdapter(prescriptionAdapter);

        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, date_list);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        date_picker.setAdapter(dateAdapter);

        all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!all.isChecked()){
                    scroll_child.addView(clients, scroll_child.indexOfChild(delete));
                    scroll_child.addView(prescriptions, scroll_child.indexOfChild(delete));
                    scroll_child.addView(entries, scroll_child.indexOfChild(delete));
                    if (clients.isChecked() || prescriptions.isChecked() || entries.isChecked()){
                        scroll_child.addView(client_picker, scroll_child.indexOfChild(delete));
                    }
                    if (prescriptions.isChecked() || entries.isChecked()){
                        scroll_child.addView(prescription_picker, scroll_child.indexOfChild(delete));
                    }
                    if (entries.isChecked()){
                        scroll_child.addView(date_picker, scroll_child.indexOfChild(delete));
                        if (!date_picker.getSelectedItem().equals("All Dates")){
                            scroll_child.addView(single_date, scroll_child.indexOfChild(delete));
                            if (date_picker.getSelectedItem().equals("Date Range")){
                                scroll_child.addView(end_date, scroll_child.indexOfChild(delete));
                                scroll_child.addView(start, scroll_child.indexOfChild(single_date));
                                scroll_child.addView(end, scroll_child.indexOfChild(end));
                            }
                        }
                        if (prescriptions.isChecked()){
                            scroll_child.addView(adherence, scroll_child.indexOfChild(entries) + 1);
                        }
                    }
                }
                else {
                    scroll_child.removeAllViews();
                    scroll_child.addView(all);
                    scroll_child.addView(delete);
                }
            }
        });
        scroll_child.addView(all);

        clients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!prescriptions.isChecked() && !entries.isChecked()){

                    if (clients.isChecked()){
                        scroll_child.addView(client_picker, scroll_child.indexOfChild(entries) + 1);
                    }
                    else {
                        scroll_child.removeView(client_picker);
                    }
                }
            }
        });

        prescriptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!entries.isChecked()){
                    if (prescriptions.isChecked()){
                        if (clients.isChecked()){
                            scroll_child.addView(prescription_picker, scroll_child.indexOfChild(client_picker) + 1);
                        }
                        else {
                            scroll_child.addView(client_picker, scroll_child.indexOfChild(entries) + 1);
                            scroll_child.addView(prescription_picker, scroll_child.indexOfChild(client_picker) + 1);
                        }
                    }
                    else {
                        scroll_child.removeView(prescription_picker);
                        if (!clients.isChecked()){
                            scroll_child.removeView(client_picker);
                        }
                    }
                }
                else {
                    if (prescriptions.isChecked()){
                        scroll_child.addView(adherence, scroll_child.indexOfChild(entries) + 1);
                    }
                    else {
                        scroll_child.removeView(adherence);
                    }
                }
            }
        });

        entries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (entries.isChecked()){
                    scroll_child.addView(date_picker, scroll_child.indexOfChild(delete));
                    if (!date_picker.getSelectedItem().equals("All Dates")){
                        scroll_child.addView(single_date, scroll_child.indexOfChild(delete));
                        if (date_picker.getSelectedItem().equals("Date Range")){
                            scroll_child.addView(end_date, scroll_child.indexOfChild(delete));
                            scroll_child.addView(start, scroll_child.indexOfChild(single_date));
                            scroll_child.addView(end, scroll_child.indexOfChild(end_date));
                        }
                    }
                    if (!prescriptions.isChecked()){
                        scroll_child.addView(prescription_picker, scroll_child.indexOfChild(date_picker));
                        if (!clients.isChecked()){
                            scroll_child.addView(client_picker, scroll_child.indexOfChild(prescription_picker));
                        }
                    }
                    else {
                        scroll_child.addView(adherence, scroll_child.indexOfChild(entries) + 1);
                    }
                }
                else {
                    scroll_child.removeView(date_picker);
                    scroll_child.removeView(single_date);
                    scroll_child.removeView(end_date);
                    scroll_child.removeView(start);
                    scroll_child.removeView(end);
                    if (!prescriptions.isChecked()){
                        scroll_child.removeView(prescription_picker);
                        if (!clients.isChecked()){
                            scroll_child.removeView(client_picker);
                        }
                    }
                    else {
                        scroll_child.removeView(adherence);
                    }
                }
            }
        });

        client_picker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (client_picker.getSelectedItemPosition() == 0 && chosen_client[0] != 0) {
                    chosen_client[0] = 0;

                    prescription_list[0].clear();
                    prescription_names[0].clear();
                    prescription_names[0].add("All Prescriptions");
                    prescription_names[0].add("Controlled Medications");
                    prescription_picker.setSelection(0);
                    prescriptionAdapter.notifyDataSetChanged();
                    chosen_prescription[0] = 0;

                    single_date.setMinDate(-2208960974144L);
                    end_date.setMinDate(-2208960974144L);
                }
                else if (client_picker.getSelectedItemPosition() != chosen_client[0]){

                    chosen_client[0] = client_picker.getSelectedItemPosition();

                    Map<String, Object> client = client_list[0].get(chosen_client[0] - 1);

                    Pair<List<Map<String, Object>>, List<String>> pair = update_prescriptions(
                            (int) client.get("id"),
                            true);
                    prescription_list[0] = pair.first;
                    prescription_names[0].clear();
                    prescription_names[0].addAll(pair.second);
                    prescription_names[0].add(1, "Controlled Medications");
                    prescription_picker.setSelection(0);
                    prescriptionAdapter.notifyDataSetChanged();
                    chosen_prescription[0] = 0;

                    single_date.setMinDate((long)0);
                    end_date.setMinDate((long)0);

                    single_date.setMinDate((long)client.get("admit"));
                    end_date.setMinDate((long)client.get("admit"));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        prescription_picker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (chosen_prescription[0] != prescription_picker.getSelectedItemPosition()){
                    if (prescription_picker.getSelectedItemPosition() <= 1){

                        if (chosen_prescription[0] > 1){
                            Map<String, Object> client = client_list[0].get(chosen_client[0] - 1);

                            single_date.setMinDate((long)0);
                            end_date.setMinDate((long)0);

                            single_date.setMinDate((long)client.get("admit"));
                            end_date.setMinDate((long)client.get("admit"));
                        }

                        chosen_prescription[0] = prescription_picker.getSelectedItemPosition();
                    }

                    else {

                        chosen_prescription[0] = prescription_picker.getSelectedItemPosition();

                        Map<String, Object> prescription = prescription_list[0].get(chosen_prescription[0] - 2);

                        single_date.setMinDate((long)0);
                        end_date.setMinDate((long)0);

                        single_date.setMinDate((long)prescription.get("start"));
                        end_date.setMinDate((long)prescription.get("start"));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        date_picker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!date_picker.getSelectedItem().equals(chosen_date[0])) {
                    scroll_child.removeView(single_date);
                    scroll_child.removeView(end_date);
                    scroll_child.removeView(start);
                    scroll_child.removeView(end);

                    if (date_picker.getSelectedItem().equals("Date Range")) {
                        chosen_date[0] = "Date Range";
                        scroll_child.addView(start, scroll_child.indexOfChild(date_picker) + 1);
                        scroll_child.addView(single_date, scroll_child.indexOfChild(date_picker) + 2);
                        scroll_child.addView(end, scroll_child.indexOfChild(date_picker) + 3);
                        scroll_child.addView(end_date, scroll_child.indexOfChild(date_picker) + 4);
                    }
                    else if(!date_picker.getSelectedItem().equals("All Dates")){
                        chosen_date[0] = "Single Date";
                        scroll_child.addView(single_date, scroll_child.indexOfChild(date_picker) + 1);
                    }
                    else {
                        chosen_date[0] = "All Dates";
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        delete.setText("Generate Spreadsheets");
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!all.isChecked() && !clients.isChecked() && !prescriptions.isChecked() && !entries.isChecked()){
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("OK", null);
                    warning.setTitle("No Data Selected");
                    warning.setMessage("No data was selected to produce spreadsheets.");
                    warning.show();
                }
                else if (chosen_date[0].equals("Date Range") && !datepicker_to_date(end_date).equals(datepicker_to_date(single_date)) &&
                        date_to_long(datepicker_to_date(single_date)) >= date_to_long(datepicker_to_date(end_date))){
                    warning.setNegativeButton("", null);
                    warning.setPositiveButton("OK", null);
                    warning.setTitle("Invalid Date Range");
                    warning.setMessage("The end date cannot be earlier than the start date");
                    warning.show();
                }
                else{
                    warning.setNegativeButton("Cancel", null);
                    warning.setPositiveButton("Generate Spreadsheets", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String file_name = "";
                            Workbook workbook = new HSSFWorkbook();

                            if (all.isChecked() || (clients.isChecked() && prescriptions.isChecked() && entries.isChecked() &&
                                    adherence.isChecked() && client_picker.getSelectedItemPosition() == 0 &&
                                    prescription_picker.getSelectedItemPosition() == 0 && date_picker.getSelectedItemPosition() == 0)){
                                List<Map<String, Object>> client = clients_db.getRows(null, null,
                                        new String[]{"active", "DESC", "name", "ASC", "admit", "ASC"}, false);

                                Sheet clients = client_sheet(workbook, "Clients", client);
                                file_name = "All Data";

                                String previous_client = "";
                                int client_counter = 1;

                                for (int i = 0; i < client.size(); i++){

                                    List<Map<String, Object>> prescription_data = prescriptions_db.getRows(null,
                                            new String[]{"client_id="+client_list[0].get(i).get("id")},
                                            new String[]{"active", "DESC", "name", "ASC", "start", "DESC"}, false);

                                    String sheet_name = client.get(i).get("name") + " Prescriptions";

                                    if (sheet_name.equals(previous_client)){
                                        sheet_name = "(" + client_counter + ") " + sheet_name;
                                        client_counter++;
                                    }
                                    else {
                                        previous_client = sheet_name;
                                        client_counter = 1;
                                    }
                                    String end = null;
                                    if (client.get(i).get("active").equals("Discharged")){
                                        end = (String)client.get(i).get("discharge");
                                    }
                                    else {
                                        end = long_to_date(System.currentTimeMillis());
                                    }
                                    Sheet prescriptions = prescription_sheet(workbook,
                                            sheet_name, prescription_data, (String) client.get(i).get("admit"), end);

                                    String previous_prescription = "";
                                    int prescription_counter = 1;

                                    for (int j = 0; j < prescription_data.size(); j++){

                                        List<Map<String, Object>> entry_data = entries_db.getRows(null,
                                                new String[]{"prescription_id="+prescription_data.get(j).get("id")},
                                                new String[]{"datetime", "ASC", "drug", "ASC", "prescription_id", "ASC"}, false);

                                        String entry_sheet_name = client.get(i).get("name") + " " + prescription_data.get(j).get("name");

                                        if (entry_sheet_name.equals(previous_prescription)){
                                            entry_sheet_name = "(" + prescription_counter + ") " + entry_sheet_name;
                                            prescription_counter++;
                                        }
                                        else {
                                            previous_prescription = entry_sheet_name;
                                            prescription_counter = 1;
                                        }
                                        Sheet entries = entry_sheet(workbook, entry_sheet_name, entry_data);
                                    }
                                }
                            }
                            else {
                                List<Map<String, Object>> client;
                                List<Map<String, Object>> client_copy = null;
                                if (chosen_client[0] != 0) {
                                    client = clients_db.getRows(null,
                                            new String[]{"id="+client_list[0].get(chosen_client[0] - 1).get("id")},
                                            new String[]{"active", "DESC", "name", "ASC"}, false);
                                    file_name += client.get(0).get("name");
                                    if (prescriptions.isChecked() && entries.isChecked() && adherence.isChecked()){
                                        client_copy = clients_db.getRows(null,
                                                new String[]{"id="+client_list[0].get(chosen_client[0] - 1).get("id")},
                                                new String[]{"active", "DESC", "name", "ASC"}, false);
                                    }
                                }
                                else {
                                    client = clients_db.getRows(null, null,
                                            new String[]{"active", "DESC", "name", "ASC"}, false);
                                    file_name += "All Clients";
                                    if (prescriptions.isChecked() && entries.isChecked() && adherence.isChecked()){
                                        client_copy = clients_db.getRows(null, null,
                                                new String[]{"active", "DESC", "name", "ASC"}, false);
                                    }
                                }

                                if (clients.isChecked()){
                                    Sheet clients = client_sheet(workbook, "Clients", client);
                                }

                                if (prescriptions.isChecked() || entries.isChecked()){
                                    String previous_client = "";
                                    int client_counter = 1;

                                    List<String> prescription_constraints = new ArrayList<>();

                                    if (chosen_prescription[0] > 1){
                                        prescription_constraints.add("id="+prescription_list[0].get(chosen_prescription[0] - 2).get("id"));
                                        file_name += " " + prescription_list[0].get(chosen_prescription[0] - 2).get("name");
                                    }
                                    else if (chosen_prescription[0] == 1){
                                        prescription_constraints.add("controlled=1");
                                        file_name += " Controlled";
                                    }
                                    else {
                                        file_name += " All Prescriptions";
                                    }

                                    List<String> entry_constraints = new ArrayList<>();
                                    if (entries.isChecked()) {

                                        if (!chosen_date[0].equals("All Dates")) {
                                            long date = date_to_long(datepicker_to_date(single_date));
                                            long end;
                                            if (chosen_date[0].equals("Date Range") && !datepicker_to_date(end_date).equals(datepicker_to_date(single_date))) {
                                                end = date_to_long(next_day(datepicker_to_date(end_date), 1));
                                                file_name += longs_to_range(date, date_to_long(datepicker_to_date(end_date))).replace("/", "-");
                                            } else {
                                                end = date_to_long(next_day(datepicker_to_date(single_date), 1));
                                                file_name += " " + long_to_date(date).replace("/", "-");
                                            }
                                            entry_constraints.add("datetime>=" + date);
                                            entry_constraints.add("datetime<" + end);
                                        } else {
                                            file_name += " All dates";
                                        }
                                    }

                                    String[] constraints = new String[prescription_constraints.size() + 1];
                                    for (int j = 0; j < prescription_constraints.size(); j++){
                                        constraints[j] = prescription_constraints.get(j);
                                    }

                                    for (int i = 0; i < client.size(); i++){
                                        constraints[prescription_constraints.size()] = "client_id="+client.get(i).get("id");

                                        List<Map<String, Object>> prescription_data = prescriptions_db.getRows(null,
                                                constraints, new String[]{"name", "ASC", "active", "DESC", "start", "DESC"}, false);

                                        if (prescriptions.isChecked()){
                                            String sheet_name = client.get(i).get("name") + " Prescriptions";

                                            if (sheet_name.equals(previous_client)){
                                                sheet_name = "(" + client_counter + ") " + sheet_name;
                                                client_counter++;
                                            }
                                            else {
                                                previous_client = sheet_name;
                                                client_counter = 1;
                                            }

                                            if (adherence.isChecked() && entries.isChecked()){
                                                String date;
                                                String end;
                                                if (!chosen_date[0].equals("All Dates")) {
                                                    date = datepicker_to_date(single_date);
                                                    if (chosen_date[0].equals("Date Range") && !datepicker_to_date(end_date).equals(datepicker_to_date(single_date))) {
                                                        end = next_day(datepicker_to_date(end_date), 1);
                                                    } else {
                                                        end = next_day(datepicker_to_date(single_date), 1);
                                                    }
                                                }
                                                else {
                                                    date = long_to_date((long)client_copy.get(i).get("admit"));
                                                    if ((int) client_copy.get(i).get("active") == 1){
                                                        end = long_to_date(System.currentTimeMillis());
                                                    }
                                                    else {
                                                        end = long_to_date((long) client_copy.get(i).get("discharge"));
                                                    }
                                                }

                                                Sheet prescriptions = prescription_sheet(workbook,
                                                        sheet_name, prescription_data, date, end);
                                            }
                                            else {
                                                Sheet prescriptions = prescription_sheet(workbook,
                                                        sheet_name, prescription_data, null, null);

                                            }

                                        }

                                        if (entries.isChecked()) {
                                            String previous_prescription = "";
                                            int prescription_counter = 1;

                                            String[] filters = new String[entry_constraints.size() + 2];

                                            for (int j = 0; j < entry_constraints.size(); j++){
                                                filters[j] = entry_constraints.get(j);
                                            }

                                            filters[entry_constraints.size()] = "client_id="+client.get(i).get("id");

                                            for (int j = 0; j < prescription_data.size(); j++){

                                                filters[entry_constraints.size() + 1] = "prescription_id=" + prescription_data.get(j).get("id");

                                                List<Map<String, Object>> entry_data = entries_db.getRows(null,
                                                        filters,
                                                        new String[]{"datetime", "ASC", "drug", "ASC", "prescription_id", "ASC"}, false);

                                                Map<String, Object> prescription = prescription_data.get(j);

                                                String entry_sheet_name = client.get(i).get("name") + " " + prescription.get("name");

                                                if (entry_sheet_name.equals(previous_prescription)){
                                                    entry_sheet_name = "(" + prescription_counter + ") " + entry_sheet_name;
                                                    prescription_counter++;
                                                }
                                                else {
                                                    previous_prescription = entry_sheet_name;
                                                    prescription_counter = 1;
                                                }
                                                Sheet entries = entry_sheet(workbook, entry_sheet_name, entry_data);
                                            }
                                        }
                                    }
                                }
                            }

                            Pair<Boolean, String> success = save_xls(workbook, file_name);

                            if (success.first){
                                Toast.makeText(getApplicationContext(), "Spreadsheet Successfully Generated: " + success.second,
                                        Toast.LENGTH_LONG).show();
                            }
                            else {
                                if (!check_folders()){
                                    Toast.makeText(getApplicationContext(), "Error: Necessary folders not present and could not be generated",
                                            Toast.LENGTH_LONG).show();
                                }
                                else {
                                    success = save_xls(workbook, file_name);
                                    if (success.first){
                                        Toast.makeText(getApplicationContext(), "Spreadsheet Successfully Generated: " + success.second,
                                                Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(), "Something went wrong, spreadsheet was not generated",
                                                Toast.LENGTH_LONG).show();

                                    }
                                }
                            }
                        }
                    });
                    warning.setTitle("Confirm Spreadsheet Generation");
                    warning.setMessage("Generate spreadsheets for the selected data? The data will remain in the database and will not be affected. Depending on how much data " +
                            "is selected, the process may take a while.");
                    warning.show();
                }
            }
        });
        scroll_child.addView(delete);
    }

    public void manage(){
        wipe("Manage Backup Data", this::manage);
        List<Long> backup_times =  gson.fromJson(prefs.getString("backup_times", gson.toJson(new ArrayList<>())), new TypeToken<List<Long>>(){}.getType());

        for (int i = 0; i < backup_times.size(); i++){
            Button backup = new Button(this);
            long time = backup_times.get(i);
            backup.setText(long_to_datetime(time));
            int finalI = i;
            backup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    warning.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            warning.setNeutralButton("", null);
                        }
                    });
                    warning.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            warning.setNeutralButton("", null);

                            Map<String, String> client_map = new HashMap<>();
                            client_map.put("name", "TINYTEXT");
                            client_map.put("admit", "BIGINT");
                            client_map.put("active", "BOOLEAN");
                            client_map.put("discharge", "BIGINT");
                            client_map.put("edits", "TINYINT");

                            DatabaseHelper client_backup = new DatabaseHelper(getApplicationContext(),
                                    "clients_" + time,
                                    "clients_" + time,
                                    client_map);


                            Map<String, String> prescriptions_map = new HashMap<>();
                            prescriptions_map.put("client_id", "INT");
                            prescriptions_map.put("drug", "TINYTEXT");
                            prescriptions_map.put("dose", "TINYTEXT");
                            prescriptions_map.put("dose_max", "FLOAT");
                            prescriptions_map.put("daily_max", "FLOAT");
                            prescriptions_map.put("instructions", "TINYTEXT");
                            prescriptions_map.put("as_needed", "BOOLEAN");
                            prescriptions_map.put("controlled", "BOOLEAN");
                            prescriptions_map.put("count", "FLOAT");
                            prescriptions_map.put("indication", "TINYTEXT");
                            prescriptions_map.put("prescriber", "TINYTEXT");
                            prescriptions_map.put("pharmacy", "TINYTEXT");
                            prescriptions_map.put("start", "BIGINT");
                            prescriptions_map.put("end", "BIGINT");
                            prescriptions_map.put("active", "BOOLEAN");
                            prescriptions_map.put("name", "TINYTEXT");
                            prescriptions_map.put("edits", "TINYINT");

                            Map<String, String> entries_map = new HashMap<>();
                            entries_map.put("client_id", "INT");
                            entries_map.put("prescription_id", "INT");
                            entries_map.put("drug", "TINYTEXT");
                            entries_map.put("datetime", "BIGINT");
                            entries_map.put("old_count", "FLOAT");
                            entries_map.put("change", "FLOAT");
                            entries_map.put("new_count", "FLOAT");
                            entries_map.put("dose_override", "BOOLEAN");
                            entries_map.put("daily_override", "BOOLEAN");
                            entries_map.put("edits", "TINYINT");
                            entries_map.put("method", "TINYTEXT");
                            entries_map.put("client_signature", "LONGTEXT");
                            entries_map.put("staff_signature_1", "LONGTEXT");
                            entries_map.put("staff_signature_2", "LONGTEXT");
                            entries_map.put("notes", "LONGTEXT");

                            DatabaseHelper prescription_backup = new DatabaseHelper(getApplicationContext(),
                                    "prescriptions_" + time,
                                    "prescriptions_" + time,
                                    prescriptions_map);

                            DatabaseHelper entry_backup = new DatabaseHelper(getApplicationContext(),
                                    "entries_" + time,
                                    "entries_" + time,
                                    entries_map);

                            Context context = getApplicationContext();

                            context.deleteDatabase(client_backup.delete_database());
                            context.deleteDatabase(prescription_backup.delete_database());
                            context.deleteDatabase(entry_backup.delete_database());

                            scroll_child.removeView(backup);
                            backup_times.remove(finalI);
                            editor.putString("backup_times", gson.toJson(backup_times));
                            editor.apply();
                        }
                    });
                    warning.setPositiveButton("Restore", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            warning.setNeutralButton("", null);
                            Map<String, String> client_map = new HashMap<>();
                            client_map.put("name", "TINYTEXT");
                            client_map.put("admit", "BIGINT");
                            client_map.put("active", "BOOLEAN");
                            client_map.put("discharge", "BIGINT");
                            client_map.put("edits", "TINYINT");

                            DatabaseHelper client_backup = new DatabaseHelper(getApplicationContext(),
                                    "clients_" + time,
                                    "clients_" + time,
                                    client_map);


                            Map<String, String> prescriptions_map = new HashMap<>();
                            prescriptions_map.put("client_id", "INT");
                            prescriptions_map.put("drug", "TINYTEXT");
                            prescriptions_map.put("dose", "TINYTEXT");
                            prescriptions_map.put("dose_max", "FLOAT");
                            prescriptions_map.put("daily_max", "FLOAT");
                            prescriptions_map.put("instructions", "TINYTEXT");
                            prescriptions_map.put("as_needed", "BOOLEAN");
                            prescriptions_map.put("controlled", "BOOLEAN");
                            prescriptions_map.put("count", "FLOAT");
                            prescriptions_map.put("indication", "TINYTEXT");
                            prescriptions_map.put("prescriber", "TINYTEXT");
                            prescriptions_map.put("pharmacy", "TINYTEXT");
                            prescriptions_map.put("start", "BIGINT");
                            prescriptions_map.put("end", "BIGINT");
                            prescriptions_map.put("active", "BOOLEAN");
                            prescriptions_map.put("name", "TINYTEXT");
                            prescriptions_map.put("edits", "TINYINT");

                            Map<String, String> entries_map = new HashMap<>();
                            entries_map.put("client_id", "INT");
                            entries_map.put("prescription_id", "INT");
                            entries_map.put("drug", "TINYTEXT");
                            entries_map.put("datetime", "BIGINT");
                            entries_map.put("old_count", "FLOAT");
                            entries_map.put("change", "FLOAT");
                            entries_map.put("new_count", "FLOAT");
                            entries_map.put("dose_override", "BOOLEAN");
                            entries_map.put("daily_override", "BOOLEAN");
                            entries_map.put("edits", "TINYINT");
                            entries_map.put("method", "TINYTEXT");
                            entries_map.put("client_signature", "LONGTEXT");
                            entries_map.put("staff_signature_1", "LONGTEXT");
                            entries_map.put("staff_signature_2", "LONGTEXT");
                            entries_map.put("notes", "LONGTEXT");

                            DatabaseHelper prescription_backup = new DatabaseHelper(getApplicationContext(),
                                    "prescriptions_" + time,
                                    "prescriptions_" + time,
                                    prescriptions_map);

                            DatabaseHelper entry_backup = new DatabaseHelper(getApplicationContext(),
                                    "entries_" + time,
                                    "entries_" + time,
                                    entries_map);

                            clients_db.reboot();
                            prescriptions_db.reboot();
                            entries_db.reboot();

                            List<Map<String, Object>> clients = client_backup.getRows(null, null, null, false);
                            List<Map<String, Object>> prescriptions = prescription_backup.getRows(null, null, null, false);
                            List<Map<String, Object>> entries = entry_backup.getRows(null, null, null, false);

                            for (int i = 0; i < clients.size(); i++){
                                clients_db.addRow(clients.get(i));
                            }
                            for (int i = 0; i < prescriptions.size(); i++){
                                prescriptions_db.addRow(prescriptions.get(i));
                            }
                            for (int i = 0; i < entries.size(); i++){
                                entries_db.addRow(entries.get(i));
                            }

                            Toast.makeText(getApplicationContext(), "Backup was restored.",
                                    Toast.LENGTH_LONG).show();

                        }
                    });
                    warning.setTitle("Restore or Delete?");
                    warning.setMessage("Would you like to restore all data to how it was at this save point, or would you like to delete this backup from the archive?");
                    warning.show();
                }
            });
            scroll_child.addView(backup);
        }
    }

    public void edit() {

        wipe("Edit Data", this::edit);

        final List<Map<String, Object>>[] client_list = new List[]{clients_db.getRows(null,
                null, new String[]{"active", "DESC", "name", "ASC", "admit", "ASC"}, false)};

        final List<String>[] client_names = new List[]{new ArrayList<>()};

        final List<Map<String, Object>>[] prescription_list = new List[]{new ArrayList<>()};
        final List<String>[] prescription_names = new List[]{new ArrayList<>()};

        final List<String>[] time_list = new List[]{new ArrayList<>()};
        final List<Long>[] datetime_list = new List[]{new ArrayList<>()};

        DatePicker single_date = new DatePicker(getApplicationContext());

        if(client_list[0].size() == 0){
            client_names[0].add("There are no clients to edit");
            prescription_names[0].add("There are no prescriptions to edit");
            time_list[0].add("There are no entries to edit");
        }

        else{
            for (int i = 0; i < client_list[0].size(); i++){
                Map<String, Object> client = client_list[0].get(i);
                if ((int)client.get("active") == 1){
                    client_names[0].add((String) client.get("name"));
                }
                else {
                    client_names[0].add(client.get("name") + longs_to_range((long)client.get("admit"), (long)client.get("discharge")));
                }
            }

            Pair<List<Map<String, Object>>, List<String>> pair = update_prescriptions(
                    (int) client_list[0].get(0).get("id"),
                    false);
            prescription_list[0] = pair.first;
            prescription_names[0] = pair.second;
        }

        String[] chosen_type = {"Client"};
        int[] chosen_client = {0};
        int[] chosen_prescription = {0};

        Spinner type_picker = new Spinner(this);
        Spinner client_picker = new Spinner(this);
        Spinner prescription_picker = new Spinner(this);
        Spinner time_picker = new Spinner(this);

        Button delete = new Button(this);

        single_date.setMaxDate(System.currentTimeMillis());

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(Arrays.asList("Client", "Prescription", "Entry Group", "Single Entry")));
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type_picker.setAdapter(typeAdapter);

        ArrayAdapter<String> clientAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, client_names[0]);
        clientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        client_picker.setAdapter(clientAdapter);

        ArrayAdapter<String> prescriptionAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, prescription_names[0]);
        prescriptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prescription_picker.setAdapter(prescriptionAdapter);

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, time_list[0]);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        time_picker.setAdapter(timeAdapter);

        type_picker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (type_picker.getSelectedItem().equals("Prescription") && !chosen_type[0].equals("Prescription")) {
                    chosen_type[0] = "Prescription";
                    scroll_child.removeAllViews();
                    scroll_child.addView(type_picker);
                    scroll_child.addView(client_picker);
                    scroll_child.addView(prescription_picker);
                    scroll_child.addView(delete);

                } else if (type_picker.getSelectedItem().equals("Entry Group") && !chosen_type[0].equals("Entry Group")) {
                    chosen_type[0] = "Entry Group";
                    scroll_child.removeAllViews();
                    scroll_child.addView(type_picker);
                    scroll_child.addView(client_picker);
                    scroll_child.addView(single_date);
                    scroll_child.addView(time_picker);
                    scroll_child.addView(delete);

                    if (client_list[0].size() > 0){
                        Map<String, Object> client = client_list[0].get(chosen_client[0]);

                        Pair<List<Long>, List<String>> time_pair = update_times(
                                (int) client.get("id"), false, single_date);

                        datetime_list[0] = time_pair.first;
                        time_list[0].clear();
                        time_list[0].addAll(time_pair.second);
                        time_picker.setSelection(0);
                        timeAdapter.notifyDataSetChanged();

                        single_date.setMinDate((long)0);
                        single_date.setMinDate((long) client.get("admit"));
                    }
                }
                else if (type_picker.getSelectedItem().equals("Client") && !chosen_type[0].equals("Client")){
                    chosen_type[0] = "Client";
                    scroll_child.removeAllViews();
                    scroll_child.addView(type_picker);
                    scroll_child.addView(client_picker);
                    scroll_child.addView(delete);
                }
                else if (type_picker.getSelectedItem().equals("Single Entry") && !chosen_type[0].equals("Single Entry")){
                    chosen_type[0] = "Single Entry";
                    scroll_child.removeAllViews();
                    scroll_child.addView(type_picker);
                    scroll_child.addView(client_picker);
                    scroll_child.addView(prescription_picker);
                    scroll_child.addView(single_date);
                    scroll_child.addView(time_picker);
                    scroll_child.addView(delete);

                    if (prescription_list[0].size() > 0){

                        Map<String, Object> prescription = prescription_list[0].get(chosen_prescription[0]);

                        Pair<List<Long>, List<String>> time_pair = update_times(
                                (int) prescription.get("id"), true, single_date);

                        datetime_list[0] = time_pair.first;
                        time_list[0].clear();
                        time_list[0].addAll(time_pair.second);
                        time_picker.setSelection(0);
                        timeAdapter.notifyDataSetChanged();

                        single_date.setMinDate((long)0);
                        single_date.setMinDate((long) prescription.get("start"));
                    }
                    else {
                        datetime_list[0].clear();
                        time_list[0].clear();
                        time_list[0].add("There are no entries to edit");

                        time_picker.setSelection(0);
                        timeAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        scroll_child.addView(type_picker);


        client_picker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (client_picker.getSelectedItemPosition() != chosen_client[0]){

                    chosen_client[0] = client_picker.getSelectedItemPosition();

                    Map<String, Object> client = client_list[0].get(chosen_client[0]);

                    Pair<List<Map<String, Object>>, List<String>> pair = update_prescriptions(
                            (int) client.get("id"),
                            false);
                    prescription_list[0] = pair.first;
                    prescription_names[0].clear();
                    prescription_names[0].addAll(pair.second);
                    prescription_picker.setSelection(0);
                    prescriptionAdapter.notifyDataSetChanged();
                    chosen_prescription[0] = 0;

                    if (type_picker.getSelectedItem().equals("Single Entry")){
                        if (prescription_list[0].size() > 0){

                            Map<String, Object> prescription = prescription_list[0].get(chosen_prescription[0]);

                            Pair<List<Long>, List<String>> time_pair = update_times(
                                    (int) prescription.get("id"), true, single_date);

                            datetime_list[0] = time_pair.first;
                            time_list[0].clear();
                            time_list[0].addAll(time_pair.second);

                            time_picker.setSelection(0);
                            timeAdapter.notifyDataSetChanged();

                            single_date.setMinDate((long)0);
                            single_date.setMinDate((long) prescription.get("start"));
                        }
                        else{
                            datetime_list[0].clear();
                            time_list[0].clear();
                            time_list[0].add("There are no entries to edit");

                            time_picker.setSelection(0);
                            timeAdapter.notifyDataSetChanged();
                        }
                    }
                    else if (type_picker.getSelectedItem().equals("Entry Group")){
                        Pair<List<Long>, List<String>> time_pair = update_times(
                                (int) client.get("id"), false, single_date);

                        datetime_list[0] = time_pair.first;
                        time_list[0].clear();
                        time_list[0].addAll(time_pair.second);

                        time_picker.setSelection(0);
                        timeAdapter.notifyDataSetChanged();

                        single_date.setMinDate((long)0);
                        single_date.setMinDate((long)client.get("admit"));
                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        scroll_child.addView(client_picker);

        prescription_picker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (prescription_picker.getSelectedItemPosition() != chosen_prescription[0]){

                    chosen_prescription[0] = prescription_picker.getSelectedItemPosition();

                    if (type_picker.getSelectedItem().equals("Single Entry")){
                        Map<String, Object> prescription = prescription_list[0].get(prescription_picker.getSelectedItemPosition());

                        Pair<List<Long>, List<String>> time_pair = update_times(
                                (int) prescription.get("id"),
                                true, single_date);

                        datetime_list[0] = time_pair.first;
                        time_list[0].clear();
                        time_list[0].addAll(time_pair.second);

                        time_picker.setSelection(0);
                        timeAdapter.notifyDataSetChanged();

                        single_date.setMinDate((long)0);
                        single_date.setMinDate((long)prescription.get("start"));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        single_date.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                if (type_picker.getSelectedItem().equals("Single Entry")){
                    if (prescription_list[0].size() > 0){

                        Map<String, Object> prescription = prescription_list[0].get(chosen_prescription[0]);

                        Pair<List<Long>, List<String>> time_pair = update_times(
                                (int) prescription.get("id"), true, single_date);

                        datetime_list[0] = time_pair.first;
                        time_list[0].clear();
                        time_list[0].addAll(time_pair.second);

                        time_picker.setSelection(0);
                        timeAdapter.notifyDataSetChanged();

                        single_date.setMinDate((long)0);
                        single_date.setMinDate((long) prescription.get("start"));
                    }
                    else{
                        datetime_list[0].clear();
                        time_list[0].clear();
                        time_list[0].add("There are no entries to edit");

                        time_picker.setSelection(0);
                        timeAdapter.notifyDataSetChanged();
                    }
                }
                else if (type_picker.getSelectedItem().equals("Entry Group")){

                    if (client_list[0].size() > 0){
                        Map<String, Object> client = client_list[0].get(chosen_client[0]);

                        Pair<List<Long>, List<String>> time_pair = update_times(
                                (int) client.get("id"), false, single_date);

                        datetime_list[0] = time_pair.first;
                        time_list[0].clear();
                        time_list[0].addAll(time_pair.second);
                        time_picker.setSelection(0);
                        timeAdapter.notifyDataSetChanged();

                        single_date.setMinDate((long)0);
                        single_date.setMinDate((long) client.get("admit"));
                    }

                }

            }
        });

        delete.setText("Edit Selected Data");
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String type = (String) type_picker.getSelectedItem();

                if (type.equals("Client") && client_list[0].size() > 0) {
                    edit_screen(clients_db.getRows(null, new String[]{"id=" + (int) client_list[0].get(chosen_client[0]).get("id")},
                            null, false), type);
                }
                else if (type.equals("Prescription") && prescription_list[0].size() > 0) {
                    edit_screen(prescriptions_db.getRows(null,
                            new String[]{"id=" + (int) prescription_list[0].get(prescription_picker.getSelectedItemPosition()).get("id")},
                            null, false), type);
                }
                else if (datetime_list[0].size() > 0) {
                    if (type.equals("Entry Group")){
                        edit_screen(entries_db.getRows(null,
                                new String[]{"datetime=" + datetime_list[0].get(time_picker.getSelectedItemPosition()),
                                        "client_id=" + (int) client_list[0].get(chosen_client[0]).get("id")},
                                new String[]{"drug", "ASC"}, false), type);

                    }
                    else if (prescription_list[0].size() > 0){
                        edit_screen(entries_db.getRows(null,
                                new String[]{"datetime=" + datetime_list[0].get(time_picker.getSelectedItemPosition()),
                                        "prescription_id=" + (int) prescription_list[0].get(prescription_picker.getSelectedItemPosition()).get("id")},
                                new String[]{"drug", "ASC"}, false), type);

                    }
                }
            }
        });
        scroll_child.addView(delete);
    }

    public void edit_screen(List<Map<String, Object>> object, String type){

        wipe("Edit " + type, () -> edit_screen(object, type));

        Button edit = new Button(this);
        edit.setText("Save Edited Data");

        if (type.equals("Client")) {

            Map<String, Object> client = object.get(0);

            EditText name = new EditText(this);
            name.setText((String)client.get("name"));
            name.setHint("Full Name");
            scroll_child.addView(name);

            CheckBox active = new CheckBox(this);
            active.setText("Active");
            scroll_child.addView(active);

            CheckBox change = new CheckBox(this);
            change.setText("Change Admission Timestamp: " + long_to_datetime((long)client.get("admit")));
            scroll_child.addView(change);

            CheckBox change_discharge = new CheckBox(this);
            change_discharge.setText("Manually Input Discharge Timestamp");

            int[] yr_mo_day_hr_min = long_to_full_date((long)client.get("admit"));

            DatePicker admit_date = new DatePicker(this);
            admit_date.setMaxDate(System.currentTimeMillis());
            admit_date.updateDate(yr_mo_day_hr_min[0], yr_mo_day_hr_min[1] - 1, yr_mo_day_hr_min[2]);

            TimePicker admit_time = new TimePicker(this);
            admit_time.setHour(yr_mo_day_hr_min[3]);
            admit_time.setMinute(yr_mo_day_hr_min[4]);

            DatePicker discharge_date = new DatePicker(this);
            discharge_date.setMaxDate(System.currentTimeMillis());
            TimePicker discharge_time = new TimePicker(this);

            if ((int)client.get("active") == 1){
                active.setChecked(true);
            }
            else {
                change_discharge.setText("Change Discharge Timestamp: " + long_to_datetime((long)client.get("discharge")));
                scroll_child.addView(change_discharge);

                yr_mo_day_hr_min = long_to_full_date((long)client.get("discharge"));

                discharge_date.updateDate(yr_mo_day_hr_min[0], yr_mo_day_hr_min[1] - 1, yr_mo_day_hr_min[2]);

                discharge_time.setHour(yr_mo_day_hr_min[3]);
                discharge_time.setMinute(yr_mo_day_hr_min[4]);
            }

            active.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (active.isChecked()){
                        scroll_child.removeView(change_discharge);
                        scroll_child.removeView(discharge_date);
                        scroll_child.removeView(discharge_time);
                    }
                    else{
                        scroll_child.addView(change_discharge, scroll_child.indexOfChild(edit));
                        if (change_discharge.isChecked()){
                            scroll_child.addView(discharge_date, scroll_child.indexOfChild(change_discharge) + 1);
                            scroll_child.addView(discharge_time, scroll_child.indexOfChild(change_discharge) + 2);
                        }
                    }
                }
            });

            change_discharge.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!change_discharge.isChecked()){
                        scroll_child.removeView(discharge_date);
                        scroll_child.removeView(discharge_time);
                    }
                    else{
                        scroll_child.addView(discharge_date, scroll_child.indexOfChild(change_discharge) + 1);
                        scroll_child.addView(discharge_time, scroll_child.indexOfChild(change_discharge) + 2);
                    }
                }
            });

            change.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!change.isChecked()){
                        scroll_child.removeView(admit_date);
                        scroll_child.removeView(admit_time);
                    }
                    else{
                        scroll_child.addView(admit_date, scroll_child.indexOfChild(change) + 1);
                        scroll_child.addView(admit_time, scroll_child.indexOfChild(admit_date) + 1);
                    }
                }
            });

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (name.getText().toString().length() > 0){
                        warning.setNegativeButton("Cancel", null);
                        warning.setPositiveButton("Edit Data", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                long time = (long)client.get("admit");
                                if (change.isChecked()){
                                    String day = datepicker_to_date(admit_date) + " " + timepicker_to_time(admit_time);
                                    time = datetime_to_long(day);
                                }

                                Map<String, Object> revised = create_client(name.getText().toString(), time);

                                if (!active.isChecked()){
                                    revised.put("active", false);
                                    long dc_time = System.currentTimeMillis();
                                    if (change_discharge.isChecked()){
                                        String day = datepicker_to_date(discharge_date) + " " + timepicker_to_time(discharge_time);
                                        dc_time = datetime_to_long(day);
                                    }
                                    else if ((int)client.get("active") == 0){
                                        dc_time = (long) client.get("discharge");
                                    }
                                    revised.put("discharge", dc_time);
                                }
                                revised.put("edits", (int) client.get("edits") + 1);

                                clients_db.update(revised, new String[]{"id="+client.get("id")});

                                List<Map<String, Object>> prescriptions = prescriptions_db.getRows(new String[]{"start", "end", "edits", "id", "active", "count"},
                                        new String[]{"client_id="+client.get("id")}, null, false);

                                for (int i = 0; i < prescriptions.size(); i++){
                                    boolean edited = false;
                                    Map<String, Object> prescription = prescriptions.get(i);
                                    List<Map<String, Object>> entries = new ArrayList<>();
                                    if (change.isChecked() && (long)prescription.get("start") == (long)client.get("admit")){
                                        prescription.put("start", time);
                                        edited = true;
                                        Map<String, Object> entry = entries_db.getSingleRow(new String[]{"edits", "id"},
                                                new String[]{"prescription_id="+prescription.get("id"), "method='INTAKE'"});
                                        entry.put("datetime", time);
                                        entry.put("edits", (int)entry.get("edits") + 1);
                                        entries.add(entry);
                                    }
                                    if (!active.isChecked() && change_discharge.isChecked() && (int)prescription.get("active") == 0 &&
                                            (long)prescription.get("end") == (long)client.get("discharge")){
                                        prescription.put("end", revised.get("discharge"));
                                        edited = true;
                                        Map<String, Object> entry = entries_db.getSingleRow(new String[]{"edits", "id"},
                                                new String[]{"prescription_id="+prescription.get("id"), "method='CLIENT DISCHARGED'"});
                                        entry.put("datetime", revised.get("discharge"));
                                        entry.put("edits", (int)entry.get("edits") + 1);
                                        entries.add(entry);
                                    }
                                    if (active.isChecked() && (int)client.get("active") == 0 && (long)prescription.get("end") == (long)client.get("discharge")){
                                        prescription.put("active", true);
                                        prescription.put("end", null);
                                        prescription.put("count", entries_db.getObject("old_count", new String[]{"prescription_id="+prescription.get("id"), "method='CLIENT DISCHARGED'"}));
                                        edited = true;
                                        entries_db.deleteRows(new String[]{"prescription_id="+prescription.get("id"), "method='CLIENT DISCHARGED'"});
                                    }
                                    if (!active.isChecked() && (int)client.get("active") == 1 && (int)prescription.get("active") == 1){
                                        prescription.put("active", false);
                                        prescription.put("end", revised.get("discharge"));
                                        edited = true;
                                        Float count = null;
                                        if (prescription.get("count") != null){
                                            count = (float)prescription.get("count");
                                        }
                                        Map<String, Object> entry = create_entry((int)client.get("id"), (int)prescription.get("id"), (String)prescription.get("name"), count,
                                                null, null, (long)revised.get("discharge"), false, false, 1, "CLIENT DISCHARGED",
                                                null, null, null, Arrays.asList("Client was discharged through editing function by Admin."));
                                        entries_db.addRow(entry);
                                    }
                                    if (edited){
                                        prescription.put("edits", (int) prescription.get("edits") + 1);
                                        prescriptions_db.update(prescription, new String[]{"id="+prescription.get("id")});
                                        for (int j = 0; j < entries.size(); j++){
                                            entries_db.update(entries.get(j), new String[]{"id="+entries.get(j).get("id")});
                                        }
                                    }
                                }

                                history.remove(0);
                                history.remove(0);
                                edit();
                            }
                        });
                        warning.setTitle("Confirm Data Change");
                        warning.setMessage("Are you sure you want to make these changes to the client's information?");
                        warning.show();
                    }
                    else{
                        warning.setNegativeButton("", null);
                        warning.setPositiveButton("OK", null);
                        warning.setTitle("Invalid Name");
                        warning.setMessage("You must enter a valid name for the client");
                        warning.show();
                    }
                }
            });
            scroll_child.addView(edit);

        }
        else if (type.equals("Prescription")) {
            Map<String, Object> prescription = object.get(0);

            final EditText name = new EditText(this);
            name.setText((String)prescription.get("drug"));
            name.setHint("Drug");
            scroll_child.addView(name);

            final EditText dose = new EditText(this);
            dose.setText((String)prescription.get("dose"));
            dose.setHint("Dose");
            scroll_child.addView(dose);

            final EditText instructions = new EditText(this);
            instructions.setText((String)prescription.get("instructions"));
            instructions.setHint("Instructions");
            scroll_child.addView(instructions);

            CheckBox as_needed = new CheckBox(this);
            as_needed.setText("Take As Needed");
            if ((int)prescription.get("as_needed") == 1) {
                as_needed.setChecked(true);
            }
            scroll_child.addView(as_needed);

            CheckBox controlled = new CheckBox(this);
            controlled.setText("Controlled");
            scroll_child.addView(controlled);

            EditText current_count = new EditText(getApplicationContext());
            current_count.setHint("Count");
            current_count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

            if ((int)prescription.get("controlled") == 1) {
                controlled.setChecked(true);
                if ((int)prescription.get("active") == 1){
                    current_count.setText(prescription.get("count").toString());
                    scroll_child.addView(current_count);
                }
                else {
                    float count = (float) entries_db.getObject("old_count", new String[]{"prescription_id="+prescription.get("id"), "datetime="+prescription.get("end")});
                    current_count.setText(String.valueOf(count));
                }
            }

            CheckBox active = new CheckBox(this);

            controlled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!controlled.isChecked()){
                        scroll_child.removeView(current_count);
                    }
                    else if (active.isChecked()){
                        scroll_child.addView(current_count, scroll_child.indexOfChild(controlled) + 1);
                    }
                }
            });

            final EditText dose_max = new EditText(getApplicationContext());
            if (prescription.get("dose_max") != null) {
                dose_max.setText(prescription.get("dose_max").toString());
            }
            dose_max.setHint("Maximum Dose");
            dose_max.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            scroll_child.addView(dose_max);

            final EditText daily_max = new EditText(getApplicationContext());
            if (prescription.get("daily_max") != null) {
                daily_max.setText(prescription.get("daily_max").toString());
            }
            daily_max.setHint("Daily Maximum");
            daily_max.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            scroll_child.addView(daily_max);

            final EditText indication = new EditText(getApplicationContext());
            if (prescription.get("indication") != null) {
                indication.setText((String)prescription.get("indication"));
            }
            indication.setHint("Indication");
            scroll_child.addView(indication);

            final EditText prescriber = new EditText(getApplicationContext());
            if (prescription.get("prescriber")!= null) {
                prescriber.setText((String)prescription.get("prescriber"));
            }
            prescriber.setHint("Prescriber");
            scroll_child.addView(prescriber);

            final EditText pharmacy = new EditText(getApplicationContext());
            if (prescription.get("pharmacy") != null) {
                pharmacy.setText((String)prescription.get("pharmacy"));
            }
            pharmacy.setHint("Pharmacy");
            scroll_child.addView(pharmacy);

            active.setText("Active");
            scroll_child.addView(active);

            CheckBox change = new CheckBox(this);
            change.setText("Change Prescription Start Timestamp: " + long_to_datetime((long)prescription.get("start")));
            scroll_child.addView(change);

            CheckBox change_discharge = new CheckBox(this);
            change_discharge.setText("Manually Input Discontinuation Timestamp");

            int[] yr_mo_day_hr_min = long_to_full_date((long)prescription.get("start"));

            DatePicker admit_date = new DatePicker(this);
            admit_date.updateDate(yr_mo_day_hr_min[0], yr_mo_day_hr_min[1] - 1, yr_mo_day_hr_min[2]);

            TimePicker admit_time = new TimePicker(this);
            admit_time.setHour(yr_mo_day_hr_min[3]);
            admit_time.setMinute(yr_mo_day_hr_min[4]);

            DatePicker discharge_date = new DatePicker(this);
            TimePicker discharge_time = new TimePicker(this);

            if ((int)prescription.get("active") == 1){
                active.setChecked(true);
            }
            else {
                change_discharge.setText("Change Discontinuation Timestamp: " + long_to_datetime((long)prescription.get("end")));
                scroll_child.addView(change_discharge);

                yr_mo_day_hr_min = long_to_full_date((long)prescription.get("end"));

                discharge_date.updateDate(yr_mo_day_hr_min[0], yr_mo_day_hr_min[1] - 1, yr_mo_day_hr_min[2]);

                discharge_time.setHour(yr_mo_day_hr_min[3]);
                discharge_time.setMinute(yr_mo_day_hr_min[4]);
            }

            active.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (active.isChecked()){
                        scroll_child.removeView(change_discharge);
                        scroll_child.removeView(discharge_date);
                        scroll_child.removeView(discharge_time);
                        if (controlled.isChecked()){
                            scroll_child.addView(current_count, scroll_child.indexOfChild(controlled) + 1);
                        }
                    }
                    else{
                        scroll_child.addView(change_discharge, scroll_child.indexOfChild(edit));
                        if (change_discharge.isChecked()){
                            scroll_child.addView(discharge_date, scroll_child.indexOfChild(change_discharge) + 1);
                            scroll_child.addView(discharge_time, scroll_child.indexOfChild(change_discharge) + 2);
                        }
                        scroll_child.removeView(current_count);
                    }
                }
            });

            change_discharge.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!change_discharge.isChecked()){
                        scroll_child.removeView(discharge_date);
                        scroll_child.removeView(discharge_time);
                    }
                    else{
                        scroll_child.addView(discharge_date, scroll_child.indexOfChild(change_discharge) + 1);
                        scroll_child.addView(discharge_time, scroll_child.indexOfChild(change_discharge) + 2);
                    }
                }
            });

            change.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!change.isChecked()){
                        scroll_child.removeView(admit_date);
                        scroll_child.removeView(admit_time);
                    }
                    else{
                        scroll_child.addView(admit_date, scroll_child.indexOfChild(change) + 1);
                        scroll_child.addView(admit_time, scroll_child.indexOfChild(admit_date) + 1);
                    }
                }
            });

            scroll_child.addView(edit);
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (name.getText().toString().length() > 0 && dose.getText().toString().length() > 0 && instructions.getText().toString().length() > 0) {

                        warning.setNegativeButton("Cancel", null);
                        warning.setPositiveButton("Edit Data", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Float max_dose = null;
                                if (dose_max.getText().toString().length() > 0) {
                                    max_dose = Float.parseFloat(dose_max.getText().toString());
                                }

                                Float max_daily = null;
                                if (daily_max.getText().toString().length() > 0) {
                                    max_daily = Float.parseFloat(daily_max.getText().toString());

                                }

                                Boolean prn = false;
                                if (as_needed.isChecked()) {
                                    prn = true;
                                }

                                Boolean control = false;
                                Float count = null;
                                if (controlled.isChecked()) {
                                    control = true;
                                    if (active.isChecked() && current_count.getText().toString().length() > 0) {
                                        count = Float.parseFloat(current_count.getText().toString());
                                    } else if (active.isChecked()){
                                        warning.setNegativeButton("", null);
                                        warning.setPositiveButton("OK", null);
                                        warning.setTitle("Invalid Count");
                                        warning.setMessage("You must enter a valid count for controlled medications");
                                        warning.show();
                                        return;
                                    }
                                    else if ((int)prescription.get("active") == 1){
                                        count = (float) prescription.get("count");
                                    }
                                }

                                String reason = null;
                                if (indication.getText().toString().length() > 0) {
                                    reason = indication.getText().toString();
                                }

                                String doctor = null;
                                if (prescriber.getText().toString().length() > 0) {
                                    doctor = prescriber.getText().toString();
                                }

                                String pharm = null;
                                if (pharmacy.getText().toString().length() > 0) {
                                    pharm = pharmacy.getText().toString();
                                }

                                long time = (long)prescription.get("start");
                                if (change.isChecked()){
                                    String day = datepicker_to_date(admit_date) + " " + timepicker_to_time(admit_time);
                                    time = datetime_to_long(day);
                                }

                                Map<String, Object> revised = create_prescription((int)prescription.get("client_id"), name.getText().toString(),
                                        dose.getText().toString(), max_dose, max_daily, instructions.getText().toString(),
                                        prn, control, count, reason, doctor, pharm, time);

                                if (!active.isChecked()){
                                    revised.put("active", false);
                                    long dc_time = System.currentTimeMillis();
                                    if (change_discharge.isChecked()){
                                        String day = datepicker_to_date(discharge_date) + " " + timepicker_to_time(discharge_time);
                                        dc_time = datetime_to_long(day);
                                    }
                                    else if ((int)prescription.get("active") == 0){
                                        dc_time = (long) prescription.get("end");
                                    }
                                    revised.put("end", dc_time);
                                }
                                revised.put("edits", (int) prescription.get("edits") + 1);

                                prescriptions_db.update(revised, new String[]{"id="+prescription.get("id")});

                                Float new_dose = null;
                                Float new_daily = null;
                                boolean check_dose = false;
                                boolean check_daily = false;

                                if (revised.get("dose_max") != null){
                                    new_dose = (float)revised.get("dose_max");
                                    if (prescription.get("dose_max") == null || (float)revised.get("dose_max") != (float)prescription.get("dose_max")){
                                        check_dose = true;
                                    }
                                }
                                if (revised.get("daily_max") != null){
                                    new_daily = (float)revised.get("daily_max");
                                    if (prescription.get("daily_max") == null || (float)revised.get("daily_max") != (float)prescription.get("daily_max")){
                                        check_daily = true;
                                    }
                                }

                                Long start_time = null;
                                if (change.isChecked()){
                                    start_time = time;
                                }

                                Boolean active_bool = null;
                                Long end_time = null;
                                if (!active.isChecked() && (int)prescription.get("active") == 1){
                                    active_bool = false;
                                    end_time = (long)revised.get("end");
                                }
                                else if (active.isChecked() && (int)prescription.get("active") == 0){
                                    active_bool = true;
                                }

                                if (!active.isChecked() && change_discharge.isChecked()){
                                    end_time = (long)revised.get("end");
                                }

                                Boolean switch_controlled = false;
                                if (control && (int)prescription.get("controlled") == 0){
                                    switch_controlled = true;
                                }

                                Float real_count = count;
                                Float change = null;
                                Float old_count = null;
                                Long count_time = null;
                                boolean reactivate = false;
                                if (control && count != null  && (int)prescription.get("controlled") == 1){
                                    if ((int)prescription.get("active") == 1){
                                        if (count != (float)prescription.get("count")){
                                            old_count = (float)prescription.get("count");
                                            change = real_count - old_count;
                                            if (active.isChecked()){
                                                count_time = System.currentTimeMillis();
                                            }
                                            else {
                                                count_time = (long)revised.get("end");
                                            }
                                        }
                                    }
                                    else {
                                        float previous_count = (float) entries_db.getObject("old_count", new String[]{"prescription_id="+prescription.get("id"), "datetime="+prescription.get("end")});
                                        if (count != previous_count){
                                            old_count = previous_count;
                                            change = real_count - old_count;
                                            if (active.isChecked()){
                                                count_time = System.currentTimeMillis();
                                            }
                                            else {
                                                count_time = (long)revised.get("end");
                                            }
                                            reactivate = true;
                                        }
                                    }
                                }

                                if (count_time == null && switch_controlled){
                                    if (active.isChecked()){
                                        count_time = System.currentTimeMillis();
                                    }
                                    else {
                                        count_time = (long)revised.get("end");
                                    }
                                }

                                edit_entries((int)prescription.get("client_id"), (int)prescription.get("id"), new_dose, new_daily,
                                        start_time, end_time, count_time, (String)revised.get("name"), active_bool, switch_controlled,
                                        real_count, change, old_count, check_dose, check_daily, reactivate);

                                history.remove(0);
                                history.remove(0);
                                edit();
                            }
                        });
                        warning.setTitle("Confirm Data Change");
                        warning.setMessage("Are you sure you want to make these changes to the prescription?");
                        warning.show();
                    }
                    else{
                        warning.setNegativeButton("", null);
                        warning.setPositiveButton("OK", null);
                        warning.setTitle("Missing Information");
                        warning.setMessage("Drug, dose, and instructions are required fields");
                        warning.show();
                    }
                }
            });
        }

        else {

            List<String> other_methods = Arrays.asList("CLIENT DISCHARGED", "COUNT", "MISCOUNT", "DISCONTINUED DUE TO UPDATE", "PRESCRIPTION DISCONTINUED");

            for (int i = 0; i < object.size(); i++) {
                Map<String, Object> entry = object.get(i);

                Button button = new Button(this);

                if (entry.get("method").equals("UPDATED PRESCRIPTION STARTED")){
                    button.setText(entry.get("drug") + " (Updated)");
                }
                else if (entry.get("method").equals("DISCONTINUED DUE TO UPDATE")){
                    button.setText(entry.get("drug") + " (Discontinued)");
                }
                else {
                    button.setText((String)entry.get("drug"));
                }
                button.setTag(false);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!(Boolean) button.getTag()) {
                            button.setTag(true);

                            LinearLayout stuff = new LinearLayout(getApplicationContext());
                            stuff.setOrientation(LinearLayout.VERTICAL);
                            scroll_child.addView(stuff, scroll_child.indexOfChild(button) + 1);

                            TextView info = new TextView(getApplicationContext());
                            String text = (String)entry.get("drug");

                            String method = (String)entry.get("method");

                            text += "\n" + method;

                            if (entry.get("old_count") != null) {
                                if (method.equals("COUNT") || method.equals("MISCOUNT")) {
                                    text += "\nExpected Count: " + entry.get("old_count");
                                }
                                else if (other_methods.contains(method)) {
                                    text += "\nRemaining Count: " + entry.get("old_count");
                                }
                                else {
                                    text += "\nPrevious Count: " + entry.get("old_count");
                                }
                            }

                            if (entry.get("change") != null) {
                                if (method.equals("TOOK MEDS")) {
                                    text += "\nChange: -" + entry.get("change");
                                }
                                else if (!method.equals("COUNT") && !method.equals("MISCOUNT")) {
                                    text += "\nChange: +" + entry.get("change");
                                }
                                else {
                                    text += "\nDiscrepancy: " + entry.get("change");
                                }
                            }

                            if (entry.get("new_count") != null) {
                                if (method.equals("COUNT") || method.equals("MISCOUNT")) {
                                    text += "\nActual Count: " + entry.get("new_count");
                                }
                                else {
                                    text += "\nNew Count: " + entry.get("new_count");
                                }
                            }


                            if ((int)entry.get("dose_override") == 1) {
                                text += "\nDOSE OVERRIDE";
                            }
                            if ((int)entry.get("daily_override") == 1) {
                                text += "\nDAILY OVERRIDE";
                            }

                            if ((int) entry.get("edits") > 0) {
                                text += "\nEDITED " + entry.get("edits") + " TIME";
                                if ((int) entry.get("edits") > 1){
                                    text += "S";
                                }
                            }

                            List<String> notes = gson.fromJson((String) entry.get("notes"), new TypeToken<List<String>>(){}.getType());
                            for (int j = 0; j < notes.size(); j++) {
                                text += "\n" + notes.get(j);
                            }
                            info.setText(text);
                            stuff.addView(info);

                            CheckBox delete = new CheckBox(getApplicationContext());
                            delete.setText("Delete This Entry");
                            stuff.addView(delete);

                            if (entry.get("method").equals("TOOK MEDS") || entry.get("method").equals("REFILL") || entry.get("method").equals("INTAKE") ||
                                    entry.get("method").equals("PRESCRIPTION STARTED") || entry.get("method").equals("UPDATED PRESCRIPTION STARTED")){
                                CheckBox change = new CheckBox(getApplicationContext());
                                if (entry.get("method").equals("TOOK MEDS")){
                                    change.setText("Change Taken Amount");
                                }
                                else if (entry.get("method").equals("REFILL")){
                                    change.setText("Change Refilled Amount");
                                }
                                else {
                                    change.setText("Change Starting Amount");
                                }
                                change.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (change.isChecked()){

                                            EditText count = new EditText(getApplicationContext());
                                            count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                                            if (entry.get("method").equals("TOOK MEDS")){
                                                count.setHint("Taken Amount");
                                            }
                                            else if (entry.get("method").equals("REFILL")){
                                                count.setHint("Refilled Amount");
                                            }
                                            else {
                                                count.setHint("Starting Amount");
                                            }
                                            if (entry.get("change") != null){
                                                count.setText(String.valueOf((float)entry.get("change")));
                                            }
                                            stuff.addView(count);
                                        }
                                        else {
                                            stuff.removeViewAt(stuff.indexOfChild(change) + 1);
                                        }

                                    }
                                });
                                stuff.addView(change);
                            }
                            else if (entry.get("method").equals("COUNT") || entry.get("method").equals("MISCOUNT")){
                                CheckBox change = new CheckBox(getApplicationContext());
                                change.setText("Change Counted Amount");
                                change.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (change.isChecked()){

                                            EditText count = new EditText(getApplicationContext());
                                            count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                                            count.setHint("Counted Amount");
                                            count.setText(String.valueOf((float)entry.get("new_count")));
                                            stuff.addView(count);
                                        }
                                        else {
                                            stuff.removeViewAt(stuff.indexOfChild(change) + 1);
                                        }

                                    }
                                });
                                stuff.addView(change);
                            }
                        }
                        else {
                            button.setTag(false);
                            scroll_child.removeViewAt(scroll_child.indexOfChild(button) + 1);
                        }
                    }
                });
                scroll_child.addView(button);
            }

            CheckBox add_new = new CheckBox(this);
            List<Map<String, Object>> possible = null;

            if (type.equals("Entry Group") && (object.get(0).get("method").equals("TOOK MEDS") || object.get(0).get("method").equals("REFILL"))){

                possible = prescriptions_db.getRows(null,
                        new String[]{"client_id="+object.get(0).get("client_id")}, new String[]{"name", "ASC"}, false);
                List<Integer> ids = new ArrayList<>();
                for (int i = 0; i < object.size(); i++){
                    ids.add((Integer)object.get(i).get("prescription_id"));
                }
                for (int i = possible.size() - 1; i > -1; i--){
                    if (ids.contains(possible.get(i).get("id")) ||
                            (long) possible.get(i).get("start") > (long)object.get(0).get("datetime") ||
                            ((int)possible.get(i).get("active") == 0 && (long) possible.get(i).get("end") < (long)object.get(0).get("datetime"))){
                        possible.remove(i);
                    }
                }
                if (possible.size() > 0){
                    add_new.setText("Add new Entries to this entry group");
                    scroll_child.addView(add_new);
                    List<Map<String, Object>> finalPossible1 = possible;
                    add_new.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (add_new.isChecked()){
                                LinearLayout stuff = new LinearLayout(getApplicationContext());
                                stuff.setOrientation(LinearLayout.VERTICAL);
                                scroll_child.addView(stuff, scroll_child.indexOfChild(add_new) + 1);
                                for (int i = 0; i < finalPossible1.size(); i++){
                                    Map<String, Object> prescription = finalPossible1.get(i);
                                    Button script = new Button(getApplicationContext());
                                    script.setTag(false);
                                    if ((int)prescription.get("active") == 1){
                                        script.setText((String)prescription.get("name"));
                                    }
                                    else {
                                        script.setText(prescription.get("name") + longs_to_range((long) prescription.get("start"), (long) prescription.get("end")));
                                    }
                                    stuff.addView(script);
                                    int finalI = i;
                                    script.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (!(boolean)script.getTag()){
                                                script.setTag(true);
                                                if (object.get(0).get("method").equals("TOOK MEDS")){

                                                    EditText current_count = new EditText(getApplicationContext());
                                                    current_count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                                                    current_count.setText(String.valueOf(0));

                                                    Button add = new Button(getApplicationContext());
                                                    add.setText("Add");
                                                    add.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            current_count.setText(String.valueOf(Float.parseFloat(current_count.getText().toString()) + 1));
                                                        }
                                                    });
                                                    add.setLayoutParams(weighted_params);

                                                    Button subtract = new Button(getApplicationContext());
                                                    subtract.setText("Subtract");
                                                    subtract.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            if (Float.parseFloat(current_count.getText().toString()) >= 1) {
                                                                current_count.setText(String.valueOf(Float.parseFloat(current_count.getText().toString()) - 1));
                                                            }
                                                            else {
                                                                current_count.setText(String.valueOf(0));
                                                            }
                                                        }
                                                    });
                                                    subtract.setTag(prescription.get("id"));
                                                    subtract.setLayoutParams(weighted_params);

                                                    LinearLayout buttons = new LinearLayout(getApplicationContext());
                                                    buttons.setOrientation(LinearLayout.HORIZONTAL);
                                                    buttons.addView(subtract);
                                                    buttons.addView(add);

                                                    EditText notes = new EditText(getApplicationContext());
                                                    notes.setHint("Notes");
                                                    notes.setTag(finalI);

                                                    stuff.addView(current_count, stuff.indexOfChild(script) + 1);
                                                    stuff.addView(buttons, stuff.indexOfChild(script) + 2);
                                                    stuff.addView(notes, stuff.indexOfChild(script) + 3);
                                                }
                                                else {
                                                    EditText current_count = new EditText(getApplicationContext());
                                                    current_count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                                                    current_count.setHint("Refill Amount");

                                                    current_count.setTag((int) prescription.get("controlled") == 1);
                                                    stuff.addView(current_count, stuff.indexOfChild(script) + 1);
                                                }
                                            }
                                            else {
                                                script.setTag(false);
                                                stuff.removeViewAt(stuff.indexOfChild(script) + 1);
                                                if (object.get(0).get("method").equals("TOOK MEDS")){
                                                    stuff.removeViewAt(stuff.indexOfChild(script) + 1);
                                                    stuff.removeViewAt(stuff.indexOfChild(script) + 1);
                                                }
                                            }

                                        }
                                    });
                                }
                            }
                            else{
                                scroll_child.removeViewAt(scroll_child.indexOfChild(add_new) + 1);
                            }
                        }
                    });
                }

            }


            int[] yr_mo_day_hr_min = long_to_full_date((long)object.get(0).get("datetime"));

            DatePicker admit_date = new DatePicker(getApplicationContext());
            admit_date.setMaxDate(System.currentTimeMillis());
            admit_date.updateDate(yr_mo_day_hr_min[0], yr_mo_day_hr_min[1] - 1, yr_mo_day_hr_min[2]);

            TimePicker admit_time = new TimePicker(getApplicationContext());
            admit_time.setHour(yr_mo_day_hr_min[3]);
            admit_time.setMinute(yr_mo_day_hr_min[4]);

            CheckBox change = new CheckBox(this);
            change.setText("Change Entry Timestamp: " + long_to_datetime((long)object.get(0).get("datetime")));
            scroll_child.addView(change);

            change.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!change.isChecked()){
                        scroll_child.removeView(admit_date);
                        scroll_child.removeView(admit_time);
                    }
                    else{
                        scroll_child.addView(admit_date, scroll_child.indexOfChild(change) + 1);
                        scroll_child.addView(admit_time, scroll_child.indexOfChild(admit_date) + 1);
                    }
                }
            });

            Button save = new Button(this);
            save.setText("Save Edits");
            scroll_child.addView(save);
            List<Map<String, Object>> finalPossible = possible;
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int add = 0;
                    Long time = null;
                    if (change.isChecked()){
                        String day = datepicker_to_date(admit_date) + " " + timepicker_to_time(admit_time);
                        time = datetime_to_long(day);
                    }
                    else {
                        time = (long)object.get(0).get("datetime");
                    }
                    for (int i = 0; i < object.size(); i++){
                        Map<String, Object> entry = object.get(i);
                        boolean edited = false;
                        if (change.isChecked()){
                            entry.put("datetime", time);
                            edited = true;
                        }
                        if ((Boolean)scroll_child.getChildAt(i + add).getTag()){
                            add++;
                            LinearLayout stuff = (LinearLayout) scroll_child.getChildAt(i + add);
                            CheckBox delete = (CheckBox)stuff.getChildAt(1);
                            if (delete.isChecked()){
                                if ((int)prescriptions_db.getObject("controlled", new String[]{"id="+entry.get("prescription_id")}) == 1){
                                    if (entry.get("method").equals("TOOK MEDS")){
                                        adjust_future_counts((int)entry.get("prescription_id"), (float)entry.get("change"), (long)entry.get("datetime"));
                                    }
                                    else if (entry.get("change") != null){
                                        adjust_future_counts((int)entry.get("prescription_id"), -1 * (float)entry.get("change"), (long)entry.get("datetime"));
                                    }
                                }
                                entries_db.delete_single_constraint("id="+entry.get("id"));
                                continue;
                            }
                            else if (entry.get("method").equals("TOOK MEDS") || entry.get("method").equals("REFILL") || entry.get("method").equals("INTAKE") ||
                                    entry.get("method").equals("PRESCRIPTION STARTED") || entry.get("method").equals("UPDATED PRESCRIPTION STARTED")){
                                CheckBox change = (CheckBox)stuff.getChildAt(2);
                                if (change.isChecked()){
                                    EditText count = (EditText) stuff.getChildAt(3);
                                    Float updated_count = null;
                                    if (count.getText().toString().length() > 0){
                                        updated_count = Float.parseFloat(count.getText().toString());
                                        if (updated_count <= 0){
                                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                            alert.setNegativeButton("", null);
                                            alert.setPositiveButton("OK", null);
                                            alert.setTitle("Invalid Count for " + entry.get("drug"));
                                            alert.setMessage(updated_count + " is not a valid amount for taking meds, refilling meds, or starting a prescription. This entry will not be altered.");
                                            alert.show();
                                            continue;
                                        }
                                    }
                                    else if ((int)prescriptions_db.getObject("controlled", new String[]{"id="+entry.get("prescription_id")}) == 1){
                                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                        alert.setNegativeButton("", null);
                                        alert.setPositiveButton("OK", null);
                                        alert.setTitle("Invalid Count for " + entry.get("drug"));
                                        alert.setMessage("A valid amount must be input for controlled medications. This entry will not be altered.");
                                        alert.show();
                                        continue;
                                    }
                                    else if (entry.get("method").equals("TOOK MEDS")){
                                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                        alert.setNegativeButton("", null);
                                        alert.setPositiveButton("OK", null);
                                        alert.setTitle("Invalid Count for " + entry.get("drug"));
                                        alert.setMessage("A valid amount must be input for taking medications. This entry will not be altered.");
                                        alert.show();
                                        continue;
                                    }

                                    Float difference = null;
                                    if (entry.get("change") != null){
                                        difference = updated_count - (float) entry.get("change");
                                    }
                                    entry.put("change", updated_count);
                                    if ((int)prescriptions_db.getObject("controlled", new String[]{"id="+entry.get("prescription_id")}) == 1){
                                        if (entry.get("method").equals("TOOK MEDS")){
                                            entry.put("new_count", (float)entry.get("new_count") - difference);
                                            adjust_future_counts((int)entry.get("prescription_id"), -1 * difference, (long)entry.get("datetime"));
                                        }
                                        else {
                                            entry.put("new_count", (float)entry.get("new_count") + difference);
                                            adjust_future_counts((int)entry.get("prescription_id"), difference, (long)entry.get("datetime"));
                                        }
                                    }
                                    edited = true;
                                }
                            }
                            else if (entry.get("method").equals("COUNT") || entry.get("method").equals("MISCOUNT")){
                                CheckBox change = (CheckBox)stuff.getChildAt(2);
                                if (change.isChecked()){
                                    EditText count = (EditText) stuff.getChildAt(3);
                                    Float updated_count = null;
                                    if (count.getText().toString().length() > 0){
                                        updated_count = Float.parseFloat(count.getText().toString());
                                        if (updated_count <= 0){
                                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                            alert.setNegativeButton("", null);
                                            alert.setPositiveButton("OK", null);
                                            alert.setTitle("Invalid Count for " + entry.get("drug"));
                                            alert.setMessage(updated_count + " is not a valid amount for counting meds. This entry will not be altered.");
                                            alert.show();
                                            continue;
                                        }
                                    }
                                    else{
                                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                        alert.setNegativeButton("", null);
                                        alert.setPositiveButton("OK", null);
                                        alert.setTitle("Invalid Count for " + entry.get("drug"));
                                        alert.setMessage("A valid amount must be input for counting medications. This entry will not be altered.");
                                        alert.show();
                                        continue;
                                    }

                                    Float difference = updated_count - (float) entry.get("new_count");
                                    entry.put("new_count", updated_count);
                                    entry.put("change", (float)entry.get("change") + difference);
                                    if ((float)entry.get("change") == 0){
                                        entry.put("method", "COUNT");
                                    }
                                    else{
                                        entry.put("method", "MISCOUNT");
                                    }
                                    adjust_future_counts((int)entry.get("prescription_id"), difference, (long)entry.get("datetime"));
                                    edited = true;
                                }
                            }
                        }
                        if (edited){
                            entry.put("edits", (int)entry.get("edits") + 1);
                            entries_db.update(entry, new String[]{"id="+entry.get("id")});
                        }
                    }

                    if (add_new.isChecked()){
                        LinearLayout stuff = (LinearLayout)scroll_child.getChildAt(scroll_child.indexOfChild(add_new) + 1);
                        add = 0;
                        for (int i = 0; i < finalPossible.size(); i++){
                            if ((Boolean)stuff.getChildAt(i + add).getTag()){
                                add++;
                                int text_location = i + add;
                                if (object.get(0).get("method").equals("TOOK MEDS")){
                                    add += 2;
                                }
                                EditText count = (EditText) stuff.getChildAt(text_location);
                                Float updated_count = null;
                                if (count.getText().toString().length() > 0){
                                    updated_count = Float.parseFloat(count.getText().toString());
                                    if (updated_count <= 0){
                                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                        alert.setNegativeButton("", null);
                                        alert.setPositiveButton("OK", null);
                                        alert.setTitle("Invalid Count for " + finalPossible.get(i).get("name"));
                                        alert.setMessage(updated_count + " is not a valid amount for taking meds, or refilling meds. This entry will not be added.");
                                        alert.show();
                                        continue;
                                    }
                                }
                                else if ((int)finalPossible.get(i).get("controlled") == 1){
                                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                    alert.setNegativeButton("", null);
                                    alert.setPositiveButton("OK", null);
                                    alert.setTitle("Invalid Count for " + finalPossible.get(i).get("name"));
                                    alert.setMessage("A valid amount must be input for controlled medications. This entry will not be added.");
                                    alert.show();
                                    continue;
                                }
                                else if (object.get(0).get("method").equals("TOOK MEDS")){
                                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                    alert.setNegativeButton("", null);
                                    alert.setPositiveButton("OK", null);
                                    alert.setTitle("Invalid Count for " + finalPossible.get(i).get("name"));
                                    alert.setMessage("A valid amount must be input for taking medications. This entry will not be added.");
                                    alert.show();
                                    continue;
                                }
                                Float old_count = null;
                                Float new_count = null;

                                if ((int)finalPossible.get(i).get("controlled") == 1){
                                    long previous_time = (long) entries_db.getObject("MAX([datetime]) AS datetime",
                                            new String[]{"prescription_id="+(int)finalPossible.get(i).get("id"), "datetime<"+time});
                                    old_count = (float) entries_db.getObject("new_count",
                                            new String[]{"prescription_id="+(int)finalPossible.get(i).get("id"), "datetime="+previous_time});
                                    if (object.get(0).get("method").equals("TOOK MEDS")){
                                        new_count = old_count - updated_count;
                                    }
                                    else {
                                        new_count = old_count + updated_count;
                                    }
                                }

                                boolean dose_override = false;
                                boolean daily_override = false;
                                if (object.get(0).get("method").equals("TOOK MEDS")){
                                    if (finalPossible.get(i).get("dose_max") != null && (float)finalPossible.get(i).get("dose_max") < updated_count){
                                        dose_override = true;
                                    }
                                    if (finalPossible.get(i).get("daily_max") != null){
                                        String date = long_to_date(time);
                                        long start = date_to_long(date);
                                        long end = date_to_long(next_day(date, 1));

                                        List<Map<String, Object>> taken = entries_db.getRows(
                                                new String[]{"id", "change", "prescription_id"},
                                                new String[]{"method='TOOK MEDS'", "datetime>="+start, "datetime<"+end, "prescription_id="+finalPossible.get(i).get("id")},
                                                null, false
                                        );
                                        float daily_count = 0;
                                        for (int j = 0; j < taken.size(); j++){
                                            daily_count += (float)taken.get(j).get("change");
                                        }
                                        if (daily_count + updated_count > (float)finalPossible.get(i).get("daily_max")){
                                            daily_override = true;
                                        }
                                    }
                                }

                                List<String> notes = new ArrayList<>();
                                notes.add("This entry was added through the editing function by Admin");
                                if (object.get(0).get("method").equals("TOOK MEDS")){
                                    EditText note = (EditText) stuff.getChildAt(i + add);
                                    if (note.getText().toString().length() > 0){
                                        notes.add(note.getText().toString());
                                    }
                                }
                                Map<String, Object> entry = create_entry((int)finalPossible.get(i).get("client_id"), (int)finalPossible.get(i).get("id"),
                                        (String)finalPossible.get(i).get("name"), old_count, updated_count, new_count, time, dose_override, daily_override, 1,
                                        (String)object.get(0).get("method"), null, null, null, notes);

                                entries_db.addRow(entry);
                                if ((int)finalPossible.get(i).get("controlled") == 1){
                                    if (object.get(0).get("method").equals("TOOK MEDS")){
                                        adjust_future_counts((int)finalPossible.get(i).get("id"), -1 * updated_count, time);
                                    }
                                    else {
                                        adjust_future_counts((int)finalPossible.get(i).get("id"), updated_count, time);
                                    }
                                }
                            }
                        }
                    }
                    history.remove(0);
                    history.remove(0);
                    edit();
                }

            });

        }
    }

    public void delete() {

        wipe("Delete Data", this::delete);

        final List<Map<String, Object>>[] client_list = new List[]{clients_db.getRows(null,
                null, new String[]{"active", "DESC", "name", "ASC", "admit", "ASC"}, false)};

        final List<String>[] client_names = new List[]{new ArrayList<>()};
        client_names[0].add("All Clients");
        for (int i = 0; i < client_list[0].size(); i++){
            Map<String, Object> client = client_list[0].get(i);
            if ((int)client.get("active") == 1){
                client_names[0].add((String) client.get("name"));
            }
            else {
                client_names[0].add(client.get("name") + longs_to_range((long)client.get("admit"), (long)client.get("discharge")));
            }
        }

        final List<Map<String, Object>>[] prescription_list = new List[]{new ArrayList<>()};
        final List<String>[] prescription_names = new List[]{new ArrayList<>()};
        prescription_names[0].add("All Prescriptions");

        List<String> date_list = new ArrayList<>();
        date_list.add("All Dates");
        date_list.add("Date Range");
        date_list.add("Single Date");

        final List<String>[] time_list = new List[]{new ArrayList<>()};
        final List<Long>[] datetime_list = new List[]{new ArrayList<>()};

        String[] chosen_type = {"Clients"};
        int[] chosen_client = {0};
        String[] chosen_prescription = {"All Prescriptions"};
        String[] chosen_date = {"All Dates"};


        Spinner type_picker = new Spinner(this);
        Spinner client_picker = new Spinner(this);
        Spinner prescription_picker = new Spinner(this);
        Spinner date_picker = new Spinner(this);
        Spinner time_picker = new Spinner(this);

        Button delete = new Button(this);

        DatePicker single_date = new DatePicker(getApplicationContext());
        DatePicker end_date = new DatePicker(getApplicationContext());

        single_date.setMaxDate(System.currentTimeMillis());
        end_date.setMaxDate(System.currentTimeMillis());

        TextView start = new TextView(getApplicationContext());
        start.setText("Start Date");

        TextView end = new TextView(getApplicationContext());
        end.setText("End Date");

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(Arrays.asList("Clients", "Client Prescriptions", "Prescription Records")));
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type_picker.setAdapter(typeAdapter);

        ArrayAdapter<String> clientAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, client_names[0]);
        clientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        client_picker.setAdapter(clientAdapter);

        ArrayAdapter<String> prescriptionAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, prescription_names[0]);
        prescriptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prescription_picker.setAdapter(prescriptionAdapter);

        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, date_list);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        date_picker.setAdapter(dateAdapter);

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, time_list[0]);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        time_picker.setAdapter(timeAdapter);

        type_picker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (type_picker.getSelectedItem().equals("Client Prescriptions") && !chosen_type[0].equals("Client Prescriptions")) {
                    chosen_type[0] = "Client Prescriptions";
                    scroll_child.removeAllViews();
                    scroll_child.addView(type_picker);
                    scroll_child.addView(client_picker);
                    scroll_child.addView(prescription_picker);
                    scroll_child.addView(delete);

                } else if (type_picker.getSelectedItem().equals("Prescription Records") && !chosen_type[0].equals("Prescription Records")) {
                    chosen_type[0] = "Prescription Records";
                    scroll_child.removeAllViews();
                    scroll_child.addView(type_picker);
                    scroll_child.addView(client_picker);
                    scroll_child.addView(prescription_picker);
                    scroll_child.addView(date_picker);

                    if (date_picker.getSelectedItem().equals("Date Range")) {
                        scroll_child.addView(start);
                        scroll_child.addView(single_date);
                        scroll_child.addView(end);
                        scroll_child.addView(end_date);
                    }
                    else if(!date_picker.getSelectedItem().equals("All Dates")){
                        scroll_child.addView(single_date);
                        if (date_picker.getSelectedItem().equals("Single Time")) {
                            scroll_child.addView(time_picker);
                        }
                    }
                    scroll_child.addView(delete);
                }
                else if (type_picker.getSelectedItem().equals("Clients") && !chosen_type[0].equals("Clients")){
                    chosen_type[0] = "Clients";
                    scroll_child.removeAllViews();
                    scroll_child.addView(type_picker);
                    scroll_child.addView(client_picker);
                    scroll_child.addView(delete);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        scroll_child.addView(type_picker);


        client_picker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (client_picker.getSelectedItemPosition() == 0 && chosen_client[0] != 0) {
                    chosen_client[0] = 0;

                    prescription_list[0].clear();
                    prescription_names[0].clear();
                    prescription_names[0].add("All Prescriptions");
                    prescription_picker.setSelection(0);
                    prescriptionAdapter.notifyDataSetChanged();
                    chosen_prescription[0] = "All Prescriptions";

                    if (date_picker.getSelectedItem().equals("Single Time")){
                        scroll_child.removeView(time_picker);
                        date_picker.setSelection(2);
                        chosen_date[0] = "Single Date";
                    }
                    date_list.remove("Single Time");
                    dateAdapter.notifyDataSetChanged();

                    single_date.setMinDate(-2208960974144L);
                    end_date.setMinDate(-2208960974144L);
                }
                else if (client_picker.getSelectedItemPosition() != chosen_client[0]){

                    if (chosen_client[0] == 0) {
                        date_list.add("Single Time");
                        dateAdapter.notifyDataSetChanged();
                    }
                    else{
                        single_date.setMinDate((long)0);
                        end_date.setMinDate((long)0);
                    }



                    chosen_client[0] = client_picker.getSelectedItemPosition();

                    Map<String, Object> client = client_list[0].get(chosen_client[0] - 1);

                    Pair<List<Map<String, Object>>, List<String>> pair = update_prescriptions(
                            (int) client.get("id"),
                            true);
                    prescription_list[0] = pair.first;
                    prescription_names[0].clear();
                    prescription_names[0].addAll(pair.second);
                    prescription_picker.setSelection(0);
                    prescriptionAdapter.notifyDataSetChanged();
                    chosen_prescription[0] = "All Prescriptions";


                    Pair<List<Long>, List<String>> time_pair = update_times(
                            (int) client.get("id"), false, single_date);

                    datetime_list[0] = time_pair.first;
                    time_list[0].clear();
                    time_list[0].addAll(time_pair.second);
                    time_picker.setSelection(0);
                    timeAdapter.notifyDataSetChanged();

                    single_date.setMinDate((long)client.get("admit"));
                    end_date.setMinDate((long)client.get("admit"));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        scroll_child.addView(client_picker);

        prescription_picker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (prescription_picker.getSelectedItemPosition() == 0 && !chosen_prescription[0].equals("All Prescriptions")){

                    chosen_prescription[0] = "All Prescriptions";

                    Map<String, Object> client = client_list[0].get(chosen_client[0] - 1);
                    Pair<List<Long>, List<String>> time_pair = update_times(
                            (int) client.get("id"), false, single_date);

                    datetime_list[0] = time_pair.first;
                    time_list[0].clear();
                    time_list[0].addAll(time_pair.second);
                    time_picker.setSelection(0);
                    timeAdapter.notifyDataSetChanged();

                    single_date.setMinDate((long)0);
                    end_date.setMinDate((long)0);

                    single_date.setMinDate((long)client.get("admit"));
                    end_date.setMinDate((long)client.get("admit"));

                }

                else if (!prescription_picker.getSelectedItem().equals(chosen_prescription[0])){
                    Map<String, Object> prescription = prescription_list[0].get(prescription_picker.getSelectedItemPosition() - 1);

                    chosen_prescription[0] = (String)prescription.get("name");

                    Pair<List<Long>, List<String>> time_pair = update_times(
                            (int) prescription.get("id"),
                            true, single_date);

                    datetime_list[0] = time_pair.first;
                    time_list[0].clear();
                    time_list[0].addAll(time_pair.second);
                    time_picker.setSelection(0);
                    timeAdapter.notifyDataSetChanged();

                    single_date.setMinDate((long)0);
                    end_date.setMinDate((long)0);

                    single_date.setMinDate((long)prescription.get("start"));
                    end_date.setMinDate((long)prescription.get("start"));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        date_picker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!date_picker.getSelectedItem().equals(chosen_date[0])) {
                    scroll_child.removeView(single_date);
                    scroll_child.removeView(end_date);
                    scroll_child.removeView(start);
                    scroll_child.removeView(end);
                    scroll_child.removeView(time_picker);

                    if (date_picker.getSelectedItem().equals("Date Range")) {
                        chosen_date[0] = "Date Range";
                        scroll_child.addView(start, scroll_child.indexOfChild(date_picker) + 1);
                        scroll_child.addView(single_date, scroll_child.indexOfChild(date_picker) + 2);
                        scroll_child.addView(end, scroll_child.indexOfChild(date_picker) + 3);
                        scroll_child.addView(end_date, scroll_child.indexOfChild(date_picker) + 4);
                    }
                    else if(!date_picker.getSelectedItem().equals("All Dates")){
                        chosen_date[0] = "Single Date";
                        scroll_child.addView(single_date, scroll_child.indexOfChild(date_picker) + 1);
                        if (date_picker.getSelectedItem().equals("Single Time")) {
                            chosen_date[0] = "Single Time";
                            scroll_child.addView(time_picker, scroll_child.indexOfChild(date_picker) + 2);
                        }
                    }
                    else {
                        chosen_date[0] = "All Dates";
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        single_date.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                if (prescription_picker.getSelectedItemPosition() != 0){

                    Map<String, Object> prescription = prescription_list[0].get(prescription_picker.getSelectedItemPosition() - 1);

                    Pair<List<Long>, List<String>> time_pair = update_times(
                            (int) prescription.get("id"),
                            true, single_date);

                    datetime_list[0] = time_pair.first;
                    time_list[0].clear();
                    time_list[0].addAll(time_pair.second);
                    time_picker.setSelection(0);
                    timeAdapter.notifyDataSetChanged();

                }

                else if (chosen_client[0] > 0){
                    int client = (int)client_list[0].get(chosen_client[0] - 1).get("id");
                    Pair<List<Long>, List<String>> time_pair = update_times(
                            client, false, single_date);

                    datetime_list[0] = time_pair.first;
                    time_list[0].clear();
                    time_list[0].addAll(time_pair.second);
                    time_picker.setSelection(0);
                    timeAdapter.notifyDataSetChanged();
                }

            }
        });

        delete.setText("Delete Selected Data");
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                warning.setNegativeButton("Cancel", null);
                warning.setPositiveButton("Delete Data", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String type = (String) type_picker.getSelectedItem();
                        Integer client = null;
                        Integer prescription = null;
                        String date = null;
                        String end = null;
                        Long time = null;

                        if (chosen_client[0] != 0) {
                            client = (int) client_list[0].get(chosen_client[0] - 1).get("id");

                            if (!chosen_prescription[0].equals("All Prescriptions")) {

                                prescription = (int)prescription_list[0].get(prescription_picker.getSelectedItemPosition() - 1).get("id");
                            }
                        }

                        if (type.equals("Prescription Records")) {
                            if (!chosen_date[0].equals("All Dates")) {

                                date = datepicker_to_date(single_date);

                                if (chosen_date[0].equals("Date Range")) {
                                    end = datepicker_to_date(end_date);
                                }
                                else if (chosen_date[0].equals("Single Time")) {
                                    if (datetime_list[0].size() > 0) {
                                        time = datetime_list[0].get(time_picker.getSelectedItemPosition());
                                    }
                                    else {
                                        return;
                                    }
                                }
                            }
                        }

                        if (client == null){
                            if (type.equals("Clients")){
                                clients_db.reboot();
                                prescriptions_db.reboot();
                                entries_db.reboot();

                                client_list[0].clear();
                                client_names[0].clear();
                                client_names[0].add("All Clients");
                                clientAdapter.notifyDataSetChanged();
                            }
                            else if (type.equals("Client Prescriptions")){
                                prescriptions_db.reboot();
                                entries_db.reboot();
                            }
                            else if (date == null){
                                entries_db.reboot();
                            }
                            else {
                                long datetime = date_to_long(date);
                                long endtime = date_to_long(next_day(date, 1));

                                if (end != null){
                                    endtime = date_to_long(next_day(end, 1));
                                }
                                entries_db.deleteRows(new String[]{"datetime>="+datetime, "datetime<"+endtime});
                            }
                        }
                        else{
                            if (type.equals("Clients")){
                                clients_db.delete_single_constraint("id="+client);
                                prescriptions_db.delete_single_constraint("client_id="+client);
                                entries_db.delete_single_constraint("client_id="+client);

                                client_list[0].remove(chosen_client[0] - 1);
                                client_names[0].remove(chosen_client[0]);
                                client_picker.setSelection(0);
                                clientAdapter.notifyDataSetChanged();
                                chosen_client[0] = 0;

                                prescription_list[0].clear();
                                prescription_names[0].clear();
                                prescription_names[0].add("All Prescriptions");
                                prescription_picker.setSelection(0);
                                prescriptionAdapter.notifyDataSetChanged();
                                chosen_prescription[0] = "All Prescriptions";

                                if (date_picker.getSelectedItem().equals("Single Time")){
                                    scroll_child.removeView(time_picker);
                                    date_picker.setSelection(2);
                                    chosen_date[0] = "Single Date";
                                }
                                date_list.remove("Single Time");
                                dateAdapter.notifyDataSetChanged();

                                single_date.setMinDate(-2208960974144L);
                                end_date.setMinDate(-2208960974144L);
                            }
                            else if (type.equals("Client Prescriptions")){
                                if (prescription == null){
                                    prescriptions_db.delete_single_constraint("client_id="+client);
                                    entries_db.delete_single_constraint("client_id="+client);

                                    prescription_list[0].clear();
                                    prescription_names[0].clear();
                                    prescription_names[0].add("All Prescriptions");
                                    prescription_picker.setSelection(0);
                                    prescriptionAdapter.notifyDataSetChanged();
                                    chosen_prescription[0] = "All Prescriptions";
                                }
                                else{
                                    prescriptions_db.delete_single_constraint("id="+prescription);
                                    entries_db.delete_single_constraint("prescription_id="+prescription);

                                    prescription_list[0].remove(prescription_picker.getSelectedItemPosition() - 1);
                                    prescription_names[0].remove(prescription_picker.getSelectedItemPosition());
                                    prescription_picker.setSelection(0);
                                    prescriptionAdapter.notifyDataSetChanged();
                                    chosen_prescription[0] = "All Prescriptions";

                                    Pair<List<Long>, List<String>> time_pair = update_times(
                                            client, false, single_date);

                                    datetime_list[0] = time_pair.first;
                                    time_list[0].clear();
                                    time_list[0].addAll(time_pair.second);
                                    time_picker.setSelection(0);
                                    timeAdapter.notifyDataSetChanged();

                                    single_date.setMinDate((long)0);
                                    end_date.setMinDate((long)0);

                                    single_date.setMinDate((long) client_list[0].get(chosen_client[0] - 1).get("admit"));
                                    end_date.setMinDate((long) client_list[0].get(chosen_client[0] - 1).get("admit"));
                                }
                            }
                            else if (date == null){

                                time_list[0].clear();
                                datetime_list[0].clear();

                                if (prescription == null){
                                    entries_db.delete_single_constraint("client_id="+client);
                                    time_list[0].add("No entries for " + client_picker.getSelectedItem() + " on " + date);
                                }
                                else{
                                    entries_db.delete_single_constraint("prescription_id="+prescription);
                                    time_list[0].add("No entries for " + prescription_picker.getSelectedItem() + " on " + date);
                                }
                                time_picker.setSelection(0);
                                timeAdapter.notifyDataSetChanged();
                            }

                            else if (time == null){
                                long datetime = date_to_long(date);
                                long endtime = date_to_long(next_day(date, 1));

                                if (end != null){
                                    endtime = date_to_long(next_day(end, 1));
                                }

                                time_list[0].clear();
                                datetime_list[0].clear();

                                if (prescription == null){
                                    entries_db.deleteRows(new String[]{"datetime>="+datetime, "datetime<"+endtime, "client_id="+client});
                                    time_list[0].add("No entries for " + client_picker.getSelectedItem() + " on " + date);                                }
                                else{
                                    entries_db.deleteRows(new String[]{"datetime>="+datetime, "datetime<"+endtime, "prescription_id="+prescription});
                                    time_list[0].add("No entries for " + prescription_picker.getSelectedItem() + " on " + date);
                                }

                                time_picker.setSelection(0);
                                timeAdapter.notifyDataSetChanged();

                            }
                            else {

                                datetime_list[0].remove(time_picker.getSelectedItemPosition());
                                time_list[0].remove(time_picker.getSelectedItemPosition());

                                if (prescription == null){
                                    entries_db.deleteRows(new String[]{"datetime="+time, "client_id="+client});
                                    if (time_list[0].size() == 0){
                                        time_list[0].add("No entries for " + client_picker.getSelectedItem() + " on " + date);
                                    }
                                }
                                else{
                                    entries_db.deleteRows(new String[]{"datetime="+time, "prescription_id="+prescription});
                                    if (time_list[0].size() == 0){
                                        time_list[0].add("No entries for " + prescription_picker.getSelectedItem() + " on " + date);
                                    }

                                }
                                time_picker.setSelection(0);
                                timeAdapter.notifyDataSetChanged();

                            }
                        }
                    }
                });
                warning.setTitle("Confirm Data Deletion");
                warning.setMessage("Deleted data cannot be viewed and cannot be recovered. Are you sure you wish to proceed with the selected deletions?");
                warning.show();
            }
        });
        scroll_child.addView(delete);
    }

    public Pair<List<Map<String, Object>>, List<String>> update_prescriptions(int client_id, boolean add_all){

        List<Map<String, Object>> current_prescriptions =  prescriptions_db.getRows(new String[]{"id", "name", "start", "end", "active"},
                new String[]{"client_id="+client_id}, new String[]{"active", "DESC", "name", "ASC"}, false);

        List<String> current_names = new ArrayList<>();

        if(add_all){
            current_names.add("All Prescriptions");
        }

        for (int i = 0; i < current_prescriptions.size(); i++){
            Map<String, Object> prescription = current_prescriptions.get(i);
            String prescription_string = (String)prescription.get("name");
            if ((int) prescription.get("active") == 0){
                prescription_string += longs_to_range((long)prescription.get("start"), (long)prescription.get("end"));
            }
            current_names.add(prescription_string);
        }

        if (current_names.size() == 0){
            current_names.add("There are no prescriptions to edit");
        }

        return new Pair<>(current_prescriptions, current_names);
    }

    public Pair<List<Long>, List<String>> update_times(int id, boolean single_script, DatePicker calendar){

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

    public Sheet client_sheet(Workbook workbook, String sheet_name, List<Map<String, Object>> client_data){

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

    public Sheet prescription_sheet(Workbook workbook, String sheet_name, List<Map<String, Object>> prescription_data, String start, String end){

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

    public Sheet entry_sheet(Workbook workbook, String sheet_name, List<Map<String, Object>> entry_data){

        Sheet entries = workbook.createSheet(sheet_name.replace("/", "-"));
        List<String> entry_headers = Arrays.asList("drug", "method", "old_count", "change", "new_count", "datetime", "notes",
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
                        row.createCell(j).setCellValue("N/A");
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

    public void insert_signature(Workbook workbook, CreationHelper helper, Drawing patriarch, byte[] bytes, int row, int col){

        int my_picture_id = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);

        ClientAnchor anchor = helper.createClientAnchor();

        anchor.setCol1(col);
        anchor.setRow1(row);
        anchor.setCol2(col + 1);
        anchor.setRow2(row + 1);

        patriarch.createPicture(anchor, my_picture_id);
    }

    public boolean check_folders(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
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

    public Pair<Boolean, String> save_xls(Workbook workbook, String file_name){
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

    public SignatureView create_signature_view(){
        SignatureView signature = new SignatureView(this, null);
        signature.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!scroll.getLayoutParams().equals(unweighted_params) && signature.getHeight() > signature_size){
                    scroll.setLayoutParams(unweighted_params);
                }
                else if (!scroll.getLayoutParams().equals(weighted_params) && signature.getHeight() < signature_size){
                    scroll.setLayoutParams(weighted_params);
                }
                signature.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, signature_size));
                signature.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        if (scroll.getLayoutParams().equals(weighted_params)){
            signature.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, signature_size, 1));
        }

        return signature;
    }

    public Map<String, Object> create_client(String name, Long time){
        Map<String, Object> client = new HashMap<>();
        client.put("name", name.toUpperCase());
        client.put("active", true);
        client.put("admit", time);
        client.put("edits", 0);

        return client;
    }

    public Map<String, Object> create_prescription(Integer client_id, String drug, String dose, Float dose_max, Float daily_max, String instructions,
                                                   Boolean as_needed, Boolean controlled, Float count, String indication, String prescriber,
                                                   String pharmacy, Long time){
        drug = drug.toUpperCase();
        dose = dose.toUpperCase();

        Map<String, Object> prescription = new HashMap<>();
        prescription.put("client_id", client_id);
        prescription.put("drug", drug);
        prescription.put("dose", dose);
        prescription.put("dose_max", dose_max);
        prescription.put("daily_max", daily_max);
        prescription.put("instructions", instructions);
        prescription.put("as_needed", as_needed);
        prescription.put("controlled", controlled);
        prescription.put("count", count);
        prescription.put("indication", indication);
        prescription.put("prescriber", prescriber);
        prescription.put("pharmacy", pharmacy);
        prescription.put("start", time);
        prescription.put("active", true);
        prescription.put("name", drug + " " + dose);
        prescription.put("edits", 0);

        return prescription;
    }

    public Map<String, Object> create_entry(Integer client_id, int prescription_id, String drug, Float old_count, Float count, Float new_count, Long time, Boolean dose_override,
                                            Boolean daily_override, Integer edits, String method,
                                            String client_signature, String staff_signature_1, String staff_signature_2,
                                            List<String> notes){

        Map<String, Object> entries_map = new HashMap<>();
        entries_map.put("client_id", client_id);
        entries_map.put("prescription_id", prescription_id);
        entries_map.put("drug", drug);
        entries_map.put("datetime", time);
        entries_map.put("old_count", old_count);
        entries_map.put("change", count);
        entries_map.put("new_count", new_count);
        entries_map.put("dose_override", dose_override);
        entries_map.put("daily_override", daily_override);
        entries_map.put("edits", edits);
        entries_map.put("method", method);
        entries_map.put("client_signature", client_signature);
        entries_map.put("staff_signature_1", staff_signature_1);
        entries_map.put("staff_signature_2", staff_signature_2);
        entries_map.put("notes", gson.toJson(notes));

        return entries_map;
    }

    public void adjust_future_counts(int prescription_id, float count_change, long datetime){
        List<Map<String, Object>> old_entries = entries_db.getRows(new String[]{"change", "id", "method", "old_count", "new_count", "edits"},
                new String[]{"datetime>"+datetime, "prescription_id="+prescription_id},
                new String[]{"datetime", "ASC"}, false);
        boolean broken = false;
        for (int i = 0; i < old_entries.size(); i++){
            Map<String, Object> entry = old_entries.get(i);
            if (entry.get("method").equals("REFILL") || entry.get("method").equals("TOOK MEDS")){
                entry.put("old_count", (float)entry.get("old_count") + count_change);
                entry.put("new_count", (float)entry.get("new_count") + count_change);
            }
            else if (entry.get("method").equals("CLIENT DISCHARGED") || entry.get("method").equals("PRESCRIPTION DISCONTINUED") || entry.get("method").equals("DISCONTINUED DUE TO UPDATE")){
                entry.put("old_count", (float)entry.get("old_count") + count_change);
            }
            else if (entry.get("method").equals("COUNT")){
                entry.put("old_count", (float)entry.get("old_count") + count_change);
                entry.put("change", -1 * count_change);
                entry.put("method", "MISCOUNT");
                entry.put("edits", (int)entry.get("edits") + 1);
                entries_db.update(entry, new String[]{"id="+(int)entry.get("id")});
                broken = true;
                break;
            }
            else if (entry.get("method").equals("MISCOUNT")){
                entry.put("old_count", (float)entry.get("old_count") + count_change);
                entry.put("change", -1 * count_change + (float)entry.get("change"));
                if ((float) entry.get("old_count") == (float) entry.get("new_count")){
                    entry.put("method", "COUNT");
                }
                entry.put("edits", (int)entry.get("edits") + 1);
                entries_db.update(entry, new String[]{"id="+(int)entry.get("id")});
                broken = true;
                break;
            }
            entry.put("edits", (int)entry.get("edits") + 1);
            entries_db.update(entry, new String[]{"id="+(int)entry.get("id")});
        }

        if (!broken){
            Map<String, Object> prescription = prescriptions_db.getSingleRow(new String[]{"count"}, new String[]{"id="+prescription_id});
            prescription.put("count", (float)prescription.get("count") + count_change);
            prescriptions_db.update(prescription, new String[]{"id="+prescription_id});
        }
    }

    public void swap_prescriptions(int old_id, Float old_dose_max, Float old_daily_max, int new_id, Float new_dose_max, Float new_daily_max, long datetime, String name){

        List<Map<String, Object>> old_entries = null;
        if (new_id == old_id){
            old_entries = entries_db.getRows(new String[]{"change", "id", "method", "datetime"},
                    new String[]{"prescription_id="+old_id},
                    null, false);

        }
        else {
            old_entries = entries_db.getRows(new String[]{"change", "id", "method", "datetime", "drug"},
                    new String[]{"datetime>"+datetime, "prescription_id="+old_id},
                    null, false);

        }
        String day_string = "";
        float day_count = 0;
        for (int i = 0; i < old_entries.size(); i++){
            Map<String, Object> entry = old_entries.get(i);
            entry.put("prescription_id", new_id);
            entry.put("drug", name);
            if (entry.get("method").equals("TOOK MEDS")){
                if (new_dose_max == null){
                    entry.put("dose_override", false);
                }
                else if (new_dose_max != old_dose_max){
                    if ((float) entry.get("change") > new_dose_max){
                        entry.put("dose_override", true);
                    }
                    else{
                        entry.put("dose_override", false);
                    }
                }
                if (new_daily_max == null){
                    entry.put("daily_override", false);
                }
                else if (new_daily_max != old_daily_max){
                    if (!long_to_date((long)entry.get("datetime")).equals(day_string)){
                        day_count = 0;
                        day_string = long_to_date((long)entry.get("datetime"));
                    }
                    day_count += (float) entry.get("change");
                    if (day_count > new_daily_max){
                        entry.put("daily_override", true);
                    }
                    else{
                        entry.put("daily_override", false);
                    }
                }
            }
            entries_db.update(entry, new String[]{"id="+(int)entry.get("id")});
        }
    }

    public void edit_entries(int client_id, int id, Float new_dose_max, Float new_daily_max, Long start, Long end, Long count_time, String name,
                             Boolean active, Boolean switch_controlled, Float real_count, Float change, Float old_count, boolean check_dose, boolean check_daily, boolean reactivate_controlled){

        List<Map<String, Object>> old_entries = entries_db.getRows(new String[]{"change", "id", "method", "datetime", "drug", "dose_override", "daily_override", "edits", "notes"},
                new String[]{"prescription_id="+id},
                new String[]{"datetime", "DESC"}, false);

        List<String> end_methods = Arrays.asList("CLIENT DISCHARGED", "PRESCRIPTION DISCONTINUED", "DISCONTINUED DUE TO UPDATE");
        List<String> start_methods = Arrays.asList("INTAKE", "UPDATED PRESCRIPTION STARTED", "PRESCRIPTION STARTED");

        String day_string = "";
        float day_count = 0;

        if (switch_controlled){
            entries_db.addRow(create_entry(client_id, id, name, null, null, real_count, count_time, false, false, 1,
                    "COUNT", null, null, null,
                    Arrays.asList("Prescription was set to 'controlled' and count was adjusted through editing function by Admin.")));
        }
        else if (reactivate_controlled){
            entries_db.addRow(create_entry(client_id, id, name, null, null, real_count, count_time, false, false, 1,
                    "COUNT", null, null, null,
                    Arrays.asList("Controlled prescription was reactivated and count was set through editing function by Admin.")));
        }
        else if (change != null){
            entries_db.addRow(create_entry(client_id, id, name, old_count, change, real_count, count_time, false, false, 1,
                    "MISCOUNT", null, null, null,
                    Arrays.asList("Prescription count was adjusted through editing function by Admin.")));
        }

        if (active != null && !active){
            entries_db.addRow(create_entry(client_id, id, name, real_count, null, null, end, false, false, 1,
                    "PRESCRIPTION DISCONTINUED", null, null, null,
                    Arrays.asList("Prescription was discontinued through editing function by Admin.")));
        }

        for (int i = 0; i < old_entries.size(); i++){
            Map<String, Object> entry = old_entries.get(i);
            if (end_methods.contains(entry.get("method")) && active != null && active){
                if (!reactivate_controlled){
                    Map<String, Object> script = new HashMap<>();
                    script.put("count", real_count);
                    prescriptions_db.update(script, new String[]{"id="+id});
                    entries_db.delete_single_constraint("id="+entry.get("id"));
                }
                else {
                    reactivate_controlled = false;
                }
            }
            else {
                boolean edited = false;
                if (!entry.get("drug").equals(name)){
                    entry.put("drug", name);
                    edited = true;
                }
                if (entry.get("method").equals("TOOK MEDS")){
                    if (check_dose){
                        if ((int)entry.get("dose_override") == 1){
                            if (new_dose_max == null || (float) entry.get("change") <= new_dose_max){
                                entry.put("dose_override", false);
                                edited = true;
                            }
                        }
                        else if (new_dose_max != null && (float) entry.get("change") > new_dose_max){
                            entry.put("dose_override", true);
                            edited = true;
                        }
                    }


                    if (check_daily){
                        if (!long_to_date((long)entry.get("datetime")).equals(day_string)){
                            day_count = 0;
                            day_string = long_to_date((long)entry.get("datetime"));
                        }
                        day_count += (float) entry.get("change");

                        if ((int)entry.get("daily_override") == 1){
                            if (new_daily_max == null || day_count <= new_daily_max){
                                entry.put("daily_override", false);
                                edited = true;
                            }
                        }
                        else if (new_daily_max != null && day_count > new_daily_max){
                            entry.put("daily_override", true);
                            edited = true;
                        }
                    }
                }
                else if (end_methods.contains(entry.get("method")) && end != null){
                    entry.put("datetime", end);
                    edited = true;
                }
                else if (start_methods.contains(entry.get("method")) && start != null){
                    entry.put("datetime", start);
                    edited = true;
                }
                else if (entry.get("method").equals("COUNT")){
                    List<String> notes = gson.fromJson((String) entry.get("notes"), new TypeToken<List<String>>(){}.getType());
                    if (notes.get(0).equals("Controlled prescription was reactivated and count was set through editing function by Admin.")){
                        reactivate_controlled = true;
                    }
                }
                if (edited){
                    entry.put("edits", (int) entry.get("edits") + 1);
                }
                entries_db.update(entry, new String[]{"id="+(int)entry.get("id")});
            }
        }
    }

    public void wipe(String title, Runnable page) {

        getSupportActionBar().setTitle(title);

        screen.removeAllViews();
        screen.addView(scroll);
        scroll_child.removeAllViews();

        if (page != null){
            history.add(0, page);
            if (history.size() > 100){
                history.remove(100);
            }
        }
    }

    public String datepicker_to_date(DatePicker calendar){
        String day;
        if (calendar.getDayOfMonth() < 10) {
            day = "0" + calendar.getDayOfMonth();
        } else {
            day = String.valueOf(calendar.getDayOfMonth());
        }

        String start_year_string = String.valueOf(calendar.getYear()).substring(String.valueOf(calendar.getYear()).length() - 2);

        return calendar.getMonth() + 1 + "/" + day + "/" + start_year_string;
    }

    public String timepicker_to_time(TimePicker clock){
        String hour;
        if (clock.getHour() < 10) {
            hour = "0" + clock.getHour();
        } else {
            hour = String.valueOf(clock.getHour());
        }

        String minute;
        if (clock.getMinute() < 10) {
            minute = "0" + clock.getMinute();
        } else {
            minute = String.valueOf(clock.getMinute());
        }

        return hour + ":" + minute;
    }



    public String long_to_date(Long date) {
        SimpleDateFormat df2 = new SimpleDateFormat("MM/dd/yy");
        return df2.format(date);
    }

    public int[] long_to_full_date(Long date) {
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy/MM/dd/HH/mm");
        String[] split = df2.format(date).split("/");
        int[] mo_day_yr = new int[5];
        for (int i = 0; i < split.length; i++){
            mo_day_yr[i] = Integer.parseInt(split[i]);
        }

        return mo_day_yr;
    }

    public String long_to_datetime(Long date) {
        SimpleDateFormat df2 = new SimpleDateFormat("MM/dd/yy HH:mm");
        return df2.format(date);
    }

    public String longs_to_range(Long start, Long end) {
        SimpleDateFormat df2 = new SimpleDateFormat("MM/dd/yy");
        return " " + df2.format(start) + " - " + df2.format(end);
    }

    public String long_to_time(Long date) {
        SimpleDateFormat df2 = new SimpleDateFormat("HH:mm");
        return df2.format(date);
    }

    public String next_day(String date, int days) {
        SimpleDateFormat df2 = new SimpleDateFormat("MM/dd/yy");

        Calendar calendar = Calendar.getInstance();
        try{
            calendar.setTime(df2.parse(date));
            calendar.add(Calendar.DATE, days);
        }
        catch(ParseException e){
            e.printStackTrace();
        }

        return df2.format(calendar.getTime());
    }

    public Long date_to_long(String date) {
        SimpleDateFormat f = new SimpleDateFormat("MM/dd/yy");
        Date d = null;
        try {
            d = f.parse(date);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return d.getTime();
    }

    public Long datetime_to_long(String datetime) {
        SimpleDateFormat f = new SimpleDateFormat("MM/dd/yy HH:mm");
        Date d = null;
        try {
            d = f.parse(datetime);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return d.getTime();
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

    public void auto_reset(){

        long time = datetime_to_long("12/01/20 01:00");

        List<String> names = Arrays.asList("CHARLIE CLIENT", "DANTE DEMO", "ERIC EXAMPLE", "PATRICK PATIENT");

        String ids = "(";
        for (int i = 0; i < names.size(); i++){
            ids += "'" + names.get(i) + "', ";
        }
        ids = ids.substring(0, ids.length() - 2) + ")";

        List<Object> auto_id = clients_db.getSingleColumn("id", new String[]{"name IN "+ids, "admit="+time},
                null, false);

        for (int i = 0; i < auto_id.size(); i++){
            int client = (int) auto_id.get(i);
            clients_db.delete_single_constraint("id="+client);
            prescriptions_db.delete_single_constraint("client_id="+client);
            entries_db.delete_single_constraint("client_id="+client);
        }

        List<Long> entry_times = new ArrayList<>();
        entry_times.add(datetime_to_long("12/02/20 02:00"));
        entry_times.add(datetime_to_long("12/02/20 03:00"));
        entry_times.add(datetime_to_long("12/02/20 04:00"));
        entry_times.add(datetime_to_long("12/03/20 02:00"));
        entry_times.add(datetime_to_long("12/03/20 03:00"));
        entry_times.add(datetime_to_long("12/03/20 04:00"));
        entry_times.add(datetime_to_long("12/04/20 02:00"));
        entry_times.add(datetime_to_long("12/04/20 03:00"));
        entry_times.add(datetime_to_long("12/04/20 04:00"));
        entry_times.add(datetime_to_long("12/05/20 02:00"));
        entry_times.add(datetime_to_long("12/05/20 03:00"));
        entry_times.add(datetime_to_long("12/05/20 04:00"));
        entry_times.add(datetime_to_long("12/06/20 02:00"));
        entry_times.add(datetime_to_long("12/06/20 03:00"));
        entry_times.add(datetime_to_long("12/06/20 04:00"));
        entry_times.add(datetime_to_long("12/07/20 02:00"));
        entry_times.add(datetime_to_long("12/07/20 03:00"));
        entry_times.add(datetime_to_long("12/07/20 04:00"));
        entry_times.add(datetime_to_long("12/08/20 02:00"));
        entry_times.add(datetime_to_long("12/08/20 03:00"));
        entry_times.add(datetime_to_long("12/08/20 04:00"));
        entry_times.add(datetime_to_long("12/09/20 02:00"));
        entry_times.add(datetime_to_long("12/09/20 03:00"));
        entry_times.add(datetime_to_long("12/09/20 04:00"));

        for (int i = 0; i < names.size(); i++){

            Map<String, Object> client = create_client(names.get(i), time);

            int client_id = clients_db.addRow(client);

            Map<String, Object> gabapentin = create_prescription(client_id, "Gabapentin", "600 MG", (float)1.0, (float)3.0,
                    "Take 1 tab 3 times daily", false, true, (float)30, "Pain",
                    "Dr. Gabriella", "CVS", time);

            Map<String, Object> clonazepam = create_prescription(client_id, "Clonazepam", "1 MG", (float)1.0, (float)2.0,
                    "Take 1 tab up to 2 times daily as needed for anxiety", true, true, (float)30, "Anxiety",
                    "Dr. Clark", "Walgreens", time);

            Map<String, Object> ibuprofen = create_prescription(client_id, "Ibuprofen", "200 MG", (float)2.0, (float)8.0,
                    "Take 1-2 tabs up to 4 times daily as needed for pain", true, false, null, "Pain",
                    "Dr. Ichabod", "BMC", time);

            Map<String, Object> sertraline = create_prescription(client_id, "Sertraline", "100 MG", (float)2.0, (float)2.0,
                    "Take 2 tabs once daily", false, false, null, "Depression",
                    "Dr. Serena", "MGH", time);

            List<Map<String, Object>> prescriptions = new ArrayList<>();

            prescriptions.add(gabapentin);
            prescriptions.add(sertraline);
            prescriptions.add(ibuprofen);
            prescriptions.add(clonazepam);

            for (int j = 0; j < prescriptions.size(); j++){
                Map<String, Object> prescription = prescriptions.get(j);

                int id = prescriptions_db.addRow(prescription);

                String drug = (String) prescription.get("name");
                Float count = (Float) prescription.get("count");

                entries_db.addRow(create_entry(client_id, id, drug, (float)0, count, count, time, false, false, 0, "INTAKE",
                        null, null, null, new ArrayList<>()));

                for (int k = 0; k < entry_times.size(); k++){
                    Float sub = null;
                    if (count != null){
                        sub = count - 1;
                    }
                    entries_db.addRow(create_entry(client_id, id, drug, count, (float) 1, sub, entry_times.get(k), false, false, 0, "TOOK MEDS",
                            null, null, null, new ArrayList<>()));
                    if (count != null){
                        count -= 1;
                    }
                }
                prescription.put("count", count);
                prescriptions_db.update(prescription, new String[]{"id="+id});

                swap_prescriptions(id, (float) -1, (float) -1, id, (float)prescription.get("dose_max"),
                        (float)prescription.get("daily_max"), time, (String)prescription.get("name"));

            }
        }
    }
}