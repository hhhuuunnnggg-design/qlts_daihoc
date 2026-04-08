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
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Refactored: extends BaseCrudPanel with custom phuong thuc filter toolbar.
 */
public class DiemThiPanel extends BaseCrudPanel<DiemThi> {

    private ThiSinhService thiSinhService;
    private DiemThiService diemThiService;
    private PhuongThucDao phuongThucDao;
    private JTable detailTable;
    private DefaultTableModel detailModel;

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
        configurePhuongThucCombo(phuongThucFilter);
        toolbar.add(phuongThucFilter);

        toolbar.add(new JLabel("  Tim kiem:"));
        searchTextField = new JTextField(15);
        searchTextField.addActionListener(e -> loadData());
        toolbar.add(searchTextField);

        JButton btnSearch = new JButton("Tim");
        btnSearch.addActionListener(e -> loadData());
        toolbar.add(btnSearch);

        toolbar.add(Box.createHorizontalStrut(16));

        JButton btnImport = new JButton("Import tu Ds thi sinh.xlsx");
        btnImport.setToolTipText("Mo man hinh import diem thi tu file Ds thi sinh.xlsx");
        btnImport.addActionListener(e -> mainFrame.showPanel("diem_import"));
        toolbar.add(btnImport);

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

        if (detailModel != null) {
            detailModel.setRowCount(0);
        }

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
        configurePhuongThucCombo(cboPt);

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

    /** Hien thi ten phuong thuc thay vi toString() day du trong JComboBox. */
    private static void configurePhuongThucCombo(JComboBox<PhuongThuc> combo) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("(Tat ca)");
                } else if (value instanceof PhuongThuc) {
                    PhuongThuc pt = (PhuongThuc) value;
                    String t = pt.getTenPhuongthuc();
                    setText(t != null && !t.isEmpty() ? t : pt.getMaPhuongthuc());
                }
                return this;
            }
        });
    }

    @Override
    protected void buildTable() {
        model = TableFactory.newReadOnlyModel(getTableColumns());
        table = TableFactory.create(model);
        configureTableColumns();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRowSelected();
            }
        });

        detailModel = TableFactory.newReadOnlyModel(
                new String[]{"Ma mon", "Ten mon", "Diem goc", "Diem quy doi", "Diem su dung"});
        detailTable = TableFactory.create(detailModel);
        detailTable.setEnabled(false);

        JScrollPane topScroll = TableFactory.wrap(table);
        topScroll.setBorder(BorderFactory.createTitledBorder("Danh sach diem thi"));

        JScrollPane bottomScroll = TableFactory.wrap(detailTable);
        bottomScroll.setBorder(BorderFactory.createTitledBorder("Chi tiet xt_diemthi_chitiet"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topScroll, bottomScroll);
        splitPane.setResizeWeight(0.62);

        add(splitPane, BorderLayout.CENTER);
    }

    @Override
    protected void onRowSelected() {
        loadDetailTable();
    }

    private void loadDetailTable() {
        if (detailModel == null) return;
        detailModel.setRowCount(0);

        Integer id = getSelectedId();
        if (id == null) return;

        DiemThi dt = diemThiService.findByIdWithDetails(id);
        if (dt == null || dt.getDanhSachDiemChiTiet() == null) return;

        List<DiemThiChiTiet> details = new ArrayList<>(dt.getDanhSachDiemChiTiet());
        details.sort(Comparator.comparing(d -> d.getMon() != null && d.getMon().getMaMon() != null
                ? d.getMon().getMaMon() : ""));

        for (DiemThiChiTiet ct : details) {
            detailModel.addRow(new Object[]{
                    ct.getMon() != null ? ct.getMon().getMaMon() : "",
                    ct.getMon() != null ? ct.getMon().getTenMon() : "",
                    ct.getDiemGoc(),
                    ct.getDiemQuydoi(),
                    ct.getDiemSudung()
            });
        }
    }
}
