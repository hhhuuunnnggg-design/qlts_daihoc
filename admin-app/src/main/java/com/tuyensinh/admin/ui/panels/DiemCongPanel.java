package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class DiemCongPanel extends JPanel {

    private MainFrame mainFrame;
    private DiemCongService diemCongService = new DiemCongService();
    private ThiSinhService thiSinhService = new ThiSinhService();
    private XetTuyenService xetTuyenService = new XetTuyenService();
    private NganhToHopService nganhToHopService = new NganhToHopService();
    private PhuongThucDao phuongThucDao = new PhuongThucDao();

    private JTable table;
    private DefaultTableModel model;
    private JButton btnAdd, btnEdit, btnDelete;
    private JLabel lblTotal;

    public DiemCongPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JLabel("Quan ly diem cong (diem uu tien, diem chung chi)"));
        toolbar.add(Box.createHorizontalStrut(20));
        btnAdd = new JButton("Them moi");
        btnAdd.addActionListener(e -> showAddDialog());
        toolbar.add(btnAdd);
        btnEdit = new JButton("Sua");
        btnEdit.addActionListener(e -> showEditDialog());
        toolbar.add(btnEdit);
        btnDelete = new JButton("Xoa");
        btnDelete.addActionListener(e -> deleteDiemCong());
        toolbar.add(btnDelete);
        add(toolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(
            new String[]{"ID", "CCCD", "Ho Ten", "Nganh", "To Hop", "Ph. thuc", "Diem CC", "Diem UT", "Diem Tong"}, 0) {
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
        List<DiemCong> list = diemCongService.findAll();
        for (DiemCong dc : list) {
            ThiSinh ts = dc.getThiSinh();
            model.addRow(new Object[]{
                dc.getDiemcongId(),
                ts != null ? ts.getCccd() : "",
                ts != null ? ts.getHoVaTen() : "",
                dc.getNganhToHop() != null ? dc.getNganhToHop().getNganh().getMaNganh() : "",
                dc.getNganhToHop() != null ? dc.getNganhToHop().getToHop().getMaTohop() : "",
                dc.getPhuongThuc() != null ? dc.getPhuongThuc().getMaPhuongthuc() : "",
                dc.getDiemChungchi(),
                dc.getDiemUutienXt(),
                dc.getDiemTong()
            });
        }
        lblTotal.setText("Tong: " + list.size() + " ban ghi");
    }

    private DiemCong getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Integer id = (Integer) model.getValueAt(row, 0);
        return diemCongService.findById(id);
    }

    private void showAddDialog() {
        JTextField txtCccd = new JTextField(20);
        JComboBox<NganhToHop> cboNt = new JComboBox<>();
        JComboBox<PhuongThuc> cboPt = new JComboBox<>();
        for (NganhToHop nt : nganhToHopService.findAll()) cboNt.addItem(nt);
        for (PhuongThuc pt : phuongThucDao.findAll()) cboPt.addItem(pt);
        JTextField txtDiemCC = new JTextField("0", 20);
        JTextField txtDiemUT = new JTextField("0", 20);

        Object[] msg = {
            "CCCD thi sinh:", txtCccd,
            "Nganh - To hop:", cboNt,
            "Phuong thuc:", cboPt,
            "Diem chung chi:", txtDiemCC,
            "Diem uu tien:", txtDiemUT
        };

        int result = JOptionPane.showConfirmDialog(this, msg, "Them diem cong", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String cccd = txtCccd.getText().trim();
            if (cccd.isEmpty()) {
                JOptionPane.showMessageDialog(this, "CCCD la bat buoc!");
                return;
            }
            Optional<ThiSinh> optTs = thiSinhService.findByCccd(cccd);
            if (optTs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Khong tim thay thi sinh!");
                return;
            }
            NganhToHop nt = (NganhToHop) cboNt.getSelectedItem();
            PhuongThuc pt = (PhuongThuc) cboPt.getSelectedItem();
            if (nt == null || pt == null) {
                JOptionPane.showMessageDialog(this, "Chon day du thong tin!");
                return;
            }

            DiemCong dc = new DiemCong();
            dc.setThiSinh(optTs.get());
            dc.setNganhToHop(nt);
            dc.setPhuongThuc(pt);
            try { dc.setDiemChungchi(new java.math.BigDecimal(txtDiemCC.getText().trim())); } catch (Exception ex) {}
            try { dc.setDiemUutienXt(new java.math.BigDecimal(txtDiemUT.getText().trim())); } catch (Exception ex) {}
            try {
                java.math.BigDecimal cc = dc.getDiemChungchi() != null ? dc.getDiemChungchi() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal ut = dc.getDiemUutienXt() != null ? dc.getDiemUutienXt() : java.math.BigDecimal.ZERO;
                dc.setDiemTong(cc.add(ut));
            } catch (Exception ex) {}

            try {
                diemCongService.save(dc);
                JOptionPane.showMessageDialog(this, "Them thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }

    private void showEditDialog() {
        DiemCong dc = getSelected();
        if (dc == null) {
            JOptionPane.showMessageDialog(this, "Chon ban ghi can sua!");
            return;
        }
        JTextField txtDiemCC = new JTextField(dc.getDiemChungchi() != null ? dc.getDiemChungchi().toString() : "0", 20);
        JTextField txtDiemUT = new JTextField(dc.getDiemUutienXt() != null ? dc.getDiemUutienXt().toString() : "0", 20);
        Object[] msg = {
            "Thi sinh: " + (dc.getThiSinh() != null ? dc.getThiSinh().getHoVaTen() : ""),
            "Diem chung chi:", txtDiemCC,
            "Diem uu tien:", txtDiemUT
        };
        int result = JOptionPane.showConfirmDialog(this, msg, "Sua diem cong", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try { dc.setDiemChungchi(new java.math.BigDecimal(txtDiemCC.getText().trim())); } catch (Exception ex) {}
            try { dc.setDiemUutienXt(new java.math.BigDecimal(txtDiemUT.getText().trim())); } catch (Exception ex) {}
            try {
                java.math.BigDecimal cc = dc.getDiemChungchi() != null ? dc.getDiemChungchi() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal ut = dc.getDiemUutienXt() != null ? dc.getDiemUutienXt() : java.math.BigDecimal.ZERO;
                dc.setDiemTong(cc.add(ut));
            } catch (Exception ex) {}
            try {
                diemCongService.update(dc);
                JOptionPane.showMessageDialog(this, "Cap nhat thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }

    private void deleteDiemCong() {
        DiemCong dc = getSelected();
        if (dc == null) {
            JOptionPane.showMessageDialog(this, "Chon ban ghi can xoa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Ban co chac xoa ban ghi nay?", "Xac nhan", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                diemCongService.delete(dc);
                JOptionPane.showMessageDialog(this, "Xoa thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }
}
