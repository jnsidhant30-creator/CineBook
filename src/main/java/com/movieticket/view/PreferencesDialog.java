package com.movieticket.view;

import com.movieticket.model.UserPreference;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.GlassCardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PreferencesDialog extends JDialog {

    private final String[] genres = {"Action", "Comedy", "Drama", "Horror", "Romance", "Sci-Fi", "Thriller", "Animation"};
    private final String[] languages = {"Hindi", "English", "Tamil", "Telugu"};

    private List<JCheckBox> genreCheckboxes;
    private List<JCheckBox> languageCheckboxes;
    private JButton saveButton;

    private UserPreference resultPreference;

    public PreferencesDialog(Window owner, int userId) {
        super(owner, "Welcome to CineBook", ModalityType.APPLICATION_MODAL);
        setSize(500, 450);
        setLocationRelativeTo(owner);
        setResizable(false);
        
        resultPreference = new UserPreference();
        resultPreference.setUserId(userId);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(CineBookTheme.BG_PRIMARY);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GlassCardPanel cardPanel = new GlassCardPanel(new BorderLayout(10, 10));
        cardPanel.setTopAccent(CineBookTheme.ACCENT_PURPLE, 4);
        cardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Choose your interests:");
        titleLabel.setFont(ThemeManager.getCardTitleFont());
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cardPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Genres
        JLabel genreLabel = new JLabel("Favorite Genres:");
        genreLabel.setFont(ThemeManager.getBodyFont());
        genreLabel.setForeground(CineBookTheme.TEXT_SECONDARY);
        genreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(genreLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel genrePanel = new JPanel(new GridLayout(0, 4, 10, 10));
        genrePanel.setOpaque(false);
        genreCheckboxes = new ArrayList<>();
        for (String g : genres) {
            JCheckBox cb = new JCheckBox(g);
            cb.setOpaque(false);
            cb.setForeground(CineBookTheme.TEXT_PRIMARY);
            cb.setFont(ThemeManager.getBodyFont());
            genreCheckboxes.add(cb);
            genrePanel.add(cb);
        }
        centerPanel.add(genrePanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Languages
        JLabel langLabel = new JLabel("Favorite Languages:");
        langLabel.setFont(ThemeManager.getBodyFont());
        langLabel.setForeground(CineBookTheme.TEXT_SECONDARY);
        langLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(langLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel langPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        langPanel.setOpaque(false);
        languageCheckboxes = new ArrayList<>();
        for (String l : languages) {
            JCheckBox cb = new JCheckBox(l);
            cb.setOpaque(false);
            cb.setForeground(CineBookTheme.TEXT_PRIMARY);
            cb.setFont(ThemeManager.getBodyFont());
            languageCheckboxes.add(cb);
            langPanel.add(cb);
        }
        centerPanel.add(langPanel);

        cardPanel.add(centerPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        saveButton = new JButton("Save Preferences");
        ThemeManager.stylePrimaryButton(saveButton);
        saveButton.setPreferredSize(new Dimension(180, 40));
        saveButton.addActionListener(e -> saveAndClose());

        JButton skipButton = new JButton("Skip for Now");
        ThemeManager.styleSecondaryButton(skipButton);
        skipButton.setPreferredSize(new Dimension(150, 40));
        skipButton.addActionListener(e -> {
            resultPreference = null;
            dispose();
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(skipButton);

        cardPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(cardPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void saveAndClose() {
        StringBuilder gBuilder = new StringBuilder();
        for (JCheckBox cb : genreCheckboxes) {
            if (cb.isSelected()) {
                if (gBuilder.length() > 0) gBuilder.append(",");
                gBuilder.append(cb.getText());
            }
        }
        
        StringBuilder lBuilder = new StringBuilder();
        for (JCheckBox cb : languageCheckboxes) {
            if (cb.isSelected()) {
                if (lBuilder.length() > 0) lBuilder.append(",");
                lBuilder.append(cb.getText());
            }
        }
        
        resultPreference.setPreferredGenres(gBuilder.toString());
        resultPreference.setPreferredLanguages(lBuilder.toString());
        dispose();
    }

    public UserPreference getResultPreference() {
        return resultPreference;
    }
}
