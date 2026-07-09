package com.autocare.vsms.controllers;

import com.autocare.vsms.database.DatabaseManager;
import com.autocare.vsms.models.Booking;
import com.autocare.vsms.utils.IdGenerator;
import com.autocare.vsms.utils.Validators;
import com.autocare.vsms.utils.Validators.ValidationResult;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookingController {

    private final DatabaseManager db = DatabaseManager.getInstance();

    private static final String JOIN_SQL = """
        SELECT Booking.*, Customer.full_name AS customer_name, Vehicle.vehicle_number,
               Service.service_name, Service.price AS service_price,
               Mechanic.full_name AS mechanic_name
        FROM Booking
        JOIN Customer ON Booking.customer_id = Customer.customer_id
        JOIN Vehicle ON Booking.vehicle_id = Vehicle.vehicle_id
        JOIN Service ON Booking.service_id = Service.service_id
        LEFT JOIN Mechanic ON Booking.mechanic_id = Mechanic.mechanic_id
        """;

    private ValidationResult validate(String customerId, String vehicleId, String serviceId, String bookingDate, String status) {
        ValidationResult r = Validators.notEmpty(customerId, "Customer");
        if (!r.valid) return r;
        r = Validators.notEmpty(vehicleId, "Vehicle");
        if (!r.valid) return r;
        r = Validators.notEmpty(serviceId, "Service");
        if (!r.valid) return r;
        r = Validators.validateDate(bookingDate, "Booking date");
        if (!r.valid) return r;
        if (!Arrays.asList(Booking.STATUS_OPTIONS).contains(status)) {
            return Validators.fail("Invalid status selected.");
        }
        return Validators.ok();
    }

    public OperationResult addBooking(String customerId, String vehicleId, String mechanicId, String serviceId,
                                       String bookingDate, String problemDescription, String status) {
        ValidationResult r = validate(customerId, vehicleId, serviceId, bookingDate, status);
        if (!r.valid) return OperationResult.fail(r.message);

        String newId = IdGenerator.generateId("Booking");
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "INSERT INTO Booking (booking_id, customer_id, vehicle_id, mechanic_id, service_id, " +
                "booking_date, problem_description, status) VALUES (?,?,?,?,?,?,?,?)")) {
            ps.setString(1, newId);
            ps.setString(2, customerId);
            ps.setString(3, vehicleId);
            if (mechanicId == null || mechanicId.isEmpty()) {
                ps.setNull(4, java.sql.Types.VARCHAR);
            } else {
                ps.setString(4, mechanicId);
            }
            ps.setString(5, serviceId);
            ps.setString(6, bookingDate);
            ps.setString(7, problemDescription == null ? "" : problemDescription.trim());
            ps.setString(8, status);
            ps.executeUpdate();
            return OperationResult.ok("Booking created successfully with ID " + newId + ".", newId);
        } catch (SQLException e) {
            return OperationResult.fail("Database error: " + e.getMessage());
        }
    }

    public OperationResult updateBooking(String bookingId, String customerId, String vehicleId, String mechanicId,
                                          String serviceId, String bookingDate, String problemDescription, String status) {
        ValidationResult r = validate(customerId, vehicleId, serviceId, bookingDate, status);
        if (!r.valid) return OperationResult.fail(r.message);

        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "UPDATE Booking SET customer_id=?, vehicle_id=?, mechanic_id=?, service_id=?, booking_date=?, " +
                "problem_description=?, status=? WHERE booking_id=?")) {
            ps.setString(1, customerId);
            ps.setString(2, vehicleId);
            if (mechanicId == null || mechanicId.isEmpty()) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } else {
                ps.setString(3, mechanicId);
            }
            ps.setString(4, serviceId);
            ps.setString(5, bookingDate);
            ps.setString(6, problemDescription == null ? "" : problemDescription.trim());
            ps.setString(7, status);
            ps.setString(8, bookingId);
            ps.executeUpdate();
            return OperationResult.ok("Booking updated successfully.");
        } catch (SQLException e) {
            return OperationResult.fail("Database error: " + e.getMessage());
        }
    }

    public OperationResult cancelBooking(String bookingId) {
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "DELETE FROM Booking WHERE booking_id=?")) {
            ps.setString(1, bookingId);
            ps.executeUpdate();
            return OperationResult.ok("Booking cancelled successfully.");
        } catch (SQLException e) {
            return OperationResult.fail("Database error: " + e.getMessage());
        }
    }

    public OperationResult updateStatus(String bookingId, String status) {
        if (!Arrays.asList(Booking.STATUS_OPTIONS).contains(status)) {
            return OperationResult.fail("Invalid status.");
        }
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "UPDATE Booking SET status=? WHERE booking_id=?")) {
            ps.setString(1, status);
            ps.setString(2, bookingId);
            ps.executeUpdate();
            return OperationResult.ok("Status updated.");
        } catch (SQLException e) {
            return OperationResult.fail("Database error: " + e.getMessage());
        }
    }

    public List<Booking> getAll() {
        return runQuery(JOIN_SQL + " ORDER BY Booking.booking_date DESC, Booking.booking_id DESC", null);
    }

    public List<Booking> search(String keyword) {
        String like = "%" + keyword.trim() + "%";
        String sql = JOIN_SQL + " WHERE Booking.booking_id LIKE ? OR Customer.full_name LIKE ? OR " +
                     "Vehicle.vehicle_number LIKE ? OR Booking.status LIKE ? " +
                     "ORDER BY Booking.booking_date DESC, Booking.booking_id DESC";
        return runQuery(sql, new String[]{like, like, like, like});
    }

    public List<Booking> getByStatus(String status) {
        String sql = JOIN_SQL + " WHERE Booking.status = ? ORDER BY Booking.booking_date DESC, Booking.booking_id DESC";
        return runQuery(sql, new String[]{status});
    }

    public Booking getById(String bookingId) {
        String sql = JOIN_SQL + " WHERE Booking.booking_id = ?";
        List<Booking> rows = runQuery(sql, new String[]{bookingId});
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int getTodayBookingsCount() {
        String today = LocalDate.now().toString();
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT COUNT(*) c FROM Booking WHERE booking_date=?")) {
            ps.setString(1, today);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("c") : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int countByStatus(String status) {
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT COUNT(*) c FROM Booking WHERE status=?")) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("c") : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasBill(String bookingId) {
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT bill_id FROM Bill WHERE booking_id=?")) {
            ps.setString(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Booking> runQuery(String sql, String[] params) {
        List<Booking> result = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setString(i + 1, params[i]);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Booking(
                        rs.getString("booking_id"),
                        rs.getString("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("vehicle_id"),
                        rs.getString("vehicle_number"),
                        rs.getString("mechanic_id"),
                        rs.getString("mechanic_name"),
                        rs.getString("service_id"),
                        rs.getString("service_name"),
                        rs.getDouble("service_price"),
                        rs.getString("booking_date"),
                        rs.getString("problem_description"),
                        rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
