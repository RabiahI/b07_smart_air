package com.example.smartairapplication.models;

import java.util.List;

public class SymptomLogEntry {
    private Long timestamp;
    private Boolean nightWaking;
    private String coughWheeze;
    private List<String> triggers; // List of triggers associated with this log

    public SymptomLogEntry() {
    }

    public SymptomLogEntry(Long timestamp, Boolean nightWaking, String coughWheeze, List<String> triggers) {
        this.timestamp = timestamp;
        this.nightWaking = nightWaking;
        this.coughWheeze = coughWheeze;
        
        this.triggers = triggers;
    }

    // Getters
    public Long getTimestamp() {
        return timestamp;
    }

    public Boolean getNightWaking() {
        // Handle null case gracefully
        return nightWaking != null ? nightWaking : false;
    }

    public String getCoughWheeze() {
        return coughWheeze;
    }

    public List<String> getTriggers() {
        return triggers;
    }

    // Setters (optional, but often helpful)
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setNightWaking(Boolean nightWaking) {
        this.nightWaking = nightWaking;
    }

    public void setCoughWheeze(String coughWheeze) {
        this.coughWheeze = coughWheeze;
    }

    public void setTriggers(List<String> triggers) {
        this.triggers = triggers;
    }
}