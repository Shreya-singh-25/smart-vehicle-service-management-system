package com.autocare.vsms.ui;

import com.autocare.vsms.controllers.CustomerController;
import com.autocare.vsms.controllers.OperationResult;
import com.autocare.vsms.models.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerPanel extends JPanel {

    private final CustomerController controller = new CustomerController();
    private String selectedId;

    private JTextField idField;
    private JTextField nameField;
    private JTextField mobileField;
    private JTextField emailField;
    private JTextField addressField;
    private JTextField searchField;

    private DefaultTableModel tableModel;
    private JTable table;

    public CustomerPanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_MAIN);
        add(buildFormCard(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        loadTable(null);
    }

    private JPanel buildFormCard() {
        JPanel card = Widgets.card();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Customer Details");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(1, 5, 10, 0));
        fields.setBackground(Theme.BG_CARD);

        idField = new JTextField("Auto-generated");
        idField.setEditable(false);
        nameField = new JTextField();
        mobileField = new JTextField();
        emailField = new JTextField();
        addressField = new JTextField();

        fields.add(Widgets.labeledField("Customer ID", idField));
        fields.add(Widgets.labeledField("Full Name *", nameField));
        fields.add(Widgets.labeledField("Mobile Number *", mobileField));
        fields.add(Widgets.labeledField("Email", emailField));
        fields.add(Widgets.labeledField("Address *", addressField));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 14));
        buttonRow.setBackground(Theme.BG_CARD);

        JButton addBtn = Widgets.primaryButton("+ Add Customer");
        addBtn.addActionListener(e -> addCustomer());
        JButton updateBtn = Widgets.secondaryButton("Update");
        updateBtn.addActionListener(e -> updateCustomer());
        JButton deleteBtn = Widgets.dangerButton("Delete");
        deleteBtn.addActionListener(e -> deleteCustomer());
        JButton clearBtn = Widgets.secondaryButton("Clear Form");
        clearBtn.addActionListener(e -> clearForm());

        buttonRow.add(addBtn);
        buttonRow.add(updateBtn);
        buttonRow.add(deleteBtn);
        buttonRow.add(clearBtn);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Theme.BG_CARD);
        center.add(fields, BorderLayout.NORTH);
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

        JLabel title = new JLabel("All Customers");
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

        String[] columns = {"ID", "Full Name", "Mobile", "Email", "Address"};
        tableModel = Widgets.buildTableModel(columns);
        table = Widgets.styledTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected();
        });
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        return card;
    }

    private void loadTable(String keyword) {
        List<Customer> customers = (keyword != null && !keyword.isEmpty())
            ? controller.search(keyword) : controller.getAll();
        List<Object[]> rows = new ArrayList<>();
        for (Customer c : customers) {
            rows.add(new Object[]{c.getCustomerId(), c.getFullName(), c.getMobileNumber(), c.getEmail(), c.getAddress()});
        }
        Widgets.fillTable(tableModel, rows);
    }

    private void onRowSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int modelRow = table.convertRowIndexToModel(row);
        selectedId = String.valueOf(tableModel.getValueAt(modelRow, 0));
        idField.setText(selectedId);
        nameField.setText(String.valueOf(tableModel.getValueAt(modelRow, 1)));
        mobileField.setText(String.valueOf(tableModel.getValueAt(modelRow, 2)));
        emailField.setText(String.valueOf(tableModel.getValueAt(modelRow, 3)));
        addressField.setText(String.valueOf(tableModel.getValueAt(modelRow, 4)));
    }

    private void clearForm() {
        selectedId = null;
        idField.setText("Auto-generated");
        nameField.setText("");
        mobileField.setText("");
        emailField.setText("");
        addressField.setText("");
        table.clearSelection();
    }

    private void addCustomer() {
        OperationResult r = controller.addCustomer(
            nameField.getText(), mobileField.getText(), emailField.getText(), addressField.getText());
        if (r.success) {
            Widgets.showSuccess(this, r.message);
            clearForm();
            loadTable(null);
        } else {
            Widgets.showError(this, r.message);
        }
    }

    private void updateCustomer() {
        if (selectedId == null) {
            Widgets.showError(this, "Please select a customer from the table first.");
            return;
        }
        OperationResult r = controller.updateCustomer(
            selectedId, nameField.getText(), mobileField.getText(), emailField.getText(), addressField.getText());
        if (r.success) {
            Widgets.showSuccess(this, r.message);
            clearForm();
            loadTable(null);
        } else {
            Widgets.showError(this, r.message);
        }
    }

    private void deleteCustomer() {
        if (selectedId == null) {
            Widgets.showError(this, "Please select a customer from the table first.");
            return;
        }
        if (Widgets.confirm(this, "Delete Customer",
                "Are you sure you want to delete this customer?\nThis will also delete their vehicles and bookings.")) {
            OperationResult r = controller.deleteCustomer(selectedId);
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
