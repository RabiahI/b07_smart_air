package com.example.smartairapplication.models;

public class RescueLogEntry {
    private final Long timestamp;
    private final String sobBefore; // Shortness of Breath level before rescue
    private final String sobAfter;  // Shortness of Breath level after rescue
    private final String puffCount; // Number of puffs taken
    private final String postFeeling; // Feeling after rescue (e.g., "Better", "Worse", "Same")

    /**
     * Constructor for RescueLogEntry.
     * @param timestamp Time log was recorded (milliseconds since epoch).
     * @param sobBefore Shortness of Breath level before rescue.
     * @param sobAfter Shortness of Breath level after rescue.
     * @param puffCount Number of puffs taken.
     * @param postFeeling Reported feeling post-dose.
     */
    public RescueLogEntry(Long timestamp, String sobBefore, String sobAfter, String puffCount, String postFeeling) {
        this.timestamp = timestamp;
        this.sobBefore = sobBefore;
        this.sobAfter = sobAfter;
        this.puffCount = puffCount;
        this.postFeeling = postFeeling;
    }

    // Getters
    public Long getTimestamp() {
        return timestamp;
    }

    public String getSobBefore() {
        return sobBefore;
    }

    public String getSobAfter() {
        return sobAfter;
    }

    public String getPuffCount() {
        return puffCount;
    }

    public String getPostFeeling() {
        return postFeeling;
    }
}