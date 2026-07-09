package com.autocare.vsms;

import com.autocare.vsms.controllers.AuthController;
import com.autocare.vsms.database.DatabaseManager;
import com.autocare.vsms.ui.LoginFrame;
import com.autocare.vsms.ui.MainFrame;

import javax.swing.*;

/**
 * Entry point for the AutoCare Vehicle Service Management System.
 *
 * Run with:   java -jar vsms.jar
 * or during development:  mvn compile exec:java -Dexec.mainClass=com.autocare.vsms.Main
 */
public class Main {

    public static void main(String[] args) {
        // Initialize (and seed, on first run) the database before showing any UI.
        DatabaseManager.getInstance();

        // Use the system's native look and feel where available, for a more
        // professional appearance on Windows/macOS/Linux.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall back to the default cross-platform look and feel
        }

        SwingUtilities.invokeLater(Main::showLogin);
    }

    private static void showLogin() {
        LoginFrame loginFrame = new LoginFrame(Main::showMainWindow);
        loginFrame.setVisible(true);
    }

    private static void showMainWindow(AuthController.AdminInfo admin) {
        MainFrame mainFrame = new MainFrame(admin, Main::showLogin);
        mainFrame.setVisible(true);
    }
}
