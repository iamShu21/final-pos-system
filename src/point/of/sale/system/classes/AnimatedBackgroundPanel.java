package point.of.sale.system.classes;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

public class AnimatedBackgroundPanel extends JPanel {
    private final List<FloatingCircle> circles;
    private final Timer animationTimer;
    private final Random random = new Random();

    public AnimatedBackgroundPanel() {
        circles = new ArrayList<>();
        initializeCircles();

        animationTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCircles();
                repaint();
            }
        });
        animationTimer.start();
    }

    private void initializeCircles() {
        // Create 6 subtle floating circles
        for (int i = 0; i < 6; i++) {
            circles.add(new FloatingCircle());
        }
    }

    private void updateCircles() {
        for (FloatingCircle circle : circles) {
            circle.update();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Draw dark navy to blue gradient background
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(10, 25, 47),  // Dark navy
            0, getHeight(), new Color(13, 27, 42)  // Slightly lighter blue
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw floating glowing circles
        for (FloatingCircle circle : circles) {
            circle.draw(g2d);
        }

        g2d.dispose();
    }

    private class FloatingCircle {
        private double x, y;
        private double dx, dy;
        private int size;
        private Color color;
        private float alpha;

        public FloatingCircle() {
            reset();
        }

        private void reset() {
            x = random.nextInt(1440);
            y = random.nextInt(900);
            dx = (random.nextDouble() - 0.5) * 0.5; // Slow movement
            dy = (random.nextDouble() - 0.5) * 0.5;
            size = 30 + random.nextInt(50); // Size between 30-80
            // Soft blue/cyan colors
            int blue = 150 + random.nextInt(100);
            int green = 100 + random.nextInt(100);
            color = new Color(0, green, blue);
            alpha = 0.1f + random.nextFloat() * 0.2f; // Subtle alpha
        }

        public void update() {
            x += dx;
            y += dy;

            // Wrap around edges
            if (x < -size) x = 1440 + size;
            if (x > 1440 + size) x = -size;
            if (y < -size) y = 900 + size;
            if (y > 900 + size) y = -size;
        }

        public void draw(Graphics2D g2d) {
            Ellipse2D circle = new Ellipse2D.Double(x - size/2, y - size/2, size, size);

            // Draw glow effect with multiple layers
            for (int i = 3; i >= 0; i--) {
                float currentAlpha = alpha * (1.0f - i * 0.2f);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentAlpha));
                g2d.setColor(color);
                double scale = 1.0 + i * 0.3;
                Ellipse2D glowCircle = new Ellipse2D.Double(
                    x - (size * scale)/2,
                    y - (size * scale)/2,
                    size * scale,
                    size * scale
                );
                g2d.fill(glowCircle);
            }

            // Reset composite
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }
}