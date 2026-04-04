package point.of.sale.system.classes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class NeoPasswordField extends JPasswordField {
    private static final long serialVersionUID = 1L;
    private String placeholder = "";
    private boolean focused;

    public NeoPasswordField(int columns) {
        super(columns);
        init();
    }

    private void init() {
        setOpaque(false);
        setForeground(new Color(20, 30, 60));
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setBorder(new EmptyBorder(10, 14, 10, 14));
        setCaretColor(new Color(0, 180, 240));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                focused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                focused = false;
                repaint();
            }
        });
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(255, 255, 255, 220));
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

        Color border = focused ? new Color(0, 180, 240) : new Color(170, 180, 200);
        g2d.setColor(border);
        g2d.setStroke(new BasicStroke(focused ? 2f : 1f));
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

        g2d.dispose();
        super.paintComponent(g);

        if (!focused && String.valueOf(getPassword()).isEmpty() && placeholder != null && !placeholder.isEmpty()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(new Color(120, 140, 170));
            g2.setFont(getFont().deriveFont(Font.ITALIC));
            Insets insets = getInsets();
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(placeholder, insets.left, y);
            g2.dispose();
        }
    }
}