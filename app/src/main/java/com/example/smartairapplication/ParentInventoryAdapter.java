package com.example.smartairapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ParentInventoryAdapter extends RecyclerView.Adapter<ParentInventoryAdapter.MedViewHolder> {
    private List<Medicine> list;
    private OnParentActionListener listener;

    public interface OnParentActionListener {
        void onEdit(Medicine med);
        void onDelete(Medicine med);
    }

    public ParentInventoryAdapter(List<Medicine> list, OnParentActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory_parent, parent, false);
        return new MedViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MedViewHolder holder, int position) {
        Medicine m = list.get(position);

        holder.name.setText(m.getName());
        holder.purchase.setText("Purchased: " + m.getPurchaseDate());
        holder.expiry.setText("Expires: " + m.getExpiryDate());
        holder.amount.setText("Amount left: " + m.getAmountLeft() + " puffs");

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(m));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(m));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class MedViewHolder extends RecyclerView.ViewHolder {
        TextView name, purchase, expiry, amount;
        Button btnEdit, btnDelete;

        MedViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txtMedName);
            purchase = itemView.findViewById(R.id.txtPurchaseDate);
            expiry = itemView.findViewById(R.id.txtExpiryDate);
            amount = itemView.findViewById(R.id.txtAmountLeft);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
