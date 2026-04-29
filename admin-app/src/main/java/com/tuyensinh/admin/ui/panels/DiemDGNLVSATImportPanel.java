package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.DiemThi;
import com.tuyensinh.entity.Mon;
import com.tuyensinh.entity.PhuongThuc;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.service.DiemThiService;
import com.tuyensinh.service.ThiSinhService;
import com.tuyensinh.dao.MonDao;
import com.tuyensinh.dao.PhuongThucDao;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DiemDGNLVSATImportPanel extends JPanel {

    /**
     * Luu y:
     * - nam_thi trong file nguon la 2025
     * - nhung nam_tuyensinh trong he thong hien dang xet theo chu ky 2025
     * => phai luu nam_tuyensinh = 2026 de TinhDiemService / XetTuyenEngine tim thay
     */
    private static final short DEFAULT_NAM_TUYEN_SINH = 2025;
    private static final String DEFAULT_GHI_CHU_DGNL = "Import tu Diem DGNL VSAT - 0908.xlsx - DGNL";
    private static final String DEFAULT_GHI_CHU_VSAT = "Import tu Diem DGNL VSAT - 0908.xlsx - VSAT";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d/M/yyyy");

    private final MainFrame mainFrame;
    private final ThiSinhService thiSinhService = new ThiSinhService();
    private final DiemThiService diemThiService = new DiemThiService();
    private final MonDao monDao = new MonDao();
    private final PhuongThucDao phuongThucDao = new PhuongThucDao();

    private JTextArea taLog;
    private JButton btnSelect;
    private JButton btnImport;
    private JLabel lblFile;
    private JFileChooser fileChooser;
    private File selectedFile;

    public DiemDGNLVSATImportPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.add(new JLabel("Import diem DGNL / VSAT tu file Diem DGNL VSAT - 0908.xlsx"));
        add(header, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 6, 6));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Thong tin file"));
        infoPanel.add(new JLabel("File da chon:"));

        lblFile = new JLabel("Chua chon file...");
        lblFile.setForeground(Color.BLUE);
        infoPanel.add(lblFile);

        btnSelect = new JButton("Chon file Excel");
        btnSelect.addActionListener(e -> selectFile());
        infoPanel.add(btnSelect);

        btnImport = new JButton("Import diem DGNL / VSAT");
        btnImport.setEnabled(false);
        btnImport.addActionListener(e -> importData());
        infoPanel.add(btnImport);

        JTextArea taTemplate = new JTextArea(
                "Nguon import dung cho buoc nay: Diem DGNL VSAT - 0908.xlsx\n\n" +
                        "File gom 2 sheet:\n" +
                        "- DGNL: moi dong = 1 lan thi DGNL\n" +
                        "- VSAT: moi dong = 1 mon thi VSAT\n\n" +
                        "Quy tac importer:\n" +
                        "1) DGNL:\n" +
                        "   - Tim thi sinh theo cot CMND\n" +
                        "   - Neu 1 thi sinh co nhieu dot thi -> lay DIEM cao nhat\n" +
                        "   - Luu vao xt_diemthi (phuong thuc DGNL)\n" +
                        "   - Luu 1 dong xt_diemthi_chitiet voi mon NL1\n\n" +
                        "2) VSAT:\n" +
                        "   - Tim thi sinh theo cot CMND\n" +
                        "   - Gom theo thi sinh + mon\n" +
                        "   - Neu 1 thi sinh thi lai cung mon -> lay DIEM cao nhat\n" +
                        "   - Luu 1 xt_diemthi/phuong thuc VSAT cho moi thi sinh\n" +
                        "   - Luu nhieu xt_diemthi_chitiet theo cac mon tot nhat\n\n" +
                        "3) Nam tuyen sinh luu trong he thong = 2025\n" +
                        "   (khong luu theo NAMTHI=2025 trong file nguon)\n\n" +
                        "Map mon VSAT:\n" +
                        "- TO_VS, M1 -> TO\n" +
                        "- LI_VS, M2 -> LI\n" +
                        "- HO_VS, M3 -> HO\n" +
                        "- SI_VS, M4 -> SI\n" +
                        "- VA_VS, M5 -> VA\n" +
                        "- SU_VS, M6 -> SU\n" +
                        "- DI_VS, M7 -> DI\n" +
                        "- N1_VS, M8 -> N1\n"
        );
        taTemplate.setEditable(false);
        taTemplate.setFont(new Font("Monospaced", Font.PLAIN, 12));
        taTemplate.setBackground(new Color(245, 245, 245));

        JScrollPane templatePane = new JScrollPane(taTemplate);
        templatePane.setBorder(BorderFactory.createTitledBorder("Huong dan import"));

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
        mainSplit.setResizeWeight(0.45);

        add(mainSplit, BorderLayout.CENTER);
    }

    private void selectFile() {
        fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chon file Diem DGNL VSAT - 0908.xlsx");
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

        SwingWorker<ImportSummary, String> worker = new SwingWorker<>() {
            @Override
            protected ImportSummary doInBackground() {
                return importExcel(selectedFile, this::publish);
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
                try {
                    ImportSummary s = get();
                    JOptionPane.showMessageDialog(
                            DiemDGNLVSATImportPanel.this,
                            s.toHumanText(),
                            "Ket qua import DGNL / VSAT",
                            s.errorRows > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            DiemDGNLVSATImportPanel.this,
                            "Loi import diem DGNL/VSAT: " + e.getMessage(),
                            "Loi",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute();
    }

    private ImportSummary importExcel(File file, Consumer<String> logger) {
        ImportSummary summary = new ImportSummary();
        logger.accept("Bat dau import file: " + file.getName());

        Map<String, ThiSinh> thiSinhByCccd = thiSinhService.findAll().stream()
                .filter(ts -> !isBlank(ts.getCccd()))
                .collect(Collectors.toMap(
                        ts -> normalizeCode(ts.getCccd()),
                        ts -> ts,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<String, Mon> monByCode = monDao.findAll().stream()
                .filter(mon -> !isBlank(mon.getMaMon()))
                .collect(Collectors.toMap(
                        mon -> normalizeCode(mon.getMaMon()),
                        mon -> mon,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        PhuongThuc ptDGNL = phuongThucDao.findByMa(PhuongThuc.DGNL)
                .orElseThrow(() -> new IllegalStateException("Khong tim thay phuong thuc DGNL trong DB."));
        PhuongThuc ptVSAT = phuongThucDao.findByMa(PhuongThuc.VSAT)
                .orElseThrow(() -> new IllegalStateException("Khong tim thay phuong thuc VSAT trong DB."));
        Mon monNL1 = monByCode.get("NL1");
        if (monNL1 == null) {
            throw new IllegalStateException("Khong tim thay mon NL1 trong DB.");
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheetDGNL = workbook.getSheet("DGNL");
            Sheet sheetVSAT = workbook.getSheet("VSAT");

            if (sheetDGNL == null) {
                throw new IllegalStateException("Khong tim thay sheet 'DGNL' trong file.");
            }
            if (sheetVSAT == null) {
                throw new IllegalStateException("Khong tim thay sheet 'VSAT' trong file.");
            }

            Map<String, DgnlRecord> bestDgnlByCccd = readBestDGNL(sheetDGNL, summary, logger);
            Map<String, Map<String, VsatRecord>> bestVsatByCccd = readBestVSAT(sheetVSAT, summary, logger);

            logger.accept("Bat dau luu du lieu DGNL...");
            for (Map.Entry<String, DgnlRecord> entry : bestDgnlByCccd.entrySet()) {
                String cccd = entry.getKey();
                DgnlRecord best = entry.getValue();

                try {
                    ThiSinh thiSinh = thiSinhByCccd.get(cccd);
                    if (thiSinh == null) {
                        summary.skippedRows++;
                        logger.accept("DGNL | " + cccd + ": bo qua vi khong tim thay thi sinh trong DB.");
                        continue;
                    }

                    LinkedHashMap<Mon, BigDecimal> diemMap = new LinkedHashMap<>();
                    diemMap.put(monNL1, best.diem);

                    String ghiChu = buildDgnlNote(best);

                    DiemThi saved = diemThiService.importOrReplaceScoreSheet(
                            thiSinh,
                            ptDGNL,
                            DEFAULT_NAM_TUYEN_SINH,
                            thiSinh.getSobaodanh(),
                            ghiChu,
                            diemMap
                    );

                    summary.importedDgnl++;
                    logger.accept("DGNL | " + cccd + ": da ghi de diem DGNL = "
                            + best.diem.toPlainString()
                            + " (dot " + safe(best.dotThi) + ", " + safe(best.ngayThiRaw) + ")"
                            + " -> diemthi_id=" + saved.getDiemthiId());
                } catch (Exception ex) {
                    summary.errorRows++;
                    logger.accept("DGNL | " + cccd + ": loi -> " + ex.getMessage());
                }
            }

            logger.accept("Bat dau luu du lieu VSAT...");
            for (Map.Entry<String, Map<String, VsatRecord>> entry : bestVsatByCccd.entrySet()) {
                String cccd = entry.getKey();
                Map<String, VsatRecord> bestByMon = entry.getValue();

                try {
                    ThiSinh thiSinh = thiSinhByCccd.get(cccd);
                    if (thiSinh == null) {
                        summary.skippedRows++;
                        logger.accept("VSAT | " + cccd + ": bo qua vi khong tim thay thi sinh trong DB.");
                        continue;
                    }

                    LinkedHashMap<Mon, BigDecimal> diemMap = new LinkedHashMap<>();
                    for (Map.Entry<String, VsatRecord> monEntry : bestByMon.entrySet()) {
                        String maMon = monEntry.getKey();
                        VsatRecord best = monEntry.getValue();

                        Mon mon = monByCode.get(normalizeCode(maMon));
                        if (mon == null) {
                            logger.accept("VSAT | " + cccd + ": khong tim thay mon '" + maMon + "' trong DB, bo qua mon nay.");
                            continue;
                        }
                        diemMap.put(mon, best.diem);
                    }

                    if (diemMap.isEmpty()) {
                        summary.skippedRows++;
                        logger.accept("VSAT | " + cccd + ": bo qua vi khong con mon hop le de luu.");
                        continue;
                    }

                    String ghiChu = DEFAULT_GHI_CHU_VSAT + ";soMon=" + diemMap.size();

                    DiemThi saved = diemThiService.importOrReplaceScoreSheet(
                            thiSinh,
                            ptVSAT,
                            DEFAULT_NAM_TUYEN_SINH,
                            thiSinh.getSobaodanh(),
                            ghiChu,
                            diemMap
                    );

                    summary.importedVsat++;
                    logger.accept("VSAT | " + cccd + ": da ghi de " + diemMap.size()
                            + " mon VSAT tot nhat -> diemthi_id=" + saved.getDiemthiId());
                } catch (Exception ex) {
                    summary.errorRows++;
                    logger.accept("VSAT | " + cccd + ": loi -> " + ex.getMessage());
                }
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }

        logger.accept("Hoan tat import. " + summary.toOneLine());
        return summary;
    }

    private Map<String, DgnlRecord> readBestDGNL(Sheet sheet, ImportSummary summary, Consumer<String> logger) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalStateException("Sheet DGNL khong co dong header.");
        }

        Map<String, Integer> headerMap = buildHeaderMap(headerRow);
        validateRequiredHeaders(headerMap, "CMND", "MAMONTHI", "DIEM", "THANGDIEM", "DOTTHI", "MADOTTHI", "NGAYTHI", "NAMTHI");

        Map<String, DgnlRecord> bestByCccd = new LinkedHashMap<>();
        int lastRow = sheet.getLastRowNum();
        logger.accept("Doc sheet DGNL - so dong du lieu uoc tinh: " + lastRow);

        for (int i = 1; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            summary.totalRowsDgnl++;
            int excelRow = i + 1;

            try {
                String cccd = normalizeCode(readCellAsText(row, headerMap.get("CMND")));
                String maMonThi = normalizeCode(readCellAsText(row, headerMap.get("MAMONTHI")));
                BigDecimal diem = readDecimal(row, headerMap.get("DIEM"));
                BigDecimal thangDiem = readDecimal(row, headerMap.get("THANGDIEM"));
                String dotThi = readCellAsText(row, headerMap.get("DOTTHI"));
                String maDotThi = readCellAsText(row, headerMap.get("MADOTTHI"));
                String ngayThi = readCellAsText(row, headerMap.get("NGAYTHI"));
                String namThi = readCellAsText(row, headerMap.get("NAMTHI"));

                if (isBlank(cccd)) {
                    summary.skippedRows++;
                    logger.accept("DGNL dong " + excelRow + ": bo qua vi thieu CMND.");
                    continue;
                }

                if (!"DGNL".equals(maMonThi)) {
                    summary.skippedRows++;
                    logger.accept("DGNL dong " + excelRow + " | " + cccd + ": bo qua vi MAMONTHI khong phai DGNL.");
                    continue;
                }

                if (diem == null) {
                    summary.skippedRows++;
                    logger.accept("DGNL dong " + excelRow + " | " + cccd + ": bo qua vi thieu DIEM.");
                    continue;
                }

                DgnlRecord current = new DgnlRecord();
                current.cccd = cccd;
                current.diem = diem;
                current.thangDiem = thangDiem;
                current.dotThi = dotThi;
                current.maDotThi = maDotThi;
                current.ngayThiRaw = ngayThi;
                current.namThi = namThi;
                current.ngayThi = parseDateQuietly(ngayThi);

                DgnlRecord old = bestByCccd.get(cccd);
                if (old == null || isBetterDgnl(current, old)) {
                    bestByCccd.put(cccd, current);
                }
            } catch (Exception ex) {
                summary.errorRows++;
                logger.accept("DGNL dong " + excelRow + ": loi -> " + ex.getMessage());
            }
        }

        logger.accept("DGNL: tim duoc " + bestByCccd.size() + " thi sinh co diem tot nhat.");
        return bestByCccd;
    }

    private Map<String, Map<String, VsatRecord>> readBestVSAT(Sheet sheet, ImportSummary summary, Consumer<String> logger) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalStateException("Sheet VSAT khong co dong header.");
        }

        Map<String, Integer> headerMap = buildHeaderMap(headerRow);
        validateRequiredHeaders(headerMap, "CMND", "MAMONTHI", "TENMONTHI", "DIEM", "THANGDIEM", "DOTTHI", "MADOTTHI", "NGAYTHI", "NAMTHI");

        Map<String, Map<String, VsatRecord>> bestByCccd = new LinkedHashMap<>();
        int lastRow = sheet.getLastRowNum();
        logger.accept("Doc sheet VSAT - so dong du lieu uoc tinh: " + lastRow);

        for (int i = 1; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            summary.totalRowsVsat++;
            int excelRow = i + 1;

            try {
                String cccd = normalizeCode(readCellAsText(row, headerMap.get("CMND")));
                String maMonThi = readCellAsText(row, headerMap.get("MAMONTHI"));
                String tenMonThi = readCellAsText(row, headerMap.get("TENMONTHI"));
                BigDecimal diem = readDecimal(row, headerMap.get("DIEM"));
                BigDecimal thangDiem = readDecimal(row, headerMap.get("THANGDIEM"));
                String dotThi = readCellAsText(row, headerMap.get("DOTTHI"));
                String maDotThi = readCellAsText(row, headerMap.get("MADOTTHI"));
                String ngayThi = readCellAsText(row, headerMap.get("NGAYTHI"));
                String namThi = readCellAsText(row, headerMap.get("NAMTHI"));

                if (isBlank(cccd)) {
                    summary.skippedRows++;
                    logger.accept("VSAT dong " + excelRow + ": bo qua vi thieu CMND.");
                    continue;
                }

                if (diem == null) {
                    summary.skippedRows++;
                    logger.accept("VSAT dong " + excelRow + " | " + cccd + ": bo qua vi thieu DIEM.");
                    continue;
                }

                String maMonHeThong = resolveVsatMonCode(maMonThi, tenMonThi);
                if (isBlank(maMonHeThong)) {
                    summary.skippedRows++;
                    logger.accept("VSAT dong " + excelRow + " | " + cccd + ": khong map duoc mon tu MAMONTHI="
                            + maMonThi + ", TENMONTHI=" + tenMonThi);
                    continue;
                }

                VsatRecord current = new VsatRecord();
                current.cccd = cccd;
                current.maMonHeThong = maMonHeThong;
                current.maMonThiNguon = maMonThi;
                current.tenMonThiNguon = tenMonThi;
                current.diem = diem;
                current.thangDiem = thangDiem;
                current.dotThi = dotThi;
                current.maDotThi = maDotThi;
                current.ngayThiRaw = ngayThi;
                current.namThi = namThi;
                current.ngayThi = parseDateQuietly(ngayThi);

                Map<String, VsatRecord> byMon = bestByCccd.computeIfAbsent(cccd, k -> new LinkedHashMap<>());
                VsatRecord old = byMon.get(maMonHeThong);
                if (old == null || isBetterVsat(current, old)) {
                    byMon.put(maMonHeThong, current);
                }

            } catch (Exception ex) {
                summary.errorRows++;
                logger.accept("VSAT dong " + excelRow + ": loi -> " + ex.getMessage());
            }
        }

        logger.accept("VSAT: tim duoc " + bestByCccd.size() + " thi sinh co it nhat 1 mon VSAT hop le.");
        return bestByCccd;
    }

    private boolean isBetterDgnl(DgnlRecord a, DgnlRecord b) {
        int cmp = nullSafeCompare(a.diem, b.diem);
        if (cmp != 0) return cmp > 0;

        cmp = nullSafeCompare(a.ngayThi, b.ngayThi);
        if (cmp != 0) return cmp > 0;

        return safe(a.dotThi).compareTo(safe(b.dotThi)) > 0;
    }

    private boolean isBetterVsat(VsatRecord a, VsatRecord b) {
        int cmp = nullSafeCompare(a.diem, b.diem);
        if (cmp != 0) return cmp > 0;

        cmp = nullSafeCompare(a.ngayThi, b.ngayThi);
        if (cmp != 0) return cmp > 0;

        return safe(a.dotThi).compareTo(safe(b.dotThi)) > 0;
    }

    private String buildDgnlNote(DgnlRecord r) {
        String note = DEFAULT_GHI_CHU_DGNL
                + ";namThi=" + safe(r.namThi)
                + ";dot=" + safe(r.dotThi)
                + ";maDot=" + safe(r.maDotThi);
        if (!isBlank(r.ngayThiRaw)) {
            note += ";ngay=" + r.ngayThiRaw;
        }
        return note.length() > 250 ? note.substring(0, 250) : note;
    }

    private String resolveVsatMonCode(String maMonThi, String tenMonThi) {
        String ma = normalizeCode(maMonThi);
        String ten = normalizeVietnamese(tenMonThi);

        if ("TO_VS".equals(ma) || "M1".equals(ma) || ten.contains("TOAN")) return "TO";
        if ("LI_VS".equals(ma) || "M2".equals(ma) || ten.contains("VATLI") || ten.contains("VATLY")) return "LI";
        if ("HO_VS".equals(ma) || "M3".equals(ma) || ten.contains("HOAHOC") || ten.equals("HOA")) return "HO";
        if ("SI_VS".equals(ma) || "M4".equals(ma) || ten.contains("SINHHOC") || ten.equals("SINH")) return "SI";
        if ("VA_VS".equals(ma) || "M5".equals(ma) || ten.contains("NGUVAN") || ten.equals("VAN")) return "VA";
        if ("SU_VS".equals(ma) || "M6".equals(ma) || ten.contains("LICHSU")) return "SU";
        if ("DI_VS".equals(ma) || "M7".equals(ma) || ten.contains("DIALI") || ten.contains("DIALY")) return "DI";
        if ("N1_VS".equals(ma) || "M8".equals(ma) || ten.contains("TIENGANH") || ten.equals("ANH")) return "N1";

        return null;
    }

    private Map<String, Integer> buildHeaderMap(Row headerRow) {
        Map<String, Integer> map = new LinkedHashMap<>();
        short lastCellNum = headerRow.getLastCellNum();
        for (int i = 0; i < lastCellNum; i++) {
            String header = readCellAsText(headerRow, i);
            if (!isBlank(header)) {
                map.put(normalizeHeader(header), i);
            }
        }
        return map;
    }

    private void validateRequiredHeaders(Map<String, Integer> headerMap, String... required) {
        for (String key : required) {
            if (!headerMap.containsKey(normalizeHeader(key))) {
                throw new IllegalStateException("Thieu cot bat buoc trong file Excel: " + key);
            }
        }
    }

    private BigDecimal readDecimal(Row row, Integer cellIndex) {
        if (row == null || cellIndex == null) {
            return null;
        }
        Cell cell = row.getCell(cellIndex, MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP);
            }
            if (cell.getCellType() == CellType.STRING) {
                String raw = cell.getStringCellValue();
                if (isBlank(raw)) return null;
                String normalized = raw.trim().replace(",", ".");
                return new BigDecimal(normalized).setScale(2, RoundingMode.HALF_UP);
            }
            if (cell.getCellType() == CellType.FORMULA) {
                try {
                    return BigDecimal.valueOf(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP);
                } catch (Exception ignore) {
                    String raw = cell.getStringCellValue();
                    if (isBlank(raw)) return null;
                    return new BigDecimal(raw.trim().replace(",", ".")).setScale(2, RoundingMode.HALF_UP);
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Gia tri diem khong hop le tai cot " + cellIndex + ": " + readCellAsText(row, cellIndex));
        }
        return null;
    }

    private String readCellAsText(Row row, Integer cellIndex) {
        if (row == null || cellIndex == null) {
            return "";
        }
        Cell cell = row.getCell(cellIndex, MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return "";
        }

        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            if (i < 0) continue;
            Cell cell = row.getCell(i, MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && !readCellAsText(row, i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private LocalDate parseDateQuietly(String raw) {
        if (isBlank(raw)) return null;
        try {
            return LocalDate.parse(raw.trim(), DATE_FMT);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private int nullSafeCompare(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }

    private int nullSafeCompare(LocalDate a, LocalDate b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }

    private String normalizeHeader(String value) {
        return normalizeCode(value);
    }

    private String normalizeCode(String value) {
        if (value == null) return null;
        String noAccent = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return noAccent.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeVietnamese(String value) {
        if (value == null) return "";
        String noAccent = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace("Đ", "D")
                .replace("đ", "d");
        return noAccent.toUpperCase(Locale.ROOT).replaceAll("[\\s_\\-./:()]+", "");
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void log(String msg) {
        String ts = java.time.LocalDateTime.now().toString().substring(11, 19);
        taLog.append("[" + ts + "] " + msg + "\n");
        taLog.setCaretPosition(taLog.getDocument().getLength());
    }

    private static final class DgnlRecord {
        String cccd;
        BigDecimal diem;
        BigDecimal thangDiem;
        String dotThi;
        String maDotThi;
        String ngayThiRaw;
        LocalDate ngayThi;
        String namThi;
    }

    private static final class VsatRecord {
        String cccd;
        String maMonHeThong;
        String maMonThiNguon;
        String tenMonThiNguon;
        BigDecimal diem;
        BigDecimal thangDiem;
        String dotThi;
        String maDotThi;
        String ngayThiRaw;
        LocalDate ngayThi;
        String namThi;
    }

    private static final class ImportSummary {
        int totalRowsDgnl;
        int totalRowsVsat;
        int skippedRows;
        int errorRows;
        int importedDgnl;
        int importedVsat;

        String toOneLine() {
            return "DGNL rows=" + totalRowsDgnl
                    + ", VSAT rows=" + totalRowsVsat
                    + ", skipped=" + skippedRows
                    + ", error=" + errorRows
                    + ", diemThi DGNL=" + importedDgnl
                    + ", diemThi VSAT=" + importedVsat;
        }

        String toHumanText() {
            return "Tong dong DGNL doc tu file: " + totalRowsDgnl + "\n"
                    + "Tong dong VSAT doc tu file: " + totalRowsVsat + "\n"
                    + "So ban ghi DGNL da ghi de/tao moi: " + importedDgnl + "\n"
                    + "So ban ghi VSAT da ghi de/tao moi: " + importedVsat + "\n"
                    + "Dong bo qua: " + skippedRows + "\n"
                    + "Dong loi: " + errorRows + "\n\n"
                    + "Luu y:\n"
                    + "- DGNL: moi thi sinh chi luu 1 diem tot nhat (mon NL1)\n"
                    + "- VSAT: moi thi sinh chi luu 1 bo diem VSAT, moi mon lay diem cao nhat\n"
                    + "- nam_tuyensinh da luu = 2025";
        }
    }
}