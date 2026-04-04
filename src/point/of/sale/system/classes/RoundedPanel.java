package point.of.sale.system.classes;

import java.awt.*;
import javax.swing.JPanel;

public class RoundedPanel extends JPanel {

    private int cornerRadius = 25;

    public RoundedPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int glowSize = 15; // thickness of glow
        int width = getWidth();
        int height = getHeight();

        // ---- GLOW EFFECT (soft layered glow) ----
        for (int i = glowSize; i >= 1; i--) {
            float alpha = (float) i / (glowSize * 8); // soft fade
            g2.setColor(new Color(100, 149, 237, (int) (alpha * 255))); // cold blue glow

            g2.fillRoundRect(
                    i,
                    i,
                    width - (i * 2),
                    height - (i * 2),
                    cornerRadius,
                    cornerRadius
            );
        }

        // ---- MAIN PANEL ----
        g2.setColor(getBackground());
        g2.fillRoundRect(
                glowSize / 2,
                glowSize / 2,
                width - glowSize,
                height - glowSize,
                cornerRadius,
                cornerRadius
        );

        g2.dispose();
    }
}