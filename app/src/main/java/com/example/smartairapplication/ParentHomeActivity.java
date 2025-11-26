package com.example.smartairapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ParentHomeActivity extends AppCompatActivity {

    Button logoutButton;
    Button childButton, manageProviderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_home);

        if (OnboardingActivity.isFirstLogin()) {
            OnboardingDialogFragment dialog = new OnboardingDialogFragment();

            Bundle args = new Bundle();
            args.putString("role", getIntent().getStringExtra("role"));
            args.putString("uid", getIntent().getStringExtra("uid"));
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "onboarding_dialog");
        }

        logoutButton = findViewById(R.id.logout);
        childButton = findViewById(R.id.btn_child);
        manageProviderButton = findViewById(R.id.buttonManageProvider);

        logoutButton.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        childButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ChildManagementActivity.class);
            startActivity(intent);
        });

        manageProviderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ManageAccessActivity.class);
                startActivity(intent);
            }
        });
    }
}
