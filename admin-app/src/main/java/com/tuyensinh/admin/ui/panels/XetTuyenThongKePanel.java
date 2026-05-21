package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.BasePanel;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.Nganh;
import com.tuyensinh.entity.NguyenVong;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.entity.ToHop;
import com.tuyensinh.service.NguyenVongService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Panel tach rieng 2 bang nang cua XetTuyenPanel.
 *
 * Ly do tach:
 * - XetTuyenPanel dang phan trang danh sach nguyen vong.
 * - Neu moi lan chuyen trang lai load them danh sach trung tuyen chi tiet + thong ke
 *   theo nganh/phuong thuc thi UI bi lag.
 * - Panel nay chi load khi nguoi dung mo muc thong ke sau xet tuyen.
 */
public class XetTuyenThongKePanel extends BasePanel {

    private final NguyenVongService nguyenVongService = new NguyenVongService();

    private JTable tblTrungTuyenTheoNganh;
    private DefaultTableModel modelTrungTuyenTheoNganh;
    private JTable tblThongKeTheoNganhPhuongThuc;
    private DefaultTableModel modelThongKeTheoNganhPhuongThuc;
    private JSpinner spLimit;
    private JLabel lblStatus;
    private JButton btnLoad;

    public XetTuyenThongKePanel(MainFrame mainFrame) {
        super(mainFrame);
        initUI();
    }

    @Override
    public String getPageTitle() {
        return "Thống kê xét tuyển";
    }

    @Override
    protected void initUI() {
        buildToolbar();
        buildTables();
        buildStatusBar();
    }

