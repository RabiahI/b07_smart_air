package com.example.smartairapplication.child_home.daily_check_in;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartairapplication.R;
import com.example.smartairapplication.models.SymptomLog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DailyCheckIn extends AppCompatActivity {

    private String childId, parentId;
    private RadioGroup nightWakingGroup, activityGroup, coughGroup;
    private CheckBox trigExercise, trigColdAir, trigDust, trigSmoke, trigIllness, trigOdors;
    private EditText inputNotes;
    private Button btnSubmit;
    private ImageView btnBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_daily_check_in);

        childId = getIntent().getStringExtra("childId");
        parentId = getIntent().getStringExtra("parentId");
        //link xml components
        btnBack = findViewById(R.id.btnBack);
        nightWakingGroup = findViewById(R.id.nightWakingGroup);
        activityGroup = findViewById(R.id.activityGroup);
        coughGroup = findViewById(R.id.coughGroup);

        trigExercise = findViewById(R.id.trigExercise);
        trigColdAir = findViewById(R.id.trigColdAir);
        trigDust = findViewById(R.id.trigDust);
        trigSmoke = findViewById(R.id.trigSmoke);
        trigIllness = findViewById(R.id.trigIllness);
        trigOdors = findViewById(R.id.trigOdors);

        inputNotes = findViewById(R.id.inputNotes);
        btnSubmit = findViewById(R.id.btnSubmitCheckIn);

        btnBack.setOnClickListener(v -> showExitConfirmation());
        btnSubmit.setOnClickListener(v -> {
            int nightId = nightWakingGroup.getCheckedRadioButtonId();
            int activityId = activityGroup.getCheckedRadioButtonId();
            int coughId = coughGroup.getCheckedRadioButtonId();

            if (nightId == -1 || activityId == -1 || coughId == -1) {
                Toast.makeText(this, "Please answer all symptom questions", Toast.LENGTH_SHORT).show();
                return;
            }

            saveSymptomLog();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmation();
            }
        });

    }

    private void saveSymptomLog() {
        //symptoms
        boolean nightWaking = nightWakingGroup.getCheckedRadioButtonId() == R.id.nightYes;
        boolean activityLimits = activityGroup.getCheckedRadioButtonId() == R.id.actYes;
        String coughLevel;
        int coughId = coughGroup.getCheckedRadioButtonId();
        if (coughId == R.id.coughNone){
            coughLevel = "none";
        } else if (coughId == R.id.coughMild){
            coughLevel = "mild";
        } else if (coughId == R.id.coughBad){
            coughLevel = "bad";
        } else {
            coughLevel = null;
        }

        //triggers
        ArrayList<String> triggerList = new ArrayList<>();
        if (trigExercise.isChecked()) triggerList.add("exercise");
        if (trigColdAir.isChecked()) triggerList.add("cold air");
        if (trigDust.isChecked()) triggerList.add("dust/pets");
        if (trigSmoke.isChecked()) triggerList.add("smoke");
        if (trigIllness.isChecked()) triggerList.add("illness");
        if (trigOdors.isChecked()) triggerList.add("strong odors");

        String notes = inputNotes.getText().toString().trim();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        SymptomLog log = new SymptomLog(timestamp, nightWaking, activityLimits, coughLevel,
                                        triggerList, notes);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                .child("Parent").child(parentId)
                .child("Children").child(childId)
                .child("Logs").child("symptomLogs");

        ref.push().setValue(log)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Daily check-in saved!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
                })
                .show();
    }
}