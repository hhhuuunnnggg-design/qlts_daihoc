package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class NganhPanel extends JPanel {

    private MainFrame mainFrame;
    private XetTuyenService service = new XetTuyenService();
    private ToHopService toHopService = new ToHopService();

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JButton btnSearch, btnAdd, btnEdit, btnDelete;
    private JLabel lblTotal;

    public NganhPanel(MainFrame mainFrame) {
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
        btnDelete.addActionListener(e -> deleteNganh());
        toolbar.add(btnDelete);

        add(toolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(
            new String[]{"ID", "Ma nganh", "Ten nganh", "Chi tieu", "Diem san", "Diem TT", "Active"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 12));
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        lblTotal = new JLabel("Tong: 0 nganh");
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(lblTotal, BorderLayout.WEST);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadData() {
        model.setRowCount(0);
        List<Nganh> list;
        String kw = txtSearch.getText().trim();
        if (kw.isEmpty()) {
            list = service.findAllNganh();
        } else {
            list = service.searchNganh(kw);
        }
        for (Nganh n : list) {
            model.addRow(new Object[]{
                n.getNganhId(),
                n.getMaNganh(),
                n.getTenNganh(),
                n.getChiTieu(),
                n.getDiemSan(),
                n.getDiemTrungTuyen(),
                n.getIsActive() ? "Active" : "Inactive"
            });
        }
        lblTotal.setText("Tong: " + list.size() + " nganh");
    }

    private Nganh getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Integer id = (Integer) model.getValueAt(row, 0);
        return service.findNganhById(id);
    }

    private void showAddDialog() {
        JTextField txtMa = new JTextField(20);
        JTextField txtTen = new JTextField(20);
        JTextField txtChiTieu = new JTextField("100", 20);
        JTextField txtDiemSan = new JTextField(20);
        JCheckBox chkActive = new JCheckBox("Active", true);

        Object[] msg = {
            "Ma nganh (*):", txtMa,
            "Ten nganh (*):", txtTen,
            "Chi tieu:", txtChiTieu,
            "Diem san:", txtDiemSan,
            "Active:", chkActive
        };

        int result = JOptionPane.showConfirmDialog(this, msg, "Them nganh moi", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ma va Ten nganh la bat buoc!");
                return;
            }
            Nganh n = new Nganh();
            n.setMaNganh(txtMa.getText().trim());
            n.setTenNganh(txtTen.getText().trim());
            try { n.setChiTieu(Integer.parseInt(txtChiTieu.getText().trim())); } catch (Exception ex) {}
            try { n.setDiemSan(new java.math.BigDecimal(txtDiemSan.getText().trim())); } catch (Exception ex) {}
            n.setIsActive(chkActive.isSelected());
            try {
                service.saveNganh(n);
                JOptionPane.showMessageDialog(this, "Them thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }

    private void showEditDialog() {
        Nganh n = getSelected();
        if (n == null) { JOptionPane.showMessageDialog(this, "Chon nganh can sua!"); return; }

        JTextField txtTen = new JTextField(n.getTenNganh());
        JTextField txtChiTieu = new JTextField(String.valueOf(n.getChiTieu()), 20);
        JTextField txtDiemSan = new JTextField(n.getDiemSan() != null ? n.getDiemSan().toString() : "", 20);
        JTextField txtDiemTT = new JTextField(n.getDiemTrungTuyen() != null ? n.getDiemTrungTuyen().toString() : "", 20);
        JCheckBox chkActive = new JCheckBox("Active", n.getIsActive());

        Object[] msg = {
            "Ma nganh: " + n.getMaNganh() + " (khong doi)",
            "Ten nganh:", txtTen,
            "Chi tieu:", txtChiTieu,
            "Diem san:", txtDiemSan,
            "Diem trung tuyen:", txtDiemTT,
            "Active:", chkActive
        };

        int result = JOptionPane.showConfirmDialog(this, msg, "Sua nganh", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            n.setTenNganh(txtTen.getText().trim());
            try { n.setChiTieu(Integer.parseInt(txtChiTieu.getText().trim())); } catch (Exception ex) {}
            try { n.setDiemSan(new java.math.BigDecimal(txtDiemSan.getText().trim())); } catch (Exception ex) {}
            try { n.setDiemTrungTuyen(new java.math.BigDecimal(txtDiemTT.getText().trim())); } catch (Exception ex) {}
            n.setIsActive(chkActive.isSelected());
            try {
                service.updateNganh(n);
                JOptionPane.showMessageDialog(this, "Cap nhat thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }

    private void deleteNganh() {
        Nganh n = getSelected();
        if (n == null) { JOptionPane.showMessageDialog(this, "Chon nganh can xoa!"); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Ban co chac xoa nganh '" + n.getTenNganh() + "'?", "Xac nhan", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.deleteNganh(n);
                JOptionPane.showMessageDialog(this, "Xoa thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }
}
