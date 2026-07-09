package com.autocare.vsms.models;

public class Booking {
    public static final String[] STATUS_OPTIONS = {"Pending", "In Progress", "Completed"};

    private String bookingId;
    private String customerId;
    private String customerName;
    private String vehicleId;
    private String vehicleNumber;
    private String mechanicId;
    private String mechanicName;
    private String serviceId;
    private String serviceName;
    private double servicePrice;
    private String bookingDate;
    private String problemDescription;
    private String status;

    public Booking(String bookingId, String customerId, String customerName, String vehicleId,
                    String vehicleNumber, String mechanicId, String mechanicName, String serviceId,
                    String serviceName, double servicePrice, String bookingDate,
                    String problemDescription, String status) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.vehicleId = vehicleId;
        this.vehicleNumber = vehicleNumber;
        this.mechanicId = mechanicId;
        this.mechanicName = mechanicName;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.servicePrice = servicePrice;
        this.bookingDate = bookingDate;
        this.problemDescription = problemDescription;
        this.status = status;
    }

    public String getBookingId() { return bookingId; }
    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getVehicleId() { return vehicleId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getMechanicId() { return mechanicId; }
    public String getMechanicName() { return mechanicName; }
    public String getServiceId() { return serviceId; }
    public String getServiceName() { return serviceName; }
    public double getServicePrice() { return servicePrice; }
    public String getBookingDate() { return bookingDate; }
    public String getProblemDescription() { return problemDescription; }
    public String getStatus() { return status; }
}
