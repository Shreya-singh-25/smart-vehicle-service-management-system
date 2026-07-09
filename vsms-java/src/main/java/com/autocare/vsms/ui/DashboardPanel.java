package com.autocare.vsms.ui;

import com.autocare.vsms.controllers.DashboardController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final DashboardController controller = new DashboardController();

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);
        buildUi();
    }

    private void buildUi() {
        DashboardController.Summary summary = controller.getSummary();

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Theme.BG_MAIN);

        wrapper.add(buildCardsGrid(summary));
        wrapper.add(Box.createVerticalStrut(16));
        wrapper.add(buildQuickActions());
        wrapper.add(Box.createVerticalStrut(16));
        wrapper.add(buildRecentBookings());

        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel buildCardsGrid(DashboardController.Summary s) {
        JPanel grid = new JPanel(new GridLayout(2, 4, 12, 12));
        grid.setBackground(Theme.BG_MAIN);

        grid.add(Widgets.statCard("Total Customers", String.valueOf(s.totalCustomers), Theme.PRIMARY,
            () -> mainFrame.showView("customers")));
        grid.add(Widgets.statCard("Total Vehicles", String.valueOf(s.totalVehicles), Theme.INFO,
            () -> mainFrame.showView("vehicles")));
        grid.add(Widgets.statCard("Total Mechanics", String.valueOf(s.totalMechanics), Theme.ACCENT,
            () -> mainFrame.showView("mechanics")));
        grid.add(Widgets.statCard("Today's Bookings", String.valueOf(s.todaysBookings), Theme.WARNING,
            () -> mainFrame.showView("bookings")));
        grid.add(Widgets.statCard("Pending Services", String.valueOf(s.pendingServices), Theme.WARNING,
            () -> mainFrame.showView("bookings")));
        grid.add(Widgets.statCard("Completed Services", String.valueOf(s.completedServices), Theme.SUCCESS,
            () -> mainFrame.showView("bookings")));
        grid.add(Widgets.statCard("Total Revenue", String.format("Rs. %,.2f", s.totalRevenue), Theme.SUCCESS,
            () -> mainFrame.showView("billing")));

        return grid;
    }

    private JPanel buildQuickActions() {
        JPanel card = Widgets.card();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Quick Actions");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setBackground(Theme.BG_CARD);

        String[][] actions = {
            {"+ New Customer", "customers"},
            {"+ New Vehicle", "vehicles"},
            {"+ New Booking", "bookings"},
            {"Generate Bill", "billing"},
            {"View Reports", "reports"},
        };
        for (String[] a : actions) {
    JButton btn = Widgets.primaryButton(a[0]);

    btn.setForeground(Color.BLACK);
    btn.setBackground(Color.WHITE);

    btn.addActionListener(e -> mainFrame.showView(a[1]));
    row.add(btn);
}
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRecentBookings() {
        JPanel card = Widgets.card();
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(0, 320));

        JLabel title = new JLabel("Recent Bookings");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(title, BorderLayout.NORTH);

        String[] columns = {"Booking ID", "Customer", "Vehicle", "Service", "Date", "Status"};
        DefaultTableModel model = Widgets.buildTableModel(columns);
        List<Object[]> rows = new ArrayList<>();
        for (DashboardController.RecentBooking b : controller.getRecentBookings(10)) {
            rows.add(new Object[]{b.bookingId, b.customerName, b.vehicleNumber, b.serviceName, b.bookingDate, b.status});
        }
        Widgets.fillTable(model, rows);
        JTable table = Widgets.styledTable(model);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }
}
