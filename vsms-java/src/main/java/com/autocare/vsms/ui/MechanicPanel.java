package com.autocare.vsms.ui;

import com.autocare.vsms.controllers.MechanicController;
import com.autocare.vsms.controllers.OperationResult;
import com.autocare.vsms.models.Mechanic;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MechanicPanel extends JPanel {

    private final MechanicController controller = new MechanicController();
    private String selectedId;

    private JTextField idField;
    private JTextField nameField;
    private JTextField mobileField;
    private JTextField experienceField;
    private JComboBox<String> specializationCombo;
    private JTextField searchField;

    private DefaultTableModel tableModel;
    private JTable table;

    public MechanicPanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_MAIN);
        add(buildFormCard(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        loadTable(null);
    }

    private JPanel buildFormCard() {
        JPanel card = Widgets.card();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Mechanic Details");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(1, 5, 10, 0));
        fields.setBackground(Theme.BG_CARD);

        idField = new JTextField("Auto-generated");
        idField.setEditable(false);
        nameField = new JTextField();
        mobileField = new JTextField();
        experienceField = new JTextField();
        specializationCombo = new JComboBox<>(MechanicController.SPECIALIZATIONS);

        fields.add(Widgets.labeledField("Mechanic ID", idField));
        fields.add(Widgets.labeledField("Name *", nameField));
        fields.add(Widgets.labeledField("Mobile *", mobileField));
        fields.add(Widgets.labeledField("Experience (years) *", experienceField));
        fields.add(Widgets.labeledField("Specialization *", specializationCombo));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 14));
        buttonRow.setBackground(Theme.BG_CARD);
        JButton addBtn = Widgets.primaryButton("+ Add Mechanic");
        addBtn.addActionListener(e -> addMechanic());
        JButton updateBtn = Widgets.secondaryButton("Update");
        updateBtn.addActionListener(e -> updateMechanic());
        JButton deleteBtn = Widgets.dangerButton("Delete");
        deleteBtn.addActionListener(e -> deleteMechanic());
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

        JLabel title = new JLabel("All Mechanics");
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

        String[] columns = {"ID", "Name", "Mobile", "Experience (yrs)", "Specialization"};
        tableModel = Widgets.buildTableModel(columns);
        table = Widgets.styledTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected();
        });
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        return card;
    }

    private void loadTable(String keyword) {
        List<Mechanic> mechanics = (keyword != null && !keyword.isEmpty())
            ? controller.search(keyword) : controller.getAll();
        List<Object[]> rows = new ArrayList<>();
        for (Mechanic m : mechanics) {
            rows.add(new Object[]{m.getMechanicId(), m.getFullName(), m.getMobileNumber(),
                m.getExperienceYears(), m.getSpecialization()});
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
        experienceField.setText(String.valueOf(tableModel.getValueAt(modelRow, 3)));
        specializationCombo.setSelectedItem(String.valueOf(tableModel.getValueAt(modelRow, 4)));
    }

    private void clearForm() {
        selectedId = null;
        idField.setText("Auto-generated");
        nameField.setText("");
        mobileField.setText("");
        experienceField.setText("");
        specializationCombo.setSelectedIndex(0);
        table.clearSelection();
    }

    private void addMechanic() {
        OperationResult r = controller.addMechanic(
            nameField.getText(), mobileField.getText(), experienceField.getText(),
            (String) specializationCombo.getSelectedItem());
        if (r.success) {
            Widgets.showSuccess(this, r.message);
            clearForm();
            loadTable(null);
        } else {
            Widgets.showError(this, r.message);
        }
    }

    private void updateMechanic() {
        if (selectedId == null) {
            Widgets.showError(this, "Please select a mechanic from the table first.");
            return;
        }
        OperationResult r = controller.updateMechanic(
            selectedId, nameField.getText(), mobileField.getText(), experienceField.getText(),
            (String) specializationCombo.getSelectedItem());
        if (r.success) {
            Widgets.showSuccess(this, r.message);
            clearForm();
            loadTable(null);
        } else {
            Widgets.showError(this, r.message);
        }
    }

    private void deleteMechanic() {
        if (selectedId == null) {
            Widgets.showError(this, "Please select a mechanic from the table first.");
            return;
        }
        if (Widgets.confirm(this, "Delete Mechanic", "Are you sure you want to delete this mechanic?")) {
            OperationResult r = controller.deleteMechanic(selectedId);
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
