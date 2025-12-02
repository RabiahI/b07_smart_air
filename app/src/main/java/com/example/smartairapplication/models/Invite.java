package com.example.smartairapplication.models;

public class Invite {
    private String code;
    private long createdAt;
    private long expiresAt;
    private boolean accepted;

    public Invite(){    }
    public Invite(String code, long createdAt, long expiresAt){
        this.code = code;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.accepted = false;
    }

    public String getCode(){return code;}
    public long getCreatedAt(){return createdAt;}
    public long getExpiresAt(){return expiresAt;}
    public boolean isAccepted(){return accepted;}
    public void setCode(String code) { this.code = code; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }

}
