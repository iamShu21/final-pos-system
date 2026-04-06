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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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

import org.mindrot.jbcrypt.BCrypt;

import java.awt.Dialog;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Vector;
import java.util.regex.Pattern;
import javax.swing.JDialog;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumnModel;

public class UserManagementPanel extends javax.swing.JPanel {

    private static final java.util.logging.Logger logger
            = java.util.logging.Logger.getLogger(UserManagementPanel.class.getName());

    private int selectedUserId = -1;
    private boolean editingMode = false;

    private final int loggedInUserId;
    private final String loggedInUserRole;

    private JDialog logHistoryDialog;
    private JTable tblLogHistory;
    private JTextField txtSearchLog;
    private DefaultTableModel logHistoryModel;
    private TableRowSorter<DefaultTableModel> logHistorySorter;
    private JLabel lblLogEntryCount;

    public UserManagementPanel(int loggedInUserId, String loggedInUserRole) {
        this.loggedInUserId = loggedInUserId;
        this.loggedInUserRole = loggedInUserRole == null ? "" : loggedInUserRole;

        initComponents();

        applyModuleTheme();

        initRoleDropdown();
        installValidationFilters();
        enhanceTable(tblUsers, jScrollPane2);

        btnUpdateUser.setEnabled(false);
        btnEditUser.setEnabled(false);
        btnDelete.setEnabled(false);

        selectedUserId = -1;
        editingMode = false;

        loadUsers();
        enableStatusToggle();
        enableRowSelection();
        initializeSearchListener();
        hookRefreshButtonIfPresent();

        initializeLogHistoryTrigger();
    }

    private void applyModuleTheme() {
        setBackground(new java.awt.Color(240, 246, 255));

        // same section colors as ProductsManagementPanel
        pnlUserInformation.setBackground(new java.awt.Color(132, 164, 224));
        pnlUserList.setBackground(new java.awt.Color(132, 164, 224));
        jPanel1.setBackground(new java.awt.Color(18, 48, 174));
        jPanel4.setBackground(new java.awt.Color(18, 48, 174));

        if (jLabel4 != null) {
            jLabel4.setForeground(new Color(88, 105, 136));
        }

        styleTextField(txtFirstName);
        styleTextField(txtMiddleName);
        styleTextField(txtLastName);
        styleTextField(txtUsername);
        stylePasswordField(txtPassword);
        styleTextField(txtSearchUser);

        styleComboBox(cmbRole);

        styleButton(btnAddUser, new java.awt.Color(34, 166, 82));
        styleButton(btnUpdateUser, new java.awt.Color(52, 120, 246));
        styleButton(btnDelete, new java.awt.Color(220, 53, 69));
        styleButton(btnEditUser, new java.awt.Color(243, 156, 18));
        styleButton(btnRefreshAll, new java.awt.Color(89, 92, 255));

        installRoundedButtonPainter(btnAddUser);
        installRoundedButtonPainter(btnUpdateUser);
        installRoundedButtonPainter(btnDelete);
        installRoundedButtonPainter(btnEditUser);
        installRoundedButtonPainter(btnRefreshAll);

        if (jdialogLogHistory != null) {
            jdialogLogHistory.setCursor(new Cursor(Cursor.HAND_CURSOR));
            jdialogLogHistory.setToolTipText("Open Log History");
        }
    }

