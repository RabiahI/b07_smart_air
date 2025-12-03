package com.example.smartairapplication.provider_home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartairapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class ProviderHomeActivity extends AppCompatActivity {

    private View triageContainer, symptomsContainer, triggersContainer,
            controllerAdherenceContainer, PEFContainer, RescueLogsContainer, triggersTagContainer, chartSummaries;
    private View nightWakingSymptomContainer, wheezingSymptomContainer;
    private TextView triageDate, triageEscalated, triageResult, triagePEF;
    private TextView triggerExercise, triggerColdAir, triggerDust, triggerPet, triggerSmoke, triggerIllness, triggerStrongOdor;
    private TextView difficultyAfterCount, difficultyBeforeCount, puffCount, feelingText;
    private TextView pefDate1, pefDate2, pefDate3, pefValue1, pefValue2, pefValue3;
    private TextView adherencePercentage, dosesTakenCount, daysMissedCount, daysTaken;
    private ProgressBar adherenceProgressBar;
    private View viewAllPefButton, viewAllTriagesButton, viewAllSymptomsButton, viewAllRescueLogsButton;
    private String childId, parentId;
    private DatabaseReference dataSharingRef;
    private ValueEventListener dataSharingListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_home);

        // Remove redundant local declarations (String childId, parentId)
        this.childId = getIntent().getStringExtra("childId");
        this.parentId = getIntent().getStringExtra("parentId");


        // find main containers
        triageContainer = findViewById(R.id.triage_container);
        symptomsContainer = findViewById(R.id.symptoms_containers);
        triggersTagContainer = findViewById(R.id.triggersTagContainer);
        controllerAdherenceContainer = findViewById(R.id.controller_adherence_containers);
        PEFContainer = findViewById(R.id.pef_containers);
        RescueLogsContainer = findViewById(R.id.rescue_log_container);

        // initialize triage
        triageDate = findViewById(R.id.triageDate);
        triageEscalated = findViewById(R.id.triageEscalated);
        triagePEF = findViewById(R.id.triagePEF);
        triageResult = findViewById(R.id.triageResult);

        // find symptom views
        nightWakingSymptomContainer = findViewById(R.id.nightWakingSymptomContainer);
        wheezingSymptomContainer = findViewById(R.id.wheezingSymptomContainer);

        // initialize triggers
        triggerExercise = findViewById(R.id.triggerExercise);
        triggerColdAir = findViewById(R.id.triggerColdAir);
        triggerDust = findViewById(R.id.triggerDust);
        triggerPet = findViewById(R.id.triggerPet);
        triggerSmoke = findViewById(R.id.triggerSmoke);
        triggerIllness = findViewById(R.id.triggerIllness);
        triggerStrongOdor = findViewById(R.id.triggerStrongOdor);

        // initialize rescue logs
        difficultyAfterCount = findViewById(R.id.difficultyAfterCount);
        difficultyBeforeCount = findViewById(R.id.difficultyBeforeCount);
        puffCount = findViewById(R.id.puffCount);
        feelingText = findViewById(R.id.feelingText);

        // initialize pef
        pefValue1 = findViewById(R.id.pefValue1);
        pefValue2 = findViewById(R.id.pefValue2);
        pefValue3 = findViewById(R.id.pefValue3);
        pefDate1 = findViewById(R.id.pefDate1);
        pefDate2 = findViewById(R.id.pefDate2);
        pefDate3 = findViewById(R.id.pefDate3);

        // initialize controller adherence stuff
        adherencePercentage = findViewById(R.id.adherencePercentage);
        dosesTakenCount = findViewById(R.id.dosesTakenCount);
        daysMissedCount = findViewById(R.id.dosesMissedCount);
        daysTaken = findViewById(R.id.daysTaken);
        adherenceProgressBar = findViewById(R.id.adherenceProgressBar);

        // INITIALIZE ALL 'VIEW ALL' BUTTONS
        viewAllTriagesButton = findViewById(R.id.viewAllTriagesButton);
        viewAllSymptomsButton = findViewById(R.id.viewAllSymptomsButton);
        viewAllPefButton = findViewById(R.id.viewAllPefButton);
        viewAllRescueLogsButton = findViewById(R.id.viewAllRescueLogsButton);

        if (viewAllPefButton != null) {
            viewAllPefButton.setOnClickListener(v -> onViewAllPefClicked());
        }
        if (viewAllSymptomsButton != null) {
            viewAllSymptomsButton.setOnClickListener(v -> onViewAllSymptomsClicked());
        }
        if (viewAllTriagesButton != null) {
            viewAllTriagesButton.setOnClickListener(v -> onViewAllTriagesClicked());
        }
        if (viewAllRescueLogsButton != null) {
            viewAllRescueLogsButton.setOnClickListener(v -> onViewAllRescueLogsClicked());
        }

        if (childId != null && parentId != null) {
            loadDataSharingSettings(childId, parentId);
        }
    }

    private void onViewAllPefClicked() {
        Intent intent = new Intent(ProviderHomeActivity.this, PefLogActivity.class);
        intent.putExtra("childId", childId);
        intent.putExtra("parentId", parentId);
        startActivity(intent);
    }

    private void onViewAllSymptomsClicked() {
        Intent intent = new Intent(ProviderHomeActivity.this, SymptomsLogActivity.class);
        intent.putExtra("childId", childId);
        intent.putExtra("parentId", parentId);
        startActivity(intent);
    }

    private void onViewAllTriagesClicked() {
        Intent intent = new Intent(ProviderHomeActivity.this, TriageLogActivity.class);
        intent.putExtra("childId", childId);
        intent.putExtra("parentId", parentId);
        startActivity(intent);
    }

    private void onViewAllRescueLogsClicked() {
        Intent intent = new Intent(ProviderHomeActivity.this, RescueLogsActivity.class);
        intent.putExtra("childId", childId);
        intent.putExtra("parentId", parentId);
        startActivity(intent);
    }

    private void onViewAllControllerAdherenceClicked() {
        Intent intent = new Intent(ProviderHomeActivity.this, ControllerAdherenceLogActivity.class);
        intent.putExtra("childId", childId);
        intent.putExtra("parentId", parentId);
        startActivity(intent);
    }

    private void loadDataSharingSettings(String childId, String parentId) {
        dataSharingRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child("Child")
                .child(childId)
                .child("DataSharing");

        dataSharingListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean showTriage = snapshot.child("showTriage").getValue(Boolean.class) != null
                        && Boolean.TRUE.equals(snapshot.child("showTriage").getValue(Boolean.class));
                boolean showSymptoms = snapshot.child("showSymptoms").getValue(Boolean.class) != null
                        && Boolean.TRUE.equals(snapshot.child("showSymptoms").getValue(Boolean.class));
                boolean showTriggers = snapshot.child("showTriggers").getValue(Boolean.class) != null
                        && Boolean.TRUE.equals(snapshot.child("showTriggers").getValue(Boolean.class));
                boolean showController = snapshot.child("showControllerAdherence").getValue(Boolean.class) != null
                        && Boolean.TRUE.equals(snapshot.child("showControllerAdherence").getValue(Boolean.class));
                boolean showPEF = snapshot.child("showPEF").getValue(Boolean.class) != null
                        && Boolean.TRUE.equals(snapshot.child("showPEF").getValue(Boolean.class));
                boolean showRescueLogs = snapshot.child("showRescueLogs").getValue(Boolean.class) != null
                        && Boolean.TRUE.equals(snapshot.child("showRescueLogs").getValue(Boolean.class));

                triageContainer.setVisibility(showTriage ? View.VISIBLE : View.GONE);
                symptomsContainer.setVisibility(showSymptoms ? View.VISIBLE : View.GONE);
                controllerAdherenceContainer.setVisibility(showController ? View.VISIBLE : View.GONE);
                PEFContainer.setVisibility(showPEF ? View.VISIBLE : View.GONE);
                RescueLogsContainer.setVisibility(showRescueLogs ? View.VISIBLE : View.GONE);


                if (showTriage) {
                    loadTriageIncident(childId, parentId);
                }

                if (showSymptoms) {
                    loadSymptoms(childId, parentId);
                }

                if (showRescueLogs) {
                    loadRescueLogs(childId, parentId);
                }

                if (showPEF) {
                    loadPEF(childId, parentId);
                }

                if (showController) {
                    loadControllerAdherence(childId, parentId);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProviderHomeActivity.this,
                        "Failed to load settings: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };
        dataSharingRef.addValueEventListener(dataSharingListener);
    }

    private void loadControllerAdherence(String childId, String parentId) {
        DatabaseReference logsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child("Parent")
                .child(parentId)
                .child("Children")
                .child(childId)
                .child("Logs")
                .child("medicineLogs");

        logsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Set<Long> daysWithController = new HashSet<>();
                    long totalPuffsTaken = 0;

                    final int ADHERENCE_PERIOD_DAYS = 30;
                    final long MILLIS_IN_DAY = TimeUnit.DAYS.toMillis(1);

                    long now = System.currentTimeMillis();
                    long thirtyDaysAgoStartOfDay = (now / MILLIS_IN_DAY) * MILLIS_IN_DAY - (ADHERENCE_PERIOD_DAYS * MILLIS_IN_DAY);

                    for (DataSnapshot logSnapshot : snapshot.getChildren()) {
                        String inhalerType = logSnapshot.child("inhalerType").getValue(String.class);
                        Long timestamp = getLongValueFromSnapshot(logSnapshot.child("timestamp"));
                        Long puffCount = getLongValueFromSnapshot(logSnapshot.child("puffCount"));

                        if ("Controller".equals(inhalerType) && timestamp != null && puffCount != null) {

                            if (timestamp >= thirtyDaysAgoStartOfDay) {
                                long startOfDay = (timestamp / MILLIS_IN_DAY) * MILLIS_IN_DAY;
                                daysWithController.add(startOfDay);
                                totalPuffsTaken += puffCount;
                            }
                        }
                    }

                    int daysTakenCount = daysWithController.size();
                    int daysMissed = Math.max(0, ADHERENCE_PERIOD_DAYS - daysTakenCount);
                    int percentage = (daysTakenCount * 100) / ADHERENCE_PERIOD_DAYS;

                    if (adherencePercentage != null) {
                        adherencePercentage.setText(String.format(Locale.getDefault(), "%d%%", percentage));
                    }
                    if (dosesTakenCount != null) {
                        dosesTakenCount.setText(String.valueOf(totalPuffsTaken));
                    }
                    if (daysMissedCount != null) {
                        daysMissedCount.setText(String.valueOf(daysMissed));
                    }
                    if (adherenceProgressBar != null) {
                        adherenceProgressBar.setProgress(percentage);
                    }

                    if (daysTaken != null) {
                        daysTaken.setText(String.valueOf(daysTakenCount));
                    }

                    controllerAdherenceContainer.setVisibility(View.VISIBLE);
                } else {
                    controllerAdherenceContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProviderHomeActivity.this,
                        "Failed to load adherence data: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                controllerAdherenceContainer.setVisibility(View.GONE);
            }
        });
    }

    private Long getLongValueFromSnapshot(DataSnapshot snapshot) {
        Object value = snapshot.getValue();
        if (value == null) {
            return null;
        }

        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                System.err.println("Failed to parse String to Long: " + value);
                return null;
            }
        }
        return null;
    }

    private void loadPEF(String childId, String parentId) {
        DatabaseReference logsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child("Parent")
                .child(parentId)
                .child("Children")
                .child(childId)
                .child("Logs")
                .child("pefLogs");

        logsRef.orderByKey()
                .limitToLast(3)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChildren()) {
                            DataSnapshot[] pefLogs = new DataSnapshot[(int) snapshot.getChildrenCount()];
                            int index = 0;
                            for (DataSnapshot log : snapshot.getChildren()) {
                                pefLogs[index++] = log;
                            }

                            TextView[] dateViews = {pefDate1, pefDate2, pefDate3};
                            TextView[] valueViews = {pefValue1, pefValue2, pefValue3};

                            int logIndex = 0;
                            for (DataSnapshot log : pefLogs) {
                                if (logIndex >= 3) break;

                                Long timestamp = log.child("timestamp").getValue(Long.class);

                                String pef = log.child("pefValue").getValue(Object.class) != null
                                        ? String.valueOf(log.child("pefValue").getValue(Object.class))
                                        : null;

                                String dateString = "N/A";
                                if (timestamp != null) {
                                    Date date = new Date(timestamp);
                                    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
                                    dateString = df.format(date);
                                }

                                if (dateViews[logIndex] != null) {
                                    dateViews[logIndex].setText(dateString);
                                }
                                if (valueViews[logIndex] != null) {
                                    valueViews[logIndex].setText(pef != null ? pef : "N/A");
                                }

                                logIndex++;
                            }

                            PEFContainer.setVisibility(View.VISIBLE);
                        } else {
                            PEFContainer.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProviderHomeActivity.this,
                                "Failed to load PEF logs: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        PEFContainer.setVisibility(View.GONE);
                    }
                });
    }

    private void loadRescueLogs(String childId, String parentId) {
        DatabaseReference logsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child("Parent")
                .child(parentId)
                .child("Children")
                .child(childId)
                .child("Logs")
                .child("medicineLogs");

        logsRef.orderByKey()
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChildren()) {
                            DataSnapshot latest = snapshot.getChildren().iterator().next();

                            String difficultyAfter = latest.child("sobAfter").getValue(Object.class) != null
                                    ? String.valueOf(latest.child("sobAfter").getValue(Object.class))
                                    : "N/A";
                            String difficultyBefore = latest.child("sobBefore").getValue(Object.class) != null
                                    ? String.valueOf(latest.child("sobBefore").getValue(Object.class))
                                    : "N/A";
                            String puffs = latest.child("puffCount").getValue(Object.class) != null
                                    ? String.valueOf(latest.child("puffCount").getValue(Object.class))
                                    : "N/A";

                            String feeling;
                            if ("Better".equals(latest.child("postFeeling").getValue(String.class))) {
                                feeling = "😊 Feeling Better";
                            } else if ("Worse".equals(latest.child("postFeeling").getValue(String.class))) {
                                feeling = "🙁 Feeling Worse";
                            } else {
                                feeling = "😐 Feeling the Same";
                            }

                            if (difficultyAfterCount != null) {
                                difficultyAfterCount.setText(difficultyAfter);
                            }
                            if (difficultyBeforeCount != null) {
                                difficultyBeforeCount.setText(difficultyBefore);
                            }
                            if (puffCount != null) {
                                puffCount.setText(puffs);
                            }

                            if (feelingText != null) {
                                feelingText.setText(feeling != null ? feeling : "N/A");
                            }

                            RescueLogsContainer.setVisibility(View.VISIBLE);

                        } else {
                            // Hide the container if no logs are found, even if 'showRescueLogs' is true
                            RescueLogsContainer.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProviderHomeActivity.this,
                                "Failed to load rescue logs: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        // Also hide the container on failure
                        RescueLogsContainer.setVisibility(View.GONE);
                    }
                });
    }

    private void loadSymptoms(String childId, String parentId) {
        DatabaseReference logsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child("Parent")
                .child(parentId)
                .child("Children")
                .child(childId)
                .child("Logs")
                .child("symptomLogs");

        logsRef.orderByKey()
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        nightWakingSymptomContainer.setVisibility(View.GONE);
                        wheezingSymptomContainer.setVisibility(View.GONE);

                        if (snapshot.exists() && snapshot.hasChildren()) {
                            DataSnapshot latest = snapshot.getChildren().iterator().next();

                            Boolean nightWakingVal = latest.child("nightWaking").getValue(Boolean.class);
                            String coughWheezeVal = latest.child("coughWheeze").getValue(String.class);

                            if (Boolean.TRUE.equals(nightWakingVal)) {
                                nightWakingSymptomContainer.setVisibility(View.VISIBLE);
                            }
                            if (coughWheezeVal != null) {
                                wheezingSymptomContainer.setVisibility(View.VISIBLE);
                            }

                            // if either symptom exists, show the symptoms container
                            if (Boolean.TRUE.equals(nightWakingVal) || coughWheezeVal != null) {
                                symptomsContainer.setVisibility(View.VISIBLE);
                                loadTriggers(childId, parentId); // Call loadTriggers here
                            } else {
                                symptomsContainer.setVisibility(View.GONE);
                            }
                        } else {
                            symptomsContainer.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProviderHomeActivity.this,
                                "Failed to load symptom data: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadTriggers(String childId, String parentId) {
        DatabaseReference logsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child("Parent")
                .child(parentId)
                .child("Children")
                .child(childId)
                .child("Logs")
                .child("symptomLogs");

        logsRef.orderByKey()
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        // Ensure all triggers start hidden
                        triggerExercise.setVisibility(View.GONE);
                        triggerColdAir.setVisibility(View.GONE);
                        triggerDust.setVisibility(View.GONE);
                        triggerPet.setVisibility(View.GONE);
                        triggerSmoke.setVisibility(View.GONE);
                        triggerIllness.setVisibility(View.GONE);
                        triggerStrongOdor.setVisibility(View.GONE);

                        if (snapshot.exists() && snapshot.hasChildren()) {
                            DataSnapshot latestSymptomLog = snapshot.getChildren().iterator().next();
                            DataSnapshot triggersSnapshot = latestSymptomLog.child("triggers");

                            if (triggersSnapshot.exists() && triggersSnapshot.hasChildren()) {
                                for (DataSnapshot triggerData : triggersSnapshot.getChildren()) {
                                    String triggerName = triggerData.getValue(String.class);
                                    if (triggerName != null) {
                                        setTriggerVisibilityByName(triggerName);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProviderHomeActivity.this,
                                "Failed to load trigger data: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean setTriggerVisibilityByName(String triggerName) {
        if (triggerName == null) return false;

        switch (triggerName.toLowerCase(Locale.ROOT)) {
            case "exercise":
                if (triggerExercise != null) triggerExercise.setVisibility(View.VISIBLE);
                return true;
            case "cold air":
                if (triggerColdAir != null) triggerColdAir.setVisibility(View.VISIBLE);
                return true;
            case "dust":
                if (triggerDust != null) triggerDust.setVisibility(View.VISIBLE);
                return true;
            case "pet":
                if (triggerPet != null) triggerPet.setVisibility(View.VISIBLE);
                return true;
            case "smoke":
                if (triggerSmoke != null) triggerSmoke.setVisibility(View.VISIBLE);
                return true;
            case "illness":
                if (triggerIllness != null) triggerIllness.setVisibility(View.VISIBLE);
                return true;
            case "strong odors":
                if (triggerStrongOdor != null) triggerStrongOdor.setVisibility(View.VISIBLE);
                return true;
            default:
                return false;
        }
    }

    private void loadTriageIncident(String childId, String parentId) {
        DatabaseReference logsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child("Parent")
                .child(parentId)
                .child("Children")
                .child(childId)
                .child("Logs")
                .child("triageLogs");

        // get LATEST log
        logsRef.orderByKey()
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChildren()) {
                            DataSnapshot latest = snapshot.getChildren().iterator().next();

                            Long timestamp = latest.child("timeStampStarted").getValue(Long.class);
                            Boolean isEscalated = latest.child("escalated").getValue(Boolean.class);
                            String pefValue = latest.child("PEF").getValue(String.class);

                            String resultText = "N/A";
                            String fetchedResult = latest.child("result").getValue(String.class);
                            if (fetchedResult != null && !fetchedResult.trim().isEmpty()) {
                                resultText = fetchedResult;
                            }

                            String dateString = "N/A";
                            if (timestamp != null) {
                                Date date = new Date(timestamp);
                                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
                                dateString = df.format(date);
                            }
                            String escalatedText = (isEscalated != null && isEscalated) ? "✓ Escalated" : "✗ Not Escalated";


                            triageDate.setText("🚨 " + dateString);
                            triageEscalated.setText(escalatedText);
                            triagePEF.setText(pefValue != null && !pefValue.isEmpty() ? "PEF: " + pefValue : "PEF: N/A");

                            triageResult.setText("Result: " + resultText);

                            triageContainer.setVisibility(View.VISIBLE);
                        } else {
                            triageContainer.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProviderHomeActivity.this,
                                "Failed to load triage data: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataSharingRef != null && dataSharingListener != null) {
            dataSharingRef.removeEventListener(dataSharingListener);
        }
    }
}
