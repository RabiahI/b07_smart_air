package com.example.smartairapplication;

import static com.example.smartairapplication.OnboardingActivity.isFirstLogin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProviderManageChildren extends AppCompatActivity implements ChildrenAdapter.OnChildClickListener {

    private Button logoutButton;
    private RecyclerView recyclerView;
    private ChildrenAdapter childrenAdapter;
    private List<Child> childrenList;
    private EditText accessCodeInput;
    private Child selectedChild;
    private Button submitAccessCodeButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_manage_children);

        recyclerView = findViewById(R.id.childrenRecyclerView);
        logoutButton = findViewById(R.id.logout);
        submitAccessCodeButton = findViewById(R.id.submitAccessCodeButton);
        accessCodeInput = findViewById(R.id.accessCodeInput);

        childrenList = new ArrayList<>();
        childrenAdapter = new ChildrenAdapter(childrenList, this); // Make sure your adapter has this constructor
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(childrenAdapter);

        loadProviderChildren();

        logoutButton.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        });

        submitAccessCodeButton.setOnClickListener(view -> {
            String enteredCode = accessCodeInput.getText().toString().trim();

            if (enteredCode.isEmpty()) {
                Toast.makeText(this, "Enter an access code", Toast.LENGTH_SHORT).show();
                return;
            }

            checkAccessCode(enteredCode);
        });



        if (isFirstLogin()) {
            OnboardingDialogFragment dialog = new OnboardingDialogFragment();
            Bundle args = new Bundle();
            args.putString("role", getIntent().getStringExtra("role"));
            args.putString("uid", getIntent().getStringExtra("uid"));
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "onboarding_dialog");
        }
    }

    @Override
    public void onChildClick(Child child) {
        Toast.makeText(this, "Clicked: " + child.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewDataClick(Child child) {
        Intent intent = new Intent(this, ProviderHomeActivity.class);
        intent.putExtra("childId", child.getChildId());
        startActivity(intent);
    }

    private void loadProviderChildren() {
        String providerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference providerChildrenRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child("Provider")
                .child(providerId)
                .child("Children");

        providerChildrenRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                childrenList.clear();
                for (DataSnapshot childSnap : snapshot.getChildren()) {
                    Child child = childSnap.getValue(Child.class);
                    if (child != null) {
                        childrenList.add(child);
                    }
                }
                childrenAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProviderManageChildren.this, "Failed to load children: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void checkAccessCode(String enteredCode) {
        DatabaseReference parentChildrenRef = FirebaseDatabase.getInstance().getReference("Users/Parent");

        parentChildrenRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean codeFound = false;

                outerLoop:
                for (DataSnapshot parentSnap : snapshot.getChildren()) {
                    for (DataSnapshot childSnap : parentSnap.child("Children").getChildren()) {
                        DataSnapshot inviteSnap = childSnap.child("Invite");
                        if (inviteSnap.exists()) {
                            String code = inviteSnap.child("code").getValue(String.class);
                            Boolean accepted = inviteSnap.child("accepted").getValue(Boolean.class);
                            Long expiresAt = inviteSnap.child("expiresAt").getValue(Long.class);
                            long currentTime = System.currentTimeMillis();

                            if (code != null && code.equals(enteredCode)) {
                                if (expiresAt != null && currentTime > expiresAt) {
                                    inviteSnap.getRef().removeValue();
                                    Toast.makeText(ProviderManageChildren.this,
                                            "This access code has expired.", Toast.LENGTH_SHORT).show();
                                } else if (accepted != null && accepted) {
                                    Toast.makeText(ProviderManageChildren.this,
                                            "Access already accepted for this child.", Toast.LENGTH_SHORT).show();
                                } else {
                                    String childId = childSnap.getKey();

                                    String childName = childSnap.child("name").getValue(String.class);
                                    Toast.makeText(ProviderManageChildren.this,
                                            "Access granted for: " + childName, Toast.LENGTH_SHORT).show();
                                    inviteSnap.child("accepted").getRef().setValue(true);

                                    Child child = new Child();
                                    child.setChildId(childId);
                                    child.setName(childName);
                                    child.setAccessStatus("accepted");

                                    childrenList.add(child);
                                    childrenAdapter.notifyItemInserted(childrenList.size() - 1);

                                    String providerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    DatabaseReference providerChildrenRef = FirebaseDatabase.getInstance()
                                            .getReference("Users")
                                            .child("Provider")
                                            .child(providerId)
                                            .child("Children")
                                            .child(childId);

                                    providerChildrenRef.setValue(child);
                                }
                                codeFound = true;
                                break outerLoop;
                            }
                        }
                    }
                }

                if (!codeFound) {
                    Toast.makeText(ProviderManageChildren.this, "Invalid access code", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProviderManageChildren.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
