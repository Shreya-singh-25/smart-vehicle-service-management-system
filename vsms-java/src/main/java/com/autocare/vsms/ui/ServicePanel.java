package com.autocare.vsms.ui;

import com.autocare.vsms.controllers.OperationResult;
import com.autocare.vsms.controllers.ServiceController;
import com.autocare.vsms.models.Service;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePanel extends JPanel {

    private final ServiceController controller = new ServiceController();
    private String selectedId;

    private JTextField idField;
    private JTextField nameField;
    private JTextField priceField;
    private JTextField timeField;
    private JTextField searchField;

    private DefaultTableModel tableModel;
    private JTable table;

    public ServicePanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_MAIN);
        add(buildFormCard(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        loadTable(null);
    }

    private JPanel buildFormCard() {
        JPanel card = Widgets.card();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Service Details");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(1, 4, 10, 0));
        fields.setBackground(Theme.BG_CARD);

        idField = new JTextField("Auto-generated");
        idField.setEditable(false);
        nameField = new JTextField();
        priceField = new JTextField();
        timeField = new JTextField();

        fields.add(Widgets.labeledField("Service ID", idField));
        fields.add(Widgets.labeledField("Service Name *", nameField));
        fields.add(Widgets.labeledField("Price (Rs.) *", priceField));
        fields.add(Widgets.labeledField("Estimated Time *", timeField));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 14));
        buttonRow.setBackground(Theme.BG_CARD);
        JButton addBtn = Widgets.primaryButton("+ Add Service");
        addBtn.addActionListener(e -> addService());
        JButton updateBtn = Widgets.secondaryButton("Update");
        updateBtn.addActionListener(e -> updateService());
        JButton deleteBtn = Widgets.dangerButton("Delete");
        deleteBtn.addActionListener(e -> deleteService());
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

        JLabel title = new JLabel("Available Services");
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

        String[] columns = {"ID", "Service Name", "Price (Rs.)", "Estimated Time"};
        tableModel = Widgets.buildTableModel(columns);
        table = Widgets.styledTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected();
        });
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        return card;
    }

    private void loadTable(String keyword) {
        List<Service> services = (keyword != null && !keyword.isEmpty())
            ? controller.search(keyword) : controller.getAll();
        List<Object[]> rows = new ArrayList<>();
        for (Service s : services) {
            rows.add(new Object[]{s.getServiceId(), s.getServiceName(), String.format("%.2f", s.getPrice()), s.getEstimatedTime()});
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
        priceField.setText(String.valueOf(tableModel.getValueAt(modelRow, 2)));
        timeField.setText(String.valueOf(tableModel.getValueAt(modelRow, 3)));
    }

    private void clearForm() {
        selectedId = null;
        idField.setText("Auto-generated");
        nameField.setText("");
        priceField.setText("");
        timeField.setText("");
        table.clearSelection();
    }

    private void addService() {
        OperationResult r = controller.addService(nameField.getText(), priceField.getText(), timeField.getText());
        if (r.success) {
            Widgets.showSuccess(this, r.message);
            clearForm();
            loadTable(null);
        } else {
            Widgets.showError(this, r.message);
        }
    }

    private void updateService() {
        if (selectedId == null) {
            Widgets.showError(this, "Please select a service from the table first.");
            return;
        }
        OperationResult r = controller.updateService(selectedId, nameField.getText(), priceField.getText(), timeField.getText());
        if (r.success) {
            Widgets.showSuccess(this, r.message);
            clearForm();
            loadTable(null);
        } else {
            Widgets.showError(this, r.message);
        }
    }

    private void deleteService() {
        if (selectedId == null) {
            Widgets.showError(this, "Please select a service from the table first.");
            return;
        }
        if (Widgets.confirm(this, "Delete Service", "Are you sure you want to delete this service?")) {
            OperationResult r = controller.deleteService(selectedId);
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
