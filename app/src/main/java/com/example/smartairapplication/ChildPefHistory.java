package com.example.smartairapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ChildPefHistory extends AppCompatActivity {

    private String childId, parentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_child_pef_history);

        childId = getIntent().getStringExtra("childId");
        parentId = getIntent().getStringExtra("parentId");

        loadPefLogs(childId);
    }

    private void loadPefLogs(String childId) {

    }
}