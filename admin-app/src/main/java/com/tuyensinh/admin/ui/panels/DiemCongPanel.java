package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.*;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Refactored: extends BaseCrudPanel, uses TableFactory + parse helpers.
 */
public class DiemCongPanel extends BaseCrudPanel<DiemCong> {

    private DiemCongService diemCongService;
    private ThiSinhService thiSinhService;
    private NganhToHopService nganhToHopService;
    private NguyenVongService nguyenVongService;
    private ThiSinhChungChiService thiSinhChungChiService;
    private ThiSinhThanhTichService thiSinhThanhTichService;
    private TinhDiemService tinhDiemService;
    private DiemCongChiTietService diemCongChiTietService;
    private com.tuyensinh.dao.PhuongThucDao phuongThucDao;

    private JTable detailTable;
    private DefaultTableModel detailModel;
    private JLabel detailTitleLabel;

    public DiemCongPanel(MainFrame mainFrame) {
        super(mainFrame);
        diemCongService = new DiemCongService();
        thiSinhService = new ThiSinhService();
        nganhToHopService = new NganhToHopService();
        nguyenVongService = new NguyenVongService();
        thiSinhChungChiService = new ThiSinhChungChiService();
        thiSinhThanhTichService = new ThiSinhThanhTichService();
        tinhDiemService = new TinhDiemService();
        diemCongChiTietService = new DiemCongChiTietService();
        phuongThucDao = new com.tuyensinh.dao.PhuongThucDao();
        initUI();
        loadData();
    }

    @Override
    protected String[] getTableColumns() {
        return new String[]{"ID", "CCCD", "Ho Ten", "Nganh", "To Hop", "Ph. thuc", "Diem CC", "Diem UTXT", "Diem UTQC", "Diem Tong"};
    }

