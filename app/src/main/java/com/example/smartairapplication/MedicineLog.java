package com.example.smartairapplication;

public class MedicineLog {
    public String inhalerType;
    public int puffCount;
    public int sobBefore;
    public int sobAfter;
    public String postFeeling;
    public long timestamp;

    public MedicineLog(){}

    public MedicineLog(String inhalerType, int puffCount, int sobBefore, int sobAfter, String postFeeling){
        this.inhalerType = inhalerType;
        this.puffCount = puffCount;
        this.sobBefore = sobBefore;
        this.sobAfter = sobAfter;
        this.postFeeling = postFeeling;
        this.timestamp = System.currentTimeMillis();
    }
}
