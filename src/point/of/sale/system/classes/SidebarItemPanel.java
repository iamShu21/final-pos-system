package point.of.sale.system.classes;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class SidebarItemPanel extends JPanel {

    private boolean hovered = false;
    private boolean selected = false;

    private final int radius = 28;

    public SidebarItemPanel() {
        setOpaque(false);
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;
        repaint();
    }

    public void setSelectedItem(boolean selected) {
        this.selected = selected;
        repaint();
    }

    public boolean isSelectedItem() {
        return selected;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        if (selected) {
            // stronger outer glow only
            g2.setColor(new Color(97, 180, 255, 70));
            g2.fillRoundRect(0, 0, w, h, radius + 14, radius + 14);

            g2.setColor(new Color(97, 180, 255, 45));
            g2.fillRoundRect(2, 2, w - 4, h - 4, radius + 10, radius + 10);

            g2.setColor(new Color(97, 180, 255, 25));
            g2.fillRoundRect(4, 4, w - 8, h - 8, radius + 6, radius + 6);

            // main gradient body
            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(102, 120, 255),
                    w, 0, new Color(84, 198, 235)
            );
            g2.setPaint(gp);
            g2.fillRoundRect(6, 6, w - 12, h - 12, radius, radius);

        } else if (hovered) {
            // softer hover without inner effect
            g2.setColor(new Color(255, 255, 255, 20));
            g2.fillRoundRect(4, 4, w - 8, h - 8, radius, radius);

            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(85, 105, 220, 90),
                    w, 0, new Color(90, 185, 225, 90)
            );
            g2.setPaint(gp);
            g2.fillRoundRect(6, 6, w - 12, h - 12, radius, radius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
