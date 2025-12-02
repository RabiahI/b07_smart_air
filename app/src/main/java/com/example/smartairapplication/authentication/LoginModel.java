package com.example.smartairapplication.authentication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginModel {

    private final FirebaseAuth mAuth;

    public interface SignInCallback {
        void onSuccess();
        void onInvalidCredentials();
        void onGenericFailure();
    }

    public interface PasswordResetCallback {
        void onSuccess();
        void onFailure();
    }

    public LoginModel() {
        mAuth = FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void signIn(String email, String password, SignInCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
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

    public void sendPasswordResetEmail(String email, PasswordResetCallback callback) {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onFailure();
            }
        });
    }
}
