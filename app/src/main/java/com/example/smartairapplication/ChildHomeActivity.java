package com.example.smartairapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChildHomeActivity extends AppCompatActivity implements PasswordDialogFragment.PasswordDialogListener {

    private FirebaseAuth mAuth;
    private int personalBest;
    private int latestPef;
    private DatabaseReference childRef;

    private CardView zoneButton, triageButton, logMedicineButton, streaksButton, dailyCheckInButton, manageInventoryButton;
    private TextView zoneTitle, zoneMessage, pefValue;
    private BottomNavigationView bottomNav;

    private String currentParentEmail;
    private boolean isParentMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_home);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        bottomNav = findViewById(R.id.bottomNav);

        // Zone button views
        zoneButton = findViewById(R.id.zone_button);
        zoneTitle = findViewById(R.id.zone_title);
        zoneMessage = findViewById(R.id.zone_message);
        pefValue = findViewById(R.id.pef_value);

        triageButton = findViewById(R.id.triageButton);
        logMedicineButton = findViewById(R.id.logMedicineButton);
        streaksButton = findViewById(R.id.streaks_button);
        manageInventoryButton = findViewById(R.id.manageInventoryButton);
        dailyCheckInButton = findViewById(R.id.dailyCheckInButton);

        // Show onboarding on first login
        if (OnboardingActivity.isFirstLogin()) {
            OnboardingDialogFragment dialog = new OnboardingDialogFragment();
            Bundle args = new Bundle();
            args.putString("role", getIntent().getStringExtra("role"));
            args.putString("uid", getIntent().getStringExtra("uid"));
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "onboarding_dialog");
        }
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in. Redirecting to login.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(ChildHomeActivity.this, Login.class));
            finish();
            return;
        }
        String intentChildId = getIntent().getStringExtra("childId");
        String intentParentId = getIntent().getStringExtra("parentId");
        final String finalChildId;
        final String finalParentId;

        if (intentChildId != null && intentParentId == null) {
            // Scenario 1: Parent viewing a specific child's profile
            finalChildId = intentChildId;
            finalParentId = currentUser.getUid();
            currentParentEmail = currentUser.getEmail();
            isParentMode = true;
        } else if (intentChildId == null && intentParentId != null) {
            // Scenario 2: Child logged in directly
            finalChildId = currentUser.getUid();
            finalParentId = intentParentId;
            isParentMode = false;
        } else if (intentChildId != null && intentChildId.equals(currentUser.getUid())) {
            // Scenario 3: Child navigating back to ChildHomeActivity after direct login
            finalChildId = intentChildId;
            finalParentId = intentParentId;
            isParentMode = false;
        }else {
            // Error or unexpected scenario
            Toast.makeText(this, "Unable to determine child context. Please re-login.", Toast.LENGTH_LONG).show();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ChildHomeActivity.this, Login.class));
            finish();
            return;
        }
        childRef = database.getReference("Users")
                .child("Parent")
                .child(finalParentId)
                .child("Children")
                .child(finalChildId);
        
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isParentMode) {
                    promptForPasswordAndExit();
                } else {
                    finish();
                }
            }
        });

        childRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Child child = snapshot.getValue(Child.class);
                    if (child != null) {
                        personalBest = child.getPersonalBest();
                        latestPef = child.getLatestPef();
                        updateZone(latestPef);
                    }
                } else {
                    Toast.makeText(ChildHomeActivity.this, "Child data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChildHomeActivity.this, "Failed to load child data.", Toast.LENGTH_SHORT).show();
            }
        });

        zoneButton.setOnClickListener(v -> showPefInputDialog(finalParentId, finalChildId));
        triageButton.setOnClickListener(v -> {
            Intent triageIntent = new Intent(ChildHomeActivity.this, TriageActivity.class);
            triageIntent.putExtra("childId", finalChildId);
            startActivity(triageIntent);
        });

        logMedicineButton.setOnClickListener(v -> {
            Intent logMedicineIntent = new Intent(ChildHomeActivity.this, LogMedicine.class);
            logMedicineIntent.putExtra("childId", finalChildId);
            logMedicineIntent.putExtra("parentId", finalParentId);
            logMedicineIntent.putExtra("isParentMode", isParentMode);
            startActivity(logMedicineIntent);
        });

        streaksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent streaksIntent = new Intent(ChildHomeActivity.this, StreaksBadgesActivity.class);
                streaksIntent.putExtra("childId", finalChildId);
                startActivity(streaksIntent);
            }
        });
        manageInventoryButton.setOnClickListener(v -> {
            Intent manageInventoryIntent = new Intent(ChildHomeActivity.this, ManageInventoryChild.class);
            manageInventoryIntent.putExtra("childId", finalChildId);
            manageInventoryIntent.putExtra("parentId", finalParentId);
            startActivity(manageInventoryIntent);
          });
        
      dailyCheckInButton.setOnClickListener(v -> {
            Intent dailyCheckInIntent = new Intent(ChildHomeActivity.this, DailyCheckIn.class);
            dailyCheckInIntent.putExtra("childId", finalChildId);
            dailyCheckInIntent.putExtra("parentId", finalParentId);
            startActivity(dailyCheckInIntent);
        });
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_log) {
                Intent logMedicineIntent = new Intent(ChildHomeActivity.this, LogMedicine.class);
                logMedicineIntent.putExtra("childId", finalChildId);
                logMedicineIntent.putExtra("parentId", finalParentId);
                logMedicineIntent.putExtra("isParentMode", isParentMode);
                startActivity(logMedicineIntent);
                return false;
            } else if (itemId == R.id.nav_settings) {
                Intent settingsIntent = new Intent(ChildHomeActivity.this, ChildSettingsActivity.class);
                settingsIntent.putExtra("childId", finalChildId);
                settingsIntent.putExtra("parentId", finalParentId);
                settingsIntent.putExtra("isParentMode", isParentMode);
                startActivity(settingsIntent);
                return false;
            } else if (itemId == R.id.nav_home){
                return true;
            }
            return false;
        });
    }

    private void promptForPasswordAndExit() {
        if (currentParentEmail != null) {
            PasswordDialogFragment dialog = new PasswordDialogFragment();
            Bundle args = new Bundle();
            args.putString("parentEmail", currentParentEmail);
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "PasswordDialogFragment");
        } else {
            Toast.makeText(ChildHomeActivity.this, "Parent email not found. Please re-login.", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ChildHomeActivity.this, Login.class));
            finish();
        }
    }

    private void showPefInputDialog(String parentId, String childId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter PEF Value");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String pefString = input.getText().toString();
            if (!pefString.isEmpty()) {
                int currentPef = Integer.parseInt(pefString);
                if (childRef != null) {
                    childRef.child("latestPef").setValue(currentPef);
                    logPefValue(currentPef, parentId, childId);
                }
            } else {
                Toast.makeText(this, "Please enter a value", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void logPefValue(int pefValue, String parentId, String childId) {
        DatabaseReference pefLogRef = FirebaseDatabase.getInstance().getReference("Users")
                .child("Parent").child(parentId)
                .child("Children").child(childId)
                .child("Logs").child("pefLogs").push();

        PefLog logEntry = new PefLog(pefValue, System.currentTimeMillis());
        pefLogRef.setValue(logEntry);
    }

    private void updateZone(int currentPef) {
        if (personalBest == 0) {
            zoneTitle.setText(R.string.today_s_zone_not_set);
            zoneMessage.setText(R.string.please_set_your_personal_best_pef);
            zoneButton.setCardBackgroundColor(Color.parseColor("#9E9E9E")); // Gray
            updatePefDisplay(currentPef);
            return;
        }

        double percentage = ((double) currentPef / personalBest) * 100;

        if (percentage >= 80) {
            zoneTitle.setText(R.string.today_s_zone_green);
            zoneMessage.setText(R.string.keep_up_your_routine);
            zoneButton.setCardBackgroundColor(Color.parseColor("#90C4A5"));
        } else if (percentage >= 50) {
            zoneTitle.setText(R.string.today_s_zone_yellow);
            zoneMessage.setText(R.string.caution_use_your_reliever_inhaler);
            zoneButton.setCardBackgroundColor(Color.parseColor("#FFC107")); // Yellow
        } else {
            zoneTitle.setText(R.string.today_s_zone_red);
            zoneMessage.setText(R.string.danger_use_your_reliever_and_see_a_doctor);
            zoneButton.setCardBackgroundColor(Color.parseColor("#F44336")); // Red
        }

        updatePefDisplay(currentPef);
    }

    private void updatePefDisplay(int currentPef) {
        pefValue.setText("PEF: " + currentPef + " (PB: " + personalBest + ")");
    }

    @Override
    public void onPasswordVerified() {
        finish();
    }
}