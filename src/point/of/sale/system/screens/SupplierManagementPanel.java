package point.of.sale.system.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import point.of.sale.system.classes.DBConnection;
import point.of.sale.system.classes.RoundedPanel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class SupplierManagementPanel extends javax.swing.JPanel {

    private static final java.util.logging.Logger logger
            = java.util.logging.Logger.getLogger(SupplierManagementPanel.class.getName());

    private int selectedSupplierId = -1;

    public SupplierManagementPanel() {
        initComponents();

        applyModuleTheme();
        installValidationFilters();
        enhanceTable(tblSuppliersList, jScrollPane2);

        btnUpdate.setEnabled(false);
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);

        loadSuppliers();
        enableRowSelection();
        initializeSearchListener();
    }

    private void applyModuleTheme() {
        setBackground(new java.awt.Color(240, 246, 255));

        // same section colors as ProductsManagementPanel
        jPanel6.setBackground(new java.awt.Color(132, 164, 224));
        jPanel3.setBackground(new java.awt.Color(132, 164, 224));
        jPanel7.setBackground(new java.awt.Color(18, 48, 174));
        jPanel4.setBackground(new java.awt.Color(18, 48, 174));

        if (jLabel10 != null) {
            jLabel10.setForeground(new Color(88, 105, 136));
        }

        styleTextField(txtSupplierName);
        styleTextField(txtContactPerson);
        styleTextField(txtContactNumber);
        styleTextField(txtEmailAddress);
        styleTextField(txtAddress);
        styleTextField(txtSearch);

        styleButton(btnAdd, new java.awt.Color(34, 166, 82));
        styleButton(btnUpdate, new java.awt.Color(52, 120, 246));
        styleButton(btnDelete, new java.awt.Color(220, 53, 69));
        styleButton(btnEdit, new java.awt.Color(243, 156, 18));
        styleButton(btnRefresh, new java.awt.Color(89, 92, 255));

        installRoundedButtonPainter(btnAdd);
        installRoundedButtonPainter(btnUpdate);
        installRoundedButtonPainter(btnDelete);
        installRoundedButtonPainter(btnEdit);
        installRoundedButtonPainter(btnRefresh);
    }

    private void installValidationFilters() {
        txtSupplierName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (Character.isISOControl(c)) {
                    return;
                }

                boolean allowed = Character.isLetterOrDigit(c)
                        || Character.isSpaceChar(c)
                        || c == '-'
                        || c == '&'
                        || c == '\''
                        || c == '.';

                if (!allowed || txtSupplierName.getText().length() >= 100) {
                    e.consume();
                }
            }
        });

        txtContactPerson.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (Character.isISOControl(c)) {
                    return;
                }

                boolean allowed = Character.isLetter(c)
                        || Character.isSpaceChar(c)
                        || c == '-'
                        || c == '\'';

                if (!allowed || txtContactPerson.getText().length() >= 100) {
                    e.consume();
                }
            }
        });

        txtContactNumber.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                String text = txtContactNumber.getText();

                if (Character.isISOControl(c)) {
                    return;
                }

                if (c == '+' && text.isEmpty()) {
                    return;
                }

                if (!Character.isDigit(c) || text.length() >= 13) {
                    e.consume();
                }
            }
        });

        txtEmailAddress.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (txtEmailAddress.getText().length() >= 100) {
                    e.consume();
                }
            }
        });

        txtAddress.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (txtAddress.getText().length() >= 255) {
                    e.consume();
                }
            }
        });
    }

    private String normalizeSpaces(String text) {
        return text == null ? "" : text.trim().replaceAll("\\s{2,}", " ");
    }

    private boolean containsLetter(String text) {
        return text != null && text.matches(".*[A-Za-z].*");
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isValidPHContactNumber(String number) {
        return number.matches("^(\\+63|0)\\d{10}$");
    }

    private void styleTextField(JTextField field) {
        if (field == null) {
            return;
        }

        field.setFont(new Font("Tahoma", Font.PLAIN, 13));
        field.setBackground(Color.WHITE);
        field.setForeground(new Color(35, 48, 68));
        field.setCaretColor(new Color(18, 48, 174));
        field.setMargin(new Insets(1, 8, 1, 8));

        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 214, 235), 1, true),
                new EmptyBorder(2, 8, 2, 8)
        ));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(89, 92, 255), 2, true),
                        new EmptyBorder(1, 7, 1, 7)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(200, 214, 235), 1, true),
                        new EmptyBorder(2, 8, 2, 8)
                ));
            }
        });
    }

    private void styleButton(javax.swing.JButton button, java.awt.Color baseColor) {
        if (button == null) {
            return;
        }

        java.awt.Color hoverColor = new java.awt.Color(
                Math.min(baseColor.getRed() + 15, 255),
                Math.min(baseColor.getGreen() + 15, 255),
                Math.min(baseColor.getBlue() + 15, 255)
        );

        java.awt.Color pressColor = new java.awt.Color(
                Math.max(baseColor.getRed() - 15, 0),
                Math.max(baseColor.getGreen() - 15, 0),
                Math.max(baseColor.getBlue() - 15, 0)
        );

        java.awt.Color disabledBg = new java.awt.Color(200, 210, 225);
        java.awt.Color disabledFg = new java.awt.Color(120, 130, 150);

        button.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 13));
        button.setForeground(java.awt.Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorder(new javax.swing.border.EmptyBorder(6, 14, 6, 14));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        button.putClientProperty("btnBaseColor", baseColor);
        button.putClientProperty("btnHoverColor", hoverColor);
        button.putClientProperty("btnPressColor", pressColor);
        button.putClientProperty("btnDisabledBg", disabledBg);
        button.putClientProperty("btnDisabledFg", disabledFg);
        button.putClientProperty("btnColor", baseColor);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!button.isEnabled()) {
                    return;
                }
                button.putClientProperty("btnColor", hoverColor);
                button.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!button.isEnabled()) {
                    return;
                }
                button.putClientProperty("btnColor", baseColor);
                button.repaint();
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (!button.isEnabled()) {
                    return;
                }
                button.putClientProperty("btnColor", pressColor);
                button.repaint();
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (!button.isEnabled()) {
                    return;
                }
                button.putClientProperty("btnColor", hoverColor);
                button.repaint();
            }
        });

        button.addPropertyChangeListener("enabled", evt -> {
            if (button.isEnabled()) {
                button.putClientProperty("btnColor", baseColor);
                button.setForeground(java.awt.Color.WHITE);
                button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            } else {
                button.putClientProperty("btnColor", disabledBg);
                button.setForeground(disabledFg);
                button.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
            button.repaint();
        });

        if (!button.isEnabled()) {
            button.putClientProperty("btnColor", disabledBg);
            button.setForeground(disabledFg);
            button.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }
    }

    private void installRoundedButtonPainter(javax.swing.JButton button) {
        if (button == null) {
            return;
        }

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(java.awt.Graphics g, javax.swing.JComponent c) {
                javax.swing.JButton b = (javax.swing.JButton) c;
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

                java.awt.Color bg;
                java.awt.Color fg;

                if (!b.isEnabled()) {
                    bg = (java.awt.Color) b.getClientProperty("btnDisabledBg");
                    fg = (java.awt.Color) b.getClientProperty("btnDisabledFg");
                    if (bg == null) {
                        bg = new java.awt.Color(200, 210, 225);
                    }
                    if (fg == null) {
                        fg = new java.awt.Color(120, 130, 150);
                    }
                } else {
                    bg = (java.awt.Color) b.getClientProperty("btnColor");
                    fg = b.getForeground();
                    if (bg == null) {
                        bg = b.getBackground();
                    }
                }

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 16, 16);

                g2.setColor(new java.awt.Color(255, 255, 255, 60));
                g2.drawRoundRect(0, 0, b.getWidth() - 1, b.getHeight() - 1, 16, 16);

                g2.setFont(b.getFont());
                java.awt.FontMetrics fm = g2.getFontMetrics();
                int x = (b.getWidth() - fm.stringWidth(b.getText())) / 2;
                int y = ((b.getHeight() - fm.getHeight()) / 2) + fm.getAscent();

                g2.setColor(fg);
                g2.drawString(b.getText(), x, y);
                g2.dispose();
            }
        });
    }

    private void loadSuppliers() {
        DefaultTableModel model = (DefaultTableModel) tblSuppliersList.getModel();
        model.setRowCount(0);

        String sql = "SELECT supplier_id, supplier_name, contact_person, contact_number, email_address, address "
                + "FROM suppliers ORDER BY supplier_id ASC";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("supplier_id"),
                    rs.getString("supplier_name"),
                    rs.getString("contact_person"),
                    rs.getString("contact_number"),
                    rs.getString("email_address"),
                    rs.getString("address")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Load suppliers failed: " + e.getMessage());
        }
    }

    private void enableRowSelection() {
        tblSuppliersList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = tblSuppliersList.getSelectedRow();
                if (row < 0) {
                    return;
                }

                selectedSupplierId = Integer.parseInt(tblSuppliersList.getValueAt(row, 0).toString());

                btnEdit.setEnabled(true);
                btnDelete.setEnabled(true);
                btnUpdate.setEnabled(false);
            }
        });
    }

    private void clearForm() {
        txtSupplierName.setText("");
        txtContactPerson.setText("");
        txtContactNumber.setText("");
        txtEmailAddress.setText("");
        txtAddress.setText("");
        txtSearch.setText("");

        selectedSupplierId = -1;

        btnUpdate.setEnabled(false);
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);

        tblSuppliersList.clearSelection();
        txtSupplierName.requestFocus();
    }

    private boolean validateForm() {
        String name = normalizeSpaces(txtSupplierName.getText());
        String person = normalizeSpaces(txtContactPerson.getText());
        String number = txtContactNumber.getText().trim();
        String email = txtEmailAddress.getText().trim();
        String address = normalizeSpaces(txtAddress.getText());

        txtSupplierName.setText(name);
        txtContactPerson.setText(person);
        txtContactNumber.setText(number);
        txtEmailAddress.setText(email);
        txtAddress.setText(address);

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Supplier name is required.");
            txtSupplierName.requestFocus();
            return false;
        }

        if (!containsLetter(name)) {
            JOptionPane.showMessageDialog(this, "Supplier name must contain at least one letter.");
            txtSupplierName.requestFocus();
            return false;
        }

        if (person.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Contact person is required.");
            txtContactPerson.requestFocus();
            return false;
        }

        if (!containsLetter(person)) {
            JOptionPane.showMessageDialog(this, "Contact person must contain letters only.");
            txtContactPerson.requestFocus();
            return false;
        }

        if (number.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Contact number is required.");
            txtContactNumber.requestFocus();
            return false;
        }

        if (!isValidPHContactNumber(number)) {
            JOptionPane.showMessageDialog(this, "Contact number must be a valid PH number (e.g. 09123456789 or +639123456789).");
            txtContactNumber.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email address is required.");
            txtEmailAddress.requestFocus();
            return false;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Invalid email address format.");
            txtEmailAddress.requestFocus();
            return false;
        }

        if (address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Address is required.");
            txtAddress.requestFocus();
            return false;
        }

        return true;
    }

    private void initializeSearchListener() {
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchSuppliers(txtSearch.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchSuppliers(txtSearch.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchSuppliers(txtSearch.getText());
            }
        });
    }

    private void searchSuppliers(String keyword) {
        DefaultTableModel model = (DefaultTableModel) tblSuppliersList.getModel();
        model.setRowCount(0);

        if (keyword == null) {
            keyword = "";
        }
        keyword = keyword.trim();

        if (keyword.isEmpty()) {
            loadSuppliers();
            return;
        }

        String sql = "SELECT supplier_id, supplier_name, contact_person, contact_number, email_address, address "
                + "FROM suppliers WHERE "
                + "supplier_name LIKE ? OR contact_person LIKE ? OR contact_number LIKE ? OR "
                + "email_address LIKE ? OR address LIKE ? "
                + "ORDER BY supplier_id ASC";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= 5; i++) {
                ps.setString(i, "%" + keyword + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("supplier_id"),
                        rs.getString("supplier_name"),
                        rs.getString("contact_person"),
                        rs.getString("contact_number"),
                        rs.getString("email_address"),
                        rs.getString("address")
                    });
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Search failed: " + e.getMessage());
        }
    }

    private boolean isDuplicateSupplier(String name, String number, String email, int excludeId) {
        String sql = "SELECT supplier_id FROM suppliers WHERE "
                + "(supplier_name=? OR contact_number=? OR email_address=?)";

        if (excludeId != -1) {
            sql += " AND supplier_id != ?";
        }

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, number);
            ps.setString(3, email);

            if (excludeId != -1) {
                ps.setInt(4, excludeId);
            }

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Validation error: " + e.getMessage());
        }

        return false;
    }

    private void refreshSupplierTable() {
        txtSearch.setText("");
        loadSuppliers();
        tblSuppliersList.clearSelection();
        clearForm();
    }

    private static class ShadowPanel extends JPanel {

        public ShadowPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int shadowGap = 8;
            int arc = 28;
            int width = getWidth() - shadowGap * 2;
            int height = getHeight() - shadowGap * 2;

            for (int i = 0; i < 8; i++) {
                g2.setColor(new Color(110, 140, 170, Math.max(3, 18 - i)));
                g2.fillRoundRect(shadowGap - i / 2, shadowGap - i / 2, width + i, height + i, arc, arc);
            }

            g2.setColor(new Color(248, 252, 255));
            g2.fillRoundRect(shadowGap, shadowGap, width, height, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class ModernScrollBar extends JScrollBar {

        public ModernScrollBar() {
            setUI(new BasicScrollBarUI() {
                @Override
                protected void configureScrollBarColors() {
                    thumbColor = new Color(170, 192, 216);
                    trackColor = new Color(236, 243, 249);
                }

                @Override
                protected JButton createDecreaseButton(int orientation) {
                    return createZeroButton();
                }

                @Override
                protected JButton createIncreaseButton(int orientation) {
                    return createZeroButton();
                }

                private JButton createZeroButton() {
                    JButton button = new JButton();
                    button.setPreferredSize(new Dimension(0, 0));
                    button.setMinimumSize(new Dimension(0, 0));
                    button.setMaximumSize(new Dimension(0, 0));
                    return button;
                }

                @Override
                protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(170, 192, 216));
                    g2.fillRoundRect(r.x + 3, r.y + 3, r.width - 6, r.height - 6, 12, 12);
                    g2.dispose();
                }

                @Override
                protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(236, 243, 249));
                    g2.fillRect(r.x, r.y, r.width, r.height);
                    g2.dispose();
                }
            });

            setPreferredSize(new Dimension(10, 10));
            setOpaque(false);
        }
    }

    public void enhanceTable(JTable table, JScrollPane scrollPane) {
        Font bodyFont = new Font("Tahoma", Font.PLAIN, 14);
        Font headerFont = new Font("Tahoma", Font.BOLD, 14);
        Font pillFont = new Font("Tahoma", Font.BOLD, 12);

        Color panelBg = new Color(241, 247, 253);
        Color cardBg = new Color(250, 253, 255);
        Color tableBg = new Color(255, 255, 255);
        Color stripeBg = new Color(246, 250, 254);
        Color headerBg = new Color(28, 74, 122);
        Color headerFg = Color.WHITE;
        Color textColor = new Color(32, 48, 70);
        Color mutedGrid = new Color(225, 234, 244);
        Color selectBg = new Color(214, 231, 250);
        Color selectFg = new Color(20, 43, 67);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.setBackground(panelBg);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(cardBg);
        scrollPane.setViewportBorder(null);

        table.setFont(bodyFont);
        table.setRowHeight(40);
        table.setShowGrid(true);
        table.setGridColor(mutedGrid);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFocusable(false);
        table.setBackground(tableBg);
        table.setForeground(textColor);
        table.setSelectionBackground(selectBg);
        table.setSelectionForeground(selectFg);
        table.setOpaque(true);
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(headerFont);
        header.setPreferredSize(new Dimension(header.getWidth(), 44));
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setOpaque(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                JLabel label = new JLabel(value == null ? "" : value.toString());
                label.setOpaque(true);
                label.setBackground(headerBg);
                label.setForeground(headerFg);
                label.setFont(headerFont);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(new EmptyBorder(11, 12, 11, 12));
                return label;
            }
        });

        DefaultTableCellRenderer luxuryRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                String text = value == null ? "" : value.toString();
                String columnName = table.getColumnName(column).toLowerCase();

                label.setOpaque(true);
                label.setFont(bodyFont);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(new EmptyBorder(8, 14, 8, 14));
                label.setText(text);

                if (isSelected) {
                    label.setBackground(selectBg);
                    label.setForeground(selectFg);
                    return label;
                }

                label.setBackground(row % 2 == 0 ? tableBg : stripeBg);
                label.setForeground(textColor);

                if (columnName.contains("role")) {
                    label.setFont(pillFont);
                    label.setText("  " + text + "  ");
                    label.setBackground(new Color(236, 243, 250));
                    label.setForeground(new Color(59, 90, 119));
                }

                if (columnName.contains("status")) {
                    label.setFont(pillFont);
                    label.setText("  " + text + "  ");
                    label.setBackground(new Color(234, 240, 247));
                    label.setForeground(new Color(78, 95, 116));
                }

                return label;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(luxuryRenderer);
        }

        if (table.getColumnCount() >= 6) {
            table.getColumnModel().getColumn(0).setPreferredWidth(70);
            table.getColumnModel().getColumn(1).setPreferredWidth(180);
            table.getColumnModel().getColumn(2).setPreferredWidth(160);
            table.getColumnModel().getColumn(3).setPreferredWidth(140);
            table.getColumnModel().getColumn(4).setPreferredWidth(220);
            table.getColumnModel().getColumn(5).setPreferredWidth(300);
        }

        scrollPane.setVerticalScrollBar(new ModernScrollBar());
        scrollPane.setHorizontalScrollBar(new ModernScrollBar());

        wrapScrollPaneInCard(scrollPane);

        TableRowSorter<DefaultTableModel> sorter
                = new TableRowSorter<>((DefaultTableModel) table.getModel());
        table.setRowSorter(sorter);

        if (table.getColumnCount() > 0) {
            sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
            sorter.sort();
        }
    }

    private void wrapScrollPaneInCard(JScrollPane scrollPane) {
        Container parent = scrollPane.getParent();
        if (parent != null && parent instanceof JViewport) {
            parent = parent.getParent();
        }

        if (parent != null) {
            parent.setLayout(new BorderLayout());
            parent.remove(scrollPane);

            JPanel roundedWrapper = new ShadowPanel();
            roundedWrapper.setLayout(new BorderLayout());
            roundedWrapper.setBorder(new EmptyBorder(14, 14, 14, 14));
            roundedWrapper.add(scrollPane, BorderLayout.CENTER);

            parent.add(roundedWrapper, BorderLayout.CENTER);
            parent.revalidate();
            parent.repaint();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel6 = new RoundedPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtSupplierName = new javax.swing.JTextField();
        btnAdd = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtContactPerson = new javax.swing.JTextField();
        txtContactNumber = new javax.swing.JTextField();
        txtEmailAddress = new javax.swing.JTextField();
        txtAddress = new javax.swing.JTextField();
        btnUpdate = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        lblPersonalDetails = new javax.swing.JLabel();
        jPanel3 = new RoundedPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblSuppliersList = new javax.swing.JTable();
        jPanel7 = new RoundedPanel();
        lblAddNewSupplier = new javax.swing.JLabel("Add New User") {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();

                g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                float[] fractions = {0f, 1f};
                java.awt.Color[] colors = {
                    new java.awt.Color(135, 206, 250), // light blue
                    java.awt.Color.WHITE
                };

                java.awt.LinearGradientPaint lgp = new java.awt.LinearGradientPaint(
                    0, 0, getWidth(), 0,
                    fractions, colors
                );

                g2.setPaint(lgp);

                java.awt.FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();

                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };
        jLabel2 = new point.of.sale.system.classes.GradientFont();
        jLabel10 = new javax.swing.JLabel();
        jPanel4 = new RoundedPanel();
        jLabel1 = new javax.swing.JLabel("Add New User") {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();

                g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                float[] fractions = {0f, 1f};
                java.awt.Color[] colors = {
                    new java.awt.Color(135, 206, 250), // light blue
                    java.awt.Color.WHITE
                };

                java.awt.LinearGradientPaint lgp = new java.awt.LinearGradientPaint(
                    0, 0, getWidth(), 0,
                    fractions, colors
                );

                g2.setPaint(lgp);

                java.awt.FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();

                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };
        btnDelete = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator1 = new javax.swing.JSeparator();

        setBackground(new java.awt.Color(240, 248, 255));
        setPreferredSize(new java.awt.Dimension(1120, 820));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel6.setBackground(new java.awt.Color(122, 170, 206));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(40, 55, 80));
        jLabel3.setText("Supplier Name:");
        jPanel6.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 70, -1, -1));

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(40, 55, 80));
        jLabel4.setText("Contact Person:");
        jPanel6.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 70, -1, -1));

        txtSupplierName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jPanel6.add(txtSupplierName, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 90, 230, 40));

        btnAdd.setBackground(new java.awt.Color(0, 166, 37));
        btnAdd.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnAdd.setForeground(new java.awt.Color(255, 255, 255));
        btnAdd.setText("Add Supplier");
        btnAdd.addActionListener(this::btnAddActionPerformed);
        jPanel6.add(btnAdd, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 150, 170, 30));

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(40, 55, 80));
        jLabel6.setText("Contact Number:");
        jPanel6.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 70, -1, -1));

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(40, 55, 80));
        jLabel7.setText("Email Address:");
        jPanel6.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 160, -1, -1));

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(40, 55, 80));
        jLabel8.setText("Address:");
        jPanel6.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 160, -1, -1));
        jPanel6.add(txtContactPerson, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 90, 230, 40));
        jPanel6.add(txtContactNumber, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 90, 230, 40));
        jPanel6.add(txtEmailAddress, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 180, 230, 40));
        jPanel6.add(txtAddress, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 180, 230, 40));

        btnUpdate.setBackground(new java.awt.Color(0, 98, 193));
        btnUpdate.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnUpdate.setForeground(new java.awt.Color(255, 255, 255));
        btnUpdate.setText("Update Supplier");
        btnUpdate.addActionListener(this::btnUpdateActionPerformed);
        jPanel6.add(btnUpdate, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 110, 170, 30));

        btnRefresh.setBackground(new java.awt.Color(44, 62, 80));
        btnRefresh.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnRefresh.setForeground(new java.awt.Color(255, 255, 255));
        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);
        jPanel6.add(btnRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 190, -1, 30));

        lblPersonalDetails.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblPersonalDetails.setForeground(new java.awt.Color(10, 25, 47));
        lblPersonalDetails.setText("Personal Details");
        jPanel6.add(lblPersonalDetails, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 200, -1));

        add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 140, 1100, 270));

        jPanel3.setBackground(new java.awt.Color(122, 170, 206));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblSuppliersList.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        tblSuppliersList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Supplier Name", "Contact Person", "Contact Number", "Email Address", "Address"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblSuppliersList.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(tblSuppliersList);
        if (tblSuppliersList.getColumnModel().getColumnCount() > 0) {
            tblSuppliersList.getColumnModel().getColumn(0).setResizable(false);
            tblSuppliersList.getColumnModel().getColumn(0).setPreferredWidth(20);
            tblSuppliersList.getColumnModel().getColumn(1).setResizable(false);
            tblSuppliersList.getColumnModel().getColumn(1).setPreferredWidth(200);
            tblSuppliersList.getColumnModel().getColumn(2).setResizable(false);
            tblSuppliersList.getColumnModel().getColumn(2).setPreferredWidth(200);
            tblSuppliersList.getColumnModel().getColumn(3).setResizable(false);
            tblSuppliersList.getColumnModel().getColumn(3).setPreferredWidth(200);
            tblSuppliersList.getColumnModel().getColumn(4).setResizable(false);
            tblSuppliersList.getColumnModel().getColumn(4).setPreferredWidth(200);
            tblSuppliersList.getColumnModel().getColumn(5).setResizable(false);
            tblSuppliersList.getColumnModel().getColumn(5).setPreferredWidth(180);
        }

        jPanel3.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 1080, 320));

        add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 470, 1100, 340));

        jPanel7.setBackground(new java.awt.Color(18, 48, 174));
        jPanel7.setMinimumSize(new java.awt.Dimension(206, 630));
        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblAddNewSupplier.setFont(new java.awt.Font("Tahoma", 1, 22)); // NOI18N
        lblAddNewSupplier.setText("Add New Supplier");
        jPanel7.add(lblAddNewSupplier, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, -1, -1));

        add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, 340, 50));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel2.setText("SUPPLIERS MANAGEMENT");
        add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 490, -1));

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel10.setText("Manage your suppliers and contacts");
        add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, -1, -1));

        jPanel4.setBackground(new java.awt.Color(18, 48, 174));
        jPanel4.setMinimumSize(new java.awt.Dimension(206, 630));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 22)); // NOI18N
        jLabel1.setText("SUPPLIERS LIST");
        jPanel4.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 190, -1));

        btnDelete.setBackground(new java.awt.Color(204, 0, 0));
        btnDelete.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnDelete.setForeground(new java.awt.Color(255, 255, 255));
        btnDelete.setText("Delete ");
        btnDelete.addActionListener(this::btnDeleteActionPerformed);
        jPanel4.add(btnDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(970, 14, 100, 30));

        btnEdit.setBackground(new java.awt.Color(243, 156, 18));
        btnEdit.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnEdit.setForeground(new java.awt.Color(255, 255, 255));
        btnEdit.setText("Edit Details");
        btnEdit.addActionListener(this::btnEditActionPerformed);
        jPanel4.add(btnEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 14, -1, 30));

        txtSearch.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        txtSearch.setMinimumSize(new java.awt.Dimension(64, 22));
        txtSearch.addActionListener(this::txtSearchActionPerformed);
        jPanel4.add(txtSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 16, 200, 25));

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Search:");
        jPanel4.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 20, -1, -1));

        add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 410, 1100, 60));

        jSeparator3.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator3.setForeground(new java.awt.Color(0, 0, 0));
        add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 30, 590, 10));

        jSeparator1.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator1.setForeground(new java.awt.Color(0, 0, 0));
        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 40, 20, 90));
    }// </editor-fold>//GEN-END:initComponents

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        searchSuppliers(txtSearch.getText());
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        refreshSupplierTable();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private boolean isSupplierUsedInProducts(int supplierId) {
        String sql = "SELECT 1 FROM products WHERE supplier_id = ? LIMIT 1";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, supplierId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Validation error: " + e.getMessage());
            return true;
        }
    }

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        if (selectedSupplierId == -1) {
            JOptionPane.showMessageDialog(this, "Select a supplier first");
            return;
        }

        if (isSupplierUsedInProducts(selectedSupplierId)) {
            JOptionPane.showMessageDialog(this, "This supplier cannot be deleted because it is already used by one or more products.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete this supplier?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM suppliers WHERE supplier_id=?";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, selectedSupplierId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Supplier deleted");

            clearForm();
            refreshSupplierTable();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Delete failed: " + e.getMessage());
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed

        if (selectedSupplierId == -1) {
            JOptionPane.showMessageDialog(this, "Select a supplier first");
            return;
        }

        int row = tblSuppliersList.getSelectedRow();
        if (row < 0) {
            return;
        }

        txtSupplierName.setText(String.valueOf(tblSuppliersList.getValueAt(row, 1)));
        txtContactPerson.setText(String.valueOf(tblSuppliersList.getValueAt(row, 2)));
        txtContactNumber.setText(String.valueOf(tblSuppliersList.getValueAt(row, 3)));
        txtEmailAddress.setText(String.valueOf(tblSuppliersList.getValueAt(row, 4)));
        txtAddress.setText(String.valueOf(tblSuppliersList.getValueAt(row, 5)));

        btnUpdate.setEnabled(true);
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        if (!validateForm()) {
            return;
        }

        String name = normalizeSpaces(txtSupplierName.getText());
        String person = normalizeSpaces(txtContactPerson.getText());
        String number = txtContactNumber.getText().trim();
        String email = txtEmailAddress.getText().trim();
        String address = normalizeSpaces(txtAddress.getText());

        if (isDuplicateSupplier(name, number, email, -1)) {
            JOptionPane.showMessageDialog(this, "Duplicate supplier detected!");
            txtSupplierName.requestFocus();
            return;
        }

        String sql = "INSERT INTO suppliers(supplier_name, contact_person, contact_number, email_address, address) "
                + "VALUES(?,?,?,?,?)";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, person);
            ps.setString(3, number);
            ps.setString(4, email);
            ps.setString(5, address);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Supplier added successfully");

            clearForm();
            refreshSupplierTable();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Add failed: " + e.getMessage());
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        if (selectedSupplierId == -1) {
            JOptionPane.showMessageDialog(this, "Select a supplier first");
            return;
        }

        if (!validateForm()) {
            return;
        }

        String name = normalizeSpaces(txtSupplierName.getText());
        String person = normalizeSpaces(txtContactPerson.getText());
        String number = txtContactNumber.getText().trim();
        String email = txtEmailAddress.getText().trim();
        String address = normalizeSpaces(txtAddress.getText());

        if (isDuplicateSupplier(name, number, email, selectedSupplierId)) {
            JOptionPane.showMessageDialog(this, "Duplicate supplier detected!");
            txtSupplierName.requestFocus();
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Update this supplier?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "UPDATE suppliers SET supplier_name=?, contact_person=?, contact_number=?, "
                + "email_address=?, address=? WHERE supplier_id=?";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, person);
            ps.setString(3, number);
            ps.setString(4, email);
            ps.setString(5, address);
            ps.setInt(6, selectedSupplierId);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Supplier updated successfully");
            } else {
                JOptionPane.showMessageDialog(this, "Update failed");
            }

            clearForm();
            refreshSupplierTable();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Update failed: " + e.getMessage());
        }
    }//GEN-LAST:event_btnUpdateActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel lblAddNewSupplier;
    private javax.swing.JLabel lblPersonalDetails;
    private javax.swing.JTable tblSuppliersList;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JTextField txtContactNumber;
    private javax.swing.JTextField txtContactPerson;
    private javax.swing.JTextField txtEmailAddress;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtSupplierName;
    // End of variables declaration//GEN-END:variables
}
