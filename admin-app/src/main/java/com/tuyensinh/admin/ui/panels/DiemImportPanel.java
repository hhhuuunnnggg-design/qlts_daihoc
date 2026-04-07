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
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DiemImportPanel extends JPanel {

    private static final short DEFAULT_NAM_TUYEN_SINH = 2026;
    private static final String DEFAULT_SHEET_NAME = "Sheet1";
    private static final String DEFAULT_GHI_CHU = "Import tu Ds thi sinh.xlsx";

    private final MainFrame mainFrame;
    private final ThiSinhService thiSinhService = new ThiSinhService();
    private final DiemThiService diemThiService = new DiemThiService();
    private final MonDao monDao = new MonDao();
    private final PhuongThucDao phuongThucDao = new PhuongThucDao();

    private JTextArea taLog;
    private JButton btnSelect, btnImport;
    private JLabel lblFile;
    private JFileChooser fileChooser;
    private File selectedFile;

    public DiemImportPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.add(new JLabel("Import diem thi tu file Ds thi sinh.xlsx"));
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

        btnImport = new JButton("Import diem THPT + NK");
        btnImport.setEnabled(false);
        btnImport.addActionListener(e -> importData());
        infoPanel.add(btnImport);

        JTextArea taTemplate = new JTextArea(
                "Nguon import dung cho buoc nay: Ds thi sinh.xlsx\n\n" +
                        "Sheet su dung: Sheet1\n" +
                        "Importer se tu dong tach thanh 2 nhom diem:\n" +
                        "- THPT: TO, VA, LI, HO, SI, SU, DI, KTPL/GDCD, TI, CNCN, CNNN, NN\n" +
                        "- NK: NK1, NK2, NK3, NK4, NK5, NK6\n\n" +
                        "Quy uoc:\n" +
                        "- Tim thi sinh theo CCCD trong file\n" +
                        "- Ngoai ngu lay diem tu cot 'NN' va ma mon tu cot 'Ma mon NN'\n" +
                        "- Neu 'KTPL' trong ma 'GDCD' co diem thi map GDCD -> KTPL\n" +
                        "- Ghi de bo diem cu theo bo khoa: Thi sinh + Phuong thuc + Nam 2026\n" +
                        "- Neu dong co ca diem THPT va NK thi tao/ghi de 2 phuong thuc THPT va NK\n"
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
        topSplit.setResizeWeight(0.25);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, logPane);
        mainSplit.setResizeWeight(0.42);

        add(mainSplit, BorderLayout.CENTER);
    }

    private void selectFile() {
        fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chon file Ds thi sinh.xlsx");
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
                return importExcel(selectedFile, msg -> publish(msg));
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
                            DiemImportPanel.this,
                            s.toHumanText(),
                            "Ket qua import diem",
                            s.errorRows > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DiemImportPanel.this,
                            "Loi import diem: " + e.getMessage(),
                            "Loi",
                            JOptionPane.ERROR_MESSAGE);
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
                .collect(Collectors.toMap(ts -> normalizeCode(ts.getCccd()), ts -> ts, (a, b) -> a, LinkedHashMap::new));

        Map<String, Mon> monByCode = monDao.findAll().stream()
                .filter(mon -> !isBlank(mon.getMaMon()))
                .collect(Collectors.toMap(mon -> normalizeCode(mon.getMaMon()), mon -> mon, (a, b) -> a, LinkedHashMap::new));

        PhuongThuc ptThpt = phuongThucDao.findByMa(PhuongThuc.THPT)
                .orElseThrow(() -> new IllegalStateException("Khong tim thay phuong thuc THPT trong DB."));
        PhuongThuc ptNk = phuongThucDao.findByMa(PhuongThuc.NK)
                .orElseThrow(() -> new IllegalStateException("Khong tim thay phuong thuc NK trong DB."));

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(DEFAULT_SHEET_NAME);
            if (sheet == null) {
                sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            }
            if (sheet == null) {
                throw new IllegalStateException("Khong tim thay sheet nao trong file Excel.");
            }

            logger.accept("Doc sheet: " + sheet.getSheetName());
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalStateException("Sheet khong co dong header.");
            }

            Map<String, Integer> headerMap = buildHeaderMap(headerRow);
            validateRequiredHeaders(headerMap);

            int lastRow = sheet.getLastRowNum();
            logger.accept("Tong so dong du lieu uoc tinh: " + lastRow);

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                summary.totalRows++;
                int excelRow = i + 1;

                try {
                    String cccd = normalizeCode(readCellAsText(row, headerMap.get("CCCD")));
                    if (isBlank(cccd)) {
                        summary.skippedRows++;
                        logger.accept("Dong " + excelRow + ": bo qua vi thieu CCCD.");
                        continue;
                    }

                    ThiSinh thiSinh = thiSinhByCccd.get(cccd);
                    if (thiSinh == null) {
                        summary.skippedRows++;
                        logger.accept("Dong " + excelRow + ": khong tim thay thi sinh theo CCCD = " + cccd);
                        continue;
                    }

                    LinkedHashMap<Mon, BigDecimal> thptScores = extractThptScores(row, headerMap, monByCode, logger, excelRow);
                    LinkedHashMap<Mon, BigDecimal> nkScores = extractNkScores(row, headerMap, monByCode, logger, excelRow);

                    if (thptScores.isEmpty() && nkScores.isEmpty()) {
                        summary.skippedRows++;
                        logger.accept("Dong " + excelRow + " | " + cccd + ": khong co diem THPT/NK hop le, bo qua.");
                        continue;
                    }

                    if (!thptScores.isEmpty()) {
                        DiemThi saved = diemThiService.importOrReplaceScoreSheet(
                                thiSinh,
                                ptThpt,
                                DEFAULT_NAM_TUYEN_SINH,
                                thiSinh.getSobaodanh(),
                                DEFAULT_GHI_CHU + " - THPT",
                                thptScores
                        );
                        summary.importedThpt++;
                        logger.accept("Dong " + excelRow + " | " + cccd + ": da ghi de diem THPT (" + saved.getDanhSachDiemChiTiet().size() + " mon).");
                    }

                    if (!nkScores.isEmpty()) {
                        DiemThi saved = diemThiService.importOrReplaceScoreSheet(
                                thiSinh,
                                ptNk,
                                DEFAULT_NAM_TUYEN_SINH,
                                thiSinh.getSobaodanh(),
                                DEFAULT_GHI_CHU + " - NK",
                                nkScores
                        );
                        summary.importedNk++;
                        logger.accept("Dong " + excelRow + " | " + cccd + ": da ghi de diem NK (" + saved.getDanhSachDiemChiTiet().size() + " mon).");
                    }

                    summary.successRows++;
                } catch (Exception ex) {
                    summary.errorRows++;
                    logger.accept("Dong " + excelRow + ": loi -> " + ex.getMessage());
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }

        logger.accept("Hoan tat import. " + summary.toOneLine());
        return summary;
    }

    private LinkedHashMap<Mon, BigDecimal> extractThptScores(Row row,
                                                             Map<String, Integer> headerMap,
                                                             Map<String, Mon> monByCode,
                                                             Consumer<String> logger,
                                                             int excelRow) {
        LinkedHashMap<Mon, BigDecimal> map = new LinkedHashMap<>();

        addScoreIfPresent(map, monByCode, "TO", readDecimal(row, headerMap.get("TO")), logger, excelRow);
        addScoreIfPresent(map, monByCode, "VA", readDecimal(row, headerMap.get("VA")), logger, excelRow);
        addScoreIfPresent(map, monByCode, "LI", readDecimal(row, headerMap.get("LI")), logger, excelRow);
        addScoreIfPresent(map, monByCode, "HO", readDecimal(row, headerMap.get("HO")), logger, excelRow);
        addScoreIfPresent(map, monByCode, "SI", readDecimal(row, headerMap.get("SI")), logger, excelRow);
        addScoreIfPresent(map, monByCode, "SU", readDecimal(row, headerMap.get("SU")), logger, excelRow);
        addScoreIfPresent(map, monByCode, "DI", readDecimal(row, headerMap.get("DI")), logger, excelRow);
        addScoreIfPresent(map, monByCode, "TI", readDecimal(row, headerMap.get("TI")), logger, excelRow);
        addScoreIfPresent(map, monByCode, "CNCN", readDecimal(row, headerMap.get("CNCN")), logger, excelRow);
        addScoreIfPresent(map, monByCode, "CNNN", readDecimal(row, headerMap.get("CNNN")), logger, excelRow);

        BigDecimal ktpl = readDecimal(row, headerMap.get("KTPL"));
        if (ktpl == null) {
            ktpl = readDecimal(row, headerMap.get("GDCD"));
        }
        addScoreIfPresent(map, monByCode, "KTPL", ktpl, logger, excelRow);

        BigDecimal nn = readDecimal(row, headerMap.get("NN"));
        if (nn != null) {
            String maMonNn = normalizeCode(readCellAsText(row, headerMap.get("MA MON NN")));
            if (isBlank(maMonNn)) {
                maMonNn = "N1";
            }
            addScoreIfPresent(map, monByCode, maMonNn, nn, logger, excelRow);
        }

        return map;
    }

    private LinkedHashMap<Mon, BigDecimal> extractNkScores(Row row,
                                                           Map<String, Integer> headerMap,
                                                           Map<String, Mon> monByCode,
                                                           Consumer<String> logger,
                                                           int excelRow) {
        LinkedHashMap<Mon, BigDecimal> map = new LinkedHashMap<>();
        addScoreIfPresent(map, monByCode, "NK1", readDecimal(row, headerMap.get("NK1")), logger, excelRow);
        addScoreIfPresent(map, monByCode, "NK2", readDecimal(row, headerMap.get("NK2")), logger, excelRow);
        addScoreIfPresent(map, monByCode, "NK3", readDecimal(row, headerMap.get("NK3")), logger, excelRow);
        addScoreIfPresent(map, monByCode, "NK4", readDecimal(row, headerMap.get("NK4")), logger, excelRow);
        addScoreIfPresent(map, monByCode, "NK5", readDecimal(row, headerMap.get("NK5")), logger, excelRow);
        addScoreIfPresent(map, monByCode, "NK6", readDecimal(row, headerMap.get("NK6")), logger, excelRow);
        return map;
    }

    private void addScoreIfPresent(Map<Mon, BigDecimal> target,
                                   Map<String, Mon> monByCode,
                                   String maMon,
                                   BigDecimal diem,
                                   Consumer<String> logger,
                                   int excelRow) {
        if (diem == null) {
            return;
        }
        Mon mon = monByCode.get(normalizeCode(maMon));
        if (mon == null) {
            logger.accept("Dong " + excelRow + ": khong tim thay mon '" + maMon + "' trong DB, bo qua cot nay.");
            return;
        }
        target.put(mon, diem);
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

    private void validateRequiredHeaders(Map<String, Integer> headerMap) {
        String[] required = {"CCCD", "TO", "VA", "LI", "HO", "SI", "SU", "DI", "GDCD", "NN", "MA MON NN", "KTPL", "TI", "CNCN", "CNNN", "NK1", "NK2", "NK3", "NK4", "NK5", "NK6"};
        for (String key : required) {
            if (!headerMap.containsKey(key)) {
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
                return BigDecimal.valueOf(cell.getNumericCellValue()).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
            if (cell.getCellType() == CellType.STRING) {
                String raw = cell.getStringCellValue();
                if (isBlank(raw)) {
                    return null;
                }
                String normalized = raw.trim().replace(",", ".");
                return new BigDecimal(normalized).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
            if (cell.getCellType() == CellType.FORMULA) {
                try {
                    return BigDecimal.valueOf(cell.getNumericCellValue()).setScale(2, BigDecimal.ROUND_HALF_UP);
                } catch (Exception ignore) {
                    String raw = cell.getStringCellValue();
                    if (isBlank(raw)) {
                        return null;
                    }
                    return new BigDecimal(raw.trim().replace(",", ".")).setScale(2, BigDecimal.ROUND_HALF_UP);
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
            if (i < 0) {
                continue;
            }
            Cell cell = row.getCell(i, MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && !readCellAsText(row, i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String normalizeHeader(String value) {
        return normalizeCode(value).replaceAll("\\s+", " ");
    }

    private String normalizeCode(String value) {
        if (value == null) {
            return null;
        }
        String noAccent = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return noAccent.trim().toUpperCase();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void log(String msg) {
        String ts = java.time.LocalDateTime.now().toString().substring(11, 19);
        taLog.append("[" + ts + "] " + msg + "\n");
        taLog.setCaretPosition(taLog.getDocument().getLength());
    }

    private static final class ImportSummary {
        int totalRows;
        int successRows;
        int skippedRows;
        int errorRows;
        int importedThpt;
        int importedNk;

        String toOneLine() {
            return "tong dong=" + totalRows
                    + ", thanh cong=" + successRows
                    + ", bo qua=" + skippedRows
                    + ", loi=" + errorRows
                    + ", ban ghi THPT=" + importedThpt
                    + ", ban ghi NK=" + importedNk;
        }

        String toHumanText() {
            return "Tong dong du lieu: " + totalRows + "\n"
                    + "Dong import thanh cong: " + successRows + "\n"
                    + "Dong bo qua: " + skippedRows + "\n"
                    + "Dong loi: " + errorRows + "\n"
                    + "Bo diem THPT da ghi de/tao moi: " + importedThpt + "\n"
                    + "Bo diem NK da ghi de/tao moi: " + importedNk;
        }
    }
}
