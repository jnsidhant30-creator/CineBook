package com.movieticket.view;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TrailerDialog extends JDialog {

    private final String movieTitle;
    private final String trailerKey;
    private final String yearStr;

    public TrailerDialog(Window owner, String movieTitle, String trailerKey, String yearStr) {
        super(owner, "Watch Trailer - " + movieTitle, ModalityType.APPLICATION_MODAL);
        this.movieTitle = movieTitle;
        this.trailerKey = trailerKey;
        this.yearStr = yearStr;

        initComponents();
        
        Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int width = Math.min(700, maxBounds.width - 40);
        int height = Math.min(400, maxBounds.height - 40);
        setSize(width, height);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(0, 20));
        contentPane.setBackground(CineBookTheme.BG_PRIMARY);
        contentPane.setBorder(new EmptyBorder(30, 40, 30, 40));
        setContentPane(contentPane);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel lblHeader = new JLabel("🎬 WATCH TRAILER");
        lblHeader.setFont(ThemeManager.getFont(Font.BOLD, 18));
        lblHeader.setForeground(CineBookTheme.ACCENT_GOLD);
        lblHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel(movieTitle);
        lblTitle.setFont(ThemeManager.getFont(Font.BOLD, 28));
        lblTitle.setForeground(CineBookTheme.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblHeader);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(lblTitle);

        contentPane.add(headerPanel, BorderLayout.NORTH);

        // Main Center Area (Fallback Message)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel lblMessage = new JLabel();
        lblMessage.setFont(ThemeManager.getFont(Font.PLAIN, 16));
        lblMessage.setForeground(CineBookTheme.TEXT_SECONDARY);
        lblMessage.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnAction = new JButton();
        btnAction.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAction.setPreferredSize(new Dimension(300, 50));
        btnAction.setMaximumSize(new Dimension(300, 50));
        btnAction.setFont(ThemeManager.getFont(Font.BOLD, 14));

        if (trailerKey != null && !trailerKey.isBlank()) {
            lblMessage.setText("Java Swing cannot reliably render embedded YouTube playback.");
            JLabel lblMessage2 = new JLabel("Please watch the official trailer directly in your browser.");
            lblMessage2.setFont(ThemeManager.getFont(Font.PLAIN, 16));
            lblMessage2.setForeground(CineBookTheme.TEXT_SECONDARY);
            lblMessage2.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            ThemeManager.stylePrimaryButton(btnAction);
            btnAction.setText(" Watch Official Trailer on YouTube");
            btnAction.setIcon(FontIcon.of(FontAwesomeSolid.PLAY, 14, Color.WHITE));
            btnAction.addActionListener(e -> openUrl("https://www.youtube.com/watch?v=" + trailerKey));
            
            centerPanel.add(Box.createVerticalStrut(20));
            centerPanel.add(lblMessage);
            centerPanel.add(Box.createVerticalStrut(5));
            centerPanel.add(lblMessage2);
            centerPanel.add(Box.createVerticalStrut(30));
            centerPanel.add(btnAction);
        } else {
            lblMessage.setText("Official trailer not available through TMDB.");
            ThemeManager.styleSecondaryButton(btnAction);
            btnAction.setText("Search Official Trailer on YouTube");
            btnAction.addActionListener(e -> {
                String q = movieTitle;
                if (yearStr != null && !yearStr.isBlank()) {
                    q += " " + yearStr;
                }
                q += " official trailer";
                String url = "https://www.youtube.com/results?search_query=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
                openUrl(url);
            });
            
            centerPanel.add(Box.createVerticalStrut(40));
            centerPanel.add(lblMessage);
            centerPanel.add(Box.createVerticalStrut(30));
            centerPanel.add(btnAction);
        }

        contentPane.add(centerPanel, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setOpaque(false);

        JButton btnClose = new JButton("Close");
        ThemeManager.styleSecondaryButton(btnClose);
        btnClose.setPreferredSize(new Dimension(120, 40));
        btnClose.addActionListener(e -> dispose());
        
        footerPanel.add(btnClose);
        contentPane.add(footerPanel, BorderLayout.SOUTH);
    }

    private void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                JOptionPane.showMessageDialog(this, "Desktop browsing is not supported on this platform.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to open browser: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
