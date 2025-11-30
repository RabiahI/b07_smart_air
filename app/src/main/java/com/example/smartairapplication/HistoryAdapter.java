package com.example.smartairapplication;

import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.flexbox.FlexboxLayout;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private Context context;
    private List<HistoryEntry> list;

    public HistoryAdapter(Context context, List<HistoryEntry> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_history_entry, parent, false);
        return new HistoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryEntry entry = list.get(position);
        List<String> symptoms = new ArrayList<>();

        //make symptom list
        if (entry.nightWaking){
            symptoms.add("Night Waking");
        }
        if (entry.activityLimits){
            symptoms.add("Activity Limits");
        }
        if (!Objects.equals(entry.coughWheeze, "none")){
            symptoms.add("Cough/Wheeze");
        }

        // date
        holder.txtDate.setText(formatDate(entry.timestamp));

        // notes
        holder.txtNotes.setText(entry.notes != null ? entry.notes : "(No notes)");

        // symptoms chips
        addChips(symptoms, holder.containerSymptoms, true);

        // trigger chips
        addChips(entry.triggers, holder.containerTriggers, false);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // chip helper function
    private void addChips(List<String> items, FlexboxLayout container, boolean isSymptom) {
        container.removeAllViews();

        if (items == null) return;

        for (String item : items) {
            TextView chip = new TextView(context);
            chip.setText(item);
            chip.setTextSize(14);
            chip.setPadding(26, 12, 26, 12);
            chip.setTextColor(Color.BLACK);
            chip.setBackgroundResource(isSymptom ?
                    R.drawable.bg_chip_symptom :
                    R.drawable.bg_chip_trigger);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(10, 10, 10, 10);

            chip.setLayoutParams(params);
            container.addView(chip);
        }
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {

        TextView txtDate, txtNotes;
        FlexboxLayout containerSymptoms, containerTriggers;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            txtDate = itemView.findViewById(R.id.txtDate);
            txtNotes = itemView.findViewById(R.id.txtNotes);
            containerSymptoms = itemView.findViewById(R.id.containerSymptoms);
            containerTriggers = itemView.findViewById(R.id.containerTriggers);
        }
    }

    private String formatDate(String timestamp) {
        try {
            DateTimeFormatter input = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            DateTimeFormatter output = DateTimeFormatter.ofPattern("MMM d, yyyy");

            LocalDateTime dt = LocalDateTime.parse(timestamp, input);
            return output.format(dt);

        } catch (Exception e) {
            // fallback if parse fails
            return timestamp;
        }
    }
}
