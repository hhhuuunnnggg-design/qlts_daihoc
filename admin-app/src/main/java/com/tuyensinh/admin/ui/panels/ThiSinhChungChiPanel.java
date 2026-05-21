package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.BaseCrudPanel;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.admin.ui.ToolbarFactory;
import com.tuyensinh.admin.ui.UIConstants;
import com.tuyensinh.admin.ui.SearchFieldOption;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.entity.ThiSinhChungChi;
import com.tuyensinh.service.ThiSinhChungChiService;
import com.tuyensinh.service.ThiSinhService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Optional;

public class ThiSinhChungChiPanel extends BaseCrudPanel<ThiSinhChungChi> {

    private final ThiSinhChungChiService service;
    private final ThiSinhService thiSinhService;
    private JComboBox<SearchFieldOption> searchFieldCombo;

    public ThiSinhChungChiPanel(MainFrame mainFrame) {
        super(mainFrame);
        this.service = new ThiSinhChungChiService();
        this.thiSinhService = new ThiSinhService();
        enablePagination();
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
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.add(new JLabel("Tim theo:"));
        searchFieldCombo = new JComboBox<>();
        searchFieldCombo.addItem(new SearchFieldOption("ALL", "Tat ca"));
        searchFieldCombo.addItem(new SearchFieldOption("ID", "ID chung chi"));
        searchFieldCombo.addItem(new SearchFieldOption("CCCD", "CCCD"));
        searchFieldCombo.addItem(new SearchFieldOption("SBD", "So bao danh"));
        searchFieldCombo.addItem(new SearchFieldOption("HOTEN", "Ho ten"));
        searchFieldCombo.addItem(new SearchFieldOption("LOAICC", "Loai chung chi"));
        searchFieldCombo.addItem(new SearchFieldOption("TENCC", "Ten chung chi"));
        searchFieldCombo.addItem(new SearchFieldOption("BAC", "Bac chung chi"));
        searchFieldCombo.addItem(new SearchFieldOption("XACMINH", "Trang thai xac minh"));
        searchFieldCombo.addActionListener(e -> doSearch());
        toolbar.add(searchFieldCombo);
        toolbar.add(new JLabel("Tu khoa:"));
        searchTextField = new JTextField(18);
        searchTextField.addActionListener(e -> doSearch());
        toolbar.add(searchTextField);
        JButton btnSearch = new JButton("Tim");
        btnSearch.addActionListener(e -> doSearch());
        toolbar.add(btnSearch);
        toolbar.add(new JButton("Import Excel") {{ addActionListener(e -> showImportDialog()); }});
        toolbar.add(new JButton("Them moi") {{ addActionListener(e -> showAddDialog()); }});
        toolbar.add(new JButton("Sua") {{ addActionListener(e -> showEditDialog()); }});
        toolbar.add(new JButton("Xoa") {{ addActionListener(e -> doDelete()); }});
        toolbar.add(new JButton("Xac minh") {{ addActionListener(e -> xacMinhSelected()); }});
        toolbar.add(new JButton("Tu choi") {{ addActionListener(e -> tuChoiSelected()); }});
        toolbar.add(new JButton("Cho xac minh") {{ addActionListener(e -> choXacMinhSelected()); }});
        add(toolbar, BorderLayout.NORTH);
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
        String field = getSelectedSearchFieldKey();
        long total = keyword.isEmpty() ? service.countAll() : service.countSearch(field, keyword);
        normalizePage(total);

        java.util.List<ThiSinhChungChi> list = keyword.isEmpty()
                ? service.findPage(currentPage, pageSize)
                : service.searchPage(field, keyword, currentPage, pageSize);

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

        updateTotalLabel(total, "chung chi");
        updatePagingState(total);
    }


    private String getSelectedSearchFieldKey() {
        Object selected = searchFieldCombo != null ? searchFieldCombo.getSelectedItem() : null;
        if (selected instanceof SearchFieldOption) {
            return ((SearchFieldOption) selected).getKey();
        }
        return "ALL";
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

    private void xacMinhSelected() {
        updateTrangThaiXacMinh("DA_XAC_MINH", true,
                "Da xac minh chung chi va cho phep ap dung khi tinh diem cong/quy doi.");
    }

    private void tuChoiSelected() {
        updateTrangThaiXacMinh("TU_CHOI", false,
                "Da tu choi chung chi. Chung chi nay se khong duoc ap dung khi tinh diem.");
    }

    private void choXacMinhSelected() {
        updateTrangThaiXacMinh("CHUA_XAC_MINH", true,
                "Da dua chung chi ve trang thai cho xac minh. Chung chi chua duoc ap dung khi tinh diem.");
    }

    private void updateTrangThaiXacMinh(String trangThai, boolean hopLe, String successMessage) {
        ThiSinhChungChi entity = getSelectedEntity();
        if (entity == null) {
            showSelectRow();
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Cap nhat chung chi ID " + entity.getChungchiId() + " sang trang thai " + trangThai + "?",
                "Xac nhan cap nhat xac minh",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.OK_OPTION) return;

        try {
            service.updateXacMinh(entity.getChungchiId(), trangThai, hopLe);
            showSuccess(this, successMessage);
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
