package com.example.smartairapplication;

public class PefLogEntry {
    private Long timestamp;
    private String value;

    public PefLogEntry() {
    }

    public PefLogEntry(Long timestamp, String value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getValue() {
        return value;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setValue(String value) {
        this.value = value;
    }
}