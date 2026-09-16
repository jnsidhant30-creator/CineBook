package com.movieticket.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import java.awt.*;

public class CineBookTheme {

    // =========================================================================
    // GLOBAL COLOR PALETTE
    // =========================================================================
    public static Color BG_PRIMARY       = new Color(5, 11, 22);       
    public static Color BG_SECONDARY     = new Color(8, 20, 38);       
    public static Color BG_NAVY          = new Color(11, 24, 48);      
    public static Color BG_SURFACE       = new Color(13, 27, 42);      
    public static Color BG_CARD          = new Color(17, 31, 51);      
    public static Color BG_CARD_HOVER    = new Color(22, 38, 61);      
    public static Color BG_INPUT         = new Color(5, 11, 20);       
    public static Color BG_HEADER        = new Color(8, 20, 38);       

    public static Color ACCENT_PURPLE    = new Color(124, 58, 237);    
    public static Color ACCENT_PURPLE_HOVER = new Color(109, 40, 217);  
    public static Color ACCENT_RED       = new Color(124, 58, 237);    
    public static Color ACCENT_RED_HOVER = new Color(109, 40, 217);    

    public static Color ACCENT_BLUE      = new Color(37, 99, 235);     
    public static Color ACCENT_CYAN      = new Color(6, 182, 212);     
    public static Color ACCENT_INFO      = new Color(6, 182, 212);     
    public static Color ACCENT_INDIGO    = new Color(99, 102, 241);    
    public static Color ACCENT_GOLD      = new Color(245, 158, 11);    
    public static Color ACCENT_SKY       = new Color(14, 165, 233);    

    public static Color TEXT_PRIMARY     = new Color(248, 250, 252);  
    public static Color TEXT_SECONDARY   = new Color(148, 163, 184);  
    public static Color TEXT_MUTED       = new Color(148, 163, 184);  

    public static Color BORDER_COLOR     = new Color(38, 54, 77);      
    public static Color BORDER_LIGHT     = new Color(51, 71, 99);      

    public static Color SUCCESS_COLOR    = new Color(34, 197, 94);     
    public static Color WARNING_COLOR    = new Color(245, 158, 11);    
    public static Color DANGER_COLOR     = new Color(239, 68, 68);     

    public enum ThemeType {
        DARK("Dark"), LIGHT("Light"), SYSTEM("Default / System");
        
