package point.of.sale.system.classes;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;

public class GradientFont extends JLabel {

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics fm = g2.getFontMetrics();
        
        int textWidth = fm.stringWidth(getText());
        int x = (getWidth() - textWidth) / 2;
        int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
        
        GradientPaint gp = new GradientPaint(0, 0, new Color(35, 50, 180), getWidth(), 0, new Color(90, 120, 255));
        g2.setPaint(gp);
        g2.setFont(getFont());
        g2.drawString(getText(), x, y);
        g2.dispose();
    }
}
