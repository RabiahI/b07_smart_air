package com.example.smartairapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChildInventoryAdapter extends RecyclerView.Adapter<ChildInventoryAdapter.MedViewHolder> {

    private List<Medicine> list;
    private OnUpdateClickListener listener;

    public interface OnUpdateClickListener {
        void onUpdate(Medicine med);
    }

    public ChildInventoryAdapter(List<Medicine> list, OnUpdateClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory_child, parent, false);
        return new MedViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MedViewHolder holder, int position) {
        Medicine m = list.get(position);

        holder.name.setText(m.getName());
        holder.purchase.setText("Purchased: " + m.getPurchaseDate());
        holder.expiry.setText("Expires: " + m.getExpiryDate());
        holder.amount.setText("Amount left: " + m.getAmountLeft() + " puffs");

        holder.updateBtn.setOnClickListener(v -> listener.onUpdate(m));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class MedViewHolder extends RecyclerView.ViewHolder {
        TextView name, purchase, expiry, amount;
        Button updateBtn;

        MedViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txtMedName);
            purchase = itemView.findViewById(R.id.txtPurchaseDate);
            expiry = itemView.findViewById(R.id.txtExpiryDate);
            amount = itemView.findViewById(R.id.txtAmountLeft);
            updateBtn = itemView.findViewById(R.id.btnUpdatePuffs);
        }
    }
}

