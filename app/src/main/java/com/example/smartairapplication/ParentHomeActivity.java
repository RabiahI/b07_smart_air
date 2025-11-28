package com.example.smartairapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ParentHomeActivity extends AppCompatActivity {

    private CardView childButton, manageProviderButton;
    private CardView manageInventoryButton;
    private BottomNavigationView bottomNav;
    private Spinner spinnerChildren;
    private List<String> childNames = new ArrayList<>();
    private List<String> childIds = new ArrayList<>();
    private String selectedChildId;
    private String parentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_home);

        if (OnboardingActivity.isFirstLogin()) {
            OnboardingDialogFragment dialog = new OnboardingDialogFragment();

            Bundle args = new Bundle();
            args.putString("role", getIntent().getStringExtra("role"));
            args.putString("uid", getIntent().getStringExtra("uid"));
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "onboarding_dialog");
        }

        spinnerChildren = findViewById(R.id.spinnerChildren);
        parentId = getIntent().getStringExtra("parentId");
        if (parentId == null) {
            // fallback to logged-in user
            parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        childButton = findViewById(R.id.manageChildrenButton);
        manageProviderButton = findViewById(R.id.manageSharingButton);
        bottomNav = findViewById(R.id.bottomNav);
        manageInventoryButton = findViewById(R.id.manageInventoryButton);

        loadChildrenIntoSpinner();

        childButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ChildManagementActivity.class);
            startActivity(intent);
        });

        manageProviderButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ManageAccessActivity.class);
            startActivity(intent);
        });

        manageInventoryButton.setOnClickListener(view -> {
            Intent intent = new Intent(ParentHomeActivity.this, ParentManageInventory.class);
            intent.putExtra("childId", selectedChildId);
            intent.putExtra("parentId", parentId);
            startActivity(intent);
        });

        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_alerts) {
                startActivity(new Intent(getApplicationContext(), ParentAlertsActivity.class));
                return false;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(getApplicationContext(), ParentHistoryActivity.class));
                return false;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(getApplicationContext(), ParentSettingsActivity.class));
                return false;
            } else if (itemId == R.id.nav_home) {
                return true;
            }
            return false;
        });
    }

    private void loadChildrenIntoSpinner() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                .child("Parent").child(parentId).child("Children");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                childNames.clear();
                childIds.clear();

                for (DataSnapshot childSnap : snapshot.getChildren()){
                    String id = childSnap.getKey();
                    String name = childSnap.child("name").getValue(String.class);

                    childIds.add(id);
                    childNames.add(name != null ? name : "No Child");
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        ParentHomeActivity.this,
                        android.R.layout.simple_spinner_item,
                        childNames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerChildren.setAdapter(adapter);

                loadSavedSelection();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadSavedSelection() {
        SharedPreferences prefs = getSharedPreferences("SmartAirPrefs", MODE_PRIVATE);
        String savedId = prefs.getString("selectedChildId", null);

        if (savedId != null) {
            int index = childIds.indexOf(savedId);
            if (index != -1) {
                spinnerChildren.setSelection(index);
                selectedChildId = savedId;
            }
        }

        setupSpinnerListener();
    }

    private void setupSpinnerListener() {
        spinnerChildren.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                selectedChildId = childIds.get(position);

                // Save selection
                SharedPreferences prefs = getSharedPreferences("SmartAirPrefs", MODE_PRIVATE);
                prefs.edit().putString("selectedChildId", selectedChildId).apply();

                Toast.makeText(ParentHomeActivity.this,
                        "Switched to: " + childNames.get(position),
                        Toast.LENGTH_SHORT).show();

                // TODO later: refresh dashboard tiles here
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}
