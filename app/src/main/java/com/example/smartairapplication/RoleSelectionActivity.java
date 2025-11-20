package com.example.smartairapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        Button buttonAdult = findViewById(R.id.buttonAdult);
        Button buttonChild = findViewById(R.id.buttonChild);

        buttonAdult.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, Registration.class);
            startActivity(intent);
        });

        buttonChild.setOnClickListener(v -> {
        });
    }
}
