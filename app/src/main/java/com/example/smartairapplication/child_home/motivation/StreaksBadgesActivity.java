package com.example.smartairapplication.child_home.motivation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartairapplication.R;
import com.example.smartairapplication.child_home.ChildHomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Objects;

public class StreaksBadgesActivity extends AppCompatActivity {

    FirebaseUser currentUser;
    private DatabaseReference dataRef;
    private Button buttonHomepage;
    private TextView controllerStreak, techniqueStreak, thresholdDesc;
    private LinearLayout controllerLayout, techniqueLayout, thresholdLayout;
    private String childId, parentId;
    private ArrayList<Long> controllerTimes;
    private ArrayList<Long> techniqueTimes;
    private int bestStreakController;
    private int rescueThreshold;
    private boolean isParentMode;
    private boolean thresholdLoaded = false;
    private boolean logsLoaded = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_streaks_badges);

        buttonHomepage = findViewById(R.id.btn_back_to_homepage);
        controllerStreak = findViewById(R.id.controllerStreak);
        techniqueStreak = findViewById(R.id.techniqueStreak);
        controllerLayout = findViewById(R.id.controller_layout);
        techniqueLayout = findViewById(R.id.technique_layout);
        thresholdLayout = findViewById(R.id.threshold_layout);
        thresholdDesc = findViewById(R.id.desc_threshold);
        controllerTimes = new ArrayList<>();
        techniqueTimes = new ArrayList<>();
        bestStreakController = 0;

        controllerLayout.setVisibility(View.GONE);
        techniqueLayout.setVisibility(View.GONE);
        thresholdLayout.setVisibility(View.GONE);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        childId = getIntent().getStringExtra("childId");
        parentId = getIntent().getStringExtra("parentId");
        isParentMode = getIntent().getBooleanExtra("isParentMode", false);

        if (parentId == null) {
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                parentId = currentUser.getUid();
            }
        }

        // check if child exists
        if (childId == null) {
            Toast.makeText(StreaksBadgesActivity.this, "Could not find data.", Toast.LENGTH_SHORT).show();
            return;
        }

        dataRef = FirebaseDatabase.getInstance().getReference("Users").child("Parent").child(parentId).child("Children").child(childId);

        buttonHomepage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StreaksBadgesActivity.this, ChildHomeActivity.class);
                intent.putExtra("childId", childId);
                intent.putExtra("parentId", parentId);
                intent.putExtra("isParentMode", isParentMode);
                startActivity(intent);
            }
        });

        DatabaseReference thresholdRef = dataRef.child("threshold");
        thresholdRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(StreaksBadgesActivity.this, "No threshold data found.", Toast.LENGTH_SHORT).show();
                    rescueThreshold = 4;
                    return;
                }
                else {
                    Long temp = snapshot.getValue(Long.class);
                    if (temp != null) {
                        rescueThreshold = temp.intValue();
                    }
                    else {
                        rescueThreshold = 4;
                    }
                }
                thresholdLoaded = true;
                testDisplayBadges();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StreaksBadgesActivity.this, "Failed to load streaks and badges data.", Toast.LENGTH_SHORT).show();
            }
        });

        displayStreaks();
    }

    private void testDisplayBadges() {
        if (thresholdLoaded && logsLoaded) {
            displayBadges();
        }
    }

    private void displayStreaks() {

        DatabaseReference logsRef = dataRef.child("Logs").child("medicineLogs");
        logsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                controllerTimes.clear();
                techniqueTimes.clear();

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
                            techniqueTimes.add(timestamp);
                        }
                    }

                    displayController();
                    displayTechnique();
                    logsLoaded = true;
                    testDisplayBadges();
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
            String streakZero = "Controller Streak: 0 days";
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

        streakDisplay = "Controller Streak: " + String.valueOf(streak) + " days";
        controllerStreak.setText(streakDisplay);
    }

    private void displayTechnique() {

        ArrayList<LocalDate> dates = new ArrayList<>();
        ArrayList<LocalDate> uniqueDates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int streak = 0;
        String streakDisplay;

        if (techniqueTimes.isEmpty()) {
            String streakZero = "Technique Streak: 0 days";
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

        streakDisplay = "Technique Streak: " + String.valueOf(streak) + " days";
        techniqueStreak.setText(streakDisplay);
    }

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
        if (rescuesThisMonth() <= rescueThreshold) {
            int count = rescuesThisMonth();
            String msg = "Low rescue month! Only " + count + " rescues this month";
            thresholdLayout.setVisibility(View.VISIBLE);
            thresholdDesc.setText(msg);
        }
    }

    private int rescuesThisMonth() {
        ArrayList<LocalDate> dates = new ArrayList<>();
        ArrayList<Month> Months = new ArrayList<>();
        LocalDate today = LocalDate.now();
        Month currentMonth = today.getMonth();
        int count = 0;

        if (techniqueTimes.isEmpty()) {
            return count;
        }

        for (Long time: techniqueTimes) {
            Month temp = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDate().getMonth();
            if (temp == currentMonth) {
                count++;
            }
        }

        return count;
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
