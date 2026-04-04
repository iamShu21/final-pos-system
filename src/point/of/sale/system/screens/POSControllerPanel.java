package point.of.sale.system.screens;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.property.UnitValue;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import point.of.sale.system.classes.DBConnection;
import point.of.sale.system.classes.RoundedPanel;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.JTableHeader;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class POSControllerPanel extends javax.swing.JPanel {

    private Timer dateTimeTimer;
    private String loggedInName;
    private String loggedInRole;
    private DefaultTableModel cartModel;
    private final DecimalFormat moneyFormat;

    private ReportsPanel reportsPanel;
    private SalesHistoryPanel salesHistoryPanel;
    private int loggedInUserId;

    private JPopupMenu cartPopupMenu;
    private DashboardPanel dashboardPanel;

    private int hoveredCartRow = -1;
    private boolean isFormattingCashTendered = false;

    private static final int PREVIEW_IMAGE_W = 150;
    private static final int PREVIEW_IMAGE_H = 110;

    private JPopupMenu productSuggestionPopup;
    private javax.swing.JList<String> suggestionList;
    private final List<ProductData> currentSuggestions = new ArrayList<ProductData>();
    private Timer searchDelay;

    public POSControllerPanel(int loggedInUserId, String loggedInName, String loggedInRole) {
        this.loggedInUserId = loggedInUserId;
        this.loggedInName = loggedInName;
        this.loggedInRole = loggedInRole;

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        moneyFormat = new DecimalFormat("#,##0.00", symbols);

        initComponents();

        applyModernUI();
        startDateTimeTimer();
        setAutomatedUserInfo();
        generateInvoiceNumber();
        setupCartTable();
        setupCashTenderedField();
        setupBarcodeSuggestions();
        initializeValues();
        bindAutomaticEvents();
        setupCartInteractions();
        setupPreviewPanel();
    }

    public POSControllerPanel() {
        this(0, "Name", "Cashier");
    }

    public void setLinkedPanels(DashboardPanel dashboardPanel, ReportsPanel reportsPanel, SalesHistoryPanel salesHistoryPanel) {
        this.dashboardPanel = dashboardPanel;
        this.reportsPanel = reportsPanel;
        this.salesHistoryPanel = salesHistoryPanel;
    }

    private void refreshLinkedPanels() {
        if (dashboardPanel != null) {
            dashboardPanel.loadDashboardData();
        }

        if (reportsPanel != null) {
            reportsPanel.refreshReports();
        }

        if (salesHistoryPanel != null) {
            salesHistoryPanel.refreshSalesHistoryRealtime();
        }
    }

    private void startDateTimeTimer() {
        final SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy - hh:mm:ss a");
        dateTimeTimer = new Timer(1000, (java.awt.event.ActionEvent e) -> {
            lblDateTime.setText(sdf.format(new Date()));
        });
        dateTimeTimer.start();
    }

    private void setupBarcodeSuggestions() {
        productSuggestionPopup = new JPopupMenu();
        productSuggestionPopup.setBorder(BorderFactory.createEmptyBorder());
        productSuggestionPopup.setFocusable(false);

        suggestionList = new javax.swing.JList<String>();
        suggestionList.setFont(new Font("Tahoma", Font.PLAIN, 13));
        suggestionList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setFixedCellHeight(30);
        suggestionList.setBackground(Color.WHITE);
        suggestionList.setForeground(new Color(35, 48, 68));
        suggestionList.setSelectionBackground(new Color(214, 231, 250));
        suggestionList.setSelectionForeground(new Color(20, 43, 67));
        suggestionList.setBorder(BorderFactory.createEmptyBorder());

        JScrollPane scroll = new JScrollPane(suggestionList);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 214, 235), 1, true),
                new EmptyBorder(2, 2, 2, 2)
        ));
        scroll.setViewportBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBar(new ModernScrollBar());

        productSuggestionPopup.add(scroll);

        barcodeInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                loadBarcodeSuggestions();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                loadBarcodeSuggestions();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                loadBarcodeSuggestions();
            }
        });

        barcodeInput.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!productSuggestionPopup.isVisible()) {
                    return;
                }

                int index = suggestionList.getSelectedIndex();

                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (index < suggestionList.getModel().getSize() - 1) {
                        suggestionList.setSelectedIndex(index + 1);
                        suggestionList.ensureIndexIsVisible(index + 1);
                    }
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (index > 0) {
                        suggestionList.setSelectedIndex(index - 1);
                        suggestionList.ensureIndexIsVisible(index - 1);
                    }
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    selectSuggestedProduct();
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    productSuggestionPopup.setVisible(false);
                }
            }
        });

        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectSuggestedProduct();
                }
            }
        });
    }

    private void applyNumericFilter(JTextField field, boolean allowDecimal) {
        if (field == null) {
            return;
        }

        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string == null) {
                    return;
                }

                String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                String next = new StringBuilder(current).insert(offset, string).toString();

                if (isValidNumericText(next, allowDecimal)) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                String next = new StringBuilder(current).replace(offset, offset + length, text == null ? "" : text).toString();

                if (isValidNumericText(next, allowDecimal)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                super.remove(fb, offset, length);
            }
        });
    }

    private boolean isValidNumericText(String text, boolean allowDecimal) {
        if (text == null || text.isEmpty()) {
            return true;
        }

        if (allowDecimal) {
            return text.matches("\\d{0,9}(\\.\\d{0,2})?");
        }

        return text.matches("\\d{0,9}");
    }

    private void setupCashTenderedField() {
        cashTendered.setHorizontalAlignment(JTextField.RIGHT);
        cashTendered.setFont(new Font("Tahoma", Font.PLAIN, 16));
        cashTendered.setText("0.00");

        applyNumericFilter(cashTendered, true);

        cashTendered.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                String raw = cashTendered.getText() == null ? "" : cashTendered.getText().trim();
                if (raw.startsWith("₱")) {
                    raw = raw.substring(1).replace(",", "").trim();
                }
                cashTendered.setText(raw);
                cashTendered.selectAll();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                double value = parseDouble(cashTendered.getText());
                cashTendered.setText(formatPlainMoney(value));
                updateChangeValue();
            }
        });

        cashTendered.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateChangeValue();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateChangeValue();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateChangeValue();
            }
        });
    }

    private String formatPlainMoney(double value) {
        return moneyFormat.format(value);
    }

    private void updateChangeValue() {
        double tendered = parseDouble(cashTendered.getText());
        double total = parseDouble(totalValue.getText());

        double change = tendered - total;
        if (change < 0) {
            change = 0;
        }

        changeValueAutomated.setText(formatMoneyWithPeso(change));
    }

    private void loadBarcodeSuggestions() {
        if (searchDelay != null && searchDelay.isRunning()) {
            searchDelay.stop();
        }

        searchDelay = new Timer(220, e -> {
            String keyword = barcodeInput.getText() == null ? "" : barcodeInput.getText().trim();

            if (keyword.length() < 1) {
                productSuggestionPopup.setVisible(false);
                currentSuggestions.clear();
                return;
            }

            List<ProductData> matches = findProductsByBarcodeOrName(keyword);

            if (matches.isEmpty()) {
                productSuggestionPopup.setVisible(false);
                currentSuggestions.clear();
                return;
            }

            currentSuggestions.clear();
            currentSuggestions.addAll(matches);

            javax.swing.DefaultListModel<String> model = new javax.swing.DefaultListModel<>();
            for (ProductData p : matches) {
                model.addElement(p.productName + "   |   " + p.barcode + "   |   " + formatMoneyWithPeso(p.sellingPrice));
            }

            suggestionList.setModel(model);
            suggestionList.setSelectedIndex(0);

            int visibleRows = Math.min(matches.size(), 6);
            int popupHeight = (visibleRows * 30) + 8;
            int popupWidth = Math.max(barcodeInput.getWidth(), 440);

            Component comp = productSuggestionPopup.getComponent(0);
            comp.setPreferredSize(new Dimension(popupWidth, popupHeight));
            comp.setMinimumSize(new Dimension(popupWidth, popupHeight));

            productSuggestionPopup.pack();
            productSuggestionPopup.show(barcodeInput, 0, barcodeInput.getHeight() + 2);
            barcodeInput.requestFocusInWindow();
        });

        searchDelay.setRepeats(false);
        searchDelay.start();
    }

    private void selectSuggestedProduct() {
        int index = suggestionList.getSelectedIndex();
        if (index < 0 || index >= currentSuggestions.size()) {
            return;
        }

        ProductData selected = currentSuggestions.get(index);

        // show ONLY product name in the text field
        barcodeInput.setText(selected.productName);

        productSuggestionPopup.setVisible(false);
        handleAddToCartByBarcode();
    }

    private void applyModernUI() {
        setBackground(new Color(240, 246, 255));
        setOpaque(true);

        Color sectionBlue = new Color(132, 164, 224);
        Color deepBlue = new Color(18, 48, 174);

        topPanel.setBackground(deepBlue);
        pnlCart.setBackground(deepBlue);

        pnlProductEntry.setBackground(sectionBlue);
        pnlClickToProductPreviewProduct.setBackground(sectionBlue);
        pnlComputation.setBackground(sectionBlue);
        pnlPaymentMethod.setBackground(sectionBlue);
        pnlTableWrap.setBackground(sectionBlue);

        pnlProductEntry.setBorder(BorderFactory.createEmptyBorder());
        pnlClickToProductPreviewProduct.setBorder(BorderFactory.createEmptyBorder());
        pnlComputation.setBorder(BorderFactory.createEmptyBorder());
        pnlPaymentMethod.setBorder(BorderFactory.createEmptyBorder());
        pnlTableWrap.setBorder(BorderFactory.createEmptyBorder());
        pnlCart.setBorder(BorderFactory.createEmptyBorder());

        styleTextField(barcodeInput);
        styleTextField(cashTendered);

        styleButton(btnAddToCart, new Color(34, 166, 82));
        styleButton(btnPrintReceipt, new Color(52, 120, 246));
        styleButton(btnPDF, new Color(111, 66, 193));

        installRoundedButtonPainter(btnAddToCart);
        installRoundedButtonPainter(btnPrintReceipt);
        installRoundedButtonPainter(btnPDF);

        styleGlassPaymentButton(btnCashPayment, new Color(46, 174, 79));
        styleGlassPaymentButton(btnGcashEwalletPayment, new Color(0, 176, 116));
        styleGlassPaymentButton(btnCardPayment, new Color(245, 158, 11));

        installGlassPaymentButtonPainter(btnCashPayment);
        installGlassPaymentButtonPainter(btnGcashEwalletPayment);
        installGlassPaymentButtonPainter(btnCardPayment);

        automatedNoOfItems.setFont(new Font("Tahoma", Font.BOLD, 16));

        subtotalValue.setText("₱0.00");
        discoutValue.setText("₱0.00");
        vatValue.setText("₱0.00");
        totalValue.setText("₱0.00");
        changeValueAutomated.setText("₱0.00");

        enhanceTable(tblCart, jScrollPane1);
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

        for (java.awt.event.FocusListener fl : field.getFocusListeners()) {
            field.removeFocusListener(fl);
        }

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(89, 92, 255), 2, true),
                        new EmptyBorder(1, 7, 1, 7)
                ));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(200, 214, 235), 1, true),
                        new EmptyBorder(2, 8, 2, 8)
                ));
            }
        });
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
        button.setBorder(new EmptyBorder(6, 14, 6, 14));
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

        if (!button.isEnabled()) {
            button.putClientProperty("btnColor", disabledBg);
            button.setForeground(disabledFg);
            button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
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
                g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 16, 16);

                g2.setColor(new Color(255, 255, 255, 60));
                g2.drawRoundRect(0, 0, b.getWidth() - 1, b.getHeight() - 1, 16, 16);

                g2.setFont(b.getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (b.getWidth() - fm.stringWidth(b.getText())) / 2;
                int y = ((b.getHeight() - fm.getHeight()) / 2) + fm.getAscent();

                g2.setColor(fg);
                g2.drawString(b.getText(), x, y);
                g2.dispose();
            }
        });
    }

    private void styleGlassPaymentButton(JButton button, Color baseColor) {
        if (button == null) {
            return;
        }

        Color hoverColor = new Color(
                Math.min(baseColor.getRed() + 18, 255),
                Math.min(baseColor.getGreen() + 18, 255),
                Math.min(baseColor.getBlue() + 18, 255)
        );

        Color pressColor = new Color(
                Math.max(baseColor.getRed() - 18, 0),
                Math.max(baseColor.getGreen() - 18, 0),
                Math.max(baseColor.getBlue() - 18, 0)
        );

        Color disabledBg = new Color(200, 210, 225);
        Color disabledFg = new Color(120, 130, 150);

        button.setFont(new Font("Tahoma", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 14, 10, 14));
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

        if (!button.isEnabled()) {
            button.putClientProperty("btnColor", disabledBg);
            button.setForeground(disabledFg);
            button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private void installGlassPaymentButtonPainter(JButton button) {
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
                    fg = Color.WHITE;
                    if (bg == null) {
                        bg = b.getBackground();
                    }
                }

                int w = b.getWidth();
                int h = b.getHeight();

                Color top = new Color(
                        Math.min(bg.getRed() + 35, 255),
                        Math.min(bg.getGreen() + 35, 255),
                        Math.min(bg.getBlue() + 35, 255)
                );
                Color bottom = new Color(
                        Math.max(bg.getRed() - 12, 0),
                        Math.max(bg.getGreen() - 12, 0),
                        Math.max(bg.getBlue() - 12, 0)
                );

                g2.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
                g2.fillRoundRect(0, 0, w, h, 22, 22);

                g2.setPaint(new GradientPaint(
                        0, 0, new Color(255, 255, 255, 145),
                        0, h / 2, new Color(255, 255, 255, 18)
                ));
                g2.fillRoundRect(2, 2, w - 4, Math.max(10, h / 2), 20, 20);

                g2.setColor(new Color(255, 255, 255, 95));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 22, 22);

                g2.setColor(new Color(255, 255, 255, 40));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, 20, 20);

                g2.setFont(new Font("Tahoma", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int x = (w - fm.stringWidth(b.getText())) / 2;
                int y = ((h - fm.getHeight()) / 2) + fm.getAscent();

                g2.setColor(fg);
                g2.drawString(b.getText(), x, y);
                g2.dispose();
            }
        });
    }

    private void setAutomatedUserInfo() {
        cashierAutomatedName.setText(loggedInName);
        automatedRole.setText(loggedInRole);
    }

    private void generateInvoiceNumber() {
        String datePart = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        int randomPart = new Random().nextInt(900) + 100;
        invoiceAutoGenerated.setText("INV-" + datePart + "-" + randomPart);
    }

    private void setupCartTable() {
        cartModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Product ID", "Product Name", "Quantity", "Price", "Discount", "Subtotal"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0, 2 ->
                        Integer.class;
                    case 3, 4, 5 ->
                        Double.class;
                    default ->
                        String.class;
                };
            }
        };

        tblCart.setModel(cartModel);
        tblCart.setRowHeight(40);
        tblCart.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        tblCart.getColumnModel().getColumn(0).setMinWidth(0);
        tblCart.getColumnModel().getColumn(0).setMaxWidth(0);
        tblCart.getColumnModel().getColumn(0).setPreferredWidth(0);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer priceRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                label.setHorizontalAlignment(SwingConstants.RIGHT);
                label.setText(formatMoneyWithPeso(parseDouble(value)));
                label.setBorder(new EmptyBorder(8, 12, 8, 14));

                if (isSelected) {
                    label.setBackground(new Color(214, 231, 250));
                    label.setForeground(new Color(20, 43, 67));
                } else if (row == hoveredCartRow) {
                    label.setBackground(new Color(235, 244, 255));
                    label.setForeground(new Color(32, 48, 70));
                } else {
                    label.setBackground(row % 2 == 0 ? new Color(255, 255, 255) : new Color(246, 250, 254));
                    label.setForeground(new Color(32, 48, 70));
                }

                return label;
            }
        };

        tblCart.getColumnModel().getColumn(1).setPreferredWidth(280);
        tblCart.getColumnModel().getColumn(2).setPreferredWidth(90);
        tblCart.getColumnModel().getColumn(3).setPreferredWidth(110);
        tblCart.getColumnModel().getColumn(4).setPreferredWidth(110);
        tblCart.getColumnModel().getColumn(5).setPreferredWidth(120);

        tblCart.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tblCart.getColumnModel().getColumn(3).setCellRenderer(priceRenderer);
        tblCart.getColumnModel().getColumn(4).setCellRenderer(priceRenderer);
        tblCart.getColumnModel().getColumn(5).setCellRenderer(priceRenderer);
    }

    private void enhanceTable(JTable table, JScrollPane scrollPane) {
        Font bodyFont = new Font("Tahoma", Font.PLAIN, 14);
        Font headerFont = new Font("Tahoma", Font.BOLD, 14);

        Color panelBg = new Color(241, 247, 253);
        Color cardBg = new Color(250, 253, 255);
        Color tableBg = new Color(255, 255, 255);
        Color stripeBg = new Color(246, 250, 254);
        Color hoverBg = new Color(235, 244, 255);
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
        header.setResizingAllowed(false);
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

        DefaultTableCellRenderer bodyRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                label.setOpaque(true);
                label.setFont(bodyFont);
                label.setBorder(new EmptyBorder(8, 14, 8, 14));
                label.setHorizontalAlignment(column == 1 ? SwingConstants.LEFT : SwingConstants.CENTER);

                if (isSelected) {
                    label.setBackground(selectBg);
                    label.setForeground(selectFg);
                } else if (row == hoveredCartRow) {
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
            if (i != 3 && i != 4 && i != 5) {
                table.getColumnModel().getColumn(i).setCellRenderer(bodyRenderer);
            }
        }

        scrollPane.setVerticalScrollBar(new ModernScrollBar());
        scrollPane.setHorizontalScrollBar(new ModernScrollBar());

        wrapScrollPaneInCard(scrollPane);

        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredCartRow) {
                    hoveredCartRow = row;
                    table.repaint();
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredCartRow = -1;
                table.repaint();
            }
        });
    }

    private void wrapScrollPaneInCard(JScrollPane scrollPane) {
        Container parent = scrollPane.getParent();
        if (parent != null && parent instanceof JViewport) {
            parent = parent.getParent();
        }

        if (parent != null && !(parent instanceof ShadowPanel)) {
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

    private void formatCashTenderedField() {
        if (isFormattingCashTendered) {
            return;
        }

        try {
            isFormattingCashTendered = true;
            double value = parseDouble(cashTendered.getText());
            cashTendered.setText(formatMoneyWithPeso(value));
        } catch (Exception ex) {
            cashTendered.setText("₱0.00");
        } finally {
            isFormattingCashTendered = false;
        }
    }

    private void initializeValues() {
        subtotalValue.setText("₱0.00");
        discoutValue.setText("₱0.00");
        vatValue.setText("₱0.00");
        totalValue.setText("₱0.00");
        changeValueAutomated.setText("₱0.00");
        automatedNoOfItems.setText("0");
        cashTendered.setText("₱0.00");
    }

    private void bindAutomaticEvents() {
        cartModel.addTableModelListener((TableModelEvent e) -> {
            updateComputation();
            updateItemCount();
        });
    }

    private void setupCartInteractions() {
        tblCart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblCart.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    tblCart.setRowSelectionInterval(row, row);
                }

                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    editSelectedItemQuantity();
                }
            }
        });

        tblCart.getInputMap(JTable.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "removeItem");
        tblCart.getActionMap().put("removeItem", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedItem();
            }
        });

        cartPopupMenu = new JPopupMenu();

        JMenuItem editQtyItem = new JMenuItem("Edit Quantity");
        editQtyItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editSelectedItemQuantity();
            }
        });

        JMenuItem removeItem = new JMenuItem("Remove Item");
        removeItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedItem();
            }
        });

        cartPopupMenu.add(editQtyItem);
        cartPopupMenu.add(removeItem);

        tblCart.setComponentPopupMenu(cartPopupMenu);

        tblCart.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handlePopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handlePopup(e);
            }

            private void handlePopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = tblCart.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        tblCart.setRowSelectionInterval(row, row);
                    }
                }
            }
        });
    }

    private void setupPreviewPanel() {
        pnlClickToProductPreviewProduct.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pnlClickToProductPreviewProduct.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                pnlClickToProductPreviewProduct.setBackground(new Color(141, 170, 224));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                pnlClickToProductPreviewProduct.setBackground(new Color(132, 164, 224));
            }
        });
    }

    private void updateComputation() {
        double gross = 0.0;
        double discount = 0.0;

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            int qty = Integer.parseInt(cartModel.getValueAt(i, 2).toString());
            double price = parseDouble(cartModel.getValueAt(i, 3));
            double rowDiscount = parseDouble(cartModel.getValueAt(i, 4));

            gross += qty * price;
            discount += rowDiscount;
        }

        double netSales = gross - discount;
        if (netSales < 0) {
            netSales = 0;
        }

        double vatableSales = netSales / 1.12;
        double vat = netSales - vatableSales;
        double total = netSales;

        subtotalValue.setText(formatMoneyWithPeso(gross));
        discoutValue.setText(formatMoneyWithPeso(discount));
        vatValue.setText(formatMoneyWithPeso(vat));
        totalValue.setText(formatMoneyWithPeso(total));

        updateChange();
    }

    private void updateChange() {
        double total = parseDouble(totalValue.getText());
        double tendered = parseDouble(cashTendered.getText());
        double change = tendered - total;

        if (change < 0) {
            changeValueAutomated.setText("₱0.00");
        } else {
            changeValueAutomated.setText(formatMoneyWithPeso(change));
        }
    }

    private void updateItemCount() {
        int totalItems = 0;

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            totalItems += ((Integer) cartModel.getValueAt(i, 2)).intValue();
        }

        automatedNoOfItems.setText(String.valueOf(totalItems));
    }

    private String formatMoney(double value) {
        return moneyFormat.format(value);
    }

    private String formatMoneyWithPeso(double value) {
        return "₱" + moneyFormat.format(value);
    }

    private double parseDouble(Object value) {
        if (value == null) {
            return 0.0;
        }

        try {
            String cleaned = value.toString()
                    .replace(",", "")
                    .replace("₱", "")
                    .trim();

            if (cleaned.length() == 0) {
                return 0.0;
            }

            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void handleAddToCartByBarcode() {
        String keyword = barcodeInput.getText().trim();

        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter or select a product first.");
            barcodeInput.requestFocus();
            return;
        }

        List<ProductData> matches = findProductsByBarcodeOrName(keyword);

        if (matches.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No product found for: " + keyword);
            barcodeInput.requestFocus();
            barcodeInput.selectAll();
            return;
        }

        ProductData product = null;

        // exact product name match first
        for (ProductData p : matches) {
            if (keyword.equalsIgnoreCase(p.productName)) {
                product = p;
                break;
            }
        }

        // exact barcode match second
        if (product == null) {
            for (ProductData p : matches) {
                if (keyword.equalsIgnoreCase(p.barcode)) {
                    product = p;
                    break;
                }
            }
        }

        // fallback to first result if still null
        if (product == null) {
            product = matches.get(0);
        }

        if (product.stockQuantity <= 0) {
            JOptionPane.showMessageDialog(this, "This product is out of stock.");
            barcodeInput.requestFocus();
            barcodeInput.selectAll();
            return;
        }

        showAddToCartDialog(product);
    }

    private List<ProductData> findProductsByBarcodeOrName(String keyword) {
        List<ProductData> products = new ArrayList<>();

        try (Connection con = DBConnection.dbConnection(); PreparedStatement pst = con.prepareStatement(
                "SELECT product_id, name, barcode, "
                + "COALESCE(selling_price,0) AS selling_price, "
                + "COALESCE(discount,0) AS discount, "
                + "COALESCE(stock_quantity,0) AS stock_quantity, "
                + "product_image "
                + "FROM products "
                + "WHERE barcode LIKE ? OR name LIKE ? "
                + "ORDER BY name ASC")) {

            pst.setString(1, "%" + keyword + "%");
            pst.setString(2, "%" + keyword + "%");

            try (ResultSet rs = pst.executeQuery()) {
                while (rs != null && rs.next()) {
                    ProductData data = new ProductData();
                    data.productId = rs.getInt("product_id");
                    data.productName = rs.getString("name");
                    data.barcode = rs.getString("barcode");
                    data.sellingPrice = rs.getDouble("selling_price");
                    data.defaultDiscount = rs.getDouble("discount");
                    data.stockQuantity = rs.getInt("stock_quantity");
                    data.productImageBase64 = rs.getString("product_image");

                    products.add(data);
                }
            }

        } catch (Exception e) {
            System.out.println("DEBUG ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        return products;
    }

    private ProductData showProductSelectionDialog(List<ProductData> matches) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        final JDialog dialog = new JDialog(owner, "Select Product", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel header = createDialogHeader("Matching Products", "Select the correct product");
        dialog.add(header, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Barcode", "Product Name", "Price", "Stock"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (ProductData p : matches) {
            model.addRow(new Object[]{
                p.productId,
                p.barcode,
                p.productName,
                formatMoneyWithPeso(p.sellingPrice),
                p.stockQuantity
            });
        }

        JTable table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        enhanceTable(table, sp);

        final ProductData[] selected = new ProductData[1];

        JButton btnSelect = new JButton("Select");
        JButton btnCancel = new JButton("Cancel");
        styleDialogButton(btnSelect, new Color(52, 120, 246), Color.WHITE);
        styleDialogButton(btnCancel, new Color(220, 220, 220), Color.BLACK);

        btnSelect.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(dialog, "Please select a product.");
                    return;
                }

                int modelRow = table.convertRowIndexToModel(row);
                int productId = Integer.parseInt(model.getValueAt(modelRow, 0).toString());

                for (ProductData p : matches) {
                    if (p.productId == productId) {
                        selected[0] = p;
                        break;
                    }
                }

                dialog.dispose();
            }
        });

        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    btnSelect.doClick();
                }
            }
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        south.setBackground(Color.WHITE);
        south.add(btnCancel);
        south.add(btnSelect);

        dialog.add(sp, BorderLayout.CENTER);
        dialog.add(south, BorderLayout.SOUTH);
        dialog.setSize(720, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setVisible(true);

        return selected[0];
    }

    private int getAvailableStock(int productId) {
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = DBConnection.dbConnection();
            String sql = "SELECT COALESCE(stock_quantity, 0) AS stock_quantity FROM products WHERE product_id = ?";
            pst = con.prepareStatement(sql);
            pst.setInt(1, productId);
            rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt("stock_quantity");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error checking stock: " + e.getMessage());
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
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
            }
        }

        return 0;
    }

    private JLabel createDialogLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Tahoma", Font.BOLD, 13));
        label.setForeground(new Color(70, 70, 70));
        return label;
    }

    private JLabel createDialogValueLabel(String text) {
        JLabel label = new JLabel(text == null ? "" : text);
        label.setFont(new Font("Tahoma", Font.PLAIN, 14));
        label.setForeground(new Color(32, 48, 70));
        label.setPreferredSize(new Dimension(320, 30));
        label.setMinimumSize(new Dimension(320, 30));
        return label;
    }

    private void styleDialogButton(JButton button, Color bg, Color fg) {
        button.setFont(new Font("Tahoma", Font.BOLD, 13));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(110, 35));
    }

    private JPanel createDialogHeader(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(33, 150, 243));
        header.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(new Font("Tahoma", Font.PLAIN, 12));
        lblSubtitle.setForeground(Color.WHITE);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(lblTitle);
        textPanel.add(lblSubtitle);

        header.add(textPanel, BorderLayout.CENTER);
        return header;
    }

    private void showAddToCartDialog(ProductData product) {
        if (product == null) {
            JOptionPane.showMessageDialog(this, "Product details could not be loaded.");
            return;
        }

        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        final JDialog dialog = new JDialog(owner, "Add Product to Cart", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel header = createDialogHeader("Add to Cart", "Review product details before adding");

        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(Color.WHITE);
        contentWrapper.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JPanel content = new JPanel(new java.awt.GridBagLayout());
        content.setBackground(Color.WHITE);

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;

        JLabel lblProductNameValue = createDialogValueLabel(product.productName);
        lblProductNameValue.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblProductNameValue.setForeground(new Color(25, 50, 90));

        JLabel lblBarcodeValue = createDialogValueLabel(product.barcode);
        JLabel lblStockValue = createDialogValueLabel(String.valueOf(product.stockQuantity));

        JLabel lblPriceValue = createDialogValueLabel(formatMoneyWithPeso(product.sellingPrice));
        lblPriceValue.setHorizontalAlignment(SwingConstants.LEFT);
        lblPriceValue.setPreferredSize(new Dimension(320, 30));
        lblPriceValue.setMinimumSize(new Dimension(320, 30));

        JSpinner quantitySpinner = new JSpinner(
                new SpinnerNumberModel(1, 1, Math.max(1, product.stockQuantity), 1)
        );
        quantitySpinner.setFont(new Font("Tahoma", Font.PLAIN, 14));
        quantitySpinner.setPreferredSize(new Dimension(200, 34));
        quantitySpinner.setMinimumSize(new Dimension(200, 34));

        JComponent spinnerEditor = quantitySpinner.getEditor();
        if (spinnerEditor instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinnerEditor;
            JTextField spinnerField = editor.getTextField();
            spinnerField.setHorizontalAlignment(JTextField.CENTER);
            spinnerField.setEditable(false);
            spinnerField.setBackground(Color.WHITE);
            spinnerField.setFont(new Font("Tahoma", Font.PLAIN, 14));
        }

        JTextField txtDiscount = new JTextField("0.00");
        txtDiscount.setPreferredSize(new Dimension(200, 34));
        txtDiscount.setMinimumSize(new Dimension(200, 34));
        styleTextField(txtDiscount);
        applyNumericFilter(txtDiscount, true);
        txtDiscount.setHorizontalAlignment(JTextField.RIGHT);

        final JLabel lblSubtotalPreview = new JLabel("₱0.00");
        lblSubtotalPreview.setFont(new Font("Tahoma", Font.BOLD, 20));
        lblSubtotalPreview.setForeground(new Color(25, 94, 190));
        lblSubtotalPreview.setHorizontalAlignment(SwingConstants.LEFT);
        lblSubtotalPreview.setPreferredSize(new Dimension(320, 36));
        lblSubtotalPreview.setMinimumSize(new Dimension(320, 36));

        addDialogRow(content, gbc, 0, "Product Name", lblProductNameValue);
        addDialogRow(content, gbc, 1, "Barcode", lblBarcodeValue);
        addDialogRow(content, gbc, 2, "Available Stock", lblStockValue);
        addDialogRow(content, gbc, 3, "Price", lblPriceValue);
        addDialogRow(content, gbc, 4, "Quantity", quantitySpinner);
        addDialogRow(content, gbc, 5, "Discount", txtDiscount);
        addDialogRow(content, gbc, 6, "Subtotal", lblSubtotalPreview);

        contentWrapper.add(content, BorderLayout.CENTER);

        JButton btnConfirm = new JButton("Confirm");
        JButton btnCancel = new JButton("Cancel");

        styleDialogButton(btnConfirm, new Color(46, 125, 50), Color.WHITE);
        styleDialogButton(btnCancel, new Color(220, 220, 220), Color.BLACK);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(0, 10, 12, 10));
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnConfirm);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(contentWrapper, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        final Runnable updatePreview = () -> {
            int qty = ((Number) quantitySpinner.getValue()).intValue();
            double discount = parseDouble(txtDiscount.getText());
            double subtotal = (qty * product.sellingPrice) - discount;

            if (subtotal < 0) {
                subtotal = 0;
            }

            lblSubtotalPreview.setText(formatMoneyWithPeso(subtotal));
        };

        quantitySpinner.addChangeListener(e -> updatePreview.run());

        txtDiscount.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePreview.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePreview.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePreview.run();
            }
        });

        btnConfirm.addActionListener(e -> {
            int quantity = ((Number) quantitySpinner.getValue()).intValue();
            double discount = parseDouble(txtDiscount.getText());

            if (quantity <= 0) {
                JOptionPane.showMessageDialog(dialog, "Quantity must be greater than 0.");
                return;
            }

            if (quantity > product.stockQuantity) {
                JOptionPane.showMessageDialog(dialog, "Quantity exceeds available stock.");
                return;
            }

            double subtotal = (quantity * product.sellingPrice) - discount;
            if (subtotal < 0) {
                JOptionPane.showMessageDialog(dialog, "Discount cannot be greater than the item total.");
                return;
            }

            addOrMergeCartItem(
                    product.productId,
                    product.productName,
                    quantity,
                    product.sellingPrice,
                    discount,
                    product.stockQuantity
            );

            dialog.dispose();
            barcodeInput.setText("");
            barcodeInput.requestFocus();
        });

        btnCancel.addActionListener(e -> {
            dialog.dispose();
            barcodeInput.requestFocus();
        });

        updatePreview.run();

        dialog.pack();
        dialog.setMinimumSize(new Dimension(700, 620));
        dialog.setSize(700, 620);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    private void addDialogRow(JPanel panel, java.awt.GridBagConstraints gbc, int row, String labelText, Component valueComponent) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(14, 10, 14, 22);
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.fill = java.awt.GridBagConstraints.NONE;

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Tahoma", Font.BOLD, 14));
        label.setForeground(new Color(48, 58, 74));
        label.setPreferredSize(new Dimension(150, 24));
        label.setMinimumSize(new Dimension(150, 24));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(14, 0, 14, 10);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.anchor = java.awt.GridBagConstraints.WEST;

        panel.add(valueComponent, gbc);
    }

    private void addOrMergeCartItem(int productId, String productName, int quantity, double price, double discount, int availableStock) {
        int existingRow = findRowByProductId(productId);

        if (existingRow >= 0) {
            int oldQty = ((Number) cartModel.getValueAt(existingRow, 2)).intValue();
            int newQty = oldQty + quantity;

            if (newQty > availableStock) {
                JOptionPane.showMessageDialog(this, "Cannot add item. Total quantity exceeds available stock.");
                return;
            }

            double oldDiscount = parseDouble(cartModel.getValueAt(existingRow, 4));
            double newDiscount = oldDiscount + discount;
            double newSubtotal = (newQty * price) - newDiscount;

            if (newSubtotal < 0) {
                newSubtotal = 0;
            }

            cartModel.setValueAt(newQty, existingRow, 2);
            cartModel.setValueAt(price, existingRow, 3);
            cartModel.setValueAt(newDiscount, existingRow, 4);
            cartModel.setValueAt(newSubtotal, existingRow, 5);
        } else {
            double subtotal = (quantity * price) - discount;

            if (subtotal < 0) {
                subtotal = 0;
            }

            cartModel.addRow(new Object[]{
                productId,
                productName,
                quantity,
                price,
                discount,
                subtotal
            });
        }

        updateComputation();
    }

    private int findRowByProductId(int productId) {
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            int rowProductId = ((Integer) cartModel.getValueAt(i, 0)).intValue();
            if (rowProductId == productId) {
                return i;
            }
        }
        return -1;
    }

    public void addCartItem(int productId, String productName, int quantity, double price, double discount) {
        addOrMergeCartItem(productId, productName, quantity, price, discount, Integer.MAX_VALUE);
    }

    private void removeSelectedItem() {
        int selectedRow = tblCart.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.");
            return;
        }

        int modelRow = tblCart.convertRowIndexToModel(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Remove selected item from cart?",
                "Confirm Remove",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            cartModel.removeRow(modelRow);
            updateComputation();
        }
    }

    private void editSelectedItemQuantity() {
        int selectedRow = tblCart.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item first.");
            return;
        }

        final int modelRow = tblCart.convertRowIndexToModel(selectedRow);

        int productId = ((Integer) cartModel.getValueAt(modelRow, 0)).intValue();
        String productName = cartModel.getValueAt(modelRow, 1).toString();
        int currentQty = ((Integer) cartModel.getValueAt(modelRow, 2)).intValue();
        final double price = parseDouble(cartModel.getValueAt(modelRow, 3));
        final double discount = parseDouble(cartModel.getValueAt(modelRow, 4));

        final int availableStock = getAvailableStock(productId);

        if (availableStock <= 0) {
            JOptionPane.showMessageDialog(this, "This product is out of stock.");
            return;
        }

        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        final JDialog dialog = new JDialog(owner, "Edit Quantity", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel header = createDialogHeader("Edit Cart Quantity", "Update the selected cart item");

        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(Color.WHITE);
        contentWrapper.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel content = new JPanel(new GridLayout(0, 2, 12, 12));
        content.setBackground(Color.WHITE);

        JLabel lblName = createDialogValueLabel(productName);
        JLabel lblPrice = createDialogValueLabel(formatMoneyWithPeso(price));
        JLabel lblStock = createDialogValueLabel(String.valueOf(availableStock));
        final JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(currentQty, 1, availableStock, 1));
        qtySpinner.setFont(new Font("Tahoma", Font.PLAIN, 13));

        final JLabel lblSubtotal = new JLabel("₱0.00");
        lblSubtotal.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblSubtotal.setForeground(new Color(0, 102, 204));

        content.add(createDialogLabel("Product Name"));
        content.add(lblName);

        content.add(createDialogLabel("Price"));
        content.add(lblPrice);

        content.add(createDialogLabel("Available Stock"));
        content.add(lblStock);

        content.add(createDialogLabel("Quantity"));
        content.add(qtySpinner);

        content.add(createDialogLabel("Discount"));
        content.add(createDialogValueLabel(formatMoneyWithPeso(discount)));

        content.add(createDialogLabel("New Subtotal"));
        content.add(lblSubtotal);

        contentWrapper.add(content, BorderLayout.CENTER);

        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");

        styleDialogButton(btnSave, new Color(25, 118, 210), Color.WHITE);
        styleDialogButton(btnCancel, new Color(220, 220, 220), Color.BLACK);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(contentWrapper, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        final Runnable updatePreview = new Runnable() {
            @Override
            public void run() {
                int qty = ((Number) qtySpinner.getValue()).intValue();
                double subtotal = (qty * price) - discount;
                if (subtotal < 0) {
                    subtotal = 0;
                }
                lblSubtotal.setText(formatMoneyWithPeso(subtotal));
            }
        };

        qtySpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                updatePreview.run();
            }
        });

        btnSave.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int newQty = ((Number) qtySpinner.getValue()).intValue();

                if (newQty <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Quantity must be greater than 0.");
                    return;
                }

                if (newQty > availableStock) {
                    JOptionPane.showMessageDialog(dialog, "Quantity exceeds available stock.");
                    return;
                }

                double newSubtotal = (newQty * price) - discount;
                if (newSubtotal < 0) {
                    newSubtotal = 0;
                }

                cartModel.setValueAt(newQty, modelRow, 2);
                cartModel.setValueAt(newSubtotal, modelRow, 5);

                dialog.dispose();
                updateComputation();
            }
        });

        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dialog.dispose();
            }
        });

        updatePreview.run();

        dialog.setSize(450, 330);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    private boolean validatePayment(String paymentMethod) {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty.");
            return false;
        }

        double total = parseDouble(totalValue.getText());
        double tendered = parseDouble(cashTendered.getText());

        if (tendered <= 0) {
            JOptionPane.showMessageDialog(this, "Please enter cash tendered or payment amount first.");
            cashTendered.requestFocus();
            cashTendered.selectAll();
            return false;
        }

        if (tendered < total) {
            JOptionPane.showMessageDialog(this,
                    paymentMethod + " payment amount is insufficient.",
                    "Insufficient Payment",
                    JOptionPane.WARNING_MESSAGE);
            cashTendered.requestFocus();
            cashTendered.selectAll();
            return false;
        }

        return true;
    }

    private String buildReceiptText() {
        StringBuilder sb = new StringBuilder();

        sb.append("==================================================\n");
        sb.append("                 PROFESSIONAL SALES RECEIPT       \n");
        sb.append("==================================================\n");
        sb.append("Invoice No : ").append(getGeneratedInvoiceNumber()).append("\n");
        sb.append("Cashier    : ").append(getCashierName()).append("\n");
        sb.append("Role       : ").append(getCashierRole()).append("\n");
        sb.append("Date/Time  : ").append(lblDateTime.getText()).append("\n");
        sb.append("==================================================\n");
        sb.append(String.format("%-20s %5s %10s %12s\n", "Item", "Qty", "Price", "Subtotal"));
        sb.append("--------------------------------------------------\n");

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String name = cartModel.getValueAt(i, 1).toString();
            int qty = ((Integer) cartModel.getValueAt(i, 2)).intValue();
            double price = parseDouble(cartModel.getValueAt(i, 3));
            double subtotal = parseDouble(cartModel.getValueAt(i, 5));

            if (name.length() > 20) {
                name = name.substring(0, 20);
            }

            sb.append(String.format("%-20s %5d %10s %12s\n",
                    name,
                    qty,
                    formatMoney(price),
                    formatMoney(subtotal)));
        }

        sb.append("==================================================\n");
        sb.append(String.format("%-26s %22s\n", "Subtotal:", formatMoneyWithPeso(parseDouble(subtotalValue.getText()))));
        sb.append(String.format("%-26s %22s\n", "Discount:", formatMoneyWithPeso(parseDouble(discoutValue.getText()))));
        sb.append(String.format("%-26s %22s\n", "VAT (12%):", formatMoneyWithPeso(parseDouble(vatValue.getText()))));
        sb.append(String.format("%-26s %22s\n", "Total:", formatMoneyWithPeso(parseDouble(totalValue.getText()))));
        sb.append(String.format("%-26s %22s\n", "Tendered:", formatMoneyWithPeso(parseDouble(cashTendered.getText()))));
        sb.append(String.format("%-26s %22s\n", "Change:", formatMoneyWithPeso(parseDouble(changeValueAutomated.getText()))));
        sb.append("==================================================\n");
        sb.append("            Thank you for your purchase!          \n");
        sb.append("==================================================\n");

        return sb.toString();
    }

    private void printReceipt() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty.");
            return;
        }

        JTextArea textArea = new JTextArea();
        textArea.setText(buildReceiptText());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        textArea.setMargin(new Insets(15, 15, 15, 15));

        try {
            boolean done = textArea.print();
            if (done) {
                JOptionPane.showMessageDialog(this, "Receipt printed successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Printing was cancelled.");
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Print error: " + e.getMessage());
        }
    }

    private void exportReceiptToPDF() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Receipt PDF");
        chooser.setSelectedFile(new File(getGeneratedInvoiceNumber() + ".pdf"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getAbsolutePath() + ".pdf");
        }

        try {
            PdfWriter writer = new PdfWriter(file.getAbsolutePath());
            PdfDocument pdf = new PdfDocument(writer);
            pdf.setDefaultPageSize(PageSize.A4);

            Document document = new Document(pdf);
            document.setMargins(30, 30, 30, 30);

            Paragraph title = new Paragraph("SALES RECEIPT")
                    .setBold()
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.BLUE);

            Paragraph storeLine = new Paragraph("Point of Sale System")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.DARK_GRAY);

            document.add(title);
            document.add(storeLine);
            document.add(new Paragraph(" "));

            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
            infoTable.setWidth(UnitValue.createPercentValue(100));

            infoTable.addCell(createInfoCell("Invoice No"));
            infoTable.addCell(createInfoValueCell(getGeneratedInvoiceNumber()));
            infoTable.addCell(createInfoCell("Cashier"));
            infoTable.addCell(createInfoValueCell(getCashierName()));
            infoTable.addCell(createInfoCell("Role"));
            infoTable.addCell(createInfoValueCell(getCashierRole()));
            infoTable.addCell(createInfoCell("Date/Time"));
            infoTable.addCell(createInfoValueCell(lblDateTime.getText()));

            document.add(infoTable);
            document.add(new Paragraph(" "));

            Table salesTable = new Table(UnitValue.createPercentArray(new float[]{4, 1, 2, 2, 2}));
            salesTable.setWidth(UnitValue.createPercentValue(100));

            salesTable.addHeaderCell(createHeaderCell("Product"));
            salesTable.addHeaderCell(createHeaderCell("Qty"));
            salesTable.addHeaderCell(createHeaderCell("Price"));
            salesTable.addHeaderCell(createHeaderCell("Discount"));
            salesTable.addHeaderCell(createHeaderCell("Subtotal"));

            for (int i = 0; i < cartModel.getRowCount(); i++) {
                salesTable.addCell(createBodyCell(cartModel.getValueAt(i, 1).toString()));
                salesTable.addCell(createBodyCell(cartModel.getValueAt(i, 2).toString(), TextAlignment.CENTER));
                salesTable.addCell(createBodyCell(formatMoneyWithPeso(parseDouble(cartModel.getValueAt(i, 3))), TextAlignment.RIGHT));
                salesTable.addCell(createBodyCell(formatMoneyWithPeso(parseDouble(cartModel.getValueAt(i, 4))), TextAlignment.RIGHT));
                salesTable.addCell(createBodyCell(formatMoneyWithPeso(parseDouble(cartModel.getValueAt(i, 5))), TextAlignment.RIGHT));
            }

            document.add(salesTable);
            document.add(new Paragraph(" "));

            Table totals = new Table(UnitValue.createPercentArray(new float[]{3, 2}));
            totals.setWidth(UnitValue.createPercentValue(45));
            totals.setHorizontalAlignment(HorizontalAlignment.RIGHT);

            totals.addCell(createInfoCell("Subtotal"));
            totals.addCell(createBodyCell(formatMoneyWithPeso(parseDouble(subtotalValue.getText())), TextAlignment.RIGHT));
            totals.addCell(createInfoCell("Discount"));
            totals.addCell(createBodyCell(formatMoneyWithPeso(parseDouble(discoutValue.getText())), TextAlignment.RIGHT));
            totals.addCell(createInfoCell("VAT (12%)"));
            totals.addCell(createBodyCell(formatMoneyWithPeso(parseDouble(vatValue.getText())), TextAlignment.RIGHT));
            totals.addCell(createInfoCell("Total"));
            totals.addCell(createBodyCell(formatMoneyWithPeso(parseDouble(totalValue.getText())), TextAlignment.RIGHT));
            totals.addCell(createInfoCell("Tendered"));
            totals.addCell(createBodyCell(formatMoneyWithPeso(parseDouble(cashTendered.getText())), TextAlignment.RIGHT));
            totals.addCell(createInfoCell("Change"));
            totals.addCell(createBodyCell(formatMoneyWithPeso(parseDouble(changeValueAutomated.getText())), TextAlignment.RIGHT));

            document.add(totals);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Thank you for your purchase!")
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.DARK_GRAY));

            document.close();

            JOptionPane.showMessageDialog(this, "PDF exported successfully:\n" + file.getAbsolutePath());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error exporting PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(40, 56, 145))
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(Border.NO_BORDER)
                .setPadding(8);
    }

    private Cell createBodyCell(String text) {
        return createBodyCell(text, TextAlignment.LEFT);
    }

    private Cell createBodyCell(String text, TextAlignment alignment) {
        return new Cell()
                .add(new Paragraph(text))
                .setTextAlignment(alignment)
                .setBorder(new SolidBorder(new com.itextpdf.kernel.colors.DeviceRgb(220, 228, 240), 1))
                .setPadding(7);
    }

    private Cell createInfoCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold())
                .setBorder(Border.NO_BORDER)
                .setPadding(4);
    }

    private Cell createInfoValueCell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setBorder(Border.NO_BORDER)
                .setPadding(4);
    }

    private boolean saveTransaction(String paymentMethod) {
        if (!validatePayment(paymentMethod)) {
            return false;
        }

        double totalAmount = parseDouble(totalValue.getText());
        double subtotal = parseDouble(subtotalValue.getText());
        double vat = parseDouble(vatValue.getText());
        double totalDiscount = parseDouble(discoutValue.getText());

        double cashTenderedValue = parseDouble(cashTendered.getText());
        double changeValue = cashTenderedValue - totalAmount;

        if (changeValue < 0) {
            JOptionPane.showMessageDialog(this, "Insufficient payment amount.");
            return false;
        }

        Connection con = null;
        PreparedStatement pstSale = null;
        PreparedStatement pstDetails = null;
        ResultSet generatedKeys = null;

        try {
            con = DBConnection.dbConnection();
            con.setAutoCommit(false);

            String saleSql = "INSERT INTO sales "
                    + "(user_id, invoice_number, subtotal, vat, discount, total_amount, payment_method, cash_tendered, change_amount, sale_date) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

            pstSale = con.prepareStatement(saleSql, PreparedStatement.RETURN_GENERATED_KEYS);

            pstSale.setInt(1, loggedInUserId);
            pstSale.setString(2, getGeneratedInvoiceNumber());
            pstSale.setDouble(3, subtotal);
            pstSale.setDouble(4, vat);
            pstSale.setDouble(5, totalDiscount);
            pstSale.setDouble(6, totalAmount);
            pstSale.setString(7, paymentMethod);
            pstSale.setDouble(8, cashTenderedValue);
            pstSale.setDouble(9, changeValue);
            pstSale.executeUpdate();

            generatedKeys = pstSale.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new SQLException("Failed to get generated sale_id.");
            }

            int saleId = generatedKeys.getInt(1);

            String detailSql = "INSERT INTO sales_details "
                    + "(sale_id, product_id, quantity, price, discount, subtotal) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            pstDetails = con.prepareStatement(detailSql);

            for (int i = 0; i < cartModel.getRowCount(); i++) {
                int productId = Integer.parseInt(cartModel.getValueAt(i, 0).toString());
                int quantity = Integer.parseInt(cartModel.getValueAt(i, 2).toString());
                double price = parseDouble(cartModel.getValueAt(i, 3));
                double discount = parseDouble(cartModel.getValueAt(i, 4));
                double subtotalRow = parseDouble(cartModel.getValueAt(i, 5));

                pstDetails.setInt(1, saleId);
                pstDetails.setInt(2, productId);
                pstDetails.setInt(3, quantity);
                pstDetails.setDouble(4, price);
                pstDetails.setDouble(5, discount);
                pstDetails.setDouble(6, subtotalRow);
                pstDetails.addBatch();

                updateProductStock(con, productId, quantity);
            }

            pstDetails.executeBatch();
            con.commit();

            refreshLinkedPanels();

            JOptionPane.showMessageDialog(this, "Transaction saved successfully.");

            clearCart();
            refreshLinkedPanels();
            return true;

        } catch (Exception e) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            JOptionPane.showMessageDialog(this, "Error saving transaction: " + e.getMessage());
            return false;

        } finally {
            try {
                if (generatedKeys != null) {
                    generatedKeys.close();
                }
            } catch (Exception e) {
            }
            try {
                if (pstDetails != null) {
                    pstDetails.close();
                }
            } catch (Exception e) {
            }
            try {
                if (pstSale != null) {
                    pstSale.close();
                }
            } catch (Exception e) {
            }
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (Exception e) {
            }
        }
    }

    private void updateProductStock(Connection con, int productId, int quantitySold) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";
        PreparedStatement pst = null;

        try {
            pst = con.prepareStatement(sql);
            pst.setInt(1, quantitySold);
            pst.setInt(2, productId);
            pst.executeUpdate();
        } finally {
            try {
                if (pst != null) {
                    pst.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public void clearCart() {
        cartModel.setRowCount(0);
        cashTendered.setText("₱0.00");
        initializeValues();
        generateInvoiceNumber();
        barcodeInput.setText("");
        barcodeInput.requestFocus();
    }

    public String getGeneratedInvoiceNumber() {
        return invoiceAutoGenerated.getText();
    }

    public String getCashierName() {
        return cashierAutomatedName.getText();
    }

    public String getCashierRole() {
        return automatedRole.getText();
    }

    private List<ProductPreviewData> getCartProductPreviewData() {
        List<ProductPreviewData> list = new ArrayList<ProductPreviewData>();

        if (cartModel.getRowCount() == 0) {
            return list;
        }

        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = DBConnection.dbConnection();

            String sql = "SELECT product_id, barcode, name, COALESCE(selling_price, 0) AS selling_price, "
                    + "COALESCE(stock_quantity, 0) AS stock_quantity, product_image "
                    + "FROM products WHERE product_id = ?";

            pst = con.prepareStatement(sql);

            for (int i = 0; i < cartModel.getRowCount(); i++) {
                int productId = Integer.parseInt(cartModel.getValueAt(i, 0).toString());
                int qty = Integer.parseInt(cartModel.getValueAt(i, 2).toString());
                double discount = parseDouble(cartModel.getValueAt(i, 4));
                double subtotal = parseDouble(cartModel.getValueAt(i, 5));

                pst.setInt(1, productId);
                rs = pst.executeQuery();

                if (rs.next()) {
                    ProductPreviewData data = new ProductPreviewData();
                    data.productId = rs.getInt("product_id");
                    data.barcode = rs.getString("barcode");
                    data.productName = rs.getString("name");
                    data.price = rs.getDouble("selling_price");
                    data.stockQuantity = rs.getInt("stock_quantity");
                    data.productImageBase64 = rs.getString("product_image");
                    data.cartQuantity = qty;
                    data.discount = discount;
                    data.subtotal = subtotal;
                    list.add(data);
                }

                try {
                    if (rs != null) {
                        rs.close();
                    }
                } catch (Exception e) {
                }
                rs = null;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading preview details: " + e.getMessage());
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
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
            }
        }

        return list;
    }

    private void showCartProductsPreviewDialog() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty.");
            return;
        }

        List<ProductPreviewData> items = getCartProductPreviewData();

        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Cart Product Preview", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(245, 249, 255));

        JPanel header = createDialogHeader("Cart Product Preview", "Showing all products currently added to the cart");
        dialog.add(header, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setBackground(new Color(245, 249, 255));
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        for (ProductPreviewData item : items) {
            listPanel.add(createPreviewCard(item));
            listPanel.add(Box.createVerticalStrut(12));
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JButton btnClose = new JButton("Close");
        styleDialogButton(btnClose, new Color(52, 120, 246), Color.WHITE);
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setBackground(Color.WHITE);
        footer.add(btnClose);

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setSize(760, 540);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    private JPanel createPreviewCard(ProductPreviewData item) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 230, 242), 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(170, 120));
        imageLabel.setBorder(new LineBorder(new Color(230, 236, 246), 1, true));
        imageLabel.setBackground(new Color(248, 251, 255));
        imageLabel.setOpaque(true);

        javax.swing.ImageIcon icon = buildScaledImageIcon(item.productImageBase64, PREVIEW_IMAGE_W, PREVIEW_IMAGE_H);
        if (icon != null) {
            imageLabel.setIcon(icon);
            imageLabel.setText("");
        } else {
            imageLabel.setText("<html><center>No Image</center></html>");
            imageLabel.setForeground(new Color(90, 105, 130));
        }

        JPanel details = new JPanel(new GridLayout(0, 2, 10, 8));
        details.setOpaque(false);

        details.add(createDialogLabel("Product Name"));
        details.add(createDialogValueLabel(item.productName));

        details.add(createDialogLabel("Barcode"));
        details.add(createDialogValueLabel(item.barcode));

        details.add(createDialogLabel("Price"));
        details.add(createDialogValueLabel(formatMoneyWithPeso(item.price)));

        details.add(createDialogLabel("Cart Quantity"));
        details.add(createDialogValueLabel(String.valueOf(item.cartQuantity)));

        details.add(createDialogLabel("Discount"));
        details.add(createDialogValueLabel(formatMoneyWithPeso(item.discount)));

        details.add(createDialogLabel("Subtotal"));
        details.add(createDialogValueLabel(formatMoneyWithPeso(item.subtotal)));

        details.add(createDialogLabel("Remaining Stock"));
        details.add(createDialogValueLabel(String.valueOf(item.stockQuantity)));

        card.add(imageLabel, BorderLayout.WEST);
        card.add(details, BorderLayout.CENTER);

        return card;
    }

    private javax.swing.ImageIcon buildScaledImageIcon(String base64, int width, int height) {
        try {
            if (base64 == null || base64.trim().isEmpty()) {
                return null;
            }

            byte[] bytes = Base64.getDecoder().decode(base64);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                return null;
            }

            Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new javax.swing.ImageIcon(scaled);

        } catch (Exception e) {
            return null;
        }
    }

    private static class ProductData {

        int productId;
        String productName;
        String barcode;
        double sellingPrice;
        double defaultDiscount;
        int stockQuantity;
        String productImageBase64;
    }

    private static class ProductPreviewData {

        int productId;
        String barcode;
        String productName;
        double price;
        int stockQuantity;
        String productImageBase64;
        int cartQuantity;
        double discount;
        double subtotal;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topPanel = new RoundedPanel();
        lblDateTime = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        automatedRole = new javax.swing.JLabel();
        cashierAutomatedName = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        invoiceAutoGenerated = new javax.swing.JLabel();
        pnlProductEntry = new RoundedPanel();
        jLabel3 = new javax.swing.JLabel();
        barcodeInput = new javax.swing.JTextField();
        btnAddToCart = new javax.swing.JButton();
        pnlComputation = new RoundedPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        vat12Percent = new javax.swing.JLabel();
        subtotalValue = new javax.swing.JLabel();
        discoutValue = new javax.swing.JLabel();
        vatValue = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel7 = new javax.swing.JLabel();
        totalValue = new javax.swing.JLabel();
        pnlPaymentMethod = new RoundedPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        changeValueAutomated = new javax.swing.JLabel();
        btnCashPayment = new javax.swing.JButton();
        cashTendered = new javax.swing.JTextField();
        btnGcashEwalletPayment = new javax.swing.JButton();
        btnCardPayment = new javax.swing.JButton();
        btnPrintReceipt = new javax.swing.JButton();
        btnPDF = new javax.swing.JButton();
        pnlCart = new RoundedPanel();
        automatedNoOfItems = new javax.swing.JLabel();
        lblNoOfItems = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel("Add New User") {     @Override     protected void paintComponent(java.awt.Graphics g) {         java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();          g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,                             java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);          float[] fractions = {0.5f, 1f};         java.awt.Color[] colors = {             java.awt.Color.WHITE,             java.awt.Color.BLUE         };          java.awt.LinearGradientPaint lgp = new java.awt.LinearGradientPaint(                 0, 0, getWidth(), 0,                 fractions, colors         );          g2.setPaint(lgp);          java.awt.FontMetrics fm = g2.getFontMetrics();         int x = (getWidth() - fm.stringWidth(getText())) / 2;         int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();          g2.drawString(getText(), x, y);          g2.dispose();     } };
        cartGIF = new javax.swing.JLabel();
        jLabel12 = new point.of.sale.system.classes.GradientFont();
        pnlClickToProductPreviewProduct = new RoundedPanel();
        jLabel13 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator5 = new javax.swing.JSeparator();
        pnlTableWrap = new RoundedPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblCart = new javax.swing.JTable();

        setBackground(new java.awt.Color(240, 248, 255));
        setPreferredSize(new java.awt.Dimension(1120, 820));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        topPanel.setBackground(new java.awt.Color(18, 48, 174));
        topPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblDateTime.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblDateTime.setForeground(new java.awt.Color(255, 255, 255));
        lblDateTime.setText("---");
        topPanel.add(lblDateTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 260, -1));

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Date & Time");
        topPanel.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, -1, -1));

        automatedRole.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        automatedRole.setForeground(new java.awt.Color(255, 255, 255));
        automatedRole.setText("Cashier");
        topPanel.add(automatedRole, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 10, -1, -1));

        cashierAutomatedName.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        cashierAutomatedName.setForeground(new java.awt.Color(255, 255, 255));
        cashierAutomatedName.setText("Name");
        topPanel.add(cashierAutomatedName, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 40, -1, -1));

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Invoice Number");
        topPanel.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 10, -1, -1));

        invoiceAutoGenerated.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        invoiceAutoGenerated.setForeground(new java.awt.Color(255, 255, 255));
        invoiceAutoGenerated.setText("Invoice Number here");
        topPanel.add(invoiceAutoGenerated, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 40, -1, -1));

        add(topPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 1100, 75));

        pnlProductEntry.setBackground(new java.awt.Color(122, 170, 206));
        pnlProductEntry.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Search for product name or barcode");
        pnlProductEntry.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(13, 13, -1, -1));

        barcodeInput.addActionListener(this::barcodeInputActionPerformed);
        pnlProductEntry.add(barcodeInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(13, 40, 250, 30));

        btnAddToCart.setText("Add to cart");
        btnAddToCart.addActionListener(this::btnAddToCartActionPerformed);
        pnlProductEntry.add(btnAddToCart, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 40, -1, 30));

        add(pnlProductEntry, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 140, 410, 90));

        pnlComputation.setBackground(new java.awt.Color(122, 170, 206));
        pnlComputation.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Computation");
        pnlComputation.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 170, -1));

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Subtotal:");
        pnlComputation.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 80, -1));

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Discount:");
        pnlComputation.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, -1, -1));

        vat12Percent.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        vat12Percent.setForeground(new java.awt.Color(255, 255, 255));
        vat12Percent.setText("VAT (12%):");
        pnlComputation.add(vat12Percent, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, -1, -1));

        subtotalValue.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        subtotalValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        subtotalValue.setText("---");
        pnlComputation.add(subtotalValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 70, 120, -1));

        discoutValue.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        discoutValue.setForeground(new java.awt.Color(255, 0, 0));
        discoutValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        discoutValue.setText("---");
        pnlComputation.add(discoutValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 110, 140, 20));

        vatValue.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        vatValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        vatValue.setText("---");
        pnlComputation.add(vatValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 150, 140, -1));

        jSeparator1.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator1.setForeground(new java.awt.Color(0, 0, 0));
        pnlComputation.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, 300, 10));

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Total:");
        pnlComputation.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 220, -1, -1));

        totalValue.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        totalValue.setForeground(new java.awt.Color(0, 0, 255));
        totalValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        totalValue.setText("---");
        pnlComputation.add(totalValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 220, 140, -1));

        add(pnlComputation, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 140, 350, 290));

        pnlPaymentMethod.setBackground(new java.awt.Color(122, 170, 206));
        pnlPaymentMethod.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Payment Methods");
        pnlPaymentMethod.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 230, -1));

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Cash Tendered:");
        pnlPaymentMethod.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 190, -1, -1));

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("Change:");
        pnlPaymentMethod.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, 70, -1));

        changeValueAutomated.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        changeValueAutomated.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        changeValueAutomated.setText("---");
        pnlPaymentMethod.add(changeValueAutomated, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 270, 120, 20));

        btnCashPayment.setText("CASH");
        btnCashPayment.addActionListener(this::btnCashPaymentActionPerformed);
        pnlPaymentMethod.add(btnCashPayment, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 70, 140, -1));
        pnlPaymentMethod.add(cashTendered, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 220, 310, 30));

        btnGcashEwalletPayment.setText("GCASH/E-WALLET");
        btnGcashEwalletPayment.addActionListener(this::btnGcashEwalletPaymentActionPerformed);
        pnlPaymentMethod.add(btnGcashEwalletPayment, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 110, 140, -1));

        btnCardPayment.setText("CARD");
        btnCardPayment.addActionListener(this::btnCardPaymentActionPerformed);
        pnlPaymentMethod.add(btnCardPayment, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 150, 140, -1));

        add(pnlPaymentMethod, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 430, 350, 330));

        btnPrintReceipt.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        btnPrintReceipt.setText("Print Receipt");
        btnPrintReceipt.addActionListener(this::btnPrintReceiptActionPerformed);
        add(btnPrintReceipt, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 760, 160, 40));

        btnPDF.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        btnPDF.setText("Export to PDF");
        btnPDF.addActionListener(this::btnPDFActionPerformed);
        add(btnPDF, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 760, 150, 40));

        pnlCart.setBackground(new java.awt.Color(18, 48, 174));
        pnlCart.setBorder(javax.swing.BorderFactory.createLineBorder(null));
        pnlCart.setForeground(new java.awt.Color(255, 255, 255));
        pnlCart.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        automatedNoOfItems.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        automatedNoOfItems.setForeground(new java.awt.Color(255, 255, 255));
        automatedNoOfItems.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        automatedNoOfItems.setText("---");
        pnlCart.add(automatedNoOfItems, new org.netbeans.lib.awtextra.AbsoluteConstraints(661, 25, -1, -1));

        lblNoOfItems.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        lblNoOfItems.setForeground(new java.awt.Color(255, 255, 255));
        lblNoOfItems.setText("No. of Item(s):");
        pnlCart.add(lblNoOfItems, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 25, -1, 20));

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Shopping Cart");
        pnlCart.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 20, 140, 30));

        cartGIF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/shopping-cart.png"))); // NOI18N
        pnlCart.add(cartGIF, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 40, 50));

        add(pnlCart, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 240, 740, 70));

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel12.setText("POINT OF SALE");
        add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        pnlClickToProductPreviewProduct.setBackground(new java.awt.Color(122, 170, 206));
        pnlClickToProductPreviewProduct.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlClickToProductPreviewProductMouseClicked(evt);
            }
        });
        pnlClickToProductPreviewProduct.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 17)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("Click to preview product");
        pnlClickToProductPreviewProduct.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 20, -1, 30));

        add(pnlClickToProductPreviewProduct, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 150, 310, 70));

        jSeparator3.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator3.setForeground(new java.awt.Color(0, 0, 0));
        add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 150, 300, 10));

        jSeparator4.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator4.setForeground(new java.awt.Color(0, 0, 0));
        add(jSeparator4, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 30, 790, 10));

        jSeparator5.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator5.setForeground(new java.awt.Color(0, 0, 0));
        add(jSeparator5, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 220, 300, 10));

        pnlTableWrap.setBackground(new java.awt.Color(122, 170, 206));
        pnlTableWrap.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblCart.setBorder(javax.swing.BorderFactory.createLineBorder(null));
        tblCart.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Product Name", "Quantity", "Price", "Discount", "Subtotal"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblCart);
        if (tblCart.getColumnModel().getColumnCount() > 0) {
            tblCart.getColumnModel().getColumn(0).setResizable(false);
            tblCart.getColumnModel().getColumn(1).setResizable(false);
            tblCart.getColumnModel().getColumn(2).setResizable(false);
            tblCart.getColumnModel().getColumn(3).setResizable(false);
            tblCart.getColumnModel().getColumn(4).setResizable(false);
        }

        pnlTableWrap.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 720, 480));

        add(pnlTableWrap, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 310, 740, 500));
    }// </editor-fold>//GEN-END:initComponents

    private void btnCashPaymentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCashPaymentActionPerformed
        saveTransaction("Cash");
    }//GEN-LAST:event_btnCashPaymentActionPerformed

    private void btnGcashEwalletPaymentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGcashEwalletPaymentActionPerformed
        saveTransaction("GCash");
    }//GEN-LAST:event_btnGcashEwalletPaymentActionPerformed

    private void btnCardPaymentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCardPaymentActionPerformed
        saveTransaction("Card");
    }//GEN-LAST:event_btnCardPaymentActionPerformed

    private void btnPrintReceiptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintReceiptActionPerformed
        printReceipt();
    }//GEN-LAST:event_btnPrintReceiptActionPerformed

    private void pnlClickToProductPreviewProductMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlClickToProductPreviewProductMouseClicked
        showCartProductsPreviewDialog();
    }//GEN-LAST:event_pnlClickToProductPreviewProductMouseClicked

    private void btnPDFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPDFActionPerformed
        exportReceiptToPDF();
    }//GEN-LAST:event_btnPDFActionPerformed

    private void btnAddToCartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddToCartActionPerformed
        handleAddToCartByBarcode();
    }//GEN-LAST:event_btnAddToCartActionPerformed

    private void barcodeInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_barcodeInputActionPerformed
        handleAddToCartByBarcode();
    }//GEN-LAST:event_barcodeInputActionPerformed
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel automatedNoOfItems;
    private javax.swing.JLabel automatedRole;
    private javax.swing.JTextField barcodeInput;
    private javax.swing.JButton btnAddToCart;
    private javax.swing.JButton btnCardPayment;
    private javax.swing.JButton btnCashPayment;
    private javax.swing.JButton btnGcashEwalletPayment;
    private javax.swing.JButton btnPDF;
    private javax.swing.JButton btnPrintReceipt;
    private javax.swing.JLabel cartGIF;
    private javax.swing.JTextField cashTendered;
    private javax.swing.JLabel cashierAutomatedName;
    private javax.swing.JLabel changeValueAutomated;
    private javax.swing.JLabel discoutValue;
    private javax.swing.JLabel invoiceAutoGenerated;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JLabel lblDateTime;
    private javax.swing.JLabel lblNoOfItems;
    private javax.swing.JPanel pnlCart;
    private javax.swing.JPanel pnlClickToProductPreviewProduct;
    private javax.swing.JPanel pnlComputation;
    private javax.swing.JPanel pnlPaymentMethod;
    private javax.swing.JPanel pnlProductEntry;
    private javax.swing.JPanel pnlTableWrap;
    private javax.swing.JLabel subtotalValue;
    private javax.swing.JTable tblCart;
    private javax.swing.JPanel topPanel;
    private javax.swing.JLabel totalValue;
    private javax.swing.JLabel vat12Percent;
    private javax.swing.JLabel vatValue;
    // End of variables declaration//GEN-END:variables
}
