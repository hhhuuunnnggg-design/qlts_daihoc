package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.MaXetTuyenMap;
import com.tuyensinh.entity.NguyenVong;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.service.MaXetTuyenMapService;
import com.tuyensinh.service.NguyenVongService;
import com.tuyensinh.service.ThiSinhService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.text.Normalizer;
import java.util.*;
import java.util.List;

public class NguyenVongImportPanel extends JPanel {

    private final NguyenVongService nguyenVongService = new NguyenVongService();
    private final ThiSinhService thiSinhService = new ThiSinhService();
    private final MaXetTuyenMapService maXetTuyenMapService = new MaXetTuyenMapService();

    private JTextArea taLog;
    private JButton btnSelect;
    private JButton btnImport;
    private JLabel lblFile;
    private File selectedFile;

    public NguyenVongImportPanel(MainFrame mainFrame) {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.add(new JLabel("Import nguyen vong tu file Excel (.xlsx)"));
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

        btnImport = new JButton("Import du lieu");
        btnImport.setEnabled(false);
        btnImport.addActionListener(e -> importData());
        infoPanel.add(btnImport);

        JTextArea taTemplate = new JTextArea(
                "File dang import theo mau Nguyenvong.xlsx\n\n" +
                        "Sheet duoc doc:\n" +
                        "- Sheet1\n" +
                        "- Sheet2\n" +
                        "- Tu bo qua TKchung\n\n" +
                        "Cac cot duoc dung:\n" +
                        "B = CCCD\n" +
                        "C = Thu tu NV\n" +
                        "F = Ma xet tuyen\n" +
                        "G = Ten ma xet tuyen (de ghi log)\n\n" +
                        "Nguyen tac import:\n" +
                        "- Tim thi sinh theo CCCD\n" +
                        "- Tim map theo xt_ma_xettuyen\n" +
                        "- Neu 1 ma xet tuyen co nhieu map, uu tien: THPT > VSAT > DGNL > XTT > NK\n" +
                        "- Neu trung (thi sinh + thu tu) thi update\n" +
                        "- Neu khong tim thay thi sinh hoac ma xet tuyen thi bo qua va ghi log\n"
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
        JFileChooser fileChooser = new JFileChooser();
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

        SwingWorker<Void, String> worker = new SwingWorker<>() {

            int total = 0;
            int inserted = 0;
            int updated = 0;
            int skipped = 0;
            int error = 0;

            @Override
            protected Void doInBackground() {
                publish("Bat dau import file: " + selectedFile.getName());

                try {
                    Map<String, ThiSinh> thiSinhByCccd = loadThiSinhMap();
                    Map<String, List<MaXetTuyenMap>> maXtMap = loadMaXetTuyenMap();

                    Map<String, NguyenVong> existingByThuTu = new LinkedHashMap<>();
                    Map<String, NguyenVong> existingByMaXt = new LinkedHashMap<>();
                    preloadExisting(existingByThuTu, existingByMaXt);

                    publish("So thi sinh trong DB: " + thiSinhByCccd.size());
                    publish("So ma xet tuyen co the map: " + maXtMap.size());
                    publish("So nguyen vong hien co trong DB: " + existingByThuTu.size());

                    try (FileInputStream fis = new FileInputStream(selectedFile);
                         Workbook workbook = new XSSFWorkbook(fis)) {

                        for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                            Sheet sheet = workbook.getSheetAt(s);
                            Header header = detectHeader(sheet);

                            if (header == null) {
                                publish("Bo qua sheet '" + sheet.getSheetName() + "' vi khong dung dinh dang nguyen vong.");
                                continue;
                            }

                            publish("Doc sheet: " + sheet.getSheetName() + " | header tai dong " + (header.rowIndex + 1));

                            int lastRow = sheet.getLastRowNum();
                            for (int i = header.rowIndex + 1; i <= lastRow; i++) {
                                Row row = sheet.getRow(i);
                                if (row == null || isRowEmpty(row)) {
                                    continue;
                                }

                                total++;
                                int excelRow = i + 1;

                                try {
                                    String cccdRaw = readCell(row, header.colCccd);
                                    String thuTuRaw = readCell(row, header.colThuTu);
                                    String maXtRaw = readCell(row, header.colMaXetTuyen);
                                    String tenMaXt = header.colTenMaXetTuyen >= 0
                                            ? readCell(row, header.colTenMaXetTuyen)
                                            : "";

                                    String cccd = normalizeCode(cccdRaw);
                                    Integer thuTu = parseInteger(thuTuRaw);
                                    String maXt = normalizeMaXetTuyen(maXtRaw);

                                    if (isBlank(cccd) || thuTu == null || isBlank(maXt)) {
                                        skipped++;
                                        publish("Sheet " + sheet.getSheetName() + " dong " + excelRow
                                                + ": bo qua vi thieu CCCD / Thu tu NV / Ma XT.");
                                        continue;
                                    }

                                    ThiSinh thiSinh = thiSinhByCccd.get(cccd);
                                    if (thiSinh == null) {
                                        skipped++;
                                        publish("Sheet " + sheet.getSheetName() + " dong " + excelRow
                                                + ": khong tim thay thi sinh CCCD=" + cccdRaw);
                                        continue;
                                    }

                                    List<MaXetTuyenMap> candidates = maXtMap.get(maXt);
                                    if (candidates == null || candidates.isEmpty()) {
                                        skipped++;
                                        publish("Sheet " + sheet.getSheetName() + " dong " + excelRow
                                                + ": khong tim thay map cho ma xet tuyen=" + maXtRaw);
                                        continue;
                                    }

                                    MaXetTuyenMap selectedMap = candidates.get(0);

                                    if (selectedMap.getNganh() == null
                                            || selectedMap.getNganhToHop() == null
                                            || selectedMap.getPhuongThuc() == null) {
                                        skipped++;
                                        publish("Sheet " + sheet.getSheetName() + " dong " + excelRow
                                                + ": map khong day du nganh / to hop / phuong thuc cho ma=" + maXtRaw);
                                        continue;
                                    }

                                    String nvKey = buildThuTuKey(thiSinh.getThisinhId(), thuTu);
                                    String maXtKey = buildMaXtKey(thiSinh.getThisinhId(), selectedMap.getMaXettuyenId());

                                    NguyenVong current = existingByThuTu.get(nvKey);
                                    NguyenVong sameMaXt = existingByMaXt.get(maXtKey);

                                    if (sameMaXt != null && (current == null || !sameEntity(sameMaXt, current))) {
                                        skipped++;
                                        publish("Sheet " + sheet.getSheetName() + " dong " + excelRow
                                                + ": bo qua vi thi sinh da co ma xet tuyen nay o NV khac (ma="
                                                + maXtRaw + ", thu tu=" + thuTu + ").");
                                        continue;
                                    }

                                    boolean isUpdate = current != null;
                                    if (!isUpdate) {
                                        current = new NguyenVong();
                                    } else {
                                        String oldMaXtKey = buildMaXtKey(current);
                                        if (oldMaXtKey != null && !oldMaXtKey.equals(maXtKey)) {
                                            existingByMaXt.remove(oldMaXtKey);
                                        }
                                    }

                                    current.setThiSinh(thiSinh);
                                    current.setThuTu(thuTu);
                                    current.setMaXetTuyenMap(selectedMap);
                                    current.setNganh(selectedMap.getNganh());
                                    current.setNganhToHop(selectedMap.getNganhToHop());
                                    current.setPhuongThuc(selectedMap.getPhuongThuc());

                                    if (current.getKetQua() == null || current.getKetQua().trim().isEmpty()) {
                                        current.setKetQua(NguyenVong.KetQua.CHO_XET);
                                    }

                                    current.setGhiChu(buildGhiChu(maXtRaw, tenMaXt, candidates.size(), selectedMap));

                                    if (!isUpdate) {
                                        NguyenVong saved = nguyenVongService.save(current);
                                        existingByThuTu.put(nvKey, saved);
                                        existingByMaXt.put(maXtKey, saved);
                                        inserted++;

                                        publish("Sheet " + sheet.getSheetName() + " dong " + excelRow
                                                + ": them moi thanh cong | CCCD=" + cccdRaw
                                                + " | NV=" + thuTu + " | Ma XT=" + maXtRaw);
                                    } else {
                                        nguyenVongService.update(current);
                                        existingByThuTu.put(nvKey, current);
                                        existingByMaXt.put(maXtKey, current);
                                        updated++;

                                        publish("Sheet " + sheet.getSheetName() + " dong " + excelRow
                                                + ": cap nhat thanh cong | CCCD=" + cccdRaw
                                                + " | NV=" + thuTu + " | Ma XT=" + maXtRaw);
                                    }

                                } catch (Exception ex) {
                                    error++;
                                    publish("Sheet " + sheet.getSheetName() + " dong " + excelRow
                                            + ": loi -> " + ex.getMessage());
                                }
                            }
                        }
                    }

                    publish("----------------------------------------");
                    publish("Import xong.");
                    publish("Tong dong xu ly: " + total);
                    publish("Them moi: " + inserted);
                    publish("Cap nhat: " + updated);
                    publish("Bo qua: " + skipped);
                    publish("Loi: " + error);

                } catch (Exception e) {
                    publish("Import that bai: " + e.getMessage());
                }

                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String line : chunks) {
                    log(line);
                }
            }

            @Override
            protected void done() {
                btnImport.setEnabled(true);
                btnSelect.setEnabled(true);

                JOptionPane.showMessageDialog(
                        NguyenVongImportPanel.this,
                        "Import da hoan tat. Kiem tra nhat ky de xem chi tiet."
                );
            }
        };

