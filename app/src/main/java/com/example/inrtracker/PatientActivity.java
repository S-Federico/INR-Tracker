// File: java/com/example/inrtracker/PatientActivity.java
package com.example.inrtracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Calendar;
import java.util.List;

public class PatientActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistory;
    private HistoryAdapter adapter;
    private List<HistoryRecord> historyList;
    private String patientName;
    private String dosage;
    private DatabaseHelper dbHelper;
    private int patientId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        patientName = getIntent().getStringExtra("patientName");
        dosage = getIntent().getStringExtra("dosage");

        dbHelper = new DatabaseHelper(this);
        patientId = dbHelper.getPatientIdByName(patientName);

        TextView textViewPatientName = findViewById(R.id.textViewPatientName);
        TextView textViewDosage = findViewById(R.id.textViewDosage);
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        FloatingActionButton fabAddRecord = findViewById(R.id.fabAddRecord);

        textViewPatientName.setText(patientName);
        textViewDosage.setText(dosage);

        historyList = dbHelper.getRecordsForPatient(patientId);
        adapter = new HistoryAdapter(historyList);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(adapter);

        fabAddRecord.setOnClickListener(v -> showAddRecordDialog());
    }

    private void showAddRecordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_record, null);
        builder.setView(dialogView);

        EditText editTextINR = dialogView.findViewById(R.id.editTextINR);
        EditText editTextDosage = dialogView.findViewById(R.id.editTextDosage);

        final Calendar calendar = Calendar.getInstance();
        final EditText editTextDate = dialogView.findViewById(R.id.editTextDate);
        editTextDate.setText(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH));

        editTextDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(PatientActivity.this,
                    (DatePicker view, int year, int monthOfYear, int dayOfMonth) -> {
                        editTextDate.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            String date = editTextDate.getText().toString();
            String inr = editTextINR.getText().toString();
            String dosage = editTextDosage.getText().toString();

            if (!inr.isEmpty() || !dosage.isEmpty()) {
                HistoryRecord newRecord = new HistoryRecord(date, inr, dosage);
                dbHelper.addRecord(patientId, newRecord);
                historyList.add(newRecord);
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.create().show();
    }
}
