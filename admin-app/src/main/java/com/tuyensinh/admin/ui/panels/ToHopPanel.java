package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class ToHopPanel extends JPanel {

    private MainFrame mainFrame;
    private XetTuyenService service = new XetTuyenService();
    private ToHopService toHopService = new ToHopService();
    private MonService monService = new MonService();

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JButton btnSearch, btnAdd, btnEdit, btnDelete;

    public ToHopPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JLabel("Tim kiem:"));
        txtSearch = new JTextField(20);
        txtSearch.addActionListener(e -> loadData());
        toolbar.add(txtSearch);
        btnSearch = new JButton("Tim");
        btnSearch.addActionListener(e -> loadData());
        toolbar.add(btnSearch);
        toolbar.add(Box.createHorizontalStrut(20));
        btnAdd = new JButton("Them moi");
        btnAdd.addActionListener(e -> showAddDialog());
        toolbar.add(btnAdd);
        btnEdit = new JButton("Sua");
        btnEdit.addActionListener(e -> showEditDialog());
        toolbar.add(btnEdit);
        btnDelete = new JButton("Xoa");
        btnDelete.addActionListener(e -> deleteToHop());
        toolbar.add(btnDelete);
        add(toolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(
            new String[]{"ID", "Ma to hop", "Ten to hop", "Mon 1", "Mon 2", "Mon 3", "Loai"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 12));
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadData() {
        model.setRowCount(0);
        List<ToHop> list;
        String kw = txtSearch.getText().trim();
        if (kw.isEmpty()) {
            list = service.findAllToHop();
        } else {
            list = service.searchToHop(kw);
        }
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
    }

    private ToHop getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Integer id = (Integer) model.getValueAt(row, 0);
        return toHopService.findById(id);
    }

    private void showAddDialog() {
        JTextField txtMa = new JTextField(20);
        JTextField txtTen = new JTextField(20);
        Object[] msg = {"Ma to hop (*):", txtMa, "Ten to hop:", txtTen};
        int result = JOptionPane.showConfirmDialog(this, msg, "Them to hop moi", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            if (txtMa.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ma to hop la bat buoc!");
                return;
            }
            ToHop th = new ToHop();
            th.setMaTohop(txtMa.getText().trim());
            th.setTenTohop(txtTen.getText().trim().isEmpty() ? null : txtTen.getText().trim());
            try {
                toHopService.save(th);
                JOptionPane.showMessageDialog(this, "Them thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }

    private void showEditDialog() {
        ToHop th = getSelected();
        if (th == null) { JOptionPane.showMessageDialog(this, "Chon to hop can sua!"); return; }
        JTextField txtTen = new JTextField(th.getTenTohop() != null ? th.getTenTohop() : "");
        Object[] msg = {"Ma to hop: " + th.getMaTohop() + " (khong doi)", "Ten to hop:", txtTen};
        int result = JOptionPane.showConfirmDialog(this, msg, "Sua to hop", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            th.setTenTohop(txtTen.getText().trim().isEmpty() ? null : txtTen.getText().trim());
            try {
                toHopService.update(th);
                JOptionPane.showMessageDialog(this, "Cap nhat thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }

    private void deleteToHop() {
        ToHop th = getSelected();
        if (th == null) { JOptionPane.showMessageDialog(this, "Chon to hop can xoa!"); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Ban co chac xoa to hop '" + th.getMaTohop() + "'?", "Xac nhan", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                toHopService.delete(th);
                JOptionPane.showMessageDialog(this, "Xoa thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }
}
