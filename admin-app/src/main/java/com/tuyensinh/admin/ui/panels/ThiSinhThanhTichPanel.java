package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.BaseCrudPanel;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.admin.ui.ToolbarFactory;
import com.tuyensinh.admin.ui.UIConstants;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.entity.ThiSinhThanhTich;
import com.tuyensinh.service.ThiSinhService;
import com.tuyensinh.service.ThiSinhThanhTichService;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ThiSinhThanhTichPanel extends BaseCrudPanel<ThiSinhThanhTich> {

    private final ThiSinhThanhTichService service;
    private final ThiSinhService thiSinhService;

    public ThiSinhThanhTichPanel(MainFrame mainFrame) {
        super(mainFrame);
        this.service = new ThiSinhThanhTichService();
        this.thiSinhService = new ThiSinhService();
        initCrudUI();
        loadData();
    }

    @Override
    protected String[] getTableColumns() {
        return new String[]{
                "ID", "CCCD", "SBD", "Ho ten",
                "Nhom TT", "Cap", "Loai giai", "Mon dat giai",
                "Nam", "Hop le", "Xac minh", "Ghi chu"
        };
    }

    @Override
    protected void buildToolbar() {
        JTextField[] searchFieldOut = new JTextField[1];
        JPanel toolbar = ToolbarFactory.createSearchToolbar(
                searchFieldOut,
                this::doSearch,
                new ToolbarFactory.ActionButton("Import Excel", this::showImportDialog),
                new ToolbarFactory.ActionButton("Them moi", this::showAddDialog),
                new ToolbarFactory.ActionButton("Sua", this::showEditDialog),
                new ToolbarFactory.ActionButton("Xoa", this::doDelete)
        );
        this.searchTextField = searchFieldOut[0];
        add(toolbar, BorderLayout.NORTH);
    }

    @Override
    protected void buildBottomBar() {
        totalLabel = new JLabel("Tong: 0 thanh tich");
        totalLabel.setFont(UIConstants.FONT_SMALL);
        add(totalLabel, BorderLayout.SOUTH);
    }

    @Override
    protected void configureTableColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);
        table.getColumnModel().getColumn(8).setPreferredWidth(60);
        table.getColumnModel().getColumn(9).setPreferredWidth(65);
        table.getColumnModel().getColumn(10).setPreferredWidth(110);
        table.getColumnModel().getColumn(11).setPreferredWidth(240);
    }

    @Override
    public void loadData() {
        model.setRowCount(0);

        String keyword = normalize(searchTextField != null ? searchTextField.getText() : "");
        List<ThiSinhThanhTich> list = service.findAll();

        if (!keyword.isEmpty()) {
            list = list.stream()
                    .filter(tt -> matchesKeyword(tt, keyword))
                    .collect(Collectors.toList());
        }

        list.sort(Comparator.comparing(
                ThiSinhThanhTich::getThanhtichId,
                Comparator.nullsLast(Integer::compareTo)
        ));

        for (ThiSinhThanhTich tt : list) {
            ThiSinh ts = tt.getThiSinh();

            model.addRow(new Object[]{
                    tt.getThanhtichId(),
                    ts != null ? safe(ts.getCccd()) : "",
                    ts != null ? safe(ts.getSobaodanh()) : "",
                    ts != null ? safe(ts.getHoVaTen()) : "",
                    safe(tt.getNhomThanhTich()),
                    safe(tt.getCapThanhTich()),
                    safe(tt.getLoaiGiai()),
                    safe(tt.getMonDatGiai()),
                    tt.getNamDatGiai() != null ? tt.getNamDatGiai().toString() : "",
                    Boolean.TRUE.equals(tt.getIsHopLe()) ? "Co" : "Khong",
                    safe(tt.getTrangThaiXacMinh()),
                    safe(tt.getGhiChu())
            });
        }

        updateTotalLabel(list.size(), "thanh tich");
    }

    @Override
    protected ThiSinhThanhTich getSelectedEntity() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Integer id = (Integer) model.getValueAt(row, 0);
        return service.findById(id);
    }

    @Override
    protected Integer getSelectedId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (Integer) model.getValueAt(row, 0);
    }

    @Override
    public String getPageTitle() {
        return "Quan ly thanh tich uu tien";
    }

    @Override
    protected String getEntityDisplayName(ThiSinhThanhTich entity) {
        ThiSinh ts = entity.getThiSinh();
        String owner = ts != null ? safe(ts.getHoVaTen()) : "Thi sinh";
        return owner + " - " + safe(entity.getNhomThanhTich()) + " - " + safe(entity.getLoaiGiai());
    }

    @Override
    protected void deleteEntity(ThiSinhThanhTich entity) throws Exception {
        service.delete(entity);
    }

    @Override
    protected void showAddDialog() {
        JTextField txtThiSinhKey = new JTextField(20);
        JTextField txtNhom = new JTextField(20);
        JTextField txtCap = new JTextField(20);
        JTextField txtLoaiGiai = new JTextField(20);
        JTextField txtTenThanhTich = new JTextField(20);
        JTextField txtMonDatGiai = new JTextField(20);
        JTextField txtLinhVuc = new JTextField(20);
        JTextField txtNam = new JTextField(20);
        JTextField txtDonVi = new JTextField(20);
        JTextField txtSoHieu = new JTextField(20);

        JCheckBox chkHopLe = new JCheckBox("Thanh tich hop le", true);
        JComboBox<String> cboTrangThai = new JComboBox<>(new String[]{
                "CHUA_XAC_MINH",
                "DA_XAC_MINH",
                "TU_CHOI"
        });

        JTextArea txtGhiChu = new JTextArea(4, 20);
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);

        Object[] form = new Object[]{
                "CCCD / SBD thi sinh (*):", txtThiSinhKey,
                "Nhom thanh tich (*):", txtNhom,
                "Cap thanh tich:", txtCap,
                "Loai giai:", txtLoaiGiai,
                "Ten thanh tich:", txtTenThanhTich,
                "Mon dat giai:", txtMonDatGiai,
                "Linh vuc:", txtLinhVuc,
                "Nam dat giai:", txtNam,
                "Don vi to chuc:", txtDonVi,
                "So hieu minh chung:", txtSoHieu,
                "Trang thai xac minh:", cboTrangThai,
                chkHopLe,
                "Ghi chu:", new JScrollPane(txtGhiChu)
        };

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Them thanh tich uu tien",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) return;

        ThiSinh thiSinh = resolveThiSinh(txtThiSinhKey.getText());
        if (thiSinh == null) {
            showError(this, "Khong tim thay thi sinh theo CCCD / SBD.");
            return;
        }

        String nhom = trimToNull(txtNhom.getText());
        if (nhom == null) {
            showMessage(this, "Nhom thanh tich la bat buoc.");
            return;
        }

        ThiSinhThanhTich entity = new ThiSinhThanhTich();
        entity.setThiSinh(thiSinh);
        entity.setNhomThanhTich(nhom);
        entity.setCapThanhTich(trimToNull(txtCap.getText()));
        entity.setLoaiGiai(trimToNull(txtLoaiGiai.getText()));
        entity.setTenThanhTich(trimToNull(txtTenThanhTich.getText()));
        entity.setMonDatGiai(trimToNull(txtMonDatGiai.getText()));
        entity.setLinhVuc(trimToNull(txtLinhVuc.getText()));
        entity.setNamDatGiai(parseShortFlexible(txtNam.getText()));
        entity.setDonViToChuc(trimToNull(txtDonVi.getText()));
        entity.setSoHieuMinhChung(trimToNull(txtSoHieu.getText()));
        entity.setIsHopLe(chkHopLe.isSelected());
        entity.setTrangThaiXacMinh((String) cboTrangThai.getSelectedItem());
        entity.setGhiChu(trimToNull(txtGhiChu.getText()));

        try {
            service.save(entity);
            showSuccess(this, "Them thanh tich thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    @Override
    protected void showEditDialog() {
        ThiSinhThanhTich entity = getSelectedEntity();
        if (entity == null) {
            showSelectRow();
            return;
        }

        ThiSinh ts = entity.getThiSinh();

        JTextField txtThiSinhKey = new JTextField(
                ts != null ? safe(ts.getCccd()) : "",
                20
        );
        txtThiSinhKey.setEditable(false);

        JTextField txtNhom = new JTextField(safe(entity.getNhomThanhTich()), 20);
        JTextField txtCap = new JTextField(safe(entity.getCapThanhTich()), 20);
        JTextField txtLoaiGiai = new JTextField(safe(entity.getLoaiGiai()), 20);
        JTextField txtTenThanhTich = new JTextField(safe(entity.getTenThanhTich()), 20);
        JTextField txtMonDatGiai = new JTextField(safe(entity.getMonDatGiai()), 20);
        JTextField txtLinhVuc = new JTextField(safe(entity.getLinhVuc()), 20);
        JTextField txtNam = new JTextField(entity.getNamDatGiai() != null ? entity.getNamDatGiai().toString() : "", 20);
        JTextField txtDonVi = new JTextField(safe(entity.getDonViToChuc()), 20);
        JTextField txtSoHieu = new JTextField(safe(entity.getSoHieuMinhChung()), 20);

        JCheckBox chkHopLe = new JCheckBox("Thanh tich hop le", Boolean.TRUE.equals(entity.getIsHopLe()));
        JComboBox<String> cboTrangThai = new JComboBox<>(new String[]{
                "CHUA_XAC_MINH",
                "DA_XAC_MINH",
                "TU_CHOI"
        });
        cboTrangThai.setSelectedItem(
                entity.getTrangThaiXacMinh() != null ? entity.getTrangThaiXacMinh() : "CHUA_XAC_MINH"
        );

        JTextArea txtGhiChu = new JTextArea(safe(entity.getGhiChu()), 4, 20);
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);

        Object[] form = new Object[]{
                "CCCD thi sinh:", txtThiSinhKey,
                "Nhom thanh tich (*):", txtNhom,
                "Cap thanh tich:", txtCap,
                "Loai giai:", txtLoaiGiai,
                "Ten thanh tich:", txtTenThanhTich,
                "Mon dat giai:", txtMonDatGiai,
                "Linh vuc:", txtLinhVuc,
                "Nam dat giai:", txtNam,
                "Don vi to chuc:", txtDonVi,
                "So hieu minh chung:", txtSoHieu,
                "Trang thai xac minh:", cboTrangThai,
                chkHopLe,
                "Ghi chu:", new JScrollPane(txtGhiChu)
        };

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Sua thanh tich uu tien",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) return;

        String nhom = trimToNull(txtNhom.getText());
        if (nhom == null) {
            showMessage(this, "Nhom thanh tich la bat buoc.");
            return;
        }

        entity.setNhomThanhTich(nhom);
        entity.setCapThanhTich(trimToNull(txtCap.getText()));
        entity.setLoaiGiai(trimToNull(txtLoaiGiai.getText()));
        entity.setTenThanhTich(trimToNull(txtTenThanhTich.getText()));
        entity.setMonDatGiai(trimToNull(txtMonDatGiai.getText()));
        entity.setLinhVuc(trimToNull(txtLinhVuc.getText()));
        entity.setNamDatGiai(parseShortFlexible(txtNam.getText()));
        entity.setDonViToChuc(trimToNull(txtDonVi.getText()));
        entity.setSoHieuMinhChung(trimToNull(txtSoHieu.getText()));
        entity.setIsHopLe(chkHopLe.isSelected());
        entity.setTrangThaiXacMinh((String) cboTrangThai.getSelectedItem());
        entity.setGhiChu(trimToNull(txtGhiChu.getText()));

        try {
            service.update(entity);
            showSuccess(this, UIConstants.MSG_UPDATE_SUCCESS);
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private void showImportDialog() {
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Import thanh tich uu tien",
                true
        );
        dialog.setContentPane(new ThiSinhThanhTichImportPanel(mainFrame));
        dialog.setSize(960, 700);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        loadData();
    }

    private ThiSinh resolveThiSinh(String rawKey) {
        String key = normalize(rawKey);
        if (key.isEmpty()) return null;

        Optional<ThiSinh> byCccd = thiSinhService.findByCccd(key);
        if (byCccd.isPresent()) return byCccd.get();

        Optional<ThiSinh> bySbd = thiSinhService.findBySoBaoDanh(key);
        return bySbd.orElse(null);
    }

    private boolean matchesKeyword(ThiSinhThanhTich tt, String keyword) {
        ThiSinh ts = tt.getThiSinh();

        return contains(ts != null ? ts.getCccd() : null, keyword)
                || contains(ts != null ? ts.getSobaodanh() : null, keyword)
                || contains(ts != null ? ts.getHoVaTen() : null, keyword)
                || contains(tt.getNhomThanhTich(), keyword)
                || contains(tt.getCapThanhTich(), keyword)
                || contains(tt.getLoaiGiai(), keyword)
                || contains(tt.getTenThanhTich(), keyword)
                || contains(tt.getMonDatGiai(), keyword)
                || contains(tt.getLinhVuc(), keyword)
                || contains(tt.getTrangThaiXacMinh(), keyword)
                || contains(tt.getGhiChu(), keyword);
    }

    private boolean contains(String source, String keyword) {
        return normalize(source).contains(keyword);
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String value = s.trim();
        return value.isEmpty() ? null : value;
    }

    private Short parseShortFlexible(String value) {
        String raw = value == null ? "" : value.trim();
        if (raw.isEmpty()) return null;
        try {
            return Short.valueOf(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}