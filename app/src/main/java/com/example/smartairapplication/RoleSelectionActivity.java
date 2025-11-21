package com.example.smartairapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RoleSelectionActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        mAuth = FirebaseAuth.getInstance();

        Button buttonAdult = findViewById(R.id.buttonAdult);
        Button buttonChild = findViewById(R.id.buttonChild);

        buttonAdult.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, Login.class);
            startActivity(intent);
        });

        buttonChild.setOnClickListener(v -> showChildInvitationDialog());
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            UserRoleManager.redirectUserBasedOnRole(this, currentUser.getUid());
        }
    }

    private void showChildInvitationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_child_invitation, null);
        builder.setView(view);

        EditText editTextCode = view.findViewById(R.id.editTextCode);
        Button buttonConfirm = view.findViewById(R.id.buttonConfirm);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);

        AlertDialog dialog = builder.create();

        buttonConfirm.setOnClickListener(v -> {
            String code = editTextCode.getText().toString().trim();
            if (!code.isEmpty()) {
                validateInvitationCode(code, dialog);
            } else {
                Toast.makeText(this, "Please enter an invitation code", Toast.LENGTH_SHORT).show();
            }
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void validateInvitationCode(String code, AlertDialog dialog) {
        DatabaseReference childInvitesRef = FirebaseDatabase.getInstance().getReference("ChildInvitations");
        childInvitesRef.child(code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ChildInvitation invitation = snapshot.getValue(ChildInvitation.class);
                    if (invitation != null && invitation.getExpiry() > System.currentTimeMillis()) {
                        dialog.dismiss();
                        Intent intent = new Intent(RoleSelectionActivity.this, ChildLoginActivity.class);
                        intent.putExtra("parentId", invitation.getParentId());
                        startActivity(intent);
                    } else {
                        snapshot.getRef().removeValue(); // Clean up expired or invalid code
                        Toast.makeText(RoleSelectionActivity.this, "Invitation code has expired", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RoleSelectionActivity.this, "Invalid invitation code", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RoleSelectionActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
