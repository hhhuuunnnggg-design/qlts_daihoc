package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.*;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.dao.BaseDao;
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
    private JButton btnXetTuyenMau;
    private JButton btnXetTuyenToanBo;
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
                "Diem TH", "Diem Cong", "Diem XT", "Nguon diem", "Ket Qua"};
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

        JLabel lblInfo = new JLabel(
                "Xet tuyen toan bo: tu tinh diem tot nhat THPT/VSAT/DGNL va xet theo thu tu nguyen vong"
        );
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        toolbar.add(lblInfo);

        toolbar.add(Box.createHorizontalStrut(16));

        btnXetTuyenToanBo = new JButton("Xet tuyen toan bo");
        btnXetTuyenToanBo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnXetTuyenToanBo.setToolTipText(
                "Chay xet tuyen toan bo du lieu, moi thi sinh chi trung tuyen 1 nguyen vong cao nhat"
        );
        btnXetTuyenToanBo.addActionListener(e -> xetTuyenToanBo());
        toolbar.add(btnXetTuyenToanBo);

        JButton btnLamMoi = new JButton("Lam moi");
        btnLamMoi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLamMoi.addActionListener(e -> lamMoiDuLieu());
        toolbar.add(btnLamMoi);

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
        // BaseDao dung ThreadLocal EntityManager. Neu khong dong EM cu,
        // Hibernate co the tra du lieu cache cu nen nut Lam moi nhin nhu khong cap nhat.
        BaseDao.closeCurrentEm();

        model.setRowCount(0);

        List<NguyenVong> list = nguyenVongService.findAll();

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
                    nv.getPhuongThucDiemTotNhat() != null ? nv.getPhuongThucDiemTotNhat() : "",
                    ketQua != null ? ketQua : "CHO_XET"
            });
        }

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                Object kq = model.getValueAt(row, 10);

                if (!isSelected) {
                    if ("TRUNG_TUYEN".equals(kq)) {
                        c.setBackground(new Color(220, 255, 220));
                    } else if ("TRUOT".equals(kq)) {
                        c.setBackground(new Color(255, 220, 220));
                    } else if ("PHOI_DU_KIEN".equals(kq)) {
                        c.setBackground(new Color(255, 255, 200));
                    } else if ("CHO_XET".equals(kq)) {
                        c.setBackground(new Color(255, 250, 205));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }

                return c;
            }
        });

        updateTotalLabel(list.size(), "ban ghi");

        if (table != null) {
            table.clearSelection();
            table.revalidate();
            table.repaint();
        }
    }

    private void lamMoiDuLieu() {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            loadData();
            if (taResult != null) {
                taResult.append("\n[Lam moi] Da nap lai du lieu moi nhat tu DB.\n");
                taResult.setCaretPosition(taResult.getDocument().getLength());
            }
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private String formatDiem(BigDecimal d) {
        if (d == null) return "";
        return d.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * Xet tuyen toan bo theo thu tu nguyen vong:
     * - Tinh diem tot nhat THPT/VSAT/DGNL cho tung nguyen vong.
     * - Chay xet tuyen toan cuc, moi thi sinh chi TRUNG_TUYEN 1 nguyen vong cao nhat.
     * - Neu dang chon phuong thuc/nganh thi chi chay trong bo loc do; neu muon chay that toan bo thi bo chon nganh.
     */
    private void xetTuyenToanBo() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Chạy xét tuyển toàn bộ sẽ cập nhật các cột sau trong DB:\n"
                        + "- diem_thxt\n"
                        + "- diem_cong\n"
                        + "- diem_uutien\n"
                        + "- diem_xettuyen\n"
                        + "- phuong_thuc_diem_tot_nhat\n"
                        + "- ket_qua\n\n"
                        + "Engine sẽ xét theo thứ tự nguyện vọng.\n"
                        + "Mỗi thí sinh chỉ được TRÚNG TUYỂN 1 nguyện vọng cao nhất.\n\n"
                        + "Bạn nên backup DB hoặc reset dữ liệu test trước khi chạy.\n\n"
                        + "Tiếp tục?",
                "Xác nhận xét tuyển toàn bộ",
                JOptionPane.OK_CANCEL_OPTION);

        if (confirm != JOptionPane.OK_OPTION) {
            return;
        }

        taResult.setText("");

        SwingWorker<XetTuyenEngine.XetTuyenToanCucResult, String> worker = new SwingWorker<>() {
            @Override
            protected XetTuyenEngine.XetTuyenToanCucResult doInBackground() {
                publish("=== XET TUYEN TOAN BO ===");
                publish("Pham vi: tat ca phuong thuc, tat ca nganh, tat ca nguyen vong");
                publish("Dang tinh diem tot nhat THPT / VSAT / DGNL...");
                publish("Dang xet tuyen theo thu tu nguyen vong...");
                publish("");

                XetTuyenEngine.XetTuyenToanCucResult r =
                        engine.xetTuyenToanCucTheoThuTuNguyenVong(
                                null,
                                null,
                                true
                        );

                publish("-----------------------");
                publish("Tong nguyen vong: " + r.soNguyenVong);
                publish("Tong thi sinh: " + r.soThiSinh);
                publish("Tinh diem OK: " + r.soTinhDiemOk);
                publish("Tinh diem loi: " + r.soTinhDiemLoi);
                publish("So vong lap xet NV: " + r.soVongLap);
                publish("TRUNG_TUYEN: " + r.soTrungTuyen);
                publish("TRUOT: " + r.soTruot);
                publish("PHOI_DU_KIEN: " + r.soPhoiDuKien);
                publish("CHO_XET: " + r.soChoXet);

                publish("");
                publish("--- Thong ke trung tuyen theo nhom nganh/phuong thuc ---");
                int shownGroup = 0;
                for (java.util.Map.Entry<String, Integer> entry : r.thongKeTheoNhom.entrySet()) {
                    if (shownGroup++ >= 30) {
                        publish("... va cac nhom khac");
                        break;
                    }
                    publish(entry.getKey() + " = " + entry.getValue());
                }

                publish("");
                publish("--- Mau 30 dong dau ---");
                int shown = 0;
                for (XetTuyenEngine.KetQuaXetTuyen item : r.danhSach) {
                    if (shown++ >= 30) break;

                    publish(item.toString());

                    if (item.ghiChu != null && !item.ghiChu.isBlank()) {
                        publish("    -> " + item.ghiChu);
                    }
                }

                publish("");
                publish("DONE.");
                return r;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String s : chunks) {
                    taResult.append(s + "\n");
                }
                taResult.setCaretPosition(taResult.getDocument().getLength());
            }

            @Override
            protected void done() {
                try {
                    XetTuyenEngine.XetTuyenToanCucResult r = get();

                    lamMoiDuLieu();

                    showSuccess(XetTuyenPanel.this,
                            "Xét tuyển toàn bộ hoàn tất!\n"
                                    + "Tổng NV: " + r.soNguyenVong
                                    + "\nThí sinh: " + r.soThiSinh
                                    + "\nTrúng tuyển: " + r.soTrungTuyen
                                    + "\nTính điểm lỗi: " + r.soTinhDiemLoi);

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
                        "1. Bam [Xet tuyen toan bo]\n" +
                        "2. He thong tu tinh diem tot nhat cho tung nguyen vong\n" +
                        "3. Diem tot nhat = max(THPT, VSAT, DGNL sau quy doi) + diem cong/uu tien\n" +
                        "4. Engine xet theo thu tu nguyen vong cua tung thi sinh\n" +
                        "5. Moi thi sinh chi duoc TRUNG_TUYEN 1 nguyen vong cao nhat\n\n" +
                        "Cac cot can kiem tra sau khi chay:\n" +
                        "- diem_thxt\n" +
                        "- diem_cong\n" +
                        "- diem_uutien\n" +
                        "- diem_xettuyen\n" +
                        "- phuong_thuc_diem_tot_nhat\n" +
                        "- ket_qua\n\n"
        );
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