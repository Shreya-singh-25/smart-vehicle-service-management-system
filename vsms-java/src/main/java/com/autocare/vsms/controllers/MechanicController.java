package com.autocare.vsms.controllers;

import com.autocare.vsms.database.DatabaseManager;
import com.autocare.vsms.models.Mechanic;
import com.autocare.vsms.utils.IdGenerator;
import com.autocare.vsms.utils.Validators;
import com.autocare.vsms.utils.Validators.ValidationResult;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MechanicController {

    public static final String[] SPECIALIZATIONS = {
        "Engine Specialist", "Brake & Suspension", "Electrical Systems",
        "AC Technician", "Body & Paint", "General Mechanic", "Transmission Specialist"
    };

    private final DatabaseManager db = DatabaseManager.getInstance();

    private ValidationResult validate(String fullName, String mobile, String experience, String specialization, String excludeId) {
        ValidationResult r = Validators.validateName(fullName, "Full name");
        if (!r.valid) return r;
        r = Validators.validateMobile(mobile);
        if (!r.valid) return r;
        r = Validators.validateInteger(experience, "Experience (years)", 0, 60);
        if (!r.valid) return r;
        r = Validators.notEmpty(specialization, "Specialization");
        if (!r.valid) return r;

        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT mechanic_id FROM Mechanic WHERE mobile_number=?")) {
            ps.setString(1, mobile.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && !rs.getString("mechanic_id").equals(excludeId)) {
                    return Validators.fail("A mechanic with this mobile number already exists.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Validators.ok();
    }

    public OperationResult addMechanic(String fullName, String mobile, String experience, String specialization) {
        ValidationResult r = validate(fullName, mobile, experience, specialization, null);
        if (!r.valid) return OperationResult.fail(r.message);

        String newId = IdGenerator.generateId("Mechanic");
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "INSERT INTO Mechanic (mechanic_id, full_name, mobile_number, experience_years, specialization) " +
                "VALUES (?,?,?,?,?)")) {
            ps.setString(1, newId);
            ps.setString(2, fullName.trim());
            ps.setString(3, mobile.trim());
            ps.setInt(4, Integer.parseInt(experience.trim()));
            ps.setString(5, specialization);
            ps.executeUpdate();
            return OperationResult.ok("Mechanic added successfully with ID " + newId + ".", newId);
        } catch (SQLException e) {
            return OperationResult.fail("Database error: " + e.getMessage());
        }
    }

    public OperationResult updateMechanic(String mechanicId, String fullName, String mobile, String experience, String specialization) {
        ValidationResult r = validate(fullName, mobile, experience, specialization, mechanicId);
        if (!r.valid) return OperationResult.fail(r.message);

        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "UPDATE Mechanic SET full_name=?, mobile_number=?, experience_years=?, specialization=? WHERE mechanic_id=?")) {
            ps.setString(1, fullName.trim());
            ps.setString(2, mobile.trim());
            ps.setInt(3, Integer.parseInt(experience.trim()));
            ps.setString(4, specialization);
            ps.setString(5, mechanicId);
            ps.executeUpdate();
            return OperationResult.ok("Mechanic updated successfully.");
        } catch (SQLException e) {
            return OperationResult.fail("Database error: " + e.getMessage());
        }
    }

    public OperationResult deleteMechanic(String mechanicId) {
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "DELETE FROM Mechanic WHERE mechanic_id=?")) {
            ps.setString(1, mechanicId);
            ps.executeUpdate();
            return OperationResult.ok("Mechanic deleted successfully.");
        } catch (SQLException e) {
            return OperationResult.fail("Database error: " + e.getMessage());
        }
    }

    public List<Mechanic> getAll() {
        List<Mechanic> result = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT * FROM Mechanic ORDER BY full_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public List<Mechanic> search(String keyword) {
        List<Mechanic> result = new ArrayList<>();
        String like = "%" + keyword.trim() + "%";
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT * FROM Mechanic WHERE full_name LIKE ? OR mobile_number LIKE ? OR mechanic_id LIKE ? " +
                "OR specialization LIKE ? ORDER BY full_name")) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
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

    public Mechanic getById(String mechanicId) {
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT * FROM Mechanic WHERE mechanic_id=?")) {
            ps.setString(1, mechanicId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int countAll() {
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT COUNT(*) c FROM Mechanic");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("c") : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Mechanic mapRow(ResultSet rs) throws SQLException {
        return new Mechanic(
            rs.getString("mechanic_id"),
            rs.getString("full_name"),
            rs.getString("mobile_number"),
            rs.getInt("experience_years"),
            rs.getString("specialization")
        );
    }
}
