package com.autocare.vsms.ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * A simple, dependency-free date picker: a text field (YYYY-MM-DD) plus a
 * button that opens a small calendar popup.
 */
public class DatePickerField extends JPanel {

    private final JTextField textField;

    public DatePickerField(String initialDate) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
        setBackground(Theme.BG_CARD);

        textField = new JTextField(initialDate == null || initialDate.isEmpty()
            ? LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) : initialDate, 12);

        JButton calendarButton = new JButton("\uD83D\uDCC5");
        calendarButton.setFocusPainted(false);
        calendarButton.addActionListener(e -> openCalendar());

        add(textField);
        add(calendarButton);
    }

    public String getText() {
        return textField.getText().trim();
    }

    public void setText(String value) {
        textField.setText(value);
    }

    private void openCalendar() {
        LocalDate current;
        try {
            current = LocalDate.parse(textField.getText().trim());
        } catch (Exception e) {
            current = LocalDate.now();
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        CalendarPopup popup = new CalendarPopup(owner, current, chosen ->
            textField.setText(chosen.format(DateTimeFormatter.ISO_LOCAL_DATE)));
        popup.setVisible(true);
    }

    /** Small modal calendar dialog used by DatePickerField. */
    private static class CalendarPopup extends JDialog {
        private YearMonth yearMonth;
        private final Consumer<LocalDate> callback;
        private final JPanel gridPanel = new JPanel();
        private final JLabel monthLabel = new JLabel();

        CalendarPopup(Window owner, LocalDate initial, Consumer<LocalDate> callback) {
            super(owner, "Select Date", ModalityType.APPLICATION_MODAL);
            this.yearMonth = YearMonth.from(initial);
            this.callback = callback;
            setSize(280, 260);
            setLocationRelativeTo(owner);
            setResizable(false);
            buildUi();
        }

        private void buildUi() {
            getContentPane().removeAll();
            setLayout(new BorderLayout());

            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(Theme.BG_CARD);
            header.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            JButton prev = new JButton("\u25C0");
            prev.addActionListener(e -> { yearMonth = yearMonth.minusMonths(1); refreshGrid(); });
            JButton next = new JButton("\u25B6");
            next.addActionListener(e -> { yearMonth = yearMonth.plusMonths(1); refreshGrid(); });

            monthLabel.setFont(Theme.FONT_BOLD);
            monthLabel.setHorizontalAlignment(SwingConstants.CENTER);

            header.add(prev, BorderLayout.WEST);
            header.add(monthLabel, BorderLayout.CENTER);
            header.add(next, BorderLayout.EAST);

            gridPanel.setLayout(new GridLayout(0, 7, 2, 2));
            gridPanel.setBackground(Theme.BG_CARD);
            gridPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            add(header, BorderLayout.NORTH);
            add(gridPanel, BorderLayout.CENTER);
            refreshGrid();
        }

        private void refreshGrid() {
            monthLabel.setText(yearMonth.getMonth().toString() + " " + yearMonth.getYear());
            gridPanel.removeAll();

            String[] dayNames = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
            for (String d : dayNames) {
                JLabel lbl = new JLabel(d, SwingConstants.CENTER);
                lbl.setFont(Theme.FONT_BOLD);
                gridPanel.add(lbl);
            }

            LocalDate firstOfMonth = yearMonth.atDay(1);
            int leadingBlanks = firstOfMonth.getDayOfWeek().getValue() - 1; // Monday = 1
            for (int i = 0; i < leadingBlanks; i++) {
                gridPanel.add(new JLabel(""));
            }

            int daysInMonth = yearMonth.lengthOfMonth();
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = yearMonth.atDay(day);
                JButton dayButton = new JButton(String.valueOf(day));
                dayButton.setMargin(new Insets(2, 2, 2, 2));
                dayButton.setFocusPainted(false);
                dayButton.addActionListener(e -> {
                    callback.accept(date);
                    dispose();
                });
                gridPanel.add(dayButton);
            }

            gridPanel.revalidate();
            gridPanel.repaint();
        }
    }
}
