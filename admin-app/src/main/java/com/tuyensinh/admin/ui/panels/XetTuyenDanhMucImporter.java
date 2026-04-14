package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.dao.BaseDao;
import com.tuyensinh.entity.Mon;
import com.tuyensinh.entity.Nganh;
import com.tuyensinh.entity.NganhPhuongThuc;
import com.tuyensinh.entity.NganhToHop;
import com.tuyensinh.entity.PhuongThuc;
import com.tuyensinh.entity.ToHop;
import com.tuyensinh.entity.ToHopMon;
import com.tuyensinh.util.HibernateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.tuyensinh.entity.NganhToHopMon;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class XetTuyenDanhMucImporter {

    private static final String FILE_TOHOP = "tohopmon.xlsx";
    private static final String FILE_CHITIEU = "Chi tieu 2025.xlsx";
    private static final String FILE_NGUONG = "Nguong dau vao 2025.xlsx";

    private final DataFormatter formatter = new DataFormatter(Locale.US);

    public ImportResult importFromDirectory(File directory) throws Exception {
        if (directory == null || !directory.isDirectory()) {
            throw new IllegalArgumentException("Thu muc import khong hop le.");
        }

        File toHopFile = requireFile(directory, FILE_TOHOP);
        File chiTieuFile = requireFile(directory, FILE_CHITIEU);
        File nguongFile = requireFile(directory, FILE_NGUONG);

        BaseDao.closeCurrentEm();

        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        try {
            ParsedData data = readSourceData(toHopFile, chiTieuFile, nguongFile, em);

            PhuongThuc ptThpt = findPhuongThucByMa(em, PhuongThuc.THPT);
            if (ptThpt == null) {
                throw new IllegalStateException(
                        "Khong tim thay phuong thuc THPT trong DB. Hay giu lai seed phuong thuc."
                );
            }

            em.getTransaction().begin();

            clearOldDanhMuc(em);

            Map<String, ToHop> toHopByMa = importToHopAndMon(em, data);
            Map<String, Nganh> nganhByMa = importNganh(em, data, toHopByMa);
            Map<String, NganhToHop> nganhToHopByKey = importNganhToHop(em, data, nganhByMa, toHopByMa);
            Map<String, Mon> monByMa = loadMonByMa(em);
            int nganhToHopMonCount = importNganhToHopMon(em, data, nganhToHopByKey, monByMa);
            int nganhPhuongThucCount = importNganhPhuongThuc(em, ptThpt, nganhByMa);

            em.getTransaction().commit();
            BaseDao.closeCurrentEm();

            return new ImportResult(
                    directory.getAbsolutePath(),
                    nganhByMa.size(),
                    toHopByMa.size(),
                    data.totalToHopMonRows(),
                    nganhToHopByKey.size(),
                    nganhToHopMonCount,
                    nganhPhuongThucCount
            );
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            if (em.isOpen()) {
                em.close();
            }
            BaseDao.closeCurrentEm();
        }
    }

    private ParsedData readSourceData(File toHopFile, File chiTieuFile, File nguongFile, EntityManager em) throws Exception {
        ParsedData data = new ParsedData();
        Map<String, Mon> monByMa = loadMonByMa(em);

        readChiTieuFile(chiTieuFile, data);
        readNguongFile(nguongFile, data);
        readToHopFile(toHopFile, data, monByMa);

        if (data.nganhByMa.isEmpty()) {
            throw new IllegalStateException("Khong doc duoc nganh nao tu file Chi tieu 2025.xlsx");
        }
        if (data.toHopByMa.isEmpty()) {
            throw new IllegalStateException("Khong doc duoc to hop nao tu file tohopmon.xlsx");
        }
        if (data.linkRows.isEmpty()) {
            throw new IllegalStateException("Khong doc duoc lien ket nganh - to hop nao tu file tohopmon.xlsx");
        }

        return data;
    }

    private void readChiTieuFile(File file, ParsedData data) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                String maNganh = normalizeCode(readString(row.getCell(1)));
                String tenNganh = readString(row.getCell(2));
                Integer chiTieu = parseInteger(row.getCell(3));

                if (isBlank(maNganh)
                        || "MÃ CTĐT".equalsIgnoreCase(maNganh)
                        || "MA CTDT".equalsIgnoreCase(maNganh)) {
                    continue;
                }
                if (isBlank(tenNganh)) {
                    continue;
                }

                NganhSource ns = data.nganhByMa.get(maNganh);
                if (ns == null) {
                    ns = new NganhSource();
                    ns.maNganh = maNganh;
                    data.nganhByMa.put(maNganh, ns);
                }
                ns.tenNganh = tenNganh;
                ns.chiTieu = chiTieu != null ? chiTieu : 0;
            }
        }
    }

    private void readNguongFile(File file, ParsedData data) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                String maNganh = normalizeCode(readString(row.getCell(1)));
                BigDecimal diemSan = parseBigDecimal(row.getCell(3));

                if (isBlank(maNganh)
                        || "MÃ XÉT TUYỂN".equalsIgnoreCase(maNganh)
                        || "MA XET TUYEN".equalsIgnoreCase(maNganh)) {
                    continue;
                }
                if (diemSan == null) {
                    continue;
                }

                data.diemSanByMa.put(maNganh, diemSan);
            }
        }
    }

    private void readToHopFile(File file, ParsedData data, Map<String, Mon> monByMa) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                String maNganh = normalizeCode(readString(row.getCell(1)));
                String tenNganh = readString(row.getCell(2));
                String rawToHop = readString(row.getCell(3));
                String maToHopCell = normalizeCode(readString(row.getCell(5)));
                String goc = readString(row.getCell(6));
                BigDecimal doLech = parseBigDecimal(row.getCell(7));

                if (isBlank(maNganh) || "MANGANH".equalsIgnoreCase(maNganh)) {
                    continue;
                }
                if (isBlank(rawToHop) && isBlank(maToHopCell)) {
                    continue;
                }

                NganhSource ns = data.nganhByMa.get(maNganh);
                if (ns == null) {
                    ns = new NganhSource();
                    ns.maNganh = maNganh;
                    ns.tenNganh = isBlank(tenNganh) ? maNganh : tenNganh;
                    ns.chiTieu = 0;
                    data.nganhByMa.put(maNganh, ns);
                } else if (isBlank(ns.tenNganh) && !isBlank(tenNganh)) {
                    ns.tenNganh = tenNganh;
                }

                ToHopSource toHopSource = parseToHopSource(rawToHop, maToHopCell, monByMa);
                ToHopSource existing = data.toHopByMa.get(toHopSource.maToHop);
                if (existing == null) {
                    data.toHopByMa.put(toHopSource.maToHop, toHopSource);
                } else if (!existing.monCodes.equals(toHopSource.monCodes)) {
                    throw new IllegalStateException(
                            "To hop " + toHopSource.maToHop + " co cau truc mon khong thong nhat trong file tohopmon.xlsx"
                    );
                }

                NganhToHopLinkSource link = new NganhToHopLinkSource();
                link.maNganh = maNganh;
                link.maToHop = toHopSource.maToHop;
                link.doLech = doLech != null ? doLech : BigDecimal.ZERO;
                data.linkRows.add(link);

                if (!isBlank(goc)) {
                    String old = data.gocToHopByNganh.get(maNganh);
                    if (old != null && !old.equals(toHopSource.maToHop)) {
                        throw new IllegalStateException(
                                "Nganh " + maNganh + " co hon 1 to hop goc trong file tohopmon.xlsx"
                        );
                    }
                    data.gocToHopByNganh.put(maNganh, toHopSource.maToHop);
                }
            }
        }
    }

    private Map<String, ToHop> importToHopAndMon(EntityManager em, ParsedData data) {
        Map<String, ToHop> map = new LinkedHashMap<>();
        Map<String, Mon> monByMa = loadMonByMa(em);

        for (ToHopSource src : data.toHopByMa.values()) {
            ToHop th = new ToHop();
            th.setMaTohop(src.maToHop);
            th.setTenTohop(buildToHopName(src.monCodes, monByMa));
            em.persist(th);
            map.put(src.maToHop, th);

            short thuTu = 1;
            for (String maMon : src.monCodes) {
                Mon mon = monByMa.get(maMon);
                if (mon == null) {
                    throw new IllegalStateException("Khong tim thay mon '" + maMon + "' trong xt_mon");
                }

                ToHopMon tm = new ToHopMon();
                tm.setToHop(th);
                tm.setMon(mon);
                tm.setThuTu(thuTu++);
                em.persist(tm);
            }
        }

        return map;
    }

    private Map<String, Nganh> importNganh(EntityManager em, ParsedData data, Map<String, ToHop> toHopByMa) {
        Map<String, Nganh> map = new LinkedHashMap<>();

        for (NganhSource src : data.nganhByMa.values()) {
            Nganh n = new Nganh();
            n.setMaNganh(src.maNganh);
            n.setTenNganh(isBlank(src.tenNganh) ? src.maNganh : src.tenNganh);
            n.setChiTieu(src.chiTieu != null ? src.chiTieu : 0);
            n.setDiemSan(data.diemSanByMa.get(src.maNganh));
            n.setDiemTrungTuyen(null);
            n.setIsActive(true);

            String maToHopGoc = data.gocToHopByNganh.get(src.maNganh);
            if (!isBlank(maToHopGoc)) {
                n.setToHopGoc(toHopByMa.get(maToHopGoc));
            }

            em.persist(n);
            map.put(src.maNganh, n);
        }

        return map;
    }

    private Map<String, NganhToHop> importNganhToHop(EntityManager em,
                                                     ParsedData data,
                                                     Map<String, Nganh> nganhByMa,
                                                     Map<String, ToHop> toHopByMa) {
        Map<String, NganhToHop> map = new LinkedHashMap<>();

        for (NganhToHopLinkSource src : data.linkRows) {
            Nganh nganh = nganhByMa.get(src.maNganh);
            ToHop toHop = toHopByMa.get(src.maToHop);

            if (nganh == null) {
                throw new IllegalStateException(
                        "Khong tim thay nganh '" + src.maNganh + "' de tao lien ket nganh - to hop"
                );
            }
            if (toHop == null) {
                throw new IllegalStateException(
                        "Khong tim thay to hop '" + src.maToHop + "' de tao lien ket nganh - to hop"
                );
            }

            String key = buildNganhToHopKey(src.maNganh, src.maToHop);
            if (map.containsKey(key)) {
                continue;
            }

            NganhToHop nt = new NganhToHop();
            nt.setNganh(nganh);
            nt.setToHop(toHop);
            nt.setDoLech(src.doLech != null ? src.doLech : BigDecimal.ZERO);
            em.persist(nt);
            map.put(key, nt);
        }

        return map;
    }

    private int importNganhToHopMon(EntityManager em,
                                    ParsedData data,
                                    Map<String, NganhToHop> nganhToHopByKey,
                                    Map<String, Mon> monByMa) {
        int count = 0;

        for (Map.Entry<String, NganhToHop> entry : nganhToHopByKey.entrySet()) {
            NganhToHop nganhToHop = entry.getValue();
            String maToHop = nganhToHop.getToHop() != null
                    ? normalizeCode(nganhToHop.getToHop().getMaTohop())
                    : null;

            ToHopSource toHopSource = data.toHopByMa.get(maToHop);
            if (toHopSource == null || toHopSource.monSpecs.isEmpty()) {
                throw new IllegalStateException(
                        "Khong tim thay cau hinh mon cho to hop '" + maToHop + "'"
                );
            }

            for (ToHopMonSource monSpec : toHopSource.monSpecs) {
                Mon mon = monByMa.get(monSpec.maMon);
                if (mon == null) {
                    throw new IllegalStateException(
                            "Khong tim thay mon '" + monSpec.maMon + "' trong xt_mon"
                    );
                }

                NganhToHopMon nthm = new NganhToHopMon();
                nthm.setNganhToHop(nganhToHop);
                nthm.setMon(mon);
                nthm.setHeSo(monSpec.heSo);
                nthm.setIsMonChinh(monSpec.isMonChinh);
                em.persist(nthm);
                count++;
            }
        }

        return count;
    }

    private String buildNganhToHopKey(String maNganh, String maToHop) {
        return normalizeCode(maNganh) + "__" + normalizeCode(maToHop);
    }

    private int importNganhPhuongThuc(EntityManager em, PhuongThuc ptThpt, Map<String, Nganh> nganhByMa) {
        int count = 0;

        for (Nganh nganh : nganhByMa.values()) {
            NganhPhuongThuc npt = new NganhPhuongThuc();
            npt.setNganh(nganh);
            npt.setPhuongThuc(ptThpt);
            npt.setChiTieu(nganh.getChiTieu());
            npt.setSoLuongHienTai(0);
            npt.setIsEnabled(true);
            em.persist(npt);
            count++;
        }

        return count;
    }

    private void clearOldDanhMuc(EntityManager em) {
        em.createNativeQuery("DELETE FROM xt_nguyenvong").executeUpdate();
        em.createNativeQuery("DELETE FROM xt_diemcong_chitiet").executeUpdate();
        em.createNativeQuery("DELETE FROM xt_diemcong").executeUpdate();
        em.createNativeQuery("DELETE FROM xt_nganh_tohop_mon").executeUpdate();
        em.createNativeQuery("DELETE FROM xt_bangquydoi").executeUpdate();
        em.createNativeQuery("DELETE FROM xt_nganh_phuongthuc").executeUpdate();
        em.createNativeQuery("DELETE FROM xt_nganh_tohop").executeUpdate();
        em.createNativeQuery("UPDATE xt_nganh SET tohop_goc_id = NULL").executeUpdate();
        em.createNativeQuery("DELETE FROM xt_nganh").executeUpdate();
        em.createNativeQuery("DELETE FROM xt_tohop_mon").executeUpdate();
        em.createNativeQuery("DELETE FROM xt_tohop").executeUpdate();
    }

    private Map<String, Mon> loadMonByMa(EntityManager em) {
        List<Mon> list = em.createQuery("FROM Mon", Mon.class).getResultList();
        Map<String, Mon> map = new LinkedHashMap<>();
        for (Mon m : list) {
            if (m.getMaMon() != null) {
                map.put(normalizeCode(m.getMaMon()), m);
            }
        }
        return map;
    }

    private PhuongThuc findPhuongThucByMa(EntityManager em, String ma) {
        List<PhuongThuc> list = em.createQuery(
                        "FROM PhuongThuc p WHERE p.maPhuongthuc = :ma", PhuongThuc.class)
                .setParameter("ma", ma)
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    private ToHopSource parseToHopSource(String rawToHop, String maToHopCell, Map<String, Mon> monByMa) {
        String raw = rawToHop != null ? rawToHop.trim() : "";
        String ma = maToHopCell;
        String body = "";

        int idxOpen = raw.indexOf('(');
        int idxClose = raw.lastIndexOf(')');
        if (idxOpen > 0 && idxClose > idxOpen) {
            if (isBlank(ma)) {
                ma = normalizeCode(raw.substring(0, idxOpen));
            }
            body = raw.substring(idxOpen + 1, idxClose);
        }

        if (isBlank(ma)) {
            throw new IllegalStateException("Khong phan tich duoc ma to hop tu gia tri: " + rawToHop);
        }
        if (isBlank(body)) {
            throw new IllegalStateException("Khong phan tich duoc danh sach mon trong to hop: " + rawToHop);
        }

        List<ToHopMonSource> monSpecs = new ArrayList<>();
        short maxHeSo = 1;

        for (String token : body.split(",")) {
            String part = token == null ? "" : token.trim();
            if (part.isEmpty()) continue;

            int dashIdx = part.indexOf('-');
            String maMon = normalizeCode(dashIdx > 0 ? part.substring(0, dashIdx) : part);
            if (isBlank(maMon)) continue;

            if (!monByMa.containsKey(maMon)) {
                throw new IllegalStateException(
                        "Mon '" + maMon + "' trong to hop '" + ma + "' chua co trong xt_mon"
                );
            }

            Short heSo = parseHeSoToken(part, ma, rawToHop);
            if (heSo > maxHeSo) {
                maxHeSo = heSo;
            }

            ToHopMonSource monSource = new ToHopMonSource();
            monSource.maMon = maMon;
            monSource.heSo = heSo;
            monSpecs.add(monSource);
        }

        if (monSpecs.size() != 3) {
            throw new IllegalStateException("To hop '" + ma + "' khong co dung 3 mon: " + rawToHop);
        }

        List<String> monCodes = new ArrayList<>();
        for (ToHopMonSource monSpec : monSpecs) {
            monSpec.isMonChinh = monSpec.heSo != null
                    && monSpec.heSo.shortValue() == maxHeSo
                    && maxHeSo > 1;
            monCodes.add(monSpec.maMon);
        }

        ToHopSource src = new ToHopSource();
        src.maToHop = ma;
        src.monCodes = monCodes;
        src.monSpecs = monSpecs;
        return src;
    }

    private Short parseHeSoToken(String token, String maToHop, String rawToHop) {
        int dashIdx = token.indexOf('-');
        if (dashIdx < 0 || dashIdx == token.length() - 1) {
            return 1;
        }

        String heSoText = token.substring(dashIdx + 1).trim();
        if (isBlank(heSoText)) {
            return 1;
        }

        try {
            return Short.parseShort(cleanNumberText(heSoText));
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Khong doc duoc he so mon trong to hop '" + maToHop + "': " + rawToHop
            );
        }
    }

    private String buildToHopName(List<String> monCodes, Map<String, Mon> monByMa) {
        List<String> parts = new ArrayList<>();
        for (String maMon : monCodes) {
            Mon mon = monByMa.get(maMon);
            parts.add(mon != null && mon.getTenMon() != null ? mon.getTenMon() : maMon);
        }
        return String.join(" - ", parts);
    }

    private File requireFile(File directory, String expectedName) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile() && f.getName().equalsIgnoreCase(expectedName)) {
                    return f;
                }
            }
        }
        throw new IllegalArgumentException(
                "Khong tim thay file '" + expectedName + "' trong thu muc: " + directory.getAbsolutePath()
        );
    }

    private String readString(Cell cell) {
        if (cell == null) return "";
        return formatter.formatCellValue(cell).trim();
    }

    private Integer parseInteger(Cell cell) {
        String s = readString(cell);
        if (isBlank(s)) return null;
        try {
            return Integer.parseInt(cleanNumberText(s));
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(Cell cell) {
        String s = readString(cell);
        if (isBlank(s)) return null;
        try {
            return new BigDecimal(cleanDecimalText(s));
        } catch (Exception e) {
            return null;
        }
    }

    private String cleanNumberText(String s) {
        String t = s.trim().replace(",", "");
        int dot = t.indexOf('.');
        if (dot >= 0) {
            t = t.substring(0, dot);
        }
        return t;
    }

    private String cleanDecimalText(String s) {
        return s.trim().replace(",", "");
    }

    private String normalizeCode(String s) {
        return s == null ? null : s.trim().toUpperCase();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static class ParsedData {
        private final Map<String, NganhSource> nganhByMa = new LinkedHashMap<>();
        private final Map<String, BigDecimal> diemSanByMa = new LinkedHashMap<>();
        private final Map<String, ToHopSource> toHopByMa = new LinkedHashMap<>();
        private final List<NganhToHopLinkSource> linkRows = new ArrayList<>();
        private final Map<String, String> gocToHopByNganh = new LinkedHashMap<>();

        private int totalToHopMonRows() {
            int total = 0;
            for (ToHopSource src : toHopByMa.values()) {
                total += src.monCodes.size();
            }
            return total;
        }
    }

    private static class NganhSource {
        private String maNganh;
        private String tenNganh;
        private Integer chiTieu;
    }

    private static class ToHopSource {
        private String maToHop;
        private List<String> monCodes = new ArrayList<>();
        private List<ToHopMonSource> monSpecs = new ArrayList<>();
    }

    private static class ToHopMonSource {
        private String maMon;
        private Short heSo = 1;
        private Boolean isMonChinh = false;
    }

    private static class NganhToHopLinkSource {
        private String maNganh;
        private String maToHop;
        private BigDecimal doLech;
    }

    public static class ImportResult {
        private final String directoryPath;
        private final int nganhCount;
        private final int toHopCount;
        private final int toHopMonCount;
        private final int nganhToHopCount;
        private final int nganhToHopMonCount;
        private final int nganhPhuongThucCount;

        public ImportResult(String directoryPath,
                            int nganhCount,
                            int toHopCount,
                            int toHopMonCount,
                            int nganhToHopCount,
                            int nganhToHopMonCount,
                            int nganhPhuongThucCount) {
            this.directoryPath = directoryPath;
            this.nganhCount = nganhCount;
            this.toHopCount = toHopCount;
            this.toHopMonCount = toHopMonCount;
            this.nganhToHopCount = nganhToHopCount;
            this.nganhToHopMonCount = nganhToHopMonCount;
            this.nganhPhuongThucCount = nganhPhuongThucCount;
        }

        public String toHumanMessage() {
            return "Import danh muc thanh cong!\n"
                    + "Thu muc: " + directoryPath + "\n\n"
                    + "So nganh: " + nganhCount + "\n"
                    + "So to hop: " + toHopCount + "\n"
                    + "So dong to hop - mon: " + toHopMonCount + "\n"
                    + "So lien ket nganh - to hop: " + nganhToHopCount + "\n"
                    + "So dong nganh - to hop - mon: " + nganhToHopMonCount + "\n"
                    + "So dong nganh - phuong thuc: " + nganhPhuongThucCount;
        }
    }
}