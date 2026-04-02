package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.*;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

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
        return new String[]{"ID", "Mã Ngành", "Tên Ngành", "Tổ hợp gốc", "Độ lệch   ", "Chi tiêu", "Điểm sàn", "Điểm TT", "Active"};
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
        table.getColumnModel().getColumn(2).setPreferredWidth(190);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);
        table.getColumnModel().getColumn(5).setPreferredWidth(70);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(80);
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
            String doLich = "";
            if (goc != null) {
                List<NganhToHop> nths = service.findNganhToHopByNganh(n.getNganhId());
                for (NganhToHop nth : nths) {
                    if (nth.getToHop().getTohopId().equals(goc.getTohopId())) {
                        doLich = nth.getDoLech() != null ? nth.getDoLech().toPlainString() : "";
                        break;
                    }
                }
            }
            model.addRow(new Object[]{
                n.getNganhId(),
                n.getMaNganh(),
                n.getTenNganh(),
                maGoc,
                doLich,
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

    private static final class ToHopRow {
        final JComboBox<ToHop> cbo;
        final JTextField txtLech;
        final JPanel rowPanel;

        ToHopRow(JPanel rowsHost, List<ToHopRow> rowRefs, List<ToHop> allToHop,
                ToHop selected, BigDecimal doLech) {
            cbo = new JComboBox<>(new Vector<>(allToHop));
            decorateToHopCombo(cbo);
            if (selected != null) {
                cbo.setSelectedItem(selected);
            }
            txtLech = new JTextField(doLech != null ? doLech.toPlainString() : "0", 8);
            JButton btnRemove = new JButton("Xoa dong");
            rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            rowPanel.add(new JLabel("Ma to hop:"));
            rowPanel.add(cbo);
            rowPanel.add(new JLabel("Do lech:"));
            rowPanel.add(txtLech);
            rowPanel.add(btnRemove);
            btnRemove.addActionListener(e -> {
                rowsHost.remove(rowPanel);
                rowRefs.remove(this);
                rowsHost.revalidate();
                rowsHost.repaint();
            });
        }
    }

    private void showNganhEditor(Nganh existing) {
        List<ToHop> allToHop = service.findAllToHop();
        if (allToHop.isEmpty()) {
            showMessage(this, "Chua co to hop mon nao trong he thong. Hay them to hop (hoac chay DataSeeder) truoc.");
            return;
        }

        boolean edit = existing != null;

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
        }

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(2, 4, 2, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.gridy = 0;
        if (!edit) {
            form.add(new JLabel("Ma nganh (*):"), gc);
            gc.gridx = 1;
            form.add(txtMa, gc);
            gc.gridy++;
        } else {
            form.add(new JLabel("Ma nganh (khong doi):"), gc);
            gc.gridx = 1;
            form.add(new JLabel(existing.getMaNganh()), gc);
            gc.gridy++;
        }
        gc.gridx = 0;
        gc.gridy++;
        form.add(new JLabel("Ten nganh (*):"), gc);
        gc.gridx = 1;
        form.add(txtTen, gc);
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

        JPanel rowsHost = new JPanel();
        rowsHost.setLayout(new BoxLayout(rowsHost, BoxLayout.Y_AXIS));
        final List<ToHopRow> rowRefs = new ArrayList<>();

        Runnable addRow = () -> {
            ToHopRow tr = new ToHopRow(rowsHost, rowRefs, allToHop, null, BigDecimal.ZERO);
            rowRefs.add(tr);
            rowsHost.add(tr.rowPanel);
            rowsHost.revalidate();
            rowsHost.repaint();
        };

        if (edit) {
            List<NganhToHop> nts = service.findNganhToHopByNganh(existing.getNganhId());
            if (!nts.isEmpty()) {
                for (NganhToHop nt : nts) {
                    ToHopRow tr = new ToHopRow(rowsHost, rowRefs, allToHop, nt.getToHop(), nt.getDoLech());
                    rowRefs.add(tr);
                    rowsHost.add(tr.rowPanel);
                }
            } else {
                addRow.run();
            }
        } else {
            addRow.run();
        }

        JButton btnAddRow = new JButton("Them dong to hop");
        btnAddRow.addActionListener(e -> addRow.run());

        JLabel lblHint = new JLabel("<html><small>Dong dau tien la <b>to hop goc</b> (tham chieu chinh).<br>"
            + "Khong trung ma to hop trong cac dong.</small></html>");
        lblHint.setBorder(new EmptyBorder(4, 0, 4, 0));

        JPanel toHopBlock = new JPanel(new BorderLayout(4, 4));
        toHopBlock.add(lblHint, BorderLayout.NORTH);
        toHopBlock.add(new JScrollPane(rowsHost), BorderLayout.CENTER);
        toHopBlock.add(btnAddRow, BorderLayout.SOUTH);

        JPanel main = new JPanel(new BorderLayout(8, 8));
        main.setBorder(new EmptyBorder(8, 8, 8, 8));
        main.add(form, BorderLayout.NORTH);
        main.add(toHopBlock, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(main);
        scroll.setPreferredSize(new Dimension(520, 420));

        int r = JOptionPane.showConfirmDialog(this, scroll,
            edit ? "Sua nganh" : "Them nganh moi",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;

        if (!edit && (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty())) {
            showMessage(this, "Ma va Ten nganh la bat buoc!");
            return;
        }
        if (edit && txtTen.getText().trim().isEmpty()) {
            showMessage(this, "Ten nganh la bat buoc!");
            return;
        }

        List<Map.Entry<Integer, BigDecimal>> links = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        for (ToHopRow tr : rowRefs) {
            ToHop th = (ToHop) tr.cbo.getSelectedItem();
            if (th == null) continue;
            if (!seen.add(th.getTohopId())) {
                showMessage(this, "Khong duoc trung ma to hop trong cac dong!");
                return;
            }
            BigDecimal dl = parseBigDecimal(tr.txtLech.getText());
            if (dl == null) dl = BigDecimal.ZERO;
            links.add(new SimpleEntry<>(th.getTohopId(), dl));
        }

        try {
            if (edit) {
                existing.setTenNganh(txtTen.getText().trim());
                existing.setChiTieu(parseInt(txtChiTieu.getText()));
                existing.setDiemSan(parseBigDecimal(txtDiemSan.getText()));
                existing.setDiemTrungTuyen(parseBigDecimal(txtDiemTT.getText()));
                existing.setIsActive(chkActive.isSelected());
                service.updateNganh(existing);
                service.syncNganhToHopForNganh(existing.getNganhId(), links);
                showSuccess(this, "Cap nhat thanh cong!");
            } else {
                Nganh n = new Nganh();
                n.setMaNganh(txtMa.getText().trim());
                n.setTenNganh(txtTen.getText().trim());
                n.setChiTieu(parseInt(txtChiTieu.getText()));
                n.setDiemSan(parseBigDecimal(txtDiemSan.getText()));
                n.setDiemTrungTuyen(parseBigDecimal(txtDiemTT.getText()));
                n.setIsActive(chkActive.isSelected());
                service.saveNganh(n);
                service.syncNganhToHopForNganh(n.getNganhId(), links);
                showSuccess(this, "Them thanh cong!");
            }
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }
}
