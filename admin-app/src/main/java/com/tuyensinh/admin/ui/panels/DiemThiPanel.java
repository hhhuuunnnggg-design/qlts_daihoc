package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.*;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import com.tuyensinh.dao.PhuongThucDao;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Optional;

/**
 * Refactored: extends BaseCrudPanel with custom phuong thuc filter toolbar.
 */
public class DiemThiPanel extends BaseCrudPanel<DiemThi> {

    private ThiSinhService thiSinhService;
    private DiemThiService diemThiService;
    private PhuongThucDao phuongThucDao;

    private JComboBox<PhuongThuc> phuongThucFilter;

    public DiemThiPanel(MainFrame mainFrame) {
        super(mainFrame);
        thiSinhService = new ThiSinhService();
        diemThiService = new DiemThiService();
        phuongThucDao = new PhuongThucDao();
        initUI();
        loadData();
    }

    @Override
    protected String[] getTableColumns() {
        return new String[]{"ID", "So BD", "CCCD", "Ho Ten", "Phuong Thuc", "Nam TS", "Ghi chu"};
    }

    @Override
    protected DiemThi getSelectedEntity() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return diemThiService.findById((Integer) model.getValueAt(row, 0));
    }

    @Override
    protected Integer getSelectedId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (Integer) model.getValueAt(row, 0);
    }

    @Override
    public String getPageTitle() {
        return UIConstants.PAGE_DIEM_THI;
    }

    @Override
    protected void buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        toolbar.add(new JLabel("Phuong thuc:"));
        phuongThucFilter = new JComboBox<>();
        phuongThucFilter.addItem(null);
        for (PhuongThuc pt : phuongThucDao.findAll()) {
            phuongThucFilter.addItem(pt);
        }
        phuongThucFilter.addActionListener(e -> loadData());
        toolbar.add(phuongThucFilter);

        toolbar.add(new JLabel("  Tim kiem:"));
        searchTextField = new JTextField(15);
        searchTextField.addActionListener(e -> loadData());
        toolbar.add(searchTextField);

        JButton btnSearch = new JButton("Tim");
        btnSearch.addActionListener(e -> loadData());
        toolbar.add(btnSearch);

        toolbar.add(Box.createHorizontalStrut(16));

        JButton btnAdd = new JButton("Them diem");
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
        totalLabel = new JLabel("Tong: 0 ban ghi");
        totalLabel.setFont(UIConstants.FONT_SMALL);
        add(totalLabel, BorderLayout.SOUTH);
    }

    @Override
    public void loadData() {
        model.setRowCount(0);
        PhuongThuc pt = (PhuongThuc) phuongThucFilter.getSelectedItem();
        String kw = searchTextField != null ? searchTextField.getText().trim() : "";

        List<DiemThi> list;
        if (!kw.isEmpty()) {
            Short ptId = pt != null ? pt.getPhuongthucId() : null;
            list = diemThiService.searchDiemThi(kw, ptId);
        } else if (pt != null) {
            list = diemThiService.findByPhuongThuc(pt.getPhuongthucId());
        } else {
            list = diemThiService.findAll();
        }

        for (DiemThi dt : list) {
            ThiSinh ts = dt.getThiSinh();
            model.addRow(new Object[]{
                dt.getDiemthiId(),
                dt.getSobaodanh(),
                ts != null ? ts.getCccd() : "",
                ts != null ? ts.getHoVaTen() : "",
                dt.getPhuongThuc() != null ? dt.getPhuongThuc().getTenPhuongthuc() : "",
                dt.getNamTuyensinh(),
                dt.getGhiChu()
            });
        }
        updateTotalLabel(list.size(), "ban ghi");
    }

    @Override
    protected String getEntityDisplayName(DiemThi dt) {
        return dt.getThiSinh() != null ? dt.getThiSinh().getHoVaTen() : String.valueOf(dt.getDiemthiId());
    }

    @Override
    protected void deleteEntity(DiemThi dt) throws Exception {
        diemThiService.delete(dt);
    }

    @Override
    protected void showAddDialog() {
        JTextField txtSbd = new JTextField(20);
        JTextField txtCccd = new JTextField(20);
        JComboBox<PhuongThuc> cboPt = new JComboBox<>();
        for (PhuongThuc pt : phuongThucDao.findAll()) cboPt.addItem(pt);

        JSpinner spnNam = new JSpinner(new SpinnerNumberModel(2026, 2020, 2030, 1));
        JTextField txtGhiChu = new JTextField(20);

        int r = JOptionPane.showConfirmDialog(this,
            new Object[]{
                "So bao danh:", txtSbd,
                "CCCD (tim thi sinh):", txtCccd,
                "Phuong thuc (*):", cboPt,
                "Nam tuyen sinh:", spnNam,
                "Ghi chu:", txtGhiChu
            },
            "Them diem thi", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        String cccd = txtCccd.getText().trim();
        if (cccd.isEmpty()) { showMessage(this, "CCCD la bat buoc!"); return; }

        PhuongThuc pt = (PhuongThuc) cboPt.getSelectedItem();
        if (pt == null) { showMessage(this, "Chon phuong thuc!"); return; }

        Optional<ThiSinh> optTs = thiSinhService.findByCccd(cccd);
        if (optTs.isEmpty()) { showMessage(this, "Khong tim thay thi sinh voi CCCD: " + cccd); return; }

        DiemThi dt = new DiemThi();
        dt.setThiSinh(optTs.get());
        dt.setPhuongThuc(pt);
        dt.setSobaodanh(txtSbd.getText().trim().isEmpty() ? null : txtSbd.getText().trim());
        dt.setNamTuyensinh((Short) spnNam.getValue());
        dt.setGhiChu(txtGhiChu.getText().trim().isEmpty() ? null : txtGhiChu.getText().trim());

        try {
            diemThiService.save(dt);
            showSuccess(this, "Them thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    @Override
    protected void showEditDialog() {
        DiemThi dt = getSelectedEntity();
        if (dt == null) { showSelectRow(); return; }

        JTextField txtSbd = new JTextField(dt.getSobaodanh() != null ? dt.getSobaodanh() : "");
        JTextField txtGhiChu = new JTextField(dt.getGhiChu() != null ? dt.getGhiChu() : "");
        JSpinner spnNam = new JSpinner(new SpinnerNumberModel(
            dt.getNamTuyensinh() != null ? dt.getNamTuyensinh().intValue() : 2026, 2020, 2030, 1));

        int r = JOptionPane.showConfirmDialog(this,
            new Object[]{
                "Thi sinh: " + (dt.getThiSinh() != null ? dt.getThiSinh().getHoVaTen() : "N/A"),
                "So bao danh:", txtSbd,
                "Nam tuyen sinh:", spnNam,
                "Ghi chu:", txtGhiChu
            },
            "Sua diem thi", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        dt.setSobaodanh(txtSbd.getText().trim().isEmpty() ? null : txtSbd.getText().trim());
        dt.setNamTuyensinh((Short) spnNam.getValue());
        dt.setGhiChu(txtGhiChu.getText().trim().isEmpty() ? null : txtGhiChu.getText().trim());

        try {
            diemThiService.update(dt);
            showSuccess(this, "Cap nhat thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }
}
