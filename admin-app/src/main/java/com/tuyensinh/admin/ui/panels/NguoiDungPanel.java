package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import com.tuyensinh.util.PasswordUtil;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class NguoiDungPanel extends JPanel {

    private NguoiDungService service = new NguoiDungService();
    private VaiTroService vaiTroService = new VaiTroService();

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JButton btnSearch, btnAdd, btnEdit, btnDelete, btnToggleActive, btnChangePass;
    private JComboBox<VaiTro> cboVaiTro;
    private JLabel lblTotal;
    private JSpinner spnPage;
    private int currentPage = 1;
    private final int pageSize = 20;

    public NguoiDungPanel(MainFrame mainFrame) {
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JLabel("Tim kiem:"));
        txtSearch = new JTextField(20);
        txtSearch.addActionListener(e -> search());
        toolbar.add(txtSearch);

        btnSearch = new JButton("Tim kiem");
        btnSearch.addActionListener(e -> search());
        toolbar.add(btnSearch);

        toolbar.add(new JLabel("  Vai tro:"));
        cboVaiTro = new JComboBox<>();
        cboVaiTro.addItem(null);
        for (VaiTro vt : vaiTroService.findAllVaiTro()) {
            cboVaiTro.addItem(vt);
        }
        cboVaiTro.addActionListener(e -> loadData());
        toolbar.add(cboVaiTro);

        toolbar.add(Box.createHorizontalStrut(20));

        btnAdd = new JButton("Them moi");
        btnAdd.addActionListener(e -> showAddDialog());
        toolbar.add(btnAdd);

        btnEdit = new JButton("Sua");
        btnEdit.addActionListener(e -> showEditDialog());
        toolbar.add(btnEdit);

        btnDelete = new JButton("Xoa");
        btnDelete.addActionListener(e -> deleteUser());
        toolbar.add(btnDelete);

        btnToggleActive = new JButton("Khoa/Mo");
        btnToggleActive.addActionListener(e -> toggleActive());
        toolbar.add(btnToggleActive);

        btnChangePass = new JButton("Doi mat khau");
        btnChangePass.addActionListener(e -> showChangePassDialog());
        toolbar.add(btnChangePass);

        add(toolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(
            new String[]{"ID", "Username", "Ho ten", "Email", "Vai tro", "Trang thai", "Ngay tao"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(130);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 12));
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        lblTotal = new JLabel("Tong: 0 nguoi dung");
        lblTotal.setFont(new Font("Arial", Font.PLAIN, 12));
        bottom.add(lblTotal, BorderLayout.WEST);

        JPanel paging = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        paging.add(new JLabel("Trang:"));
        spnPage = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        spnPage.addChangeListener(e -> {
            currentPage = (Integer) spnPage.getValue();
            loadData();
        });
        paging.add(spnPage);

        JButton btnPrev = new JButton("<<");
        btnPrev.addActionListener(e -> {
            if (currentPage > 1) spnPage.setValue(--currentPage);
        });
        paging.add(btnPrev);

        JButton btnNext = new JButton(">>");
        btnNext.addActionListener(e -> {
            int totalPages = getTotalPages();
            if (currentPage < totalPages) spnPage.setValue(++currentPage);
        });
        paging.add(btnNext);

        bottom.add(paging, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadData() {
        model.setRowCount(0);
        VaiTro selectedVaiTro = (VaiTro) cboVaiTro.getSelectedItem();
        String keyword = txtSearch.getText().trim();

        List<NguoiDung> list;
        if (keyword.isEmpty()) {
            if (selectedVaiTro != null) {
                list = service.findByRole(selectedVaiTro.getVaitroId());
            } else {
                list = service.findByPage(currentPage, pageSize);
            }
        } else {
            list = service.search(keyword);
        }

        for (NguoiDung nd : list) {
            model.addRow(new Object[]{
                nd.getNguoidungId(),
                nd.getUsername(),
                nd.getHoTen(),
                nd.getEmail(),
                nd.getVaiTro() != null ? nd.getVaiTro().getTenVaitro() : "",
                nd.getIsActive() ? "Active" : "Khoa",
                nd.getCreatedAt() != null ? nd.getCreatedAt().toString().substring(0, 10) : ""
            });
        }

        long total = service.countAll();
        lblTotal.setText("Tong: " + total + " nguoi dung");
        int totalPages = (int) Math.ceil((double) total / pageSize);
        spnPage.setModel(new SpinnerNumberModel(currentPage, 1, Math.max(1, totalPages), 1));
    }

    private void search() {
        currentPage = 1;
        loadData();
    }

    private int getTotalPages() {
        return (int) Math.ceil((double) service.countAll() / pageSize);
    }

    private NguoiDung getSelectedUser() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Integer id = (Integer) model.getValueAt(row, 0);
        return service.findById(id);
    }

    private void showAddDialog() {
        JTextField txtUser = new JTextField(20);
        JPasswordField txtPass = new JPasswordField(20);
        JTextField txtHoTen = new JTextField(20);
        JTextField txtEmail = new JTextField(20);
        JComboBox<VaiTro> cboVT = new JComboBox<>();
        for (VaiTro vt : vaiTroService.findAllVaiTro()) {
            cboVT.addItem(vt);
        }

        Object[] msg = {
            "Username:", txtUser,
            "Password:", txtPass,
            "Ho ten:", txtHoTen,
            "Email:", txtEmail,
            "Vai tro:", cboVT
        };

        int result = JOptionPane.showConfirmDialog(this, msg, "Them nguoi dung moi", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String username = txtUser.getText().trim();
            String password = new String(txtPass.getPassword());
            String hoTen = txtHoTen.getText().trim();
            String email = txtEmail.getText().trim();
            VaiTro vt = (VaiTro) cboVT.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username va password khong duoc trong!");
                return;
            }
            if (vt == null) {
                JOptionPane.showMessageDialog(this, "Chon vai tro!");
                return;
            }

            NguoiDung nd = new NguoiDung();
            nd.setUsername(username);
            nd.setPasswordHash(PasswordUtil.hashPassword(password));
            nd.setHoTen(hoTen.isEmpty() ? null : hoTen);
            nd.setEmail(email.isEmpty() ? null : email);
            nd.setVaiTro(vt);
            nd.setIsActive(true);

            try {
                service.save(nd);
                JOptionPane.showMessageDialog(this, "Them thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }

    private void showEditDialog() {
        NguoiDung nd = getSelectedUser();
        if (nd == null) {
            JOptionPane.showMessageDialog(this, "Chon nguoi dung can sua!");
            return;
        }

        JTextField txtHoTen = new JTextField(nd.getHoTen() != null ? nd.getHoTen() : "");
        JTextField txtEmail = new JTextField(nd.getEmail() != null ? nd.getEmail() : "");
        JComboBox<VaiTro> cboVT = new JComboBox<>();
        for (VaiTro vt : vaiTroService.findAllVaiTro()) {
            cboVT.addItem(vt);
            if (vt.getVaitroId().equals(nd.getVaiTro().getVaitroId())) {
                cboVT.setSelectedItem(vt);
            }
        }

        Object[] msg = {
            "Username: " + nd.getUsername() + " (khong doi)",
            "Ho ten:", txtHoTen,
            "Email:", txtEmail,
            "Vai tro:", cboVT
        };

        int result = JOptionPane.showConfirmDialog(this, msg, "Sua nguoi dung", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            nd.setHoTen(txtHoTen.getText().trim().isEmpty() ? null : txtHoTen.getText().trim());
            nd.setEmail(txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim());
            nd.setVaiTro((VaiTro) cboVT.getSelectedItem());
            try {
                service.update(nd);
                JOptionPane.showMessageDialog(this, "Cap nhat thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }

    private void deleteUser() {
        NguoiDung nd = getSelectedUser();
        if (nd == null) {
            JOptionPane.showMessageDialog(this, "Chon nguoi dung can xoa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Ban co chac muon xoa nguoi dung '" + nd.getUsername() + "'?",
            "Xac nhan xoa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.delete(nd);
                JOptionPane.showMessageDialog(this, "Xoa thanh cong!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }

    private void toggleActive() {
        NguoiDung nd = getSelectedUser();
        if (nd == null) {
            JOptionPane.showMessageDialog(this, "Chon nguoi dung!");
            return;
        }
        try {
            service.toggleActive(nd);
            loadData();
            JOptionPane.showMessageDialog(this,
                nd.getIsActive() ? "Da khoa tai khoan!" : "Da mo tai khoan!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
        }
    }

    private void showChangePassDialog() {
        NguoiDung nd = getSelectedUser();
        if (nd == null) {
            JOptionPane.showMessageDialog(this, "Chon nguoi dung!");
            return;
        }
        JPasswordField txtNewPass = new JPasswordField(20);
        JPasswordField txtConfirm = new JPasswordField(20);
        Object[] msg = {
            "Username: " + nd.getUsername(),
            "Mat khau moi:", txtNewPass,
            "Xac nhan mat khau:", txtConfirm
        };
        int result = JOptionPane.showConfirmDialog(this, msg, "Doi mat khau", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String np = new String(txtNewPass.getPassword());
            String nc = new String(txtConfirm.getPassword());
            if (!np.equals(nc)) {
                JOptionPane.showMessageDialog(this, "Mat khau xac nhan khong khop!");
                return;
            }
            if (np.length() < 6) {
                JOptionPane.showMessageDialog(this, "Mat khau phai it nhat 6 ky tu!");
                return;
            }
            try {
                service.updatePassword(nd, np);
                JOptionPane.showMessageDialog(this, "Doi mat khau thanh cong!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
            }
        }
    }
}
