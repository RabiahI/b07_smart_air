package com.example.smartairapplication;

import java.util.Map;

public class TriageLogEntry {
    // Map directly to Firebase keys
    private Long timeStampStarted;
    private Boolean escalated;
    private String latestPef;
    // Map the complex "redFlags" node as a generic map, assuming "result" is inside
    private Map<String, Object> redFlags;

    // Required default constructor for Firebase
    public TriageLogEntry() {
    }

    // Getters
    public Long getTimeStampStarted() {
        return timeStampStarted;
    }

    public Boolean getEscalated() {
        return escalated;
    }

    public String getLatestPef() {
        return latestPef;
    }

    public Map<String, Object> getRedFlags() {
        return redFlags;
    }

    // Custom method to get the result string easily
    public String getResultText() {
        if (redFlags != null && redFlags.containsKey("result")) {
            return String.valueOf(redFlags.get("result"));
        }
        return "N/A";
    }
}