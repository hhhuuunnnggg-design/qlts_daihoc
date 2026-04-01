package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class XetTuyenPanel extends JPanel {

    private MainFrame mainFrame;
    private XetTuyenService xetTuyenService = new XetTuyenService();
    private NguyenVongService nguyenVongService = new NguyenVongService();

    private JComboBox<PhuongThuc> cboPhuongThuc;
    private JComboBox<Nganh> cboNganh;
    private JTextArea taResult;
    private JButton btnXetTuyen;

    public XetTuyenPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.add(new JLabel("Xet tuyen tu dong"));
        add(header, BorderLayout.NORTH);

        // Options
        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT));
        options.add(new JLabel("Phuong thuc:"));
        cboPhuongThuc = new JComboBox<>();
        for (PhuongThuc pt : xetTuyenService.findActivePhuongThuc()) {
            cboPhuongThuc.addItem(pt);
        }
        options.add(cboPhuongThuc);

        options.add(new JLabel("  Nganh:"));
        cboNganh = new JComboBox<>();
        for (Nganh n : xetTuyenService.findActiveNganh()) {
            cboNganh.addItem(n);
        }
        options.add(cboNganh);

        btnXetTuyen = new JButton("Bat dau xet tuyen");
        btnXetTuyen.addActionListener(e -> xetTuyen());
        options.add(btnXetTuyen);

        add(options, BorderLayout.NORTH);

        // Result
        taResult = new JTextArea();
        taResult.setFont(new Font("Monospaced", Font.PLAIN, 13));
        taResult.setEditable(false);
        taResult.setText("Chon phuong thuc va nganh, sau do nhan 'Bat dau xet tuyen'.\n\n");
        taResult.append("Quy tac xet tuyen:\n");
        taResult.append("1. So sanh diem xet tuyen voi diem san cua nganh\n");
        taResult.append("2. Diem xet tuyen = diem_thxt + diem_cong (neu co)\n");
        taResult.append("3. Sap xep theo diem giam dan\n");
        taResult.append("4. Lay dau danh theo chi tieu nganh\n");
        taResult.append("5. Cap nhat ket qua: TRUNG_TUYEN / TRUOT / CHO_XET\n");

        JScrollPane sp = new JScrollPane(taResult);
        sp.setBorder(BorderFactory.createTitledBorder("Ket qua xet tuyen"));
        add(sp, BorderLayout.CENTER);
    }

    private void xetTuyen() {
        PhuongThuc pt = (PhuongThuc) cboPhuongThuc.getSelectedItem();
        Nganh nganh = (Nganh) cboNganh.getSelectedItem();

        if (pt == null || nganh == null) {
            JOptionPane.showMessageDialog(this, "Chon day du phuong thuc va nganh!");
            return;
        }

        taResult.setText("Dang xu ly xet tuyen...\n");
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                publish("=== XET TUYEN ===\n");
                publish("Phuong thuc: " + pt.getTenPhuongthuc() + " (" + pt.getMaPhuongthuc() + ")\n");
                publish("Nganh: " + nganh.getTenNganh() + " (" + nganh.getMaNganh() + ")\n");
                publish("Chi tieu: " + nganh.getChiTieu() + "\n");
                publish("Diem san: " + (nganh.getDiemSan() != null ? nganh.getDiemSan() : "Chua dat") + "\n");
                publish("-----------------------\n");

                List<NguyenVong> nvList = nguyenVongService.findAll();
                int countTrungTuyen = 0;

                for (NguyenVong nv : nvList) {
                    if (nv.getNganh() != null && nv.getNganh().getNganhId().equals(nganh.getNganhId())
                        && nv.getPhuongThuc() != null && nv.getPhuongThuc().getPhuongthucId().equals(pt.getPhuongthucId())) {

                        boolean trungTuyen = false;
                        if (nv.getDiemXettuyen() != null && nganh.getDiemSan() != null) {
                            if (nv.getDiemXettuyen().compareTo(nganh.getDiemSan()) >= 0 && countTrungTuyen < nganh.getChiTieu()) {
                                trungTuyen = true;
                                countTrungTuyen++;
                            }
                        }

                        String ketQua = trungTuyen ? "TRUNG_TUYEN" : "CHO_XET";
                        publish("TS: " + (nv.getThiSinh() != null ? nv.getThiSinh().getHoVaTen() : "")
                            + " | Diem: " + (nv.getDiemXettuyen() != null ? nv.getDiemXettuyen() : "N/A")
                            + " | KQ: " + ketQua + "\n");

                        nv.setKetQua(ketQua);
                        nguyenVongService.update(nv);
                    }
                }

                publish("-----------------------\n");
                publish("Tong so trung tuyen: " + countTrungTuyen + " / " + nganh.getChiTieu() + "\n");
                publish("Xet tuyen hoan tat!\n");
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                StringBuilder sb = new StringBuilder(taResult.getText());
                for (String s : chunks) sb.append(s);
                taResult.setText(sb.toString());
                taResult.setCaretPosition(taResult.getDocument().getLength());
            }

            @Override
            protected void done() {
                JOptionPane.showMessageDialog(XetTuyenPanel.this, "Xet tuyen hoan tat!");
            }
        };
        worker.execute();
    }
}
