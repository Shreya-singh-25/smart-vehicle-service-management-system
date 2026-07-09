package com.autocare.vsms.controllers;

import com.autocare.vsms.database.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DashboardController {

    private final DatabaseManager db = DatabaseManager.getInstance();

    public static class Summary {
        public int totalCustomers;
        public int totalVehicles;
        public int totalMechanics;
        public int todaysBookings;
        public int pendingServices;
        public int completedServices;
        public double totalRevenue;
    }

    public static class RecentBooking {
        public final String bookingId;
        public final String customerName;
        public final String vehicleNumber;
        public final String serviceName;
        public final String bookingDate;
        public final String status;

        public RecentBooking(String bookingId, String customerName, String vehicleNumber,
                              String serviceName, String bookingDate, String status) {
            this.bookingId = bookingId;
            this.customerName = customerName;
            this.vehicleNumber = vehicleNumber;
            this.serviceName = serviceName;
            this.bookingDate = bookingDate;
            this.status = status;
        }
    }

    public Summary getSummary() {
        Summary s = new Summary();
        String today = LocalDate.now().toString();
        s.totalCustomers = singleCount("SELECT COUNT(*) c FROM Customer", null);
        s.totalVehicles = singleCount("SELECT COUNT(*) c FROM Vehicle", null);
        s.totalMechanics = singleCount("SELECT COUNT(*) c FROM Mechanic", null);
        s.todaysBookings = singleCount("SELECT COUNT(*) c FROM Booking WHERE booking_date=?", today);
        s.pendingServices = singleCount("SELECT COUNT(*) c FROM Booking WHERE status IN ('Pending','In Progress')", null);
        s.completedServices = singleCount("SELECT COUNT(*) c FROM Booking WHERE status='Completed'", null);

        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT COALESCE(SUM(total_amount),0) t FROM Bill");
             ResultSet rs = ps.executeQuery()) {
            s.totalRevenue = rs.next() ? rs.getDouble("t") : 0.0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return s;
    }

    public List<RecentBooking> getRecentBookings(int limit) {
        List<RecentBooking> result = new ArrayList<>();
        String sql = """
            SELECT Booking.booking_id, Customer.full_name AS customer_name, Vehicle.vehicle_number,
                   Service.service_name, Booking.booking_date, Booking.status
            FROM Booking
            JOIN Customer ON Booking.customer_id = Customer.customer_id
            JOIN Vehicle ON Booking.vehicle_id = Vehicle.vehicle_id
            JOIN Service ON Booking.service_id = Service.service_id
            ORDER BY Booking.created_at DESC
            LIMIT ?
            """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new RecentBooking(
                        rs.getString("booking_id"), rs.getString("customer_name"),
                        rs.getString("vehicle_number"), rs.getString("service_name"),
                        rs.getString("booking_date"), rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private int singleCount(String sql, String param) {
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            if (param != null) {
                ps.setString(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("c") : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
