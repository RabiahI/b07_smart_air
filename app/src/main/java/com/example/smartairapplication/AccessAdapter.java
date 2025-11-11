package com.example.smartairapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        holder.textViewInviteStatus.setText("Access: Not Shared");

        holder.buttonInviteProvider.setOnClickListener(v -> listener.onInviteClick(position));
    }

    @Override
    public int getItemCount() {
        return childList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewChildName, textViewInviteStatus;
        Button buttonInviteProvider;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewChildName = itemView.findViewById(R.id.textViewChildName);
            textViewInviteStatus = itemView.findViewById(R.id.textViewInviteStatus);
            buttonInviteProvider = itemView.findViewById(R.id.buttonInviteProvider);
        }
    }


}
