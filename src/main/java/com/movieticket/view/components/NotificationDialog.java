package com.movieticket.view.components;

import com.movieticket.model.Notification;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

/**
 * NotificationDialog.java — Premium Dark Glass Notification Dropdown Modal for Customer Portal.
 * Displays user notification history, relative timestamps, visual UNREAD vs READ states,
 * and handles click-to-read and "Mark All as Read" actions.
 */
public class NotificationDialog extends JDialog {

    private JPanel listContainer;
    private JButton btnMarkAllRead;
    private JButton btnRefresh;
    private JLabel countHeaderLabel;

    private Consumer<Notification> onNotificationClick;
    private Runnable onMarkAllRead;
    private Runnable onRefreshRequested;

    public NotificationDialog(Window owner, String title) {
        super(owner, title, ModalityType.MODELESS);
        setSize(480, 560);
        setResizable(false);
        setLocationRelativeTo(owner);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(CineBookTheme.BG_PRIMARY);
        mainPanel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(CineBookTheme.BORDER_COLOR, 1),
                new EmptyBorder(14, 16, 14, 16)
        ));

        // 1. Top Header Bar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleBox.setOpaque(false);
        JLabel bellIcon = new JLabel(FontIcon.of(FontAwesomeSolid.BELL, 20, CineBookTheme.ACCENT_PURPLE));
        JLabel titleLabel = new JLabel("NOTIFICATIONS");
        titleLabel.setFont(ThemeManager.getHeadingFont(Font.BOLD, 18));
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);

        countHeaderLabel = new JLabel("(0 Unread)");
        countHeaderLabel.setFont(ThemeManager.getBodyFont());
        countHeaderLabel.setForeground(CineBookTheme.TEXT_MUTED);

        titleBox.add(bellIcon);
        titleBox.add(titleLabel);
        titleBox.add(countHeaderLabel);
        headerPanel.add(titleBox, BorderLayout.WEST);

        // Action buttons
        JPanel actionBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionBox.setOpaque(false);

        btnMarkAllRead = new JButton("Mark All Read");
        ThemeManager.styleSecondaryButton(btnMarkAllRead);
        btnMarkAllRead.setFont(ThemeManager.getSmallFont());
        btnMarkAllRead.setPreferredSize(new Dimension(110, 30));
        btnMarkAllRead.addActionListener(e -> {
            if (onMarkAllRead != null) onMarkAllRead.run();
        });

        btnRefresh = new JButton(FontIcon.of(FontAwesomeSolid.SYNC_ALT, 12, CineBookTheme.TEXT_SECONDARY));
        ThemeManager.styleSecondaryButton(btnRefresh);
        btnRefresh.setPreferredSize(new Dimension(32, 30));
        btnRefresh.setToolTipText("Refresh Notifications");
        btnRefresh.addActionListener(e -> {
            if (onRefreshRequested != null) onRefreshRequested.run();
        });

        actionBox.add(btnMarkAllRead);
        actionBox.add(btnRefresh);
        headerPanel.add(actionBox, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Notification List Container (Scrollable)
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(CineBookTheme.BG_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Close Bar
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        bottomPanel.setOpaque(false);
        JButton btnClose = new JButton("Close");
        ThemeManager.styleSecondaryButton(btnClose);
        btnClose.setPreferredSize(new Dimension(90, 32));
        btnClose.addActionListener(e -> setVisible(false));
        bottomPanel.add(btnClose);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    public void setNotifications(List<Notification> notifications, int unreadCount) {
        listContainer.removeAll();
        countHeaderLabel.setText("(" + unreadCount + " Unread)");

        if (notifications == null || notifications.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setOpaque(false);
            emptyPanel.setPreferredSize(new Dimension(420, 320));

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);

            JLabel iconLbl = new JLabel(FontIcon.of(FontAwesomeSolid.BELL_SLASH, 48, CineBookTheme.TEXT_MUTED), SwingConstants.CENTER);
            iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel titleLbl = new JLabel("No Notifications Yet", SwingConstants.CENTER);
            titleLbl.setFont(ThemeManager.getHeadingFont(Font.BOLD, 16));
            titleLbl.setForeground(CineBookTheme.TEXT_PRIMARY);
            titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel descLbl = new JLabel("Your booking updates and reminders will appear here.", SwingConstants.CENTER);
            descLbl.setFont(ThemeManager.getBodyFont());
            descLbl.setForeground(CineBookTheme.TEXT_MUTED);
            descLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            content.add(iconLbl);
            content.add(Box.createVerticalStrut(12));
            content.add(titleLbl);
            content.add(Box.createVerticalStrut(4));
            content.add(descLbl);

            emptyPanel.add(content);
            listContainer.add(emptyPanel);
        } else {
            for (Notification n : notifications) {
                listContainer.add(createNotificationCard(n));
                listContainer.add(Box.createVerticalStrut(8));
            }
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel createNotificationCard(Notification n) {
        boolean unread = !n.isReadStatus();

        // Card Panel with Left Accent border for UNREAD items
        JPanel card = new JPanel(new BorderLayout(12, 6));
        card.setOpaque(true);
        card.setBackground(unread ? CineBookTheme.BG_CARD : CineBookTheme.BG_SURFACE);
        card.setBorder(new CompoundBorder(
                new MatteBorder(0, unread ? 4 : 0, 0, 0, CineBookTheme.ACCENT_PURPLE),
                new CompoundBorder(
                        BorderFactory.createLineBorder(unread ? CineBookTheme.ACCENT_PURPLE : CineBookTheme.BORDER_COLOR, 1),
                        new EmptyBorder(10, 12, 10, 12)
                )
        ));
        card.setMaximumSize(new Dimension(2000, 110));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Type Icon
        FontAwesomeSolid iconEnum = getIconForType(n.getNotificationType());
        Color iconColor = getIconColorForType(n.getNotificationType());
        JLabel typeIconLabel = new JLabel(FontIcon.of(iconEnum, 22, iconColor));
        typeIconLabel.setVerticalAlignment(SwingConstants.TOP);
        card.add(typeIconLabel, BorderLayout.WEST);

        // Center Content (Title, Message, Timestamp)
        JPanel centerBox = new JPanel();
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
        centerBox.setOpaque(false);

        JLabel titleLbl = new JLabel(n.getTitle());
        titleLbl.setFont(ThemeManager.getHeadingFont(unread ? Font.BOLD : Font.PLAIN, 14));
        titleLbl.setForeground(unread ? CineBookTheme.TEXT_PRIMARY : CineBookTheme.TEXT_SECONDARY);

        JTextArea msgArea = new JTextArea(n.getMessage());
        msgArea.setFont(ThemeManager.getFont(Font.PLAIN, 12));
        msgArea.setForeground(unread ? CineBookTheme.TEXT_SECONDARY : CineBookTheme.TEXT_MUTED);
        msgArea.setWrapStyleWord(true);
        msgArea.setLineWrap(true);
        msgArea.setEditable(false);
        msgArea.setOpaque(false);

        JLabel timeLbl = new JLabel(formatRelativeTime(n.getCreatedAt()));
        timeLbl.setFont(ThemeManager.getSmallFont());
        timeLbl.setForeground(CineBookTheme.TEXT_MUTED);

        centerBox.add(titleLbl);
        centerBox.add(Box.createVerticalStrut(2));
        centerBox.add(msgArea);
        centerBox.add(Box.createVerticalStrut(4));
        centerBox.add(timeLbl);

        card.add(centerBox, BorderLayout.CENTER);

        // Hover & Click listeners
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onNotificationClick != null) {
                    onNotificationClick.accept(n);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(25, 42, 68));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(unread ? CineBookTheme.BG_CARD : CineBookTheme.BG_SURFACE);
            }
        });

        return card;
    }

    private FontAwesomeSolid getIconForType(String type) {
        if (type == null) return FontAwesomeSolid.BELL;
        switch (type.toUpperCase()) {
            case "BOOKING_CONFIRMED": return FontAwesomeSolid.TICKET_ALT;
            case "BOOKING_CANCELLED": return FontAwesomeSolid.TIMES_CIRCLE;
            case "SHOW_REMINDER": return FontAwesomeSolid.CLOCK;
            case "NEW_MOVIE": return FontAwesomeSolid.FILM;
            case "SHOW_UPDATE": return FontAwesomeSolid.EXCLAMATION_TRIANGLE;
            default: return FontAwesomeSolid.BELL;
        }
    }

    private Color getIconColorForType(String type) {
        if (type == null) return CineBookTheme.ACCENT_PURPLE;
        switch (type.toUpperCase()) {
            case "BOOKING_CONFIRMED": return CineBookTheme.SUCCESS_COLOR;
            case "BOOKING_CANCELLED": return CineBookTheme.DANGER_COLOR;
            case "SHOW_REMINDER": return CineBookTheme.ACCENT_GOLD;
            case "NEW_MOVIE": return CineBookTheme.ACCENT_CYAN;
            case "SHOW_UPDATE": return CineBookTheme.WARNING_COLOR;
            default: return CineBookTheme.ACCENT_PURPLE;
        }
    }

    private String formatRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) return "Just now";
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long seconds = duration.getSeconds();
        if (seconds < 60) return "Just now";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + " mins ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + (hours == 1 ? " hour ago" : " hours ago");
        long days = hours / 24;
        if (days < 7) return days + (days == 1 ? " day ago" : " days ago");
        return dateTime.toLocalDate().toString();
    }

    public void setOnNotificationClick(Consumer<Notification> callback) { this.onNotificationClick = callback; }
    public void setOnMarkAllRead(Runnable callback) { this.onMarkAllRead = callback; }
    public void setOnRefreshRequested(Runnable callback) { this.onRefreshRequested = callback; }
}
