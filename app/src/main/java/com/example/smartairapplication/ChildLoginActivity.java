package com.example.smartairapplication;

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

    TextInputEditText editTextEmail, editTextPassword;
    Button buttonLogin;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    private String parentId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_login);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBar);

        parentId = getIntent().getStringExtra("parentId");
        if (parentId == null) {
            Toast.makeText(this, "Parent ID is missing. Please use an invitation code.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

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

                                DatabaseReference childRef = FirebaseDatabase.getInstance().getReference("Users")
                                        .child("Parent").child(parentId).child("Children").child(firebaseUser.getUid());

                                childRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        progressBar.setVisibility(View.GONE);
                                        if (snapshot.exists()) {
                                            SharedPreferences.Editor editor = getSharedPreferences("ChildPrefs", MODE_PRIVATE).edit();
                                            editor.putString("parentId", parentId);
                                            editor.apply();

                                            Toast.makeText(ChildLoginActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(ChildLoginActivity.this, ChildHomeActivity.class);
                                            intent.putExtra("parentId", parentId);
                                            startActivity(intent);
                                            finish();
                                        } else {

                                            Toast.makeText(ChildLoginActivity.this, "Login failed: Account not associated with this parent.", Toast.LENGTH_LONG).show();
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
