package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.BasePanel;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.admin.ui.TableFactory;
import com.tuyensinh.admin.ui.UIConstants;
import com.tuyensinh.entity.NguyenVong;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.service.NguyenVongService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class NguyenVongPanel extends BasePanel {

    private final NguyenVongService service;
    private JTable table;
    private DefaultTableModel model;
    private JLabel lblTotal;

    public NguyenVongPanel(MainFrame mainFrame) {
        super(mainFrame);
        this.service = new NguyenVongService();
        initUI();
        loadData();
    }

    @Override
    public String getPageTitle() {
        return UIConstants.PAGE_NGUYEN_VONG;
    }

    @Override
    protected void initUI() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.add(new JLabel("Quan ly nguyen vong xet tuyen"));
        toolbar.add(Box.createHorizontalStrut(20));

        JButton btnImport = new JButton("Import Excel");
        btnImport.addActionListener(e -> showImportDialog());
        toolbar.add(btnImport);

        JButton btnDelete = new JButton("Xoa");
        btnDelete.addActionListener(e -> deleteNv());
        toolbar.add(btnDelete);

        add(toolbar, BorderLayout.NORTH);

        model = TableFactory.newReadOnlyModel(
                "ID", "CCCD", "Ho Ten", "Nganh", "To Hop", "Ph. thuc", "Ma XT", "Thu tu", "Diem XT", "Ket qua"
        );
        table = TableFactory.create(model);
        add(TableFactory.wrap(table), BorderLayout.CENTER);

        lblTotal = new JLabel("Tong: 0");
        lblTotal.setFont(UIConstants.FONT_SMALL);
        add(lblTotal, BorderLayout.SOUTH);
    }

    @Override
    public void loadData() {
        model.setRowCount(0);

        List<NguyenVong> list = service.findAll();
        for (NguyenVong nv : list) {
            ThiSinh ts = nv.getThiSinh();

            model.addRow(new Object[]{
                    nv.getNguyenvongId(),
                    ts != null ? ts.getCccd() : "",
                    ts != null ? ts.getHoVaTen() : "",
                    nv.getNganh() != null ? nv.getNganh().getMaNganh() : "",
                    nv.getNganhToHop() != null && nv.getNganhToHop().getToHop() != null
                            ? nv.getNganhToHop().getToHop().getMaTohop() : "",
                    nv.getPhuongThuc() != null ? nv.getPhuongThuc().getMaPhuongthuc() : "",
                    nv.getMaXetTuyenMap() != null ? nv.getMaXetTuyenMap().getMaXetTuyen() : "",
                    nv.getThuTu(),
                    nv.getDiemXettuyen() != null ? nv.getDiemXettuyen() : "",
                    nv.getKetQua()
            });
        }

        lblTotal.setText("Tong: " + list.size());
    }

    private NguyenVong getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return service.findById((Integer) model.getValueAt(row, 0));
    }

    private void showImportDialog() {
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Import nguyen vong",
                true
        );
        dialog.setContentPane(new NguyenVongImportPanel(mainFrame));
        dialog.setSize(920, 680);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        loadData();
    }

    private void deleteNv() {
        NguyenVong nv = getSelected();
        if (nv == null) {
            showSelectRow(this);
            return;
        }

        String name = nv.getThiSinh() != null
                ? nv.getThiSinh().getHoVaTen()
                : String.valueOf(nv.getNguyenvongId());

        if (confirmDelete(this, name) != JOptionPane.YES_OPTION) return;

        try {
            service.delete(nv);
            showSuccess(this, "Xoa thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }
}