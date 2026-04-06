package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.dao.BaseDao;
import com.tuyensinh.entity.MaXetTuyenMap;
import com.tuyensinh.entity.Nganh;
import com.tuyensinh.entity.NganhToHop;
import com.tuyensinh.entity.PhuongThuc;
import com.tuyensinh.util.HibernateUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.FileInputStream;
import java.text.Normalizer;
import java.util.*;

public class MaXetTuyenExcelImporter {

    private final DataFormatter formatter = new DataFormatter(Locale.US);

    public ImportResult importFromExcel(File file, boolean replaceAll) throws Exception {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("File import khong hop le.");
        }

        BaseDao.closeCurrentEm();
        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();

        try (FileInputStream fis = new FileInputStream(file);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalStateException("Khong tim thay sheet nao trong file Excel.");
            }

            Header header = detectHeader(sheet);
            if (header == null) {
                throw new IllegalStateException(
                        "Khong xac dinh duoc dong header. Can toi thieu cac cot: ma_xet_tuyen, ma_nganh, phuong_thuc."
                );
            }

            Map<String, Nganh> nganhByMa = loadNganhByMa(em);
            Map<String, PhuongThuc> phuongThucByKey = loadPhuongThucByKey(em);
            Map<String, NganhToHop> nganhToHopByKey = loadNganhToHopByKey(em);
            Map<String, MaXetTuyenMap> existingByKey = replaceAll ? new LinkedHashMap<>() : loadExistingMap(em);

            int totalRows = 0;
            int inserted = 0;
            int updated = 0;
            int skipped = 0;

