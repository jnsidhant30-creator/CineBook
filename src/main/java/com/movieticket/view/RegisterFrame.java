package com.movieticket.view;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.CinematicBackgroundPanel;
import com.movieticket.view.components.GlassCardPanel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * RegisterFrame.java — Premium Cinematic Registration Screen for CineBook.
 * Features full-screen CinematicBackgroundPanel, semi-transparent dark card (#0D1B2A @ 90%),
 * dark input fields, and royal purple primary action buttons.
 */
public class RegisterFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton clearButton;
    private JButton backButton;

    public RegisterFrame() {
        setTitle(ThemeManager.APP_DISPLAY_NAME + " — Register Account");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        ThemeManager.applyWindowIcon(this);
        initComponents();
        pack();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int targetWidth = Math.min(1040, maxBounds.width);
        int targetHeight = Math.min(640, maxBounds.height - 40);
        // Root Container: Cinematic Background Panel with STRONG intensity
        CinematicBackgroundPanel backgroundPanel = new CinematicBackgroundPanel(CinematicBackgroundPanel.Intensity.STRONG, 0.65f);
        backgroundPanel.setPreferredSize(new Dimension(targetWidth, targetHeight));
        backgroundPanel.setLayout(new GridBagLayout());

        // Central Semi-Transparent Dark Glass Registration Card
        GlassCardPanel cardPanel = new GlassCardPanel(new GridBagLayout(), 18, new Color(13, 27, 42, 230), new Color(124, 58, 237, 80));
        cardPanel.setPreferredSize(new Dimension(460, 560));
        cardPanel.setTopAccent(CineBookTheme.ACCENT_PURPLE, 4);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 30, 6, 30);
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        int row = 0;

        // 1. Logo Graphic Image
        gbc.gridy = row++;
        gbc.insets = new Insets(8, 30, 4, 30);
        JLabel logoLabel = new JLabel(ThemeManager.getLogoIcon(72, 72), SwingConstants.CENTER);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(logoLabel, gbc);

        // 2. Title & Subtitle
        gbc.gridy = row++;
        gbc.insets = new Insets(2, 30, 2, 30);
        JLabel regTitle = new JLabel("Create Account", SwingConstants.CENTER);
        regTitle.setFont(ThemeManager.getFont(Font.BOLD, 30));
        regTitle.setForeground(CineBookTheme.TEXT_PRIMARY);
        cardPanel.add(regTitle, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 30, 10, 30);
        JLabel regSubtitle = new JLabel("Join CineBook for Instant Cinema Reservations", SwingConstants.CENTER);
        regSubtitle.setFont(ThemeManager.getFont(Font.PLAIN, 12));
        regSubtitle.setForeground(CineBookTheme.TEXT_MUTED);
        cardPanel.add(regSubtitle, gbc);

        // 3. Username Label & Field
        gbc.gridy = row++;
        gbc.insets = new Insets(4, 30, 2, 30);
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(ThemeManager.getLabelFont());
        userLabel.setForeground(CineBookTheme.TEXT_SECONDARY);
        cardPanel.add(userLabel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(2, 30, 8, 30);
        usernameField = new JTextField();
        ThemeManager.styleTextField(usernameField);
        usernameField.setPreferredSize(new Dimension(360, 36));
        cardPanel.add(usernameField, gbc);

        // 4. Password Label & Field
        gbc.gridy = row++;
        gbc.insets = new Insets(4, 30, 2, 30);
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(ThemeManager.getLabelFont());
        passLabel.setForeground(CineBookTheme.TEXT_SECONDARY);
        cardPanel.add(passLabel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(2, 30, 8, 30);
        passwordField = new JPasswordField();
        ThemeManager.stylePasswordField(passwordField);
        passwordField.setPreferredSize(new Dimension(360, 36));
        cardPanel.add(passwordField, gbc);

        // 5. Confirm Password Label & Field
        gbc.gridy = row++;
        gbc.insets = new Insets(4, 30, 2, 30);
        JLabel confirmPassLabel = new JLabel("Confirm Password");
        confirmPassLabel.setFont(ThemeManager.getLabelFont());
        confirmPassLabel.setForeground(CineBookTheme.TEXT_SECONDARY);
        cardPanel.add(confirmPassLabel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(2, 30, 14, 30);
        confirmPasswordField = new JPasswordField();
        ThemeManager.stylePasswordField(confirmPasswordField);
        confirmPasswordField.setPreferredSize(new Dimension(360, 36));
        cardPanel.add(confirmPasswordField, gbc);

        // 6. Action Buttons (Register & Clear)
        gbc.gridy = row++;
        gbc.insets = new Insets(6, 30, 8, 30);
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        buttonPanel.setOpaque(false);

        registerButton = new JButton("REGISTER");
        ThemeManager.stylePrimaryButton(registerButton);
        registerButton.setPreferredSize(new Dimension(160, 38));
        buttonPanel.add(registerButton);

        clearButton = new JButton("Clear");
        ThemeManager.styleSecondaryButton(clearButton);
        clearButton.setPreferredSize(new Dimension(160, 38));
        buttonPanel.add(clearButton);

        cardPanel.add(buttonPanel, gbc);

        // 7. Back to Login Button
        gbc.gridy = row++;
        gbc.insets = new Insets(6, 30, 10, 30);
        backButton = new JButton("Back to Login");
        backButton.setFont(ThemeManager.getFont(Font.BOLD, 13));
        backButton.setForeground(CineBookTheme.ACCENT_PURPLE);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cardPanel.add(backButton, gbc);

        // --- Theme Switcher (Top Right) ---
        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        themePanel.setOpaque(false);
        JLabel themeLabel = new JLabel(org.kordamp.ikonli.swing.FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.ADJUST, 14, CineBookTheme.TEXT_MUTED));
        JComboBox<CineBookTheme.ThemeType> themeComboBox = new JComboBox<>(CineBookTheme.ThemeType.values());
        ThemeManager.styleComboBox(themeComboBox);
        themeComboBox.setPreferredSize(new Dimension(130, 28));
        themeComboBox.setSelectedItem(CineBookTheme.getCurrentTheme());
        themeComboBox.addActionListener(e -> {
            CineBookTheme.ThemeType selected = (CineBookTheme.ThemeType) themeComboBox.getSelectedItem();
            if (selected != null) {
                CineBookTheme.setTheme(selected);
            }
        });
        themePanel.add(themeLabel);
        themePanel.add(themeComboBox);

        GridBagConstraints themeGbc = new GridBagConstraints();
        themeGbc.gridx = 0;
        themeGbc.gridy = 0;
        themeGbc.anchor = GridBagConstraints.NORTHEAST;
        themeGbc.weightx = 1.0;
        themeGbc.weighty = 1.0;
        themeGbc.insets = new Insets(10, 0, 0, 10);
        
        GridBagConstraints centerGbc = new GridBagConstraints();
        centerGbc.gridx = 0;
        centerGbc.gridy = 0;
        centerGbc.anchor = GridBagConstraints.CENTER;
        centerGbc.weightx = 1.0;
        centerGbc.weighty = 1.0;

        backgroundPanel.add(themePanel, themeGbc);
        backgroundPanel.add(cardPanel, centerGbc);
        add(backgroundPanel);
    }

    public JTextField getUsernameField() { return usernameField; }
    public JPasswordField getPasswordField() { return passwordField; }
    public JPasswordField getConfirmPasswordField() { return confirmPasswordField; }
    public JButton getRegisterButton() { return registerButton; }
    public JButton getClearButton() { return clearButton; }
    public JButton getBackButton() { return backButton; }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
    }
}
