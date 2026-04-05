package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.BaseCrudPanel;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.admin.ui.UIConstants;
import com.tuyensinh.entity.Nganh;
import com.tuyensinh.entity.NganhToHop;
import com.tuyensinh.entity.ToHop;
import com.tuyensinh.service.NganhToHopService;
import com.tuyensinh.service.XetTuyenService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class NganhToHopPanel extends BaseCrudPanel<NganhToHop> {

    private final XetTuyenService xetTuyenService;
    private final NganhToHopService service;

    public NganhToHopPanel(MainFrame mainFrame) {
        super(mainFrame);
        this.xetTuyenService = new XetTuyenService();
        this.service = new NganhToHopService();
        initCrudUI();
        loadData();
    }

    @Override
    public String getPageTitle() {
        return UIConstants.PAGE_NGANH_TO_HOP;
    }

    @Override
    protected String[] getTableColumns() {
        return new String[]{
                "ID", "Ma nganh", "Ten nganh", "Ma to hop", "Ten to hop", "Do lech"
        };
    }

    @Override
    protected Integer getSelectedId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (Integer) model.getValueAt(row, 0);
    }

    @Override
    protected NganhToHop getSelectedEntity() {
        Integer id = getSelectedId();
        return id == null ? null : service.findById(id);
    }

    @Override
    protected void configureTableColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(240);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(240);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
    }

    @Override
    protected void buildBottomBar() {
        totalLabel = new JLabel("Tong: 0 lien ket");
        totalLabel.setFont(UIConstants.FONT_SMALL);
        add(totalLabel, BorderLayout.SOUTH);
    }

    @Override
    public void loadData() {
        model.setRowCount(0);

        String kw = searchTextField != null ? searchTextField.getText().trim().toLowerCase() : "";
        List<NganhToHop> list = service.findAll();

        int count = 0;
        for (NganhToHop nt : list) {
            if (!matchKeyword(nt, kw)) continue;

            model.addRow(new Object[]{
                    nt.getNganhTohopId(),
                    nt.getNganh() != null ? safe(nt.getNganh().getMaNganh()) : "",
                    nt.getNganh() != null ? safe(nt.getNganh().getTenNganh()) : "",
                    nt.getToHop() != null ? safe(nt.getToHop().getMaTohop()) : "",
                    nt.getToHop() != null ? safe(nt.getToHop().getTenTohop()) : "",
                    nt.getDoLech() != null ? nt.getDoLech() : BigDecimal.ZERO
            });
            count++;
        }

        updateTotalLabel(count, "lien ket");
    }

    @Override
    protected String getEntityDisplayName(NganhToHop entity) {
        String maNganh = entity.getNganh() != null ? safe(entity.getNganh().getMaNganh()) : "";
        String maToHop = entity.getToHop() != null ? safe(entity.getToHop().getMaTohop()) : "";
        return maNganh + " - " + maToHop;
    }

    @Override
    protected void deleteEntity(NganhToHop entity) {
        service.delete(entity);
    }

    @Override
    protected void showAddDialog() {
        showEditor(null);
    }

    @Override
    protected void showEditDialog() {
        NganhToHop nt = getSelectedEntity();
        if (nt == null) {
            showSelectRow();
            return;
        }
        showEditor(nt);
    }

    private void showEditor(NganhToHop existing) {
        boolean edit = existing != null;

        List<Nganh> dsNganh = xetTuyenService.findAllNganh();
        List<ToHop> dsToHop = xetTuyenService.findAllToHop();

        JComboBox<Nganh> cboNganh = new JComboBox<>();
        for (Nganh n : dsNganh) cboNganh.addItem(n);
        decorateNganhCombo(cboNganh);

        JComboBox<ToHop> cboToHop = new JComboBox<>();
        for (ToHop th : dsToHop) cboToHop.addItem(th);
        decorateToHopCombo(cboToHop);

        JTextField txtDoLech = new JTextField(12);

        if (edit) {
            selectNganh(cboNganh, existing.getNganh());
            selectToHop(cboToHop, existing.getToHop());
            txtDoLech.setText(existing.getDoLech() != null ? existing.getDoLech().toPlainString() : "0");
        } else {
            txtDoLech.setText("0");
        }

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.gridy = 0;

        if (edit) {
            form.add(new JLabel("ID:"), gc);
            gc.gridx = 1;
            form.add(new JLabel(String.valueOf(existing.getNganhTohopId())), gc);
            gc.gridy++;
        }

        gc.gridx = 0;
        form.add(new JLabel("Nganh (*):"), gc);
        gc.gridx = 1;
        form.add(cboNganh, gc);
        gc.gridy++;

        gc.gridx = 0;
        form.add(new JLabel("To hop (*):"), gc);
        gc.gridx = 1;
        form.add(cboToHop, gc);
        gc.gridy++;

        gc.gridx = 0;
        form.add(new JLabel("Do lech:"), gc);
        gc.gridx = 1;
        form.add(txtDoLech, gc);

        int r = JOptionPane.showConfirmDialog(
                this,
                form,
                edit ? "Sua lien ket nganh - to hop" : "Them lien ket nganh - to hop",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (r != JOptionPane.OK_OPTION) return;

        Nganh nganh = (Nganh) cboNganh.getSelectedItem();
        ToHop toHop = (ToHop) cboToHop.getSelectedItem();

        if (nganh == null || toHop == null) {
            showMessage(this, "Hay chon day du nganh va to hop!");
            return;
        }

        BigDecimal doLech = parseBigDecimal(txtDoLech.getText());
        if (doLech == null) doLech = BigDecimal.ZERO;

        try {
            if (edit) {
                existing.setNganh(nganh);
                existing.setToHop(toHop);
                existing.setDoLech(doLech);
                service.update(existing);
                showSuccess(this, "Cap nhat thanh cong!");
            } else {
                NganhToHop nt = new NganhToHop();
                nt.setNganh(nganh);
                nt.setToHop(toHop);
                nt.setDoLech(doLech);
                service.save(nt);
                showSuccess(this, "Them thanh cong!");
            }
            loadData();
        } catch (Exception ex) {
            showError(this, getRootMessage(ex));
        }
    }

    private boolean matchKeyword(NganhToHop nt, String kw) {
        if (kw == null || kw.isEmpty()) return true;

        String maNganh = nt.getNganh() != null ? safe(nt.getNganh().getMaNganh()).toLowerCase() : "";
        String tenNganh = nt.getNganh() != null ? safe(nt.getNganh().getTenNganh()).toLowerCase() : "";
        String maToHop = nt.getToHop() != null ? safe(nt.getToHop().getMaTohop()).toLowerCase() : "";
        String tenToHop = nt.getToHop() != null ? safe(nt.getToHop().getTenTohop()).toLowerCase() : "";

        return maNganh.contains(kw)
                || tenNganh.contains(kw)
                || maToHop.contains(kw)
                || tenToHop.contains(kw);
    }

    private void decorateNganhCombo(JComboBox<Nganh> cbo) {
        cbo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Nganh) {
                    Nganh n = (Nganh) value;
                    setText(safe(n.getMaNganh()) + " - " + safe(n.getTenNganh()));
                }
                return this;
            }
        });
    }

    private void decorateToHopCombo(JComboBox<ToHop> cbo) {
        cbo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ToHop) {
                    ToHop th = (ToHop) value;
                    setText(safe(th.getMaTohop()) + " - " + safe(th.getTenTohop()));
                }
                return this;
            }
        });
    }

    private void selectNganh(JComboBox<Nganh> cbo, Nganh selected) {
        if (selected == null || selected.getNganhId() == null) return;
        for (int i = 0; i < cbo.getItemCount(); i++) {
            Nganh item = cbo.getItemAt(i);
            if (item != null && selected.getNganhId().equals(item.getNganhId())) {
                cbo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectToHop(JComboBox<ToHop> cbo, ToHop selected) {
        if (selected == null || selected.getTohopId() == null) return;
        for (int i = 0; i < cbo.getItemCount(); i++) {
            ToHop item = cbo.getItemAt(i);
            if (item != null && selected.getTohopId().equals(item.getTohopId())) {
                cbo.setSelectedIndex(i);
                return;
            }
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
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
            msg = "Khong the thuc hien thao tac.";
        }
        return msg;
    }
}