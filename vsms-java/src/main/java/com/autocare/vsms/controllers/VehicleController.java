package com.autocare.vsms.controllers;

import com.autocare.vsms.database.DatabaseManager;
import com.autocare.vsms.models.Vehicle;
import com.autocare.vsms.utils.IdGenerator;
import com.autocare.vsms.utils.Validators;
import com.autocare.vsms.utils.Validators.ValidationResult;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VehicleController {

    public static final String[] FUEL_TYPES = {"Petrol", "Diesel", "Electric", "CNG", "Hybrid"};
    public static final String[] VEHICLE_TYPES = {"Hatchback", "Sedan", "SUV", "Bike", "Truck", "Van", "Other"};

    private final DatabaseManager db = DatabaseManager.getInstance();

    private ValidationResult validate(String vehicleNumber, String ownerId, String company, String model,
                                       String year, String fuelType, String vehicleType, String excludeId) {
        ValidationResult r = Validators.validateVehicleNumber(vehicleNumber);
        if (!r.valid) return r;
        r = Validators.notEmpty(ownerId, "Owner");
        if (!r.valid) return r;

        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT customer_id FROM Customer WHERE customer_id=?")) {
            ps.setString(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Validators.fail("Selected owner does not exist.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        r = Validators.notEmpty(company, "Company");
        if (!r.valid) return r;
        r = Validators.notEmpty(model, "Model");
        if (!r.valid) return r;
        r = Validators.validateYear(year);
        if (!r.valid) return r;
        r = Validators.notEmpty(fuelType, "Fuel type");
        if (!r.valid) return r;
        r = Validators.notEmpty(vehicleType, "Vehicle type");
        if (!r.valid) return r;

        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT vehicle_id FROM Vehicle WHERE vehicle_number=?")) {
            ps.setString(1, vehicleNumber.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && !rs.getString("vehicle_id").equals(excludeId)) {
                    return Validators.fail("A vehicle with this registration number already exists.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Validators.ok();
    }

    public OperationResult addVehicle(String vehicleNumber, String ownerId, String company, String model,
                                       String year, String fuelType, String vehicleType) {
        ValidationResult r = validate(vehicleNumber, ownerId, company, model, year, fuelType, vehicleType, null);
        if (!r.valid) return OperationResult.fail(r.message);

        String newId = IdGenerator.generateId("Vehicle");
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "INSERT INTO Vehicle (vehicle_id, vehicle_number, owner_id, company, model, " +
                "manufacturing_year, fuel_type, vehicle_type) VALUES (?,?,?,?,?,?,?,?)")) {
            ps.setString(1, newId);
            ps.setString(2, vehicleNumber.trim().toUpperCase());
            ps.setString(3, ownerId);
            ps.setString(4, company.trim());
            ps.setString(5, model.trim());
            ps.setInt(6, Integer.parseInt(year.trim()));
            ps.setString(7, fuelType);
            ps.setString(8, vehicleType);
            ps.executeUpdate();
            return OperationResult.ok("Vehicle added successfully with ID " + newId + ".", newId);
        } catch (SQLException e) {
            return OperationResult.fail("Database error: " + e.getMessage());
        }
    }

    public OperationResult updateVehicle(String vehicleId, String vehicleNumber, String ownerId, String company,
                                          String model, String year, String fuelType, String vehicleType) {
        ValidationResult r = validate(vehicleNumber, ownerId, company, model, year, fuelType, vehicleType, vehicleId);
        if (!r.valid) return OperationResult.fail(r.message);

        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "UPDATE Vehicle SET vehicle_number=?, owner_id=?, company=?, model=?, manufacturing_year=?, " +
                "fuel_type=?, vehicle_type=? WHERE vehicle_id=?")) {
            ps.setString(1, vehicleNumber.trim().toUpperCase());
            ps.setString(2, ownerId);
            ps.setString(3, company.trim());
            ps.setString(4, model.trim());
            ps.setInt(5, Integer.parseInt(year.trim()));
            ps.setString(6, fuelType);
            ps.setString(7, vehicleType);
            ps.setString(8, vehicleId);
            ps.executeUpdate();
            return OperationResult.ok("Vehicle updated successfully.");
        } catch (SQLException e) {
            return OperationResult.fail("Database error: " + e.getMessage());
        }
    }

    public OperationResult deleteVehicle(String vehicleId) {
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "DELETE FROM Vehicle WHERE vehicle_id=?")) {
            ps.setString(1, vehicleId);
            ps.executeUpdate();
            return OperationResult.ok("Vehicle deleted successfully.");
        } catch (SQLException e) {
            return OperationResult.fail("Database error: " + e.getMessage());
        }
    }

    public List<Vehicle> getAll() {
        List<Vehicle> result = new ArrayList<>();
        String sql = "SELECT Vehicle.*, Customer.full_name AS owner_name FROM Vehicle " +
                     "JOIN Customer ON Vehicle.owner_id = Customer.customer_id ORDER BY Vehicle.vehicle_number";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public List<Vehicle> search(String keyword) {
        List<Vehicle> result = new ArrayList<>();
        String like = "%" + keyword.trim() + "%";
        String sql = "SELECT Vehicle.*, Customer.full_name AS owner_name FROM Vehicle " +
                     "JOIN Customer ON Vehicle.owner_id = Customer.customer_id " +
                     "WHERE Vehicle.vehicle_number LIKE ? OR Customer.full_name LIKE ? OR Vehicle.vehicle_id LIKE ? " +
                     "ORDER BY Vehicle.vehicle_number";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public Vehicle getById(String vehicleId) {
        String sql = "SELECT Vehicle.*, Customer.full_name AS owner_name FROM Vehicle " +
                     "JOIN Customer ON Vehicle.owner_id = Customer.customer_id WHERE Vehicle.vehicle_id=?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Vehicle> getVehiclesForOwner(String ownerId) {
        List<Vehicle> result = new ArrayList<>();
        String sql = "SELECT Vehicle.*, Customer.full_name AS owner_name FROM Vehicle " +
                     "JOIN Customer ON Vehicle.owner_id = Customer.customer_id WHERE Vehicle.owner_id=?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public int countAll() {
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT COUNT(*) c FROM Vehicle");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("c") : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Vehicle mapRow(ResultSet rs) throws SQLException {
        return new Vehicle(
            rs.getString("vehicle_id"),
            rs.getString("vehicle_number"),
            rs.getString("owner_id"),
            rs.getString("owner_name"),
            rs.getString("company"),
            rs.getString("model"),
            rs.getInt("manufacturing_year"),
            rs.getString("fuel_type"),
            rs.getString("vehicle_type")
        );
    }
}
