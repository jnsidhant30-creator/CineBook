package com.movieticket.view;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.GlassCardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SubmitReviewDialog extends JDialog {

    private int selectedRating = 0;
    private JTextArea reviewTextArea;
    private JButton submitButton;
    private JButton cancelButton;
    private List<JButton> starButtons;

    public SubmitReviewDialog(Window owner, String targetName) {
        super(owner, "Submit Review - " + targetName, ModalityType.APPLICATION_MODAL);
        setSize(400, 350);
        setLocationRelativeTo(owner);
        setResizable(false);

        initComponents(targetName);
    }

    private void initComponents(String targetName) {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(CineBookTheme.BG_PRIMARY);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GlassCardPanel cardPanel = new GlassCardPanel(new BorderLayout(10, 10));
        cardPanel.setTopAccent(CineBookTheme.ACCENT_PURPLE, 3);
        cardPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header
        JLabel titleLabel = new JLabel("Rate " + targetName);
        titleLabel.setFont(ThemeManager.getCardTitleFont());
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cardPanel.add(titleLabel, BorderLayout.NORTH);

        // Center Content
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Star Rating Panel
        JPanel starPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        starPanel.setOpaque(false);
        starButtons = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            JButton starBtn = new JButton("★");
            starBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
            starBtn.setForeground(CineBookTheme.TEXT_MUTED);
            starBtn.setContentAreaFilled(false);
            starBtn.setBorderPainted(false);
            starBtn.setFocusPainted(false);
            starBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            final int ratingValue = i;
            starBtn.addActionListener(e -> setRating(ratingValue));

            starButtons.add(starBtn);
            starPanel.add(starBtn);
        }

        centerPanel.add(starPanel);
        centerPanel.add(Box.createVerticalStrut(15));

        // Review Text
        JLabel lblReview = new JLabel("Optional Review:");
        lblReview.setFont(ThemeManager.getBodyFont());
        lblReview.setForeground(CineBookTheme.TEXT_SECONDARY);
        lblReview.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblReview);
        centerPanel.add(Box.createVerticalStrut(5));

        reviewTextArea = new JTextArea(4, 20);
        reviewTextArea.setLineWrap(true);
        reviewTextArea.setWrapStyleWord(true);
        ThemeManager.styleTextArea(reviewTextArea);
        JScrollPane scrollPane = new JScrollPane(reviewTextArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        centerPanel.add(scrollPane);

        cardPanel.add(centerPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        cancelButton = new JButton("Cancel");
        ThemeManager.styleSecondaryButton(cancelButton);
        cancelButton.setPreferredSize(new Dimension(120, 36));
        cancelButton.addActionListener(e -> dispose());

        submitButton = new JButton("Submit");
        ThemeManager.stylePrimaryButton(submitButton);
        submitButton.setPreferredSize(new Dimension(120, 36));

        buttonPanel.add(cancelButton);
        buttonPanel.add(submitButton);

        cardPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(cardPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private void setRating(int rating) {
        this.selectedRating = rating;
        for (int i = 0; i < starButtons.size(); i++) {
            if (i < rating) {
                starButtons.get(i).setForeground(CineBookTheme.ACCENT_GOLD);
            } else {
                starButtons.get(i).setForeground(CineBookTheme.TEXT_MUTED);
            }
        }
    }

    public int getSelectedRating() {
        return selectedRating;
    }

    public String getReviewText() {
        return reviewTextArea.getText();
    }

    public JButton getSubmitButton() {
        return submitButton;
    }
}