            em.getTransaction().begin();
            try {
                if (replaceAll) {
                    em.createQuery("delete from MaXetTuyenMap").executeUpdate();
                }

                int lastRow = sheet.getLastRowNum();
                for (int i = header.rowIndex + 1; i <= lastRow; i++) {
                    Row row = sheet.getRow(i);
                    if (row == null || isRowEmpty(row)) {
                        continue;
                    }

                    totalRows++;

                    String maXetTuyen = normalizeCode(readCell(row, header.colMaXetTuyen));
                    String maNganh = normalizeCode(readCell(row, header.colMaNganh));
                    String phuongThucToken = normalizeCode(readCell(row, header.colPhuongThuc));
                    String tenChuongTrinh = readCell(row, header.colTenChuongTrinh);
                    String maToHopNguon = normalizeCode(readCell(row, header.colMaToHop));
                    String ghiChu = readCell(row, header.colGhiChu);
                    Boolean isActive = parseBooleanFlag(readCell(row, header.colIsActive));

                    if (isBlank(maXetTuyen) || isBlank(maNganh) || isBlank(phuongThucToken)) {
                        skipped++;
                        continue;
                    }

                    Nganh nganh = nganhByMa.get(maNganh);
                    if (nganh == null) {
                        throw new IllegalStateException("Dong " + (i + 1) + ": khong tim thay nganh ma = " + maNganh);
                    }

                    PhuongThuc pt = resolvePhuongThuc(phuongThucToken, phuongThucByKey);
                    if (pt == null) {
                        throw new IllegalStateException("Dong " + (i + 1) + ": khong tim thay phuong thuc = " + phuongThucToken);
                    }

                    NganhToHop nganhToHop = null;
                    if (!isBlank(maToHopNguon)) {
                        nganhToHop = nganhToHopByKey.get(maNganh + "|" + maToHopNguon);
                    }

                    if (nganhToHop != null && nganhToHop.getToHop() != null && nganhToHop.getToHop().getMaTohop() != null) {
                        maToHopNguon = normalizeCode(nganhToHop.getToHop().getMaTohop());
                    }

                    if (tenChuongTrinh == null || tenChuongTrinh.trim().isEmpty()) {
                        tenChuongTrinh = nganh.getTenNganh();
                    }
                    if (isActive == null) {
                        isActive = true;
                    }

                    String uniqueKey = buildUniqueKey(maXetTuyen, pt.getPhuongthucId(), maToHopNguon);

                    MaXetTuyenMap entity = existingByKey.get(uniqueKey);
                    boolean isUpdate = entity != null;

                    if (!isUpdate) {
                        entity = new MaXetTuyenMap();
                        entity.setMaXetTuyen(maXetTuyen);
                        inserted++;
                    } else {
                        updated++;
                    }

                    entity.setTenChuongTrinh(tenChuongTrinh);
                    entity.setNganh(nganh);
                    entity.setPhuongThuc(pt);
                    entity.setNganhToHop(nganhToHop);
                    entity.setMaTohopNguon(maToHopNguon == null ? "" : maToHopNguon);
                    entity.setGhiChu(isBlank(ghiChu) ? null : ghiChu.trim());
                    entity.setIsActive(isActive);

                    if (!isUpdate) {
                        em.persist(entity);
                        existingByKey.put(uniqueKey, entity);
                    }
                }

                em.getTransaction().commit();
            } catch (Exception ex) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw ex;
            }

            BaseDao.closeCurrentEm();

            return new ImportResult(
                    file.getAbsolutePath(),
                    totalRows,
                    inserted,
                    updated,
                    skipped,
                    replaceAll
            );
        } finally {
            if (em.isOpen()) {
                em.close();
            }
            BaseDao.closeCurrentEm();
        }
    }

    private Map<String, Nganh> loadNganhByMa(EntityManager em) {
        List<Nganh> list = em.createQuery("from Nganh", Nganh.class).getResultList();
        Map<String, Nganh> map = new LinkedHashMap<>();
        for (Nganh n : list) {
            if (n.getMaNganh() != null) {
                map.put(normalizeCode(n.getMaNganh()), n);
            }
        }
        return map;
    }

    private Map<String, PhuongThuc> loadPhuongThucByKey(EntityManager em) {
        List<PhuongThuc> list = em.createQuery("from PhuongThuc", PhuongThuc.class).getResultList();
        Map<String, PhuongThuc> map = new LinkedHashMap<>();
        for (PhuongThuc p : list) {
            if (p.getMaPhuongthuc() != null) {
                map.put(normalizeCode(p.getMaPhuongthuc()), p);
            }
            if (p.getTenPhuongthuc() != null) {
                map.put(normalizeCode(p.getTenPhuongthuc()), p);
            }
            map.put(String.valueOf(p.getPhuongthucId()), p);
        }
        return map;
    }

    private PhuongThuc resolvePhuongThuc(String token, Map<String, PhuongThuc> map) {
        if (isBlank(token)) return null;

        PhuongThuc pt = map.get(normalizeCode(token));
        if (pt != null) return pt;

        String t = normalizeCode(token);
        if (t.contains("THPT")) return map.get("THPT");
        if (t.contains("DGNL")) return map.get("DGNL");
        if (t.contains("VSAT")) return map.get("VSAT");
        if (t.contains("XTT")) return map.get("XTT");
        if (t.contains("NK")) return map.get("NK");

        return null;
    }

    private Map<String, NganhToHop> loadNganhToHopByKey(EntityManager em) {
        List<NganhToHop> list = em.createQuery("from NganhToHop", NganhToHop.class).getResultList();
        Map<String, NganhToHop> map = new LinkedHashMap<>();
        for (NganhToHop nt : list) {
            if (nt.getNganh() == null || nt.getToHop() == null) continue;
            String maNganh = nt.getNganh().getMaNganh();
            String maToHop = nt.getToHop().getMaTohop();
            if (maNganh == null || maToHop == null) continue;
            map.put(normalizeCode(maNganh) + "|" + normalizeCode(maToHop), nt);
        }
        return map;
    }

    private Map<String, MaXetTuyenMap> loadExistingMap(EntityManager em) {
        List<MaXetTuyenMap> list = em.createQuery("from MaXetTuyenMap", MaXetTuyenMap.class).getResultList();
        Map<String, MaXetTuyenMap> map = new LinkedHashMap<>();
        for (MaXetTuyenMap x : list) {
            if (x.getPhuongThuc() == null) continue;
            String key = buildUniqueKey(
                    x.getMaXetTuyen(),
                    x.getPhuongThuc().getPhuongthucId(),
                    x.getMaTohopNguon()
            );
            map.put(key, x);
        }
        return map;
    }

    private String buildUniqueKey(String maXetTuyen, Short phuongThucId, String maToHopNguon) {
        return normalizeCode(maXetTuyen) + "|" + phuongThucId + "|" + normalizeCode(maToHopNguon);
    }

    private Header detectHeader(Sheet sheet) {
        int maxCheckRow = Math.min(sheet.getLastRowNum(), 10);

        for (int r = 0; r <= maxCheckRow; r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;

            Map<String, Integer> map = new HashMap<>();
            short lastCell = row.getLastCellNum();
            for (int c = 0; c < lastCell; c++) {
                String raw = readCell(row, c);
                if (isBlank(raw)) continue;
                map.put(normalizeHeader(raw), c);
            }

            Integer maXt = findHeader(map, "MAXETTUYEN", "MAXT", "MAXET");
            Integer maNganh = findHeader(map, "MANGANH", "MACTDT", "MACT");
            Integer pt = findHeader(map, "MAPHUONGTHUC", "PHUONGTHUC", "PT");

            if (maXt != null && maNganh != null && pt != null) {
                Header h = new Header();
                h.rowIndex = r;
                h.colMaXetTuyen = maXt;
                h.colMaNganh = maNganh;
                h.colPhuongThuc = pt;
                h.colTenChuongTrinh = findHeader(map, "TENCHUONGTRINH", "TENNGANH", "TENCTDT");
                h.colMaToHop = findHeader(map, "MATOHOP", "TOHOP", "MATOHOPNGUON");
                h.colGhiChu = findHeader(map, "GHICHU", "NOTE");
                h.colIsActive = findHeader(map, "ISACTIVE", "ACTIVE", "HIEULUC");
                return h;
            }
        }

        return null;
    }

    private Integer findHeader(Map<String, Integer> map, String... keys) {
        for (String k : keys) {
            if (map.containsKey(k)) {
                return map.get(k);
            }
        }
        return null;
    }

    private String normalizeHeader(String s) {
        if (s == null) return "";
        String x = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "");
        return x;
    }

    private String readCell(Row row, Integer colIndex) {
        if (row == null || colIndex == null || colIndex < 0) return "";
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        return formatter.formatCellValue(cell).trim();
    }

    private Boolean parseBooleanFlag(String s) {
        if (isBlank(s)) return null;
        String v = normalizeCode(s);
        if ("1".equals(v) || "TRUE".equals(v) || "YES".equals(v) || "Y".equals(v) || "ACTIVE".equals(v)) return true;
        if ("0".equals(v) || "FALSE".equals(v) || "NO".equals(v) || "N".equals(v) || "INACTIVE".equals(v)) return false;
        return null;
    }

    private String normalizeCode(String s) {
        return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        short lastCell = row.getLastCellNum();
        for (int i = 0; i < lastCell; i++) {
            if (!readCell(row, i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static class Header {
        int rowIndex;
        Integer colMaXetTuyen;
        Integer colTenChuongTrinh;
        Integer colMaNganh;
        Integer colPhuongThuc;
        Integer colMaToHop;
        Integer colGhiChu;
        Integer colIsActive;
    }

    public static class ImportResult {
        private final String filePath;
        private final int totalRows;
        private final int inserted;
        private final int updated;
        private final int skipped;
        private final boolean replaceAll;

        public ImportResult(String filePath, int totalRows, int inserted, int updated, int skipped, boolean replaceAll) {
            this.filePath = filePath;
            this.totalRows = totalRows;
            this.inserted = inserted;
            this.updated = updated;
            this.skipped = skipped;
            this.replaceAll = replaceAll;
        }

        public String toHumanMessage() {
            return "Import ma xet tuyen thanh cong.\n\n"
                    + "File: " + filePath + "\n"
                    + "Che do: " + (replaceAll ? "Xoa het va import lai" : "Upsert") + "\n"
                    + "Tong dong doc: " + totalRows + "\n"
                    + "Them moi: " + inserted + "\n"
                    + "Cap nhat: " + updated + "\n"
                    + "Bo qua: " + skipped;
        }
    }
}