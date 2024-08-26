package com.example.inrtracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryRecord> historyList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(HistoryRecord record);
        void onDeleteClick(HistoryRecord record);
    }

    public HistoryAdapter(List<HistoryRecord> historyList, OnItemClickListener listener) {
        this.historyList = historyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryRecord record = historyList.get(position);
        holder.bind(record, listener);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {

        private TextView dateTextView;
        private TextView inrTextView;
        private TextView dosageTextView;
        private ImageButton editButton;
        private ImageButton deleteButton;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.textViewDate);
            inrTextView = itemView.findViewById(R.id.textViewINR);
            dosageTextView = itemView.findViewById(R.id.textViewDosage);
            editButton = itemView.findViewById(R.id.buttonEditRecord);
            deleteButton = itemView.findViewById(R.id.buttonDeleteRecord);
        }

        public void bind(HistoryRecord record, OnItemClickListener listener) {
            dateTextView.setText(record.getDate());

            // Mostra " - " se INR o Dosaggio non sono presenti
            inrTextView.setText("INR: " + (record.getInr() != null ? record.getInr() : "-"));
            dosageTextView.setText("Dosaggio Cumadin: " + (record.getDosage() != null ? record.getDosage() : "-"));

            // Gestisci il click sul pulsante di modifica
            editButton.setOnClickListener(v -> listener.onEditClick(record));

            // Gestisci il click sul pulsante di cancellazione
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(record));
        }
    }
}
