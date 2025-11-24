package com.example.smartairapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TechniqueHelper extends AppCompatActivity {

    private ImageView btnBackSteps;
    private TextView txtStepCounter;
    private ImageView imgStep;
    private TextView txtStepTitle;
    private TextView txtStepDescription;
    private Button btnNextStep;
    private TechniqueStep[] steps;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_technique_steps);

        btnBackSteps = findViewById(R.id.btnBackSteps);
        txtStepCounter = findViewById(R.id.txtStepCounter);
        imgStep = findViewById(R.id.imgStep);
        txtStepTitle = findViewById(R.id.txtStepTitle);
        txtStepDescription = findViewById(R.id.txtStepDescription);
        btnNextStep = findViewById(R.id.btnNextStep);

        steps = TechniqueStep.createDefaultSteps();

        //initial UI
        updateStepUI();

        //back with confirmation
        btnBackSteps.setOnClickListener(v -> showExitConfirmation());

        //next / finish button
        btnNextStep.setOnClickListener(v -> {
            if (currentIndex < steps.length - 1){
                currentIndex++;
                updateStepUI();
            } else {
                //last step finished (aka finish button)
                Intent result = new Intent();
                result.putExtra("techniqueCompleted", true);
                result.putExtra("highQualityTechnique", true);
                setResult(RESULT_OK, result);
                finish();
            }
        });

    }

    private void updateStepUI() {
        TechniqueStep step = steps[currentIndex];

        int stepNumber = currentIndex + 1;
        int totalSteps = steps.length;

        txtStepCounter.setText("Step " + stepNumber + " of " + totalSteps);
        imgStep.setImageResource(step.getImageResId());
        txtStepTitle.setText(step.getTitle());
        txtStepDescription.setText(step.getDescription());

        if (currentIndex == totalSteps - 1) {
            btnNextStep.setText("Finish");
        } else {
            btnNextStep.setText("I did it!");
        }
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Leave practice?")
                .setMessage("If you go back now, this practice session won’t be saved.")
                .setPositiveButton("Leave", (dialog, which) -> finish())
                .setNegativeButton("Stay", (dialog, which) -> dialog.dismiss())
                .show();
    }
}