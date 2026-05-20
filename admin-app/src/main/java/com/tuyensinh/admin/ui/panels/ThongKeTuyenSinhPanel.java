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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;

public class ThongKeTuyenSinhPanel extends BasePanel {

    private final ThongKeTuyenSinhService service = new ThongKeTuyenSinhService();

    private JPanel cardsPanel;

    private JTable tblTheoNganh;
    private DefaultTableModel modelTheoNganh;

    private JTable tblTheoPhuongThuc;
    private DefaultTableModel modelTheoPhuongThuc;

    private JTable tblTopNganh;
    private DefaultTableModel modelTopNganh;

    private JTable tblTheoDoiTuong;
    private DefaultTableModel modelTheoDoiTuong;

    private JTable tblTheoKhuVuc;
    private DefaultTableModel modelTheoKhuVuc;

    private JTable tblDiemThiSinh;
    private DefaultTableModel modelDiemThiSinh;

    private JTextField txtThiSinhKey;
    private JLabel lblThiSinhInfo;

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
        tabs.addTab("Thí sinh", buildThiSinhPanel());

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
        centerTable(tblTheoNganh);

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
        centerTable(tblTheoPhuongThuc);

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
        centerTable(tblTopNganh);

        tblTopNganh.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblTopNganh.getColumnModel().getColumn(1).setPreferredWidth(90);
        tblTopNganh.getColumnModel().getColumn(2).setPreferredWidth(300);
        tblTopNganh.getColumnModel().getColumn(3).setPreferredWidth(130);

