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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddChildActivity extends AppCompatActivity {

    private EditText editTextName, editTextDob, editTextAge, editTextNotes;
    private DatabaseReference parentRef;


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
            editTextDob.setText(intent.getStringExtra("dob"));
            editTextAge.setText(String.valueOf(intent.getIntExtra("age", 0)));
            editTextNotes.setText(intent.getStringExtra("notes"));

            buttonSaveChild.setText("Update Child");

            buttonSaveChild.setOnClickListener(v -> updateChildInFirebase(childId));
        } else {
            textViewTitle.setText("Add Child");
            buttonSaveChild.setOnClickListener(v -> saveChildToFirebase());
        }
        Button buttonCancel = findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle("Discard Changes?")
                .setMessage("Are you sure you want to cancel? Unsaved changes will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show());
    }

    private void saveChildToFirebase(){
        String name = editTextName.getText().toString().trim();
        String dob = editTextDob.getText().toString().trim();
        String ageStr = editTextAge.getText().toString().trim();
        String notes = editTextNotes.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(dob) || TextUtils.isEmpty(ageStr)){
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        int age;

        try {
            age = Integer.parseInt(ageStr);
        } catch(NumberFormatException e){
            Toast.makeText(this, "Invalid age format", Toast.LENGTH_SHORT).show();
            return;
        }

        String childId = parentRef.push().getKey();

        if (childId==null){
            Toast.makeText(this, "Error generating child ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Child child = new Child(null, childId, name, dob, notes, age);
        parentRef.child(childId).setValue(child).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Toast.makeText(AddChildActivity.this, "Child added successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else{
                Toast.makeText(AddChildActivity.this, "Failed to add child.", Toast.LENGTH_SHORT).show();
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

        Child updatedChild = new Child(null, childId, name, dob, notes, age);

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
