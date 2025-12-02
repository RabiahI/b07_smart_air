package com.example.smartairapplication.provider_home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartairapplication.R;
import com.example.smartairapplication.models.RescueLogEntry;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RescueLogsAdapter extends RecyclerView.Adapter<RescueLogsAdapter.LogViewHolder> {

    private final List<RescueLogEntry> logList;

    public RescueLogsAdapter(List<RescueLogEntry> logList) {
        this.logList = logList;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // R.layout.item_rescue_log needs to be created to define the look of a single log entry
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rescue_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        RescueLogEntry entry = logList.get(position);

        // 1. Format Date/Time
        String dateString = "Date N/A";
        if (entry.getTimestamp() != null) {
            Date date = new Date(entry.getTimestamp());
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
            dateString = df.format(date);
        }
        holder.dateTextView.setText(dateString);

        // 2. Display Puffs
        holder.puffsTextView.setText(String.format("Puffs Taken: %s", entry.getPuffCount()));

        // 3. Display SOB (Shortness of Breath) levels
        holder.sobBeforeTextView.setText(String.format("SOB Before: %s", entry.getSobBefore()));
        holder.sobAfterTextView.setText(String.format("SOB After: %s", entry.getSobAfter()));

        // 4. Display Feeling, using icons for better readability
        String feelingText = entry.getPostFeeling();
        String displayFeeling;
        if ("Better".equalsIgnoreCase(feelingText)) {
            displayFeeling = "😊 Feeling Better";
        } else if ("Worse".equalsIgnoreCase(feelingText)) {
            displayFeeling = "🙁 Feeling Worse";
        } else {
            displayFeeling = "😐 Feeling the Same";
        }
        holder.feelingTextView.setText(displayFeeling);
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    /**
     * ViewHolder class for caching view lookups for list items.
     */
    public static class LogViewHolder extends RecyclerView.ViewHolder {
        // NOTE: These IDs must correspond to the views in R.layout.item_rescue_log.
        public final TextView dateTextView;
        public final TextView puffsTextView;
        public final TextView sobBeforeTextView;
        public final TextView sobAfterTextView;
        public final TextView feelingTextView;

        public LogViewHolder(View view) {
            super(view);
            // Assuming standard ID names based on common Android practices
            dateTextView = view.findViewById(R.id.rescueLogDate);
            puffsTextView = view.findViewById(R.id.rescueLogPuffs);
            sobBeforeTextView = view.findViewById(R.id.rescueLogSobBefore);
            sobAfterTextView = view.findViewById(R.id.rescueLogSobAfter);
            feelingTextView = view.findViewById(R.id.rescueLogFeeling);
        }
    }
}