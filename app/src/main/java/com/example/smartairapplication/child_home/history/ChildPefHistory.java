package com.example.smartairapplication.child_home.history;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartairapplication.R;
import com.example.smartairapplication.models.PefLog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChildPefHistory extends AppCompatActivity {

    private String childId, parentId;
    private RecyclerView recyclerView;
    private ChildPefLogAdapter adapter;
    private List<PefLog> pefList = new ArrayList<>();
    private int personalBest;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_child_pef_history);

        childId = getIntent().getStringExtra("childId");
        parentId = getIntent().getStringExtra("parentId");

        recyclerView = findViewById(R.id.recyclerPefLogs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        DatabaseReference childRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child("Parent").child(parentId)
                .child("Children").child(childId);

        childRef.child("personalBest").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer pb = snapshot.getValue(Integer.class);
                    if (pb != null){
                        personalBest = pb;

                        //create adapter with pb
                        adapter = new ChildPefLogAdapter(ChildPefHistory.this, pefList, personalBest);
                        recyclerView.setAdapter(adapter);

                        // load PEF logs AFTER we know PB
                        loadPefHistory();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

    }

    private void loadPefHistory() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child("Parent").child(parentId)
                .child("Children").child(childId)
                .child("Logs").child("pefLogs");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pefList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    PefLog entry = snap.getValue(PefLog.class);
                    if (entry != null) pefList.add(entry);
                }

                // sort newest first
                pefList.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChildPefHistory.this,
                        "Failed to load PEF history", Toast.LENGTH_SHORT).show();
            }
        });
    }
}