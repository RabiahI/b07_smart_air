package com.example.smartairapplication.models;

public class Alert {
    private String type;
    private String message;
    private long timestamp;
    private String severity;
    private String childId;

    public Alert() {
        // Default constructor required for calls to DataSnapshot.getValue(Alert.class)
    }

    public Alert(String type, String message, long timestamp, String severity, String childId) {
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
        this.severity = severity;
        this.childId = childId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }
}
