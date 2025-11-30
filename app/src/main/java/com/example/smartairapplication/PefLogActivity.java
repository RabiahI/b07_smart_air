package com.example.smartairapplication;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PefLogActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PefLogAdapter adapter;
    private List<PefLogEntry> pefLogList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pef_log);

        // Set up the back button in the action bar (if available)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("PEF Log History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String childId = getIntent().getStringExtra("childId");
        String parentId = getIntent().getStringExtra("parentId");

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.pef_log_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        pefLogList = new ArrayList<>();
        adapter = new PefLogAdapter(pefLogList);
        recyclerView.setAdapter(adapter);

        if (childId != null && parentId != null) {
            loadPefLogs(childId, parentId);
        } else {
            Toast.makeText(this, "Error: Missing child or parent ID.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Handle the back button click
        return true;
    }

    private void loadPefLogs(String childId, String parentId) {
        // Use the same Firebase path as in ProviderHomeActivity
        DatabaseReference logsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child("Parent")
                .child(parentId)
                .child("Children")
                .child(childId)
                .child("Logs")
                .child("pefLogs");

        logsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pefLogList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot logSnapshot : snapshot.getChildren()) {
                        Long timestamp = logSnapshot.child("timestamp").getValue(Long.class);
                        // Ensure we handle different potential data types for 'value'
                        String value = logSnapshot.child("value").getValue(Object.class) != null
                                ? String.valueOf(logSnapshot.child("value").getValue(Object.class))
                                : "N/A";

                        if (timestamp != null) {
                            pefLogList.add(new PefLogEntry(timestamp, value));
                        }
                    }
                }

                if (pefLogList.isEmpty()) {
                    Toast.makeText(PefLogActivity.this, "No PEF logs found for this child.", Toast.LENGTH_SHORT).show();
                } else {
                    // Sort the list by timestamp in descending order (newest first)
                    Collections.sort(pefLogList, (o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp()));
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PefLogActivity.this, "Failed to load PEF logs: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}