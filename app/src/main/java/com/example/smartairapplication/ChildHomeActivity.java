package com.example.smartairapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChildHomeActivity extends AppCompatActivity implements PasswordDialogFragment.PasswordDialogListener {

    private TextView textViewName, textViewDob, textViewAge, textViewNotes;
    private Button buttonLogout, buttonBackToParent;
    private FirebaseAuth mAuth;
    private int personalBest;
    private int latestPef;
    private DatabaseReference childRef;

    private CardView zoneButton;
    private TextView zoneTitle, zoneMessage, pefValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_home);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Views
        textViewName = findViewById(R.id.textViewName);
        textViewDob = findViewById(R.id.textViewDob);
        textViewAge = findViewById(R.id.textViewAge);
        textViewNotes = findViewById(R.id.textViewNotes);
        buttonLogout = findViewById(R.id.logout);
        buttonBackToParent = findViewById(R.id.backToParent);

        // Zone button views
        zoneButton = findViewById(R.id.zone_button);
        zoneTitle = findViewById(R.id.zone_title);
        zoneMessage = findViewById(R.id.zone_message);
        pefValue = findViewById(R.id.pef_value);

        // Show onboarding on first login
        if (OnboardingActivity.isFirstLogin()) {
            OnboardingDialogFragment dialog = new OnboardingDialogFragment();

            Bundle args = new Bundle();
            args.putString("role", getIntent().getStringExtra("role"));
            args.putString("uid", getIntent().getStringExtra("uid"));
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "onboarding_dialog");
        }

        Intent intent = getIntent();
        String childId = intent.getStringExtra("childId");

        if (childId != null) {
            // Parent viewing a specific child
            buttonBackToParent.setVisibility(View.VISIBLE);
            buttonLogout.setVisibility(View.GONE);

            String parentId = null;
            if (mAuth.getCurrentUser() != null) {
                parentId = mAuth.getCurrentUser().getUid();
            }
            if (parentId != null) {
                childRef = database.getReference("Users")
                        .child("Parent")
                        .child(parentId)
                        .child("Children")
                        .child(childId);
            }

            buttonBackToParent.setOnClickListener(v ->
                    new PasswordDialogFragment().show(getSupportFragmentManager(), "PasswordDialogFragment")
            );
        } else {
            // Child logged in directly
            buttonBackToParent.setVisibility(View.GONE);
            buttonLogout.setVisibility(View.VISIBLE);

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                childRef = database.getReference("Users")
                        .child("Child")
                        .child(currentUser.getUid());
            }

            buttonLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent1 = new Intent(getApplicationContext(), ChildLoginActivity.class);
                startActivity(intent1);
                finish();
            });
        }

        if (childRef != null) {
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
        }

        zoneButton.setOnClickListener(v -> showPefInputDialog());
    }

    private void showPefInputDialog() {
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
                    if (currentPef > personalBest) {
                        childRef.child("personalBest").setValue(currentPef);
                        Toast.makeText(ChildHomeActivity.this, "New Personal Best!", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a value", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
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
