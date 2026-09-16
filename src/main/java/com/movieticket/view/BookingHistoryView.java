package com.movieticket.view;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.GlassCardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BookingHistoryView extends JPanel {

    private JPanel listPanel;
    private JScrollPane scrollPane;
    private JPanel emptyStatePanel;
    private JLabel emptyStateLabel;
    private String selectedBookingId = null;

    private JButton refreshButton;
    private JButton backButton;
    private JButton viewDetailsButton;
    private JButton cancelUserBookingButton;
    private JButton btnRateMovie;
    private JButton btnRateTheatre;

    private Runnable selectionListener;
    private Runnable doubleClickListener;

    public BookingHistoryView() {
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBackground(CineBookTheme.BG_PRIMARY);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        initComponents();
    }

    private void initComponents() {
        // 1. Header & Actions
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setOpaque(false);

        JLabel titleLabel = new JLabel("My Tickets & Bookings");
        titleLabel.setFont(ThemeManager.getFont(Font.BOLD, 36));
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Review your ticket reservation history, seats, and digital receipts.");
        subtitleLabel.setFont(ThemeManager.getFont(Font.PLAIN, 16));
        subtitleLabel.setForeground(CineBookTheme.TEXT_MUTED);

        titleContainer.add(titleLabel);
        titleContainer.add(Box.createVerticalStrut(8));
        titleContainer.add(subtitleLabel);
        headerPanel.add(titleContainer, BorderLayout.WEST);

        JPanel topButtons = new com.movieticket.view.components.ResponsiveGridPanel(new com.movieticket.view.components.WrapLayout(FlowLayout.RIGHT, 16, 0));
        topButtons.setOpaque(false);

        refreshButton = new JButton("Refresh");
        ThemeManager.stylePrimaryButton(refreshButton);
        refreshButton.setPreferredSize(new Dimension(140, 48));

        backButton = new JButton("Back to Dashboard");
        ThemeManager.styleSecondaryButton(backButton);
        backButton.setPreferredSize(new Dimension(180, 48));

        topButtons.add(refreshButton);
        topButtons.add(backButton);
        headerPanel.add(topButtons, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Main List Container
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        scrollPane = new JScrollPane(listPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Empty state container
        emptyStatePanel = new JPanel(new GridBagLayout());
        emptyStatePanel.setOpaque(false);
        emptyStateLabel = new JLabel("No bookings found. You haven't booked any movie tickets yet.");
        emptyStateLabel.setFont(ThemeManager.getFont(Font.PLAIN, 18));
        emptyStateLabel.setForeground(CineBookTheme.TEXT_MUTED);
        emptyStatePanel.add(emptyStateLabel);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(scrollPane, BorderLayout.CENTER);
        
        // Add empty state, we swap them in setEmptyStateVisible
        add(centerWrapper, BorderLayout.CENTER);

        // 3. Bottom Action Panel
        JPanel bottomActions = new com.movieticket.view.components.ResponsiveGridPanel(new com.movieticket.view.components.WrapLayout(FlowLayout.RIGHT, 16, 0));
        bottomActions.setOpaque(false);

        cancelUserBookingButton = new JButton("Cancel Booking");
        ThemeManager.styleDangerButton(cancelUserBookingButton);
        cancelUserBookingButton.setPreferredSize(new Dimension(180, 48));

        btnRateTheatre = new JButton("Rate Theatre");
        ThemeManager.styleSecondaryButton(btnRateTheatre);
        btnRateTheatre.setPreferredSize(new Dimension(160, 48));
        btnRateTheatre.setVisible(false);

        btnRateMovie = new JButton("Rate Movie");
        ThemeManager.styleSecondaryButton(btnRateMovie);
        btnRateMovie.setPreferredSize(new Dimension(160, 48));
        btnRateMovie.setVisible(false);

        viewDetailsButton = new JButton("View Digital Receipt");
        ThemeManager.styleSuccessButton(viewDetailsButton);
        viewDetailsButton.setPreferredSize(new Dimension(220, 48));

        bottomActions.add(cancelUserBookingButton);
        bottomActions.add(btnRateTheatre);
        bottomActions.add(btnRateMovie);
        bottomActions.add(viewDetailsButton);
        
        // wrap bottomActions to add top padding
        JPanel bottomWrapper = new JPanel(new BorderLayout());
        bottomWrapper.setOpaque(false);
        bottomWrapper.setBorder(new EmptyBorder(24, 0, 0, 0));
        bottomWrapper.add(bottomActions, BorderLayout.EAST);
        
        add(bottomWrapper, BorderLayout.SOUTH);
    }

    public void setEmptyStateVisible(boolean visible) {
        if (visible) {
            scrollPane.setViewportView(emptyStatePanel);
        } else {
            scrollPane.setViewportView(listPanel);
        }
        revalidate();
        repaint();
    }
    
    public void clearBookings() {
        listPanel.removeAll();
        selectedBookingId = null;
        revalidate();
        repaint();
    }
    
    public void addBookingCard(String bookingId, String bookingDate, String movie, String theatre, 
                               String screen, String showDate, String showTime, String seats, 
                               int tickets, String total, String status, String paymentMethod, String txnId) {
        
        GlassCardPanel card = new GlassCardPanel(new BorderLayout(20, 10));
        
        // Color coding based on status
        Color accentColor = CineBookTheme.ACCENT_BLUE;
        if ("CONFIRMED".equalsIgnoreCase(status)) accentColor = CineBookTheme.SUCCESS_COLOR;
        else if ("CANCELLED".equalsIgnoreCase(status)) accentColor = CineBookTheme.DANGER_COLOR;
        else if ("PENDING".equalsIgnoreCase(status)) accentColor = CineBookTheme.WARNING_COLOR;
        
        card.setTopAccent(accentColor, 4);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 2, 2, 2, CineBookTheme.BG_CARD),
            new EmptyBorder(20, 24, 20, 24)
        ));
        
        // Info Layout
        JPanel leftInfo = new JPanel(new GridLayout(3, 1, 0, 4));
        leftInfo.setOpaque(false);
        
        JLabel movieLbl = new JLabel(movie);
        movieLbl.setFont(ThemeManager.getFont(Font.BOLD, 22));
        movieLbl.setForeground(CineBookTheme.TEXT_PRIMARY);
        
        JLabel detailsLbl = new JLabel(String.format("%s | %s | %s %s", theatre, screen, showDate, showTime));
        detailsLbl.setFont(ThemeManager.getFont(Font.PLAIN, 14));
        detailsLbl.setForeground(CineBookTheme.TEXT_MUTED);
        
        JLabel seatsLbl = new JLabel(tickets + " Ticket(s): " + seats);
        seatsLbl.setFont(ThemeManager.getFont(Font.PLAIN, 14));
        seatsLbl.setForeground(CineBookTheme.TEXT_MUTED);
        
        leftInfo.add(movieLbl);
        leftInfo.add(detailsLbl);
        leftInfo.add(seatsLbl);
        
        // Right Side Layout
        JPanel rightInfo = new JPanel(new GridLayout(3, 1, 0, 4));
        rightInfo.setOpaque(false);
        
        JLabel idLbl = new JLabel("Booking " + bookingId, SwingConstants.RIGHT);
        idLbl.setFont(ThemeManager.getFont(Font.BOLD, 16));
        idLbl.setForeground(CineBookTheme.TEXT_PRIMARY);
        
        JLabel amountLbl = new JLabel(total, SwingConstants.RIGHT);
        amountLbl.setFont(ThemeManager.getFont(Font.BOLD, 20));
        amountLbl.setForeground(CineBookTheme.SUCCESS_COLOR);
        
        JLabel statusLbl = new JLabel(status.toUpperCase(), SwingConstants.RIGHT);
        statusLbl.setFont(ThemeManager.getFont(Font.BOLD, 14));
        statusLbl.setForeground(accentColor);
        
        rightInfo.add(idLbl);
        rightInfo.add(amountLbl);
        rightInfo.add(statusLbl);
        
        card.add(leftInfo, BorderLayout.CENTER);
        card.add(rightInfo, BorderLayout.EAST);
        
        // Selection Interaction
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Clear all borders
                for (Component c : listPanel.getComponents()) {
                    if (c instanceof GlassCardPanel) {
                        ((GlassCardPanel) c).setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(2, 2, 2, 2, CineBookTheme.BG_CARD),
                            new EmptyBorder(20, 24, 20, 24)
                        ));
                    }
                }
                // Highlight this
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(2, 2, 2, 2, CineBookTheme.ACCENT_PURPLE),
                    new EmptyBorder(20, 24, 20, 24)
                ));
                selectedBookingId = bookingId;
                
                if (selectionListener != null) selectionListener.run();
                if (e.getClickCount() == 2 && doubleClickListener != null) {
                    doubleClickListener.run();
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!bookingId.equals(selectedBookingId)) {
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(2, 2, 2, 2, CineBookTheme.BORDER_COLOR),
                        new EmptyBorder(20, 24, 20, 24)
                    ));
                    card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!bookingId.equals(selectedBookingId)) {
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(2, 2, 2, 2, CineBookTheme.BG_CARD),
                        new EmptyBorder(20, 24, 20, 24)
                    ));
                    card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
        
        // Wrapper for spacing
        JPanel cardWrapper = new JPanel(new BorderLayout());
        cardWrapper.setOpaque(false);
        cardWrapper.setBorder(new EmptyBorder(0, 0, 16, 0));
        cardWrapper.add(card, BorderLayout.CENTER);
        
        // Maximum width constraint
        cardWrapper.setMaximumSize(new Dimension(1600, 140));
        
        listPanel.add(cardWrapper);
        revalidate();
        repaint();
    }

    public void addSelectionListener(Runnable listener) {
        this.selectionListener = listener;
    }

    public void addDoubleClickListener(Runnable listener) {
        this.doubleClickListener = listener;
    }

    public String getSelectedBookingId() {
        return selectedBookingId;
    }

    // Getters
    public JButton getRefreshButton() { return refreshButton; }
    public JButton getBackButton() { return backButton; }
    public JButton getViewDetailsButton() { return viewDetailsButton; }
    public JButton getCancelUserBookingButton() { return cancelUserBookingButton; }
    public JButton getBtnRateMovie() { return btnRateMovie; }
    public JButton getBtnRateTheatre() { return btnRateTheatre; }
}
