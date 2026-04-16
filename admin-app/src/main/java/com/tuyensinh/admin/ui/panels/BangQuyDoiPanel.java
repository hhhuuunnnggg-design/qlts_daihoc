package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.*;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import com.tuyensinh.dao.*;
import org.apache.poi.ss.usermodel.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * CRUD + Import Excel cho xt_bangquydoi.
 *
 * Mau file de import:
 * - Cot A: ma_quydoi
 * - Cot B: ma_phuongthuc
 * - Cot C: ma_tohop        (co the de trong)
 * - Cot D: ma_mon          (co the de trong)
 * - Cot E: diem_tu
 * - Cot F: diem_den
 * - Cot G: diem_quydoi_tu
 * - Cot H: diem_quydoi_den
 * - Cot I: phan_vi         (co the de trong)
 *
 * Co the dung dong header voi cac ten cot tuong ung.
 */
public class BangQuyDoiPanel extends BaseCrudPanel<BangQuyDoi> {

    private final BangQuyDoiService service;
    private final PhuongThucDao phuongThucDao;
    private final ToHopDao toHopDao;
    private final MonDao monDao;

    public BangQuyDoiPanel(MainFrame mainFrame) {
        super(mainFrame);
        service = new BangQuyDoiService();
        phuongThucDao = new PhuongThucDao();
        toHopDao = new ToHopDao();
        monDao = new MonDao();
        initUI();
        loadData();
    }

    @Override
    protected String[] getTableColumns() {
        return new String[]{"ID", "Ma QD", "Ph. thuc", "To hop", "Mon", "Diem A", "Diem B", "Diem QD A", "Diem QD B", "Phan vi"};
    }

