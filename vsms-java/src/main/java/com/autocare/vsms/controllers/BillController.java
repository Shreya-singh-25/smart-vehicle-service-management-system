package com.autocare.vsms.controllers;

import com.autocare.vsms.database.DatabaseManager;
import com.autocare.vsms.models.Bill;
import com.autocare.vsms.utils.IdGenerator;
import com.autocare.vsms.utils.Validators;
import com.autocare.vsms.utils.Validators.ValidationResult;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BillController {

    private final DatabaseManager db = DatabaseManager.getInstance();

    public static class BookingOption {
        public final String bookingId;
        public final String label;

        public BookingOption(String bookingId, String label) {
            this.bookingId = bookingId;
            this.label = label;
        }

        @Override
        public String toString() { return label; }
    }

    private ValidationResult validate(String bookingId, String partsCost, String labourCharge,
                                       String taxPercent, String discount) {
        ValidationResult r = Validators.notEmpty(bookingId, "Booking");
        if (!r.valid) return r;
        r = Validators.validatePositiveNumber(partsCost, "Parts cost", true);
        if (!r.valid) return r;
        r = Validators.validatePositiveNumber(labourCharge, "Labour charge", true);
        if (!r.valid) return r;
        r = Validators.validatePositiveNumber(taxPercent, "Tax (GST %)", true);
        if (!r.valid) return r;
        r = Validators.validatePositiveNumber(discount, "Discount", true);
        if (!r.valid) return r;
        return Validators.ok();
    }

    public OperationResult createBill(String bookingId, String partsCost, String labourCharge,
                                       String taxPercent, String discount) {
        ValidationResult r = validate(bookingId, partsCost, labourCharge, taxPercent, discount);
        if (!r.valid) return OperationResult.fail(r.message);

        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT bill_id FROM Bill WHERE booking_id=?")) {
            ps.setString(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return OperationResult.fail("A bill already exists for this booking.");
                }
            }
        } catch (SQLException e) {
            return OperationResult.fail("Database error: " + e.getMessage());
        }

        double parts = Double.parseDouble(partsCost.trim());
        double labour = Double.parseDouble(labourCharge.trim());
        double tax = Double.parseDouble(taxPercent.trim());
        double disc = Double.parseDouble(discount.trim());
        double total = Bill.calculateTotal(parts, labour, tax, disc);
        String newId = IdGenerator.generateId("Bill");

        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "INSERT INTO Bill (bill_id, booking_id, parts_cost, labour_charge, tax_percent, discount, " +
                "total_amount) VALUES (?,?,?,?,?,?,?)")) {
            ps.setString(1, newId);
            ps.setString(2, bookingId);
            ps.setDouble(3, parts);
            ps.setDouble(4, labour);
            ps.setDouble(5, tax);
            ps.setDouble(6, disc);
            ps.setDouble(7, total);
            ps.executeUpdate();
        } catch (SQLException e) {
            return OperationResult.fail("Database error: " + e.getMessage());
        }

        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "UPDATE Booking SET status='Completed' WHERE booking_id=?")) {
            ps.setString(1, bookingId);
            ps.executeUpdate();
        } catch (SQLException e) {
            return OperationResult.fail("Database error: " + e.getMessage());
        }

        return OperationResult.ok("Bill generated successfully with ID " + newId + ".", newId);
    }

    public Bill getBillDetails(String billId) {
        String sql = """
            SELECT Bill.*, Booking.booking_id AS bk_id, Booking.status, Customer.full_name AS customer_name,
                   Customer.mobile_number AS customer_mobile, Vehicle.vehicle_number,
                   Vehicle.company AS vehicle_company, Vehicle.model AS vehicle_model,
                   Service.service_name, Mechanic.full_name AS mechanic_name
            FROM Bill
            JOIN Booking ON Bill.booking_id = Booking.booking_id
            JOIN Customer ON Booking.customer_id = Customer.customer_id
            JOIN Vehicle ON Booking.vehicle_id = Vehicle.vehicle_id
            JOIN Service ON Booking.service_id = Service.service_id
            LEFT JOIN Mechanic ON Booking.mechanic_id = Mechanic.mechanic_id
            WHERE Bill.bill_id = ?
            """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Bill bill = new Bill(
                    rs.getString("bill_id"),
                    rs.getString("bk_id"),
                    rs.getDouble("parts_cost"),
                    rs.getDouble("labour_charge"),
                    rs.getDouble("tax_percent"),
                    rs.getDouble("discount"),
                    rs.getDouble("total_amount"),
                    rs.getString("bill_date")
                );
                bill.setCustomerName(rs.getString("customer_name"));
                bill.setCustomerMobile(rs.getString("customer_mobile"));
                bill.setVehicleNumber(rs.getString("vehicle_number"));
                bill.setVehicleCompany(rs.getString("vehicle_company"));
                bill.setVehicleModel(rs.getString("vehicle_model"));
                bill.setServiceName(rs.getString("service_name"));
                bill.setMechanicName(rs.getString("mechanic_name"));
                bill.setStatus(rs.getString("status"));
                return bill;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Bill> getAllBills() {
        String sql = """
            SELECT Bill.*, Customer.full_name AS customer_name, Vehicle.vehicle_number
            FROM Bill
            JOIN Booking ON Bill.booking_id = Booking.booking_id
            JOIN Customer ON Booking.customer_id = Customer.customer_id
            JOIN Vehicle ON Booking.vehicle_id = Vehicle.vehicle_id
            ORDER BY Bill.bill_date DESC
            """;
        return runBillListQuery(sql, null);
    }

    public List<Bill> search(String keyword) {
        String like = "%" + keyword.trim() + "%";
        String sql = """
            SELECT Bill.*, Customer.full_name AS customer_name, Vehicle.vehicle_number
            FROM Bill
            JOIN Booking ON Bill.booking_id = Booking.booking_id
            JOIN Customer ON Booking.customer_id = Customer.customer_id
            JOIN Vehicle ON Booking.vehicle_id = Vehicle.vehicle_id
            WHERE Bill.bill_id LIKE ? OR Customer.full_name LIKE ? OR Vehicle.vehicle_number LIKE ?
            ORDER BY Bill.bill_date DESC
            """;
        return runBillListQuery(sql, new String[]{like, like, like});
    }

    public double totalRevenue() {
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT COALESCE(SUM(total_amount),0) total FROM Bill");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble("total") : 0.0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<BookingOption> getCompletedBookingsWithoutBill() {
        List<BookingOption> result = new ArrayList<>();
        String sql = """
            SELECT Booking.booking_id, Customer.full_name AS customer_name, Vehicle.vehicle_number,
                   Service.service_name
            FROM Booking
            JOIN Customer ON Booking.customer_id = Customer.customer_id
            JOIN Vehicle ON Booking.vehicle_id = Vehicle.vehicle_id
            JOIN Service ON Booking.service_id = Service.service_id
            LEFT JOIN Bill ON Booking.booking_id = Bill.booking_id
            WHERE Bill.bill_id IS NULL
            ORDER BY Booking.booking_date DESC
            """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String label = rs.getString("booking_id") + " - " + rs.getString("customer_name") +
                               " - " + rs.getString("vehicle_number") + " - " + rs.getString("service_name");
                result.add(new BookingOption(rs.getString("booking_id"), label));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private List<Bill> runBillListQuery(String sql, String[] params) {
        List<Bill> result = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setString(i + 1, params[i]);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Bill bill = new Bill(
                        rs.getString("bill_id"),
                        rs.getString("booking_id"),
                        rs.getDouble("parts_cost"),
                        rs.getDouble("labour_charge"),
                        rs.getDouble("tax_percent"),
                        rs.getDouble("discount"),
                        rs.getDouble("total_amount"),
                        rs.getString("bill_date")
                    );
                    bill.setCustomerName(rs.getString("customer_name"));
                    bill.setVehicleNumber(rs.getString("vehicle_number"));
                    result.add(bill);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
