package com.example.smartairapplication.parent_home.manage_children;

import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.example.smartairapplication.models.CreateChildRequest;
import com.example.smartairapplication.R;
import com.example.smartairapplication.models.Child;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.functions.FirebaseFunctions;

import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddChildActivity extends AppCompatActivity {

    private EditText editTextName, editTextDob, editTextAge, editTextNotes, editTextThreshold, editTextController;
    private TextInputEditText editTextUsername, editTextPassword;
    private DatabaseReference parentRef;
    private int personalBest;
    private int latestPef;
    private int threshold;
    private int controllersDays;


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

        String finalParentId = parentId; 
        if (finalParentId != null) {
            parentRef = database.getReference("Users").child("Parent").child(finalParentId).child("Children");
        }

        editTextName = findViewById(R.id.editTextName);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextDob = findViewById(R.id.editTextDob);
        editTextAge = findViewById(R.id.editTextAge);
        editTextNotes = findViewById(R.id.editTextNotes);
        editTextThreshold = findViewById(R.id.editTextThreshold);
        editTextController = findViewById(R.id.editTextControllerDays);
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

            String dobStr = intent.getStringExtra("dob");
            editTextDob.setText(dobStr);

            if (dobStr != null && !dobStr.isEmpty()) {
                LocalDate birth = LocalDate.parse(dobStr);
                int age = Period.between(birth, LocalDate.now()).getYears();
                editTextAge.setText(String.valueOf(age));
            }

            editTextNotes.setText(intent.getStringExtra("notes"));
            personalBest = intent.getIntExtra("personalBest", 0);
            latestPef = intent.getIntExtra("latestPef", 0);
            threshold = intent.getIntExtra("threshold", 0);
            controllersDays = intent.getIntExtra("controllerDays", 0);

            buttonSaveChild.setText("Update Child");

            buttonSaveChild.setOnClickListener(v -> updateChildInFirebase(childId, finalParentId));
        } else {
            textViewTitle.setText("Add Child");
            buttonSaveChild.setOnClickListener(v -> saveChildToFirebase(mAuth, finalParentId));
        }
        Button buttonCancel = findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle("Discard Changes?")
                .setMessage("Are you sure you want to cancel? Unsaved changes will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show());

        editTextDob.setOnClickListener(v -> showDatePicker(editTextDob));
    }

    private void showDatePicker(EditText target) {
        final Calendar calendar = Calendar.getInstance();

        // if editing, set initial date to existing DOB
        String existingDob = target.getText().toString().trim();
        if (!existingDob.isEmpty()) {
            LocalDate ld = LocalDate.parse(existingDob);
            calendar.set(ld.getYear(), ld.getMonthValue() - 1, ld.getDayOfMonth());
        }


        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {

                    // Format DOB as yyyy-MM-dd
                    String dobStr = String.format("%04d-%02d-%02d", year, month + 1, day);
                    target.setText(dobStr);

                    // compute age
                    LocalDate today = LocalDate.now();
                    LocalDate dob = LocalDate.of(year, month + 1, day);
                    int age = Period.between(dob, today).getYears();

                    editTextAge.setText(String.valueOf(age));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Prevent selecting future dates
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        dialog.show();
    }

    private Map<String, Object> toMap(CreateChildRequest r, String parentId) {
        Map<String, Object> map = new HashMap<>();
        map.put("email", r.email);
        map.put("password", r.password);
        map.put("name", r.name);
        map.put("dob", r.dob);
        map.put("notes", r.notes);
        map.put("age", r.age);
        map.put("threshold", r.threshold);
        map.put("controllerDays", r.controllerDays);
        map.put("parentId", parentId);
        return map;
    }

    private void saveChildToFirebase(FirebaseAuth mAuth, String parentId){
        String name = editTextName.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String dob = editTextDob.getText().toString().trim();
        String ageStr = editTextAge.getText().toString().trim();
        String notes = editTextNotes.getText().toString().trim();
        String thresholdStr = editTextThreshold.getText().toString().trim();
        String controllerDaysStr = editTextController.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(dob) || TextUtils.isEmpty(ageStr) || TextUtils.isEmpty(thresholdStr) || TextUtils.isEmpty(controllerDaysStr)){
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDate birth = LocalDate.parse(dob);
        LocalDate today = LocalDate.now();
        int age = Period.between(birth, today).getYears();

        if (age < 0) {
            Toast.makeText(this, "DOB cannot be in the future", Toast.LENGTH_SHORT).show();
            return;
        }
        if (age == 0) {
            Toast.makeText(this, "Child must be at least 1 year old", Toast.LENGTH_SHORT).show();
            return;
        }


        String childEmail = username + "@smartair.ca";

        if (parentId == null) {
            Toast.makeText(this, "Parent ID is not available. Please re-login.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "parentId = " + parentId, Toast.LENGTH_LONG).show();

        try {
            threshold = Integer.parseInt(thresholdStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid threshold format", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            controllersDays = Integer.parseInt(controllerDaysStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid controller days format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (threshold < 1) {
            Toast.makeText(this, "Threshold days must be at least 1.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (controllersDays < 1) {
            Toast.makeText(this, "Controller days must be at least 1", Toast.LENGTH_SHORT).show();
            return;
        }

        String finalParentId = parentId; // For use in lambda
        CreateChildRequest request = new CreateChildRequest(
                childEmail,
                password,
                name,
                dob,
                notes,
                age,
                threshold,
                controllersDays
        );

        FirebaseFunctions.getInstance()
                .getHttpsCallable("createChildUser")
                .call(toMap(request, finalParentId))
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        String errorMessage = (e != null)
                                ? e.getMessage()
                                : "Unknown error.";

                        Toast.makeText(AddChildActivity.this,
                                "Failed to create child account: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Success — child created in Auth + Database
                    Toast.makeText(AddChildActivity.this,
                            "Child added successfully!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void updateChildInFirebase(String childId, String parentId) {
        String name = editTextName.getText().toString().trim();
        String dob = editTextDob.getText().toString().trim();
        String notes = editTextNotes.getText().toString().trim();
        String thresholdStr = editTextThreshold.getText().toString().trim();
        String controllerDaysStr = editTextController.getText().toString().trim();

        LocalDate birth = LocalDate.parse(dob);
        LocalDate today = LocalDate.now();
        int age = Period.between(birth, today).getYears();
        if (age < 0) {
            Toast.makeText(this, "DOB cannot be in the future", Toast.LENGTH_SHORT).show();
            return;
        }
        if (age == 0) {
            Toast.makeText(this, "Child must be at least 1 year old", Toast.LENGTH_SHORT).show();
            return;
        }


        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(dob)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            threshold = Integer.parseInt(thresholdStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid threshold format", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            controllersDays = Integer.parseInt(controllerDaysStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid controller days format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (threshold < 1) {
            Toast.makeText(this, "Threshold days must be at least 1.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (controllersDays < 1) {
            Toast.makeText(this, "Controller days must be at least 1", Toast.LENGTH_SHORT).show();
            return;
        }

        if (parentId == null) {
            Toast.makeText(this, "Parent ID is not available for update.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            threshold = Integer.parseInt(thresholdStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid threshold format", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            controllersDays = Integer.parseInt(controllerDaysStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid controller days format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (threshold < 1) {
            Toast.makeText(this, "Threshold days must be at least 1.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (controllersDays < 1) {
            Toast.makeText(this, "Controller days must be at least 1", Toast.LENGTH_SHORT).show();
            return;
        }


        FirebaseDatabase.getInstance().getReference("Users")
                .child("Parent")
                .child(parentId)
                .child("Children")
                .child(childId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Child existingChild = snapshot.getValue(Child.class);
                            if (existingChild != null) {
                                existingChild.setName(name);
                                existingChild.setDob(dob);
                                existingChild.setAge(age);
                                existingChild.setNotes(notes);
                                existingChild.setPersonalBest(personalBest);
                                existingChild.setLatestPef(latestPef);
                                existingChild.setThreshold(threshold);
                                existingChild.setControllerDays(controllersDays);

                                parentRef.child(childId).setValue(existingChild)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(AddChildActivity.this, "Child updated successfully!", Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                Toast.makeText(AddChildActivity.this, "Failed to update child.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(AddChildActivity.this, "Child data not found for update.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddChildActivity.this, "Database error during child update: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
