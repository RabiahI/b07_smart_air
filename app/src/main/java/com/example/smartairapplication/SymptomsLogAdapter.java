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

public class SymptomsLogAdapter extends RecyclerView.Adapter<SymptomsLogAdapter.SymptomViewHolder> {

    private final List<SymptomLogEntry> logList;

    public SymptomsLogAdapter(List<SymptomLogEntry> logList) {
        this.logList = logList;
    }

    @NonNull
    @Override
    public SymptomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout (you must create R.layout.item_symptom_log)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_symptom_log, parent, false);
        return new SymptomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SymptomViewHolder holder, int position) {
        SymptomLogEntry entry = logList.get(position);

        // 1. Format Date
        String dateString = "N/A";
        if (entry.getTimestamp() != null) {
            Date date = new Date(entry.getTimestamp());
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
            dateString = df.format(date);
        }
        holder.logDate.setText("Logged: " + dateString);

        // 2. Format Symptoms
        StringBuilder symptomSummary = new StringBuilder();

        // Night Waking
        if (entry.getNightWaking()) {
            symptomSummary.append("🌙 Night Waking reported.");
        }

        // Cough/Wheeze
        String coughWheeze = entry.getCoughWheeze();
        if (coughWheeze != null && !coughWheeze.trim().isEmpty()) {
            if (symptomSummary.length() > 0) symptomSummary.append("\n");
            symptomSummary.append("💨 Wheezing/Cough: ").append(coughWheeze);
        }

        if (symptomSummary.length() == 0) {
            holder.symptomDetails.setText("No specific symptoms recorded.");
        } else {
            holder.symptomDetails.setText(symptomSummary.toString());
        }

        // 3. Format Triggers
        List<String> triggers = entry.getTriggers();
        if (triggers != null && !triggers.isEmpty()) {
            String triggersString = String.join(", ", triggers);
            holder.triggerList.setText("💥 Triggers: " + triggersString);
            holder.triggerList.setVisibility(View.VISIBLE);
        } else {
            holder.triggerList.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    static class SymptomViewHolder extends RecyclerView.ViewHolder {
        TextView logDate, symptomDetails, triggerList;

        public SymptomViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views (IDs must match your item_symptom_log.xml)
            logDate = itemView.findViewById(R.id.symptom_log_date);
            symptomDetails = itemView.findViewById(R.id.symptom_details);
            triggerList = itemView.findViewById(R.id.trigger_list);
        }
    }
}
