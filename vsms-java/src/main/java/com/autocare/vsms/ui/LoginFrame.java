package com.autocare.vsms.ui;

import com.autocare.vsms.controllers.AuthController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

/**
 * Login screen: username, password, login button, forgot password, exit.
 */
public class LoginFrame extends JFrame {

    private final AuthController auth = new AuthController();
    private final Consumer<AuthController.AdminInfo> onLoginSuccess;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;

    public LoginFrame(Consumer<AuthController.AdminInfo> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;

        setTitle("AutoCare - Vehicle Service Management System | Login");
        setSize(980, 600);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Theme.BG_MAIN);

        buildUi();
    }

    private void buildUi() {
        setLayout(new GridLayout(1, 2));

        add(buildBrandingPanel());
        add(buildFormPanel());

        getRootPane().setDefaultButton(null);
    }

    private JPanel buildBrandingPanel() {
        JPanel left = new JPanel(new GridBagLayout());
        left.setBackground(Theme.PRIMARY);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(Theme.PRIMARY);

        JLabel icon = new JLabel("\uD83D\uDE97");
        icon.setFont(new Font(Theme.FONT_FAMILY, Font.PLAIN, 50));
        icon.setForeground(Color.WHITE);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appName = new JLabel("AutoCare");
        appName.setFont(new Font(Theme.FONT_FAMILY, Font.BOLD, 28));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Vehicle Service Management System");
        subtitle.setFont(new Font(Theme.FONT_FAMILY, Font.PLAIN, 13));
        subtitle.setForeground(new Color(0xdb, 0xe8, 0xff));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel description = new JLabel("<html><center>Manage customers, vehicles, bookings, billing<br>and reports — all in one place.</center></html>");
        description.setFont(new Font(Theme.FONT_FAMILY, Font.PLAIN, 12));
        description.setForeground(new Color(0xc7, 0xdc, 0xff));
        description.setAlignmentX(Component.CENTER_ALIGNMENT);
        description.setBorder(new EmptyBorder(20, 0, 0, 0));

        inner.add(icon);
        inner.add(Box.createVerticalStrut(10));
        inner.add(appName);
        inner.add(Box.createVerticalStrut(4));
        inner.add(subtitle);
        inner.add(description);

        left.add(inner);
        return left;
    }

    private JPanel buildFormPanel() {
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Theme.BG_MAIN);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Theme.BG_MAIN);
        form.setPreferredSize(new Dimension(360, 420));

        JLabel title = new JLabel("Welcome Back");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to continue to your dashboard");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(0, 0, 24, 0));

        usernameField = new JTextField();
        usernameField.setFont(Theme.FONT_NORMAL);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        passwordField = new JPasswordField();
        passwordField.setFont(Theme.FONT_NORMAL);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        passwordField.addActionListener(this::handleLogin);

        JCheckBox showPassword = new JCheckBox("Show password");
        showPassword.setBackground(Theme.BG_MAIN);
        showPassword.setFont(Theme.FONT_SMALL);
        showPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPassword.addActionListener(e ->
            passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : '\u2022'));
        passwordField.setEchoChar('\u2022');

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Theme.DANGER);
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginButton = Widgets.primaryButton("Login");
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginButton.addActionListener(this::handleLogin);

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setBackground(Theme.BG_MAIN);
        bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JButton forgotLink = new JButton("Forgot Password?");
        forgotLink.setBorderPainted(false);
        forgotLink.setContentAreaFilled(false);
        forgotLink.setForeground(Theme.PRIMARY);
        forgotLink.setFont(Theme.FONT_SMALL);
        forgotLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotLink.addActionListener(e -> new ForgotPasswordDialog(this, auth).setVisible(true));

        JButton exitButton = Widgets.secondaryButton("Exit");
        exitButton.addActionListener(e -> dispose());

        bottomRow.add(forgotLink, BorderLayout.WEST);
        bottomRow.add(exitButton, BorderLayout.EAST);

        JLabel hint = new JLabel("Default login → admin / admin123");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setBorder(new EmptyBorder(20, 0, 0, 0));

        form.add(title);
        form.add(subtitle);
        form.add(Widgets.labeledField("Username", usernameField));
        form.add(Box.createVerticalStrut(14));
        form.add(Widgets.labeledField("Password", passwordField));
        form.add(Box.createVerticalStrut(4));
        showPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(showPassword);
        form.add(Box.createVerticalStrut(4));
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(10));
        form.add(loginButton);
        form.add(Box.createVerticalStrut(8));
        form.add(bottomRow);
        form.add(hint);

        right.add(form);
        return right;
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        AuthController.AdminInfo[] holder = new AuthController.AdminInfo[1];
        var result = auth.login(username, password, holder);
        if (!result.success) {
            errorLabel.setText(result.message);
            return;
        }
        errorLabel.setText(" ");
        dispose();
        onLoginSuccess.accept(holder[0]);
    }
}
