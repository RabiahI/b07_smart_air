package com.example.smartairapplication;

public class Medicine {
    public String id;
    public String name;
    public String purchaseDate;
    public String expiryDate;
    public int amountLeft;
    public boolean lowFlag;
    public boolean expiryAlertSent = false;

    public Medicine() {} // Firebase

    public Medicine(String name, String purchaseDate, String expiryDate, int amountLeft, boolean lowFlag) {
        this.name = name;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.amountLeft = amountLeft;
        this.lowFlag = lowFlag;
    }
}

