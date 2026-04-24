package point.of.sale.system.screens;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import point.of.sale.system.classes.DBConnection;
import point.of.sale.system.classes.RoundedPanel;
import org.mindrot.jbcrypt.BCrypt;

public class Login extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger
            = java.util.logging.Logger.getLogger(Login.class.getName());

    public Login() {
        initComponents();
        setupForm();

    }

    private void setupForm() {
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(btnLogin);

        bgPanel.setBackground(new Color(232, 239, 248));

        containerPanel.setOpaque(false);
        containerPanel.setBackground(new Color(255, 255, 255, 220));

        leftPanel.setOpaque(true);
        leftPanel.setBackground(new Color(246, 250, 255));

        rightPanel.setOpaque(false);
        rightPanel.setBackground(new Color(255, 255, 255, 0));

        lblBrandName.setForeground(new Color(24, 55, 126));
        lblWelcome.setForeground(new Color(86, 119, 196));
        lblSub.setForeground(new Color(110, 125, 155));
        lblUsername.setForeground(new Color(24, 55, 126));
        lblPassword.setForeground(new Color(24, 55, 126));
        lblFooter.setForeground(new Color(130, 145, 170));

        tt.setForeground(new Color(22, 45, 102));
        jLabel2.setForeground(new Color(72, 92, 135));
        jLabel3.setForeground(new Color(72, 92, 135));

        if (containerPanel instanceof point.of.sale.system.classes.AnimatedGlowingPanel glass) {
            glass.setBorderRadius(34);
            glass.setGlowColor(new Color(0x6A, 0xA8, 0xE8));
            glass.setGlowSpeed(0.025f);
            glass.setGlowWidth(12);
        }

        if (txtUsername instanceof point.of.sale.system.classes.NeoTextField neoUser) {
            neoUser.setPlaceholder("Enter your username");
        }

        if (txtPassword instanceof point.of.sale.system.classes.NeoPasswordField neoPass) {
            neoPass.setPlaceholder("Enter your password");
            neoPass.setEchoChar('•');
        } else {
            txtPassword.setEchoChar('•');
        }

        if (btnLogin instanceof point.of.sale.system.classes.EnhancedGradientButton egBtn) {
            egBtn.setGradientColors(
                    new Color(0x8D, 0xD0, 0xFC),
                    new Color(0x10, 0x37, 0x83)
            );
            egBtn.setBorderRadius(24);
            btnLogin.setFont(new Font("Tahoma", Font.BOLD, 17));
            btnLogin.setForeground(Color.WHITE);
        }

        showPassword.setOpaque(false);
        showPassword.setForeground(new Color(95, 110, 145));
        showPassword.setFont(new Font("Tahoma", Font.PLAIN, 12));
        showPassword.setFocusPainted(false);
        showPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));

        loadBrandIcon();
        setupPreview();
    }

    private void loadBrandIcon() {
        try {
            ImageIcon icon = new ImageIcon(
                    getClass().getResource("/point/of/sale/system/icons/brandlogo.png")
            );
            Image scaled = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            lblBrandIcon.setIcon(new ImageIcon(scaled));
            lblBrandIcon.setText("");
            lblBrandIcon.setHorizontalAlignment(SwingConstants.CENTER);
        } catch (Exception e) {
            lblBrandIcon.setText("•");
            lblBrandIcon.setForeground(new Color(40, 104, 232));
            lblBrandIcon.setFont(new Font("Tahoma", Font.BOLD, 18));
        }
    }

    private void setupPreview() {
        try {
            ImageIcon preview = new ImageIcon(
                    getClass().getResource("/point/of/sale/system/icons/dashboard_preview.png")
            );
            Image scaled = preview.getImage().getScaledInstance(630, 480, Image.SCALE_SMOOTH);
            lblDashboardPreview.setIcon(new ImageIcon(scaled));
            lblDashboardPreview.setText("");
            lblDashboardPreview.setHorizontalAlignment(SwingConstants.CENTER);
            lblDashboardPreview.setVerticalAlignment(SwingConstants.CENTER);
            lblDashboardPreview.setOpaque(false);
        } catch (Exception e) {
            lblDashboardPreview.setIcon(null);
            lblDashboardPreview.setOpaque(true);
            lblDashboardPreview.setBackground(Color.WHITE);
            lblDashboardPreview.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(10, 10, 10, 10),
                    BorderFactory.createLineBorder(new Color(200, 210, 230))
            ));
            lblDashboardPreview.setHorizontalAlignment(SwingConstants.CENTER);
            lblDashboardPreview.setVerticalAlignment(SwingConstants.CENTER);
            lblDashboardPreview.setForeground(new Color(95, 112, 145));
            lblDashboardPreview.setFont(new Font("Tahoma", Font.PLAIN, 16));
            lblDashboardPreview.setText("<html><center>"
                    + "<div style='font-size:20px; font-weight:700; color:#24395f;'>Dashboard Preview</div>"
                    + "<div style='margin-top:10px;'>Manage sales, inventory, reports, and users in one place.</div>"
                    + "</center></html>");
        }
    }

    private void loginUser() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.");
            return;
        }

        String sql = "SELECT user_id, username, role, password, status FROM users WHERE username = ? LIMIT 1";

        try (Connection conn = DBConnection.dbConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, username);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    boolean passwordMatches;

                    if (storedPassword.startsWith("$2a$")) {
                        passwordMatches = BCrypt.checkpw(password, storedPassword);
                    } else {
                        passwordMatches = password.equals(storedPassword);
                    }

                    if (passwordMatches) {
                        // Check if user is active
                        String userStatus = rs.getString("status");
                        if (!"Active".equalsIgnoreCase(userStatus)) {
                            JOptionPane.showMessageDialog(this, "This account is inactive. Please contact administrator.");
                            txtPassword.setText("");
                            txtPassword.requestFocus();
                            return;
                        }

                        new MainFrame(
                                rs.getInt("user_id"),
                                rs.getString("username"),
                                rs.getString("role")
                        ).setVisible(true);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid username or password.");
                        txtPassword.setText("");
                        txtPassword.requestFocus();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password.");
                    txtPassword.setText("");
                    txtPassword.requestFocus();
                }
            }

        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Login error", ex);
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgPanel = new javax.swing.JPanel();
        containerPanel = new point.of.sale.system.classes.AnimatedGlowingPanel(
            new java.awt.Color(0x5B, 0x8E, 0xDB)
        );
        leftPanel = new javax.swing.JPanel();
        lblWelcome = new javax.swing.JLabel("Login to Dashboard") {

            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();

                // Smooth text
                g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Gradient colors (#9bafd9 → #103783)
                float[] fractions = {0f, 1f};
                java.awt.Color[] colors = {
                    new java.awt.Color(0x9B, 0xAF, 0xD9),
                    new java.awt.Color(0x10, 0x37, 0x83)
                };

                java.awt.LinearGradientPaint lgp = new java.awt.LinearGradientPaint(
                    0, 0, getWidth(), 0,
                    fractions, colors
                );

                g2.setPaint(lgp);

                // Center text
                java.awt.FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();

                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };
        lblBrandName = new javax.swing.JLabel();
        lblSub = new javax.swing.JLabel();
        lblPassword = new javax.swing.JLabel();
        lblBrandIcon = new javax.swing.JLabel();
        txtUsername = new point.of.sale.system.classes.NeoTextField(20);
        lblUsername = new javax.swing.JLabel();
        txtPassword = new point.of.sale.system.classes.NeoPasswordField(20);
        showPassword = new javax.swing.JCheckBox();
        btnLogin = new point.of.sale.system.classes.EnhancedGradientButton(
            "LOGIN",
            new java.awt.Color(0x9B, 0xAF, 0xD9),
            new java.awt.Color(0x10, 0x37, 0x83)
        );
        lblFooter = new javax.swing.JLabel();
        lblBrandName1 = new javax.swing.JLabel();
        rightPanel = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                    java.awt.RenderingHints.VALUE_RENDER_QUALITY);

                int w = getWidth();
                int h = getHeight();
                int arc = 34;

                java.awt.GradientPaint gp = new java.awt.GradientPaint(
                    0, 0, new java.awt.Color(0x8D, 0xD0, 0xFC),
                    0, h, new java.awt.Color(0x5B, 0x8E, 0xDB)
                );

                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, arc, arc);

                g2.setColor(new java.awt.Color(255, 255, 255, 65));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

                g2.dispose();
            }
        };
        tt = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblDashboardPreview = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        bgPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        containerPanel.setBackground(new java.awt.Color(255, 255, 255));
        containerPanel.setPreferredSize(new java.awt.Dimension(1150, 700));
        containerPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        leftPanel.setBackground(new java.awt.Color(255, 255, 255));
        leftPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblWelcome.setFont(new java.awt.Font("Tahoma", 1, 48)); // NOI18N
        lblWelcome.setText("WELCOME");
        leftPanel.add(lblWelcome, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 160, -1, 50));

        lblBrandName.setFont(new java.awt.Font("Tahoma", 1, 21)); // NOI18N
        lblBrandName.setText("POINT OF SALE");
        leftPanel.add(lblBrandName, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 40, 190, 20));

        lblSub.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblSub.setText("Enter your username and password to access your account");
        leftPanel.add(lblSub, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 210, -1, -1));

        lblPassword.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblPassword.setText("Password");
        leftPanel.add(lblPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 375, 100, 20));

        lblBrandIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/brandlogo.png"))); // NOI18N
        leftPanel.add(lblBrandIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 70, 70));

        txtUsername.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        leftPanel.add(txtUsername, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 310, 340, 42));

        lblUsername.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblUsername.setText("Username");
        leftPanel.add(lblUsername, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 285, 100, 20));
        leftPanel.add(txtPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 400, 340, 42));

        showPassword.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        showPassword.setForeground(new java.awt.Color(120, 120, 135));
        showPassword.setText("Show password");
        showPassword.addActionListener(this::showPasswordActionPerformed);
        leftPanel.add(showPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 450, -1, -1));

        btnLogin.setText("LOGIN");
        btnLogin.addActionListener(this::btnLoginActionPerformed);
        leftPanel.add(btnLogin, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 500, 340, 42));

        lblFooter.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFooter.setText("© 2026 POS System. All rights reserved.");
        leftPanel.add(lblFooter, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 600, -1, -1));

        lblBrandName1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        lblBrandName1.setText("SYSTEM");
        leftPanel.add(lblBrandName1, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 60, 130, 20));

        containerPanel.add(leftPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 480, 700));

        rightPanel.setBackground(new java.awt.Color(204, 204, 255));
        rightPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tt.setFont(new java.awt.Font("Tahoma", 1, 27)); // NOI18N
        tt.setText("Effortlessly manage your POS and your team");
        rightPanel.add(tt, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, 690, -1));

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel2.setText("Login to access your POS Dashboard. ");
        rightPanel.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, -1));

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel3.setText("Simplify transactions, control inventory, and gain insights with ease.");
        rightPanel.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, -1, -1));

        lblDashboardPreview.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/dashboard_preview.png"))); // NOI18N
        lblDashboardPreview.setText("---");
        rightPanel.add(lblDashboardPreview, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, 630, 480));

        containerPanel.add(rightPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 0, 670, 700));

        bgPanel.add(containerPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(145, 100, 1150, 700));

        getContentPane().add(bgPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1440, 900));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
        loginUser();
    }//GEN-LAST:event_btnLoginActionPerformed

    private void showPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPasswordActionPerformed
        if (showPassword.isSelected()) {
            txtPassword.setEchoChar((char) 0);
        } else {
            txtPassword.setEchoChar('•');
        }
    }//GEN-LAST:event_showPasswordActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Login().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bgPanel;
    private javax.swing.JButton btnLogin;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel lblBrandIcon;
    private javax.swing.JLabel lblBrandName;
    private javax.swing.JLabel lblBrandName1;
    private javax.swing.JLabel lblDashboardPreview;
    private javax.swing.JLabel lblFooter;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblSub;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JCheckBox showPassword;
    private javax.swing.JLabel tt;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
