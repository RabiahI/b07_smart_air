package com.example.smartairapplication;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManageInventoryChild extends AppCompatActivity {

    private RecyclerView recyclerInventory;
    private ChildInventoryAdapter adapter;
    private List<Medicine> medicineList = new ArrayList<>();
    private String childId, parentId;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_inventory_child);

        childId = getIntent().getStringExtra("childId");
        parentId = getIntent().getStringExtra("parentId");

        recyclerInventory = findViewById(R.id.recyclerInventory);
        recyclerInventory.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ChildInventoryAdapter(medicineList, this::showUpdateDialog);
        recyclerInventory.setAdapter(adapter);

        btnBack = findViewById(R.id.btnBack);

        loadInventory();

        btnBack.setOnClickListener(v -> showExitConfirmation());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmation();
            }
        });
    }

    private void showExitConfirmation() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Return to home?")
                .setMessage("If you leave now, any unsaved changes will be lost.")
                .setPositiveButton("Leave", (dialog, which) -> finish())
                .setNegativeButton("Stay", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void loadInventory() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                .child("Parent").child(parentId)
                .child("Children").child(childId)
                .child("Inventory");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                // handle inventory node if missing
                if (!snapshot.exists()) {
                    medicineList.clear();
                    adapter.notifyDataSetChanged();
                    return;
                }

                medicineList.clear();
                for (DataSnapshot medSnap : snapshot.getChildren()) {
                    Medicine med = medSnap.getValue(Medicine.class);
                    if (med != null) {
                        med.setId(medSnap.getKey());
                        medicineList.add(med);
                        checkIfExpired(med);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void showUpdateDialog(Medicine medicine) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter remaining puffs");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String text = input.getText().toString().trim();

            if (text.isEmpty()) {
                Toast.makeText(this, "Please enter a valid amount.", Toast.LENGTH_SHORT).show();
                return;
            }

            int newAmount;
            try {
                newAmount = Integer.parseInt(text);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newAmount < 0) {
                Toast.makeText(this, "Amount cannot be negative.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean wasLow = medicine.getAmountLeft() <= 20;
            boolean isLow = newAmount <= 20;

            if (isLow && !wasLow) {
                sendLowStockAlert(medicine.getName());
            }

            // save to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                    .child("Parent").child(parentId)
                    .child("Children").child(childId)
                    .child("Inventory").child(medicine.getId());

            ref.child("amountLeft").setValue(newAmount);
            ref.child("lowFlag").setValue(isLow)
                    .addOnSuccessListener(a ->
                            Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void logInventoryUpdate(String medicineName, int oldAmount, int newAmount) {
        try {
            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("timestamp", System.currentTimeMillis());
            logEntry.put("medicine", medicineName);
            logEntry.put("amount_before", oldAmount);
            logEntry.put("amount_after", newAmount);
            logEntry.put("user_action", "Inventory amount manually updated by Parent.");

            // Write the log entry to the separate history node
            DatabaseReference logRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child("Parent").child(parentId)
                    .child("Children").child(childId)
                    .child("InventoryHistory")
                    .push();

            logRef.setValue(logEntry)
                    .addOnSuccessListener(a -> {
                        Log.d("InventoryLog", "Successfully logged update for " + medicineName);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("InventoryLog", "Failed to write history log: " + e.getMessage());
                    });

        } catch (Exception e) {
            Log.e("InventoryLog", "Error creating log entry: " + e.getMessage());
        }
    }

    private void sendLowStockAlert(String medicineName) {
        String message = medicineName + " is low in stock. Remaining puffs are 20 or less.";
        Alert alert = new Alert("Low Stock", message, System.currentTimeMillis(), "Medium", childId);
        DatabaseReference alertsRef = FirebaseDatabase.getInstance().getReference("Users")
                .child("Parent").child(parentId).child("Alerts");
        alertsRef.push().setValue(alert);
    }

    private void checkIfExpired(Medicine medicine) {
        if (medicine.getExpiryAlertSent()) {
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy", Locale.US);
        try {
            Date expiryDate = sdf.parse(medicine.getExpiryDate());
            if (new Date().after(expiryDate)) {
                sendExpiryAlert(medicine);
            }
        } catch (ParseException e) {
        }
    }

    private void sendExpiryAlert(Medicine medicine) {
        String message = medicine.getName() + " has expired. Please replace it.";
        Alert alert = new Alert("Medication Expired", message, System.currentTimeMillis(), "High", childId);
        DatabaseReference alertsRef = FirebaseDatabase.getInstance().getReference("Users")
                .child("Parent").child(parentId).child("Alerts");
        alertsRef.push().setValue(alert);

        DatabaseReference medRef = FirebaseDatabase.getInstance().getReference("Users")
                .child("Parent").child(parentId)
                .child("Children").child(childId)
                .child("Inventory").child(medicine.getId());
        medRef.child("expiryAlertSent").setValue(true);
    }
}
