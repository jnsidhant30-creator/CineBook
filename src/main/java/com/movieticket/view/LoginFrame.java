package com.movieticket.view;

import com.movieticket.util.AnimationUtils;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.CinematicBackgroundPanel;
import com.movieticket.view.components.GlassCardPanel;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Path2D;

/**
 * LoginFrame.java — Completely Redesigned Premium Cinematic Login Window for CineBook.
 * Features a mobile-first portrait dimension (430x932px), glowing border, and dynamic layout.
 */
public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton clearButton;
    private JButton registerButton;
    private JButton exitButton;
    private JToggleButton eyeToggleButton;
    private JCheckBox rememberMeCheckbox;
    private JLabel errorLabel;

    // Animated Containers
    private CinematicBackgroundPanel backgroundPanel;
    private GlassCardPanel cardPanel;
    private JPanel logoPanel;
    private JPanel appNamePanel;
    private JPanel brandingPanel;
    
    // Animation state
    private float cardOffsetY = 20.0f; 
    private float cardAlpha = 0.0f;    
    private float logoScale = 0.96f;
    private float logoAlpha = 0.0f;
    private float titleAlpha = 0.0f;
    private float brandingOffsetY = 10.0f;
    
    private int shakeOffsetX = 0;
    private Timer slideFadeTimer;
    private boolean animationCompleted = false;

    // Original Login text for loading state
    private final String LOGIN_TEXT = "LOGIN";
    private final String LOADING_TEXT = "SIGNING IN...";

    public LoginFrame() {
        setTitle("CineBook - Cinematic Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Removed fixed dimensions to let it size naturally and fit the screen correctly
        setMinimumSize(new Dimension(450, 700));
        setResizable(true); // Allow resizing to avoid clipping
        
        ThemeManager.applyWindowIcon(this);
        initComponents();
        pack();
        setLocationRelativeTo(null);
        startIntroAnimation();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) { cleanup(); }
            @Override
            public void windowClosing(WindowEvent e) { cleanup(); }
        });
    }
    
    private void cleanup() {
        if (slideFadeTimer != null && slideFadeTimer.isRunning()) {
            slideFadeTimer.stop();
        }
        if (backgroundPanel != null) {
            backgroundPanel.cleanup();
        }
    }

    private void initComponents() {
        backgroundPanel = new CinematicBackgroundPanel(CinematicBackgroundPanel.Intensity.STRONG, 0.60f);
        // Removed fixed preferred size so it fills the JFrame
        backgroundPanel.setLayout(new GridBagLayout());

        // Skip animation on click
        backgroundPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                skipAnimation();
            }
        });

        // Content wrapper to handle shake offsets
        JPanel contentWrapper = new JPanel(new GridBagLayout()) {
            @Override
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(x + shakeOffsetX, y, width, height);
            }
        };
        contentWrapper.setOpaque(false);

        GridBagConstraints wrapperGbc = new GridBagConstraints();
        wrapperGbc.gridx = 0;
        wrapperGbc.gridy = 0;
        wrapperGbc.anchor = GridBagConstraints.CENTER;
        wrapperGbc.insets = new Insets(10, 0, 15, 0);

        // --- Theme Selector ---
        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        themePanel.setOpaque(false);
        JComboBox<CineBookTheme.ThemeType> themeSelector = new JComboBox<>(CineBookTheme.ThemeType.values());
        themeSelector.setSelectedItem(CineBookTheme.getCurrentTheme());
        ThemeManager.styleComboBox(themeSelector);
        themeSelector.addActionListener(e -> {
            CineBookTheme.ThemeType selected = (CineBookTheme.ThemeType) themeSelector.getSelectedItem();
            if (selected != null) {
                CineBookTheme.setTheme(selected);
            }
        });
        JLabel themeLabel = new JLabel("Theme: ");
        themeLabel.setForeground(new Color(226, 232, 240));
        themePanel.add(themeLabel);
        themePanel.add(themeSelector);
        
        GridBagConstraints themeGbc = new GridBagConstraints();
        themeGbc.gridx = 0;
        themeGbc.gridy = 0;
        themeGbc.anchor = GridBagConstraints.NORTHEAST;
        themeGbc.insets = new Insets(10, 10, 10, 10);
        contentWrapper.add(themePanel, themeGbc);

        // --- Branding Section ---
        initBrandingComponents();
        brandingPanel = createBrandingPanel();
        wrapperGbc.gridy = 1;
        contentWrapper.add(brandingPanel, wrapperGbc);

        // --- Card Panel ---
        wrapperGbc.gridy = 2;
        wrapperGbc.insets = new Insets(0, 0, 10, 0);

        // Transparent dark card (#12122a with ~80% opacity)
        cardPanel = new GlassCardPanel(new GridBagLayout(), 20, new Color(18, 18, 42, 204), null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (cardAlpha < 1.0f) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, cardAlpha));
                }
                super.paintComponent(g2);
                g2.dispose();
            }
            @Override
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(x, y + (int)cardOffsetY, width, height);
            }
        };
        // Removed fixed dimensions (setPreferredSize) so it automatically expands correctly
        cardPanel.setBorder(new EmptyBorder(32, 32, 24, 32));
        cardPanel.setUseGradientBorder(true); // Glowing cyan to magenta border

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        int row = 0;

        // 1. Heading
        gbc.gridy = row++;
        gbc.insets = new Insets(5, 0, 2, 0);
        JLabel welcomeTitle = new JLabel("Welcome Back", SwingConstants.CENTER);
        welcomeTitle.setFont(ThemeManager.getHeadingFont(Font.BOLD, 26));
        welcomeTitle.setForeground(Color.WHITE);
        cardPanel.add(welcomeTitle, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 14, 0);
        JLabel welcomeSub = new JLabel("Sign in to continue your cinematic experience", SwingConstants.CENTER);
        welcomeSub.setFont(ThemeManager.getFont(Font.PLAIN, 12));
        welcomeSub.setForeground(new Color(156, 163, 175)); // gray text
        cardPanel.add(welcomeSub, gbc);

        // Error Label
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 8, 0);
        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setFont(ThemeManager.getFont(Font.BOLD, 12));
        errorLabel.setForeground(new Color(239, 68, 68)); // Soft red
        errorLabel.setVisible(false);
        cardPanel.add(errorLabel, gbc);

        // 2. Email / Username Field
        gbc.gridy = row++;
        gbc.insets = new Insets(4, 0, 4, 0);
        JLabel userLabel = new JLabel("Email / Username");
        userLabel.setFont(ThemeManager.getFont(Font.BOLD, 13));
        userLabel.setForeground(new Color(226, 232, 240)); // light gray
        cardPanel.add(userLabel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(2, 0, 14, 0);
        JPanel userFieldPanel = createIconTextFieldPanel(false, "Enter your email or username", new Color(0, 180, 216));
        cardPanel.add(userFieldPanel, gbc);

        // 3. Password Field
        gbc.gridy = row++;
        gbc.insets = new Insets(4, 0, 4, 0);
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(ThemeManager.getFont(Font.BOLD, 13));
        passLabel.setForeground(new Color(226, 232, 240)); // light gray
        cardPanel.add(passLabel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(2, 0, 10, 0);
        JPanel passFieldPanel = createIconTextFieldPanel(true, "Enter your password", new Color(139, 92, 246));
        cardPanel.add(passFieldPanel, gbc);

        // 4. Remember Me Checkbox
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 16, 0);
        rememberMeCheckbox = new JCheckBox("Remember Me");
        rememberMeCheckbox.setFont(ThemeManager.getFont(Font.PLAIN, 12));
        rememberMeCheckbox.setForeground(new Color(156, 163, 175));
        rememberMeCheckbox.setOpaque(false);
        rememberMeCheckbox.setFocusPainted(false);
        cardPanel.add(rememberMeCheckbox, gbc);

        // 5. LOGIN Button
        gbc.gridy = row++;
        gbc.insets = new Insets(4, 0, 16, 0);
        loginButton = createGradientLoginButton();
        cardPanel.add(loginButton, gbc);

        // 6. Action Bar Buttons (Clear Fields & Exit App)
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 16, 0);
        JPanel actionRow = new JPanel(new GridLayout(1, 2, 10, 0));
        actionRow.setOpaque(false);
        
        clearButton = createOutlineButton(
            "Clear Fields",
            FontIcon.of(FontAwesomeSolid.BROOM, 13, new Color(0, 180, 216)),
            new Color(0, 180, 216),
            new Color(0, 180, 216, 20)
        );
        exitButton = createOutlineButton(
            "Exit App",
            FontIcon.of(FontAwesomeSolid.SIGN_OUT_ALT, 13, new Color(139, 92, 246)),
            new Color(139, 92, 246),
            new Color(139, 92, 246, 20)
        );
        actionRow.add(clearButton);
        actionRow.add(exitButton);
        cardPanel.add(actionRow, gbc);

        // 7. Register link at bottom
        gbc.gridy = row++;
        gbc.insets = new Insets(4, 0, 5, 0);
        JPanel registerPromptPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        registerPromptPanel.setOpaque(false);
        JLabel noAccountLabel = new JLabel("Don't have an account?");
        noAccountLabel.setFont(ThemeManager.getBodyFont());
        noAccountLabel.setForeground(new Color(156, 163, 175));
        registerPromptPanel.add(noAccountLabel);

        registerButton = new JButton("<html><u>Register here</u></html>");
        registerButton.setFont(ThemeManager.getHeadingFont(Font.BOLD, 13));
        registerButton.setForeground(new Color(139, 92, 246));
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        registerButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { registerButton.setForeground(new Color(168, 85, 247)); }
            @Override
            public void mouseExited(MouseEvent e) { registerButton.setForeground(new Color(139, 92, 246)); }
        });
        registerPromptPanel.add(registerButton);

        cardPanel.add(registerPromptPanel, gbc);
        contentWrapper.add(cardPanel, wrapperGbc);
        
        // Wrap content in a transparent JScrollPane to fix clipping
        JScrollPane scrollPane = new JScrollPane(contentWrapper);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Add scroll pane to the center of the background panel
        GridBagConstraints centerGbc = new GridBagConstraints();
        centerGbc.gridx = 0;
        centerGbc.gridy = 0;
        centerGbc.fill = GridBagConstraints.BOTH;
        centerGbc.weightx = 1.0;
        centerGbc.weighty = 1.0;

        backgroundPanel.add(scrollPane, centerGbc);
        add(backgroundPanel);
    }

    private void initBrandingComponents() {
        // Logo: Restored actual CineBook logo
        logoPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                if (logoAlpha < 1.0f) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, logoAlpha));
                }
                g2.dispose();
            }
        };
        logoPanel.setOpaque(false);
        
        JLabel actualLogo = new JLabel(ThemeManager.getMainLogoIcon(80, 80));
        actualLogo.setHorizontalAlignment(SwingConstants.CENTER);
        logoPanel.add(actualLogo, BorderLayout.CENTER);

        // App Name "CineBook" in large bold font
        appNamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (titleAlpha < 1.0f) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, titleAlpha));
                }

                Font font = ThemeManager.getBrandFont(Font.BOLD, 32);
                g2.setFont(font);
                FontMetrics fm = g2.getFontMetrics(font);

                String part1 = "Cine";
                String part2 = "Book";
                int w1 = fm.stringWidth(part1);
                int w2 = fm.stringWidth(part2);
                int totalW = w1 + w2;

                int x = (getWidth() - totalW) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

                // "Cine" in white
                g2.setColor(Color.WHITE);
                g2.drawString(part1, x, y);

                // "Book" in gradient purple-to-blue
                GradientPaint textGrad = new GradientPaint(
                    x + w1, 0, new Color(139, 92, 246),
                    x + totalW, 0, new Color(0, 180, 216)
                );
                g2.setPaint(textGrad);
                g2.drawString(part2, x + w1, y);

                g2.dispose();
            }
        };
        appNamePanel.setPreferredSize(new Dimension(300, 45));
        appNamePanel.setOpaque(false);
    }

    private JPanel createBrandingPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(x, y + (int) brandingOffsetY, width, height);
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                if (titleAlpha < 1.0f) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, titleAlpha));
                }
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel.add(logoPanel, gbc);

        gbc.gridy = 1;
        panel.add(appNamePanel, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(2, 0, 2, 0);
        JLabel subtitleLabel = new JLabel("MOVIE TICKET MANAGEMENT SYSTEM", SwingConstants.CENTER);
        subtitleLabel.setFont(ThemeManager.getHeadingFont(Font.BOLD, 10));
        subtitleLabel.setForeground(new Color(168, 85, 247)); // purple-violet
        panel.add(subtitleLabel, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(5, 0, 0, 0);
        JLabel taglineLabel = new JLabel("Your Seat. Your Movie. Your Moment.", SwingConstants.CENTER);
        taglineLabel.setFont(ThemeManager.getFont(Font.ITALIC, 12));
        taglineLabel.setForeground(new Color(156, 163, 175)); // subtle gray
        panel.add(taglineLabel, gbc);

        return panel;
    }

    private JPanel createIconTextFieldPanel(boolean isPassword, String placeholder, Color focusColor) {
        final JTextField field;
        if (isPassword) {
            passwordField = new JPasswordField();
            field = passwordField;
        } else {
            usernameField = new JTextField();
            field = usernameField;
        }
        setupField(field, placeholder);

        JPanel panel = new JPanel(new BorderLayout(8, 0)) {
            private boolean isFocused = false;
            {
                field.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) { isFocused = true; repaint(); }
                    @Override
                    public void focusLost(FocusEvent e) { isFocused = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                
                // Dark background (#0d1117)
                g2.setColor(new Color(13, 17, 23));
                g2.fillRoundRect(0, 0, w - 1, h - 1, 8, 8); // 8px corner radius
                
                // Border (#2a3a4a or focusColor on focus)
                if (isFocused) {
                    g2.setColor(focusColor);
                    g2.setStroke(new BasicStroke(1.5f));
                } else {
                    g2.setColor(new Color(42, 58, 74));
                    g2.setStroke(new BasicStroke(1.0f));
                }
                g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(316, 48));
        panel.setBorder(new EmptyBorder(0, 12, 0, 12));

        FontIcon leftIcon = FontIcon.of(
            isPassword ? FontAwesomeSolid.LOCK : FontAwesomeSolid.USER, 
            15, 
            new Color(156, 163, 175) // person & lock silhouette in gray
        );
        JLabel iconLabel = new JLabel(leftIcon);
        panel.add(iconLabel, BorderLayout.WEST);

        if (isPassword) {
            eyeToggleButton = new JToggleButton(FontIcon.of(FontAwesomeSolid.EYE, 15, new Color(156, 163, 175)));
            eyeToggleButton.setFocusPainted(false);
            eyeToggleButton.setContentAreaFilled(false);
            eyeToggleButton.setBorderPainted(false);
            eyeToggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            eyeToggleButton.addActionListener(e -> {
                if (eyeToggleButton.isSelected()) {
                    passwordField.setEchoChar((char) 0);
                    eyeToggleButton.setIcon(FontIcon.of(FontAwesomeSolid.EYE_SLASH, 15, new Color(0, 180, 216)));
                } else {
                    passwordField.setEchoChar('•');
                    eyeToggleButton.setIcon(FontIcon.of(FontAwesomeSolid.EYE, 15, new Color(156, 163, 175)));
                }
            });
            panel.add(field, BorderLayout.CENTER);
            panel.add(eyeToggleButton, BorderLayout.EAST);
        } else {
            panel.add(field, BorderLayout.CENTER);
        }
        return panel;
    }

    private void setupField(JTextField field, String placeholder) {
        field.setFont(ThemeManager.getBodyFont());
        field.setForeground(Color.WHITE);
        field.setBackground(new Color(13, 17, 23));
        field.setOpaque(false);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        field.putClientProperty("JTextField.placeholderText", placeholder);
    }

    private JButton createGradientLoginButton() {
        JButton btn = new JButton(LOGIN_TEXT) {
            private boolean isHovered = false;
            private boolean isPressed = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { if (isEnabled()) { isHovered = true; repaint(); } }
                    @Override public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
                    @Override public void mousePressed(MouseEvent e) { if (isEnabled()) { isPressed = true; repaint(); } }
                    @Override public void mouseReleased(MouseEvent e) { isPressed = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                // Colors: Cyan/blue (#00b4d8) to purple (#8b5cf6)
                Color c1 = isHovered ? new Color(0, 200, 235) : new Color(0, 180, 216);
                Color c2 = isHovered ? new Color(155, 110, 255) : new Color(139, 92, 246);
                
                double scale = isPressed ? 0.98 : (isHovered ? 1.01 : 1.0);
                int nw = (int) (w * scale);
                int nh = (int) (h * scale);
                int x = (w - nw) / 2;
                int y = (h - nh) / 2;

                // Subtle glow on hover
                if (isHovered && isEnabled()) {
                    for (int i = 6; i > 0; i--) {
                        g2.setColor(new Color(139, 92, 246, (int) (12 * i * 0.1)));
                        g2.fillRoundRect(x - i, y - i, nw + i * 2, nh + i * 2, 16, 16);
                    }
                }

                // Gradient Fill
                GradientPaint grad = new GradientPaint(x, y, c1, x + nw, y + nh, c2);
                g2.setPaint(grad);
                g2.fillRoundRect(x, y, nw, nh, 12, 12); // 12px corners

                if (!isEnabled()) {
                    g2.setColor(new Color(0, 0, 0, 120));
                    g2.fillRoundRect(x, y, nw, nh, 12, 12);
                }

                // Text
                g2.setFont(ThemeManager.getHeadingFont(Font.BOLD, 15));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                String text = getText();
                int textX = (w - fm.stringWidth(text)) / 2;
                int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, textX, textY);

                g2.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(316, 52));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createOutlineButton(String text, Icon icon, Color fgColor, Color hoverBgColor) {
        JButton btn = new JButton(text) {
            private boolean isHovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                // Semi-transparent hover overlay
                if (isHovered) {
                    g2.setColor(hoverBgColor);
                    g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
                }

                // Outlined border (#2a3a4a)
                g2.setColor(new Color(42, 58, 74));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

                // Content centering layout
                int textW = g2.getFontMetrics().stringWidth(getText());
                int iconW = icon != null ? icon.getIconWidth() : 0;
                int gap = 6;
                int totalW = iconW + (iconW > 0 ? gap : 0) + textW;
                int startX = (w - totalW) / 2;

                if (icon != null) {
                    int iconY = (h - icon.getIconHeight()) / 2;
                    icon.paintIcon(this, g2, startX, iconY);
                }

                g2.setFont(ThemeManager.getHeadingFont(Font.BOLD, 12));
                g2.setColor(fgColor);
                FontMetrics fm = g2.getFontMetrics();
                int textX = startX + iconW + (iconW > 0 ? gap : 0);
                int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), textX, textY);

                g2.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(150, 42));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void startIntroAnimation() {
        cardAlpha = 0.0f;
        cardOffsetY = 15.0f;
        logoAlpha = 0.0f;
        logoScale = 0.96f;
        titleAlpha = 0.0f;
        brandingOffsetY = 10.0f;

        slideFadeTimer = new Timer(16, null);
        long startTime = System.currentTimeMillis();
        slideFadeTimer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            
            // Logo & Title branding animation
            if (elapsed > 100) {
                float progress = Math.min(1.0f, (elapsed - 100) / 400.0f);
                float eased = (float) (1.0 - Math.pow(1.0 - progress, 3));
                logoAlpha = eased;
                logoScale = 0.96f + (0.04f * eased);
            }
            
            if (elapsed > 250) {
                float progress = Math.min(1.0f, (elapsed - 250) / 350.0f);
                float eased = (float) (1.0 - Math.pow(1.0 - progress, 3));
                titleAlpha = eased;
                brandingOffsetY = 10.0f * (1.0f - eased);
            }
            
            // Card fade and slide
            if (elapsed > 350) {
                float progress = Math.min(1.0f, (elapsed - 350) / 450.0f);
                float eased = (float) (1.0 - Math.pow(1.0 - progress, 3));
                cardAlpha = eased;
                cardOffsetY = 20.0f * (1.0f - eased);
            }
            
            if (elapsed > 800) {
                animationCompleted = true;
                slideFadeTimer.stop();
                usernameField.requestFocusInWindow();
            }
            
            if (logoPanel != null) logoPanel.repaint();
            if (appNamePanel != null) appNamePanel.repaint();
            if (brandingPanel != null) brandingPanel.repaint();
            cardPanel.repaint();
            cardPanel.getParent().validate();
        });
        slideFadeTimer.start();
    }

    public void skipAnimation() {
        if (!animationCompleted) {
            animationCompleted = true;
            if (slideFadeTimer != null && slideFadeTimer.isRunning()) slideFadeTimer.stop();
            logoAlpha = 1.0f;
            logoScale = 1.0f;
            titleAlpha = 1.0f;
            brandingOffsetY = 0.0f;
            cardAlpha = 1.0f;
            cardOffsetY = 0.0f;
            
            if (logoPanel != null) logoPanel.repaint();
            if (appNamePanel != null) appNamePanel.repaint();
            if (brandingPanel != null) brandingPanel.repaint();
            cardPanel.repaint();
            cardPanel.getParent().validate();
        }
    }

    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        loginButton.setText(LOGIN_TEXT);
        loginButton.setEnabled(true);
        
        // Shake animation
        AnimationUtils.animate(250, (raw, eased) -> {
            shakeOffsetX = (int) (Math.sin(raw * Math.PI * 6) * 8);
            cardPanel.getParent().validate();
        }, () -> {
            shakeOffsetX = 0;
            cardPanel.getParent().validate();
        });
    }

    public void showSuccess(Runnable onComplete) {
        errorLabel.setVisible(false);
        loginButton.setText(" LOGIN SUCCESSFUL");
        loginButton.setIcon(org.kordamp.ikonli.swing.FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CHECK_CIRCLE, 14, Color.WHITE));
        
        AnimationUtils.animate(600, (raw, eased) -> {
            cardAlpha = 1.0f - (eased * 0.8f); 
            backgroundPanel.setDarkOverlayOpacity(0.60f + (eased * 0.40f)); 
            cardPanel.repaint();
        }, () -> {
            if (onComplete != null) onComplete.run();
        });
    }

    public void setLoading(boolean loading) {
        if (loading) {
            errorLabel.setVisible(false);
            loginButton.setText(LOADING_TEXT);
            loginButton.setEnabled(false);
        } else {
            loginButton.setText(LOGIN_TEXT);
            loginButton.setEnabled(true);
        }
    }

    public JTextField getUsernameField() { return usernameField; }
    public JPasswordField getPasswordField() { return passwordField; }
    public JButton getLoginButton() { return loginButton; }
    public JButton getClearButton() { return clearButton; }
    public JButton getRegisterButton() { return registerButton; }
    public JButton getExitButton() { return exitButton; }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        errorLabel.setVisible(false);
    }
}
