package com.autocare.vsms.ui;

import com.autocare.vsms.controllers.*;
import com.autocare.vsms.models.Booking;
import com.autocare.vsms.models.Customer;
import com.autocare.vsms.models.Mechanic;
import com.autocare.vsms.models.Service;
import com.autocare.vsms.models.Vehicle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BookingPanel extends JPanel {

    private final BookingController controller = new BookingController();
    private final CustomerController customerController = new CustomerController();
    private final VehicleController vehicleController = new VehicleController();
    private final MechanicController mechanicController = new MechanicController();
    private final ServiceController serviceController = new ServiceController();

    private String selectedId;

    private JTextField idField;
    private JComboBox<Customer> customerCombo;
    private JComboBox<Vehicle> vehicleCombo;
    private JComboBox<MechanicOption> mechanicCombo;
    private JComboBox<Service> serviceCombo;
    private DatePickerField datePicker;
    private JComboBox<String> statusCombo;
    private JTextArea descriptionArea;

    private JComboBox<String> filterCombo;
    private JTextField searchField;

    private DefaultTableModel tableModel;
    private JTable table;

    /** Wrapper so "-- Not Assigned --" can appear in the mechanic combo alongside real mechanics. */
    private static class MechanicOption {
        final String id; // null means "not assigned"
        final String label;
        MechanicOption(String id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    public BookingPanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_MAIN);
        add(buildFormCard(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        loadTable(null, "All");
    }

    private JPanel buildFormCard() {
        JPanel card = Widgets.card();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Booking Details");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel row1 = new JPanel(new GridLayout(1, 4, 10, 0));
        row1.setBackground(Theme.BG_CARD);
        idField = new JTextField("Auto-generated");
        idField.setEditable(false);

        customerCombo = new JComboBox<>();
        refreshCustomerList();
        customerCombo.addActionListener(e -> refreshVehicleListForSelectedCustomer());

        vehicleCombo = new JComboBox<>();
        mechanicCombo = new JComboBox<>();
        refreshMechanicList();

        row1.add(Widgets.labeledField("Booking ID", idField));
        row1.add(Widgets.labeledField("Customer *", customerCombo));
        row1.add(Widgets.labeledField("Vehicle *", vehicleCombo));
        row1.add(Widgets.labeledField("Mechanic", mechanicCombo));

        JPanel row2 = new JPanel(new GridLayout(1, 3, 10, 0));
        row2.setBackground(Theme.BG_CARD);

        serviceCombo = new JComboBox<>();
        refreshServiceList();

        datePicker = new DatePickerField(null);
        statusCombo = new JComboBox<>(Booking.STATUS_OPTIONS);

        row2.add(Widgets.labeledField("Service *", serviceCombo));
        row2.add(Widgets.labeledField("Booking Date *", datePicker));
        row2.add(Widgets.labeledField("Status *", statusCombo));

        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(Theme.FONT_NORMAL);
        JScrollPane descScroll = new JScrollPane(descriptionArea);

        JPanel row3 = new JPanel(new BorderLayout());
        row3.setBackground(Theme.BG_CARD);
        row3.add(Widgets.labeledField("Problem Description", descScroll), BorderLayout.CENTER);

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBackground(Theme.BG_CARD);
        rows.add(row1);
        rows.add(Box.createVerticalStrut(10));
        rows.add(row2);
        rows.add(Box.createVerticalStrut(10));
        rows.add(row3);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 14));
        buttonRow.setBackground(Theme.BG_CARD);
        JButton createBtn = Widgets.primaryButton("+ Create Booking");
        createBtn.addActionListener(e -> createBooking());
        JButton editBtn = Widgets.secondaryButton("Edit Booking");
        editBtn.addActionListener(e -> editBooking());
        JButton cancelBtn = Widgets.dangerButton("Cancel Booking");
        cancelBtn.addActionListener(e -> cancelBooking());
        JButton clearBtn = Widgets.secondaryButton("Clear Form");
        clearBtn.addActionListener(e -> clearForm());
        buttonRow.add(createBtn);
        buttonRow.add(editBtn);
        buttonRow.add(cancelBtn);
        buttonRow.add(clearBtn);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Theme.BG_CARD);
        center.add(rows, BorderLayout.NORTH);
        center.add(buttonRow, BorderLayout.SOUTH);
        card.add(center, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = Widgets.card();
        card.setLayout(new BorderLayout());

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(Theme.BG_CARD);
        topRow.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel title = new JLabel("All Bookings");
        title.setFont(Theme.FONT_SUBTITLE);
        topRow.add(title, BorderLayout.WEST);

        JPanel rightWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightWrap.setBackground(Theme.BG_CARD);

        JLabel filterLabel = new JLabel("Filter Status:");
        String[] filterOptions = {"All", "Pending", "In Progress", "Completed"};
        filterCombo = new JComboBox<>(filterOptions);
        filterCombo.addActionListener(e -> loadTable(null, (String) filterCombo.getSelectedItem()));

        searchField = new JTextField(18);
        JButton searchBtn = Widgets.primaryButton("Search");
        searchBtn.addActionListener(e -> loadTable(searchField.getText().trim(), "All"));
        JButton clearSearchBtn = Widgets.secondaryButton("Clear");
        clearSearchBtn.addActionListener(e -> {
            searchField.setText("");
            filterCombo.setSelectedItem("All");
            loadTable(null, "All");
        });

        rightWrap.add(filterLabel);
        rightWrap.add(filterCombo);
        rightWrap.add(searchField);
        rightWrap.add(searchBtn);
        rightWrap.add(clearSearchBtn);
        topRow.add(rightWrap, BorderLayout.EAST);

        card.add(topRow, BorderLayout.NORTH);

        String[] columns = {"ID", "Customer", "Vehicle", "Service", "Mechanic", "Date", "Status"};
        tableModel = Widgets.buildTableModel(columns);
        table = Widgets.styledTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected();
        });
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        return card;
    }

    // ------------------------------------------------------------------
    private void refreshCustomerList() {
        Customer selected = (Customer) customerCombo.getSelectedItem();
        customerCombo.removeAllItems();
        for (Customer c : customerController.getAll()) {
            customerCombo.addItem(c);
        }
        if (selected != null) customerCombo.setSelectedItem(selected);
    }

    private void refreshVehicleListForSelectedCustomer() {
        vehicleCombo.removeAllItems();
        Customer selected = (Customer) customerCombo.getSelectedItem();
        if (selected == null) return;
        for (Vehicle v : vehicleController.getVehiclesForOwner(selected.getCustomerId())) {
            vehicleCombo.addItem(v);
        }
    }

    private void refreshMechanicList() {
        mechanicCombo.removeAllItems();
        mechanicCombo.addItem(new MechanicOption(null, "-- Not Assigned --"));
        for (Mechanic m : mechanicController.getAll()) {
            mechanicCombo.addItem(new MechanicOption(m.getMechanicId(), m.getFullName() + " (" + m.getMechanicId() + ")"));
        }
    }

    private void refreshServiceList() {
        serviceCombo.removeAllItems();
        for (Service s : serviceController.getAll()) {
            serviceCombo.addItem(s);
        }
    }

    private void loadTable(String keyword, String statusFilter) {
        List<Booking> bookings;
        if (keyword != null && !keyword.isEmpty()) {
            bookings = controller.search(keyword);
        } else if (statusFilter != null && !statusFilter.equals("All")) {
            bookings = controller.getByStatus(statusFilter);
        } else {
            bookings = controller.getAll();
        }
        List<Object[]> rows = new ArrayList<>();
        for (Booking b : bookings) {
            rows.add(new Object[]{
                b.getBookingId(), b.getCustomerName(), b.getVehicleNumber(), b.getServiceName(),
                b.getMechanicName() == null ? "Not Assigned" : b.getMechanicName(), b.getBookingDate(), b.getStatus()
            });
        }
        Widgets.fillTable(tableModel, rows);
    }

    private void onRowSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int modelRow = table.convertRowIndexToModel(row);
        selectedId = String.valueOf(tableModel.getValueAt(modelRow, 0));
        Booking b = controller.getById(selectedId);
        if (b == null) return;

        idField.setText(b.getBookingId());
        refreshCustomerList();
        for (int i = 0; i < customerCombo.getItemCount(); i++) {
            if (customerCombo.getItemAt(i).getCustomerId().equals(b.getCustomerId())) {
                customerCombo.setSelectedIndex(i);
                break;
            }
        }
        refreshVehicleListForSelectedCustomer();
        for (int i = 0; i < vehicleCombo.getItemCount(); i++) {
            if (vehicleCombo.getItemAt(i).getVehicleId().equals(b.getVehicleId())) {
                vehicleCombo.setSelectedIndex(i);
                break;
            }
        }
        refreshMechanicList();
        for (int i = 0; i < mechanicCombo.getItemCount(); i++) {
            MechanicOption opt = mechanicCombo.getItemAt(i);
            if ((opt.id == null && b.getMechanicId() == null) ||
                (opt.id != null && opt.id.equals(b.getMechanicId()))) {
                mechanicCombo.setSelectedIndex(i);
                break;
            }
        }
        refreshServiceList();
        for (int i = 0; i < serviceCombo.getItemCount(); i++) {
            if (serviceCombo.getItemAt(i).getServiceId().equals(b.getServiceId())) {
                serviceCombo.setSelectedIndex(i);
                break;
            }
        }
        datePicker.setText(b.getBookingDate());
        statusCombo.setSelectedItem(b.getStatus());
        descriptionArea.setText(b.getProblemDescription());
    }

    private void clearForm() {
        selectedId = null;
        idField.setText("Auto-generated");
        refreshCustomerList();
        if (customerCombo.getItemCount() > 0) customerCombo.setSelectedIndex(-1);
        vehicleCombo.removeAllItems();
        refreshMechanicList();
        mechanicCombo.setSelectedIndex(0);
        refreshServiceList();
        if (serviceCombo.getItemCount() > 0) serviceCombo.setSelectedIndex(-1);
        datePicker.setText(java.time.LocalDate.now().toString());
        statusCombo.setSelectedIndex(0);
        descriptionArea.setText("");
        table.clearSelection();
    }

    private void createBooking() {
        Customer customer = (Customer) customerCombo.getSelectedItem();
        Vehicle vehicle = (Vehicle) vehicleCombo.getSelectedItem();
        Service service = (Service) serviceCombo.getSelectedItem();
        MechanicOption mechanic = (MechanicOption) mechanicCombo.getSelectedItem();

        if (customer == null || vehicle == null || service == null) {
            Widgets.showError(this, "Please select customer, vehicle, and service.");
            return;
        }
        OperationResult r = controller.addBooking(
            customer.getCustomerId(), vehicle.getVehicleId(), mechanic == null ? null : mechanic.id,
            service.getServiceId(), datePicker.getText(), descriptionArea.getText(),
            (String) statusCombo.getSelectedItem());
        if (r.success) {
            Widgets.showSuccess(this, r.message);
            clearForm();
            loadTable(null, "All");
        } else {
            Widgets.showError(this, r.message);
        }
    }

    private void editBooking() {
        if (selectedId == null) {
            Widgets.showError(this, "Please select a booking from the table first.");
            return;
        }
        Customer customer = (Customer) customerCombo.getSelectedItem();
        Vehicle vehicle = (Vehicle) vehicleCombo.getSelectedItem();
        Service service = (Service) serviceCombo.getSelectedItem();
        MechanicOption mechanic = (MechanicOption) mechanicCombo.getSelectedItem();

        if (customer == null || vehicle == null || service == null) {
            Widgets.showError(this, "Please select customer, vehicle, and service.");
            return;
        }
        OperationResult r = controller.updateBooking(
            selectedId, customer.getCustomerId(), vehicle.getVehicleId(), mechanic == null ? null : mechanic.id,
            service.getServiceId(), datePicker.getText(), descriptionArea.getText(),
            (String) statusCombo.getSelectedItem());
        if (r.success) {
            Widgets.showSuccess(this, r.message);
            clearForm();
            loadTable(null, "All");
        } else {
            Widgets.showError(this, r.message);
        }
    }

    private void cancelBooking() {
        if (selectedId == null) {
            Widgets.showError(this, "Please select a booking from the table first.");
            return;
        }
        if (Widgets.confirm(this, "Cancel Booking", "Are you sure you want to cancel this booking?")) {
            OperationResult r = controller.cancelBooking(selectedId);
            if (r.success) {
                Widgets.showSuccess(this, r.message);
                clearForm();
                loadTable(null, "All");
            } else {
                Widgets.showError(this, r.message);
            }
        }
    }
}
