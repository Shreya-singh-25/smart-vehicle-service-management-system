package com.autocare.vsms.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Exports tabular report data to a CSV file.
 */
public final class CsvExport {

    private CsvExport() { }

    public static void exportToCsv(String filePath, String[] headers, List<Object[]> rows) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(String.join(",", escapeRow(headers)));
            writer.write("\n");
            for (Object[] row : rows) {
                String[] cells = new String[row.length];
                for (int i = 0; i < row.length; i++) {
                    cells[i] = row[i] == null ? "" : row[i].toString();
                }
                writer.write(String.join(",", escapeRow(cells)));
                writer.write("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to export CSV", e);
        }
    }

    private static String[] escapeRow(String[] cells) {
        String[] escaped = new String[cells.length];
        for (int i = 0; i < cells.length; i++) {
            String cell = cells[i] == null ? "" : cells[i];
            if (cell.contains(",") || cell.contains("\"") || cell.contains("\n")) {
                cell = "\"" + cell.replace("\"", "\"\"") + "\"";
            }
            escaped[i] = cell;
        }
        return escaped;
    }
}
