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
import com.example.smartairapplication.models.MedicineLog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChildMedicineLogHistory extends AppCompatActivity {
    private String childId, parentId;
    private RecyclerView recyclerView;
    private ChildMedicineLogAdapter adapter;
    private List<MedicineLog> logs = new ArrayList<>();
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_child_medicine_log_history);

        childId = getIntent().getStringExtra("childId");
        parentId = getIntent().getStringExtra("parentId");

        recyclerView = findViewById(R.id.recyclerMedicineLogs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ChildMedicineLogAdapter(logs);
        recyclerView.setAdapter(adapter);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        loadLogs();
    }

    private void loadLogs() {
        if (parentId == null || childId == null){
            Toast.makeText(this, "Missing child context", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                .child("Parent").child(parentId)
                .child("Children").child(childId)
                .child("Logs").child("medicineLogs");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                logs.clear();
                for (DataSnapshot s : snapshot.getChildren()) {
                    MedicineLog log = s.getValue(MedicineLog.class);
                    if (log!=null) logs.add(log);
                }
                Collections.reverse(logs); //newest first
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChildMedicineLogHistory.this, "Failed to load logs", Toast.LENGTH_SHORT).show();
            }
        });
    }
}