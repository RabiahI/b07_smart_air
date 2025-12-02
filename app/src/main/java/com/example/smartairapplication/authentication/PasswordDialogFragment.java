package com.example.smartairapplication.authentication;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class PasswordDialogFragment extends DialogFragment {

    public interface PasswordDialogListener {
        void onPasswordVerified();
    }

    private PasswordDialogListener listener;
    private EditText passwordInput;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (PasswordDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement PasswordDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        builder.setTitle("Enter Parent Password");

        passwordInput = new EditText(getActivity());
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(passwordInput);

        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String password = passwordInput.getText().toString();
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser parent = mAuth.getCurrentUser();

                if (parent != null && parent.getEmail() != null && !password.isEmpty()) {
                    mAuth.signInWithEmailAndPassword(parent.getEmail(), password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    listener.onPasswordVerified();
                                    dismiss();
                                } else {
                                    Toast.makeText(getContext(), "Wrong password. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(getContext(), "Password cannot be empty.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
