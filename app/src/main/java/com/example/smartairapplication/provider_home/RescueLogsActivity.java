package com.example.smartairapplication.provider_home;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartairapplication.R;
import com.example.smartairapplication.models.RescueLogEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RescueLogsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RescueLogsAdapter adapter;
    private List<RescueLogEntry> rescueLogList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Assuming you have a layout file named activity_rescue_logs.xml
        setContentView(R.layout.activity_rescue_logs);

        // Set up the back button in the action bar (if available)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Rescue Inhaler Logs");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String childId = getIntent().getStringExtra("childId");
        String parentId = getIntent().getStringExtra("parentId");

        // Initialize RecyclerView
        // Assuming you have a RecyclerView ID named rescue_log_recycler_view
        recyclerView = findViewById(R.id.rescue_log_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        rescueLogList = new ArrayList<>();
        adapter = new RescueLogsAdapter(rescueLogList);
        recyclerView.setAdapter(adapter);

        if (childId != null && parentId != null) {
            loadRescueLogs(childId, parentId);
        } else {
            Toast.makeText(this, "Error: Missing child or parent ID.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Handle the back button click
        return true;
    }

    private void loadRescueLogs(String childId, String parentId) {
        // Path points to "medicineLogs" as seen in ProviderHomeActivity.loadRescueLogs
        DatabaseReference logsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child("Parent")
                .child(parentId)
                .child("Children")
                .child(childId)
                .child("Logs")
                .child("medicineLogs");

        logsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                rescueLogList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot logSnapshot : snapshot.getChildren()) {

                        // Safely retrieve timestamp, handling String, Long, or Integer types
                        Long timestamp = null;
                        // Check for common timestamp key names
                        Object timestampObj = logSnapshot.child("timestamp").getValue();
                        if (timestampObj == null) {
                            timestampObj = logSnapshot.child("timeStamp").getValue(); // Sometimes keys use an uppercase 'T'
                        }


                        if (timestampObj instanceof Long) {
                            timestamp = (Long) timestampObj;
                        } else if (timestampObj instanceof String) {
                            try {
                                // Attempt to parse the string as a long
                                timestamp = Long.parseLong((String) timestampObj);
                            } catch (NumberFormatException e) {
                                // Log the error if the string is not a valid number
                                System.err.println("Timestamp stored as an invalid String: " + timestampObj + ". Error: " + e.getMessage());
                            }
                        } else if (timestampObj instanceof Integer) {
                            // Handle case where it might have been stored as an Integer
                            timestamp = ((Integer) timestampObj).longValue();
                        }

                        // Data retrieval based on ProviderHomeActivity (using String.valueOf for safety)
                        String sobBefore = String.valueOf(logSnapshot.child("sobBefore").getValue());
                        String sobAfter = String.valueOf(logSnapshot.child("sobAfter").getValue());
                        String puffCount = String.valueOf(logSnapshot.child("puffCount").getValue());
                        String postFeeling = logSnapshot.child("postFeeling").getValue(String.class);

                        // Clean up "null" strings resulting from String.valueOf(null)
                        sobBefore = ("null".equalsIgnoreCase(sobBefore) || sobBefore.isEmpty()) ? "N/A" : sobBefore;
                        sobAfter = ("null".equalsIgnoreCase(sobAfter) || sobAfter.isEmpty()) ? "N/A" : sobAfter;
                        puffCount = ("null".equalsIgnoreCase(puffCount) || puffCount.isEmpty()) ? "N/A" : puffCount;
                        postFeeling = (postFeeling == null || postFeeling.isEmpty()) ? "N/A" : postFeeling;

                        if (timestamp != null) {
                            rescueLogList.add(new RescueLogEntry(timestamp, sobBefore, sobAfter, puffCount, postFeeling));
                        }
                    }
                }

                if (rescueLogList.isEmpty()) {
                    Toast.makeText(RescueLogsActivity.this, "No rescue logs found.", Toast.LENGTH_SHORT).show();
                } else {
                    // Sort the list by timestamp in descending order (newest first)
                    Collections.sort(rescueLogList, (o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp()));
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RescueLogsActivity.this, "Failed to load rescue logs: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}