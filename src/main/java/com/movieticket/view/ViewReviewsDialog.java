package com.movieticket.view;

import com.movieticket.model.Review;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.GlassCardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ViewReviewsDialog extends JDialog {

    private JPanel reviewsListPanel;
    private JButton btnLoadMore;
    private JButton btnClose;
    
    // Pagination state
    private int currentOffset = 0;
    private final int limit = 10;
    private boolean hasMore = true;

    public ViewReviewsDialog(Window owner, String targetName) {
        super(owner, "User Reviews - " + targetName, ModalityType.MODELESS);
        setSize(500, 600);
        setLocationRelativeTo(owner);

        initComponents(targetName);
    }

    private void initComponents(String targetName) {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(CineBookTheme.BG_PRIMARY);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JLabel titleLabel = new JLabel("Reviews for " + targetName);
        titleLabel.setFont(ThemeManager.getPageTitleFont());
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Center Content
        reviewsListPanel = new JPanel();
        reviewsListPanel.setLayout(new BoxLayout(reviewsListPanel, BoxLayout.Y_AXIS));
        reviewsListPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(reviewsListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CineBookTheme.BG_PRIMARY);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        bottomPanel.setOpaque(false);

        btnLoadMore = new JButton("Load More");
        ThemeManager.styleSecondaryButton(btnLoadMore);
        
        btnClose = new JButton("Close");
        ThemeManager.stylePrimaryButton(btnClose);
        btnClose.addActionListener(e -> dispose());

        bottomPanel.add(btnLoadMore);
        bottomPanel.add(btnClose);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    public void addReviews(List<Review> reviews) {
        if (reviews.isEmpty()) {
            hasMore = false;
            btnLoadMore.setVisible(false);
            if (currentOffset == 0) {
                JLabel emptyLabel = new JLabel("No reviews yet.");
                emptyLabel.setForeground(CineBookTheme.TEXT_MUTED);
                emptyLabel.setFont(ThemeManager.getBodyFont());
                reviewsListPanel.add(emptyLabel);
            }
        } else {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            for (Review r : reviews) {
                GlassCardPanel reviewCard = new GlassCardPanel(new BorderLayout(5, 5));
                reviewCard.setBorder(new EmptyBorder(10, 10, 10, 10));
                
                JPanel headerPanel = new JPanel(new BorderLayout());
                headerPanel.setOpaque(false);
                
                JLabel lblUser = new JLabel(r.getUsername() != null ? r.getUsername() : "Anonymous");
                lblUser.setFont(ThemeManager.getCardTitleFont());
                lblUser.setForeground(CineBookTheme.TEXT_PRIMARY);
                
                JLabel lblDate = new JLabel(r.getCreatedAt() != null ? r.getCreatedAt().format(dtf) : "");
                lblDate.setFont(ThemeManager.getBodyFont());
                lblDate.setForeground(CineBookTheme.TEXT_MUTED);
                
                headerPanel.add(lblUser, BorderLayout.WEST);
                headerPanel.add(lblDate, BorderLayout.EAST);
                
                JPanel starsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
                starsPanel.setOpaque(false);
                for (int i = 1; i <= 5; i++) {
                    JLabel star = new JLabel("★");
                    star.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                    star.setForeground(i <= r.getRating() ? CineBookTheme.ACCENT_GOLD : CineBookTheme.TEXT_MUTED);
                    starsPanel.add(star);
                }
                
                reviewCard.add(headerPanel, BorderLayout.NORTH);
                
                JPanel centerPanel = new JPanel(new BorderLayout());
                centerPanel.setOpaque(false);
                centerPanel.add(starsPanel, BorderLayout.NORTH);
                
                if (r.getReviewText() != null && !r.getReviewText().trim().isEmpty()) {
                    JTextArea txtReview = new JTextArea(r.getReviewText());
                    txtReview.setLineWrap(true);
                    txtReview.setWrapStyleWord(true);
                    txtReview.setOpaque(false);
                    txtReview.setEditable(false);
                    txtReview.setForeground(CineBookTheme.TEXT_SECONDARY);
                    txtReview.setFont(ThemeManager.getBodyFont());
                    txtReview.setBorder(new EmptyBorder(5, 5, 0, 0));
                    centerPanel.add(txtReview, BorderLayout.CENTER);
                }
                
                reviewCard.add(centerPanel, BorderLayout.CENTER);
                
                reviewCard.setMaximumSize(new Dimension(800, 150));
                reviewsListPanel.add(reviewCard);
                reviewsListPanel.add(Box.createVerticalStrut(10));
            }
            
            if (reviews.size() < limit) {
                hasMore = false;
                btnLoadMore.setVisible(false);
            } else {
                currentOffset += reviews.size();
                btnLoadMore.setVisible(true);
            }
        }
        reviewsListPanel.revalidate();
        reviewsListPanel.repaint();
    }
    
    public void resetPagination() {
        reviewsListPanel.removeAll();
        currentOffset = 0;
        hasMore = true;
    }

    public int getCurrentOffset() {
        return currentOffset;
    }

    public int getLimit() {
        return limit;
    }

    public JButton getBtnLoadMore() {
        return btnLoadMore;
    }
}
