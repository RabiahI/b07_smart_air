package com.example.smartairapplication.parent_home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartairapplication.parent_home.alerts.ParentAlertsActivity;
import com.example.smartairapplication.parent_home.history_browser.ParentHistoryActivity;
import com.example.smartairapplication.R;
import com.example.smartairapplication.authentication.Login;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ParentSettingsActivity extends AppCompatActivity {

    private TextView textViewName, textViewNotes;
    private Button logoutButton;
    private BottomNavigationView bottomNav;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_settings);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        textViewName = findViewById(R.id.textViewName);
        textViewNotes = findViewById(R.id.textViewNotes);
        logoutButton = findViewById(R.id.logout);
        bottomNav = findViewById(R.id.bottomNav);

        if (currentUser != null) {
            textViewName.setText(currentUser.getDisplayName());
            // Notes are not available in the standard FirebaseUser object.
            // You would need to fetch this from your database if you store it there.
            textViewNotes.setText(""); 
        }

        logoutButton.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });


        bottomNav.setSelectedItemId(R.id.nav_settings);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                finish();
                return true;
            } else if (itemId == R.id.nav_alerts) {
                startActivity(new Intent(getApplicationContext(), ParentAlertsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(getApplicationContext(), ParentHistoryActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_settings) {
                return true;
            }
            return false;
        });
    }
}