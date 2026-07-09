package com.autocare.vsms.models;

public class Service {
    private String serviceId;
    private String serviceName;
    private double price;
    private String estimatedTime;

    public Service(String serviceId, String serviceName, double price, String estimatedTime) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.price = price;
        this.estimatedTime = estimatedTime;
    }

    public String getServiceId() { return serviceId; }
    public String getServiceName() { return serviceName; }
    public double getPrice() { return price; }
    public String getEstimatedTime() { return estimatedTime; }

    @Override
    public String toString() {
        return serviceName + " - Rs." + String.format("%.0f", price);
    }
}
