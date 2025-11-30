package com.example.smartairapplication;

import static com.example.smartairapplication.OnboardingActivity.isFirstLogin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProviderHomeActivity extends AppCompatActivity {

    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_home);

        if (OnboardingActivity.isFirstLogin()) {
            OnboardingDialogFragment dialog = new OnboardingDialogFragment();

            Bundle args = new Bundle();
            args.putString("role", getIntent().getStringExtra("role"));
            args.putString("uid", getIntent().getStringExtra("uid"));
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "onboarding_dialog");
        }

        button = findViewById(R.id.logout);

        button.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
