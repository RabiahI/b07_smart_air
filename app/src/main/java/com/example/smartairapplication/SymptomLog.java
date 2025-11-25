package com.example.smartairapplication;

import java.util.ArrayList;

public class SymptomLog {
    public String timestamp;
    public boolean nightWaking;
    public boolean activityLimits;
    public String coughWheeze; //"none", "mild", or "bad"
    public ArrayList<String> triggers;
    public String notes;

    public SymptomLog(){}

    public SymptomLog(String timestamp, boolean nightWaking, boolean activityLimits,
                      String coughWheeze, ArrayList<String> triggers, String notes){
        this.timestamp = timestamp;
        this.nightWaking = nightWaking;
        this.activityLimits = activityLimits;
        this.coughWheeze = coughWheeze;
        this.triggers = triggers;
        this.notes = notes;
    }

}
