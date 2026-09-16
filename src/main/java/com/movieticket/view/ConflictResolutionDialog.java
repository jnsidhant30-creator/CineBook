package com.movieticket.view;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.model.Show;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConflictResolutionDialog extends JDialog {

    private final Show existingShow;
    private final LocalTime requestedStart;
    private final LocalTime requestedEnd;
    private final String requestedMovieTitle;
    private final List<LocalTime> suggestedSlots;
    
    public enum ConflictResult {
        CHOOSE_ANOTHER_TIME,
        CHOOSE_ANOTHER_SCREEN,
        CANCEL
    }
    
    private ConflictResult result = ConflictResult.CANCEL;
    private LocalTime selectedSlot = null;
    
    private JComboBox<String> slotCombo;

    public ConflictResolutionDialog(JFrame parent, Show existingShow, String requestedMovieTitle, LocalTime requestedStart, LocalTime requestedEnd, List<LocalTime> suggestedSlots) {
        super(parent, "⚠️ SHOW SCHEDULING CONFLICT", true);
        this.existingShow = existingShow;
        this.requestedMovieTitle = requestedMovieTitle;
        this.requestedStart = requestedStart;
        this.requestedEnd = requestedEnd;
        this.suggestedSlots = suggestedSlots;
        
        initUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBackground(CineBookTheme.BG_PRIMARY);
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(contentPane);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        // Info Panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(CineBookTheme.BG_PRIMARY);
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblReason = new JLabel("Reason: The selected screen is already occupied during this time.");
        lblReason.setFont(ThemeManager.getBodyFont());
        lblReason.setForeground(CineBookTheme.DANGER_COLOR);
        infoPanel.add(lblReason);
        infoPanel.add(Box.createVerticalStrut(15));

        infoPanel.add(createDetailLabel("Screen:", existingShow.getScreenNumber()));
        infoPanel.add(createDetailLabel("Date:", existingShow.getShowDate().toString()));
        infoPanel.add(Box.createVerticalStrut(10));
        
        infoPanel.add(createDetailLabel("Existing Show:", "Show ID " + existingShow.getShowId()));
        String exTime = existingShow.getShowTime().format(timeFormatter) + " - " + 
                        (existingShow.getEndTime() != null ? existingShow.getEndTime().format(timeFormatter) : "N/A");
        infoPanel.add(createDetailLabel("Existing Time:", exTime + " (Includes 15m Buffer)"));
        infoPanel.add(Box.createVerticalStrut(10));

        infoPanel.add(createDetailLabel("Requested Show:", requestedMovieTitle));
        String reqTime = requestedStart.format(timeFormatter) + " - " + requestedEnd.format(timeFormatter);
        infoPanel.add(createDetailLabel("Requested Time:", reqTime));
        
        contentPane.add(infoPanel, BorderLayout.NORTH);

        // Suggestions Panel
        if (suggestedSlots != null && !suggestedSlots.isEmpty()) {
            JPanel suggestionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            suggestionsPanel.setBackground(CineBookTheme.BG_PRIMARY);
            JLabel lblSuggest = new JLabel("Suggested Available Times:");
            lblSuggest.setFont(ThemeManager.getLabelFont());
            lblSuggest.setForeground(CineBookTheme.TEXT_SECONDARY);
            suggestionsPanel.add(lblSuggest);
            
            slotCombo = new JComboBox<>();
            ThemeManager.styleComboBox(slotCombo);
            for (LocalTime slot : suggestedSlots) {
                slotCombo.addItem(slot.format(timeFormatter));
            }
            suggestionsPanel.add(slotCombo);
            
            contentPane.add(suggestionsPanel, BorderLayout.CENTER);
        }

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(CineBookTheme.BG_PRIMARY);

        JButton btnAnotherTime = new JButton(suggestedSlots != null && !suggestedSlots.isEmpty() ? "Use Suggested Time" : "Choose Another Time");
        ThemeManager.stylePrimaryButton(btnAnotherTime);
        btnAnotherTime.addActionListener(e -> {
            if (suggestedSlots != null && !suggestedSlots.isEmpty()) {
                selectedSlot = suggestedSlots.get(slotCombo.getSelectedIndex());
            }
            result = ConflictResult.CHOOSE_ANOTHER_TIME;
            dispose();
        });
        
        JButton btnAnotherScreen = new JButton("Choose Another Screen");
        ThemeManager.styleSecondaryButton(btnAnotherScreen);
        btnAnotherScreen.addActionListener(e -> {
            result = ConflictResult.CHOOSE_ANOTHER_SCREEN;
            dispose();
        });

        JButton btnCancel = new JButton("Cancel");
        ThemeManager.styleSecondaryButton(btnCancel);
        btnCancel.addActionListener(e -> {
            result = ConflictResult.CANCEL;
            dispose();
        });

        buttonPanel.add(btnAnotherTime);
        buttonPanel.add(btnAnotherScreen);
        buttonPanel.add(btnCancel);

        contentPane.add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createDetailLabel(String labelText, String valueText) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setBackground(CineBookTheme.BG_PRIMARY);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel l = new JLabel(String.format("%-20s ", labelText));
        l.setFont(ThemeManager.getLabelFont());
        l.setForeground(CineBookTheme.TEXT_SECONDARY);
        l.setPreferredSize(new Dimension(120, 20));
        
        JLabel v = new JLabel(valueText);
        v.setFont(ThemeManager.getBodyFont());
        v.setForeground(CineBookTheme.TEXT_PRIMARY);
        
        p.add(l);
        p.add(v);
        return p;
    }
    
    public ConflictResult getResult() {
        return result;
    }
    
    public LocalTime getSelectedSlot() {
        return selectedSlot;
    }
}
