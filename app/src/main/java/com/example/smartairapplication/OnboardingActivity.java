package com.example.smartairapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (isFirstLogin()) {
            Bundle extras = getIntent().getExtras();
            String role = extras != null ? extras.getString("role", "User") : "User";
            String uid = extras != null ? extras.getString("uid", "") : "";

            OnboardingDialogFragment dialog = new OnboardingDialogFragment();
            Bundle args = new Bundle();
            args.putString("role", role);
            args.putString("uid", uid);
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "onboardingDialog");
        } else {
            // Not a first login, close this activity
            finish();
        }
    }

    public static boolean isFirstLogin() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            long creationTime = user.getMetadata().getCreationTimestamp();
            long lastSignInTime = user.getMetadata().getLastSignInTimestamp();

            if (creationTime == lastSignInTime) {
                return true;
            }
        }
        return false;
    }




}