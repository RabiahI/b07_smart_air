package com.example.smartairapplication.provider_home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartairapplication.R;
import com.example.smartairapplication.models.PefLogEntry;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PefLogAdapter extends RecyclerView.Adapter<PefLogAdapter.PefViewHolder> {

    private final List<PefLogEntry> logList;

    public PefLogAdapter(List<PefLogEntry> logList) {
        this.logList = logList;
    }

    @NonNull
    @Override
    public PefViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_provider_pef_log, parent, false);
        return new PefViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PefViewHolder holder, int position) {
        PefLogEntry entry = logList.get(position);

        String dateString = "N/A";
        if (entry.getTimestamp() != null) {
            Date date = new Date(entry.getTimestamp());
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
            dateString = df.format(date);
        }

        holder.pefDate.setText(dateString);
        holder.pefValue.setText("PEF: " + (entry.getValue() != null ? entry.getValue() + " L/min" : "N/A"));
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    static class PefViewHolder extends RecyclerView.ViewHolder {
        TextView pefDate, pefValue;

        public PefViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views
            pefDate = itemView.findViewById(R.id.pef_log_date);
            pefValue = itemView.findViewById(R.id.pef_log_value);
        }
    }
}