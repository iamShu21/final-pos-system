package point.of.sale.system.screens;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.RenderingHints;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import point.of.sale.system.classes.DBConnection;

public class DashboardPanel extends javax.swing.JPanel {

    private static final Color PAGE_BG = new Color(245, 247, 251);
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color CARD_BG_HOVER = new Color(248, 251, 255);
    private static final Color CARD_BORDER = new Color(225, 232, 242);

    private static final Color TEXT_PRIMARY = new Color(24, 39, 58);
    private static final Color TEXT_SECONDARY = new Color(88, 105, 136);
    private static final Color TEXT_MUTED = new Color(120, 134, 156);

    private static final Color ACCENT_BLUE = new Color(58, 123, 255);
    private static final Color ACCENT_BLUE_DARK = new Color(41, 98, 255);
    private static final Color ACCENT_GREEN = new Color(22, 163, 74);
    private static final Color ACCENT_RED = new Color(220, 38, 38);
    private static final Color ACCENT_ORANGE = new Color(245, 158, 11);
    private static final Color ACCENT_PURPLE = new Color(139, 92, 246);
    private static final Color ACCENT_GRAY = new Color(107, 114, 128);

    private static final Color COLOR_UP = new Color(22, 163, 74);
    private static final Color COLOR_DOWN = new Color(220, 38, 38);
    private static final Color COLOR_NEUTRAL = new Color(100, 116, 139);

    private static final int LOW_STOCK_THRESHOLD = 10;

    private final NumberFormat wholeNumberFormat = NumberFormat.getIntegerInstance();
    private final DecimalFormat moneyFormat = new DecimalFormat("₱#,##0.00");

    public DashboardPanel() {
        initComponents();
        applyDashboardTheme();
        loadDashboardData();
    }

    private void applyDashboardTheme() {
        setBackground(PAGE_BG);
        styleDashboardLabels();
        styleSummaryCards();
        styleChartContainers();
    }

    private void styleDashboardLabels() {
        Font valueFont = new Font("Tahoma", Font.BOLD, 30);
        Font trendFont = new Font("Tahoma", Font.PLAIN, 13);

        autoNumber.setFont(valueFont);
        autoNumber1.setFont(valueFont);
        autoNumber2.setFont(valueFont);
        autoNumber3.setFont(valueFont);
        autoNumber4.setFont(valueFont);
        autoNumber5.setFont(valueFont);

        autoNumber.setForeground(TEXT_PRIMARY);
        autoNumber1.setForeground(TEXT_PRIMARY);
        autoNumber2.setForeground(TEXT_PRIMARY);
        autoNumber3.setForeground(TEXT_PRIMARY);
        autoNumber4.setForeground(TEXT_PRIMARY);
        autoNumber5.setForeground(TEXT_PRIMARY);

        trendWithAutomatedValue.setFont(trendFont);
        trendWithAutomatedValue2.setFont(trendFont);
        trendWithAutomatedValue3.setFont(trendFont);
        trendWithAutomatedValue4.setFont(trendFont);
        trendWithAutomatedValue5.setFont(trendFont);
        trendWithAutomatedValue6.setFont(trendFont);

        trendWithAutomatedValue.setForeground(TEXT_MUTED);
        trendWithAutomatedValue2.setForeground(TEXT_MUTED);
        trendWithAutomatedValue3.setForeground(TEXT_MUTED);
        trendWithAutomatedValue4.setForeground(TEXT_MUTED);
        trendWithAutomatedValue5.setForeground(TEXT_MUTED);
        trendWithAutomatedValue6.setForeground(TEXT_MUTED);

        if (lblDashboardSubtitle != null) {
            lblDashboardSubtitle.setForeground(TEXT_SECONDARY);
            lblDashboardSubtitle.setFont(new Font("Tahoma", Font.PLAIN, 14));
        }
    }

    private void styleSummaryCards() {
        styleCardFromLabel(autoNumber1);
        styleCardFromLabel(autoNumber2);
        styleCardFromLabel(autoNumber);
        styleCardFromLabel(autoNumber4);
        styleCardFromLabel(autoNumber5);
        styleCardFromLabel(autoNumber3);
    }

