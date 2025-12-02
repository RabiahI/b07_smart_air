package com.example.smartairapplication;

public class Medicine {
    private String id;
    private String name;
    private String purchaseDate;
    private String expiryDate;
    private int amountLeft;
    private boolean lowFlag;
    private boolean expiryAlertSent = false;

    public Medicine() {} // Firebase

    public Medicine(String name, String purchaseDate, String expiryDate, int amountLeft, boolean lowFlag) {
        this.name = name;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.amountLeft = amountLeft;
        this.lowFlag = lowFlag;
    }

    public String getId(){return id;}
    public String getName(){return name;}
    public String getPurchaseDate(){return purchaseDate;}
    public String getExpiryDate(){return expiryDate;}
    public int getAmountLeft(){return amountLeft;}
    public boolean getLowFlag(){return lowFlag;}
    public boolean getExpiryAlertSent(){return expiryAlertSent;}

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPurchaseDate(String purchaseDate) { this.purchaseDate = purchaseDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public void setAmountLeft(int amountLeft) { this.amountLeft = amountLeft; }
    public void setLowFlag(boolean lowFlag) { this.lowFlag = lowFlag; }
    public void setExpiryAlertSent(boolean expiryAlertSent) { this.expiryAlertSent = expiryAlertSent; }
}

