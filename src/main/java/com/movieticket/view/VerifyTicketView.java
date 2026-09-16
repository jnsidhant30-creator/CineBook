package com.movieticket.view;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.GlassCardPanel;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class VerifyTicketView extends JPanel {

    private JTextField bookingIdField;
    private JButton verifyIdBtn;
    private JButton scanQrBtn;
    
    private JPanel resultPanel;
    private JLabel statusLabel;
    private JLabel detailsLabel;

    public VerifyTicketView() {
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Verify Digital Ticket");
        titleLabel.setFont(ThemeManager.getPageTitleFont());
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Verify customer tickets using Booking ID or by scanning their QR code image.");
        subtitleLabel.setFont(ThemeManager.getBodyFont());
        subtitleLabel.setForeground(CineBookTheme.TEXT_MUTED);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitleLabel);
        
        add(headerPanel, BorderLayout.NORTH);

        // Input Card
        GlassCardPanel inputCard = new GlassCardPanel(new BorderLayout(15, 15));
        inputCard.setTopAccent(CineBookTheme.ACCENT_CYAN, 3);
        inputCard.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        JPanel inputGrid = new JPanel(new GridLayout(2, 1, 15, 15));
        inputGrid.setOpaque(false);
        
        // ID Input
        JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        idPanel.setOpaque(false);
        JLabel idLabel = new JLabel("Booking ID:");
        idLabel.setForeground(CineBookTheme.TEXT_PRIMARY);
        idLabel.setFont(ThemeManager.getLabelFont());
        
        bookingIdField = new JTextField(20);
        ThemeManager.styleTextField(bookingIdField);
        
        verifyIdBtn = new JButton("Verify ID");
        ThemeManager.stylePrimaryButton(verifyIdBtn);
        verifyIdBtn.setIcon(FontIcon.of(FontAwesomeSolid.SEARCH, 14, CineBookTheme.TEXT_PRIMARY));
        
        idPanel.add(idLabel);
        idPanel.add(bookingIdField);
        idPanel.add(verifyIdBtn);
        
        // QR Input
        JPanel qrPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        qrPanel.setOpaque(false);
        scanQrBtn = new JButton("Upload QR Image...");
        ThemeManager.styleSecondaryButton(scanQrBtn);
        scanQrBtn.setIcon(FontIcon.of(FontAwesomeSolid.QRCODE, 14, CineBookTheme.TEXT_PRIMARY));
        
        qrPanel.add(scanQrBtn);
        
        inputGrid.add(idPanel);
        inputGrid.add(qrPanel);
        
        inputCard.add(inputGrid, BorderLayout.NORTH);
        
        // Result Display
        resultPanel = new JPanel(new BorderLayout(10, 10));
        resultPanel.setOpaque(false);
        resultPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, CineBookTheme.BORDER_COLOR));
        resultPanel.setVisible(false); // Hide until verification
        
        JPanel resPadding = new JPanel(new BorderLayout(10, 10));
        resPadding.setOpaque(false);
        resPadding.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        statusLabel = new JLabel("Status");
        statusLabel.setFont(ThemeManager.getPageTitleFont());
        
        detailsLabel = new JLabel();
        detailsLabel.setForeground(CineBookTheme.TEXT_PRIMARY);
        detailsLabel.setFont(ThemeManager.getBodyFont());
        
        resPadding.add(statusLabel, BorderLayout.NORTH);
        resPadding.add(detailsLabel, BorderLayout.CENTER);
        
        resultPanel.add(resPadding, BorderLayout.CENTER);
        
        inputCard.add(resultPanel, BorderLayout.CENTER);
        
        add(inputCard, BorderLayout.CENTER);
    }
    
    public void showResult(boolean isValid, String statusText, String detailsHtml) {
        resultPanel.setVisible(true);
        if (isValid) {
            statusLabel.setText(" VALID TICKET");
            statusLabel.setIcon(FontIcon.of(FontAwesomeSolid.CHECK_CIRCLE, 24, CineBookTheme.SUCCESS_COLOR));
            statusLabel.setForeground(Color.GREEN);
        } else if ("CANCELLED".equalsIgnoreCase(statusText)) {
            statusLabel.setText(" CANCELLED TICKET");
            statusLabel.setIcon(FontIcon.of(FontAwesomeSolid.EXCLAMATION_TRIANGLE, 24, CineBookTheme.WARNING_COLOR));
            statusLabel.setForeground(Color.ORANGE);
        } else {
            statusLabel.setText(" INVALID TICKET");
            statusLabel.setIcon(FontIcon.of(FontAwesomeSolid.TIMES_CIRCLE, 24, CineBookTheme.DANGER_COLOR));
            statusLabel.setForeground(Color.RED);
        }
        detailsLabel.setText("<html>" + detailsHtml.replace("\n", "<br>") + "</html>");
    }
    
    public void clearResult() {
        resultPanel.setVisible(false);
        bookingIdField.setText("");
    }
    
    public JTextField getBookingIdField() { return bookingIdField; }
    public JButton getVerifyIdBtn() { return verifyIdBtn; }
    public JButton getScanQrBtn() { return scanQrBtn; }
}
