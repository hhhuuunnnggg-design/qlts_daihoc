package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.dao.BaseDao;
import com.tuyensinh.entity.Mon;
import com.tuyensinh.entity.ToHop;
import com.tuyensinh.entity.ToHopMon;
import com.tuyensinh.util.HibernateUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.persistence.EntityManager;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Import rieng danh muc to hop tu file tohopmon.xlsx.
 *
 * Lop nay tu doc cau truc xlsx bang java.util.zip + DOM, khong import lai Nganh,
 * Nguyen vong, Diem cong hay Bang quy doi. Khi gap to hop da ton tai, importer chi
 * cap nhat ten to hop va nap lai 3 dong xt_tohop_mon cua chinh to hop do.
 */
public class ToHopExcelImporter {

    public ImportResult importFromFile(File file) throws Exception {
        if (file == null || !file.isFile()) {
            throw new IllegalArgumentException("File import khong hop le.");
        }
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw new IllegalArgumentException("Vui long chon file Excel .xlsx, vi du: tohopmon.xlsx");
        }

        BaseDao.closeCurrentEm();
        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        try {
            Map<String, Mon> monByMa = loadMonByMa(em);
            Map<String, ToHopSource> sourceByMa = readToHopSources(file, monByMa);
            if (sourceByMa.isEmpty()) {
                throw new IllegalStateException("Khong doc duoc to hop nao tu file: " + file.getName());
            }

            em.getTransaction().begin();

            int inserted = 0;
            int updated = 0;
            int monRows = 0;

            for (ToHopSource src : sourceByMa.values()) {
                ToHop toHop = findToHopByMa(em, src.maToHop);
                boolean isNew = false;
                if (toHop == null) {
                    toHop = new ToHop();
                    toHop.setMaTohop(src.maToHop);
                    isNew = true;
                }
                toHop.setTenTohop(buildToHopName(src.monCodes, monByMa));

                if (isNew) {
                    em.persist(toHop);
                    em.flush();
                    inserted++;
                } else {
                    toHop = em.merge(toHop);
                    updated++;
                }

                em.createQuery("DELETE FROM ToHopMon tm WHERE tm.toHop.tohopId = :tohopId")
                        .setParameter("tohopId", toHop.getTohopId())
                        .executeUpdate();

                short thuTu = 1;
                for (String maMon : src.monCodes) {
                    Mon mon = monByMa.get(maMon);
                    if (mon == null) {
                        throw new IllegalStateException("Khong tim thay mon '" + maMon + "' trong xt_mon");
                    }

                    ToHopMon tm = new ToHopMon();
                    tm.setToHop(toHop);
                    tm.setMon(mon);
                    tm.setThuTu(thuTu++);
                    em.persist(tm);
                    monRows++;
                }
            }

            em.getTransaction().commit();
            BaseDao.closeCurrentEm();

            return new ImportResult(file.getAbsolutePath(), sourceByMa.size(), inserted, updated, monRows);
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

    private Map<String, ToHopSource> readToHopSources(File file, Map<String, Mon> monByMa) throws Exception {
        List<Map<Integer, String>> rows = readFirstSheet(file);
        Map<String, ToHopSource> result = new LinkedHashMap<>();

        int excelRow = 0;
        for (Map<Integer, String> row : rows) {
            excelRow++;
            String maNganh = normalizeCode(row.get(1));
            String rawToHop = safe(row.get(3));
            String maToHopCell = normalizeCode(row.get(5));

            if (isBlank(rawToHop) && isBlank(maToHopCell)) {
                continue;
            }
            if ("MANGANH".equalsIgnoreCase(maNganh) || "MA NGANH".equalsIgnoreCase(maNganh)) {
                continue;
            }

            ToHopSource src;
            try {
                src = parseToHopSource(rawToHop, maToHopCell, monByMa);
            } catch (Exception ex) {
                throw new IllegalStateException("Dong Excel " + excelRow + ": " + ex.getMessage(), ex);
            }

            ToHopSource old = result.get(src.maToHop);
            if (old == null) {
                result.put(src.maToHop, src);
            } else if (!old.monCodes.equals(src.monCodes)) {
                throw new IllegalStateException(
                        "To hop " + src.maToHop + " co cau truc mon khong thong nhat trong file."
                );
            }
        }

        return result;
    }

    private List<Map<Integer, String>> readFirstSheet(File file) throws Exception {
        try (ZipFile zip = new ZipFile(file)) {
            List<String> sharedStrings = readSharedStrings(zip);
            ZipEntry sheetEntry = zip.getEntry("xl/worksheets/sheet1.xml");
            if (sheetEntry == null) {
                throw new IllegalStateException("Khong tim thay sheet dau tien trong file xlsx.");
            }

            Document doc;
            try (InputStream in = zip.getInputStream(sheetEntry)) {
                doc = newDocument(in);
            }

            List<Map<Integer, String>> rows = new ArrayList<>();
            NodeList rowNodes = doc.getElementsByTagName("row");
            for (int i = 0; i < rowNodes.getLength(); i++) {
                Element rowEl = (Element) rowNodes.item(i);
                Map<Integer, String> values = new LinkedHashMap<>();
                NodeList cells = rowEl.getElementsByTagName("c");
                for (int j = 0; j < cells.getLength(); j++) {
                    Element cellEl = (Element) cells.item(j);
                    String ref = cellEl.getAttribute("r");
                    int colIndex = columnIndexFromRef(ref);
                    if (colIndex < 0) continue;
                    values.put(colIndex, readCellValue(cellEl, sharedStrings));
                }
                rows.add(values);
            }
            return rows;
        }
    }

    private List<String> readSharedStrings(ZipFile zip) throws Exception {
        List<String> result = new ArrayList<>();
        ZipEntry entry = zip.getEntry("xl/sharedStrings.xml");
        if (entry == null) {
            return result;
        }

        Document doc;
        try (InputStream in = zip.getInputStream(entry)) {
            doc = newDocument(in);
        }

        NodeList siNodes = doc.getElementsByTagName("si");
        for (int i = 0; i < siNodes.getLength(); i++) {
            Element si = (Element) siNodes.item(i);
            NodeList textNodes = si.getElementsByTagName("t");
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < textNodes.getLength(); j++) {
                sb.append(textNodes.item(j).getTextContent());
            }
            result.add(sb.toString().trim());
        }
        return result;
    }

