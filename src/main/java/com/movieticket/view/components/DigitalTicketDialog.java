package com.movieticket.view.components;

import com.movieticket.model.Booking;
import com.movieticket.model.Movie;
import com.movieticket.model.Show;
import com.movieticket.model.Theatre;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.QrCodeService;
import com.movieticket.util.ThemeManager;
import com.movieticket.util.TicketPdfService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;
import java.awt.image.BufferedImage;
import java.io.File;

public class DigitalTicketDialog extends JDialog {

    private final Booking booking;
    private final Movie movie;
    private final Theatre theatre;
    private final Show show;
    private final String seats;
    private final String ticketId;

    private BufferedImage qrImage;
    private QrCodeService qrCodeService = new QrCodeService();
    private TicketPdfService ticketPdfService = new TicketPdfService();

    public DigitalTicketDialog(Window owner, Booking booking, Movie movie, Theatre theatre, Show show, String seats, String ticketId) {
        super(owner, "Digital Ticket", ModalityType.APPLICATION_MODAL);
        this.booking = booking;
        this.movie = movie;
        this.theatre = theatre;
        this.show = show;
        this.seats = seats;
        this.ticketId = ticketId;

        generateQr();
        initComponents();

        pack();
        setLocationRelativeTo(owner);
    }

    private void generateQr() {
        try {
            String dateStr = show.getShowDate() != null ? show.getShowDate().toString() : "";
            String timeStr = show.getShowTime() != null ? show.getShowTime().toString() : "";
            String qrData = String.format("CINEBOOK|BOOKING_ID=%s|MOVIE=%s|SHOW=%s %s|SEATS=%s",
                    ticketId, movie.getTitle(), dateStr, timeStr, seats);
            
            qrImage = qrCodeService.generateQrCodeImage(qrData, 200, 200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(CineBookTheme.BG_PRIMARY);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel(" Booking Confirmed!");
        titleLabel.setIcon(FontIcon.of(FontAwesomeSolid.CHECK_CIRCLE, 24, CineBookTheme.SUCCESS_COLOR));
        titleLabel.setFont(ThemeManager.getHeadingFont(Font.BOLD, 22));
        titleLabel.setForeground(CineBookTheme.ACCENT_CYAN);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subLabel = new JLabel("Your movie ticket has been booked successfully.");
        subLabel.setFont(ThemeManager.getBodyFont());
        subLabel.setForeground(CineBookTheme.TEXT_MUTED);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(subLabel);
        headerPanel.add(Box.createVerticalStrut(15));
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Ticket Card
        GlassCardPanel cardPanel = new GlassCardPanel(new BorderLayout(10, 10));
        cardPanel.setTopAccent(CineBookTheme.ACCENT_PURPLE, 4);
        cardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel detailsPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        detailsPanel.setOpaque(false);
        
        detailsPanel.add(createDetailRow("Movie", movie.getTitle()));
        detailsPanel.add(createDetailRow("Theatre", theatre.getTheatreName()));
        
        String dateStr = show.getShowDate() != null ? show.getShowDate().toString() : "";
        String timeStr = show.getShowTime() != null ? show.getShowTime().toString() : "";
        detailsPanel.add(createDetailRow("Date & Time", dateStr + "  " + timeStr));
        
        detailsPanel.add(createDetailRow("Seats", seats));
        detailsPanel.add(createDetailRow("Total", "Rs. " + booking.getTotalAmount().toString()));
        detailsPanel.add(createDetailRow("Booking ID", ticketId));
        detailsPanel.add(createDetailRow("Status", booking.getStatus()));
        
        cardPanel.add(detailsPanel, BorderLayout.CENTER);
        
        // QR Code Panel
        JPanel qrPanel = new JPanel(new BorderLayout());
        qrPanel.setOpaque(false);
        if (qrImage != null) {
            JLabel qrLabel = new JLabel(new ImageIcon(qrImage));
            qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
            qrPanel.add(qrLabel, BorderLayout.CENTER);
            
            JLabel scanLabel = new JLabel("Scan at theatre entrance");
            scanLabel.setFont(ThemeManager.getSmallFont());
            scanLabel.setForeground(CineBookTheme.TEXT_MUTED);
            scanLabel.setHorizontalAlignment(SwingConstants.CENTER);
            qrPanel.add(scanLabel, BorderLayout.SOUTH);
        } else {
            JLabel errorLabel = new JLabel("QR Code unavailable");
            errorLabel.setForeground(Color.RED);
            qrPanel.add(errorLabel, BorderLayout.CENTER);
        }
        
        cardPanel.add(qrPanel, BorderLayout.EAST);
        
        mainPanel.add(cardPanel, BorderLayout.CENTER);

        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);
        
        JButton downloadBtn = new JButton("Download Ticket");
        ThemeManager.stylePrimaryButton(downloadBtn);
        downloadBtn.addActionListener(e -> downloadPdf());
        
        JButton closeBtn = new JButton("Close");
        ThemeManager.styleSecondaryButton(closeBtn);
        closeBtn.addActionListener(e -> dispose());
        
        actionPanel.add(downloadBtn);
        actionPanel.add(closeBtn);
        
        mainPanel.add(actionPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createDetailRow(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JLabel lblName = new JLabel(label + ": ");
        lblName.setFont(ThemeManager.getBodyFont());
        lblName.setForeground(CineBookTheme.TEXT_MUTED);
        lblName.setPreferredSize(new Dimension(100, 20));
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(ThemeManager.getCardTitleFont());
        lblValue.setForeground(CineBookTheme.TEXT_PRIMARY);
        
        panel.add(lblName, BorderLayout.WEST);
        panel.add(lblValue, BorderLayout.CENTER);
        return panel;
    }
    
    private void downloadPdf() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Digital Ticket");
        fileChooser.setSelectedFile(new File("CineBook_Ticket_" + ticketId + ".pdf"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                ticketPdfService.generatePdf(booking, movie, theatre, show, seats, ticketId, qrImage, fileToSave);
                JOptionPane.showMessageDialog(this, "Ticket saved successfully to:\n" + fileToSave.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to save ticket: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
