package com.example.smartairapplication;;

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
    // private TextView textEmpty; // Uncomment if you add a TextView for empty state

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure you create the layout file: res/layout/activity_triage_log.xml
        setContentView(R.layout.activity_triage_log);

        // Get IDs passed from ProviderHomeActivity
        childId = getIntent().getStringExtra("childId");
        parentId = getIntent().getStringExtra("parentId");

        if (childId == null || parentId == null) {
            Toast.makeText(this, "Error: Missing child or parent ID.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize RecyclerView and its components
        // Ensure the ID below matches your activity_triage_log.xml
        recyclerView = findViewById(R.id.triageLogsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with the list
        adapter = new TriageLogAdapter(logList);
        recyclerView.setAdapter(adapter);

        // textEmpty = findViewById(R.id.textEmptyTriageLogs); // Uncomment this line if used

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

        // KEY DIFFERENCE: Using orderByChild() or orderByKey() without limitToLast()
        // retrieves ALL logs under the node.
        logsRef.orderByKey()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        logList.clear();

                        if (snapshot.exists() && snapshot.hasChildren()) {
                            for (DataSnapshot logSnap : snapshot.getChildren()) {
                                // Deserialize the log entry into your POJO class
                                TriageLogEntry entry = logSnap.getValue(TriageLogEntry.class);
                                if (entry != null) {
                                    // Assuming TriageLogEntry has a logId field
                                    // entry.logId = logSnap.getKey();
                                    logList.add(entry);
                                }
                            }

                            // Reverse the list to show newest logs at the top
                            Collections.reverse(logList);

                            adapter.notifyDataSetChanged();
                            // if (textEmpty != null) textEmpty.setVisibility(View.GONE);

                        } else {
                            adapter.notifyDataSetChanged();
                            // if (textEmpty != null) textEmpty.setVisibility(View.VISIBLE);
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