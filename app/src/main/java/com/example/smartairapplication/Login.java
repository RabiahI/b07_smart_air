package com.example.smartairapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartairapplication.authentication.Registration;
import com.example.smartairapplication.authentication.UserRoleManager;
import com.google.android.material.textfield.TextInputEditText;

public class Login extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword;
    Button buttonLogin;
    ProgressBar progressBar;
    TextView textView, forgotPassword;
    private LoginPresenter presenter;

    @Override
    public void onStart() {
        super.onStart();
        presenter.checkCurrentUser();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        presenter = new LoginPresenter(this);

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.registerNow);
        forgotPassword = findViewById(R.id.forgot_password);

        textView.setOnClickListener(view -> presenter.onRegisterClicked());

        forgotPassword.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
            builder.setTitle("Forgot Password");
            builder.setMessage("Enter your email to receive a password reset link.");
            final EditText input = new EditText(Login.this);
            builder.setView(input);
            builder.setPositiveButton("Reset", (dialog, which) -> {
                String email = input.getText().toString().trim();
                presenter.resetPassword(email);
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.create().show();
        });

        buttonLogin.setOnClickListener(view -> {
            String email = editTextEmail.getText() == null ? "" : editTextEmail.getText().toString();
            String password = editTextPassword.getText() == null ? "" : editTextPassword.getText().toString();
            presenter.login(email, password);
        });
    }

    public void showLoading() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void navigateToRegistration() {
        Intent intent = new Intent(this, Registration.class);
        startActivity(intent);
        finish();
    }

    public void navigateBasedOnRole(String uid) {
        UserRoleManager.redirectUserBasedOnRole(this, uid);
    }
}
