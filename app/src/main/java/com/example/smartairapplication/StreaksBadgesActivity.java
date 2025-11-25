package com.example.smartairapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Objects;

public class StreaksBadgesActivity extends AppCompatActivity {

    FirebaseUser currentUser;
    private DatabaseReference dataRef;
    private Button buttonHomepage;
    private TextView controllerStreak, techniqueStreak, controllerDesc, techniqueDesc, thresholdDesc;
    private LinearLayout controllerLayout, techniqueLayout, thresholdLayout;
    private ImageView controllerBadge, techniqueBadge, thresholdBadge;
    private String childId, parentId;
    private ArrayList<Long> controllerTimes;
    private ArrayList<Long> techniqueTimes;
    private int bestStreakController, totalTechniqueCount;
    private int rescueThreshold;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_streaks_badges);

        buttonHomepage = findViewById(R.id.btn_back_to_homepage);
        controllerStreak = findViewById(R.id.controllerStreak);
        techniqueStreak = findViewById(R.id.techniqueStreak);
        controllerLayout = findViewById(R.id.controller_layout);
        controllerBadge = findViewById(R.id.badge_controller);
        controllerDesc  = findViewById(R.id.desc_controller);
        techniqueLayout = findViewById(R.id.technique_layout);
        techniqueBadge = findViewById(R.id.badge_technique);
        techniqueDesc = findViewById(R.id.desc_technique);
        thresholdLayout = findViewById(R.id.threshold_layout);
        thresholdBadge = findViewById(R.id.badge_threshold);
        thresholdDesc = findViewById(R.id.desc_threshold);
        controllerTimes = new ArrayList<>();
        techniqueTimes = new ArrayList<>();
        bestStreakController = 0;
        totalTechniqueCount = 0;
        rescueThreshold = 4;

        controllerLayout.setVisibility(View.GONE);
        techniqueLayout.setVisibility(View.GONE);
        thresholdLayout.setVisibility(View.GONE);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        childId = getIntent().getStringExtra("childId");

        if (currentUser != null) {
            parentId = currentUser.getUid();
        }

        // check if child exists
        if (childId == null) {
            Toast.makeText(StreaksBadgesActivity.this, "Could not find data.", Toast.LENGTH_SHORT).show();
            return;
        }

        // check if parent is logged in
        if (parentId != null) {
            dataRef = FirebaseDatabase.getInstance().getReference("Users").child("Parent").child(parentId).child("Children").child(childId);
        }
        else {
            dataRef = FirebaseDatabase.getInstance().getReference("Users").child("Child").child(childId);
        }

        buttonHomepage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StreaksBadgesActivity.this, ChildHomeActivity.class);
                startActivity(intent);
            }
        });

        displayStreaks();
        displayBadges();
    }

    private void displayStreaks() {

        DatabaseReference logsRef = dataRef.child("Logs").child("medicineLogs");
        logsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    String controllerMsg = "Controller Streak: No log information found";
                    String techniqueMsg = "Technique Streak: No log information found";
                    controllerStreak.setText(controllerMsg);
                    techniqueStreak.setText(techniqueMsg);
                    return;
                }
                else {
                    for (DataSnapshot childSnapShot: snapshot.getChildren()) {
                        String inhalerType = childSnapShot.child("inhalerType").getValue(String.class);
                        Long timestamp = childSnapShot.child("timestamp").getValue(Long.class);

                        if (Objects.equals(inhalerType, "Controller")) {
                            controllerTimes.add(timestamp);
                        }
                        else {
                            techniqueTimes.add(timestamp);
                        }

                    }

                    displayController();
                    displayTechnique();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StreaksBadgesActivity.this, "Failed to load streaks and badges data.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void displayController() {

        ArrayList<LocalDate> dates = new ArrayList<>();
        ArrayList<LocalDate> uniqueDates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int streak = 0;
        String streakDisplay;

        if (controllerTimes.isEmpty()) {
            String streakZero = "Controller Streak: 0";
            controllerStreak.setText(streakZero);
            return;
        }
        controllerTimes.sort(null);

        for (Long time: controllerTimes) {
            dates.add(Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDate());
        }

        for (LocalDate d: dates) {
            if (!uniqueDates.contains(d)) {
                uniqueDates.add(d);
            }
        }
        while (uniqueDates.contains(today)) {
            today = today.minusDays(1);
            streak++;
        }
        if (streak > bestStreakController) {
            bestStreakController = streak;
        }

        streakDisplay = "Controller Streak: " + String.valueOf(streak);
        controllerStreak.setText(streakDisplay);
    }

    private void displayTechnique() {

        ArrayList<LocalDate> dates = new ArrayList<>();
        ArrayList<LocalDate> uniqueDates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int streak = 0;
        String streakDisplay;

        if (techniqueTimes.isEmpty()) {
            String streakZero = "Technique Streak: 0";
            techniqueStreak.setText(streakZero);
            return;
        }
        techniqueTimes.sort(null);

        for (Long time: techniqueTimes) {
            dates.add(Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDate());
        }

        for (LocalDate d: dates) {
            if (!uniqueDates.contains(d)) {
                uniqueDates.add(d);
            }
        }
        while (uniqueDates.contains(today)) {
            today = today.minusDays(1);
            streak++;
        }

        streakDisplay = "Technique Streak: " + String.valueOf(streak);
        techniqueStreak.setText(streakDisplay);
    }

    //setVisibility.(View...)
    private void displayBadges() {


        // First perfect controller week
        if (bestStreakController >= 7) {
            controllerLayout.setVisibility(View.VISIBLE);
        }

        // 10 high quality technique sessions
        if (countTechnique() >= 10) {
            techniqueLayout.setVisibility(View.VISIBLE);
        }

        // low rescue month
        // TO DO

    }

    private int countTechnique() {

        int total = 0;

        if (techniqueTimes.isEmpty()) return 0;
        else {
            for (Long time : techniqueTimes) {
                total++;
            }
        }

        return total;
    }

}
