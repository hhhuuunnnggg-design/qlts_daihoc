package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.BaseCrudPanel;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.admin.ui.ToolbarFactory;
import com.tuyensinh.admin.ui.UIConstants;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.entity.ThiSinhChungChi;
import com.tuyensinh.service.ThiSinhChungChiService;
import com.tuyensinh.service.ThiSinhService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ThiSinhChungChiPanel extends BaseCrudPanel<ThiSinhChungChi> {

    private final ThiSinhChungChiService service;
    private final ThiSinhService thiSinhService;

    public ThiSinhChungChiPanel(MainFrame mainFrame) {
        super(mainFrame);
        this.service = new ThiSinhChungChiService();
        this.thiSinhService = new ThiSinhService();
        initCrudUI();
        loadData();
    }

    @Override
    protected String[] getTableColumns() {
        return new String[]{
                "ID", "CCCD", "SBD", "Ho ten",
                "Loai CC", "Ten chung chi", "Diem goc", "Bac",
                "Hop le", "Xac minh", "Ghi chu"
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
        totalLabel = new JLabel("Tong: 0 chung chi");
        totalLabel.setFont(UIConstants.FONT_SMALL);
        add(totalLabel, BorderLayout.SOUTH);
    }

    @Override
    protected void configureTableColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(170);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(80);
        table.getColumnModel().getColumn(8).setPreferredWidth(65);
        table.getColumnModel().getColumn(9).setPreferredWidth(110);
        table.getColumnModel().getColumn(10).setPreferredWidth(220);
    }

    @Override
    public void loadData() {
        model.setRowCount(0);

        String keyword = normalize(searchTextField != null ? searchTextField.getText() : "");
        List<ThiSinhChungChi> list = service.findAll();

        if (!keyword.isEmpty()) {
            list = list.stream()
                    .filter(cc -> matchesKeyword(cc, keyword))
                    .collect(Collectors.toList());
        }

        list.sort(Comparator.comparing(
                ThiSinhChungChi::getChungchiId,
                Comparator.nullsLast(Integer::compareTo)
        ));

        for (ThiSinhChungChi cc : list) {
            ThiSinh ts = cc.getThiSinh();

            model.addRow(new Object[]{
                    cc.getChungchiId(),
                    ts != null ? safe(ts.getCccd()) : "",
                    ts != null ? safe(ts.getSobaodanh()) : "",
                    ts != null ? safe(ts.getHoVaTen()) : "",
                    safe(cc.getLoaiChungChi()),
                    safe(cc.getTenChungChi()),
                    cc.getDiemGoc() != null ? cc.getDiemGoc().toPlainString() : "",
                    safe(cc.getBacChungChi()),
                    Boolean.TRUE.equals(cc.getIsHopLe()) ? "Co" : "Khong",
                    safe(cc.getTrangThaiXacMinh()),
                    safe(cc.getGhiChu())
            });
        }

        updateTotalLabel(list.size(), "chung chi");
    }

    @Override
    protected ThiSinhChungChi getSelectedEntity() {
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
        return "Quan ly chung chi ngoai ngu";
    }

    @Override
    protected String getEntityDisplayName(ThiSinhChungChi entity) {
        ThiSinh ts = entity.getThiSinh();
        String owner = ts != null ? safe(ts.getHoVaTen()) : "Thi sinh";
        return owner + " - " + safe(entity.getLoaiChungChi());
    }

    @Override
    protected void deleteEntity(ThiSinhChungChi entity) throws Exception {
        service.delete(entity);
    }

    @Override
    protected void showAddDialog() {
        JTextField txtThiSinhKey = new JTextField(20);
        JTextField txtLoai = new JTextField(20);
        JTextField txtTen = new JTextField(20);
        JTextField txtDiemGoc = new JTextField(20);
        JTextField txtBac = new JTextField(20);
        JTextField txtSoHieu = new JTextField(20);
        JTextField txtDonViCap = new JTextField(20);

        JCheckBox chkHopLe = new JCheckBox("Chung chi hop le", true);
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
                "Loai chung chi (*):", txtLoai,
                "Ten chung chi:", txtTen,
                "Diem goc:", txtDiemGoc,
                "Bac chung chi:", txtBac,
                "So hieu:", txtSoHieu,
                "Don vi cap:", txtDonViCap,
                "Trang thai xac minh:", cboTrangThai,
                chkHopLe,
                "Ghi chu:", new JScrollPane(txtGhiChu)
        };

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Them chung chi ngoai ngu",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) return;

        ThiSinh thiSinh = resolveThiSinh(txtThiSinhKey.getText());
        if (thiSinh == null) {
            showError(this, "Khong tim thay thi sinh theo CCCD / SBD.");
            return;
        }

        String loai = normalize(txtLoai.getText());
        if (loai.isEmpty()) {
            showMessage(this, "Loai chung chi la bat buoc.");
            return;
        }

        ThiSinhChungChi entity = new ThiSinhChungChi();
        entity.setThiSinh(thiSinh);
        entity.setLoaiChungChi(loai);
        entity.setTenChungChi(emptyToNull(txtTen.getText()));
        entity.setDiemGoc(parseBigDecimalFlexible(txtDiemGoc.getText()));
        entity.setBacChungChi(emptyToNull(txtBac.getText()));
        entity.setSoHieu(emptyToNull(txtSoHieu.getText()));
        entity.setDonViCap(emptyToNull(txtDonViCap.getText()));
        entity.setIsHopLe(chkHopLe.isSelected());
        entity.setTrangThaiXacMinh((String) cboTrangThai.getSelectedItem());
        entity.setGhiChu(emptyToNull(txtGhiChu.getText()));

        try {
            service.save(entity);
            showSuccess(this, "Them chung chi thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    @Override
    protected void showEditDialog() {
        ThiSinhChungChi entity = getSelectedEntity();
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

        JTextField txtLoai = new JTextField(safe(entity.getLoaiChungChi()), 20);
        JTextField txtTen = new JTextField(safe(entity.getTenChungChi()), 20);
        JTextField txtDiemGoc = new JTextField(
                entity.getDiemGoc() != null ? entity.getDiemGoc().toPlainString() : "",
                20
        );
        JTextField txtBac = new JTextField(safe(entity.getBacChungChi()), 20);
        JTextField txtSoHieu = new JTextField(safe(entity.getSoHieu()), 20);
        JTextField txtDonViCap = new JTextField(safe(entity.getDonViCap()), 20);

        JCheckBox chkHopLe = new JCheckBox("Chung chi hop le", Boolean.TRUE.equals(entity.getIsHopLe()));
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
                "Loai chung chi (*):", txtLoai,
                "Ten chung chi:", txtTen,
                "Diem goc:", txtDiemGoc,
                "Bac chung chi:", txtBac,
                "So hieu:", txtSoHieu,
                "Don vi cap:", txtDonViCap,
                "Trang thai xac minh:", cboTrangThai,
                chkHopLe,
                "Ghi chu:", new JScrollPane(txtGhiChu)
        };

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Sua chung chi ngoai ngu",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) return;

        String loai = normalize(txtLoai.getText());
        if (loai.isEmpty()) {
            showMessage(this, "Loai chung chi la bat buoc.");
            return;
        }

        entity.setLoaiChungChi(loai);
        entity.setTenChungChi(emptyToNull(txtTen.getText()));
        entity.setDiemGoc(parseBigDecimalFlexible(txtDiemGoc.getText()));
        entity.setBacChungChi(emptyToNull(txtBac.getText()));
        entity.setSoHieu(emptyToNull(txtSoHieu.getText()));
        entity.setDonViCap(emptyToNull(txtDonViCap.getText()));
        entity.setIsHopLe(chkHopLe.isSelected());
        entity.setTrangThaiXacMinh((String) cboTrangThai.getSelectedItem());
        entity.setGhiChu(emptyToNull(txtGhiChu.getText()));

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
                "Import chung chi ngoai ngu",
                true
        );
        dialog.setContentPane(new ThiSinhChungChiImportPanel(mainFrame));
        dialog.setSize(920, 680);
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

    private boolean matchesKeyword(ThiSinhChungChi cc, String keyword) {
        ThiSinh ts = cc.getThiSinh();

        return contains(ts != null ? ts.getCccd() : null, keyword)
                || contains(ts != null ? ts.getSobaodanh() : null, keyword)
                || contains(ts != null ? ts.getHoVaTen() : null, keyword)
                || contains(cc.getLoaiChungChi(), keyword)
                || contains(cc.getTenChungChi(), keyword)
                || contains(cc.getBacChungChi(), keyword)
                || contains(cc.getTrangThaiXacMinh(), keyword)
                || contains(cc.getGhiChu(), keyword);
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

    private String emptyToNull(String s) {
        String v = s == null ? null : s.trim();
        return (v == null || v.isEmpty()) ? null : v;
    }

    private BigDecimal parseBigDecimalFlexible(String value) {
        String raw = value == null ? "" : value.trim();
        if (raw.isEmpty()) return null;

        raw = raw.replace(",", ".");
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}