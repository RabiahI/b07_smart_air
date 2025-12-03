package com.example.smartairapplication.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartairapplication.Login;
import com.example.smartairapplication.parent_home.ParentHomeActivity;
import com.example.smartairapplication.provider_home.ProviderManageChildren;
import com.example.smartairapplication.R;
import com.example.smartairapplication.models.User;
import com.example.smartairapplication.models.Parent;
import com.example.smartairapplication.models.Provider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Registration extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword, editTextConfirmPassword;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;
    View spinnerFragmentContainer;
    SpinnerFragment spinnerFragment;

    FirebaseDatabase database;
    DatabaseReference usersRef;

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            UserRoleManager.redirectUserBasedOnRole(this, currentUser.getUid());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("Users");

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextConfirmPassword = findViewById(R.id.confirmPassword);
        buttonReg = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);
        spinnerFragmentContainer = findViewById(R.id.spinner_fragment_container);
        spinnerFragment = (SpinnerFragment) getSupportFragmentManager().findFragmentById(R.id.spinner_fragment_container);

        // The role selection spinner is always visible here, as this activity is no longer used for child registration

        textView.setOnClickListener(view -> {
            Intent loginIntent = new Intent(Registration.this, Login.class);
            startActivity(loginIntent);
            finish();
        });

        buttonReg.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            String email, password, confirmPassword, selectedRole;
            email = String.valueOf(editTextEmail.getText());
            password = String.valueOf(editTextPassword.getText());
            confirmPassword = String.valueOf(editTextConfirmPassword.getText());
            selectedRole = spinnerFragment.getSelectedRole();

            // Validation
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(Registration.this, "All fields are required", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(selectedRole)) {
                Toast.makeText(Registration.this, "Please select a role", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(Registration.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(Registration.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            // Firebase auth
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String uid = firebaseUser.getUid();

                                User user;
                                if ("Parent".equals(selectedRole)) {
                                    user = new Parent(email);
                                } else {
                                    user = new Provider(email);
                                }

                                DatabaseReference roleRef = usersRef.child(selectedRole).child(uid);
                                roleRef.setValue(user).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(Registration.this, "Account Created.", Toast.LENGTH_SHORT).show();

                                        Intent homeIntent;
                                        switch (selectedRole) {
                                            case "Provider":
                                                homeIntent = new Intent(Registration.this, ProviderManageChildren.class);
                                                break;
                                            default: // Parent
                                                homeIntent = new Intent(Registration.this, ParentHomeActivity.class);
                                                break;
                                        }
                                        homeIntent.putExtra("role", selectedRole);
                                        homeIntent.putExtra("uid", firebaseUser.getUid());
                                        startActivity(homeIntent);
                                        finish();

                                    } else {
                                        Toast.makeText(Registration.this, "Failed to save user data.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(Registration.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