    @Override
    protected BangQuyDoi getSelectedEntity() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return service.findById((Integer) model.getValueAt(row, 0));
    }

    @Override
    protected Integer getSelectedId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (Integer) model.getValueAt(row, 0);
    }

    @Override
    public String getPageTitle() {
        return UIConstants.PAGE_BANG_QUY_DOI;
    }

    @Override
    protected void buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        toolbar.add(new JLabel("Tim kiem:"));
        searchTextField = new JTextField(20);
        searchTextField.addActionListener(e -> doSearch());
        toolbar.add(searchTextField);

        JButton btnSearch = new JButton("Tim kiem");
        btnSearch.addActionListener(e -> doSearch());
        toolbar.add(btnSearch);

        toolbar.add(Box.createHorizontalStrut(16));

        JButton btnImport = new JButton("Import Excel");
        btnImport.addActionListener(e -> doImportExcel());
        toolbar.add(btnImport);

        JButton btnAdd = new JButton("Them moi");
        btnAdd.addActionListener(e -> showAddDialog());
        toolbar.add(btnAdd);

        JButton btnEdit = new JButton("Sua");
        btnEdit.addActionListener(e -> showEditDialog());
        toolbar.add(btnEdit);

        JButton btnDelete = new JButton("Xoa");
        btnDelete.addActionListener(e -> doDelete());
        toolbar.add(btnDelete);

        add(toolbar, BorderLayout.NORTH);
    }

    @Override
    protected void buildBottomBar() {
        totalLabel = new JLabel("Tong: 0");
        totalLabel.setFont(UIConstants.FONT_SMALL);
        add(totalLabel, BorderLayout.SOUTH);
    }

    @Override
    public void loadData() {
        model.setRowCount(0);
        String kw = searchTextField.getText().trim();
        List<BangQuyDoi> list = kw.isEmpty() ? service.findAll() : service.search(kw);

        for (BangQuyDoi bqd : list) {
            model.addRow(new Object[]{
                    bqd.getBangquydoiId(),
                    bqd.getMaQuydoi(),
                    bqd.getPhuongThuc() != null ? bqd.getPhuongThuc().getMaPhuongthuc() : "",
                    bqd.getToHop() != null ? bqd.getToHop().getMaTohop() : "",
                    bqd.getMon() != null ? bqd.getMon().getMaMon() : "",
                    bqd.getDiemTu(),
                    bqd.getDiemDen(),
                    bqd.getDiemQuydoiTu(),
                    bqd.getDiemQuydoiDen(),
                    bqd.getPhanVi()
            });
        }
        updateTotalLabel(list.size(), "ban ghi");
    }

    @Override
    protected String getEntityDisplayName(BangQuyDoi bqd) {
        return bqd.getMaQuydoi();
    }

    @Override
    protected void deleteEntity(BangQuyDoi bqd) throws Exception {
        service.delete(bqd);
    }

    @Override
    protected void showAddDialog() {
        JTextField txtMa = new JTextField(20);
        JComboBox<PhuongThuc> cboPt = new JComboBox<>();
        JComboBox<ToHop> cboTh = new JComboBox<>();
        JComboBox<Mon> cboMon = new JComboBox<>();
        cboTh.addItem(null);
        cboMon.addItem(null);
        for (PhuongThuc pt : phuongThucDao.findAll()) cboPt.addItem(pt);
        for (ToHop th : toHopDao.findAll()) cboTh.addItem(th);
        for (Mon m : monDao.findAll()) cboMon.addItem(m);

        JTextField txtTu = new JTextField("0", 10);
        JTextField txtDen = new JTextField("30", 10);
        JTextField txtQdTu = new JTextField("0", 10);
        JTextField txtQdDen = new JTextField("30", 10);

        int r = JOptionPane.showConfirmDialog(this, new Object[]{
                "Ma quy doi (*):", txtMa,
                "Phuong thuc (*):", cboPt,
                "To hop:", cboTh,
                "Mon:", cboMon,
                "Diem tu:", txtTu, "Den:", txtDen,
                "Diem quy doi tu:", txtQdTu, "Den:", txtQdDen
        }, "Them bang quy doi", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        if (txtMa.getText().trim().isEmpty()) {
            showMessage(this, "Ma quy doi la bat buoc!");
            return;
        }

        BangQuyDoi bqd = new BangQuyDoi();
        bqd.setMaQuydoi(txtMa.getText().trim());
        bqd.setPhuongThuc((PhuongThuc) cboPt.getSelectedItem());
        bqd.setToHop((ToHop) cboTh.getSelectedItem());
        bqd.setMon((Mon) cboMon.getSelectedItem());
        bqd.setDiemTu(parseBigDecimal(txtTu.getText()));
        bqd.setDiemDen(parseBigDecimal(txtDen.getText()));
        bqd.setDiemQuydoiTu(parseBigDecimal(txtQdTu.getText()));
        bqd.setDiemQuydoiDen(parseBigDecimal(txtQdDen.getText()));

        try {
            service.save(bqd);
            showSuccess(this, "Them thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    @Override
    protected void showEditDialog() {
        BangQuyDoi bqd = getSelectedEntity();
        if (bqd == null) {
            showSelectRow();
            return;
        }

        JTextField txtTu = new JTextField(bqd.getDiemTu() != null ? bqd.getDiemTu().toString() : "0", 10);
        JTextField txtDen = new JTextField(bqd.getDiemDen() != null ? bqd.getDiemDen().toString() : "30", 10);
        JTextField txtQdTu = new JTextField(bqd.getDiemQuydoiTu() != null ? bqd.getDiemQuydoiTu().toString() : "0", 10);
        JTextField txtQdDen = new JTextField(bqd.getDiemQuydoiDen() != null ? bqd.getDiemQuydoiDen().toString() : "30", 10);

        int r = JOptionPane.showConfirmDialog(this, new Object[]{
                "Ma: " + bqd.getMaQuydoi(),
                "Diem tu:", txtTu, "Den:", txtDen,
                "Diem quy doi tu:", txtQdTu, "Den:", txtQdDen
        }, "Sua bang quy doi", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        bqd.setDiemTu(parseBigDecimal(txtTu.getText()));
        bqd.setDiemDen(parseBigDecimal(txtDen.getText()));
        bqd.setDiemQuydoiTu(parseBigDecimal(txtQdTu.getText()));
        bqd.setDiemQuydoiDen(parseBigDecimal(txtQdDen.getText()));

        try {
            service.update(bqd);
            showSuccess(this, "Cap nhat thanh cong!");
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private void doImportExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chon file Excel bang quy doi");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileNameExtensionFilter("Excel files", "xlsx", "xls"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Import du lieu vao xt_bangquydoi?\n" +
                        "- Neu ma_quydoi da ton tai: cap nhat\n" +
                        "- Neu chua ton tai: them moi\n\n" +
                        "File: " + file.getName(),
                "Xac nhan import",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (confirm != JOptionPane.OK_OPTION) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<ImportResult, Void> worker = new SwingWorker<>() {
            @Override
            protected ImportResult doInBackground() throws Exception {
                return importExcel(file);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    ImportResult rs = get();
                    loadData();
                    JOptionPane.showMessageDialog(
                            BangQuyDoiPanel.this,
                            "Import xong!\n" +
                                    "- Them moi: " + rs.inserted + "\n" +
                                    "- Cap nhat: " + rs.updated + "\n" +
                                    "- Bo qua: " + rs.skipped +
                                    (rs.errors.length() > 0 ? "\n\nChi tiet loi:\n" + rs.errors : ""),
                            "Ket qua import",
                            rs.errors.length() > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception ex) {
                    showError(BangQuyDoiPanel.this, ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private ImportResult importExcel(File file) throws Exception {
        ImportResult rs = new ImportResult();
        DataFormatter formatter = new DataFormatter(Locale.US);

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) throw new IllegalStateException("File khong co sheet nao.");

            Row header = sheet.getRow(sheet.getFirstRowNum());
            int idxMa = 0, idxPt = 1, idxTh = 2, idxMon = 3, idxTu = 4, idxDen = 5, idxQdTu = 6, idxQdDen = 7, idxPhanVi = 8;

            if (header != null) {
                idxMa     = findHeaderIndex(header, formatter, "ma_quydoi", "ma quy doi", "maqd");
                idxPt     = findHeaderIndex(header, formatter, "ma_phuongthuc", "ma phuong thuc", "phuongthuc", "phuong thuc");
                idxTh     = findHeaderIndex(header, formatter, "ma_tohop", "ma to hop", "tohop", "to hop");
                idxMon    = findHeaderIndex(header, formatter, "ma_mon", "ma mon", "mon");
                idxTu     = findHeaderIndex(header, formatter, "diem_tu", "diem tu");
                idxDen    = findHeaderIndex(header, formatter, "diem_den", "diem den");
                idxQdTu   = findHeaderIndex(header, formatter, "diem_quydoi_tu", "diem quy doi tu", "diemqd_tu");
                idxQdDen  = findHeaderIndex(header, formatter, "diem_quydoi_den", "diem quy doi den", "diemqd_den");
                idxPhanVi = findHeaderIndex(header, formatter, "phan_vi", "phan vi");

                if (idxMa < 0) idxMa = 0;
                if (idxPt < 0) idxPt = 1;
                if (idxTh < 0) idxTh = 2;
                if (idxMon < 0) idxMon = 3;
                if (idxTu < 0) idxTu = 4;
                if (idxDen < 0) idxDen = 5;
                if (idxQdTu < 0) idxQdTu = 6;
                if (idxQdDen < 0) idxQdDen = 7;
                if (idxPhanVi < 0) idxPhanVi = 8;
            }

            int startRow = sheet.getFirstRowNum() + 1;
            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isBlankRow(row, formatter)) continue;

                try {
                    String maQuyDoi = getCellString(row, idxMa, formatter);
                    String maPhuongThuc = getCellString(row, idxPt, formatter);
                    String maToHop = getCellString(row, idxTh, formatter);
                    String maMon = getCellString(row, idxMon, formatter);

                    BigDecimal diemTu = parseNullableBigDecimal(getCellString(row, idxTu, formatter));
                    BigDecimal diemDen = parseNullableBigDecimal(getCellString(row, idxDen, formatter));
                    BigDecimal diemQdTu = parseNullableBigDecimal(getCellString(row, idxQdTu, formatter));
                    BigDecimal diemQdDen = parseNullableBigDecimal(getCellString(row, idxQdDen, formatter));
                    Integer phanVi = parseNullableInteger(getCellString(row, idxPhanVi, formatter));

                    if (isBlank(maQuyDoi) && isBlank(maPhuongThuc) && diemTu == null && diemDen == null && diemQdTu == null && diemQdDen == null) {
                        continue;
                    }

                    if (isBlank(maQuyDoi)) {
                        throw new IllegalArgumentException("Thieu ma_quydoi");
                    }
                    if (isBlank(maPhuongThuc)) {
                        throw new IllegalArgumentException("Thieu ma_phuongthuc");
                    }
                    if (diemTu == null || diemDen == null || diemQdTu == null || diemQdDen == null) {
                        throw new IllegalArgumentException("Thieu diem_tu/diem_den/diem_quydoi_tu/diem_quydoi_den");
                    }

                    PhuongThuc phuongThuc = phuongThucDao.findByMa(maPhuongThuc.trim()).orElse(null);
                    if (phuongThuc == null) {
                        throw new IllegalArgumentException("Khong tim thay phuong thuc: " + maPhuongThuc);
                    }

                    ToHop toHop = null;
                    if (!isBlank(maToHop)) {
                        toHop = toHopDao.findByMa(maToHop.trim()).orElse(null);
                        if (toHop == null) {
                            throw new IllegalArgumentException("Khong tim thay to hop: " + maToHop);
                        }
                    }

                    Mon mon = null;
                    if (!isBlank(maMon)) {
                        mon = monDao.findByMa(maMon.trim()).orElse(null);
                        if (mon == null) {
                            throw new IllegalArgumentException("Khong tim thay mon: " + maMon);
                        }
                    }

                    BangQuyDoi entity = service.findByMa(maQuyDoi.trim());
                    boolean isUpdate = entity != null;
                    if (entity == null) entity = new BangQuyDoi();

                    entity.setMaQuydoi(maQuyDoi.trim());
                    entity.setPhuongThuc(phuongThuc);
                    entity.setToHop(toHop);
                    entity.setMon(mon);
                    entity.setDiemTu(diemTu);
                    entity.setDiemDen(diemDen);
                    entity.setDiemQuydoiTu(diemQdTu);
                    entity.setDiemQuydoiDen(diemQdDen);
                    entity.setPhanVi(phanVi);

                    if (isUpdate) {
                        service.update(entity);
                        rs.updated++;
                    } else {
                        service.save(entity);
                        rs.inserted++;
                    }
                } catch (Exception ex) {
                    rs.skipped++;
                    rs.errors.append("Dong ").append(i + 1).append(": ").append(ex.getMessage()).append("\n");
                }
            }
        }

        return rs;
    }

    private int findHeaderIndex(Row header, DataFormatter formatter, String... aliases) {
        for (Cell cell : header) {
            String value = normalizeHeader(formatter.formatCellValue(cell));
            for (String alias : aliases) {
                if (value.equals(normalizeHeader(alias))) {
                    return cell.getColumnIndex();
                }
            }
        }
        return -1;
    }

    private String normalizeHeader(String s) {
        return s == null ? "" : s.trim().toLowerCase()
                                .replace("đ", "d")
                                .replace("_", "")
                                .replace(" ", "");
    }

    private boolean isBlankRow(Row row, DataFormatter formatter) {
        short lastCell = row.getLastCellNum();
        if (lastCell < 0) return true;
        for (int i = 0; i < lastCell; i++) {
            if (!getCellString(row, i, formatter).isBlank()) return false;
        }
        return true;
    }

    private String getCellString(Row row, int col, DataFormatter formatter) {
        if (row == null || col < 0) return "";
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private BigDecimal parseNullableBigDecimal(String s) {
        if (isBlank(s)) return null;
        String normalized = s.trim().replace(",", ".");
        return new BigDecimal(normalized);
    }

    private Integer parseNullableInteger(String s) {
        if (isBlank(s)) return null;
        return Integer.parseInt(s.trim());
    }

    private static class ImportResult {
        int inserted;
        int updated;
        int skipped;
        StringBuilder errors = new StringBuilder();
    }
}