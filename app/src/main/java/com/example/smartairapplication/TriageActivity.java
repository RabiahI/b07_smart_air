package com.example.smartairapplication;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TriageActivity extends AppCompatActivity {

    private LinearLayout questionLayout, resultLayout, inputsLayout;
    private TextView questionText, severityLabel, actionLabel, timerText;
    private Button yesButton, noButton, startOverButton, submitInputsButton;
    private EditText editTextPef, editTextRescueAttempts;

    private List<String> questions;
    private int currentQuestionIndex = 0;
    private List<Boolean> answers = new ArrayList<>();
    private String childId;

    private CountDownTimer countDownTimer;
    private long timeStampStarted;
    private String logEntryId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triage);

        timeStampStarted = System.currentTimeMillis();
        childId = getIntent().getStringExtra("childId");

        questionLayout = findViewById(R.id.question_layout);
        resultLayout = findViewById(R.id.result_layout);
        inputsLayout = findViewById(R.id.inputs_layout);
        questionText = findViewById(R.id.question_text);
        severityLabel = findViewById(R.id.severity_label);
        actionLabel = findViewById(R.id.action_label);
        timerText = findViewById(R.id.timer_text);
        yesButton = findViewById(R.id.yes_button);
        noButton = findViewById(R.id.no_button);
        startOverButton = findViewById(R.id.start_over_button);
        editTextPef = findViewById(R.id.edit_text_pef);
        editTextRescueAttempts = findViewById(R.id.edit_text_rescue_attempts);
        submitInputsButton = findViewById(R.id.submit_inputs_button);


        questions = new ArrayList<>();
        questions.add("Is the person unable to speak in full sentences?");
        questions.add("Are their lips blue or grey?");
        questions.add("Is their chest pulling in with each breath?");
        questions.add("Are they wheezing but can still speak?");

        displayQuestion();

        yesButton.setOnClickListener(v -> handleAnswer(true));
        noButton.setOnClickListener(v -> handleAnswer(false));
        submitInputsButton.setOnClickListener(v -> {
            inputsLayout.setVisibility(View.GONE);
            evaluateTriage();
        });
        startOverButton.setOnClickListener(v -> {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            currentQuestionIndex = 0;
            answers.clear();
            resultLayout.setVisibility(View.GONE);
            questionLayout.setVisibility(View.VISIBLE);
            timerText.setVisibility(View.GONE);
            displayQuestion();
            timeStampStarted = System.currentTimeMillis();
            logEntryId = null;
        });
    }

    private void displayQuestion() {
        if (currentQuestionIndex < questions.size()) {
            questionText.setText(questions.get(currentQuestionIndex));
        } else {
            questionLayout.setVisibility(View.GONE);
            inputsLayout.setVisibility(View.VISIBLE);
        }
    }

    private void handleAnswer(boolean answer) {
        answers.add(answer);
        currentQuestionIndex++;
        displayQuestion();
    }

    private void evaluateTriage() {
        boolean severe = answers.get(0) || answers.get(1) || answers.get(2);
        String result;
        if (severe) {
            severityLabel.setText("Severity: Severe");
            actionLabel.setText("Action: Call 911 or go to the nearest hospital immediately.");
            result = "Severe";
            logTriageData(result, true, false);
            sendParentAlert(result);
        } else {
            severityLabel.setText("Severity: Mild/Moderate");
            actionLabel.setText("Action: Start home steps and monitor.");
            result = "Mild/Moderate";
            startTimer();
            logTriageData(result, false, false);
            sendParentAlert(result);
        }

        resultLayout.setVisibility(View.VISIBLE);
    }

    private void logTriageData(String result, boolean escalated, boolean parentAlertSent) {
        if (childId == null) {
            Toast.makeText(TriageActivity.this, "Cannot save log, child ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference parentsRef = FirebaseDatabase.getInstance().getReference("Users/Parent");
        parentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String parentId = null;
                for (DataSnapshot parentSnapshot : dataSnapshot.getChildren()) {
                    if (parentSnapshot.child("Children").hasChild(childId)) {
                        parentId = parentSnapshot.getKey();
                        break;
                    }
                }

                if (parentId == null) {
                    Toast.makeText(TriageActivity.this, "Could not find parent to save log.", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference triageLogRef;
                if (logEntryId == null) {
                    triageLogRef = FirebaseDatabase.getInstance().getReference("Users")
                            .child("Parent").child(parentId)
                            .child("Children").child(childId)
                            .child("Logs").child("triageLogs").push();
                    logEntryId = triageLogRef.getKey();
                } else {
                    triageLogRef = FirebaseDatabase.getInstance().getReference("Users")
                            .child("Parent").child(parentId)
                            .child("Children").child(childId)
                            .child("Logs").child("triageLogs").child(logEntryId);
                }

                String pefString = editTextPef.getText().toString();
                String rescueAttempts = editTextRescueAttempts.getText().toString();

                TriageLog.RedFlags redFlags = new TriageLog.RedFlags(answers.get(0), answers.get(1), answers.get(2));
                TriageLog logData = new TriageLog(rescueAttempts, pefString, result, "", timeStampStarted,
                        escalated, parentAlertSent, redFlags);

                triageLogRef.setValue(logData);

                if (!pefString.isEmpty()) {
                    int pefValue = Integer.parseInt(pefString);
                    logPefValue(pefValue, parentId, childId);
                    DatabaseReference childRef = FirebaseDatabase.getInstance().getReference("Users")
                            .child("Parent").child(parentId)
                            .child("Children").child(childId);
                    childRef.child("latestPef").setValue(pefValue);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(TriageActivity.this, "Database error while saving log: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logPefValue(int pefValue, String parentId, String childId) {
        DatabaseReference pefLogRef = FirebaseDatabase.getInstance().getReference("Users")
                .child("Parent").child(parentId)
                .child("Children").child(childId)
                .child("Logs").child("pefLogs").push();

        PefLog logEntry = new PefLog(pefValue, System.currentTimeMillis());
        pefLogRef.setValue(logEntry);
    }

    private void startTimer() {
        timerText.setVisibility(View.VISIBLE);
        countDownTimer = new CountDownTimer(10000, 1000) { // 10 minutes = 600000, 10 seconds = 10000
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                timerText.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                timerText.setText("00:00");
                Toast.makeText(TriageActivity.this, "Symptoms have not improved. Alerting parent.", Toast.LENGTH_SHORT).show();
                sendParentAlert("Escalation");
            }
        }.start();
    }



    private void sendParentAlert(final String triageResult) {
        if (childId == null) {
            Toast.makeText(TriageActivity.this, "Could not find parent to alert.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference parentsRef = FirebaseDatabase.getInstance().getReference("Users/Parent");
        parentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String parentId = null;
                DataSnapshot targetParentSnapshot = null;
                for (DataSnapshot parentSnapshot : dataSnapshot.getChildren()) {
                    if (parentSnapshot.child("Children").hasChild(childId)) {
                        parentId = parentSnapshot.getKey();
                        targetParentSnapshot = parentSnapshot;
                        break;
                    }
                }

                if (parentId == null) {
                    Toast.makeText(TriageActivity.this, "Could not find parent to alert.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String childName = targetParentSnapshot.child("Children").child(childId).child("name").getValue(String.class);
                childName = (childName != null && !childName.isEmpty()) ? childName : "Your child";

                String alertMessage;
                switch (triageResult) {
                    case "Severe":
                        alertMessage = childName + " is experiencing a severe asthma event.";
                        break;
                    case "Mild/Moderate":
                        alertMessage = childName + " has started a triage for a mild/moderate asthma event.";
                        break;
                    case "Escalation":
                        alertMessage = childName + "'s symptoms have not improved after 10 minutes.";
                        break;
                    default:
                        alertMessage = childName + " requires assistance.";
                        break;
                }

                DatabaseReference parentAlertRef = FirebaseDatabase.getInstance()
                        .getReference("Users")
                        .child("Parent")
                        .child(parentId)
                        .child("Alerts");
                parentAlertRef.push().setValue(alertMessage);
                Toast.makeText(TriageActivity.this, "Parent has been alerted.", Toast.LENGTH_SHORT).show();

                if ("Escalation".equals(triageResult) && logEntryId != null) {
                    DatabaseReference logRef = FirebaseDatabase.getInstance().getReference("Users")
                            .child("Parent").child(parentId)
                            .child("Children").child(childId)
                            .child("Logs").child("triageLogs").child(logEntryId);
                    logRef.child("escalated").setValue(true);
                    logRef.child("parentAlertSent").setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(TriageActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