    private void styleCardFromLabel(JLabel valueLabel) {
        if (valueLabel == null) {
            return;
        }

        Component parent = valueLabel.getParent();
        if (!(parent instanceof JPanel)) {
            return;
        }

        JPanel card = (JPanel) parent;
        card.setOpaque(true);
        card.setBackground(CARD_BG);
        card.setBorder(createCardBorder());

        for (Component child : card.getComponents()) {
            if (child instanceof JLabel) {
                JLabel lbl = (JLabel) child;

                if (lbl == valueLabel) {
                    lbl.setForeground(TEXT_PRIMARY);
                    lbl.setFont(new Font("Tahoma", Font.BOLD, 30));
                } else {
                    String text = lbl.getText() == null ? "" : lbl.getText().trim();

                    if (text.startsWith("Total") || text.startsWith("Sales") || text.startsWith("Low Stock")) {
                        lbl.setForeground(TEXT_SECONDARY);
                        lbl.setFont(new Font("Tahoma", Font.BOLD, 14));
                    } else if (text.startsWith("→") || text.startsWith("↗") || text.startsWith("↘") || text.contains("Change")) {
                        lbl.setForeground(TEXT_MUTED);
                        lbl.setFont(new Font("Tahoma", Font.PLAIN, 13));
                    }
                }
            }
        }

        addCardHoverEffect(card);
    }

    private void styleChartContainers() {
        styleChartPanel(pnlDailySalesChart);
        styleChartPanel(pnlMonthlySalesChart);
        styleChartPanel(pnlTopSellingProductsCharts);
    }

    private void styleChartPanel(JPanel panel) {
        if (panel == null) {
            return;
        }

        panel.setOpaque(true);
        panel.setBackground(CARD_BG);
        panel.setBorder(createCardBorder());

        Component parent = panel.getParent();
        if (parent instanceof JPanel) {
            JPanel wrapper = (JPanel) parent;
            wrapper.setOpaque(true);
            wrapper.setBackground(CARD_BG);
            wrapper.setBorder(createCardBorder());
        }
    }

