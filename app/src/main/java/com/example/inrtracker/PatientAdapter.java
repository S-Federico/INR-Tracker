package com.example.inrtracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    private List<Patient> patientList;
    private OnItemClickListener listener;
    private DatabaseHelper dbHelper;  // Variabile di istanza

    // Dichiarazione dell'interfaccia OnItemClickListener
    public interface OnItemClickListener {
        void onEditClick(Patient patient);
        void onDeleteClick(Patient patient);
        void onItemClick(Patient patient);
    }

    // Costruttore per inizializzare la lista di pazienti, il DatabaseHelper e il listener
    public PatientAdapter(List<Patient> patientList, DatabaseHelper dbHelper, OnItemClickListener listener) {
        this.patientList = patientList;
        this.dbHelper = dbHelper;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patientList.get(position);
        holder.bind(patient, listener);
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    // Classe interna per il ViewHolder del paziente
    class PatientViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView;
        private TextView nextDosageTextView;
        private ImageButton editButton;
        private ImageButton deleteButton;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewPatientName);
            nextDosageTextView = itemView.findViewById(R.id.textViewNextDosage);
            editButton = itemView.findViewById(R.id.buttonEditPatient);
            deleteButton = itemView.findViewById(R.id.buttonDeletePatient);
        }

        public void bind(Patient patient, OnItemClickListener listener) {
            nameTextView.setText(patient.getName());

            // Calcola la prossima dose di Coumadin o mostra un placeholder
            String nextDosage = calculateNextDosage(patient);
            nextDosageTextView.setText("Prossima dose: \n" + nextDosage);

            // Gestisci il click sul pulsante di modifica
            editButton.setOnClickListener(v -> listener.onEditClick(patient));

            // Gestisci il click sul pulsante di cancellazione
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(patient));

            // Gestisci il click sull'intera riga del paziente
            itemView.setOnClickListener(v -> listener.onItemClick(patient));
        }

        private String calculateNextDosage(Patient patient) {
            // Ottieni i record storici del paziente dal database
            List<HistoryRecord> historyList = dbHelper.getRecordsForPatient(patient.getId());

            // Verifica se ci sono dati storici
            if (historyList.isEmpty()) {
                return "-"; // Se non ci sono dati, mostra un placeholder
            }

            // Calcola l'ultima dose e la prossima data
            double targetINR = dbHelper.getTargetINR(patient.getId());
            String lastINRStr = historyList.get(historyList.size() - 1).getInr();
            double lastINR = parseDoubleSafe(lastINRStr);
            double lastDosage = parseDoubleSafe(patient.getDosage());

            // Logica per calcolare la prossima dose
            double a = calculateA(historyList);
            double b = calculateB(historyList);

            double nextDosage = lastDosage + (targetINR - lastINR) / a - b;
            String nextDate = calculateNextDate();

            // Formatta il testo da visualizzare
            return nextDate + " - " + nextDosage + " mg";
        }

        // Metodo per calcolare la prossima data
        private String calculateNextDate() {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1); // Ad esempio, un giorno dopo
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.format(calendar.getTime());
        }

        // Metodi per calcolare A e B
        private double calculateA(List<HistoryRecord> historyList) {
            double totalIncrease = 0;
            int count = 0;

            for (int i = 1; i < historyList.size(); i++) {
                HistoryRecord previousRecord = historyList.get(i - 1);
                HistoryRecord currentRecord = historyList.get(i);

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

            return count > 0 ? totalIncrease / count : 0.5; // Default a 0.5 se non ci sono dati sufficienti
        }

        private double calculateB(List<HistoryRecord> historyList) {
            double totalDecrease = 0;
            int count = 0;

            for (int i = 1; i < historyList.size(); i++) {
                HistoryRecord previousRecord = historyList.get(i - 1);
                HistoryRecord currentRecord = historyList.get(i);

                if (previousRecord.getDosage() == null && currentRecord.getInr() != null) {
                    double previousINR = parseDoubleSafe(previousRecord.getInr());
                    double currentINR = parseDoubleSafe(currentRecord.getInr());

                    if (currentINR < previousINR) {
                        totalDecrease += (previousINR - currentINR);
                        count++;
                    }
                }
            }

            return count > 0 ? totalDecrease / count : 0.1; // Default a 0.1 se non ci sono dati sufficienti
        }

        private double parseDoubleSafe(String value) {
            if (value == null || value.trim().isEmpty()) {
                return 0.0;
            }
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
    }
}
