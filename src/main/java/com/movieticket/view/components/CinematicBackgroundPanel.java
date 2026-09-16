package com.movieticket.view.components;

import com.movieticket.util.BackgroundImageManager;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CinematicBackgroundPanel.java — Dual Projector Cinematic Background.
 */
public class CinematicBackgroundPanel extends JPanel {

    public enum Intensity {
        STRONG,   // For Login / Register screens
        SUBTLE    // For Admin Dashboard & Main Viewports
    }

    private Intensity intensity;
    private float darkOverlayOpacity;

    // Animated Light Movement State
    private float animPhase = 0.0f; // 0.0 to 1.0 (loops every ~3000ms)
    private float ambientPhase = 0.0f; // For very slow ambient changes
    private Timer lightAnimationTimer;
    private boolean animationEnabled = true;

    // Cinematic Particles
    private List<Particle> particles;
    private Random random = new Random();

    // Center Reveal state
    private boolean revealTriggered = false;
    private float revealPhase = 0.0f;

    public CinematicBackgroundPanel() {
        this(Intensity.SUBTLE, 0.70f);
    }

    public CinematicBackgroundPanel(Intensity intensity) {
        this(intensity, intensity == Intensity.STRONG ? 0.65f : 0.80f);
    }

    public CinematicBackgroundPanel(Intensity intensity, float darkOverlayOpacity) {
        this.intensity = intensity;
        this.darkOverlayOpacity = Math.max(0.40f, Math.min(0.90f, darkOverlayOpacity));
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(CineBookTheme.BG_PRIMARY);

        initParticles();
        setupAnimationTimer();
    }

    private void initParticles() {
        particles = new ArrayList<>();
        int particleCount = intensity == Intensity.STRONG ? 40 : 15;
        for (int i = 0; i < particleCount; i++) {
            particles.add(new Particle());
        }
    }

