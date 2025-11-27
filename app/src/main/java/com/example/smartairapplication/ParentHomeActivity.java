package com.example.smartairapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class ParentHomeActivity extends AppCompatActivity {

    private CardView childButton, manageProviderButton;
    private BottomNavigationView bottomNav;


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

        childButton = findViewById(R.id.manageChildrenButton);
        manageProviderButton = findViewById(R.id.manageSharingButton);
        bottomNav = findViewById(R.id.bottomNav);

        childButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ChildManagementActivity.class);
            startActivity(intent);
        });

        manageProviderButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ManageAccessActivity.class);
            startActivity(intent);
        });

        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_alerts) {
                startActivity(new Intent(getApplicationContext(), ParentAlertsActivity.class));
                return false;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(getApplicationContext(), ParentHistoryActivity.class));
                return false;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(getApplicationContext(), ParentSettingsActivity.class));
                return false;
            } else if (itemId == R.id.nav_home) {
                return true;
            }
            return false;
        });
    }
}
