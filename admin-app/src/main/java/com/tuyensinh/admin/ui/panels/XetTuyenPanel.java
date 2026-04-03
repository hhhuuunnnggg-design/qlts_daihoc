package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.*;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class XetTuyenPanel extends BaseCrudPanel<NguyenVong> {

    private final XetTuyenService xetTuyenService = new XetTuyenService();
    private final NguyenVongService nguyenVongService = new NguyenVongService();
    private final TinhDiemService tinhDiemService = new TinhDiemService();
    private final XetTuyenEngine engine = new XetTuyenEngine();
    private final DiemCongService diemCongService = new DiemCongService();

    private JComboBox<PhuongThuc> cboPhuongThuc;
    private JComboBox<Nganh> cboNganh;
    private JTextArea taResult;
    private JButton btnXetTuyen;
    private JButton btnTinhDiem;
    private JButton btnDiemUuTien;

    public XetTuyenPanel(MainFrame mainFrame) {
        super(mainFrame);
        initUI();
        loadData();
    }

    @Override
    protected String[] getTableColumns() {
        return new String[]{"ID", "Ho Ten", "CCCD", "Nganh", "To Hop", "P. Thuc",
                            "Diem TH", "Diem Cong", "Diem XT", "Ket Qua"};
    }

    @Override
    protected NguyenVong getSelectedEntity() {
        int row = table.getSelectedRow();
        return row < 0 ? null : nguyenVongService.findById((Integer) model.getValueAt(row, 0));
    }

    @Override
    protected Integer getSelectedId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (Integer) model.getValueAt(row, 0);
    }

    @Override
    public String getPageTitle() {
        return UIConstants.PAGE_XET_TUYEN;
    }

    @Override
    protected void buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        toolbar.add(new JLabel("Phuong thuc:"));
        cboPhuongThuc = new JComboBox<>();
        cboPhuongThuc.addItem(null);
        for (PhuongThuc pt : xetTuyenService.findActivePhuongThuc()) {
            cboPhuongThuc.addItem(pt);
        }
        cboPhuongThuc.addActionListener(e -> loadData());
        toolbar.add(cboPhuongThuc);

        toolbar.add(new JLabel("  Nganh:"));
        cboNganh = new JComboBox<>();
        cboNganh.addItem(null);
        for (Nganh n : xetTuyenService.findActiveNganh()) {
            cboNganh.addItem(n);
        }
        cboNganh.addActionListener(e -> loadData());
        toolbar.add(cboNganh);

        toolbar.add(Box.createHorizontalStrut(12));

        btnTinhDiem = new JButton("Tinh diem");
        btnTinhDiem.addActionListener(e -> tinhDiemAll());
        toolbar.add(btnTinhDiem);

        btnDiemUuTien = new JButton("Diem uu tien");
        btnDiemUuTien.addActionListener(e -> diemUuTienAuto());
        toolbar.add(btnDiemUuTien);

        btnXetTuyen = new JButton("Xet tuyen");
        btnXetTuyen.addActionListener(e -> xetTuyen());
        toolbar.add(btnXetTuyen);

        add(toolbar, BorderLayout.NORTH);
    }

    @Override
    protected void buildBottomBar() {
        totalLabel = new JLabel("Tong: 0 ban ghi");
        totalLabel.setFont(UIConstants.FONT_SMALL);
        add(totalLabel, BorderLayout.SOUTH);
    }

    @Override
    public void loadData() {
        model.setRowCount(0);
        PhuongThuc pt = (PhuongThuc) cboPhuongThuc.getSelectedItem();
        Nganh nganh = (Nganh) cboNganh.getSelectedItem();

        List<NguyenVong> list;
        if (nganh != null && pt != null) {
            list = nguyenVongService.findAll().stream()
                .filter(nv -> nganh.getNganhId().equals(nv.getNganh().getNganhId())
                    && pt.getPhuongthucId().equals(nv.getPhuongThuc().getPhuongthucId()))
                .toList();
        } else if (pt != null) {
            list = nguyenVongService.findAll().stream()
                .filter(nv -> pt.getPhuongthucId().equals(nv.getPhuongThuc().getPhuongthucId()))
                .toList();
        } else {
            list = nguyenVongService.findAll();
        }

        for (NguyenVong nv : list) {
            ThiSinh ts = nv.getThiSinh();
            String ketQua = nv.getKetQua();

            model.addRow(new Object[]{
                nv.getNguyenvongId(),
                ts != null ? ts.getHoVaTen() : "",
                ts != null ? ts.getCccd() : "",
                nv.getNganh() != null ? nv.getNganh().getMaNganh() : "",
                nv.getNganhToHop() != null && nv.getNganhToHop().getToHop() != null
                    ? nv.getNganhToHop().getToHop().getMaTohop() : "",
                nv.getPhuongThuc() != null ? nv.getPhuongThuc().getMaPhuongthuc() : "",
                nv.getDiemThxt() != null ? formatDiem(nv.getDiemThxt()) : "",
                nv.getDiemCong() != null ? formatDiem(nv.getDiemCong()) : "",
                nv.getDiemXettuyen() != null ? formatDiem(nv.getDiemXettuyen()) : "",
                ketQua != null ? ketQua : "CHO_XET"
            });
        }

        // Doi mau dong theo ket qua
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                Object kq = model.getValueAt(row, 9);
                if (!isSelected) {
                    if ("TRUNG_TUYEN".equals(kq)) c.setBackground(new Color(220, 255, 220));
                    else if ("TRUOT".equals(kq)) c.setBackground(new Color(255, 220, 220));
                    else if ("PHOI_DU_KIEN".equals(kq)) c.setBackground(new Color(255, 255, 200));
                    else if ("CHO_XET".equals(kq)) c.setBackground(new Color(255, 250, 205));
                    else c.setBackground(Color.WHITE);
                }
                return c;
            }
        });

        updateTotalLabel(list.size(), "ban ghi");
    }

    private String formatDiem(BigDecimal d) {
        if (d == null) return "";
        return d.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    /** Tinh diem cho tat ca nguyen vong hien tai (theo loc). */
    private void tinhDiemAll() {
        PhuongThuc pt = (PhuongThuc) cboPhuongThuc.getSelectedItem();
        Nganh nganh = (Nganh) cboNganh.getSelectedItem();

        if (pt == null || nganh == null) {
            showMessage(this, "Chon phuong thuc VA nganh truoc!");
            return;
        }

        int[] counts = {0, 0, 0};
        List<NguyenVong> nvs = nguyenVongService.findAll().stream()
            .filter(nv -> nganh.getNganhId().equals(nv.getNganh().getNganhId())
                && pt.getPhuongthucId().equals(nv.getPhuongThuc().getPhuongthucId()))
            .toList();

        if (nvs.isEmpty()) {
            showMessage(this, "Khong co nguyen vong nao!");
            return;
        }

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                for (NguyenVong nv : nvs) {
                    try {
                        engine.tinhDiemNguyenVong(nv);
                        counts[0]++; // thanh cong
                    } catch (Exception e) {
                        counts[1]++; // loi
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                showSuccess(XetTuyenPanel.this,
                    "Tinh diem xong!\nThanh cong: " + counts[0] + "\nLoi: " + counts[1]);
                loadData();
            }
        };
        worker.execute();
    }

    /** Tu dong dien diem uu tien (Khu Vuc + Doi Tuong) cho tat ca thi sinh. */
    private void diemUuTienAuto() {
        int r = JOptionPane.showConfirmDialog(this,
            "Tu dong dien diem uu tien cho tat ca thi sinh?\n"
            + "Diem = Muc KV (neu co) + Muc DT (neu co)\n"
            + "Chi tao diem cong cho nhung nguyen vong da co.",
            "Xac nhan", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            int tao = 0, capNhat = 0;
            @Override
            protected Void doInBackground() throws Exception {
                List<ThiSinh> allTs = new ThiSinhService().findAll();
                for (ThiSinh ts : allTs) {
                    java.util.List<DiemCong> dcs = tinhDiemService.taoDiemCongTuDong(ts, null);
                    for (DiemCong dc : dcs) {
                        if (dc.getDiemcongId() == null) {
                            diemCongService.save(dc);
                            tao++;
                        } else {
                            diemCongService.update(dc);
                            capNhat++;
                        }
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                showSuccess(XetTuyenPanel.this,
                    "Diem uu tien hoan tat!\nTao moi: " + tao + "\nCap nhat: " + capNhat);
            }
        };
        worker.execute();
    }

    /** Chay xet tuyen: tinh diem + xep hang + danh gia ket qua. */
    private void xetTuyen() {
        PhuongThuc pt = (PhuongThuc) cboPhuongThuc.getSelectedItem();
        Nganh nganh = (Nganh) cboNganh.getSelectedItem();

        if (pt == null || nganh == null) {
            showMessage(this, "Chon phuong thuc VA nganh truoc!");
            return;
        }

        taResult.setText("");

        SwingWorker<String, String> worker = new SwingWorker<>() {
            java.util.List<String> logLines = new java.util.ArrayList<>();

            private void log(String s) { logLines.add(s); publish(s); }

            @Override
            protected String doInBackground() {
                log("=== XET TUYEN ===");
                log("Phuong thuc: " + pt.getTenPhuongthuc() + " (" + pt.getMaPhuongthuc() + ")");
                log("Nganh: " + nganh.getTenNganh() + " (" + nganh.getMaNganh() + ")");
                log("Chi tieu: " + nganh.getChiTieu());
                log("Diem san: " + (nganh.getDiemSan() != null ? nganh.getDiemSan() : "Chua dat"));
                log("-----------------------");

                // 1. Tinh diem
                List<NguyenVong> nvs = nguyenVongService.findAll().stream()
                    .filter(nv -> nganh.getNganhId().equals(nv.getNganh().getNganhId())
                        && pt.getPhuongthucId().equals(nv.getPhuongThuc().getPhuongthucId()))
                    .toList();

                int coDiem = 0, khongDiem = 0;
                for (NguyenVong nv : nvs) {
                    try {
                        TinhDiemService.KetQuaDiem kq = engine.tinhDiemNguyenVong(nv);
                        if (kq != null && kq.diemXettuyen != null
                                && kq.diemXettuyen.compareTo(BigDecimal.ZERO) > 0) {
                            coDiem++;
                        } else {
                            khongDiem++;
                        }
                    } catch (Exception e) {
                        khongDiem++;
                    }
                }
                log("Tinh diem: " + coDiem + " co diem, " + khongDiem + " khong co diem");

                // 2. Xet tuyen
                XetTuyenEngine.DotXetTuyenResult result;
                try {
                    result = engine.xetTuyenNganhPhuongThuc(nganh.getNganhId(), pt.getPhuongthucId());
                } catch (Exception e) {
                    log("LOI: " + e.getMessage());
                    return "ERROR";
                }

                log("");
                log("=== KET QUA ===");
                log("TRUNG_TUYEN: " + result.soTrungTuyen + " / " + nganh.getChiTieu());
                log("PHOI_DU_KIEN: " + result.soPhoiDuKien);
                log("TRUOT: " + result.soTruot);
                log("CHO_XET: " + result.soChoXet);
                log("THIEU_DIEM: " + result.soThieuDiem);
                log("");
                log("--- Chi tiet (top 20) ---");
                int count = 0;
                for (XetTuyenEngine.KetQuaXetTuyen kq : result.danhSach) {
                    if (count++ >= 20) break;
                    log(kq.toString());
                }
                if (result.danhSach.size() > 20) {
                    log("... va " + (result.danhSach.size() - 20) + " nguoi nua");
                }
                log("");
                log("DONE.");
                return "OK";
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String s : chunks) taResult.append(s + "\n");
                taResult.setCaretPosition(taResult.getDocument().getLength());
            }

            @Override
            protected void done() {
                try {
                    String status = get();
                    for (String s : logLines) taResult.append(s + "\n");
                    taResult.setCaretPosition(taResult.getDocument().getLength());
                    loadData();
                    if ("OK".equals(status)) {
                        JOptionPane.showMessageDialog(XetTuyenPanel.this,
                            "Xet tuyen hoan tat!");
                    }
                } catch (Exception ex) {
                    showError(XetTuyenPanel.this, ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    @Override
    protected void deleteEntity(NguyenVong nv) throws Exception {
        nguyenVongService.delete(nv);
    }

    @Override
    protected String getEntityDisplayName(NguyenVong nv) {
        return nv.getThiSinh() != null ? nv.getThiSinh().getHoVaTen() : String.valueOf(nv.getNguyenvongId());
    }

    @Override
    protected void initUI() {
        buildToolbar();
        buildTable();
        buildBottomBar();
        buildLogArea();
    }

    private void buildLogArea() {
        taResult = new JTextArea(6, 0);
        taResult.setFont(new Font("Monospaced", Font.PLAIN, 12));
        taResult.setEditable(false);
        taResult.setLineWrap(true);
        taResult.setText(
            "Huong dan:\n" +
            "1. Chon Phuong thuc + Nganh\n" +
            "2. [Tinh diem] — tinh diem_xettuyen\n" +
            "3. [Diem uu tien] — tu dong dien muc uu tien KV/DT\n" +
            "4. [Xet tuyen] — TRUNG_TUYEN / TRUOT / PHOI_DU_KIEN\n" +
            "Cong thuc: diem_xettuyen = diem_thxt + diem_cong\n" +
            "  diem_thxt = sum(diem * he_so) - do_lech\n" +
            "  diem_cong = diem_chungchi + diem_uutien\n\n");
        JScrollPane sp = new JScrollPane(taResult);
        sp.setBorder(BorderFactory.createTitledBorder("Log xet tuyen"));

        JPanel south = new JPanel(new BorderLayout(0, 4));
        south.add(sp, BorderLayout.CENTER);

        totalLabel = new JLabel("Tong: 0 ban ghi");
        totalLabel.setFont(UIConstants.FONT_SMALL);
        south.add(totalLabel, BorderLayout.SOUTH);

        add(south, BorderLayout.SOUTH);
    }
}
