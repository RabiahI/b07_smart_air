package com.example.smartairapplication;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserRoleManager {

    public static void redirectUserBasedOnRole(Activity activity, String uid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        String[] roles = {"Parent", "Provider", "Child"};
        for (String role : roles) {
            DatabaseReference roleRef = userRef.child(role).child(uid);
            roleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(activity, "Login Successful!", Toast.LENGTH_SHORT).show();
                        Intent intent;
                        switch (role) {
                            case "Parent":
                                intent = new Intent(activity, ParentHomeActivity.class);
                                break;
                            case "Provider":
                                intent = new Intent(activity, ProviderManageChildren.class);
                                break;
                            default:
                                intent = new Intent(activity, ChildHomeActivity.class);
                                break;
                        }
                        activity.startActivity(intent);
                        activity.finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(activity, "Failed to get user data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
