package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.BaseCrudPanel;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.admin.ui.UIConstants;
import com.tuyensinh.entity.Mon;
import com.tuyensinh.entity.Nganh;
import com.tuyensinh.entity.NganhToHop;
import com.tuyensinh.entity.NganhToHopMon;
import com.tuyensinh.service.MonService;
import com.tuyensinh.service.NganhToHopMonService;
import com.tuyensinh.service.NganhToHopService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class NganhToHopMonPanel extends BaseCrudPanel<NganhToHopMon> {

    private final NganhToHopMonService service;
    private final NganhToHopService nganhToHopService;
    private final MonService monService;

    public NganhToHopMonPanel(MainFrame mainFrame) {
        super(mainFrame);
        this.service = new NganhToHopMonService();
        this.nganhToHopService = new NganhToHopService();
        this.monService = new MonService();
        enablePagination();
        initCrudUI();
        loadData();
    }

    @Override
    public String getPageTitle() {
        return UIConstants.PAGE_NGANH_TO_HOP_MON;
    }

    @Override
    protected String[] getTableColumns() {
        return new String[]{
                "ID", "Ma nganh", "Ten nganh", "Ma to hop", "Ten to hop",
                "Ma mon", "Ten mon", "He so", "Mon chinh"
        };
    }

    @Override
    protected Integer getSelectedId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (Integer) model.getValueAt(row, 0);
    }

    @Override
    protected NganhToHopMon getSelectedEntity() {
        Integer id = getSelectedId();
        return id == null ? null : findByIdInList(id);
    }

    private NganhToHopMon findByIdInList(Integer id) {
        return service.findAll().stream()
                .filter(e -> e.getNganhTohopMonId() != null
                        && e.getNganhTohopMonId().equals(id))
                .findFirst().orElse(null);
    }

    @Override
    protected void configureTableColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(160);
        table.getColumnModel().getColumn(5).setPreferredWidth(70);
        table.getColumnModel().getColumn(6).setPreferredWidth(140);
        table.getColumnModel().getColumn(7).setPreferredWidth(60);
        table.getColumnModel().getColumn(8).setPreferredWidth(80);
    }

    @Override
    protected void buildBottomBar() {
        totalLabel = new JLabel("Tong: 0 mon");
        totalLabel.setFont(UIConstants.FONT_SMALL);
        pageSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
        JPanel paging = com.tuyensinh.admin.ui.ToolbarFactory.createPagingPanel(pageSpinner, () -> {
            currentPage = (Integer) pageSpinner.getValue();
            loadData();
        });
        add(com.tuyensinh.admin.ui.ToolbarFactory.createBottomBar(totalLabel, paging), BorderLayout.SOUTH);
    }

    @Override
    public void loadData() {
        model.setRowCount(0);

        String kw = getSearchKeyword();
        List<NganhToHopMon> all = service.findAll();

        List<NganhToHopMon> filtered = kw.isEmpty() ? all
                : all.stream().filter(e -> matchKeyword(e, kw)).toList();

        int total = filtered.size();
        normalizePage(total);
        int from = (currentPage - 1) * pageSize;
        int to = Math.min(from + pageSize, filtered.size());

        if (from < filtered.size()) {
            for (NganhToHopMon nthm : filtered.subList(from, to)) {
                model.addRow(new Object[]{
                        nthm.getNganhTohopMonId(),
                        nthm.getNganhToHop() != null && nthm.getNganhToHop().getNganh() != null
                                ? nthm.getNganhToHop().getNganh().getMaNganh() : "",
                        nthm.getNganhToHop() != null && nthm.getNganhToHop().getNganh() != null
                                ? nthm.getNganhToHop().getNganh().getTenNganh() : "",
                        nthm.getNganhToHop() != null && nthm.getNganhToHop().getToHop() != null
                                ? nthm.getNganhToHop().getToHop().getMaTohop() : "",
                        nthm.getNganhToHop() != null && nthm.getNganhToHop().getToHop() != null
                                ? nthm.getNganhToHop().getToHop().getTenTohop() : "",
                        nthm.getMon() != null ? nthm.getMon().getMaMon() : "",
                        nthm.getMon() != null ? nthm.getMon().getTenMon() : "",
                        nthm.getHeSo() != null ? nthm.getHeSo() : 1,
                        nthm.getIsMonChinh() != null && nthm.getIsMonChinh() ? "Co" : "Khong"
                });
            }
        }

        updateTotalLabel(total, "mon");
        updatePagingState(total);
    }

    @Override
    protected String getEntityDisplayName(NganhToHopMon entity) {
        String maNganh = entity.getNganhToHop() != null && entity.getNganhToHop().getNganh() != null
                ? entity.getNganhToHop().getNganh().getMaNganh() : "";
        String maToHop = entity.getNganhToHop() != null && entity.getNganhToHop().getToHop() != null
                ? entity.getNganhToHop().getToHop().getMaTohop() : "";
        String maMon = entity.getMon() != null ? entity.getMon().getMaMon() : "";
        return maNganh + " - " + maToHop + " - " + maMon;
    }

    @Override
    protected void deleteEntity(NganhToHopMon entity) {
        service.delete(entity);
    }

    @Override
    protected void showAddDialog() {
        showEditor(null);
    }

    @Override
    protected void showEditDialog() {
        NganhToHopMon nthm = getSelectedEntity();
        if (nthm == null) {
            showSelectRow();
            return;
        }
        showEditor(nthm);
    }

    private void showEditor(NganhToHopMon existing) {
        boolean edit = existing != null;

        List<NganhToHop> dsNth = nganhToHopService.findAll();
        List<Mon> dsMon = monService.findAll();

        JComboBox<NganhToHop> cboNth = new JComboBox<>();
        for (NganhToHop nth : dsNth) cboNth.addItem(nth);
        decorateNthCombo(cboNth);

        JComboBox<Mon> cboMon = new JComboBox<>();
        for (Mon m : dsMon) cboMon.addItem(m);
        decorateMonCombo(cboMon);

        JSpinner spnHeSo = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        JCheckBox chkMonChinh = new JCheckBox("La mon chinh");

        if (edit) {
            selectNth(cboNth, existing.getNganhToHop());
            selectMon(cboMon, existing.getMon());
            spnHeSo.setValue(existing.getHeSo() != null ? existing.getHeSo() : 1);
            chkMonChinh.setSelected(existing.getIsMonChinh() != null && existing.getIsMonChinh());
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
            form.add(new JLabel(String.valueOf(existing.getNganhTohopMonId())), gc);
            gc.gridy++;
        }

        gc.gridx = 0;
        form.add(new JLabel("Nganh - To hop (*):"), gc);
        gc.gridx = 1;
        form.add(cboNth, gc);
        gc.gridy++;

        gc.gridx = 0;
        form.add(new JLabel("Mon (*):"), gc);
        gc.gridx = 1;
        form.add(cboMon, gc);
        gc.gridy++;

        gc.gridx = 0;
        form.add(new JLabel("He so:"), gc);
        gc.gridx = 1;
        form.add(spnHeSo, gc);
        gc.gridy++;

        gc.gridx = 0;
        form.add(new JLabel(""), gc);
        gc.gridx = 1;
        form.add(chkMonChinh, gc);
        gc.gridy++;

        int r = JOptionPane.showConfirmDialog(
                this,
                form,
                edit ? "Sua mon trong nganh-to hop" : "Them mon vao nganh-to hop",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (r != JOptionPane.OK_OPTION) return;

        NganhToHop nth = (NganhToHop) cboNth.getSelectedItem();
        Mon mon = (Mon) cboMon.getSelectedItem();

        if (nth == null || mon == null) {
            showMessage(this, "Hay chon day du nganh-to hop va mon!");
            return;
        }

        Short heSoVal = ((Number) spnHeSo.getValue()).shortValue();
        Boolean isMonChinh = chkMonChinh.isSelected();

        try {
            if (edit) {
                existing.setNganhToHop(nth);
                existing.setMon(mon);
                existing.setHeSo(heSoVal);
                existing.setIsMonChinh(isMonChinh);
                service.update(existing);
                showSuccess(this, "Cap nhat thanh cong!");
            } else {
                NganhToHopMon nthm = new NganhToHopMon();
                nthm.setNganhToHop(nth);
                nthm.setMon(mon);
                nthm.setHeSo(heSoVal);
                nthm.setIsMonChinh(isMonChinh);
                service.save(nthm);
                showSuccess(this, "Them thanh cong!");
            }
            loadData();
        } catch (Exception ex) {
            showError(this, getRootMessage(ex));
        }
    }

    private boolean matchKeyword(NganhToHopMon nthm, String kw) {
        if (kw == null || kw.isEmpty()) return true;
        String lower = kw.toLowerCase();

        String maNganh = "";
        String tenNganh = "";
        String maToHop = "";
        String tenToHop = "";
        String maMon = "";
        String tenMon = "";

        if (nthm.getNganhToHop() != null) {
            if (nthm.getNganhToHop().getNganh() != null) {
                maNganh = safe(nthm.getNganhToHop().getNganh().getMaNganh()).toLowerCase();
                tenNganh = safe(nthm.getNganhToHop().getNganh().getTenNganh()).toLowerCase();
            }
            if (nthm.getNganhToHop().getToHop() != null) {
                maToHop = safe(nthm.getNganhToHop().getToHop().getMaTohop()).toLowerCase();
                tenToHop = safe(nthm.getNganhToHop().getToHop().getTenTohop()).toLowerCase();
            }
        }
        if (nthm.getMon() != null) {
            maMon = safe(nthm.getMon().getMaMon()).toLowerCase();
            tenMon = safe(nthm.getMon().getTenMon()).toLowerCase();
        }

        return maNganh.contains(lower) || tenNganh.contains(lower)
                || maToHop.contains(lower) || tenToHop.contains(lower)
                || maMon.contains(lower) || tenMon.contains(lower);
    }

    private void decorateNthCombo(JComboBox<NganhToHop> cbo) {
        cbo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                         boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NganhToHop) {
                    NganhToHop nth = (NganhToHop) value;
                    Nganh n = nth.getNganh();
                    String maNganh = n != null ? safe(n.getMaNganh()) : "";
                    String tenNganh = n != null ? safe(n.getTenNganh()) : "";
                    String maTh = nth.getToHop() != null ? safe(nth.getToHop().getMaTohop()) : "";
                    setText(maNganh + " - " + tenNganh + " / " + maTh);
                }
                return this;
            }
        });
    }

    private void decorateMonCombo(JComboBox<Mon> cbo) {
        cbo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                         boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Mon) {
                    Mon m = (Mon) value;
                    setText(safe(m.getMaMon()) + " - " + safe(m.getTenMon()));
                }
                return this;
            }
        });
    }

    private void selectNth(JComboBox<NganhToHop> cbo, NganhToHop selected) {
        if (selected == null || selected.getNganhTohopId() == null) return;
        for (int i = 0; i < cbo.getItemCount(); i++) {
            NganhToHop item = cbo.getItemAt(i);
            if (item != null && selected.getNganhTohopId().equals(item.getNganhTohopId())) {
                cbo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectMon(JComboBox<Mon> cbo, Mon selected) {
        if (selected == null || selected.getMonId() == null) return;
        for (int i = 0; i < cbo.getItemCount(); i++) {
            Mon item = cbo.getItemAt(i);
            if (item != null && selected.getMonId().equals(item.getMonId())) {
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
