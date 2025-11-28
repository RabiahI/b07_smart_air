package com.example.smartairapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ParentHistoryActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<HistoryEntry> historyList = new ArrayList<>();

    private String parentId;
    private String childId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_history);

        parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        SharedPreferences prefs = getSharedPreferences("SmartAirPrefs", MODE_PRIVATE);
        childId = prefs.getString("selectedChildId", null);

        if (childId == null){
            Toast.makeText(this, "Please select a child first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(this, historyList);
        recyclerView.setAdapter(adapter);

        loadHistory();

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_history);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                finish();
                return true;
            } else if (itemId == R.id.nav_alerts) {
                startActivity(new Intent(getApplicationContext(), ParentAlertsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(getApplicationContext(), ParentSettingsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_history) {
                return true;
            }
            return false;
        });
    }

    private void loadHistory() {
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
                        historyList.add(entry);
                    }
                }

                //sort from newest to oldest
                historyList.sort((a, b) -> b.timestamp.compareTo(a.timestamp));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ParentHistoryActivity.this, "Failed to load histpry", Toast.LENGTH_SHORT).show();
            }
        });
    }
}