    private void buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);

        JLabel lblInfo = new JLabel("Danh sách trúng tuyển chi tiết theo ngành và số lượng trúng tuyển theo ngành - phương thức");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        toolbar.add(lblInfo);

        toolbar.add(Box.createHorizontalStrut(16));
        toolbar.add(new JLabel("Giới hạn dòng chi tiết:"));

        spLimit = new JSpinner(new SpinnerNumberModel(20000, 100, 100000, 1000));
        spLimit.setPreferredSize(new Dimension(90, 28));
        toolbar.add(spLimit);

        btnLoad = new JButton("Tải dữ liệu");
        btnLoad.addActionListener(e -> loadData());
        toolbar.add(btnLoad);

        JButton btnBack = new JButton("Về xét tuyển");
        btnBack.addActionListener(e -> mainFrame.showPanel("xettuyen"));
        toolbar.add(btnBack);

        add(toolbar, BorderLayout.NORTH);
    }

    private void buildTables() {
        modelTrungTuyenTheoNganh = new DefaultTableModel(
                new String[]{"Ngành", "Tên ngành", "CCCD", "Họ tên", "NV", "Tổ hợp gốc", "THM tốt nhất", "Điểm TH", "Điểm cộng", "Điểm UT", "Điểm XT", "Nguồn điểm"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblTrungTuyenTheoNganh = new JTable(modelTrungTuyenTheoNganh);
        tblTrungTuyenTheoNganh.setAutoCreateRowSorter(true);
        tblTrungTuyenTheoNganh.setRowHeight(24);
        JScrollPane spTrungTuyen = new JScrollPane(tblTrungTuyenTheoNganh);
        spTrungTuyen.setBorder(BorderFactory.createTitledBorder("Danh sách trúng tuyển chi tiết theo ngành"));

        modelThongKeTheoNganhPhuongThuc = new DefaultTableModel(
                new String[]{"Mã ngành", "Tên ngành", "Phương thức điểm", "Số trúng tuyển"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblThongKeTheoNganhPhuongThuc = new JTable(modelThongKeTheoNganhPhuongThuc);
        tblThongKeTheoNganhPhuongThuc.setAutoCreateRowSorter(true);
        tblThongKeTheoNganhPhuongThuc.setRowHeight(24);
        JScrollPane spThongKe = new JScrollPane(tblThongKeTheoNganhPhuongThuc);
        spThongKe.setBorder(BorderFactory.createTitledBorder("Số lượng trúng tuyển từng phương thức theo ngành"));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Trúng tuyển theo ngành", spTrungTuyen);
        tabs.addTab("SL theo ngành - phương thức", spThongKe);

        add(tabs, BorderLayout.CENTER);
    }

    private void buildStatusBar() {
        lblStatus = new JLabel("Chưa tải dữ liệu.");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(lblStatus, BorderLayout.SOUTH);
    }

    @Override
    public void loadData() {
        int limit = spLimit != null && spLimit.getValue() instanceof Number
                ? ((Number) spLimit.getValue()).intValue()
                : 20000;

        if (btnLoad != null) btnLoad.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        lblStatus.setText("Đang tải dữ liệu thống kê xét tuyển...");

        SwingWorker<ThongKeData, Void> worker = new SwingWorker<>() {
            @Override
            protected ThongKeData doInBackground() {
                ThongKeData data = new ThongKeData();
                data.trungTuyen = nguyenVongService.findTrungTuyenChiTietTheoNganh(limit);
                data.thongKe = nguyenVongService.thongKeTrungTuyenTheoNganhPhuongThuc();
                return data;
            }

            @Override
            protected void done() {
                try {
                    ThongKeData data = get();
                    renderData(data);
                    lblStatus.setText("Đã tải " + data.trungTuyen.size()
                            + " dòng chi tiết; " + data.thongKe.size()
                            + " dòng thống kê.");
                } catch (Exception ex) {
                    lblStatus.setText("Lỗi tải dữ liệu: " + ex.getMessage());
                    showError(XetTuyenThongKePanel.this, ex.getMessage());
                } finally {
                    if (btnLoad != null) btnLoad.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

    private void renderData(ThongKeData data) {
        modelTrungTuyenTheoNganh.setRowCount(0);
        for (NguyenVong nv : data.trungTuyen) {
            ThiSinh ts = nv.getThiSinh();
            Nganh n = nv.getNganh();
            ToHop th = nv.getNganhToHop() != null ? nv.getNganhToHop().getToHop() : null;

            modelTrungTuyenTheoNganh.addRow(new Object[]{
                    n != null ? n.getMaNganh() : "",
                    n != null ? n.getTenNganh() : "",
                    ts != null ? ts.getCccd() : "",
                    ts != null ? ts.getHoVaTen() : "",
                    nv.getThuTu() != null ? nv.getThuTu() : "",
                    th != null ? th.getMaTohop() : "",
                    getToHopTotNhatDisplay(nv),
                    nv.getDiemThxt() != null ? formatDiem(nv.getDiemThxt()) : "",
                    nv.getDiemCong() != null ? formatDiem(nv.getDiemCong()) : "",
                    nv.getDiemUutien() != null ? formatDiem(nv.getDiemUutien()) : "",
                    nv.getDiemXettuyen() != null ? formatDiem(nv.getDiemXettuyen()) : "",
                    nv.getPhuongThucDiemTotNhat() != null ? nv.getPhuongThucDiemTotNhat() : ""
            });
        }

        modelThongKeTheoNganhPhuongThuc.setRowCount(0);
        for (Object[] row : data.thongKe) {
            modelThongKeTheoNganhPhuongThuc.addRow(new Object[]{
                    row.length > 0 && row[0] != null ? row[0] : "",
                    row.length > 1 && row[1] != null ? row[1] : "",
                    row.length > 2 && row[2] != null ? row[2] : "",
                    row.length > 3 && row[3] != null ? row[3] : 0
            });
        }
    }

    private String getToHopTotNhatDisplay(NguyenVong nv) {
        if (nv == null) return "";
        if (nv.getToHopDiemTotNhat() != null && !nv.getToHopDiemTotNhat().trim().isEmpty()) {
            return nv.getToHopDiemTotNhat();
        }
        return parseToHopTotNhatFromGhiChu(nv.getGhiChu());
    }

    private String parseToHopTotNhatFromGhiChu(String ghiChu) {
        if (ghiChu == null) return "";

        java.util.regex.Matcher direct = java.util.regex.Pattern
                .compile("Lay diem cao nhat:\\s*[^|()]+\\(([^)]+)\\)")
                .matcher(ghiChu);
        if (direct.find()) {
            return direct.group(1);
        }

        java.util.regex.Matcher sourceMatcher = java.util.regex.Pattern
                .compile("Lay diem cao nhat:\\s*([A-Za-z0-9_]+)")
                .matcher(ghiChu);
        String nguon = sourceMatcher.find() ? sourceMatcher.group(1) : null;

        java.util.regex.Pattern pattern = nguon != null
                ? java.util.regex.Pattern.compile(java.util.regex.Pattern.quote(nguon) + "\\(([^)]+)\\)=([0-9]+(?:\\.[0-9]+)?)")
                : java.util.regex.Pattern.compile("[A-Za-z0-9_]+\\(([^)]+)\\)=([0-9]+(?:\\.[0-9]+)?)");

        java.util.regex.Matcher matcher = pattern.matcher(ghiChu);
        String bestToHop = "";
        double bestScore = -1.0;

        while (matcher.find()) {
            try {
                double score = Double.parseDouble(matcher.group(2));
                if (score > bestScore) {
                    bestScore = score;
                    bestToHop = matcher.group(1);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return bestToHop;
    }

    private String formatDiem(BigDecimal d) {
        if (d == null) return "";
        return d.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private static class ThongKeData {
        List<NguyenVong> trungTuyen;
        List<Object[]> thongKe;
    }
}
