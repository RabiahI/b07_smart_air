package com.example.smartairapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ChildMedicineLogAdapter extends RecyclerView.Adapter<ChildMedicineLogAdapter.MedLogViewHolder> {

    private List<MedicineLog> logs;

    public ChildMedicineLogAdapter(List<MedicineLog> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public MedLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine_log, parent, false);
        return new MedLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedLogViewHolder holder, int position) {
        MedicineLog log = logs.get(position);

        long timestamp = log.timestamp;  // timestamp

        //convert to date
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String formattedDate = sdf.format(date);

        //convert to time
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        String formattedTime = timeFormat.format(date);

        holder.txtName.setText(log.inhalerType);
        holder.txtDate.setText(formattedDate);
        holder.txtTime.setText(formattedTime);
        holder.txtDosage.setText(String.valueOf(log.puffCount));

        //styling for rescue vs controller
        if (Objects.equals(log.inhalerType, "Rescue")){
            holder.layoutMedicine.setBackgroundResource(R.drawable.bg_orange_log);
        } else {
            holder.layoutMedicine.setBackgroundResource(R.drawable.bg_blue_log);
        }
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    public static class MedLogViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtDate, txtTime, txtDosage;
        LinearLayout layoutMedicine;
        public MedLogViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtMedType);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtDosage = itemView.findViewById(R.id.doseNum);

            layoutMedicine = itemView.findViewById(R.id.layoutMedicine);
        }
    }
}

