package com.example.medicationtracker;

import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.Check_Password.check_password;
import static com.example.medicationtracker.Create_Client.create_client;
import static com.example.medicationtracker.DateToLong.date_to_long;
import static com.example.medicationtracker.DatepickerToDate.datepicker_to_date;
import static com.example.medicationtracker.LongToDatetime.long_to_datetime;
import static com.example.medicationtracker.LongsToRange.longs_to_range;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.MainActivity.gson;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.MainActivity.prescriptions_db;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.NextDay.next_day;
import static com.example.medicationtracker.Staff.staff;
import static com.example.medicationtracker.UI_Message.ui_message;
import static com.example.medicationtracker.UpdatePrescriptions.update_prescriptions;
import static com.example.medicationtracker.UpdateTimes.update_times;
import static com.example.medicationtracker.Wipe.wipe;

public class Delete {
    public static void delete(MainActivity Activity) {

        wipe(Activity, "Delete Data", () -> delete(Activity));
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

        List<String> date_list = new ArrayList<>();
        date_list.add("All Dates");
        /*date_list.add("Date Range");
        date_list.add("Single Date");*/
        date_list.add("Up To Selected Date");

        final List<String>[] time_list = new List[]{new ArrayList<>()};
        final List<Long>[] datetime_list = new List[]{new ArrayList<>()};

        String[] chosen_type = {"Clients"};
        int[] chosen_client = {0};
        String[] chosen_prescription = {"All Prescriptions"};
        String[] chosen_date = {"All Dates"};


        Spinner type_picker = new Spinner(Activity);
        Spinner client_picker = new Spinner(Activity);
        Spinner prescription_picker = new Spinner(Activity);
        Spinner date_picker = new Spinner(Activity);
        Spinner time_picker = new Spinner(Activity);

        Button delete = new Button(Activity);

        DatePicker single_date = new DatePicker(Activity);
        DatePicker end_date = new DatePicker(Activity);

        single_date.setMaxDate(System.currentTimeMillis());
        end_date.setMaxDate(System.currentTimeMillis());

        TextView start = new TextView(Activity);
        start.setText("Start Date");

        TextView end = new TextView(Activity);
        end.setText("End Date");

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(Activity, android.R.layout.simple_spinner_item, new ArrayList<>(Arrays.asList("Clients", "Client Prescriptions", "Prescription Records")));
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type_picker.setAdapter(typeAdapter);

        ArrayAdapter<String> clientAdapter = new ArrayAdapter<>(Activity, android.R.layout.simple_spinner_item, client_names[0]);
        clientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        client_picker.setAdapter(clientAdapter);

        ArrayAdapter<String> prescriptionAdapter = new ArrayAdapter<>(Activity, android.R.layout.simple_spinner_item, prescription_names[0]);
        prescriptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prescription_picker.setAdapter(prescriptionAdapter);

        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(Activity, android.R.layout.simple_spinner_item, date_list);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        date_picker.setAdapter(dateAdapter);

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(Activity, android.R.layout.simple_spinner_item, time_list[0]);
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
                    else if(date_picker.getSelectedItem().equals("Up To Selected Date")){
                        chosen_date[0] = "Up To Selected Date";
                        scroll_child.addView(single_date, scroll_child.indexOfChild(date_picker) + 1);

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
                EditText pass = new EditText(Activity);
                pass.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
                pass.setHint("Enter Admin Password");

                warning.setView(pass);
                warning.setNegativeButton("Cancel", null);
                warning.setPositiveButton("Delete Data", new DialogInterface.OnClickListener() {
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
                            String type = (String) type_picker.getSelectedItem();
                            Integer client = null;
                            Integer prescription = null;
                            String date = null;
                            String end = null;
                            Long time = null;

                            String message_string = " Data for ";

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
                                message_string += "All Clients";
                            }
                            else {
                                message_string += client_names[0].get(chosen_client[0]);
                            }

                            if (!type.equals("Clients")){
                                message_string += ", ";

                                if (prescription == null){
                                    message_string += "All Prescriptions";
                                }
                                else {
                                    message_string += prescription_names[0].get(prescription_picker.getSelectedItemPosition());
                                }

                                if (!type.equals("Client Prescriptions")){
                                    message_string += ", ";
                                    if (time != null){
                                        message_string += long_to_datetime(time);
                                    }
                                    else {
                                        if (date == null){
                                            message_string += "All Dates";
                                        }
                                        else{
                                            if (chosen_date[0].equals("Up To Selected Date")){
                                                message_string += "up through ";
                                            }
                                            message_string += date;
                                        }

                                        if (end != null){
                                            message_string += " - " + end;
                                        }
                                    }
                                }
                            }

                            if (client == null){
                                if (type.equals("Clients")){
                                    clients_db.delete_single_constraint("class='client'");
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
                                else if (chosen_date[0].equals("Up To Selected Date")){
                                    long endtime = date_to_long(next_day(date, 1));
                                    entries_db.delete_single_constraint("datetime<"+endtime);
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

                                else if (chosen_date[0].equals("Up To Selected Date")){
                                    long endtime = date_to_long(next_day(date, 1));

                                    time_list[0].clear();
                                    datetime_list[0].clear();

                                    if (prescription == null){
                                        entries_db.deleteRows(new String[]{"datetime<"+endtime, "client_id="+client});
                                        time_list[0].add("No entries for " + client_picker.getSelectedItem() + " on " + date);
                                    }
                                    else{
                                        entries_db.deleteRows(new String[]{"datetime<"+endtime, "prescription_id="+prescription});
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

                            message_string += " successfully deleted.";
                            ui_message(Activity, message_string);
                        }

                    }
                });
                warning.setTitle("Confirm Data Deletion");
                warning.setMessage("Deleted data cannot be viewed and cannot be recovered. Are you sure you wish to proceed with the selected deletions? Please enter your password to confirm. You must be an administrator to do this.");
                warning.show();
            }
        });
        scroll_child.addView(delete);
    }
}
