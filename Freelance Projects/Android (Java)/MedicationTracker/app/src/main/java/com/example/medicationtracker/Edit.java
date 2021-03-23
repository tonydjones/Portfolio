package com.example.medicationtracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
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
import android.widget.TimePicker;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.DatepickerData.datepicker_data;
import static com.example.medicationtracker.EditScreen.edit_screen;
import static com.example.medicationtracker.LongsToRange.longs_to_range;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.entries_db;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.MainActivity.prescriptions_db;
import static com.example.medicationtracker.UpdatePrescriptions.update_prescriptions;
import static com.example.medicationtracker.UpdateTimes.update_times;
import static com.example.medicationtracker.Wipe.wipe;

public class Edit {
    public static void edit(MainActivity Activity, Map<String, Integer> packet) {

        wipe(Activity, "Edit Data", () -> edit(Activity, packet));
        LinearLayout scroll_child = Activity.scroll_child;

        final List<Map<String, Object>>[] client_list = new List[]{clients_db.getRows(null,
                new String[]{"class='client'"}, new String[]{"active", "DESC", "name", "ASC", "admit", "ASC"}, false)};

        final List<String>[] client_names = new List[]{new ArrayList<>()};

        final List<Map<String, Object>>[] prescription_list = new List[]{new ArrayList<>()};
        final List<String>[] prescription_names = new List[]{new ArrayList<>()};

        final List<String>[] time_list = new List[]{new ArrayList<>()};
        final List<Long>[] datetime_list = new List[]{new ArrayList<>()};

        DatePicker single_date = new DatePicker(Activity);

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

        Spinner type_picker = new Spinner(Activity);
        Spinner client_picker = new Spinner(Activity);
        Spinner prescription_picker = new Spinner(Activity);
        Spinner time_picker = new Spinner(Activity);

        Button delete = new Button(Activity);

        single_date.setMaxDate(System.currentTimeMillis());

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(Activity, android.R.layout.simple_spinner_item, new ArrayList<>(Arrays.asList("Client", "Prescription", "Entry Group", "Single Entry")));
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type_picker.setAdapter(typeAdapter);

        ArrayAdapter<String> clientAdapter = new ArrayAdapter<>(Activity, android.R.layout.simple_spinner_item, client_names[0]);
        clientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        client_picker.setAdapter(clientAdapter);

        ArrayAdapter<String> prescriptionAdapter = new ArrayAdapter<>(Activity, android.R.layout.simple_spinner_item, prescription_names[0]);
        prescriptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prescription_picker.setAdapter(prescriptionAdapter);

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(Activity, android.R.layout.simple_spinner_item, time_list[0]);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        time_picker.setAdapter(timeAdapter);

        type_picker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Log.e("type", "changed");

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
                if (packet != null && packet.get("type") >= 2){
                    if (packet.get("time") >= time_picker.getCount()){
                        time_picker.setSelection(packet.get("time") - 1);
                    }
                    else {
                        time_picker.setSelection(packet.get("time"));
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
                Log.e("client", "changed");
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
                Log.e("prescription", "changed");
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

                Log.e("date", "changed");

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
                    Map<String, Integer> new_packet = new HashMap<>();
                    new_packet.put("type", 0);
                    new_packet.put("client", client_picker.getSelectedItemPosition());

                    history.remove(0);
                    history.add(0, () -> edit(Activity, new_packet));

                    edit_screen(Activity, clients_db.getRows(null, new String[]{"id=" + (int) client_list[0].get(chosen_client[0]).get("id")},
                            null, false), type);
                }
                else if (type.equals("Prescription") && prescription_list[0].size() > 0) {

                    Map<String, Integer> new_packet = new HashMap<>();
                    new_packet.put("type", 1);
                    new_packet.put("client", client_picker.getSelectedItemPosition());
                    new_packet.put("prescription", prescription_picker.getSelectedItemPosition());

                    history.remove(0);
                    history.add(0, () -> edit(Activity, new_packet));

                    edit_screen(Activity, prescriptions_db.getRows(null,
                            new String[]{"id=" + (int) prescription_list[0].get(prescription_picker.getSelectedItemPosition()).get("id")},
                            null, false), type);
                }
                else if (datetime_list[0].size() > 0) {

                    if (type.equals("Entry Group")){

                        Map<String, Integer> new_packet = new HashMap<>();
                        new_packet.put("type", 2);
                        new_packet.put("client", client_picker.getSelectedItemPosition());
                        new_packet.put("time", time_picker.getSelectedItemPosition());
                        Map<String, Integer> date_map = datepicker_data(single_date);
                        new_packet.put("year", date_map.get("year"));
                        new_packet.put("month", date_map.get("month"));
                        new_packet.put("day", date_map.get("day"));

                        history.remove(0);
                        history.add(0, () -> edit(Activity, new_packet));

                        edit_screen(Activity, entries_db.getRows(null,
                                new String[]{"datetime=" + datetime_list[0].get(time_picker.getSelectedItemPosition()),
                                        "client_id=" + (int) client_list[0].get(chosen_client[0]).get("id")},
                                new String[]{"drug", "ASC"}, false), type);

                    }
                    else if (prescription_list[0].size() > 0){

                        Map<String, Integer> new_packet = new HashMap<>();
                        new_packet.put("type", 3);
                        new_packet.put("client", client_picker.getSelectedItemPosition());
                        new_packet.put("prescription", prescription_picker.getSelectedItemPosition());
                        new_packet.put("time", time_picker.getSelectedItemPosition());
                        Map<String, Integer> date_map = datepicker_data(single_date);
                        new_packet.put("year", date_map.get("year"));
                        new_packet.put("month", date_map.get("month"));
                        new_packet.put("day", date_map.get("day"));

                        history.remove(0);
                        history.add(0, () -> edit(Activity, new_packet));

                        edit_screen(Activity, entries_db.getRows(null,
                                new String[]{"datetime=" + datetime_list[0].get(time_picker.getSelectedItemPosition()),
                                        "prescription_id=" + (int) prescription_list[0].get(prescription_picker.getSelectedItemPosition()).get("id")},
                                new String[]{"drug", "ASC"}, false), type);

                    }
                }
            }
        });
        scroll_child.addView(delete);

        if (packet != null){
            if (packet.get("type") == 0){
                type_picker.setSelection(0);
                client_picker.setSelection(packet.get("client"));
            }
            else if (packet.get("type") == 1){
                type_picker.setSelection(1);
                client_picker.setSelection(packet.get("client"));
                prescription_picker.setSelection(packet.get("prescription"));
            }
            else {
                type_picker.setSelection(packet.get("type"));
                client_picker.setSelection(packet.get("client"));
                single_date.updateDate(packet.get("year"), packet.get("month"), packet.get("day"));

                if (packet.get("type") == 3){
                    prescription_picker.setSelection(packet.get("prescription"));
                }
            }
        }
    }
}