    private CompoundBorder createCardBorder() {
        return BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        );
    }

    private void addCardHoverEffect(final JPanel card) {
        final Color normal = CARD_BG;
        final Color hover = CARD_BG_HOVER;

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(hover);
                card.setBorder(new CompoundBorder(
                        new LineBorder(new Color(198, 214, 235), 1, true),
                        new EmptyBorder(14, 14, 14, 14)
                ));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(normal);
                card.setBorder(createCardBorder());
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                card.setBorder(new CompoundBorder(
                        new LineBorder(ACCENT_BLUE, 1, true),
                        new EmptyBorder(14, 14, 14, 14)
                ));
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                card.setBorder(new CompoundBorder(
                        new LineBorder(new Color(198, 214, 235), 1, true),
                        new EmptyBorder(14, 14, 14, 14)
                ));
            }
        });
    }

    public final void loadDashboardData() {
        loadSummaryCards();
        loadCharts();
    }

    private void loadSummaryCards() {
        try (Connection conn = DBConnection.dbConnection()) {

            int totalProducts = getCount(conn, "SELECT COUNT(*) FROM products");
            int previousProducts = getCountBeforeDate(conn,
                    "SELECT COUNT(*) FROM products WHERE DATE(created_at) < CURDATE()");
            setCountLabel(autoNumber1, totalProducts);
            setTrendLabel(trendWithAutomatedValue, totalProducts, previousProducts, false);

            int totalCategories = getCount(conn, "SELECT COUNT(*) FROM categories");
            int previousCategories = getCountBeforeDate(conn,
                    "SELECT COUNT(*) FROM categories WHERE DATE(created_at) < CURDATE()");
            setCountLabel(autoNumber2, totalCategories);
            setTrendLabel(trendWithAutomatedValue2, totalCategories, previousCategories, false);

            int totalSuppliers = getCount(conn, "SELECT COUNT(*) FROM suppliers");
            int previousSuppliers = getCountBeforeDate(conn,
                    "SELECT COUNT(*) FROM suppliers WHERE DATE(created_at) < CURDATE()");
            setCountLabel(autoNumber, totalSuppliers);
            setTrendLabel(trendWithAutomatedValue3, totalSuppliers, previousSuppliers, false);

            int totalUsers = getCount(conn, "SELECT COUNT(*) FROM users");
            int previousUsers = getCountBeforeDate(conn,
                    "SELECT COUNT(*) FROM users WHERE DATE(created_at) < CURDATE()");
            setCountLabel(autoNumber4, totalUsers);
            setTrendLabel(trendWithAutomatedValue4, totalUsers, previousUsers, false);

            double salesToday = getDoubleValue(conn,
                    "SELECT COALESCE(SUM(total_amount), 0) FROM sales WHERE DATE(sale_date) = CURDATE()");
            double salesYesterday = getDoubleValue(conn,
                    "SELECT COALESCE(SUM(total_amount), 0) FROM sales WHERE DATE(sale_date) = CURDATE() - INTERVAL 1 DAY");
            autoNumber5.setText(moneyFormat.format(salesToday));
            setTrendLabel(trendWithAutomatedValue5, salesToday, salesYesterday, true);

            int lowStockToday = getIntValue(conn,
                    "SELECT COUNT(*) FROM products WHERE stock_quantity <= ?");
            int lowStockYesterday = lowStockToday;
            autoNumber3.setText(wholeNumberFormat.format(lowStockToday));
            setTrendLabelForLowStock(trendWithAutomatedValue6, lowStockToday, lowStockYesterday);

        } catch (SQLException ex) {
            ex.printStackTrace();

            autoNumber1.setText("0");
            autoNumber2.setText("0");
            autoNumber.setText("0");
            autoNumber4.setText("0");
            autoNumber5.setText("₱0.00");
            autoNumber3.setText("0");

            setErrorTrend(trendWithAutomatedValue);
            setErrorTrend(trendWithAutomatedValue2);
            setErrorTrend(trendWithAutomatedValue3);
            setErrorTrend(trendWithAutomatedValue4);
            setErrorTrend(trendWithAutomatedValue5);
            setErrorTrend(trendWithAutomatedValue6);
        }
    }

    private void loadCharts() {
        showDailySalesChart();
        showMonthlySalesChart();
        showTopSellingProductsChart();
    }

    private void showDailySalesChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try (Connection conn = DBConnection.dbConnection()) {

            Map<String, Double> dailyData = createCurrentWeekMap();

            String sql
                    = "SELECT DAYOFWEEK(sale_date) AS day_no, COALESCE(SUM(total_amount), 0) AS total "
                    + "FROM sales "
                    + "WHERE YEARWEEK(sale_date, 1) = YEARWEEK(CURDATE(), 1) "
                    + "GROUP BY DAYOFWEEK(sale_date) "
                    + "ORDER BY DAYOFWEEK(sale_date)";

            try (PreparedStatement pst = conn.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {

                while (rs.next()) {
                    int dayNo = rs.getInt("day_no");
                    double total = rs.getDouble("total");

                    String dayLabel = convertDayOfWeek(dayNo);
                    if (dayLabel != null) {
                        dailyData.put(dayLabel, total);
                    }
                }
            }

            for (Map.Entry<String, Double> entry : dailyData.entrySet()) {
                dataset.addValue(entry.getValue(), "Sales", entry.getKey());
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            dataset.addValue(0, "Sales", "Mon");
            dataset.addValue(0, "Sales", "Tue");
            dataset.addValue(0, "Sales", "Wed");
            dataset.addValue(0, "Sales", "Thu");
            dataset.addValue(0, "Sales", "Fri");
            dataset.addValue(0, "Sales", "Sat");
            dataset.addValue(0, "Sales", "Sun");
        }

        JFreeChart chart = ChartFactory.createLineChart(null, "", "", dataset);
        chart.setBackgroundPaint(CARD_BG);
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(CARD_BG);
        plot.setRangeGridlinePaint(new Color(231, 236, 243));
        plot.setRangeGridlineStroke(new BasicStroke(1f));
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(false);

        LineAndShapeRenderer renderer = new LineAndShapeRenderer(true, true);
        renderer.setSeriesPaint(0, ACCENT_BLUE);
        renderer.setSeriesStroke(0, new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setSeriesShapesVisible(0, true);
        plot.setRenderer(renderer);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("Tahoma", Font.PLAIN, 12));
        domainAxis.setTickLabelPaint(TEXT_SECONDARY);
        domainAxis.setAxisLineVisible(false);
        domainAxis.setTickMarksVisible(false);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("Tahoma", Font.PLAIN, 12));
        rangeAxis.setTickLabelPaint(TEXT_SECONDARY);
        rangeAxis.setAxisLineVisible(false);
        rangeAxis.setTickMarksVisible(false);

        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setItemFont(new Font("Tahoma", Font.PLAIN, 12));
            legend.setBackgroundPaint(CARD_BG);
            legend.setItemPaint(TEXT_SECONDARY);
        }

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(CARD_BG);
        chartPanel.setBorder(BorderFactory.createEmptyBorder());
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setPopupMenu(null);

        pnlDailySalesChart.setLayout(new BorderLayout());
        pnlDailySalesChart.removeAll();
        pnlDailySalesChart.setBackground(CARD_BG);
        pnlDailySalesChart.add(chartPanel, BorderLayout.CENTER);
        pnlDailySalesChart.revalidate();
        pnlDailySalesChart.repaint();
    }

    private void showMonthlySalesChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try (Connection conn = DBConnection.dbConnection()) {

            Map<String, Double> monthlyData = createLastSixMonthsMap();

            String sql
                    = "SELECT DATE_FORMAT(sale_date, '%b') AS month_name, "
                    + "       YEAR(sale_date) AS year_no, "
                    + "       MONTH(sale_date) AS month_no, "
                    + "       COALESCE(SUM(total_amount), 0) AS total "
                    + "FROM sales "
                    + "WHERE sale_date >= DATE_SUB(CURDATE(), INTERVAL 5 MONTH) "
                    + "GROUP BY YEAR(sale_date), MONTH(sale_date), DATE_FORMAT(sale_date, '%b') "
                    + "ORDER BY YEAR(sale_date), MONTH(sale_date)";

            try (PreparedStatement pst = conn.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {

                while (rs.next()) {
                    String monthName = rs.getString("month_name");
                    double total = rs.getDouble("total");
                    monthlyData.put(monthName, total);
                }
            }

            for (Map.Entry<String, Double> entry : monthlyData.entrySet()) {
                dataset.addValue(entry.getValue(), "Sales", entry.getKey());
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            dataset.addValue(0, "Sales", "Jan");
            dataset.addValue(0, "Sales", "Feb");
            dataset.addValue(0, "Sales", "Mar");
            dataset.addValue(0, "Sales", "Apr");
            dataset.addValue(0, "Sales", "May");
            dataset.addValue(0, "Sales", "Jun");
        }

        JFreeChart chart = ChartFactory.createBarChart(null, "", "", dataset);
        chart.setBackgroundPaint(CARD_BG);
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(CARD_BG);
        plot.setRangeGridlinePaint(new Color(231, 236, 243));
        plot.setRangeGridlineStroke(new BasicStroke(1f));
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(false);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, ACCENT_BLUE_DARK);
        renderer.setShadowVisible(false);
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        renderer.setMaximumBarWidth(0.12);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("Tahoma", Font.PLAIN, 12));
        domainAxis.setTickLabelPaint(TEXT_SECONDARY);
        domainAxis.setAxisLineVisible(false);
        domainAxis.setTickMarksVisible(false);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("Tahoma", Font.PLAIN, 12));
        rangeAxis.setTickLabelPaint(TEXT_SECONDARY);
        rangeAxis.setAxisLineVisible(false);
        rangeAxis.setTickMarksVisible(false);

        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setItemFont(new Font("Tahoma", Font.PLAIN, 12));
            legend.setBackgroundPaint(CARD_BG);
            legend.setItemPaint(TEXT_SECONDARY);
        }

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(CARD_BG);
        chartPanel.setBorder(BorderFactory.createEmptyBorder());
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setPopupMenu(null);

        pnlMonthlySalesChart.setLayout(new BorderLayout());
        pnlMonthlySalesChart.removeAll();
        pnlMonthlySalesChart.setBackground(CARD_BG);
        pnlMonthlySalesChart.add(chartPanel, BorderLayout.CENTER);
        pnlMonthlySalesChart.revalidate();
        pnlMonthlySalesChart.repaint();
    }

    private void showTopSellingProductsChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();

        try (Connection conn = DBConnection.dbConnection()) {

            String sql = "SELECT p.name, SUM(sd.quantity) AS total_qty "
                    + "FROM sales_details sd "
                    + "JOIN products p ON sd.product_id = p.product_id "
                    + "GROUP BY p.product_id, p.name "
                    + "ORDER BY total_qty DESC "
                    + "LIMIT 5";

            try (PreparedStatement pst = conn.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {

                boolean hasData = false;

                while (rs.next()) {
                    hasData = true;
                    dataset.setValue(rs.getString("name"), rs.getDouble("total_qty"));
                }

                if (!hasData) {
                    dataset.setValue("No Sales Yet", 1);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            dataset.setValue("No Data", 1);
        }

        JFreeChart chart = ChartFactory.createPieChart(null, dataset, true, true, false);
        chart.setBackgroundPaint(CARD_BG);
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(CARD_BG);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        plot.setInteriorGap(0.06);
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 230));
        plot.setLabelOutlinePaint(CARD_BORDER);
        plot.setLabelShadowPaint(null);
        plot.setLabelPaint(TEXT_PRIMARY);
        plot.setLabelFont(new Font("Tahoma", Font.PLAIN, 12));
        plot.setSimpleLabels(false);
        plot.setCircular(true);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} {2}"));

        Color[] colors = {
            ACCENT_BLUE,
            ACCENT_GREEN,
            ACCENT_ORANGE,
            ACCENT_PURPLE,
            ACCENT_GRAY
        };

        int colorIndex = 0;
        for (Object key : dataset.getKeys()) {
            plot.setSectionPaint((Comparable) key, colors[colorIndex % colors.length]);
            colorIndex++;
        }

        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setItemFont(new Font("Tahoma", Font.PLAIN, 12));
            legend.setBackgroundPaint(CARD_BG);
            legend.setItemPaint(TEXT_SECONDARY);
        }

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(CARD_BG);
        chartPanel.setBorder(BorderFactory.createEmptyBorder());
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setPopupMenu(null);

        pnlTopSellingProductsCharts.setLayout(new BorderLayout());
        pnlTopSellingProductsCharts.removeAll();
        pnlTopSellingProductsCharts.setBackground(CARD_BG);
        pnlTopSellingProductsCharts.add(chartPanel, BorderLayout.CENTER);
        pnlTopSellingProductsCharts.revalidate();
        pnlTopSellingProductsCharts.repaint();
    }

    private void setCountLabel(JLabel label, int value) {
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setText(wholeNumberFormat.format(value));
    }

    private void setTrendLabel(JLabel label, double current, double previous, boolean currencyStyle) {
        String display;

        if (current > previous) {
            label.setForeground(COLOR_UP);
            display = "↗ Higher";
        } else if (current < previous) {
            label.setForeground(COLOR_DOWN);
            display = "↘ Lower";
        } else {
            label.setForeground(COLOR_NEUTRAL);
            display = "→ No change";
        }

        if (previous > 0) {
            double percent = ((current - previous) / previous) * 100.0;
            String sign = percent > 0 ? "+" : "";
            display += " (" + sign + String.format("%.1f", percent) + "%)";
        } else if (current > 0) {
            display += currencyStyle ? " (new sales)" : " (new)";
        }

        label.setText(display);
    }

    private void setTrendLabelForLowStock(JLabel label, double current, double previous) {
        String display;

        if (current > previous) {
            label.setForeground(COLOR_DOWN);
            display = "↘ More low-stock items";
        } else if (current < previous) {
            label.setForeground(COLOR_UP);
            display = "↗ Stock improved";
        } else {
            label.setForeground(COLOR_NEUTRAL);
            display = "→ No change";
        }

        label.setText(display);
    }

    private void setErrorTrend(JLabel label) {
        label.setForeground(COLOR_DOWN);
        label.setText("Data unavailable");
    }

    private int getCount(Connection conn, String sql) throws SQLException {
        try (PreparedStatement pst = conn.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private int getCountBeforeDate(Connection conn, String sql) {
        try (PreparedStatement pst = conn.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            return 0;
        }
        return 0;
    }

    private double getDoubleValue(Connection conn, String sql) throws SQLException {
        try (PreparedStatement pst = conn.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0;
    }

    private int getIntValue(Connection conn, String sql) throws SQLException {
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, LOW_STOCK_THRESHOLD);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private Map<String, Double> createCurrentWeekMap() {
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("Mon", 0.0);
        map.put("Tue", 0.0);
        map.put("Wed", 0.0);
        map.put("Thu", 0.0);
        map.put("Fri", 0.0);
        map.put("Sat", 0.0);
        map.put("Sun", 0.0);
        return map;
    }

    private Map<String, Double> createLastSixMonthsMap() {
        Map<String, Double> map = new LinkedHashMap<>();
        YearMonth current = YearMonth.now().minusMonths(5);

        for (int i = 0; i < 6; i++) {
            map.put(getMonthShortName(current.getMonthValue()), 0.0);
            current = current.plusMonths(1);
        }

        return map;
    }

    private String convertDayOfWeek(int mysqlDayOfWeek) {
        switch (mysqlDayOfWeek) {
            case 2:
                return "Mon";
            case 3:
                return "Tue";
            case 4:
                return "Wed";
            case 5:
                return "Thu";
            case 6:
                return "Fri";
            case 7:
                return "Sat";
            case 1:
                return "Sun";
            default:
                return null;
        }
    }

    private String getMonthShortName(int month) {
        switch (month) {
            case 1:
                return "Jan";
            case 2:
                return "Feb";
            case 3:
                return "Mar";
            case 4:
                return "Apr";
            case 5:
                return "May";
            case 6:
                return "Jun";
            case 7:
                return "Jul";
            case 8:
                return "Aug";
            case 9:
                return "Sep";
            case 10:
                return "Oct";
            case 11:
                return "Nov";
            case 12:
                return "Dec";
            default:
                return "";
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

        lblDashboardTitle = new point.of.sale.system.classes.GradientFont();
        lblDashboardSubtitle = new javax.swing.JLabel();
        suppliersCard = new javax.swing.JPanel();
        lblTotalSupplier = new javax.swing.JLabel();
        autoNumber = new javax.swing.JLabel();
        trendWithAutomatedValue3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblSuppliersIcon = new javax.swing.JLabel();
        productsCard = new javax.swing.JPanel();
        lblTotalProducts = new javax.swing.JLabel();
        autoNumber1 = new javax.swing.JLabel();
        trendWithAutomatedValue = new javax.swing.JLabel();
        insertIcon = new javax.swing.JLabel();
        categoriesCard = new javax.swing.JPanel();
        lblTotalCat = new javax.swing.JLabel();
        autoNumber2 = new javax.swing.JLabel();
        trendWithAutomatedValue2 = new javax.swing.JLabel();
        lblCategoriesIcon = new javax.swing.JLabel();
        stocksCard = new javax.swing.JPanel();
        lblStockItems = new javax.swing.JLabel();
        autoNumber3 = new javax.swing.JLabel();
        trendWithAutomatedValue6 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        totalUserCard = new javax.swing.JPanel();
        lblTotalUsers = new javax.swing.JLabel();
        autoNumber4 = new javax.swing.JLabel();
        trendWithAutomatedValue4 = new javax.swing.JLabel();
        lblUsersIcon = new javax.swing.JLabel();
        salesCard = new javax.swing.JPanel();
        lblSales = new javax.swing.JLabel();
        autoNumber5 = new javax.swing.JLabel();
        trendWithAutomatedValue5 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        pnlTopSellingProducts = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        pnlTopSellingProductsCharts = new javax.swing.JPanel();
        pnlMonthlySales = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        pnlMonthlySalesChart = new javax.swing.JPanel();
        pnlDailySales = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        pnlDailySalesChart = new javax.swing.JPanel();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator1 = new javax.swing.JSeparator();

        setBackground(new java.awt.Color(240, 248, 255));
        setPreferredSize(new java.awt.Dimension(1120, 820));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblDashboardTitle.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        lblDashboardTitle.setText("DASHBOARD");
        add(lblDashboardTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        lblDashboardSubtitle.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        lblDashboardSubtitle.setText("Welcome back! Here's your business overview");
        add(lblDashboardSubtitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, -1, -1));

        suppliersCard.setBackground(new java.awt.Color(122, 170, 206));
        suppliersCard.setPreferredSize(new java.awt.Dimension(320, 150));
        suppliersCard.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTotalSupplier.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblTotalSupplier.setText("Total Suppliers");
        suppliersCard.add(lblTotalSupplier, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        autoNumber.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        autoNumber.setText(".......");
        suppliersCard.add(autoNumber, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 190, -1));

        trendWithAutomatedValue3.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        trendWithAutomatedValue3.setText(".......");
        suppliersCard.add(trendWithAutomatedValue3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 200, -1));

        jLabel4.setText("jLabel4");
        suppliersCard.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 150, -1, -1));

        lblSuppliersIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/supply-chain.png"))); // NOI18N
        suppliersCard.add(lblSuppliersIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 20, 40, 37));

        add(suppliersCard, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 110, -1, -1));

        productsCard.setBackground(new java.awt.Color(122, 170, 206));
        productsCard.setPreferredSize(new java.awt.Dimension(320, 150));
        productsCard.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTotalProducts.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblTotalProducts.setText("Total Products");
        productsCard.add(lblTotalProducts, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        autoNumber1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        autoNumber1.setText("........");
        productsCard.add(autoNumber1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 160, 30));

        trendWithAutomatedValue.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        trendWithAutomatedValue.setText(".......");
        productsCard.add(trendWithAutomatedValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 150, 20));

        insertIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/box.png"))); // NOI18N
        productsCard.add(insertIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 20, -1, -1));

        add(productsCard, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 110, -1, -1));

        categoriesCard.setBackground(new java.awt.Color(122, 170, 206));
        categoriesCard.setPreferredSize(new java.awt.Dimension(320, 150));
        categoriesCard.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTotalCat.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblTotalCat.setText("Total Categories");
        categoriesCard.add(lblTotalCat, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        autoNumber2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        autoNumber2.setText(".......");
        categoriesCard.add(autoNumber2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 160, -1));

        trendWithAutomatedValue2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        trendWithAutomatedValue2.setText(".......");
        categoriesCard.add(trendWithAutomatedValue2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 170, -1));

        lblCategoriesIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/list.png"))); // NOI18N
        lblCategoriesIcon.setToolTipText("");
        categoriesCard.add(lblCategoriesIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 20, 40, 37));

        add(categoriesCard, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 110, -1, -1));

        stocksCard.setBackground(new java.awt.Color(122, 170, 206));
        stocksCard.setPreferredSize(new java.awt.Dimension(320, 150));
        stocksCard.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblStockItems.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblStockItems.setText("Low Stock  Items");
        stocksCard.add(lblStockItems, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        autoNumber3.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        autoNumber3.setText(".......");
        stocksCard.add(autoNumber3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 180, -1));

        trendWithAutomatedValue6.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        trendWithAutomatedValue6.setText(".......");
        stocksCard.add(trendWithAutomatedValue6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 170, -1));

        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/caution.png"))); // NOI18N
        stocksCard.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 20, -1, -1));

        add(stocksCard, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 280, -1, -1));

        totalUserCard.setBackground(new java.awt.Color(122, 170, 206));
        totalUserCard.setPreferredSize(new java.awt.Dimension(320, 150));
        totalUserCard.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTotalUsers.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblTotalUsers.setText("Total Users");
        totalUserCard.add(lblTotalUsers, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        autoNumber4.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        autoNumber4.setText(".......");
        totalUserCard.add(autoNumber4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 190, -1));

        trendWithAutomatedValue4.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        trendWithAutomatedValue4.setText(".......");
        totalUserCard.add(trendWithAutomatedValue4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 180, -1));

        lblUsersIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/group.png"))); // NOI18N
        totalUserCard.add(lblUsersIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 20, 40, 37));

        add(totalUserCard, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 280, -1, -1));

        salesCard.setBackground(new java.awt.Color(122, 170, 206));
        salesCard.setPreferredSize(new java.awt.Dimension(320, 150));
        salesCard.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblSales.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblSales.setText("Sales Today");
        salesCard.add(lblSales, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        autoNumber5.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        autoNumber5.setText(".......");
        salesCard.add(autoNumber5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 290, -1));

        trendWithAutomatedValue5.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        trendWithAutomatedValue5.setText(".......");
        salesCard.add(trendWithAutomatedValue5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 150, -1));

        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/point/of/sale/system/icons/sales.png"))); // NOI18N
        salesCard.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 20, -1, -1));

        add(salesCard, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 280, -1, -1));

        pnlTopSellingProducts.setBackground(new java.awt.Color(64, 96, 147));
        pnlTopSellingProducts.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel3.setText("Top Selling Products");
        pnlTopSellingProducts.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        pnlTopSellingProductsCharts.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout pnlTopSellingProductsChartsLayout = new javax.swing.GroupLayout(pnlTopSellingProductsCharts);
        pnlTopSellingProductsCharts.setLayout(pnlTopSellingProductsChartsLayout);
        pnlTopSellingProductsChartsLayout.setHorizontalGroup(
            pnlTopSellingProductsChartsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 380, Short.MAX_VALUE)
        );
        pnlTopSellingProductsChartsLayout.setVerticalGroup(
            pnlTopSellingProductsChartsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 280, Short.MAX_VALUE)
        );

        pnlTopSellingProducts.add(pnlTopSellingProductsCharts, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 380, 280));

        add(pnlTopSellingProducts, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 460, 400, 340));

        pnlMonthlySales.setBackground(new java.awt.Color(64, 96, 147));
        pnlMonthlySales.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel2.setText("Monthly Sales");
        pnlMonthlySales.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        pnlMonthlySalesChart.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout pnlMonthlySalesChartLayout = new javax.swing.GroupLayout(pnlMonthlySalesChart);
        pnlMonthlySalesChart.setLayout(pnlMonthlySalesChartLayout);
        pnlMonthlySalesChartLayout.setHorizontalGroup(
            pnlMonthlySalesChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 290, Short.MAX_VALUE)
        );
        pnlMonthlySalesChartLayout.setVerticalGroup(
            pnlMonthlySalesChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 280, Short.MAX_VALUE)
        );

        pnlMonthlySales.add(pnlMonthlySalesChart, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 290, 280));

        add(pnlMonthlySales, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 460, 310, 340));

        pnlDailySales.setBackground(new java.awt.Color(64, 96, 147));
        pnlDailySales.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel1.setText("Daily Sales");
        pnlDailySales.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        pnlDailySalesChart.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout pnlDailySalesChartLayout = new javax.swing.GroupLayout(pnlDailySalesChart);
        pnlDailySalesChart.setLayout(pnlDailySalesChartLayout);
        pnlDailySalesChartLayout.setHorizontalGroup(
            pnlDailySalesChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 290, Short.MAX_VALUE)
        );
        pnlDailySalesChartLayout.setVerticalGroup(
            pnlDailySalesChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 280, Short.MAX_VALUE)
        );

        pnlDailySales.add(pnlDailySalesChart, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 290, 280));

        add(pnlDailySales, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 460, 310, 340));

        jSeparator3.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator3.setForeground(new java.awt.Color(0, 0, 0));
        add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 30, 590, 10));

        jSeparator1.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator1.setForeground(new java.awt.Color(0, 0, 0));
        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 40, 20, 90));
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel autoNumber;
    private javax.swing.JLabel autoNumber1;
    private javax.swing.JLabel autoNumber2;
    private javax.swing.JLabel autoNumber3;
    private javax.swing.JLabel autoNumber4;
    private javax.swing.JLabel autoNumber5;
    private javax.swing.JPanel categoriesCard;
    private javax.swing.JLabel insertIcon;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel lblCategoriesIcon;
    private javax.swing.JLabel lblDashboardSubtitle;
    private javax.swing.JLabel lblDashboardTitle;
    private javax.swing.JLabel lblSales;
    private javax.swing.JLabel lblStockItems;
    private javax.swing.JLabel lblSuppliersIcon;
    private javax.swing.JLabel lblTotalCat;
    private javax.swing.JLabel lblTotalProducts;
    private javax.swing.JLabel lblTotalSupplier;
    private javax.swing.JLabel lblTotalUsers;
    private javax.swing.JLabel lblUsersIcon;
    private javax.swing.JPanel pnlDailySales;
    private javax.swing.JPanel pnlDailySalesChart;
    private javax.swing.JPanel pnlMonthlySales;
    private javax.swing.JPanel pnlMonthlySalesChart;
    private javax.swing.JPanel pnlTopSellingProducts;
    private javax.swing.JPanel pnlTopSellingProductsCharts;
    private javax.swing.JPanel productsCard;
    private javax.swing.JPanel salesCard;
    private javax.swing.JPanel stocksCard;
    private javax.swing.JPanel suppliersCard;
    private javax.swing.JPanel totalUserCard;
    private javax.swing.JLabel trendWithAutomatedValue;
    private javax.swing.JLabel trendWithAutomatedValue2;
    private javax.swing.JLabel trendWithAutomatedValue3;
    private javax.swing.JLabel trendWithAutomatedValue4;
    private javax.swing.JLabel trendWithAutomatedValue5;
    private javax.swing.JLabel trendWithAutomatedValue6;
    // End of variables declaration//GEN-END:variables
}
