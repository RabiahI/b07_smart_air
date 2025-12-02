package com.example.smartairapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChildrenAdapter extends RecyclerView.Adapter<ChildrenAdapter.ChildViewHolder> {
    private final List<Child> childrenList;
    private final OnChildClickListener listener;

    public interface OnChildClickListener {
        void onChildClick(Child child);
        void onViewDataClick(Child child);
    }

    public ChildrenAdapter(List<Child> childrenList, OnChildClickListener listener) {
        this.childrenList = childrenList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.provider_child_container, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        Child child = childrenList.get(position);
        holder.nameText.setText(child.getName());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChildClick(child);
            }
        });

        holder.viewDataButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDataClick(child);
            }
        });
    }

    @Override
    public int getItemCount() {
        return childrenList.size();
    }

    public static class ChildViewHolder extends RecyclerView.ViewHolder {

        TextView nameText;
        Button viewDataButton;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.childName);
            viewDataButton = itemView.findViewById(R.id.viewDataButton);
        }
    }


}