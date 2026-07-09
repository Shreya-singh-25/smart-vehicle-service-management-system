package com.autocare.vsms.utils;

import com.autocare.vsms.models.Bill;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Generates a professional, printable HTML invoice for a bill and opens it
 * in the system's default web browser, from where the user can use the
 * browser's native Print dialog to print or save it as a PDF. This keeps
 * the project dependency-free (no external PDF library required) while
 * still producing a polished, print-ready invoice.
 */
public final class InvoiceGenerator {

    private static final String TEMPLATE = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
        <meta charset="UTF-8">
        <title>Invoice %1$s</title>
        <style>
            body { font-family: 'Segoe UI', Arial, sans-serif; background:#f0f4f8; margin:0; padding:30px; color:#1f2937; }
            .invoice-box { max-width:800px; margin:auto; background:#ffffff; padding:40px;
                           border-radius:10px; box-shadow:0 2px 10px rgba(0,0,0,0.08); }
            .header { display:flex; justify-content:space-between; align-items:center;
                      border-bottom:3px solid #1e56a0; padding-bottom:16px; margin-bottom:24px; }
            .header h1 { color:#1e56a0; margin:0; font-size:26px; }
            .header p { margin:2px 0; color:#555; font-size:13px; }
            .badge { display:inline-block; padding:6px 14px; border-radius:20px; background:#e8f0fe;
                     color:#1e56a0; font-weight:bold; font-size:13px; }
            table { width:100%%; border-collapse:collapse; margin-top:10px; }
            th, td { text-align:left; padding:10px 8px; border-bottom:1px solid #e5e7eb; font-size:14px; }
            th { background:#1e56a0; color:#fff; }
            .totals td { border:none; padding:6px 8px; }
            .totals .label { text-align:right; font-weight:600; }
            .grand-total { font-size:18px; font-weight:bold; color:#1e56a0; }
            .section-title { color:#1e56a0; font-size:15px; margin-top:26px; margin-bottom:6px;
                              border-left:4px solid #1e56a0; padding-left:8px; }
            .info-grid { display:grid; grid-template-columns:1fr 1fr; gap:6px 30px; font-size:14px; }
            .footer { margin-top:30px; text-align:center; color:#888; font-size:12px; }
            @media print { body { background:#fff; padding:0; } .invoice-box { box-shadow:none; } }
        </style>
        </head>
        <body>
        <div class="invoice-box">
            <div class="header">
                <div>
                    <h1>AutoCare Service Center</h1>
                    <p>123 Service Lane, Lucknow, Uttar Pradesh</p>
                    <p>Phone: +91-522-1234567 | GSTIN: 09ABCDE1234F1Z5</p>
                </div>
                <div class="badge">INVOICE</div>
            </div>

            <div class="info-grid">
                <div><b>Invoice No:</b> %1$s</div>
                <div><b>Date:</b> %2$s</div>
                <div><b>Customer Name:</b> %3$s</div>
                <div><b>Mobile:</b> %4$s</div>
                <div><b>Vehicle Number:</b> %5$s</div>
                <div><b>Vehicle:</b> %6$s %7$s</div>
            </div>

            <div class="section-title">Service Performed</div>
            <table>
                <tr><th>Booking ID</th><th>Service</th><th>Mechanic</th><th>Status</th></tr>
                <tr>
                    <td>%8$s</td>
                    <td>%9$s</td>
                    <td>%10$s</td>
                    <td>%11$s</td>
                </tr>
            </table>

            <div class="section-title">Charges</div>
            <table class="totals">
                <tr><td class="label">Parts Cost:</td><td>Rs. %12$.2f</td></tr>
                <tr><td class="label">Labour Charge:</td><td>Rs. %13$.2f</td></tr>
                <tr><td class="label">GST (%14$.1f%%):</td><td>Rs. %15$.2f</td></tr>
                <tr><td class="label">Discount:</td><td>- Rs. %16$.2f</td></tr>
                <tr><td class="label grand-total">Total Amount:</td><td class="grand-total">Rs. %17$.2f</td></tr>
            </table>

            <div class="footer">
                <p>Thank you for choosing AutoCare Service Center!</p>
                <p>This is a computer-generated invoice.</p>
            </div>
        </div>
        <script>
            window.onload = function() { window.print(); };
        </script>
        </body>
        </html>
        """;

    private InvoiceGenerator() { }

    /** Builds and saves an HTML invoice file for the given bill. Returns the file path. */
    public static String generateInvoiceHtml(Bill bill) {
        Path invoiceDir = Paths.get("data", "invoices");
        try {
            Files.createDirectories(invoiceDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create invoices folder", e);
        }

        double subtotal = bill.getPartsCost() + bill.getLabourCharge();
        double taxAmount = subtotal * (bill.getTaxPercent() / 100.0);

        String html = String.format(
            TEMPLATE,
            bill.getBillId(),
            bill.getBillDate(),
            nullToDash(bill.getCustomerName()),
            nullToDash(bill.getCustomerMobile()),
            nullToDash(bill.getVehicleNumber()),
            nullToDash(bill.getVehicleCompany()),
            nullToDash(bill.getVehicleModel()),
            bill.getBookingId(),
            nullToDash(bill.getServiceName()),
            bill.getMechanicName() == null ? "Not Assigned" : bill.getMechanicName(),
            nullToDash(bill.getStatus()),
            bill.getPartsCost(),
            bill.getLabourCharge(),
            bill.getTaxPercent(),
            taxAmount,
            bill.getDiscount(),
            bill.getTotalAmount()
        );

        Path filePath = invoiceDir.resolve(bill.getBillId() + ".html");
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writer.write(html);
        } catch (IOException e) {
            throw new RuntimeException("Could not write invoice file", e);
        }
        return filePath.toString();
    }

    /** Opens the given invoice HTML file in the system's default browser. */
    public static void openInvoiceInBrowser(String filePath) {
        try {
            File file = new File(filePath);
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(file.toURI().toString()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not open invoice in browser", e);
        }
    }

    private static String nullToDash(String value) {
        return (value == null || value.isEmpty()) ? "-" : value;
    }
}
