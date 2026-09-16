package com.movieticket.view;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.AnimatedCheckmarkSpinner;
import com.movieticket.view.components.DigitalTicketCard;
import com.movieticket.view.components.GlassCardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Booking Confirmation screen — Wide Checkout Layout
 */
public class BookingConfirmationView extends JPanel {

    private JLabel movieLabel;
    private JLabel theatreLabel;
    private JLabel screenLabel;
    private JLabel dateLabel;
    private JLabel timeLabel;
    private JLabel seatsLabel;
    private JLabel ticketCountLabel;
    private JLabel pricePerTicketLabel;
    private JLabel totalAmountLabel;

    private DigitalTicketCard digitalTicketCard;
    private AnimatedCheckmarkSpinner animatedCheckmarkSpinner;
    private JLabel statusNoticeLabel;
    private JPanel overlayStatusPanel;

    private JButton confirmButton;
    private JButton backToSeatsButton;
    private JButton cancelButton;

    public BookingConfirmationView() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        setBackground(CineBookTheme.BG_PRIMARY);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        initComponents();
    }

    private void initComponents() {
        // 1. Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 24, 0));

        JLabel titleLabel = new JLabel("Review Booking");
        titleLabel.setFont(ThemeManager.getFont(Font.BOLD, 36));
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Please verify your movie and seat selection before proceeding to payment.");
        subtitleLabel.setFont(ThemeManager.getFont(Font.PLAIN, 16));
        subtitleLabel.setForeground(CineBookTheme.TEXT_MUTED);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Main Content (Wide Layout: Left Breakdown, Right Ticket Preview)
        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setOpaque(false);
        
        GridBagConstraints gbcMain = new GridBagConstraints();
        gbcMain.fill = GridBagConstraints.BOTH;
        gbcMain.weighty = 1.0;

        // --- Left: Wide Breakdown Panel ---
        GlassCardPanel breakdownCard = new GlassCardPanel(new GridBagLayout());
        breakdownCard.setTopAccent(CineBookTheme.ACCENT_PURPLE, 4);
        breakdownCard.setBorder(new EmptyBorder(32, 40, 32, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(12, 0, 12, 40);
        gbc.gridx = 0;
        int row = 0;

        gbc.gridy = row++;
        gbc.gridwidth = 2;
        JLabel invoiceTitle = new JLabel("Booking Breakdown");
        invoiceTitle.setFont(ThemeManager.getFont(Font.BOLD, 24));
        invoiceTitle.setForeground(CineBookTheme.TEXT_PRIMARY);
        breakdownCard.add(invoiceTitle, gbc);

        gbc.gridy = row++;
        JSeparator sep1 = new JSeparator();
        sep1.setForeground(CineBookTheme.BORDER_COLOR);
        breakdownCard.add(sep1, gbc);

        gbc.gridwidth = 1;

        // Row 1
        gbc.gridy = row; gbc.gridx = 0; breakdownCard.add(createLabel("Movie"), gbc);
        gbc.gridx = 1; movieLabel = createValueLabel("—"); movieLabel.setForeground(CineBookTheme.ACCENT_GOLD); breakdownCard.add(movieLabel, gbc);
        row++;

        // Row 2
        gbc.gridy = row; gbc.gridx = 0; breakdownCard.add(createLabel("Cinema Hall"), gbc);
        gbc.gridx = 1; theatreLabel = createValueLabel("—"); breakdownCard.add(theatreLabel, gbc);
        row++;

        // Row 3
        gbc.gridy = row; gbc.gridx = 0; breakdownCard.add(createLabel("Screen"), gbc);
        gbc.gridx = 1; screenLabel = createValueLabel("Screen 1"); breakdownCard.add(screenLabel, gbc);
        row++;

        // Row 4
        gbc.gridy = row; gbc.gridx = 0; breakdownCard.add(createLabel("Date"), gbc);
        gbc.gridx = 1; dateLabel = createValueLabel("—"); breakdownCard.add(dateLabel, gbc);
        row++;

        // Row 5
        gbc.gridy = row; gbc.gridx = 0; breakdownCard.add(createLabel("Time"), gbc);
        gbc.gridx = 1; timeLabel = createValueLabel("—"); breakdownCard.add(timeLabel, gbc);
        row++;

        // Row 6
        gbc.gridy = row; gbc.gridx = 0; breakdownCard.add(createLabel("Selected Seats"), gbc);
        gbc.gridx = 1; seatsLabel = createValueLabel("—"); seatsLabel.setForeground(CineBookTheme.ACCENT_CYAN); breakdownCard.add(seatsLabel, gbc);
        row++;

        // Row 7
        gbc.gridy = row; gbc.gridx = 0; breakdownCard.add(createLabel("Ticket Count"), gbc);
        gbc.gridx = 1; ticketCountLabel = createValueLabel("0"); breakdownCard.add(ticketCountLabel, gbc);
        row++;

        // Row 8
        gbc.gridy = row; gbc.gridx = 0; breakdownCard.add(createLabel("Ticket Price"), gbc);
        gbc.gridx = 1; pricePerTicketLabel = createValueLabel("₹0.00 each"); breakdownCard.add(pricePerTicketLabel, gbc);
        row++;

        // Divider
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(CineBookTheme.BORDER_COLOR);
        breakdownCard.add(sep2, gbc);

        // Total
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 1;
        JLabel totalText = new JLabel("Total Amount");
        totalText.setFont(ThemeManager.getFont(Font.BOLD, 20));
        totalText.setForeground(CineBookTheme.TEXT_MUTED);
        breakdownCard.add(totalText, gbc);

        gbc.gridx = 1;
        totalAmountLabel = new JLabel("₹0.00");
        totalAmountLabel.setFont(ThemeManager.getFont(Font.BOLD, 32));
        totalAmountLabel.setForeground(CineBookTheme.SUCCESS_COLOR);
        breakdownCard.add(totalAmountLabel, gbc);

        // Add Left Panel to Main Content
        gbcMain.gridx = 0;
        gbcMain.weightx = 0.6; // 60% left
        gbcMain.insets = new Insets(0, 0, 0, 20);
        mainContent.add(breakdownCard, gbcMain);


        // --- Right: Digital Ticket Preview ---
        JPanel rightTicketBox = new JPanel(new GridBagLayout());
        rightTicketBox.setOpaque(false);

        digitalTicketCard = new DigitalTicketCard();
        rightTicketBox.add(digitalTicketCard);

        overlayStatusPanel = new JPanel();
        overlayStatusPanel.setLayout(new BoxLayout(overlayStatusPanel, BoxLayout.Y_AXIS));
        overlayStatusPanel.setOpaque(false);
        overlayStatusPanel.setVisible(false);

        animatedCheckmarkSpinner = new AnimatedCheckmarkSpinner();
        animatedCheckmarkSpinner.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusNoticeLabel = new JLabel("Processing...", SwingConstants.CENTER);
        statusNoticeLabel.setFont(ThemeManager.getFont(Font.BOLD, 20));
        statusNoticeLabel.setForeground(CineBookTheme.TEXT_PRIMARY);
        statusNoticeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        overlayStatusPanel.add(animatedCheckmarkSpinner);
        overlayStatusPanel.add(Box.createVerticalStrut(16));
        overlayStatusPanel.add(statusNoticeLabel);

        rightTicketBox.add(overlayStatusPanel);

        // Add Right Panel to Main Content
        gbcMain.gridx = 1;
        gbcMain.weightx = 0.4; // 40% right
        gbcMain.insets = new Insets(0, 20, 0, 0);
        mainContent.add(rightTicketBox, gbcMain);

        add(mainContent, BorderLayout.CENTER);

        // 3. Bottom Actions Panel
        JPanel actions = new com.movieticket.view.components.ResponsiveGridPanel(new com.movieticket.view.components.WrapLayout(FlowLayout.RIGHT, 16, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(24, 0, 0, 0));

        cancelButton = new JButton("Cancel Booking");
        ThemeManager.styleDangerButton(cancelButton);
        cancelButton.setPreferredSize(new Dimension(180, 48));
        cancelButton.setFont(ThemeManager.getFont(Font.BOLD, 14));

        backToSeatsButton = new JButton("Back to Seats");
        ThemeManager.styleSecondaryButton(backToSeatsButton);
        backToSeatsButton.setPreferredSize(new Dimension(180, 48));
        backToSeatsButton.setFont(ThemeManager.getFont(Font.BOLD, 14));

        confirmButton = new JButton("Proceed to Payment");
        ThemeManager.styleSuccessButton(confirmButton);
        confirmButton.setPreferredSize(new Dimension(240, 48));
        confirmButton.setFont(ThemeManager.getFont(Font.BOLD, 14));

        actions.add(cancelButton);
        actions.add(backToSeatsButton);
        actions.add(confirmButton);
        add(actions, BorderLayout.SOUTH);
    }

    public void playBookingConfirmationSequence(Runnable onConfirmed) {
        confirmButton.setEnabled(false);
        backToSeatsButton.setEnabled(false);
        cancelButton.setEnabled(false);

        digitalTicketCard.setVisible(false);
        overlayStatusPanel.setVisible(true);

        animatedCheckmarkSpinner.setMode(AnimatedCheckmarkSpinner.Mode.SPINNER);
        statusNoticeLabel.setText("Securing your seats...");
        statusNoticeLabel.setForeground(CineBookTheme.TEXT_PRIMARY);

        Timer step1Timer = new Timer(600, e -> {
            animatedCheckmarkSpinner.setMode(AnimatedCheckmarkSpinner.Mode.CHECKMARK);
            statusNoticeLabel.setText("Seats Secured!");
            statusNoticeLabel.setForeground(CineBookTheme.SUCCESS_COLOR);

            Timer step2Timer = new Timer(600, e2 -> {
                overlayStatusPanel.setVisible(false);
                digitalTicketCard.setVisible(true);
                confirmButton.setEnabled(true);
                backToSeatsButton.setEnabled(true);
                cancelButton.setEnabled(true);

                if (onConfirmed != null) {
                    onConfirmed.run();
                }
            });
            step2Timer.setRepeats(false);
            step2Timer.start();
        });
        step1Timer.setRepeats(false);
        step1Timer.start();
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ThemeManager.getFont(Font.PLAIN, 18));
        l.setForeground(CineBookTheme.TEXT_MUTED);
        return l;
    }

    private JLabel createValueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ThemeManager.getFont(Font.BOLD, 18));
        l.setForeground(CineBookTheme.TEXT_PRIMARY);
        return l;
    }

    public void syncDigitalTicket() {
        digitalTicketCard.setTicketData(
                "PENDING",
                movieLabel.getText(),
                theatreLabel.getText(),
                screenLabel.getText(),
                dateLabel.getText(),
                timeLabel.getText(),
                seatsLabel.getText(),
                totalAmountLabel.getText(),
                "PENDING PAYMENT"
        );
    }

    // Getters
    public JLabel getMovieLabel() { return movieLabel; }
    public JLabel getTheatreLabel() { return theatreLabel; }
    public JLabel getScreenLabel() { return screenLabel; }
    public JLabel getDateLabel() { return dateLabel; }
    public JLabel getTimeLabel() { return timeLabel; }
    public JLabel getSeatsLabel() { return seatsLabel; }
    public JLabel getTicketCountLabel() { return ticketCountLabel; }
    public JLabel getPricePerTicketLabel() { return pricePerTicketLabel; }
    public JLabel getTotalAmountLabel() { return totalAmountLabel; }
    public JButton getConfirmButton() { return confirmButton; }
    public JButton getPayConfirmButton() { return confirmButton; }
    public JButton getBackToSeatsButton() { return backToSeatsButton; }
    public JButton getCancelButton() { return cancelButton; }
    public DigitalTicketCard getDigitalTicketCard() { return digitalTicketCard; }
}
