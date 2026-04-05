package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.*;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;

/**
 * Quan ly nganh: form co nhieu dong (ma to hop + do lech), dong dau tien la to hop goc.
 */
public class NganhPanel extends BaseCrudPanel<Nganh> {

    private XetTuyenService service;

    public NganhPanel(MainFrame mainFrame) {
        super(mainFrame);
        service = new XetTuyenService();
        initUI();
        loadData();
    }

    @Override
    protected String[] getTableColumns() {
        return new String[]{"ID", "Mã Ngành", "Tên Ngành", "Tổ hợp gốc", "Chi tiêu", "Điểm sàn", "Điểm TT", "Active"};
    }

    @Override
    protected Nganh getSelectedEntity() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return service.findNganhById(getSelectedId());
    }

    @Override
    protected Integer getSelectedId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (Integer) model.getValueAt(row, 0);
    }

    @Override
    public String getPageTitle() {
        return UIConstants.PAGE_NGANH;
    }

    @Override
    protected void configureTableColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(70);
    }

    @Override
    protected void buildBottomBar() {
        totalLabel = new JLabel("Tong: 0 nganh");
        totalLabel.setFont(UIConstants.FONT_SMALL);
        add(totalLabel, BorderLayout.SOUTH);
    }

    @Override
    public void loadData() {
        model.setRowCount(0);
        String kw = searchTextField.getText().trim();
        var list = kw.isEmpty() ? service.findAllNganh() : service.searchNganh(kw);
        for (Nganh n : list) {
            ToHop goc = n.getToHopGoc();
            String maGoc = goc != null ? goc.getMaTohop() : "";
            model.addRow(new Object[]{
                n.getNganhId(),
                n.getMaNganh(),
                n.getTenNganh(),
                maGoc,
                n.getChiTieu(),
                n.getDiemSan(),
                n.getDiemTrungTuyen(),
                n.getIsActive() ? "Active" : "Inactive"
            });
        }
        updateTotalLabel(list.size(), "nganh");
    }

    @Override
    protected String getEntityDisplayName(Nganh n) {
        return n.getTenNganh();
    }

    @Override
    protected void deleteEntity(Nganh n) throws Exception {
        service.deleteNganh(n);
    }

    @Override
    protected void showAddDialog() {
        showNganhEditor(null);
    }

    @Override
    protected void showEditDialog() {
        Nganh n = getSelectedEntity();
        if (n == null) { showSelectRow(); return; }
        n = service.findNganhById(n.getNganhId());
        showNganhEditor(n);
    }

    private static void decorateToHopCombo(JComboBox<ToHop> cbo) {
        cbo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ToHop) {
                    ToHop th = (ToHop) value;
                    String ten = th.getTenTohop() != null ? th.getTenTohop() : "";
                    setText(th.getMaTohop() + " — " + ten);
                }
                return this;
            }
        });
    }


    private void showNganhEditor(Nganh existing) {
        boolean edit = existing != null;

        List<ToHop> allToHop = service.findAllToHop();
        JComboBox<ToHop> cboToHopGoc = new JComboBox<>();
        cboToHopGoc.addItem(null);
        for (ToHop th : allToHop) cboToHopGoc.addItem(th);
        decorateToHopCombo(cboToHopGoc);

        JTextField txtMa = new JTextField(20);
        JTextField txtTen = new JTextField(20);
        JTextField txtChiTieu = new JTextField("100", 20);
        JTextField txtDiemSan = new JTextField(20);
        JTextField txtDiemTT = new JTextField(20);
        JCheckBox chkActive = new JCheckBox("Active", true);

        if (edit) {
            txtMa.setText(existing.getMaNganh());
            txtMa.setEditable(false);
            txtTen.setText(existing.getTenNganh());
            txtChiTieu.setText(existing.getChiTieu() != null ? String.valueOf(existing.getChiTieu()) : "");
            txtDiemSan.setText(existing.getDiemSan() != null ? existing.getDiemSan().toPlainString() : "");
            txtDiemTT.setText(existing.getDiemTrungTuyen() != null ? existing.getDiemTrungTuyen().toPlainString() : "");
            chkActive.setSelected(Boolean.TRUE.equals(existing.getIsActive()));
            if (existing.getToHopGoc() != null) {
                cboToHopGoc.setSelectedItem(existing.getToHopGoc());
            }
        }

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.gridy = 0;

        if (!edit) {
            form.add(new JLabel("Ma nganh (*):"), gc);
            gc.gridx = 1;
            form.add(txtMa, gc);
            gc.gridy++;
        } else {
            form.add(new JLabel("Ma nganh:"), gc);
            gc.gridx = 1;
            form.add(new JLabel(existing.getMaNganh()), gc);
            gc.gridy++;
        }

        gc.gridx = 0;
        form.add(new JLabel("Ten nganh (*):"), gc);
        gc.gridx = 1;
        form.add(txtTen, gc);
        gc.gridy++;

        gc.gridx = 0;
        form.add(new JLabel("To hop goc:"), gc);
        gc.gridx = 1;
        form.add(cboToHopGoc, gc);
        gc.gridy++;

        gc.gridx = 0;
        form.add(new JLabel("Chi tieu:"), gc);
        gc.gridx = 1;
        form.add(txtChiTieu, gc);
        gc.gridy++;

        gc.gridx = 0;
        form.add(new JLabel("Diem san:"), gc);
        gc.gridx = 1;
        form.add(txtDiemSan, gc);
        gc.gridy++;

        gc.gridx = 0;
        form.add(new JLabel("Diem trung tuyen:"), gc);
        gc.gridx = 1;
        form.add(txtDiemTT, gc);
        gc.gridy++;

        gc.gridx = 0;
        form.add(new JLabel("Active:"), gc);
        gc.gridx = 1;
        form.add(chkActive, gc);

        int r = JOptionPane.showConfirmDialog(
                this,
                form,
                edit ? "Sua nganh" : "Them nganh moi",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (r != JOptionPane.OK_OPTION) return;

        if (!edit && (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty())) {
            showMessage(this, "Ma va Ten nganh la bat buoc!");
            return;
        }
        if (edit && txtTen.getText().trim().isEmpty()) {
            showMessage(this, "Ten nganh la bat buoc!");
            return;
        }

        try {
            if (edit) {
                existing.setTenNganh(txtTen.getText().trim());
                existing.setToHopGoc((ToHop) cboToHopGoc.getSelectedItem());
                existing.setChiTieu(parseInt(txtChiTieu.getText()));
                existing.setDiemSan(parseBigDecimal(txtDiemSan.getText()));
                existing.setDiemTrungTuyen(parseBigDecimal(txtDiemTT.getText()));
                existing.setIsActive(chkActive.isSelected());
                service.updateNganh(existing);
                showSuccess(this, "Cap nhat thanh cong!");
            } else {
                Nganh n = new Nganh();
                n.setMaNganh(txtMa.getText().trim());
                n.setTenNganh(txtTen.getText().trim());
                n.setToHopGoc((ToHop) cboToHopGoc.getSelectedItem());
                n.setChiTieu(parseInt(txtChiTieu.getText()));
                n.setDiemSan(parseBigDecimal(txtDiemSan.getText()));
                n.setDiemTrungTuyen(parseBigDecimal(txtDiemTT.getText()));
                n.setIsActive(chkActive.isSelected());
                service.saveNganh(n);
                showSuccess(this, "Them thanh cong!");
            }
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    @Override
    protected void buildToolbar() {
        JTextField[] searchFieldOut = new JTextField[1];
        JPanel toolbar = ToolbarFactory.createSearchToolbar(
                searchFieldOut,
                this::doSearch,
                new ToolbarFactory.ActionButton("Them moi", this::showAddDialog),
                new ToolbarFactory.ActionButton("Sua", this::showEditDialog),
                new ToolbarFactory.ActionButton("Xoa", this::doDelete),
                new ToolbarFactory.ActionButton("Import Excel", this::importDanhMucExcel)
        );
        this.searchTextField = searchFieldOut[0];
        add(toolbar, BorderLayout.NORTH);
    }

    private void importDanhMucExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chon thu muc chua 3 file Excel danh muc");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        int choose = chooser.showOpenDialog(this);
        if (choose != JFileChooser.APPROVE_OPTION) return;

        File dir = chooser.getSelectedFile();
        if (dir == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Se import lai danh muc tu thu muc:\n" + dir.getAbsolutePath() + "\n\n"
                        + "Luu y:\n"
                        + "- Chuc nang nay se xoa du lieu cu trong cac bang danh muc xet tuyen\n"
                        + "- xt_tohop, xt_tohop_mon, xt_nganh, xt_nganh_tohop, xt_nganh_phuongthuc\n"
                        + "- dong thoi xoa ca du lieu lien quan phat sinh nhu nguyen vong / diem cong de tranh loi FK\n\n"
                        + "Ban co muon tiep tuc khong?",
                "Xac nhan import danh muc",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            XetTuyenDanhMucImporter.ImportResult result =
                    new XetTuyenDanhMucImporter().importFromDirectory(dir);

            loadData();
            showSuccess(this, result.toHumanMessage());
        } catch (Exception ex) {
            showError(this, getRootMessage(ex));
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private String getRootMessage(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }

        String msg = root.getMessage();
        if (msg == null || msg.trim().isEmpty()) {
            msg = ex.getMessage();
        }
        if (msg == null || msg.trim().isEmpty()) {
            msg = "Khong the import danh muc.";
        }
        return msg;
    }
}
