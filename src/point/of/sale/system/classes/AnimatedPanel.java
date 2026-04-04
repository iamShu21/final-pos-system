package point.of.sale.system.classes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AnimatedPanel extends JPanel {

    private int x = 0;

    public AnimatedPanel() {
        Timer timer = new Timer(20, e -> {
            x += 5;
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.fillRect(x, 50, 60, 60);
    }
}