package com.movieticket.view.components;

import com.movieticket.model.TmdbMovie;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ImageLoader;
import com.movieticket.util.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;

public class TmdbMovieCard extends GlassCardPanel {

    private TmdbMovie movie;
    private JLabel posterLabel;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    
    private Runnable onClickAction;

    public TmdbMovieCard(TmdbMovie movie) {
        super(new BorderLayout(5, 5));
        this.movie = movie;
        
        setTopAccent(CineBookTheme.ACCENT_CYAN, 3);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(200, 310));
        setMinimumSize(new Dimension(200, 310));
        setMaximumSize(new Dimension(200, 310));

        // Poster Area
        posterLabel = new JLabel("Loading...");
        posterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        posterLabel.setForeground(CineBookTheme.TEXT_MUTED);
        posterLabel.setPreferredSize(new Dimension(180, 240));
        add(posterLabel, BorderLayout.CENTER);

        // Info Area
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        String title = movie.getTitle() != null ? movie.getTitle() : "Unknown Title";
        titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        if (title.length() > 22) {
            titleLabel.setText(title.substring(0, 20) + "...");
        }

        String subtitleText = "★ " + String.format("%.1f", movie.getVoteAverage()) 
                              + " • " + movie.getYear() 
                              + " • " + movie.getPrimaryGenre();
        subtitleLabel = new JLabel(subtitleText);
        subtitleLabel.setFont(ThemeManager.getSmallFont());
        subtitleLabel.setForeground(CineBookTheme.ACCENT_GOLD);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(subtitleLabel);
        
        add(infoPanel, BorderLayout.SOUTH);

        // Hover & Click Effects
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(new Color(25, 45, 70, 200));
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(CineBookTheme.BG_CARD);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (onClickAction != null) {
                    onClickAction.run();
                }
            }
        });

        loadPosterAsync();
    }
    
    public void setOnClickAction(Runnable action) {
        this.onClickAction = action;
    }

    private void loadPosterAsync() {
        if (movie.getPosterPath() == null || movie.getPosterPath().isBlank()) {
            posterLabel.setText("No Poster");
            return;
        }

        SwingWorker<Image, Void> worker = new SwingWorker<>() {
            @Override
            protected Image doInBackground() {
                return ImageLoader.loadTmdbImage(movie.getPosterPath(), "w342");
            }

            @Override
            protected void done() {
                try {
                    Image img = get();
                    if (img != null) {
                        Image scaled = img.getScaledInstance(180, 240, Image.SCALE_SMOOTH);
                        posterLabel.setIcon(new ImageIcon(scaled));
                        posterLabel.setText("");
                    } else {
                        posterLabel.setText("No Poster");
                    }
                } catch (Exception ex) {
                    posterLabel.setText("No Poster");
                }
            }
        };
        worker.execute();
    }
}
