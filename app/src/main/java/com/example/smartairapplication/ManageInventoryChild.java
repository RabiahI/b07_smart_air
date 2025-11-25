package com.example.smartairapplication;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageInventoryChild extends AppCompatActivity {

    private RecyclerView recyclerInventory;
    private InventoryAdapter adapter;
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

        adapter = new InventoryAdapter(medicineList, this::showUpdateDialog);
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
                .setPositiveButton("Leave", (dialog, which) -> {
                    finish();
                })
                .setNegativeButton("Stay", (dialog, which) -> {
                    dialog.dismiss();
                })
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
                medicineList.clear();
                for (DataSnapshot medSnap : snapshot.getChildren()) {
                    Medicine med = medSnap.getValue(Medicine.class);
                    med.id = medSnap.getKey();
                    medicineList.add(med);
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
            // validate input
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

            // save to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                    .child("Parent").child(parentId)
                    .child("Children").child(childId)
                    .child("Inventory").child(medicine.id)
                    .child("amountLeft");

            ref.setValue(newAmount)
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

}