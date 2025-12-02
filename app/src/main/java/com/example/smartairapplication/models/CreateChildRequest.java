package com.example.smartairapplication.models;

public class CreateChildRequest {
    public String email;
    public String password;
    public String name;
    public String dob;
    public String notes;
    public int age;
    public int threshold;
    public int controllerDays;

    public CreateChildRequest() {}

    public CreateChildRequest(String email, String password, String name, String dob,
                              String notes, int age, int threshold, int controllerDays) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.dob = dob;
        this.notes = notes;
        this.age = age;
        this.threshold = threshold;
        this.controllerDays = controllerDays;
    }
}
