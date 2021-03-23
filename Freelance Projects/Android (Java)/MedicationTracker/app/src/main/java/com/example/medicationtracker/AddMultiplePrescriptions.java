package com.example.medicationtracker;

import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.poi.sl.usermodel.Line;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.medicationtracker.Check_User.check_user;
import static com.example.medicationtracker.Clients.clients;
import static com.example.medicationtracker.Create_Client.create_client;
import static com.example.medicationtracker.MainActivity.clients_db;
import static com.example.medicationtracker.MainActivity.warning;
import static com.example.medicationtracker.MainActivity.history;
import static com.example.medicationtracker.MainActivity.weighted_params;
import static com.example.medicationtracker.NewPrescription.new_prescription;
import static com.example.medicationtracker.NewPrescriptionSignoff.new_prescription_signoff;
import static com.example.medicationtracker.UI_Message.ui_message;
import static com.example.medicationtracker.Wipe.wipe;

public class AddMultiplePrescriptions {
    public static void add_multiple_prescriptions(MainActivity Activity, String name, Integer client_id, List<Map<String, Object>> prescriptions, String pass) {
        List<Map<String, Object>> prescription_copy = new ArrayList<>();
        prescription_copy.addAll(prescriptions);
        if (name != null){
            wipe(Activity, name + " Intake Prescriptions", () -> add_multiple_prescriptions(Activity, name, client_id, prescription_copy, pass));
        }
        else{
            wipe(Activity, "New Prescriptions", () -> add_multiple_prescriptions(Activity, name, client_id, prescription_copy, pass));
        }
        LinearLayout scroll_child = Activity.scroll_child;

        if (prescriptions.size() > 0){

            for (int i = 0; i < prescriptions.size(); i++) {
                Map<String, Object> prescription = prescriptions.get(i);

                String text = (String) prescription.get("drug");
                text += "\n" + prescription.get("dose");
                text += "\n" + prescription.get("instructions");

                if ((boolean) prescription.get("as_needed")) {
                    text += "\nTo be taken as needed";
                }

                if ((boolean) prescription.get("controlled")) {
                    text += "\nCONTROLLED";
                }

                if (prescription.get("count") != null) {
                    text += "\nStarting Count: " + prescription.get("count");
                }

                if (prescription.get("dose_max") != null) {
                    text += "\nMaximum Dose: " + prescription.get("dose_max");
                }

                if (prescription.get("daily_max") != null) {
                    text += "\nDaily Maximum: " + prescription.get("daily_max");
                }

                if (prescription.get("indication") != null) {
                    text += "\nIndication: " + prescription.get("indication");
                }

                if (prescription.get("prescriber") != null) {
                    text += "\nPrescriber: " + prescription.get("prescriber");
                }

                if (prescription.get("pharmacy") != null) {
                    text += "\nPharmacy: " + prescription.get("pharmacy");
                }
                text += "\n";

                LinearLayout row = new LinearLayout(Activity);
                row.setOrientation(LinearLayout.HORIZONTAL);

                TextView scripts = new TextView(Activity);
                scripts.setText(text);
                Log.e("text", text);
                scripts.setLayoutParams(weighted_params);

                Button edit = new Button(Activity);
                edit.setText("Edit");
                int finalI = i;
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Map<String, Object> prescription = prescriptions.remove(finalI);
                        new_prescription(Activity, name, client_id, prescriptions, pass, prescription);
                    }
                });

                Button delete = new Button(Activity);
                delete.setText("Delete");
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prescriptions.remove(finalI);
                        history.remove(0);
                        add_multiple_prescriptions(Activity, name, client_id, prescriptions, pass);
                    }
                });

                scroll_child.addView(row);
                row.addView(scripts);
                row.addView(edit);
                row.addView(delete);
            }

        }

        Button new_script = new Button(Activity);
        new_script.setText("Add A New Prescription");
        new_script.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new_prescription(Activity, name, client_id, prescriptions, pass, null);
            }
        });
        scroll_child.addView(new_script);

        Button done = new Button(Activity);
        done.setText("No More Prescriptions To Add");
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (prescriptions.size() > 0) {
                    new_prescription_signoff(Activity, name, client_id, prescriptions, pass);
                }
                else if (name != null){
                    EditText pw = new EditText(Activity);
                    pw.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    pw.setHint("Enter Password");
                    warning.setView(pw);
                    warning.setNegativeButton("Cancel", null);
                    warning.setPositiveButton("Add Client", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Map<String, Object> staff = check_user(pw.getText().toString());

                            if (staff == null){
                                warning.setView(null);
                                warning.setNegativeButton("", null);
                                warning.setPositiveButton("OK", null);
                                warning.setTitle("Invalid password");
                                warning.setMessage("That password does not match the password of any current staff members.");
                                warning.show();
                                return;
                            }

                            clients_db.addRow(create_client(name, System.currentTimeMillis(), "client", pass));
                            history.remove(0);
                            history.remove(0);
                            history.remove(0);
                            ui_message(Activity, "Client added to database with no starting prescriptions.");
                            clients(Activity);
                        }
                    });
                    warning.setTitle("No Prescriptions Added");
                    warning.setMessage("Are you sure you want to add this client with no prescriptions? Please enter your password to confirm this action.");
                    warning.show();
                }
            }
        });
        scroll_child.addView(done);
    }
}
