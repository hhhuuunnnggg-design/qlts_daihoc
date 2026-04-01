package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.*;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Refactored: extends BasePanel, uses TableFactory.
 */
public class NganhToHopPanel extends BasePanel {

    private XetTuyenService xetTuyenService;
    private NganhToHopService service;
    private ToHopService toHopService;

    private JTable table;
    private DefaultTableModel model;

    public NganhToHopPanel(MainFrame mainFrame) {
        super(mainFrame);
        xetTuyenService = new XetTuyenService();
        service = new NganhToHopService();
        toHopService = new ToHopService();
        initUI();
        loadData();
    }

    @Override
    public String getPageTitle() {
        return UIConstants.PAGE_NGANH_TO_HOP;
    }

    @Override
    protected void initUI() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.add(new JLabel("Gan to hop mon cho nganh tuyen sinh"));
        toolbar.add(Box.createHorizontalStrut(20));

        JButton btnAdd = new JButton("Gan to hop");
        btnAdd.addActionListener(e -> showAddDialog());
        toolbar.add(btnAdd);

        JButton btnDelete = new JButton("Xoa");
        btnDelete.addActionListener(e -> deleteNt());
        toolbar.add(btnDelete);

        add(toolbar, BorderLayout.NORTH);

        model = TableFactory.newReadOnlyModel(
            "ID", "Ma Nganh", "Ten Nganh", "Ma To Hop", "Ten To Hop", "Do lech");
        table = TableFactory.create(model);
        add(TableFactory.wrap(table), BorderLayout.CENTER);

        JLabel lblTotal = new JLabel("Tong: 0");
        lblTotal.setFont(UIConstants.FONT_SMALL);
        add(lblTotal, BorderLayout.SOUTH);
    }

    @Override
    public void loadData() {
        model.setRowCount(0);
        List<NganhToHop> list = service.findAll();
        for (NganhToHop nt : list) {
            model.addRow(new Object[]{
                nt.getNganhTohopId(),
                nt.getNganh() != null ? nt.getNganh().getMaNganh() : "",
                nt.getNganh() != null ? nt.getNganh().getTenNganh() : "",
                nt.getToHop() != null ? nt.getToHop().getMaTohop() : "",
                nt.getToHop() != null ? nt.getToHop().getTenTohop() : "",
                nt.getDoLech()
            });
        }
    }

    private NganhToHop getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return service.findById((Integer) model.getValueAt(row, 0));
    }

    private void showAddDialog() {
        JComboBox<Nganh> cboN = new JComboBox<>();
        JComboBox<ToHop> cboTh = new JComboBox<>();
        JTextField txtDl = new JTextField("0", 10);

        for (Nganh n : xetTuyenService.findActiveNganh()) cboN.addItem(n);
        for (ToHop th : toHopService.findAll()) cboTh.addItem(th);

        int r = JOptionPane.showConfirmDialog(this,
            new Object[]{
                "Nganh (*):", cboN,
                "To hop (*):", cboTh,
                "Do lech:", txtDl
            },
            "Gan to hop cho nganh", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        Nganh n = (Nganh) cboN.getSelectedItem();
        ToHop th = (ToHop) cboTh.getSelectedItem();
        if (n == null || th == null) { showMessage(this, "Chon day du thong tin!"); return; }

        NganhToHop nt = new NganhToHop();
        nt.setNganh(n);
        nt.setToHop(th);
        nt.setDoLech(parseBigDecimal(txtDl.getText()));

        try {
            service.save(nt);
            showSuccess(this, "Gan thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private void deleteNt() {
        NganhToHop nt = getSelected();
        if (nt == null) { showSelectRow(this); return; }
        if (confirmDelete(this, nt.getNganh() != null ? nt.getNganh().getMaNganh() : "") != JOptionPane.YES_OPTION) return;

        try {
            service.delete(nt);
            showSuccess(this, UIConstants.MSG_DELETE_SUCCESS);
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }
}
