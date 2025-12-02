package com.example.smartairapplication.child_home.history;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartairapplication.R;
import com.example.smartairapplication.models.PefLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChildPefLogAdapter extends RecyclerView.Adapter<ChildPefLogAdapter.PefLogViewHolder> {
    private List<PefLog> pefList;
    private int personalBest;
    private Context context;

    public ChildPefLogAdapter(Context context, List<PefLog> pefList, int personalBest){
        this.context = context;
        this.pefList = pefList;
        this.personalBest = personalBest;
    }

    @NonNull
    @Override
    public PefLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pef_log, parent, false);
        return new PefLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildPefLogAdapter.PefLogViewHolder holder, int position) {
        PefLog entry = pefList.get(position);

        long timestamp = entry.getTimestamp();  // timestamp

        //convert to date
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String formattedDate = sdf.format(date);

        //convert to time
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        String formattedTime = timeFormat.format(date);

        holder.txtDate.setText(formattedDate);
        holder.txtTime.setText(formattedTime);
        holder.txtPefValue.setText(String.valueOf(entry.getPefValue()));

        //compute zone
        String zone = computeZone(entry.getPefValue());

        //styling for zones
        switch (zone) {
            case "Green":
                holder.layoutPef.setBackgroundResource(R.drawable.bg_pef_green);
                holder.tagZone.setText("Green Zone");
                holder.tagStatus.setText("Great! ✓");
                holder.tagZone.setBackgroundResource(R.drawable.bg_chip_green);
                holder.tagZone.setTextColor(Color.parseColor("#000000"));
                holder.tagStatus.setTextColor(Color.parseColor("#2D7A3F"));
                holder.imgPefIcon.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#00CD78")));
                break;

            case "Yellow":
                holder.layoutPef.setBackgroundResource(R.drawable.bg_pef_yellow);
                holder.tagZone.setText("Yellow Zone");
                holder.tagStatus.setText("Caution");
                holder.tagZone.setBackgroundResource(R.drawable.bg_chip_yellow);
                holder.tagZone.setTextColor(Color.parseColor("#000000"));
                holder.tagStatus.setTextColor(Color.parseColor("#C78F00"));
                holder.imgPefIcon.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#FFC107")));
                break;

            case "Red":
                holder.layoutPef.setBackgroundResource(R.drawable.bg_pef_red);
                holder.tagZone.setText("Red Zone");
                holder.tagStatus.setText("Danger!");
                holder.tagZone.setBackgroundResource(R.drawable.bg_chip_red);
                holder.tagZone.setTextColor(Color.parseColor("#000000"));
                holder.tagStatus.setTextColor(Color.parseColor("#B00020"));
                holder.imgPefIcon.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#FF5252")));
                break;
        }

    }

    private String computeZone(int value) {
        if (personalBest == 0) return "Green"; //fallback
        double percentage = ((double) value / personalBest) * 100;
        if (percentage >= 80) return "Green";
        else if (percentage >= 50) return "Yellow";
        else return "Red";
    }

    @Override
    public int getItemCount() {
        return pefList.size();
    }

    public static class PefLogViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate, txtTime, tagZone, tagStatus, txtPefValue;
        LinearLayout layoutPef;
        ImageView imgPefIcon;
        public PefLogViewHolder(@NonNull View itemView) {
            super(itemView);

            txtDate = itemView.findViewById(R.id.txtPefDate);
            txtTime = itemView.findViewById(R.id.txtPefTime);
            tagZone = itemView.findViewById(R.id.tagZone);
            tagStatus = itemView.findViewById(R.id.tagStatus);
            txtPefValue = itemView.findViewById(R.id.txtPefValue);

            layoutPef = itemView.findViewById(R.id.layoutPef);
            imgPefIcon = itemView.findViewById(R.id.imgPefIcon);
        }
    }

}
