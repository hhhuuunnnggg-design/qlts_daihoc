package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.*;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import javax.swing.*;
import java.awt.*;

/**
 * Refactored: extends BaseCrudPanel, uses TableFactory + parse helpers.
 */
public class NganhPanel extends BaseCrudPanel<Nganh> {

    private XetTuyenService service;

    public NganhPanel(MainFrame mainFrame) {
        super(mainFrame);
        service = new XetTuyenService();
        initUI();
        loadData();
    }

    @Override
    protected String[] getTableColumns() {
        return new String[]{"ID", "Ma nganh", "Ten nganh", "Chi tieu", "Diem san", "Diem TT", "Active"};
    }

    @Override
    protected Nganh getSelectedEntity() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return service.findNganhById(getSelectedId());
    }

    @Override
    protected Integer getSelectedId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (Integer) model.getValueAt(row, 0);
    }

    @Override
    public String getPageTitle() {
        return UIConstants.PAGE_NGANH;
    }

    @Override
    protected void configureTableColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
    }

    @Override
    protected void buildBottomBar() {
        totalLabel = new JLabel("Tong: 0 nganh");
        totalLabel.setFont(UIConstants.FONT_SMALL);
        add(totalLabel, BorderLayout.SOUTH);
    }

    @Override
    public void loadData() {
        model.setRowCount(0);
        String kw = searchTextField.getText().trim();
        var list = kw.isEmpty() ? service.findAllNganh() : service.searchNganh(kw);
        for (Nganh n : list) {
            model.addRow(new Object[]{
                n.getNganhId(),
                n.getMaNganh(),
                n.getTenNganh(),
                n.getChiTieu(),
                n.getDiemSan(),
                n.getDiemTrungTuyen(),
                n.getIsActive() ? "Active" : "Inactive"
            });
        }
        updateTotalLabel(list.size(), "nganh");
    }

    @Override
    protected String getEntityDisplayName(Nganh n) {
        return n.getTenNganh();
    }

    @Override
    protected void deleteEntity(Nganh n) throws Exception {
        service.deleteNganh(n);
    }

    @Override
    protected void showAddDialog() {
        JTextField txtMa = new JTextField(20);
        JTextField txtTen = new JTextField(20);
        JTextField txtChiTieu = new JTextField("100", 20);
        JTextField txtDiemSan = new JTextField(20);
        JCheckBox chkActive = new JCheckBox("Active", true);

        int r = JOptionPane.showConfirmDialog(this,
            new Object[]{
                "Ma nganh (*):", txtMa,
                "Ten nganh (*):", txtTen,
                "Chi tieu:", txtChiTieu,
                "Diem san:", txtDiemSan,
                "Active:", chkActive
            },
            "Them nganh moi", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()) {
            showMessage(this, "Ma va Ten nganh la bat buoc!");
            return;
        }

        Nganh n = new Nganh();
        n.setMaNganh(txtMa.getText().trim());
        n.setTenNganh(txtTen.getText().trim());
        n.setChiTieu(parseInt(txtChiTieu.getText()));
        n.setDiemSan(parseBigDecimal(txtDiemSan.getText()));
        n.setIsActive(chkActive.isSelected());

        try {
            service.saveNganh(n);
            showSuccess(this, "Them thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    @Override
    protected void showEditDialog() {
        Nganh n = getSelectedEntity();
        if (n == null) { showSelectRow(); return; }

        JTextField txtTen = new JTextField(n.getTenNganh());
        JTextField txtChiTieu = new JTextField(String.valueOf(n.getChiTieu() != null ? n.getChiTieu() : ""), 20);
        JTextField txtDiemSan = new JTextField(n.getDiemSan() != null ? n.getDiemSan().toString() : "", 20);
        JTextField txtDiemTT = new JTextField(n.getDiemTrungTuyen() != null ? n.getDiemTrungTuyen().toString() : "", 20);
        JCheckBox chkActive = new JCheckBox("Active", n.getIsActive());

        int r = JOptionPane.showConfirmDialog(this,
            new Object[]{
                "Ma nganh: " + n.getMaNganh() + " (khong doi)",
                "Ten nganh:", txtTen,
                "Chi tieu:", txtChiTieu,
                "Diem san:", txtDiemSan,
                "Diem trung tuyen:", txtDiemTT,
                "Active:", chkActive
            },
            "Sua nganh", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        n.setTenNganh(txtTen.getText().trim());
        n.setChiTieu(parseInt(txtChiTieu.getText()));
        n.setDiemSan(parseBigDecimal(txtDiemSan.getText()));
        n.setDiemTrungTuyen(parseBigDecimal(txtDiemTT.getText()));
        n.setIsActive(chkActive.isSelected());

        try {
            service.updateNganh(n);
            showSuccess(this, "Cap nhat thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }
}