    @Override
    protected DiemCong getSelectedEntity() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return diemCongService.findById((Integer) model.getValueAt(row, 0));
    }

    @Override
    protected Integer getSelectedId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (Integer) model.getValueAt(row, 0);
    }

    @Override
    public String getPageTitle() {
        return UIConstants.PAGE_DIEM_CONG;
    }

    @Override
    protected void buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.add(new JLabel("Quan ly diem cong (diem uu tien, diem chung chi)"));
        toolbar.add(Box.createHorizontalStrut(20));

        JButton btnAutoAll = new JButton("Tao tat ca diem cong");
        btnAutoAll.addActionListener(e -> showGenerateAllDialog());
        toolbar.add(btnAutoAll);

        JButton btnAdd = new JButton("Them moi");
        btnAdd.addActionListener(e -> showAddDialog());
        toolbar.add(btnAdd);

        JButton btnEdit = new JButton("Sua");
        btnEdit.addActionListener(e -> showEditDialog());
        toolbar.add(btnEdit);

        JButton btnDelete = new JButton("Xoa");
        btnDelete.addActionListener(e -> doDelete());
        toolbar.add(btnDelete);

        add(toolbar, BorderLayout.NORTH);
    }

    @Override
    protected void buildBottomBar() {
        totalLabel = new JLabel("Tong: 0");
        totalLabel.setFont(UIConstants.FONT_SMALL);
        add(totalLabel, BorderLayout.SOUTH);
    }

    @Override
    public void loadData() {
        model.setRowCount(0);
        var list = diemCongService.findAll();
        for (DiemCong dc : list) {
            ThiSinh ts = dc.getThiSinh();
            model.addRow(new Object[]{
                    dc.getDiemcongId(),
                    ts != null ? ts.getCccd() : "",
                    ts != null ? ts.getHoVaTen() : "",
                    dc.getNganhToHop() != null ? dc.getNganhToHop().getNganh().getMaNganh() : "",
                    dc.getNganhToHop() != null ? dc.getNganhToHop().getToHop().getMaTohop() : "",
                    dc.getPhuongThuc() != null ? dc.getPhuongThuc().getMaPhuongthuc() : "",
                    dc.getTongDiemChungChi(),
                    dc.getTongDiemUutienXt(),
                    dc.getTongDiemUutienQuyChe(),
                    dc.getTongDiemCong()
            });
        }
        updateTotalLabel(list.size(), "ban ghi");

        if (model.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
            loadDetailForSelectedRow();
        } else {
            if (detailModel != null) {
                detailModel.setRowCount(0);
            }
            if (detailTitleLabel != null) {
                detailTitleLabel.setText("Chi tiet diem cong: khong co du lieu");
            }
        }
    }

    @Override
    protected String getEntityDisplayName(DiemCong dc) {
        return dc.getThiSinh() != null ? dc.getThiSinh().getHoVaTen() : String.valueOf(dc.getDiemcongId());
    }

    @Override
    protected void deleteEntity(DiemCong dc) throws Exception {
        diemCongService.delete(dc);
    }

    @Override
    protected void showAddDialog() {
        JTextField txtCccd = new JTextField(20);
        JComboBox<NganhToHop> cboNt = new JComboBox<>();
        JComboBox<PhuongThuc> cboPt = new JComboBox<>();
        for (NganhToHop nt : nganhToHopService.findAll()) cboNt.addItem(nt);
        for (PhuongThuc pt : phuongThucDao.findAll()) cboPt.addItem(pt);
        configureNganhToHopCombo(cboNt);
        configurePhuongThucCombo(cboPt);

        JTextField txtDiemCC = new JTextField("0", 20);
        JTextField txtDiemUTXT = new JTextField("0", 20);
        JTextField txtDiemUTQC = new JTextField("0", 20);

        int r = JOptionPane.showConfirmDialog(this, new Object[]{
                "CCCD thi sinh:", txtCccd,
                "Nganh - To hop:", cboNt,
                "Phuong thuc:", cboPt,
                "Tong diem chung chi:", txtDiemCC,
                "Tong diem uu tien XT:", txtDiemUTXT,
                "Tong diem uu tien quy che:", txtDiemUTQC
        }, "Them diem cong", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        String cccd = txtCccd.getText().trim();
        if (cccd.isEmpty()) {
            showMessage(this, "CCCD la bat buoc!");
            return;
        }

        var optTs = thiSinhService.findByCccd(cccd);
        if (optTs.isEmpty()) {
            showMessage(this, "Khong tim thay thi sinh!");
            return;
        }

        ThiSinh ts = optTs.get();

        // Auto-fill diem uu tien quy che tu KhuVuc + DoiTuong
        BigDecimal diemUuTienQuyCheAuto = tinhDiemUuTien(ts);
        if (diemUuTienQuyCheAuto.compareTo(BigDecimal.ZERO) > 0) {
            txtDiemUTQC.setText(diemUuTienQuyCheAuto.setScale(2, RoundingMode.HALF_UP).toPlainString());
            String kv = ts.getKhuVucUutien() != null
                    ? ts.getKhuVucUutien().getTenKhuvuc() + " (" + ts.getKhuVucUutien().getMucDiem() + ")"
                    : "Khong co";
            String dt = ts.getDoiTuongUutien() != null
                    ? ts.getDoiTuongUutien().getTenDoituong() + " (" + ts.getDoiTuongUutien().getMucDiem() + ")"
                    : "Khong co";
            showMessage(this, "Diem uu tien quy che tu dong: "
                    + diemUuTienQuyCheAuto.setScale(2, RoundingMode.HALF_UP).toPlainString()
                    + "\n(Khu vuc: " + kv + " | Doi tuong: " + dt + ")");
        }

        NganhToHop nt = (NganhToHop) cboNt.getSelectedItem();
        PhuongThuc pt = (PhuongThuc) cboPt.getSelectedItem();
        if (nt == null || pt == null) {
            showMessage(this, "Chon day du thong tin!");
            return;
        }

        DiemCong dc = new DiemCong();
        dc.setThiSinh(ts);
        dc.setNganhToHop(nt);
        dc.setPhuongThuc(pt);

        dc.setTongDiemChungChi(parseBigDecimal(txtDiemCC.getText()));
        dc.setTongDiemUutienXt(parseBigDecimal(txtDiemUTXT.getText()));
        dc.setTongDiemUutienQuyChe(parseBigDecimal(txtDiemUTQC.getText()));

        BigDecimal cc = safe(dc.getTongDiemChungChi());
        BigDecimal utXt = safe(dc.getTongDiemUutienXt());
        BigDecimal utQc = safe(dc.getTongDiemUutienQuyChe());
        dc.setTongDiemCong(cc.add(utXt).add(utQc));

        try {
            diemCongService.save(dc);
            showSuccess(this, "Them thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private static BigDecimal tinhDiemUuTien(ThiSinh ts) {
        BigDecimal tong = BigDecimal.ZERO;
        if (ts.getKhuVucUutien() != null && ts.getKhuVucUutien().getMucDiem() != null) {
            tong = tong.add(ts.getKhuVucUutien().getMucDiem());
        }
        if (ts.getDoiTuongUutien() != null && ts.getDoiTuongUutien().getMucDiem() != null) {
            tong = tong.add(ts.getDoiTuongUutien().getMucDiem());
        }
        return tong;
    }

    private static BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    @Override
    protected void showEditDialog() {
        DiemCong dc = getSelectedEntity();
        if (dc == null) {
            showSelectRow();
            return;
        }

        JTextField txtDiemCC = new JTextField(
                dc.getTongDiemChungChi() != null ? dc.getTongDiemChungChi().toString() : "0", 20);
        JTextField txtDiemUTXT = new JTextField(
                dc.getTongDiemUutienXt() != null ? dc.getTongDiemUutienXt().toString() : "0", 20);
        JTextField txtDiemUTQC = new JTextField(
                dc.getTongDiemUutienQuyChe() != null ? dc.getTongDiemUutienQuyChe().toString() : "0", 20);

        int r = JOptionPane.showConfirmDialog(this, new Object[]{
                "Thi sinh: " + (dc.getThiSinh() != null ? dc.getThiSinh().getHoVaTen() : ""),
                "Tong diem chung chi:", txtDiemCC,
                "Tong diem uu tien XT:", txtDiemUTXT,
                "Tong diem uu tien quy che:", txtDiemUTQC
        }, "Sua diem cong", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        dc.setTongDiemChungChi(parseBigDecimal(txtDiemCC.getText()));
        dc.setTongDiemUutienXt(parseBigDecimal(txtDiemUTXT.getText()));
        dc.setTongDiemUutienQuyChe(parseBigDecimal(txtDiemUTQC.getText()));

        BigDecimal cc = safe(dc.getTongDiemChungChi());
        BigDecimal utXt = safe(dc.getTongDiemUutienXt());
        BigDecimal utQc = safe(dc.getTongDiemUutienQuyChe());
        dc.setTongDiemCong(cc.add(utXt).add(utQc));

        try {
            diemCongService.update(dc);
            showSuccess(this, "Cap nhat thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    /** Hien thi ma nganh, ten nganh, ma to hop (khong dung toString day du). */
    private static void configureNganhToHopCombo(JComboBox<NganhToHop> combo) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NganhToHop) {
                    NganhToHop nt = (NganhToHop) value;
                    Nganh n = nt.getNganh();
                    ToHop th = nt.getToHop();
                    String maNganh = n != null && n.getMaNganh() != null ? n.getMaNganh() : "?";
                    String tenNganh = n != null && n.getTenNganh() != null ? n.getTenNganh() : "";
                    String maTh = th != null && th.getMaTohop() != null ? th.getMaTohop() : "?";
                    String tenTh = th != null && th.getTenTohop() != null ? th.getTenTohop() : "";
                    String doLech = nt.getDoLech() != null ? nt.getDoLech().toString() : "";
                    setText(maNganh + " | " + tenNganh + " | " + maTh + " | " + tenTh + " | " + doLech);
                }
                return this;
            }
        });
    }

    /** Hien thi ten phuong thuc va thang diem. */
    private static void configurePhuongThucCombo(JComboBox<PhuongThuc> combo) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof PhuongThuc) {
                    PhuongThuc pt = (PhuongThuc) value;
                    String ten = pt.getTenPhuongthuc() != null ? pt.getTenPhuongthuc() : pt.getMaPhuongthuc();
                    BigDecimal thang = pt.getThangDiem();
                    String thangStr = thang != null ? thang.stripTrailingZeros().toPlainString() : "";
                    setText(ten + " — " + thangStr);
                }
                return this;
            }
        });
    }

    private boolean coNguyenVong(ThiSinh ts) {
        if (ts == null || ts.getThisinhId() == null) return false;
        List<NguyenVong> nvs = nguyenVongService.findByThiSinhId(ts.getThisinhId());
        return nvs != null && !nvs.isEmpty();
    }

    private boolean coNguonDuLieuTinhDiem(ThiSinh ts) {
        if (ts == null || ts.getThisinhId() == null) return false;

        boolean coChungChi = !thiSinhChungChiService.findHopLeByThiSinhId(ts.getThisinhId()).isEmpty();
        boolean coThanhTich = !thiSinhThanhTichService.findHopLeByThiSinhId(ts.getThisinhId()).isEmpty();
        boolean coUuTienQuyChe = tinhDiemUuTien(ts).compareTo(BigDecimal.ZERO) > 0;

        return coChungChi || coThanhTich || coUuTienQuyChe;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String[] getDetailTableColumns() {
        return new String[]{
                "ID", "Loai nguon", "Ma nguon", "Ten nguon",
                "Cap ap dung", "Mon lien quan", "Gia tri goc",
                "Diem quy doi", "Diem cong", "Thu tu", "Ap dung", "Ghi chu"
        };
    }

    @Override
    protected void buildTable() {
        // ===== BANG TONG DIEM CONG =====
        model = TableFactory.newReadOnlyModel(getTableColumns());
        table = TableFactory.create(model);
        configureTableColumns();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRowSelected();
            }
        });

        JScrollPane topScroll = TableFactory.wrap(table);
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Danh sach diem cong",
                TitledBorder.LEFT,
                TitledBorder.TOP
        ));
        topPanel.add(topScroll, BorderLayout.CENTER);

        // ===== BANG CHI TIET =====
        detailModel = TableFactory.newReadOnlyModel(getDetailTableColumns());
        detailTable = TableFactory.create(detailModel);
        configureDetailTableColumns();

        JScrollPane bottomScroll = TableFactory.wrap(detailTable);

        detailTitleLabel = new JLabel("Chi tiet diem cong: chua chon dong nao");
        detailTitleLabel.setBorder(BorderFactory.createEmptyBorder(4, 6, 6, 6));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Chi tiet diem cong",
                TitledBorder.LEFT,
                TitledBorder.TOP
        ));
        bottomPanel.add(detailTitleLabel, BorderLayout.NORTH);
        bottomPanel.add(bottomScroll, BorderLayout.CENTER);

        // ===== CHIA DOI MAN HINH =====
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
        splitPane.setResizeWeight(0.62);
        splitPane.setDividerLocation(320);

        add(splitPane, BorderLayout.CENTER);
    }

    private void configureDetailTableColumns() {
        if (detailTable == null) return;

        detailTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        detailTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        detailTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        detailTable.getColumnModel().getColumn(3).setPreferredWidth(180);
        detailTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        detailTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        detailTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        detailTable.getColumnModel().getColumn(7).setPreferredWidth(90);
        detailTable.getColumnModel().getColumn(8).setPreferredWidth(90);
        detailTable.getColumnModel().getColumn(9).setPreferredWidth(60);
        detailTable.getColumnModel().getColumn(10).setPreferredWidth(65);
        detailTable.getColumnModel().getColumn(11).setPreferredWidth(260);
    }

    @Override
    protected void onRowSelected() {
        loadDetailForSelectedRow();
    }

    private void loadDetailForSelectedRow() {
        if (detailModel == null) return;

        detailModel.setRowCount(0);

        DiemCong selected = getSelectedEntity();
        if (selected == null) {
            if (detailTitleLabel != null) {
                detailTitleLabel.setText("Chi tiet diem cong: chua chon dong nao");
            }
            return;
        }

        List<DiemCongChiTiet> details = diemCongChiTietService.findByDiemCongId(selected.getDiemcongId());

        String cccd = selected.getThiSinh() != null ? safe(selected.getThiSinh().getCccd()) : "";
        String hoTen = selected.getThiSinh() != null ? safe(selected.getThiSinh().getHoVaTen()) : "";

        if (detailTitleLabel != null) {
            detailTitleLabel.setText(
                    "Chi tiet diem cong cho: " + cccd + " - " + hoTen
                            + " | So dong: " + details.size()
            );
        }

        for (DiemCongChiTiet ct : details) {
            detailModel.addRow(new Object[]{
                    ct.getDiemcongCtId(),
                    ct.getLoaiNguon() != null ? ct.getLoaiNguon().name() : "",
                    safe(ct.getMaNguon()),
                    safe(ct.getTenNguon()),
                    safe(ct.getCapApDung()),
                    safe(ct.getMonLienQuan()),
                    safe(ct.getGiaTriGoc()),
                    bd(ct.getDiemQuyDoi()),
                    bd(ct.getDiemCongGiaTri()),
                    ct.getThuTuUuTien(),
                    Boolean.TRUE.equals(ct.getIsApDung()) ? "Co" : "Khong",
                    safe(ct.getGhiChu())
            });
        }
    }

    private String bd(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private void showGenerateAllDialog() {
        JCheckBox chkOnlyHasSource = new JCheckBox("Chi tao cho thi sinh co nguon du lieu diem cong", true);
        JCheckBox chkOnlyHasNguyenVong = new JCheckBox("Chi tao cho thi sinh co nguyen vong", true);
        JCheckBox chkClearOld = new JCheckBox("Xoa toan bo diem cong cu truoc khi tao lai", false);

        JSpinner spLimit = new JSpinner(new SpinnerNumberModel(0, 0, 200000, 100));

        Object[] form = new Object[]{
                "Tuy chon tao tat ca diem cong:",
                chkOnlyHasSource,
                chkOnlyHasNguyenVong,
                chkClearOld,
                "Gioi han so luong (0 = khong gioi han):", spLimit
        };

        int r = JOptionPane.showConfirmDialog(
                this,
                form,
                "Tao tat ca diem cong",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (r != JOptionPane.OK_OPTION) return;

        boolean onlyHasSource = chkOnlyHasSource.isSelected();
        boolean onlyHasNguyenVong = chkOnlyHasNguyenVong.isSelected();
        boolean clearOld = chkClearOld.isSelected();
        int limit = (Integer) spLimit.getValue();

        runGenerateAll(onlyHasSource, onlyHasNguyenVong, clearOld, limit);
    }

    private void runGenerateAll(boolean onlyHasSource, boolean onlyHasNguyenVong, boolean clearOld, int limit) {
        List<ThiSinh> all = new ArrayList<>(thiSinhService.findAll());
        all.sort(Comparator.comparing(
                ThiSinh::getThisinhId,
                Comparator.nullsLast(Integer::compareTo)
        ));

        if (clearOld) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Ban chac chan muon xoa toan bo diem cong va chi tiet diem cong cu?",
                    "Xac nhan xoa du lieu cu",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            List<DiemCong> oldList = new ArrayList<>(diemCongService.findAll());
            for (DiemCong dc : oldList) {
                try {
                    diemCongChiTietService.deleteByDiemCongId(dc.getDiemcongId());
                } catch (Exception ignored) {
                }
            }
            for (DiemCong dc : oldList) {
                try {
                    diemCongService.delete(dc);
                } catch (Exception ignored) {
                }
            }
        }

        StringBuilder log = new StringBuilder();
        int processed = 0;
        int success = 0;
        int skipped = 0;
        int error = 0;

        for (ThiSinh ts : all) {
            if (limit > 0 && processed >= limit) break;

            try {
                if (onlyHasNguyenVong && !coNguyenVong(ts)) {
                    skipped++;
                    continue;
                }

                if (onlyHasSource && !coNguonDuLieuTinhDiem(ts)) {
                    skipped++;
                    continue;
                }

                List<DiemCong> created = tinhDiemService.taoDiemCongTuDong(ts, null);
                processed++;

                if (created == null || created.isEmpty()) {
                    skipped++;
                    log.append("- ")
                            .append(safe(ts.getCccd())).append(" | ")
                            .append(safe(ts.getHoVaTen()))
                            .append(": khong tao duoc ban ghi diem cong.\n");
                } else {
                    success++;
                    log.append("+ ")
                            .append(safe(ts.getCccd())).append(" | ")
                            .append(safe(ts.getHoVaTen()))
                            .append(": tao/cap nhat ")
                            .append(created.size())
                            .append(" ban ghi.\n");
                }
            } catch (Exception ex) {
                error++;
                Throwable root = ex;
                while (root.getCause() != null) {
                    root = root.getCause();
                }

                log.append("! ")
                        .append(safe(ts.getCccd())).append(" | ")
                        .append(safe(ts.getHoVaTen()))
                        .append(": loi -> ")
                        .append(root.getMessage())
                        .append("\n");
            }
        }

        loadData();

        JTextArea taLog = new JTextArea(log.toString(), 20, 70);
        taLog.setEditable(false);
        taLog.setCaretPosition(0);

        JOptionPane.showMessageDialog(
                this,
                new Object[]{
                        "Ket qua tao tat ca diem cong:",
                        "Da xu ly: " + processed,
                        "Thanh cong: " + success,
                        "Bo qua: " + skipped,
                        "Loi: " + error,
                        new JScrollPane(taLog)
                },
                "Ket qua",
                error > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE
        );
    }
}