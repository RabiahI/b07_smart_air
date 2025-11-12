package com.example.smartairapplication;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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


public class ManageAccessActivity extends AppCompatActivity implements AccessAdapter.OnInviteClickListener{

    private RecyclerView recyclerViewAccess;
    private AccessAdapter adapter;
    private List<Child> childList;
    private DatabaseReference childrenRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_access);

        mAuth = FirebaseAuth.getInstance();
        String parentId = mAuth.getCurrentUser().getUid();
        childrenRef = FirebaseDatabase.getInstance().getReference("Users").child("Parent").child(parentId).child("Children");
        recyclerViewAccess = findViewById(R.id.recyclerViewAccess);
        recyclerViewAccess.setLayoutManager(new LinearLayoutManager(this));

        childList = new ArrayList<>();
        adapter = new AccessAdapter(this, childList, this);
        recyclerViewAccess.setAdapter(adapter);

        loadChildren();
    }

    private void loadChildren(){
        childrenRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                childList.clear();
                for (DataSnapshot childSnap : snapshot.getChildren()){
                    Child child = childSnap.getValue(Child.class);
                    if (child!= null) {
                        DatabaseReference inviteRef = childSnap.getRef().child("Invite");

                        inviteRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot inviteSnap) {
                                if (inviteSnap.exists()) {
                                    Boolean accepted = inviteSnap.child("accepted").getValue(Boolean.class);
                                    String code = inviteSnap.child("code").getValue(String.class);
                                    Long expiresAt = inviteSnap.child("expiresAt").getValue(Long.class);
                                    long currentTime = System.currentTimeMillis();

                                    if (expiresAt != null && currentTime > expiresAt) {
                                        // Code expired — reset
                                        inviteSnap.getRef().removeValue();
                                        child.setAccessStatus("not_shared");
                                        child.setInviteCode(null);
                                        childrenRef.child(child.getChildId()).setValue(child);
                                        Toast.makeText(ManageAccessActivity.this,
                                                "Invite code for " + child.getName() + " has expired.",
                                                Toast.LENGTH_SHORT).show();
                                    } else if (accepted != null && accepted) {
                                        child.setAccessStatus("accepted");
                                    } else if (code != null) {
                                        child.setAccessStatus("generated");
                                        child.setInviteCode(code);
                                    } else {
                                        child.setAccessStatus("not_shared");
                                    }
                                } else {
                                    child.setAccessStatus("not_shared");
                                }
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(ManageAccessActivity.this, "Error loading invites", Toast.LENGTH_SHORT).show();
                            }
                        });

                        childList.add(child);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageAccessActivity.this, "Failed to load children.", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public void onInviteClick(int position) {
        Child selectedChild = childList.get(position);
        showInviteProviderDialog(selectedChild);
    }

    private void showInviteProviderDialog(Child child){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_invite_provider, null);


        TextView textViewInviteCode = view.findViewById(R.id.textViewInviteCode);
        Button buttonGenerate = view.findViewById(R.id.buttonGenerateCode);
        Button buttonCopy = view.findViewById(R.id.buttonCopyCode);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        ImageButton buttonToggleVisibility = view.findViewById(R.id.buttonToggleVisibility);
        final boolean[] isHidden = {true};

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        DatabaseReference inviteRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child("Parent")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Children")
                .child(child.getChildId())
                .child("Invite");

        buttonGenerate.setOnClickListener(v -> {
            String code = generateInviteCode();
            textViewInviteCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            textViewInviteCode.setText(code);
            buttonToggleVisibility.setImageResource(R.drawable.ic_visibility_off);
            isHidden[0] = true;
            Invite invite = new Invite(code, System.currentTimeMillis(), System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000);
            inviteRef.setValue(invite)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Invite code generated for " + child.getName(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to generate invite code.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        buttonToggleVisibility.setOnClickListener(v -> {
            if (isHidden[0]){
                //show code
                textViewInviteCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                buttonToggleVisibility.setImageResource(R.drawable.ic_visibility_on);

            } else{
                //hide code
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
                Toast.makeText(this, "No code to copy.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
    }
    private String generateInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int index = (int) (Math.random() * chars.length());
            code.append(chars.charAt(index));
        }
        return code.toString();
    }

}