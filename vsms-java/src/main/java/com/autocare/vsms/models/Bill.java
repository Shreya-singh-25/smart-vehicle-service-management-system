package com.autocare.vsms.models;

public class Bill {
    private String billId;
    private String bookingId;
    private double partsCost;
    private double labourCharge;
    private double taxPercent;
    private double discount;
    private double totalAmount;
    private String billDate;

    // Extra joined fields used when rendering an invoice
    private String customerName;
    private String customerMobile;
    private String vehicleNumber;
    private String vehicleCompany;
    private String vehicleModel;
    private String serviceName;
    private String mechanicName;
    private String status;

    public Bill(String billId, String bookingId, double partsCost, double labourCharge,
                double taxPercent, double discount, double totalAmount, String billDate) {
        this.billId = billId;
        this.bookingId = bookingId;
        this.partsCost = partsCost;
        this.labourCharge = labourCharge;
        this.taxPercent = taxPercent;
        this.discount = discount;
        this.totalAmount = totalAmount;
        this.billDate = billDate;
    }

    public static double calculateTotal(double partsCost, double labourCharge, double taxPercent, double discount) {
        double subtotal = partsCost + labourCharge;
        double taxAmount = subtotal * (taxPercent / 100.0);
        double total = subtotal + taxAmount - discount;
        return Math.max(Math.round(total * 100.0) / 100.0, 0);
    }

    public String getBillId() { return billId; }
    public String getBookingId() { return bookingId; }
    public double getPartsCost() { return partsCost; }
    public double getLabourCharge() { return labourCharge; }
    public double getTaxPercent() { return taxPercent; }
    public double getDiscount() { return discount; }
    public double getTotalAmount() { return totalAmount; }
    public String getBillDate() { return billDate; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerMobile() { return customerMobile; }
    public void setCustomerMobile(String customerMobile) { this.customerMobile = customerMobile; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public String getVehicleCompany() { return vehicleCompany; }
    public void setVehicleCompany(String vehicleCompany) { this.vehicleCompany = vehicleCompany; }
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getMechanicName() { return mechanicName; }
    public void setMechanicName(String mechanicName) { this.mechanicName = mechanicName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
