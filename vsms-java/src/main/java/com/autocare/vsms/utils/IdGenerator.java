package com.autocare.vsms.utils;

import com.autocare.vsms.database.DatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates human-readable, auto-incrementing IDs for every entity
 * (e.g. CUST005, VEH012, BKG034, BILL021) by inspecting the current
 * max ID already present in the database.
 */
public final class IdGenerator {

    private static final Map<String, String[]> PREFIX_MAP = new HashMap<>();
    static {
        PREFIX_MAP.put("Customer", new String[]{"customer_id", "CUST"});
        PREFIX_MAP.put("Vehicle", new String[]{"vehicle_id", "VEH"});
        PREFIX_MAP.put("Mechanic", new String[]{"mechanic_id", "MECH"});
        PREFIX_MAP.put("Service", new String[]{"service_id", "SRV"});
        PREFIX_MAP.put("Booking", new String[]{"booking_id", "BKG"});
        PREFIX_MAP.put("Bill", new String[]{"bill_id", "BILL"});
    }

    private IdGenerator() { }

    public static String generateId(String tableName) {
        String[] mapping = PREFIX_MAP.get(tableName);
        if (mapping == null) {
            throw new IllegalArgumentException("Unknown table for ID generation: " + tableName);
        }
        String idColumn = mapping[0];
        String prefix = mapping[1];

        int maxNum = 0;
        DatabaseManager db = DatabaseManager.getInstance();
        try (Statement st = db.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT " + idColumn + " FROM " + tableName)) {
            while (rs.next()) {
                String rawId = rs.getString(1);
                if (rawId != null && rawId.startsWith(prefix)) {
                    String suffix = rawId.substring(prefix.length());
                    try {
                        int num = Integer.parseInt(suffix);
                        maxNum = Math.max(maxNum, num);
                    } catch (NumberFormatException ignored) {
                        // skip malformed IDs
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to generate ID for " + tableName, e);
        }

        return String.format("%s%03d", prefix, maxNum + 1);
    }
}
