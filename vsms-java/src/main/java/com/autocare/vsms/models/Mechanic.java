package com.autocare.vsms.models;

public class Mechanic {
    private String mechanicId;
    private String fullName;
    private String mobileNumber;
    private int experienceYears;
    private String specialization;

    public Mechanic(String mechanicId, String fullName, String mobileNumber, int experienceYears, String specialization) {
        this.mechanicId = mechanicId;
        this.fullName = fullName;
        this.mobileNumber = mobileNumber;
        this.experienceYears = experienceYears;
        this.specialization = specialization;
    }

    public String getMechanicId() { return mechanicId; }
    public String getFullName() { return fullName; }
    public String getMobileNumber() { return mobileNumber; }
    public int getExperienceYears() { return experienceYears; }
    public String getSpecialization() { return specialization; }

    @Override
    public String toString() {
        return fullName + " (" + mechanicId + ")";
    }
}
