package point.of.sale.system.classes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Premium rounded neon button with hover/pressed effects.
 */
public class NeonButton extends JButton {
    private static final long serialVersionUID = 1L;
    private float hoverProgress = 0f;
    private boolean hover;
    private boolean pressed;

    public NeonButton(String text) {
        super(text);
        init();
    }

    private void init() {
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.BOLD, 15));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                animateHover(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                animateHover(false);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    private void animateHover(boolean toOn) {
        Timer timer = new Timer(20, null);
        timer.addActionListener(e -> {
            if (toOn) {
                hoverProgress = Math.min(1f, hoverProgress + 0.08f);
            } else {
                hoverProgress = Math.max(0f, hoverProgress - 0.08f);
            }
            repaint();
            if (hoverProgress <= 0f && !toOn || hoverProgress >= 1f && toOn) {
                ((Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        float alpha = pressed ? 0.85f : 1f;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        Color c1 = new Color(0, 120, 200);
        Color c2 = new Color(0, 185, 250);
        GradientPaint gp = new GradientPaint(0, 0, c1, 0, h, c2);
        g2d.setPaint(gp);
        g2d.fillRoundRect(0, 0, w, h, 18, 18);

        Color glow = new Color(0, 210, 255, (int) (hoverProgress * 180));
        g2d.setColor(glow);
        g2d.setStroke(new BasicStroke(3f));
        g2d.drawRoundRect(1, 1, w - 2, h - 2, 18, 18);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        g2d.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 120), 0, h, new Color(255, 255, 255, 0)));
        g2d.fillRoundRect(0, 0, w, h, 18, 18);

        g2d.dispose();
        super.paintComponent(g);
    }
}
