package point.of.sale.system.screens;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import point.of.sale.system.classes.DBConnection;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import point.of.sale.system.classes.GlassPanel;
import point.of.sale.system.classes.RoundedPanel;

public class ReportsPanel extends javax.swing.JPanel {

    private DefaultTableModel model;
    private String currentReportType = "DAILY";
    private final java.text.DecimalFormat tablePesoFormat = new java.text.DecimalFormat("₱#,##0.00");

    public ReportsPanel() {
        initComponents();
        applyLuxuryTheme();
        initializeTable();
        loadCategories();
        loadDailySalesReport();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                refreshReports();
            }
        });

    }

    public void refreshReports() {
        loadCategories();
        applyCurrentReport();
    }

    public void showDailySalesReport() {
        activateReportCard(pnlDailySalesReport);
        loadDailySalesReport();
    }

    public void showLowStockReport() {
        activateReportCard(pnlLowStockReport);
        loadLowStockReport();
    }

    private int hoveredReportRow = -1;

    private void initializeTable() {
        model = new DefaultTableModel();
        tblReports.setModel(model);
        tblReports.setRowHeight(42);
        tblReports.getTableHeader().setReorderingAllowed(false);

        enhanceTable(tblReports, scrollReportsTable);
    }

    private void loadCategories() {
        cmbCategory.removeAllItems();
        cmbCategory.addItem("All Categories");

        String sql = "SELECT name FROM categories ORDER BY name ASC";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                cmbCategory.addItem(rs.getString("name"));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage());
        }
    }

    private String getSelectedCategory() {
        Object selected = cmbCategory.getSelectedItem();
        return selected == null ? "All Categories" : selected.toString();
    }

    private String getFormattedDate(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private void resetTable() {
        model.setRowCount(0);
        model.setColumnCount(0);
    }

    private void loadDailySalesReport() {
        currentReportType = "DAILY";
        lblCurrentReportTitle.setText("Daily Sales Report");
        resetTable();

        model.setColumnIdentifiers(new String[]{"Date", "Category", "Sales", "Quantity", "Status"});
        enhanceTable(tblReports, scrollReportsTable);

        String selectedCategory = getSelectedCategory();
        String startDate = getFormattedDate(dcStartDate.getDate());
        String endDate = getFormattedDate(dcEndDate.getDate());

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DATE(s.sale_date) AS report_date, ");
        sql.append("c.name AS category_name, ");
        sql.append("SUM(sd.subtotal) AS total_sales, ");
        sql.append("SUM(sd.quantity) AS total_quantity, ");
        sql.append("s.status ");
        sql.append("FROM sales s ");
        sql.append("JOIN sales_details sd ON s.sale_id = sd.sale_id ");
        sql.append("JOIN products p ON sd.product_id = p.product_id ");
        sql.append("JOIN categories c ON p.category_id = c.category_id ");
        sql.append("WHERE (s.status IS NULL OR s.status = 'COMPLETED' OR s.status = '') ");

        if (!"All Categories".equals(selectedCategory)) {
            sql.append("AND c.name = ? ");
        }
        if (startDate != null) {
            sql.append("AND DATE(s.sale_date) >= ? ");
        }
        if (endDate != null) {
            sql.append("AND DATE(s.sale_date) <= ? ");
        }

        sql.append("GROUP BY DATE(s.sale_date), c.name ");
        sql.append("ORDER BY DATE(s.sale_date) DESC, c.name ASC");

        try (Connection con = DBConnection.dbConnection(); PreparedStatement pst = con.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (!"All Categories".equals(selectedCategory)) {
                pst.setString(paramIndex++, selectedCategory);
            }
            if (startDate != null) {
                pst.setString(paramIndex++, startDate);
            }
            if (endDate != null) {
                pst.setString(paramIndex++, endDate);
            }

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("report_date"),
                        rs.getString("category_name"),
                        rs.getDouble("total_sales"),
                        rs.getInt("total_quantity"),
                        rs.getString("status")
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading daily sales report: " + e.getMessage());
        }
    }

    private void applyLuxuryTheme() {
        setOpaque(false);

        styleReportCard(pnlDailySalesReport, new Color(58, 123, 255), jLabel2, logo);
        styleReportCard(pnlMonthlySalesReport, new Color(14, 165, 233), jLabel3, logo1);
        styleReportCard(pnlInventoryReport, new Color(34, 197, 94), jLabel5, logo3);
        styleReportCard(pnlLowStockReport, new Color(245, 158, 11), jLabel4, logo2);
        styleReportCard(pnlProfitReport, new Color(139, 92, 246), jLabel6, logo4);

        stylePanelGlass(pnlFilters);
        stylePanelHeader(pnlExportButtons);

        styleComboBox(cmbCategory);
        styleDateChooser(dcStartDate);
        styleDateChooser(dcEndDate);

        styleButton(btnApplyFilter, new Color(89, 92, 255));
        styleButton(btnResetFilter, new Color(100, 116, 139));
        styleButton(btnExportPDF, new Color(220, 53, 69));
        styleButton(btnExportExcel, new Color(22, 163, 74));
        styleButton(btnRefresh, new Color(89, 92, 255));

        installRoundedButtonPainter(btnApplyFilter);
        installRoundedButtonPainter(btnResetFilter);
        installRoundedButtonPainter(btnExportPDF);
        installRoundedButtonPainter(btnExportExcel);
        installRoundedButtonPainter(btnRefresh);

        if (lblReportsModule != null) {
            lblReportsModule.setForeground(new Color(49, 73, 209));
        }
        if (lblSubTitle != null) {
            lblSubTitle.setForeground(new Color(67, 93, 135));
            lblSubTitle.setFont(new Font("Tahoma", Font.PLAIN, 15));
        }
        if (lblCurrentReportTitle != null) {
            lblCurrentReportTitle.setForeground(new Color(26, 45, 84));
            lblCurrentReportTitle.setFont(new Font("Tahoma", Font.BOLD, 21));
        }

        activateReportCard(pnlDailySalesReport);
    }

    private void styleReportCard(JPanel panel, Color accent, JLabel titleLabel, JLabel iconLabel) {
        if (panel == null) {
            return;
        }

        panel.setOpaque(false);
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(224, 232, 243), 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        panel.putClientProperty("accentColor", accent);
        panel.putClientProperty("hover", Boolean.FALSE);
        panel.putClientProperty("active", Boolean.FALSE);

        if (titleLabel != null) {
            titleLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
            titleLabel.setForeground(new Color(33, 54, 86));
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }

        if (iconLabel != null) {
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }

        if (Boolean.TRUE.equals(panel.getClientProperty("reportCardStyled"))) {
            updateReportCardAppearance(panel);
            return;
        }

        panel.putClientProperty("reportCardStyled", Boolean.TRUE);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.putClientProperty("hover", Boolean.TRUE);
                updateReportCardAppearance(panel);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.putClientProperty("hover", Boolean.FALSE);
                updateReportCardAppearance(panel);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (panel != null) {
                    panel.setBorder(new CompoundBorder(
                            new LineBorder(accent.darker(), 1, true),
                            new EmptyBorder(13, 12, 11, 12)
                    ));
                    panel.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                updateReportCardAppearance(panel);
            }
        });

        updateReportCardAppearance(panel);
    }

    private void activateReportCard(JPanel selectedPanel) {
        setReportCardActive(pnlDailySalesReport, pnlDailySalesReport == selectedPanel);
        setReportCardActive(pnlMonthlySalesReport, pnlMonthlySalesReport == selectedPanel);
        setReportCardActive(pnlInventoryReport, pnlInventoryReport == selectedPanel);
        setReportCardActive(pnlLowStockReport, pnlLowStockReport == selectedPanel);
        setReportCardActive(pnlProfitReport, pnlProfitReport == selectedPanel);
    }

    private void setReportCardActive(JPanel panel, boolean active) {
        if (panel == null) {
            return;
        }
        panel.putClientProperty("active", Boolean.valueOf(active));
        updateReportCardAppearance(panel);
    }

    private void updateReportCardAppearance(JPanel panel) {
        if (panel == null) {
            return;
        }

        Color accent = (Color) panel.getClientProperty("accentColor");
        if (accent == null) {
            accent = new Color(58, 123, 255);
        }

        boolean hover = Boolean.TRUE.equals(panel.getClientProperty("hover"));
        boolean active = Boolean.TRUE.equals(panel.getClientProperty("active"));

        Color normalBorder = new Color(224, 232, 243);
        Color hoverBorder = mix(accent, Color.WHITE, 0.35f);
        Color activeBorder = accent;

        int top = 12;
        int left = 12;
        int bottom = 12;
        int right = 12;

        if (hover && !active) {
            top = 10;
            bottom = 14;
        }

        if (active) {
            panel.setBorder(new CompoundBorder(
                    new CompoundBorder(
                            new LineBorder(activeBorder, 2, true),
                            new EmptyBorder(1, 1, 1, 1)
                    ),
                    new EmptyBorder(top, left, bottom, right)
            ));
        } else if (hover) {
            panel.setBorder(new CompoundBorder(
                    new LineBorder(hoverBorder, 1, true),
                    new EmptyBorder(top, left, bottom, right)
            ));
        } else {
            panel.setBorder(new CompoundBorder(
                    new LineBorder(normalBorder, 1, true),
                    new EmptyBorder(top, left, bottom, right)
            ));
        }

        for (Component c : panel.getComponents()) {
            if (c instanceof JLabel lbl) {
                String text = lbl.getText() == null ? "" : lbl.getText().trim();

                if (!text.isEmpty()) {
                    if (active) {
                        lbl.setForeground(new Color(20, 44, 86));
                    } else if (hover) {
                        lbl.setForeground(new Color(28, 52, 96));
                    } else {
                        lbl.setForeground(new Color(30, 49, 76));
                    }
                }
            }
        }

        panel.repaint();
    }

    private Color mix(Color c1, Color c2, float ratio) {
        float safeRatio = Math.max(0f, Math.min(1f, ratio));
        float inverse = 1f - safeRatio;

        int r = (int) (c1.getRed() * inverse + c2.getRed() * safeRatio);
        int g = (int) (c1.getGreen() * inverse + c2.getGreen() * safeRatio);
        int b = (int) (c1.getBlue() * inverse + c2.getBlue() * safeRatio);

        return new Color(r, g, b);
    }

    private void stylePanelGlass(JPanel panel) {
        if (panel == null) {
            return;
        }
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));
    }

    private void stylePanelHeader(JPanel panel) {
        if (panel == null) {
            return;
        }
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 12, 10, 12));
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

        DefaultTableCellRenderer reportsRenderer = new DefaultTableCellRenderer() {
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

                if (columnName.contains("sales")
                        || columnName.contains("amount")
                        || columnName.contains("total")
                        || columnName.contains("profit")
                        || columnName.contains("cost")
                        || columnName.contains("price")) {

                    label.setHorizontalAlignment(SwingConstants.RIGHT);

                    try {
                        double amount = Double.parseDouble(
                                text.replace("₱", "").replace(",", "").trim()
                        );
                        label.setText(tablePesoFormat.format(amount));
                    } catch (Exception ex) {
                        label.setText(text);
                    }

                    label.setFont(bodyFont.deriveFont(Font.BOLD));
                    label.setForeground(new Color(18, 66, 141));
                }

                if (columnName.contains("quantity")
                        || columnName.contains("items")
                        || columnName.contains("stock")
                        || columnName.contains("reorder")) {
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                }

                if (columnName.contains("status")) {
                    label.setFont(pillFont);
                    label.setText("  " + text + "  ");

                    if (text.equalsIgnoreCase("In Stock")) {
                        label.setBackground(new Color(212, 236, 247));
                        label.setForeground(new Color(23, 91, 120));
                    } else if (text.equalsIgnoreCase("Low Stock")) {
                        label.setBackground(new Color(254, 243, 199));
                        label.setForeground(new Color(146, 64, 14));
                    } else if (text.equalsIgnoreCase("Out of Stock")) {
                        label.setBackground(new Color(254, 226, 226));
                        label.setForeground(new Color(153, 27, 27));
                    } else {
                        label.setBackground(new Color(234, 240, 247));
                        label.setForeground(new Color(78, 95, 116));
                    }
                }

                return label;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(reportsRenderer);
        }

        // Match widths more cleanly for different reports
        if (table.getColumnCount() == 4) {
            table.getColumnModel().getColumn(0).setPreferredWidth(180);
            table.getColumnModel().getColumn(1).setPreferredWidth(180);
            table.getColumnModel().getColumn(2).setPreferredWidth(180);
            table.getColumnModel().getColumn(3).setPreferredWidth(120);
        } else if (table.getColumnCount() == 6) {
            table.getColumnModel().getColumn(0).setPreferredWidth(90);
            table.getColumnModel().getColumn(1).setPreferredWidth(220);
            table.getColumnModel().getColumn(2).setPreferredWidth(150);
            table.getColumnModel().getColumn(3).setPreferredWidth(90);
            table.getColumnModel().getColumn(4).setPreferredWidth(120);
            table.getColumnModel().getColumn(5).setPreferredWidth(120);
        } else if (table.getColumnCount() >= 7) {
            table.getColumnModel().getColumn(0).setPreferredWidth(90);
            table.getColumnModel().getColumn(1).setPreferredWidth(220);
            table.getColumnModel().getColumn(2).setPreferredWidth(150);
            table.getColumnModel().getColumn(3).setPreferredWidth(90);
            table.getColumnModel().getColumn(4).setPreferredWidth(120);
            table.getColumnModel().getColumn(5).setPreferredWidth(120);
            table.getColumnModel().getColumn(6).setPreferredWidth(120);
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
        if (scrollPane == null) {
            return;
        }

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

        if (parent instanceof ShadowPanel) {
            return;
        }

        for (Component c : parent.getComponents()) {
            if (c instanceof ShadowPanel) {
                ShadowPanel wrapper = (ShadowPanel) c;
                if (wrapper.getComponentCount() == 0) {
                    wrapper.setLayout(new BorderLayout());
                    wrapper.add(scrollPane, BorderLayout.CENTER);
                } else if (scrollPane.getParent() != wrapper) {
                    wrapper.removeAll();
                    wrapper.add(scrollPane, BorderLayout.CENTER);
                }
                wrapper.revalidate();
                wrapper.repaint();
                return;
            }
        }

        parent.remove(scrollPane);

        ShadowPanel wrapper = new ShadowPanel();
        wrapper.setLayout(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(12, 12, 12, 12));
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
                g2.setColor(new Color(110, 140, 170, Math.max(3, 18 - i)));
                g2.fillRoundRect(
                        shadowGap - i / 2,
                        shadowGap - i / 2,
                        width + i,
                        height + i,
                        arc,
                        arc
                );
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

    private void loadMonthlySalesReport() {
        currentReportType = "MONTHLY";
        lblCurrentReportTitle.setText("Monthly Sales Report");
        resetTable();

        model.setColumnIdentifiers(new String[]{"Month", "Category", "Sales", "Quantity", "Status"});
        enhanceTable(tblReports, scrollReportsTable);

        String selectedCategory = getSelectedCategory();
        String startDate = getFormattedDate(dcStartDate.getDate());
        String endDate = getFormattedDate(dcEndDate.getDate());

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DATE_FORMAT(s.sale_date, '%Y-%m') AS report_month, ");
        sql.append("c.name AS category_name, ");
        sql.append("SUM(sd.subtotal) AS total_sales, ");
        sql.append("SUM(sd.quantity) AS total_quantity, ");
        sql.append("s.status ");
        sql.append("FROM sales s ");
        sql.append("JOIN sales_details sd ON s.sale_id = sd.sale_id ");
        sql.append("JOIN products p ON sd.product_id = p.product_id ");
        sql.append("JOIN categories c ON p.category_id = c.category_id ");
        sql.append("WHERE (s.status IS NULL OR s.status = 'COMPLETED' OR s.status = '') ");

        if (!"All Categories".equals(selectedCategory)) {
            sql.append("AND c.name = ? ");
        }
        if (startDate != null) {
            sql.append("AND DATE(s.sale_date) >= ? ");
        }
        if (endDate != null) {
            sql.append("AND DATE(s.sale_date) <= ? ");
        }

        sql.append("GROUP BY DATE_FORMAT(s.sale_date, '%Y-%m'), c.name ");
        sql.append("ORDER BY report_month DESC, c.name ASC");

        try (Connection con = DBConnection.dbConnection(); PreparedStatement pst = con.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (!"All Categories".equals(selectedCategory)) {
                pst.setString(paramIndex++, selectedCategory);
            }
            if (startDate != null) {
                pst.setString(paramIndex++, startDate);
            }
            if (endDate != null) {
                pst.setString(paramIndex++, endDate);
            }

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("report_month"),
                        rs.getString("category_name"),
                        rs.getDouble("total_sales"),
                        rs.getInt("total_quantity"),
                        rs.getString("status")
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading monthly sales report: " + e.getMessage());
        }
    }

    private void loadInventoryReport() {
        currentReportType = "INVENTORY";
        lblCurrentReportTitle.setText("Inventory Report");
        resetTable();

        model.setColumnIdentifiers(new String[]{
            "Product ID", "Product Name", "Category", "Stock", "Cost Price", "Selling Price", "Status"
        });
        enhanceTable(tblReports, scrollReportsTable);

        String selectedCategory = getSelectedCategory();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.product_id, p.name AS product_name, c.name AS category_name, ");
        sql.append("p.stock_quantity, p.cost_price, p.selling_price, ");
        sql.append("CASE ");
        sql.append("WHEN p.stock_quantity <= 0 THEN 'Out of Stock' ");
        sql.append("WHEN p.stock_quantity <= p.reorder_level THEN 'Low Stock' ");
        sql.append("ELSE 'In Stock' ");
        sql.append("END AS stock_status ");
        sql.append("FROM products p ");
        sql.append("JOIN categories c ON p.category_id = c.category_id ");
        sql.append("WHERE 1=1 ");

        if (!"All Categories".equals(selectedCategory)) {
            sql.append("AND c.name = ? ");
        }

        sql.append("ORDER BY p.name ASC");

        try (Connection con = DBConnection.dbConnection(); PreparedStatement pst = con.prepareStatement(sql.toString())) {

            if (!"All Categories".equals(selectedCategory)) {
                pst.setString(1, selectedCategory);
            }

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("category_name"),
                        rs.getInt("stock_quantity"),
                        rs.getDouble("cost_price"),
                        rs.getDouble("selling_price"),
                        rs.getString("stock_status")
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading inventory report: " + e.getMessage());
        }
    }

    private void loadLowStockReport() {
        currentReportType = "LOW_STOCK";
        lblCurrentReportTitle.setText("Low Stock Report");
        resetTable();

        model.setColumnIdentifiers(new String[]{
            "Product ID", "Product Name", "Category", "Stock", "Reorder Level", "Status"
        });
        enhanceTable(tblReports, scrollReportsTable);

        String selectedCategory = getSelectedCategory();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.product_id, p.name AS product_name, c.name AS category_name, ");
        sql.append("p.stock_quantity, p.reorder_level, ");
        sql.append("CASE ");
        sql.append("WHEN p.stock_quantity <= 0 THEN 'Out of Stock' ");
        sql.append("ELSE 'Low Stock' ");
        sql.append("END AS stock_status ");
        sql.append("FROM products p ");
        sql.append("JOIN categories c ON p.category_id = c.category_id ");
        sql.append("WHERE p.stock_quantity <= p.reorder_level ");

        if (!"All Categories".equals(selectedCategory)) {
            sql.append("AND c.name = ? ");
        }

        sql.append("ORDER BY p.stock_quantity ASC, p.name ASC");

        try (Connection con = DBConnection.dbConnection(); PreparedStatement pst = con.prepareStatement(sql.toString())) {

            if (!"All Categories".equals(selectedCategory)) {
                pst.setString(1, selectedCategory);
            }

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("category_name"),
                        rs.getInt("stock_quantity"),
                        rs.getInt("reorder_level"),
                        rs.getString("stock_status")
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading low stock report: " + e.getMessage());
        }
    }

    private void loadProfitReport() {
        currentReportType = "PROFIT";
        lblCurrentReportTitle.setText("Profit Report");
        resetTable();

        model.setColumnIdentifiers(new String[]{"Date", "Category", "Sales", "Profit", "Status"});
        enhanceTable(tblReports, scrollReportsTable);

        String selectedCategory = getSelectedCategory();
        String startDate = getFormattedDate(dcStartDate.getDate());
        String endDate = getFormattedDate(dcEndDate.getDate());

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DATE(s.sale_date) AS report_date, ");
        sql.append("c.name AS category_name, ");
        sql.append("SUM(sd.subtotal) AS total_sales, ");
        sql.append("SUM((sd.price - p.cost_price) * sd.quantity - sd.discount) AS total_profit, ");
        sql.append("s.status ");
        sql.append("FROM sales s ");
        sql.append("JOIN sales_details sd ON s.sale_id = sd.sale_id ");
        sql.append("JOIN products p ON sd.product_id = p.product_id ");
        sql.append("JOIN categories c ON p.category_id = c.category_id ");
        sql.append("WHERE (s.status IS NULL OR s.status = 'COMPLETED' OR s.status = '') ");

        if (!"All Categories".equals(selectedCategory)) {
            sql.append("AND c.name = ? ");
        }
        if (startDate != null) {
            sql.append("AND DATE(s.sale_date) >= ? ");
        }
        if (endDate != null) {
            sql.append("AND DATE(s.sale_date) <= ? ");
        }

        sql.append("GROUP BY DATE(s.sale_date), c.name ");
        sql.append("ORDER BY DATE(s.sale_date) DESC, c.name ASC");

        try (Connection con = DBConnection.dbConnection(); PreparedStatement pst = con.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (!"All Categories".equals(selectedCategory)) {
                pst.setString(paramIndex++, selectedCategory);
            }
            if (startDate != null) {
                pst.setString(paramIndex++, startDate);
            }
            if (endDate != null) {
                pst.setString(paramIndex++, endDate);
            }

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("report_date"),
                        rs.getString("category_name"),
                        rs.getDouble("total_sales"),
                        rs.getDouble("total_profit"),
                        rs.getString("status")
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading profit report: " + e.getMessage());
        }
    }

    private void applyCurrentReport() {
        switch (currentReportType) {
            case "MONTHLY" ->
                loadMonthlySalesReport();
            case "INVENTORY" ->
                loadInventoryReport();
            case "LOW_STOCK" ->
                loadLowStockReport();
            case "PROFIT" ->
                loadProfitReport();
            default ->
                loadDailySalesReport();
        }
    }

    private void resetFilters() {
        if (cmbCategory.getItemCount() > 0) {
            cmbCategory.setSelectedIndex(0);
        }
        dcStartDate.setDate(null);
        dcEndDate.setDate(null);
    }

    private void exportTableToPDF() {
        if (tblReports.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("report.pdf"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".pdf")) {
                    file = new File(file.getAbsolutePath() + ".pdf");
                }

                PdfWriter writer = new PdfWriter(file);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                document.add(new Paragraph(lblCurrentReportTitle.getText()));
                document.add(new Paragraph("Generated: " + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a").format(new Date())));

                int columnCount = tblReports.getColumnCount();
                Table table = new Table(columnCount);

                for (int i = 0; i < columnCount; i++) {
                    table.addHeaderCell(tblReports.getColumnName(i));
                }

                for (int row = 0; row < tblReports.getRowCount(); row++) {
                    for (int col = 0; col < columnCount; col++) {
                        Object value = tblReports.getValueAt(row, col);
                        table.addCell(value == null ? "" : value.toString());
                    }
                }

                document.add(table);
                document.close();

                JOptionPane.showMessageDialog(this, "PDF exported successfully.");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error exporting PDF: " + e.getMessage());
            }
        }
    }

    private void exportTableToCSV() {
        if (tblReports.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Excel-Compatible CSV");
        chooser.setSelectedFile(new File("reports_export.csv"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }

        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(file))) {
            DefaultTableModel currentModel = (DefaultTableModel) tblReports.getModel();

            for (int col = 0; col < currentModel.getColumnCount(); col++) {
                writer.write(escapeCSV(currentModel.getColumnName(col)));
                if (col < currentModel.getColumnCount() - 1) {
                    writer.write(",");
                }
            }
            writer.newLine();

            for (int row = 0; row < currentModel.getRowCount(); row++) {
                for (int col = 0; col < currentModel.getColumnCount(); col++) {
                    Object value = currentModel.getValueAt(row, col);
                    writer.write(escapeCSV(value == null ? "" : value.toString()));
                    if (col < currentModel.getColumnCount() - 1) {
                        writer.write(",");
                    }
                }
                writer.newLine();
            }

            JOptionPane.showMessageDialog(this, "Report exported successfully.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error exporting report: " + e.getMessage());
        }
    }

    private String escapeCSV(String value) {
        String text = value == null ? "" : value;
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            text = "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblReportsModule = new point.of.sale.system.classes.GradientFont();
        lblSubTitle = new javax.swing.JLabel();
        pnlExportButtons = new RoundedPanel();
        btnExportPDF = new javax.swing.JButton();
        btnExportExcel = new javax.swing.JButton();
        lblCurrentReportTitle = new javax.swing.JLabel("Add New User") {
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
        btnRefresh = new javax.swing.JButton();
        jPanel1 = new RoundedPanel();
        scrollReportsTable = new javax.swing.JScrollPane();
        tblReports = new javax.swing.JTable();
        jPanel2 = new RoundedPanel();
        pnlFilters = new javax.swing.JPanel();
        lblCategory = new javax.swing.JLabel();
        lblStartDate = new javax.swing.JLabel();
        lblEndDate = new javax.swing.JLabel();
        cmbCategory = new javax.swing.JComboBox<>();
        dcStartDate = new com.toedter.calendar.JDateChooser();
        dcEndDate = new com.toedter.calendar.JDateChooser();
        btnApplyFilter = new javax.swing.JButton();
        btnResetFilter = new javax.swing.JButton();
        lblFilters = new javax.swing.JLabel();
        pnlDailySalesReport = new GlassPanel();
        logo = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        pnlMonthlySalesReport = new GlassPanel();
        logo1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        pnlInventoryReport = new GlassPanel();
        logo3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        pnlLowStockReport = new GlassPanel();
        logo2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        pnlProfitReport = new GlassPanel();
        logo4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();

        setBackground(new java.awt.Color(240, 248, 255));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblReportsModule.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        lblReportsModule.setText("REPORTS ");
        add(lblReportsModule, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 190, -1));

        lblSubTitle.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        lblSubTitle.setText("View different reports");
        add(lblSubTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, -1, -1));

        pnlExportButtons.setBackground(new java.awt.Color(18, 48, 174));
        pnlExportButtons.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnExportPDF.setText("Export to PDF");
        btnExportPDF.addActionListener(this::btnExportPDFActionPerformed);
        pnlExportButtons.add(btnExportPDF, new org.netbeans.lib.awtextra.AbsoluteConstraints(810, 20, -1, 30));

        btnExportExcel.setText("Export to Excel");
        btnExportExcel.addActionListener(this::btnExportExcelActionPerformed);
        pnlExportButtons.add(btnExportExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 20, -1, 30));

        lblCurrentReportTitle.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        lblCurrentReportTitle.setText("Daily Report");
        pnlExportButtons.add(lblCurrentReportTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 24, -1, 20));

        btnRefresh.setText("Refresh");
        pnlExportButtons.add(btnRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 20, 100, 30));

        add(pnlExportButtons, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 410, 1100, 70));

        jPanel1.setBackground(new java.awt.Color(122, 170, 206));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblReports.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        scrollReportsTable.setViewportView(tblReports);

        jPanel1.add(scrollReportsTable, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 1080, 310));

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 480, 1100, 330));

        jPanel2.setBackground(new java.awt.Color(122, 170, 206));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlFilters.setBackground(new java.awt.Color(255, 255, 255));
        pnlFilters.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblCategory.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        lblCategory.setForeground(new java.awt.Color(40, 55, 80));
        lblCategory.setText("Category");
        pnlFilters.add(lblCategory, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, -1, -1));

        lblStartDate.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        lblStartDate.setForeground(new java.awt.Color(40, 55, 80));
        lblStartDate.setText("Start Date");
        pnlFilters.add(lblStartDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 50, -1, -1));

        lblEndDate.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        lblEndDate.setForeground(new java.awt.Color(40, 55, 80));
        lblEndDate.setText("End Date");
        pnlFilters.add(lblEndDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 50, -1, -1));

        pnlFilters.add(cmbCategory, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 70, 160, 30));
        pnlFilters.add(dcStartDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 70, 180, 30));
        pnlFilters.add(dcEndDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 70, 180, 30));

        btnApplyFilter.setText("Apply Filter");
        btnApplyFilter.addActionListener(this::btnApplyFilterActionPerformed);
        pnlFilters.add(btnApplyFilter, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 30, -1, -1));

        btnResetFilter.setText("Reset Filter");
        btnResetFilter.addActionListener(this::btnResetFilterActionPerformed);
        pnlFilters.add(btnResetFilter, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 70, -1, -1));

        lblFilters.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        lblFilters.setText("Filters");
        pnlFilters.add(lblFilters, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        jPanel2.add(pnlFilters, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 1080, 130));

        add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 260, 1100, 150));

        pnlDailySalesReport.setBackground(new java.awt.Color(255, 255, 255));
        pnlDailySalesReport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlDailySalesReportMouseClicked(evt);
            }
        });
        pnlDailySalesReport.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        logo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/sales (1).png"))); // NOI18N
        pnlDailySalesReport.add(logo, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 40, 32, 32));

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Daily Sales Report");
        pnlDailySalesReport.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 90, 200, 30));

        add(pnlDailySalesReport, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, 205, 150));

        pnlMonthlySalesReport.setBackground(new java.awt.Color(255, 255, 255));
        pnlMonthlySalesReport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlMonthlySalesReportMouseClicked(evt);
            }
        });
        pnlMonthlySalesReport.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        logo1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logo1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/calendar.png"))); // NOI18N
        logo1.setVerifyInputWhenFocusTarget(false);
        pnlMonthlySalesReport.add(logo1, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 40, 32, 32));

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Monthly Sales Report");
        pnlMonthlySalesReport.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 90, 200, 30));

        add(pnlMonthlySalesReport, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 90, 205, 150));

        pnlInventoryReport.setBackground(new java.awt.Color(255, 255, 255));
        pnlInventoryReport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlInventoryReportMouseClicked(evt);
            }
        });
        pnlInventoryReport.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        logo3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logo3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/material-management.png"))); // NOI18N
        logo3.setVerifyInputWhenFocusTarget(false);
        pnlInventoryReport.add(logo3, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 40, 32, 32));

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Inventory Report");
        pnlInventoryReport.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 90, 200, 30));

        add(pnlInventoryReport, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 90, 205, 150));

        pnlLowStockReport.setBackground(new java.awt.Color(255, 255, 255));
        pnlLowStockReport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlLowStockReportMouseClicked(evt);
            }
        });
        pnlLowStockReport.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        logo2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logo2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/stock-market.png"))); // NOI18N
        logo2.setVerifyInputWhenFocusTarget(false);
        pnlLowStockReport.add(logo2, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 40, 32, 32));

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Low Stock Report");
        pnlLowStockReport.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 90, 200, 30));

        add(pnlLowStockReport, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 90, 205, 150));

        pnlProfitReport.setBackground(new java.awt.Color(255, 255, 255));
        pnlProfitReport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlProfitReportMouseClicked(evt);
            }
        });
        pnlProfitReport.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        logo4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logo4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/profit.png"))); // NOI18N
        logo4.setVerifyInputWhenFocusTarget(false);
        pnlProfitReport.add(logo4, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 40, 32, 32));

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Profit Report");
        pnlProfitReport.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 90, 200, 30));

        add(pnlProfitReport, new org.netbeans.lib.awtextra.AbsoluteConstraints(900, 90, 205, 150));

        jSeparator1.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator1.setForeground(new java.awt.Color(0, 0, 0));
        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 40, 20, 40));

        jSeparator3.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator3.setForeground(new java.awt.Color(0, 0, 0));
        add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 30, 890, 10));
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        if (btnRefresh != null) {
            animateRefreshButton();
        }
        applyCurrentReport();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void animateRefreshButton() {
        final int[] step = {0};
        Timer timer = new Timer(20, null);
        timer.addActionListener(e -> {
            step[0]++;
            float t = step[0] / 18f;
            int pad = (int) (Math.sin(t * Math.PI) * 4);
            btnRefresh.setBorder(new javax.swing.border.EmptyBorder(10 - pad, 18, 10 + pad, 18));
            btnRefresh.repaint();
            if (step[0] >= 18) {
                timer.stop();
                btnRefresh.setBorder(new javax.swing.border.EmptyBorder(10, 18, 10, 18));
                btnRefresh.repaint();
            }
        });
        timer.start();
    }

    private void btnApplyFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnApplyFilterActionPerformed
        applyCurrentReport();
    }//GEN-LAST:event_btnApplyFilterActionPerformed

    private void btnResetFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetFilterActionPerformed
        resetFilters();
        applyCurrentReport();

        cmbCategory.setSelectedIndex(0);
        dcStartDate.setDate(null);
        dcEndDate.setDate(null);
        refreshReports();
    }//GEN-LAST:event_btnResetFilterActionPerformed

    private void btnExportPDFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportPDFActionPerformed
        exportTableToPDF();
    }//GEN-LAST:event_btnExportPDFActionPerformed

    private void btnExportExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportExcelActionPerformed
        exportTableToCSV();
    }//GEN-LAST:event_btnExportExcelActionPerformed

    private void pnlDailySalesReportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlDailySalesReportMouseClicked
        activateReportCard(pnlDailySalesReport);
        loadDailySalesReport();
    }//GEN-LAST:event_pnlDailySalesReportMouseClicked

    private void pnlMonthlySalesReportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlMonthlySalesReportMouseClicked
        activateReportCard(pnlMonthlySalesReport);
        loadMonthlySalesReport();
    }//GEN-LAST:event_pnlMonthlySalesReportMouseClicked

    private void pnlInventoryReportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlInventoryReportMouseClicked
        activateReportCard(pnlInventoryReport);
        loadInventoryReport();
    }//GEN-LAST:event_pnlInventoryReportMouseClicked

    private void pnlLowStockReportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlLowStockReportMouseClicked
        activateReportCard(pnlLowStockReport);
        loadLowStockReport();
    }//GEN-LAST:event_pnlLowStockReportMouseClicked

    private void pnlProfitReportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlProfitReportMouseClicked
        activateReportCard(pnlProfitReport);
        loadProfitReport();
    }//GEN-LAST:event_pnlProfitReportMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnApplyFilter;
    private javax.swing.JButton btnExportExcel;
    private javax.swing.JButton btnExportPDF;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnResetFilter;
    private javax.swing.JComboBox<String> cmbCategory;
    private com.toedter.calendar.JDateChooser dcEndDate;
    private com.toedter.calendar.JDateChooser dcStartDate;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel lblCategory;
    private javax.swing.JLabel lblCurrentReportTitle;
    private javax.swing.JLabel lblEndDate;
    private javax.swing.JLabel lblFilters;
    private javax.swing.JLabel lblReportsModule;
    private javax.swing.JLabel lblStartDate;
    private javax.swing.JLabel lblSubTitle;
    private javax.swing.JLabel logo;
    private javax.swing.JLabel logo1;
    private javax.swing.JLabel logo2;
    private javax.swing.JLabel logo3;
    private javax.swing.JLabel logo4;
    private javax.swing.JPanel pnlDailySalesReport;
    private javax.swing.JPanel pnlExportButtons;
    private javax.swing.JPanel pnlFilters;
    private javax.swing.JPanel pnlInventoryReport;
    private javax.swing.JPanel pnlLowStockReport;
    private javax.swing.JPanel pnlMonthlySalesReport;
    private javax.swing.JPanel pnlProfitReport;
    private javax.swing.JScrollPane scrollReportsTable;
    private javax.swing.JTable tblReports;
    // End of variables declaration//GEN-END:variables
}
