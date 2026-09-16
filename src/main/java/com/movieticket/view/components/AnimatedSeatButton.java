package com.movieticket.view.components;

import com.movieticket.util.AnimationUtils;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * AnimatedSeatButton.java — Modern Cinema Seat Component with Smooth Color & Glow Transitions.
 * Features 150ms color interpolation from Available (Green #22C55E) to Selected (Yellow #F59E0B),
 * outer glow ring, and subtle scale pulse.
 */
public class AnimatedSeatButton extends JToggleButton {

    public enum SeatState {
        AVAILABLE,
        SELECTED,
        BOOKED,
        BLOCKED,
        HELD_BY_ME,
        HELD_BY_OTHER
    }

    private SeatState currentState = SeatState.AVAILABLE;
    private float animationProgress = 0.0f; // 0.0 = Available (Green), 1.0 = Selected (Yellow)
    private Timer transitionTimer;

    public AnimatedSeatButton(String seatNumber) {
        super(seatNumber);
        setFont(ThemeManager.getFont(Font.BOLD, 13));
        setPreferredSize(new Dimension(60, 44));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addActionListener(e -> {
            if (isEnabled() && currentState != SeatState.BOOKED && currentState != SeatState.BLOCKED) {
                setSeatSelected(isSelected(), true);
            }
        });
    }

    public void setSeatState(SeatState state) {
        this.currentState = state;
        if (state == SeatState.BOOKED || state == SeatState.BLOCKED || state == SeatState.HELD_BY_OTHER) {
            setEnabled(false);
            animationProgress = 0.0f;
            setSelected(false);
        } else if (state == SeatState.SELECTED || state == SeatState.HELD_BY_ME) {
            setEnabled(true);
            setSelected(true);
            animationProgress = 1.0f;
        } else {
            setEnabled(true);
            setSelected(false);
            animationProgress = 0.0f;
        }
        repaint();
    }

    public void setSeatSelected(boolean selected, boolean animate) {
        setSelected(selected);
        this.currentState = selected ? SeatState.SELECTED : SeatState.AVAILABLE;

        if (!animate) {
            animationProgress = selected ? 1.0f : 0.0f;
            repaint();
            return;
        }

        float target = selected ? 1.0f : 0.0f;
        float start = animationProgress;

        if (transitionTimer != null && transitionTimer.isRunning()) {
            transitionTimer.stop();
        }

        transitionTimer = AnimationUtils.animate(AnimationUtils.FAST, (rawProgress, easedProgress) -> {
            animationProgress = AnimationUtils.lerp(start, target, easedProgress);
            repaint();
        }, null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Determine base color based on state and animation progress
        Color baseBg;
        Color borderColor;
        Color textColor;

        if (!isEnabled() || currentState == SeatState.BOOKED || currentState == SeatState.HELD_BY_OTHER || currentState == SeatState.BLOCKED) {
            baseBg = new Color(25, 35, 50);
            if (currentState == SeatState.HELD_BY_OTHER) {
                borderColor = CineBookTheme.TEXT_MUTED;
                textColor = CineBookTheme.TEXT_MUTED;
            } else if (currentState == SeatState.BLOCKED) {
                borderColor = CineBookTheme.BORDER_COLOR;
                textColor = CineBookTheme.TEXT_MUTED;
            } else {
                borderColor = CineBookTheme.DANGER_COLOR;
                textColor = CineBookTheme.DANGER_COLOR;
            }
        } else if (currentState == SeatState.HELD_BY_ME) {
            baseBg = AnimationUtils.blendColors(new Color(15, 25, 40), new Color(40, 20, 60), animationProgress);
            borderColor = AnimationUtils.blendColors(CineBookTheme.SUCCESS_COLOR, CineBookTheme.ACCENT_PURPLE, animationProgress);
            textColor = AnimationUtils.blendColors(CineBookTheme.TEXT_PRIMARY, CineBookTheme.TEXT_PRIMARY, animationProgress);
        } else {
            // Lerp from Available (Green) to Selected (Yellow)
            baseBg = AnimationUtils.blendColors(new Color(15, 25, 40), new Color(40, 35, 15), animationProgress);
            borderColor = AnimationUtils.blendColors(CineBookTheme.SUCCESS_COLOR, CineBookTheme.ACCENT_GOLD, animationProgress);
            textColor = AnimationUtils.blendColors(CineBookTheme.TEXT_PRIMARY, CineBookTheme.TEXT_PRIMARY, animationProgress);
        }

        // 1. Draw Outer Glow if Selected or Held By Me
        if (animationProgress > 0.01f && isEnabled() && (currentState == SeatState.SELECTED || currentState == SeatState.HELD_BY_ME)) {
            int glowSize = (int) (6 * animationProgress);
            Color glowColor = (currentState == SeatState.HELD_BY_ME) ?
                    new Color(CineBookTheme.ACCENT_PURPLE.getRed(), CineBookTheme.ACCENT_PURPLE.getGreen(), CineBookTheme.ACCENT_PURPLE.getBlue(), (int) (60 * animationProgress)) :
                    new Color(CineBookTheme.ACCENT_GOLD.getRed(), CineBookTheme.ACCENT_GOLD.getGreen(), CineBookTheme.ACCENT_GOLD.getBlue(), (int) (60 * animationProgress));

            g2.setColor(glowColor);
            g2.fillRoundRect(2, 2, w - 4, h - 4, 12, 12);
        }

        // Scale Pulse Offset (1-2px pulse when selected)
        int scaleOffset = (int) (animationProgress * 2.0f);



        // 2. Draw Main Seat Body
        g2.setColor(baseBg);
        g2.fillRoundRect(3, 3, w - 6, h - 6, 8, 8);

        // 3. Draw Seat Border
        g2.setColor(borderColor);
        int strokeWidth = isSelected() ? 2 : 1;
        g2.setStroke(new BasicStroke(strokeWidth));
        g2.drawRoundRect(3, 3, w - 6, h - 6, 8, 8);

        // 4. Draw Seat Number Text
        g2.setColor(textColor);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        String text = getText();

        // Add subtle sparkle icon for selected seats
        if (isSelected()) {
            text = text + "✦";
        }

        int textX = (w - fm.stringWidth(text)) / 2;
        int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, textX, textY);

        g2.dispose();
    }
}
