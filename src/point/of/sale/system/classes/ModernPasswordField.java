package point.of.sale.system.classes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Modern password field with rounded corners, icon support, and focus glow effect.
 */
public class ModernPasswordField extends JPasswordField {
    private static final long serialVersionUID = 1L;
    private boolean isFocused = false;
    private Color focusGlowColor = new Color(0, 188, 212);
    private int borderRadius = 12;
    private String placeholder = "";

    public ModernPasswordField(int columns) {
        super(columns);
        initPasswordField();
    }

    private void initPasswordField() {
        setOpaque(false);
        setBackground(new Color(255, 255, 255, 240));
        setForeground(new Color(20, 30, 45));
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setCaretColor(new Color(0, 160, 230));
        setBorder(new EmptyBorder(10, 40, 10, 14));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, width, height, borderRadius, borderRadius);

        if (isFocused) {
            g2d.setColor(new Color(focusGlowColor.getRed(), focusGlowColor.getGreen(), focusGlowColor.getBlue(), 70));
            g2d.fillRoundRect(-2, -2, width + 4, height + 4, borderRadius, borderRadius);
        }

        g2d.setColor(isFocused ? focusGlowColor : new Color(180, 190, 205));
        g2d.setStroke(new BasicStroke(isFocused ? 2.0f : 1.0f));
        g2d.drawRoundRect(0, 0, width - 1, height - 1, borderRadius, borderRadius);

        if (!isFocused && getPassword().length == 0 && placeholder != null && !placeholder.isEmpty()) {
            g2d.setColor(new Color(120, 139, 153));
            g2d.setFont(getFont().deriveFont(Font.ITALIC));
            FontMetrics fm = g2d.getFontMetrics();
            int textY = (height - fm.getHeight()) / 2 + fm.getAscent();
            g2d.drawString(placeholder, 14, textY);
        }

        super.paintComponent(g);
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setFocusGlowColor(Color color) {
        this.focusGlowColor = color;
    }

    public void setBorderRadius(int radius) {
        this.borderRadius = radius;
    }
}
