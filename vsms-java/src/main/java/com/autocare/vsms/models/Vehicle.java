package com.autocare.vsms.models;

public class Vehicle {
    private String vehicleId;
    private String vehicleNumber;
    private String ownerId;
    private String ownerName; // populated when joined with Customer
    private String company;
    private String model;
    private int manufacturingYear;
    private String fuelType;
    private String vehicleType;

    public Vehicle(String vehicleId, String vehicleNumber, String ownerId, String ownerName,
                    String company, String model, int manufacturingYear, String fuelType, String vehicleType) {
        this.vehicleId = vehicleId;
        this.vehicleNumber = vehicleNumber;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.company = company;
        this.model = model;
        this.manufacturingYear = manufacturingYear;
        this.fuelType = fuelType;
        this.vehicleType = vehicleType;
    }

    public String getVehicleId() { return vehicleId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public String getCompany() { return company; }
    public String getModel() { return model; }
    public int getManufacturingYear() { return manufacturingYear; }
    public String getFuelType() { return fuelType; }
    public String getVehicleType() { return vehicleType; }

    @Override
    public String toString() {
        return vehicleNumber + " (" + company + " " + model + ")";
    }
}
