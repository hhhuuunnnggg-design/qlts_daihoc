package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import com.tuyensinh.dao.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class BangQuyDoiPanel extends JPanel {

    private MainFrame mainFrame;
    private BangQuyDoiService service = new BangQuyDoiService();
    private PhuongThucDao phuongThucDao = new PhuongThucDao();
    private ToHopDao toHopDao = new ToHopDao();
    private MonDao monDao = new MonDao();

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JButton btnAdd, btnEdit, btnDelete;
    private JLabel lblTotal;

    public BangQuyDoiPanel(MainFrame mainFrame) {
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
        toolbar.add(Box.createHorizontalStrut(20));
        btnAdd = new JButton("Them moi");
        btnAdd.addActionListener(e -> showAddDialog());
        toolbar.add(btnAdd);
        btnEdit = new JButton("Sua");
        btnEdit.addActionListener(e -> showEditDialog());
        toolbar.add(btnEdit);
        btnDelete = new JButton("Xoa");
        btnDelete.addActionListener(e -> deleteBqd());
        toolbar.add(btnDelete);
        add(toolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(
            new String[]{"ID", "Ma QD", "Ph. thuc", "To hop", "Mon", "Diem A", "Diem B", "Diem QD A", "Diem QD B", "Phan vi"}, 0) {
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
        String kw = txtSearch.getText().trim();
        List<BangQuyDoi> list;
        if (kw.isEmpty()) {
            list = service.findAll();
        } else {
            list = service.search(kw);
        }
        for (BangQuyDoi bqd : list) {
            model.addRow(new Object[]{
                bqd.getBangquydoiId(),
                bqd.getMaQuydoi(),
                bqd.getPhuongThuc() != null ? bqd.getPhuongThuc().getMaPhuongthuc() : "",
                bqd.getToHop() != null ? bqd.getToHop().getMaTohop() : "",
                bqd.getMon() != null ? bqd.getMon().getMaMon() : "",
                bqd.getDiemTu(),
                bqd.getDiemDen(),
                bqd.getDiemQuydoiTu(),
                bqd.getDiemQuydoiDen(),
                bqd.getPhanVi()
            });
        }
        lblTotal.setText("Tong: " + list.size() + " ban ghi");
    }

    private BangQuyDoi getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Integer id = (Integer) model.getValueAt(row, 0);
        return service.findById(id);
    }

    private void showAddDialog() {
        JTextField txtMa = new JTextField(20);
        JComboBox<PhuongThuc> cboPt = new JComboBox<>();
        JComboBox<ToHop> cboTh = new JComboBox<>();
        JComboBox<Mon> cboMon = new JComboBox<>();
        cboTh.addItem(null);
        cboMon.addItem(null);
        for (PhuongThuc pt : phuongThucDao.findAll()) cboPt.addItem(pt);
        for (ToHop th : toHopDao.findAll()) cboTh.addItem(th);
        for (Mon m : monDao.findAll()) cboMon.addItem(m);
        JTextField txtTu = new JTextField("0", 10);
        JTextField txtDen = new JTextField("30", 10);
        JTextField txtQdTu = new JTextField("0", 10);
        JTextField txtQdDen = new JTextField("30", 10);

        Object[] msg = {
            "Ma quy doi (*):", txtMa,
            "Phuong thuc (*):", cboPt,
            "To hop:", cboTh,
            "Mon:", cboMon,
            "Diem tu:", txtTu, "Den:", txtDen,
            "Diem quy doi tu:", txtQdTu, "Den:", txtQdDen
        };

        int result = JOptionPane.showConfirmDialog(this, msg, "Them bang quy doi", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            if (txtMa.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ma quy doi la bat buoc!");
                return;
            }
            BangQuyDoi bqd = new BangQuyDoi();
            bqd.setMaQuydoi(txtMa.getText().trim());
            bqd.setPhuongThuc((PhuongThuc) cboPt.getSelectedItem());
            bqd.setToHop((ToHop) cboTh.getSelectedItem());
            bqd.setMon((Mon) cboMon.getSelectedItem());
            try { bqd.setDiemTu(new BigDecimal(txtTu.getText().trim())); } catch (Exception ex) {}
            try { bqd.setDiemDen(new BigDecimal(txtDen.getText().trim())); } catch (Exception ex) {}
            try { bqd.setDiemQuydoiTu(new BigDecimal(txtQdTu.getText().trim())); } catch (Exception ex) {}
            try { bqd.setDiemQuydoiDen(new BigDecimal(txtQdDen.getText().trim())); } catch (Exception ex) {}
            try {
                service.save(bqd);
                JOptionPane.showMessageDialog(this, "Them thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }

    private void showEditDialog() {
        BangQuyDoi bqd = getSelected();
        if (bqd == null) {
            JOptionPane.showMessageDialog(this, "Chon ban ghi can sua!");
            return;
        }
        JTextField txtTu = new JTextField(bqd.getDiemTu() != null ? bqd.getDiemTu().toString() : "0", 10);
        JTextField txtDen = new JTextField(bqd.getDiemDen() != null ? bqd.getDiemDen().toString() : "30", 10);
        JTextField txtQdTu = new JTextField(bqd.getDiemQuydoiTu() != null ? bqd.getDiemQuydoiTu().toString() : "0", 10);
        JTextField txtQdDen = new JTextField(bqd.getDiemQuydoiDen() != null ? bqd.getDiemQuydoiDen().toString() : "30", 10);
        Object[] msg = {
            "Ma: " + bqd.getMaQuydoi(),
            "Diem tu:", txtTu, "Den:", txtDen,
            "Diem quy doi tu:", txtQdTu, "Den:", txtQdDen
        };
        int result = JOptionPane.showConfirmDialog(this, msg, "Sua bang quy doi", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try { bqd.setDiemTu(new BigDecimal(txtTu.getText().trim())); } catch (Exception ex) {}
            try { bqd.setDiemDen(new BigDecimal(txtDen.getText().trim())); } catch (Exception ex) {}
            try { bqd.setDiemQuydoiTu(new BigDecimal(txtQdTu.getText().trim())); } catch (Exception ex) {}
            try { bqd.setDiemQuydoiDen(new BigDecimal(txtQdDen.getText().trim())); } catch (Exception ex) {}
            try {
                service.update(bqd);
                JOptionPane.showMessageDialog(this, "Cap nhat thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }

    private void deleteBqd() {
        BangQuyDoi bqd = getSelected();
        if (bqd == null) {
            JOptionPane.showMessageDialog(this, "Chon ban ghi can xoa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Ban co sach xoa ban ghi nay?", "Xac nhan", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.delete(bqd);
                JOptionPane.showMessageDialog(this, "Xoa thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }
}
