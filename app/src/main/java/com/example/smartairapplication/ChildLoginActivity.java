package com.example.smartairapplication;

import android.content.Intent;
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

    TextInputEditText editTextEmail, editTextPassword;
    Button buttonLogin;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_login);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBar);
        buttonLogin.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String username, password;
            username = String.valueOf(editTextEmail.getText()).trim();
            password = String.valueOf(editTextPassword.getText());

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
                                final String childUid = firebaseUser.getUid();
                                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

                                usersRef.child("Parent").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot parentsSnapshot) {
                                        String foundParentId = null;
                                        Child foundChild = null;

                                        for (DataSnapshot parentSnapshot : parentsSnapshot.getChildren()) {
                                            String currentParentId = parentSnapshot.getKey();
                                            DataSnapshot childrenSnapshot = parentSnapshot.child("Children").child(childUid);
                                            if (childrenSnapshot.exists()) {
                                                foundParentId = currentParentId;
                                                foundChild = childrenSnapshot.getValue(Child.class);
                                                break;
                                            }
                                        }

                                        if (foundParentId != null && foundChild != null) {
                                            // Child found, proceed to ChildHomeActivity
                                            Toast.makeText(ChildLoginActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(ChildLoginActivity.this, ChildHomeActivity.class);
                                            intent.putExtra("childId", childUid);
                                            intent.putExtra("parentId", foundParentId);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(ChildLoginActivity.this, "Login failed: Child account not found under any parent.", Toast.LENGTH_LONG).show();
                                            mAuth.signOut();
                                            progressBar.setVisibility(View.GONE);
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