        private final String displayName;
        ThemeType(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    private static ThemeType currentTheme = ThemeType.DARK;
    private static final java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(CineBookTheme.class);
    private static final String PREF_THEME = "app_theme";
    
    public static ThemeType getCurrentTheme() {
        return currentTheme;
    }

    public static void setTheme(ThemeType theme) {
        currentTheme = theme;
        prefs.put(PREF_THEME, theme.name());
        
        boolean isDark = theme == ThemeType.DARK;
        
        if (theme == ThemeType.SYSTEM) {
            isDark = isSystemDarkMode();
        }

        if (isDark) {
            applyDarkPalette();
            FlatDarkLaf.setup();
        } else {
            applyLightPalette();
            FlatLightLaf.setup();
        }

        setupUIManager(isDark);
        ThemeManager.syncColors();
        
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }

    public static boolean isSystemDarkMode() {
        try {
            Process process = Runtime.getRuntime().exec("reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize\" /v AppsUseLightTheme");
            try (java.util.Scanner scanner = new java.util.Scanner(process.getInputStream())) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.contains("AppsUseLightTheme")) {
                        return line.contains("0x0"); // 0 = dark, 1 = light
                    }
                }
            }
        } catch (Exception e) {
            // Fallback if not Windows or registry read fails
        }
        return true; // Default to dark on error
    }

    private static void applyDarkPalette() {
        BG_PRIMARY       = new Color(5, 11, 22);       
        BG_SECONDARY     = new Color(8, 20, 38);       
        BG_NAVY          = new Color(11, 24, 48);      
        BG_SURFACE       = new Color(13, 27, 42);      
        BG_CARD          = new Color(17, 31, 51);      
        BG_CARD_HOVER    = new Color(22, 38, 61);      
        BG_INPUT         = new Color(5, 11, 20);       
        BG_HEADER        = new Color(8, 20, 38);       
        TEXT_PRIMARY     = new Color(248, 250, 252);  
        TEXT_SECONDARY   = new Color(148, 163, 184);  
        TEXT_MUTED       = new Color(148, 163, 184);  
        BORDER_COLOR     = new Color(38, 54, 77);      
        BORDER_LIGHT     = new Color(51, 71, 99);
        ACCENT_RED       = new Color(124, 58, 237);    
        ACCENT_RED_HOVER = new Color(109, 40, 217); 
    }

    private static void applyLightPalette() {
        BG_PRIMARY       = new Color(241, 245, 249); // slate-100
        BG_SECONDARY     = new Color(226, 232, 240); // slate-200
        BG_NAVY          = new Color(148, 163, 184); 
        BG_SURFACE       = new Color(255, 255, 255); // white
        BG_CARD          = new Color(255, 255, 255); 
        BG_CARD_HOVER    = new Color(248, 250, 252); // slate-50
        BG_INPUT         = new Color(255, 255, 255); 
        BG_HEADER        = new Color(255, 255, 255); 
        TEXT_PRIMARY     = new Color(15, 23, 42);    // slate-900
        TEXT_SECONDARY   = new Color(71, 85, 105);   // slate-600
        TEXT_MUTED       = new Color(100, 116, 139); // slate-500
        BORDER_COLOR     = new Color(203, 213, 225); // slate-300
        BORDER_LIGHT     = new Color(226, 232, 240);
        ACCENT_RED       = new Color(99, 102, 241);  // Indigo for light mode    
        ACCENT_RED_HOVER = new Color(79, 70, 229); 
    }

    public static void install() {
        String savedThemeStr = prefs.get(PREF_THEME, ThemeType.SYSTEM.name());
        ThemeType loadedTheme;
        try {
            loadedTheme = ThemeType.valueOf(savedThemeStr);
        } catch (IllegalArgumentException e) {
            loadedTheme = ThemeType.SYSTEM;
        }
        setTheme(loadedTheme);
    }

    private static void setupUIManager(boolean isDark) {
        try {
            UIManager.put("Component.arc", 10);
            UIManager.put("Button.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 6);

            UIManager.put("Panel.background", BG_PRIMARY);
            UIManager.put("Panel.foreground", TEXT_PRIMARY);

            UIManager.put("TableHeader.background", BG_SECONDARY);
            UIManager.put("TableHeader.foreground", TEXT_PRIMARY);
            UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 13));

            UIManager.put("Table.background", BG_CARD);
            UIManager.put("Table.foreground", TEXT_PRIMARY);
            UIManager.put("Table.rowHeight", 38);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.gridColor", BORDER_COLOR);
            UIManager.put("Table.alternateRowColor", isDark ? new Color(14, 26, 44) : new Color(248, 250, 252));
            UIManager.put("Table.selectionBackground", ACCENT_PURPLE);
            UIManager.put("Table.selectionForeground", isDark ? TEXT_PRIMARY : Color.WHITE);
            UIManager.put("Table.selectionArc", 6);

            UIManager.put("TextField.background", BG_INPUT);
            UIManager.put("TextField.foreground", TEXT_PRIMARY);
            UIManager.put("TextField.caretForeground", TEXT_PRIMARY);

            UIManager.put("PasswordField.background", BG_INPUT);
            UIManager.put("PasswordField.foreground", TEXT_PRIMARY);

            UIManager.put("ComboBox.background", BG_INPUT);
            UIManager.put("ComboBox.foreground", TEXT_PRIMARY);

            UIManager.put("PopupMenu.background", BG_SECONDARY);
            UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(BORDER_COLOR, 1));

            UIManager.put("OptionPane.background", BG_SECONDARY);
            UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);

        } catch (Exception e) {
            System.err.println("[CineBookTheme] Could not set up theme: " + e.getMessage());
        }
    }

    public static FontIcon createIcon(Ikon iconCode, int iconSize, Color color) {
        if (iconCode == null) return null;
        return FontIcon.of(iconCode, iconSize, color != null ? color : TEXT_PRIMARY);
    }
}
