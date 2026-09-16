package com.movieticket.view;

import com.movieticket.model.Seat;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.GlassCardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Arc2D;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

/**
 * Dynamic Seat Selection Screen — Premium Cinema Atmosphere.
 * Features illuminated curved screen arc, state-colored seat buttons:
 * Available (Green), Booked (Red), Selected (Yellow glow), Blocked (Gray).
 */
public class SeatSelectionView extends JPanel {

    // Show Header details
    private JLabel lblShowInfo;
    private JLabel lblMovieTitle;

    // Dynamic Seat Map Container
    private JPanel seatMapContainer;
    private final Map<Integer, JToggleButton> seatButtonMap = new LinkedHashMap<>();
    private final Map<Integer, Seat> seatObjectMap = new LinkedHashMap<>();

    // Summary Card
    private JLabel lblSummaryMovie;
    private JLabel lblSummaryTheatre;
    private JLabel lblSummaryDateTime;
    private JLabel lblSummaryPrice;
    private JLabel selectedSeatsLabel;
    private JLabel ticketCountLabel;
    private JLabel totalAmountLabel;
    private JLabel lblHoldCountdown;

    // Smart Seat & Group Booking Controls
    private JButton btnFindBestSeats;
    private JComboBox<String> groupCountCombo;
    private java.math.BigDecimal activeTicketPrice = java.math.BigDecimal.ZERO;

    // Action Buttons
    private JButton confirmBookingButton;
    private JButton clearSelectionButton;
    private JButton backButton;

    public SeatSelectionView() {
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initComponents();
    }

    private void initComponents() {
        // 1. Top Panel: Title, Show Metadata, and Curved Screen Banner
        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setOpaque(false);

        JPanel headerTextPanel = new JPanel(new BorderLayout());
        headerTextPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Select Cinema Seats");
        titleLabel.setFont(ThemeManager.getPageTitleFont());
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);
        headerTextPanel.add(titleLabel, BorderLayout.WEST);

        lblMovieTitle = new JLabel("Movie: —");
        lblMovieTitle.setFont(ThemeManager.getSectionHeaderFont());
        lblMovieTitle.setForeground(CineBookTheme.ACCENT_GOLD);
        headerTextPanel.add(lblMovieTitle, BorderLayout.EAST);

        topPanel.add(headerTextPanel, BorderLayout.NORTH);

        // Show metadata sub-bar with Smart Seat & Group Booking Controls
        JPanel subBar = new JPanel(new BorderLayout(10, 0));
        subBar.setOpaque(false);

        lblShowInfo = new JLabel("Theatre: — | Screen: — | Date & Time: — | Price: ₹0.00");
        lblShowInfo.setFont(ThemeManager.getBodyFont());
        lblShowInfo.setForeground(CineBookTheme.TEXT_MUTED);
        subBar.add(lblShowInfo, BorderLayout.WEST);

        JPanel smartControlsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        smartControlsBox.setOpaque(false);

        JLabel groupLbl = new JLabel("Group Size:");
        groupLbl.setFont(ThemeManager.getSmallFont());
        groupLbl.setForeground(CineBookTheme.TEXT_MUTED);
        smartControlsBox.add(groupLbl);

        groupCountCombo = new JComboBox<>(new String[]{"1 Seat", "2 Seats", "3 Seats", "4 Seats", "5 Seats", "6 Seats"});
        ThemeManager.styleComboBox(groupCountCombo);
        groupCountCombo.setSelectedIndex(1);
        groupCountCombo.setPreferredSize(new Dimension(95, 30));
        smartControlsBox.add(groupCountCombo);

