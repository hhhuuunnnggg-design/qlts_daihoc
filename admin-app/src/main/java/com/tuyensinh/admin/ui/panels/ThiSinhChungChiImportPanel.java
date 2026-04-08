package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.entity.ThiSinhChungChi;
import com.tuyensinh.service.ThiSinhChungChiService;
import com.tuyensinh.service.ThiSinhService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ThiSinhChungChiImportPanel extends JPanel {

    private final MainFrame mainFrame;
    private final ThiSinhChungChiService chungChiService = new ThiSinhChungChiService();
    private final ThiSinhService thiSinhService = new ThiSinhService();

    private JTextArea taLog;
    private JButton btnSelect;
    private JButton btnImport;
    private JLabel lblFile;
    private JCheckBox chkClearOld;
    private JFileChooser fileChooser;

    private File selectedFile;

    public ThiSinhChungChiImportPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.add(new JLabel("Import chung chi ngoai ngu tu file Excel (.xlsx)"));
        add(header, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 6, 6));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Thong tin file"));
        infoPanel.add(new JLabel("File da chon:"));

        lblFile = new JLabel("Chua chon file...");
        lblFile.setForeground(Color.BLUE);
        infoPanel.add(lblFile);

        chkClearOld = new JCheckBox("Xoa du lieu cu bang xt_thisinh_chungchi truoc khi import", false);
        infoPanel.add(chkClearOld);

        btnSelect = new JButton("Chon file Excel");
        btnSelect.addActionListener(e -> selectFile());
        infoPanel.add(btnSelect);

        btnImport = new JButton("Import du lieu");
        btnImport.setEnabled(false);
        btnImport.addActionListener(e -> importData());
        infoPanel.add(btnImport);

        JTextArea taTemplate = new JTextArea(
                "File hien tai dang import theo mau Ds quy doi tieng Anh.xlsx\n\n" +
                        "Sheet dung: import_xettuyen\n\n" +
                        "Cac cot duoc dung:\n" +
                        "B = CCCD\n" +
                        "C = Chung chi ngoai ngu\n" +
                        "D = Diem / Bac chung chi\n" +
                        "E = Diem quy doi (chi luu vao ghi chu de doi chieu)\n" +
                        "F = Diem cong (chi luu vao ghi chu de doi chieu)\n\n" +
                        "Rule import:\n" +
                        "- Tim thi sinh theo CCCD\n" +
                        "- Loai chung chi duoc chuan hoa: IELTS, VSTEP, APTIS, TOEFL_ITP, TOEIC_4KYNANG, PTE, LINGUASKILL, KHAC\n" +
                        "- Bac chung chi luu nguyen van cot D\n" +
                        "- Diem goc chi parse khi cot D la so hop le\n" +
                        "- Neu trung theo (thi sinh + loai + ten + bac) thi update dong cu\n" +
                        "- Gia tri Excel E/F duoc luu vao ghi_chu de sau nay test service\n"
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
        topSplit.setResizeWeight(0.28);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, logPane);
        mainSplit.setResizeWeight(0.48);

        add(mainSplit, BorderLayout.CENTER);
    }

    private void selectFile() {
        fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chon file Excel");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

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
                        List<ThiSinhChungChi> allOld = new ArrayList<>(chungChiService.findAll());
                        publish("Dang xoa " + allOld.size() + " dong chung chi cu...");
                        for (ThiSinhChungChi item : allOld) {
                            chungChiService.delete(item);
                        }
                        publish("Da xoa du lieu cu.");
                    }

                    try (FileInputStream fis = new FileInputStream(selectedFile);
                         Workbook workbook = new XSSFWorkbook(fis)) {

                        Sheet sheet = workbook.getSheet("import_xettuyen");
                        if (sheet == null) {
                            sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
                        }
                        if (sheet == null) {
                            publish("Khong tim thay sheet trong file.");
                            return null;
                        }

                        int lastRow = sheet.getLastRowNum();
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
                                String tenChungChiRaw = readCell(row, 2);
                                String bacRaw = readCell(row, 3);
                                String diemQuyDoi = readCell(row, 4);
                                String diemCong = readCell(row, 5);

                                if (isBlank(cccd) || isBlank(tenChungChiRaw)) {
                                    skipped++;
                                    publish("Dong " + excelRow + ": bo qua vi thieu CCCD/ten chung chi.");
                                    continue;
                                }

                                Optional<ThiSinh> optThiSinh = thiSinhService.findByCccd(cccd.trim());
                                if (!optThiSinh.isPresent()) {
                                    skipped++;
                                    publish("Dong " + excelRow + ": khong tim thay thi sinh theo CCCD -> " + cccd);
                                    continue;
                                }

                                ThiSinh thiSinh = optThiSinh.get();
                                String loai = detectLoaiChungChi(tenChungChiRaw);
                                BigDecimal diemGoc = parseBigDecimalFlexible(bacRaw);
                                String ghiChu = buildGhiChu(diemQuyDoi, diemCong);

                                ThiSinhChungChi existing = findExisting(
                                        thiSinh.getThisinhId(),
                                        loai,
                                        tenChungChiRaw,
                                        bacRaw
                                );

                                if (existing == null) {
                                    ThiSinhChungChi entity = new ThiSinhChungChi();
                                    entity.setThiSinh(thiSinh);
                                    entity.setLoaiChungChi(loai);
                                    entity.setTenChungChi(trimToNull(tenChungChiRaw));
                                    entity.setBacChungChi(trimToNull(bacRaw));
                                    entity.setDiemGoc(diemGoc);
                                    entity.setIsHopLe(Boolean.TRUE);
                                    entity.setTrangThaiXacMinh("CHUA_XAC_MINH");
                                    entity.setGhiChu(ghiChu);
                                    chungChiService.save(entity);

                                    inserted++;
                                    publish("Dong " + excelRow + " | " + cccd + ": them moi " + loai + " - " + safe(thiSinh.getHoVaTen()));
                                } else {
                                    existing.setLoaiChungChi(loai);
                                    existing.setTenChungChi(trimToNull(tenChungChiRaw));
                                    existing.setBacChungChi(trimToNull(bacRaw));
                                    existing.setDiemGoc(diemGoc);
                                    existing.setIsHopLe(Boolean.TRUE);
                                    if (isBlank(existing.getTrangThaiXacMinh())) {
                                        existing.setTrangThaiXacMinh("CHUA_XAC_MINH");
                                    }
                                    existing.setGhiChu(ghiChu);
                                    chungChiService.update(existing);

                                    updated++;
                                    publish("Dong " + excelRow + " | " + cccd + ": cap nhat " + loai + " - " + safe(thiSinh.getHoVaTen()));
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
                        ThiSinhChungChiImportPanel.this,
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

    private ThiSinhChungChi findExisting(Integer thisinhId, String loai, String ten, String bac) {
        List<ThiSinhChungChi> list = chungChiService.findByThiSinhId(thisinhId);
        String loaiN = normalize(loai);
        String tenN = normalize(ten);
        String bacN = normalize(bac);

        for (ThiSinhChungChi item : list) {
            if (normalize(item.getLoaiChungChi()).equals(loaiN)
                    && normalize(item.getTenChungChi()).equals(tenN)
                    && normalize(item.getBacChungChi()).equals(bacN)) {
                return item;
            }
        }
        return null;
    }

    private String buildGhiChu(String diemQuyDoi, String diemCong) {
        StringBuilder sb = new StringBuilder();

        if (!isBlank(diemQuyDoi)) {
            sb.append("excel_diem_quy_doi=").append(diemQuyDoi.trim());
        }
        if (!isBlank(diemCong)) {
            if (sb.length() > 0) sb.append("; ");
            sb.append("excel_diem_cong=").append(diemCong.trim());
        }

        return sb.length() == 0 ? null : sb.toString();
    }

    private String detectLoaiChungChi(String tenChungChi) {
        String text = normalize(tenChungChi);

        if (text.contains("ielts")) return "IELTS";
        if (text.contains("vstep")) return "VSTEP";
        if (text.contains("aptis")) return "APTIS";
        if (text.contains("toefl")) return "TOEFL_ITP";
        if (text.contains("toeic")) return "TOEIC_4KYNANG";
        if (text.contains("pearson") || text.contains("pte") || text.contains("peic")) return "PTE";
        if (text.contains("linguaskill")) return "LINGUASKILL";

        return "KHAC";
    }

    private String readCell(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) return "";

        DataFormatter formatter = new DataFormatter(Locale.US);
        return formatter.formatCellValue(cell).trim();
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int i = 0; i < 6; i++) {
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

    private BigDecimal parseBigDecimalFlexible(String value) {
        String raw = value == null ? "" : value.trim();
        if (raw.isEmpty()) return null;

        raw = raw.replace(",", ".");
        if (!raw.matches("[-+]?\\d+(\\.\\d+)?")) {
            return null;
        }

        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void log(String message) {
        taLog.append(message + "\n");
        taLog.setCaretPosition(taLog.getDocument().getLength());
    }
}