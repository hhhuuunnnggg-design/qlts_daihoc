package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.BasePanel;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.admin.ui.TableFactory;
import com.tuyensinh.admin.ui.UIConstants;
import com.tuyensinh.dao.BaseDao;
import com.tuyensinh.service.ThongKeTuyenSinhService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class ThongKeTuyenSinhPanel extends BasePanel {

    private final ThongKeTuyenSinhService service = new ThongKeTuyenSinhService();

    private JPanel cardsPanel;

    private JTable tblTheoNganh;
    private DefaultTableModel modelTheoNganh;

    private JTable tblTheoPhuongThuc;
    private DefaultTableModel modelTheoPhuongThuc;

    private JTable tblTopNganh;
    private DefaultTableModel modelTopNganh;

    private JButton btnRefresh;

    private final DecimalFormat numberFmt = new DecimalFormat("#,##0");

    public ThongKeTuyenSinhPanel(MainFrame mainFrame) {
        super(mainFrame);
        initUI();
        loadData();
    }

    @Override
    public String getPageTitle() {
        return "Thống kê tuyển sinh";
    }

    @Override
    protected void initUI() {
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel title = new JLabel("Thống kê tuyển sinh");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JLabel subTitle = new JLabel("Tổng quan dữ liệu thí sinh, nguyện vọng, kết quả xét tuyển theo ngành và phương thức");
        subTitle.setFont(UIConstants.FONT_SMALL);
        subTitle.setForeground(UIConstants.TEXT_SECONDARY);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(3));
        titleBox.add(subTitle);

        btnRefresh = new JButton("Làm mới");
        btnRefresh.setFont(UIConstants.FONT_BODY);
        btnRefresh.addActionListener(e -> loadData());

        toolbar.add(titleBox, BorderLayout.WEST);
        toolbar.add(btnRefresh, BorderLayout.EAST);

        return toolbar;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setOpaque(false);

        cardsPanel = new JPanel(new GridLayout(2, 3, 12, 12));
        cardsPanel.setOpaque(false);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIConstants.FONT_BODY);

        tabs.addTab("Theo ngành", buildTheoNganhPanel());
        tabs.addTab("Theo phương thức", buildTheoPhuongThucPanel());
        tabs.addTab("Top ngành nhiều nguyện vọng", buildTopNganhPanel());

        content.add(cardsPanel, BorderLayout.NORTH);
        content.add(tabs, BorderLayout.CENTER);

        return content;
    }

    private JScrollPane buildTheoNganhPanel() {
        modelTheoNganh = TableFactory.newReadOnlyModel(
                "Mã ngành",
                "Tên ngành",
                "Chỉ tiêu",
                "Tổng NV",
                "Trúng tuyển",
                "Trượt",
                "Điểm chuẩn / Điểm TT"
        );

        tblTheoNganh = TableFactory.create(modelTheoNganh);
        tblTheoNganh.setAutoCreateRowSorter(true);

        tblTheoNganh.getColumnModel().getColumn(0).setPreferredWidth(90);
        tblTheoNganh.getColumnModel().getColumn(1).setPreferredWidth(260);
        tblTheoNganh.getColumnModel().getColumn(2).setPreferredWidth(80);
        tblTheoNganh.getColumnModel().getColumn(3).setPreferredWidth(90);
        tblTheoNganh.getColumnModel().getColumn(4).setPreferredWidth(90);
        tblTheoNganh.getColumnModel().getColumn(5).setPreferredWidth(80);
        tblTheoNganh.getColumnModel().getColumn(6).setPreferredWidth(130);

        return TableFactory.wrap(tblTheoNganh);
    }

    private JScrollPane buildTheoPhuongThucPanel() {
        modelTheoPhuongThuc = TableFactory.newReadOnlyModel(
                "Mã PT",
                "Phương thức",
                "Tổng NV",
                "Trúng tuyển",
                "Trượt",
                "Điểm XT trung bình"
        );

        tblTheoPhuongThuc = TableFactory.create(modelTheoPhuongThuc);
        tblTheoPhuongThuc.setAutoCreateRowSorter(true);

        tblTheoPhuongThuc.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblTheoPhuongThuc.getColumnModel().getColumn(1).setPreferredWidth(280);
        tblTheoPhuongThuc.getColumnModel().getColumn(2).setPreferredWidth(90);
        tblTheoPhuongThuc.getColumnModel().getColumn(3).setPreferredWidth(90);
        tblTheoPhuongThuc.getColumnModel().getColumn(4).setPreferredWidth(80);
        tblTheoPhuongThuc.getColumnModel().getColumn(5).setPreferredWidth(130);

        return TableFactory.wrap(tblTheoPhuongThuc);
    }

    private JScrollPane buildTopNganhPanel() {
        modelTopNganh = TableFactory.newReadOnlyModel(
                "Hạng",
                "Mã ngành",
                "Tên ngành",
                "Tổng nguyện vọng"
        );

        tblTopNganh = TableFactory.create(modelTopNganh);
        tblTopNganh.setAutoCreateRowSorter(true);

        tblTopNganh.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblTopNganh.getColumnModel().getColumn(1).setPreferredWidth(90);
        tblTopNganh.getColumnModel().getColumn(2).setPreferredWidth(300);
        tblTopNganh.getColumnModel().getColumn(3).setPreferredWidth(130);

        return TableFactory.wrap(tblTopNganh);
    }

    @Override
    public void loadData() {
        btnRefresh.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<ThongKeTuyenSinhService.DashboardData, Void> worker =
                new SwingWorker<ThongKeTuyenSinhService.DashboardData, Void>() {

                    @Override
                    protected ThongKeTuyenSinhService.DashboardData doInBackground() {
                        BaseDao.closeCurrentEm();
                        return service.loadDashboard();
                    }

                    @Override
                    protected void done() {
                        try {
                            ThongKeTuyenSinhService.DashboardData data = get();
                            renderDashboard(data);
                        } catch (Exception ex) {
                            showError(ThongKeTuyenSinhPanel.this, ex.getMessage());
                        } finally {
                            btnRefresh.setEnabled(true);
                            setCursor(Cursor.getDefaultCursor());
                        }
                    }
                };

        worker.execute();
    }

    private void renderDashboard(ThongKeTuyenSinhService.DashboardData data) {
        if (data == null || data.tongQuan == null) return;

        renderCards(data.tongQuan);
        renderTheoNganh(data);
        renderTheoPhuongThuc(data);
        renderTopNganh(data);
    }

    private void renderCards(ThongKeTuyenSinhService.TongQuanDto dto) {
        cardsPanel.removeAll();

        cardsPanel.add(createStatCard("Tổng thí sinh", dto.tongThiSinh, UIConstants.STAT_GREEN));
        cardsPanel.add(createStatCard("Tổng ngành", dto.tongNganh, UIConstants.STAT_BLUE));
        cardsPanel.add(createStatCard("Tổng nguyện vọng", dto.tongNguyenVong, UIConstants.STAT_ORANGE));
        cardsPanel.add(createStatCard("Số trúng tuyển", dto.soTrungTuyen, UIConstants.STAT_TEAL));
        cardsPanel.add(createStatCard("Số trượt", dto.soTruot, UIConstants.STAT_RED));
        cardsPanel.add(createStatCard("Số chưa xét", dto.soChuaXet, UIConstants.STAT_PURPLE));

        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private JPanel createStatCard(String title, long value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(8, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 229, 235)),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UIConstants.FONT_SMALL);
        lblTitle.setForeground(UIConstants.TEXT_SECONDARY);

        JLabel lblValue = new JLabel(numberFmt.format(value));
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(accent);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);

        return card;
    }

    private void renderTheoNganh(ThongKeTuyenSinhService.DashboardData data) {
        modelTheoNganh.setRowCount(0);

        for (ThongKeTuyenSinhService.TheoNganhDto row : data.theoNganh) {
            modelTheoNganh.addRow(new Object[]{
                    row.maNganh,
                    row.tenNganh,
                    formatNumber(row.chiTieu),
                    formatNumber(row.tongNguyenVong),
                    formatNumber(row.soTrungTuyen),
                    formatNumber(row.soTruot),
                    formatDiem(row.diemTrungTuyen)
            });
        }
    }

    private void renderTheoPhuongThuc(ThongKeTuyenSinhService.DashboardData data) {
        modelTheoPhuongThuc.setRowCount(0);

        for (ThongKeTuyenSinhService.TheoPhuongThucDto row : data.theoPhuongThuc) {
            modelTheoPhuongThuc.addRow(new Object[]{
                    row.maPhuongThuc,
                    row.tenPhuongThuc,
                    formatNumber(row.tongNguyenVong),
                    formatNumber(row.soTrungTuyen),
                    formatNumber(row.soTruot),
                    formatDiem(row.diemXetTuyenTrungBinh)
            });
        }
    }

    private void renderTopNganh(ThongKeTuyenSinhService.DashboardData data) {
        modelTopNganh.setRowCount(0);

        int rank = 1;
        for (ThongKeTuyenSinhService.TopNganhDto row : data.topNganh) {
            modelTopNganh.addRow(new Object[]{
                    rank++,
                    row.maNganh,
                    row.tenNganh,
                    formatNumber(row.tongNguyenVong)
            });
        }
    }

    private String formatNumber(Number value) {
        if (value == null) return "0";
        return numberFmt.format(value);
    }

    private String formatDiem(BigDecimal value) {
        if (value == null) return "";
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}