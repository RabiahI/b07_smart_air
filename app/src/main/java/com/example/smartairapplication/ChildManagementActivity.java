package com.example.smartairapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChildManagementActivity extends AppCompatActivity implements ChildAdapter.OnItemClickListener {

    private RecyclerView recyclerViewChildren;
    private ChildAdapter adapter;
    private List<Child> childList;
    private DatabaseReference childrenRef;
    private FirebaseAuth mAuth;
    private FloatingActionButton fabAddChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_management);

        mAuth = FirebaseAuth.getInstance();
        String parentId = mAuth.getCurrentUser().getUid();
        childrenRef = FirebaseDatabase.getInstance().getReference("Users").child("Parent").child(parentId).child("Children");
        recyclerViewChildren = findViewById(R.id.recyclerViewChildren);
        recyclerViewChildren.setLayoutManager(new LinearLayoutManager(this));
        childList = new ArrayList<>();
        adapter = new ChildAdapter(this, childList, this);
        recyclerViewChildren.setAdapter(adapter);

        fabAddChild = findViewById(R.id.fabAddChild);
        fabAddChild.setOnClickListener(v ->
                startActivity(new Intent(ChildManagementActivity.this, AddChildActivity.class)));

        loadChildren();
    }

    private void loadChildren() {
        childrenRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                childList.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Child child = childSnapshot.getValue(Child.class);
                    if (child != null) {
                        childList.add(child);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChildManagementActivity.this, "Failed to load children.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditClick(int position) {
        Child selectedChild = childList.get(position);
        Intent intent = new Intent(ChildManagementActivity.this, AddChildActivity.class);
        intent.putExtra("childId", selectedChild.getChildId());
        intent.putExtra("name", selectedChild.getName());
        intent.putExtra("dob", selectedChild.getDob());
        intent.putExtra("age", selectedChild.getAge());
        intent.putExtra("notes", selectedChild.getNotes());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(int position) {
        Child selectedChild = childList.get(position);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Child")
                .setMessage("Are you sure you want to delete " + selectedChild.getName() + "? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    childrenRef.child(selectedChild.getChildId()).removeValue()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "Child deleted successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Failed to delete child", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.ic_dialog_alert)
                .show();
    }

}