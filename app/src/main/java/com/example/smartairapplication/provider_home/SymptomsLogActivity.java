package com.example.smartairapplication.provider_home;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartairapplication.R;
import com.example.smartairapplication.models.SymptomLogEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException; // Added Import for timestamp parsing
import java.text.SimpleDateFormat; // Added Import for timestamp parsing
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale; // Added Import for timestamp parsing

public class SymptomsLogActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SymptomsLogAdapter adapter;
    private List<SymptomLogEntry> symptomLogList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms_log);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Symptoms & Triggers Log");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String childId = getIntent().getStringExtra("childId");
        String parentId = getIntent().getStringExtra("parentId");

        recyclerView = findViewById(R.id.symptoms_log_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        symptomLogList = new ArrayList<>();
        adapter = new SymptomsLogAdapter(symptomLogList);
        recyclerView.setAdapter(adapter);

        if (childId != null && parentId != null) {
            loadSymptomLogs(childId, parentId);
        } else {
            Toast.makeText(this, "Error: Missing child or parent ID.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Handle the back button click
        return true;
    }

    private void loadSymptomLogs(String childId, String parentId) {
        DatabaseReference logsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child("Parent")
                .child(parentId)
                .child("Children")
                .child(childId)
                .child("Logs")
                .child("symptomLogs");

        // Define the date format used in your Firebase database (e.g., "2025-11-29 12:13")
        final SimpleDateFormat firebaseDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

        logsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                symptomLogList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot logSnapshot : snapshot.getChildren()) {

                        Long timestamp = null;
                        Object timestampObj = logSnapshot.child("timestamp").getValue();

                        // FIX: Handle the String Timestamp from the database
                        if (timestampObj instanceof String) {
                            try {
                                // Parse the String "yyyy-MM-dd HH:mm" into a Date, then get the milliseconds
                                timestamp = firebaseDateFormat.parse((String) timestampObj).getTime();
                            } catch (ParseException e) {
                                System.err.println("Timestamp stored as unparseable String: " + timestampObj + ". Error: " + e.getMessage());
                            }
                        }
                        // Original logic for numeric types retained for safety
                        else if (timestampObj instanceof Long) {
                            timestamp = (Long) timestampObj;
                        } else if (timestampObj instanceof Integer) {
                            timestamp = ((Integer) timestampObj).longValue();
                        }

                        // Original data retrieval for other fields
                        Boolean nightWaking = logSnapshot.child("nightWaking").getValue(Boolean.class);
                        String coughWheeze = logSnapshot.child("coughWheeze").getValue(String.class);

                        // Load triggers list (array/list of strings in Firebase)
                        List<String> triggers = new ArrayList<>();
                        DataSnapshot triggersSnapshot = logSnapshot.child("triggers");
                        if (triggersSnapshot.exists()) {
                            // Check if 'triggers' is a list of simple string values
                            for (DataSnapshot trigger : triggersSnapshot.getChildren()) {
                                String triggerName = trigger.getValue(String.class);
                                if (triggerName != null) {
                                    triggers.add(triggerName);
                                }
                            }
                        }

                        if (timestamp != null) {
                            // Check for null or required fields before adding
                            if (nightWaking != null && coughWheeze != null) {
                                symptomLogList.add(new SymptomLogEntry(timestamp, nightWaking, coughWheeze, triggers));
                            } else {
                                System.err.println("Skipping log entry due to missing required field (nightWaking or coughWheeze).");
                            }
                        }
                    }
                }

                if (symptomLogList.isEmpty()) {
                    Toast.makeText(SymptomsLogActivity.this, "No symptom or trigger logs found.", Toast.LENGTH_SHORT).show();
                } else {
                    // Sort the list by timestamp in descending order (newest first)
                    Collections.sort(symptomLogList, (o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp()));
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SymptomsLogActivity.this, "Failed to load symptom logs: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}