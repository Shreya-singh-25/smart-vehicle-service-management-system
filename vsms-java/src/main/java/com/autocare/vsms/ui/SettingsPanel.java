package com.autocare.vsms.ui;

import com.autocare.vsms.controllers.AuthController;
import com.autocare.vsms.controllers.OperationResult;
import com.autocare.vsms.utils.BackupRestore;
import com.autocare.vsms.utils.Validators;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

public class SettingsPanel extends JPanel {

    private final MainFrame mainFrame;
    private final AuthController auth = new AuthController();

    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel backupInfoLabel;

    public SettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_MAIN);

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Theme.BG_MAIN);
        wrapper.add(buildPasswordCard());
        wrapper.add(Box.createVerticalStrut(14));
        wrapper.add(buildBackupCard());
        wrapper.add(Box.createVerticalStrut(14));
        wrapper.add(buildLogoutCard());

        add(wrapper, BorderLayout.NORTH);
    }

    private JPanel buildPasswordCard() {
        JPanel card = Widgets.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Change Password");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        oldPasswordField = new JPasswordField();
        newPasswordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();

        oldPasswordField.setMaximumSize(new Dimension(320, 32));
        newPasswordField.setMaximumSize(new Dimension(320, 32));
        confirmPasswordField.setMaximumSize(new Dimension(320, 32));

        JButton updateBtn = Widgets.primaryButton("Update Password");
        updateBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        updateBtn.addActionListener(e -> changePassword());

        JPanel oldWrap = Widgets.labeledField("Current Password", oldPasswordField);
        JPanel newWrap = Widgets.labeledField("New Password", newPasswordField);
        JPanel confirmWrap = Widgets.labeledField("Confirm New Password", confirmPasswordField);
        oldWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        newWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmWrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(oldWrap);
        card.add(Box.createVerticalStrut(8));
        card.add(newWrap);
        card.add(Box.createVerticalStrut(8));
        card.add(confirmWrap);
        card.add(Box.createVerticalStrut(10));
        card.add(updateBtn);

        return card;
    }

    private JPanel buildBackupCard() {
        JPanel card = Widgets.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Database Backup & Restore");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel subtitle = new JLabel("Create a backup copy of your data, or restore from a previous backup.");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonRow.setBackground(Theme.BG_CARD);
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton backupBtn = Widgets.primaryButton("\uD83D\uDCBE Backup Database");
        backupBtn.addActionListener(e -> backupDatabase());
        JButton restoreBtn = Widgets.secondaryButton("\u267B Restore Database");
        restoreBtn.addActionListener(e -> restoreDatabase());
        buttonRow.add(backupBtn);
        buttonRow.add(restoreBtn);

        backupInfoLabel = new JLabel();
        backupInfoLabel.setFont(Theme.FONT_SMALL);
        backupInfoLabel.setForeground(Theme.TEXT_MUTED);
        backupInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        backupInfoLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        refreshBackupInfo();

        card.add(title);
        card.add(subtitle);
        card.add(buttonRow);
        card.add(backupInfoLabel);

        return card;
    }

    private JPanel buildLogoutCard() {
        JPanel card = Widgets.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Session");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        JButton logoutBtn = Widgets.dangerButton("\uD83D\uDEAA Logout");
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.addActionListener(e -> mainFrame.logout());

        card.add(title);
        card.add(logoutBtn);
        return card;
    }

    private void refreshBackupInfo() {
        List<File> backups = BackupRestore.listBackups();
        if (backups.isEmpty()) {
            backupInfoLabel.setText("No backups created yet.");
        } else {
            backupInfoLabel.setText(backups.size() + " backup(s) available. Most recent: " + backups.get(0).getName());
        }
    }

    private void changePassword() {
        String username = mainFrame.getAdmin().username;
        String oldPw = new String(oldPasswordField.getPassword());
        String newPw = new String(newPasswordField.getPassword());
        String confirmPw = new String(confirmPasswordField.getPassword());

        var validation = Validators.validatePassword(newPw, 4);
        if (!validation.valid) {
            Widgets.showError(this, validation.message);
            return;
        }
        if (!newPw.equals(confirmPw)) {
            Widgets.showError(this, "New password and confirmation do not match.");
            return;
        }

        OperationResult r = auth.changePassword(username, oldPw, newPw);
        if (r.success) {
            Widgets.showSuccess(this, r.message);
            oldPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
        } else {
            Widgets.showError(this, r.message);
        }
    }

    private void backupDatabase() {
        if (Widgets.confirm(this, "Backup Database", "Create a new backup of the current database?")) {
            String path = BackupRestore.backupDatabase();
            Widgets.showSuccess(this, "Database backed up successfully to:\n" + path);
            refreshBackupInfo();
        }
    }

    private void restoreDatabase() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a backup file to restore");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Database files (*.db)", "db"));
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        if (Widgets.confirm(this, "Restore Database",
                "Restoring will overwrite all current data with the selected backup.\n" +
                "This action cannot be undone. Continue?")) {
            boolean success = BackupRestore.restoreDatabase(chooser.getSelectedFile().getAbsolutePath());
            if (success) {
                Widgets.showSuccess(this, "Database restored successfully. Please restart the application.");
            } else {
                Widgets.showError(this, "Failed to restore database. File not found.");
            }
        }
    }
}
