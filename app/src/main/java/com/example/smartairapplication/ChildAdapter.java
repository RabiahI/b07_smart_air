package com.example.smartairapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ChildViewHolder>{
    private Context context;
    private List<Child> childList;
    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public ChildAdapter(Context context, List<Child> childList, OnItemClickListener listener){
        this.context = context;
        this.childList = childList;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(context).inflate(R.layout.child_card, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position){
        Child child = childList.get(position);
        holder.textViewName.setText(child.getName());
        holder.textViewDob.setText("DOB: " + child.getDob());
        holder.textViewAge.setText(String.valueOf(child.getAge()));
        holder.textViewNotes.setText("Notes: " + child.getNotes());

        holder.editButton.setOnClickListener(v -> listener.onEditClick(position));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(position));
    }
    @Override
    public int getItemCount(){
        return childList.size();
    }

    public static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewDob, textViewAge, textViewNotes;
        Button editButton, deleteButton;
        public ChildViewHolder(@NonNull View itemView){
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewDob = itemView.findViewById(R.id.textViewDob);
            textViewAge = itemView.findViewById(R.id.textViewAge);
            textViewNotes = itemView.findViewById(R.id.textViewNotes);

            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
