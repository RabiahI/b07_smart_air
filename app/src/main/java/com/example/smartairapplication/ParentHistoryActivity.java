package com.example.smartairapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class ParentHistoryActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private final List<HistoryEntry> historyList = new ArrayList<>();
    private final List<HistoryEntry> displayList = new ArrayList<>();

    private String parentId;
    private String childId;

    private ImageView filterButton;

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

        adapter = new HistoryAdapter(this, displayList);
        recyclerView.setAdapter(adapter);

        filterButton = findViewById(R.id.filterButton);
        filterButton.setOnClickListener(v -> showFilterSheet());

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

        loadHistory();

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
                        if (entry.triggers == null){
                            entry.triggers = new ArrayList<>();
                        }
                        historyList.add(entry);
                    }
                }
                //default: show everything before filtering
                displayList.clear();
                displayList.addAll(historyList);

                //sort from newest to oldest
                displayList.sort((a, b) -> {
                    String ta = a.timestamp != null ? a.timestamp : "";
                    String tb = b.timestamp != null ? b.timestamp : "";
                    return tb.compareTo(ta); // descending
                });
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ParentHistoryActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFilterSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_history_filter, null);
        dialog.setContentView(view);

        EditText startDate = view.findViewById(R.id.inputStartDate);
        EditText endDate = view.findViewById(R.id.inputEndDate);

        ChipGroup symptomsGroup = view.findViewById(R.id.chipGroupSymptoms);
        ChipGroup triggersGroup = view.findViewById(R.id.chipGroupTriggers);

        Button btnApply = view.findViewById(R.id.btnApplyFilters);
        Button btnClear = view.findViewById(R.id.btnClearFilters);

        // Date pickers
        View.OnClickListener datePicker = v -> {
            EditText target = (EditText) v;
            Calendar c = Calendar.getInstance();
            DatePickerDialog picker = new DatePickerDialog(
                    this,
                    (dp, y, m, d) -> target.setText(String.format("%04d-%02d-%02d", y, m + 1, d)),
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH));
            picker.show();
        };

        startDate.setOnClickListener(datePicker);
        endDate.setOnClickListener(datePicker);

        // apply filters
        btnApply.setOnClickListener(v -> {
            HistoryFilter filter = new HistoryFilter();

            filter.startDate = startDate.getText().toString().trim();
            filter.endDate = endDate.getText().toString().trim();

            // read symptom chips - normalize to lowercase
            for (int i = 0; i < symptomsGroup.getChildCount(); i++) {
                Chip chip = (Chip) symptomsGroup.getChildAt(i);
                if (chip.isChecked()) {
                    filter.symptoms.add(chip.getText().toString().toLowerCase().trim());
                }
            }

            // read trigger chips - normalize to lowercase
            for (int i = 0; i < triggersGroup.getChildCount(); i++) {
                Chip chip = (Chip) triggersGroup.getChildAt(i);
                if (chip.isChecked()) filter.triggers.add(chip.getText().toString().toLowerCase().trim());
            }

            applyFilter(filter);
            dialog.dismiss();
        });

        // clear filters
        btnClear.setOnClickListener(v -> {
            displayList.clear();
            displayList.addAll(historyList);

            displayList.sort((a, b) -> {
                String ta = a.timestamp != null ? a.timestamp : "";
                String tb = b.timestamp != null ? b.timestamp : "";
                return tb.compareTo(ta);
            });

            adapter.notifyDataSetChanged();
            dialog.dismiss();

        });

        dialog.show();
    }

    private void applyFilter(HistoryFilter filter) {
        displayList.clear(); //clear existing results

        for (HistoryEntry entry : historyList) {

            // date filter
            LocalDate entryDate;

            if (entry.timestamp == null || entry.timestamp.length() < 10){
                continue; //skip bad or missing timestamps
            }

            try {
                String dateOnly = entry.timestamp.substring(0, 10);
                entryDate = LocalDate.parse(dateOnly);
            } catch (Exception e) {
                continue; //malformed date string, skip entry
            }

            LocalDate start = (filter.startDate == null || filter.startDate.isEmpty())
                    ? null : LocalDate.parse(filter.startDate);
            LocalDate end = (filter.endDate == null || filter.endDate.isEmpty())
                    ? null : LocalDate.parse(filter.endDate);

            if (start != null && entryDate.isBefore(start)) continue;
            if (end != null && entryDate.isAfter(end)) continue;

            // symptom filtering (rebuild symptoms list for each entry)
            if (!filter.symptoms.isEmpty()) {

                List<String> entrySymptoms = new ArrayList<>();

                if (entry.nightWaking) entrySymptoms.add("night waking");
                if (entry.activityLimits) entrySymptoms.add("activity limits");
                if (!Objects.equals(entry.coughWheeze, "none"))
                    entrySymptoms.add("cough/wheeze");

                // does this entry match ANY selected symptoms?
                boolean matchSymptom = false;
                for (String symptom : filter.symptoms) {
                    if (entrySymptoms.contains(symptom)) {
                        matchSymptom = true;
                        break;
                    }
                }

                if (!matchSymptom) continue;
            }


            // triggers
            if (!filter.triggers.isEmpty()) {

                List<String> entryTriggers = new ArrayList<>();
                if (entry.triggers != null){
                    for (String t : entry.triggers){
                        if (t != null){
                            entryTriggers.add(t.toLowerCase().trim());
                        }
                    }
                }
                boolean matchTrigger = false;
                for (String selected : filter.triggers) {
                    if (entryTriggers.contains(selected)) {
                        matchTrigger = true;
                        break;
                    }
                }
                if (!matchTrigger) continue;
            }
            displayList.add(entry); //passed all filters
        }
        displayList.sort((a, b) -> {
            String ta = a.timestamp != null ? a.timestamp : "";
            String tb = b.timestamp != null ? b.timestamp : "";
            return tb.compareTo(ta);
        });
        adapter.notifyDataSetChanged();
    }
}