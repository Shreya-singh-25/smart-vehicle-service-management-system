package com.autocare.vsms.models;

public class Customer {
    private String customerId;
    private String fullName;
    private String mobileNumber;
    private String email;
    private String address;

    public Customer(String customerId, String fullName, String mobileNumber, String email, String address) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.mobileNumber = mobileNumber;
        this.email = email;
        this.address = address;
    }

    public String getCustomerId() { return customerId; }
    public String getFullName() { return fullName; }
    public String getMobileNumber() { return mobileNumber; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }

    @Override
    public String toString() {
        return fullName + " (" + customerId + ")";
    }
}
