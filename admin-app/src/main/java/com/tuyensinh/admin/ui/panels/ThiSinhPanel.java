package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import com.tuyensinh.util.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ThiSinhPanel extends JPanel {

    private MainFrame mainFrame;
    private ThiSinhService service = new ThiSinhService();
    private NguoiDungService nguoiDungService = new NguoiDungService();
    private DoiTuongService doituongService = new DoiTuongService();
    private KhuVucService khuvucService = new KhuVucService();

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JButton btnSearch, btnAdd, btnEdit, btnDelete, btnViewDiem;
    private JLabel lblTotal;
    private JSpinner spnPage;
    private int currentPage = 1;
    private final int pageSize = 20;

    public ThiSinhPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JLabel("Tim kiem (CCCD/Ho ten/SBD):"));
        txtSearch = new JTextField(20);
        txtSearch.addActionListener(e -> search());
        toolbar.add(txtSearch);

        btnSearch = new JButton("Tim kiem");
        btnSearch.addActionListener(e -> search());
        toolbar.add(btnSearch);

        toolbar.add(Box.createHorizontalStrut(20));
        btnAdd = new JButton("Them moi");
        btnAdd.addActionListener(e -> showAddDialog());
        toolbar.add(btnAdd);

        btnEdit = new JButton("Sua");
        btnEdit.addActionListener(e -> showEditDialog());
        toolbar.add(btnEdit);

        btnDelete = new JButton("Xoa");
        btnDelete.addActionListener(e -> deleteThiSinh());
        toolbar.add(btnDelete);

        btnViewDiem = new JButton("Xem diem");
        btnViewDiem.addActionListener(e -> showDiemDialog());
        toolbar.add(btnViewDiem);

        add(toolbar, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(
            new String[]{"ID", "So BD", "CCCD", "Ho", "Ten", "Ngay sinh", "GT", "Dien thoai", "Email", "DT UT", "KV UT"}, 0) {
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

        // Bottom
        JPanel bottom = new JPanel(new BorderLayout());
        lblTotal = new JLabel("Tong: 0 thi sinh");
        bottom.add(lblTotal, BorderLayout.WEST);

        JPanel paging = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        paging.add(new JLabel("Trang:"));
        spnPage = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        spnPage.addChangeListener(e -> {
            currentPage = (Integer) spnPage.getValue();
            loadData();
        });
        paging.add(spnPage);
        paging.add(new JButton("<<") {{ addActionListener(e -> { if (currentPage > 1) spnPage.setValue(--currentPage); }); }});
        paging.add(new JButton(">>") {{ addActionListener(e -> { int tp = getTotalPages(); if (currentPage < tp) spnPage.setValue(++currentPage); }); }});
        bottom.add(paging, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadData() {
        model.setRowCount(0);
        String keyword = txtSearch.getText().trim();
        List<ThiSinh> list;

        if (keyword.isEmpty()) {
            list = service.findByPage(currentPage, pageSize);
        } else {
            list = service.findByPageWithSearch(keyword, currentPage, pageSize);
        }

        for (ThiSinh ts : list) {
            model.addRow(new Object[]{
                ts.getThisinhId(),
                ts.getSobaodanh(),
                ts.getCccd(),
                ts.getHo(),
                ts.getTen(),
                ts.getNgaySinh() != null ? DateUtil.formatDateShort(ts.getNgaySinh()) : "",
                ts.getGioiTinh(),
                ts.getDienThoai(),
                ts.getEmail(),
                ts.getDoiTuongUutien() != null ? ts.getDoiTuongUutien().getMaDoituong() : "",
                ts.getKhuVucUutien() != null ? ts.getKhuVucUutien().getMaKhuvuc() : ""
            });
        }

        long total = service.countBySearch(keyword);
        lblTotal.setText("Tong: " + total + " thi sinh");
        int tp = getTotalPages();
        spnPage.setModel(new SpinnerNumberModel(currentPage, 1, Math.max(1, tp), 1));
    }

    private int getTotalPages() {
        return (int) Math.ceil((double) service.countBySearch(txtSearch.getText().trim()) / pageSize);
    }

    private void search() {
        currentPage = 1;
        loadData();
    }

    private ThiSinh getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Integer id = (Integer) model.getValueAt(row, 0);
        return service.findById(id);
    }

    private static void decorateDoiTuongCombo(JComboBox<DoiTuongUutien> cbo) {
        cbo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("");
                } else if (value instanceof DoiTuongUutien) {
                    setText(((DoiTuongUutien) value).getMaDoituong());
                }
                return this;
            }
        });
    }

    /**
     * Chon ngay sinh bang JSpinner (mui ten tang/giam), dinh dang hien thi dd/MM/yy.
     */
    private static JSpinner createNgaySinhSpinner(LocalDate initial) {
        Calendar min = Calendar.getInstance();
        min.set(1950, Calendar.JANUARY, 1, 0, 0, 0);
        min.set(Calendar.MILLISECOND, 0);
        // Khong dung "hom nay" lam max: du lieu sai (VD ngay sinh 2026) se lam vo SpinnerDateModel
        // (bat buoc start <= value <= end).
        Calendar max = Calendar.getInstance();
        max.set(2040, Calendar.DECEMBER, 31, 23, 59, 59);
        max.set(Calendar.MILLISECOND, 999);

        Date init;
        if (initial != null) {
            init = java.sql.Date.valueOf(initial);
        } else {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.YEAR, -18);
            init = c.getTime();
        }
        long tMin = min.getTimeInMillis();
        long tMax = max.getTimeInMillis();
        long tInit = init.getTime();
        if (tInit < tMin) {
            init = min.getTime();
        } else if (tInit > tMax) {
            init = max.getTime();
        }
        SpinnerDateModel model = new SpinnerDateModel(init, min.getTime(), max.getTime(), Calendar.DAY_OF_MONTH);
        JSpinner sp = new JSpinner(model);
        sp.setEditor(new JSpinner.DateEditor(sp, "dd/MM/yyyy"));
        Dimension dim = sp.getPreferredSize();
        sp.setPreferredSize(new Dimension(Math.max(120, dim.width), dim.height));
        return sp;
    }

    private static LocalDate localDateFromSpinner(JSpinner sp) {
        Date date = (Date) sp.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static void decorateKhuVucCombo(JComboBox<KhuVucUutien> cbo) {
        cbo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("");
                } else if (value instanceof KhuVucUutien) {
                    setText(((KhuVucUutien) value).getMaKhuvuc());
                }
                return this;
            }
        });
    }

    private void showAddDialog() {
        JTextField txtCccd = new JTextField(20);
        JTextField txtHo = new JTextField(20);
        JTextField txtTen = new JTextField(20);
        JTextField txtSbd = new JTextField(20);
        JTextField txtDt = new JTextField(20);
        JTextField txtEmail = new JTextField(20);
        JSpinner spnNs = createNgaySinhSpinner(null);
        JComboBox<String> cboGt = new JComboBox<>(new String[]{"", "Nam", "Nu"});
        JComboBox<DoiTuongUutien> cboDt = new JComboBox<>();
        JComboBox<KhuVucUutien> cboKv = new JComboBox<>();
        cboDt.addItem(null);
        cboKv.addItem(null);
        for (DoiTuongUutien d : doituongService.findAll()) cboDt.addItem(d);
        for (KhuVucUutien k : khuvucService.findAll()) cboKv.addItem(k);
        decorateDoiTuongCombo(cboDt);
        decorateKhuVucCombo(cboKv);

        Object[] msg = {
            "CCCD (*):", txtCccd,
            "So bao danh:", txtSbd,
            "Ho (*):", txtHo,
            "Ten (*):", txtTen,
            "Ngay sinh (dd/MM/yyyy):", spnNs,
            "Gioi tinh:", cboGt,
            "Dien thoai:", txtDt,
            "Email:", txtEmail,
            "Doi tuong UT:", cboDt,
            "Khu vuc UT:", cboKv
        };

        int result = JOptionPane.showConfirmDialog(this, msg, "Them thi sinh moi", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            if (txtCccd.getText().trim().isEmpty() || txtHo.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "CCCD, Ho, Ten la bat buoc!");
                return;
            }
            ThiSinh ts = new ThiSinh();
            ts.setCccd(txtCccd.getText().trim());
            ts.setSobaodanh(txtSbd.getText().trim().isEmpty() ? null : txtSbd.getText().trim());
            ts.setHo(txtHo.getText().trim());
            ts.setTen(txtTen.getText().trim());
            ts.setNgaySinh(localDateFromSpinner(spnNs));
            ts.setGioiTinh((String) cboGt.getSelectedItem());
            ts.setDienThoai(txtDt.getText().trim().isEmpty() ? null : txtDt.getText().trim());
            ts.setEmail(txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim());
            ts.setDoiTuongUutien((DoiTuongUutien) cboDt.getSelectedItem());
            ts.setKhuVucUutien((KhuVucUutien) cboKv.getSelectedItem());

            try {
                service.save(ts);
                JOptionPane.showMessageDialog(this, "Them thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }

    private void showEditDialog() {
        ThiSinh ts = getSelected();
        if (ts == null) {
            JOptionPane.showMessageDialog(this, "Chon thi sinh can sua!");
            return;
        }
        JTextField txtHo = new JTextField(ts.getHo());
        JTextField txtTen = new JTextField(ts.getTen());
        JTextField txtDt = new JTextField(ts.getDienThoai() != null ? ts.getDienThoai() : "");
        JTextField txtEmail = new JTextField(ts.getEmail() != null ? ts.getEmail() : "");
        JSpinner spnNs = createNgaySinhSpinner(ts.getNgaySinh());
        JComboBox<String> cboGt = new JComboBox<>(new String[]{"", "Nam", "Nu"});
        if (ts.getGioiTinh() != null) cboGt.setSelectedItem(ts.getGioiTinh());
        JComboBox<DoiTuongUutien> cboDt = new JComboBox<>();
        JComboBox<KhuVucUutien> cboKv = new JComboBox<>();
        cboDt.addItem(null); cboKv.addItem(null);
        for (DoiTuongUutien d : doituongService.findAll()) { cboDt.addItem(d); if (ts.getDoiTuongUutien() != null && d.getDoituongId().equals(ts.getDoiTuongUutien().getDoituongId())) cboDt.setSelectedItem(d); }
        for (KhuVucUutien k : khuvucService.findAll()) { cboKv.addItem(k); if (ts.getKhuVucUutien() != null && k.getKhuvucId().equals(ts.getKhuVucUutien().getKhuvucId())) cboKv.setSelectedItem(k); }
        decorateDoiTuongCombo(cboDt);
        decorateKhuVucCombo(cboKv);

        Object[] msg = {
            "CCCD: " + ts.getCccd() + " (khong doi)",
            "Ho:", txtHo,
            "Ten:", txtTen,
            "Ngay sinh (dd/MM/yyyy):", spnNs,
            "Gioi tinh:", cboGt,
            "Dien thoai:", txtDt,
            "Email:", txtEmail,
            "Doi tuong UT:", cboDt,
            "Khu vuc UT:", cboKv
        };

        int result = JOptionPane.showConfirmDialog(this, msg, "Sua thi sinh", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            ts.setHo(txtHo.getText().trim());
            ts.setTen(txtTen.getText().trim());
            ts.setNgaySinh(localDateFromSpinner(spnNs));
            ts.setGioiTinh((String) cboGt.getSelectedItem());
            ts.setDienThoai(txtDt.getText().trim().isEmpty() ? null : txtDt.getText().trim());
            ts.setEmail(txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim());
            ts.setDoiTuongUutien((DoiTuongUutien) cboDt.getSelectedItem());
            ts.setKhuVucUutien((KhuVucUutien) cboKv.getSelectedItem());
            try {
                service.update(ts);
                JOptionPane.showMessageDialog(this, "Cap nhat thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }

    private void deleteThiSinh() {
        ThiSinh ts = getSelected();
        if (ts == null) {
            JOptionPane.showMessageDialog(this, "Chon thi sinh can xoa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Ban co chac muon xoa thi sinh '" + ts.getHoVaTen() + "'?\nLuu y: Tat ca diem thi va nguyen vong cua ho se bi xoa!",
            "Xac nhan xoa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.delete(ts);
                JOptionPane.showMessageDialog(this, "Xoa thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }

    private void showDiemDialog() {
        ThiSinh ts = getSelected();
        if (ts == null) {
            JOptionPane.showMessageDialog(this, "Chon thi sinh!");
            return;
        }
        List<DiemThi> diemList = service.getDiemThiList(ts.getThisinhId());
        StringBuilder sb = new StringBuilder();
        sb.append("Thi sinh: ").append(ts.getHoVaTen()).append("\n");
        sb.append("CCCD: ").append(ts.getCccd()).append("\n\n");
        if (diemList.isEmpty()) {
            sb.append("Chua co diem thi nao.\n");
        } else {
            for (DiemThi dt : diemList) {
                sb.append("Phuong thuc: ").append(dt.getPhuongThuc().getTenPhuongthuc()).append("\n");
                sb.append("  Nam tuyen sinh: ").append(dt.getNamTuyensinh()).append("\n");
                if (dt.getDanhSachDiemChiTiet() != null) {
                    for (DiemThiChiTiet dct : dt.getDanhSachDiemChiTiet()) {
                        sb.append("  ").append(dct.getMon().getMaMon()).append(": ")
                          .append(dct.getDiemSudung() != null ? dct.getDiemSudung() : "N/A").append("\n");
                    }
                }
                sb.append("\n");
            }
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(450, 350));
        JOptionPane.showMessageDialog(this, sp, "Diem thi - " + ts.getHoVaTen(), JOptionPane.INFORMATION_MESSAGE);
    }
}