        return TableFactory.wrap(tblTopNganh);
    }
    private JPanel buildThiSinhPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 0, 0, 0));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                buildThiSinhThongKePanel(),
                buildThiSinhChiTietPanel()
        );
        splitPane.setResizeWeight(0.42);
        splitPane.setBorder(null);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildThiSinhThongKePanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));
        panel.setOpaque(false);

        modelTheoDoiTuong = TableFactory.newReadOnlyModel(
                "Mã ĐT",
                "Đối tượng ưu tiên",
                "Số lượng"
        );

        tblTheoDoiTuong = TableFactory.create(modelTheoDoiTuong);
        tblTheoDoiTuong.setAutoCreateRowSorter(true);
        centerTable(tblTheoDoiTuong);

        tblTheoDoiTuong.getColumnModel().getColumn(0).setPreferredWidth(70);
        tblTheoDoiTuong.getColumnModel().getColumn(1).setPreferredWidth(260);
        tblTheoDoiTuong.getColumnModel().getColumn(2).setPreferredWidth(90);

        modelTheoKhuVuc = TableFactory.newReadOnlyModel(
                "Mã KV",
                "Khu vực ưu tiên",
                "Số lượng"
        );

        tblTheoKhuVuc = TableFactory.create(modelTheoKhuVuc);
        tblTheoKhuVuc.setAutoCreateRowSorter(true);
        centerTable(tblTheoKhuVuc);

        tblTheoKhuVuc.getColumnModel().getColumn(0).setPreferredWidth(70);
        tblTheoKhuVuc.getColumnModel().getColumn(1).setPreferredWidth(260);
        tblTheoKhuVuc.getColumnModel().getColumn(2).setPreferredWidth(90);

        JScrollPane spDoiTuong = TableFactory.wrap(tblTheoDoiTuong);
        spDoiTuong.setBorder(BorderFactory.createTitledBorder("Thống kê thí sinh theo đối tượng ưu tiên"));

        JScrollPane spKhuVuc = TableFactory.wrap(tblTheoKhuVuc);
        spKhuVuc.setBorder(BorderFactory.createTitledBorder("Thống kê thí sinh theo khu vực ưu tiên"));

        panel.add(spDoiTuong);
        panel.add(spKhuVuc);

        return panel;
    }

    private JPanel buildThiSinhChiTietPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        JPanel searchPanel = new JPanel(new BorderLayout(8, 8));
        searchPanel.setOpaque(false);

        JPanel leftSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftSearch.setOpaque(false);

        leftSearch.add(new JLabel("Nhập CCCD / SBD:"));

        txtThiSinhKey = new JTextField(22);
        txtThiSinhKey.addActionListener(e -> searchThiSinhDetail());
        leftSearch.add(txtThiSinhKey);

        JButton btnSearch = new JButton("Xem chi tiết");
        btnSearch.addActionListener(e -> searchThiSinhDetail());
        leftSearch.add(btnSearch);

        lblThiSinhInfo = new JLabel("Chưa chọn thí sinh");
        lblThiSinhInfo.setFont(UIConstants.FONT_SMALL);
        lblThiSinhInfo.setForeground(UIConstants.TEXT_SECONDARY);

        searchPanel.add(leftSearch, BorderLayout.WEST);
        searchPanel.add(lblThiSinhInfo, BorderLayout.CENTER);

        modelDiemThiSinh = TableFactory.newReadOnlyModel(
                "Phương thức",
                "Năm TS",
                "SBD",
                "Mã môn",
                "Tên môn",
                "Điểm gốc",
                "Điểm quy đổi",
                "Điểm sử dụng"
        );

        tblDiemThiSinh = TableFactory.create(modelDiemThiSinh);
        tblDiemThiSinh.setAutoCreateRowSorter(true);
        centerTable(tblDiemThiSinh);

        tblDiemThiSinh.getColumnModel().getColumn(0).setPreferredWidth(120);
        tblDiemThiSinh.getColumnModel().getColumn(1).setPreferredWidth(70);
        tblDiemThiSinh.getColumnModel().getColumn(2).setPreferredWidth(90);
        tblDiemThiSinh.getColumnModel().getColumn(3).setPreferredWidth(70);
        tblDiemThiSinh.getColumnModel().getColumn(4).setPreferredWidth(160);
        tblDiemThiSinh.getColumnModel().getColumn(5).setPreferredWidth(90);
        tblDiemThiSinh.getColumnModel().getColumn(6).setPreferredWidth(100);
        tblDiemThiSinh.getColumnModel().getColumn(7).setPreferredWidth(100);

        JScrollPane spDiem = TableFactory.wrap(tblDiemThiSinh);
        spDiem.setBorder(BorderFactory.createTitledBorder("Chi tiết điểm thí sinh: THPT, DGNL, VSAT"));

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(spDiem, BorderLayout.CENTER);

        return panel;
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
        renderThongKeThiSinh(data);
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

    private void renderThongKeThiSinh(ThongKeTuyenSinhService.DashboardData data) {
        if (modelTheoDoiTuong != null) {
            modelTheoDoiTuong.setRowCount(0);

            if (data.thiSinhTheoDoiTuong != null) {
                for (ThongKeTuyenSinhService.ThongKeThiSinhGroupDto row : data.thiSinhTheoDoiTuong) {
                    modelTheoDoiTuong.addRow(new Object[]{
                            row.ma,
                            row.ten,
                            formatNumber(row.soLuong)
                    });
                }
            }
        }

        if (modelTheoKhuVuc != null) {
            modelTheoKhuVuc.setRowCount(0);

            if (data.thiSinhTheoKhuVuc != null) {
                for (ThongKeTuyenSinhService.ThongKeThiSinhGroupDto row : data.thiSinhTheoKhuVuc) {
                    modelTheoKhuVuc.addRow(new Object[]{
                            row.ma,
                            row.ten,
                            formatNumber(row.soLuong)
                    });
                }
            }
        }
    }

    private void searchThiSinhDetail() {
        String key = txtThiSinhKey != null ? txtThiSinhKey.getText().trim() : "";
        if (key.isEmpty()) {
            showError(this, "Vui lòng nhập CCCD hoặc số báo danh thí sinh.");
            return;
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            ThongKeTuyenSinhService.ThiSinhDetailDto info = service.findThiSinhDetail(key);
            modelDiemThiSinh.setRowCount(0);

            if (info == null) {
                lblThiSinhInfo.setText("Không tìm thấy thí sinh theo CCCD/SBD: " + key);
                return;
            }

            lblThiSinhInfo.setText(
                    "Thí sinh: " + info.hoTen
                            + " | CCCD: " + info.cccd
                            + " | SBD: " + info.sobaodanh
                            + " | ĐT: " + info.doiTuong
                            + " | KV: " + info.khuVuc
            );

            java.util.List<ThongKeTuyenSinhService.DiemThiSinhDto> rows =
                    service.findDiemThiSinh(key);

            for (ThongKeTuyenSinhService.DiemThiSinhDto row : rows) {
                modelDiemThiSinh.addRow(new Object[]{
                        row.phuongThuc,
                        row.namTuyenSinh,
                        row.sobaodanh,
                        row.maMon,
                        row.tenMon,
                        formatDiem(row.diemGoc),
                        formatDiem(row.diemQuyDoi),
                        formatDiem(row.diemSuDung)
                });
            }

        } catch (Exception ex) {
            showError(this, ex.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void centerTable(JTable table) {
        // Căn giữa dữ liệu trong ô
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setVerticalAlignment(SwingConstants.CENTER);

        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setCellRenderer(centerRenderer);
        }

        // Căn giữa header
        JTableHeader header = table.getTableHeader();
        DefaultTableCellRenderer headerRenderer =
                (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // Căn giữa nội dung nhiều dòng nếu có
        table.setRowHeight(32);

        // Nếu muốn chữ cũng ở giữa theo chiều dọc rõ hơn
        table.setShowGrid(true);
        table.setGridColor(new Color(220, 220, 220));
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