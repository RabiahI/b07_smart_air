package com.example.smartairapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

        DatabaseReference childRef = null;

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
                Intent intent1 = new Intent(getApplicationContext(), Login.class);
                startActivity(intent1);
                finish();
            });
        }

        if (childRef != null) {
            childRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
    }

    @Override
    public void onPasswordVerified() {
        finish();
    }
}