package com.movieticket.view.components;

import com.movieticket.util.CineBookTheme;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * RoundedButton.java — Modern Swing Button with rounded corners, custom hover states,
 * elevation shadow effects, and optional Ikonli vector icon integration.
 */
public class RoundedButton extends JButton {

    private int cornerRadius = 10;
    private Color normalBg;
    private Color hoverBg;
    private Color pressedBg;
    private Color borderColor;
    private boolean isHovered = false;
    private boolean isPressed = false;

    public RoundedButton(String text) {
        this(text, null, CineBookTheme.ACCENT_RED, CineBookTheme.ACCENT_RED_HOVER);
    }

    public RoundedButton(String text, Ikon iconCode) {
        this(text, iconCode, CineBookTheme.ACCENT_RED, CineBookTheme.ACCENT_RED_HOVER);
    }

    public RoundedButton(String text, Ikon iconCode, Color bg, Color hoverBg) {
        super(text);
        this.normalBg = bg;
        this.hoverBg = hoverBg;
        this.pressedBg = hoverBg.darker();
        this.borderColor = bg;

        if (iconCode != null) {
            setIcon(FontIcon.of(iconCode, 18, CineBookTheme.TEXT_PRIMARY));
            setIconTextGap(8);
        }

        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setForeground(CineBookTheme.TEXT_PRIMARY);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = false;
                    isPressed = false;
                    repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = false;
                    repaint();
                }
            }
        });
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    public void setButtonColors(Color bg, Color hoverBg, Color pressedBg) {
        this.normalBg = bg;
        this.hoverBg = hoverBg;
        this.pressedBg = pressedBg;
        repaint();
    }

    public void setIkon(Ikon iconCode, int iconSize, Color iconColor) {
        if (iconCode != null) {
            setIcon(FontIcon.of(iconCode, iconSize, iconColor != null ? iconColor : getForeground()));
            setIconTextGap(8);
        } else {
            setIcon(null);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Determine current background color based on state
        Color currentBg = normalBg;
        if (!isEnabled()) {
            currentBg = CineBookTheme.BG_CARD_HOVER.darker();
        } else if (isPressed) {
            currentBg = pressedBg;
        } else if (isHovered) {
            currentBg = hoverBg;
        }

        // Paint rounded background
        g2.setColor(currentBg);
        g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);

        // Optional subtle border highlight on hover
        if (isHovered && isEnabled()) {
            g2.setColor(borderColor.brighter());
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();

        super.paintComponent(g);
    }
}
