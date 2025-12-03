package com.example.smartairapplication.models;

import java.util.List;

public class HistoryEntry {
    private String id;
    private String timestamp;
    private boolean nightWaking;
    private boolean activityLimits;
    private String coughWheeze;
    private List<String> triggers;
    private String notes;

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

    public String getId(){return id;}
    public String getTimestamp(){return timestamp;}
    public boolean getNightWaking(){return nightWaking;}
    public boolean getActivityLimits(){return activityLimits;}
    public String getCoughWheeze(){return coughWheeze;}
    public List<String> getTriggers(){return triggers;}
    public String getNotes(){return notes;}

    public void setId(String id){this.id = id;}
    public void setTimestamp(String timestamp){this.timestamp = timestamp;}
    public void setNightWaking(boolean nightWaking){this.nightWaking = nightWaking;}
    public void setActivityLimits(boolean activityLimits){this.activityLimits = activityLimits;}
    public void setCoughWheeze(String coughWheeze){this.coughWheeze = coughWheeze;}
    public void setTriggers(List<String> triggers){this.triggers = triggers;}
    public void setNotes(String notes){this.notes = notes;}
}
