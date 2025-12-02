package com.example.smartairapplication.child_home.log_medicine.technique_helper;

import com.example.smartairapplication.R;

public class TechniqueStep {
    private final String title;
    private final String description;
    private final int imageResId;

    public TechniqueStep(String title, String description, int imageResId){
        this.title = title;
        this.description = description;
        this.imageResId = imageResId;
    }
    public String getTitle(){
        return title;
    }
    public String getDescription(){
        return description;
    }
    public int getImageResId(){
        return imageResId;
    }

    public static TechniqueStep[] createDefaultSteps(){
        return new TechniqueStep[]{
                new TechniqueStep("Get your inhaler ready",
                        "1. Remove the cap\n2. Check there's nothing inside the mouthpiece\n3. Give the inhaler a good shake",
                        R.drawable.tech_step1),
                new TechniqueStep("Prime it if needed",
                        "If you haven’t used your inhaler recently, spray it into the air once (away from your face)",
                        R.drawable.tech_step2),
                new TechniqueStep("Breathe out",
                        "Breathe out as much as you can, away from the inhaler, so your lungs are ready",
                        R.drawable.tech_step3),
                new TechniqueStep("Seal your lips",
                        "Put the mouthpiece in your mouth and close your lips tightly around it",
                        R.drawable.tech_step4),
                new TechniqueStep("Breathe in with a press",
                        "Start breathing in slowly and deeply\nWhile you breathe in, press down the canister one time",
                        R.drawable.tech_step5),
                new TechniqueStep("Hold your breath",
                        "Take the inhaler out of your mouth and hold your breath for 5–10 seconds",
                        R.drawable.tech_step6),
                new TechniqueStep("Breathe out slowly",
                        "Breathe out slowly and gently\nIf you need another puff, wait 30–60 seconds and repeat from Step 3",
                        R.drawable.tech_step7)
        };
    }
}
