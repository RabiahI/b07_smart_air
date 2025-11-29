package com.example.smartairapplication;

import java.util.ArrayList;
import java.util.List;

public class HistoryEntry {
    public String id;
    public String timestamp;
    public boolean nightWaking;
    public boolean activityLimits;
    public String coughWheeze;
    public List<String> triggers;
    public String notes;

    public HistoryEntry(){}

    public HistoryEntry(String timestamp, boolean nightWaking, boolean activityLimits, String coughWheeze, List<String> triggers,
                        String notes){
        this.timestamp = timestamp;
        this.nightWaking = nightWaking;
        this.activityLimits = activityLimits;
        this.coughWheeze = coughWheeze;
        this.triggers = triggers;
        this.notes = notes;
    }
}
