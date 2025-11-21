package com.example.smartairapplication;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.material.textfield.TextInputEditText; // Import TextInputEditText

public class AddChildActivity extends AppCompatActivity {

    private EditText editTextName, editTextDob, editTextAge, editTextNotes;
    private TextInputEditText editTextUsername, editTextPassword;
    private DatabaseReference parentRef;
    private int personalBest;
    private int latestPef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_child);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        String parentId = null;
        if (mAuth.getCurrentUser() != null) {
            parentId = mAuth.getCurrentUser().getUid();
        }
        if (parentId != null) {
            parentRef = database.getReference("Users").child("Parent").child(parentId).child("Children");
        }

        editTextName = findViewById(R.id.editTextName);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextDob = findViewById(R.id.editTextDob);
        editTextAge = findViewById(R.id.editTextAge);
        editTextNotes = findViewById(R.id.editTextNotes);
        Button buttonSaveChild = findViewById(R.id.buttonSave);
        TextView textViewTitle = findViewById(R.id.textViewTitle);

        // Check if we are editing an existing child
        Intent intent = getIntent();
        if (intent.hasExtra("childId")) {
            textViewTitle.setText("Edit Child");
            String childId = intent.getStringExtra("childId");
            editTextName.setText(intent.getStringExtra("name"));
            editTextUsername.setVisibility(View.GONE);
            editTextPassword.setVisibility(View.GONE);
            editTextDob.setText(intent.getStringExtra("dob"));
            editTextAge.setText(String.valueOf(intent.getIntExtra("age", 0)));
            editTextNotes.setText(intent.getStringExtra("notes"));
            personalBest = intent.getIntExtra("personalBest", 0);
            latestPef = intent.getIntExtra("latestPef", 0);

            buttonSaveChild.setText("Update Child");

            buttonSaveChild.setOnClickListener(v -> updateChildInFirebase(childId));
        } else {
            textViewTitle.setText("Add Child");
            buttonSaveChild.setOnClickListener(v -> saveChildToFirebase(mAuth));
        }
        Button buttonCancel = findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle("Discard Changes?")
                .setMessage("Are you sure you want to cancel? Unsaved changes will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show());
    }

    private void saveChildToFirebase(FirebaseAuth mAuth){
        String name = editTextName.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String dob = editTextDob.getText().toString().trim();
        String ageStr = editTextAge.getText().toString().trim();
        String notes = editTextNotes.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(dob) || TextUtils.isEmpty(ageStr)){
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch(NumberFormatException e){
            Toast.makeText(this, "Invalid age format", Toast.LENGTH_SHORT).show();
            return;
        }

        String childEmail = username + "@smartair.ca";


        mAuth.createUserWithEmailAndPassword(childEmail, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String childId = firebaseUser.getUid();
                            Child child = new Child(childEmail, childId, name, dob, notes, age, 0, 0);
                            parentRef.child(childId).setValue(child).addOnCompleteListener(dbTask -> {
                                if (dbTask.isSuccessful()){
                                    Toast.makeText(AddChildActivity.this, "Child added successfully!", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else{
                                    firebaseUser.delete();
                                    Toast.makeText(AddChildActivity.this, "Failed to add child to database.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Authentication failed.";
                        Toast.makeText(AddChildActivity.this, "Failed to create child account: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateChildInFirebase(String childId) {
        String name = editTextName.getText().toString().trim();
        String dob = editTextDob.getText().toString().trim();
        String ageStr = editTextAge.getText().toString().trim();
        String notes = editTextNotes.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(dob) || TextUtils.isEmpty(ageStr)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid age format", Toast.LENGTH_SHORT).show();
            return;
        }

        Child updatedChild = new Child(null, childId, name, dob, notes, age, personalBest, latestPef);

        parentRef.child(childId).setValue(updatedChild)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Child updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update child.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
