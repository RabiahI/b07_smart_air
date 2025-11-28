package com.example.smartairapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

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

    private CardView zoneButton;
    private TextView zoneTitle, zoneMessage, pefValue;


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
            parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        childButton = findViewById(R.id.manageChildrenButton);
        manageProviderButton = findViewById(R.id.manageSharingButton);
        bottomNav = findViewById(R.id.bottomNav);
        manageInventoryButton = findViewById(R.id.manageInventoryButton);
        
        zoneButton = findViewById(R.id.zone_button);
        zoneTitle = findViewById(R.id.zone_title);
        zoneMessage = findViewById(R.id.zone_message);
        pefValue = findViewById(R.id.pef_value);
        zoneButton.setOnClickListener(v -> showSetPersonalBestDialog());


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
        
        if (selectedChildId != null) {
            updatePefDisplayForChild(selectedChildId);
        }

        setupSpinnerListener();
    }

    private void setupSpinnerListener() {
        spinnerChildren.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                selectedChildId = childIds.get(position);
                SharedPreferences prefs = getSharedPreferences("SmartAirPrefs", MODE_PRIVATE);
                prefs.edit().putString("selectedChildId", selectedChildId).apply();

                Toast.makeText(ParentHomeActivity.this,
                        "Switched to: " + childNames.get(position),
                        Toast.LENGTH_SHORT).show();
                updatePefDisplayForChild(selectedChildId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updatePefDisplayForChild(String childId) {
        if (childId == null) return;
        DatabaseReference childRef = FirebaseDatabase.getInstance().getReference("Users")
                .child("Parent").child(parentId).child("Children").child(childId);

        childRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer personalBest = snapshot.child("personalBest").getValue(Integer.class);
                    Integer latestPef = snapshot.child("latestPef").getValue(Integer.class);
                    
                    if (personalBest == null) personalBest = 0;
                    if (latestPef == null) latestPef = 0;

                    updateZone(latestPef, personalBest);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ParentHomeActivity.this, "Failed to load PEF data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateZone(int currentPef, int personalBest) {
        if (personalBest == 0) {
            zoneTitle.setText(R.string.today_s_zone_not_set);
            zoneMessage.setText(R.string.please_set_your_personal_best_pef);
            zoneButton.setCardBackgroundColor(Color.parseColor("#9E9E9E")); // Gray
            pefValue.setText("PEF: Not Set");
            return;
        }

        double percentage = ((double) currentPef / personalBest) * 100;

        if (percentage >= 80) {
            zoneTitle.setText(R.string.today_s_zone_green);
            zoneMessage.setText(""); 
            zoneButton.setCardBackgroundColor(Color.parseColor("#90C4A5"));
        } else if (percentage >= 50) {
            zoneTitle.setText(R.string.today_s_zone_yellow);
            zoneMessage.setText("Caution: Child may need their reliever inhaler.");
            zoneButton.setCardBackgroundColor(Color.parseColor("#FFC107")); // Yellow
        } else {
            zoneTitle.setText(R.string.today_s_zone_red);
            zoneMessage.setText("Danger: Child may need reliever and medical attention.");
            zoneButton.setCardBackgroundColor(Color.parseColor("#F44336")); // Red
        }
        
        pefValue.setText("PEF: " + currentPef + " (PB: " + personalBest + ")");
    }

    private void showSetPersonalBestDialog() {
        if (selectedChildId == null) {
            Toast.makeText(this, "Please select a child first.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Personal Best");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter new Personal Best PEF value");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String pbString = input.getText().toString();
            if (!pbString.isEmpty()) {
                int newPersonalBest = Integer.parseInt(pbString);
                DatabaseReference childRef = FirebaseDatabase.getInstance().getReference("Users")
                        .child("Parent").child(parentId).child("Children").child(selectedChildId);
                childRef.child("personalBest").setValue(newPersonalBest)
                        .addOnSuccessListener(aVoid -> Toast.makeText(ParentHomeActivity.this, "Personal Best updated.", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(ParentHomeActivity.this, "Failed to update Personal Best.", Toast.LENGTH_SHORT).show());
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
