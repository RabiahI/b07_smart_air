package com.example.smartairapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "ChildLoginPrefs";
    private static final String PARENT_ID_KEY = "parentId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);


        String savedParentId = sharedPreferences.getString(PARENT_ID_KEY, null);
        if (savedParentId != null && !savedParentId.trim().isEmpty()) {
            Intent intent = new Intent(RoleSelectionActivity.this, ChildLoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }


        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
        } else {
            setContentView(R.layout.activity_role_selection);

            Button buttonAdult = findViewById(R.id.buttonAdult);
            Button buttonChild = findViewById(R.id.buttonChild);

            buttonAdult.setOnClickListener(v -> {
                Intent intent = new Intent(RoleSelectionActivity.this, Login.class);
                startActivity(intent);
            });

            buttonChild.setOnClickListener(v -> showChildInvitationDialog());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            UserRoleManager.redirectUserBasedOnRole(this, currentUser.getUid());
            finish();
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
        DatabaseReference invitesRef = FirebaseDatabase.getInstance().getReference("invitationCodes");
        invitesRef.child(code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    InvitationLookup lookup = snapshot.getValue(InvitationLookup.class);
                    if (lookup != null && lookup.getParentId() != null) {
                        String parentId = lookup.getParentId();
                        sharedPreferences.edit().putString(PARENT_ID_KEY, parentId).apply();

                        dialog.dismiss();
                        Intent intent = new Intent(RoleSelectionActivity.this, ChildLoginActivity.class);
                        intent.putExtra("parentId", parentId);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RoleSelectionActivity.this, "Invalid invitation code.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RoleSelectionActivity.this, "Invalid invitation code.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RoleSelectionActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}