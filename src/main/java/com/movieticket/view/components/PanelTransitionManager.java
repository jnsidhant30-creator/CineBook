package com.movieticket.view.components;

import com.movieticket.util.CineBookTheme;

import javax.swing.*;
import java.awt.*;

/**
 * PanelTransitionManager.java — Container managing smooth fade-in panel transitions
 * when switching between views in CardLayout and providing sliding indicator state animations.
 */
public class PanelTransitionManager extends JPanel {

    private final CardLayout cardLayout;
    private float alpha = 1.0f;
    private Timer fadeTimer;

    public PanelTransitionManager() {
        this.cardLayout = new CardLayout();
        setLayout(cardLayout);
        setOpaque(false);
    }

    public CardLayout getCardLayout() {
        return cardLayout;
    }

    public void addPanel(JComponent panel, String cardName) {
        add(panel, cardName);
    }

    /**
     * Switches card with a smooth alpha fade-in animation.
     */
    public void showCardAnimated(String cardName) {
        cardLayout.show(this, cardName);

        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }

        alpha = 0.20f;
        repaint();

        fadeTimer = new Timer(16, e -> {
            alpha += 0.10f;
            if (alpha >= 1.0f) {
                alpha = 1.0f;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        fadeTimer.start();
    }

    @Override
    protected void paintChildren(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        super.paintChildren(g2);
        g2.dispose();
    }
}
