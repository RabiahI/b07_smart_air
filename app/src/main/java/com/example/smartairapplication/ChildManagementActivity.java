package com.example.smartairapplication;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_management);

        mAuth = FirebaseAuth.getInstance();
        String parentId = null;
        if (mAuth.getCurrentUser() != null) {
            parentId = mAuth.getCurrentUser().getUid();
        }
        if (parentId != null) {
            childrenRef = FirebaseDatabase.getInstance().getReference("Users").child("Parent").child(parentId).child("Children");
        }
        recyclerViewChildren = findViewById(R.id.recyclerViewChildren);
        recyclerViewChildren.setLayoutManager(new LinearLayoutManager(this));
        childList = new ArrayList<>();
        adapter = new ChildAdapter(this, childList, this);
        recyclerViewChildren.setAdapter(adapter);

        fabAddChild = findViewById(R.id.fabAddChild);
        fabAddChild.setOnClickListener(v -> showAddChildOptions());

        loadChildren();
    }

    private void showAddChildOptions() {
        new AlertDialog.Builder(this)
                .setTitle("Add a Child")
                .setItems(new String[]{"Create manually", "Invite a child"}, (dialog, which) -> {
                    if (which == 0) {
                        startActivity(new Intent(ChildManagementActivity.this, AddChildActivity.class));
                    } else {
                        showInviteChildDialog();
                    }
                })
                .show();
    }

    private void showInviteChildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_invite_provider, null);

        TextView title = view.findViewById(R.id.textTitle);
        title.setText("Invite a Child");

        TextView subtitle = view.findViewById(R.id.textSubtitle);
        subtitle.setText("Generate a one-time code for your child to create an account. The code will expire in 24 hours.");

        TextView textViewInviteCode = view.findViewById(R.id.textViewInviteCode);
        Button buttonGenerate = view.findViewById(R.id.buttonGenerateCode);
        Button buttonCopy = view.findViewById(R.id.buttonCopyCode);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        ImageButton buttonToggleVisibility = view.findViewById(R.id.buttonToggleVisibility);
        final boolean[] isHidden = {true};

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        DatabaseReference childInvitesRef = FirebaseDatabase.getInstance().getReference("ChildInvitations");

        buttonGenerate.setOnClickListener(v -> {
            String code = CodeGeneratorUtils.generateInviteCode();
            textViewInviteCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            textViewInviteCode.setText(code);
            buttonToggleVisibility.setImageResource(R.drawable.ic_visibility_off);
            isHidden[0] = true;

            String parentId = mAuth.getCurrentUser().getUid();
            long expiry = System.currentTimeMillis() + 24 * 60 * 60 * 1000; // 24 hours

            ChildInvitation invitation = new ChildInvitation(code, parentId, expiry);

            childInvitesRef.child(code).setValue(invitation)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Invite code generated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to generate invite code", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        buttonToggleVisibility.setOnClickListener(v -> {
            if (isHidden[0]) {
                textViewInviteCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                buttonToggleVisibility.setImageResource(R.drawable.ic_visibility_on);
            } else {
                textViewInviteCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                buttonToggleVisibility.setImageResource(R.drawable.ic_visibility_off);
            }
            isHidden[0] = !isHidden[0];
        });

        buttonCopy.setOnClickListener(v -> {
            String code = textViewInviteCode.getText().toString();
            if (!code.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Invite Code", code);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Invite code copied", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No code to copy", Toast.LENGTH_SHORT).show();
            }
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
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
        startActivity(intent);
    }
}