    private void setupAnimationTimer() {
        // ~30 fps. Target 3000ms loop => 33ms per frame = ~90 frames.
        // 1.0f / 90.0f = 0.0111f increment.
        lightAnimationTimer = new Timer(33, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isShowing() && animationEnabled) {
                    animPhase += 0.011f;
                    if (animPhase >= 1.0f) {
                        animPhase -= 1.0f;
                    }
                    ambientPhase += 0.005f;
                    if (ambientPhase >= (float) (2 * Math.PI * 100)) {
                        ambientPhase = 0.0f;
                    }
                    
                    // Manage Reveal Phase based on left projector reaching center.
                    // Left projector phase is animPhase. Reaches bottom (center) at 0.5.
                    if (animPhase >= 0.45f && animPhase <= 0.65f) {
                        revealTriggered = true;
                        revealPhase += 0.05f; // Fast fade in
                        if (revealPhase > 1.0f) revealPhase = 1.0f;
                    } else {
                        revealTriggered = false;
                        revealPhase -= 0.03f; // Slower fade out
                        if (revealPhase < 0.0f) revealPhase = 0.0f;
                    }

                    updateParticles();
                    repaint();
                }
            }
        });
    }

    private void updateParticles() {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        for (Particle p : particles) {
            p.update(w, h);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (lightAnimationTimer != null && !lightAnimationTimer.isRunning() && animationEnabled) {
            lightAnimationTimer.start();
        }
    }

    @Override
    public void removeNotify() {
        if (lightAnimationTimer != null && lightAnimationTimer.isRunning()) {
            lightAnimationTimer.stop();
        }
        super.removeNotify();
    }
    
    public void cleanup() {
        if (lightAnimationTimer != null) {
            lightAnimationTimer.stop();
        }
    }

    public void setAnimationEnabled(boolean enabled) {
        this.animationEnabled = enabled;
        if (!enabled && lightAnimationTimer.isRunning()) {
            lightAnimationTimer.stop();
        } else if (enabled && isShowing() && !lightAnimationTimer.isRunning()) {
            lightAnimationTimer.start();
        }
    }

    public void setIntensity(Intensity intensity) {
        this.intensity = intensity;
        repaint();
    }

    public void setDarkOverlayOpacity(float opacity) {
        this.darkOverlayOpacity = Math.max(0.40f, Math.min(0.90f, opacity));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // 1. BASE BACKGROUND: Deep Space Gradient (#0a0a1a to #1a1a2e)
        GradientPaint linearGradient = new GradientPaint(
                0, 0, new Color(10, 10, 26),
                0, h, new Color(26, 26, 46)
        );
        g2.setPaint(linearGradient);
        g2.fillRect(0, 0, w, h);

        // 2. SUBTLE FILM GRAIN TEXTURE
        Random noiseRandom = new Random(12345);
        int grainDensity = w * h / 200; // Scaled to size
        g2.setColor(new Color(255, 255, 255, 6)); // very faint white grain
        for (int i = 0; i < grainDensity; i++) {
            int gx = noiseRandom.nextInt(w);
            int gy = noiseRandom.nextInt(h);
            g2.fillRect(gx, gy, 1, 1);
        }

        // 3. CINEMATIC DECORATIONS (Film reels in corners)
        paintCinematicDecorations(g2, w, h);

        // 4. SPOTLIGHT EFFECTS
        if (intensity == Intensity.STRONG) {
            paintProjectors(g2, w, h);
        }

        // 5. STAR PARTICLES
        paintParticles(g2);

        // 6. DARK OVERLAY
        int overlayAlpha = (int) (darkOverlayOpacity * 255);
        g2.setColor(new Color(5, 11, 22, overlayAlpha));
        g2.fillRect(0, 0, w, h);

        g2.dispose();
    }

    private void paintProjectors(Graphics2D g2, int w, int h) {
        // Spotlight pulse effects
        double pulseLeft = 0.85 + 0.15 * Math.sin(ambientPhase * 1.5);
        double pulseRight = 0.85 + 0.15 * Math.cos(ambientPhase * 1.5);

        // Top-left blue spotlight cone
        Path2D leftCone = new Path2D.Double();
        leftCone.moveTo(0, 0);
        leftCone.lineTo(w * 0.45, h);
        leftCone.lineTo(w * 0.95, h);
        leftCone.closePath();

        Color blueSpot = new Color(0, 180, 216, (int) (40 * pulseLeft));
        Color blueFade = new Color(0, 180, 216, 0);
        LinearGradientPaint leftGrad = new LinearGradientPaint(
            0, 0, (float) (w * 0.75), (float) h,
            new float[]{0.0f, 1.0f},
            new Color[]{blueSpot, blueFade}
        );
        g2.setPaint(leftGrad);
        g2.fill(leftCone);

        // Top-right purple spotlight cone
        Path2D rightCone = new Path2D.Double();
        rightCone.moveTo(w, 0);
        rightCone.lineTo(w * 0.55, h);
        rightCone.lineTo(w * 0.05, h);
        rightCone.closePath();

        Color purpleSpot = new Color(139, 92, 246, (int) (40 * pulseRight));
        Color purpleFade = new Color(139, 92, 246, 0);
        LinearGradientPaint rightGrad = new LinearGradientPaint(
            w, 0, (float) (w * 0.25), (float) h,
            new float[]{0.0f, 1.0f},
            new Color[]{purpleSpot, purpleFade}
        );
        g2.setPaint(rightGrad);
        g2.fill(rightCone);

        // Ambient Corner Glows
        float glowRadius = Math.min(w, h) * 0.75f;
        float[] dists = {0.0f, 1.0f};

        RadialGradientPaint leftCornerGlow = new RadialGradientPaint(
            0f, 0f, glowRadius, dists,
            new Color[]{new Color(0, 180, 216, (int) (55 * pulseLeft)), new Color(0, 0, 0, 0)}
        );
        g2.setPaint(leftCornerGlow);
        g2.fillRect(0, 0, w, h);

        RadialGradientPaint rightCornerGlow = new RadialGradientPaint(
            w, 0f, glowRadius, dists,
            new Color[]{new Color(139, 92, 246, (int) (55 * pulseRight)), new Color(0, 0, 0, 0)}
        );
        g2.setPaint(rightCornerGlow);
        g2.fillRect(0, 0, w, h);
    }

    private void paintParticles(Graphics2D g2) {
        for (Particle p : particles) {
            double twinkle = Math.sin(ambientPhase * p.twinkleSpeed + p.twinkleOffset);
            float alpha = (float) (0.3 + 0.7 * (twinkle + 1.0) / 2.0);
            alpha = Math.max(0.1f, Math.min(1.0f, alpha));

            Color baseColor;
            if (p.type == 0) baseColor = new Color(168, 85, 247); // purple
            else if (p.type == 1) baseColor = new Color(0, 180, 216); // blue/cyan
            else baseColor = new Color(255, 255, 255); // white star

            g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int) (alpha * 180)));
            g2.fill(new Ellipse2D.Double(p.x, p.y, p.size, p.size));
        }
    }

    private void paintCinematicDecorations(Graphics2D g2, int w, int h) {
        int patternAlpha = (intensity == Intensity.STRONG) ? 10 : 6;
        g2.setStroke(new BasicStroke(1.0f));

        // Film Reel outlines rotating slowly in the background corners
        // Top-Right Corner
        g2.setColor(new Color(168, 85, 247, patternAlpha));
        Graphics2D gr = (Graphics2D) g2.create();
        gr.translate(w - 60, 80);
        gr.rotate(ambientPhase * 0.05);
        drawFilmReelOutline(gr, 0, 0, 70);
        gr.dispose();
        
        // Bottom-Left Corner
        g2.setColor(new Color(0, 180, 216, patternAlpha));
        Graphics2D gl = (Graphics2D) g2.create();
        gl.translate(60, h - 100);
        gl.rotate(-ambientPhase * 0.05);
        drawFilmReelOutline(gl, 0, 0, 70);
        gl.dispose();

        // Moving film strips at the borders
        g2.setColor(new Color(42, 58, 74, patternAlpha));
        double stripOffset = (ambientPhase * 10) % 40;
        drawFilmStrip(g2, 10, (int) -stripOffset, h + 80);
        drawFilmStrip(g2, w - 26, (int) -stripOffset, h + 80);
    }

    private void drawFilmReelOutline(Graphics2D g2, int cx, int cy, int radius) {
        g2.draw(new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2));
        g2.draw(new Ellipse2D.Double(cx - radius * 0.3, cy - radius * 0.3, radius * 0.6, radius * 0.6));
        int numHoles = 6;
        for (int i = 0; i < numHoles; i++) {
            double angle = i * (2 * Math.PI / numHoles);
            double hx = cx + Math.cos(angle) * (radius * 0.65);
            double hy = cy + Math.sin(angle) * (radius * 0.65);
            g2.draw(new Ellipse2D.Double(hx - 8, hy - 8, 16, 16));
        }
    }

    private void drawFilmStrip(Graphics2D g2, int x, int startY, int maxH) {
        for (int y = startY; y < maxH; y += 35) {
            g2.drawRoundRect(x, y, 16, 22, 4, 4);
        }
    }

    private class Particle {
        double x, y;
        double size;
        double twinkleSpeed;
        double twinkleOffset;
        double driftX, driftY;
        int type;

        Particle() {
            resetFull(1920, 1080);
            this.y = random.nextDouble() * 1080;
        }

        void resetFull(int w, int h) {
            this.x = random.nextDouble() * w;
            this.y = random.nextDouble() * h;
            this.size = 1.0 + random.nextDouble() * 2.0;
            this.twinkleSpeed = 0.5 + random.nextDouble() * 1.5;
            this.twinkleOffset = random.nextDouble() * Math.PI * 2;
            this.driftX = -0.05 + random.nextDouble() * 0.1;
            this.driftY = -0.05 - random.nextDouble() * 0.15; // Slow upward drift
            this.type = random.nextInt(3);
        }

        void update(int w, int h) {
            this.x += this.driftX;
            this.y += this.driftY;
            if (this.x < 0 || this.x > w || this.y < 0) {
                this.x = random.nextDouble() * w;
                this.y = h + 10;
            }
        }
    }
}
