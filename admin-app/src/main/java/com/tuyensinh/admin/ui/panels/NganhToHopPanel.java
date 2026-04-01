package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class NganhToHopPanel extends JPanel {

    private MainFrame mainFrame;
    private XetTuyenService xetTuyenService = new XetTuyenService();
    private NganhToHopService nganhToHopService = new NganhToHopService();
    private ToHopService toHopService = new ToHopService();

    private JTable table;
    private DefaultTableModel model;
    private JButton btnAdd, btnDelete;
    private JLabel lblTotal;

    public NganhToHopPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JLabel("Gan to hop mon cho nganh tuyen sinh"));
        toolbar.add(Box.createHorizontalStrut(20));
        btnAdd = new JButton("Gan to hop");
        btnAdd.addActionListener(e -> showAddDialog());
        toolbar.add(btnAdd);
        btnDelete = new JButton("Xoa");
        btnDelete.addActionListener(e -> deleteNt());
        toolbar.add(btnDelete);
        add(toolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(
            new String[]{"ID", "Ma Nganh", "Ten Nganh", "Ma To Hop", "Ten To Hop", "Do lech"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(25);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 12));
        add(new JScrollPane(table), BorderLayout.CENTER);

        lblTotal = new JLabel("Tong: 0");
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(lblTotal, BorderLayout.WEST);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadData() {
        model.setRowCount(0);
        List<NganhToHop> list = nganhToHopService.findAll();
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
        lblTotal.setText("Tong: " + list.size() + " ban ghi");
    }

    private NganhToHop getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Integer id = (Integer) model.getValueAt(row, 0);
        return nganhToHopService.findById(id);
    }

    private void showAddDialog() {
        JComboBox<Nganh> cboN = new JComboBox<>();
        JComboBox<ToHop> cboTh = new JComboBox<>();
        JTextField txtDl = new JTextField("0", 10);

        for (Nganh n : xetTuyenService.findActiveNganh()) cboN.addItem(n);
        for (ToHop th : toHopService.findAll()) cboTh.addItem(th);

        Object[] msg = {
            "Nganh (*):", cboN,
            "To hop (*):", cboTh,
            "Do lech:", txtDl
        };

        int result = JOptionPane.showConfirmDialog(this, msg, "Gan to hop cho nganh", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Nganh n = (Nganh) cboN.getSelectedItem();
            ToHop th = (ToHop) cboTh.getSelectedItem();
            if (n == null || th == null) {
                JOptionPane.showMessageDialog(this, "Chon day du thong tin!");
                return;
            }
            NganhToHop nt = new NganhToHop();
            nt.setNganh(n);
            nt.setToHop(th);
            try { nt.setDoLech(new java.math.BigDecimal(txtDl.getText().trim())); } catch (Exception ex) {}
            try {
                nganhToHopService.save(nt);
                JOptionPane.showMessageDialog(this, "Gan thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }

    private void deleteNt() {
        NganhToHop nt = getSelected();
        if (nt == null) {
            JOptionPane.showMessageDialog(this, "Chon ban ghi can xoa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Ban co sach xoa ban ghi nay?", "Xac nhan", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                nganhToHopService.delete(nt);
                JOptionPane.showMessageDialog(this, "Xoa thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }
}
