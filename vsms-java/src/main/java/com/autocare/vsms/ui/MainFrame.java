package com.autocare.vsms.ui;

import com.autocare.vsms.controllers.AuthController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Main application shell shown after login: sidebar navigation, top bar,
 * and a content area (CardLayout) that swaps between the different
 * module panels.
 */
public class MainFrame extends JFrame {

    private final AuthController.AdminInfo admin;
    private final Runnable onLogout;

    private final JPanel contentArea = new JPanel(new CardLayout());
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private final Map<String, Supplier<JPanel>> viewFactories = new LinkedHashMap<>();
    private final Map<String, JPanel> loadedViews = new LinkedHashMap<>();
    private JLabel pageTitleLabel;
    private JLabel clockLabel;
    private String activeKey = "dashboard";

    public MainFrame(AuthController.AdminInfo admin, Runnable onLogout) {
        this.admin = admin;
        this.onLogout = onLogout;

        setTitle("AutoCare - Vehicle Service Management System");
        setSize(1280, 760);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Theme.BG_MAIN);

        registerViewFactories();
        buildLayout();
        showView("dashboard");
        startClock();
    }

    private void registerViewFactories() {
        viewFactories.put("dashboard", () -> new DashboardPanel(this));
        viewFactories.put("customers", CustomerPanel::new);
        viewFactories.put("vehicles", VehiclePanel::new);
        viewFactories.put("mechanics", MechanicPanel::new);
        viewFactories.put("services", ServicePanel::new);
        viewFactories.put("bookings", BookingPanel::new);
        viewFactories.put("billing", BillingPanel::new);
        viewFactories.put("reports", ReportsPanel::new);
        viewFactories.put("search", SearchPanel::new);
        viewFactories.put("settings", () -> new SettingsPanel(this));
    }

    private void buildLayout() {
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Theme.BG_MAIN);
        right.add(buildTopBar(), BorderLayout.NORTH);

        contentArea.setBackground(Theme.BG_MAIN);
        contentArea.setBorder(new EmptyBorder(20, 20, 20, 20));
        right.add(contentArea, BorderLayout.CENTER);

        add(right, BorderLayout.CENTER);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBorder(new EmptyBorder(20, 16, 16, 16));

        JLabel logo = new JLabel("\uD83D\uDE97 AutoCare");
        logo.setFont(new Font(Theme.FONT_FAMILY, Font.BOLD, 17));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoSub = new JLabel("Service Management");
        logoSub.setFont(Theme.FONT_SMALL);
        logoSub.setForeground(new Color(0x9d, 0xb4, 0xd9));
        logoSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoSub.setBorder(new EmptyBorder(0, 0, 16, 0));

        sidebar.add(logo);
        sidebar.add(logoSub);

        String[][] navItems = {
            {"dashboard", "\uD83C\uDFE0  Dashboard"},
            {"customers", "\uD83D\uDC65  Customers"},
            {"vehicles", "\uD83D\uDE97  Vehicles"},
            {"mechanics", "\uD83D\uDD27  Mechanics"},
            {"services", "\uD83D\uDEE0  Services"},
            {"bookings", "\uD83D\uDCC5  Bookings"},
            {"billing", "\uD83E\uDDFE  Billing"},
            {"reports", "\uD83D\uDCCA  Reports"},
            {"search", "\uD83D\uDD0D  Global Search"},
            {"settings", "\u2699  Settings"},
        };

        for (String[] item : navItems) {
            String key = item[0];
            JButton button = new JButton(item[1]);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setBackground(Theme.BG_SIDEBAR);
            button.setForeground(Color.WHITE);
            button.setFont(Theme.FONT_NORMAL);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setContentAreaFilled(true);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            button.addActionListener(e -> showView(key));
            sidebar.add(Box.createVerticalStrut(2));
            sidebar.add(button);
            navButtons.put(key, button);
        }

        sidebar.add(Box.createVerticalGlue());

        JLabel userLabel = new JLabel("\uD83D\uDC64 " +
            (admin.fullName != null && !admin.fullName.isEmpty() ? admin.fullName : admin.username));
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(Theme.FONT_BOLD);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton logoutButton = new JButton("\uD83D\uDEAA Logout");
        logoutButton.setBorderPainted(false);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setForeground(new Color(0xff, 0x80, 0x80));
        logoutButton.setFont(Theme.FONT_NORMAL);
        logoutButton.setHorizontalAlignment(SwingConstants.LEFT);
        logoutButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutButton.addActionListener(e -> logout());

        sidebar.add(userLabel);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(logoutButton);

        return sidebar;
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Theme.BG_CARD);
        topBar.setPreferredSize(new Dimension(0, 56));
        topBar.setBorder(new EmptyBorder(0, 20, 0, 20));

        pageTitleLabel = new JLabel("Dashboard");
        pageTitleLabel.setFont(Theme.FONT_SUBTITLE);
        pageTitleLabel.setForeground(Theme.TEXT_DARK);

        clockLabel = new JLabel();
        clockLabel.setFont(Theme.FONT_SMALL);
        clockLabel.setForeground(Theme.TEXT_MUTED);

        topBar.add(pageTitleLabel, BorderLayout.WEST);
        topBar.add(clockLabel, BorderLayout.EAST);
        return topBar;
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> updateClock());
        timer.setInitialDelay(0);
        timer.start();
    }

    private void updateClock() {
        clockLabel.setText(LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy  |  hh:mm a")));
    }

    private static final Map<String, String> TITLES = new LinkedHashMap<>();
    static {
        TITLES.put("dashboard", "Dashboard");
        TITLES.put("customers", "Customer Management");
        TITLES.put("vehicles", "Vehicle Management");
        TITLES.put("mechanics", "Mechanic Management");
        TITLES.put("services", "Service Management");
        TITLES.put("bookings", "Service Booking");
        TITLES.put("billing", "Billing");
        TITLES.put("reports", "Reports");
        TITLES.put("search", "Global Search");
        TITLES.put("settings", "Settings");
    }

    /** Switches the visible panel. Panels are (re)built fresh each time they are
     *  navigated to, except dashboard/settings which need a MainFrame reference and
     *  are cached lazily on first build. */
    public void showView(String key) {
        if (!viewFactories.containsKey(key)) {
            return;
        }
        activeKey = key;
        pageTitleLabel.setText(TITLES.getOrDefault(key, key));
        highlightNav(key);

        // Rebuild every time so freshly added data (customers, vehicles, etc.)
        // is reflected immediately when the user re-visits a tab.
        JPanel panel = viewFactories.get(key).get();
        contentArea.removeAll();
        contentArea.add(panel, key);
        CardLayout cl = (CardLayout) contentArea.getLayout();
        contentArea.revalidate();
        contentArea.repaint();
        cl.show(contentArea, key);
    }

    private void highlightNav(String activeKeyToShow) {
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equals(activeKeyToShow);
            entry.getValue().setBackground(active ? Theme.BG_SIDEBAR_ACTIVE : Theme.BG_SIDEBAR);
            entry.getValue().setFont(active ? Theme.FONT_BOLD : Theme.FONT_NORMAL);
        }
    }

    public void logout() {
        if (Widgets.confirm(this, "Logout", "Are you sure you want to logout?")) {
            dispose();
            onLogout.run();
        }
    }

    public AuthController.AdminInfo getAdmin() {
        return admin;
    }
}
