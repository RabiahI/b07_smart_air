package com.example.smartairapplication.authentication;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChildLoginModel {

    private final FirebaseAuth mAuth;
    private final DatabaseReference databaseReference;

    public interface SignInCallback {
        void onSuccess();
        void onInvalidCredentials();
        void onVerificationFailure();
        void onGenericFailure();
    }

    public ChildLoginModel() {
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void signOut() {
        mAuth.signOut();
    }

    public void verifyChild(String parentId, String childId, SignInCallback callback) {
        DatabaseReference parentRef = databaseReference.child("Parent").child(parentId).child("Children").child(childId);
        parentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    callback.onSuccess();
                } else {
                    callback.onVerificationFailure();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onGenericFailure();
            }
        });
    }

    public void signInAndVerifyChild(String email, String password, String parentId, SignInCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            verifyChild(parentId, firebaseUser.getUid(), callback);
                        } else {
                            callback.onGenericFailure();
                        }
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthInvalidUserException ||
                                e instanceof FirebaseAuthInvalidCredentialsException) {
                            callback.onInvalidCredentials();
                        } else {
                            callback.onGenericFailure();
                        }
                    }
                });
    }
}
