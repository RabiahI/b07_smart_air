package com.example.smartairapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class LogMedicine extends AppCompatActivity {
    private String childId, parentId;
    private boolean isParentMode;

    private boolean techniqueCompleted = false;
    private boolean highQualityTechnique = false;

    private ImageView btnReturn;
    private CardView btnRescue, btnController;
    private String inhalerType = null;

    private TextView btnMinus, btnPlus, txtCount;
    private int puffCount = 0;

    private TextView sobBefore1, sobBefore2, sobBefore3, sobBefore4, sobBefore5;
    private TextView sobAfter1, sobAfter2, sobAfter3, sobAfter4, sobAfter5;
    private int sobBefore = -1;
    private int sobAfter = -1;

    private Button btnPostBetter, btnPostSame, btnPostWorse;
    private String postFeeling = null;

    private Button btnStartTechnique;
    private Button btnSave;
    private ActivityResultLauncher<Intent> techniqueLauncher;
    private LinearLayout sobBeforeRatingCard, sobAfterRatingCard;
    private LinearLayout puffCard, afterButtons, medicineTypeLayout;
    private TextView txtBeforeSOB, txtAfterSOB, numOfPuffsText, txtPostCheck;
    private ScrollView questions;
    private BottomNavigationView bottomNav;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_medicine);

        childId = getIntent().getStringExtra("childId");
        parentId = getIntent().getStringExtra("parentId");
        isParentMode = getIntent().getBooleanExtra("isParentMode", false);

        //link xml elements
        btnReturn = findViewById(R.id.btnReturn);
        bottomNav = findViewById(R.id.bottomNav);

        medicineTypeLayout = findViewById(R.id.medicineTypeLayout);
        btnRescue = findViewById(R.id.btnRescue);
        btnController = findViewById(R.id.btnController);

        btnStartTechnique = findViewById(R.id.btnStartTechnique);

        questions = findViewById(R.id.sectionQuestions);
        numOfPuffsText = findViewById(R.id.numOfPuffsText);
        puffCard = findViewById(R.id.puffCard);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        txtCount = findViewById(R.id.txtCount);

        txtBeforeSOB = findViewById(R.id.txtBeforeSOB);
        sobBeforeRatingCard = findViewById(R.id.sobBeforeRatingCard);
        sobBefore1 = findViewById(R.id.sobBefore1);
        sobBefore2 = findViewById(R.id.sobBefore2);
        sobBefore3 = findViewById(R.id.sobBefore3);
        sobBefore4 = findViewById(R.id.sobBefore4);
        sobBefore5 = findViewById(R.id.sobBefore5);

        txtAfterSOB = findViewById(R.id.txtAfterSOB);
        sobAfterRatingCard = findViewById(R.id.sobAfterRatingCard);
        sobAfter1 = findViewById(R.id.sobAfter1);
        sobAfter2 = findViewById(R.id.sobAfter2);
        sobAfter3 = findViewById(R.id.sobAfter3);
        sobAfter4 = findViewById(R.id.sobAfter4);
        sobAfter5 = findViewById(R.id.sobAfter5);

        txtPostCheck = findViewById(R.id.txtPostCheck);
        afterButtons = findViewById(R.id.afterButtons);
        btnPostBetter = findViewById(R.id.btnPostBetter);
        btnPostSame = findViewById(R.id.btnPostSame);
        btnPostWorse = findViewById(R.id.btnPostWorse);

        btnSave = findViewById(R.id.btnSave);

        //hide everything except rescue/controller at start
        hideAllQuestions();
        medicineTypeLayout.setVisibility(View.VISIBLE);

        setupSobSelector(Arrays.asList(sobBefore1, sobBefore2, sobBefore3, sobBefore4, sobBefore5), true);
        setupSobSelector(Arrays.asList(sobAfter1, sobAfter2, sobAfter3, sobAfter4, sobAfter5), false);

        techniqueLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        boolean techniqueCompleted = result.getData().getBooleanExtra("techniqueCompleted", false);

                        boolean highQualityTechnique = result.getData().getBooleanExtra("highQualityTechnique", false);

                        this.techniqueCompleted = techniqueCompleted;
                        this.highQualityTechnique = highQualityTechnique;

                        if (techniqueCompleted) {
                            showControllerAfterTechnique();
                            Toast.makeText(this, "Technique Completed!", Toast.LENGTH_SHORT).show();
                        }
                        updateSaveButton();
                    }
                }
        );

        btnReturn.setOnClickListener(v -> showExitConfirmation());

        btnRescue.setOnClickListener(v->{
            inhalerType = "Rescue";
            showRescueQuestions();
            updateSaveButton();
        });

        btnController.setOnClickListener(v->{
            inhalerType = "Controller";
            showControllerInitial();
            updateSaveButton();
        });

        btnStartTechnique.setOnClickListener(v -> {
            if (puffCount == 0 || sobBefore == -1){
                Toast.makeText(this, "Please fill puff count and breathing before starting", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent i = new Intent(LogMedicine.this, TechniqueIntro.class);
            i.putExtra("childId", childId);
            techniqueLauncher.launch(i);
        });

        btnPlus.setOnClickListener(v -> {
            puffCount++;
            txtCount.setText(String.valueOf(puffCount));
            updateSaveButton();
        });

        btnMinus.setOnClickListener(v -> {
            if (puffCount > 0){
                puffCount--;
            }
            txtCount.setText(String.valueOf(puffCount));
            updateSaveButton();
        });

        btnPostBetter.setOnClickListener(v->{
            postFeeling = "Better";
            highlightPostButton(btnPostBetter);
            updateSaveButton();
        });

        btnPostSame.setOnClickListener(v->{
            postFeeling = "Same";
            highlightPostButton(btnPostSame);
            updateSaveButton();
        });

        btnPostWorse.setOnClickListener(v->{
            postFeeling = "Worse";
            highlightPostButton(btnPostWorse);
            updateSaveButton();
        });

        btnSave.setOnClickListener(v -> saveLogToDatabase());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmation();
            }
        });
        bottomNav.setSelectedItemId(R.id.nav_log);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                showExitConfirmation();
                return false;
            } else if (itemId == R.id.nav_settings) {
                new AlertDialog.Builder(this)
                        .setTitle("Go to Settings?")
                        .setMessage("If you leave now, any unsaved changes will be lost.")
                        .setPositiveButton("Leave", (dialog, which) -> {
                            Intent settingsIntent = new Intent(LogMedicine.this, ChildSettingsActivity.class);
                            settingsIntent.putExtra("childId", childId);
                            settingsIntent.putExtra("parentId", parentId);
                            settingsIntent.putExtra("isParentMode", isParentMode);
                            startActivity(settingsIntent);
                            finish();
                        })
                        .setNegativeButton("Stay", (dialog, which) -> {
                            dialog.dismiss();
                            bottomNav.setSelectedItemId(R.id.nav_log);
                        })
                        .show();
                return false;
            } else if (itemId == R.id.nav_history){
                Intent intent = new Intent(LogMedicine.this, ChildHistory.class);
                intent.putExtra("childId", childId);
                intent.putExtra("parentId", parentId);
                intent.putExtra("isParentMode", isParentMode);
                startActivity(intent);
                finish();
            } else if (itemId == R.id.nav_log){
                return true;
            }
            return false;
        });

    }

    private void showControllerAfterTechnique() {
        showBasicBeforeDose();
        txtAfterSOB.setVisibility(View.VISIBLE);
        sobAfterRatingCard.setVisibility(View.VISIBLE);
        txtPostCheck.setVisibility(View.VISIBLE);
        afterButtons.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.VISIBLE);
    }

    private void showControllerInitial() {
        medicineTypeLayout.setVisibility(View.GONE);
        questions.setVisibility(View.VISIBLE);
        hideAllQuestions();
        showBasicBeforeDose();
        btnStartTechnique.setVisibility(View.VISIBLE);
    }

    private void showBasicBeforeDose() {
        txtBeforeSOB.setVisibility(View.VISIBLE);
        sobBeforeRatingCard.setVisibility(View.VISIBLE);
        numOfPuffsText.setVisibility(View.VISIBLE);
        puffCard.setVisibility(View.VISIBLE);
        btnStartTechnique.setVisibility(View.GONE);
    }

    private void showRescueQuestions() {
        questions.setVisibility(View.VISIBLE);
        hideAllQuestions();
        showBasicBeforeDose();
        txtAfterSOB.setVisibility(View.VISIBLE);
        sobAfterRatingCard.setVisibility(View.VISIBLE);
        txtPostCheck.setVisibility(View.VISIBLE);
        afterButtons.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.VISIBLE);

    }

    private void hideAllQuestions(){
        medicineTypeLayout.setVisibility(View.GONE);

        txtBeforeSOB.setVisibility(View.GONE);
        sobBeforeRatingCard.setVisibility(View.GONE);

        numOfPuffsText.setVisibility(View.GONE);
        puffCard.setVisibility(View.GONE);

        btnStartTechnique.setVisibility(View.GONE);

        txtAfterSOB.setVisibility(View.GONE);
        sobAfterRatingCard.setVisibility(View.GONE);

        txtPostCheck.setVisibility(View.GONE);
        afterButtons.setVisibility(View.GONE);

        btnSave.setVisibility(View.GONE);
    }

    private void showExitConfirmation(){
        new AlertDialog.Builder(this)
                .setTitle("Return to home?")
                .setMessage("If you leave now, any unsaved changes will be lost.")
                .setPositiveButton("Leave", (dialog, which) -> {
                    finish();
                })
                .setNegativeButton("Stay", (dialog, which) -> {
                    dialog.dismiss();
                    bottomNav.setSelectedItemId(R.id.nav_log);
                })
                .show();
    }
    private void setupSobSelector(List<TextView> circles, boolean isBefore){
        for (int i=0; i < circles.size(); i++){
            int rating = i+1;

            circles.get(i).setOnClickListener(v->{
                for (TextView c : circles) {
                    c.setSelected(false);
                }
                v.setSelected(true);
                if (isBefore){
                    sobBefore = rating;
                }
                else{
                    sobAfter = rating;
                }
                updateSaveButton();
            });
        }
    }

    private void highlightPostButton(Button selected){
        btnPostBetter.setAlpha(0.5f);
        btnPostSame.setAlpha(0.5f);
        btnPostWorse.setAlpha(0.5f);
        selected.setAlpha(1f);
    }

    private void updateSaveButton(){
        boolean ready = inhalerType != null
                && sobBefore != -1
                && sobAfter != -1
                && postFeeling != null;

        btnSave.setEnabled(ready);

        if (ready){
            btnSave.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.vivid_blue)
            );
            btnSave.setTextColor(Color.WHITE);
        } else {
            btnSave.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.light_grey)
            );
            btnSave.setTextColor(Color.parseColor("#7E7474"));
        }
    }

    private void saveLogToDatabase(){
        MedicineLog log = new MedicineLog(inhalerType, puffCount, sobBefore, sobAfter, postFeeling);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                .child("Parent")
                .child(parentId)
                .child("Children")
                .child(childId)
                .child("Logs")
                .child("medicineLogs");
        String logId = ref.push().getKey();

        ref.child(logId).setValue(log)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Medicine log saved!", Toast.LENGTH_SHORT).show();
                    OverviewCalculator.updateDailyOverview(parentId, childId);
                    if ("Rescue".equals(inhalerType)) {
                        OverviewCalculator.updateDailyOverview(parentId, childId);
                        checkForRapidRescueRepeats();
                    }
                    if ("Worse".equals(postFeeling)) {
                        DatabaseReference childRef = FirebaseDatabase.getInstance().getReference("Users")
                                .child("Parent").child(parentId)
                                .child("Children").child(childId);

                        childRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                String childName = snapshot.exists() ? snapshot.getValue(String.class) : "Your child";
                                String message = childName + " is feeling worse after their dose.";
                                Alert newAlert = new Alert("Worse After Dose", message, System.currentTimeMillis(), "high", childId);

                                DatabaseReference parentAlertRef = FirebaseDatabase.getInstance().getReference("Users")
                                        .child("Parent")
                                        .child(parentId)
                                        .child("Alerts");
                                parentAlertRef.push().setValue(newAlert);
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                Toast.makeText(LogMedicine.this, "Failed to retrieve child's name for alert.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " +e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void checkForRapidRescueRepeats() {
        DatabaseReference logsRef = FirebaseDatabase.getInstance().getReference("Users")
                .child("Parent")
                .child(parentId)
                .child("Children")
                .child(childId)
                .child("Logs")
                .child("medicineLogs");

        long threeHoursAgo = System.currentTimeMillis() - (3 * 60 * 60 * 1000);

        logsRef.orderByChild("timestamp").startAt(threeHoursAgo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int rescueCount = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MedicineLog log = snapshot.getValue(MedicineLog.class);
                    if (log != null && "Rescue".equals(log.getInhalerType())) {
                        rescueCount++;
                    }
                }

                if (rescueCount >= 3) {
                    DatabaseReference parentAlertRef = FirebaseDatabase.getInstance().getReference("Users")
                            .child("Parent")
                            .child(parentId)
                            .child("Alerts");
                    
                    String message = "Your child has used their rescue inhaler " + rescueCount + " times in the last 3 hours.";
                    Alert newAlert = new Alert("Rapid Rescue Repeats", message, System.currentTimeMillis(), "high", childId);
                    parentAlertRef.push().setValue(newAlert);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LogMedicine.this, "Failed to check for rapid rescue repeats: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}