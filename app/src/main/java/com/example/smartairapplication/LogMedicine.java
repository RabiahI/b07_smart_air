package com.example.smartairapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogMedicine extends AppCompatActivity {
    private String childId, parentId;
    private ImageView btnReturn;
    private Button btnRescue, btnController;
    private String inhalerType = null;

    private TextView btnMinus, btnPlus, txtCount;
    private int puffCount = 0;

    private TextView sobBefore1, sobBefore2, sobBefore3, sobBefore4, sobBefore5;
    private TextView sobAfter1, sobAfter2, sobAfter3, sobAfter4, sobAfter5;
    private int sobBefore = -1;
    private int sobAfter = -1;

    private Button btnPostBetter, btnPostSame, btnPostWorse;
    private String postFeeling = null;

    private Button btnSave;
    private TextView txtTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_medicine);

        childId = getIntent().getStringExtra("childId");
        parentId = getIntent().getStringExtra("parentId");

        //link xml elements
        btnReturn = findViewById(R.id.btnReturn);
        btnRescue = findViewById(R.id.btnRescue);
        btnController = findViewById(R.id.btnController);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        txtCount = findViewById(R.id.txtCount);

        sobBefore1 = findViewById(R.id.sobBefore1);
        sobBefore2 = findViewById(R.id.sobBefore2);
        sobBefore3 = findViewById(R.id.sobBefore3);
        sobBefore4 = findViewById(R.id.sobBefore4);
        sobBefore5 = findViewById(R.id.sobBefore5);

        sobAfter1 = findViewById(R.id.sobAfter1);
        sobAfter2 = findViewById(R.id.sobAfter2);
        sobAfter3 = findViewById(R.id.sobAfter3);
        sobAfter4 = findViewById(R.id.sobAfter4);
        sobAfter5 = findViewById(R.id.sobAfter5);

        btnPostBetter = findViewById(R.id.btnPostBetter);
        btnPostSame = findViewById(R.id.btnPostSame);
        btnPostWorse = findViewById(R.id.btnPostWorse);

        txtTime = findViewById(R.id.txtTime);
        btnSave = findViewById(R.id.btnSave);

        setupSobSelector(Arrays.asList(sobBefore1, sobBefore2, sobBefore3, sobBefore4, sobBefore5), true);
        setupSobSelector(Arrays.asList(sobAfter1, sobAfter2, sobAfter3, sobAfter4, sobAfter5), false);

        btnReturn.setOnClickListener(v -> showExitConfirmation());

        btnRescue.setOnClickListener(v->{
            inhalerType = "Rescue";
            btnRescue.setAlpha(1f);
            btnController.setAlpha(0.5f);
            updateSaveButton();
        });

        btnController.setOnClickListener(v->{
            inhalerType = "Controller";
            btnController.setAlpha(1f);
            btnRescue.setAlpha(0.5f);
            updateSaveButton();
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

        String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
        txtTime.setText("Taken at " + time);

        btnSave.setOnClickListener(v -> saveLogToDatabase());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmation();
            }
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
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " +e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}