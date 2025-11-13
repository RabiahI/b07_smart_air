package com.example.smartairapplication;

public class Child extends User {
    private String childId;
    private String name;
    private String dob;
    private String notes;
    private int age;
    private String accessStatus; //"not_shared", "generated", "accepted"
    private String inviteCode; //provider's invite code

    public Child(){}

    public Child(String email, String childId, String name, String dob, String notes, int age){
        super(email);
        this.childId = childId;
        this.name = name;
        this.dob = dob;
        this.notes = notes;
        this.age = age;
    }

    public String getChildId(){ return childId; }
    public String getName(){ return name; }
    public String getDob(){ return dob; }
    public String getNotes(){ return notes; }
    public int getAge(){ return age; }
    public String getAccessStatus(){return accessStatus;}
    public String getInviteCode(){return inviteCode;}
    public void setChildId(String childId){
        this.childId = childId;
    }
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

    public void setAccessStatus(String accessStatus){this.accessStatus = accessStatus;  }
    public void setInviteCode(String inviteCode){this.inviteCode = inviteCode;  }
}
