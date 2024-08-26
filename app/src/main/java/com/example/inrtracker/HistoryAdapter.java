// File: java/com/tuo/package/name/HistoryAdapter.java
package com.example.inrtracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryRecord> historyList;

    public HistoryAdapter(List<HistoryRecord> historyList) {
        this.historyList = historyList;
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
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {

        private TextView dateTextView;
        private TextView inrTextView;
        private TextView dosageTextView;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.textViewDate);
            inrTextView = itemView.findViewById(R.id.textViewINR);
            dosageTextView = itemView.findViewById(R.id.textViewDosage);
        }

        public void bind(HistoryRecord record) {
            dateTextView.setText(record.getDate());
            inrTextView.setText("INR: " + record.getInr());
            dosageTextView.setText("Dosaggio: " + record.getDosage());
        }
    }
}
