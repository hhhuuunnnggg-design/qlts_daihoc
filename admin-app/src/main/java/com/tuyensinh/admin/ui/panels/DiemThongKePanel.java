package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.PhuongThuc;
import com.tuyensinh.service.DiemThiService;
import com.tuyensinh.dao.PhuongThucDao;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class DiemThongKePanel extends JPanel {

    private MainFrame mainFrame;
    private DiemThiService diemThiService = new DiemThiService();
    private PhuongThucDao phuongThucDao = new PhuongThucDao();

    private JTable table;
    private DefaultTableModel model;
    private JComboBox<PhuongThuc> cboPhuongThuc;
    private JButton btnThongKe;

    public DiemThongKePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
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
        toolbar.add(cboPhuongThuc);
        btnThongKe = new JButton("Thong ke");
        btnThongKe.addActionListener(e -> thongKe());
        toolbar.add(btnThongKe);
        add(toolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(
            new String[]{"Ma mon", "Ten mon", "Diem TB", "Diem Min", "Diem Max", "So luong"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(25);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 12));
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Summary panel
        JPanel summary = new JPanel(new GridLayout(1, 4, 10, 10));
        summary.setBorder(BorderFactory.createTitledBorder("Tong quan"));
        summary.add(new JLabel("  Chon phuong thuc va nhan 'Thong ke' de xem chi tiet"));
        add(summary, BorderLayout.SOUTH);
    }

    private void thongKe() {
        PhuongThuc pt = (PhuongThuc) cboPhuongThuc.getSelectedItem();
        if (pt == null) {
            JOptionPane.showMessageDialog(this, "Chon phuong thuc!");
            return;
        }

        model.setRowCount(0);
        List<Object[]> rows = diemThiService.thongKeDiemTheoMon(pt.getPhuongthucId());

        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chua co du lieu diem cho phuong thuc nay!");
            return;
        }

        for (Object[] row : rows) {
            String maMon = row[0] != null ? row[0].toString() : "";
            String tenMon = row[1] != null ? row[1].toString() : "";
            String avg = row[2] != null ? String.format("%.2f", (Double) row[2]) : "-";
            String min = row[3] != null ? String.format("%.2f", ((Number) row[3]).doubleValue()) : "-";
            String max = row[4] != null ? String.format("%.2f", ((Number) row[4]).doubleValue()) : "-";
            String count = row[5] != null ? row[5].toString() : "0";
            model.addRow(new Object[]{maMon, tenMon, avg, min, max, count});
        }
    }
}
