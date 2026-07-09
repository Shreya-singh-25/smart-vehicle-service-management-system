package com.autocare.vsms.controllers;

import com.autocare.vsms.database.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Report generation queries. Each report method returns a list of String[]
 * rows already formatted for display, along with a fixed set of headers
 * accessible via the corresponding *_HEADERS constant.
 */
public class ReportController {

    private final DatabaseManager db = DatabaseManager.getInstance();

    public static final String[] BOOKING_HEADERS =
        {"Booking ID", "Customer", "Vehicle", "Service", "Price", "Date", "Status", "Mechanic"};
    public static final String[] REVENUE_HEADERS =
        {"Bill ID", "Customer", "Vehicle", "Total (Rs.)", "Bill Date"};
    public static final String[] CUSTOMER_HEADERS =
        {"Customer ID", "Name", "Mobile", "Vehicles", "Bookings"};
    public static final String[] STATUS_HEADERS =
        {"Booking ID", "Customer", "Vehicle", "Service", "Date", "Status", "Mechanic"};

    private static final String BOOKING_JOIN_SQL = """
        SELECT Booking.booking_id, Customer.full_name AS customer_name, Vehicle.vehicle_number,
               Service.service_name, Service.price, Booking.booking_date, Booking.status,
               Mechanic.full_name AS mechanic_name
        FROM Booking
        JOIN Customer ON Booking.customer_id = Customer.customer_id
        JOIN Vehicle ON Booking.vehicle_id = Vehicle.vehicle_id
        JOIN Service ON Booking.service_id = Service.service_id
        LEFT JOIN Mechanic ON Booking.mechanic_id = Mechanic.mechanic_id
        """;

    private List<String[]> bookingsBetween(String startDate, String endDate) {
        List<String[]> rows = new ArrayList<>();
        String sql = BOOKING_JOIN_SQL + " WHERE Booking.booking_date BETWEEN ? AND ? ORDER BY Booking.booking_date DESC";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, startDate);
            ps.setString(2, endDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                        rs.getString("booking_id"), rs.getString("customer_name"), rs.getString("vehicle_number"),
                        rs.getString("service_name"), String.format("%.2f", rs.getDouble("price")),
                        rs.getString("booking_date"), rs.getString("status"),
                        rs.getString("mechanic_name") == null ? "-" : rs.getString("mechanic_name")
                    });
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rows;
    }

    public List<String[]> dailyReport(String date) {
        String d = (date == null || date.isEmpty()) ? LocalDate.now().toString() : date;
        return bookingsBetween(d, d);
    }

    public List<String[]> weeklyReport(String endDate) {
        LocalDate end = (endDate == null || endDate.isEmpty()) ? LocalDate.now() : LocalDate.parse(endDate);
        LocalDate start = end.minusDays(6);
        return bookingsBetween(start.toString(), end.toString());
    }

    public List<String[]> monthlyReport(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return bookingsBetween(start.toString(), end.toString());
    }

    public List<String[]> revenueReport(String startDate, String endDate) {
        List<String[]> rows = new ArrayList<>();
        String sql;
        boolean hasRange = startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty();
        if (hasRange) {
            sql = """
                SELECT Bill.bill_id, Bill.bill_date, Bill.total_amount, Customer.full_name AS customer_name,
                       Vehicle.vehicle_number
                FROM Bill
                JOIN Booking ON Bill.booking_id = Booking.booking_id
                JOIN Customer ON Booking.customer_id = Customer.customer_id
                JOIN Vehicle ON Booking.vehicle_id = Vehicle.vehicle_id
                WHERE date(Bill.bill_date) BETWEEN ? AND ?
                ORDER BY Bill.bill_date DESC
                """;
        } else {
            sql = """
                SELECT Bill.bill_id, Bill.bill_date, Bill.total_amount, Customer.full_name AS customer_name,
                       Vehicle.vehicle_number
                FROM Bill
                JOIN Booking ON Bill.booking_id = Booking.booking_id
                JOIN Customer ON Booking.customer_id = Customer.customer_id
                JOIN Vehicle ON Booking.vehicle_id = Vehicle.vehicle_id
                ORDER BY Bill.bill_date DESC
                """;
        }
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            if (hasRange) {
                ps.setString(1, startDate);
                ps.setString(2, endDate);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                        rs.getString("bill_id"), rs.getString("customer_name"), rs.getString("vehicle_number"),
                        String.format("%.2f", rs.getDouble("total_amount")), rs.getString("bill_date")
                    });
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rows;
    }

    public List<String[]> customerReport() {
        List<String[]> rows = new ArrayList<>();
        String sql = """
            SELECT Customer.customer_id, Customer.full_name, Customer.mobile_number,
                   COUNT(DISTINCT Vehicle.vehicle_id) AS vehicle_count,
                   COUNT(DISTINCT Booking.booking_id) AS booking_count
            FROM Customer
            LEFT JOIN Vehicle ON Vehicle.owner_id = Customer.customer_id
            LEFT JOIN Booking ON Booking.customer_id = Customer.customer_id
            GROUP BY Customer.customer_id
            ORDER BY Customer.full_name
            """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new String[]{
                    rs.getString("customer_id"), rs.getString("full_name"), rs.getString("mobile_number"),
                    String.valueOf(rs.getInt("vehicle_count")), String.valueOf(rs.getInt("booking_count"))
                });
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rows;
    }

    public List<String[]> pendingServicesReport() {
        return statusReport("Booking.status IN ('Pending', 'In Progress')", "Booking.booking_date");
    }

    public List<String[]> completedServicesReport() {
        return statusReport("Booking.status = 'Completed'", "Booking.booking_date DESC");
    }

    private List<String[]> statusReport(String whereClause, String orderBy) {
        List<String[]> rows = new ArrayList<>();
        String sql = """
            SELECT Booking.booking_id, Customer.full_name AS customer_name, Vehicle.vehicle_number,
                   Service.service_name, Booking.booking_date, Booking.status, Mechanic.full_name AS mechanic_name
            FROM Booking
            JOIN Customer ON Booking.customer_id = Customer.customer_id
            JOIN Vehicle ON Booking.vehicle_id = Vehicle.vehicle_id
            JOIN Service ON Booking.service_id = Service.service_id
            LEFT JOIN Mechanic ON Booking.mechanic_id = Mechanic.mechanic_id
            WHERE """ + whereClause + " ORDER BY " + orderBy;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new String[]{
                    rs.getString("booking_id"), rs.getString("customer_name"), rs.getString("vehicle_number"),
                    rs.getString("service_name"), rs.getString("booking_date"), rs.getString("status"),
                    rs.getString("mechanic_name") == null ? "-" : rs.getString("mechanic_name")
                });
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rows;
    }
}
