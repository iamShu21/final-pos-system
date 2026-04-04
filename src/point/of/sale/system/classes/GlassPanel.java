package point.of.sale.system.classes;

import javax.swing.*;
import java.awt.*;

/**
 * Glass panel (horizontal gradient: EBF4F5 → B5C6E0)
 * Clean, modern, matches POS UI
 */
public class GlassPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public GlassPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();
        int arc = 28;

        // 🌫️ soft shadow (bluish, matches sidebar theme)
        for (int i = 12; i >= 1; i--) {
            float alpha = 0.008f * i;
            g2.setColor(new Color(60 / 255f, 90 / 255f, 160 / 255f, alpha));
            g2.fillRoundRect(i / 2, i / 2, w - i, h - i, arc + 8, arc + 8);
        }

        // 🌊 MAIN horizontal gradient (LEFT → RIGHT)
        GradientPaint body = new GradientPaint(
                0, 0, new Color(0xEB, 0xF4, 0xF5),
                w, 0, new Color(0xB5, 0xC6, 0xE0)
        );
        g2.setPaint(body);
        g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

        // ✨ top glass highlight
        GradientPaint highlight = new GradientPaint(
                0, 0, new Color(255, 255, 255, 110),
                0, h / 2, new Color(255, 255, 255, 20)
        );
        g2.setPaint(highlight);
        g2.fillRoundRect(1, 1, w - 2, h / 2, arc, arc);

        // 🔲 border (cool blue tone)
        GradientPaint border = new GradientPaint(
                0, 0, new Color(255, 255, 255, 180),
                w, h, new Color(150, 170, 210, 140)
        );
        g2.setPaint(border);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

        // 💎 inner highlight line
        g2.setColor(new Color(255, 255, 255, 60));
        g2.drawRoundRect(1, 1, w - 3, h - 3, arc - 2, arc - 2);

        g2.dispose();
        super.paintComponent(g);
    }
}