        worker.execute();
    }

    private Map<String, ThiSinh> loadThiSinhMap() {
        Map<String, ThiSinh> map = new LinkedHashMap<>();
        for (ThiSinh ts : thiSinhService.findAll()) {
            if (ts.getCccd() != null) {
                map.put(normalizeCode(ts.getCccd()), ts);
            }
        }
        return map;
    }

    private Map<String, List<MaXetTuyenMap>> loadMaXetTuyenMap() {
        Map<String, List<MaXetTuyenMap>> map = new LinkedHashMap<>();

        for (MaXetTuyenMap item : maXetTuyenMapService.findAll()) {
            if (item == null || item.getMaXetTuyen() == null) continue;
            if (!Boolean.TRUE.equals(item.getIsActive())) continue;

            String key = normalizeMaXetTuyen(item.getMaXetTuyen());
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
        }

        Comparator<MaXetTuyenMap> comparator = Comparator
                .comparingInt((MaXetTuyenMap m) ->
                        getPhuongThucPriority(m.getPhuongThuc() != null ? m.getPhuongThuc().getMaPhuongthuc() : null))
                .thenComparing(m ->
                        m.getNganhToHop() != null && m.getNganhToHop().getToHop() != null
                                ? safeUpper(m.getNganhToHop().getToHop().getMaTohop())
                                : "")
                .thenComparing(m -> safeUpper(m.getTenChuongTrinh()));

        for (List<MaXetTuyenMap> list : map.values()) {
            list.sort(comparator);
        }

        return map;
    }

    private void preloadExisting(Map<String, NguyenVong> existingByThuTu, Map<String, NguyenVong> existingByMaXt) {
        for (NguyenVong nv : nguyenVongService.findAll()) {
            if (nv.getThiSinh() == null || nv.getThuTu() == null) {
                continue;
            }

            existingByThuTu.put(buildThuTuKey(nv.getThiSinh().getThisinhId(), nv.getThuTu()), nv);

            String maXtKey = buildMaXtKey(nv);
            if (maXtKey != null) {
                existingByMaXt.put(maXtKey, nv);
            }
        }
    }

    private String buildThuTuKey(Integer thiSinhId, Integer thuTu) {
        return thiSinhId + "|" + thuTu;
    }

    private boolean sameEntity(NguyenVong a, NguyenVong b) {
        if (a == null || b == null) return false;
        if (a.getNguyenvongId() != null && b.getNguyenvongId() != null) {
            return a.getNguyenvongId().equals(b.getNguyenvongId());
        }
        return a == b;
    }

    private String buildGhiChu(String maXtRaw, String tenMaXt,
                               int candidateCount, MaXetTuyenMap selectedMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("IMPORT_NV");

        if (!isBlank(maXtRaw)) {
            sb.append(" | MA_XT=").append(maXtRaw.trim());
        }

        if (!isBlank(tenMaXt)) {
            sb.append(" | TEN_MA_XT=").append(tenMaXt.trim());
        }

        if (candidateCount > 1) {
            sb.append(" | AUTO_CHON=")
                    .append(selectedMap.getPhuongThuc() != null ? selectedMap.getPhuongThuc().getMaPhuongthuc() : "")
                    .append("/")
                    .append(selectedMap.getNganhToHop() != null && selectedMap.getNganhToHop().getToHop() != null
                            ? selectedMap.getNganhToHop().getToHop().getMaTohop()
                            : "");
        }

        return sb.toString();
    }

    private Header detectHeader(Sheet sheet) {
        int maxCheckRow = Math.min(sheet.getLastRowNum(), 10);

        for (int r = 0; r <= maxCheckRow; r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;

            Header header = new Header();
            header.rowIndex = r;
            header.colCccd = -1;
            header.colThuTu = -1;
            header.colMaXetTuyen = -1;
            header.colTenMaXetTuyen = -1;

            short lastCell = row.getLastCellNum();
            for (int c = 0; c < lastCell; c++) {
                String label = normalizeHeader(readCell(row, c));
                if (label.isEmpty()) continue;

                if (header.colCccd < 0 && "cccd".equals(label)) {
                    header.colCccd = c;
                } else if (header.colThuTu < 0
                        && (label.contains("thu tu nv") || "thu tu".equals(label) || label.contains("nguyen vong"))) {
                    header.colThuTu = c;
                } else if (header.colMaXetTuyen < 0 && "ma xet tuyen".equals(label)) {
                    header.colMaXetTuyen = c;
                } else if (header.colTenMaXetTuyen < 0 && "ten ma xet tuyen".equals(label)) {
                    header.colTenMaXetTuyen = c;
                }
            }

            if (header.colCccd >= 0 && header.colThuTu >= 0 && header.colMaXetTuyen >= 0) {
                return header;
            }
        }

        return null;
    }

    private String readCell(Row row, int cellIndex) {
        if (cellIndex < 0) return "";
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        return new DataFormatter().formatCellValue(cell).trim();
    }

    private boolean isRowEmpty(Row row) {
        DataFormatter formatter = new DataFormatter();
        short lastCellNum = row.getLastCellNum();
        if (lastCellNum < 0) return true;

        for (int i = 0; i < lastCellNum; i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && !formatter.formatCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private Integer parseInteger(String s) {
        if (isBlank(s)) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private int getPhuongThucPriority(String maPt) {
        String s = safeUpper(maPt);
        if ("THPT".equals(s)) return 1;
        if ("VSAT".equals(s)) return 2;
        if ("DGNL".equals(s)) return 3;
        if ("XTT".equals(s)) return 4;
        if ("NK".equals(s)) return 5;
        return 99;
    }

    private String normalizeMaXetTuyen(String maXt) {
        if (maXt == null) return "";
        return maXt.trim().toUpperCase()
                .replace(" ", "")
                .replace("-", "");
    }

    private String normalizeCode(String s) {
        return s == null ? "" : s.trim().toUpperCase();
    }

    private String normalizeHeader(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private String safeUpper(String s) {
        return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void log(String msg) {
        taLog.append("[" + java.time.LocalDateTime.now().toString().substring(11, 19) + "] " + msg + "\n");
        taLog.setCaretPosition(taLog.getDocument().getLength());
    }

    private static class Header {
        int rowIndex;
        int colCccd;
        int colThuTu;
        int colMaXetTuyen;
        int colTenMaXetTuyen;
    }

    private String buildMaXtKey(NguyenVong nv) {
        if (nv == null || nv.getThiSinh() == null || nv.getMaXetTuyenMap() == null || nv.getMaXetTuyenMap().getMaXettuyenId() == null) {
            return null;
        }
        return buildMaXtKey(nv.getThiSinh().getThisinhId(), nv.getMaXetTuyenMap().getMaXettuyenId());
    }

    private String buildMaXtKey(Integer thiSinhId, Integer maXettuyenId) {
        return thiSinhId + "|" + maXettuyenId;
    }
}