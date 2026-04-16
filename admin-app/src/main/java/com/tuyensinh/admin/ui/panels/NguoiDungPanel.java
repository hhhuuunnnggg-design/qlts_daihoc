package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.*;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import com.tuyensinh.util.PasswordUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * Refactored: extend BaseCrudPanel, use TableFactory + ToolbarFactory.
 * Bo sung phan trang that cho panel nguoi dung.
 */
public class NguoiDungPanel extends BaseCrudPanel<NguoiDung> {

    private final NguoiDungService service;
    private final VaiTroService vaiTroService;

    private JComboBox<VaiTro> vaiTroFilter;
    private JButton btnToggleActive, btnChangePass;

    public NguoiDungPanel(MainFrame mainFrame) {
        super(mainFrame);
        service = new NguoiDungService();
        vaiTroService = new VaiTroService();
        usePagination = true;
        pageSize = 20;
        initUI();
        loadData();
    }

    @Override
    protected String[] getTableColumns() {
        return new String[]{"ID", "Username", "Ho ten", "Email", "Vai tro", "Trang thai", "Ngay tao"};
    }

    @Override
    protected NguoiDung getSelectedEntity() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Integer id = (Integer) model.getValueAt(row, 0);
        return service.findById(id);
    }

    @Override
    protected Integer getSelectedId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (Integer) model.getValueAt(row, 0);
    }

    @Override
    public String getPageTitle() {
        return UIConstants.PAGE_NGUOI_DUNG;
    }

    @Override
    protected void buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        toolbar.add(new JLabel("Tim kiem:"));
        searchTextField = new JTextField(20);
        searchTextField.addActionListener(e -> doSearch());
        toolbar.add(searchTextField);

        JButton btnSearch = new JButton("Tim kiem");
        btnSearch.addActionListener(e -> doSearch());
        toolbar.add(btnSearch);

        toolbar.add(new JLabel("  Vai tro:"));
        vaiTroFilter = new JComboBox<>();
        vaiTroFilter.addItem(null);
        for (VaiTro vt : vaiTroService.findAllVaiTro()) {
            vaiTroFilter.addItem(vt);
        }
        vaiTroFilter.addActionListener(e -> {
            currentPage = 1;
            loadData();
        });
        toolbar.add(vaiTroFilter);

        toolbar.add(Box.createHorizontalStrut(16));

        JButton btnAdd = new JButton("Them moi");
        btnAdd.addActionListener(e -> showAddDialog());
        toolbar.add(btnAdd);

        JButton btnEdit = new JButton("Sua");
        btnEdit.addActionListener(e -> showEditDialog());
        toolbar.add(btnEdit);

        JButton btnDelete = new JButton("Xoa");
        btnDelete.addActionListener(e -> doDelete());
        toolbar.add(btnDelete);

        btnToggleActive = new JButton("Khoa/Mo");
        btnToggleActive.addActionListener(e -> toggleActive());
        toolbar.add(btnToggleActive);

        btnChangePass = new JButton("Doi mat khau");
        btnChangePass.addActionListener(e -> showChangePassDialog());
        toolbar.add(btnChangePass);

        add(toolbar, BorderLayout.NORTH);
    }

    @Override
    protected void configureTableColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(130);
    }

    @Override
    protected void buildBottomBar() {
        totalLabel = new JLabel("Tong: 0 nguoi dung");
        totalLabel.setFont(UIConstants.FONT_SMALL);

        pageSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
        JPanel paging = ToolbarFactory.createPagingPanel(pageSpinner, () -> {
            currentPage = (Integer) pageSpinner.getValue();
            loadData();
        });

        add(ToolbarFactory.createBottomBar(totalLabel, paging), BorderLayout.SOUTH);
    }

    @Override
    protected void doSearch() {
        currentPage = 1;
        loadData();
    }

    @Override
    public void loadData() {
        model.setRowCount(0);

        VaiTro selected = (VaiTro) vaiTroFilter.getSelectedItem();
        String keyword = searchTextField.getText().trim();

        List<NguoiDung> list;
        long total;

        if (!keyword.isEmpty()) {
            List<NguoiDung> raw = service.search(keyword);
            total = raw.size();
            list = paginate(raw, currentPage, pageSize);
        } else if (selected != null) {
            List<NguoiDung> raw = service.findByRole(selected.getVaitroId());
            total = raw.size();
            list = paginate(raw, currentPage, pageSize);
        } else {
            total = service.countAll();
            int totalPages = service.getTotalPages(total, pageSize);
            if (currentPage > totalPages) currentPage = Math.max(1, totalPages);
            list = service.findByPage(currentPage, pageSize);
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

        updateTotalLabel(total, "nguoi dung");

        if (pageSpinner != null) {
            ToolbarFactory.updatePagingSpinner(pageSpinner, currentPage, (int) total, pageSize);
        }
    }

    private List<NguoiDung> paginate(List<NguoiDung> raw, int page, int size) {
        if (raw == null || raw.isEmpty()) {
            currentPage = 1;
            return Collections.emptyList();
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) raw.size() / size));
        if (page > totalPages) {
            currentPage = totalPages;
        } else if (page < 1) {
            currentPage = 1;
        }

        int from = (currentPage - 1) * size;
        int to = Math.min(from + size, raw.size());
        if (from >= raw.size()) return Collections.emptyList();
        return raw.subList(from, to);
    }

    @Override
    protected String getEntityDisplayName(NguoiDung nd) {
        return nd.getUsername();
    }

    @Override
    protected void deleteEntity(NguoiDung nd) throws Exception {
        service.delete(nd);
    }

    @Override
    protected void showAddDialog() {
        JTextField txtUser = new JTextField(20);
        JPasswordField txtPass = new JPasswordField(20);
        JTextField txtHoTen = new JTextField(20);
        JTextField txtEmail = new JTextField(20);
        JComboBox<VaiTro> cboVT = new JComboBox<>();
        for (VaiTro vt : vaiTroService.findAllVaiTro()) cboVT.addItem(vt);

        Object[] msg = {
                "Username:", txtUser,
                "Password:", txtPass,
                "Ho ten:", txtHoTen,
                "Email:", txtEmail,
                "Vai tro:", cboVT
        };

        int r = JOptionPane.showConfirmDialog(this, msg, "Them nguoi dung moi", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        String username = txtUser.getText().trim();
        String password = new String(txtPass.getPassword());
        VaiTro vt = (VaiTro) cboVT.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage(this, "Username va password khong duoc trong!");
            return;
        }
        if (vt == null) {
            showMessage(this, "Chon vai tro!");
            return;
        }

        NguoiDung nd = new NguoiDung();
        nd.setUsername(username);
        nd.setPasswordHash(PasswordUtil.hashPassword(password));
        nd.setHoTen(txtHoTen.getText().trim().isEmpty() ? null : txtHoTen.getText().trim());
        nd.setEmail(txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim());
        nd.setVaiTro(vt);
        nd.setIsActive(true);

        try {
            service.save(nd);
            showSuccess(this, "Them thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    @Override
    protected void showEditDialog() {
        NguoiDung nd = getSelectedEntity();
        if (nd == null) {
            showSelectRow();
            return;
        }

        JTextField txtHoTen = new JTextField(nd.getHoTen() != null ? nd.getHoTen() : "");
        JTextField txtEmail = new JTextField(nd.getEmail() != null ? nd.getEmail() : "");
        JComboBox<VaiTro> cboVT = new JComboBox<>();
        for (VaiTro vt : vaiTroService.findAllVaiTro()) {
            cboVT.addItem(vt);
            if (nd.getVaiTro() != null && vt.getVaitroId().equals(nd.getVaiTro().getVaitroId())) {
                cboVT.setSelectedItem(vt);
            }
        }

        int r = JOptionPane.showConfirmDialog(this, new Object[]{
                "Username: " + nd.getUsername() + " (khong doi)",
                "Ho ten:", txtHoTen,
                "Email:", txtEmail,
                "Vai tro:", cboVT
        }, "Sua nguoi dung", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        nd.setHoTen(txtHoTen.getText().trim().isEmpty() ? null : txtHoTen.getText().trim());
        nd.setEmail(txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim());
        nd.setVaiTro((VaiTro) cboVT.getSelectedItem());

        try {
            service.update(nd);
            showSuccess(this, "Cap nhat thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private void toggleActive() {
        NguoiDung nd = getSelectedEntity();
        if (nd == null) {
            showSelectRow();
            return;
        }
        try {
            boolean wasActive = Boolean.TRUE.equals(nd.getIsActive());
            service.toggleActive(nd);
            loadData();
            showMessage(this, wasActive ? "Da khoa tai khoan!" : "Da mo tai khoan!");
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private void showChangePassDialog() {
        NguoiDung nd = getSelectedEntity();
        if (nd == null) {
            showSelectRow();
            return;
        }

        JPasswordField txtNew = new JPasswordField(20);
        JPasswordField txtConfirm = new JPasswordField(20);

        int r = JOptionPane.showConfirmDialog(this, new Object[]{
                "Username: " + nd.getUsername(),
                "Mat khau moi:", txtNew,
                "Xac nhan mat khau:", txtConfirm
        }, "Doi mat khau", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        String np = new String(txtNew.getPassword());
        String nc = new String(txtConfirm.getPassword());
        if (!np.equals(nc)) {
            showMessage(this, "Mat khau xac nhan khong khop!");
            return;
        }
        if (np.length() < 6) {
            showMessage(this, "Mat khau phai it nhat 6 ky tu!");
            return;
        }

        try {
            service.updatePassword(nd, np);
            showSuccess(this, "Doi mat khau thanh cong!");
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }
}