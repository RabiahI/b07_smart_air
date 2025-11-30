package com.example.smartairapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ChildHistory extends AppCompatActivity {
    private CardView medicineLogsButton, symptomLogsButton, pefLogsButton;
    private String childId, parentId;
    private BottomNavigationView bottomNav;
    private boolean isParentMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_child_history);

        medicineLogsButton = findViewById(R.id.cardMedicineLogs);
        symptomLogsButton = findViewById(R.id.cardSymptomLogs);
        pefLogsButton = findViewById(R.id.cardPefLogs);

        childId = getIntent().getStringExtra("childId");
        parentId = getIntent().getStringExtra("parentId");
        isParentMode = getIntent().getBooleanExtra("isParentMode", false);

        bottomNav = findViewById(R.id.bottomNav);

        medicineLogsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChildMedicineLogHistory.class);
            intent.putExtra("childId", childId);
            intent.putExtra("parentId", parentId);
            startActivity(intent);
        });

        symptomLogsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChildDailyCheckInHistory.class);
            intent.putExtra("childId", childId);
            intent.putExtra("parentId", parentId);
            startActivity(intent);
        });

        pefLogsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChildPefHistory.class);
            intent.putExtra("childId", childId);
            intent.putExtra("parentId", parentId);
            startActivity(intent);
        });

        bottomNav.setSelectedItemId(R.id.nav_history);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                finish();
                return true;
            } else if (itemId == R.id.nav_log) {
                Intent intent = new Intent(ChildHistory.this, LogMedicine.class);
                intent.putExtra("childId", childId);
                intent.putExtra("parentId", parentId);
                intent.putExtra("isParentMode", isParentMode);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_settings){
                Intent intent = new Intent(ChildHistory.this, ChildSettingsActivity.class);
                intent.putExtra("childId", childId);
                intent.putExtra("parentId", parentId);
                intent.putExtra("isParentMode", isParentMode);
                startActivity(intent);
                finish();
            } else if (itemId == R.id.nav_history) {
                return true;
            }
            return false;
        });

    }
}