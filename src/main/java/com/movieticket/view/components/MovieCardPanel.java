package com.movieticket.view.components;

import com.movieticket.model.Movie;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ImageLoader;
import com.movieticket.util.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class MovieCardPanel extends GlassCardPanel {

    private Movie movie;
    private JLabel posterLabel;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JButton bookButton;
    
    private Runnable onClickAction;
    private Runnable onBookAction;

    public MovieCardPanel(Movie movie) {
        super(new BorderLayout(5, 5));
        this.movie = movie;
        
        setTopAccent(CineBookTheme.ACCENT_PURPLE, 2);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        // We will let ResponsiveGridPanel handle the preferred size dynamically

        // Poster Area
        posterLabel = new JLabel("Loading...", SwingConstants.CENTER) {
            private Image originalImage;
            private Image scaledImage;
            private int lastW = -1;
            private int lastH = -1;
            
            @Override
            public void setIcon(Icon icon) {
                if (icon instanceof ImageIcon) {
                    originalImage = ((ImageIcon) icon).getImage();
                    scaledImage = null;
                    super.setIcon(null); // Don't use default icon painting
                } else {
                    super.setIcon(icon);
                }
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                if (originalImage != null) {
                    int w = getWidth();
                    int h = getHeight();
                    if (w <= 0 || h <= 0) return;
                    
                    if (scaledImage == null || w != lastW || h != lastH) {
                        lastW = w;
                        lastH = h;
                        int imgW = originalImage.getWidth(null);
                        int imgH = originalImage.getHeight(null);
                        if (imgW > 0 && imgH > 0) {
                            double ratio = (double) imgW / imgH;
                            int drawW = w;
                            int drawH = (int) (w / ratio);
                            if (drawH > h) {
                                drawH = h;
                                drawW = (int) (h * ratio);
                            }
                            BufferedImage bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                            Graphics2D g2 = bimg.createGraphics();
                            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            int x = (w - drawW) / 2;
                            int y = (h - drawH) / 2;
                            g2.drawImage(originalImage, x, y, drawW, drawH, null);
                            g2.dispose();
                            scaledImage = bimg;
                        }
                    }
                    if (scaledImage != null) {
                        g.drawImage(scaledImage, 0, 0, null);
                    }
                } else {
                    super.paintComponent(g);
                }
            }
        };
        posterLabel.setForeground(CineBookTheme.TEXT_MUTED);
        posterLabel.setFont(ThemeManager.getFont(Font.ITALIC, 14));
        add(posterLabel, BorderLayout.CENTER);

        // Info Area
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        String safeTitle = movie.getTitle().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        titleLabel = new JLabel("<html><div style='text-align: center; width: 100%;'>" + safeTitle + "</div></html>");
        titleLabel.setFont(ThemeManager.getFont(Font.BOLD, 15));
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Ensure 2 lines are allocated for the title even if it's 1 line, to maintain alignment
        titleLabel.setPreferredSize(new Dimension(0, 42));
        titleLabel.setMinimumSize(new Dimension(0, 42));
        titleLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        titleLabel.setVerticalAlignment(SwingConstants.TOP);

        String ratingText = movie.getRating() != null ? movie.getRating().toPlainString() : "N/A";
        String genreText = movie.getGenre() != null ? movie.getGenre() : "";
        if (genreText.length() > 15) {
            genreText = genreText.substring(0, 14) + "…";
        }
        
        String subtitleText = "<html><div style='text-align: center;'><b>★ " + ratingText + "</b> &nbsp;•&nbsp; " + genreText + "</div></html>";
        subtitleLabel = new JLabel(subtitleText);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(CineBookTheme.ACCENT_GOLD);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setPreferredSize(new Dimension(0, 20));
        subtitleLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        
        bookButton = new JButton("BOOK TICKETS");
        ThemeManager.stylePrimaryButton(bookButton);
        bookButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bookButton.setPreferredSize(new Dimension(140, 32));
        bookButton.setMaximumSize(new Dimension(160, 32));
        bookButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        bookButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        bookButton.addActionListener(e -> {
            if (onBookAction != null) onBookAction.run();
            else 
            if (onClickAction != null) onClickAction.run();
        });

        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(subtitleLabel);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(bookButton);
        
        add(infoPanel, BorderLayout.SOUTH);

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
    
    public void setOnBookAction(Runnable action) { this.onBookAction = action; }
    public void setOnClickAction(Runnable action) {
        this.onClickAction = action;
    }

    private void loadPosterAsync() {
        SwingWorker<Image, Void> worker = new SwingWorker<>() {
            @Override
            protected Image doInBackground() {
                // Request a larger high-res poster from TMDB. 
                // "w342" is TMDB standard size. Dimensions: 342 width, ~513 height
                return ImageLoader.loadMoviePoster(movie, "w342", 342, 513);
            }
            @Override
            protected void done() {
                try {
                    Image scaled = get();
                    if (scaled != null) {
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
    
    @Override
    public Dimension getPreferredSize() {
        int w = getWidth();
        if (w <= 0) {
            Container p = getParent();
            if (p != null && p.getWidth() > 0) {
                LayoutManager lm = p.getLayout();
                if (lm instanceof GridLayout) {
                    GridLayout gl = (GridLayout) lm;
                    int cols = gl.getColumns();
                    if (cols > 0) {
                        int gaps = gl.getHgap() * (cols - 1);
                        w = (p.getWidth() - p.getInsets().left - p.getInsets().right - gaps) / cols;
                    }
                }
            }
        }
        if (w <= 0) w = 220; // fallback width
        
        // Height: poster ratio is approx 1.5 (2:3). Info panel height is approx 110px.
        int h = (int) (w * 1.5) + 110;
        return new Dimension(w, h);
    }
}

