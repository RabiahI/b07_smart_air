package com.example.smartairapplication.child_home.history;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartairapplication.parent_home.history_browser.HistoryAdapter;
import com.example.smartairapplication.models.HistoryEntry;
import com.example.smartairapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChildDailyCheckInHistory extends AppCompatActivity {

    private String childId, parentId;
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<HistoryEntry> historyList = new ArrayList<>();
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_child_daily_check_in_history);

        childId = getIntent().getStringExtra("childId");
        parentId = getIntent().getStringExtra("parentId");

        recyclerView = findViewById(R.id.recyclerSymptomLogs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HistoryAdapter(this, historyList);
        recyclerView.setAdapter(adapter);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        loadDailyCheckInHistory();
    }

    private void loadDailyCheckInHistory() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                .child("Parent").child(parentId)
                .child("Children").child(childId)
                .child("Logs").child("symptomLogs");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();
                for (DataSnapshot snap : snapshot.getChildren()){
                    HistoryEntry entry = snap.getValue(HistoryEntry.class);
                    if (entry != null){
                        entry.id = snap.getKey();
                        if (entry.triggers == null){
                            entry.triggers = new ArrayList<>();
                        }
                        historyList.add(entry);
                    }
                }

                //sort from newest to oldest
                historyList.sort((a, b) -> {
                    String ta = a.timestamp != null ? a.timestamp : "";
                    String tb = b.timestamp != null ? b.timestamp : "";
                    return tb.compareTo(ta); // descending
                });
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChildDailyCheckInHistory.this, "Failed to load history", Toast.LENGTH_SHORT).show();
            }
        });
    }
}