package com.example.smartairapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ChildLoginActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ChildLoginPrefs";
    private static final String PARENT_ID_KEY = "parentId";

    TextInputEditText editTextUsername;
    TextInputEditText editTextPassword;
    Button buttonLogin;
    ProgressBar progressBar;
    private String parentId;
    private SharedPreferences sharedPreferences;
    private ChildLoginPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_login);

        editTextUsername = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBar);
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        parentId = getIntent().getStringExtra("parentId");
        if (parentId == null) {
            parentId = sharedPreferences.getString(PARENT_ID_KEY, null);
        } else {
            sharedPreferences.edit().putString(PARENT_ID_KEY, parentId).apply();
        }

        if (parentId == null || parentId.trim().isEmpty()) {
            Toast.makeText(this, "Parent ID not found. Please use invitation code again.", Toast.LENGTH_LONG).show();
            navigateToRoleSelection();
            return;
        }

        presenter = new ChildLoginPresenter(this, parentId);
        presenter.checkCurrentUser();

        buttonLogin.setOnClickListener(v -> {
            String username = String.valueOf(editTextUsername.getText()).trim();
            String password = String.valueOf(editTextPassword.getText());
            presenter.login(username, password);
        });
    }

    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void navigateToChildHome(String parentId) {
        Intent intent = new Intent(this, ChildHomeActivity.class);
        intent.putExtra("parentId", parentId);
        startActivity(intent);
        finish();
    }

    public void navigateToRoleSelection() {
        startActivity(new Intent(this, RoleSelectionActivity.class));
        finish();
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
    }
}
