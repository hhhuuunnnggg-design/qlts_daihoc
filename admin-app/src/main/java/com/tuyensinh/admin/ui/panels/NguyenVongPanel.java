package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.BasePanel;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.admin.ui.TableFactory;
import com.tuyensinh.admin.ui.ToolbarFactory;
import com.tuyensinh.admin.ui.UIConstants;
import com.tuyensinh.entity.MaXetTuyenMap;
import com.tuyensinh.entity.NganhToHop;
import com.tuyensinh.entity.NguyenVong;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.service.MaXetTuyenMapService;
import com.tuyensinh.service.NguyenVongService;
import com.tuyensinh.service.ThiSinhService;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class NguyenVongPanel extends BasePanel {

    private final NguyenVongService service;
    private final ThiSinhService thiSinhService;
    private final MaXetTuyenMapService maXetTuyenMapService;

    private JTable table;
    private DefaultTableModel model;

    private JTable detailTable;
    private DefaultTableModel detailModel;

    private JTextField txtSearch;
    private JLabel lblTotal;
    private JLabel lblDetailTitle;
    private JSpinner pageSpinner;

    private int currentPage = 1;
    private final int pageSize = 20;

    private List<NguyenVong> currentList = new ArrayList<>();

    public NguyenVongPanel(MainFrame mainFrame) {
        super(mainFrame);
        this.service = new NguyenVongService();
        this.thiSinhService = new ThiSinhService();
        this.maXetTuyenMapService = new MaXetTuyenMapService();
        initUI();
        loadData();
    }

    @Override
    public String getPageTitle() {
        return UIConstants.PAGE_NGUYEN_VONG;
    }

    @Override
    protected void initUI() {
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        lblTotal = new JLabel("Tong: 0");
        lblTotal.setFont(UIConstants.FONT_SMALL);
        pageSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
        JPanel paging = ToolbarFactory.createPagingPanel(pageSpinner, () -> {
            currentPage = (Integer) pageSpinner.getValue();
            loadData();
        });
        add(ToolbarFactory.createBottomBar(lblTotal, paging), BorderLayout.SOUTH);
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        toolbar.add(new JLabel("Tim (CCCD / SBD / Ho ten / Ma XT / Nganh):"));

        txtSearch = new JTextField(24);
        txtSearch.addActionListener(e -> doSearch());
        toolbar.add(txtSearch);

        JButton btnSearch = new JButton("Tim");
        btnSearch.addActionListener(e -> doSearch());
        toolbar.add(btnSearch);

        JButton btnClear = new JButton("Lam moi");
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            currentPage = 1;
            loadData();
        });
        toolbar.add(btnClear);

        toolbar.add(Box.createHorizontalStrut(12));

        JButton btnImport = new JButton("Import Excel");
        btnImport.addActionListener(e -> showImportDialog());
        toolbar.add(btnImport);

        JButton btnAdd = new JButton("Them");
        btnAdd.addActionListener(e -> addNv());
        toolbar.add(btnAdd);

        JButton btnEdit = new JButton("Sua");
        btnEdit.addActionListener(e -> editNv());
        toolbar.add(btnEdit);

        JButton btnDelete = new JButton("Xoa");
        btnDelete.addActionListener(e -> deleteNv());
        toolbar.add(btnDelete);

        return toolbar;
    }

    private JComponent buildCenter() {
        model = TableFactory.newReadOnlyModel(
                "ID", "CCCD", "SBD", "Ho ten", "NV", "Ma XT", "CT", "Nganh", "To hop", "Ph. thuc", "Diem XT", "Ket qua"
        );
        table = TableFactory.create(model);
        configureMainTable(table);

        table.getSelectionModel().addListSelectionListener(this::onMainSelectionChanged);

        detailModel = TableFactory.newReadOnlyModel(
                "NV", "Ma XT", "CT", "Ten CT", "Nganh", "To hop", "Ph. thuc", "Diem XT", "Ket qua", "Ghi chu"
        );
        detailTable = TableFactory.create(detailModel);
        configureDetailTable(detailTable);

        JPanel detailPanel = new JPanel(new BorderLayout(6, 6));
        detailPanel.setBorder(BorderFactory.createTitledBorder("Tra cuu theo thi sinh"));

        lblDetailTitle = new JLabel("Chua chon thi sinh.");
        lblDetailTitle.setFont(UIConstants.FONT_BODY);
        detailPanel.add(lblDetailTitle, BorderLayout.NORTH);
        detailPanel.add(TableFactory.wrap(detailTable), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                TableFactory.wrap(table),
                detailPanel
        );
        splitPane.setResizeWeight(0.62);

        return splitPane;
    }

    @Override
    public void loadData() {
        model.setRowCount(0);
        detailModel.setRowCount(0);
        lblDetailTitle.setText("Chua chon thi sinh.");

        String keyword = normalizeForSearch(txtSearch != null ? txtSearch.getText() : "");
        long total = keyword.isEmpty() ? service.countAll() : service.countSearch(keyword);
        normalizeCurrentPage(total);
        currentList = keyword.isEmpty()
                ? service.findPage(currentPage, pageSize)
                : service.searchPage(keyword, currentPage, pageSize);

        for (NguyenVong nv : currentList) {
            ThiSinh ts = nv.getThiSinh();

            model.addRow(new Object[]{
                    nv.getNguyenvongId(),
                    ts != null ? safe(ts.getCccd()) : "",
                    ts != null ? safe(ts.getSobaodanh()) : "",
                    ts != null ? safe(ts.getHoVaTen()) : "",
                    nv.getThuTu(),
                    getMaXetTuyen(nv),
                    getChuongTrinhTag(nv),
                    nv.getNganh() != null ? safe(nv.getNganh().getTenNganh()) : "",
                    getToHopDisplay(nv),
                    nv.getPhuongThuc() != null ? safe(nv.getPhuongThuc().getMaPhuongthuc()) : "",
                    formatScore(nv.getDiemXettuyen()),
                    safe(nv.getKetQua())
            });
        }

        Set<Integer> uniqueThiSinhIds = new LinkedHashSet<>();
        for (NguyenVong nv : currentList) {
            if (nv.getThiSinh() != null && nv.getThiSinh().getThisinhId() != null) {
                uniqueThiSinhIds.add(nv.getThiSinh().getThisinhId());
            }
        }

        lblTotal.setText("Tong NV: " + total
                + " | Trang: " + currentPage
                + " | Tren trang: " + currentList.size()
                + " | Thi sinh tren trang: " + uniqueThiSinhIds.size());
        updatePagingState(total);

        if (!currentList.isEmpty()) {
            table.setRowSelectionInterval(0, 0);
            loadDetailForSelected();
        }
    }

    private void doSearch() {
        currentPage = 1;
        loadData();
    }

    private void normalizeCurrentPage(long total) {
        int totalPages = Math.max(1, (int) Math.ceil((double) total / pageSize));
        if (currentPage < 1) currentPage = 1;
        if (currentPage > totalPages) currentPage = totalPages;
    }

    private void updatePagingState(long total) {
        normalizeCurrentPage(total);
        if (pageSpinner != null) {
            ToolbarFactory.updatePagingSpinner(pageSpinner, currentPage, (int) Math.min(Integer.MAX_VALUE, total), pageSize);
        }
    }

    private void onMainSelectionChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            loadDetailForSelected();
        }
    }

    private void loadDetailForSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            detailModel.setRowCount(0);
            lblDetailTitle.setText("Chua chon thi sinh.");
            return;
        }

        Integer nguyenvongId = (Integer) model.getValueAt(row, 0);
        NguyenVong selected = service.findById(nguyenvongId);
        if (selected == null || selected.getThiSinh() == null) {
            detailModel.setRowCount(0);
            lblDetailTitle.setText("Khong tim thay thong tin thi sinh.");
            return;
        }

        ThiSinh ts = selected.getThiSinh();
        List<NguyenVong> list = service.findByThiSinhId(ts.getThisinhId());

        detailModel.setRowCount(0);
        for (NguyenVong nv : list) {
            detailModel.addRow(new Object[]{
                    nv.getThuTu(),
                    getMaXetTuyen(nv),
                    getChuongTrinhTag(nv),
                    getTenChuongTrinh(nv),
                    nv.getNganh() != null ? safe(nv.getNganh().getTenNganh()) : "",
                    getToHopDisplay(nv),
                    nv.getPhuongThuc() != null ? safe(nv.getPhuongThuc().getMaPhuongthuc()) : "",
                    formatScore(nv.getDiemXettuyen()),
                    safe(nv.getKetQua()),
                    safe(nv.getGhiChu())
            });
        }

        lblDetailTitle.setText(
                "Thi sinh: " + safe(ts.getHoVaTen())
                        + " | CCCD: " + safe(ts.getCccd())
                        + " | SBD: " + safe(ts.getSobaodanh())
                        + " | So NV: " + list.size()
        );
    }

    private NguyenVong getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;

        Integer id = (Integer) model.getValueAt(row, 0);
        return service.findById(id);
    }

    private void showImportDialog() {
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Import nguyen vong",
                true
        );
        dialog.setContentPane(new NguyenVongImportPanel(mainFrame));
        dialog.setSize(920, 680);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        loadData();
    }

    private void deleteNv() {
        NguyenVong nv = getSelected();
        if (nv == null) {
            showSelectRow(this);
            return;
        }

        String name = nv.getThiSinh() != null
                ? nv.getThiSinh().getHoVaTen() + " | NV" + nv.getThuTu()
                : String.valueOf(nv.getNguyenvongId());

        if (confirmDelete(this, name) != JOptionPane.YES_OPTION) return;

        try {
            service.delete(nv);
            showSuccess(this, "Xoa thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }


    private void addNv() {
        JTextField txtThiSinh = new JTextField(22);
        JLabel lblThiSinh = new JLabel("Nhap CCCD hoac SBD, bam Tim TS de lay thu tu goi y.");
        JButton btnFindThiSinh = new JButton("Tim TS");

        JTextField txtMaXt = new JTextField(22);
        JComboBox<MaXetTuyenMap> cboMap = new JComboBox<>();
        JButton btnFindMaXt = new JButton("Tim Ma XT");
        loadMapCombo(cboMap, "", null);

        JSpinner spnThuTu = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        JComboBox<String> cboKetQua = buildKetQuaCombo(NguyenVong.KetQua.CHO_XET);
        JTextField txtDiemThxt = new JTextField(10);
        JTextField txtDiemThgxt = new JTextField(10);
        JTextField txtDiemCong = new JTextField(10);
        JTextField txtDiemUuTien = new JTextField(10);
        JTextField txtDiemXetTuyen = new JTextField(10);
        JTextField txtNguonDiem = new JTextField(10);
        JTextField txtGhiChu = new JTextField(24);

        btnFindThiSinh.addActionListener(e -> {
            ThiSinh ts = findThiSinhByIdentifier(txtThiSinh.getText());
            if (ts == null) {
                lblThiSinh.setText("Khong tim thay thi sinh.");
                return;
            }
            int nextOrder = service.findByThiSinhId(ts.getThisinhId()).size() + 1;
            spnThuTu.setValue(nextOrder);
            lblThiSinh.setText(formatThiSinh(ts) + " | Goi y thu tu NV: " + nextOrder);
        });

        btnFindMaXt.addActionListener(e -> loadMapCombo(cboMap, txtMaXt.getText(), null));

        JPanel thiSinhPanel = new JPanel(new BorderLayout(6, 0));
        thiSinhPanel.add(txtThiSinh, BorderLayout.CENTER);
        thiSinhPanel.add(btnFindThiSinh, BorderLayout.EAST);

        JPanel mapPanel = new JPanel(new BorderLayout(6, 0));
        mapPanel.add(txtMaXt, BorderLayout.CENTER);
        mapPanel.add(btnFindMaXt, BorderLayout.EAST);

        int r = JOptionPane.showConfirmDialog(
                this,
                new Object[]{
                        "CCCD/SBD thi sinh (*):", thiSinhPanel,
                        lblThiSinh,
                        "Tim ma xet tuyen:", mapPanel,
                        "Chon ma xet tuyen - phuong thuc - to hop (*):", cboMap,
                        "Thu tu nguyen vong:", spnThuTu,
                        "Diem THXT:", txtDiemThxt,
                        "Diem THGXT:", txtDiemThgxt,
                        "Diem cong:", txtDiemCong,
                        "Diem uu tien:", txtDiemUuTien,
                        "Diem xet tuyen:", txtDiemXetTuyen,
                        "Nguon diem tot nhat:", txtNguonDiem,
                        "Ket qua:", cboKetQua,
                        "Ghi chu:", txtGhiChu
                },
                "Them nguyen vong",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (r != JOptionPane.OK_OPTION) return;

        ThiSinh ts = findThiSinhByIdentifier(txtThiSinh.getText());
        if (ts == null) {
            showMessage(this, "Khong tim thay thi sinh theo CCCD/SBD: " + txtThiSinh.getText());
            return;
        }

        MaXetTuyenMap map = (MaXetTuyenMap) cboMap.getSelectedItem();
        if (map == null) {
            showMessage(this, "Chua chon ma xet tuyen!");
            return;
        }
        if (!isMapUsableForNguyenVong(map)) return;

        Integer thuTu = (Integer) spnThuTu.getValue();
        String duplicate = validateNguyenVongUnique(null, ts.getThisinhId(), map, thuTu);
        if (duplicate != null) {
            showMessage(this, duplicate);
            return;
        }

        try {
            NguyenVong nv = new NguyenVong();
            nv.setThiSinh(ts);
            applyMapToNguyenVong(nv, map);
            nv.setThuTu(thuTu);
            applyOptionalFields(nv, txtDiemThxt, txtDiemThgxt, txtDiemCong, txtDiemUuTien,
                    txtDiemXetTuyen, txtNguonDiem, cboKetQua, txtGhiChu);

            service.save(nv);
            showSuccess(this, "Them nguyen vong thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private void editNv() {
        NguyenVong nv = getSelected();
        if (nv == null) {
            showSelectRow(this);
            return;
        }

        JTextField txtMaXt = new JTextField(getMaXetTuyen(nv), 22);
        JComboBox<MaXetTuyenMap> cboMap = new JComboBox<>();
        JButton btnFindMaXt = new JButton("Tim Ma XT");
        loadMapCombo(cboMap, getMaXetTuyen(nv), nv.getMaXetTuyenMap());

        JSpinner spnThuTu = new JSpinner(new SpinnerNumberModel(
                nv.getThuTu() != null ? nv.getThuTu() : 1, 1, 99, 1));
        JComboBox<String> cboKetQua = buildKetQuaCombo(nv.getKetQua());
        JTextField txtDiemThxt = new JTextField(formatScore(nv.getDiemThxt()), 10);
        JTextField txtDiemThgxt = new JTextField(formatScore(nv.getDiemThgxt()), 10);
        JTextField txtDiemCong = new JTextField(formatScore(nv.getDiemCong()), 10);
        JTextField txtDiemUuTien = new JTextField(formatScore(nv.getDiemUutien()), 10);
        JTextField txtDiemXetTuyen = new JTextField(formatScore(nv.getDiemXettuyen()), 10);
        JTextField txtNguonDiem = new JTextField(safe(nv.getPhuongThucDiemTotNhat()), 10);
        JTextField txtGhiChu = new JTextField(safe(nv.getGhiChu()), 24);

        btnFindMaXt.addActionListener(e -> loadMapCombo(cboMap, txtMaXt.getText(), nv.getMaXetTuyenMap()));

        JPanel mapPanel = new JPanel(new BorderLayout(6, 0));
        mapPanel.add(txtMaXt, BorderLayout.CENTER);
        mapPanel.add(btnFindMaXt, BorderLayout.EAST);

        String tsInfo = nv.getThiSinh() != null ? formatThiSinh(nv.getThiSinh()) : "Khong ro thi sinh";

        int r = JOptionPane.showConfirmDialog(
                this,
                new Object[]{
                        "Thi sinh: " + tsInfo,
                        "Tim ma xet tuyen:", mapPanel,
                        "Chon ma xet tuyen - phuong thuc - to hop (*):", cboMap,
                        "Thu tu nguyen vong:", spnThuTu,
                        "Diem THXT:", txtDiemThxt,
                        "Diem THGXT:", txtDiemThgxt,
                        "Diem cong:", txtDiemCong,
                        "Diem uu tien:", txtDiemUuTien,
                        "Diem xet tuyen:", txtDiemXetTuyen,
                        "Nguon diem tot nhat:", txtNguonDiem,
                        "Ket qua:", cboKetQua,
                        "Ghi chu:", txtGhiChu
                },
                "Sua nguyen vong",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (r != JOptionPane.OK_OPTION) return;

        MaXetTuyenMap map = (MaXetTuyenMap) cboMap.getSelectedItem();
        if (map == null) {
            showMessage(this, "Chua chon ma xet tuyen!");
            return;
        }
        if (!isMapUsableForNguyenVong(map)) return;

        Integer thiSinhId = nv.getThiSinh() != null ? nv.getThiSinh().getThisinhId() : null;
        Integer thuTu = (Integer) spnThuTu.getValue();
        String duplicate = validateNguyenVongUnique(nv.getNguyenvongId(), thiSinhId, map, thuTu);
        if (duplicate != null) {
            showMessage(this, duplicate);
            return;
        }

        try {
            applyMapToNguyenVong(nv, map);
            nv.setThuTu(thuTu);
            applyOptionalFields(nv, txtDiemThxt, txtDiemThgxt, txtDiemCong, txtDiemUuTien,
                    txtDiemXetTuyen, txtNguonDiem, cboKetQua, txtGhiChu);

            service.update(nv);
            showSuccess(this, "Cap nhat nguyen vong thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private void loadMapCombo(JComboBox<MaXetTuyenMap> combo, String keyword, MaXetTuyenMap selected) {
        combo.removeAllItems();
        List<MaXetTuyenMap> maps = (keyword == null || keyword.trim().isEmpty())
                ? maXetTuyenMapService.findAll()
                : maXetTuyenMapService.search(keyword.trim());
        for (MaXetTuyenMap map : maps) {
            combo.addItem(map);
        }
        combo.setRenderer(createMaXetTuyenRenderer());

        if (selected != null && selected.getMaXettuyenId() != null) {
            boolean found = false;
            for (int i = 0; i < combo.getItemCount(); i++) {
                MaXetTuyenMap item = combo.getItemAt(i);
                if (item != null && selected.getMaXettuyenId().equals(item.getMaXettuyenId())) {
                    combo.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                combo.addItem(selected);
                combo.setSelectedIndex(combo.getItemCount() - 1);
            }
        }
    }

    private ListCellRenderer<? super MaXetTuyenMap> createMaXetTuyenRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof MaXetTuyenMap) {
                    MaXetTuyenMap map = (MaXetTuyenMap) value;
                    String nganh = map.getNganh() != null
                            ? safe(map.getNganh().getMaNganh()) + " - " + safe(map.getNganh().getTenNganh())
                            : "Chua map nganh";
                    String pt = map.getPhuongThuc() != null ? safe(map.getPhuongThuc().getMaPhuongthuc()) : "?";
                    String toHop = map.getNganhToHop() != null && map.getNganhToHop().getToHop() != null
                            ? safe(map.getNganhToHop().getToHop().getMaTohop())
                            : safe(map.getMaTohopNguon());
                    setText(safe(map.getMaXetTuyen()) + " | " + pt + " | " + toHop + " | " + nganh);
                }
                return this;
            }
        };
    }

    private JComboBox<String> buildKetQuaCombo(String selected) {
        JComboBox<String> combo = new JComboBox<>(new String[]{
                NguyenVong.KetQua.CHO_XET,
                NguyenVong.KetQua.TRUNG_TUYEN,
                NguyenVong.KetQua.TRUOT,
                NguyenVong.KetQua.PHOI_DU_KIEN
        });
        if (selected != null && !selected.trim().isEmpty()) {
            combo.setSelectedItem(selected.trim());
        }
        return combo;
    }

    private ThiSinh findThiSinhByIdentifier(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return null;
        String key = keyword.trim();
        Optional<ThiSinh> byCccd = thiSinhService.findByCccd(key);
        if (byCccd.isPresent()) return byCccd.get();
        return thiSinhService.findBySoBaoDanh(key).orElse(null);
    }

    private String formatThiSinh(ThiSinh ts) {
        if (ts == null) return "";
        return safe(ts.getHoVaTen())
                + " | CCCD: " + safe(ts.getCccd())
                + " | SBD: " + safe(ts.getSobaodanh());
    }

    private boolean isMapUsableForNguyenVong(MaXetTuyenMap map) {
        if (map.getNganh() == null) {
            showMessage(this, "Ma xet tuyen nay chua map nganh.");
            return false;
        }
        if (map.getNganhToHop() == null) {
            showMessage(this, "Ma xet tuyen nay chua map nganh - to hop.");
            return false;
        }
        if (map.getPhuongThuc() == null) {
            showMessage(this, "Ma xet tuyen nay chua map phuong thuc.");
            return false;
        }
        return true;
    }

    private void applyMapToNguyenVong(NguyenVong nv, MaXetTuyenMap map) {
        nv.setMaXetTuyenMap(map);
        nv.setNganh(map.getNganh());
        nv.setNganhToHop(map.getNganhToHop());
        nv.setPhuongThuc(map.getPhuongThuc());
    }

    private void applyOptionalFields(NguyenVong nv,
                                     JTextField txtDiemThxt,
                                     JTextField txtDiemThgxt,
                                     JTextField txtDiemCong,
                                     JTextField txtDiemUuTien,
                                     JTextField txtDiemXetTuyen,
                                     JTextField txtNguonDiem,
                                     JComboBox<String> cboKetQua,
                                     JTextField txtGhiChu) {
        nv.setDiemThxt(parseOptionalBigDecimal(txtDiemThxt.getText(), "Diem THXT"));
        nv.setDiemThgxt(parseOptionalBigDecimal(txtDiemThgxt.getText(), "Diem THGXT"));
        nv.setDiemCong(parseOptionalBigDecimal(txtDiemCong.getText(), "Diem cong"));
        nv.setDiemUutien(parseOptionalBigDecimal(txtDiemUuTien.getText(), "Diem uu tien"));
        nv.setDiemXettuyen(parseOptionalBigDecimal(txtDiemXetTuyen.getText(), "Diem xet tuyen"));
        nv.setPhuongThucDiemTotNhat(toNullIfBlank(txtNguonDiem.getText()));
        nv.setKetQua((String) cboKetQua.getSelectedItem());
        nv.setGhiChu(toNullIfBlank(txtGhiChu.getText()));
    }

    private BigDecimal parseOptionalBigDecimal(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) return null;
        String normalized = text.trim().replace(',', '.');
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " khong hop le: " + text);
        }
    }

    private String validateNguyenVongUnique(Integer currentId, Integer thiSinhId, MaXetTuyenMap map, Integer thuTu) {
        if (thiSinhId == null) return null;
        List<NguyenVong> list = service.findByThiSinhId(thiSinhId);
        for (NguyenVong old : list) {
            if (currentId != null && currentId.equals(old.getNguyenvongId())) {
                continue;
            }
            if (thuTu != null && thuTu.equals(old.getThuTu())) {
                return "Thi sinh nay da co nguyen vong thu " + thuTu + ". Hay chon thu tu khac.";
            }
            if (isSameChoice(old, map)) {
                return "Nguyen vong nay da ton tai cho thi sinh nay.";
            }
        }
        return null;
    }

    private boolean isSameChoice(NguyenVong old, MaXetTuyenMap map) {
        if (old == null || map == null) return false;
        if (old.getMaXetTuyenMap() != null
                && map.getMaXettuyenId() != null
                && map.getMaXettuyenId().equals(old.getMaXetTuyenMap().getMaXettuyenId())) {
            return true;
        }
        NganhToHop nth = map.getNganhToHop();
        return old.getNganh() != null && map.getNganh() != null
                && old.getNganh().getNganhId().equals(map.getNganh().getNganhId())
                && old.getNganhToHop() != null && nth != null
                && old.getNganhToHop().getNganhTohopId().equals(nth.getNganhTohopId())
                && old.getPhuongThuc() != null && map.getPhuongThuc() != null
                && old.getPhuongThuc().getPhuongthucId().equals(map.getPhuongThuc().getPhuongthucId());
    }

    private String toNullIfBlank(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void configureMainTable(JTable table) {
        setColumnWidth(table, 0, 60);   // ID
        setColumnWidth(table, 1, 110);  // CCCD
        setColumnWidth(table, 2, 90);   // SBD
        setColumnWidth(table, 3, 180);  // Ho ten
        setColumnWidth(table, 4, 45);   // NV
        setColumnWidth(table, 5, 95);   // Ma XT
        setColumnWidth(table, 6, 60);   // CT
        setColumnWidth(table, 7, 210);  // Nganh
        setColumnWidth(table, 8, 80);   // To hop
        setColumnWidth(table, 9, 75);   // PT
        setColumnWidth(table, 10, 75);  // Diem XT
        setColumnWidth(table, 11, 90);  // Ket qua
    }

    private void configureDetailTable(JTable table) {
        setColumnWidth(table, 0, 45);
        setColumnWidth(table, 1, 95);
        setColumnWidth(table, 2, 60);
        setColumnWidth(table, 3, 220);
        setColumnWidth(table, 4, 210);
        setColumnWidth(table, 5, 80);
        setColumnWidth(table, 6, 75);
        setColumnWidth(table, 7, 75);
        setColumnWidth(table, 8, 90);
        setColumnWidth(table, 9, 260);
    }

    private void setColumnWidth(JTable table, int colIndex, int width) {
        TableColumn col = table.getColumnModel().getColumn(colIndex);
        col.setPreferredWidth(width);
    }

    private boolean matchesKeyword(NguyenVong nv, String keyword) {
        StringBuilder sb = new StringBuilder();

        ThiSinh ts = nv.getThiSinh();
        if (ts != null) {
            sb.append(' ').append(safe(ts.getCccd()));
            sb.append(' ').append(safe(ts.getSobaodanh()));
            sb.append(' ').append(safe(ts.getHoVaTen()));
        }

        sb.append(' ').append(getMaXetTuyen(nv));
        sb.append(' ').append(getTenChuongTrinh(nv));

        if (nv.getNganh() != null) {
            sb.append(' ').append(safe(nv.getNganh().getMaNganh()));
            sb.append(' ').append(safe(nv.getNganh().getTenNganh()));
        }

        sb.append(' ').append(getToHopDisplay(nv));

        if (nv.getPhuongThuc() != null) {
            sb.append(' ').append(safe(nv.getPhuongThuc().getMaPhuongthuc()));
            sb.append(' ').append(safe(nv.getPhuongThuc().getTenPhuongthuc()));
        }

        return normalizeForSearch(sb.toString()).contains(keyword);
    }

    private String getMaXetTuyen(NguyenVong nv) {
        return nv.getMaXetTuyenMap() != null ? safe(nv.getMaXetTuyenMap().getMaXetTuyen()) : "";
    }

    private String getTenChuongTrinh(NguyenVong nv) {
        MaXetTuyenMap map = nv.getMaXetTuyenMap();
        if (map == null) return "";
        return safe(map.getTenChuongTrinh());
    }

    private String getChuongTrinhTag(NguyenVong nv) {
        return isClc(nv) ? "CLC" : "THUONG";
    }

    private boolean isClc(NguyenVong nv) {
        String maXt = getMaXetTuyen(nv).toUpperCase(Locale.ROOT);
        String tenCt = normalizeForSearch(getTenChuongTrinh(nv));
        return maXt.contains("CLC")
                || tenCt.contains("chat luong cao");
    }

    private String getToHopDisplay(NguyenVong nv) {
        if (nv.getNganhToHop() != null && nv.getNganhToHop().getToHop() != null) {
            return safe(nv.getNganhToHop().getToHop().getMaTohop());
        }
        if (nv.getMaXetTuyenMap() != null) {
            return safe(nv.getMaXetTuyenMap().getMaTohopNguon());
        }
        return "";
    }

    private String formatScore(BigDecimal score) {
        return score == null ? "" : score.stripTrailingZeros().toPlainString();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String normalizeForSearch(String input) {
        if (input == null) return "";
        String s = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .trim();
        return s.replaceAll("\\s+", " ");
    }
}