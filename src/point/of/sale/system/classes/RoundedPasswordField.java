package point.of.sale.system.classes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Insets;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

public class RoundedPasswordField extends JPasswordField {

    private final int radius = 25;

    public RoundedPasswordField() {
        setOpaque(false);
        setBackground(new Color(245, 245, 245));
        setForeground(new Color(60, 60, 60));
        setCaretColor(new Color(40, 40, 40));
        setBorder(new EmptyBorder(10, 15, 10, 15));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(210, 210, 210));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

        g2.dispose();
    }

    @Override
    public Insets getInsets() {
        return new Insets(10, 15, 10, 15);
    }
}