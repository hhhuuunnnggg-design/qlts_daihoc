package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.BaseCrudPanel;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.admin.ui.TableFactory;
import com.tuyensinh.admin.ui.ToolbarFactory;
import com.tuyensinh.admin.ui.UIConstants;
import com.tuyensinh.entity.Mon;
import com.tuyensinh.entity.ToHop;
import com.tuyensinh.entity.ToHopMon;
import com.tuyensinh.service.ToHopService;
import com.tuyensinh.service.XetTuyenService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ToHopPanel extends BaseCrudPanel<ToHop> {

    private final XetTuyenService service;
    private final ToHopService toHopService;

    private JTable tableMon;
    private DefaultTableModel monModel;

    public ToHopPanel(MainFrame mainFrame) {
        super(mainFrame);
        service = new XetTuyenService();
        toHopService = new ToHopService();
        initUI();
        loadData();
    }

    @Override
    protected String[] getTableColumns() {
        return new String[]{"ID", "Ma to hop", "Ten to hop", "Mon 1", "Mon 2", "Mon 3", "Loai"};
    }

    @Override
    protected ToHop getSelectedEntity() {
        int row = table.getSelectedRow();
        return row < 0 ? null : toHopService.findById((Integer) model.getValueAt(row, 0));
    }

    @Override
    protected Integer getSelectedId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (Integer) model.getValueAt(row, 0);
    }

    @Override
    public String getPageTitle() {
        return UIConstants.PAGE_TO_HOP;
    }

    @Override
    protected void initUI() {
        buildToolbarCustom();
        buildMainContent();
        buildBottomBar();
    }

    private void buildToolbarCustom() {
        JTextField[] searchFieldOut = new JTextField[1];
        JPanel toolbar = ToolbarFactory.createSearchToolbar(
                searchFieldOut,
                this::doSearch,
                new ToolbarFactory.ActionButton("Them to hop", this::showAddDialog),
                new ToolbarFactory.ActionButton("Sua to hop", this::showEditDialog),
                new ToolbarFactory.ActionButton("Xoa to hop", this::doDelete),
                new ToolbarFactory.ActionButton("Them mon", this::showAddMonDialog),
                new ToolbarFactory.ActionButton("Sua mon", this::showEditMonDialog),
                new ToolbarFactory.ActionButton("Xoa mon", this::deleteToHopMon)
        );
        this.searchTextField = searchFieldOut[0];
        add(toolbar, BorderLayout.NORTH);
    }

    private void buildMainContent() {
        model = TableFactory.newReadOnlyModel(getTableColumns());
        table = TableFactory.create(model);
        configureTableColumns();

        monModel = TableFactory.newReadOnlyModel("ID", "Ma mon", "Ten mon", "Thu tu");
        tableMon = TableFactory.create(monModel);
        configureMonTableColumns();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadMonData();
            }
        });

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                TableFactory.wrap(table),
                TableFactory.wrap(tableMon)
        );
        splitPane.setResizeWeight(0.58);

        add(splitPane, BorderLayout.CENTER);
    }

    @Override
    protected void configureTableColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);
        table.getColumnModel().getColumn(5).setPreferredWidth(70);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);
    }

    private void configureMonTableColumns() {
        tableMon.getColumnModel().getColumn(0).setPreferredWidth(40);
        tableMon.getColumnModel().getColumn(1).setPreferredWidth(100);
        tableMon.getColumnModel().getColumn(2).setPreferredWidth(240);
        tableMon.getColumnModel().getColumn(3).setPreferredWidth(70);
    }

    @Override
    protected void buildBottomBar() {
        totalLabel = new JLabel("Tong: 0 to hop");
        totalLabel.setFont(UIConstants.FONT_SMALL);
        add(totalLabel, BorderLayout.SOUTH);
    }

    @Override
    public void loadData() {
        model.setRowCount(0);

        String kw = searchTextField != null ? searchTextField.getText().trim() : "";
        List<ToHop> list = kw.isEmpty() ? service.findAllToHop() : service.searchToHop(kw);

        for (ToHop th : list) {
            List<ToHopMon> monList = toHopService.getMonByToHop(th.getTohopId());

            String m1 = "";
            String m2 = "";
            String m3 = "";
            String loai = "Thong thuong";

            for (int i = 0; i < monList.size() && i < 3; i++) {
                ToHopMon tm = monList.get(i);
                String maMon = tm.getMon() != null ? tm.getMon().getMaMon() : "";

                if (i == 0) m1 = maMon;
                if (i == 1) m2 = maMon;
                if (i == 2) m3 = maMon;

                if (isNangKhieuMon(maMon)) {
                    loai = "Nang khieu";
                }
            }

            model.addRow(new Object[]{
                    th.getTohopId(),
                    th.getMaTohop(),
                    th.getTenTohop(),
                    m1,
                    m2,
                    m3,
                    loai
            });
        }

        updateTotalLabel(list.size(), "to hop");

        if (model.getRowCount() > 0) {
            if (table.getSelectedRow() < 0) {
                table.setRowSelectionInterval(0, 0);
            } else {
                loadMonData();
            }
        } else {
            monModel.setRowCount(0);
        }
    }

    private void loadMonData() {
        monModel.setRowCount(0);

        ToHop th = getSelectedEntity();
        if (th == null) return;

        List<ToHopMon> ds = toHopService.getMonByToHop(th.getTohopId());
        for (ToHopMon tm : ds) {
            Mon mon = tm.getMon();
            monModel.addRow(new Object[]{
                    tm.getTohopMonId(),
                    mon != null ? mon.getMaMon() : "",
                    mon != null ? mon.getTenMon() : "",
                    tm.getThuTu()
            });
        }
    }

    private ToHopMon getSelectedToHopMon() {
        int row = tableMon.getSelectedRow();
        if (row < 0) return null;

        Integer id = (Integer) monModel.getValueAt(row, 0);
        ToHop th = getSelectedEntity();
        if (th == null) return null;

        return toHopService.getMonByToHop(th.getTohopId()).stream()
                .filter(x -> id.equals(x.getTohopMonId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    protected String getEntityDisplayName(ToHop th) {
        return th.getMaTohop();
    }

    @Override
    protected void deleteEntity(ToHop th) throws Exception {
        toHopService.delete(th);
    }

    @Override
    protected void showAddDialog() {
        JTextField txtMa = new JTextField(20);
        JTextField txtTen = new JTextField(20);

        int r = JOptionPane.showConfirmDialog(
                this,
                new Object[]{"Ma to hop (*):", txtMa, "Ten to hop:", txtTen},
                "Them to hop moi",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (r != JOptionPane.OK_OPTION) return;

        if (txtMa.getText().trim().isEmpty()) {
            showMessage(this, "Ma to hop la bat buoc!");
            return;
        }

        ToHop th = new ToHop();
        th.setMaTohop(txtMa.getText().trim());
        th.setTenTohop(txtTen.getText().trim().isEmpty() ? null : txtTen.getText().trim());

        try {
            toHopService.save(th);
            showSuccess(this, "Them thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    @Override
    protected void showEditDialog() {
        ToHop th = getSelectedEntity();
        if (th == null) {
            showSelectRow();
            return;
        }

        JTextField txtTen = new JTextField(th.getTenTohop() != null ? th.getTenTohop() : "");

        int r = JOptionPane.showConfirmDialog(
                this,
                new Object[]{"Ma to hop: " + th.getMaTohop() + " (khong doi)", "Ten to hop:", txtTen},
                "Sua to hop",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (r != JOptionPane.OK_OPTION) return;

        th.setTenTohop(txtTen.getText().trim().isEmpty() ? null : txtTen.getText().trim());

        try {
            toHopService.update(th);
            showSuccess(this, "Cap nhat thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private void showAddMonDialog() {
        ToHop th = getSelectedEntity();
        if (th == null) {
            showMessage(this, "Hay chon 1 to hop truoc!");
            return;
        }

        JComboBox<Mon> cboMon = new JComboBox<>();
        for (Mon m : toHopService.findAllMonHoc()) {
            cboMon.addItem(m);
        }
        cboMon.setRenderer(createMonRenderer());

        JTextField txtThuTu = new JTextField("1", 10);

        int r = JOptionPane.showConfirmDialog(
                this,
                new Object[]{"Mon (*):", cboMon, "Thu tu:", txtThuTu},
                "Them mon vao to hop",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (r != JOptionPane.OK_OPTION) return;

        Mon mon = (Mon) cboMon.getSelectedItem();
        if (mon == null) {
            showMessage(this, "Chua chon mon!");
            return;
        }

        boolean daTonTai = toHopService.getMonByToHop(th.getTohopId()).stream()
                .anyMatch(x -> x.getMon() != null && mon.getMonId().equals(x.getMon().getMonId()));
        if (daTonTai) {
            showMessage(this, "Mon nay da co trong to hop!");
            return;
        }

        ToHopMon tm = new ToHopMon();
        tm.setToHop(th);
        tm.setMon(mon);
        tm.setThuTu(parseShort(txtThuTu.getText()));

        try {
            toHopService.saveToHopMon(tm);
            showSuccess(this, "Them mon thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private void showEditMonDialog() {
        ToHopMon tm = getSelectedToHopMon();
        if (tm == null) {
            showMessage(this, "Hay chon 1 mon trong to hop!");
            return;
        }

        JComboBox<Mon> cboMon = new JComboBox<>();
        for (Mon m : toHopService.findAllMonHoc()) {
            cboMon.addItem(m);
        }
        cboMon.setRenderer(createMonRenderer());
        if (tm.getMon() != null) {
            cboMon.setSelectedItem(tm.getMon());
        }

        JTextField txtThuTu = new JTextField(
                tm.getThuTu() != null ? String.valueOf(tm.getThuTu()) : "1",
                10
        );

        int r = JOptionPane.showConfirmDialog(
                this,
                new Object[]{"Mon (*):", cboMon, "Thu tu:", txtThuTu},
                "Sua mon trong to hop",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (r != JOptionPane.OK_OPTION) return;

        Mon mon = (Mon) cboMon.getSelectedItem();
        if (mon == null) {
            showMessage(this, "Chua chon mon!");
            return;
        }

        ToHop th = getSelectedEntity();
        boolean biTrung = toHopService.getMonByToHop(th.getTohopId()).stream().anyMatch(x -> {
            if (x.getTohopMonId().equals(tm.getTohopMonId())) return false;
            return x.getMon() != null && mon.getMonId().equals(x.getMon().getMonId());
        });
        if (biTrung) {
            showMessage(this, "Mon nay da ton tai trong to hop!");
            return;
        }

        tm.setMon(mon);
        tm.setThuTu(parseShort(txtThuTu.getText()));

        try {
            toHopService.updateToHopMon(tm);
            showSuccess(this, "Cap nhat mon thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private void deleteToHopMon() {
        ToHopMon tm = getSelectedToHopMon();
        if (tm == null) {
            showMessage(this, "Hay chon 1 mon trong to hop!");
            return;
        }

        String ten = tm.getMon() != null
                ? tm.getMon().getMaMon() + " - " + tm.getMon().getTenMon()
                : String.valueOf(tm.getTohopMonId());

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Ban co chac muon xoa mon khoi to hop?\n" + ten,
                "Xac nhan",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            toHopService.deleteToHopMon(tm);
            showSuccess(this, "Xoa mon thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private ListCellRenderer<? super Mon> createMonRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Mon) {
                    Mon m = (Mon) value;
                    setText(m.getMaMon() + " - " + m.getTenMon());
                }
                return this;
            }
        };
    }

    private Short parseShort(String s) {
        try {
            return (s == null || s.trim().isEmpty()) ? 1 : Short.valueOf(s.trim());
        } catch (Exception e) {
            return 1;
        }
    }

    private boolean isNangKhieuMon(String maMon) {
        if (maMon == null) {
            return false;
        }

        String normalized = maMon.trim().toUpperCase();
        return normalized.matches("^NK\\d+.*");
    }
}