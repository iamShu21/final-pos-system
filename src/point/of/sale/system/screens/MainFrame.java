package point.of.sale.system.screens;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import point.of.sale.system.classes.SidebarItemPanel;

public class MainFrame extends javax.swing.JFrame {

    private int userId;
    private Timer dateTimeTimer;
    private final String loggedInUsername;
    private final String loggedInUserRole;

    private DashboardPanel dashboardPanel;
    private UserManagementPanel userManagementPanel;
    private CategoryManagementPanel categoryManagementPanel;
    private SupplierManagementPanel supplierManagementPanel;
    private ProductsManagementPanel productsManagementPanel;
    private InventoryManagementPanel inventoryManagementPanel;
    private POSControllerPanel posControllerPanel;
    private SalesHistoryPanel salesHistoryPanel;
    private ReportsPanel reportsPanel;

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MainFrame.class.getName());

    public MainFrame() {
        this(0, "Username", "User Role");
    }

    public MainFrame(String username, String userRole) {
        this(0, username, userRole);
    }

    public MainFrame(int userId, String username, String userRole) {
        initComponents();

        this.userId = userId;
        this.loggedInUsername = username;
        this.loggedInUserRole = userRole;

        setLoggedInUserInfo();
        loadPanels();
        showPanel("dashboard");
        startDateTimeTimer();
        setupSidebarHoverEffects();
        setActiveNav(navDashboard);
    }

    private void setLoggedInUserInfo() {
        lblAutomatedUsername.setText(loggedInUsername);
        lblAutomatedUserRole.setText(loggedInUserRole);
    }

    private void loadPanels() {
        dashboardPanel = new DashboardPanel();
        userManagementPanel = new UserManagementPanel(userId, loggedInUserRole);
        categoryManagementPanel = new CategoryManagementPanel();
        supplierManagementPanel = new SupplierManagementPanel();
        productsManagementPanel = new ProductsManagementPanel();
        inventoryManagementPanel = new InventoryManagementPanel();
        salesHistoryPanel = new SalesHistoryPanel();
        reportsPanel = new ReportsPanel();
        posControllerPanel = new POSControllerPanel(userId, loggedInUsername, loggedInUserRole);

        // connect linked panels so POS refreshes them after a successful sale
        // use this if your POSControllerPanel has the 2-parameter version   

        posControllerPanel.setLinkedPanels(dashboardPanel, reportsPanel, salesHistoryPanel);
        

        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(userManagementPanel, "users");
        contentPanel.add(categoryManagementPanel, "categories");
        contentPanel.add(supplierManagementPanel, "suppliers");
        contentPanel.add(productsManagementPanel, "products");
        contentPanel.add(inventoryManagementPanel, "inventories");
        contentPanel.add(posControllerPanel, "pos");
        contentPanel.add(salesHistoryPanel, "salesHistory");
        contentPanel.add(reportsPanel, "reports");
    }

    private void showPanel(String name) {
        java.awt.CardLayout cl = (java.awt.CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, name);
    }

    private void startDateTimeTimer() {
        final SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy - hh:mm:ss a");

        dateTimeTimer = new Timer(1000, e -> lblDateTime.setText(sdf.format(new Date())));
        dateTimeTimer.start();
    }

    private void setupSidebarHoverEffects() {
        setupSingleNavHover(navDashboard);
        setupSingleNavHover(navUsers);
        setupSingleNavHover(navCategories);
        setupSingleNavHover(navSuppliers);
        setupSingleNavHover(navProducts);
        setupSingleNavHover(navInventory);
        setupSingleNavHover(navPOS);
        setupSingleNavHover(navSalesHistory);
        setupSingleNavHover(navReports);
    }

    private void setActiveNav(javax.swing.JPanel activePanel) {
        javax.swing.JPanel[] navItems = {
            navDashboard, navUsers, navCategories, navSuppliers,
            navProducts, navInventory, navPOS, navSalesHistory, navReports
        };

        for (javax.swing.JPanel panel : navItems) {
            if (panel instanceof SidebarItemPanel) {
                SidebarItemPanel item = (SidebarItemPanel) panel;
                item.setSelectedItem(false);
                item.setHovered(false);
            }
        }

        if (activePanel instanceof SidebarItemPanel) {
            SidebarItemPanel activeItem = (SidebarItemPanel) activePanel;
            activeItem.setSelectedItem(true);
        }
    }

    private void setupSingleNavHover(javax.swing.JPanel panel) {
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (panel instanceof SidebarItemPanel) {
                    SidebarItemPanel item = (SidebarItemPanel) panel;
                    if (!item.isSelectedItem()) {
                        item.setHovered(true);
                    }
                }
                panel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (panel instanceof SidebarItemPanel) {
                    SidebarItemPanel item = (SidebarItemPanel) panel;
                    if (!item.isSelectedItem()) {
                        item.setHovered(false);
                    }
                }
            }
        });
    }
    
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainContainer = new javax.swing.JPanel();
        sidebarPanel = new javax.swing.JPanel();
        brandPanel = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        lblSystemTitle = new javax.swing.JLabel();
        lblSystemSubtitle = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        menuPanel = new javax.swing.JPanel();
        navDashboard = new SidebarItemPanel();
        lblDashboardIcon = new javax.swing.JLabel();
        lblDashboardText = new javax.swing.JLabel();
        navUsers = new SidebarItemPanel();
        lblUsersIcon = new javax.swing.JLabel();
        lblUsersText = new javax.swing.JLabel();
        navCategories = new SidebarItemPanel();
        lblCategoriesIcon = new javax.swing.JLabel();
        lblCategoriesText = new javax.swing.JLabel();
        navSuppliers = new SidebarItemPanel();
        lblSuppliersIcon = new javax.swing.JLabel();
        lblSuppliersText = new javax.swing.JLabel();
        navProducts = new SidebarItemPanel();
        lblProductsIcon = new javax.swing.JLabel();
        lblProductsText = new javax.swing.JLabel();
        navInventory = new SidebarItemPanel();
        lblInventoryIcon = new javax.swing.JLabel();
        lblInventoryText = new javax.swing.JLabel();
        navPOS = new SidebarItemPanel();
        lblPOSIcon = new javax.swing.JLabel();
        lblPOSText = new javax.swing.JLabel();
        navSalesHistory = new SidebarItemPanel();
        lblSalesHistoryIcon = new javax.swing.JLabel();
        lblSalesHistoryText = new javax.swing.JLabel();
        navReports = new SidebarItemPanel();
        lblReportsIcon = new javax.swing.JLabel();
        lblReportsText = new javax.swing.JLabel();
        userInfoPanel = new SidebarItemPanel();
        lbluserIcon = new javax.swing.JLabel();
        lblAutomatedUsername = new javax.swing.JLabel();
        lblAutomatedUserRole = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        rightPanel = new javax.swing.JPanel();
        topbarPanel = new javax.swing.JPanel();
        lblDateTime = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        btnLogout = new javax.swing.JButton();
        contentPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        mainContainer.setBackground(new java.awt.Color(211, 218, 217));
        mainContainer.setPreferredSize(new java.awt.Dimension(1440, 900));
        mainContainer.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        sidebarPanel.setBackground(new java.awt.Color(17, 31, 162));
        sidebarPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        brandPanel.setBackground(new java.awt.Color(17, 31, 162));
        brandPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/point-of-service.png"))); // NOI18N
        brandPanel.add(lblLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 72, 70));

        lblSystemTitle.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblSystemTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblSystemTitle.setText("POS SYSTEM");
        brandPanel.add(lblSystemTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 20, 170, 30));

        lblSystemSubtitle.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblSystemSubtitle.setForeground(new java.awt.Color(255, 255, 255));
        lblSystemSubtitle.setText("POS ni Shuhari");
        brandPanel.add(lblSystemSubtitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 60, 130, -1));
        brandPanel.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 90, -1, 100));

        sidebarPanel.add(brandPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 320, 100));

        menuPanel.setBackground(new java.awt.Color(17, 31, 162));
        menuPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        navDashboard.setBackground(new java.awt.Color(17, 31, 162));
        navDashboard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navDashboardMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                navDashboardMouseEntered(evt);
            }
        });
        navDashboard.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblDashboardIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/dashboard.png"))); // NOI18N
        navDashboard.add(lblDashboardIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 40, 37));

        lblDashboardText.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblDashboardText.setForeground(new java.awt.Color(255, 255, 255));
        lblDashboardText.setText("Dashboard");
        navDashboard.add(lblDashboardText, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, 100, 37));

        menuPanel.add(navDashboard, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 280, 60));

        navUsers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navUsersMouseClicked(evt);
            }
        });
        navUsers.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblUsersIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/group.png"))); // NOI18N
        navUsers.add(lblUsersIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 40, 37));

        lblUsersText.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblUsersText.setForeground(new java.awt.Color(255, 255, 255));
        lblUsersText.setText("Users");
        navUsers.add(lblUsersText, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, 66, 37));

        menuPanel.add(navUsers, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 570, 280, 60));

        navCategories.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navCategoriesMouseClicked(evt);
            }
        });
        navCategories.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblCategoriesIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/list.png"))); // NOI18N
        lblCategoriesIcon.setToolTipText("");
        navCategories.add(lblCategoriesIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 40, 37));

        lblCategoriesText.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblCategoriesText.setForeground(new java.awt.Color(255, 255, 255));
        lblCategoriesText.setText("Categories");
        navCategories.add(lblCategoriesText, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, 90, 37));

        menuPanel.add(navCategories, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, 280, 60));

        navSuppliers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navSuppliersMouseClicked(evt);
            }
        });
        navSuppliers.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblSuppliersIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/supply-chain.png"))); // NOI18N
        navSuppliers.add(lblSuppliersIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 40, 37));

        lblSuppliersText.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblSuppliersText.setForeground(new java.awt.Color(255, 255, 255));
        lblSuppliersText.setText("Suppliers");
        navSuppliers.add(lblSuppliersText, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, 90, 37));

        menuPanel.add(navSuppliers, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 150, 280, 60));

        navProducts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navProductsMouseClicked(evt);
            }
        });
        navProducts.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblProductsIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/box.png"))); // NOI18N
        navProducts.add(lblProductsIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 40, 37));

        lblProductsText.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblProductsText.setForeground(new java.awt.Color(255, 255, 255));
        lblProductsText.setText("Products");
        navProducts.add(lblProductsText, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, 80, 37));

        menuPanel.add(navProducts, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 220, 280, 60));

        navInventory.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navInventoryMouseClicked(evt);
            }
        });
        navInventory.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblInventoryIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/inventory.png"))); // NOI18N
        navInventory.add(lblInventoryIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 40, 37));

        lblInventoryText.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblInventoryText.setForeground(new java.awt.Color(255, 255, 255));
        lblInventoryText.setText("Inventory");
        navInventory.add(lblInventoryText, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, 80, 37));

        menuPanel.add(navInventory, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 290, 280, 60));

        navPOS.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navPOSMouseClicked(evt);
            }
        });
        navPOS.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblPOSIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/shopping-cart.png"))); // NOI18N
        navPOS.add(lblPOSIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 40, 37));

        lblPOSText.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblPOSText.setForeground(new java.awt.Color(255, 255, 255));
        lblPOSText.setText("POS Controller");
        navPOS.add(lblPOSText, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, 130, 37));

        menuPanel.add(navPOS, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 360, 280, 60));

        navSalesHistory.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navSalesHistoryMouseClicked(evt);
            }
        });
        navSalesHistory.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblSalesHistoryIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/cash-register.png"))); // NOI18N
        navSalesHistory.add(lblSalesHistoryIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 40, 37));

        lblSalesHistoryText.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblSalesHistoryText.setForeground(new java.awt.Color(255, 255, 255));
        lblSalesHistoryText.setText("Sales History");
        navSalesHistory.add(lblSalesHistoryText, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, 110, 37));

        menuPanel.add(navSalesHistory, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 430, 280, 60));

        navReports.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navReportsMouseClicked(evt);
            }
        });
        navReports.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblReportsIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/report.png"))); // NOI18N
        navReports.add(lblReportsIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 40, 37));

        lblReportsText.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblReportsText.setForeground(new java.awt.Color(255, 255, 255));
        lblReportsText.setText("Reports");
        navReports.add(lblReportsText, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, 66, 37));

        menuPanel.add(navReports, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 500, 280, 60));

        userInfoPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lbluserIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/usertop.png"))); // NOI18N
        userInfoPanel.add(lbluserIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 40, 40));

        lblAutomatedUsername.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        lblAutomatedUsername.setForeground(new java.awt.Color(255, 255, 255));
        lblAutomatedUsername.setText("Username");
        userInfoPanel.add(lblAutomatedUsername, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, 90, -1));

        lblAutomatedUserRole.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblAutomatedUserRole.setForeground(new java.awt.Color(255, 255, 255));
        lblAutomatedUserRole.setText("Userrole");
        userInfoPanel.add(lblAutomatedUserRole, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 30, 90, -1));
        userInfoPanel.add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(-20, 0, 340, 20));

        menuPanel.add(userInfoPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 710, 300, 60));

        sidebarPanel.add(menuPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 300, 770));
        sidebarPanel.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 100, 320, 30));

        mainContainer.add(sidebarPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 320, 900));

        rightPanel.setBackground(new java.awt.Color(204, 204, 204));
        rightPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        topbarPanel.setBackground(new java.awt.Color(255, 222, 66));
        topbarPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblDateTime.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblDateTime.setText("...");
        topbarPanel.add(lblDateTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 20, 270, 40));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/clock.png"))); // NOI18N
        topbarPanel.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, -1, -1));

        btnLogout.setBackground(new java.awt.Color(231, 43, 32));
        btnLogout.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnLogout.setForeground(new java.awt.Color(255, 255, 255));
        btnLogout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/logout1.png"))); // NOI18N
        btnLogout.setText("Logout");
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(this::btnLogoutActionPerformed);
        topbarPanel.add(btnLogout, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 20, 130, 40));

        rightPanel.add(topbarPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1120, 80));

        contentPanel.setBackground(new java.awt.Color(211, 218, 217));
        contentPanel.setLayout(new java.awt.CardLayout());
        rightPanel.add(contentPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 80, 1120, 820));

        mainContainer.add(rightPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 0, 1120, 900));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void navDashboardMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navDashboardMouseClicked
        showPanel("dashboard");
        setActiveNav(navDashboard);
    }//GEN-LAST:event_navDashboardMouseClicked

    private void navUsersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navUsersMouseClicked
        showPanel("users");
        setActiveNav(navUsers);
    }//GEN-LAST:event_navUsersMouseClicked

    private void navSuppliersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navSuppliersMouseClicked
        showPanel("suppliers");
        setActiveNav(navSuppliers);

    }//GEN-LAST:event_navSuppliersMouseClicked

    private void navProductsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navProductsMouseClicked
        showPanel("products");
        setActiveNav(navProducts);

    }//GEN-LAST:event_navProductsMouseClicked

    private void navPOSMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navPOSMouseClicked
        showPanel("pos");
        setActiveNav(navPOS);
    }//GEN-LAST:event_navPOSMouseClicked

    private void navSalesHistoryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navSalesHistoryMouseClicked
        showPanel("salesHistory");
        setActiveNav(navSalesHistory);
    }//GEN-LAST:event_navSalesHistoryMouseClicked

    private void navReportsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navReportsMouseClicked
        showPanel("reports");
        setActiveNav(navReports);
    }//GEN-LAST:event_navReportsMouseClicked

    private void navCategoriesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navCategoriesMouseClicked
        showPanel("categories");
        setActiveNav(navCategories);
    }//GEN-LAST:event_navCategoriesMouseClicked

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Logout Confirmation",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            if (dateTimeTimer != null) {
                dateTimeTimer.stop();
            }

            new Login().setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void navInventoryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navInventoryMouseClicked
        showPanel("inventories");
        setActiveNav(navInventory);
    }//GEN-LAST:event_navInventoryMouseClicked

    private void navDashboardMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navDashboardMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_navDashboardMouseEntered

    public static void main(String args[]) {
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

        java.awt.EventQueue.invokeLater(()
                -> new MainFrame("Username", "User Role").setVisible(true)
        );
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel brandPanel;
    private javax.swing.JButton btnLogout;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel lblAutomatedUserRole;
    private javax.swing.JLabel lblAutomatedUsername;
    private javax.swing.JLabel lblCategoriesIcon;
    private javax.swing.JLabel lblCategoriesText;
    private javax.swing.JLabel lblDashboardIcon;
    private javax.swing.JLabel lblDashboardText;
    private javax.swing.JLabel lblDateTime;
    private javax.swing.JLabel lblInventoryIcon;
    private javax.swing.JLabel lblInventoryText;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblPOSIcon;
    private javax.swing.JLabel lblPOSText;
    private javax.swing.JLabel lblProductsIcon;
    private javax.swing.JLabel lblProductsText;
    private javax.swing.JLabel lblReportsIcon;
    private javax.swing.JLabel lblReportsText;
    private javax.swing.JLabel lblSalesHistoryIcon;
    private javax.swing.JLabel lblSalesHistoryText;
    private javax.swing.JLabel lblSuppliersIcon;
    private javax.swing.JLabel lblSuppliersText;
    private javax.swing.JLabel lblSystemSubtitle;
    private javax.swing.JLabel lblSystemTitle;
    private javax.swing.JLabel lblUsersIcon;
    private javax.swing.JLabel lblUsersText;
    private javax.swing.JLabel lbluserIcon;
    private javax.swing.JPanel mainContainer;
    private javax.swing.JPanel menuPanel;
    private javax.swing.JPanel navCategories;
    private javax.swing.JPanel navDashboard;
    private javax.swing.JPanel navInventory;
    private javax.swing.JPanel navPOS;
    private javax.swing.JPanel navProducts;
    private javax.swing.JPanel navReports;
    private javax.swing.JPanel navSalesHistory;
    private javax.swing.JPanel navSuppliers;
    private javax.swing.JPanel navUsers;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JPanel sidebarPanel;
    private javax.swing.JPanel topbarPanel;
    private javax.swing.JPanel userInfoPanel;
    // End of variables declaration//GEN-END:variables
}
