package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.*;
import com.tuyensinh.entity.MaXetTuyenMap;
import com.tuyensinh.entity.Nganh;
import com.tuyensinh.entity.NganhToHop;
import com.tuyensinh.entity.PhuongThuc;
import com.tuyensinh.entity.ToHop;
import com.tuyensinh.service.MaXetTuyenMapService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class MaXetTuyenPanel extends BaseCrudPanel<MaXetTuyenMap> {

    private final MaXetTuyenMapService service;

    public MaXetTuyenPanel(MainFrame mainFrame) {
        super(mainFrame);
        this.service = new MaXetTuyenMapService();
        initUI();
        loadData();
    }

    @Override
    public String getPageTitle() {
        return "Quan ly ma xet tuyen";
    }

    @Override
    protected String[] getTableColumns() {
        return new String[]{
                "ID", "Ma XT", "Ten chuong trinh", "Ma nganh", "Ten nganh",
                "Phuong thuc", "To hop", "TH nguon", "Active"
        };
    }

    @Override
    protected MaXetTuyenMap getSelectedEntity() {
        Integer id = getSelectedId();
        return id == null ? null : service.findById(id);
    }

    @Override
    protected Integer getSelectedId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (Integer) model.getValueAt(row, 0);
    }

    @Override
    protected void configureTableColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(240);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(220);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(80);
        table.getColumnModel().getColumn(8).setPreferredWidth(70);
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
                new ToolbarFactory.ActionButton("Import Excel", this::importExcel)
        );
        this.searchTextField = searchFieldOut[0];
        add(toolbar, BorderLayout.NORTH);
    }

    @Override
    protected void buildBottomBar() {
        totalLabel = new JLabel("Tong: 0 ma xet tuyen");
        totalLabel.setFont(UIConstants.FONT_SMALL);
        add(totalLabel, BorderLayout.SOUTH);
    }

    @Override
    public void loadData() {
        model.setRowCount(0);

        String kw = searchTextField != null ? searchTextField.getText().trim() : "";
        List<MaXetTuyenMap> list = kw.isEmpty() ? service.findAll() : service.search(kw);

        for (MaXetTuyenMap x : list) {
            String maNganh = x.getNganh() != null ? x.getNganh().getMaNganh() : "";
            String tenNganh = x.getNganh() != null ? x.getNganh().getTenNganh() : "";
            String maPt = x.getPhuongThuc() != null ? x.getPhuongThuc().getMaPhuongthuc() : "";
            String maToHop = "";
            if (x.getNganhToHop() != null && x.getNganhToHop().getToHop() != null) {
                maToHop = x.getNganhToHop().getToHop().getMaTohop();
            }

            model.addRow(new Object[]{
                    x.getMaXettuyenId(),
                    x.getMaXetTuyen(),
                    x.getTenChuongTrinh(),
                    maNganh,
                    tenNganh,
                    maPt,
                    maToHop,
                    x.getMaTohopNguon(),
                    Boolean.TRUE.equals(x.getIsActive()) ? "Active" : "Inactive"
            });
        }

        updateTotalLabel(list.size(), "ma xet tuyen");
    }

    @Override
    protected String getEntityDisplayName(MaXetTuyenMap entity) {
        return entity.getMaXetTuyen();
    }

    @Override
    protected void deleteEntity(MaXetTuyenMap entity) throws Exception {
        service.delete(entity);
    }

    @Override
    protected void showAddDialog() {
        showEditor(null);
    }

    @Override
    protected void showEditDialog() {
        MaXetTuyenMap selected = getSelectedEntity();
        if (selected == null) {
            showSelectRow();
            return;
        }
        showEditor(service.findById(selected.getMaXettuyenId()));
    }

    private void showEditor(MaXetTuyenMap existing) {
        boolean edit = existing != null;

        JTextField txtMaXt = new JTextField(22);
        JTextField txtTenCt = new JTextField(22);
        JTextField txtMaToHopNguon = new JTextField(12);
        JTextField txtGhiChu = new JTextField(22);
        JCheckBox chkActive = new JCheckBox("Active", true);

        JComboBox<Nganh> cboNganh = new JComboBox<>();
        for (Nganh n : service.findAllNganh()) {
            cboNganh.addItem(n);
        }
        cboNganh.setRenderer(createNganhRenderer());

        JComboBox<PhuongThuc> cboPhuongThuc = new JComboBox<>();
        for (PhuongThuc p : service.findAllPhuongThuc()) {
            cboPhuongThuc.addItem(p);
        }
        cboPhuongThuc.setRenderer(createPhuongThucRenderer());

        JComboBox<NganhToHop> cboNganhToHop = new JComboBox<>();
        cboNganhToHop.setRenderer(createNganhToHopRenderer());

        Runnable reloadNganhToHop = () -> {
            Nganh selectedNganh = (Nganh) cboNganh.getSelectedItem();
            NganhToHop old = (NganhToHop) cboNganhToHop.getSelectedItem();

            cboNganhToHop.removeAllItems();
            cboNganhToHop.addItem(null);

            if (selectedNganh != null) {
                List<NganhToHop> links = service.findNganhToHopByNganh(selectedNganh.getNganhId());
                for (NganhToHop nt : links) {
                    cboNganhToHop.addItem(nt);
                }
            }

            if (old != null) {
                for (int i = 0; i < cboNganhToHop.getItemCount(); i++) {
                    NganhToHop item = cboNganhToHop.getItemAt(i);
                    if (item != null && old.getNganhTohopId().equals(item.getNganhTohopId())) {
                        cboNganhToHop.setSelectedIndex(i);
                        break;
                    }
                }
            }
        };

        cboNganh.addActionListener(e -> {
            reloadNganhToHop.run();
            NganhToHop nt = (NganhToHop) cboNganhToHop.getSelectedItem();
            if (nt != null && nt.getToHop() != null) {
                txtMaToHopNguon.setText(nt.getToHop().getMaTohop());
            }
        });

        cboNganhToHop.addActionListener(e -> {
            NganhToHop nt = (NganhToHop) cboNganhToHop.getSelectedItem();
            if (nt != null && nt.getToHop() != null && (txtMaToHopNguon.getText() == null || txtMaToHopNguon.getText().trim().isEmpty())) {
                txtMaToHopNguon.setText(nt.getToHop().getMaTohop());
            }
        });

        if (edit) {
            txtMaXt.setText(existing.getMaXetTuyen());
            txtMaXt.setEditable(false);
            txtTenCt.setText(existing.getTenChuongTrinh() != null ? existing.getTenChuongTrinh() : "");
            txtMaToHopNguon.setText(existing.getMaTohopNguon() != null ? existing.getMaTohopNguon() : "");
            txtGhiChu.setText(existing.getGhiChu() != null ? existing.getGhiChu() : "");
            chkActive.setSelected(Boolean.TRUE.equals(existing.getIsActive()));

            if (existing.getNganh() != null) {
                selectNganh(cboNganh, existing.getNganh().getNganhId());
            }
            if (existing.getPhuongThuc() != null) {
                selectPhuongThuc(cboPhuongThuc, existing.getPhuongThuc().getPhuongthucId());
            }

            reloadNganhToHop.run();

            if (existing.getNganhToHop() != null) {
                selectNganhToHop(cboNganhToHop, existing.getNganhToHop().getNganhTohopId());
            }
        } else {
            reloadNganhToHop.run();
        }

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.gridx = 0;
        gc.gridy = 0;

        form.add(new JLabel("Ma xet tuyen (*):"), gc);
        gc.gridx = 1;
        form.add(txtMaXt, gc);
        gc.gridy++;

        gc.gridx = 0;
        form.add(new JLabel("Ten chuong trinh:"), gc);
        gc.gridx = 1;
        form.add(txtTenCt, gc);
        gc.gridy++;

        gc.gridx = 0;
        form.add(new JLabel("Nganh (*):"), gc);
        gc.gridx = 1;
        form.add(cboNganh, gc);
        gc.gridy++;

        gc.gridx = 0;
        form.add(new JLabel("Phuong thuc (*):"), gc);
        gc.gridx = 1;
        form.add(cboPhuongThuc, gc);
        gc.gridy++;

        gc.gridx = 0;
        form.add(new JLabel("Nganh - To hop:"), gc);
        gc.gridx = 1;
        form.add(cboNganhToHop, gc);
        gc.gridy++;

        gc.gridx = 0;
        form.add(new JLabel("Ma to hop nguon:"), gc);
        gc.gridx = 1;
        form.add(txtMaToHopNguon, gc);
        gc.gridy++;

        gc.gridx = 0;
        form.add(new JLabel("Ghi chu:"), gc);
        gc.gridx = 1;
        form.add(txtGhiChu, gc);
        gc.gridy++;

        gc.gridx = 0;
        form.add(new JLabel("Trang thai:"), gc);
        gc.gridx = 1;
        form.add(chkActive, gc);

        int r = JOptionPane.showConfirmDialog(
                this,
                form,
                edit ? "Sua ma xet tuyen" : "Them ma xet tuyen",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (r != JOptionPane.OK_OPTION) return;

        String maXt = upperTrim(txtMaXt.getText());
        if (maXt.isEmpty()) {
            showMessage(this, "Ma xet tuyen la bat buoc!");
            return;
        }

        Nganh nganh = (Nganh) cboNganh.getSelectedItem();
        if (nganh == null) {
            showMessage(this, "Nganh la bat buoc!");
            return;
        }

        PhuongThuc pt = (PhuongThuc) cboPhuongThuc.getSelectedItem();
        if (pt == null) {
            showMessage(this, "Phuong thuc la bat buoc!");
            return;
        }

        NganhToHop nganhToHop = (NganhToHop) cboNganhToHop.getSelectedItem();
        String maToHopNguon = upperTrim(txtMaToHopNguon.getText());

        if (maToHopNguon.isEmpty() && nganhToHop != null && nganhToHop.getToHop() != null) {
            maToHopNguon = upperTrim(nganhToHop.getToHop().getMaTohop());
        }

        try {
            if (!edit) {
                if (service.findExact(maXt, pt.getPhuongthucId(), maToHopNguon).isPresent()) {
                    showMessage(this, "Combo ma xet tuyen + phuong thuc + to hop nguon da ton tai!");
                    return;
                }

                MaXetTuyenMap x = new MaXetTuyenMap();
                x.setMaXetTuyen(maXt);
                x.setTenChuongTrinh(emptyToNull(txtTenCt.getText()));
                x.setNganh(nganh);
                x.setPhuongThuc(pt);
                x.setNganhToHop(nganhToHop);
                x.setMaTohopNguon(maToHopNguon);
                x.setGhiChu(emptyToNull(txtGhiChu.getText()));
                x.setIsActive(chkActive.isSelected());

                service.save(x);
                showSuccess(this, "Them thanh cong!");
            } else {
                String oldUniqueMaXt = existing.getMaXetTuyen();
                Short oldPtId = existing.getPhuongThuc() != null ? existing.getPhuongThuc().getPhuongthucId() : null;
                String oldToHop = existing.getMaTohopNguon();

                boolean uniqueChanged = !oldUniqueMaXt.equalsIgnoreCase(maXt)
                        || oldPtId == null || !oldPtId.equals(pt.getPhuongthucId())
                        || !nullSafeUpper(oldToHop).equals(nullSafeUpper(maToHopNguon));

                if (uniqueChanged && service.findExact(maXt, pt.getPhuongthucId(), maToHopNguon).isPresent()) {
                    showMessage(this, "Combo ma xet tuyen + phuong thuc + to hop nguon da ton tai!");
                    return;
                }

                existing.setMaXetTuyen(maXt);
                existing.setTenChuongTrinh(emptyToNull(txtTenCt.getText()));
                existing.setNganh(nganh);
                existing.setPhuongThuc(pt);
                existing.setNganhToHop(nganhToHop);
                existing.setMaTohopNguon(maToHopNguon);
                existing.setGhiChu(emptyToNull(txtGhiChu.getText()));
                existing.setIsActive(chkActive.isSelected());

                service.update(existing);
                showSuccess(this, "Cap nhat thanh cong!");
            }

            loadData();
        } catch (Exception ex) {
            showError(this, getRootMessage(ex));
        }
    }

    private void importExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chon file Excel ma xet tuyen");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (file == null) return;

        Object[] options = {"Upsert", "Xoa het + import lai", "Huy"};
        int mode = JOptionPane.showOptionDialog(
                this,
                "Chon che do import cho file:\n" + file.getAbsolutePath() + "\n\n"
                        + "Upsert: neu trung combo thi cap nhat, chua co thi them moi.\n"
                        + "Xoa het + import lai: xoa toan bo xt_ma_xettuyen roi import lai tu file.",
                "Import ma xet tuyen",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]
        );

        if (mode == 2 || mode == JOptionPane.CLOSED_OPTION) return;

        boolean replaceAll = mode == 1;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            MaXetTuyenExcelImporter.ImportResult importResult =
                    new MaXetTuyenExcelImporter().importFromExcel(file, replaceAll);

            loadData();
            showSuccess(this, importResult.toHumanMessage());
        } catch (Exception ex) {
            showError(this, getRootMessage(ex));
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private ListCellRenderer<? super Nganh> createNganhRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Nganh) {
                    Nganh n = (Nganh) value;
                    setText(n.getMaNganh() + " - " + n.getTenNganh());
                }
                return this;
            }
        };
    }

    private ListCellRenderer<? super PhuongThuc> createPhuongThucRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof PhuongThuc) {
                    PhuongThuc p = (PhuongThuc) value;
                    setText(p.getMaPhuongthuc() + " - " + p.getTenPhuongthuc());
                }
                return this;
            }
        };
    }

    private ListCellRenderer<? super NganhToHop> createNganhToHopRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("(Khong gan nganh-to-hop)");
                } else if (value instanceof NganhToHop) {
                    NganhToHop nt = (NganhToHop) value;
                    ToHop th = nt.getToHop();
                    setText(th != null ? th.getMaTohop() : "(Khong ro to hop)");
                }
                return this;
            }
        };
    }

    private void selectNganh(JComboBox<Nganh> cbo, Integer nganhId) {
        if (nganhId == null) return;
        for (int i = 0; i < cbo.getItemCount(); i++) {
            Nganh n = cbo.getItemAt(i);
            if (n != null && nganhId.equals(n.getNganhId())) {
                cbo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectPhuongThuc(JComboBox<PhuongThuc> cbo, Short phuongThucId) {
        if (phuongThucId == null) return;
        for (int i = 0; i < cbo.getItemCount(); i++) {
            PhuongThuc p = cbo.getItemAt(i);
            if (p != null && phuongThucId.equals(p.getPhuongthucId())) {
                cbo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectNganhToHop(JComboBox<NganhToHop> cbo, Integer nganhToHopId) {
        if (nganhToHopId == null) return;
        for (int i = 0; i < cbo.getItemCount(); i++) {
            NganhToHop nt = cbo.getItemAt(i);
            if (nt != null && nganhToHopId.equals(nt.getNganhTohopId())) {
                cbo.setSelectedIndex(i);
                return;
            }
        }
    }

    private String upperTrim(String s) {
        return s == null ? "" : s.trim().toUpperCase();
    }

    private String emptyToNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }

    private String nullSafeUpper(String s) {
        return s == null ? "" : s.trim().toUpperCase();
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
            msg = "Co loi xay ra.";
        }
        return msg;
    }
}