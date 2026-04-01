package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class NguyenVongPanel extends JPanel {

    private NguyenVongService nguyenVongService = new NguyenVongService();

    private JTable table;
    private DefaultTableModel model;
    private JButton btnDelete;
    private JLabel lblTotal;

    public NguyenVongPanel(MainFrame mainFrame) {
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JLabel("Quan ly nguyen vong xet tuyen"));
        toolbar.add(Box.createHorizontalStrut(20));
        btnDelete = new JButton("Xoa");
        btnDelete.addActionListener(e -> deleteNv());
        toolbar.add(btnDelete);
        add(toolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(
            new String[]{"ID", "CCCD", "Ho Ten", "Nganh", "To Hop", "Ph. thuc", "Thu tu", "Diem XT", "Ket qua"}, 0) {
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
        List<NguyenVong> list = nguyenVongService.findAll();
        for (NguyenVong nv : list) {
            ThiSinh ts = nv.getThiSinh();
            model.addRow(new Object[]{
                nv.getNguyenvongId(),
                ts != null ? ts.getCccd() : "",
                ts != null ? ts.getHoVaTen() : "",
                nv.getNganh() != null ? nv.getNganh().getMaNganh() : "",
                nv.getNganhToHop() != null && nv.getNganhToHop().getToHop() != null ? nv.getNganhToHop().getToHop().getMaTohop() : "",
                nv.getPhuongThuc() != null ? nv.getPhuongThuc().getMaPhuongthuc() : "",
                nv.getThuTu(),
                nv.getDiemXettuyen() != null ? nv.getDiemXettuyen() : "",
                nv.getKetQua()
            });
        }
        lblTotal.setText("Tong: " + list.size() + " nguyen vong");
    }

    private NguyenVong getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Integer id = (Integer) model.getValueAt(row, 0);
        return nguyenVongService.findById(id);
    }

    private void deleteNv() {
        NguyenVong nv = getSelected();
        if (nv == null) {
            JOptionPane.showMessageDialog(this, "Chon nguyen vong can xoa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Ban co chac xoa nguyen vong nay?", "Xac nhan", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                nguyenVongService.delete(nv);
                JOptionPane.showMessageDialog(this, "Xoa thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }
}
