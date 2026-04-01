package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.dao.PhuongThucDao;
import com.tuyensinh.service.*;
import com.tuyensinh.util.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class DiemThiPanel extends JPanel {

    private MainFrame mainFrame;
    private ThiSinhService thiSinhService = new ThiSinhService();
    private DiemThiService diemThiService = new DiemThiService();
    private PhuongThucDao phuongThucDao = new PhuongThucDao();

    private JTable table;
    private DefaultTableModel model;
    private JComboBox<PhuongThuc> cboPhuongThuc;
    private JTextField txtSearch;
    private JButton btnSearch, btnAdd, btnEdit, btnDelete;
    private JLabel lblTotal;

    public DiemThiPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JLabel("Phuong thuc:"));
        cboPhuongThuc = new JComboBox<>();
        cboPhuongThuc.addItem(null);
        for (PhuongThuc pt : phuongThucDao.findAll()) {
            cboPhuongThuc.addItem(pt);
        }
        cboPhuongThuc.addActionListener(e -> loadData());
        toolbar.add(cboPhuongThuc);

        toolbar.add(new JLabel("  Tim kiem:"));
        txtSearch = new JTextField(15);
        txtSearch.addActionListener(e -> loadData());
        toolbar.add(txtSearch);
        btnSearch = new JButton("Tim");
        btnSearch.addActionListener(e -> loadData());
        toolbar.add(btnSearch);

        toolbar.add(Box.createHorizontalStrut(20));
        btnAdd = new JButton("Them diem");
        btnAdd.addActionListener(e -> showAddDialog());
        toolbar.add(btnAdd);

        btnEdit = new JButton("Sua");
        btnEdit.addActionListener(e -> showEditDialog());
        toolbar.add(btnEdit);

        btnDelete = new JButton("Xoa");
        btnDelete.addActionListener(e -> deleteDiem());
        toolbar.add(btnDelete);

        add(toolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(
            new String[]{"ID", "So BD", "CCCD", "Ho Ten", "Phuong Thuc", "Nam TS", "Ghi chu"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 12));
        add(new JScrollPane(table), BorderLayout.CENTER);

        lblTotal = new JLabel("Tong: 0 ban ghi");
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(lblTotal, BorderLayout.WEST);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadData() {
        model.setRowCount(0);
        PhuongThuc pt = (PhuongThuc) cboPhuongThuc.getSelectedItem();
        String kw = txtSearch.getText().trim();

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
            String hoTen = ts != null ? ts.getHoVaTen() : "";
            String cccd = ts != null ? ts.getCccd() : "";
            model.addRow(new Object[]{
                dt.getDiemthiId(),
                dt.getSobaodanh(),
                cccd,
                hoTen,
                dt.getPhuongThuc() != null ? dt.getPhuongThuc().getTenPhuongthuc() : "",
                dt.getNamTuyensinh(),
                dt.getGhiChu()
            });
        }
        lblTotal.setText("Tong: " + list.size() + " ban ghi");
    }

    private DiemThi getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Integer id = (Integer) model.getValueAt(row, 0);
        return diemThiService.findById(id);
    }

    private void showAddDialog() {
        JTextField txtSbd = new JTextField(20);
        JTextField txtCccd = new JTextField(20);
        JComboBox<PhuongThuc> cboPt = new JComboBox<>();
        for (PhuongThuc pt : phuongThucDao.findAll()) {
            cboPt.addItem(pt);
        }
        JSpinner spnNam = new JSpinner(new SpinnerNumberModel((Integer) 2026, (Integer) 2020, (Integer) 2030, (Integer) 1));
        JTextField txtGhiChu = new JTextField(20);

        Object[] msg = {
            "So bao danh:", txtSbd,
            "CCCD (tim thi sinh):", txtCccd,
            "Phuong thuc (*):", cboPt,
            "Nam tuyen sinh:", spnNam,
            "Ghi chu:", txtGhiChu
        };

        int result = JOptionPane.showConfirmDialog(this, msg, "Them diem thi", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String cccd = txtCccd.getText().trim();
            if (cccd.isEmpty()) {
                JOptionPane.showMessageDialog(this, "CCCD la bat buoc!");
                return;
            }
            PhuongThuc pt = (PhuongThuc) cboPt.getSelectedItem();
            if (pt == null) {
                JOptionPane.showMessageDialog(this, "Chon phuong thuc!");
                return;
            }

            Optional<ThiSinh> optTs = thiSinhService.findByCccd(cccd);
            if (optTs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Khong tim thay thi sinh voi CCCD: " + cccd);
                return;
            }

            DiemThi dt = new DiemThi();
            dt.setThiSinh(optTs.get());
            dt.setPhuongThuc(pt);
            dt.setSobaodanh(txtSbd.getText().trim().isEmpty() ? null : txtSbd.getText().trim());
            dt.setNamTuyensinh((Short) spnNam.getValue());
            dt.setGhiChu(txtGhiChu.getText().trim().isEmpty() ? null : txtGhiChu.getText().trim());

            try {
                diemThiService.save(dt);
                JOptionPane.showMessageDialog(this, "Them thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }

    private void showEditDialog() {
        DiemThi dt = getSelected();
        if (dt == null) {
            JOptionPane.showMessageDialog(this, "Chon ban ghi can sua!");
            return;
        }
        JTextField txtSbd = new JTextField(dt.getSobaodanh() != null ? dt.getSobaodanh() : "");
        JTextField txtGhiChu = new JTextField(dt.getGhiChu() != null ? dt.getGhiChu() : "");
        JSpinner spnNam = new JSpinner(new SpinnerNumberModel(Integer.valueOf(dt.getNamTuyensinh()), Integer.valueOf(2020), Integer.valueOf(2030), Integer.valueOf(1)));

        Object[] msg = {
            "Thi sinh: " + (dt.getThiSinh() != null ? dt.getThiSinh().getHoVaTen() : "N/A"),
            "So bao danh:", txtSbd,
            "Nam tuyen sinh:", spnNam,
            "Ghi chu:", txtGhiChu
        };

        int result = JOptionPane.showConfirmDialog(this, msg, "Sua diem thi", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            dt.setSobaodanh(txtSbd.getText().trim().isEmpty() ? null : txtSbd.getText().trim());
            dt.setNamTuyensinh((Short) spnNam.getValue());
            dt.setGhiChu(txtGhiChu.getText().trim().isEmpty() ? null : txtGhiChu.getText().trim());
            try {
                diemThiService.update(dt);
                JOptionPane.showMessageDialog(this, "Cap nhat thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }

    private void deleteDiem() {
        DiemThi dt = getSelected();
        if (dt == null) {
            JOptionPane.showMessageDialog(this, "Chon ban ghi can xoa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Ban co chac xoa diem thi cua '" + (dt.getThiSinh() != null ? dt.getThiSinh().getHoVaTen() : "") + "'?",
            "Xac nhan", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                diemThiService.delete(dt);
                JOptionPane.showMessageDialog(this, "Xoa thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }
}
