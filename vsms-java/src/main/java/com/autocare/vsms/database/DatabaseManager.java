package com.autocare.vsms.database;

import java.io.File;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Central point of access to the SQLite database. Handles connection setup,
 * schema creation (with proper primary/foreign keys), and first-run sample
 * data seeding. Implemented as a simple singleton so every controller shares
 * one open connection.
 */
public final class DatabaseManager {

    private static DatabaseManager instance;
    private final Connection connection;
    private final String dbPath;

    private DatabaseManager() throws SQLException {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        this.dbPath = Paths.get("data", "vsms.db").toString();
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        createSchema();
        seedData();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            try {
                instance = new DatabaseManager();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to initialize database", e);
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public String getDbPath() {
        return dbPath;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {
            // ignore on shutdown
        }
    }

    // ------------------------------------------------------------------
    // Password hashing (SHA-256) — shared by DatabaseManager seeding and
    // AuthController, kept here to avoid a circular dependency.
    // ------------------------------------------------------------------
    public static String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawPassword.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Unable to hash password", e);
        }
    }

    // ------------------------------------------------------------------
    // Schema
    // ------------------------------------------------------------------
    private void createSchema() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Admin (
                    admin_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    full_name TEXT,
                    security_question TEXT,
                    security_answer TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Customer (
                    customer_id TEXT PRIMARY KEY,
                    full_name TEXT NOT NULL,
                    mobile_number TEXT NOT NULL UNIQUE,
                    email TEXT,
                    address TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Vehicle (
                    vehicle_id TEXT PRIMARY KEY,
                    vehicle_number TEXT NOT NULL UNIQUE,
                    owner_id TEXT NOT NULL,
                    company TEXT,
                    model TEXT,
                    manufacturing_year INTEGER,
                    fuel_type TEXT,
                    vehicle_type TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (owner_id) REFERENCES Customer(customer_id) ON DELETE CASCADE
                )
            """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Mechanic (
                    mechanic_id TEXT PRIMARY KEY,
                    full_name TEXT NOT NULL,
                    mobile_number TEXT NOT NULL UNIQUE,
                    experience_years INTEGER,
                    specialization TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Service (
                    service_id TEXT PRIMARY KEY,
                    service_name TEXT NOT NULL UNIQUE,
                    price REAL NOT NULL,
                    estimated_time TEXT
                )
            """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Booking (
                    booking_id TEXT PRIMARY KEY,
                    customer_id TEXT NOT NULL,
                    vehicle_id TEXT NOT NULL,
                    mechanic_id TEXT,
                    service_id TEXT NOT NULL,
                    booking_date TEXT NOT NULL,
                    problem_description TEXT,
                    status TEXT NOT NULL DEFAULT 'Pending',
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE,
                    FOREIGN KEY (vehicle_id) REFERENCES Vehicle(vehicle_id) ON DELETE CASCADE,
                    FOREIGN KEY (mechanic_id) REFERENCES Mechanic(mechanic_id) ON DELETE SET NULL,
                    FOREIGN KEY (service_id) REFERENCES Service(service_id) ON DELETE RESTRICT
                )
            """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Bill (
                    bill_id TEXT PRIMARY KEY,
                    booking_id TEXT NOT NULL,
                    parts_cost REAL DEFAULT 0,
                    labour_charge REAL DEFAULT 0,
                    tax_percent REAL DEFAULT 18,
                    discount REAL DEFAULT 0,
                    total_amount REAL NOT NULL,
                    bill_date TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE
                )
            """);
        }
    }

    // ------------------------------------------------------------------
    // Sample / seed data (only inserted once, on first run)
    // ------------------------------------------------------------------
    private void seedData() throws SQLException {
        if (count("Admin") == 0) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO Admin (username, password_hash, full_name, security_question, security_answer) " +
                    "VALUES (?,?,?,?,?)")) {
                ps.setString(1, "admin");
                ps.setString(2, hashPassword("admin123"));
                ps.setString(3, "System Administrator");
                ps.setString(4, "What is your favorite color?");
                ps.setString(5, hashPassword("blue"));
                ps.executeUpdate();
            }
        }

        if (count("Service") == 0) {
            String[][] services = {
                {"SRV001", "Oil Change", "799.0", "30 mins"},
                {"SRV002", "Engine Service", "3499.0", "3 hours"},
                {"SRV003", "Brake Repair", "1299.0", "1.5 hours"},
                {"SRV004", "Wheel Alignment", "999.0", "45 mins"},
                {"SRV005", "Battery Replacement", "2499.0", "20 mins"},
                {"SRV006", "Washing", "299.0", "20 mins"},
                {"SRV007", "General Service", "1899.0", "2 hours"},
            };
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO Service (service_id, service_name, price, estimated_time) VALUES (?,?,?,?)")) {
                for (String[] s : services) {
                    ps.setString(1, s[0]);
                    ps.setString(2, s[1]);
                    ps.setDouble(3, Double.parseDouble(s[2]));
                    ps.setString(4, s[3]);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }

        if (count("Customer") == 0) {
            String[][] customers = {
                {"CUST001", "Rohan Sharma", "9876543210", "rohan.sharma@example.com", "12 MG Road, Lucknow"},
                {"CUST002", "Priya Verma", "9876543211", "priya.verma@example.com", "45 Gomti Nagar, Lucknow"},
                {"CUST003", "Amit Singh", "9876543212", "amit.singh@example.com", "8 Hazratganj, Lucknow"},
                {"CUST004", "Sneha Gupta", "9876543213", "sneha.gupta@example.com", "23 Alambagh, Lucknow"},
            };
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO Customer (customer_id, full_name, mobile_number, email, address) VALUES (?,?,?,?,?)")) {
                for (String[] c : customers) {
                    for (int i = 0; i < c.length; i++) {
                        ps.setString(i + 1, c[i]);
                    }
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }

        if (count("Vehicle") == 0) {
            Object[][] vehicles = {
                {"VEH001", "UP32AB1234", "CUST001", "Maruti Suzuki", "Swift", 2020, "Petrol", "Hatchback"},
                {"VEH002", "UP32CD5678", "CUST002", "Honda", "City", 2019, "Petrol", "Sedan"},
                {"VEH003", "UP32EF9012", "CUST003", "Hyundai", "Creta", 2021, "Diesel", "SUV"},
                {"VEH004", "UP32GH3456", "CUST004", "Bajaj", "Pulsar 150", 2022, "Petrol", "Bike"},
            };
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO Vehicle (vehicle_id, vehicle_number, owner_id, company, model, " +
                    "manufacturing_year, fuel_type, vehicle_type) VALUES (?,?,?,?,?,?,?,?)")) {
                for (Object[] v : vehicles) {
                    ps.setString(1, (String) v[0]);
                    ps.setString(2, (String) v[1]);
                    ps.setString(3, (String) v[2]);
                    ps.setString(4, (String) v[3]);
                    ps.setString(5, (String) v[4]);
                    ps.setInt(6, (Integer) v[5]);
                    ps.setString(7, (String) v[6]);
                    ps.setString(8, (String) v[7]);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }

        if (count("Mechanic") == 0) {
            Object[][] mechanics = {
                {"MECH001", "Ramesh Yadav", "9123456780", 8, "Engine Specialist"},
                {"MECH002", "Suresh Kumar", "9123456781", 5, "Brake & Suspension"},
                {"MECH003", "Vikram Rathi", "9123456782", 10, "Electrical Systems"},
            };
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO Mechanic (mechanic_id, full_name, mobile_number, experience_years, specialization) " +
                    "VALUES (?,?,?,?,?)")) {
                for (Object[] m : mechanics) {
                    ps.setString(1, (String) m[0]);
                    ps.setString(2, (String) m[1]);
                    ps.setString(3, (String) m[2]);
                    ps.setInt(4, (Integer) m[3]);
                    ps.setString(5, (String) m[4]);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }

        if (count("Booking") == 0) {
            String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
            String twoDaysAgo = LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE);

            String[][] bookings = {
                {"BKG001", "CUST001", "VEH001", "MECH001", "SRV001", today, "Regular oil change requested.", "Pending"},
                {"BKG002", "CUST002", "VEH002", "MECH002", "SRV003", today, "Brakes squeaking on braking.", "In Progress"},
                {"BKG003", "CUST003", "VEH003", "MECH003", "SRV007", twoDaysAgo, "Annual general service.", "Completed"},
                {"BKG004", "CUST004", "VEH004", "MECH001", "SRV006", yesterday, "Full wash and polish.", "Completed"},
            };
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO Booking (booking_id, customer_id, vehicle_id, mechanic_id, service_id, " +
                    "booking_date, problem_description, status) VALUES (?,?,?,?,?,?,?,?)")) {
                for (String[] b : bookings) {
                    for (int i = 0; i < b.length; i++) {
                        ps.setString(i + 1, b[i]);
                    }
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }

        if (count("Bill") == 0) {
            int billNo = 1;
            try (Statement st = connection.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT booking_id, service_id FROM Booking WHERE status='Completed'")) {
                while (rs.next()) {
                    String bookingId = rs.getString("booking_id");
                    String serviceId = rs.getString("service_id");
                    double servicePrice = 500.0;
                    try (PreparedStatement psPrice = connection.prepareStatement(
                            "SELECT price FROM Service WHERE service_id=?")) {
                        psPrice.setString(1, serviceId);
                        try (ResultSet priceRs = psPrice.executeQuery()) {
                            if (priceRs.next()) {
                                servicePrice = priceRs.getDouble("price");
                            }
                        }
                    }
                    double partsCost = 200.0;
                    double labour = servicePrice;
                    double taxPercent = 18.0;
                    double discount = 0.0;
                    double subtotal = partsCost + labour;
                    double total = Math.round((subtotal + (subtotal * taxPercent / 100.0) - discount) * 100.0) / 100.0;

                    try (PreparedStatement psBill = connection.prepareStatement(
                            "INSERT INTO Bill (bill_id, booking_id, parts_cost, labour_charge, tax_percent, " +
                            "discount, total_amount) VALUES (?,?,?,?,?,?,?)")) {
                        psBill.setString(1, String.format("BILL%03d", billNo));
                        psBill.setString(2, bookingId);
                        psBill.setDouble(3, partsCost);
                        psBill.setDouble(4, labour);
                        psBill.setDouble(5, taxPercent);
                        psBill.setDouble(6, discount);
                        psBill.setDouble(7, total);
                        psBill.executeUpdate();
                    }
                    billNo++;
                }
            }
        }
    }

    private int count(String table) throws SQLException {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) c FROM " + table)) {
            return rs.next() ? rs.getInt("c") : 0;
        }
    }
}
