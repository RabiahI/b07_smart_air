package com.example.smartairapplication.models;

import java.util.Map;

public class TriageLogEntry {
    private Long timeStampStarted;
    private Boolean escalated;
    private String latestPef;
    private Map<String, Object> redFlags;

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

    public String getResultText() {
        if (redFlags != null && redFlags.containsKey("result")) {
            return String.valueOf(redFlags.get("result"));
        }
        return "N/A";
    }
}