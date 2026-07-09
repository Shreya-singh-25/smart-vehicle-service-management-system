package com.autocare.vsms.controllers;

import com.autocare.vsms.database.DatabaseManager;
import com.autocare.vsms.models.Customer;
import com.autocare.vsms.utils.IdGenerator;
import com.autocare.vsms.utils.Validators;
import com.autocare.vsms.utils.Validators.ValidationResult;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerController {

    private final DatabaseManager db = DatabaseManager.getInstance();

    private ValidationResult validate(String fullName, String mobile, String email, String address, String excludeId) {
        ValidationResult r = Validators.validateName(fullName, "Full name");
        if (!r.valid) return r;
        r = Validators.validateMobile(mobile);
        if (!r.valid) return r;
        r = Validators.validateEmail(email, false);
        if (!r.valid) return r;
        r = Validators.notEmpty(address, "Address");
        if (!r.valid) return r;

        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT customer_id FROM Customer WHERE mobile_number=?")) {
            ps.setString(1, mobile.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && !rs.getString("customer_id").equals(excludeId)) {
                    return Validators.fail("A customer with this mobile number already exists.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Validators.ok();
    }

    public OperationResult addCustomer(String fullName, String mobile, String email, String address) {
        ValidationResult r = validate(fullName, mobile, email, address, null);
        if (!r.valid) return OperationResult.fail(r.message);

        String newId = IdGenerator.generateId("Customer");
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "INSERT INTO Customer (customer_id, full_name, mobile_number, email, address) VALUES (?,?,?,?,?)")) {
            ps.setString(1, newId);
            ps.setString(2, fullName.trim());
            ps.setString(3, mobile.trim());
            ps.setString(4, email == null ? "" : email.trim());
            ps.setString(5, address.trim());
            ps.executeUpdate();
            return OperationResult.ok("Customer added successfully with ID " + newId + ".", newId);
        } catch (SQLException e) {
            return OperationResult.fail("Database error: " + e.getMessage());
        }
    }

    public OperationResult updateCustomer(String customerId, String fullName, String mobile, String email, String address) {
        ValidationResult r = validate(fullName, mobile, email, address, customerId);
        if (!r.valid) return OperationResult.fail(r.message);

        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "UPDATE Customer SET full_name=?, mobile_number=?, email=?, address=? WHERE customer_id=?")) {
            ps.setString(1, fullName.trim());
            ps.setString(2, mobile.trim());
            ps.setString(3, email == null ? "" : email.trim());
            ps.setString(4, address.trim());
            ps.setString(5, customerId);
            ps.executeUpdate();
            return OperationResult.ok("Customer updated successfully.");
        } catch (SQLException e) {
            return OperationResult.fail("Database error: " + e.getMessage());
        }
    }

    public OperationResult deleteCustomer(String customerId) {
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "DELETE FROM Customer WHERE customer_id=?")) {
            ps.setString(1, customerId);
            ps.executeUpdate();
            return OperationResult.ok("Customer deleted successfully.");
        } catch (SQLException e) {
            return OperationResult.fail("Database error: " + e.getMessage());
        }
    }

    public List<Customer> getAll() {
        List<Customer> result = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT * FROM Customer ORDER BY full_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public List<Customer> search(String keyword) {
        List<Customer> result = new ArrayList<>();
        String like = "%" + keyword.trim() + "%";
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT * FROM Customer WHERE full_name LIKE ? OR mobile_number LIKE ? OR customer_id LIKE ? " +
                "ORDER BY full_name")) {
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

    public Customer getById(String customerId) {
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT * FROM Customer WHERE customer_id=?")) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int countAll() {
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT COUNT(*) c FROM Customer");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("c") : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
            rs.getString("customer_id"),
            rs.getString("full_name"),
            rs.getString("mobile_number"),
            rs.getString("email"),
            rs.getString("address")
        );
    }
}
