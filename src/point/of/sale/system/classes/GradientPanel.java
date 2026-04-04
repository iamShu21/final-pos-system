package point.of.sale.system.classes;

import java.awt.*;
import javax.swing.JPanel;

public class GradientPanel extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        int width = getWidth();
        int height = getHeight();

        Color color1 = new Color(38, 54, 170);   // dark blue
        Color color2 = new Color(91, 166, 229);  // light blue

        GradientPaint gp = new GradientPaint(
                0, 0, color1,
                width, height, color2
        );

        g2d.setPaint(gp);
        g2d.fillRect(0, 0, width, height);
    }
}