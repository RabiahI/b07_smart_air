package com.example.smartairapplication.models;

public class PefLog {
    private int pefValue;
    private long timestamp;

    public PefLog() {
    }

    public PefLog(int value, long timestamp) {
        this.pefValue = value;
        this.timestamp = timestamp;
    }
    public int getPefValue() {
        return pefValue;
    }

    public long getTimestamp() {
        return timestamp;
    }
}