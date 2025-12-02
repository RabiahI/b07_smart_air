package com.example.smartairapplication.models;

public class MedicineLog {
    private String inhalerType;
    private int puffCount;
    private int sobBefore;
    private int sobAfter;
    private String postFeeling;
    private long timestamp;

    public MedicineLog(){}

    public MedicineLog(String inhalerType, int puffCount, int sobBefore, int sobAfter, String postFeeling){
        this.inhalerType = inhalerType;
        this.puffCount = puffCount;
        this.sobBefore = sobBefore;
        this.sobAfter = sobAfter;
        this.postFeeling = postFeeling;
        this.timestamp = System.currentTimeMillis();
    }

    public String getInhalerType() {
        return inhalerType;
    }

    public int getPuffCount() {
        return puffCount;
    }

    public int getSobBefore() {
        return sobBefore;
    }

    public int getSobAfter() {
        return sobAfter;
    }

    public String getPostFeeling() {
        return postFeeling;
    }

    public long getTimestamp() {
        return timestamp;
    }
}