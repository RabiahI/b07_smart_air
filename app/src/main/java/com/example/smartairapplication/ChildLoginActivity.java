package com.example.smartairapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChildLoginActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ChildLoginPrefs";
    private static final String PARENT_ID_KEY = "parentId";

    TextInputEditText editTextEmail, editTextPassword;
    Button buttonLogin;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    private String parentId;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_login);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBar);
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        parentId = getIntent().getStringExtra("parentId");
        if (parentId == null) {
            parentId = sharedPreferences.getString(PARENT_ID_KEY, null);
        } else {
            sharedPreferences.edit().putString(PARENT_ID_KEY, parentId).apply();
        }

        if (parentId == null || parentId.trim().isEmpty()) {
            Toast.makeText(this, "Parent ID not found. Please use invitation code again.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, RoleSelectionActivity.class));
            finish();
            return;
        }


        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            progressBar.setVisibility(View.VISIBLE);
            String childId = currentUser.getUid();
            DatabaseReference parentRef = FirebaseDatabase.getInstance().getReference("Users").child("Parent").child(parentId);
            parentRef.child("Children").child(childId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(ChildLoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ChildLoginActivity.this, ChildHomeActivity.class);
                        intent.putExtra("childId", childId);
                        intent.putExtra("parentId", parentId);
                        startActivity(intent);
                        finish();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        mAuth.signOut();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                     progressBar.setVisibility(View.GONE);
                }
            });
        }

        buttonLogin.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String username = String.valueOf(editTextEmail.getText()).trim();
            String password = String.valueOf(editTextPassword.getText());

            if (TextUtils.isEmpty(username)) {
                Toast.makeText(ChildLoginActivity.this, "Enter username", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(ChildLoginActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            String childEmail = username + "@smartair.ca";
            mAuth.signInWithEmailAndPassword(childEmail, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String childId = firebaseUser.getUid();
                                DatabaseReference parentRef = FirebaseDatabase.getInstance().getReference("Users").child("Parent").child(parentId);
                                parentRef.child("Children").child(childId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            // Child is confirmed to be under the correct parent
                                            Toast.makeText(ChildLoginActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(ChildLoginActivity.this, ChildHomeActivity.class);
                                            intent.putExtra("childId", childId);
                                            intent.putExtra("parentId", parentId);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            // User is authenticated but not registered under this parent
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(ChildLoginActivity.this, "Login failed: You are not registered under this parent.", Toast.LENGTH_LONG).show();
                                            mAuth.signOut();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(ChildLoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                        mAuth.signOut();
                                    }
                                });
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(ChildLoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}