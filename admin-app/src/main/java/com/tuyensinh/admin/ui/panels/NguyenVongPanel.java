package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.BasePanel;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.admin.ui.TableFactory;
import com.tuyensinh.admin.ui.UIConstants;
import com.tuyensinh.entity.MaXetTuyenMap;
import com.tuyensinh.entity.NguyenVong;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.service.NguyenVongService;

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
import java.util.Set;

public class NguyenVongPanel extends BasePanel {

    private final NguyenVongService service;

    private JTable table;
    private DefaultTableModel model;

    private JTable detailTable;
    private DefaultTableModel detailModel;

    private JTextField txtSearch;
    private JLabel lblTotal;
    private JLabel lblDetailTitle;

    private List<NguyenVong> currentList = new ArrayList<>();

    public NguyenVongPanel(MainFrame mainFrame) {
        super(mainFrame);
        this.service = new NguyenVongService();
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
        add(lblTotal, BorderLayout.SOUTH);
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        toolbar.add(new JLabel("Tim (CCCD / SBD / Ho ten / Ma XT / Nganh):"));

        txtSearch = new JTextField(24);
        txtSearch.addActionListener(e -> loadData());
        toolbar.add(txtSearch);

        JButton btnSearch = new JButton("Tim");
        btnSearch.addActionListener(e -> loadData());
        toolbar.add(btnSearch);

        JButton btnClear = new JButton("Lam moi");
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            loadData();
        });
        toolbar.add(btnClear);

        toolbar.add(Box.createHorizontalStrut(12));

        JButton btnImport = new JButton("Import Excel");
        btnImport.addActionListener(e -> showImportDialog());
        toolbar.add(btnImport);

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
        List<NguyenVong> all = service.findAll();
        currentList = new ArrayList<>();

        for (NguyenVong nv : all) {
            if (keyword.isEmpty() || matchesKeyword(nv, keyword)) {
                currentList.add(nv);
            }
        }

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

        lblTotal.setText("Tong NV: " + currentList.size() + " | Thi sinh: " + uniqueThiSinhIds.size());

        if (!currentList.isEmpty()) {
            table.setRowSelectionInterval(0, 0);
            loadDetailForSelected();
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