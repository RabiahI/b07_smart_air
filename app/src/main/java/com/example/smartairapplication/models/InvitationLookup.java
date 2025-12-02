package com.example.smartairapplication.models;

public class InvitationLookup {
    public String parentId;

    public InvitationLookup() {
    }

    public InvitationLookup(String parentId) {
        this.parentId = parentId;
    }

    public String getParentId() {
        return parentId;
    }
}
