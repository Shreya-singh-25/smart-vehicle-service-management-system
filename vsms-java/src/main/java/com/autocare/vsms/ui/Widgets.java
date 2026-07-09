package com.autocare.vsms.ui;

import javax.swing.*;

import javax.swing.border.Border;

import javax.swing.border.EmptyBorder;

import javax.swing.border.LineBorder;

import javax.swing.table.DefaultTableModel;

import java.awt.*;

import java.awt.event.MouseAdapter;

import java.awt.event.MouseEvent;

import java.util.List;

/**

Reusable UI components shared across all views: stat cards, status

badges, confirmation/alert dialogs, and a simple sortable table builder.


*/

public final class Widgets {

private Widgets() { }



// ------------------------------------------------------------------

// Cards / panels

// ------------------------------------------------------------------

public static JPanel card() {

    JPanel panel = new JPanel();

    panel.setBackground(Theme.BG_CARD);

    panel.setBorder(new EmptyBorder(16, 16, 16, 16));

    return panel;

}



public static JPanel statCard(String title, String value, Color accentColor, Runnable onClick) {

    JPanel panel = new JPanel(new BorderLayout());

    panel.setBackground(Theme.BG_CARD);

    panel.setBorder(BorderFactory.createCompoundBorder(

        new LineBorder(Theme.BORDER, 1, true), new EmptyBorder(16, 16, 16, 16)));



    JLabel valueLabel = new JLabel(value);

    valueLabel.setFont(Theme.FONT_CARD_VALUE);

    valueLabel.setForeground(accentColor);



    JLabel titleLabel = new JLabel(title);

    titleLabel.setFont(Theme.FONT_SMALL);

    titleLabel.setForeground(Theme.TEXT_MUTED);



    JPanel textWrap = new JPanel();

    textWrap.setLayout(new BoxLayout(textWrap, BoxLayout.Y_AXIS));

    textWrap.setBackground(Theme.BG_CARD);

    valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    textWrap.add(valueLabel);

    textWrap.add(Box.createVerticalStrut(4));

    textWrap.add(titleLabel);



    panel.add(textWrap, BorderLayout.CENTER);



    if (onClick != null) {

        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        panel.addMouseListener(new MouseAdapter() {

            @Override

            public void mouseClicked(MouseEvent e) {

                onClick.run();

            }

        });

    }

    return panel;

}



// ------------------------------------------------------------------

// Status badge

// ------------------------------------------------------------------

public static JLabel statusBadge(String status) {

    JLabel label = new JLabel("  " + status + "  ");

    label.setOpaque(true);

    label.setBackground(Theme.statusColor(status));

    label.setForeground(Color.BLACK);

    label.setFont(Theme.FONT_SMALL);

    label.setBorder(new EmptyBorder(3, 6, 3, 6));

    return label;

}



// ------------------------------------------------------------------

// Dialogs

// ------------------------------------------------------------------

public static boolean confirm(Component parent, String title, String message) {

    int result = JOptionPane.showConfirmDialog(

        parent, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

    return result == JOptionPane.YES_OPTION;

}



public static void showSuccess(Component parent, String message) {

    JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);

}



public static void showError(Component parent, String message) {

    JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);

}



// ------------------------------------------------------------------

// Buttons

// ------------------------------------------------------------------

public static JButton primaryButton(String text) {

    JButton button = new JButton(text);

    button.setBackground(Theme.PRIMARY);

    button.setForeground(Color.BLACK);

    button.setFont(Theme.FONT_BOLD);

    button.setFocusPainted(false);

    button.setBorder(new EmptyBorder(8, 16, 8, 16));

    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    return button;

}



public static JButton secondaryButton(String text) {

    JButton button = new JButton(text);

    button.setBackground(Theme.BG_CARD);

    button.setForeground(Theme.PRIMARY);

    button.setFont(Theme.FONT_BOLD);

    button.setFocusPainted(false);

    button.setBorder(BorderFactory.createCompoundBorder(

        new LineBorder(Theme.PRIMARY, 1, true), new EmptyBorder(7, 15, 7, 15)));

    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    return button;

}



public static JButton dangerButton(String text) {

    JButton button = new JButton(text);

    button.setBackground(Theme.DANGER);

    button.setForeground(Color.BLACK);

    button.setFont(Theme.FONT_BOLD);

    button.setFocusPainted(false);

    button.setBorder(new EmptyBorder(8, 16, 8, 16));

    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    return button;

}



// ------------------------------------------------------------------

// Table helper (non-editable table backed by a DefaultTableModel)

// ------------------------------------------------------------------

public static DefaultTableModel buildTableModel(String[] columns) {

    return new DefaultTableModel(columns, 0) {

        @Override

        public boolean isCellEditable(int row, int column) {

            return false;

        }

    };

}



public static void fillTable(DefaultTableModel model, List<Object[]> rows) {

    model.setRowCount(0);

    for (Object[] row : rows) {

        model.addRow(row);

    }

}



public static JTable styledTable(DefaultTableModel model) {

    JTable table = new JTable(model);

    table.setRowHeight(26);

    table.setFont(Theme.FONT_NORMAL);

    table.setSelectionBackground(Theme.PRIMARY_LIGHT);

    table.setSelectionForeground(Theme.TEXT_DARK);

    table.setGridColor(Theme.BORDER);

    table.getTableHeader().setFont(Theme.FONT_BOLD);

    table.getTableHeader().setBackground(Theme.PRIMARY);

    table.getTableHeader().setForeground(Color.BLACK);

    table.setAutoCreateRowSorter(true);

    table.setFillsViewportHeight(true);

    return table;

}



// ------------------------------------------------------------------

// Labeled field helper for forms (label above input, GridBag-friendly)

// ------------------------------------------------------------------

public static JPanel labeledField(String label, JComponent field) {

    JPanel wrap = new JPanel();

    wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));

    wrap.setBackground(Theme.BG_CARD);

    JLabel lbl = new JLabel(label);

    lbl.setFont(Theme.FONT_NORMAL);

    lbl.setForeground(Theme.TEXT_DARK);

    lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

    field.setAlignmentX(Component.LEFT_ALIGNMENT);

    if (field.getPreferredSize() != null) {

        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height + 6));

    }

    wrap.add(lbl);

    wrap.add(Box.createVerticalStrut(4));

    wrap.add(field);

    return wrap;

}

}