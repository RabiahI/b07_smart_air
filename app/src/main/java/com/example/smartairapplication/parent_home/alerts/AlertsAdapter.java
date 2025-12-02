package com.example.smartairapplication.parent_home.alerts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartairapplication.R;
import com.example.smartairapplication.models.Alert;

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
        if (severity == null) {
            severity = "low";
        }

        holder.severityBubble.setText(severity);
        int iconResId;
        int mainColor;
        int glowColor;
        
        switch (severity.toLowerCase()) {
            case "high":
                iconResId = R.drawable.ic_circlewarning;
                mainColor = Color.parseColor("#F44336"); // Red
                glowColor = Color.parseColor("#FFCDD2"); // Light Red
                break;
            case "medium":
                iconResId = R.drawable.ic_warning;
                mainColor = Color.parseColor("#FFA500"); // Orange
                glowColor = Color.parseColor("#FFE0B2"); // Light Orange
                break;
            case "low":
                iconResId = R.drawable.ic_bell;
                mainColor = Color.parseColor("#2196F3"); // Blue
                glowColor = Color.parseColor("#BBDEFB"); // Light Blue
                break;
            default:
                iconResId = R.drawable.ic_bell;
                mainColor = Color.GRAY;
                glowColor = Color.LTGRAY;
                break;
        }

        Drawable icon = AppCompatResources.getDrawable(context, iconResId);
        DrawableCompat.setTint(icon, mainColor);
        holder.severityIcon.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);

        LayerDrawable background = (LayerDrawable) holder.severityIcon.getBackground();
        GradientDrawable roundedSquare = (GradientDrawable) background.getDrawable(0);
        roundedSquare.setColor(glowColor);
        holder.severityBubble.getBackground().setTint(mainColor);
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    public static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView severityIcon;
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
