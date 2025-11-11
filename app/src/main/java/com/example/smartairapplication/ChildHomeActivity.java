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

public class ChildHomeActivity extends AppCompatActivity {

    private TextView textViewName, textViewDob, textViewAge, textViewNotes;
    private Button buttonLogout, buttonBackToParent;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_home);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        textViewName = findViewById(R.id.textViewName);
        textViewDob = findViewById(R.id.textViewDob);
        textViewAge = findViewById(R.id.textViewAge);
        textViewNotes = findViewById(R.id.textViewNotes);
        buttonLogout = findViewById(R.id.logout);
        buttonBackToParent = findViewById(R.id.backToParent);

        Intent intent = getIntent();
        String childId = intent.getStringExtra("childId");

        DatabaseReference childRef;

        if (childId != null) {
            // Launched from Parent's account
            buttonBackToParent.setVisibility(View.VISIBLE);
            buttonLogout.setVisibility(View.GONE);

            String parentId = mAuth.getCurrentUser().getUid();
            childRef = database.getReference("Users").child("Parent").child(parentId).child("Children").child(childId);

            buttonBackToParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } else {
            // Child logged in directly
            buttonBackToParent.setVisibility(View.GONE);
            buttonLogout.setVisibility(View.VISIBLE);

            FirebaseUser currentUser = mAuth.getCurrentUser();
            childRef = database.getReference("Users").child("Child").child(currentUser.getUid());

            buttonLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    startActivity(intent);
                    finish();
                }
            });
        }

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
