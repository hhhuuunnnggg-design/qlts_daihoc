package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.*;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Refactored: extends BaseCrudPanel, uses TableFactory + parse helpers.
 */
public class DiemCongPanel extends BaseCrudPanel<DiemCong> {

    private DiemCongService diemCongService;
    private ThiSinhService thiSinhService;
    private NganhToHopService nganhToHopService;
    private com.tuyensinh.dao.PhuongThucDao phuongThucDao;

    public DiemCongPanel(MainFrame mainFrame) {
        super(mainFrame);
        diemCongService = new DiemCongService();
        thiSinhService = new ThiSinhService();
        nganhToHopService = new NganhToHopService();
        phuongThucDao = new com.tuyensinh.dao.PhuongThucDao();
        initUI();
        loadData();
    }

    @Override
    protected String[] getTableColumns() {
        return new String[]{"ID", "CCCD", "Ho Ten", "Nganh", "To Hop", "Ph. thuc", "Diem CC", "Diem UT", "Diem Tong"};
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
                dc.getDiemChungchi(),
                dc.getDiemUutienXt(),
                dc.getDiemTong()
            });
        }
        updateTotalLabel(list.size(), "ban ghi");
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
        JTextField txtDiemUT = new JTextField("0", 20);

        int r = JOptionPane.showConfirmDialog(this, new Object[]{
            "CCCD thi sinh:", txtCccd,
            "Nganh - To hop:", cboNt,
            "Phuong thuc:", cboPt,
            "Diem chung chi:", txtDiemCC,
            "Diem uu tien:", txtDiemUT
        }, "Them diem cong", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        String cccd = txtCccd.getText().trim();
        if (cccd.isEmpty()) { showMessage(this, "CCCD la bat buoc!"); return; }

        var optTs = thiSinhService.findByCccd(cccd);
        if (optTs.isEmpty()) { showMessage(this, "Khong tim thay thi sinh!"); return; }

        ThiSinh ts = optTs.get();

        // Auto-fill diem uu tien tu KhuVuc + DoiTuong
        BigDecimal diemUuTienAuto = tinhDiemUuTien(ts);
        if (diemUuTienAuto.compareTo(BigDecimal.ZERO) > 0) {
            txtDiemUT.setText(diemUuTienAuto.setScale(2, RoundingMode.HALF_UP).toPlainString());
            String kv = ts.getKhuVucUutien() != null ? ts.getKhuVucUutien().getTenKhuvuc() + " (" + ts.getKhuVucUutien().getMucDiem() + ")" : "Khong co";
            String dt = ts.getDoiTuongUutien() != null ? ts.getDoiTuongUutien().getTenDoituong() + " (" + ts.getDoiTuongUutien().getMucDiem() + ")" : "Khong co";
            showMessage(this, "Diem uu tien tu dong: " + diemUuTienAuto.setScale(2, RoundingMode.HALF_UP).toPlainString()
                + "\n(Khu vuc: " + kv + " | Doi tuong: " + dt + ")");
        }

        NganhToHop nt = (NganhToHop) cboNt.getSelectedItem();
        PhuongThuc pt = (PhuongThuc) cboPt.getSelectedItem();
        if (nt == null || pt == null) { showMessage(this, "Chon day du thong tin!"); return; }

        DiemCong dc = new DiemCong();
        dc.setThiSinh(ts);
        dc.setNganhToHop(nt);
        dc.setPhuongThuc(pt);
        dc.setDiemChungchi(parseBigDecimal(txtDiemCC.getText()));
        dc.setDiemUutienXt(parseBigDecimal(txtDiemUT.getText()));

        BigDecimal cc = dc.getDiemChungchi() != null ? dc.getDiemChungchi() : BigDecimal.ZERO;
        BigDecimal ut = dc.getDiemUutienXt() != null ? dc.getDiemUutienXt() : BigDecimal.ZERO;
        dc.setDiemTong(cc.add(ut));

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

    @Override
    protected void showEditDialog() {
        DiemCong dc = getSelectedEntity();
        if (dc == null) { showSelectRow(); return; }

        JTextField txtDiemCC = new JTextField(dc.getDiemChungchi() != null ? dc.getDiemChungchi().toString() : "0", 20);
        JTextField txtDiemUT = new JTextField(dc.getDiemUutienXt() != null ? dc.getDiemUutienXt().toString() : "0", 20);

        int r = JOptionPane.showConfirmDialog(this, new Object[]{
            "Thi sinh: " + (dc.getThiSinh() != null ? dc.getThiSinh().getHoVaTen() : ""),
            "Diem chung chi:", txtDiemCC,
            "Diem uu tien:", txtDiemUT
        }, "Sua diem cong", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        dc.setDiemChungchi(parseBigDecimal(txtDiemCC.getText()));
        dc.setDiemUutienXt(parseBigDecimal(txtDiemUT.getText()));

        BigDecimal cc = dc.getDiemChungchi() != null ? dc.getDiemChungchi() : BigDecimal.ZERO;
        BigDecimal ut = dc.getDiemUutienXt() != null ? dc.getDiemUutienXt() : BigDecimal.ZERO;
        dc.setDiemTong(cc.add(ut));

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
                    String doLech = nt != null && nt.getDoLech() != null ? nt.getDoLech().toString() : "";
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
}
