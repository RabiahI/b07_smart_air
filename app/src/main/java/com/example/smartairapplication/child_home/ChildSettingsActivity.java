package com.example.smartairapplication.child_home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartairapplication.R;
import com.example.smartairapplication.authentication.Login;
import com.example.smartairapplication.authentication.PasswordDialogFragment;
import com.example.smartairapplication.authentication.RoleSelectionActivity;
import com.example.smartairapplication.child_home.history.ChildHistory;
import com.example.smartairapplication.child_home.log_medicine.LogMedicine;
import com.example.smartairapplication.models.Child;
import com.example.smartairapplication.parent_home.ParentHomeActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChildSettingsActivity extends AppCompatActivity implements PasswordDialogFragment.PasswordDialogListener {

    private TextView textViewName, textViewDob, textViewAge, textViewNotes;
    private Button buttonLogout, buttonBackToParent;
    private FirebaseAuth mAuth;
    private DatabaseReference childRef;
    private BottomNavigationView bottomNav;

    private String currentParentEmail;
    private boolean isParentMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_settings);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        textViewName = findViewById(R.id.textViewName);
        textViewDob = findViewById(R.id.textViewDob);
        textViewAge = findViewById(R.id.textViewAge);
        textViewNotes = findViewById(R.id.textViewNotes);
        buttonLogout = findViewById(R.id.logout);
        buttonBackToParent = findViewById(R.id.backToParent);
        bottomNav = findViewById(R.id.bottomNav);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in. Redirecting to login.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(ChildSettingsActivity.this, Login.class));
            finish();
            return;
        }

        // Use the passed boolean as the source of truth
        isParentMode = getIntent().getBooleanExtra("isParentMode", false);

        String intentChildId = getIntent().getStringExtra("childId");
        String intentParentId = getIntent().getStringExtra("parentId");
        final String finalChildId;
        final String finalParentId;
        
        if (isParentMode) {
            // If in parent mode, the childId comes from the intent and parentId is the current user
            finalChildId = intentChildId;
            finalParentId = currentUser.getUid();
            currentParentEmail = currentUser.getEmail();
        } else {
            // If not in parent mode, the childId is the current user and parentId comes from the intent
            finalChildId = currentUser.getUid();
            finalParentId = intentParentId;
        }

        childRef = database.getReference("Users")
                .child("Parent")
                .child(finalParentId)
                .child("Children")
                .child(finalChildId);

        if (isParentMode) {
            buttonBackToParent.setVisibility(View.VISIBLE);
            buttonLogout.setVisibility(View.GONE);
            buttonBackToParent.setOnClickListener(v -> promptForPasswordAndExit());
        } else {
            buttonBackToParent.setVisibility(View.GONE);
            buttonLogout.setVisibility(View.VISIBLE);
            buttonLogout.setOnClickListener(v -> {
                SharedPreferences sharedPreferences = getSharedPreferences("ChildLoginPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("parentId");
                editor.remove("childName");
                editor.apply();

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), RoleSelectionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

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
                        textViewName.setText(child.getName());
                        textViewDob.setText("DOB: " + child.getDob());
                        textViewAge.setText("Age: " + child.getAge());
                        textViewNotes.setText("Notes: " + child.getNotes());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChildSettingsActivity.this, "Failed to load child data.", Toast.LENGTH_SHORT).show();
            }
        });

        bottomNav.setSelectedItemId(R.id.nav_settings);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                finish();
                return true; 
            } else if (itemId == R.id.nav_log) {
                Intent intent = new Intent(ChildSettingsActivity.this, LogMedicine.class);
                intent.putExtra("childId", finalChildId);
                intent.putExtra("parentId", finalParentId);
                intent.putExtra("isParentMode", isParentMode);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_history){
                Intent intent = new Intent(ChildSettingsActivity.this, ChildHistory.class);
                intent.putExtra("childId", finalChildId);
                intent.putExtra("parentId", finalParentId);
                intent.putExtra("isParentMode", isParentMode);
                startActivity(intent);
                finish();
            } else if (itemId == R.id.nav_settings) {
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
            Toast.makeText(ChildSettingsActivity.this, "Parent email not found. Please re-login.", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ChildSettingsActivity.this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onPasswordVerified() {
        Intent intent = new Intent(ChildSettingsActivity.this, ParentHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}