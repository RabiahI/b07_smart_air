package com.example.smartairapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
        spinnerFragment = (SpinnerFragment) getSupportFragmentManager().findFragmentById(R.id.spinner_fragment_container);

        textView.setOnClickListener(view -> {
            Intent intent = new Intent(Registration.this, Login.class);
            startActivity(intent);
            finish();
        });

        buttonReg.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            String email, password, confirmPassword, role;
            email = String.valueOf(editTextEmail.getText());
            password = String.valueOf(editTextPassword.getText());
            confirmPassword = String.valueOf(editTextConfirmPassword.getText());
            role = spinnerFragment != null ? spinnerFragment.getSelectedRole() : "";

            // Validation
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Registration.this, "Enter email", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(Registration.this, "Enter password", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(Registration.this, "Confirm your password", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(Registration.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(role)) {
                Toast.makeText(Registration.this, "Please select a role", Toast.LENGTH_SHORT).show();
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

                                // Create role-specific user object
                                User user;
                                switch (role) {
                                    case "Parent":
                                        user = new Parent(email);
                                        break;
                                    case "Provider":
                                        user = new Provider(email);
                                        break;
                                    case "Child":
                                        user = new Child(email, uid, null, null, null, 0, 0, 0);
                                        break;
                                    default:
                                        Toast.makeText(Registration.this, "Invalid role selected.", Toast.LENGTH_SHORT).show();
                                        return;
                                }

                                DatabaseReference roleRef = usersRef.child(role).child(uid);
                                roleRef.setValue(user).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(Registration.this, "Account Created.",
                                                Toast.LENGTH_SHORT).show();

                                        // Redirect based on role, passing extras for onboarding
                                        switch (role) {
                                            case "Child":
                                                Intent childIntent = new Intent(Registration.this, ChildHomeActivity.class);
                                                childIntent.putExtra("role", role);
                                                childIntent.putExtra("uid", firebaseUser.getUid());
                                                startActivity(childIntent);
                                                finish();
                                                break;

                                            case "Provider":
                                                Intent providerIntent = new Intent(Registration.this, ProviderManageChildren.class);
                                                providerIntent.putExtra("role", role);
                                                providerIntent.putExtra("uid", firebaseUser.getUid());
                                                startActivity(providerIntent);
                                                finish();
                                                break;

                                            default: // Parent
                                                Intent parentIntent = new Intent(Registration.this, ParentHomeActivity.class);
                                                parentIntent.putExtra("role", role);
                                                parentIntent.putExtra("uid", firebaseUser.getUid());
                                                startActivity(parentIntent);
                                                finish();
                                                break;
                                        }

                                    } else {
                                        Toast.makeText(Registration.this, "Failed to save user data.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            // If sign up fails, display a message to the user.
                            Toast.makeText(Registration.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

        });
    }
}
