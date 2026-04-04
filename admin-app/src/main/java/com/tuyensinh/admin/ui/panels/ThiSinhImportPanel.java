package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.DoiTuongUutien;
import com.tuyensinh.entity.KhuVucUutien;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.service.DoiTuongService;
import com.tuyensinh.service.KhuVucService;
import com.tuyensinh.service.ThiSinhService;
import com.tuyensinh.util.DateUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ThiSinhImportPanel extends JPanel {

    private final MainFrame mainFrame;
    private final ThiSinhService thiSinhService = new ThiSinhService();
    private final DoiTuongService doiTuongService = new DoiTuongService();
    private final KhuVucService khuVucService = new KhuVucService();

    private JTextArea taLog;
    private JButton btnSelect, btnImport;
    private JLabel lblFile;
    private JFileChooser fileChooser;

    private File selectedFile;

    public ThiSinhImportPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.add(new JLabel("Import thi sinh tu file Excel (.xlsx)"));
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
                "File hien tai dang import theo mau Ds thi sinh.xlsx\n\n" +
                        "Cac cot duoc dung:\n" +
                        "B  = CCCD\n" +
                        "C  = Ho Ten\n" +
                        "D  = Ngay sinh\n" +
                        "E  = Gioi tinh\n" +
                        "F  = DTUT\n" +
                        "G  = KVUT\n" +
                        "AJ = Noi sinh\n\n" +
                        "Ghi chu:\n" +
                        "- Bo qua cot STT\n" +
                        "- Khong co cot SoBaoDanh -> he thong tu sinh\n" +
                        "- Khong co cot DienThoai, Email -> de null\n" +
                        "- Ho Ten se duoc tach thanh Ho va Ten\n" +
                        "- Ngay sinh chap nhan dang dd/MM/yyyy hoac o dang cell date Excel\n" +
                        "- Gioi tinh: Nam / Nu / Nữ\n" +
                        "- DTUT va KVUT se map theo ma trong DB\n"
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

        SwingWorker<Void, String> worker = new SwingWorker<>() {

            int total = 0;
            int success = 0;
            int updated = 0;
            int skipped = 0;
            int error = 0;

            @Override
            protected Void doInBackground() {
                publish("Bat dau import file: " + selectedFile.getName());

                try {
                    List<DoiTuongUutien> dsDt = doiTuongService.findAll();
                    List<KhuVucUutien> dsKv = khuVucService.findAll();

                    Map<String, DoiTuongUutien> mapDt = buildDoiTuongMap(dsDt);
                    Map<String, KhuVucUutien> mapKv = buildKhuVucMap(dsKv);

                    try (FileInputStream fis = new FileInputStream(selectedFile);
                         Workbook workbook = new XSSFWorkbook(fis)) {

                        Sheet sheet = workbook.getSheetAt(0);
                        if (sheet == null) {
                            publish("Khong tim thay sheet trong file.");
                            return null;
                        }

                        int lastRow = sheet.getLastRowNum();
                        publish("Tong so dong du lieu (uoc tinh): " + lastRow);

                        String currentMaxSbd = thiSinhService.generateSoBaoDanh();
                        int nextSbdNumber = extractSoBaoDanhNumber(thiSinhService.generateSoBaoDanh());

                        for (int i = 1; i <= lastRow; i++) {
                            Row row = sheet.getRow(i);
                            if (row == null || isRowEmpty(row)) {
                                continue;
                            }

                            total++;
                            int excelRow = i + 1;

                            try {
                                String cccd = readCell(row, 1);          // cot CCCD
                                String hoTen = readCell(row, 2);         // cot Ho Ten
                                String ngaySinhRaw = readCell(row, 3);   // cot Ngay sinh
                                String gioiTinh = readCell(row, 4);      // cot Gioi tinh
                                String maDt = readCell(row, 5);          // cot DTUT
                                String maKv = readCell(row, 6);          // cot KVUT
                                String noiSinh = readCell(row, 35);      // cot Noi sinh

                                String soBaoDanh = null;                 // file nay khong co cot SBD
                                String dienThoai = null;                 // file nay khong co
                                String email = null;                     // file nay khong co

                                publish("DEBUG dong " + excelRow +
                                        " | cccd='" + cccd +
                                        "' | hoTen='" + hoTen +
                                        "' | ngaySinh='" + ngaySinhRaw +
                                        "' | gioiTinh='" + gioiTinh + "'");

                                if (isBlank(cccd) || isBlank(hoTen)) {
                                    skipped++;
                                    publish("Dong " + excelRow + ": bo qua vi thieu CCCD/Ho Ten.");
                                    continue;
                                }

                                String[] nameParts = splitFullName(hoTen);
                                String ho = nameParts[0];
                                String ten = nameParts[1];

                                LocalDate ngaySinh = parseExcelDate(row.getCell(3), ngaySinhRaw);
                                if (!isBlank(ngaySinhRaw) && ngaySinh == null) {
                                    skipped++;
                                    publish("Dong " + excelRow + ": ngay sinh khong hop le -> " + ngaySinhRaw);
                                    continue;
                                }

                                DoiTuongUutien doiTuong = null;
                                if (!isBlank(maDt)) {
                                    String maDtNormalized = normalizeMaDt(maDt);
                                    doiTuong = mapDt.get(maDtNormalized);

                                    if (doiTuong == null) {
                                        skipped++;
                                        publish("Dong " + excelRow + ": ma doi tuong khong ton tai -> " + maDt
                                                + " | chuan hoa -> " + maDtNormalized);
                                        continue;
                                    }
                                }

                                KhuVucUutien khuVuc = null;
                                if (!isBlank(maKv)) {
                                    khuVuc = mapKv.get(maKv.trim().toUpperCase());
                                    if (khuVuc == null) {
                                        skipped++;
                                        publish("Dong " + excelRow + ": ma khu vuc khong ton tai -> " + maKv);
                                        continue;
                                    }
                                }

                                Optional<ThiSinh> opt = thiSinhService.findByCccd(cccd.trim());
                                boolean isUpdate = opt.isPresent();

                                ThiSinh ts = isUpdate ? opt.get() : new ThiSinh();
                                ts.setCccd(cccd.trim());
                                ts.setHo(ho.trim());
                                ts.setTen(ten.trim());
                                ts.setNgaySinh(ngaySinh);
                                ts.setGioiTinh(normalizeGender(gioiTinh));
                                ts.setDienThoai(emptyToNull(dienThoai));
                                ts.setEmail(emptyToNull(email));
                                ts.setNoiSinh(emptyToNull(noiSinh));
                                ts.setDoiTuongUutien(doiTuong);
                                ts.setKhuVucUutien(khuVuc);

                                if (!isUpdate) {
                                    String sbd = emptyToNull(soBaoDanh);
                                    if (sbd == null) {
                                        sbd = String.format("TS%05d", nextSbdNumber++);
                                    } else {
                                        Optional<ThiSinh> bySbd = thiSinhService.findBySoBaoDanh(sbd);
                                        if (bySbd.isPresent()) {
                                            skipped++;
                                            publish("Dong " + excelRow + ": so bao danh bi trung -> " + sbd);
                                            continue;
                                        }
                                    }
                                    ts.setSobaodanh(sbd);
                                    thiSinhService.save(ts);
                                    success++;
                                    publish("Dong " + excelRow + ": them moi thanh cong CCCD=" + ts.getCccd() + ", SBD=" + ts.getSobaodanh());
                                } else {
                                    String sbdMoi = emptyToNull(soBaoDanh);
                                    if (sbdMoi != null && !sbdMoi.equals(ts.getSobaodanh())) {
                                        Optional<ThiSinh> bySbd = thiSinhService.findBySoBaoDanh(sbdMoi);
                                        if (bySbd.isPresent() && !bySbd.get().getThisinhId().equals(ts.getThisinhId())) {
                                            skipped++;
                                            publish("Dong " + excelRow + ": khong the update vi SBD trung -> " + sbdMoi);
                                            continue;
                                        }
                                        ts.setSobaodanh(sbdMoi);
                                    } else if (ts.getSobaodanh() == null || ts.getSobaodanh().trim().isEmpty()) {
                                        ts.setSobaodanh(thiSinhService.generateSoBaoDanh());
                                    }

                                    thiSinhService.update(ts);
                                    updated++;
                                    publish("Dong " + excelRow + ": cap nhat thanh cong CCCD=" + ts.getCccd());
                                }

                            } catch (Exception ex) {
                                error++;
                                publish("Dong " + excelRow + ": loi -> " + ex.getMessage());
                            }
                        }
                    }

                    publish("----------------------------------------");
                    publish("Import xong.");
                    publish("Tong dong xu ly: " + total);
                    publish("Them moi: " + success);
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
                        ThiSinhImportPanel.this,
                        "Import da hoan tat. Kiem tra nhat ky de xem chi tiet."
                );
            }
        };

        worker.execute();
    }

    private Map<String, DoiTuongUutien> buildDoiTuongMap(List<DoiTuongUutien> list) {
        Map<String, DoiTuongUutien> map = new HashMap<>();
        for (DoiTuongUutien d : list) {
            if (d != null && d.getMaDoituong() != null) {
                map.put(d.getMaDoituong().trim().toUpperCase(), d);
            }
        }
        return map;
    }

    private Map<String, KhuVucUutien> buildKhuVucMap(List<KhuVucUutien> list) {
        Map<String, KhuVucUutien> map = new HashMap<>();
        for (KhuVucUutien k : list) {
            if (k != null && k.getMaKhuVuc() != null) {
                map.put(k.getMaKhuVuc().trim().toUpperCase(), k);
            }
        }
        return map;
    }

    private String readCell(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";

        DataFormatter formatter = new DataFormatter();

        if (cell.getCellType() == CellType.FORMULA) {
            return formatter.formatCellValue(cell).trim();
        }

        return formatter.formatCellValue(cell).trim();
    }

    private LocalDate parseExcelDate(Cell cell, String raw) {
        try {
            if (cell != null && cell.getCellType() == CellType.NUMERIC && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
        } catch (Exception ignored) {
        }
        return DateUtil.parseDate(raw);
    }

    private String normalizeGender(String gt) {
        if (isBlank(gt)) return null;
        String value = gt.trim().toLowerCase();
        if (value.equals("nam")) return "Nam";
        if (value.equals("nu") || value.equals("nữ")) return "Nu";
        return gt.trim();
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

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String emptyToNull(String s) {
        return isBlank(s) ? null : s.trim();
    }

    private void log(String msg) {
        taLog.append("[" + java.time.LocalDateTime.now().toString().substring(11, 19) + "] " + msg + "\n");
        taLog.setCaretPosition(taLog.getDocument().getLength());
    }

    private String[] splitFullName(String fullName) {
        if (fullName == null) return new String[]{"", ""};

        String s = fullName.trim().replaceAll("\\s+", " ");
        if (s.isEmpty()) return new String[]{"", ""};

        int lastSpace = s.lastIndexOf(' ');
        if (lastSpace < 0) {
            return new String[]{"", s};
        }

        String ho = s.substring(0, lastSpace).trim();
        String ten = s.substring(lastSpace + 1).trim();
        return new String[]{ho, ten};
    }

    private String normalizeMaDt(String maDt) {
        if (maDt == null) return null;
        String s = maDt.trim().toUpperCase();
        if (s.isEmpty()) return null;

        if (s.length() >= 2
                && Character.isDigit(s.charAt(0))
                && Character.isDigit(s.charAt(1))) {
            return s.substring(0, 2);
        }

        return s;
    }

    private int extractSoBaoDanhNumber(String sbd) {
        if (sbd == null) return 0;
        String s = sbd.trim().toUpperCase();
        if (!s.startsWith("TS")) return 0;
        try {
            return Integer.parseInt(s.substring(2));
        } catch (Exception e) {
            return 0;
        }
    }
}