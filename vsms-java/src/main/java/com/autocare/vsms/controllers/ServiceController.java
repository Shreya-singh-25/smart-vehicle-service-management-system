package com.autocare.vsms.controllers;

import com.autocare.vsms.database.DatabaseManager;
import com.autocare.vsms.models.Service;
import com.autocare.vsms.utils.IdGenerator;
import com.autocare.vsms.utils.Validators;
import com.autocare.vsms.utils.Validators.ValidationResult;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceController {

    private final DatabaseManager db = DatabaseManager.getInstance();

    private ValidationResult validate(String serviceName, String price, String estimatedTime, String excludeId) {
        ValidationResult r = Validators.notEmpty(serviceName, "Service name");
        if (!r.valid) return r;
        r = Validators.validatePositiveNumber(price, "Price", false);
        if (!r.valid) return r;
        r = Validators.notEmpty(estimatedTime, "Estimated time");
        if (!r.valid) return r;

        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT service_id FROM Service WHERE service_name=?")) {
            ps.setString(1, serviceName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && !rs.getString("service_id").equals(excludeId)) {
                    return Validators.fail("A service with this name already exists.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Validators.ok();
    }

    public OperationResult addService(String serviceName, String price, String estimatedTime) {
        ValidationResult r = validate(serviceName, price, estimatedTime, null);
        if (!r.valid) return OperationResult.fail(r.message);

        String newId = IdGenerator.generateId("Service");
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "INSERT INTO Service (service_id, service_name, price, estimated_time) VALUES (?,?,?,?)")) {
            ps.setString(1, newId);
            ps.setString(2, serviceName.trim());
            ps.setDouble(3, Double.parseDouble(price.trim()));
            ps.setString(4, estimatedTime.trim());
            ps.executeUpdate();
            return OperationResult.ok("Service added successfully with ID " + newId + ".", newId);
        } catch (SQLException e) {
            return OperationResult.fail("Database error: " + e.getMessage());
        }
    }

    public OperationResult updateService(String serviceId, String serviceName, String price, String estimatedTime) {
        ValidationResult r = validate(serviceName, price, estimatedTime, serviceId);
        if (!r.valid) return OperationResult.fail(r.message);

        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "UPDATE Service SET service_name=?, price=?, estimated_time=? WHERE service_id=?")) {
            ps.setString(1, serviceName.trim());
            ps.setDouble(2, Double.parseDouble(price.trim()));
            ps.setString(3, estimatedTime.trim());
            ps.setString(4, serviceId);
            ps.executeUpdate();
            return OperationResult.ok("Service updated successfully.");
        } catch (SQLException e) {
            return OperationResult.fail("Database error: " + e.getMessage());
        }
    }

    public OperationResult deleteService(String serviceId) {
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "DELETE FROM Service WHERE service_id=?")) {
            ps.setString(1, serviceId);
            ps.executeUpdate();
            return OperationResult.ok("Service deleted successfully.");
        } catch (SQLException e) {
            return OperationResult.fail("Database error: " + e.getMessage());
        }
    }

    public List<Service> getAll() {
        List<Service> result = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT * FROM Service ORDER BY service_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public List<Service> search(String keyword) {
        List<Service> result = new ArrayList<>();
        String like = "%" + keyword.trim() + "%";
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT * FROM Service WHERE service_name LIKE ? OR service_id LIKE ? ORDER BY service_name")) {
            ps.setString(1, like);
            ps.setString(2, like);
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

    public Service getById(String serviceId) {
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT * FROM Service WHERE service_id=?")) {
            ps.setString(1, serviceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Service mapRow(ResultSet rs) throws SQLException {
        return new Service(
            rs.getString("service_id"),
            rs.getString("service_name"),
            rs.getDouble("price"),
            rs.getString("estimated_time")
        );
    }
}
