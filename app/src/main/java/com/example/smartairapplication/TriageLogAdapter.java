package com.example.smartairapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TriageLogAdapter extends RecyclerView.Adapter<TriageLogAdapter.TriageViewHolder> {

    private final List<TriageLogEntry> logList;

    public TriageLogAdapter(List<TriageLogEntry> logList) {
        this.logList = logList;
    }

    @NonNull
    @Override
    public TriageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_triage_log, parent, false);
        return new TriageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TriageViewHolder holder, int position) {
        TriageLogEntry entry = logList.get(position);

        // Format Date
        String dateString = "N/A";
        if (entry.getTimeStampStarted() != null) {
            Date date = new Date(entry.getTimeStampStarted());
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
            dateString = df.format(date);
        }

        // Bind data to views
        holder.logDate.setText("🚨 " + dateString);
        holder.logPEF.setText("Latest PEF: " + (entry.getLatestPef() != null ? entry.getLatestPef() : "N/A"));
        holder.logResult.setText("Result: " + entry.getResultText());

        // Handle Escalation Status and color
        String escalatedText;
        if (entry.getEscalated() != null && entry.getEscalated()) {
            escalatedText = "✓ Escalated";
            // Set text color to red (using deprecated method for compatibility)
            holder.logEscalated.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
        } else {
            escalatedText = "✗ Not Escalated";
            // Set text color to green (using deprecated method for compatibility)
            holder.logEscalated.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
        }
        holder.logEscalated.setText(escalatedText);
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    static class TriageViewHolder extends RecyclerView.ViewHolder {
        // Declare views based on item_triage_log.xml
        TextView logDate, logEscalated, logResult, logPEF;

        public TriageViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views by finding their IDs
            logDate = itemView.findViewById(R.id.logDate);
            logEscalated = itemView.findViewById(R.id.logEscalated);
            logResult = itemView.findViewById(R.id.logResult);
            logPEF = itemView.findViewById(R.id.logPEF);
        }
    }
}