package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.*;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Refactored: extends BaseCrudPanel, uses TableFactory + parse helpers.
 * loadData() fetchs subject list per row — kept as-is (business-specific logic).
 */
public class ToHopPanel extends BaseCrudPanel<ToHop> {

    private XetTuyenService service;
    private ToHopService toHopService;

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
    protected void configureTableColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
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
        var list = kw.isEmpty() ? service.findAllToHop() : service.searchToHop(kw);

        for (ToHop th : list) {
            List<ToHopMon> monList = toHopService.getMonByToHop(th.getTohopId());
            String m1 = "", m2 = "", m3 = "";
            String loai = "Thong thuong";
            if (th.getMaTohop() != null && th.getMaTohop().startsWith("NK")) {
                loai = "Nang khieu";
            }
            for (int i = 0; i < monList.size() && i < 3; i++) {
                if (i == 0) m1 = monList.get(i).getMon().getMaMon();
                if (i == 1) m2 = monList.get(i).getMon().getMaMon();
                if (i == 2) m3 = monList.get(i).getMon().getMaMon();
            }
            model.addRow(new Object[]{th.getTohopId(), th.getMaTohop(), th.getTenTohop(), m1, m2, m3, loai});
        }
        updateTotalLabel(list.size(), "");
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

        int r = JOptionPane.showConfirmDialog(this,
            new Object[]{"Ma to hop (*):", txtMa, "Ten to hop:", txtTen},
            "Them to hop moi", JOptionPane.OK_CANCEL_OPTION);
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
        if (th == null) { showSelectRow(); return; }

        JTextField txtTen = new JTextField(th.getTenTohop() != null ? th.getTenTohop() : "");

        int r = JOptionPane.showConfirmDialog(this,
            new Object[]{"Ma to hop: " + th.getMaTohop() + " (khong doi)", "Ten to hop:", txtTen},
            "Sua to hop", JOptionPane.OK_CANCEL_OPTION);
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
}
