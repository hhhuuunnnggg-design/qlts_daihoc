package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.*;
import com.tuyensinh.admin.ui.SearchFieldOption;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import com.tuyensinh.dao.PhuongThucDao;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
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
    private MonService monService;
    private JTable detailTable;
    private DefaultTableModel detailModel;

    private JComboBox<SearchFieldOption> searchFieldCombo;

    public DiemThiPanel(MainFrame mainFrame) {
        super(mainFrame);
        thiSinhService = new ThiSinhService();
        diemThiService = new DiemThiService();
        phuongThucDao = new PhuongThucDao();
        monService = new MonService();
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

        toolbar.add(new JLabel("Tim theo:"));
        searchFieldCombo = new JComboBox<>();
        searchFieldCombo.addItem(new SearchFieldOption("ALL", "Tat ca"));
        searchFieldCombo.addItem(new SearchFieldOption("ID", "ID phieu diem"));
        searchFieldCombo.addItem(new SearchFieldOption("CCCD", "CCCD"));
        searchFieldCombo.addItem(new SearchFieldOption("SBD", "So bao danh"));
        searchFieldCombo.addItem(new SearchFieldOption("HOTEN", "Ho ten"));
        searchFieldCombo.addItem(new SearchFieldOption("MAPT", "Ma phuong thuc"));
        searchFieldCombo.addItem(new SearchFieldOption("TENPT", "Ten phuong thuc"));
        searchFieldCombo.addItem(new SearchFieldOption("NAM", "Nam tuyen sinh"));
        searchFieldCombo.addItem(new SearchFieldOption("GHICHU", "Ghi chu"));
        searchFieldCombo.addActionListener(e -> { currentPage = 1; loadData(); });
        toolbar.add(searchFieldCombo);

        toolbar.add(new JLabel("  Tu khoa:"));
        searchTextField = new JTextField(15);
        searchTextField.addActionListener(e -> doSearch());
        toolbar.add(searchTextField);

        JButton btnSearch = new JButton("Tim");
        btnSearch.addActionListener(e -> doSearch());
        toolbar.add(btnSearch);

        toolbar.add(Box.createHorizontalStrut(16));

        JButton btnImport = new JButton("Import diem THPT + NK");
        btnImport.setToolTipText("Mo man hinh import diem thi tu file Ds thi sinh.xlsx");
        btnImport.addActionListener(e -> mainFrame.showPanel("diem_import"));
        toolbar.add(btnImport);

        JButton btnImportDGNLVSAT = new JButton("Import diem DGNL/VSAT");
        btnImportDGNLVSAT.setToolTipText("Mo man hinh import file Diem DGNL VSAT - 0908.xlsx");
        btnImportDGNLVSAT.addActionListener(e -> mainFrame.showPanel("diem_dgnl_vsat_import"));
        toolbar.add(btnImportDGNLVSAT);

        JButton btnAdd = new JButton("Them diem");
        btnAdd.addActionListener(e -> showAddDialog());
        toolbar.add(btnAdd);

        JButton btnEdit = new JButton("Sua");
        btnEdit.addActionListener(e -> showEditDialog());
        toolbar.add(btnEdit);

        JButton btnDelete = new JButton("Xoa");
        btnDelete.addActionListener(e -> doDelete());
        toolbar.add(btnDelete);

        toolbar.add(Box.createHorizontalStrut(16));

        JButton btnAddDetail = new JButton("Them mon diem");
        btnAddDetail.setToolTipText("Them 1 dong xt_diemthi_chitiet cho phieu diem dang chon");
        btnAddDetail.addActionListener(e -> showAddDetailDialog());
        toolbar.add(btnAddDetail);

        JButton btnEditDetail = new JButton("Sua diem mon");
        btnEditDetail.setToolTipText("Sua diem goc/quy doi/su dung cua mon dang chon o bang chi tiet");
        btnEditDetail.addActionListener(e -> showEditDetailDialog());
        toolbar.add(btnEditDetail);

        JButton btnDeleteDetail = new JButton("Xoa mon diem");
        btnDeleteDetail.setToolTipText("Xoa dong diem chi tiet dang chon");
        btnDeleteDetail.addActionListener(e -> deleteDetail());
        toolbar.add(btnDeleteDetail);

        add(toolbar, BorderLayout.NORTH);
    }

    @Override
    protected void buildBottomBar() {
        totalLabel = new JLabel("Tong: 0 ban ghi");
        totalLabel.setFont(UIConstants.FONT_SMALL);
        pageSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
        JPanel paging = ToolbarFactory.createPagingPanel(pageSpinner, () -> {
            currentPage = (Integer) pageSpinner.getValue();
            loadData();
        });
        add(ToolbarFactory.createBottomBar(totalLabel, paging), BorderLayout.SOUTH);
    }

    @Override
    public void loadData() {
        model.setRowCount(0);

        if (detailModel != null) {
            detailModel.setRowCount(0);
        }

        String kw = getSearchKeyword();
        String searchField = getSelectedSearchFieldKey();

        long total;
        List<DiemThi> list;
        if (!kw.isEmpty()) {
            total = diemThiService.countSearchDiemThi(searchField, kw);
            normalizePage(total);
            list = diemThiService.searchDiemThiPage(searchField, kw, currentPage, pageSize);
        } else {
            total = diemThiService.countAll();
            normalizePage(total);
            list = diemThiService.findPage(currentPage, pageSize);
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
        updateTotalLabel(total, "ban ghi");
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
        for (PhuongThuc pt : phuongThucDao.findAll()) {
            if (PhuongThuc.XTT.equalsIgnoreCase(pt.getMaPhuongthuc())) {
                continue;
            }
            cboPt.addItem(pt);
        }
        configurePhuongThucCombo(cboPt);

        JSpinner spnNam = new JSpinner(new SpinnerNumberModel(2025, 2020, 2030, 1));
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
        dt.setNamTuyensinh(((Number) spnNam.getValue()).shortValue());
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
            dt.getNamTuyensinh() != null ? dt.getNamTuyensinh().intValue() : 2025, 2020, 2030, 1));

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
        dt.setNamTuyensinh(((Number) spnNam.getValue()).shortValue());
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
                new String[]{"ID", "Ma mon", "Ten mon", "Diem goc", "Diem quy doi", "Diem su dung"});
        detailTable = TableFactory.create(detailModel);
        detailTable.getColumnModel().getColumn(0).setMinWidth(0);
        detailTable.getColumnModel().getColumn(0).setMaxWidth(0);
        detailTable.getColumnModel().getColumn(0).setPreferredWidth(0);
        detailTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && detailTable.getSelectedRow() >= 0) {
                    showEditDetailDialog();
                }
            }
        });

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
                    ct.getDiemthiCtId(),
                    ct.getMon() != null ? ct.getMon().getMaMon() : "",
                    ct.getMon() != null ? ct.getMon().getTenMon() : "",
                    ct.getDiemGoc(),
                    ct.getDiemQuydoi(),
                    ct.getDiemSudung()
            });
        }
    }

    private void showAddDetailDialog() {
        DiemThi dt = getSelectedEntity();
        if (dt == null) {
            showMessage(this, "Hay chon 1 phieu diem truoc!");
            return;
        }

        JComboBox<Mon> cboMon = buildMonCombo(null);
        JTextField txtDiemGoc = new JTextField(10);
        JTextField txtDiemQuydoi = new JTextField(10);
        JTextField txtDiemSudung = new JTextField(10);

        int r = JOptionPane.showConfirmDialog(
                this,
                new Object[]{
                        "Mon (*):", cboMon,
                        "Diem goc:", txtDiemGoc,
                        "Diem quy doi:", txtDiemQuydoi,
                        "Diem su dung:", txtDiemSudung
                },
                "Them diem chi tiet",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (r != JOptionPane.OK_OPTION) return;

        Mon mon = (Mon) cboMon.getSelectedItem();
        if (mon == null) {
            showMessage(this, "Chua chon mon!");
            return;
        }

        if (diemThiService.findChiTietByDiemThiAndMon(dt.getDiemthiId(), mon.getMonId()) != null) {
            showMessage(this, "Phieu diem nay da co diem mon " + mon.getMaMon() + ". Hay dung nut Sua diem mon.");
            return;
        }

        try {
            DiemThiChiTiet ct = new DiemThiChiTiet();
            ct.setDiemThi(dt);
            ct.setMon(mon);
            applyScoreFields(ct, txtDiemGoc, txtDiemQuydoi, txtDiemSudung);

            if (ct.getDiemGoc() == null && ct.getDiemQuydoi() == null && ct.getDiemSudung() == null) {
                showMessage(this, "Can nhap it nhat 1 cot diem.");
                return;
            }

            diemThiService.saveChiTiet(ct);
            showSuccess(this, "Them diem chi tiet thanh cong!");
            loadDetailTable();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private void showEditDetailDialog() {
        DiemThiChiTiet ct = getSelectedDetail();
        if (ct == null) {
            showMessage(this, "Hay chon 1 dong diem chi tiet!");
            return;
        }

        JComboBox<Mon> cboMon = buildMonCombo(ct.getMon());
        JTextField txtDiemGoc = new JTextField(formatScoreForEdit(ct.getDiemGoc()), 10);
        JTextField txtDiemQuydoi = new JTextField(formatScoreForEdit(ct.getDiemQuydoi()), 10);
        JTextField txtDiemSudung = new JTextField(formatScoreForEdit(ct.getDiemSudung()), 10);

        int r = JOptionPane.showConfirmDialog(
                this,
                new Object[]{
                        "Mon (*):", cboMon,
                        "Diem goc:", txtDiemGoc,
                        "Diem quy doi:", txtDiemQuydoi,
                        "Diem su dung:", txtDiemSudung
                },
                "Sua diem chi tiet",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (r != JOptionPane.OK_OPTION) return;

        Mon selectedMon = (Mon) cboMon.getSelectedItem();
        if (selectedMon == null) {
            showMessage(this, "Chua chon mon!");
            return;
        }

        try {
            boolean changedMon = ct.getMon() == null
                    || !selectedMon.getMonId().equals(ct.getMon().getMonId());
            if (changedMon) {
                DiemThi parent = ct.getDiemThi();
                Integer parentId = parent != null ? parent.getDiemthiId() : getSelectedId();
                DiemThiChiTiet duplicate = diemThiService.findChiTietByDiemThiAndMon(parentId, selectedMon.getMonId());
                if (duplicate != null && !duplicate.getDiemthiCtId().equals(ct.getDiemthiCtId())) {
                    showMessage(this, "Phieu diem nay da co diem mon " + selectedMon.getMaMon() + ".");
                    return;
                }
                ct.setMon(selectedMon);
            }

            applyScoreFields(ct, txtDiemGoc, txtDiemQuydoi, txtDiemSudung);
            if (ct.getDiemGoc() == null && ct.getDiemQuydoi() == null && ct.getDiemSudung() == null) {
                showMessage(this, "Can nhap it nhat 1 cot diem.");
                return;
            }

            diemThiService.updateChiTiet(ct);
            showSuccess(this, "Cap nhat diem chi tiet thanh cong!");
            loadDetailTable();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private void deleteDetail() {
        DiemThiChiTiet ct = getSelectedDetail();
        if (ct == null) {
            showMessage(this, "Hay chon 1 dong diem chi tiet!");
            return;
        }

        String monName = ct.getMon() != null
                ? ct.getMon().getMaMon() + " - " + ct.getMon().getTenMon()
                : String.valueOf(ct.getDiemthiCtId());

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Ban co chac muon xoa diem mon nay?\n" + monName,
                "Xac nhan",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            diemThiService.deleteChiTiet(ct);
            showSuccess(this, "Xoa diem chi tiet thanh cong!");
            loadDetailTable();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private DiemThiChiTiet getSelectedDetail() {
        if (detailTable == null || detailModel == null) return null;
        int row = detailTable.getSelectedRow();
        if (row < 0) return null;

        Object idValue = detailModel.getValueAt(row, 0);
        Long id = null;
        if (idValue instanceof Long) {
            id = (Long) idValue;
        } else if (idValue instanceof Number) {
            id = ((Number) idValue).longValue();
        } else if (idValue != null) {
            try {
                id = Long.parseLong(idValue.toString());
            } catch (NumberFormatException ignored) {
            }
        }
        return id == null ? null : diemThiService.findChiTietById(id);
    }

    private JComboBox<Mon> buildMonCombo(Mon selected) {
        JComboBox<Mon> combo = new JComboBox<>();
        for (Mon mon : monService.findAll()) {
            combo.addItem(mon);
        }
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Mon) {
                    Mon mon = (Mon) value;
                    setText(mon.getMaMon() + " - " + mon.getTenMon());
                }
                return this;
            }
        });

        if (selected != null && selected.getMonId() != null) {
            for (int i = 0; i < combo.getItemCount(); i++) {
                Mon item = combo.getItemAt(i);
                if (item != null && selected.getMonId().equals(item.getMonId())) {
                    combo.setSelectedIndex(i);
                    break;
                }
            }
        }
        return combo;
    }

    private void applyScoreFields(DiemThiChiTiet ct,
                                  JTextField txtDiemGoc,
                                  JTextField txtDiemQuydoi,
                                  JTextField txtDiemSudung) {
        BigDecimal diemGoc = parseScore(txtDiemGoc.getText());
        BigDecimal diemQuydoi = parseScore(txtDiemQuydoi.getText());
        BigDecimal diemSudung = parseScore(txtDiemSudung.getText());

        if (diemQuydoi == null) {
            diemQuydoi = diemGoc;
        }
        if (diemSudung == null) {
            diemSudung = diemQuydoi != null ? diemQuydoi : diemGoc;
        }

        ct.setDiemGoc(diemGoc);
        ct.setDiemQuydoi(diemQuydoi);
        ct.setDiemSudung(diemSudung);
    }

    private BigDecimal parseScore(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        String normalized = text.trim().replace(',', '.');
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Diem khong hop le: " + text);
        }
    }

    private String formatScoreForEdit(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

}
