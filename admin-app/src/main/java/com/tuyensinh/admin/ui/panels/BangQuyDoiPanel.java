package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.*;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import com.tuyensinh.dao.*;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Refactored: extends BaseCrudPanel, uses TableFactory + parse helpers.
 */
public class BangQuyDoiPanel extends BaseCrudPanel<BangQuyDoi> {

    private BangQuyDoiService service;
    private PhuongThucDao phuongThucDao;
    private ToHopDao toHopDao;
    private MonDao monDao;

    public BangQuyDoiPanel(MainFrame mainFrame) {
        super(mainFrame);
        service = new BangQuyDoiService();
        phuongThucDao = new PhuongThucDao();
        toHopDao = new ToHopDao();
        monDao = new MonDao();
        initUI();
        loadData();
    }

    @Override
    protected String[] getTableColumns() {
        return new String[]{"ID", "Ma QD", "Ph. thuc", "To hop", "Mon", "Diem A", "Diem B", "Diem QD A", "Diem QD B", "Phan vi"};
    }

    @Override
    protected BangQuyDoi getSelectedEntity() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return service.findById((Integer) model.getValueAt(row, 0));
    }

    @Override
    protected Integer getSelectedId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (Integer) model.getValueAt(row, 0);
    }

    @Override
    public String getPageTitle() {
        return UIConstants.PAGE_BANG_QUY_DOI;
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
        String kw = searchTextField.getText().trim();
        List<BangQuyDoi> list = kw.isEmpty() ? service.findAll() : service.search(kw);

        for (BangQuyDoi bqd : list) {
            model.addRow(new Object[]{
                bqd.getBangquydoiId(),
                bqd.getMaQuydoi(),
                bqd.getPhuongThuc() != null ? bqd.getPhuongThuc().getMaPhuongthuc() : "",
                bqd.getToHop() != null ? bqd.getToHop().getMaTohop() : "",
                bqd.getMon() != null ? bqd.getMon().getMaMon() : "",
                bqd.getDiemTu(),
                bqd.getDiemDen(),
                bqd.getDiemQuydoiTu(),
                bqd.getDiemQuydoiDen(),
                bqd.getPhanVi()
            });
        }
        updateTotalLabel(list.size(), "ban ghi");
    }

    @Override
    protected String getEntityDisplayName(BangQuyDoi bqd) {
        return bqd.getMaQuydoi();
    }

    @Override
    protected void deleteEntity(BangQuyDoi bqd) throws Exception {
        service.delete(bqd);
    }

    @Override
    protected void showAddDialog() {
        JTextField txtMa = new JTextField(20);
        JComboBox<PhuongThuc> cboPt = new JComboBox<>();
        JComboBox<ToHop> cboTh = new JComboBox<>();
        JComboBox<Mon> cboMon = new JComboBox<>();
        cboTh.addItem(null);
        cboMon.addItem(null);
        for (PhuongThuc pt : phuongThucDao.findAll()) cboPt.addItem(pt);
        for (ToHop th : toHopDao.findAll()) cboTh.addItem(th);
        for (Mon m : monDao.findAll()) cboMon.addItem(m);

        JTextField txtTu = new JTextField("0", 10);
        JTextField txtDen = new JTextField("30", 10);
        JTextField txtQdTu = new JTextField("0", 10);
        JTextField txtQdDen = new JTextField("30", 10);

        int r = JOptionPane.showConfirmDialog(this, new Object[]{
            "Ma quy doi (*):", txtMa,
            "Phuong thuc (*):", cboPt,
            "To hop:", cboTh,
            "Mon:", cboMon,
            "Diem tu:", txtTu, "Den:", txtDen,
            "Diem quy doi tu:", txtQdTu, "Den:", txtQdDen
        }, "Them bang quy doi", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        if (txtMa.getText().trim().isEmpty()) {
            showMessage(this, "Ma quy doi la bat buoc!");
            return;
        }

        BangQuyDoi bqd = new BangQuyDoi();
        bqd.setMaQuydoi(txtMa.getText().trim());
        bqd.setPhuongThuc((PhuongThuc) cboPt.getSelectedItem());
        bqd.setToHop((ToHop) cboTh.getSelectedItem());
        bqd.setMon((Mon) cboMon.getSelectedItem());
        bqd.setDiemTu(parseBigDecimal(txtTu.getText()));
        bqd.setDiemDen(parseBigDecimal(txtDen.getText()));
        bqd.setDiemQuydoiTu(parseBigDecimal(txtQdTu.getText()));
        bqd.setDiemQuydoiDen(parseBigDecimal(txtQdDen.getText()));

        try {
            service.save(bqd);
            showSuccess(this, "Them thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    @Override
    protected void showEditDialog() {
        BangQuyDoi bqd = getSelectedEntity();
        if (bqd == null) { showSelectRow(); return; }

        JTextField txtTu = new JTextField(bqd.getDiemTu() != null ? bqd.getDiemTu().toString() : "0", 10);
        JTextField txtDen = new JTextField(bqd.getDiemDen() != null ? bqd.getDiemDen().toString() : "30", 10);
        JTextField txtQdTu = new JTextField(bqd.getDiemQuydoiTu() != null ? bqd.getDiemQuydoiTu().toString() : "0", 10);
        JTextField txtQdDen = new JTextField(bqd.getDiemQuydoiDen() != null ? bqd.getDiemQuydoiDen().toString() : "30", 10);

        int r = JOptionPane.showConfirmDialog(this, new Object[]{
            "Ma: " + bqd.getMaQuydoi(),
            "Diem tu:", txtTu, "Den:", txtDen,
            "Diem quy doi tu:", txtQdTu, "Den:", txtQdDen
        }, "Sua bang quy doi", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        bqd.setDiemTu(parseBigDecimal(txtTu.getText()));
        bqd.setDiemDen(parseBigDecimal(txtDen.getText()));
        bqd.setDiemQuydoiTu(parseBigDecimal(txtQdTu.getText()));
        bqd.setDiemQuydoiDen(parseBigDecimal(txtQdDen.getText()));

        try {
            service.update(bqd);
            showSuccess(this, "Cap nhat thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }
}
