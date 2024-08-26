package com.example.inrtracker;

import android.view.LayoutInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    private List<Patient> patientList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Patient patient);
    }

    public PatientAdapter(List<Patient> patientList, OnItemClickListener listener) {
        this.patientList = patientList;
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

    static class PatientViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView;
        private TextView dosageTextView;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewPatientName);
            dosageTextView = itemView.findViewById(R.id.textViewDosage);
        }

        public void bind(Patient patient, OnItemClickListener listener) {
            nameTextView.setText(patient.getName());
            dosageTextView.setText(patient.getDosage());
            itemView.setOnClickListener(v -> listener.onItemClick(patient));
        }
    }
}
