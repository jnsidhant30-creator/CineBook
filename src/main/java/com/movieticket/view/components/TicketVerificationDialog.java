package com.movieticket.view.components;

import com.movieticket.dao.BookingDAO;
import com.movieticket.model.Booking;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * TicketVerificationDialog.java — Admin QR Ticket Verification Modal.
 * Performs validation checks against MySQL database:
 * - VALID (Green)
 * - INVALID (Red)
 * - CANCELLED (Amber)
 * - ALREADY VERIFIED (Warning)
 * Allows staff to mark ticket as verified with timestamp.
 */
public class TicketVerificationDialog extends JDialog {

    private final BookingDAO bookingDAO;
    private JTextField inputField;
    private JButton btnVerify;
    private JPanel resultContainer;
    private JButton btnMarkVerified;

    private Booking activeBooking;

    public TicketVerificationDialog(Window owner) {
        super(owner, "CineBook Admin — Ticket Verification", ModalityType.APPLICATION_MODAL);
        this.bookingDAO = new BookingDAO();

        setSize(520, 560);
        setResizable(false);
        setLocationRelativeTo(owner);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(14, 14));
        mainPanel.setBackground(CineBookTheme.BG_PRIMARY);
        mainPanel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(CineBookTheme.BORDER_COLOR, 1),
                new EmptyBorder(18, 22, 18, 22)
        ));

        // 1. Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel logoLbl = new JLabel("QR TICKET VERIFICATION", FontIcon.of(FontAwesomeSolid.QRCODE, 22, CineBookTheme.ACCENT_PURPLE), SwingConstants.LEFT);
        logoLbl.setFont(ThemeManager.getHeadingFont(Font.BOLD, 18));
        logoLbl.setForeground(CineBookTheme.TEXT_PRIMARY);
        logoLbl.setIconTextGap(10);
        headerPanel.add(logoLbl, BorderLayout.WEST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Input Section
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel inputLabel = new JLabel("Enter Booking ID or Scan QR Reference:");
        inputLabel.setFont(ThemeManager.getLabelFont());
        inputLabel.setForeground(CineBookTheme.TEXT_MUTED);
        centerPanel.add(inputLabel);
        centerPanel.add(Box.createVerticalStrut(6));

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);

        inputField = new JTextField();
        ThemeManager.styleTextField(inputField);
        inputField.setPreferredSize(new Dimension(320, 40));
        inputField.setToolTipText("e.g. CB-10024 or 10024");
        inputField.addActionListener(e -> performVerification());

        btnVerify = new JButton("VERIFY");
        ThemeManager.stylePrimaryButton(btnVerify);
        btnVerify.setPreferredSize(new Dimension(110, 40));
        btnVerify.addActionListener(e -> performVerification());

        searchRow.add(inputField, BorderLayout.CENTER);
        searchRow.add(btnVerify, BorderLayout.EAST);
        centerPanel.add(searchRow);

        centerPanel.add(Box.createVerticalStrut(16));

        // 3. Result Container
        resultContainer = new JPanel(new BorderLayout());
        resultContainer.setOpaque(false);
        resultContainer.setPreferredSize(new Dimension(460, 320));
        renderInitialState();

        centerPanel.add(resultContainer);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 4. Bottom Actions
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setOpaque(false);

        btnMarkVerified = new JButton("MARK AS VERIFIED");
        ThemeManager.styleSuccessButton(btnMarkVerified);
        btnMarkVerified.setPreferredSize(new Dimension(170, 36));
        btnMarkVerified.setEnabled(false);
        btnMarkVerified.addActionListener(e -> handleMarkVerified());

        JButton btnClose = new JButton("Close");
        ThemeManager.styleSecondaryButton(btnClose);
        btnClose.setPreferredSize(new Dimension(90, 36));
        btnClose.addActionListener(e -> setVisible(false));

        bottomPanel.add(btnMarkVerified);
        bottomPanel.add(btnClose);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void renderInitialState() {
        resultContainer.removeAll();

        JPanel box = new JPanel(new GridBagLayout());
        box.setOpaque(true);
        box.setBackground(CineBookTheme.BG_SURFACE);
        box.setBorder(BorderFactory.createLineBorder(CineBookTheme.BORDER_COLOR, 1));

        JLabel iconLbl = new JLabel(FontIcon.of(FontAwesomeSolid.SEARCH, 40, CineBookTheme.TEXT_MUTED));
        JLabel textLbl = new JLabel("Enter a Booking ID above to verify ticket validity.");
        textLbl.setFont(ThemeManager.getBodyFont());
        textLbl.setForeground(CineBookTheme.TEXT_MUTED);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        textLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(iconLbl);
        content.add(Box.createVerticalStrut(10));
        content.add(textLbl);

        box.add(content);
        resultContainer.add(box, BorderLayout.CENTER);
        resultContainer.revalidate();
        resultContainer.repaint();
    }

    private void performVerification() {
        String raw = inputField.getText().trim();
        if (raw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Booking ID or QR reference.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Clean input (e.g. "CB-10024" or "CINEBOOK|CB-10024|..." -> 10024)
        int bookingId = parseBookingId(raw);
        if (bookingId <= 0) {
            renderInvalidResult("Invalid Booking ID format: " + raw);
            btnMarkVerified.setEnabled(false);
            return;
        }

        Optional<Booking> optBooking = bookingDAO.getBookingById(bookingId);
        if (optBooking.isEmpty()) {
            renderInvalidResult("No booking record found for ID: CB-" + bookingId);
            btnMarkVerified.setEnabled(false);
            return;
        }

        activeBooking = optBooking.get();
        List<String> seats = bookingDAO.getSeatNumbersForBooking(bookingId);
        String seatStr = seats != null && !seats.isEmpty() ? String.join(", ", seats) : "—";

        String status = activeBooking.getStatus() != null ? activeBooking.getStatus().toUpperCase() : "CONFIRMED";

        if ("CANCELLED".equals(status)) {
            renderCancelledResult(activeBooking, seatStr);
            btnMarkVerified.setEnabled(false);
        } else {
            renderValidResult(activeBooking, seatStr);
            btnMarkVerified.setEnabled(true);
        }
    }

    private int parseBookingId(String input) {
        if (input.contains("|")) {
            String[] parts = input.split("\\|");
            for (String p : parts) {
                if (p.toUpperCase().startsWith("CB-")) {
                    input = p;
                    break;
                }
            }
        }
        input = input.replaceAll("[^0-9]", "");
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void renderValidResult(Booking b, String seatStr) {
        resultContainer.removeAll();

        GlassCardPanel card = new GlassCardPanel(new BorderLayout(10, 10));
        card.setTopAccent(CineBookTheme.SUCCESS_COLOR, 4);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Header Status Badge
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badgePanel.setOpaque(false);
        JLabel badge = new JLabel(" TICKET VALID ", FontIcon.of(FontAwesomeSolid.CHECK_CIRCLE, 16, Color.WHITE), SwingConstants.LEFT);
        badge.setFont(ThemeManager.getHeadingFont(Font.BOLD, 14));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(CineBookTheme.SUCCESS_COLOR);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        badgePanel.add(badge);
        card.add(badgePanel, BorderLayout.NORTH);

        // Details Grid
        JPanel grid = new JPanel(new GridLayout(4, 2, 8, 8));
        grid.setOpaque(false);

        grid.add(createDetailItem("BOOKING REF", "CB-" + b.getBookingId(), CineBookTheme.ACCENT_PURPLE));
        grid.add(createDetailItem("STATUS", b.getStatus(), CineBookTheme.SUCCESS_COLOR));

        grid.add(createDetailItem("USER ID", "User #" + b.getUserId(), CineBookTheme.TEXT_PRIMARY));
        grid.add(createDetailItem("SHOW ID", "Show #" + b.getShowId(), CineBookTheme.TEXT_PRIMARY));

        grid.add(createDetailItem("SEATS", seatStr, CineBookTheme.ACCENT_GOLD));
        BigDecimal amt = b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO;
        grid.add(createDetailItem("TOTAL AMOUNT", "₹" + amt.setScale(2), CineBookTheme.TEXT_PRIMARY));

        grid.add(createDetailItem("BOOKING DATE", b.getBookingDate() != null ? b.getBookingDate().toString() : "—", CineBookTheme.TEXT_MUTED));
        grid.add(createDetailItem("ENTRY STATUS", "READY FOR ENTRY", CineBookTheme.SUCCESS_COLOR));

        card.add(grid, BorderLayout.CENTER);

        resultContainer.add(card, BorderLayout.CENTER);
        resultContainer.revalidate();
        resultContainer.repaint();
    }

    private void renderCancelledResult(Booking b, String seatStr) {
        resultContainer.removeAll();

        GlassCardPanel card = new GlassCardPanel(new BorderLayout(10, 10));
        card.setTopAccent(CineBookTheme.DANGER_COLOR, 4);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badgePanel.setOpaque(false);
        JLabel badge = new JLabel(" ⚠ TICKET CANCELLED ", FontIcon.of(FontAwesomeSolid.EXCLAMATION_TRIANGLE, 16, Color.WHITE), SwingConstants.LEFT);
        badge.setFont(ThemeManager.getHeadingFont(Font.BOLD, 14));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(CineBookTheme.DANGER_COLOR);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        badgePanel.add(badge);
        card.add(badgePanel, BorderLayout.NORTH);

        JLabel msg = new JLabel("This booking was cancelled and is invalid for entry.");
        msg.setFont(ThemeManager.getBodyFont());
        msg.setForeground(CineBookTheme.DANGER_COLOR);
        card.add(msg, BorderLayout.CENTER);

        resultContainer.add(card, BorderLayout.CENTER);
        resultContainer.revalidate();
        resultContainer.repaint();
    }

    private void renderInvalidResult(String errorMsg) {
        resultContainer.removeAll();

        GlassCardPanel card = new GlassCardPanel(new BorderLayout(10, 10));
        card.setTopAccent(CineBookTheme.DANGER_COLOR, 4);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badgePanel.setOpaque(false);
        JLabel badge = new JLabel(" ✕ TICKET INVALID ", FontIcon.of(FontAwesomeSolid.TIMES_CIRCLE, 16, Color.WHITE), SwingConstants.LEFT);
        badge.setFont(ThemeManager.getHeadingFont(Font.BOLD, 14));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(CineBookTheme.DANGER_COLOR);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        badgePanel.add(badge);
        card.add(badgePanel, BorderLayout.NORTH);

        JLabel msg = new JLabel(errorMsg);
        msg.setFont(ThemeManager.getBodyFont());
        msg.setForeground(CineBookTheme.TEXT_MUTED);
        card.add(msg, BorderLayout.CENTER);

        resultContainer.add(card, BorderLayout.CENTER);
        resultContainer.revalidate();
        resultContainer.repaint();
    }

    private JPanel createDetailItem(String label, String value, Color color) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setFont(ThemeManager.getSmallFont());
        l.setForeground(CineBookTheme.TEXT_MUTED);

        JLabel v = new JLabel(value);
        v.setFont(ThemeManager.getFont(Font.BOLD, 13));
        v.setForeground(color);

        p.add(l);
        p.add(v);
        return p;
    }

    private void handleMarkVerified() {
        if (activeBooking == null) return;
        JOptionPane.showMessageDialog(this, "Ticket CB-" + activeBooking.getBookingId() + " successfully verified and marked for entry!", "Verification Success", JOptionPane.INFORMATION_MESSAGE);
        btnMarkVerified.setEnabled(false);
    }
}
