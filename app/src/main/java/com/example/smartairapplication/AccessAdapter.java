package com.example.smartairapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AccessAdapter extends RecyclerView.Adapter<AccessAdapter.ViewHolder> {
    private Context context;
    private List<Child> childList;
    private OnInviteClickListener listener;

    public interface OnInviteClickListener {
        void onInviteClick(int position);
        void onRevokeClick(int position);

    }
    public AccessAdapter(Context context, List<Child> childList, OnInviteClickListener listener) {
        this.context = context;
        this.childList = childList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(context).inflate(R.layout.child_access_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Child child = childList.get(position);
        holder.textViewChildName.setText(child.getName());

        String status = child.getAccessStatus();

        if (status == null || status.equals("not_shared")){
            holder.textViewAccessStatus.setText("Access: Not Shared");
            holder.iconAccessStatus.setImageResource(R.drawable.ic_lock_outline);
            holder.iconAccessStatus.setColorFilter(Color.parseColor("#757575"));
            holder.layoutInviteCode.setVisibility(View.GONE);
        } else if (status.equals("generated")){
            holder.textViewAccessStatus.setText("Access: Code Generated");
            holder.iconAccessStatus.setImageResource(R.drawable.ic_hourglass_empty);
            holder.iconAccessStatus.setColorFilter(Color.parseColor("#FFC107"));
            holder.layoutInviteCode.setVisibility(View.VISIBLE);

            //mask code by default
            holder.textViewInviteCode.setText("••••••");

            holder.buttonToggleCodeVisibility.setOnClickListener(v-> {
                if (holder.textViewInviteCode.getText().toString().equals("••••••")){
                    holder.textViewInviteCode.setText(child.getInviteCode());
                    holder.buttonToggleCodeVisibility.setImageResource(R.drawable.ic_visibility_on);
                } else{
                    holder.textViewInviteCode.setText("••••••");
                    holder.buttonToggleCodeVisibility.setImageResource(R.drawable.ic_visibility_off);
                }
            });

        } else if(status.equals("accepted")){
            holder.textViewAccessStatus.setText("Access: Shared with Provider");
            holder.iconAccessStatus.setImageResource(R.drawable.ic_check_circle);
            holder.iconAccessStatus.setColorFilter(Color.parseColor("#43A047"));
            holder.layoutInviteCode.setVisibility(View.GONE);
        }


        holder.buttonInviteProvider.setOnClickListener(v -> listener.onInviteClick(position));
        holder.buttonRevokeAccess.setOnClickListener(v -> listener.onRevokeClick(position));
    }

    @Override
    public int getItemCount() {
        return childList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewChildName, textViewAccessStatus, textViewInviteCode;
        ImageView iconAccessStatus;
        ImageButton buttonToggleCodeVisibility;
        LinearLayout layoutInviteCode;
        Button buttonInviteProvider, buttonRevokeAccess;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewChildName = itemView.findViewById(R.id.textViewChildName);
            textViewAccessStatus = itemView.findViewById(R.id.textViewAccessStatus);
            iconAccessStatus = itemView.findViewById(R.id.iconAccessStatus);
            layoutInviteCode = itemView.findViewById(R.id.layoutInviteCode);
            textViewInviteCode = itemView.findViewById(R.id.textViewInviteCode);
            buttonToggleCodeVisibility = itemView.findViewById(R.id.buttonToggleCodeVisibility);
            buttonInviteProvider = itemView.findViewById(R.id.buttonInviteProvider);
            buttonRevokeAccess = itemView.findViewById(R.id.buttonRevokeAccess);

        }
    }


}
