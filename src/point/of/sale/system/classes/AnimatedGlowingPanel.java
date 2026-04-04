package point.of.sale.system.classes;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class AnimatedGlowingPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private float glowAlpha = 0.42f;
    private boolean glowIncreasing = true;
    private float glowSpeed = 0.018f;

    private Color glowColor;
    private int borderRadius = 34;
    private int borderWidth = 1;
    private int glowWidth = 8;

    private Timer animationTimer;

    public AnimatedGlowingPanel() {
        this(new Color(79, 141, 253));
    }

    public AnimatedGlowingPanel(Color glowColor) {
        this.glowColor = glowColor;
        setOpaque(false);
        startAnimation();
    }

    private void startAnimation() {
        animationTimer = new Timer(16, e -> {
            if (glowIncreasing) {
                glowAlpha += glowSpeed;
                if (glowAlpha >= 0.75f) {
                    glowAlpha = 0.75f;
                    glowIncreasing = false;
                }
            } else {
                glowAlpha -= glowSpeed;
                if (glowAlpha <= 0.35f) {
                    glowAlpha = 0.35f;
                    glowIncreasing = true;
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
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();

        Shape panelShape = new RoundRectangle2D.Float(0, 0, w - 1, h - 1, borderRadius, borderRadius);

        // soft outer shadow
        for (int i = 18; i >= 1; i--) {
            float alpha = 0.006f * i;
            g2.setColor(new Color(90 / 255f, 105 / 255f, 170 / 255f, alpha));
            g2.fillRoundRect(i / 2, i / 2, w - i, h - i, borderRadius + 8, borderRadius + 8);
        }

        // soft glow border
        for (int i = glowWidth; i >= 1; i--) {
            float alpha = (glowAlpha * 0.06f) * ((float) i / glowWidth);
            g2.setColor(new Color(
                    glowColor.getRed() / 255f,
                    glowColor.getGreen() / 255f,
                    glowColor.getBlue() / 255f,
                    alpha
            ));
            g2.drawRoundRect(
                    i / 2,
                    i / 2,
                    w - i,
                    h - i,
                    borderRadius + 4,
                    borderRadius + 4
            );
        }

        // body clip
        g2.setClip(panelShape);

        // main body gradient: #6F7BF7 -> #9BAFD9
        GradientPaint bodyGradient = new GradientPaint(
                0, 0, new Color(0x6F, 0x7B, 0xF7),
                0, h, new Color(0x9B, 0xAF, 0xD9)
        );
        g2.setPaint(bodyGradient);
        g2.fillRoundRect(0, 0, w - 1, h - 1, borderRadius, borderRadius);

        // top glossy highlight
        GradientPaint highlightGradient = new GradientPaint(
                0, 0, new Color(255, 255, 255, 85),
                0, h / 2, new Color(255, 255, 255, 18)
        );
        g2.setPaint(highlightGradient);
        g2.fillRoundRect(0, 0, w - 1, h / 2, borderRadius, borderRadius);

        g2.setClip(null);

        // border
        GradientPaint borderGradient = new GradientPaint(
                0, 0, new Color(255, 255, 255, 170),
                w, h, new Color(
                        glowColor.getRed(),
                        glowColor.getGreen(),
                        glowColor.getBlue(),
                        (int) (180 * glowAlpha)
                )
        );
        g2.setPaint(borderGradient);
        g2.setStroke(new BasicStroke(borderWidth + 0.5f));
        g2.drawRoundRect(
                borderWidth,
                borderWidth,
                w - (borderWidth * 2) - 1,
                h - (borderWidth * 2) - 1,
                borderRadius,
                borderRadius
        );

        // subtle inner highlight line
        g2.setColor(new Color(255, 255, 255, 45));
        g2.drawRoundRect(
                borderWidth + 1,
                borderWidth + 1,
                w - (borderWidth * 2) - 3,
                h - (borderWidth * 2) - 3,
                borderRadius - 2,
                borderRadius - 2
        );

        g2.dispose();
    }

    @Override
    protected void paintChildren(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape clip = new RoundRectangle2D.Float(
                2, 2, getWidth() - 4, getHeight() - 4, borderRadius, borderRadius
        );
        g2.setClip(clip);

        super.paintChildren(g2);
        g2.dispose();
    }

    public void setGlowColor(Color color) {
        this.glowColor = color;
        repaint();
    }

    public void setBorderRadius(int radius) {
        this.borderRadius = radius;
        repaint();
    }

    public void setGlowSpeed(float speed) {
        this.glowSpeed = speed;
    }

    public void setGlowWidth(int glowWidth) {
        this.glowWidth = glowWidth;
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