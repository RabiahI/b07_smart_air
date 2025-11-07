package com.example.smartairapplication;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddChildActivity extends AppCompatActivity {

    private EditText editTextName, editTextDob, editTextAge, editTextNotes;
    private Button buttonSaveChild;
    private DatabaseReference parentRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_child);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        String parentId = mAuth.getCurrentUser().getUid();
        parentRef = database.getReference("Users").child("Parent").child(parentId).child("Children");

        editTextName = findViewById(R.id.editTextName);
        editTextDob = findViewById(R.id.editTextDob);
        editTextAge = findViewById(R.id.editTextAge);
        editTextNotes = findViewById(R.id.editTextNotes);
        buttonSaveChild = findViewById(R.id.buttonSave);

        buttonSaveChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                saveChildToFirebase();
            }
        });
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

        Child child = new Child(childId, name, dob, notes, age);
        parentRef.child(childId).setValue(child).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(AddChildActivity.this, "Child added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else{
                    Toast.makeText(AddChildActivity.this, "Failed to add child.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}