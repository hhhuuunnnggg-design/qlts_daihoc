package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.entity.ThiSinhThanhTich;
import com.tuyensinh.service.ThiSinhService;
import com.tuyensinh.service.ThiSinhThanhTichService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ThiSinhThanhTichImportPanel extends JPanel {

    private static final short DEFAULT_NAM_DAT_GIAI = 2025;

    private final MainFrame mainFrame;
    private final ThiSinhThanhTichService thanhTichService = new ThiSinhThanhTichService();
    private final ThiSinhService thiSinhService = new ThiSinhService();

    private JTextArea taLog;
    private JButton btnSelect;
    private JButton btnImport;
    private JLabel lblFile;
    private JCheckBox chkClearOld;
    private JFileChooser fileChooser;
    private File selectedFile;

    public ThiSinhThanhTichImportPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.add(new JLabel("Import thanh tich uu tien tu file Uu tien xet tuyen.xlsx"));
        add(header, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 6, 6));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Thong tin file"));
        infoPanel.add(new JLabel("File da chon:"));

        lblFile = new JLabel("Chua chon file...");
        lblFile.setForeground(Color.BLUE);
        infoPanel.add(lblFile);

        chkClearOld = new JCheckBox("Xoa du lieu cu bang xt_thisinh_thanh_tich truoc khi import", false);
        infoPanel.add(chkClearOld);

        btnSelect = new JButton("Chon file Excel");
        btnSelect.addActionListener(e -> selectFile());
        infoPanel.add(btnSelect);

        btnImport = new JButton("Import du lieu");
        btnImport.setEnabled(false);
        btnImport.addActionListener(e -> importData());
        infoPanel.add(btnImport);

        JTextArea taTemplate = new JTextArea(
                "Nguon import: Uu tien xet tuyen.xlsx\n\n" +
                        "Sheet su dung: ds thi sinh\n\n" +
                        "Cac cot duoc dung:\n" +
                        "B = CCCD\n" +
                        "C = Cap\n" +
                        "D = DT\n" +
                        "E = Ma mon\n" +
                        "F = Loai giai\n" +
                        "G = Diem cong cho mon dat giai\n" +
                        "H = Diem cong cho THXT ko co mon dat giai\n" +
                        "I = Co C/C\n\n" +
                        "Rule import:\n" +
                        "- Tim thi sinh theo CCCD\n" +
                        "- Chi import sheet ds thi sinh\n" +
                        "- Tao / cap nhat theo khoa mem:\n" +
                        "  (thi sinh + nhom thanh tich + cap + loai giai + mon dat giai)\n" +
                        "- Ten thanh tich se ghep tu nhom + cap + loai giai\n" +
                        "- Mac dinh nam dat giai = 2025\n" +
                        "- Cot G/H/I duoc luu vao ghi_chu de doi chieu test service\n"
        );
        taTemplate.setEditable(false);
        taTemplate.setFont(new Font("Monospaced", Font.PLAIN, 12));
        taTemplate.setBackground(new Color(245, 245, 245));

        JScrollPane templatePane = new JScrollPane(taTemplate);
        templatePane.setBorder(BorderFactory.createTitledBorder("Huong dan cot"));

        taLog = new JTextArea();
        taLog.setEditable(false);
        taLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        taLog.setBackground(new Color(30, 30, 30));
        taLog.setForeground(new Color(0, 255, 0));

        JScrollPane logPane = new JScrollPane(taLog);
        logPane.setBorder(BorderFactory.createTitledBorder("Nhat ky import"));

        JSplitPane topSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, infoPanel, templatePane);
        topSplit.setResizeWeight(0.30);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, logPane);
        mainSplit.setResizeWeight(0.48);

        add(mainSplit, BorderLayout.CENTER);
    }

    private void selectFile() {
        fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chon file Uu tien xet tuyen.xlsx");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Excel files", "xlsx", "xls"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            lblFile.setText(selectedFile.getAbsolutePath());
            btnImport.setEnabled(true);
            log("Da chon file: " + selectedFile.getName());
        }
    }

    private void importData() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Vui long chon file Excel truoc!");
            return;
        }

        btnImport.setEnabled(false);
        btnSelect.setEnabled(false);
        taLog.setText("");

        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {

            int total = 0;
            int inserted = 0;
            int updated = 0;
            int skipped = 0;
            int error = 0;

            @Override
            protected Void doInBackground() {
                publish("Bat dau import file: " + selectedFile.getName());

                try {
                    if (chkClearOld.isSelected()) {
                        List<ThiSinhThanhTich> allOld = new ArrayList<>(thanhTichService.findAll());
                        publish("Dang xoa " + allOld.size() + " dong thanh tich cu...");
                        for (ThiSinhThanhTich item : allOld) {
                            thanhTichService.delete(item);
                        }
                        publish("Da xoa du lieu cu.");
                    }

                    try (FileInputStream fis = new FileInputStream(selectedFile);
                         Workbook workbook = WorkbookFactory.create(fis)) {

                        Sheet sheet = workbook.getSheet("ds thi sinh");
                        if (sheet == null) {
                            sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
                        }
                        if (sheet == null) {
                            publish("Khong tim thay sheet ds thi sinh.");
                            return null;
                        }

                        int lastRow = sheet.getLastRowNum();
                        publish("Doc sheet: " + sheet.getSheetName());
                        publish("Tong so dong du lieu (uoc tinh): " + lastRow);

                        for (int i = 1; i <= lastRow; i++) {
                            Row row = sheet.getRow(i);
                            if (row == null || isRowEmpty(row)) {
                                continue;
                            }

                            total++;
                            int excelRow = i + 1;

                            try {
                                String cccd = readCell(row, 1);
                                String cap = readCell(row, 2);
                                String nhom = readCell(row, 3);
                                String maMon = readCell(row, 4);
                                String loaiGiai = readCell(row, 5);
                                String diemCongMon = readCell(row, 6);
                                String diemCongKhongMon = readCell(row, 7);
                                String coChungChi = readCell(row, 8);

                                if (isBlank(cccd) || isBlank(nhom)) {
                                    skipped++;
                                    publish("Dong " + excelRow + ": bo qua vi thieu CCCD/Nhom thanh tich.");
                                    continue;
                                }

                                Optional<ThiSinh> optThiSinh = thiSinhService.findByCccd(cccd.trim());
                                if (!optThiSinh.isPresent()) {
                                    skipped++;
                                    publish("Dong " + excelRow + ": khong tim thay thi sinh theo CCCD -> " + cccd);
                                    continue;
                                }

                                ThiSinh thiSinh = optThiSinh.get();
                                String tenThanhTich = buildTenThanhTich(nhom, cap, loaiGiai);
                                String ghiChu = buildGhiChu(diemCongMon, diemCongKhongMon, coChungChi);

                                ThiSinhThanhTich existing = findExisting(
                                        thiSinh.getThisinhId(),
                                        nhom,
                                        cap,
                                        loaiGiai,
                                        maMon
                                );

                                if (existing == null) {
                                    ThiSinhThanhTich entity = new ThiSinhThanhTich();
                                    entity.setThiSinh(thiSinh);
                                    entity.setNhomThanhTich(trimToNull(nhom));
                                    entity.setCapThanhTich(trimToNull(cap));
                                    entity.setLoaiGiai(trimToNull(loaiGiai));
                                    entity.setTenThanhTich(tenThanhTich);
                                    entity.setMonDatGiai(trimToNull(maMon));
                                    entity.setLinhVuc(null);
                                    entity.setNamDatGiai(DEFAULT_NAM_DAT_GIAI);
                                    entity.setDonViToChuc(null);
                                    entity.setSoHieuMinhChung(null);
                                    entity.setIsHopLe(Boolean.TRUE);
                                    entity.setTrangThaiXacMinh("CHUA_XAC_MINH");
                                    entity.setGhiChu(ghiChu);

                                    thanhTichService.save(entity);
                                    inserted++;

                                    publish("Dong " + excelRow + " | " + cccd + ": them moi thanh tich - " + safe(thiSinh.getHoVaTen()));
                                } else {
                                    existing.setNhomThanhTich(trimToNull(nhom));
                                    existing.setCapThanhTich(trimToNull(cap));
                                    existing.setLoaiGiai(trimToNull(loaiGiai));
                                    existing.setTenThanhTich(tenThanhTich);
                                    existing.setMonDatGiai(trimToNull(maMon));
                                    if (existing.getNamDatGiai() == null) {
                                        existing.setNamDatGiai(DEFAULT_NAM_DAT_GIAI);
                                    }
                                    existing.setIsHopLe(Boolean.TRUE);
                                    if (isBlank(existing.getTrangThaiXacMinh())) {
                                        existing.setTrangThaiXacMinh("CHUA_XAC_MINH");
                                    }
                                    existing.setGhiChu(ghiChu);

                                    thanhTichService.update(existing);
                                    updated++;

                                    publish("Dong " + excelRow + " | " + cccd + ": cap nhat thanh tich - " + safe(thiSinh.getHoVaTen()));
                                }
                            } catch (Exception ex) {
                                error++;
                                publish("Dong " + excelRow + ": loi -> " + ex.getMessage());
                            }
                        }
                    }
                } catch (Exception ex) {
                    publish("Loi tong the: " + ex.getMessage());
                }

                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String msg : chunks) {
                    log(msg);
                }
            }

            @Override
            protected void done() {
                btnImport.setEnabled(true);
                btnSelect.setEnabled(true);

                log("----------------------------------------");
                log("Tong dong doc: " + total);
                log("Them moi: " + inserted);
                log("Cap nhat: " + updated);
                log("Bo qua: " + skipped);
                log("Loi: " + error);

                JOptionPane.showMessageDialog(
                        ThiSinhThanhTichImportPanel.this,
                        "Import xong!\nThem moi: " + inserted +
                                "\nCap nhat: " + updated +
                                "\nBo qua: " + skipped +
                                "\nLoi: " + error,
                        "Ket qua import",
                        error > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE
                );
            }
        };

        worker.execute();
    }

    private ThiSinhThanhTich findExisting(Integer thisinhId, String nhom, String cap, String loaiGiai, String maMon) {
        List<ThiSinhThanhTich> list = thanhTichService.findByThiSinhId(thisinhId);

        String nhomN = normalize(nhom);
        String capN = normalize(cap);
        String loaiN = normalize(loaiGiai);
        String monN = normalize(maMon);

        for (ThiSinhThanhTich item : list) {
            if (normalize(item.getNhomThanhTich()).equals(nhomN)
                    && normalize(item.getCapThanhTich()).equals(capN)
                    && normalize(item.getLoaiGiai()).equals(loaiN)
                    && normalize(item.getMonDatGiai()).equals(monN)) {
                return item;
            }
        }
        return null;
    }

    private String buildTenThanhTich(String nhom, String cap, String loaiGiai) {
        StringBuilder sb = new StringBuilder();

        if (!isBlank(nhom)) sb.append(nhom.trim());
        if (!isBlank(cap)) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(cap.trim());
        }
        if (!isBlank(loaiGiai)) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(loaiGiai.trim());
        }

        return sb.length() == 0 ? null : sb.toString();
    }

    private String buildGhiChu(String diemCongMon, String diemCongKhongMon, String coChungChi) {
        StringBuilder sb = new StringBuilder();

        if (!isBlank(diemCongMon)) {
            sb.append("excel_cong_mon=").append(diemCongMon.trim());
        }
        if (!isBlank(diemCongKhongMon)) {
            if (sb.length() > 0) sb.append("; ");
            sb.append("excel_cong_khong_mon=").append(diemCongKhongMon.trim());
        }
        if (!isBlank(coChungChi)) {
            if (sb.length() > 0) sb.append("; ");
            sb.append("co_cc=").append(coChungChi.trim());
        }

        return sb.length() == 0 ? null : sb.toString();
    }

    private String readCell(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex, MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";

        DataFormatter formatter = new DataFormatter(Locale.US);
        return formatter.formatCellValue(cell).trim();
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int i = 0; i <= 8; i++) {
            if (!isBlank(readCell(row, i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String value = s.trim();
        return value.isEmpty() ? null : value;
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void log(String message) {
        taLog.append(message + "\n");
        taLog.setCaretPosition(taLog.getDocument().getLength());
    }
}