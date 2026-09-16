package com.movieticket.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ThemeManager {

    public static Color BG_PRIMARY     = CineBookTheme.BG_PRIMARY;
    public static Color BG_SECONDARY   = CineBookTheme.BG_SECONDARY;
    public static Color BG_CARD        = CineBookTheme.BG_CARD;
    public static Color BG_CARD_HOVER  = CineBookTheme.BG_CARD_HOVER;
    public static Color BG_INPUT       = CineBookTheme.BG_INPUT;
    public static Color BG_HEADER      = CineBookTheme.BG_HEADER;

    public static Color ACCENT_RED     = CineBookTheme.ACCENT_RED;
    public static Color ACCENT_RED_HOVER = CineBookTheme.ACCENT_RED_HOVER;
    public static Color ACCENT_INDIGO  = CineBookTheme.ACCENT_INDIGO;
    public static Color ACCENT_GOLD    = CineBookTheme.ACCENT_GOLD;

    public static Color TEXT_PRIMARY   = CineBookTheme.TEXT_PRIMARY;
    public static Color TEXT_MUTED     = CineBookTheme.TEXT_MUTED;
    public static Color TEXT_SECONDARY = CineBookTheme.TEXT_SECONDARY;

    public static Color BORDER_COLOR   = CineBookTheme.BORDER_COLOR;
    public static Color BORDER_LIGHT   = CineBookTheme.BORDER_LIGHT;

    public static Color SUCCESS_COLOR  = CineBookTheme.SUCCESS_COLOR;
    public static Color WARNING_COLOR  = CineBookTheme.WARNING_COLOR;
    public static Color DANGER_COLOR   = CineBookTheme.DANGER_COLOR;
    
    public static void syncColors() {
        BG_PRIMARY     = CineBookTheme.BG_PRIMARY;
        BG_SECONDARY   = CineBookTheme.BG_SECONDARY;
        BG_CARD        = CineBookTheme.BG_CARD;
        BG_CARD_HOVER  = CineBookTheme.BG_CARD_HOVER;
        BG_INPUT       = CineBookTheme.BG_INPUT;
        BG_HEADER      = CineBookTheme.BG_HEADER;
        ACCENT_RED     = CineBookTheme.ACCENT_RED;
        ACCENT_RED_HOVER = CineBookTheme.ACCENT_RED_HOVER;
        ACCENT_INDIGO  = CineBookTheme.ACCENT_INDIGO;
        ACCENT_GOLD    = CineBookTheme.ACCENT_GOLD;
        TEXT_PRIMARY   = CineBookTheme.TEXT_PRIMARY;
        TEXT_MUTED     = CineBookTheme.TEXT_MUTED;
        TEXT_SECONDARY = CineBookTheme.TEXT_SECONDARY;
        BORDER_COLOR   = CineBookTheme.BORDER_COLOR;
        BORDER_LIGHT   = CineBookTheme.BORDER_LIGHT;
        SUCCESS_COLOR  = CineBookTheme.SUCCESS_COLOR;
        WARNING_COLOR  = CineBookTheme.WARNING_COLOR;
        DANGER_COLOR   = CineBookTheme.DANGER_COLOR;
    }

    private static String brandFontFamily   = "Segoe UI";
    private static String headingFontFamily = "Segoe UI";
    private static String bodyFontFamily    = "Segoe UI";

    static {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Set<String> availableFonts = new HashSet<>(Arrays.asList(ge.getAvailableFontFamilyNames()));

            brandFontFamily   = availableFonts.contains("Montserrat") ? "Montserrat" : (availableFonts.contains("Poppins") ? "Poppins" : "Segoe UI");
            headingFontFamily = availableFonts.contains("Poppins") ? "Poppins" : (availableFonts.contains("Montserrat") ? "Montserrat" : "Segoe UI");
            bodyFontFamily    = availableFonts.contains("Inter") ? "Inter" : (availableFonts.contains("Segoe UI") ? "Segoe UI" : Font.SANS_SERIF);
        } catch (Exception e) {
            brandFontFamily   = Font.SANS_SERIF;
            headingFontFamily = Font.SANS_SERIF;
            bodyFontFamily    = Font.SANS_SERIF;
        }
    }

    public static String getFontFamily() { return headingFontFamily; }
    public static Font getFont(int style, float size) { return new Font(bodyFontFamily, style, (int) size); }
    public static Font getHeadingFont(int style, float size) { return new Font(headingFontFamily, style, (int) size); }
    public static Font getBrandFont(int style, float size) { return new Font(brandFontFamily, style, (int) size); }

    public static Font getAppTitleFont() { return getBrandFont(Font.BOLD, 28); }
    public static Font getPageTitleFont() { return getHeadingFont(Font.BOLD, 22); }
    public static Font getSectionHeaderFont() { return getHeadingFont(Font.BOLD, 18); }
    public static Font getCardTitleFont() { return getHeadingFont(Font.BOLD, 15); }
    public static Font getLabelFont() { return getFont(Font.BOLD, 13); }
    public static Font getBodyFont() { return getFont(Font.PLAIN, 13); }
    public static Font getSmallFont() { return getFont(Font.PLAIN, 11); }
    public static Font getButtonFont() { return getHeadingFont(Font.BOLD, 13); }

    public static void applyGlobalTheme() {
        CineBookTheme.install();
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                setBackground(BG_CARD); // dynamic
                super.paintComponent(g);
            }
        };
        panel.setBackground(BG_CARD);
        panel.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    public static JPanel createCardPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                setBackground(BG_CARD); // dynamic
                super.paintComponent(g);
            }
        };
        panel.setBackground(BG_CARD);
        panel.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    public static void stylePrimaryButton(JButton button) {
        styleButton(button, ACCENT_RED, ACCENT_RED_HOVER, TEXT_PRIMARY);
    }

    public static void styleSecondaryButton(JButton button) {
        styleButton(button, BG_CARD, BG_CARD_HOVER, TEXT_PRIMARY);
        button.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(7, 15, 7, 15)
        ));
    }

    public static void styleDangerButton(JButton button) {
        styleButton(button, DANGER_COLOR, DANGER_COLOR.darker(), TEXT_PRIMARY);
    }

    public static void styleSuccessButton(JButton button) {
        styleButton(button, SUCCESS_COLOR, SUCCESS_COLOR.darker(), TEXT_PRIMARY);
    }

    public static void styleButton(JButton button, Color bg, Color hoverBg, Color fg) {
        button.setFont(getButtonFont());
        button.setForeground(fg);
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));

        // Let components update based on current theme manager values if repainted
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) button.setBackground(hoverBg);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) button.setBackground(bg);
            }
        });
    }

    public static void styleTextField(JTextField field) {
        field.setFont(getBodyFont());
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(BG_INPUT);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
    }

    public static void stylePasswordField(JPasswordField field) {
        field.setFont(getBodyFont());
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(BG_INPUT);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
    }

    public static void styleComboBox(JComboBox<?> box) {
        box.setFont(getBodyFont());
        box.setForeground(TEXT_PRIMARY);
        box.setBackground(BG_INPUT);
        box.setBorder(new LineBorder(BORDER_COLOR, 1));
    }

    public static void styleTextArea(JTextArea area) {
        area.setFont(getBodyFont());
        area.setForeground(TEXT_PRIMARY);
        area.setBackground(BG_INPUT);
        area.setCaretColor(TEXT_PRIMARY);
        area.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    public static void styleTable(JTable table) {
        table.setFont(getBodyFont());
        table.setRowHeight(34);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(ACCENT_RED);
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(true);
        table.setGridColor(BORDER_COLOR);

        JTableHeader header = table.getTableHeader();
        header.setFont(getLabelFont());
        header.setBackground(BG_HEADER);
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(ThemeManager.ACCENT_RED);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? ThemeManager.BG_CARD : ThemeManager.BG_PRIMARY);
                    c.setForeground(ThemeManager.TEXT_PRIMARY);
                }
                if (c instanceof JLabel) {
                    ((JLabel) c).setBorder(new EmptyBorder(4, 10, 4, 10));
                }
                return c;
            }
        });
    }

    public static void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    public static void showWarning(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }
    public static void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static final String APP_DISPLAY_NAME = "CineBook";
    public static final String APP_SUBTITLE     = "Movie Ticket Management System";

    public static Image loadImageResource(String resourcePath) {
        try {
            java.net.URL imgUrl = ThemeManager.class.getResource(resourcePath);
            if (imgUrl != null) return javax.imageio.ImageIO.read(imgUrl);
        } catch (Exception e) {}
        return null;
    }

    public static ImageIcon getLogoIcon(int width, int height) {
        Image img = loadImageResource("/images/app-icon.png");
        if (img == null) img = loadImageResource("/images/logo.png");
        if (img != null) return new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        return new ImageIcon(createFallbackIconImage(width, height));
    }

    public static ImageIcon getMainLogoIcon(int width, int height) {
        Image img = loadImageResource("/images/logo.png");
        if (img != null) return new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        return getLogoIcon(width, height);
    }

    public static void applyWindowIcon(Window window) {
        if (window == null) return;
        try {
            java.util.List<Image> icons = new java.util.ArrayList<>();
            int[] sizes = {16, 32, 48, 64, 128, 256};
            for (int s : sizes) {
                Image img = loadImageResource("/images/app-icon.png");
                if (img != null) icons.add(img.getScaledInstance(s, s, Image.SCALE_SMOOTH));
                else icons.add(createFallbackIconImage(s, s));
            }
            if (!icons.isEmpty()) window.setIconImages(icons);
        } catch (Exception e) {}
    }

    public static Image createFallbackIconImage(int width, int height) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int r = (int) (Math.min(width, height) * 0.15);
        g.setColor(ACCENT_RED);
        g.fillRoundRect(2, 2, width - 4, height - 4, r, r);
        g.setColor(ACCENT_GOLD);
        g.setStroke(new BasicStroke(Math.max(1, (float) (Math.min(width, height) * 0.04))));
        g.drawRoundRect(4, 4, width - 8, height - 8, r, r);
        int triW = (int) (width * 0.35), triH = (int) (height * 0.40);
        int triX = (width - triW) / 2 + 1, triY = (height - triH) / 2;
        g.setColor(TEXT_PRIMARY);
        g.fillPolygon(new int[]{triX, triX + triW, triX}, new int[]{triY, triY + (triH / 2), triY + triH}, 3);
        g.dispose();
        return img;
    }
}
