package com.example.smartairapplication;

public class Invite {
    private String code;
    private long createdAt;
    private long expiresAt;

    public Invite(){    }
    public Invite(String code, long createdAt, long expiresAt){
        this.code = code;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getCode(){return code;}
    public long getCreatedAt(){return createdAt;}
    public long getExpiresAt(){return expiresAt;}
}
