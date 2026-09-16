package com.movieticket.view.components;

import com.movieticket.util.CineBookTheme;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * AnimatedCounterLabel.java — JLabel subclass that smoothly animates number count-ups from 0
 * to a target integer or currency value using a lightweight Swing Timer.
 */
public class AnimatedCounterLabel extends JLabel {

    private int currentValue = 0;
    private int targetValue = 0;
    private BigDecimal currentCurrency = BigDecimal.ZERO;
    private BigDecimal targetCurrency = BigDecimal.ZERO;
    private boolean isCurrencyMode = false;

    private Timer animationTimer;
    private int durationMs = 600;
    private int frames = 25;

    public AnimatedCounterLabel() {
        this("0");
    }

    public AnimatedCounterLabel(String initialText) {
        super(initialText);
        setFont(new Font("Segoe UI", Font.BOLD, 28));
        setForeground(CineBookTheme.TEXT_PRIMARY);
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    /**
     * Animates integer count-up from current value to target value.
     */
    public void setValueAnimated(int target) {
        this.isCurrencyMode = false;
        this.targetValue = target;

        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        final int startValue = currentValue;
        final int delta = target - startValue;
        if (delta == 0) {
            setText(String.valueOf(target));
            return;
        }

        final long startTime = System.currentTimeMillis();
        int delay = durationMs / frames;

        animationTimer = new Timer(delay, e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            double progress = Math.min(1.0, (double) elapsed / durationMs);

            // Smooth ease-out quad interpolation
            double eased = 1 - Math.pow(1 - progress, 2);
            currentValue = (int) Math.round(startValue + delta * eased);
            setText(String.valueOf(currentValue));

            if (progress >= 1.0) {
                currentValue = target;
                setText(String.valueOf(target));
                ((Timer) e.getSource()).stop();
            }
        });

        animationTimer.start();
    }

    /**
     * Animates currency amount count-up from current currency to target currency amount.
     */
    public void setCurrencyAnimated(BigDecimal target) {
        this.isCurrencyMode = true;
        this.targetCurrency = target != null ? target : BigDecimal.ZERO;

        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        final BigDecimal startValue = currentCurrency;
        final BigDecimal delta = targetCurrency.subtract(startValue);
        if (delta.compareTo(BigDecimal.ZERO) == 0) {
            setText("₹" + targetCurrency.setScale(2, RoundingMode.HALF_UP));
            return;
        }

        final long startTime = System.currentTimeMillis();
        int delay = durationMs / frames;

        animationTimer = new Timer(delay, e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            double progress = Math.min(1.0, (double) elapsed / durationMs);

            // Smooth ease-out quad interpolation
            double eased = 1 - Math.pow(1 - progress, 2);
            BigDecimal currentStep = startValue.add(delta.multiply(BigDecimal.valueOf(eased)));
            currentCurrency = currentStep.setScale(2, RoundingMode.HALF_UP);
            setText("₹" + currentCurrency);

            if (progress >= 1.0) {
                currentCurrency = targetCurrency.setScale(2, RoundingMode.HALF_UP);
                setText("₹" + currentCurrency);
                ((Timer) e.getSource()).stop();
            }
        });

        animationTimer.start();
    }

    public int getValue() {
        return currentValue;
    }

    public BigDecimal getCurrency() {
        return currentCurrency;
    }
}