    private Document newDocument(InputStream in) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setExpandEntityReferences(false);
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (Exception ignored) {
            // Mot so JRE khong ho tro day du cac feature bao ve XML. Bo qua de tranh loi khoi dong.
        }
        return factory.newDocumentBuilder().parse(in);
    }

    private String readCellValue(Element cellEl, List<String> sharedStrings) {
        String type = cellEl.getAttribute("t");
        if ("inlineStr".equals(type)) {
            NodeList textNodes = cellEl.getElementsByTagName("t");
            return textNodes.getLength() == 0 ? "" : textNodes.item(0).getTextContent().trim();
        }

        String raw = getFirstChildText(cellEl, "v");
        if (raw == null) raw = getFirstChildText(cellEl, "t");
        if (raw == null) return "";
        raw = raw.trim();

        if ("s".equals(type)) {
            try {
                int idx = Integer.parseInt(raw);
                return idx >= 0 && idx < sharedStrings.size() ? sharedStrings.get(idx) : "";
            } catch (NumberFormatException ex) {
                return "";
            }
        }

        return raw;
    }

    private String getFirstChildText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) return null;
        Node node = nodes.item(0);
        return node == null ? null : node.getTextContent();
    }

    private int columnIndexFromRef(String ref) {
        if (ref == null || ref.isEmpty()) return -1;
        int value = 0;
        boolean hasLetter = false;
        for (int i = 0; i < ref.length(); i++) {
            char ch = Character.toUpperCase(ref.charAt(i));
            if (ch < 'A' || ch > 'Z') break;
            value = value * 26 + (ch - 'A' + 1);
            hasLetter = true;
        }
        return hasLetter ? value - 1 : -1;
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

        List<String> monCodes = new ArrayList<>();
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
            monCodes.add(maMon);
        }

        if (monCodes.size() != 3) {
            throw new IllegalStateException("To hop '" + ma + "' khong co dung 3 mon: " + rawToHop);
        }

        ToHopSource src = new ToHopSource();
        src.maToHop = ma;
        src.monCodes = monCodes;
        return src;
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

    private ToHop findToHopByMa(EntityManager em, String maToHop) {
        List<ToHop> list = em.createQuery(
                        "FROM ToHop th WHERE UPPER(th.maTohop) = :ma", ToHop.class)
                .setParameter("ma", normalizeCode(maToHop))
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    private String buildToHopName(List<String> monCodes, Map<String, Mon> monByMa) {
        List<String> parts = new ArrayList<>();
        for (String maMon : monCodes) {
            Mon mon = monByMa.get(maMon);
            parts.add(mon != null && mon.getTenMon() != null ? mon.getTenMon() : maMon);
        }
        return String.join(" - ", parts);
    }

    private String normalizeCode(String s) {
        return s == null ? null : s.trim().toUpperCase(Locale.ROOT);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static class ToHopSource {
        private String maToHop;
        private List<String> monCodes = new ArrayList<>();
    }

    public static class ImportResult {
        private final String filePath;
        private final int totalToHop;
        private final int insertedToHop;
        private final int updatedToHop;
        private final int toHopMonRows;

        public ImportResult(String filePath, int totalToHop, int insertedToHop, int updatedToHop, int toHopMonRows) {
            this.filePath = filePath;
            this.totalToHop = totalToHop;
            this.insertedToHop = insertedToHop;
            this.updatedToHop = updatedToHop;
            this.toHopMonRows = toHopMonRows;
        }

        public String toHumanMessage() {
            return "Import to hop thanh cong!\n"
                    + "File: " + filePath + "\n\n"
                    + "Tong so to hop doc duoc: " + totalToHop + "\n"
                    + "Them moi: " + insertedToHop + "\n"
                    + "Cap nhat: " + updatedToHop + "\n"
                    + "So dong xt_tohop_mon da nap lai: " + toHopMonRows + "\n\n"
                    + "Luu y: chuc nang nay chi cap nhat xt_tohop va xt_tohop_mon, khong xoa nguyen vong/diem/nganh.";
        }
    }
}
