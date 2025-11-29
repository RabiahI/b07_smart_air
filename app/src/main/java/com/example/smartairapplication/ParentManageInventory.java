package com.example.smartairapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ParentManageInventory extends AppCompatActivity {

    private RecyclerView recycler;
    private FloatingActionButton btnAdd;
    private ImageView btnBack;

    private List<Medicine> list = new ArrayList<>();
    private ParentInventoryAdapter adapter;

    private String parentId, childId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_parent_manage_inventory);

        parentId = getIntent().getStringExtra("parentId");
        childId = getIntent().getStringExtra("childId");

        recycler = findViewById(R.id.recyclerInventory);
        btnAdd = findViewById(R.id.btnAddMedicine);
        btnBack = findViewById(R.id.btnBack);

        adapter = new ParentInventoryAdapter(list, new ParentInventoryAdapter.OnParentActionListener() {
            @Override
            public void onEdit(Medicine med) {
                showEditDialog(med);
            }

            @Override
            public void onDelete(Medicine med) {
                confirmDelete(med);
            }
        });

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        loadInventory();

        btnAdd.setOnClickListener(v -> showAddDialog());

        btnBack.setOnClickListener(v -> showExitConfirmation());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmation();
            }
        });

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
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Medicine m = ds.getValue(Medicine.class);
                    if (m != null) {
                        m.id = ds.getKey();
                        list.add(m);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_medicine, null);

        EditText inputName = dialogView.findViewById(R.id.inputName);
        EditText inputPurchase = dialogView.findViewById(R.id.inputPurchase);
        EditText inputExpiry = dialogView.findViewById(R.id.inputExpiry);
        EditText inputAmount = dialogView.findViewById(R.id.inputAmount);

        inputPurchase.setOnClickListener(v -> showDatePicker(inputPurchase));
        inputExpiry.setOnClickListener(v -> showDatePicker(inputExpiry));

        new AlertDialog.Builder(this)
                .setTitle("Add Medicine")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {

                    String n = inputName.getText().toString().trim();
                    String p = inputPurchase.getText().toString().trim();
                    String e = inputExpiry.getText().toString().trim();
                    String a = inputAmount.getText().toString().trim();

                    if (n.isEmpty() || p.isEmpty() || e.isEmpty() || a.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int amount = Integer.parseInt(a);

                    Medicine med = new Medicine(n, p, e, amount, amount <= 20);

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                            .child("Parent").child(parentId)
                            .child("Children").child(childId)
                            .child("Inventory");

                    String id = ref.push().getKey();
                    med.id = id;
                    ref.child(id).setValue(med);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(Medicine med) {

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_medicine, null);

        EditText inputName = dialogView.findViewById(R.id.inputName);
        EditText inputPurchase = dialogView.findViewById(R.id.inputPurchase);
        EditText inputExpiry = dialogView.findViewById(R.id.inputExpiry);
        EditText inputAmount = dialogView.findViewById(R.id.inputAmount);

        inputName.setText(med.name);
        inputPurchase.setText(med.purchaseDate);
        inputExpiry.setText(med.expiryDate);
        inputAmount.setText(String.valueOf(med.amountLeft));

        inputPurchase.setOnClickListener(v -> showDatePicker(inputPurchase));
        inputExpiry.setOnClickListener(v -> showDatePicker(inputExpiry));

        new AlertDialog.Builder(this)
                .setTitle("Edit Medicine")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {

                    String n = inputName.getText().toString().trim();
                    String p = inputPurchase.getText().toString().trim();
                    String e = inputExpiry.getText().toString().trim();
                    String a = inputAmount.getText().toString().trim();

                    if (n.isEmpty() || p.isEmpty() || e.isEmpty() || a.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int amount = Integer.parseInt(a);
                    boolean lowFlag = amount <= 20;

                    Medicine updated = new Medicine(n, p, e, amount, lowFlag);
                    updated.id = med.id;

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                            .child("Parent").child(parentId)
                            .child("Children").child(childId)
                            .child("Inventory").child(med.id);

                    ref.setValue(updated);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(Medicine med) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Medicine?")
                .setMessage("This will permanently remove this medicine from inventory.")
                .setPositiveButton("Delete", (dialog, which) -> {

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                            .child("Parent").child(parentId)
                            .child("Children").child(childId)
                            .child("Inventory").child(med.id);

                    ref.removeValue();

                    Toast.makeText(this, "Medicine removed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDatePicker(EditText target) {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(this,
                (DatePicker datePicker, int year, int month, int day) -> {
                    String dateStr = (month + 1) + "/" + day + "/" + year;
                    target.setText(dateStr);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

}