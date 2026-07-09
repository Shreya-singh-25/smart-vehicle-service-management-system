package com.autocare.vsms.ui;

import com.autocare.vsms.controllers.AuthController;
import com.autocare.vsms.controllers.OperationResult;
import com.autocare.vsms.utils.Validators;

import javax.swing.*;
import java.awt.*;

/**
 * Security-question based password reset flow, shown as a modal dialog
 * with three sequential steps.
 */
public class ForgotPasswordDialog extends JDialog {

    private final AuthController auth;
    private String username;
    private String question;

    public ForgotPasswordDialog(Frame owner, AuthController auth) {
        super(owner, "Reset Password", true);
        this.auth = auth;
        setSize(380, 340);
        setLocationRelativeTo(owner);
        setResizable(false);
        buildStep1();
    }

    private void resetContent(JPanel content) {
        getContentPane().removeAll();
        content.setBackground(Theme.BG_CARD);
        content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        setContentPane(content);
        revalidate();
        repaint();
    }

    private void buildStep1() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Forgot Password");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Enter your username to continue.");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(2, 0, 16, 0));

        JTextField usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JButton nextButton = Widgets.primaryButton("Next");
        nextButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        nextButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        nextButton.addActionListener(e -> {
            String uname = usernameField.getText().trim();
            String q = auth.getSecurityQuestion(uname);
            if (q == null) {
                Widgets.showError(this, "No account found with that username.");
                return;
            }
            this.username = uname;
            this.question = q;
            buildStep2();
        });

        content.add(title);
        content.add(subtitle);
        content.add(Widgets.labeledField("Username", usernameField));
        content.add(Box.createVerticalStrut(16));
        content.add(nextButton);

        resetContent(content);
    }

    private void buildStep2() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Security Question");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel questionLabel = new JLabel("<html>" + question + "</html>");
        questionLabel.setFont(Theme.FONT_SMALL);
        questionLabel.setForeground(Theme.TEXT_MUTED);
        questionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        questionLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 16, 0));

        JTextField answerField = new JTextField();
        answerField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JButton verifyButton = Widgets.primaryButton("Verify");
        verifyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        verifyButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        verifyButton.addActionListener(e -> {
            if (!auth.verifySecurityAnswer(username, answerField.getText())) {
                Widgets.showError(this, "Incorrect answer. Please try again.");
                return;
            }
            buildStep3();
        });

        content.add(title);
        content.add(questionLabel);
        content.add(Widgets.labeledField("Your Answer", answerField));
        content.add(Box.createVerticalStrut(16));
        content.add(verifyButton);

        resetContent(content);
    }

    private void buildStep3() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Set New Password");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Choose a new password (min. 4 characters).");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(2, 0, 16, 0));

        JPasswordField newPw = new JPasswordField();
        newPw.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        JPasswordField confirmPw = new JPasswordField();
        confirmPw.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JButton resetButton = Widgets.primaryButton("Reset Password");
        resetButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        resetButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        resetButton.addActionListener(e -> {
            String pw1 = new String(newPw.getPassword());
            String pw2 = new String(confirmPw.getPassword());
            var validation = Validators.validatePassword(pw1, 4);
            if (!validation.valid) {
                Widgets.showError(this, validation.message);
                return;
            }
            if (!pw1.equals(pw2)) {
                Widgets.showError(this, "Passwords do not match.");
                return;
            }
            auth.resetPassword(username, pw1);
            Widgets.showSuccess(this, "Password reset successfully. You can now log in.");
            dispose();
        });

        content.add(title);
        content.add(subtitle);
        content.add(Widgets.labeledField("New Password", newPw));
        content.add(Box.createVerticalStrut(12));
        content.add(Widgets.labeledField("Confirm Password", confirmPw));
        content.add(Box.createVerticalStrut(16));
        content.add(resetButton);

        resetContent(content);
    }
}
