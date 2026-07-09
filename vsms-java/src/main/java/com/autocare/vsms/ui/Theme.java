package com.autocare.vsms.ui;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

/**
 * Central place for colors and fonts, matching a clean blue & white
 * professional theme.
 */
public final class Theme {

    public static final Color PRIMARY = new Color(0x1e, 0x56, 0xa0);
    public static final Color PRIMARY_DARK = new Color(0x16, 0x3f, 0x77);
    public static final Color PRIMARY_LIGHT = new Color(0xe8, 0xf0, 0xfe);
    public static final Color ACCENT = new Color(0x3d, 0x8b, 0xfd);
    public static final Color SUCCESS = new Color(0x1f, 0x9d, 0x55);
    public static final Color WARNING = new Color(0xe2, 0xa4, 0x00);
    public static final Color DANGER = new Color(0xd6, 0x45, 0x45);
    public static final Color INFO = new Color(0x2b, 0x7a, 0x9e);

    public static final Color BG_MAIN = new Color(0xf4, 0xf7, 0xfb);
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BG_SIDEBAR = new Color(0x16, 0x32, 0x5c);
    public static final Color BG_SIDEBAR_ACTIVE = PRIMARY;
    public static final Color TEXT_DARK = new Color(0x1f, 0x29, 0x37);
    public static final Color TEXT_MUTED = new Color(0x6b, 0x72, 0x80);
    public static final Color BORDER = new Color(0xe2, 0xe8, 0xf0);

    public static final String FONT_FAMILY = "Segoe UI";

    public static final Font FONT_NORMAL = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font(FONT_FAMILY, Font.BOLD, 15);
    public static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 11);
    public static final Font FONT_CARD_VALUE = new Font(FONT_FAMILY, Font.BOLD, 26);

    private static final Map<String, Color> STATUS_COLORS = new HashMap<>();
    static {
        STATUS_COLORS.put("Pending", WARNING);
        STATUS_COLORS.put("In Progress", INFO);
        STATUS_COLORS.put("Completed", SUCCESS);
    }

    public static Color statusColor(String status) {
        return STATUS_COLORS.getOrDefault(status, TEXT_MUTED);
    }

    private Theme() { }
}
