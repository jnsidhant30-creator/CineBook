package com.movieticket.view.components;

import javax.swing.*;
import java.awt.*;

public class ResponsiveGridPanel extends JPanel implements Scrollable {

    public ResponsiveGridPanel() {
        super(new GridLayout(0, 6, 20, 20));
        setOpaque(false);
        setBorder(new javax.swing.border.EmptyBorder(0, 40, 0, 40));
    }
    
    public ResponsiveGridPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 20;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return Math.max(20, visibleRect.height - 20);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
