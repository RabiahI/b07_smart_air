package com.example.smartairapplication;

import android.os.Bundle;
import android.widget.Toast;
import android.view.View; // Needed for textEmpty visibility

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartairapplication.R;
import com.example.smartairapplication.TriageLogAdapter;
import com.example.smartairapplication.TriageLogEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TriageLogActivity extends AppCompatActivity {

    private String childId, parentId;
    private RecyclerView recyclerView;

    // IMPORTANT: You must create the TriageLogEntry and TriageLogAdapter classes
    private TriageLogAdapter adapter;
    private List<TriageLogEntry> logList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triage_log);

        childId = getIntent().getStringExtra("childId");
        parentId = getIntent().getStringExtra("parentId");

        if (childId == null || parentId == null) {
            Toast.makeText(this, "Error: Missing child or parent ID.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.triageLogsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TriageLogAdapter(logList);
        recyclerView.setAdapter(adapter);


        loadAllTriageLogs();
    }

    private void loadAllTriageLogs() {
        DatabaseReference logsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child("Parent")
                .child(parentId)
                .child("Children")
                .child(childId)
                .child("Logs")
                .child("triageLogs");

        logsRef.orderByKey()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        logList.clear();

                        if (snapshot.exists() && snapshot.hasChildren()) {
                            for (DataSnapshot logSnap : snapshot.getChildren()) {

                                TriageLogEntry entry = logSnap.getValue(TriageLogEntry.class);
                                if (entry != null) {
                                    logList.add(entry);
                                }
                            }

                            Collections.reverse(logList);

                            adapter.notifyDataSetChanged();

                        } else {
                            adapter.notifyDataSetChanged();
                            Toast.makeText(TriageLogActivity.this, "No triage logs found.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TriageLogActivity.this,
                                "Failed to load triage logs: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}