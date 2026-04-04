package point.of.sale.system.classes;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;

public class GradientButton extends JButton {

    private boolean hover = false;
    private final int radius = 25;

    public GradientButton() {
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(Color.WHITE);
        setOpaque(false);

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                hover = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (hover) {
            g2.setColor(new Color(90, 120, 255, 70));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            g2.setColor(new Color(90, 120, 255, 40));
            g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, radius, radius);
        }

        Color start;
        Color end;

        if (hover) {
            start = new Color(60, 80, 220);
            end = new Color(110, 140, 255);
        } else {
            start = new Color(17, 31, 162);
            end = new Color(60, 90, 255);
        }

        GradientPaint gp = new GradientPaint(0, 0, start, getWidth(), 0, end);
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        g2.dispose();
        super.paintComponent(g);
    }
}
