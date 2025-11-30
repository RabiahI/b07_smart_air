package com.example.smartairapplication;

public class DailyOverview {
    public String date;
    public String zone;
    public int rescueCount;

    public DailyOverview() {
    }

    public DailyOverview(String date, String zone, int rescueCount) {
        this.date = date;
        this.zone = zone;
        this.rescueCount = rescueCount;
    }
}