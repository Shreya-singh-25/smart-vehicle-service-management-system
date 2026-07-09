package com.autocare.vsms.ui;

import com.autocare.vsms.controllers.ReportController;
import com.autocare.vsms.utils.CsvExport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportsPanel extends JPanel {

    private final ReportController controller = new ReportController();

    private static final String[] REPORT_TYPES = {
        "Daily Report", "Weekly Report", "Monthly Report", "Revenue Report",
        "Customer Report", "Pending Services Report", "Completed Services Report"
    };

    private JComboBox<String> reportTypeCombo;
    private DatePickerField fromDate;
    private DatePickerField toDate;
    private JLabel summaryLabel;
    private JPanel tableWrap;

    private String[] currentHeaders;
    private List<String[]> currentRows = new ArrayList<>();

    public ReportsPanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_MAIN);
        add(buildTopCard(), BorderLayout.NORTH);
        add(buildResultCard(), BorderLayout.CENTER);
        generateReport();
    }

    private JPanel buildTopCard() {
        JPanel card = Widgets.card();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Select Report");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
        row.setBackground(Theme.BG_CARD);

        reportTypeCombo = new JComboBox<>(REPORT_TYPES);
        fromDate = new DatePickerField(LocalDate.now().minusDays(6).toString());
        toDate = new DatePickerField(LocalDate.now().toString());

        row.add(Widgets.labeledField("Report Type", reportTypeCombo));
        row.add(Widgets.labeledField("From Date", fromDate));
        row.add(Widgets.labeledField("To Date", toDate));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 18));
        buttons.setBackground(Theme.BG_CARD);
        JButton generateBtn = Widgets.primaryButton("Generate");
        generateBtn.addActionListener(e -> generateReport());
        JButton exportBtn = Widgets.secondaryButton("\u2B07 Export CSV");
        exportBtn.addActionListener(e -> exportCsv());
        buttons.add(generateBtn);
        buttons.add(exportBtn);
        row.add(buttons);

        card.add(row, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildResultCard() {
        JPanel card = Widgets.card();
        card.setLayout(new BorderLayout());

        summaryLabel = new JLabel();
        summaryLabel.setFont(Theme.FONT_SUBTITLE);
        summaryLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(summaryLabel, BorderLayout.NORTH);

        tableWrap = new JPanel(new BorderLayout());
        tableWrap.setBackground(Theme.BG_CARD);
        card.add(tableWrap, BorderLayout.CENTER);

        return card;
    }

    private void generateReport() {
        String reportType = (String) reportTypeCombo.getSelectedItem();
        String[] headers;
        List<String[]> rows;
        String summary;

        switch (reportType) {
            case "Daily Report" -> {
                rows = controller.dailyReport(fromDate.getText());
                headers = ReportController.BOOKING_HEADERS;
                summary = "Daily Report — " + rows.size() + " booking(s)";
            }
            case "Weekly Report" -> {
                rows = controller.weeklyReport(toDate.getText());
                headers = ReportController.BOOKING_HEADERS;
                summary = "Weekly Report — " + rows.size() + " booking(s)";
            }
            case "Monthly Report" -> {
                LocalDate now = LocalDate.now();
                rows = controller.monthlyReport(now.getYear(), now.getMonthValue());
                headers = ReportController.BOOKING_HEADERS;
                summary = "Monthly Report (" + now.getMonth() + " " + now.getYear() + ") — " + rows.size() + " booking(s)";
            }
            case "Revenue Report" -> {
                rows = controller.revenueReport(fromDate.getText(), toDate.getText());
                headers = ReportController.REVENUE_HEADERS;
                double total = 0;
                for (String[] r : rows) total += Double.parseDouble(r[3]);
                summary = String.format("Revenue Report — %d invoice(s), Total: Rs. %,.2f", rows.size(), total);
            }
            case "Customer Report" -> {
                rows = controller.customerReport();
                headers = ReportController.CUSTOMER_HEADERS;
                summary = "Customer Report — " + rows.size() + " customer(s)";
            }
            case "Pending Services Report" -> {
                rows = controller.pendingServicesReport();
                headers = ReportController.STATUS_HEADERS;
                summary = "Pending Services Report — " + rows.size() + " booking(s)";
            }
            case "Completed Services Report" -> {
                rows = controller.completedServicesReport();
                headers = ReportController.STATUS_HEADERS;
                summary = "Completed Services Report — " + rows.size() + " booking(s)";
            }
            default -> {
                rows = new ArrayList<>();
                headers = new String[0];
                summary = "";
            }
        }

        currentHeaders = headers;
        currentRows = rows;
        summaryLabel.setText(summary);
        renderTable(headers, rows);
    }

    private void renderTable(String[] headers, List<String[]> rows) {
        tableWrap.removeAll();
        DefaultTableModel model = Widgets.buildTableModel(headers);
        List<Object[]> objectRows = new ArrayList<>();
        for (String[] row : rows) objectRows.add(row);
        Widgets.fillTable(model, objectRows);
        JTable table = Widgets.styledTable(model);
        tableWrap.add(new JScrollPane(table), BorderLayout.CENTER);
        tableWrap.revalidate();
        tableWrap.repaint();
    }

    private void exportCsv() {
        if (currentRows.isEmpty()) {
            Widgets.showError(this, "There is no report data to export. Generate a report first.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        String reportType = (String) reportTypeCombo.getSelectedItem();
        chooser.setSelectedFile(new File(reportType.replace(" ", "_") + ".csv"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        List<Object[]> objectRows = new ArrayList<>();
        for (String[] row : currentRows) objectRows.add(row);
        CsvExport.exportToCsv(chooser.getSelectedFile().getAbsolutePath(), currentHeaders, objectRows);
        Widgets.showSuccess(this, "Report exported successfully to:\n" + chooser.getSelectedFile().getAbsolutePath());
    }
}
