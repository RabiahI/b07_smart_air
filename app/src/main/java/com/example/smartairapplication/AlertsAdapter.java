package com.example.smartairapplication;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.AlertViewHolder> {

    private final List<Alert> alertList;
    private final Context context;

    public AlertsAdapter(Context context, List<Alert> alertList) {
        this.context = context;
        this.alertList = alertList;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        Alert alert = alertList.get(position);

        holder.alertType.setText(alert.getType());
        holder.alertMessage.setText(alert.getMessage());

        long now = System.currentTimeMillis();
        CharSequence ago = DateUtils.getRelativeTimeSpanString(alert.getTimestamp(), now, DateUtils.MINUTE_IN_MILLIS);
        holder.alertTimestamp.setText(ago);

        String severity = alert.getSeverity();
        if (severity != null) {
            holder.severityBubble.setText(severity);
            int iconResId;
            int bubbleColor;

            switch (severity.toLowerCase()) {
                case "high":
                    iconResId = R.drawable.ic_circlewarning;
                    bubbleColor = Color.parseColor("#F44336"); // Red
                    break;
                case "medium":
                    iconResId = R.drawable.ic_warning;
                    bubbleColor = Color.parseColor("#FFA500"); // Orange
                    break;
                case "low":
                    iconResId = R.drawable.ic_bell;
                    bubbleColor = Color.parseColor("#2196F3"); // Blue
                    break;
                default:
                    iconResId = R.drawable.ic_bell; // Default icon
                    bubbleColor = Color.GRAY;
                    break;
            }
            holder.severityIcon.setImageResource(iconResId);
            
            // Set bubble color
            holder.severityBubble.getBackground().setTint(bubbleColor);
        }
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    public static class AlertViewHolder extends RecyclerView.ViewHolder {
        ImageView severityIcon;
        TextView alertType;
        TextView alertMessage;
        TextView alertTimestamp;
        TextView severityBubble;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            severityIcon = itemView.findViewById(R.id.severityIcon);
            alertType = itemView.findViewById(R.id.alertType);
            alertMessage = itemView.findViewById(R.id.alertMessage);
            alertTimestamp = itemView.findViewById(R.id.alertTimestamp);
            severityBubble = itemView.findViewById(R.id.severityBubble);
        }
    }
}
