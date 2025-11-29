package com.example.smartairapplication;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChildManagementActivity extends AppCompatActivity implements ChildAdapter.OnItemClickListener {

    private RecyclerView recyclerViewChildren;
    private ChildAdapter adapter;
    private List<Child> childList;
    private DatabaseReference childrenRef;
    private FirebaseAuth mAuth;
    private FloatingActionButton fabAddChild;
    private TextView textViewParentInviteCode;
    private DatabaseReference parentRef;
    private String currentParentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_management);

        mAuth = FirebaseAuth.getInstance();
        String parentId = null;
        if (mAuth.getCurrentUser() != null) {
            parentId = mAuth.getCurrentUser().getUid();
        }
        currentParentId = parentId;

        if (parentId != null) {
            childrenRef = FirebaseDatabase.getInstance().getReference("Users").child("Parent").child(parentId).child("Children");
            parentRef = FirebaseDatabase.getInstance().getReference("Users").child("Parent").child(parentId);
        }
        recyclerViewChildren = findViewById(R.id.recyclerViewChildren);
        recyclerViewChildren.setLayoutManager(new LinearLayoutManager(this));
        childList = new ArrayList<>();
        adapter = new ChildAdapter(this, childList, this);
        recyclerViewChildren.setAdapter(adapter);

        fabAddChild = findViewById(R.id.fabAddChild);
        fabAddChild.setOnClickListener(v -> startActivity(new Intent(ChildManagementActivity.this, AddChildActivity.class)));

        textViewParentInviteCode = findViewById(R.id.textViewParentInviteCode);
        ImageButton buttonCopyCode = findViewById(R.id.buttonCopyCode);
        ImageButton buttonRegenerateCode = findViewById(R.id.buttonRegenerateCode);

        buttonCopyCode.setOnClickListener(v -> copyInviteCode());
        buttonRegenerateCode.setOnClickListener(v -> regenerateInviteCode());


        loadChildren();
        if (parentRef != null) {
            setupParentInviteCode();
        }
    }

    private void setupParentInviteCode() {
        parentRef.child("childInvitationCode").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    textViewParentInviteCode.setText(snapshot.getValue(String.class));
                } else {
                    generateAndSaveNewCode(null);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChildManagementActivity.this, "Failed to load invitation code.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void copyInviteCode() {
        String code = textViewParentInviteCode.getText().toString();
        if (!code.isEmpty() && !code.equals("Loading...")) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Parent Invite Code", code);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Invite code copied", Toast.LENGTH_SHORT).show();
        }
    }

    private void regenerateInviteCode() {
        new AlertDialog.Builder(this)
                .setTitle("Regenerate Code")
                .setMessage("Are you sure? Your old code will stop working.")
                .setPositiveButton("Regenerate", (dialog, which) -> {
                    String oldCode = textViewParentInviteCode.getText().toString();
                    if (!oldCode.equals("Loading...")) {
                        generateAndSaveNewCode(oldCode);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void generateAndSaveNewCode(final String oldCode) {
        String newCode = CodeGeneratorUtils.generateInviteCode();
        DatabaseReference invitesRef = FirebaseDatabase.getInstance().getReference("invitationCodes");

        parentRef.child("childInvitationCode").setValue(newCode).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                invitesRef.child(newCode).setValue(new InvitationLookup(currentParentId));
                if (oldCode != null && !oldCode.isEmpty()) {
                    invitesRef.child(oldCode).removeValue();
                }
                Toast.makeText(ChildManagementActivity.this, "Invite code updated.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ChildManagementActivity.this, "Failed to update code.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadChildren() {
        childrenRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                childList.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Child child = childSnapshot.getValue(Child.class);
                    if (child != null) {
                        childList.add(child);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChildManagementActivity.this, "Failed to load children.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditClick(int position) {
        Child selectedChild = childList.get(position);
        Intent intent = new Intent(ChildManagementActivity.this, AddChildActivity.class);
        intent.putExtra("childId", selectedChild.getChildId());
        intent.putExtra("name", selectedChild.getName());
        intent.putExtra("dob", selectedChild.getDob());
        intent.putExtra("age", selectedChild.getAge());
        intent.putExtra("notes", selectedChild.getNotes());
        intent.putExtra("personalBest", selectedChild.getPersonalBest());
        intent.putExtra("latestPef", selectedChild.getLatestPef());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(int position) {
        Child selectedChild = childList.get(position);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Child")
                .setMessage("Are you sure you want to delete " + selectedChild.getName() + "? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> childrenRef.child(selectedChild.getChildId()).removeValue()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Child deleted successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Failed to delete child", Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onLoginClick(int position) {
        Child selectedChild = childList.get(position);
        Intent intent = new Intent(ChildManagementActivity.this, ChildHomeActivity.class);
        intent.putExtra("childId", selectedChild.getChildId());
        intent.putExtra("parentId", currentParentId);
        startActivity(intent);
    }
}
