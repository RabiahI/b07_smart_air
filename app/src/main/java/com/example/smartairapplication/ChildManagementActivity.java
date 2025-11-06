package com.example.smartairapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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

public class ChildManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChildren;
    private ChildAdapter adapter;
    private List<Child> childList;
    private DatabaseReference childrenRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_management);

        mAuth = FirebaseAuth.getInstance();
        childrenRef = FirebaseDatabase.getInstance().getReference("Children");
        recyclerViewChildren = findViewById(R.id.recyclerViewChildren);
        recyclerViewChildren.setLayoutManager(new LinearLayoutManager(this));
        childList = new ArrayList<>();
        adapter = new ChildAdapter(this, childList, new ChildAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                // TODO: later open AddChildActivity to edit existing data
            }

            @Override
            public void onDeleteClick(int position) {
                // TODO: delete from Firebase
            }
        });
        recyclerViewChildren.setAdapter(adapter);

        FloatingActionButton fabAddChild = findViewById(R.id.fabAddChild);
        fabAddChild.setOnClickListener(v ->
                startActivity(new Intent(ChildManagementActivity.this, AddChildActivity.class)));

        loadChildren();
    }

    private void loadChildren() {
        String parentId = mAuth.getCurrentUser().getUid();
        childrenRef.child(parentId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                childList.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Child child = childSnapshot.getValue(Child.class);
                    if (child != null) childList.add(child);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}