    private void stylePasswordField(javax.swing.JPasswordField field) {
        if (field == null) {
            return;
        }

        field.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 13));
        field.setBackground(java.awt.Color.WHITE);
        field.setForeground(new java.awt.Color(35, 48, 68));
        field.setCaretColor(new java.awt.Color(18, 48, 174));
        field.setMargin(new java.awt.Insets(1, 8, 1, 8));

        field.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new java.awt.Color(200, 214, 235), 1, true),
                new javax.swing.border.EmptyBorder(2, 8, 2, 8)
        ));

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        new javax.swing.border.LineBorder(new java.awt.Color(89, 92, 255), 2, true),
                        new javax.swing.border.EmptyBorder(1, 7, 1, 7)
                ));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        new javax.swing.border.LineBorder(new java.awt.Color(200, 214, 235), 1, true),
                        new javax.swing.border.EmptyBorder(2, 8, 2, 8)
                ));
            }
        });
    }

    private void styleTextField(javax.swing.JTextField field) {
        if (field == null) {
            return;
        }

        field.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 13));
        field.setBackground(java.awt.Color.WHITE);
        field.setForeground(new java.awt.Color(35, 48, 68));
        field.setCaretColor(new java.awt.Color(18, 48, 174));
        field.setMargin(new java.awt.Insets(1, 8, 1, 8));

        field.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new java.awt.Color(200, 214, 235), 1, true),
                new javax.swing.border.EmptyBorder(2, 8, 2, 8)
        ));

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        new javax.swing.border.LineBorder(new java.awt.Color(89, 92, 255), 2, true),
                        new javax.swing.border.EmptyBorder(1, 7, 1, 7)
                ));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        new javax.swing.border.LineBorder(new java.awt.Color(200, 214, 235), 1, true),
                        new javax.swing.border.EmptyBorder(2, 8, 2, 8)
                ));
            }
        });
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
                new LineBorder(new Color(200, 214, 235), 1, true),
                new EmptyBorder(1, 6, 1, 6)
        ));
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

    private void installValidationFilters() {
        KeyAdapter nameAdapter = new KeyAdapter() {
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

                JTextField source = (JTextField) e.getSource();
                if (!allowed || source.getText().length() >= 50) {
                    e.consume();
                }
            }
        };

        txtFirstName.addKeyListener(nameAdapter);
        txtMiddleName.addKeyListener(nameAdapter);
        txtLastName.addKeyListener(nameAdapter);

        txtUsername.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (Character.isISOControl(c)) {
                    return;
                }

                boolean allowed = Character.isLetterOrDigit(c)
                        || c == '_'
                        || c == '.';

                if (!allowed || txtUsername.getText().length() >= 30) {
                    e.consume();
                }
            }
        });

        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (txtPassword.getPassword().length >= 50) {
                    e.consume();
                }
            }
        });
    }

    private String normalizeSpaces(String text) {
        return text == null ? "" : text.trim().replaceAll("\\s{2,}", " ");
    }

    private boolean isValidUsername(String username) {
        return username.matches("^[A-Za-z0-9_.]{4,30}$");
    }

    private boolean isStrongPassword(String password) {
        return password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,50}$");
    }

    private boolean isUsernameDuplicate(String username, int excludeUserId) {
        String sql = "SELECT user_id FROM users WHERE LOWER(username) = LOWER(?)";
        if (excludeUserId != -1) {
            sql += " AND user_id != ?";
        }

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            if (excludeUserId != -1) {
                ps.setInt(2, excludeUserId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Validation error: " + e.getMessage());
            return true;
        }
    }

    private int countUsersByRole(String role, int excludeUserId) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        if (excludeUserId != -1) {
            sql += " AND user_id != ?";
        }

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, role);

            if (excludeUserId != -1) {
                ps.setInt(2, excludeUserId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Validation error: " + e.getMessage());
        }

        return 0;
    }

    private boolean isSingleInstanceRoleTaken(String role, int excludeUserId) {
        if (!"Super Admin".equalsIgnoreCase(role) && !"Manager".equalsIgnoreCase(role)) {
            return false;
        }
        return countUsersByRole(role, excludeUserId) >= 1;
    }

    private boolean isDeletingOwnAccount() {
        return selectedUserId != -1 && selectedUserId == loggedInUserId;
    }

    private void insertLog(String action) {
        String sql = "INSERT INTO user_logs (username, user_role, action) VALUES (?, ?, ?)";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, loggedInUserId <= 0 ? "Unknown" : getLoggedInUsername());
            ps.setString(2, loggedInUserRole == null ? "" : loggedInUserRole);
            ps.setString(3, action);

            ps.executeUpdate();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to save log: " + e.getMessage());
        }
    }

    private String getLoggedInUsername() {
        String sql = "SELECT username FROM users WHERE user_id = ? LIMIT 1";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, loggedInUserId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }

        } catch (Exception e) {
            logger.log(java.util.logging.Level.WARNING, "Failed to get logged-in username", e);
        }

        return "Unknown";
    }

    private void loadLogHistory() {
        if (logHistoryModel == null) {
            return;
        }

        logHistoryModel.setRowCount(0);

        String sql = "SELECT log_datetime, username, user_role, action FROM user_logs ORDER BY log_datetime DESC";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                logHistoryModel.addRow(new Object[]{
                    rs.getTimestamp("log_datetime"),
                    rs.getString("username"),
                    rs.getString("user_role"),
                    rs.getString("action")
                });
            }

            if (lblLogEntryCount != null) {
                int count = logHistoryModel.getRowCount();
                if (count == 0) {
                    lblLogEntryCount.setText("Showing 0-0 of 0 entries");
                } else {
                    lblLogEntryCount.setText("Showing 1-" + count + " of " + count + " entries");
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load log history: " + e.getMessage());
        }
    }

    private void initializeLogHistoryTrigger() {
        if (jdialogLogHistory == null) {
            return;
        }

        jdialogLogHistory.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showLogHistoryDialog();
            }
        });
    }

    private void showLogHistoryDialog() {
        if (logHistoryDialog == null) {
            Window owner = SwingUtilities.getWindowAncestor(this);

            if (owner instanceof Frame) {
                logHistoryDialog = new JDialog((Frame) owner, "Log History", true);
            } else if (owner instanceof Dialog) {
                logHistoryDialog = new JDialog((Dialog) owner, "Log History", true);
            } else {
                logHistoryDialog = new JDialog((Frame) null, "Log History", true);
            }

            logHistoryDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            JPanel root = new JPanel(new BorderLayout(12, 12));
            root.setBackground(Color.WHITE);
            root.setBorder(new EmptyBorder(16, 16, 16, 16));

            JLabel title = new JLabel("Log History");
            title.setFont(new Font("Tahoma", Font.BOLD, 22));
            title.setForeground(new Color(35, 48, 68));

            txtSearchLog = new JTextField();
            txtSearchLog.setFont(new Font("Tahoma", Font.PLAIN, 13));
            txtSearchLog.setPreferredSize(new Dimension(240, 36));
            txtSearchLog.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(210, 220, 235), 1, true),
                    new EmptyBorder(6, 10, 6, 10)
            ));

            lblLogEntryCount = new JLabel("Showing 0-0 of 0 entries");
            lblLogEntryCount.setFont(new Font("Tahoma", Font.PLAIN, 12));
            lblLogEntryCount.setForeground(new Color(100, 110, 130));

            JPanel topPanel = new JPanel(new BorderLayout(10, 10));
            topPanel.setOpaque(false);
            topPanel.add(title, BorderLayout.WEST);
            topPanel.add(txtSearchLog, BorderLayout.EAST);

            JPanel infoPanel = new JPanel(new BorderLayout());
            infoPanel.setOpaque(false);
            infoPanel.add(lblLogEntryCount, BorderLayout.WEST);

            JPanel northPanel = new JPanel(new BorderLayout(0, 8));
            northPanel.setOpaque(false);
            northPanel.add(topPanel, BorderLayout.NORTH);
            northPanel.add(infoPanel, BorderLayout.SOUTH);

            logHistoryModel = new DefaultTableModel(
                    new Object[]{"Date & Time", "Username", "UserRole", "Action"}, 0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            tblLogHistory = new JTable(logHistoryModel);
            tblLogHistory.setFont(new Font("Tahoma", Font.PLAIN, 13));
            tblLogHistory.setRowHeight(34);
            tblLogHistory.setGridColor(new Color(230, 235, 245));
            tblLogHistory.setSelectionBackground(new Color(225, 235, 250));
            tblLogHistory.setSelectionForeground(new Color(35, 48, 68));
            tblLogHistory.setShowGrid(true);
            tblLogHistory.setFillsViewportHeight(true);
            tblLogHistory.setBackground(Color.WHITE);
            tblLogHistory.setForeground(new Color(35, 48, 68));

            JTableHeader header = tblLogHistory.getTableHeader();
            header.setFont(new Font("Tahoma", Font.BOLD, 13));
            header.setBackground(new Color(245, 247, 250));
            header.setForeground(new Color(35, 48, 68));
            header.setPreferredSize(new Dimension(header.getWidth(), 36));
            header.setReorderingAllowed(false);

            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

            for (int i = 0; i < tblLogHistory.getColumnCount(); i++) {
                tblLogHistory.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }

            tblLogHistory.getColumnModel().getColumn(0).setPreferredWidth(180);
            tblLogHistory.getColumnModel().getColumn(1).setPreferredWidth(140);
            tblLogHistory.getColumnModel().getColumn(2).setPreferredWidth(120);
            tblLogHistory.getColumnModel().getColumn(3).setPreferredWidth(420);

            logHistorySorter = new TableRowSorter<>(logHistoryModel);
            tblLogHistory.setRowSorter(logHistorySorter);

            txtSearchLog.getDocument().addDocumentListener(new DocumentListener() {
                private void filter() {
                    String text = txtSearchLog.getText().trim();
                    if (text.isEmpty()) {
                        logHistorySorter.setRowFilter(null);
                    } else {
                        logHistorySorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
                    }
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    filter();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    filter();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    filter();
                }
            });

            JScrollPane scrollPane = new JScrollPane(tblLogHistory);
            scrollPane.setBorder(new LineBorder(new Color(220, 225, 235), 1, true));
            scrollPane.getViewport().setBackground(Color.WHITE);

            JButton btnClose = new JButton("Close");
            btnClose.setFont(new Font("Tahoma", Font.BOLD, 13));
            btnClose.setFocusPainted(false);
            btnClose.setBackground(new Color(34, 166, 82)); // green
            btnClose.setForeground(Color.WHITE);
            btnClose.setPreferredSize(new Dimension(100, 36));
            btnClose.addActionListener(e -> {
                if (logHistoryDialog != null) {
                    logHistoryDialog.setVisible(false);
                    logHistoryDialog.dispose();
                    logHistoryDialog = null;
                }
            });

            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setOpaque(false);
            bottomPanel.add(btnClose, BorderLayout.EAST);

            root.add(northPanel, BorderLayout.NORTH);
            root.add(scrollPane, BorderLayout.CENTER);
            root.add(bottomPanel, BorderLayout.SOUTH);

            logHistoryDialog.setContentPane(root);
            logHistoryDialog.setSize(900, 500);
            logHistoryDialog.setLocationRelativeTo(this);
            logHistoryDialog.setResizable(false);
            logHistoryDialog.getRootPane().setDefaultButton(btnClose);
        }

        loadLogHistory();
        logHistoryDialog.setVisible(true);
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

                    if (text.equalsIgnoreCase("Super Admin")) {
                        label.setBackground(new Color(213, 227, 246));
                        label.setForeground(new Color(23, 63, 107));
                    } else if (text.equalsIgnoreCase("Manager")) {
                        label.setBackground(new Color(221, 234, 249));
                        label.setForeground(new Color(30, 79, 123));
                    } else if (text.equalsIgnoreCase("Cashier")) {
                        label.setBackground(new Color(228, 239, 251));
                        label.setForeground(new Color(36, 91, 132));
                    } else if (text.equalsIgnoreCase("Inventory Clerk")) {
                        label.setBackground(new Color(233, 242, 252));
                        label.setForeground(new Color(47, 97, 134));
                    } else {
                        label.setBackground(new Color(236, 243, 250));
                        label.setForeground(new Color(59, 90, 119));
                    }
                }

                if (columnName.contains("status")) {
                    label.setFont(pillFont);
                    label.setText("  " + text + "  ");

                    if (text.equalsIgnoreCase("Active")) {
                        label.setBackground(new Color(212, 236, 247));
                        label.setForeground(new Color(23, 91, 120));
                    } else if (text.equalsIgnoreCase("Inactive")) {
                        label.setBackground(new Color(229, 235, 244));
                        label.setForeground(new Color(86, 98, 118));
                    } else {
                        label.setBackground(new Color(234, 240, 247));
                        label.setForeground(new Color(78, 95, 116));
                    }
                }

                return label;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(luxuryRenderer);
        }

        if (table.getColumnCount() >= 7) {
            table.getColumnModel().getColumn(0).setPreferredWidth(70);
            table.getColumnModel().getColumn(1).setPreferredWidth(120);
            table.getColumnModel().getColumn(2).setPreferredWidth(120);
            table.getColumnModel().getColumn(3).setPreferredWidth(120);
            table.getColumnModel().getColumn(4).setPreferredWidth(150);
            table.getColumnModel().getColumn(5).setPreferredWidth(150);
            table.getColumnModel().getColumn(6).setPreferredWidth(110);
        }

        scrollPane.setVerticalScrollBar(new ModernScrollBar());
        scrollPane.setHorizontalScrollBar(new ModernScrollBar());

        wrapScrollPaneInCard(scrollPane);

        TableRowSorter<DefaultTableModel> sorter
                = new TableRowSorter<>((DefaultTableModel) table.getModel());
        table.setRowSorter(sorter);
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

    private void initRoleDropdown() {
        cmbRole.removeAllItems();
        cmbRole.addItem("Super Admin");
        cmbRole.addItem("Manager");
        cmbRole.addItem("Cashier");
        cmbRole.addItem("Inventory Clerk");

        // RBAC: Restrict role selection for non-Super Admin
        if (!"Super Admin".equalsIgnoreCase(loggedInUserRole)) {
            cmbRole.removeItem("Super Admin");
        }

        cmbRole.setSelectedIndex(0);
    }

    private void hookRefreshButtonIfPresent() {
        JButton refreshButton = findButtonByNameOrText(this, "btnRefresh", "Refresh");
        if (refreshButton == null) {
            return;
        }

        if (Boolean.TRUE.equals(refreshButton.getClientProperty("refreshHookInstalled"))) {
            return;
        }

        refreshButton.putClientProperty("refreshHookInstalled", Boolean.TRUE);
        refreshButton.addActionListener(e -> {
            clearForm();
            txtSearchUser.setText("");
            loadUsers();
        });
    }

    private JButton findButtonByNameOrText(Container root, String buttonName, String buttonText) {
        if (root == null) {
            return null;
        }

        for (Component component : root.getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                String name = button.getName();
                String text = button.getText();

                if ((name != null && name.equals(buttonName))
                        || (text != null && text.trim().equalsIgnoreCase(buttonText))) {
                    return button;
                }
            }

            if (component instanceof Container) {
                JButton nested = findButtonByNameOrText((Container) component, buttonName, buttonText);
                if (nested != null) {
                    return nested;
                }
            }
        }

        return null;
    }

    private void initializeSearchListener() {
        txtSearchUser.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchUsers();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchUsers();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchUsers();
            }
        });
    }

    private void loadUsers() {
        DefaultTableModel model = (DefaultTableModel) tblUsers.getModel();
        model.setRowCount(0);

        tblUsers.clearSelection();
        selectedUserId = -1;
        editingMode = false;

        String sql = "SELECT user_id, first_name, middle_name, last_name, username, role, status "
                + "FROM users ORDER BY user_id ASC";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("user_id"),
                    rs.getString("first_name"),
                    rs.getString("middle_name"),
                    rs.getString("last_name"),
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getString("status")
                });
            }

            if (tblUsers.getRowSorter() instanceof TableRowSorter) {
                @SuppressWarnings("unchecked")
                TableRowSorter<DefaultTableModel> sorter
                        = (TableRowSorter<DefaultTableModel>) tblUsers.getRowSorter();
                sorter.setSortKeys(null);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Load users failed: " + e.getMessage());
        }
    }

    private void searchUsers() {
        String keyword = txtSearchUser.getText().trim();

        if (keyword.isEmpty()) {
            loadUsers();
            return;
        }

        DefaultTableModel model = (DefaultTableModel) tblUsers.getModel();
        model.setRowCount(0);

        tblUsers.clearSelection();
        selectedUserId = -1;
        editingMode = false;

        String sql = "SELECT user_id, first_name, middle_name, last_name, username, role, status "
                + "FROM users WHERE "
                + "first_name LIKE ? OR "
                + "middle_name LIKE ? OR "
                + "last_name LIKE ? OR "
                + "username LIKE ? OR "
                + "role LIKE ? OR "
                + "status LIKE ? "
                + "ORDER BY user_id ASC";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= 6; i++) {
                ps.setString(i, "%" + keyword + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("user_id"),
                        rs.getString("first_name"),
                        rs.getString("middle_name"),
                        rs.getString("last_name"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("status")
                    });
                }
            }

            if (tblUsers.getRowSorter() instanceof TableRowSorter) {
                @SuppressWarnings("unchecked")
                TableRowSorter<DefaultTableModel> sorter
                        = (TableRowSorter<DefaultTableModel>) tblUsers.getRowSorter();
                sorter.setSortKeys(null);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void refreshUserTable() {
        tblUsers.clearSelection();
        selectedUserId = -1;
        editingMode = false;

        if (txtSearchUser.getText().trim().isEmpty()) {
            loadUsers();
        } else {
            searchUsers();
        }
    }

    private void enableRowSelection() {
        tblUsers.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int viewRow = tblUsers.getSelectedRow();
                if (viewRow < 0) {
                    return;
                }

                int modelRow = tblUsers.convertRowIndexToModel(viewRow);
                Object value = tblUsers.getModel().getValueAt(modelRow, 0);
                if (value == null) {
                    return;
                }

                selectedUserId = Integer.parseInt(value.toString());

                btnEditUser.setEnabled(true);
                btnDelete.setEnabled(true);
                btnUpdateUser.setEnabled(false);
                editingMode = false;
            }
        });
    }

    private void enableStatusToggle() {
        tblUsers.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = tblUsers.rowAtPoint(e.getPoint());
                int viewCol = tblUsers.columnAtPoint(e.getPoint());

                if (viewRow >= 0 && viewCol == 6) {
                    int modelRow = tblUsers.convertRowIndexToModel(viewRow);

                    int id = Integer.parseInt(tblUsers.getModel().getValueAt(modelRow, 0).toString());
                    String status = tblUsers.getModel().getValueAt(modelRow, 6).toString();
                    String newStatus = status.equalsIgnoreCase("Active") ? "Inactive" : "Active";

                    int confirm = JOptionPane.showConfirmDialog(
                            UserManagementPanel.this,
                            "Set user " + newStatus + "?",
                            "Confirm",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (confirm == JOptionPane.YES_OPTION) {
                        updateStatus(id, newStatus);
                    }
                }
            }
        });
    }

    private void updateStatus(int id, String status) {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();

            insertLog("Changed status of user ID " + id + " to " + status);
            refreshUserTable();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void clearForm() {
        txtFirstName.setText("");
        txtMiddleName.setText("");
        txtLastName.setText("");
        txtUsername.setText("");
        txtPassword.setText("");

        cmbRole.setSelectedIndex(0);

        selectedUserId = -1;
        editingMode = false;

        btnUpdateUser.setEnabled(false);
        btnEditUser.setEnabled(false);
        btnDelete.setEnabled(false);

        tblUsers.clearSelection();
        txtFirstName.requestFocus();
    }

    private boolean validateNameField(String value, String fieldName, boolean required) {
        String text = value.trim();

        if (required && text.isEmpty()) {
            JOptionPane.showMessageDialog(this, fieldName + " is required.");
            return false;
        }

        if (!required && text.isEmpty()) {
            return true;
        }

        if (!text.matches("^[A-Za-z][A-Za-z\\s'-]*$")) {
            JOptionPane.showMessageDialog(this, fieldName + " must contain letters only.");
            return false;
        }

        return true;
    }

    private boolean validateForm() {
        String firstName = normalizeSpaces(txtFirstName.getText());
        String middleName = normalizeSpaces(txtMiddleName.getText());
        String lastName = normalizeSpaces(txtLastName.getText());
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        Object role = cmbRole.getSelectedItem();

        txtFirstName.setText(firstName);
        txtMiddleName.setText(middleName);
        txtLastName.setText(lastName);
        txtUsername.setText(username);

        if (!validateNameField(firstName, "First Name", true)) {
            return false;
        }
        if (!validateNameField(middleName, "Middle Name", false)) {
            return false;
        }
        if (!validateNameField(lastName, "Last Name", true)) {
            return false;
        }

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username is required.");
            txtUsername.requestFocus();
            return false;
        }

        if (!isValidUsername(username)) {
            JOptionPane.showMessageDialog(this, "Username must be 4-30 characters and only use letters, numbers, underscore, or dot.");
            txtUsername.requestFocus();
            return false;
        }

        if (isUsernameDuplicate(username, selectedUserId)) {
            JOptionPane.showMessageDialog(this, "Username already exists.");
            txtUsername.requestFocus();
            return false;
        }

        if (password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password is required.");
            txtPassword.requestFocus();
            return false;
        }

        if (!isStrongPassword(password)) {
            JOptionPane.showMessageDialog(this, "Password must be at least 8 characters and contain both letters and numbers.");
            txtPassword.requestFocus();
            return false;
        }

        if (role == null || role.toString().trim().isEmpty() || role.toString().equalsIgnoreCase("Select Role")) {
            JOptionPane.showMessageDialog(this, "Please select a valid role.");
            cmbRole.requestFocus();
            return false;
        }
        String selectedRole = role.toString().trim();

        if (isSingleInstanceRoleTaken(selectedRole, selectedUserId)) {
            JOptionPane.showMessageDialog(this, "Only one " + selectedRole + " is allowed in the system.");
            cmbRole.requestFocus();
            return false;
        }

        return true;
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

    private void updateLogHistoryCount() {
        if (lblLogEntryCount == null || tblLogHistory == null || logHistoryModel == null) {
            return;
        }

        int visible = tblLogHistory.getRowCount();
        int total = logHistoryModel.getRowCount();

        if (visible == 0) {
            lblLogEntryCount.setText("Showing 0-0 of " + total + " entries");
        } else {
            lblLogEntryCount.setText("Showing 1-" + visible + " of " + total + " entries");
        }
    }

    private void insertLog(String username, String userRole, String action) {
        String sql = "INSERT INTO user_logs (username, user_role, action) VALUES (?, ?, ?)";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, userRole);
            ps.setString(3, action);
            ps.executeUpdate();

        } catch (Exception e) {
            logger.warning("Log insert failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlUserInformation = new RoundedPanel();
        lblFirstName = new javax.swing.JLabel();
        lblMiddleName = new javax.swing.JLabel();
        lblLastName = new javax.swing.JLabel();
        lblUsername = new javax.swing.JLabel();
        txtFirstName = new javax.swing.JTextField();
        txtMiddleName = new javax.swing.JTextField();
        txtLastName = new javax.swing.JTextField();
        txtUsername = new javax.swing.JTextField();
        txtPassword = new javax.swing.JPasswordField();
        cmbRole = new javax.swing.JComboBox<>();
        lblRole = new javax.swing.JLabel();
        lblPassword = new javax.swing.JLabel();
        lblPersonalDetails = new javax.swing.JLabel();
        btnUpdateUser = new javax.swing.JButton();
        btnAddUser = new javax.swing.JButton();
        btnRefreshAll = new javax.swing.JButton();
        pnlUserList = new RoundedPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblUsers = new javax.swing.JTable();
        jLabel1 = new point.of.sale.system.classes.GradientFont();
        jLabel4 = new javax.swing.JLabel();
        jPanel4 = new RoundedPanel();
        jLabel2 = new javax.swing.JLabel("Add New User") {
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
        jLabel3 = new javax.swing.JLabel();
        txtSearchUser = new javax.swing.JTextField();
        btnEditUser = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        jdialogLogHistory = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jPanel1 = new RoundedPanel();
        lblAddNewUser = new javax.swing.JLabel("Add New User") {
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
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator1 = new javax.swing.JSeparator();

        setBackground(new java.awt.Color(240, 248, 255));
        setPreferredSize(new java.awt.Dimension(1120, 820));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlUserInformation.setBackground(new java.awt.Color(122, 170, 206));
        pnlUserInformation.setMinimumSize(new java.awt.Dimension(920, 260));
        pnlUserInformation.setPreferredSize(new java.awt.Dimension(960, 280));
        pnlUserInformation.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblFirstName.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        lblFirstName.setForeground(new java.awt.Color(40, 55, 80));
        lblFirstName.setText("First Name:");
        pnlUserInformation.add(lblFirstName, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 80, -1, -1));

        lblMiddleName.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        lblMiddleName.setForeground(new java.awt.Color(40, 55, 80));
        lblMiddleName.setText("Middle Name:");
        pnlUserInformation.add(lblMiddleName, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 80, -1, -1));

        lblLastName.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        lblLastName.setForeground(new java.awt.Color(40, 55, 80));
        lblLastName.setText("Last Name:");
        pnlUserInformation.add(lblLastName, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 80, -1, -1));

        lblUsername.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        lblUsername.setForeground(new java.awt.Color(40, 55, 80));
        lblUsername.setText("Username:");
        pnlUserInformation.add(lblUsername, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 160, -1, -1));

        txtFirstName.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        pnlUserInformation.add(txtFirstName, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 100, 230, 40));

        txtMiddleName.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        pnlUserInformation.add(txtMiddleName, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 100, 230, 40));

        txtLastName.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        pnlUserInformation.add(txtLastName, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 100, 230, 40));

        txtUsername.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        pnlUserInformation.add(txtUsername, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 180, 230, 40));

        txtPassword.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        pnlUserInformation.add(txtPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 180, 230, 40));

        cmbRole.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        pnlUserInformation.add(cmbRole, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 180, 230, 40));

        lblRole.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        lblRole.setForeground(new java.awt.Color(40, 55, 80));
        lblRole.setText("Role:");
        pnlUserInformation.add(lblRole, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 160, -1, -1));

        lblPassword.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        lblPassword.setForeground(new java.awt.Color(40, 55, 80));
        lblPassword.setText("Password:");
        pnlUserInformation.add(lblPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 160, -1, -1));

        lblPersonalDetails.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblPersonalDetails.setForeground(new java.awt.Color(10, 25, 47));
        lblPersonalDetails.setText("Personal Details");
        pnlUserInformation.add(lblPersonalDetails, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 200, -1));

        btnUpdateUser.setBackground(new java.awt.Color(0, 98, 193));
        btnUpdateUser.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnUpdateUser.setForeground(new java.awt.Color(255, 255, 255));
        btnUpdateUser.setText("Update User");
        btnUpdateUser.setBorderPainted(false);
        btnUpdateUser.setFocusCycleRoot(true);
        btnUpdateUser.setFocusPainted(false);
        btnUpdateUser.addActionListener(this::btnUpdateUserActionPerformed);
        pnlUserInformation.add(btnUpdateUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 150, 120, 30));

        btnAddUser.setBackground(new java.awt.Color(0, 166, 37));
        btnAddUser.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnAddUser.setForeground(new java.awt.Color(255, 255, 255));
        btnAddUser.setText("Add User");
        btnAddUser.setBorderPainted(false);
        btnAddUser.setFocusPainted(false);
        btnAddUser.addActionListener(this::btnAddUserActionPerformed);
        pnlUserInformation.add(btnAddUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 110, 120, 30));

        btnRefreshAll.setBackground(new java.awt.Color(44, 62, 80));
        btnRefreshAll.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnRefreshAll.setForeground(new java.awt.Color(255, 255, 255));
        btnRefreshAll.setText("Refresh");
        btnRefreshAll.addActionListener(this::btnRefreshAllActionPerformed);
        pnlUserInformation.add(btnRefreshAll, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 190, -1, 30));

        add(pnlUserInformation, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 140, 1100, 270));

        pnlUserList.setBackground(new java.awt.Color(122, 170, 206));
        pnlUserList.setMinimumSize(new java.awt.Dimension(920, 260));
        pnlUserList.setPreferredSize(new java.awt.Dimension(960, 280));
        pnlUserList.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblUsers.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        tblUsers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "First Name", "Middle Name", "Last Name", "Username", "Role", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblUsers.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(tblUsers);
        if (tblUsers.getColumnModel().getColumnCount() > 0) {
            tblUsers.getColumnModel().getColumn(0).setResizable(false);
            tblUsers.getColumnModel().getColumn(0).setPreferredWidth(40);
            tblUsers.getColumnModel().getColumn(1).setResizable(false);
            tblUsers.getColumnModel().getColumn(1).setPreferredWidth(200);
            tblUsers.getColumnModel().getColumn(2).setResizable(false);
            tblUsers.getColumnModel().getColumn(2).setPreferredWidth(200);
            tblUsers.getColumnModel().getColumn(3).setResizable(false);
            tblUsers.getColumnModel().getColumn(3).setPreferredWidth(200);
            tblUsers.getColumnModel().getColumn(4).setResizable(false);
            tblUsers.getColumnModel().getColumn(4).setPreferredWidth(180);
            tblUsers.getColumnModel().getColumn(5).setResizable(false);
            tblUsers.getColumnModel().getColumn(5).setPreferredWidth(150);
            tblUsers.getColumnModel().getColumn(6).setResizable(false);
            tblUsers.getColumnModel().getColumn(6).setPreferredWidth(150);
        }

        pnlUserList.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 1080, 310));

        add(pnlUserList, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 470, 1100, 330));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel1.setText("USERS MANAGEMENT");
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 400, -1));

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel4.setText("Manage system users and their roles");
        add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, -1, -1));

        jPanel4.setBackground(new java.awt.Color(18, 48, 174));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 22)); // NOI18N
        jLabel2.setText("USERS LIST");
        jPanel4.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, -1, -1));

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Search:");
        jPanel4.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 20, -1, -1));

        txtSearchUser.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        txtSearchUser.addActionListener(this::txtSearchUserActionPerformed);
        jPanel4.add(txtSearchUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 17, 200, 25));

        btnEditUser.setBackground(new java.awt.Color(243, 156, 18));
        btnEditUser.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnEditUser.setForeground(new java.awt.Color(255, 255, 255));
        btnEditUser.setText("Edit User");
        btnEditUser.setBorderPainted(false);
        btnEditUser.setFocusPainted(false);
        btnEditUser.addActionListener(this::btnEditUserActionPerformed);
        jPanel4.add(btnEditUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 14, 110, 30));

        btnDelete.setBackground(new java.awt.Color(204, 0, 0));
        btnDelete.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnDelete.setForeground(new java.awt.Color(255, 255, 255));
        btnDelete.setText("Delete User");
        btnDelete.setFocusPainted(false);
        btnDelete.addActionListener(this::btnDeleteActionPerformed);
        jPanel4.add(btnDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(820, 14, 120, 30));

        jdialogLogHistory.setBackground(new java.awt.Color(18, 48, 174));
        jdialogLogHistory.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jdialogLogHistoryMouseClicked(evt);
            }
        });
        jdialogLogHistory.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText(" Click to view log history");
        jdialogLogHistory.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, 20));

        jPanel4.add(jdialogLogHistory, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 10, 180, 40));

        add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 410, 1100, 60));

        jPanel1.setBackground(new java.awt.Color(18, 48, 174));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblAddNewUser.setFont(new java.awt.Font("Tahoma", 1, 22)); // NOI18N
        lblAddNewUser.setText("Add New User");
        jPanel1.add(lblAddNewUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 10, -1, -1));

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, 270, 50));

        jSeparator3.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator3.setForeground(new java.awt.Color(0, 0, 0));
        add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 30, 670, 10));

        jSeparator1.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator1.setForeground(new java.awt.Color(0, 0, 0));
        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 40, 20, 90));
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddUserActionPerformed
        if (!validateForm()) {
            return;
        }

        String role = cmbRole.getSelectedItem().toString();

        // RBAC: Non-Super Admin cannot create Super Admin
        if (!"Super Admin".equalsIgnoreCase(loggedInUserRole) && "Super Admin".equalsIgnoreCase(role)) {
            JOptionPane.showMessageDialog(this, "Access denied: Cannot create Super Admin account.");
            return;
        }

        String firstName = normalizeSpaces(txtFirstName.getText());
        String middleName = normalizeSpaces(txtMiddleName.getText());
        String lastName = normalizeSpaces(txtLastName.getText());
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        String sql = "INSERT INTO users(first_name, middle_name, last_name, username, password, role, status) "
                + "VALUES(?,?,?,?,?,?,?)";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, firstName);
            ps.setString(2, middleName);
            ps.setString(3, lastName);
            ps.setString(4, username);
            ps.setString(5, hashedPassword);
            ps.setString(6, role);
            ps.setString(7, "Active");

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "User added successfully");
            insertLog("Added user: " + username + " (" + role + ")");

            clearForm();
            loadUsers();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Add failed: " + e.getMessage());
        }
    }//GEN-LAST:event_btnAddUserActionPerformed

    private void btnUpdateUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateUserActionPerformed
        if (selectedUserId == -1) {
            JOptionPane.showMessageDialog(this, "Select user first");
            return;
        }

        if (!validateForm()) {
            return;
        }

        String role = cmbRole.getSelectedItem().toString();

        // RBAC: Non-Super Admin cannot modify Super Admin accounts or assign Super Admin role
        if (!"Super Admin".equalsIgnoreCase(loggedInUserRole)) {
            try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement("SELECT role FROM users WHERE user_id = ?")) {

                ps.setInt(1, selectedUserId);
                ResultSet rs = ps.executeQuery();

                if (rs.next() && "Super Admin".equalsIgnoreCase(rs.getString("role"))) {
                    JOptionPane.showMessageDialog(this, "Access denied: Cannot modify Super Admin account.");
                    return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error checking user role: " + e.getMessage());
                return;
            }

            if ("Super Admin".equalsIgnoreCase(role)) {
                JOptionPane.showMessageDialog(this, "Access denied: Cannot assign Super Admin role.");
                return;
            }
        }

        String firstName = normalizeSpaces(txtFirstName.getText());
        String middleName = normalizeSpaces(txtMiddleName.getText());
        String lastName = normalizeSpaces(txtLastName.getText());
        String username = txtUsername.getText().trim();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Update this user?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "UPDATE users SET first_name=?, middle_name=?, last_name=?, username=?, role=? WHERE user_id=?";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, firstName);
            ps.setString(2, middleName);
            ps.setString(3, lastName);
            ps.setString(4, username);
            ps.setString(5, role);
            ps.setInt(6, selectedUserId);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "User updated successfully");
            } else {
                JOptionPane.showMessageDialog(this, "Update failed");
            }

            insertLog("Updated user ID " + selectedUserId + " to username: " + username + " (" + role + ")");

            clearForm();
            loadUsers();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Update failed: " + e.getMessage());
        }
    }//GEN-LAST:event_btnUpdateUserActionPerformed

    private void txtSearchUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchUserActionPerformed
        searchUsers();
    }//GEN-LAST:event_txtSearchUserActionPerformed

    private boolean isLastActiveSuperAdmin(int userId) {
        String roleSql = "SELECT role, status FROM users WHERE user_id = ?";
        String countSql = "SELECT COUNT(*) FROM users WHERE role = 'Super Admin' AND status = 'Active'";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement rolePs = con.prepareStatement(roleSql); PreparedStatement countPs = con.prepareStatement(countSql)) {

            rolePs.setInt(1, userId);

            try (ResultSet rs = rolePs.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                String role = rs.getString("role");
                String status = rs.getString("status");

                if (!"Super Admin".equalsIgnoreCase(role) || !"Active".equalsIgnoreCase(status)) {
                    return false;
                }
            }

            try (ResultSet rs2 = countPs.executeQuery()) {
                if (rs2.next()) {
                    return rs2.getInt(1) <= 1;
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Validation error: " + e.getMessage());
            return true;
        }

        return false;
    }

    private boolean isLastManager(int userId) {
        String roleSql = "SELECT role FROM users WHERE user_id = ?";
        String countSql = "SELECT COUNT(*) FROM users WHERE role = 'Manager'";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps1 = con.prepareStatement(roleSql); PreparedStatement ps2 = con.prepareStatement(countSql)) {

            ps1.setInt(1, userId);

            try (ResultSet rs = ps1.executeQuery()) {
                if (rs.next()) {
                    if (!"Manager".equalsIgnoreCase(rs.getString("role"))) {
                        return false;
                    }
                }
            }

            try (ResultSet rs2 = ps2.executeQuery()) {
                if (rs2.next()) {
                    return rs2.getInt(1) <= 1;
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            return true;
        }

        return false;
    }

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        if (selectedUserId == -1) {
            JOptionPane.showMessageDialog(this, "Select user first");
            return;
        }

        // 🔥 BLOCK SELF DELETE (SUPER ADMIN ONLY)
        if ("Super Admin".equalsIgnoreCase(loggedInUserRole) && isDeletingOwnAccount()) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own account while logged in as Super Admin.");
            return;
        }

        // 🔥 BLOCK LAST SUPER ADMIN
        if (isLastActiveSuperAdmin(selectedUserId)) {
            JOptionPane.showMessageDialog(this, "You cannot delete the last active Super Admin.");
            return;
        }

        if (isLastManager(selectedUserId)) {
            JOptionPane.showMessageDialog(this, "You cannot delete the last Manager.");
            return;
        }

        // RBAC: Non-Super Admin cannot delete Super Admin accounts
        if (!"Super Admin".equalsIgnoreCase(loggedInUserRole)) {
            try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement("SELECT role FROM users WHERE user_id = ?")) {
                ps.setInt(1, selectedUserId);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && "Super Admin".equalsIgnoreCase(rs.getString("role"))) {
                    JOptionPane.showMessageDialog(this, "Access denied: Cannot delete Super Admin accounts.");
                    return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error checking user role: " + e.getMessage());
                return;
            }
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete this user?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection con = DBConnection.dbConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, selectedUserId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "User deleted successfully");
            insertLog("Deleted user ID " + selectedUserId);

            clearForm();
            loadUsers();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Delete failed: " + e.getMessage());
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnEditUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditUserActionPerformed
        if (selectedUserId == -1) {
            JOptionPane.showMessageDialog(this, "Select a user first");
            return;
        }

        int viewRow = tblUsers.getSelectedRow();
        if (viewRow < 0) {
            return;
        }

        int modelRow = tblUsers.convertRowIndexToModel(viewRow);

        txtFirstName.setText(String.valueOf(tblUsers.getModel().getValueAt(modelRow, 1)));
        txtMiddleName.setText(String.valueOf(tblUsers.getModel().getValueAt(modelRow, 2)));
        txtLastName.setText(String.valueOf(tblUsers.getModel().getValueAt(modelRow, 3)));
        txtUsername.setText(String.valueOf(tblUsers.getModel().getValueAt(modelRow, 4)));
        cmbRole.setSelectedItem(String.valueOf(tblUsers.getModel().getValueAt(modelRow, 5)));

        txtPassword.setText("");
        editingMode = true;
        btnUpdateUser.setEnabled(true);
    }//GEN-LAST:event_btnEditUserActionPerformed

    private void btnRefreshAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshAllActionPerformed
        clearForm();
        loadUsers();
    }//GEN-LAST:event_btnRefreshAllActionPerformed

    private void jdialogLogHistoryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jdialogLogHistoryMouseClicked
        showLogHistoryDialog();
    }//GEN-LAST:event_jdialogLogHistoryMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddUser;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEditUser;
    private javax.swing.JButton btnRefreshAll;
    private javax.swing.JButton btnUpdateUser;
    private javax.swing.JComboBox<String> cmbRole;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JPanel jdialogLogHistory;
    private javax.swing.JLabel lblAddNewUser;
    private javax.swing.JLabel lblFirstName;
    private javax.swing.JLabel lblLastName;
    private javax.swing.JLabel lblMiddleName;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblPersonalDetails;
    private javax.swing.JLabel lblRole;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JPanel pnlUserInformation;
    private javax.swing.JPanel pnlUserList;
    private javax.swing.JTable tblUsers;
    private javax.swing.JTextField txtFirstName;
    private javax.swing.JTextField txtLastName;
    private javax.swing.JTextField txtMiddleName;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtSearchUser;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
