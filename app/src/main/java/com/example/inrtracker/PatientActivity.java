package com.example.inrtracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PatientActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistory;
    private HistoryAdapter adapter;
    private List<HistoryRecord> historyList;
    private String patientName;
    private String dosage;
    private DatabaseHelper dbHelper;
    private int patientId;
    private double targetINR = 2.5; // Default target INR
    private TextView textViewTargetINR, textViewEstimatedDosage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        patientName = getIntent().getStringExtra("patientName");
        dosage = getIntent().getStringExtra("dosage");

        dbHelper = new DatabaseHelper(this);
        patientId = dbHelper.getPatientIdByName(patientName);
        targetINR = dbHelper.getTargetINR(patientId); // Recupera il target INR dal DB

        TextView textViewPatientName = findViewById(R.id.textViewPatientName);
        textViewTargetINR = findViewById(R.id.textViewTargetINR);
        textViewEstimatedDosage = findViewById(R.id.textViewEstimatedDosage);
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        FloatingActionButton fabAddRecord = findViewById(R.id.fabAddRecord);
        Button fabSetTargetINR = findViewById(R.id.buttonSetTargetINR);

        Button buttonViewTherapeuticPlan = findViewById(R.id.buttonViewTherapeuticPlan);
        buttonViewTherapeuticPlan.setOnClickListener(v -> showTherapeuticPlan());

        textViewPatientName.setText(patientName);

        // Inizializza historyList come una lista vuota se è null
        historyList = dbHelper.getRecordsForPatient(patientId);
        if (historyList == null) {
            historyList = new ArrayList<>();
        }

        adapter = new HistoryAdapter(historyList, new HistoryAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(HistoryRecord record) {
                showEditRecordDialog(record);
            }

            @Override
            public void onDeleteClick(HistoryRecord record) {
                showDeleteRecordDialog(record);
            }
        });

        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(adapter);

        updateTargetINRUI();
        updateEstimatedDosageUI();

        fabAddRecord.setOnClickListener(v -> showAddRecordDialog());
        fabSetTargetINR.setOnClickListener(v -> showSetTargetINRDialog());

        textViewPatientName.setOnLongClickListener(v -> {
            showDeletePatientDialog();
            return true;
        });
    }

    private void showEditRecordDialog(HistoryRecord record) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_record, null);
        builder.setView(dialogView);

        EditText editTextINR = dialogView.findViewById(R.id.editTextINR);
        EditText editTextDosage = dialogView.findViewById(R.id.editTextDosage);
        EditText editTextDate = dialogView.findViewById(R.id.editTextDate);

        editTextINR.setText(record.getInr());
        editTextDosage.setText(record.getDosage());
        editTextDate.setText(record.getDate());

        builder.setPositiveButton("OK", (dialog, which) -> {
            record.setInr(editTextINR.getText().toString());
            record.setDosage(editTextDosage.getText().toString());
            record.setDate(editTextDate.getText().toString());

            dbHelper.updateRecord(record); // Aggiorna il record nel database
            adapter.notifyDataSetChanged(); // Aggiorna l'adattatore
            onRecordUpdated(); // Chiama per aggiornare i calcoli
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.create().show();
    }

    private void updateTargetINRUI() {
        textViewTargetINR.setText("Target INR: " + targetINR);
    }

    // Modifica updateEstimatedDosageUI per includere la data e la dose di mantenimento
    private void updateEstimatedDosageUI() {
        double estimatedDosage = calculateDosageBasedOnHistory(targetINR);
        String nextDate = calculateNextDate(); // Implementa la logica per ottenere la prossima data

        textViewEstimatedDosage.setText("Prossima dose: \n" + nextDate + " " + estimatedDosage + " mg");

        double maintenanceDosage = calculateMaintenanceDosage();
        TextView textViewMaintenanceDosage = findViewById(R.id.textViewMaintenanceDosage);
        textViewMaintenanceDosage.setText("Dose di mantenimento: \n" + maintenanceDosage + " mg");

        dbHelper.updateDosage(patientId, estimatedDosage); // Salva il dosaggio stimato nel DB
        dbHelper.updateMaintenanceDosage(patientId, maintenanceDosage); // Salva la dose di mantenimento nel DB
    }

    // Calcolo della dose di mantenimento basata su a e b
    private double calculateMaintenanceDosage() {
        double a = calculateA();
        double b = calculateB();
        return targetINR / a + b; // Calcola la dose di mantenimento utilizzando i valori di a e b
    }


    // Calcolo della prossima data
    private String calculateNextDate() {
        // Logica per calcolare la prossima data in cui somministrare la dose
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1); // Ad esempio, un giorno dopo
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    private double calculateDosageBasedOnHistory(double targetINR) {
        if (historyList.isEmpty()) {
            if (isNumeric(dosage)) {
                return Double.parseDouble(dosage);
            } else {
                return 0.0; // Valore di default se il dosaggio non è valido
            }
        }

        String lastINRStr = historyList.get(historyList.size() - 1).getInr();
        double lastINR = parseDoubleSafe(lastINRStr);

        // Logica migliorata per evitare dosaggi negativi
        double calculatedDosage = Double.parseDouble(dosage);
        if (lastINR < targetINR) {
            calculatedDosage += 0.5;
        } else {
            calculatedDosage -= 0.5;
        }

        // Non permettere dosaggi negativi
        return Math.max(0, calculatedDosage);
    }

    private double parseDoubleSafe(String value) {
        if (isNumeric(value)) {
            return Double.parseDouble(value);
        } else {
            return 0.0; // Oppure un altro valore di default
        }
    }

    private boolean isNumeric(String value) {
        if (value == null || value.equals("N/A") || value.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showSetTargetINRDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_set_target_inr, null);
        builder.setView(dialogView);

        EditText editTextTargetINR = dialogView.findViewById(R.id.editTextTargetINR);
        EditText editTextDaysToTarget = dialogView.findViewById(R.id.editTextDaysToTarget);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String target = editTextTargetINR.getText().toString();
            String days = editTextDaysToTarget.getText().toString();

            if (!target.isEmpty() && !days.isEmpty()) {
                targetINR = Double.parseDouble(target);
                int daysToTarget = Integer.parseInt(days);
                dbHelper.updateTargetINR(patientId, targetINR);
                dbHelper.updateDaysToTarget(patientId, daysToTarget);
                updateTargetINRUI();
                onRecordUpdated(); // Ricalcola il dosaggio stimato e aggiorna la UI
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.create().show();
    }

    // Aggiungi questo metodo per visualizzare il piano terapeutico
    private void showTherapeuticPlan() {
        int daysToTarget = dbHelper.getDaysToTarget(patientId);
        List<String> plan = new ArrayList<>();
        double currentINR = parseDoubleSafe(historyList.get(historyList.size() - 1).getInr());
        double dosage = parseDoubleSafe(this.dosage);

        double a = calculateA(); // Calcola a dai dati
        double b = calculateB(); // Calcola b dai dati

        for (int i = 1; i <= daysToTarget; i++) {
            // Calcola la dose per ogni giorno usando l'equazione n(x*a - b) = y
            dosage = (targetINR + (b * i)) / (a * i);

            // Aggiungi al piano terapeutico
            plan.add("Giorno " + i + ": " + dosage + " mg");
        }

        // Mostra il piano terapeutico in un AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Piano Terapeutico");
        builder.setItems(plan.toArray(new String[0]), null);
        builder.setPositiveButton("OK", null);
        builder.show();
    }



    private void showDeleteRecordDialog(HistoryRecord record) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Sì", (dialog, which) -> {
            dbHelper.deleteRecord(record.getId()); // Usa il metodo getId()
            historyList.remove(record);
            adapter.notifyDataSetChanged();
            onRecordUpdated(); // Ricalcola e aggiorna tutto dopo la cancellazione
        });
        builder.setTitle("Elimina Record")
                .setMessage("Sei sicuro di voler eliminare questo record?")
                .setPositiveButton("Sì", (dialog, which) -> {
                    dbHelper.deleteRecord(record.getId()); // Usa il metodo getId()
                    historyList.remove(record);
                    adapter.notifyDataSetChanged();
                    updateEstimatedDosageUI(); // Ricalcola il dosaggio stimato dopo la cancellazione
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void showDeletePatientDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Elimina Paziente")
                .setMessage("Sei sicuro di voler eliminare questo paziente?")
                .setPositiveButton("Sì", (dialog, which) -> {
                    dbHelper.deletePatient(patientId);
                    finish(); // Chiudi l'attività corrente
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void onRecordUpdated() {
        // Ricalcola a e b dai dati disponibili
        double a = calculateA();
        double b = calculateB();

        // Aggiorna la UI per la dose stimata basata sulla storia
        updateEstimatedDosageUI();

        // Calcola e aggiorna la dose di mantenimento
        double maintenanceDosage = calculateMaintenanceDosage();
        dbHelper.updateMaintenanceDosage(patientId, maintenanceDosage);
        TextView textViewMaintenanceDosage = findViewById(R.id.textViewMaintenanceDosage);
        textViewMaintenanceDosage.setText("Dose di mantenimento: " + maintenanceDosage + " mg");

        // Aggiorna il piano terapeutico basato su a, b e il numero di giorni per raggiungere il target INR
        showTherapeuticPlan();
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
                    (view, year, monthOfYear, dayOfMonth) -> editTextDate.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth),
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            String date = editTextDate.getText().toString();
            String inr = editTextINR.getText().toString();
            String dosage = editTextDosage.getText().toString();

            if (!inr.isEmpty() && dosage.isEmpty()) {
                HistoryRecord newRecord = new HistoryRecord(date, inr, null);
                dbHelper.addRecord(patientId, newRecord);
                historyList.add(newRecord);
            } else if (inr.isEmpty() && !dosage.isEmpty()) {
                HistoryRecord newRecord = new HistoryRecord(date, null, dosage);
                dbHelper.addRecord(patientId, newRecord);
                historyList.add(newRecord);
            } else if (!inr.isEmpty() && !dosage.isEmpty()) {
                HistoryRecord newRecordINR = new HistoryRecord(date, inr, null);
                HistoryRecord newRecordDosage = new HistoryRecord(date, null, dosage);
                dbHelper.addRecord(patientId, newRecordINR);
                dbHelper.addRecord(patientId, newRecordDosage);
                historyList.add(newRecordINR);
                historyList.add(newRecordDosage);
            }
            adapter.notifyDataSetChanged();
            onRecordUpdated(); // Chiama per aggiornare i calcoli
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.create().show();
    }

    private double calculateDosage(double currentINR, double targetINR) {
        // Calcola il dosaggio basato sul target INR e l'INR corrente
        return currentINR < targetINR ? Double.parseDouble(dosage) + 0.5 : Double.parseDouble(dosage) - 0.5;
    }

    private double calculateA() {
        double totalIncrease = 0;
        int count = 0;

        for (int i = 1; i < historyList.size(); i++) {
            HistoryRecord previousRecord = historyList.get(i - 1);
            HistoryRecord currentRecord = historyList.get(i);

            // Considera solo i casi in cui è stata somministrata una dose e l'INR è aumentato
            if (previousRecord.getDosage() != null && currentRecord.getInr() != null) {
                double previousINR = parseDoubleSafe(previousRecord.getInr());
                double currentINR = parseDoubleSafe(currentRecord.getInr());
                double dosage = parseDoubleSafe(previousRecord.getDosage());

                if (currentINR > previousINR && dosage > 0) {
                    totalIncrease += (currentINR - previousINR) / dosage;
                    count++;
                }
            }
        }

        // Restituisce l'incremento medio di INR per unità di dose
        return count > 0 ? totalIncrease / count : 0.5; // Default a 0.5 se non ci sono dati sufficienti
    }

    private double calculateB() {
        double totalDecrease = 0;
        int count = 0;

        for (int i = 1; i < historyList.size(); i++) {
            HistoryRecord previousRecord = historyList.get(i - 1);
            HistoryRecord currentRecord = historyList.get(i);

            // Considera solo i casi in cui non è stata somministrata una dose e l'INR è diminuito
            if (previousRecord.getDosage() == null && currentRecord.getInr() != null) {
                double previousINR = parseDoubleSafe(previousRecord.getInr());
                double currentINR = parseDoubleSafe(currentRecord.getInr());

                if (currentINR < previousINR) {
                    totalDecrease += (previousINR - currentINR);
                    count++;
                }
            }
        }

        // Restituisce la diminuzione media dell'INR per giorno
        return count > 0 ? totalDecrease / count : 0.1; // Default a 0.1 se non ci sono dati sufficienti
    }

}
