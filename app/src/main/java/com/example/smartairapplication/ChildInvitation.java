package com.example.smartairapplication;

public class ChildInvitation {
    public String code;
    public String parentId;
    public long expiry;

    public ChildInvitation() {
        // Default constructor required for calls to DataSnapshot.getValue(ChildInvitation.class)
    }

    public ChildInvitation(String code, String parentId, long expiry) {
        this.code = code;
        this.parentId = parentId;
        this.expiry = expiry;
    }

    public String getCode() {
        return code;
    }

    public String getParentId() {
        return parentId;
    }

    public long getExpiry() {
        return expiry;
    }
}
