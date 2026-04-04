package point.of.sale.system.classes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EnhancedGradientButton extends JButton {
    private static final long serialVersionUID = 1L;

    private Color startColor;
    private Color endColor;
    private float hoverAlpha = 0.0f;
    private boolean isPressed = false;
    private Timer animationTimer;
    private int borderRadius = 18;

    public EnhancedGradientButton(String text) {
        super(text);
        this.startColor = new Color(0x8D, 0xD0, 0xFC);
        this.endColor = new Color(0x10, 0x37, 0x83);
        initButton();
    }

    public EnhancedGradientButton(String text, Color startColor, Color endColor) {
        super(text);
        this.startColor = startColor;
        this.endColor = endColor;
        initButton();
    }

    private void initButton() {
        setContentAreaFilled(false);
        setOpaque(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setFont(new Font("Tahoma", Font.BOLD, 14));
        setForeground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder());
        setMargin(new Insets(0, 0, 0, 0));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                animateHover(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                animateHover(false);
                isPressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    private void animateHover(boolean enter) {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        animationTimer = new Timer(16, e -> {
            if (enter) {
                hoverAlpha += 0.08f;
                if (hoverAlpha >= 1.0f) {
                    hoverAlpha = 1.0f;
                    ((Timer) e.getSource()).stop();
                }
            } else {
                hoverAlpha -= 0.08f;
                if (hoverAlpha <= 0.0f) {
                    hoverAlpha = 0.0f;
                    ((Timer) e.getSource()).stop();
                }
            }
            repaint();
        });
        animationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        int pad = 2; // keeps all effects inside the component bounds
        float scale = isPressed ? 0.985f : 1.0f;
        int bw = (int) ((w - pad * 2) * scale);
        int bh = (int) ((h - pad * 2) * scale);
        int bx = (w - bw) / 2;
        int by = (h - bh) / 2;

        Shape buttonShape = new java.awt.geom.RoundRectangle2D.Float(
                bx, by, bw - 1, bh - 1, borderRadius, borderRadius
        );

        // Inner hover glow only, no outside spill
        if (hoverAlpha > 0f) {
            GradientPaint hoverGlow = new GradientPaint(
                    bx, by, new Color(255, 255, 255, (int) (45 * hoverAlpha)),
                    bx, by + bh, new Color(255, 255, 255, 0)
            );
            g2.setPaint(hoverGlow);
            g2.fillRoundRect(bx, by, bw, bh, borderRadius, borderRadius);
        }

        // Main gradient
        GradientPaint gp = new GradientPaint(
                bx, by, startColor,
                bx + bw, by, endColor
        );
        g2.setPaint(gp);
        g2.fill(buttonShape);

        // Top highlight
        g2.setClip(buttonShape);
        GradientPaint topLight = new GradientPaint(
                0, by, new Color(255, 255, 255, 90),
                0, by + bh / 2, new Color(255, 255, 255, 0)
        );
        g2.setPaint(topLight);
        g2.fillRect(bx, by, bw, bh / 2);
        g2.setClip(null);

        // Border
        g2.setColor(new Color(255, 255, 255, (int) (90 + 50 * hoverAlpha)));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(bx, by, bw - 1, bh - 1, borderRadius, borderRadius);

        // Text
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int tx = bx + (bw - fm.stringWidth(getText())) / 2;
        int ty = by + ((bh - fm.getHeight()) / 2) + fm.getAscent();
        g2.setColor(getForeground());
        g2.drawString(getText(), tx, ty);

        g2.dispose();
    }

    public void setGradientColors(Color startColor, Color endColor) {
        this.startColor = startColor;
        this.endColor = endColor;
        repaint();
    }

    public void setBorderRadius(int radius) {
        this.borderRadius = radius;
        repaint();
    }

    @Override
    public void removeNotify() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        super.removeNotify();
    }
}