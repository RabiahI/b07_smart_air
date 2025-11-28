package com.example.smartairapplication;

public class Alert {
    private String type;
    private String message;
    private long timestamp;
    private String severity;

    public Alert() {
        // Default constructor required for calls to DataSnapshot.getValue(Alert.class)
    }

    public Alert(String type, String message, long timestamp, String severity) {
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
        this.severity = severity;
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
}
