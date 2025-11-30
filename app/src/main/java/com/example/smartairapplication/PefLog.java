package com.example.smartairapplication;

public class PefLog {
    private int value;
    private long timestamp;

    public PefLog() {
    }

    public PefLog(int value, long timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }
    public int getPefValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }
}