package com.autocare.vsms.ui;

import com.autocare.vsms.controllers.BillController;
import com.autocare.vsms.controllers.OperationResult;
import com.autocare.vsms.models.Bill;
import com.autocare.vsms.utils.InvoiceGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BillingPanel extends JPanel {

    private final BillController controller = new BillController();
    private String lastGeneratedBillId;

    private JComboBox<BillController.BookingOption> bookingCombo;
    private JTextField partsField;
    private JTextField labourField;
    private JTextField taxField;
    private JTextField discountField;
    private JLabel totalPreviewLabel;
    private JTextField searchField;

    private DefaultTableModel tableModel;
    private JTable table;

    public BillingPanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_MAIN);
        add(buildFormCard(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        loadTable(null);
    }

    private JPanel buildFormCard() {
        JPanel card = Widgets.card();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Generate Invoice");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel row1 = new JPanel(new GridLayout(1, 2, 10, 0));
        row1.setBackground(Theme.BG_CARD);
        bookingCombo = new JComboBox<>();
        refreshBookingList();
        row1.add(Widgets.labeledField("Completed Booking (unbilled) *", bookingCombo));

        JPanel previewWrap = new JPanel();
        previewWrap.setLayout(new BoxLayout(previewWrap, BoxLayout.Y_AXIS));
        previewWrap.setBackground(Theme.BG_CARD);
        JLabel previewTitle = new JLabel("Total Amount (preview)");
        previewTitle.setFont(Theme.FONT_NORMAL);
        totalPreviewLabel = new JLabel("Rs. 0.00");
        totalPreviewLabel.setFont(Theme.FONT_SUBTITLE);
        totalPreviewLabel.setForeground(Theme.PRIMARY);
        previewWrap.add(previewTitle);
        previewWrap.add(totalPreviewLabel);
        row1.add(previewWrap);

        JPanel row2 = new JPanel(new GridLayout(1, 4, 10, 0));
        row2.setBackground(Theme.BG_CARD);
        partsField = new JTextField("0");
        labourField = new JTextField("0");
        taxField = new JTextField("18");
        discountField = new JTextField("0");

        DocumentListener previewListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updatePreview(); }
            public void removeUpdate(DocumentEvent e) { updatePreview(); }
            public void changedUpdate(DocumentEvent e) { updatePreview(); }
        };
        partsField.getDocument().addDocumentListener(previewListener);
        labourField.getDocument().addDocumentListener(previewListener);
        taxField.getDocument().addDocumentListener(previewListener);
        discountField.getDocument().addDocumentListener(previewListener);

        row2.add(Widgets.labeledField("Parts Cost (Rs.) *", partsField));
        row2.add(Widgets.labeledField("Labour Charge (Rs.) *", labourField));
        row2.add(Widgets.labeledField("Tax / GST (%) *", taxField));
        row2.add(Widgets.labeledField("Discount (Rs.)", discountField));

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBackground(Theme.BG_CARD);
        rows.add(row1);
        rows.add(Box.createVerticalStrut(10));
        rows.add(row2);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 14));
        buttonRow.setBackground(Theme.BG_CARD);
        JButton generateBtn = Widgets.primaryButton("\uD83E\uDDFE Generate Bill");
        generateBtn.addActionListener(e -> generateBill());
        JButton printBtn = Widgets.secondaryButton("\uD83D\uDDA8 Print Last Bill");
        printBtn.addActionListener(e -> printLastBill());
        JButton clearBtn = Widgets.secondaryButton("Clear Form");
        clearBtn.addActionListener(e -> clearForm());
        buttonRow.add(generateBtn);
        buttonRow.add(printBtn);
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

        JLabel title = new JLabel("All Invoices");
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

        String[] columns = {"Bill ID", "Customer", "Vehicle", "Total (Rs.)", "Date"};
        tableModel = Widgets.buildTableModel(columns);
        table = Widgets.styledTable(tableModel);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    printSelectedBill();
                }
            }
        });
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel hint = new JLabel("Double-click a row to reprint / save that invoice.");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_MUTED);
        hint.setBorder(new EmptyBorder(6, 0, 0, 0));
        card.add(hint, BorderLayout.SOUTH);

        return card;
    }

    private void refreshBookingList() {
        bookingCombo.removeAllItems();
        for (BillController.BookingOption opt : controller.getCompletedBookingsWithoutBill()) {
            bookingCombo.addItem(opt);
        }
    }

    private void updatePreview() {
        try {
            double parts = parseOrZero(partsField.getText());
            double labour = parseOrZero(labourField.getText());
            double tax = parseOrZero(taxField.getText());
            double discount = parseOrZero(discountField.getText());
            double total = Bill.calculateTotal(parts, labour, tax, discount);
            totalPreviewLabel.setText(String.format("Rs. %,.2f", total));
        } catch (Exception e) {
            totalPreviewLabel.setText("Rs. --");
        }
    }

    private double parseOrZero(String value) {
        try {
            return value == null || value.isEmpty() ? 0 : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void loadTable(String keyword) {
        List<Bill> bills = (keyword != null && !keyword.isEmpty())
            ? controller.search(keyword) : controller.getAllBills();
        List<Object[]> rows = new ArrayList<>();
        for (Bill b : bills) {
            rows.add(new Object[]{b.getBillId(), b.getCustomerName(), b.getVehicleNumber(),
                String.format("%.2f", b.getTotalAmount()), b.getBillDate()});
        }
        Widgets.fillTable(tableModel, rows);
    }

    private void clearForm() {
        refreshBookingList();
        if (bookingCombo.getItemCount() > 0) bookingCombo.setSelectedIndex(-1);
        partsField.setText("0");
        labourField.setText("0");
        taxField.setText("18");
        discountField.setText("0");
    }

    private void generateBill() {
        BillController.BookingOption selected = (BillController.BookingOption) bookingCombo.getSelectedItem();
        if (selected == null) {
            Widgets.showError(this, "Please select a completed booking to bill.");
            return;
        }
        OperationResult r = controller.createBill(
            selected.bookingId, partsField.getText(), labourField.getText(), taxField.getText(), discountField.getText());
        if (!r.success) {
            Widgets.showError(this, r.message);
            return;
        }
        Widgets.showSuccess(this, r.message);
        lastGeneratedBillId = r.generatedId;
        clearForm();
        loadTable(null);
        if (Widgets.confirm(this, "Print Invoice", "Bill generated. Do you want to print / preview it now?")) {
            openInvoice(lastGeneratedBillId);
        }
    }

    private void printLastBill() {
        if (lastGeneratedBillId == null) {
            Widgets.showError(this, "No bill has been generated yet in this session. " +
                "Double-click a bill in the table below to print it.");
            return;
        }
        openInvoice(lastGeneratedBillId);
    }

    private void printSelectedBill() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int modelRow = table.convertRowIndexToModel(row);
        String billId = String.valueOf(tableModel.getValueAt(modelRow, 0));
        openInvoice(billId);
    }

    private void openInvoice(String billId) {
        Bill bill = controller.getBillDetails(billId);
        if (bill == null) {
            Widgets.showError(this, "Could not find bill details.");
            return;
        }
        String path = InvoiceGenerator.generateInvoiceHtml(bill);
        InvoiceGenerator.openInvoiceInBrowser(path);
        Widgets.showSuccess(this, "Invoice saved to:\n" + path +
            "\n\nIt has also been opened in your browser where you can print or save it as PDF.");
    }
}
