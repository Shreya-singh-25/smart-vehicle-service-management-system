package com.autocare.vsms.ui;

import com.autocare.vsms.controllers.BookingController;
import com.autocare.vsms.controllers.CustomerController;
import com.autocare.vsms.controllers.VehicleController;
import com.autocare.vsms.models.Booking;
import com.autocare.vsms.models.Customer;
import com.autocare.vsms.models.Vehicle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SearchPanel extends JPanel {

    private final CustomerController customerController = new CustomerController();
    private final VehicleController vehicleController = new VehicleController();
    private final BookingController bookingController = new BookingController();

    private JTextField queryField;
    private DefaultTableModel customerModel;
    private DefaultTableModel vehicleModel;
    private DefaultTableModel bookingModel;

    public SearchPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG_MAIN);
        add(buildTopCard(), BorderLayout.NORTH);
        add(buildResultsArea(), BorderLayout.CENTER);
    }

    private JPanel buildTopCard() {
        JPanel card = Widgets.card();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Global Search");
        title.setFont(Theme.FONT_SUBTITLE);
        card.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("Search across Customer Name, Mobile Number, Vehicle Number, and Booking ID.");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setBorder(new EmptyBorder(4, 0, 10, 0));

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchRow.setBackground(Theme.BG_CARD);
        queryField = new JTextField(30);
        JButton searchBtn = Widgets.primaryButton("\uD83D\uDD0D Search Everything");
        searchBtn.addActionListener(e -> runSearch());
        searchRow.add(queryField);
        searchRow.add(searchBtn);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Theme.BG_CARD);
        center.add(subtitle, BorderLayout.NORTH);
        center.add(searchRow, BorderLayout.CENTER);
        card.add(center, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildResultsArea() {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(Theme.BG_MAIN);

        wrap.add(buildResultSection("Matching Customers",
            new String[]{"ID", "Name", "Mobile", "Email"}, m -> customerModel = m));
        wrap.add(Box.createVerticalStrut(10));
        wrap.add(buildResultSection("Matching Vehicles",
            new String[]{"ID", "Vehicle No.", "Owner", "Company", "Model"}, m -> vehicleModel = m));
        wrap.add(Box.createVerticalStrut(10));
        wrap.add(buildResultSection("Matching Bookings",
            new String[]{"ID", "Customer", "Vehicle", "Service", "Status"}, m -> bookingModel = m));

        JScrollPane scrollPane = new JScrollPane(wrap);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Theme.BG_MAIN);
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    private JPanel buildResultSection(String title, String[] columns, java.util.function.Consumer<DefaultTableModel> modelSetter) {
        JPanel card = Widgets.card();
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(0, 180));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.FONT_BOLD);
        titleLabel.setBorder(new EmptyBorder(0, 0, 6, 0));
        card.add(titleLabel, BorderLayout.NORTH);

        DefaultTableModel model = Widgets.buildTableModel(columns);
        modelSetter.accept(model);
        JTable table = Widgets.styledTable(model);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        return card;
    }

    private void runSearch() {
        String keyword = queryField.getText().trim();
        if (keyword.isEmpty()) return;

        List<Object[]> customerRows = new ArrayList<>();
        for (Customer c : customerController.search(keyword)) {
            customerRows.add(new Object[]{c.getCustomerId(), c.getFullName(), c.getMobileNumber(), c.getEmail()});
        }
        Widgets.fillTable(customerModel, customerRows);

        List<Object[]> vehicleRows = new ArrayList<>();
        for (Vehicle v : vehicleController.search(keyword)) {
            vehicleRows.add(new Object[]{v.getVehicleId(), v.getVehicleNumber(), v.getOwnerName(), v.getCompany(), v.getModel()});
        }
        Widgets.fillTable(vehicleModel, vehicleRows);

        List<Object[]> bookingRows = new ArrayList<>();
        for (Booking b : bookingController.search(keyword)) {
            bookingRows.add(new Object[]{b.getBookingId(), b.getCustomerName(), b.getVehicleNumber(), b.getServiceName(), b.getStatus()});
        }
        Widgets.fillTable(bookingModel, bookingRows);
    }
}
