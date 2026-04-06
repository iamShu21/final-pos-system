package point.of.sale.system.screens;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.property.UnitValue;
import java.beans.PropertyChangeEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import point.of.sale.system.classes.DBConnection;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.JTableHeader;
import point.of.sale.system.classes.GlassPanel;
import point.of.sale.system.classes.RoundedPanel;
import org.mindrot.jbcrypt.BCrypt;

public class SalesHistoryPanel extends javax.swing.JPanel {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SalesHistoryPanel.class.getName());

    private final List<SalesRecord> allSalesRecords = new ArrayList<SalesRecord>();
    private final List<SalesRecord> filteredSalesRecords = new ArrayList<SalesRecord>();
    private DefaultTableModel tableModel;
    private String currentLoggedInUsername = "";
    private String currentLoggedInUserRole = "";

    private final SimpleDateFormat tableDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
    private final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final DecimalFormat pesoFormat = new DecimalFormat("₱#,##0.00");
    private final java.text.DecimalFormat tablePesoFormat = new java.text.DecimalFormat("₱#,##0.00");

    public SalesHistoryPanel() {
        initComponents();
        fixTableContainer();
        applyLuxuryTheme();
        initializeTable();
        loadSalesDataFromDatabase();
        loadCashierList();
        setupFilters();
        setupTableContextMenu();
        applyFilters();

    }

    public void refreshSalesHistoryRealtime() {
        loadSalesDataFromDatabase();
        loadCashierList();
        applyFilters();

        tableModel.fireTableDataChanged();
        tblSalesTransaction.revalidate();
        tblSalesTransaction.repaint();
        tableScrollPane.revalidate();
        tableScrollPane.repaint();
    }

    public void resetAndRefreshSalesHistory() {
        searchInvoiceNumber.setText("");
        listOfCashiers.setSelectedIndex(0);
        dateFrom.setDate(null);
        dateTo.setDate(null);

        loadSalesDataFromDatabase();
        loadCashierList();
        applyFilters();

        tableModel.fireTableDataChanged();
        tblSalesTransaction.revalidate();
        tblSalesTransaction.repaint();
        tableScrollPane.revalidate();
        tableScrollPane.repaint();
    }

    private void fixTableContainer() {
        // NetBeans generated code wrapped one scrollpane inside another.
        // This makes sure the visible scrollpane directly shows the table.
        tableScrollPane.setViewportView(tblSalesTransaction);
    }

    private int hoveredSalesRow = -1;

    private void initializeTable() {
        tableModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Invoice Number", "Date", "Cashier", "Items", "Total Amount", "Payment Method"
                }
        ) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };

        tblSalesTransaction.setModel(tableModel);
        tblSalesTransaction.getTableHeader().setReorderingAllowed(false);
        tblSalesTransaction.setRowHeight(42);

        enhanceTable(tblSalesTransaction, tableScrollPane);
    }

    public void setLoggedInUserInfo(String username, String userRole) {
        this.currentLoggedInUsername = username == null ? "" : username;
        this.currentLoggedInUserRole = userRole == null ? "" : userRole;
    }

    private boolean isCurrentUserCashier() {
        return "Cashier".equalsIgnoreCase(currentLoggedInUserRole);
    }

    private String getApproverRoleForCurrentUser() {
        return currentLoggedInUserRole == null ? "" : currentLoggedInUserRole;
    }

    private void loadSalesDataFromDatabase() {
        allSalesRecords.clear();

        String sql
                = "SELECT "
                + "    s.sale_id, "
                + "    s.invoice_number, "
                + "    s.sale_date, "
                + "    TRIM(CONCAT( "
                + "        COALESCE(u.first_name, ''), ' ', "
                + "        COALESCE(NULLIF(u.middle_name, ''), ''), "
                + "        CASE "
                + "            WHEN COALESCE(NULLIF(u.middle_name, ''), '') = '' THEN '' "
                + "            ELSE ' ' "
                + "        END, "
                + "        COALESCE(u.last_name, '') "
                + "    )) AS cashier, "
                + "    COALESCE(SUM(sd.quantity), 0) AS total_items, "
                + "    COALESCE(s.total_amount, 0) AS total_amount, "
                + "    COALESCE(s.payment_method, '') AS payment_method, "
                + "    COALESCE(SUM(((COALESCE(sd.price, 0) - COALESCE(p.cost_price, 0)) * COALESCE(sd.quantity, 0)) - COALESCE(sd.discount, 0)), 0) AS total_profit "
                + "FROM sales s "
                + "INNER JOIN users u ON s.user_id = u.user_id "
                + "LEFT JOIN sales_details sd ON s.sale_id = sd.sale_id "
                + "LEFT JOIN products p ON sd.product_id = p.product_id "
                + "GROUP BY s.sale_id, s.invoice_number, s.sale_date, u.first_name, u.middle_name, u.last_name, s.total_amount, s.payment_method "
                + "ORDER BY s.sale_date DESC";

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.dbConnection();
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();

            while (rs.next()) {
                SalesRecord record = new SalesRecord();
                record.saleId = rs.getInt("sale_id");
                record.invoiceNumber = rs.getString("invoice_number");
                record.date = rs.getTimestamp("sale_date");
                record.cashier = rs.getString("cashier");
                record.items = rs.getInt("total_items");
                record.totalAmount = rs.getDouble("total_amount");
                record.paymentMethod = rs.getString("payment_method");
                record.profit = rs.getDouble("total_profit");

                allSalesRecords.add(record);
            }

        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error loading sales history", ex);
            JOptionPane.showMessageDialog(this, "Error loading sales history: " + ex.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
            }
            try {
                if (pst != null) {
                    pst.close();
                }
            } catch (Exception e) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
            }
        }
    }

    private void loadCashierList() {
        Object previousSelection = listOfCashiers.getSelectedItem();

        listOfCashiers.removeAllItems();
        listOfCashiers.addItem("All Cashiers");

        Set<String> cashierNames = new LinkedHashSet<String>();

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            String sql
                    = "SELECT DISTINCT "
                    + "TRIM(CONCAT( "
                    + "    COALESCE(u.first_name, ''), ' ', "
                    + "    COALESCE(NULLIF(u.middle_name, ''), ''), "
                    + "    CASE "
                    + "        WHEN COALESCE(NULLIF(u.middle_name, ''), '') = '' THEN '' "
                    + "        ELSE ' ' "
                    + "    END, "
                    + "    COALESCE(u.last_name, '') "
                    + ")) AS cashier "
                    + "FROM sales s "
                    + "INNER JOIN users u ON s.user_id = u.user_id "
                    + "ORDER BY cashier ASC";

            conn = DBConnection.dbConnection();
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();

            while (rs.next()) {
                String cashier = rs.getString("cashier");
                if (cashier != null && cashier.trim().length() > 0) {
                    cashierNames.add(cashier.trim());
                }
            }

        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.WARNING, "Could not load cashier list from database, using loaded records instead.", ex);

            for (int i = 0; i < allSalesRecords.size(); i++) {
                SalesRecord record = allSalesRecords.get(i);
                if (record.cashier != null && record.cashier.trim().length() > 0) {
                    cashierNames.add(record.cashier.trim());
                }
            }

        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
            }
            try {
                if (pst != null) {
                    pst.close();
                }
            } catch (Exception e) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
            }
        }

        for (String cashier : cashierNames) {
            listOfCashiers.addItem(cashier);
        }

        if (previousSelection != null) {
            boolean found = false;
            for (int i = 0; i < listOfCashiers.getItemCount(); i++) {
                if (previousSelection.toString().equalsIgnoreCase(listOfCashiers.getItemAt(i))) {
                    listOfCashiers.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }

            if (!found) {
                listOfCashiers.setSelectedIndex(0);
            }
        } else {
            listOfCashiers.setSelectedIndex(0);
        }
    }

    private void applyLuxuryTheme() {
        setBackground(new Color(240, 246, 255));

        if (jPanel1 != null) {
            jPanel1.setOpaque(false);
        }

        styleSummaryCard(pnlTotalSales, new Color(58, 123, 255));
        styleSummaryCard(pnlTotalTransactions, new Color(22, 163, 74));
        styleSummaryCard(pnlTotalItemsSold, new Color(245, 158, 11));
        styleSummaryCard(pnlTotalProfit, new Color(139, 92, 246));

        styleFilterPanel(pnlFilters);
        styleHeaderPanel(tblHeader);

        styleTextField(searchInvoiceNumber);
        styleComboBox(listOfCashiers);
        styleDateChooser(dateFrom);
        styleDateChooser(dateTo);

        styleButton(btnRefresh, new Color(89, 92, 255));
        styleButton(btnExportToPDF, new Color(220, 53, 69));
        styleButton(btnExportToExcel, new Color(22, 163, 74));

        installRoundedButtonPainter(btnRefresh);
        installRoundedButtonPainter(btnExportToPDF);
        installRoundedButtonPainter(btnExportToExcel);

        if (jLabel2 != null) {
            jLabel2.setForeground(new Color(23, 43, 77));
        }
        if (jLabel17 != null) {
            jLabel17.setForeground(new Color(88, 105, 136));
        }
        if (jLabel11 != null) {
            jLabel11.setForeground(new Color(26, 45, 84));
        }

    }

    private void styleSummaryCard(JPanel panel, Color accent) {
        if (panel == null) {
            return;
        }

        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.putClientProperty("accentColor", accent);

        for (Component c : panel.getComponents()) {
            if (c instanceof JLabel lbl) {
                String text = lbl.getText() == null ? "" : lbl.getText().trim();

                if (text.toLowerCase().contains("total")
                        || text.toLowerCase().contains("sales")
                        || text.toLowerCase().contains("transactions")
                        || text.toLowerCase().contains("items")
                        || text.toLowerCase().contains("profit")) {
                    lbl.setForeground(new Color(92, 108, 132));
                }
            }
        }

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.repaint();
            }
        });
    }

    private void styleFilterPanel(JPanel panel) {
        if (panel == null) {
            return;
        }
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));
    }

    private void styleHeaderPanel(JPanel panel) {
        if (panel == null) {
            return;
        }
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 12, 10, 12));
    }

    private void styleTextField(JTextField field) {
        if (field == null) {
            return;
        }

        field.setFont(new Font("Tahoma", Font.PLAIN, 13));
        field.setBackground(Color.WHITE);
        field.setForeground(new Color(35, 48, 68));
        field.setCaretColor(new Color(59, 118, 255));
        field.setMargin(new Insets(2, 10, 2, 10));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(205, 219, 238), 1, true),
                new EmptyBorder(7, 10, 7, 10)
        ));
    }

    private void styleComboBox(JComboBox<?> combo) {
        if (combo == null) {
            return;
        }

        combo.setFont(new Font("Tahoma", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setForeground(new Color(35, 48, 68));
        combo.setFocusable(false);
        combo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(205, 219, 238), 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
    }

    private void styleDateChooser(com.toedter.calendar.JDateChooser chooser) {
        if (chooser == null) {
            return;
        }

        chooser.setBackground(Color.WHITE);
        chooser.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(205, 219, 238), 1, true),
                new EmptyBorder(1, 1, 1, 1)
        ));

        JTextField editor = ((JTextField) chooser.getDateEditor().getUiComponent());
        editor.setFont(new Font("Tahoma", Font.PLAIN, 13));
        editor.setBorder(new EmptyBorder(7, 10, 7, 10));
        editor.setBackground(Color.WHITE);
        editor.setForeground(new Color(35, 48, 68));
        editor.setCaretColor(new Color(59, 118, 255));

        // 🔥 ADD THESE
        editor.setEditable(false);
        editor.setFocusable(false);
    }

    private void styleButton(JButton button, Color baseColor) {
        if (button == null) {
            return;
        }

        Color hoverColor = new Color(
                Math.min(baseColor.getRed() + 15, 255),
                Math.min(baseColor.getGreen() + 15, 255),
                Math.min(baseColor.getBlue() + 15, 255)
        );

        Color pressColor = new Color(
                Math.max(baseColor.getRed() - 15, 0),
                Math.max(baseColor.getGreen() - 15, 0),
                Math.max(baseColor.getBlue() - 15, 0)
        );

        Color disabledBg = new Color(200, 210, 225);
        Color disabledFg = new Color(120, 130, 150);

        button.setFont(new Font("Tahoma", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 18, 8, 18));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.putClientProperty("btnBaseColor", baseColor);
        button.putClientProperty("btnHoverColor", hoverColor);
        button.putClientProperty("btnPressColor", pressColor);
        button.putClientProperty("btnDisabledBg", disabledBg);
        button.putClientProperty("btnDisabledFg", disabledFg);
        button.putClientProperty("btnColor", baseColor);

        for (java.awt.event.MouseListener ml : button.getMouseListeners()) {
            button.removeMouseListener(ml);
        }

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!button.isEnabled()) {
                    return;
                }
                button.putClientProperty("btnColor", hoverColor);
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!button.isEnabled()) {
                    return;
                }
                button.putClientProperty("btnColor", baseColor);
                button.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!button.isEnabled()) {
                    return;
                }
                button.putClientProperty("btnColor", pressColor);
                button.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
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
                button.setForeground(Color.WHITE);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else {
                button.putClientProperty("btnColor", disabledBg);
                button.setForeground(disabledFg);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            button.repaint();
        });
    }

    private void installRoundedButtonPainter(JButton button) {
        if (button == null) {
            return;
        }

        button.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                JButton b = (JButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg;
                Color fg;

                if (!b.isEnabled()) {
                    bg = (Color) b.getClientProperty("btnDisabledBg");
                    fg = (Color) b.getClientProperty("btnDisabledFg");
                    if (bg == null) {
                        bg = new Color(200, 210, 225);
                    }
                    if (fg == null) {
                        fg = new Color(120, 130, 150);
                    }
                } else {
                    bg = (Color) b.getClientProperty("btnColor");
                    fg = b.getForeground();
                    if (bg == null) {
                        bg = b.getBackground();
                    }
                }

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 18, 18);

                g2.setColor(new Color(255, 255, 255, 60));
                g2.drawRoundRect(0, 0, b.getWidth() - 1, b.getHeight() - 1, 18, 18);

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

    private void enhanceTable(JTable table, JScrollPane scrollPane) {
        Font bodyFont = new Font("Tahoma", Font.PLAIN, 14);
        Font headerFont = new Font("Tahoma", Font.BOLD, 14);
        Font pillFont = new Font("Tahoma", Font.BOLD, 12);

        Color panelBg = new Color(241, 247, 253);
        Color tableBg = Color.WHITE;
        Color headerBg = new Color(28, 74, 122);
        Color headerFg = Color.WHITE;
        Color textColor = new Color(32, 48, 70);
        Color gridColor = new Color(225, 234, 244);
        Color selectBg = new Color(232, 240, 252);
        Color selectFg = new Color(20, 43, 67);
        Color cardBg = new Color(250, 253, 255);
        Color stripeBg = new Color(246, 250, 254);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.setBackground(panelBg);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(cardBg);
        scrollPane.setViewportBorder(null);

        table.setBackground(tableBg);
        table.setForeground(textColor);
        table.setGridColor(gridColor);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setRowHeight(50);
        table.setFocusable(false);
        table.setOpaque(true);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(selectBg);
        table.setSelectionForeground(selectFg);
        table.setBorder(BorderFactory.createEmptyBorder());

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 45));

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
                label.setBorder(new EmptyBorder(14, 12, 14, 12));
                return label;
            }
        });

        scrollPane.setViewportBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        table.setIntercellSpacing(new Dimension(0, 0));
        header.setBorder(null);

        DefaultTableCellRenderer bodyRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                String text = value == null ? "" : value.toString();
                String columnName = table.getColumnName(column).toLowerCase();

                label.setOpaque(true);
                label.setText(text);
                label.setBorder(new EmptyBorder(10, 14, 10, 14));
                label.setForeground(textColor);
                label.setBackground(tableBg);
                label.setBackground(row % 2 == 0 ? tableBg : stripeBg);
                label.setForeground(textColor);

                Font base = table.getFont();
                if (base != null) {
                    label.setFont(base);
                }

                if (isSelected) {
                    label.setBackground(selectBg);
                    label.setForeground(selectFg);
                }

                if (column == 2) {
                    label.setHorizontalAlignment(SwingConstants.LEFT);
                } else {
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                }

                if (columnName.contains("items")) {
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                }

                if (columnName.contains("total")) {
                    label.setHorizontalAlignment(SwingConstants.RIGHT);
                    try {
                        double amount = Double.parseDouble(text.replace("₱", "").replace(",", "").trim());
                        label.setText(tablePesoFormat.format(amount));
                    } catch (Exception ex) {
                        label.setText(text);
                    }

                    Font baseFont = table.getFont();
                    if (baseFont != null) {
                        label.setFont(baseFont.deriveFont(Font.BOLD));
                    }
                    label.setForeground(new Color(18, 66, 141));
                }

                if (columnName.contains("payment")) {
                    label.setFont(pillFont);
                    label.setText("  " + text + "  ");

                    String method = text.toLowerCase();

                    if (method.contains("cash")) {
                        label.setBackground(new Color(220, 252, 231));
                        label.setForeground(new Color(22, 101, 52));
                    } else if (method.contains("gcash")) {
                        label.setBackground(new Color(219, 234, 254));
                        label.setForeground(new Color(30, 64, 175));
                    } else if (method.contains("card")) {
                        label.setBackground(new Color(254, 243, 199));
                        label.setForeground(new Color(146, 64, 14));
                    }
                }

                return label;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(bodyRenderer);
        }

        if (table.getColumnCount() >= 6) {
            table.getColumnModel().getColumn(0).setPreferredWidth(240);
            table.getColumnModel().getColumn(1).setPreferredWidth(230);
            table.getColumnModel().getColumn(2).setPreferredWidth(300);
            table.getColumnModel().getColumn(3).setPreferredWidth(90);
            table.getColumnModel().getColumn(4).setPreferredWidth(160);
            table.getColumnModel().getColumn(5).setPreferredWidth(150);
        }

        scrollPane.setVerticalScrollBar(new ModernScrollBar());
        scrollPane.setHorizontalScrollBar(new ModernScrollBar());

        wrapScrollPaneInCard(scrollPane);
    }

    private void wrapScrollPaneInCard(JScrollPane scrollPane) {
        Container parent = scrollPane.getParent();

        if (parent == null) {
            return;
        }

        if (parent instanceof JViewport) {
            parent = parent.getParent();
        }

        if (parent == null) {
            return;
        }

        for (Component c : parent.getComponents()) {
            if (c instanceof ShadowPanel) {
                return;
            }
        }

        parent.remove(scrollPane);

        ShadowPanel wrapper = new ShadowPanel();
        wrapper.setLayout(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(14, 14, 14, 14));
        wrapper.add(scrollPane, BorderLayout.CENTER);

        parent.setLayout(new BorderLayout());
        parent.add(wrapper, BorderLayout.CENTER);
        parent.revalidate();
        parent.repaint();
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
                g2.setColor(new Color(120, 145, 175, Math.max(3, 18 - i)));
                g2.fillRoundRect(
                        shadowGap - i / 2,
                        shadowGap - i / 2,
                        width + i,
                        height + i,
                        arc,
                        arc
                );
            }

            g2.setPaint(new GradientPaint(
                    0, 0, new Color(253, 255, 255),
                    0, getHeight(), new Color(245, 249, 255)
            ));
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

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(
                0, 0, new Color(244, 249, 255),
                getWidth(), getHeight(), new Color(230, 239, 255)
        );

        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();

        super.paintComponent(g);
    }

    private void setupFilters() {
        searchInvoiceNumber.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilters();
            }
        });

        listOfCashiers.addActionListener((java.awt.event.ActionEvent e) -> {
            applyFilters();
        });

        dateFrom.addPropertyChangeListener("date", (PropertyChangeEvent evt) -> {
            applyFilters();
        });

        dateTo.addPropertyChangeListener("date", (PropertyChangeEvent evt) -> {
            applyFilters();
        });

        btnExportToExcel.addActionListener((java.awt.event.ActionEvent e) -> {
            exportFilteredTableToCSV();
        });

        btnExportToPDF.addActionListener((java.awt.event.ActionEvent e) -> {
            exportFilteredTableToPDF();
        });
    }

    private void applyFilters() {
        filteredSalesRecords.clear();
        tableModel.setRowCount(0);

        String invoiceSearch = searchInvoiceNumber.getText() == null
                ? ""
                : searchInvoiceNumber.getText().trim().toLowerCase();

        String selectedCashier = listOfCashiers.getSelectedItem() == null
                ? "All Cashiers"
                : listOfCashiers.getSelectedItem().toString();

        Date fromDate = dateFrom.getDate();
        Date toDate = dateTo.getDate();

        for (int i = 0; i < allSalesRecords.size(); i++) {
            SalesRecord record = allSalesRecords.get(i);

            boolean matchesInvoice = invoiceSearch.length() == 0
                    || (record.invoiceNumber != null && record.invoiceNumber.toLowerCase().contains(invoiceSearch));

            boolean matchesCashier = "All Cashiers".equalsIgnoreCase(selectedCashier)
                    || (record.cashier != null && record.cashier.equalsIgnoreCase(selectedCashier));

            boolean matchesDate = isWithinDateRange(record.date, fromDate, toDate);

            if (matchesInvoice && matchesCashier && matchesDate) {
                filteredSalesRecords.add(record);

                tableModel.addRow(new Object[]{
                    nullToEmpty(record.invoiceNumber),
                    formatTableDate(record.date),
                    nullToEmpty(record.cashier),
                    Integer.valueOf(record.items),
                    formatPeso(record.totalAmount),
                    nullToEmpty(record.paymentMethod)
                });
            }
        }

        updateSummaryCards();
    }

    private void setupTableContextMenu() {
        tblSalesTransaction.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showSalesHistoryPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showSalesHistoryPopup(e);
            }
        });
    }

    private void showSalesHistoryPopup(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return;
        }

        int row = tblSalesTransaction.rowAtPoint(e.getPoint());
        if (row < 0) {
            return;
        }

        if (!tblSalesTransaction.isRowSelected(row)) {
            tblSalesTransaction.setRowSelectionInterval(row, row);
        }

        JPopupMenu popup = new JPopupMenu();

        JMenuItem viewReceipt = new JMenuItem("View Receipt");
        JMenuItem refund = new JMenuItem("Refund");
        JMenuItem voidTransaction = new JMenuItem("Void Transaction");

        viewReceipt.addActionListener(action -> handleViewReceiptAction());
        refund.addActionListener(action -> handleRefundAction());
        voidTransaction.addActionListener(action -> handleVoidTransactionAction());

        popup.add(viewReceipt);
        popup.add(refund);
        popup.add(voidTransaction);
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    private void handleViewReceiptAction() {
        int selectedRow = tblSalesTransaction.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "No transaction selected.");
            return;
        }

        int modelRow = tblSalesTransaction.convertRowIndexToModel(selectedRow);
        if (modelRow < 0 || modelRow >= filteredSalesRecords.size()) {
            JOptionPane.showMessageDialog(this, "No transaction selected.");
            return;
        }

        SalesRecord record = filteredSalesRecords.get(modelRow);
        showReceiptDetails(record);
    }

    private void handleRefundAction() {
        int selectedRow = tblSalesTransaction.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "No transaction selected.");
            return;
        }

        if (!canModifySaleHistory()) {
            JOptionPane.showMessageDialog(this, "Access denied.");
            return;
        }

        int modelRow = tblSalesTransaction.convertRowIndexToModel(selectedRow);
        if (modelRow < 0 || modelRow >= filteredSalesRecords.size()) {
            JOptionPane.showMessageDialog(this, "No transaction selected.");
            return;
        }

        SalesRecord record = filteredSalesRecords.get(modelRow);
        if (!confirmAction("refund")) {
            return;
        }

        try {
            String currentStatus = getSaleStatus(record.saleId);
            if (currentStatus != null && ("VOIDED".equalsIgnoreCase(currentStatus) || "REFUNDED".equalsIgnoreCase(currentStatus))) {
                JOptionPane.showMessageDialog(this, "This transaction is already " + currentStatus + ".");
                return;
            }

            performSaleStatusUpdate(record, "REFUNDED");
            insertUserLog(currentLoggedInUsername, currentLoggedInUserRole,
                    "Refunded transaction " + record.invoiceNumber + " by " + currentLoggedInUsername);
            refreshAfterSaleAction();
            JOptionPane.showMessageDialog(this, "Transaction refunded successfully.");
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Refund action failed", ex);
            JOptionPane.showMessageDialog(this, "Unable to refund transaction: " + ex.getMessage());
        }
    }

    private void handleVoidTransactionAction() {
        int selectedRow = tblSalesTransaction.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "No transaction selected.");
            return;
        }

        if (!canModifySaleHistory()) {
            JOptionPane.showMessageDialog(this, "Access denied.");
            return;
        }

        int modelRow = tblSalesTransaction.convertRowIndexToModel(selectedRow);
        if (modelRow < 0 || modelRow >= filteredSalesRecords.size()) {
            JOptionPane.showMessageDialog(this, "No transaction selected.");
            return;
        }

        SalesRecord record = filteredSalesRecords.get(modelRow);
        if (!confirmAction("void")) {
            return;
        }

        try {
            String currentStatus = getSaleStatus(record.saleId);
            if (currentStatus != null && ("VOIDED".equalsIgnoreCase(currentStatus) || "REFUNDED".equalsIgnoreCase(currentStatus))) {
                JOptionPane.showMessageDialog(this, "This transaction is already " + currentStatus + ".");
                return;
            }

            performSaleStatusUpdate(record, "VOIDED");
            insertUserLog(currentLoggedInUsername, currentLoggedInUserRole,
                    "Voided transaction " + record.invoiceNumber + " by " + currentLoggedInUsername);
            refreshAfterSaleAction();
            JOptionPane.showMessageDialog(this, "Transaction voided successfully.");
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Void action failed", ex);
            JOptionPane.showMessageDialog(this, "Unable to void transaction: " + ex.getMessage());
        }
    }

    private boolean requestApproval(String actionType) {
        final boolean[] approved = {false};

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog;

        if (owner instanceof Frame) {
            dialog = new JDialog((Frame) owner, actionType + " Approval", true);
        } else if (owner instanceof Dialog) {
            dialog = new JDialog((Dialog) owner, actionType + " Approval", true);
        } else {
            dialog = new JDialog((Frame) null, actionType + " Approval", true);
        }

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new GridLayout(2, 2, 10, 10));
        content.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblUsername = new JLabel("Approver Username:");
        JTextField txtUsername = new JTextField();

        JLabel lblPassword = new JLabel("Approver Password:");
        JPasswordField txtPassword = new JPasswordField();

        content.add(lblUsername);
        content.add(txtUsername);
        content.add(lblPassword);
        content.add(txtPassword);

        JButton btnApprove = new JButton("Approve");
        JButton btnCancel = new JButton("Cancel");

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnApprove);

        JPanel root = new JPanel(new BorderLayout());
        root.add(content, BorderLayout.CENTER);
        root.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        btnApprove.addActionListener(e -> {
            String approverUsername = txtUsername.getText().trim();
            String approverPassword = new String(txtPassword.getPassword()).trim();

            if (approverUsername.isEmpty() || approverPassword.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username and password are required.");
                return;
            }

            if (approverUsername.equalsIgnoreCase(currentLoggedInUsername)) {
                insertUserLog(approverUsername, "",
                        actionType + " approval denied: cashier self-approval attempt");
                JOptionPane.showMessageDialog(dialog, "Cashiers cannot approve their own actions.");
                return;
            }

            String approverRole = validateApproverCredentials(approverUsername, approverPassword);
            if (approverRole == null) {
                insertUserLog(approverUsername, "",
                        actionType + " approval denied: invalid credentials or insufficient privileges");
                return;
            }

            insertUserLog(approverUsername, approverRole,
                    actionType + " approved by " + approverUsername);
            approved[0] = true;
            dialog.dispose();
        });

        btnCancel.addActionListener(e -> {
            insertUserLog(currentLoggedInUsername, currentLoggedInUserRole,
                    actionType + " approval cancelled by " + currentLoggedInUsername);
            dialog.dispose();
        });

        dialog.setVisible(true);
        return approved[0];
    }

    private String validateApproverCredentials(String username, String password) {
        String sql = "SELECT role, password, status FROM users WHERE username = ? LIMIT 1";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, username);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "Approver credentials are invalid.");
                    return null;
                }

                String role = rs.getString("role");
                String storedPassword = rs.getString("password");
                String status = rs.getString("status");
                boolean passwordMatches;

                if (storedPassword != null && storedPassword.startsWith("$2a$")) {
                    passwordMatches = BCrypt.checkpw(password, storedPassword);
                } else {
                    passwordMatches = password.equals(storedPassword);
                }

                if (!passwordMatches) {
                    JOptionPane.showMessageDialog(this, "Approver credentials are invalid.");
                    return null;
                }

                if (!"Active".equalsIgnoreCase(status)) {
                    JOptionPane.showMessageDialog(this, "Approver account is not active.");
                    return null;
                }

                if (!"Manager".equalsIgnoreCase(role) && !"Super Admin".equalsIgnoreCase(role)) {
                    JOptionPane.showMessageDialog(this, "Approver must be a Manager or Super Admin.");
                    return null;
                }

                return role;
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Approver validation error", ex);
            JOptionPane.showMessageDialog(this, "Unable to validate approver credentials: " + ex.getMessage());
            return null;
        }
    }

    private void insertUserLog(String username, String userRole, String action) {
        String sql = "INSERT INTO user_logs (username, user_role, action) VALUES (?, ?, ?)";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, username);
            pst.setString(2, userRole);
            pst.setString(3, action);
            pst.executeUpdate();
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Failed to insert user log", ex);
        }
    }

    private boolean canModifySaleHistory() {
        return "Manager".equalsIgnoreCase(currentLoggedInUserRole)
                || "Super Admin".equalsIgnoreCase(currentLoggedInUserRole);
    }

    private boolean confirmAction(String actionName) {
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to " + actionName + " the selected transaction?",
                "Confirm " + actionName.substring(0, 1).toUpperCase() + actionName.substring(1),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    private String getSaleStatus(int saleId) throws SQLException {
        String sql = "SELECT status FROM sales WHERE sale_id = ? LIMIT 1";
        try (Connection con = DBConnection.dbConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, saleId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("status");
                }
            }
        }
        return null;
    }

    private void performSaleStatusUpdate(SalesRecord record, String newStatus) throws SQLException {
        Connection con = null;
        try {
            con = DBConnection.dbConnection();
            con.setAutoCommit(false);

            if (!restoreStocksForSale(con, record.saleId)) {
                con.rollback();
                throw new SQLException("Failed to restore stock for sale " + record.saleId);
            }

            String updateSaleSql = "UPDATE sales SET status = ? WHERE sale_id = ?";
            try (PreparedStatement pst = con.prepareStatement(updateSaleSql)) {
                pst.setString(1, newStatus);
                pst.setInt(2, record.saleId);
                int rows = pst.executeUpdate();
                if (rows == 0) {
                    con.rollback();
                    throw new SQLException("Sale not found for update.");
                }
            }

            con.commit();
        } catch (SQLException ex) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    logger.log(java.util.logging.Level.SEVERE, "Rollback failed", rollbackEx);
                }
            }
            throw ex;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException ex) {
                    logger.log(java.util.logging.Level.SEVERE, "Failed to close connection", ex);
                }
            }
        }
    }

    private boolean restoreStocksForSale(Connection con, int saleId) throws SQLException {
        String detailsSql = "SELECT product_id, quantity FROM sales_details WHERE sale_id = ?";
        try (PreparedStatement pstDetails = con.prepareStatement(detailsSql)) {
            pstDetails.setInt(1, saleId);
            try (ResultSet rs = pstDetails.executeQuery()) {
                while (rs.next()) {
                    int productId = rs.getInt("product_id");
                    int quantity = rs.getInt("quantity");
                    if (quantity <= 0) {
                        continue;
                    }

                    String updateStockSql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE product_id = ?";
                    try (PreparedStatement pstUpdate = con.prepareStatement(updateStockSql)) {
                        pstUpdate.setInt(1, quantity);
                        pstUpdate.setInt(2, productId);
                        int rows = pstUpdate.executeUpdate();
                        if (rows == 0) {
                            throw new SQLException("Product not found while restoring stock: " + productId);
                        }
                    }
                }
            }
        }
        return true;
    }

    private void refreshAfterSaleAction() {
        resetAndRefreshSalesHistory();
    }

    private void showReceiptDetails(SalesRecord record) {
        if (record == null) {
            JOptionPane.showMessageDialog(this, "No transaction selected.");
            return;
        }

        String headerSql = "SELECT s.invoice_number, s.sale_date, s.subtotal, s.vat, s.discount, "
                + "s.total_amount, s.payment_method, s.cash_tendered, s.change_amount, "
                + "TRIM(CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(NULLIF(u.middle_name, ''), ''), "
                + "CASE WHEN COALESCE(NULLIF(u.middle_name, ''), '') = '' THEN '' ELSE ' ' END, COALESCE(u.last_name, ''))) "
                + "AS cashier "
                + "FROM sales s "
                + "INNER JOIN users u ON s.user_id = u.user_id "
                + "WHERE s.sale_id = ?";

        String detailsSql = "SELECT COALESCE(p.name, '') AS product_name, sd.quantity, sd.price, sd.discount, sd.subtotal "
                + "FROM sales_details sd "
                + "LEFT JOIN products p ON sd.product_id = p.product_id "
                + "WHERE sd.sale_id = ?";

        StringBuilder receiptText = new StringBuilder();

        try (Connection con = DBConnection.dbConnection(); PreparedStatement pstHeader = con.prepareStatement(headerSql); PreparedStatement pstDetails = con.prepareStatement(detailsSql)) {

            pstHeader.setInt(1, record.saleId);
            try (ResultSet rsHeader = pstHeader.executeQuery()) {
                if (rsHeader.next()) {
                    receiptText.append("INVOICE: ").append(nullToEmpty(rsHeader.getString("invoice_number"))).append("\n");
                    receiptText.append("Date: ").append(formatTableDate(rsHeader.getTimestamp("sale_date"))).append("\n");
                    receiptText.append("Cashier: ").append(nullToEmpty(rsHeader.getString("cashier"))).append("\n");
                    receiptText.append("Payment Method: ").append(nullToEmpty(rsHeader.getString("payment_method"))).append("\n");
                    receiptText.append("Subtotal: ").append(formatPeso(rsHeader.getDouble("subtotal"))).append("\n");
                    receiptText.append("VAT: ").append(formatPeso(rsHeader.getDouble("vat"))).append("\n");
                    receiptText.append("Discount: ").append(formatPeso(rsHeader.getDouble("discount"))).append("\n");
                    receiptText.append("Total: ").append(formatPeso(rsHeader.getDouble("total_amount"))).append("\n");
                    receiptText.append("Cash Tendered: ").append(formatPeso(rsHeader.getDouble("cash_tendered"))).append("\n");
                    receiptText.append("Change: ").append(formatPeso(rsHeader.getDouble("change_amount"))).append("\n\n");
                } else {
                    JOptionPane.showMessageDialog(this, "Transaction details not found.");
                    return;
                }
            }

            pstDetails.setInt(1, record.saleId);
            try (ResultSet rsDetails = pstDetails.executeQuery()) {
                receiptText.append("Item\tQty\tPrice\tDiscount\tSubtotal\n");
                receiptText.append("-------------------------------------------------------------\n");
                while (rsDetails.next()) {
                    String itemName = rsDetails.getString("product_name");
                    int qty = rsDetails.getInt("quantity");
                    double price = rsDetails.getDouble("price");
                    double discount = rsDetails.getDouble("discount");
                    double subtotal = rsDetails.getDouble("subtotal");
                    receiptText.append(itemName).append("\t")
                            .append(qty).append("\t")
                            .append(formatPeso(price)).append("\t")
                            .append(formatPeso(discount)).append("\t")
                            .append(formatPeso(subtotal)).append("\n");
                }
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Error loading receipt details", ex);
            JOptionPane.showMessageDialog(this, "Unable to load receipt details: " + ex.getMessage());
            return;
        }

        JTextArea textArea = new JTextArea(receiptText.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setCaretPosition(0);

        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Receipt Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean isWithinDateRange(Date saleDate, Date fromDate, Date toDate) {
        if (saleDate == null) {
            return false;
        }

        long saleTime = saleDate.getTime();

        if (fromDate != null) {
            long fromTime = removeTime(fromDate).getTime();
            if (saleTime < fromTime) {
                return false;
            }
        }

        if (toDate != null) {
            long toTime = endOfDay(toDate).getTime();
            if (saleTime > toTime) {
                return false;
            }
        }

        return true;
    }

    private Date removeTime(Date date) {
        try {
            return fileDateFormat.parse(fileDateFormat.format(date));
        } catch (Exception e) {
            return date;
        }
    }

    private Date endOfDay(Date date) {
        return new Date(removeTime(date).getTime() + (24L * 60L * 60L * 1000L) - 1L);
    }

    private void updateSummaryCards() {
        double totalSales = 0.0;
        int totalTransactions = filteredSalesRecords.size();
        int totalItemsSold = 0;
        double totalProfit = 0.0;

        for (int i = 0; i < filteredSalesRecords.size(); i++) {
            SalesRecord record = filteredSalesRecords.get(i);
            totalSales += record.totalAmount;
            totalItemsSold += record.items;
            totalProfit += record.profit;
        }

        totalSalesAutomatedValue.setText(formatPeso(totalSales));
        totalTransactionsAutomatedValue.setText(String.valueOf(totalTransactions));
        totalItemsSoldAutomatedValue.setText(String.valueOf(totalItemsSold));
        totalProfitAutomatedValue.setText(formatPeso(totalProfit));
    }

    private String formatTableDate(Date date) {
        return date == null ? "" : tableDateFormat.format(date);
    }

    private String formatPeso(double amount) {
        return pesoFormat.format(amount);
    }

    private void exportFilteredTableToCSV() {
        if (filteredSalesRecords.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No data to export.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Excel-Compatible CSV");
        chooser.setSelectedFile(new File("sales_history.csv"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }

        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write("Invoice Number,Date,Cashier,Items,Total Amount,Payment Method");
            writer.newLine();

            for (int i = 0; i < filteredSalesRecords.size(); i++) {
                SalesRecord record = filteredSalesRecords.get(i);

                writer.write(escapeCSV(record.invoiceNumber) + ","
                        + escapeCSV(formatTableDate(record.date)) + ","
                        + escapeCSV(record.cashier) + ","
                        + record.items + ","
                        + escapeCSV(formatPeso(record.totalAmount)) + ","
                        + escapeCSV(record.paymentMethod));
                writer.newLine();
            }

            JOptionPane.showMessageDialog(this, "CSV exported successfully.");

        } catch (IOException ex) {
            logger.log(java.util.logging.Level.SEVERE, "CSV export error", ex);
            JOptionPane.showMessageDialog(this, "Error exporting CSV: " + ex.getMessage());
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e) {
            }
        }
    }

    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    private void exportFilteredTableToPDF() {
        if (filteredSalesRecords.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No data to export.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save PDF");
        chooser.setSelectedFile(new File("sales_history.pdf"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getAbsolutePath() + ".pdf");
        }

        Document document = null;

        try {
            PdfWriter writer = new PdfWriter(file.getAbsolutePath());
            PdfDocument pdf = new PdfDocument(writer);
            document = new Document(pdf);

            document.add(new Paragraph("Sales History Report").setBold().setFontSize(16));
            document.add(new Paragraph("Generated on: " + tableDateFormat.format(new Date())));
            document.add(new Paragraph(" "));

            Table pdfTable = new Table(UnitValue.createPercentArray(new float[]{3f, 3f, 3f, 1.5f, 2.5f, 2.5f}));
            pdfTable.setWidth(UnitValue.createPercentValue(100));

            pdfTable.addHeaderCell(new Cell().add(new Paragraph("Invoice Number").setBold()));
            pdfTable.addHeaderCell(new Cell().add(new Paragraph("Date").setBold()));
            pdfTable.addHeaderCell(new Cell().add(new Paragraph("Cashier").setBold()));
            pdfTable.addHeaderCell(new Cell().add(new Paragraph("Items").setBold()));
            pdfTable.addHeaderCell(new Cell().add(new Paragraph("Total Amount").setBold()));
            pdfTable.addHeaderCell(new Cell().add(new Paragraph("Payment Method").setBold()));

            for (int i = 0; i < filteredSalesRecords.size(); i++) {
                SalesRecord record = filteredSalesRecords.get(i);

                pdfTable.addCell(new Cell().add(new Paragraph(nullToEmpty(record.invoiceNumber))));
                pdfTable.addCell(new Cell().add(new Paragraph(formatTableDate(record.date))));
                pdfTable.addCell(new Cell().add(new Paragraph(nullToEmpty(record.cashier))));
                pdfTable.addCell(new Cell().add(new Paragraph(String.valueOf(record.items))));
                pdfTable.addCell(new Cell().add(new Paragraph(formatPeso(record.totalAmount))));
                pdfTable.addCell(new Cell().add(new Paragraph(nullToEmpty(record.paymentMethod))));
            }

            document.add(pdfTable);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Total Sales: " + totalSalesAutomatedValue.getText()));
            document.add(new Paragraph("Total Transactions: " + totalTransactionsAutomatedValue.getText()));
            document.add(new Paragraph("Total Items Sold: " + totalItemsSoldAutomatedValue.getText()));
            document.add(new Paragraph("Total Profit: " + totalProfitAutomatedValue.getText()));

            JOptionPane.showMessageDialog(this, "PDF exported successfully.");

        } catch (Exception ex) {
            logger.log(java.util.logging.Level.SEVERE, "PDF export error", ex);
            JOptionPane.showMessageDialog(this, "Error exporting PDF: " + ex.getMessage());
        } finally {
            try {
                if (document != null) {
                    document.close();
                }
            } catch (Exception e) {
            }
        }
    }

    private String nullToEmpty(String text) {
        return text == null ? "" : text;
    }

    private static class SalesRecord {

        int saleId;
        String invoiceNumber;
        Date date;
        String cashier;
        int items;
        double totalAmount;
        String paymentMethod;
        double profit;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlTotalSales = new GlassPanel();
        jLabel1 = new javax.swing.JLabel();
        totalSalesAutomatedValue = new javax.swing.JLabel();
        salesIcon = new javax.swing.JLabel();
        pnlTotalTransactions = new GlassPanel();
        jLabel3 = new javax.swing.JLabel();
        totalTransactionsAutomatedValue = new javax.swing.JLabel();
        transacIcon = new javax.swing.JLabel();
        pnlTotalItemsSold = new GlassPanel();
        jLabel4 = new javax.swing.JLabel();
        totalItemsSoldAutomatedValue = new javax.swing.JLabel();
        soldIcon = new javax.swing.JLabel();
        pnlTotalProfit = new GlassPanel();
        jLabel5 = new javax.swing.JLabel();
        totalProfitAutomatedValue = new javax.swing.JLabel();
        profitIcon = new javax.swing.JLabel();
        tblHeader = new RoundedPanel();
        jLabel11 = new javax.swing.JLabel("Add New User") {
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
        btnExportToPDF = new javax.swing.JButton();
        btnExportToExcel = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        jLabel2 = new point.of.sale.system.classes.GradientFont();
        jLabel17 = new javax.swing.JLabel();
        jPanel1 = new RoundedPanel();
        tableScrollPane = new javax.swing.JScrollPane();
        tblSalesTransaction = new javax.swing.JTable();
        jPanel2 = new RoundedPanel();
        pnlFilters = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        dateFrom = new com.toedter.calendar.JDateChooser();
        dateTo = new com.toedter.calendar.JDateChooser();
        listOfCashiers = new javax.swing.JComboBox<>();
        searchInvoiceNumber = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();

        setBackground(new java.awt.Color(240, 248, 255));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlTotalSales.setBackground(new java.awt.Color(255, 255, 255));
        pnlTotalSales.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel1.setText("Total Sales");
        pnlTotalSales.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        totalSalesAutomatedValue.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        totalSalesAutomatedValue.setText("---");
        pnlTotalSales.add(totalSalesAutomatedValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 170, -1));

        salesIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/sales1.png"))); // NOI18N
        pnlTotalSales.add(salesIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 20, -1, -1));

        add(pnlTotalSales, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 100, 250, 150));

        pnlTotalTransactions.setBackground(new java.awt.Color(255, 255, 255));
        pnlTotalTransactions.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel3.setText("Total Transactions");
        pnlTotalTransactions.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        totalTransactionsAutomatedValue.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        totalTransactionsAutomatedValue.setText("---");
        pnlTotalTransactions.add(totalTransactionsAutomatedValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 210, -1));

        transacIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/shopping-cart.png"))); // NOI18N
        pnlTotalTransactions.add(transacIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 20, 40, -1));

        add(pnlTotalTransactions, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 100, 250, 150));

        pnlTotalItemsSold.setBackground(new java.awt.Color(255, 255, 255));
        pnlTotalItemsSold.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel4.setText("Total Items Sold");
        pnlTotalItemsSold.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        totalItemsSoldAutomatedValue.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        totalItemsSoldAutomatedValue.setText("---");
        pnlTotalItemsSold.add(totalItemsSoldAutomatedValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 200, -1));

        soldIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/box.png"))); // NOI18N
        pnlTotalItemsSold.add(soldIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 20, -1, -1));

        add(pnlTotalItemsSold, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 100, 240, 150));

        pnlTotalProfit.setBackground(new java.awt.Color(255, 255, 255));
        pnlTotalProfit.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel5.setText("Total Profit");
        pnlTotalProfit.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        totalProfitAutomatedValue.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        totalProfitAutomatedValue.setText("---");
        pnlTotalProfit.add(totalProfitAutomatedValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 170, -1));

        profitIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/profit.png"))); // NOI18N
        pnlTotalProfit.add(profitIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 20, -1, -1));

        add(pnlTotalProfit, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 100, 240, 150));

        tblHeader.setBackground(new java.awt.Color(18, 48, 174));
        tblHeader.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel11.setText("Sales Transactions");
        tblHeader.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 22, -1, -1));

        btnExportToPDF.setText("Export to PDF");
        tblHeader.add(btnExportToPDF, new org.netbeans.lib.awtextra.AbsoluteConstraints(810, 20, -1, -1));

        btnExportToExcel.setText("Export to Excel");
        tblHeader.add(btnExportToExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 20, -1, -1));

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);
        tblHeader.add(btnRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(718, 20, -1, -1));

        add(tblHeader, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 420, 1100, 70));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel2.setText("SALES HISTORY");
        add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 300, -1));

        jLabel17.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel17.setText("View and manage sales transaction");
        add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, -1, -1));

        jPanel1.setBackground(new java.awt.Color(122, 170, 206));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblSalesTransaction.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        tblSalesTransaction.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Invoice Number", "Date", "Cashier", "Items", "Total Amount", "Payment Method"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblSalesTransaction.getTableHeader().setReorderingAllowed(false);
        tableScrollPane.setViewportView(tblSalesTransaction);
        if (tblSalesTransaction.getColumnModel().getColumnCount() > 0) {
            tblSalesTransaction.getColumnModel().getColumn(0).setResizable(false);
            tblSalesTransaction.getColumnModel().getColumn(0).setPreferredWidth(200);
            tblSalesTransaction.getColumnModel().getColumn(1).setResizable(false);
            tblSalesTransaction.getColumnModel().getColumn(1).setPreferredWidth(160);
            tblSalesTransaction.getColumnModel().getColumn(2).setResizable(false);
            tblSalesTransaction.getColumnModel().getColumn(2).setPreferredWidth(180);
            tblSalesTransaction.getColumnModel().getColumn(3).setResizable(false);
            tblSalesTransaction.getColumnModel().getColumn(3).setPreferredWidth(60);
            tblSalesTransaction.getColumnModel().getColumn(4).setResizable(false);
            tblSalesTransaction.getColumnModel().getColumn(4).setPreferredWidth(150);
            tblSalesTransaction.getColumnModel().getColumn(5).setResizable(false);
            tblSalesTransaction.getColumnModel().getColumn(5).setPreferredWidth(160);
        }

        jPanel1.add(tableScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 1080, 300));

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 490, 1100, 320));

        jPanel2.setBackground(new java.awt.Color(122, 170, 206));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlFilters.setBackground(new java.awt.Color(255, 255, 255));
        pnlFilters.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel6.setText("Filters");
        pnlFilters.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 80, -1));

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(40, 55, 80));
        jLabel7.setText("Invoice Number");
        pnlFilters.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 50, -1, -1));

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(40, 55, 80));
        jLabel8.setText("Date From");
        pnlFilters.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, -1, -1));

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(40, 55, 80));
        jLabel9.setText("Date To");
        pnlFilters.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 50, -1, -1));

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(40, 55, 80));
        jLabel10.setText("Cashier");
        pnlFilters.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 50, -1, -1));
        pnlFilters.add(dateFrom, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 160, 30));
        pnlFilters.add(dateTo, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 70, 160, 30));

        listOfCashiers.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        pnlFilters.add(listOfCashiers, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 70, 230, 30));

        searchInvoiceNumber.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        pnlFilters.add(searchInvoiceNumber, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 70, 190, 30));

        jPanel2.add(pnlFilters, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 1080, 130));

        add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 270, 1100, 150));

        jSeparator1.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator1.setForeground(new java.awt.Color(0, 0, 0));
        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 40, 20, 50));

        jSeparator3.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator3.setForeground(new java.awt.Color(0, 0, 0));
        add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 30, 760, 10));
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        resetAndRefreshSalesHistory();
    }//GEN-LAST:event_btnRefreshActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExportToExcel;
    private javax.swing.JButton btnExportToPDF;
    private javax.swing.JButton btnRefresh;
    private com.toedter.calendar.JDateChooser dateFrom;
    private com.toedter.calendar.JDateChooser dateTo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JComboBox<String> listOfCashiers;
    private javax.swing.JPanel pnlFilters;
    private javax.swing.JPanel pnlTotalItemsSold;
    private javax.swing.JPanel pnlTotalProfit;
    private javax.swing.JPanel pnlTotalSales;
    private javax.swing.JPanel pnlTotalTransactions;
    private javax.swing.JLabel profitIcon;
    private javax.swing.JLabel salesIcon;
    private javax.swing.JTextField searchInvoiceNumber;
    private javax.swing.JLabel soldIcon;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JPanel tblHeader;
    private javax.swing.JTable tblSalesTransaction;
    private javax.swing.JLabel totalItemsSoldAutomatedValue;
    private javax.swing.JLabel totalProfitAutomatedValue;
    private javax.swing.JLabel totalSalesAutomatedValue;
    private javax.swing.JLabel totalTransactionsAutomatedValue;
    private javax.swing.JLabel transacIcon;
    // End of variables declaration//GEN-END:variables
}
