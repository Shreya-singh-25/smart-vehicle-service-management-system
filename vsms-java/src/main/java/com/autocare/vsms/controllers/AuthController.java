package com.autocare.vsms.controllers;

import com.autocare.vsms.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthController {

    private final DatabaseManager db = DatabaseManager.getInstance();

    public static class AdminInfo {
        public final String username;
        public final String fullName;

        public AdminInfo(String username, String fullName) {
            this.username = username;
            this.fullName = fullName;
        }
    }

    /** Returns an AdminInfo on success, or null on failure (check the message via login(...) overload if needed). */
    public OperationResult login(String username, String password, AdminInfo[] outAdmin) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return OperationResult.fail("Username and password are required.");
        }
        String sql = "SELECT * FROM Admin WHERE username = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return OperationResult.fail("Invalid username or password.");
                }
                String storedHash = rs.getString("password_hash");
                if (!storedHash.equals(DatabaseManager.hashPassword(password))) {
                    return OperationResult.fail("Invalid username or password.");
                }
                outAdmin[0] = new AdminInfo(rs.getString("username"), rs.getString("full_name"));
                return OperationResult.ok("Login successful.");
            }
        } catch (SQLException e) {
            return OperationResult.fail("Database error: " + e.getMessage());
        }
    }

    public String getSecurityQuestion(String username) {
        String sql = "SELECT security_question FROM Admin WHERE username=?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("security_question") : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verifySecurityAnswer(String username, String answer) {
        String sql = "SELECT security_answer FROM Admin WHERE username=?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                String storedHash = rs.getString("security_answer");
                return storedHash.equals(DatabaseManager.hashPassword(answer.trim().toLowerCase()));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void resetPassword(String username, String newPassword) {
        String sql = "UPDATE Admin SET password_hash=? WHERE username=?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, DatabaseManager.hashPassword(newPassword));
            ps.setString(2, username.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public OperationResult changePassword(String username, String oldPassword, String newPassword) {
        AdminInfo[] holder = new AdminInfo[1];
        OperationResult loginResult = login(username, oldPassword, holder);
        if (!loginResult.success) {
            return OperationResult.fail("Current password is incorrect.");
        }
        resetPassword(username, newPassword);
        return OperationResult.ok("Password changed successfully.");
    }
}
