package point.of.sale.system.screens;

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
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import point.of.sale.system.classes.DBConnection;
import point.of.sale.system.classes.RoundedPanel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ProductsManagementPanel extends javax.swing.JPanel {

    private static final java.util.logging.Logger logger
            = java.util.logging.Logger.getLogger(ProductsManagementPanel.class.getName());

    private Integer selectedProductId = null;

    private final Map<String, Integer> categoryNameToId = new LinkedHashMap<String, Integer>();
    private final Map<String, Integer> supplierNameToId = new LinkedHashMap<String, Integer>();

    private TableRowSorter<DefaultTableModel> sorter;

    private final DecimalFormat moneyFormat;
    private boolean isFormattingCost = false;
    private boolean isFormattingSelling = false;

    private int hoveredTableRow = -1;

    private String selectedProductImageBase64 = null;
    private static final int IMAGE_PREVIEW_WIDTH = 300;
    private static final int IMAGE_PREVIEW_HEIGHT = 170;

    public ProductsManagementPanel() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        moneyFormat = new DecimalFormat("#,##0.00", symbols);

        initComponents();

        applyModernUI();
        installValidationFilters();

        spinnerReorderLevel.setModel(new SpinnerNumberModel(0, 0, 999999, 1));

        setupTableSorter();
        initFilterCombos();
        setButtonsDefaultState();
        hookTableSelection();
        hookClearButton();
        setupPriceFieldFormatting();
        setupPriceColumnsRenderer();

        loadCategoriesToCombos();
        loadSuppliersToCombos();
        loadProducts(null);
        applyTableFilters();
        initializeSearchListener();

        barcode.setEditable(false);
        setupImagePreviewPlaceholder();

    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(
                0, 0, new Color(243, 248, 255),
                getWidth(), getHeight(), new Color(228, 239, 255)
        );

        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();

        super.paintComponent(g);
    }

    private void applyModernUI() {
        setOpaque(false);

        styleTextField(barcode);
        styleTextField(txtProductName);
        styleTextField(txtCostPrice);
        styleTextField(txtSellingPrice);
        styleTextField(txtStockQuantity);
        styleTextField(txtSearch);

        styleComboBox(cmbCategory);
        styleComboBox(cmbSupplier);
        styleComboBox(filterbycategory);
        styleComboBox(cmbStockStatus);

        styleButton(btnAdd, new Color(34, 166, 82));
        styleButton(btnEdit, new Color(243, 156, 18));
        styleButton(btnUpdate, new Color(52, 120, 246));
        styleButton(btnDelete, new Color(220, 53, 69));
        styleButton(btnGenerateBarcode, new Color(89, 92, 255));
        styleButton(btnRefresh, new Color(89, 92, 255));
        styleButton(btnUploadImage, new Color(111, 66, 193));

        installRoundedButtonPainter(btnAdd);
        installRoundedButtonPainter(btnEdit);
        installRoundedButtonPainter(btnUpdate);
        installRoundedButtonPainter(btnDelete);
        installRoundedButtonPainter(btnGenerateBarcode);
        installRoundedButtonPainter(btnRefresh);
        installRoundedButtonPainter(btnUploadImage);

        enhanceTable(tblProductList, jScrollPane2);

        if (jLabel2 != null) {
            jLabel2.setForeground(new Color(26, 45, 84));
        }
        if (jLabel16 != null) {
            jLabel16.setForeground(new Color(88, 105, 136));
        }

        pnlInputs.setBackground(new Color(132, 164, 224));
        pnlTABLE.setBackground(new Color(132, 164, 224));
        jPanel7.setBackground(new Color(18, 48, 174));
        jPanel4.setBackground(new Color(18, 48, 174));

        styleImagePreview();
    }

    private void installValidationFilters() {
        txtProductName.addKeyListener(new KeyAdapter() {
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
                        || c == '.'
                        || c == '/';

                if (!allowed || txtProductName.getText().length() >= 120) {
                    e.consume();
                }
            }
        });

        txtStockQuantity.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (Character.isISOControl(c)) {
                    return;
                }

                if (!Character.isDigit(c) || txtStockQuantity.getText().length() >= 9) {
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

    private BigDecimal parseMoney(String text) {
    if (text == null) {
        return null;
    }

    text = text.replace("₱", "")
               .replace(",", "")
               .trim();

    if (text.isEmpty()) {
        return null;
    }

    try {
        return new BigDecimal(text);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isDuplicateBarcode(String barcodeValue, Integer excludeProductId) {
        String sql = "SELECT product_id FROM products WHERE barcode = ?";
        if (excludeProductId != null) {
            sql += " AND product_id != ?";
        }

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, barcodeValue);

            if (excludeProductId != null) {
                ps.setInt(2, excludeProductId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Validation error: " + e.getMessage());
            return true;
        }
    }

    private boolean validateProductForm() {
        String productName = normalizeSpaces(txtProductName.getText());
        String barcodeValue = barcode.getText().trim();
        String stockQtyText = txtStockQuantity.getText().trim();

        BigDecimal costPrice = parseMoney(txtCostPrice.getText());
        BigDecimal sellingPrice = parseMoney(txtSellingPrice.getText());

        txtProductName.setText(productName);

        if (barcodeValue.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Barcode is required. Please generate barcode first.");
            return false;
        }

        if (productName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Product name is required.");
            txtProductName.requestFocus();
            return false;
        }

        if (!containsLetter(productName)) {
            JOptionPane.showMessageDialog(this, "Product name must contain at least one letter.");
            txtProductName.requestFocus();
            return false;
        }

        if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "Cost price must be a valid number greater than 0.");
            txtCostPrice.requestFocus();
            return false;
        }

        if (sellingPrice == null || sellingPrice.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "Selling price must be a valid number greater than 0.");
            txtSellingPrice.requestFocus();
            return false;
        }

        if (sellingPrice.compareTo(costPrice) < 0) {
            JOptionPane.showMessageDialog(this, "Selling price must not be lower than cost price.");
            txtSellingPrice.requestFocus();
            return false;
        }

        if (stockQtyText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Stock quantity is required.");
            txtStockQuantity.requestFocus();
            return false;
        }

        int stockQty;
        try {
            stockQty = Integer.parseInt(stockQtyText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Stock quantity must be a whole number.");
            txtStockQuantity.requestFocus();
            return false;
        }

        if (stockQty < 0) {
            JOptionPane.showMessageDialog(this, "Stock quantity must not be negative.");
            txtStockQuantity.requestFocus();
            return false;
        }

        int reorderLevel = (Integer) spinnerReorderLevel.getValue();
        if (reorderLevel < 0) {
            JOptionPane.showMessageDialog(this, "Reorder level must not be negative.");
            spinnerReorderLevel.requestFocus();
            return false;
        }

        if (cmbCategory.getSelectedItem() == null || cmbCategory.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Please select a category.");
            cmbCategory.requestFocus();
            return false;
        }

        if ("Select Category".equals(cmbCategory.getSelectedItem().toString())) {
            JOptionPane.showMessageDialog(this, "Please select a valid category.");
            cmbCategory.requestFocus();
            return false;
        }

        if (cmbSupplier.getSelectedItem() == null || cmbSupplier.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Please select a supplier.");
            cmbSupplier.requestFocus();
            return false;
        }

        if ("Select Supplier".equals(cmbSupplier.getSelectedItem().toString())) {
            JOptionPane.showMessageDialog(this, "Please select a valid supplier.");
            cmbSupplier.requestFocus();
            return false;
        }

        String selectedCategoryName = cmbCategory.getSelectedItem().toString();
        Integer categoryId = categoryNameToId.get(selectedCategoryName);

        if (categoryId == null) {
            JOptionPane.showMessageDialog(this, "Invalid category selected.");
            cmbCategory.requestFocus();
            return false;
        }

        if (isDuplicateProductNameInCategory(productName, categoryId, selectedProductId)) {
            JOptionPane.showMessageDialog(this, "A product with the same name already exists in this category.");
            txtProductName.requestFocus();
            return false;
        }

        if (isDuplicateBarcode(barcodeValue, selectedProductId)) {
            JOptionPane.showMessageDialog(this, "Barcode already exists.");
            return false;
        }

        return true;
    }

    private void styleImagePreview() {
        if (productImage == null) {
            return;
        }

        productImage.setOpaque(true);
        productImage.setBackground(new Color(245, 249, 255));
        productImage.setForeground(new Color(90, 105, 130));
        productImage.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(214, 225, 242), 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));
        productImage.setHorizontalAlignment(SwingConstants.CENTER);
        productImage.setVerticalAlignment(SwingConstants.CENTER);
    }

    private void setupImagePreviewPlaceholder() {
        showImagePreview(null);
    }

    private void showImagePreview(String base64) {
        try {
            if (base64 == null || base64.trim().isEmpty()) {
                productImage.setIcon(null);
                productImage.setText("<html><center>No Image<br>Selected</center></html>");
                return;
            }

            byte[] bytes = Base64.getDecoder().decode(base64);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));

            if (image == null) {
                productImage.setIcon(null);
                productImage.setText("<html><center>Invalid Image</center></html>");
                return;
            }

            Image scaled = image.getScaledInstance(
                    IMAGE_PREVIEW_WIDTH - 20,
                    IMAGE_PREVIEW_HEIGHT - 20,
                    Image.SCALE_SMOOTH
            );

            productImage.setText("");
            productImage.setIcon(new ImageIcon(scaled));

        } catch (Exception ex) {
            productImage.setIcon(null);
            productImage.setText("<html><center>Image Preview<br>Unavailable</center></html>");
        }
    }

    private String encodeImageFileToBase64(File file) throws Exception {
        BufferedImage original = ImageIO.read(file);
        if (original == null) {
            throw new Exception("Selected file is not a valid image.");
        }

        BufferedImage normalized = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2 = normalized.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, normalized.getWidth(), normalized.getHeight());
        g2.drawImage(original, 0, 0, null);
        g2.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(normalized, "jpg", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private String getProductImageById(int productId) {
        String sql = "SELECT product_image FROM products WHERE product_id = ? LIMIT 1";

        try (Connection conn = DBConnection.dbConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("product_image");
                }
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Load product image failed", ex);
        }

        return null;
    }

    private void styleTextField(JTextField field) {
        if (field == null) {
            return;
        }

        field.setFont(new Font("Tahoma", Font.PLAIN, 13));
        field.setBackground(Color.WHITE);
        field.setForeground(new Color(40, 52, 71));
        field.setCaretColor(new Color(89, 92, 255));
        field.setMargin(new Insets(1, 8, 1, 8));

        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(214, 225, 242), 1, true),
                new EmptyBorder(2, 8, 2, 8)
        ));
    }

    private void styleComboBox(JComboBox<?> combo) {
        if (combo == null) {
            return;
        }

        combo.setFont(new Font("Tahoma", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.setForeground(new Color(40, 52, 71));
        combo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(214, 225, 242), 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
        combo.setFocusable(false);
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

        for (java.awt.event.MouseListener ml : button.getMouseListeners()) {
            button.removeMouseListener(ml);
        }

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

    public void refreshProducts() {
        loadCategoriesToCombos();
        loadSuppliersToCombos();
        loadProducts(null);
        applyTableFilters();
    }

    private void setupTableSorter() {
        DefaultTableModel model = (DefaultTableModel) tblProductList.getModel();
        sorter = new TableRowSorter<DefaultTableModel>(model);
        tblProductList.setRowSorter(sorter);
        sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(0, javax.swing.SortOrder.ASCENDING)));
        sorter.sort();
    }

    private void enhanceTable(JTable table, JScrollPane scrollPane) {
        Font bodyFont = new Font("Tahoma", Font.PLAIN, 13);
        Font headerFont = new Font("Tahoma", Font.BOLD, 13);

        Color panelBg = new Color(241, 247, 253);
        Color cardBg = new Color(250, 253, 255);
        Color tableBg = new Color(255, 255, 255);
        Color stripeBg = new Color(246, 250, 254);
        Color hoverBg = new Color(235, 244, 255);
        Color headerBg = new Color(40, 56, 145);
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
        table.setRowHeight(38);
        table.setShowGrid(true);
        table.setGridColor(mutedGrid);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFocusable(false);
        table.setBackground(tableBg);
        table.setForeground(textColor);
        table.setSelectionBackground(selectBg);
        table.setSelectionForeground(selectFg);
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(headerFont);
        header.setPreferredSize(new Dimension(header.getWidth(), 46));
        header.setReorderingAllowed(false);
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
                label.setBorder(new EmptyBorder(10, 10, 10, 10));
                return label;
            }
        });

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(new EmptyBorder(8, 12, 8, 12));
                label.setFont(bodyFont);

                if (isSelected) {
                    label.setBackground(selectBg);
                    label.setForeground(selectFg);
                } else if (row == hoveredTableRow) {
                    label.setBackground(hoverBg);
                    label.setForeground(textColor);
                } else {
                    label.setBackground(row % 2 == 0 ? tableBg : stripeBg);
                    label.setForeground(textColor);
                }

                return label;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        if (table.getColumnCount() >= 9) {
            table.getColumnModel().getColumn(0).setPreferredWidth(60);
            table.getColumnModel().getColumn(1).setPreferredWidth(170);
            table.getColumnModel().getColumn(2).setPreferredWidth(230);
            table.getColumnModel().getColumn(3).setPreferredWidth(150);
            table.getColumnModel().getColumn(4).setPreferredWidth(160);
            table.getColumnModel().getColumn(5).setPreferredWidth(130);
            table.getColumnModel().getColumn(6).setPreferredWidth(140);
            table.getColumnModel().getColumn(7).setPreferredWidth(130);
            table.getColumnModel().getColumn(8).setPreferredWidth(130);
        }

        scrollPane.setVerticalScrollBar(new ModernScrollBar());
        scrollPane.setHorizontalScrollBar(new ModernScrollBar());

        wrapScrollPaneInCard(scrollPane);

        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredTableRow) {
                    hoveredTableRow = row;
                    table.repaint();
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredTableRow = -1;
                table.repaint();
            }
        });
    }

    private void wrapScrollPaneInCard(JScrollPane scrollPane) {
        Container parent = scrollPane.getParent();
        if (parent instanceof JViewport) {
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

    private void hookTableSelection() {
        tblProductList.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            storeSelectedProductIdOnly();
        });
    }

    private void hookClearButton() {
        if (btnRefresh != null) {
            btnRefresh.addActionListener(e -> {
                animateRefreshButton();
                clearForm();
                refreshProducts();
            });
        }
    }

    private void animateRefreshButton() {
        final int[] step = {0};

        Timer timer = new Timer(20, null);
        timer.addActionListener(e -> {
            step[0]++;

            float t = step[0] / 18f;
            int pad = (int) (Math.sin(t * Math.PI) * 4);

            btnRefresh.setBorder(new EmptyBorder(10 - pad, 18, 10 + pad, 18));
            btnRefresh.repaint();

            if (step[0] >= 18) {
                timer.stop();
                btnRefresh.setBorder(new EmptyBorder(10, 18, 10, 18));
                btnRefresh.repaint();
            }
        });
        timer.start();
    }

    private void initFilterCombos() {
        filterbycategory.removeAllItems();
        filterbycategory.addItem("All");

        cmbStockStatus.removeAllItems();
        cmbStockStatus.addItem("All");
        cmbStockStatus.addItem("In Stock");
        cmbStockStatus.addItem("Low Stock");
        cmbStockStatus.addItem("Out of Stock");

        cmbCategory.removeAllItems();
        cmbSupplier.removeAllItems();
    }

    private void setButtonsDefaultState() {
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);
        btnUpdate.setEnabled(false);
    }

    private void clearForm() {
        selectedProductId = null;
        selectedProductImageBase64 = null;

        barcode.setText("");
        txtProductName.setText("");
        txtCostPrice.setText("0.00");
        txtSellingPrice.setText("0.00");
        txtStockQuantity.setText("");
        spinnerReorderLevel.setValue(0);

        if (cmbCategory.getItemCount() > 0) {
            cmbCategory.setSelectedIndex(0);
        }
        if (cmbSupplier.getItemCount() > 0) {
            cmbSupplier.setSelectedIndex(0);
        }

        showImagePreview(null);

        tblProductList.clearSelection();
        setButtonsDefaultState();
        txtProductName.requestFocus();
    }

    private void loadCategoriesToCombos() {
        categoryNameToId.clear();
        cmbCategory.removeAllItems();

        filterbycategory.removeAllItems();
        filterbycategory.addItem("All");

        cmbCategory.addItem("Select Category");

        String sql = "SELECT category_id, name FROM categories ORDER BY name ASC";

        try (Connection conn = DBConnection.dbConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("category_id");
                String name = rs.getString("name");

                categoryNameToId.put(name, id);
                cmbCategory.addItem(name);
                filterbycategory.addItem(name);
            }

        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Load categories failed", ex);
            JOptionPane.showMessageDialog(this, "Failed to load categories.\n" + ex.getMessage());
        }
    }

    private void loadSuppliersToCombos() {
        supplierNameToId.clear();
        cmbSupplier.removeAllItems();

        cmbSupplier.addItem("Select Supplier");

        String sql = "SELECT supplier_id, supplier_name FROM suppliers ORDER BY supplier_name ASC";

        try (Connection conn = DBConnection.dbConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("supplier_id");
                String name = rs.getString("supplier_name");

                supplierNameToId.put(name, id);
                cmbSupplier.addItem(name);
            }

        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Load suppliers failed", ex);
            JOptionPane.showMessageDialog(this, "Failed to load suppliers.\n" + ex.getMessage());
        }
    }

    private void loadProducts(String keyword) {
        DefaultTableModel model = (DefaultTableModel) tblProductList.getModel();
        model.setRowCount(0);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.product_id, p.barcode, p.name, ")
                .append("c.name AS category_name, s.supplier_name AS supplier_name, ")
                .append("p.cost_price, p.selling_price, p.stock_quantity, p.reorder_level ")
                .append("FROM products p ")
                .append("LEFT JOIN categories c ON p.category_id = c.category_id ")
                .append("LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id ")
                .append("WHERE 1=1 ");

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        if (hasKeyword) {
            sql.append("AND (p.barcode LIKE ? OR p.name LIKE ? OR c.name LIKE ? OR s.supplier_name LIKE ?) ");
        }

        sql.append("ORDER BY p.product_id ASC");

        try (Connection conn = DBConnection.dbConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (hasKeyword) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
                ps.setString(4, like);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[]{
                        rs.getInt("product_id"),
                        rs.getString("barcode"),
                        rs.getString("name"),
                        rs.getString("category_name"),
                        rs.getString("supplier_name"),
                        rs.getBigDecimal("cost_price"),
                        rs.getBigDecimal("selling_price"),
                        rs.getInt("stock_quantity"),
                        rs.getInt("reorder_level")
                    };
                    model.addRow(row);
                }
            }

        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Load products failed", ex);
            JOptionPane.showMessageDialog(this, "Failed to load products.\n" + ex.getMessage());
        }

        applyTableFilters();
    }

    private void applyTableFilters() {
        if (sorter == null) {
            return;
        }

        final String selectedCat = (String) filterbycategory.getSelectedItem();
        final String selectedStatus = (String) cmbStockStatus.getSelectedItem();

        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String catName = safe(entry.getStringValue(3));
                int stockQty = safeInt(entry.getStringValue(7));
                int reorder = safeInt(entry.getStringValue(8));

                if (selectedCat != null && !"All".equalsIgnoreCase(selectedCat)) {
                    if (!catName.equalsIgnoreCase(selectedCat)) {
                        return false;
                    }
                }

                if (selectedStatus != null && !"All".equalsIgnoreCase(selectedStatus)) {
                    String status = computeStockStatus(stockQty, reorder);
                    if (!status.equalsIgnoreCase(selectedStatus)) {
                        return false;
                    }
                }

                return true;
            }
        });
    }

    private String computeStockStatus(int stockQty, int reorderLevel) {
        if (stockQty <= 0) {
            return "Out of Stock";
        }
        if (stockQty <= reorderLevel) {
            return "Low Stock";
        }
        return "In Stock";
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private int safeInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private int getSpinnerIntValue() {
        Object value = spinnerReorderLevel.getValue();
        if (value instanceof Integer) {
            return ((Integer) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }

    private String generateUniqueBarcodeText() {
        return "P" + System.currentTimeMillis();
    }

    private boolean barcodeExists(String code) throws SQLException {
        String sql = "SELECT 1 FROM products WHERE TRIM(barcode) = TRIM(?) LIMIT 1";

        try (Connection conn = DBConnection.dbConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code.trim());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Integer getProductIdByBarcode(String code) throws SQLException {
        String sql = "SELECT product_id FROM products WHERE TRIM(barcode) = TRIM(?) LIMIT 1";

        try (Connection conn = DBConnection.dbConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Integer.valueOf(rs.getInt("product_id"));
                }
                return null;
            }
        }
    }

    private int getSelectedCategoryId() {
        String name = (String) cmbCategory.getSelectedItem();
        Integer id = categoryNameToId.get(name);
        return id == null ? 0 : id.intValue();
    }

    private int getSelectedSupplierId() {
        String name = (String) cmbSupplier.getSelectedItem();
        Integer id = supplierNameToId.get(name);
        return id == null ? 0 : id.intValue();
    }

    private boolean validateRequiredFields() {
        String b = barcode.getText() == null ? "" : barcode.getText().trim();
        String name = txtProductName.getText() == null ? "" : txtProductName.getText().trim();

        if (b.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Barcode is required.");
            return false;
        }
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Product name is required.");
            return false;
        }
        if (cmbCategory.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Category is required.");
            return false;
        }
        if (cmbSupplier.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Supplier is required.");
            return false;
        }

        try {
            BigDecimal cost = parseMoneyToBigDecimal(txtCostPrice.getText());
            BigDecimal sell = parseMoneyToBigDecimal(txtSellingPrice.getText());
            int stock = Integer.parseInt(txtStockQuantity.getText().trim());
            int reorder = getSpinnerIntValue();

            if (cost.compareTo(BigDecimal.ZERO) < 0 || sell.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "Prices must not be negative.");
                return false;
            }
            if (stock < 0 || reorder < 0) {
                JOptionPane.showMessageDialog(this, "Stock and reorder level must not be negative.");
                return false;
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for prices and stock.");
            return false;
        }

        return true;
    }

    private void insertProduct() {
        if (!validateRequiredFields()) {
            return;
        }

        String code = barcode.getText().trim();

        try {
            if (barcodeExists(code)) {
                JOptionPane.showMessageDialog(this, "Duplicate barcode detected!");
                return;
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Barcode check failed", ex);
            JOptionPane.showMessageDialog(this, "Database error while checking barcode.\n" + ex.getMessage());
            return;
        }

        String sql = "INSERT INTO products "
                + "(barcode, name, category_id, supplier_id, cost_price, selling_price, stock_quantity, reorder_level, product_image) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.dbConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            ps.setString(2, txtProductName.getText().trim());
            ps.setInt(3, getSelectedCategoryId());
            ps.setInt(4, getSelectedSupplierId());
            ps.setBigDecimal(5, parseMoneyToBigDecimal(txtCostPrice.getText()));
            ps.setBigDecimal(6, parseMoneyToBigDecimal(txtSellingPrice.getText()));
            ps.setInt(7, Integer.parseInt(txtStockQuantity.getText().trim()));
            ps.setInt(8, getSpinnerIntValue());
            ps.setString(9, selectedProductImageBase64);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Product saved successfully!");
            clearForm();
            refreshProducts();

        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Insert product failed", ex);
            JOptionPane.showMessageDialog(this, "Failed to save product.\n" + ex.getMessage());
        }
    }

    private void updateProduct() {
        if (selectedProductId == null) {
            JOptionPane.showMessageDialog(this, "Please select a product first.");
            return;
        }

        if (!validateRequiredFields()) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Update this product?",
                "Confirm Update",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String code = barcode.getText().trim();

        try {
            Integer ownerId = getProductIdByBarcode(code);

            if (ownerId != null && !ownerId.equals(selectedProductId)) {
                JOptionPane.showMessageDialog(this, "Duplicate barcode detected!");
                return;
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Barcode check failed", ex);
            JOptionPane.showMessageDialog(this, "Database error while checking barcode.\n" + ex.getMessage());
            return;
        }

        String sql = "UPDATE products SET "
                + "barcode=?, name=?, category_id=?, supplier_id=?, cost_price=?, selling_price=?, stock_quantity=?, reorder_level=?, product_image=? "
                + "WHERE product_id=?";

        try (Connection conn = DBConnection.dbConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            ps.setString(2, txtProductName.getText().trim());
            ps.setInt(3, getSelectedCategoryId());
            ps.setInt(4, getSelectedSupplierId());
            ps.setBigDecimal(5, parseMoneyToBigDecimal(txtCostPrice.getText()));
            ps.setBigDecimal(6, parseMoneyToBigDecimal(txtSellingPrice.getText()));
            ps.setInt(7, Integer.parseInt(txtStockQuantity.getText().trim()));
            ps.setInt(8, getSpinnerIntValue());
            ps.setString(9, selectedProductImageBase64);
            ps.setInt(10, selectedProductId.intValue());

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Product updated successfully!");
            clearForm();
            refreshProducts();

        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Update product failed", ex);
            JOptionPane.showMessageDialog(this, "Failed to update product.\n" + ex.getMessage());
        }
    }

    private void deleteProduct() {
        if (selectedProductId == null) {
            JOptionPane.showMessageDialog(this, "Please select a product first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete selected product?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM products WHERE product_id=?";

        try (Connection conn = DBConnection.dbConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, selectedProductId.intValue());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Product deleted successfully!");
            clearForm();
            refreshProducts();

        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Delete product failed", ex);
            JOptionPane.showMessageDialog(this, "Failed to delete product.\n" + ex.getMessage());
        }
    }

    private void storeSelectedProductIdOnly() {
        int viewRow = tblProductList.getSelectedRow();
        if (viewRow < 0) {
            selectedProductId = null;
            setButtonsDefaultState();
            return;
        }

        int modelRow = tblProductList.convertRowIndexToModel(viewRow);
        DefaultTableModel model = (DefaultTableModel) tblProductList.getModel();

        Object idObj = model.getValueAt(modelRow, 0);
        selectedProductId = (idObj == null) ? null : Integer.valueOf(Integer.parseInt(idObj.toString()));

        btnEdit.setEnabled(true);
        btnDelete.setEnabled(true);
        btnUpdate.setEnabled(false);
    }

    private void fillFormFromSelectedRow() {
        if (selectedProductId == null) {
            JOptionPane.showMessageDialog(this, "Please select a product first.");
            return;
        }

        int viewRow = tblProductList.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product first.");
            return;
        }

        int modelRow = tblProductList.convertRowIndexToModel(viewRow);
        DefaultTableModel model = (DefaultTableModel) tblProductList.getModel();

        barcode.setText(String.valueOf(model.getValueAt(modelRow, 1)));
        txtProductName.setText(String.valueOf(model.getValueAt(modelRow, 2)));

        String catName = String.valueOf(model.getValueAt(modelRow, 3));
        String supName = String.valueOf(model.getValueAt(modelRow, 4));

        cmbCategory.setSelectedItem(catName);
        cmbSupplier.setSelectedItem(supName);

        Object costObj = model.getValueAt(modelRow, 5);
        Object sellObj = model.getValueAt(modelRow, 6);

        txtCostPrice.setText(formatPeso(costObj));
        txtSellingPrice.setText(formatPeso(sellObj));
        txtStockQuantity.setText(String.valueOf(model.getValueAt(modelRow, 7)));

        Object reorderObj = model.getValueAt(modelRow, 8);
        int reorderLevel = 0;
        if (reorderObj != null) {
            try {
                reorderLevel = Integer.parseInt(reorderObj.toString());
            } catch (NumberFormatException ex) {
                reorderLevel = 0;
            }
        }
        spinnerReorderLevel.setValue(Integer.valueOf(reorderLevel));

        selectedProductImageBase64 = getProductImageById(selectedProductId.intValue());
        showImagePreview(selectedProductImageBase64);
    }

    private void initializeSearchListener() {
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchProducts();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchProducts();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchProducts();
            }
        });
    }

    private void searchProducts() {
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim();

        if (keyword.isEmpty()) {
            loadProducts(null);
        } else {
            loadProducts(keyword);
        }
    }

    private void setupPriceFieldFormatting() {
        installPesoFieldBehavior(txtCostPrice, true);
        installPesoFieldBehavior(txtSellingPrice, false);

        txtCostPrice.setText("₱0.00");
        txtSellingPrice.setText("₱0.00");
    }

    private void installPesoFieldBehavior(JTextField field, boolean isCostField) {
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                try {
                    BigDecimal value = parseMoneyToBigDecimal(field.getText());
                    field.setText(value.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString());
                    field.selectAll();
                } catch (Exception ex) {
                    field.setText("0.00");
                    field.selectAll();
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                formatPriceField(field, isCostField);
            }
        });

        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    private void formatPriceField(JTextField field, boolean isCostField) {
        if (isCostField && isFormattingCost) {
            return;
        }
        if (!isCostField && isFormattingSelling) {
            return;
        }

        try {
            if (isCostField) {
                isFormattingCost = true;
            } else {
                isFormattingSelling = true;
            }

            BigDecimal value = parseMoneyToBigDecimal(field.getText());
            field.setText("₱" + moneyFormat.format(value));

        } catch (Exception ex) {
            field.setText("₱0.00");
        } finally {
            if (isCostField) {
                isFormattingCost = false;
            } else {
                isFormattingSelling = false;
            }
        }
    }

    private BigDecimal parseMoneyToBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        String cleaned = value.toString()
                .replace("₱", "")
                .replace(",", "")
                .trim();

        if (cleaned.isEmpty() || ".".equals(cleaned)) {
            return BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        return new BigDecimal(cleaned).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private String formatPeso(Object value) {
        try {
            BigDecimal bd = parseMoneyToBigDecimal(value);
            return "₱" + moneyFormat.format(bd);
        } catch (Exception e) {
            return "₱0.00";
        }
    }

    private void setupPriceColumnsRenderer() {
        DefaultTableCellRenderer priceRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (value == null) {
                    setText("₱0.00");
                    return;
                }

                try {
                    BigDecimal amount = parseMoneyToBigDecimal(value);
                    setText("₱" + moneyFormat.format(amount));
                } catch (Exception ex) {
                    setText("₱0.00");
                }
            }
        };

        tblProductList.getColumnModel().getColumn(5).setCellRenderer(priceRenderer);
        tblProductList.getColumnModel().getColumn(6).setCellRenderer(priceRenderer);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlInputs = new RoundedPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtProductName = new javax.swing.JTextField();
        txtCostPrice = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtSellingPrice = new javax.swing.JTextField();
        txtStockQuantity = new javax.swing.JTextField();
        cmbCategory = new javax.swing.JComboBox<>();
        cmbSupplier = new javax.swing.JComboBox<>();
        barcode = new javax.swing.JTextField();
        spinnerReorderLevel = new javax.swing.JSpinner();
        btnGenerateBarcode = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        productImage = new javax.swing.JLabel();
        btnUploadImage = new javax.swing.JButton();
        pnlTABLE = new RoundedPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblProductList = new javax.swing.JTable();
        jPanel7 = new RoundedPanel();
        lblAddNewProduct = new javax.swing.JLabel("Add New User") {
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
        jLabel16 = new javax.swing.JLabel();
        jPanel4 = new RoundedPanel();
        jLabel10 = new javax.swing.JLabel("Add New User") {
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
        btnEdit = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        filterbycategory = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        cmbStockStatus = new javax.swing.JComboBox<>();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator1 = new javax.swing.JSeparator();
        btnAdd = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();

        setBackground(new java.awt.Color(240, 248, 255));
        setPreferredSize(new java.awt.Dimension(1120, 820));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlInputs.setBackground(new java.awt.Color(122, 170, 206));
        pnlInputs.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(40, 55, 80));
        jLabel3.setText("Product Barcode:");
        pnlInputs.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(40, 55, 80));
        jLabel4.setText("Product Name:");
        pnlInputs.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 20, -1, -1));

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(40, 55, 80));
        jLabel6.setText("Category:");
        pnlInputs.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 20, -1, -1));

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(40, 55, 80));
        jLabel7.setText("Supplier:");
        pnlInputs.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, -1, -1));

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(40, 55, 80));
        jLabel8.setText("Cost Price:");
        pnlInputs.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 110, -1, -1));

        txtProductName.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        pnlInputs.add(txtProductName, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 40, 220, 40));

        txtCostPrice.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        pnlInputs.add(txtCostPrice, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 130, 220, 40));

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(40, 55, 80));
        jLabel1.setText("Selling Price:");
        pnlInputs.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 110, -1, -1));

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(40, 55, 80));
        jLabel5.setText("Stock Quantity:");
        pnlInputs.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 190, -1, -1));

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(40, 55, 80));
        jLabel9.setText("Reorder Level:");
        pnlInputs.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 190, -1, -1));

        txtSellingPrice.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        pnlInputs.add(txtSellingPrice, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 130, 220, 40));

        txtStockQuantity.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        pnlInputs.add(txtStockQuantity, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, 220, 40));

        cmbCategory.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        pnlInputs.add(cmbCategory, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 40, 220, 40));

        cmbSupplier.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        pnlInputs.add(cmbSupplier, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, 220, 40));

        barcode.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        barcode.addActionListener(this::barcodeActionPerformed);
        pnlInputs.add(barcode, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 220, 40));

        spinnerReorderLevel.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        pnlInputs.add(spinnerReorderLevel, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 210, 180, 40));

        btnGenerateBarcode.setText("Generate Barcode");
        btnGenerateBarcode.addActionListener(this::btnGenerateBarcodeActionPerformed);
        pnlInputs.add(btnGenerateBarcode, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 80, 130, 30));

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(40, 55, 80));
        jLabel13.setText("Product Image");
        pnlInputs.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 20, -1, -1));

        productImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        productImage.setText("prod image");
        pnlInputs.add(productImage, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 50, 290, 170));

        btnUploadImage.setText("Upload Image");
        btnUploadImage.addActionListener(this::btnUploadImageActionPerformed);
        pnlInputs.add(btnUploadImage, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 220, -1, 30));

        add(pnlInputs, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 140, 1100, 270));

        pnlTABLE.setBackground(new java.awt.Color(122, 170, 206));
        pnlTABLE.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblProductList.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        tblProductList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Barcode", "Product Name", "Category", "Supplier", "Cost Price", "Selling Price", "Stock Quantity", "Reorder Level"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblProductList.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(tblProductList);
        if (tblProductList.getColumnModel().getColumnCount() > 0) {
            tblProductList.getColumnModel().getColumn(0).setResizable(false);
            tblProductList.getColumnModel().getColumn(0).setPreferredWidth(30);
            tblProductList.getColumnModel().getColumn(1).setResizable(false);
            tblProductList.getColumnModel().getColumn(1).setPreferredWidth(200);
            tblProductList.getColumnModel().getColumn(2).setResizable(false);
            tblProductList.getColumnModel().getColumn(2).setPreferredWidth(200);
            tblProductList.getColumnModel().getColumn(3).setResizable(false);
            tblProductList.getColumnModel().getColumn(3).setPreferredWidth(200);
            tblProductList.getColumnModel().getColumn(4).setResizable(false);
            tblProductList.getColumnModel().getColumn(4).setPreferredWidth(190);
            tblProductList.getColumnModel().getColumn(5).setResizable(false);
            tblProductList.getColumnModel().getColumn(5).setPreferredWidth(150);
            tblProductList.getColumnModel().getColumn(6).setResizable(false);
            tblProductList.getColumnModel().getColumn(6).setPreferredWidth(150);
            tblProductList.getColumnModel().getColumn(7).setResizable(false);
            tblProductList.getColumnModel().getColumn(7).setPreferredWidth(170);
            tblProductList.getColumnModel().getColumn(8).setResizable(false);
            tblProductList.getColumnModel().getColumn(8).setPreferredWidth(140);
        }

        pnlTABLE.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(8, 10, 1080, 320));

        add(pnlTABLE, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 470, 1100, 340));

        jPanel7.setBackground(new java.awt.Color(18, 48, 174));
        jPanel7.setMinimumSize(new java.awt.Dimension(206, 630));
        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblAddNewProduct.setFont(new java.awt.Font("Tahoma", 1, 22)); // NOI18N
        lblAddNewProduct.setForeground(new java.awt.Color(255, 255, 255));
        lblAddNewProduct.setText("Add New Product");
        jPanel7.add(lblAddNewProduct, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, -1, -1));

        add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, 340, 50));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel2.setText("PRODUCTS MANAGEMENT");
        add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 490, -1));

        jLabel16.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel16.setText("Manage your product inventory");
        add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, -1, -1));

        jPanel4.setBackground(new java.awt.Color(18, 48, 174));
        jPanel4.setMinimumSize(new java.awt.Dimension(206, 630));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 22)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("PRODUCTS LIST");
        jPanel4.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 190, 30));

        btnEdit.setBackground(new java.awt.Color(243, 156, 18));
        btnEdit.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnEdit.setForeground(new java.awt.Color(255, 255, 255));
        btnEdit.setText("Edit");
        btnEdit.addActionListener(this::btnEditActionPerformed);
        jPanel4.add(btnEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(990, 20, 80, 30));

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Search:");
        jPanel4.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 23, -1, -1));

        txtSearch.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        txtSearch.setMinimumSize(new java.awt.Dimension(64, 22));
        txtSearch.addActionListener(this::txtSearchActionPerformed);
        jPanel4.add(txtSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 20, 170, 27));

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText("Filter by Category:");
        jPanel4.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 23, -1, -1));

        filterbycategory.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        filterbycategory.addActionListener(this::filterbycategoryActionPerformed);
        jPanel4.add(filterbycategory, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 20, 110, -1));

        jLabel15.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setText("Stock Status: ");
        jPanel4.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 23, -1, -1));

        cmbStockStatus.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cmbStockStatus.addActionListener(this::cmbStockStatusActionPerformed);
        jPanel4.add(cmbStockStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 20, 120, -1));

        add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 410, 1100, 70));

        jSeparator3.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator3.setForeground(new java.awt.Color(0, 0, 0));
        add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 30, 590, 10));

        jSeparator1.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator1.setForeground(new java.awt.Color(0, 0, 0));
        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 40, 20, 90));

        btnAdd.setBackground(new java.awt.Color(0, 166, 37));
        btnAdd.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnAdd.setForeground(new java.awt.Color(255, 255, 255));
        btnAdd.setText("Save");
        btnAdd.addActionListener(this::btnAddActionPerformed);
        add(btnAdd, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 90, 80, 30));

        btnUpdate.setBackground(new java.awt.Color(0, 98, 193));
        btnUpdate.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnUpdate.setForeground(new java.awt.Color(255, 255, 255));
        btnUpdate.setText("Update ");
        btnUpdate.addActionListener(this::btnUpdateActionPerformed);
        add(btnUpdate, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 90, 90, 30));

        btnDelete.setBackground(new java.awt.Color(204, 0, 0));
        btnDelete.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnDelete.setForeground(new java.awt.Color(255, 255, 255));
        btnDelete.setText("Delete ");
        btnDelete.addActionListener(this::btnDeleteActionPerformed);
        add(btnDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 90, 90, 30));

        btnRefresh.setBackground(new java.awt.Color(44, 62, 80));
        btnRefresh.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnRefresh.setForeground(new java.awt.Color(255, 255, 255));
        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);
        add(btnRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(990, 90, 90, 30));
    }// </editor-fold>//GEN-END:initComponents

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        searchProducts();
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        storeSelectedProductIdOnly();
        fillFormFromSelectedRow();
        btnUpdate.setEnabled(true);
    }//GEN-LAST:event_btnEditActionPerformed

    private void cmbStockStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbStockStatusActionPerformed
        applyTableFilters();
    }//GEN-LAST:event_cmbStockStatusActionPerformed

    private void filterbycategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterbycategoryActionPerformed
        applyTableFilters();
    }//GEN-LAST:event_filterbycategoryActionPerformed

    private boolean isDuplicateProductNameInCategory(String productName, int categoryId, Integer excludeProductId) {
        String sql = "SELECT product_id FROM products WHERE LOWER(TRIM(name)) = LOWER(TRIM(?)) AND category_id = ?";
        if (excludeProductId != null) {
            sql += " AND product_id != ?";
        }

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, productName);
            ps.setInt(2, categoryId);

            if (excludeProductId != null) {
                ps.setInt(3, excludeProductId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Validation error: " + e.getMessage());
            return true;
        }
    }

    private void btnGenerateBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateBarcodeActionPerformed
        String newBarcode = generateUniqueBarcodeText();

        try {
            while (barcodeExists(newBarcode)) {
                newBarcode = generateUniqueBarcodeText();
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Barcode duplicate check failed", ex);
            JOptionPane.showMessageDialog(this, "Database error while checking barcode.\n" + ex.getMessage());
            return;
        }

        barcode.setText(newBarcode);
    }//GEN-LAST:event_btnGenerateBarcodeActionPerformed

    private void barcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_barcodeActionPerformed

    }//GEN-LAST:event_barcodeActionPerformed

    private boolean isProductUsedInSales(int productId) {
        String sql = "SELECT 1 FROM sales_details WHERE product_id = ? LIMIT 1";

        try (Connection conn = DBConnection.dbConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Validation failed", ex);
            JOptionPane.showMessageDialog(this, "Validation error.\n" + ex.getMessage());
            return true;
        }
    }

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        if (selectedProductId == null) {
            JOptionPane.showMessageDialog(this, "Select a product first.");
            return;
        }

        if (isProductUsedInSales(selectedProductId)) {
            JOptionPane.showMessageDialog(this, "This product cannot be deleted because it already exists in sales records.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete this product?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM products WHERE product_id = ?";

        try (Connection conn = DBConnection.dbConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, selectedProductId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Product deleted successfully.");

            clearForm();
            refreshProducts();

        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Delete product failed", ex);
            JOptionPane.showMessageDialog(this, "Failed to delete product.\n" + ex.getMessage());
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        if (selectedProductId == null) {
            JOptionPane.showMessageDialog(this, "Select a product first.");
            return;
        }

        if (!validateProductForm()) {
            return;
        }

        String barcodeValue = barcode.getText().trim();
        String productName = normalizeSpaces(txtProductName.getText());
        BigDecimal costPrice = parseMoney(txtCostPrice.getText());
        BigDecimal sellingPrice = parseMoney(txtSellingPrice.getText());
        int stockQuantity = Integer.parseInt(txtStockQuantity.getText().trim());
        int reorderLevel = getSpinnerIntValue();

        String selectedCategoryName = cmbCategory.getSelectedItem().toString();
        String selectedSupplierName = cmbSupplier.getSelectedItem().toString();

        Integer categoryId = categoryNameToId.get(selectedCategoryName);
        Integer supplierId = supplierNameToId.get(selectedSupplierName);

        if (categoryId == null || supplierId == null) {
            JOptionPane.showMessageDialog(this, "Invalid category or supplier selected.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Update this product?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "UPDATE products SET barcode=?, name=?, category_id=?, supplier_id=?, cost_price=?, "
                + "selling_price=?, stock_quantity=?, reorder_level=?, product_image=? "
                + "WHERE product_id=?";

        try (Connection conn = DBConnection.dbConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, barcodeValue);
            ps.setString(2, productName);
            ps.setInt(3, categoryId);
            ps.setInt(4, supplierId);
            ps.setBigDecimal(5, costPrice);
            ps.setBigDecimal(6, sellingPrice);
            ps.setInt(7, stockQuantity);
            ps.setInt(8, reorderLevel);
            ps.setString(9, selectedProductImageBase64);
            ps.setInt(10, selectedProductId);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Product updated successfully.");

            clearForm();
            refreshProducts();

        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Update product failed", ex);
            JOptionPane.showMessageDialog(this, "Failed to update product.\n" + ex.getMessage());
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        if (!validateProductForm()) {
            return;
        }

        String barcodeValue = barcode.getText().trim();
        String productName = normalizeSpaces(txtProductName.getText());
        BigDecimal costPrice = parseMoney(txtCostPrice.getText());
        BigDecimal sellingPrice = parseMoney(txtSellingPrice.getText());
        int stockQuantity = Integer.parseInt(txtStockQuantity.getText().trim());
        int reorderLevel = getSpinnerIntValue();

        String selectedCategoryName = cmbCategory.getSelectedItem().toString();
        String selectedSupplierName = cmbSupplier.getSelectedItem().toString();

        Integer categoryId = categoryNameToId.get(selectedCategoryName);
        Integer supplierId = supplierNameToId.get(selectedSupplierName);

        if (categoryId == null || supplierId == null) {
            JOptionPane.showMessageDialog(this, "Invalid category or supplier selected.");
            return;
        }

        String sql = "INSERT INTO products (barcode, name, category_id, supplier_id, cost_price, selling_price, stock_quantity, reorder_level, product_image) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.dbConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, barcodeValue);
            ps.setString(2, productName);
            ps.setInt(3, categoryId);
            ps.setInt(4, supplierId);
            ps.setBigDecimal(5, costPrice);
            ps.setBigDecimal(6, sellingPrice);
            ps.setInt(7, stockQuantity);
            ps.setInt(8, reorderLevel);
            ps.setString(9, selectedProductImageBase64);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Product added successfully.");

            clearForm();
            refreshProducts();

        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Add product failed", ex);
            JOptionPane.showMessageDialog(this, "Failed to add product.\n" + ex.getMessage());
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        clearForm();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnUploadImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUploadImageActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Product Image");

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        try {
            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType == null || !mimeType.startsWith("image")) {
                JOptionPane.showMessageDialog(this, "Please select a valid image file.");
                return;
            }

            selectedProductImageBase64 = encodeImageFileToBase64(file);
            showImagePreview(selectedProductImageBase64);
            JOptionPane.showMessageDialog(this, "Image loaded successfully. Save or update the product to keep it.");

        } catch (Exception ex) {
            logger.log(java.util.logging.Level.SEVERE, "Upload image failed", ex);
            JOptionPane.showMessageDialog(this, "Failed to load image.\n" + ex.getMessage());
        }
    }//GEN-LAST:event_btnUploadImageActionPerformed
    private static class ShadowPanel extends JPanel {

        public ShadowPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int shadowGap = 8;
            int arc = 26;
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField barcode;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnGenerateBarcode;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JButton btnUploadImage;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JComboBox<String> cmbStockStatus;
    private javax.swing.JComboBox<String> cmbSupplier;
    private javax.swing.JComboBox<String> filterbycategory;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel lblAddNewProduct;
    private javax.swing.JPanel pnlInputs;
    private javax.swing.JPanel pnlTABLE;
    private javax.swing.JLabel productImage;
    private javax.swing.JSpinner spinnerReorderLevel;
    private javax.swing.JTable tblProductList;
    private javax.swing.JTextField txtCostPrice;
    private javax.swing.JTextField txtProductName;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtSellingPrice;
    private javax.swing.JTextField txtStockQuantity;
    // End of variables declaration//GEN-END:variables
}
