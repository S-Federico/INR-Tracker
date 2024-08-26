package com.example.inrtracker;
// File: java/com/tuo/package/name/MainActivity.java

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PatientAdapter adapter;
    private List<Patient> patientList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewPatients);
        FloatingActionButton fabAddPatient = findViewById(R.id.fabAddPatient);

        dbHelper = new DatabaseHelper(this);
        patientList = dbHelper.getAllPatients();

        adapter = new PatientAdapter(patientList, this::onPatientClicked);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fabAddPatient.setOnClickListener(v -> showAddPatientDialog());
    }

    private void showAddPatientDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_patient, null);
        builder.setView(dialogView);

        EditText editTextName = dialogView.findViewById(R.id.editTextPatientName);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String name = editTextName.getText().toString();
            if (!name.isEmpty()) {
                Patient newPatient = new Patient(name);
                dbHelper.addPatient(newPatient);
                patientList.add(newPatient);
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.create().show();
    }

    private void onPatientClicked(Patient patient) {
        Intent intent = new Intent(MainActivity.this, PatientActivity.class);
        intent.putExtra("patientName", patient.getName());
        intent.putExtra("dosage", patient.getDosage());
        startActivity(intent);
    }
}
