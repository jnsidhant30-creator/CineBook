package com.movieticket.view.components;

import com.movieticket.model.Booking;
import com.movieticket.model.BookingSeat;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * DigitalTicketCard.java — Cinematic Digital Ticket Presentation Component.
 * Features coupon notch perforations, QR code vector graphic, high contrast movie metadata,
 * seat numbers, booking ID, and total cost breakdown.
 */
public class DigitalTicketCard extends JPanel {

    private String bookingReference;
    private String movieTitle;
    private String theatreName;
    private String screenName;
    private String showDate;
    private String showTime;
    private String seatNumbers;
    private String totalAmount;
    private String statusText;

    public DigitalTicketCard() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(380, 520));
        setBorder(new EmptyBorder(20, 24, 20, 24));

        setTicketData("CB-10024", "INCEPTION", "PVR CINEMAS — CITY MALL", "SCREEN 1",
                "12 AUG 2026", "10:00 AM", "A4 · A5", "₹540.00", "CONFIRMED");
    }

    public void setTicketData(String bookingRef, String movie, String theatre, String screen,
                              String date, String time, String seats, String total, String status) {
        this.bookingReference = bookingRef != null ? bookingRef : "CB-00000";
        this.movieTitle = movie != null ? movie.toUpperCase() : "MOVIE TITLE";
        this.theatreName = theatre != null ? theatre.toUpperCase() : "CINEMA THEATRE";
        this.screenName = screen != null ? screen : "SCREEN 1";
        this.showDate = date != null ? date : "—";
        this.showTime = time != null ? time : "—";
        this.seatNumbers = seats != null ? seats : "—";
        this.totalAmount = total != null ? total : "₹0.00";
        this.statusText = status != null ? status.toUpperCase() : "CONFIRMED";

        buildUI();
    }

    public void updateFromBooking(Booking booking, String movie, String theatre, String screen, String date, String time, String seats) {
        if (booking == null) return;

        String ref = "CB-" + booking.getBookingId();
        BigDecimal amt = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
        String totalStr = "₹" + amt.setScale(2).toString();

        setTicketData(ref, movie, theatre, screen, date, time, seats, totalStr, booking.getStatus());
    }

    private void buildUI() {
        removeAll();

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // 1. Header: CineBook Brand + Status Badge
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel logoLbl = new JLabel("CINEBOOK", FontIcon.of(FontAwesomeSolid.TICKET_ALT, 18, CineBookTheme.ACCENT_PURPLE), SwingConstants.LEFT);
        logoLbl.setFont(ThemeManager.getFont(Font.BOLD, 16));
        logoLbl.setForeground(CineBookTheme.TEXT_PRIMARY);
        logoLbl.setIconTextGap(8);
        headerRow.add(logoLbl, BorderLayout.WEST);

        JLabel statusLbl = new JLabel("● " + statusText);
        statusLbl.setFont(ThemeManager.getFont(Font.BOLD, 11));
        statusLbl.setForeground(CineBookTheme.SUCCESS_COLOR);
        headerRow.add(statusLbl, BorderLayout.EAST);

        contentPanel.add(headerRow);
        contentPanel.add(Box.createVerticalStrut(14));

        // 2. Movie Title (Large & Bold - NO POSTER)
        JLabel movieLbl = new JLabel("<html><div style='text-align: center; color: #F8FAFC;'>" + movieTitle + "</div></html>");
        movieLbl.setFont(ThemeManager.getFont(Font.BOLD, 22));
        movieLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(movieLbl);

        contentPanel.add(Box.createVerticalStrut(10));

        // 3. Theatre & Screen
        JLabel theatreLbl = new JLabel(theatreName + " • " + screenName, SwingConstants.CENTER);
        theatreLbl.setFont(ThemeManager.getFont(Font.BOLD, 12));
        theatreLbl.setForeground(CineBookTheme.ACCENT_CYAN);
        theatreLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(theatreLbl);

        contentPanel.add(Box.createVerticalStrut(16));

        // Separator Line
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(38, 54, 77));
        sep.setMaximumSize(new Dimension(320, 2));
        contentPanel.add(sep);
        contentPanel.add(Box.createVerticalStrut(16));

        // 4. Grid Details: Date/Time, Seats, Booking ID
        JPanel detailsGrid = new JPanel(new GridLayout(3, 2, 10, 10));
        detailsGrid.setOpaque(false);

        detailsGrid.add(createDetailItem("DATE", showDate, CineBookTheme.TEXT_PRIMARY));
        detailsGrid.add(createDetailItem("TIME", showTime, CineBookTheme.TEXT_PRIMARY));

        detailsGrid.add(createDetailItem("SEATS", seatNumbers, CineBookTheme.ACCENT_GOLD));
        detailsGrid.add(createDetailItem("BOOKING ID", bookingReference, CineBookTheme.ACCENT_PURPLE));

        detailsGrid.add(createDetailItem("TOTAL AMOUNT", totalAmount, CineBookTheme.SUCCESS_COLOR));
        detailsGrid.add(createDetailItem("PAYMENT", "PAID ONLINE", CineBookTheme.TEXT_MUTED));

        contentPanel.add(detailsGrid);
        contentPanel.add(Box.createVerticalStrut(20));

        // 5. Real ZXing QR Code Graphic
        String payload = "CINEBOOK|" + bookingReference + "|" + movieTitle + "|" + showDate + "|" + seatNumbers;
        java.awt.image.BufferedImage qrImage = com.movieticket.util.QRCodeUtils.generateQRCodeImage(payload, 100);

        JLabel qrImageLabel = new JLabel(new ImageIcon(qrImage));
        qrImageLabel.setPreferredSize(new Dimension(100, 100));
        qrImageLabel.setMaximumSize(new Dimension(100, 100));
        qrImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrImageLabel.setBorder(BorderFactory.createLineBorder(new Color(248, 250, 252), 2));

        contentPanel.add(qrImageLabel);

        add(contentPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel createDetailItem(String labelText, String valueText, Color valueColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ThemeManager.getSmallFont());
        lbl.setForeground(CineBookTheme.TEXT_MUTED);

        JLabel val = new JLabel(valueText);
        val.setFont(ThemeManager.getFont(Font.BOLD, 13));
        val.setForeground(valueColor);

        panel.add(lbl);
        panel.add(val);
        return panel;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Ticket Card Surface Fill (#0D1B2A @ 95% opacity)
        g2.setColor(new Color(13, 27, 42, 240));
        g2.fillRoundRect(0, 0, w - 1, h - 1, 16, 16);

        // 2. Coupon Perforation Side Notches (Left & Right Cutouts)
        int notchY = (int) (h * 0.65);
        int notchRadius = 14;

        g2.setColor(CineBookTheme.BG_PRIMARY);
        g2.fillArc(-notchRadius, notchY - notchRadius, notchRadius * 2, notchRadius * 2, 270, 180);
        g2.fillArc(w - notchRadius, notchY - notchRadius, notchRadius * 2, notchRadius * 2, 90, 180);

        // 3. Dashed Perforation Line across ticket
        g2.setColor(new Color(38, 54, 77));
        Stroke dashed = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{6.0f, 6.0f}, 0.0f);
        g2.setStroke(dashed);
        g2.drawLine(notchRadius + 4, notchY, w - notchRadius - 4, notchY);

        // 4. Ticket Border
        g2.setStroke(new BasicStroke(1.2f));
        g2.setColor(new Color(124, 58, 237, 100)); // Purple glow border
        g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);

        g2.dispose();
        super.paintComponent(g);
    }
}
