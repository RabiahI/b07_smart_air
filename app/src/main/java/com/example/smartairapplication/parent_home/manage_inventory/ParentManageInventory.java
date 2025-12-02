package com.example.smartairapplication.parent_home.manage_inventory;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartairapplication.R;
import com.example.smartairapplication.models.Alert;
import com.example.smartairapplication.models.Medicine;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
                        m.setId(ds.getKey());
                        list.add(m);

                        if (!m.getExpiryAlertSent()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy", Locale.US);
                            try {
                                Date expiryDate = sdf.parse(m.getExpiryDate());
                                if (new Date().after(expiryDate)) {
                                    String message = m.getName() + " has expired. Please replace it.";
                                    Alert alert = new Alert("Medication Expired", message, System.currentTimeMillis(), "High", childId);
                                    DatabaseReference alertsRef = FirebaseDatabase.getInstance().getReference("Users")
                                            .child("Parent").child(parentId).child("Alerts");
                                    alertsRef.push().setValue(alert);

                                    ds.getRef().child("expiryAlertSent").setValue(true);
                                }
                            } catch (ParseException e) {
                            }
                        }
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
                    boolean isLow = amount <= 20;

                    if (isLow) {
                        String message = n + " is low in stock. Remaining puffs are 20 or less.";
                        Alert alert = new Alert("Low Stock", message, System.currentTimeMillis(), "Medium", childId);
                        DatabaseReference alertsRef = FirebaseDatabase.getInstance().getReference("Users")
                                .child("Parent").child(parentId).child("Alerts");
                        alertsRef.push().setValue(alert);
                    }

                    Medicine med = new Medicine(n, p, e, amount, isLow);

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                            .child("Parent").child(parentId)
                            .child("Children").child(childId)
                            .child("Inventory");

                    String id = ref.push().getKey();
                    med.setId(id);
                    assert id != null;
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

        inputName.setText(med.getName());
        inputPurchase.setText(med.getPurchaseDate());
        inputExpiry.setText(med.getExpiryDate());
        inputAmount.setText(String.valueOf(med.getAmountLeft()));

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
                    boolean wasLow = med.getLowFlag();
                    boolean isLow = amount <= 20;

                    if (isLow && !wasLow) {
                        String message = n + " is low in stock. Remaining puffs are 20 or less.";
                        Alert alert = new Alert("Low Stock", message, System.currentTimeMillis(), "Medium", childId);
                        DatabaseReference alertsRef = FirebaseDatabase.getInstance().getReference("Users")
                                .child("Parent").child(parentId).child("Alerts");
                        alertsRef.push().setValue(alert);
                    }

                    Medicine updated = new Medicine(n, p, e, amount, isLow);
                    updated.setId(med.getId());
                    updated.setExpiryAlertSent(med.getExpiryAlertSent());

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                            .child("Parent").child(parentId)
                            .child("Children").child(childId)
                            .child("Inventory").child(med.getId());

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
                            .child("Inventory").child(med.getId());

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