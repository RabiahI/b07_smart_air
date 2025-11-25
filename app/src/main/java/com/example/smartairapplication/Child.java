package com.example.smartairapplication;

public class Child extends User {
    private String childId;
    private String parentId; // Added parentId field
    private String name;
    private String dob;
    private String notes;
    private int age;
    private int personalBest;
    private int latestPef;
    private String accessStatus; //"not_shared", "generated", "accepted"
    private String inviteCode; //provider's invite code

    public Child(){}

    public Child(String email, String childId, String name, String dob, String notes, int age, int personalBest, int latestPef){
        super(email);
        this.childId = childId;
        this.name = name;
        this.dob = dob;
        this.notes = notes;
        this.age = age;
        this.personalBest = personalBest;
        this.latestPef = latestPef;
    }

    // Constructor with parentId
    public Child(String email, String childId, String parentId, String name, String dob, String notes, int age, int personalBest, int latestPef){
        super(email);
        this.childId = childId;
        this.parentId = parentId;
        this.name = name;
        this.dob = dob;
        this.notes = notes;
        this.age = age;
        this.personalBest = personalBest;
        this.latestPef = latestPef;
    }

    public String getChildId(){ return childId; }
    public String getParentId(){ return parentId; }
    public String getName(){ return name; }
    public String getDob(){ return dob; }
    public String getNotes(){ return notes; }
    public int getAge(){ return age; }
    public int getPersonalBest() { return personalBest; }
    public int getLatestPef() { return latestPef; }
    public String getAccessStatus(){return accessStatus;}
    public String getInviteCode(){return inviteCode;}
    public void setChildId(String childId){
        this.childId = childId;
    }
    public void setParentId(String parentId){ this.parentId = parentId; }

    public void setName(String name){
        this.name = name;
    }
    public void setDob(String dob){
        this.dob = dob;
    }
    public void setNotes(String notes){
        this.notes = notes;
    }
    public void setAge(int age){
        this.age = age;
    }
    public void setPersonalBest(int personalBest) { this.personalBest = personalBest; }
    public void setLatestPef(int latestPef) { this.latestPef = latestPef; }

    public void setAccessStatus(String accessStatus){this.accessStatus = accessStatus;  }
    public void setInviteCode(String inviteCode){this.inviteCode = inviteCode;  }
}
