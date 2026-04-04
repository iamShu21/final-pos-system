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
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import point.of.sale.system.classes.DBConnection;
import point.of.sale.system.classes.RoundedPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;

public class CategoryManagementPanel extends javax.swing.JPanel {

    private static final java.util.logging.Logger logger
            = java.util.logging.Logger.getLogger(CategoryManagementPanel.class.getName());

    private int selectedCategoryId = -1;

    public CategoryManagementPanel() {
        initComponents();

        applyModuleTheme();
        installValidationFilters();
        enhanceTable(tblCategory, jScrollPane2);

        btnUpdate.setEnabled(false);
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);

        loadCategories();
        enableRowSelection();
        initializeSearchListener();

    }

    private void applyModuleTheme() {
        setBackground(new java.awt.Color(240, 246, 255));

        // same section colors as ProductsManagementPanel
        pnlAddNewCategory.setBackground(new java.awt.Color(132, 164, 224));
        pnlCategoryList.setBackground(new java.awt.Color(132, 164, 224));
        jPanel7.setBackground(new java.awt.Color(18, 48, 174));
        jPanel4.setBackground(new java.awt.Color(18, 48, 174));

        if (jLabel6 != null) {
            jLabel6.setForeground(new Color(88, 105, 136));
        }

        styleTextField(txtCategoryName);
        styleTextArea(txtDescription);
        styleTextField(txtSearch);

        styleButton(btnAdd, new java.awt.Color(34, 166, 82));
        styleButton(btnUpdate, new java.awt.Color(52, 120, 246));
        styleButton(btnDelete, new java.awt.Color(220, 53, 69));
        styleButton(btnEdit, new java.awt.Color(243, 156, 18));
        styleButton(btnRefreshAll, new java.awt.Color(89, 92, 255));

        installRoundedButtonPainter(btnAdd);
        installRoundedButtonPainter(btnUpdate);
        installRoundedButtonPainter(btnDelete);
        installRoundedButtonPainter(btnEdit);
        installRoundedButtonPainter(btnRefreshAll);
    }

    private void installValidationFilters() {
        txtCategoryName.addKeyListener(new KeyAdapter() {
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
                        || c == '\'';

                if (!allowed || txtCategoryName.getText().length() >= 100) {
                    e.consume();
                }
            }
        });

        txtDescription.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (txtDescription.getText().length() >= 255) {
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

    private boolean isCategoryNameDuplicate(String categoryName, int excludeId) {
        String sql = "SELECT category_id FROM categories WHERE LOWER(TRIM(name)) = LOWER(TRIM(?))";
        if (excludeId != -1) {
            sql += " AND category_id != ?";
        }

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, categoryName);

            if (excludeId != -1) {
                ps.setInt(2, excludeId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Validation error: " + e.getMessage());
            return true;
        }
    }

    private boolean validateForm() {
        String categoryName = normalizeSpaces(txtCategoryName.getText());
        String description = normalizeSpaces(txtDescription.getText());

        txtCategoryName.setText(categoryName);
        txtDescription.setText(description);

        if (categoryName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Category name is required.");
            txtCategoryName.requestFocus();
            return false;
        }

        if (!containsLetter(categoryName)) {
            JOptionPane.showMessageDialog(this, "Category name must contain at least one letter.");
            txtCategoryName.requestFocus();
            return false;
        }

        if (categoryName.length() > 100) {
            JOptionPane.showMessageDialog(this, "Category name must not exceed 100 characters.");
            txtCategoryName.requestFocus();
            return false;
        }

        if (!description.isEmpty() && description.length() > 255) {
            JOptionPane.showMessageDialog(this, "Description must not exceed 255 characters.");
            txtDescription.requestFocus();
            return false;
        }

        return true;
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

    private void styleTextArea(javax.swing.JTextArea area) {
        if (area == null) {
            return;
        }

        area.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 13));
        area.setBackground(java.awt.Color.WHITE);
        area.setForeground(new java.awt.Color(35, 48, 68));
        area.setCaretColor(new java.awt.Color(18, 48, 174));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setMargin(new java.awt.Insets(6, 8, 6, 8));

        area.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new java.awt.Color(200, 214, 235), 1, true),
                new javax.swing.border.EmptyBorder(2, 4, 2, 4)
        ));

        area.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                area.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        new javax.swing.border.LineBorder(new java.awt.Color(89, 92, 255), 2, true),
                        new javax.swing.border.EmptyBorder(1, 3, 1, 3)
                ));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                area.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        new javax.swing.border.LineBorder(new java.awt.Color(200, 214, 235), 1, true),
                        new javax.swing.border.EmptyBorder(2, 4, 2, 4)
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

        if (table.getColumnCount() >= 3) {
            table.getColumnModel().getColumn(0).setPreferredWidth(70);
            table.getColumnModel().getColumn(1).setPreferredWidth(260);
            table.getColumnModel().getColumn(2).setPreferredWidth(650);
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

    private void loadCategories() {
        DefaultTableModel model = (DefaultTableModel) tblCategory.getModel();
        model.setRowCount(0);

        String sql = "SELECT category_id, name, description FROM categories ORDER BY category_id ASC";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("category_id"),
                    rs.getString("name"),
                    rs.getString("description")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Load failed: " + e.getMessage());
        }
    }

    private void enableRowSelection() {
        tblCategory.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = tblCategory.getSelectedRow();
                if (row < 0) {
                    return;
                }

                selectedCategoryId = Integer.parseInt(tblCategory.getValueAt(row, 0).toString());
                btnEdit.setEnabled(true);
                btnDelete.setEnabled(true);
                btnUpdate.setEnabled(false);
            }
        });
    }

    private void clearForm() {
        txtCategoryName.setText("");
        txtDescription.setText("");
        txtSearch.setText("");

        selectedCategoryId = -1;

        btnUpdate.setEnabled(false);
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);

        tblCategory.clearSelection();
        txtCategoryName.requestFocus();
    }

    private void searchCategories(String keyword) {
        DefaultTableModel model = (DefaultTableModel) tblCategory.getModel();
        model.setRowCount(0);

        if (keyword == null) {
            keyword = "";
        }
        keyword = keyword.trim();

        if (keyword.isEmpty()) {
            loadCategories();
            return;
        }

        String sql = "SELECT category_id, name, description FROM categories "
                + "WHERE name LIKE ? OR description LIKE ? "
                + "ORDER BY category_id ASC";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("category_id"),
                        rs.getString("name"),
                        rs.getString("description")
                    });
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Search failed: " + e.getMessage());
        }
    }

    private void initializeSearchListener() {
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchCategories(txtSearch.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchCategories(txtSearch.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchCategories(txtSearch.getText());
            }
        });
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlCategoryList = new RoundedPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblCategory = new javax.swing.JTable();
        pnlAddNewCategory = new RoundedPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtDescription = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        txtCategoryName = new javax.swing.JTextField();
        btnAdd = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnRefreshAll = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel2 = new point.of.sale.system.classes.GradientFont();
        jPanel7 = new RoundedPanel();
        lblAddNewUser = new javax.swing.JLabel("Add New User") {     @Override     protected void paintComponent(java.awt.Graphics g) {         java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();          g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,                             java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);          float[] fractions = {0.5f, 1f};         java.awt.Color[] colors = {             java.awt.Color.WHITE,             java.awt.Color.BLUE         };          java.awt.LinearGradientPaint lgp = new java.awt.LinearGradientPaint(                 0, 0, getWidth(), 0,                 fractions, colors         );          g2.setPaint(lgp);          java.awt.FontMetrics fm = g2.getFontMetrics();         int x = (getWidth() - fm.stringWidth(getText())) / 2;         int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();          g2.drawString(getText(), x, y);          g2.dispose();     } };
        jPanel4 = new RoundedPanel();
        jLabel1 = new javax.swing.JLabel("Add New User") {     @Override     protected void paintComponent(java.awt.Graphics g) {         java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();          g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,                             java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);          float[] fractions = {0.5f, 1f};         java.awt.Color[] colors = {             java.awt.Color.WHITE,             java.awt.Color.BLUE         };          java.awt.LinearGradientPaint lgp = new java.awt.LinearGradientPaint(                 0, 0, getWidth(), 0,                 fractions, colors         );          g2.setPaint(lgp);          java.awt.FontMetrics fm = g2.getFontMetrics();         int x = (getWidth() - fm.stringWidth(getText())) / 2;         int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();          g2.drawString(getText(), x, y);          g2.dispose();     } };
        btnEdit = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator1 = new javax.swing.JSeparator();

        setBackground(new java.awt.Color(240, 248, 255));
        setPreferredSize(new java.awt.Dimension(1120, 820));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlCategoryList.setBackground(new java.awt.Color(122, 170, 206));
        pnlCategoryList.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblCategory.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        tblCategory.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Category Name", "Description"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(tblCategory);
        if (tblCategory.getColumnModel().getColumnCount() > 0) {
            tblCategory.getColumnModel().getColumn(0).setResizable(false);
            tblCategory.getColumnModel().getColumn(0).setPreferredWidth(10);
            tblCategory.getColumnModel().getColumn(1).setResizable(false);
            tblCategory.getColumnModel().getColumn(1).setPreferredWidth(300);
            tblCategory.getColumnModel().getColumn(2).setResizable(false);
            tblCategory.getColumnModel().getColumn(2).setPreferredWidth(400);
        }

        pnlCategoryList.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 1080, 310));

        add(pnlCategoryList, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 470, 1100, 330));

        pnlAddNewCategory.setBackground(new java.awt.Color(122, 170, 206));
        pnlAddNewCategory.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel3.setText("Category Name: ");
        pnlAddNewCategory.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, -1));

        txtDescription.setColumns(20);
        txtDescription.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        txtDescription.setRows(5);
        jScrollPane1.setViewportView(txtDescription);

        pnlAddNewCategory.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 100, 560, 120));

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel4.setText("Description:");
        pnlAddNewCategory.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, -1, -1));

        txtCategoryName.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        pnlAddNewCategory.add(txtCategoryName, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 30, 260, 40));

        btnAdd.setBackground(new java.awt.Color(0, 166, 37));
        btnAdd.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnAdd.setForeground(new java.awt.Color(255, 255, 255));
        btnAdd.setText("Add Category");
        btnAdd.setFocusPainted(false);
        btnAdd.addActionListener(this::btnAddActionPerformed);
        pnlAddNewCategory.add(btnAdd, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 110, 160, 30));

        btnUpdate.setBackground(new java.awt.Color(0, 98, 193));
        btnUpdate.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnUpdate.setForeground(new java.awt.Color(255, 255, 255));
        btnUpdate.setText("Update Category");
        btnUpdate.setFocusPainted(false);
        btnUpdate.addActionListener(this::btnUpdateActionPerformed);
        pnlAddNewCategory.add(btnUpdate, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 150, 160, 30));

        btnRefreshAll.setBackground(new java.awt.Color(44, 62, 80));
        btnRefreshAll.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnRefreshAll.setForeground(new java.awt.Color(255, 255, 255));
        btnRefreshAll.setText("Refresh");
        btnRefreshAll.addActionListener(this::btnRefreshAllActionPerformed);
        pnlAddNewCategory.add(btnRefreshAll, new org.netbeans.lib.awtextra.AbsoluteConstraints(900, 190, -1, 30));

        add(pnlAddNewCategory, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 140, 1100, 270));

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel6.setText("Manage product categories");
        add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, -1, -1));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel2.setText("CATEGORIES MANAGEMENT");
        add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 520, -1));

        jPanel7.setBackground(new java.awt.Color(18, 48, 174));
        jPanel7.setMinimumSize(new java.awt.Dimension(206, 630));
        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblAddNewUser.setFont(new java.awt.Font("Tahoma", 1, 22)); // NOI18N
        lblAddNewUser.setText("Add New Category");
        jPanel7.add(lblAddNewUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, -1, -1));

        add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, 340, 50));

        jPanel4.setBackground(new java.awt.Color(18, 48, 174));
        jPanel4.setMinimumSize(new java.awt.Dimension(206, 630));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 22)); // NOI18N
        jLabel1.setText("CATEGORIES LIST");
        jPanel4.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 210, -1));

        btnEdit.setBackground(new java.awt.Color(243, 156, 18));
        btnEdit.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnEdit.setForeground(new java.awt.Color(255, 255, 255));
        btnEdit.setText("Edit Category");
        btnEdit.setFocusPainted(false);
        btnEdit.addActionListener(this::btnEditActionPerformed);
        jPanel4.add(btnEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 14, 140, 30));

        btnDelete.setBackground(new java.awt.Color(204, 0, 0));
        btnDelete.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnDelete.setForeground(new java.awt.Color(255, 255, 255));
        btnDelete.setText("Delete Category");
        btnDelete.setFocusPainted(false);
        btnDelete.addActionListener(this::btnDeleteActionPerformed);
        jPanel4.add(btnDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 14, -1, 30));

        txtSearch.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        txtSearch.setMinimumSize(new java.awt.Dimension(64, 22));
        txtSearch.setPreferredSize(new java.awt.Dimension(64, 27));
        txtSearch.addActionListener(this::txtSearchActionPerformed);
        jPanel4.add(txtSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 16, 200, 25));

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Search:");
        jPanel4.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 20, -1, 20));

        add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 410, 1100, 60));

        jSeparator3.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator3.setForeground(new java.awt.Color(0, 0, 0));
        add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 30, 560, 10));

        jSeparator1.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator1.setForeground(new java.awt.Color(0, 0, 0));
        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 40, 20, 90));
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        if (!validateForm()) {
            return;
        }

        String categoryName = normalizeSpaces(txtCategoryName.getText());
        String description = normalizeSpaces(txtDescription.getText());

        if (isCategoryNameDuplicate(categoryName, -1)) {
            JOptionPane.showMessageDialog(this, "Category name already exists.");
            txtCategoryName.requestFocus();
            return;
        }

        String sql = "INSERT INTO categories(name, description) VALUES(?, ?)";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, categoryName);
            ps.setString(2, description);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Category added");

            clearForm();
            loadCategories();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Add failed: " + e.getMessage());
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        if (selectedCategoryId == -1) {
            JOptionPane.showMessageDialog(this, "Select a category first");
            return;
        }

        if (!validateForm()) {
            return;
        }

        String categoryName = normalizeSpaces(txtCategoryName.getText());
        String description = normalizeSpaces(txtDescription.getText());

        if (isCategoryNameDuplicate(categoryName, selectedCategoryId)) {
            JOptionPane.showMessageDialog(this, "Category name already exists.");
            txtCategoryName.requestFocus();
            return;
        }

        String sql = "UPDATE categories SET name=?, description=? WHERE category_id=?";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, categoryName);
            ps.setString(2, description);
            ps.setInt(3, selectedCategoryId);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Category updated");

            clearForm();
            loadCategories();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Update failed: " + e.getMessage());
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        searchCategories(txtSearch.getText());
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        if (selectedCategoryId == -1) {
            JOptionPane.showMessageDialog(this, "Select a category first");
            return;
        }

        int row = tblCategory.getSelectedRow();
        if (row < 0) {
            return;
        }

        txtCategoryName.setText(tblCategory.getValueAt(row, 1).toString());
        txtDescription.setText(tblCategory.getValueAt(row, 2) == null ? "" : tblCategory.getValueAt(row, 2).toString());

        btnUpdate.setEnabled(true);
    }//GEN-LAST:event_btnEditActionPerformed

    private boolean isCategoryUsedInProducts(int categoryId) {
        String sql = "SELECT 1 FROM products WHERE category_id = ? LIMIT 1";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, categoryId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Validation error: " + e.getMessage());
            return true;
        }
    }

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        if (selectedCategoryId == -1) {
            JOptionPane.showMessageDialog(this, "Select a category first");
            return;
        }

        if (isCategoryUsedInProducts(selectedCategoryId)) {
            JOptionPane.showMessageDialog(this, "This category cannot be deleted because it is already used by one or more products.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete this category?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM categories WHERE category_id=?";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, selectedCategoryId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Category deleted");

            clearForm();
            loadCategories();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Delete failed: " + e.getMessage());
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnRefreshAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshAllActionPerformed
        clearForm();
        loadCategories();
    }//GEN-LAST:event_btnRefreshAllActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnRefreshAll;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel lblAddNewUser;
    private javax.swing.JPanel pnlAddNewCategory;
    private javax.swing.JPanel pnlCategoryList;
    private javax.swing.JTable tblCategory;
    private javax.swing.JTextField txtCategoryName;
    private javax.swing.JTextArea txtDescription;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
