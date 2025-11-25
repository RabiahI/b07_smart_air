package com.example.smartairapplication;

public class Medicine {
    public String id;
    public String name;
    public String purchaseDate;
    public String expiryDate;
    public int amountLeft;

    public Medicine() {} // Firebase

    public Medicine(String name, String purchaseDate, String expiryDate, int amountLeft) {
        this.name = name;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.amountLeft = amountLeft;
    }
}

