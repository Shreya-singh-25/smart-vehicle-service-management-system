package com.autocare.vsms.ui;

import com.autocare.vsms.controllers.CustomerController;
import com.autocare.vsms.controllers.OperationResult;
import com.autocare.vsms.controllers.VehicleController;
import com.autocare.vsms.models.Customer;
import com.autocare.vsms.models.Vehicle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VehiclePanel extends JPanel {

    private final VehicleController controller = new VehicleController();
    private final CustomerController customerController = new CustomerController();
    private String selectedId;

    private JTextField idField;
    private JTextField numberField;
    private JComboBox<Customer> ownerCombo;
    private JTextField companyField;
    private JTextField modelField;
    private JTextField yearField;
    private JComboBox<String> fuelCombo;
    private JComboBox<String> typeCombo;
    private JTextField searchField;

    private DefaultTableModel tableModel;
    private JTable table;

    public VehiclePanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_MAIN);
        add(buildFormCard(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        loadTable(null);
    }

    private JPanel buildFormCard() {
        JPanel card = Widgets.card();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Vehicle Details");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel row1 = new JPanel(new GridLayout(1, 4, 10, 0));
        row1.setBackground(Theme.BG_CARD);
        idField = new JTextField("Auto-generated");
        idField.setEditable(false);
        numberField = new JTextField();
        ownerCombo = new JComboBox<>();
        refreshOwnerList();
        companyField = new JTextField();

        row1.add(Widgets.labeledField("Vehicle ID", idField));
        row1.add(Widgets.labeledField("Vehicle Number *", numberField));
        row1.add(Widgets.labeledField("Owner (Customer) *", ownerCombo));
        row1.add(Widgets.labeledField("Company *", companyField));

        JPanel row2 = new JPanel(new GridLayout(1, 4, 10, 0));
        row2.setBackground(Theme.BG_CARD);
        modelField = new JTextField();
        yearField = new JTextField();
        fuelCombo = new JComboBox<>(VehicleController.FUEL_TYPES);
        typeCombo = new JComboBox<>(VehicleController.VEHICLE_TYPES);

        row2.add(Widgets.labeledField("Model *", modelField));
        row2.add(Widgets.labeledField("Manufacturing Year *", yearField));
        row2.add(Widgets.labeledField("Fuel Type *", fuelCombo));
        row2.add(Widgets.labeledField("Vehicle Type *", typeCombo));

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBackground(Theme.BG_CARD);
        rows.add(row1);
        rows.add(Box.createVerticalStrut(10));
        rows.add(row2);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 14));
        buttonRow.setBackground(Theme.BG_CARD);
        JButton addBtn = Widgets.primaryButton("+ Add Vehicle");
        addBtn.addActionListener(e -> addVehicle());
        JButton updateBtn = Widgets.secondaryButton("Edit");
        updateBtn.addActionListener(e -> updateVehicle());
        JButton deleteBtn = Widgets.dangerButton("Delete");
        deleteBtn.addActionListener(e -> deleteVehicle());
        JButton clearBtn = Widgets.secondaryButton("Clear Form");
        clearBtn.addActionListener(e -> clearForm());
        buttonRow.add(addBtn);
        buttonRow.add(updateBtn);
        buttonRow.add(deleteBtn);
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

        JLabel title = new JLabel("All Vehicles");
        title.setFont(Theme.FONT_SUBTITLE);
        topRow.add(title, BorderLayout.WEST);

        JPanel searchWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchWrap.setBackground(Theme.BG_CARD);
        searchField = new JTextField(22);
        JButton searchBtn = Widgets.primaryButton("Search");
        searchBtn.addActionListener(e -> loadTable(searchField.getText().trim()));
        JButton clearSearchBtn = Widgets.secondaryButton("Clear");
        clearSearchBtn.addActionListener(e -> { searchField.setText(""); loadTable(null); });
        searchWrap.add(searchField);
        searchWrap.add(searchBtn);
        searchWrap.add(clearSearchBtn);
        topRow.add(searchWrap, BorderLayout.EAST);

        card.add(topRow, BorderLayout.NORTH);

        String[] columns = {"ID", "Vehicle No.", "Owner", "Company", "Model", "Year", "Fuel", "Type"};
        tableModel = Widgets.buildTableModel(columns);
        table = Widgets.styledTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected();
        });
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        return card;
    }

    private void refreshOwnerList() {
        ownerCombo.removeAllItems();
        for (Customer c : customerController.getAll()) {
            ownerCombo.addItem(c);
        }
    }

    private void loadTable(String keyword) {
        List<Vehicle> vehicles = (keyword != null && !keyword.isEmpty())
            ? controller.search(keyword) : controller.getAll();
        List<Object[]> rows = new ArrayList<>();
        for (Vehicle v : vehicles) {
            rows.add(new Object[]{v.getVehicleId(), v.getVehicleNumber(), v.getOwnerName(), v.getCompany(),
                v.getModel(), v.getManufacturingYear(), v.getFuelType(), v.getVehicleType()});
        }
        Widgets.fillTable(tableModel, rows);
    }

    private void onRowSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int modelRow = table.convertRowIndexToModel(row);
        selectedId = String.valueOf(tableModel.getValueAt(modelRow, 0));
        Vehicle v = controller.getById(selectedId);
        if (v == null) return;
        idField.setText(v.getVehicleId());
        numberField.setText(v.getVehicleNumber());
        refreshOwnerList();
        for (int i = 0; i < ownerCombo.getItemCount(); i++) {
            if (ownerCombo.getItemAt(i).getCustomerId().equals(v.getOwnerId())) {
                ownerCombo.setSelectedIndex(i);
                break;
            }
        }
        companyField.setText(v.getCompany());
        modelField.setText(v.getModel());
        yearField.setText(String.valueOf(v.getManufacturingYear()));
        fuelCombo.setSelectedItem(v.getFuelType());
        typeCombo.setSelectedItem(v.getVehicleType());
    }

    private void clearForm() {
        selectedId = null;
        idField.setText("Auto-generated");
        numberField.setText("");
        refreshOwnerList();
        if (ownerCombo.getItemCount() > 0) ownerCombo.setSelectedIndex(-1);
        companyField.setText("");
        modelField.setText("");
        yearField.setText("");
        fuelCombo.setSelectedIndex(0);
        typeCombo.setSelectedIndex(0);
        table.clearSelection();
    }

    private String resolveOwnerId() {
        Customer selected = (Customer) ownerCombo.getSelectedItem();
        return selected == null ? null : selected.getCustomerId();
    }

    private void addVehicle() {
        String ownerId = resolveOwnerId();
        if (ownerId == null) {
            Widgets.showError(this, "Please select a valid owner (customer).");
            return;
        }
        OperationResult r = controller.addVehicle(
            numberField.getText(), ownerId, companyField.getText(), modelField.getText(),
            yearField.getText(), (String) fuelCombo.getSelectedItem(), (String) typeCombo.getSelectedItem());
        if (r.success) {
            Widgets.showSuccess(this, r.message);
            clearForm();
            loadTable(null);
        } else {
            Widgets.showError(this, r.message);
        }
    }

    private void updateVehicle() {
        if (selectedId == null) {
            Widgets.showError(this, "Please select a vehicle from the table first.");
            return;
        }
        String ownerId = resolveOwnerId();
        if (ownerId == null) {
            Widgets.showError(this, "Please select a valid owner (customer).");
            return;
        }
        OperationResult r = controller.updateVehicle(
            selectedId, numberField.getText(), ownerId, companyField.getText(), modelField.getText(),
            yearField.getText(), (String) fuelCombo.getSelectedItem(), (String) typeCombo.getSelectedItem());
        if (r.success) {
            Widgets.showSuccess(this, r.message);
            clearForm();
            loadTable(null);
        } else {
            Widgets.showError(this, r.message);
        }
    }

    private void deleteVehicle() {
        if (selectedId == null) {
            Widgets.showError(this, "Please select a vehicle from the table first.");
            return;
        }
        if (Widgets.confirm(this, "Delete Vehicle", "Are you sure you want to delete this vehicle?")) {
            OperationResult r = controller.deleteVehicle(selectedId);
            if (r.success) {
                Widgets.showSuccess(this, r.message);
                clearForm();
                loadTable(null);
            } else {
                Widgets.showError(this, r.message);
            }
        }
    }
}