        btnFindBestSeats = new JButton("✦ FIND BEST SEATS", org.kordamp.ikonli.swing.FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.MAGIC, 12, Color.WHITE));
        ThemeManager.stylePrimaryButton(btnFindBestSeats);
        btnFindBestSeats.setFont(ThemeManager.getFont(Font.BOLD, 12));
        btnFindBestSeats.setPreferredSize(new Dimension(160, 30));
        btnFindBestSeats.addActionListener(e -> triggerSmartSeatRecommendation());
        smartControlsBox.add(btnFindBestSeats);

        subBar.add(smartControlsBox, BorderLayout.EAST);
        topPanel.add(subBar, BorderLayout.CENTER);

        // Cinema Screen Curved Arc Banner
        JPanel screenPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Curved Screen Glow Line
                g2.setColor(new Color(6, 182, 212, 180)); // Cyan glow
                g2.setStroke(new BasicStroke(4.0f));
                g2.draw(new Arc2D.Double(w * 0.15, 6, w * 0.70, 30, 0, 180, Arc2D.OPEN));

                g2.setColor(CineBookTheme.TEXT_PRIMARY);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new Arc2D.Double(w * 0.15, 8, w * 0.70, 30, 0, 180, Arc2D.OPEN));
            }
        };
        screenPanel.setOpaque(false);
        screenPanel.setPreferredSize(new Dimension(800, 44));
        screenPanel.setLayout(new GridBagLayout());

        JLabel screenLabel = new JLabel("────────────────  CINEMA SCREEN  ────────────────");
        screenLabel.setFont(ThemeManager.getFont(Font.BOLD, 12));
        screenLabel.setForeground(CineBookTheme.ACCENT_CYAN);
        screenPanel.add(screenLabel);
        topPanel.add(screenPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // 2. Center Panel: Dynamic Seat Map inside JScrollPane + Legend
        GlassCardPanel centerContainer = new GlassCardPanel(new BorderLayout(8, 8));
        centerContainer.setTopAccent(CineBookTheme.ACCENT_PURPLE, 3);

        seatMapContainer = new com.movieticket.view.components.ResponsiveGridPanel();
        seatMapContainer.setLayout(new BoxLayout(seatMapContainer, BoxLayout.Y_AXIS));

        JScrollPane seatScrollPane = new JScrollPane(seatMapContainer);
        seatScrollPane.setBorder(BorderFactory.createEmptyBorder());
        seatScrollPane.getViewport().setBackground(CineBookTheme.BG_CARD);
        centerContainer.add(seatScrollPane, BorderLayout.CENTER);

        // Legend at bottom of seat map
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        legendPanel.setOpaque(false);
        legendPanel.setBorder(new LineBorder(CineBookTheme.BORDER_COLOR, 1));

        legendPanel.add(createLegendItem("Available", CineBookTheme.BG_INPUT, CineBookTheme.SUCCESS_COLOR, CineBookTheme.SUCCESS_COLOR));
        legendPanel.add(createLegendItem("Booked", CineBookTheme.BG_HEADER, CineBookTheme.DANGER_COLOR, CineBookTheme.DANGER_COLOR));
        legendPanel.add(createLegendItem("Held by You", new Color(40, 20, 60), CineBookTheme.ACCENT_PURPLE, CineBookTheme.ACCENT_PURPLE));
        legendPanel.add(createLegendItem("Held by Other", CineBookTheme.BG_HEADER, CineBookTheme.TEXT_MUTED, CineBookTheme.TEXT_MUTED));
        legendPanel.add(createLegendItem("Selected", CineBookTheme.ACCENT_GOLD, CineBookTheme.TEXT_PRIMARY, CineBookTheme.ACCENT_GOLD));
        legendPanel.add(createLegendItem("Blocked", CineBookTheme.BG_HEADER, CineBookTheme.TEXT_MUTED, CineBookTheme.BORDER_COLOR));

        centerContainer.add(legendPanel, BorderLayout.SOUTH);

        // Layout the 70/30 split
        JPanel splitPanel = new JPanel(new GridBagLayout());
        splitPanel.setOpaque(false);
        
        GridBagConstraints splitGbc = new GridBagConstraints();
        splitGbc.fill = GridBagConstraints.BOTH;
        splitGbc.insets = new Insets(0, 0, 0, 15);
        splitGbc.gridx = 0;
        splitGbc.gridy = 0;
        splitGbc.weighty = 1.0;
        splitGbc.weightx = 0.70; // 70% for seats
        
        splitPanel.add(centerContainer, splitGbc);
        GlassCardPanel rightCard = new GlassCardPanel(new GridBagLayout());
        rightCard.setPreferredSize(new Dimension(330, 420));
        rightCard.setTopAccent(CineBookTheme.ACCENT_GOLD, 3);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        int row = 0;
        gbc.gridy = row++;
        JLabel summaryHeader = new JLabel("Booking Summary");
        summaryHeader.setFont(ThemeManager.getSectionHeaderFont());
        summaryHeader.setForeground(CineBookTheme.TEXT_PRIMARY);
        rightCard.add(summaryHeader, gbc);

        gbc.gridy = row++;
        JSeparator sep1 = new JSeparator();
        sep1.setForeground(CineBookTheme.BORDER_COLOR);
        rightCard.add(sep1, gbc);

        gbc.gridy = row++;
        lblSummaryMovie = createSummaryItem("Movie:", "—");
        rightCard.add(lblSummaryMovie, gbc);

        gbc.gridy = row++;
        lblSummaryTheatre = createSummaryItem("Theatre & Screen:", "—");
        rightCard.add(lblSummaryTheatre, gbc);

        gbc.gridy = row++;
        lblSummaryDateTime = createSummaryItem("Date & Time:", "—");
        rightCard.add(lblSummaryDateTime, gbc);

        gbc.gridy = row++;
        lblSummaryPrice = createSummaryItem("Price / Ticket:", "₹0.00");
        rightCard.add(lblSummaryPrice, gbc);

        gbc.gridy = row++;
        JLabel lblSeatsHeader = new JLabel("Selected Seats:");
        lblSeatsHeader.setFont(ThemeManager.getLabelFont());
        lblSeatsHeader.setForeground(CineBookTheme.TEXT_MUTED);
        rightCard.add(lblSeatsHeader, gbc);

        gbc.gridy = row++;
        selectedSeatsLabel = new JLabel("None Selected");
        selectedSeatsLabel.setFont(ThemeManager.getCardTitleFont());
        selectedSeatsLabel.setForeground(CineBookTheme.ACCENT_GOLD);
        rightCard.add(selectedSeatsLabel, gbc);

        gbc.gridy = row++;
        ticketCountLabel = new JLabel("Tickets: 0");
        ticketCountLabel.setFont(ThemeManager.getBodyFont());
        ticketCountLabel.setForeground(CineBookTheme.TEXT_MUTED);
        rightCard.add(ticketCountLabel, gbc);

        gbc.gridy = row++;
        lblHoldCountdown = new JLabel(" ");
        lblHoldCountdown.setFont(ThemeManager.getFont(Font.BOLD, 12));
        lblHoldCountdown.setForeground(CineBookTheme.ACCENT_CYAN);
        rightCard.add(lblHoldCountdown, gbc);

        gbc.gridy = row++;
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(CineBookTheme.BORDER_COLOR);
        rightCard.add(sep2, gbc);

        gbc.gridy = row++;
        JLabel totalTitle = new JLabel("Total Amount Payable:");
        totalTitle.setFont(ThemeManager.getLabelFont());
        totalTitle.setForeground(CineBookTheme.TEXT_PRIMARY);
        rightCard.add(totalTitle, gbc);

        gbc.gridy = row++;
        totalAmountLabel = new JLabel("₹0.00");
        totalAmountLabel.setFont(ThemeManager.getFont(Font.BOLD, 26));
        totalAmountLabel.setForeground(CineBookTheme.SUCCESS_COLOR);
        rightCard.add(totalAmountLabel, gbc);

        // Action buttons
        gbc.gridy = row++;
        gbc.insets = new Insets(14, 4, 6, 4);
        confirmBookingButton = new JButton("Proceed to Confirm");
        ThemeManager.stylePrimaryButton(confirmBookingButton);
        confirmBookingButton.setPreferredSize(new Dimension(260, 40));
        rightCard.add(confirmBookingButton, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(4, 4, 4, 4);
        clearSelectionButton = new JButton("Clear Selection");
        ThemeManager.styleSecondaryButton(clearSelectionButton);
        clearSelectionButton.setPreferredSize(new Dimension(260, 36));
        rightCard.add(clearSelectionButton, gbc);

        gbc.gridy = row++;
        backButton = new JButton("Back to Showtimes");
        ThemeManager.styleSecondaryButton(backButton);
        backButton.setPreferredSize(new Dimension(260, 36));
        rightCard.add(backButton, gbc);

        splitGbc.gridx = 1;
        splitGbc.weightx = 0.30; // 30% for summary
        splitGbc.insets = new Insets(0, 0, 0, 0);
        
        splitPanel.add(rightCard, splitGbc);
        add(splitPanel, BorderLayout.CENTER);
    }

    private JLabel createSummaryItem(String title, String value) {
        JLabel l = new JLabel("<html><font color='#94A3B8'><b>" + title + "</b></font> <font color='#F8FAFC'>" + value + "</font></html>");
        l.setFont(ThemeManager.getBodyFont());
        return l;
    }

    private JPanel createLegendItem(String label, Color bg, Color fg, Color border) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);

        JLabel box = new JLabel(" ");
        box.setOpaque(true);
        box.setBackground(bg);
        box.setPreferredSize(new Dimension(22, 22));
        box.setBorder(BorderFactory.createLineBorder(border, 2));
        p.add(box);

        JLabel text = new JLabel(label);
        text.setFont(ThemeManager.getLabelFont());
        text.setForeground(CineBookTheme.TEXT_PRIMARY);
        p.add(text);

        return p;
    }

    public void buildSeatMap(List<Seat> seats, Set<Integer> bookedSeatIds, Set<Integer> heldByMe, Set<Integer> heldByOther, ActionListener seatClickListener) {
        seatMapContainer.removeAll();
        seatButtonMap.clear();
        seatObjectMap.clear();

        if (seats == null || seats.isEmpty()) {
            JLabel emptyLabel = new JLabel("No seats configured for this theatre.", SwingConstants.CENTER);
            emptyLabel.setFont(ThemeManager.getCardTitleFont());
            emptyLabel.setForeground(CineBookTheme.TEXT_MUTED);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            seatMapContainer.add(Box.createVerticalGlue());
            seatMapContainer.add(emptyLabel);
            seatMapContainer.add(Box.createVerticalGlue());
            seatMapContainer.revalidate();
            seatMapContainer.repaint();
            return;
        }

        // Group seats by row
        Map<String, List<Seat>> seatsByRow = new LinkedHashMap<>();
        for (Seat s : seats) {
            String rowName = s.getRowName();
            if (rowName == null || rowName.isEmpty()) {
                String sNum = s.getSeatNumber().trim();
                rowName = sNum.replaceAll("\\d+", "");
                if (rowName.isEmpty()) rowName = "A";
            }
            seatsByRow.computeIfAbsent(rowName, k -> new ArrayList<>()).add(s);
        }

        for (Map.Entry<String, List<Seat>> entry : seatsByRow.entrySet()) {
            String rowName = entry.getKey();
            List<Seat> rowSeats = entry.getValue();

            com.movieticket.view.components.ResponsiveGridPanel rowPanel = new com.movieticket.view.components.ResponsiveGridPanel(new com.movieticket.view.components.WrapLayout(FlowLayout.CENTER, 10, 6));

            JLabel rowLabel = new JLabel("Row " + rowName);
            rowLabel.setFont(ThemeManager.getLabelFont());
            rowLabel.setForeground(CineBookTheme.TEXT_MUTED);
            rowLabel.setPreferredSize(new Dimension(60, 42));
            rowPanel.add(rowLabel);

            for (Seat seat : rowSeats) {
                int seatId = seat.getSeatId();
                String seatNum = seat.getSeatNumber();
                String category = seat.getSeatType() != null ? seat.getSeatType().toUpperCase() : "REGULAR";
                boolean isBooked = bookedSeatIds.contains(seatId) || "INACTIVE".equalsIgnoreCase(seat.getStatus());

                com.movieticket.view.components.AnimatedSeatButton btn = new com.movieticket.view.components.AnimatedSeatButton(seatNum);
                if (isBooked) {
                    btn.setSeatState(com.movieticket.view.components.AnimatedSeatButton.SeatState.BOOKED);
                    btn.setToolTipText("Seat " + seatNum + " [" + category + "] - BOOKED");
                } else if (heldByOther != null && heldByOther.contains(seatId)) {
                    btn.setSeatState(com.movieticket.view.components.AnimatedSeatButton.SeatState.HELD_BY_OTHER);
                    btn.setToolTipText("Seat " + seatNum + " [" + category + "] - Held by Another User");
                } else if (heldByMe != null && heldByMe.contains(seatId)) {
                    btn.setSeatState(com.movieticket.view.components.AnimatedSeatButton.SeatState.HELD_BY_ME);
                    btn.setToolTipText("Seat " + seatNum + " [" + category + "] - Held by You");
                    if (seatClickListener != null) {
                        btn.addActionListener(seatClickListener);
                    }
                } else {
                    btn.setSeatState(com.movieticket.view.components.AnimatedSeatButton.SeatState.AVAILABLE);
                    btn.setToolTipText("Seat " + seatNum + " [" + category + "] - Available");
                    if (seatClickListener != null) {
                        btn.addActionListener(seatClickListener);
                    }
                }

                rowPanel.add(btn);
                seatButtonMap.put(seatId, btn);
                seatObjectMap.put(seatId, seat);
            }

            seatMapContainer.add(rowPanel);
        }

        seatMapContainer.revalidate();
        seatMapContainer.repaint();
    }

    public void updateSummary(String movieTitle, String theatreName, String screen, String dateTime, String selectedSeatsHtml, int totalTickets, BigDecimal totalAmount) {
        updateShowDetailsAndSummary(movieTitle, theatreName, screen, dateTime, selectedSeatsHtml, totalTickets, totalAmount);
    }

    public void setHoldCountdownText(String text) {
        if (lblHoldCountdown != null) {
            lblHoldCountdown.setText(text);
        }
    }

    public void updateShowDetailsAndSummary(
            String movieTitle, String theatreName, String screen, String dateTime, String selectedSeatsHtml, int totalTickets, BigDecimal totalAmount) {
        lblMovieTitle.setText("Movie: " + (movieTitle != null ? movieTitle : "—"));
        lblShowInfo.setText("Theatre: " + (theatreName != null ? theatreName : "—") +
                " | Screen: " + (screen != null ? screen : "1") +
                " | Date & Time: " + (dateTime != null ? dateTime : "—"));

        lblSummaryMovie.setText("<html><font color='#94A3B8'><b>Movie:</b></font> <font color='#F8FAFC'>" + (movieTitle != null ? movieTitle : "—") + "</font></html>");
        lblSummaryTheatre.setText("<html><font color='#94A3B8'><b>Theatre & Screen:</b></font> <font color='#F8FAFC'>" + (theatreName != null ? theatreName : "—") + " (" + (screen != null ? screen : "1") + ")</font></html>");
        lblSummaryDateTime.setText("<html><font color='#94A3B8'><b>Date & Time:</b></font> <font color='#F8FAFC'>" + (dateTime != null ? dateTime : "—") + "</font></html>");
        lblSummaryPrice.setText("<html><font color='#94A3B8'><b>Pricing:</b></font> <font color='#F8FAFC'>Dynamic & Category based</font></html>");

        if (totalTickets == 0) {
            selectedSeatsLabel.setText("None Selected");
            ticketCountLabel.setText("Tickets: 0");
            totalAmountLabel.setText("₹0.00");
        } else {
            selectedSeatsLabel.setText(selectedSeatsHtml);
            ticketCountLabel.setText("Tickets: " + totalTickets);
            totalAmountLabel.setText("₹" + (totalAmount != null ? totalAmount.setScale(2).toString() : "0.00"));
        }
    }

    public void clearSelection() {
        for (JToggleButton btn : seatButtonMap.values()) {
            if (btn.isEnabled()) {
                btn.setSelected(false);
                btn.setBackground(CineBookTheme.BG_INPUT);
                btn.setForeground(CineBookTheme.TEXT_PRIMARY);
                btn.setBorder(BorderFactory.createLineBorder(CineBookTheme.SUCCESS_COLOR, 1));
            }
        }
        selectedSeatsLabel.setText("None Selected");
        ticketCountLabel.setText("Tickets: 0");
        totalAmountLabel.setText("₹0.00");
    }

    public List<Seat> getSelectedSeats() {
        List<Seat> selected = new ArrayList<>();
        for (Map.Entry<Integer, JToggleButton> entry : seatButtonMap.entrySet()) {
            if (entry.getValue().isSelected()) {
                Seat s = seatObjectMap.get(entry.getKey());
                if (s != null) selected.add(s);
            }
        }
        return selected;
    }

    public void revertSelectionTo(List<Seat> previousSelection) {
        Set<Integer> previousIds = new HashSet<>();
        if (previousSelection != null) {
            for (Seat s : previousSelection) {
                previousIds.add(s.getSeatId());
            }
        }

        for (Map.Entry<Integer, JToggleButton> entry : seatButtonMap.entrySet()) {
            JToggleButton btn = entry.getValue();
            if (!btn.isEnabled()) continue;

            boolean shouldBeSelected = previousIds.contains(entry.getKey());
            if (btn.isSelected() != shouldBeSelected) {
                btn.setSelected(shouldBeSelected);
                if (shouldBeSelected) {
                    btn.setBackground(CineBookTheme.ACCENT_GOLD);
                    btn.setForeground(CineBookTheme.TEXT_PRIMARY);
                    btn.setBorder(BorderFactory.createLineBorder(CineBookTheme.ACCENT_GOLD, 2));
                } else {
                    btn.setBackground(CineBookTheme.BG_INPUT);
                    btn.setForeground(CineBookTheme.TEXT_PRIMARY);
                    btn.setBorder(BorderFactory.createLineBorder(CineBookTheme.SUCCESS_COLOR, 1));
                }
            }
        }
    }

    // Fast refresh states without rebuilding the map
    public void refreshSeatStates(Set<Integer> bookedSeatIds, Set<Integer> heldByMe, Set<Integer> heldByOther) {
        for (Map.Entry<Integer, JToggleButton> entry : seatButtonMap.entrySet()) {
            int seatId = entry.getKey();
            com.movieticket.view.components.AnimatedSeatButton btn = (com.movieticket.view.components.AnimatedSeatButton) entry.getValue();
            Seat seat = seatObjectMap.get(seatId);
            if (seat == null) continue;

            boolean isBooked = bookedSeatIds.contains(seatId) || "INACTIVE".equalsIgnoreCase(seat.getStatus());
            
            if (isBooked) {
                btn.setSeatState(com.movieticket.view.components.AnimatedSeatButton.SeatState.BOOKED);
            } else if (heldByOther != null && heldByOther.contains(seatId)) {
                btn.setSeatState(com.movieticket.view.components.AnimatedSeatButton.SeatState.HELD_BY_OTHER);
            } else if (heldByMe != null && heldByMe.contains(seatId)) {
                // If it's already selected/held by me in the UI, we just ensure it has HELD_BY_ME state
                if (!btn.isSelected()) {
                    btn.setSeatState(com.movieticket.view.components.AnimatedSeatButton.SeatState.HELD_BY_ME);
                }
            } else {
                if (btn.isSelected()) {
                    // It is selected locally but not held? We will keep it selected visually until the server says it's held by someone else
                    btn.setSeatState(com.movieticket.view.components.AnimatedSeatButton.SeatState.SELECTED);
                } else {
                    btn.setSeatState(com.movieticket.view.components.AnimatedSeatButton.SeatState.AVAILABLE);
                }
            }
        }
    }

    // Getters
    public JButton getConfirmBookingButton() { return confirmBookingButton; }
    public JButton getClearSelectionButton() { return clearSelectionButton; }
    public JButton getBackButton() { return backButton; }
    public JLabel getSelectedSeatsLabel() { return selectedSeatsLabel; }
    public JLabel getTotalAmountLabel() { return totalAmountLabel; }

    public void triggerSmartSeatRecommendation() {
        if (seatObjectMap == null || seatObjectMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seat data is loading. Please select a showtime first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String countStr = (String) groupCountCombo.getSelectedItem();
        int requestedCount = 2;
        try {
            requestedCount = Integer.parseInt(countStr.replaceAll("[^0-9]", ""));
        } catch (Exception ignored) {}

        List<Seat> allSeats = new ArrayList<>(seatObjectMap.values());
        Set<Integer> bookedSeatIds = new HashSet<>();
        for (Map.Entry<Integer, JToggleButton> entry : seatButtonMap.entrySet()) {
            if (!entry.getValue().isEnabled()) {
                bookedSeatIds.add(entry.getKey());
            }
        }

        com.movieticket.util.SmartSeatRecommendationEngine.RecommendationResult result =
                com.movieticket.util.SmartSeatRecommendationEngine.recommendSeats(allSeats, bookedSeatIds, requestedCount);

        if (result.getRecommendedSeats().isEmpty()) {
            JOptionPane.showMessageDialog(this, result.getRationale(), "Smart Seat Recommendation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        showRecommendationDialog(result);
    }

    private void showRecommendationDialog(com.movieticket.util.SmartSeatRecommendationEngine.RecommendationResult result) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Smart Seat Recommendation", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(440, 320);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout(14, 14));
        p.setBackground(CineBookTheme.BG_PRIMARY);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CineBookTheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        // Header
        JLabel titleLbl = new JLabel("✦ SMART SEAT RECOMMENDATION", SwingConstants.LEFT);
        titleLbl.setFont(ThemeManager.getHeadingFont(Font.BOLD, 16));
        titleLbl.setForeground(CineBookTheme.TEXT_PRIMARY);
        p.add(titleLbl, BorderLayout.NORTH);

        // Center Content Card
        GlassCardPanel card = new GlassCardPanel(new BorderLayout(10, 10));
        card.setTopAccent(CineBookTheme.ACCENT_PURPLE, 3);
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JPanel centerBox = new JPanel();
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
        centerBox.setOpaque(false);

        JLabel lblSeatsHeader = new JLabel("Recommended Seats:");
        lblSeatsHeader.setFont(ThemeManager.getSmallFont());
        lblSeatsHeader.setForeground(CineBookTheme.TEXT_MUTED);

        JLabel lblSeatsVal = new JLabel(result.getFormattedSeatNumbers());
        lblSeatsVal.setFont(ThemeManager.getHeadingFont(Font.BOLD, 22));
        lblSeatsVal.setForeground(CineBookTheme.ACCENT_GOLD);

        JTextArea rationaleArea = new JTextArea(result.getRationale());
        rationaleArea.setFont(ThemeManager.getBodyFont());
        rationaleArea.setForeground(CineBookTheme.TEXT_SECONDARY);
        rationaleArea.setOpaque(false);
        rationaleArea.setEditable(false);

        centerBox.add(lblSeatsHeader);
        centerBox.add(Box.createVerticalStrut(4));
        centerBox.add(lblSeatsVal);
        centerBox.add(Box.createVerticalStrut(10));
        centerBox.add(rationaleArea);

        card.add(centerBox, BorderLayout.CENTER);
        p.add(card, BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnApply = new JButton("USE THESE SEATS");
        ThemeManager.stylePrimaryButton(btnApply);
        btnApply.setPreferredSize(new Dimension(160, 36));
        btnApply.addActionListener(e -> {
            applyRecommendedSeats(result.getRecommendedSeats());
            dlg.dispose();
        });

        JButton btnCancel = new JButton("Cancel");
        ThemeManager.styleSecondaryButton(btnCancel);
        btnCancel.setPreferredSize(new Dimension(90, 36));
        btnCancel.addActionListener(e -> dlg.dispose());

        btnPanel.add(btnApply);
        btnPanel.add(btnCancel);
        p.add(btnPanel, BorderLayout.SOUTH);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    private void applyRecommendedSeats(List<Seat> seats) {
        if (seats == null || seats.isEmpty()) return;

        clearSelection();

        for (Seat s : seats) {
            JToggleButton btn = seatButtonMap.get(s.getSeatId());
            if (btn != null && btn.isEnabled()) {
                btn.setSelected(true);
                btn.setBackground(CineBookTheme.ACCENT_GOLD);
                btn.setForeground(CineBookTheme.TEXT_PRIMARY);
                btn.setBorder(BorderFactory.createLineBorder(CineBookTheme.ACCENT_GOLD, 2));
            }
        }

        List<Seat> sel = getSelectedSeats();
        List<String> names = new ArrayList<>();
        for (Seat s : sel) {
            names.add(s.getSeatNumber());
        }
        selectedSeatsLabel.setText(String.join(", ", names));
        ticketCountLabel.setText("Tickets: " + sel.size());
        totalAmountLabel.setText("Check dynamic total at checkout");
    }
}
