package com.example.smartairapplication;

import static android.content.Intent.getIntent;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class ManageDataSharingActivity extends AppCompatActivity {

    public static final String EXTRA_CHILD_ID = "extra_child_id";
    public static final String EXTRA_CHILD_NAME = "extra_child_name";
    private SwitchCompat switchRescueLogs, switchControllerAdherence, switchSymptoms,
            switchTriggers, switchPEF, switchTriageIncidents, switchChartSummaries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_data_sharing);

        String childId = getIntent().getStringExtra(EXTRA_CHILD_ID);
        String childName = getIntent().getStringExtra(EXTRA_CHILD_NAME);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Data Sharing for " + childName);
        }

        switchRescueLogs = findViewById(R.id.switchRescueLogs);
        switchControllerAdherence = findViewById(R.id.switchControllerAdherence);
        switchSymptoms = findViewById(R.id.switchSymptoms);
        switchTriggers = findViewById(R.id.switchTriggers);
        switchPEF = findViewById(R.id.switchPEF);
        switchTriageIncidents = findViewById(R.id.switchTriageIncidents);
        switchChartSummaries = findViewById(R.id.switchChartSummaries);

        // share all button
        Button shareAllButton = findViewById(R.id.shareAllButton);
        shareAllButton.setOnClickListener(v -> {
            switchRescueLogs.setChecked(true);
            switchControllerAdherence.setChecked(true);
            switchSymptoms.setChecked(true);
            switchTriggers.setChecked(true);
            switchPEF.setChecked(true);
            switchTriageIncidents.setChecked(true);
            switchChartSummaries.setChecked(true);
        });

        // stop sharing all button
        Button stopSharingAllButton = findViewById(R.id.stopSharingAllButton);
        stopSharingAllButton.setOnClickListener(v -> {
            switchRescueLogs.setChecked(false);
            switchControllerAdherence.setChecked(false);
            switchSymptoms.setChecked(false);
            switchTriggers.setChecked(false);
            switchPEF.setChecked(false);
            switchTriageIncidents.setChecked(false);
            switchChartSummaries.setChecked(false);
        });


        // logout button
        Button backButton = findViewById(R.id.buttonBackToManageChildren);

        backButton.setOnClickListener(v -> {
            finish();
        });


    }


}