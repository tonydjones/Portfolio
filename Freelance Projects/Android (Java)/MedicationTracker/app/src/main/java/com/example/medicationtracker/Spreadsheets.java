package com.example.medicationtracker;

import android.content.DialogInterface;
import android.text.InputType;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.Check_Folders.check_folders;
import static com.example.medicationtracker.Check_Password.check_password;
import static com.example.medicationtracker.ClientSheet.client_sheet;
import static com.example.medicationtracker.Create_Client.create_client;
import static com.example.medicationtracker.DateToLong.date_to_long;
import static com.example.medicationtracker.DatepickerToDate.datepicker_to_date;
import static com.example.medicationtracker.Long_To_Date.long_to_date;
import static com.example.medicationtracker.LongsToRange.longs_to_range;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.MainActivity.prescriptions_db;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.NextDay.next_day;
import static com.example.medicationtracker.PrescriptionSheet.prescription_sheet;
import static com.example.medicationtracker.Staff.staff;
import static com.example.medicationtracker.UI_Message.ui_message;
import static com.example.medicationtracker.UpdatePrescriptions.update_prescriptions;
import static com.example.medicationtracker.Wipe.wipe;
import static com.example.medicationtracker.EntrySheet.entry_sheet;
import static com.example.medicationtracker.SaveXLS.save_xls;

public class Spreadsheets {
    public static void spreadsheets(MainActivity Activity) {

        wipe(Activity, "Create Spreadsheets", () -> spreadsheets(Activity));
        LinearLayout scroll_child = Activity.scroll_child;

        final List<Map<String, Object>>[] client_list = new List[]{clients_db.getRows(null,
                new String[]{"class='client'"}, new String[]{"active", "DESC", "name", "ASC", "admit", "ASC"}, false)};

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

        Spinner client_picker = new Spinner(Activity);
        Spinner prescription_picker = new Spinner(Activity);
        Spinner date_picker = new Spinner(Activity);

        Button delete = new Button(Activity);

        DatePicker single_date = new DatePicker(Activity);
        DatePicker end_date = new DatePicker(Activity);

        single_date.setMaxDate(System.currentTimeMillis());
        end_date.setMaxDate(System.currentTimeMillis());

        TextView start = new TextView(Activity);
        start.setText("Start Date");

        TextView end = new TextView(Activity);
        end.setText("End Date");

        CheckBox all = new CheckBox(Activity);
        all.setText("All Data");
        all.setChecked(true);

        CheckBox clients = new CheckBox(Activity);
        clients.setText("Clients");
        clients.setChecked(true);

        CheckBox prescriptions = new CheckBox(Activity);
        prescriptions.setText("Prescriptions");
        prescriptions.setChecked(true);

        CheckBox entries = new CheckBox(Activity);
        entries.setText("Entries");
        entries.setChecked(true);

        CheckBox adherence = new CheckBox(Activity);
        adherence.setText("Include Adherence Data in Prescription Spreadsheet");
        adherence.setChecked(true);

        ArrayAdapter<String> clientAdapter = new ArrayAdapter<>(Activity, android.R.layout.simple_spinner_item, client_names[0]);
        clientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        client_picker.setAdapter(clientAdapter);

        ArrayAdapter<String> prescriptionAdapter = new ArrayAdapter<>(Activity, android.R.layout.simple_spinner_item, prescription_names[0]);
        prescriptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prescription_picker.setAdapter(prescriptionAdapter);

        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(Activity, android.R.layout.simple_spinner_item, date_list);
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
                EditText pass = new EditText(Activity);
                pass.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
                pass.setHint("Enter Admin Password");
                warning.setNegativeButton("Cancel", null);
                warning.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!check_password(pass.getText().toString(), "admin")){
                            pass.setText("");
                            warning.setView(null);
                            warning.setNegativeButton("", null);
                            warning.setPositiveButton("OK", null);
                            warning.setTitle("Invalid password");
                            warning.setMessage("That password does not match the password of any current administrators.");
                            warning.show();
                        }
                        else {
                            if (!all.isChecked() && !clients.isChecked() && !prescriptions.isChecked() && !entries.isChecked()){
                                warning.setView(null);
                                warning.setNegativeButton("", null);
                                warning.setPositiveButton("OK", null);
                                warning.setTitle("No Data Selected");
                                warning.setMessage("No data was selected to produce spreadsheets.");
                                warning.show();
                            }
                            else if (chosen_date[0].equals("Date Range") && !datepicker_to_date(end_date).equals(datepicker_to_date(single_date)) &&
                                    date_to_long(datepicker_to_date(single_date)) >= date_to_long(datepicker_to_date(end_date))){
                                warning.setView(null);
                                warning.setNegativeButton("", null);
                                warning.setPositiveButton("OK", null);
                                warning.setTitle("Invalid Date Range");
                                warning.setMessage("The end date cannot be earlier than the start date");
                                warning.show();
                            }
                            else{
                                warning.setView(null);
                                warning.setNegativeButton("Cancel", null);
                                warning.setPositiveButton("Generate Spreadsheets", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String file_name = "";
                                        Workbook workbook = new HSSFWorkbook();

                                        if (all.isChecked() || (clients.isChecked() && prescriptions.isChecked() && entries.isChecked() &&
                                                adherence.isChecked() && client_picker.getSelectedItemPosition() == 0 &&
                                                prescription_picker.getSelectedItemPosition() == 0 && date_picker.getSelectedItemPosition() == 0)){
                                            List<Map<String, Object>> client = clients_db.getRows(null, new String[]{"class='client'"},
                                                    new String[]{"active", "DESC", "name", "ASC", "admit", "ASC"}, false);

                                            Sheet clients = client_sheet(workbook, "Clients", client);
                                            file_name = "All Data";

                                            String previous_client = "";
                                            int client_counter = 1;

                                            for (int i = 0; i < client.size(); i++){

                                                List<Map<String, Object>> prescription_data = prescriptions_db.getRows(null,
                                                        new String[]{"client_id="+client_list[0].get(i).get("id")},
                                                        new String[]{"name", "ASC", "active", "DESC", "start", "DESC"}, false);

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
                                                client = clients_db.getRows(null, new String[]{"class='client'"},
                                                        new String[]{"active", "DESC", "name", "ASC"}, false);
                                                file_name += "All Clients";
                                                if (prescriptions.isChecked() && entries.isChecked() && adherence.isChecked()){
                                                    client_copy = clients_db.getRows(null, new String[]{"class='client'"},
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
                                            ui_message(Activity, "Spreadsheet Successfully Generated: " + success.second);
                                        }
                                        else {
                                            if (!check_folders(Activity)){
                                                ui_message(Activity, "Error: Necessary folders not present and could not be generated");
                                            }
                                            else {
                                                success = save_xls(workbook, file_name);
                                                if (success.first){
                                                    ui_message(Activity, "Spreadsheet Successfully Generated: " + success.second);
                                                }
                                                else{
                                                    ui_message(Activity, "Something went wrong, spreadsheet was not generated");

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
                    }
                });
                warning.setTitle("Password Required");
                warning.setView(pass);
                warning.setMessage("Please enter your password to generate the selected spreadsheets. Please note you must be an administrator to do this.");
                warning.show();
            }
        });
        scroll_child.addView(delete);
    }
}
