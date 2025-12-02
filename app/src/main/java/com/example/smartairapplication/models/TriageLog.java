package com.example.smartairapplication.models;

public class TriageLog {

    public String recentRescue;
    public String PEF;
    public String result;
    public String notes;
    public long timeStampStarted;
    public boolean escalated;
    public boolean parentAlertSent;
    public RedFlags redFlags;

    public TriageLog() {
    }

    public TriageLog(String recentRescue, String PEF, String result, String notes,
                     long timeStampStarted, boolean escalated, boolean parentAlertSent,
                     RedFlags redFlags) {
        this.recentRescue = recentRescue;
        this.PEF = PEF;
        this.result = result;
        this.notes = notes;
        this.timeStampStarted = timeStampStarted;
        this.escalated = escalated;
        this.parentAlertSent = parentAlertSent;
        this.redFlags = redFlags;
    }

    public static class RedFlags {
        public boolean cantSpeak;
        public boolean blueLips;
        public boolean chestRestriction;

        public RedFlags() {
        }

        public RedFlags(boolean cantSpeak, boolean blueLips, boolean chestRestriction) {
            this.cantSpeak = cantSpeak;
            this.blueLips = blueLips;
            this.chestRestriction = chestRestriction;
        }
    }
}
