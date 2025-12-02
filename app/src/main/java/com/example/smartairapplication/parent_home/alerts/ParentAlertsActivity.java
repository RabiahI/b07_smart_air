package com.example.smartairapplication.parent_home.alerts;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartairapplication.R;
import com.example.smartairapplication.models.Alert;
import com.example.smartairapplication.parent_home.ParentSettingsActivity;
import com.example.smartairapplication.parent_home.history_browser.ParentHistoryActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ParentAlertsActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private RecyclerView alertsRecyclerView;
    private AlertsAdapter alertsAdapter;
    private List<Alert> alertList;
    private DatabaseReference alertsRef;
    private FirebaseAuth mAuth;
    private Spinner sortSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_alerts);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        bottomNav = findViewById(R.id.bottomNav);
        alertsRecyclerView = findViewById(R.id.alertsRecyclerView);
        sortSpinner = findViewById(R.id.sortSpinner);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);

        alertsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        alertList = new ArrayList<>();
        alertsAdapter = new AlertsAdapter(this, alertList);
        alertsRecyclerView.setAdapter(alertsAdapter);
        
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sortBy = parent.getItemAtPosition(position).toString();
                if (sortBy.equalsIgnoreCase("Sort by Date")) {
                    alertList.sort((a1, a2) -> Long.compare(a2.getTimestamp(), a1.getTimestamp()));
                } else if (sortBy.equalsIgnoreCase("Sort by Severity")) {
                    alertList.sort((a1, a2) -> {
                        int severity1 = getSeverityValue(a1.getSeverity());
                        int severity2 = getSeverityValue(a2.getSeverity());
                        return Integer.compare(severity2, severity1);
                    });
                }
                alertsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        if (currentUser != null) {
            String parentId = currentUser.getUid();
            alertsRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child("Parent")
                    .child(parentId)
                    .child("Alerts");

            alertsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    alertList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Alert alert = snapshot.getValue(Alert.class);
                        if (alert != null) {
                            alertList.add(alert);
                        }
                    }

                    String sortBy = sortSpinner.getSelectedItem().toString();
                     if (sortBy.equalsIgnoreCase("Sort by Date")) {
                        alertList.sort((a1, a2) -> Long.compare(a2.getTimestamp(), a1.getTimestamp()));
                    } else if (sortBy.equalsIgnoreCase("Sort by Severity")) {
                        alertList.sort((a1, a2) -> {
                            int severity1 = getSeverityValue(a1.getSeverity());
                            int severity2 = getSeverityValue(a2.getSeverity());
                            return Integer.compare(severity2, severity1);
                        });
                    }
                    alertsAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ParentAlertsActivity.this, "Failed to load alerts.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        bottomNav.setSelectedItemId(R.id.nav_alerts);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                finish();
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(getApplicationContext(), ParentHistoryActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(getApplicationContext(), ParentSettingsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_alerts) {
                return true;
            }
            return false;
        });
    }

    private int getSeverityValue(String severity) {
        if (severity == null) return 0;
        switch (severity.toLowerCase()) {
            case "high":
                return 3;
            case "medium":
                return 2;
            case "low":
                return 1;
            default:
                return 0;
        }
    }
}
