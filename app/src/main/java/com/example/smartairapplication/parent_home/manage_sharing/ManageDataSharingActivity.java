package com.example.smartairapplication.parent_home.manage_sharing;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.smartairapplication.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ManageDataSharingActivity extends AppCompatActivity {

    public static final String EXTRA_CHILD_ID = "extra_child_id";
    public static final String EXTRA_CHILD_NAME = "extra_child_name";

    private SwitchCompat switchRescueLogs, switchControllerAdherence, switchSymptoms,
            switchTriggers, switchPEF, switchTriageIncidents;

    private SharedPreferences prefs;
    private String childId;
    private TextView childNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_data_sharing);

        childId = getIntent().getStringExtra(EXTRA_CHILD_ID);
        String childName = getIntent().getStringExtra(EXTRA_CHILD_NAME);
        childNameText = findViewById(R.id.childNameText);
        childNameText.setText(childName);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Data Sharing for " + childName);
        }

        // find views
        switchRescueLogs = findViewById(R.id.switchRescueLogs);
        switchControllerAdherence = findViewById(R.id.switchControllerAdherence);
        switchSymptoms = findViewById(R.id.switchSymptoms);
        switchTriggers = findViewById(R.id.switchTriggers);
        switchPEF = findViewById(R.id.switchPEF);
        switchTriageIncidents = findViewById(R.id.switchTriageIncidents);

        prefs = getSharedPreferences("data_sharing_prefs", MODE_PRIVATE);

        // load saved states for this child
        switchRescueLogs.setChecked(load("showRescueLogs"));
        switchControllerAdherence.setChecked(load("showControllerAdherence"));
        switchSymptoms.setChecked(load("showSymptoms"));
        switchTriggers.setChecked(load("showTriggers"));
        switchPEF.setChecked(load("showPEF"));
        switchTriageIncidents.setChecked(load("showTriage"));

        // attach listeners to save both locally and in Firebase
        setupListener(switchRescueLogs, "showRescueLogs");
        setupListener(switchControllerAdherence, "showControllerAdherence");
        setupListener(switchSymptoms, "showSymptoms");
        setupListener(switchTriggers, "showTriggers");
        setupListener(switchPEF, "showPEF");
        setupListener(switchTriageIncidents, "showTriage");

        // share all button
        Button shareAllButton = findViewById(R.id.shareAllButton);
        shareAllButton.setOnClickListener(v -> {
            setAllSwitches(true);
        });

        // stop sharing all button
        Button stopSharingAllButton = findViewById(R.id.stopSharingAllButton);
        stopSharingAllButton.setOnClickListener(v -> {
            setAllSwitches(false);
        });

        // back button
        Button backButton = findViewById(R.id.buttonBackToManageChildren);
        backButton.setOnClickListener(v -> finish());
    }

    // helper to attach listener
    private void setupListener(SwitchCompat switchCompat, String key) {
        switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveLocally(key, isChecked);
            saveToFirebase(childId, key, isChecked);
        });
    }

    // save locally
    private void saveLocally(String key, boolean value) {
        prefs.edit().putBoolean(childId + "_" + key, value).apply();
    }

    // load locally
    private boolean load(String key) {
        return prefs.getBoolean(childId + "_" + key, false);
    }

    // save to Firebase
    private void saveToFirebase(String childId, String key, boolean value) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child("Child")
                .child(childId)
                .child("DataSharing")
                .child(key);

        ref.setValue(value)
                .addOnSuccessListener(aVoid -> Log.d("ManageDataSharing", key + " updated in Firebase"))
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update " + key + " in Firebase", Toast.LENGTH_SHORT).show());
    }

    // toggle all switches at once
    private void setAllSwitches(boolean state) {
        // Toggle all but the last switch immediately
        if (switchRescueLogs.isChecked() != state) {
            switchRescueLogs.performClick();
        }
        if (switchControllerAdherence.isChecked() != state) {
            switchControllerAdherence.performClick();
        }
        if (switchSymptoms.isChecked() != state) {
            switchSymptoms.performClick();
        }
        if (switchTriggers.isChecked() != state) {
            switchTriggers.performClick();
        }
        if (switchPEF.isChecked() != state) {
            switchPEF.performClick();
        }

        // Toggle the last switch after a short delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (switchTriageIncidents.isChecked() != state) {
                switchTriageIncidents.performClick();
            }
        }, 100);
    }
}