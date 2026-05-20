package com.tuyensinh.web.servlet;

import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import com.tuyensinh.web.servlet.base.ModelAndView;
import com.tuyensinh.web.servlet.dto.NganhToHopDto;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class TraCuuDiemServlet extends BaseServlet {

    private final XetTuyenService xetTuyenService = new XetTuyenService();
    private final DoiTuongService doiTuongService = new DoiTuongService();
    private final KhuVucService khuVucService = new KhuVucService();
    private final com.tuyensinh.dao.BangQuyDoiDao bangQuyDoiDao =
            new com.tuyensinh.dao.BangQuyDoiDao();

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal THIRTY = new BigDecimal("30");
    private static final int SCALE = 3;

    // === GET: hiển thị form ===
    @Override
    protected ModelAndView handleGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ModelAndView auth = authController.requireLogin(request, response);
        if (auth != null) return auth;
        return pageWithData(buildNganhToHopDtoList());
    }

    // === POST: xử lý tính điểm ===
    @Override
    protected ModelAndView handlePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ModelAndView auth = authController.requireLogin(request, response);
        if (auth != null) return auth;

        String ptId = reqParam(request, "phuongThucId");
        String nthId = reqParam(request, "nganhToHopId");
        String dtId = reqParam(request, "doiTuongId");
        String kvId = reqParam(request, "khuVucId");

        if (isBlank(ptId) || isBlank(nthId)) {
            setMessage(request, "Vui lòng chọn đầy đủ thông tin.", "danger");
            return reloadPage(null);
        }

        try {
            NganhToHop nganhToHop = findNganhToHop(nthId);
            PhuongThuc phuongThuc = findPhuongThuc(ptId);

            if (nganhToHop == null || phuongThuc == null) {
                setMessage(request, "Không tìm thấy ngành hoặc phương thức xét tuyển.", "danger");
                return reloadPage(null);
            }

            Map<String, Object> ketQua = buildKetQua(request, nganhToHop, phuongThuc, dtId, kvId);
            return reloadPage(ketQua);

        } catch (Exception e) {
            setMessage(request, "Đã xảy ra lỗi: " + e.getMessage(), "danger");
            return reloadPage(null);
        }
    }

    // === Tính điểm cho DGNL (dùng BQD với nội suy tuyến tính) ===
    // Trả về ĐTHGXT (điểm sau quy đổi, trước khi cộng ưu tiên)
    private BigDecimal buildDgnlResult(HttpServletRequest request, Map<String, Object> result,
            PhuongThuc pt, ToHop toHop, NganhToHop nganhToHop) {

        BigDecimal diemDgnl = parseDiem(reqParam(request, "diemDgnl"));
        result.put("diemDgnl", diemDgnl);

        BigDecimal diemThgxt = ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal diemSauDoLech = ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        String ghiChuDoLech = "";
        String thgxtDisplay = null;
        boolean coBangQuyDoi = false;

        if (diemDgnl != null && diemDgnl.compareTo(ZERO) > 0) {
            // Ưu tiên 1: Dùng quy đổi theo đúng tổ hợp hiện tại (nếu có bảng riêng)
            BangQuyDoi bqdTuyChon = bangQuyDoiDao.quyDoiDiem(
                    pt.getPhuongthucId(),
                    toHop != null ? toHop.getTohopId() : null,
                    null,
                    diemDgnl
            );

            // Ưu tiên 2: Dùng quy đổi không phụ thuộc tổ hợp (giống admin)
            // quyDoiDiemBatKyToHop KHÔNG filter theo toHop, nên luôn tìm được
            // bảng DGNL đại diện A01/B00/C01/D01 trong DB
            BangQuyDoi bqd = bqdTuyChon;
            if (bqd == null) {
                bqd = bangQuyDoiDao.quyDoiDiemBatKyToHop(
                        pt.getPhuongthucId(),
                        null,
                        diemDgnl
                );
            }

            if (bqd != null) {
                coBangQuyDoi = true;
                diemThgxt = quyDoi(bqd, diemDgnl);

                BigDecimal a = bqd.getDiemTu();
                BigDecimal b = bqd.getDiemDen();
                BigDecimal c = bqd.getDiemQuydoiTu();
                BigDecimal d = bqd.getDiemQuydoiDen();

                thgxtDisplay = formatDiem(diemThgxt)
                        + " = " + formatDiem(c) + " + (" + formatDiem(diemDgnl) + " − " + formatDiem(a)
                        + ") × (" + formatDiem(d) + " − " + formatDiem(c) + ") / (" + formatDiem(b) + " − " + formatDiem(a) + ")"
                        + "  &nbsp;|&nbsp;  Bảng QD: [" + formatDiem(a) + "–" + formatDiem(b)
                        + "] → [" + formatDiem(c) + "–" + formatDiem(d) + "]";
            } else {
                // Không có BQD: dùng công thức gốc ĐDGNL × 30 / 1200
                diemThgxt = diemDgnl.multiply(THIRTY)
                        .divide(new BigDecimal("1200"), SCALE, RoundingMode.HALF_UP);
                thgxtDisplay = formatDiem(diemThgxt)
                        + " = " + formatDiem(diemDgnl) + " × 30 / 1200"
                        + "  &nbsp;|&nbsp;  <em class=\"text-warning\">Không tìm thấy BQD — dùng công thức gốc</em>";
            }

            // Trừ độ lệch tổ hợp (nếu có)
            diemSauDoLech = diemThgxt;
            if (nganhToHop.getDoLech() != null && nganhToHop.getDoLech().compareTo(ZERO) > 0) {
                diemSauDoLech = clamp30(diemThgxt.subtract(nganhToHop.getDoLech()));
                BigDecimal dl = nganhToHop.getDoLech().setScale(2, RoundingMode.HALF_UP);
                ghiChuDoLech = "Trừ độ lệch " + dl + " điểm (tổ hợp " + toHop.getMaTohop() + " → gốc)";
            }
        }

        result.put("coBangQuyDoi", coBangQuyDoi);
        result.put("diemThgxt", diemThgxt);
        result.put("diemThgxtDisplay", thgxtDisplay);
        result.put("diemSauDoLech", diemSauDoLech);
        result.put("ghiChuDoLech", ghiChuDoLech);
        result.put("diemThxt", diemSauDoLech);

        return diemThgxt;
    }

    // === Tính điểm cho VSAT (thang điểm 150 → thang điểm 10 dùng BQD theo môn) ===
    private BigDecimal buildVsatResult(HttpServletRequest request, Map<String, Object> result,
            PhuongThuc pt, ToHop toHop, NganhToHop nganhToHop) {

        List<NganhToHopMon> monList = nganhToHop.getDanhSachNganhToHopMon();
        if (monList == null) monList = List.of();

        List<Map<String, Object>> diemMonList = new ArrayList<>();
        BigDecimal tongDiem = ZERO;
        BigDecimal tongHeSo = ZERO;
        boolean coBangQuyDoi = false;
        String thgxtDisplay = null;

        for (NganhToHopMon nthm : monList) {
            Mon mon = nthm.getMon();
            if (mon == null) continue;

            String diemRaw = reqParam(request, "diem_mon_" + mon.getMonId());
            BigDecimal diemGoc = parseDiem(diemRaw);
            BigDecimal diemSauQd = null;
            String ghiChuQd = null;

            if (diemGoc != null) {
                // VSAT: tra BQD theo phuongthuc_id + mon_id (tohopId = null)
                BangQuyDoi bqd = bangQuyDoiDao.quyDoiDiem(
                        pt.getPhuongthucId(),
                        null,
                        mon.getMonId(),
                        diemGoc
                );
                if (bqd != null) {
                    coBangQuyDoi = true;
                    diemSauQd = quyDoi(bqd, diemGoc);
                    if (diemSauQd != null) {
                        BigDecimal a = bqd.getDiemTu();
                        BigDecimal b = bqd.getDiemDen();
                        BigDecimal c = bqd.getDiemQuydoiTu();
                        BigDecimal d = bqd.getDiemQuydoiDen();
                        ghiChuQd = formatDiem(diemSauQd)
                                + " = " + formatDiem(c) + " + (" + formatDiem(diemGoc) + " − " + formatDiem(a)
                                + ") × (" + formatDiem(d) + " − " + formatDiem(c) + ") / (" + formatDiem(b) + " − " + formatDiem(a) + ")"
                                + " &nbsp;|&nbsp; BQD: [" + formatDiem(a) + "–" + formatDiem(b)
                                + "] → [" + formatDiem(c) + "–" + formatDiem(d) + "]";
                    }
                } else {
                    // Không có BQD → dùng công thức gốc: diemGoc × 10 / 150
                    diemSauQd = diemGoc.multiply(new BigDecimal("10"))
                            .divide(new BigDecimal("150"), SCALE, RoundingMode.HALF_UP);
                    ghiChuQd = formatDiem(diemSauQd)
                            + " = " + formatDiem(diemGoc) + " × 10 / 150"
                            + " &nbsp;|&nbsp; <em class=\"text-warning\">Không tìm thấy BQD — dùng công thức gốc</em>";
                }
            }

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("monId", mon.getMonId());
            m.put("maMon", mon.getMaMon());
            m.put("tenMon", mon.getTenMon());
            m.put("heSo", nthm.getHeSo() != null ? nthm.getHeSo().intValue() : 1);
            m.put("diemGoc", diemGoc);
            m.put("diemSauQd", diemSauQd);
            m.put("ghiChuQd", ghiChuQd);
            diemMonList.add(m);

            BigDecimal diemDung = diemSauQd != null ? diemSauQd : diemGoc;
            if (diemDung == null) diemDung = ZERO;

            BigDecimal heSoVal = nthm.getHeSo() != null
                    ? new BigDecimal(nthm.getHeSo().toString()) : BigDecimal.ONE;
            if (heSoVal.compareTo(ZERO) <= 0) heSoVal = BigDecimal.ONE;
            tongHeSo = tongHeSo.add(heSoVal);
            tongDiem = tongDiem.add(diemDung.multiply(heSoVal).setScale(SCALE, RoundingMode.HALF_UP));
        }

        result.put("diemMonList", diemMonList);
        result.put("coBangQuyDoi", coBangQuyDoi);

        // ĐTHGXT = (tổ(d × w) / tổ(w)) × 3
        BigDecimal diemThgxt = ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        if (tongHeSo.compareTo(ZERO) > 0 && !diemMonList.isEmpty()) {
            BigDecimal tb = tongDiem.divide(tongHeSo, SCALE, RoundingMode.HALF_UP);
            diemThgxt = clamp30(tb.multiply(new BigDecimal("3")));

            List<String> parts = new ArrayList<>();
            for (Map<String, Object> m : diemMonList) {
                BigDecimal d = (BigDecimal) m.get("diemSauQd");
                Integer hs = (Integer) m.get("heSo");
                if (d != null) {
                    parts.add("(" + formatDiem(d) + "×" + hs + ")");
                }
            }
            if (!parts.isEmpty()) {
                String sumStr = String.join(" + ", parts);
                thgxtDisplay = formatDiem(diemThgxt)
                        + " = [" + sumStr + "] / " + tongHeSo + " × 3";
            }
        }
        result.put("diemThgxtDisplay", thgxtDisplay);

        // Trừ độ lệch
        BigDecimal diemThxt = diemThgxt;
        String ghiChuDoLech = "";
        if (nganhToHop.getDoLech() != null && nganhToHop.getDoLech().compareTo(ZERO) > 0) {
            diemThxt = clamp30(diemThgxt.subtract(nganhToHop.getDoLech()));
            BigDecimal dl = nganhToHop.getDoLech().setScale(2, RoundingMode.HALF_UP);
            ghiChuDoLech = "Trừ độ lệch " + dl + " điểm (tổ hợp " + toHop.getMaTohop() + " → gốc)";
        }

        result.put("diemThgxt", diemThgxt);
        result.put("diemSauDoLech", diemThxt);
        result.put("ghiChuDoLech", ghiChuDoLech);
        result.put("diemThxt", diemThxt);

        return diemThgxt;
    }

    // === Tính điểm cho THPT (dùng BQD) ===
    // Trả về ĐTHGXT
    private BigDecimal buildThptResult(HttpServletRequest request, Map<String, Object> result,
            PhuongThuc pt, ToHop toHop, NganhToHop nganhToHop) {

        List<NganhToHopMon> monList = nganhToHop.getDanhSachNganhToHopMon();
        if (monList == null) monList = List.of();

        List<Map<String, Object>> diemMonList = new ArrayList<>();
        BigDecimal tongDiem = ZERO;
        BigDecimal tongHeSo = ZERO;

        for (NganhToHopMon nthm : monList) {
            Mon mon = nthm.getMon();
            if (mon == null) continue;

            String diemRaw = reqParam(request, "diem_mon_" + mon.getMonId());
            BigDecimal diemGoc = parseDiem(diemRaw);
            BigDecimal diemSauQd = null;
            String ghiChuQd = null;

            if (diemGoc != null) {
                BangQuyDoi bqd = bangQuyDoiDao.quyDoiDiem(
                        pt.getPhuongthucId(),
                        toHop != null ? toHop.getTohopId() : null,
                        mon.getMonId(),
                        diemGoc
                );
                if (bqd != null) {
                    diemSauQd = quyDoi(bqd, diemGoc);
                    if (diemSauQd != null) {
                        ghiChuQd = formatDiem(diemGoc)
                                + " → [" + formatDiem(bqd.getDiemTu()) + "–" + formatDiem(bqd.getDiemDen())
                                + "] → " + formatDiem(bqd.getDiemQuydoiTu()) + "–" + formatDiem(bqd.getDiemQuydoiDen());
                    }
                }
            }

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("monId", mon.getMonId());
            m.put("maMon", mon.getMaMon());
            m.put("tenMon", mon.getTenMon());
            m.put("heSo", nthm.getHeSo() != null ? nthm.getHeSo().intValue() : 1);
            m.put("diemGoc", diemGoc);
            m.put("diemSauQd", diemSauQd);
            m.put("ghiChuQd", ghiChuQd);
            diemMonList.add(m);

            BigDecimal diemDung = diemSauQd != null ? diemSauQd : diemGoc;
            if (diemDung == null) diemDung = ZERO;

            BigDecimal heSoVal = nthm.getHeSo() != null
                    ? new BigDecimal(nthm.getHeSo().toString()) : BigDecimal.ONE;
            if (heSoVal.compareTo(ZERO) <= 0) heSoVal = BigDecimal.ONE;
            tongHeSo = tongHeSo.add(heSoVal);

            tongDiem = tongDiem.add(diemDung.multiply(heSoVal).setScale(SCALE, RoundingMode.HALF_UP));
        }

        result.put("diemMonList", diemMonList);

        // ĐTHGXT = (tổ(d × w) / tổ(w)) × 3
        BigDecimal diemThgxt = ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        String thgxtDisplay = null;
        if (tongHeSo.compareTo(ZERO) > 0 && !diemMonList.isEmpty()) {
            BigDecimal tb = tongDiem.divide(tongHeSo, SCALE, RoundingMode.HALF_UP);
            diemThgxt = clamp30(tb.multiply(new BigDecimal("3")));

            List<String> parts = new ArrayList<>();
            for (Map<String, Object> m : diemMonList) {
                BigDecimal d = (BigDecimal) m.get("diemGoc");
                Integer hs = (Integer) m.get("heSo");
                if (d != null) {
                    parts.add("(" + formatDiem(d) + "×" + hs + ")");
                }
            }
            if (!parts.isEmpty()) {
                String sumStr = String.join(" + ", parts);
                thgxtDisplay = formatDiem(diemThgxt)
                        + " = [" + sumStr + "] / " + tongHeSo + " × 3";
            }
        }
        result.put("diemThgxtDisplay", thgxtDisplay);

        // Trừ độ lệch
        BigDecimal diemThxt = diemThgxt;
        String ghiChuDoLech = "";
        if (nganhToHop.getDoLech() != null && nganhToHop.getDoLech().compareTo(ZERO) > 0) {
            diemThxt = clamp30(diemThgxt.subtract(nganhToHop.getDoLech()));
            BigDecimal dl = nganhToHop.getDoLech().setScale(2, RoundingMode.HALF_UP);
            ghiChuDoLech = "Trừ độ lệch " + dl + " điểm (tổ hợp " + toHop.getMaTohop() + " → gốc)";
        }

        result.put("diemThgxt", diemThgxt);
        result.put("diemSauDoLech", diemThxt);
        result.put("ghiChuDoLech", ghiChuDoLech);
        result.put("diemThxt", diemThxt);

        return diemThgxt;
    }

    // === Điểm ưu tiên — hàm tái sử dụng cho mọi phương thức ===
    // Đọc khu vực / đối tượng từ result map.
    // ĐTHGXT là điểm tổ hợp gốc (trước khi cộng ưu tiên).
    // Nếu ĐTHGXT >= 22.5 → áp dụng công thức giảm điểm cộng.
    // Trả về Map chứa đầy đủ thông tin để JSP hiển thị.
    private Map<String, Object> tinhDiemUuTien(Map<String, Object> result, BigDecimal diemThgxt) {
        Map<String, Object> ut = new LinkedHashMap<>();

        BigDecimal kvDiem = ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal dtDiem = ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        String ghiChuKv = null;
        String ghiChuDt = null;

        KhuVucUutien kv = (KhuVucUutien) result.get("khuVuc");
        if (kv != null && kv.getMucDiem() != null) {
            kvDiem = kv.getMucDiem().setScale(SCALE, RoundingMode.HALF_UP);
            ghiChuKv = "Khu vực " + kv.getMaKhuVuc() + " (" + kvDiem.setScale(2) + " đ)";
        }

        DoiTuongUutien dt = (DoiTuongUutien) result.get("doiTuong");
        if (dt != null && dt.getMucDiem() != null) {
            dtDiem = dt.getMucDiem().setScale(SCALE, RoundingMode.HALF_UP);
            ghiChuDt = "Đối tượng " + dt.getMaDoituong() + " (" + dtDiem.setScale(2) + " đ)";
        }

        BigDecimal tongUuTien = kvDiem.add(dtDiem);
        ut.put("tongUuTien", tongUuTien);

        // Công thức SGU: nếu ĐTHGXT >= 22.5 thì giảm điểm cộng theo tỷ lệ
        BigDecimal NGUONG_225 = new BigDecimal("22.5");
        BigDecimal diemCong = tongUuTien;
        String ghiChuCong = null;

        if (diemThgxt != null && diemThgxt.compareTo(NGUONG_225) >= 0 && tongUuTien.compareTo(ZERO) > 0) {
            BigDecimal heSo = new BigDecimal("30").subtract(diemThgxt)
                    .divide(new BigDecimal("7.5"), SCALE, RoundingMode.HALF_UP);
            diemCong = tongUuTien.multiply(heSo).setScale(SCALE, RoundingMode.HALF_UP);
            String tongStr = formatDiem(tongUuTien);
            String heSoStr = formatDiem(heSo);
            String thgxtStr = formatDiem(diemThgxt);
            ghiChuCong = tongStr + " × (30 − " + thgxtStr + ") / 7.5"
                    + " = " + formatDiem(diemCong)
                    + " &nbsp;|&nbsp; <em class=\"text-warning\">Hệ số " + heSoStr
                    + " (ĐTHGXT ≥ 22.5)</em>";
        }

        ut.put("kvDiem", kvDiem);
        ut.put("dtDiem", dtDiem);
        ut.put("diemCong", diemCong);
        ut.put("ghiChuKv", ghiChuKv);
        ut.put("ghiChuDt", ghiChuDt);
        ut.put("ghiChuCong", ghiChuCong);
        return ut;
    }

    // === Đánh giá kết quả ===
    private void evaluateKetQua(Map<String, Object> result, BigDecimal diemXetTuyen, Nganh nganh) {
        if (diemXetTuyen == null || diemXetTuyen.compareTo(ZERO) <= 0) {
            result.put("dat", false);
            result.put("lyDo", "Chưa nhập điểm thi.");
            return;
        }

        boolean dat = true;
        List<String> reasons = new ArrayList<>();

        if (nganh.getDiemSan() != null && diemXetTuyen.compareTo(nganh.getDiemSan()) < 0) {
            dat = false;
            reasons.add("Điểm xét tuyển (" + formatDiem(diemXetTuyen)
                    + ") thấp hơn điểm sàn ngành (" + formatDiem(nganh.getDiemSan()) + ").");
        }

        if (nganh.getDiemTrungTuyen() != null
                && diemXetTuyen.compareTo(nganh.getDiemTrungTuyen()) < 0) {
            dat = false;
            reasons.add("Điểm xét tuyển (" + formatDiem(diemXetTuyen)
                    + ") thấp hơn điểm trúng tuyển ngành (" + formatDiem(nganh.getDiemTrungTuyen()) + ").");
        }

        result.put("dat", dat);
        result.put("lyDo", reasons.isEmpty() ? null : String.join(" ", reasons));
    }

    // === Build toàn bộ kết quả ===
    private Map<String, Object> buildKetQua(HttpServletRequest request,
            NganhToHop nganhToHop, PhuongThuc pt, String dtId, String kvId) {

        Map<String, Object> result = new LinkedHashMap<>();

        Nganh nganh = nganhToHop.getNganh();
        ToHop toHop = nganhToHop.getToHop();
        result.put("nganhToHop", convertToDto(nganhToHop));
        result.put("phuongThuc", pt);
        result.put("doiTuong", findDoiTuong(dtId));
        result.put("khuVuc", findKhuVuc(kvId));
        result.put("nganh", nganh);

        BigDecimal diemThgxt = ZERO;
        if (isDgnl(pt)) {
            diemThgxt = buildDgnlResult(request, result, pt, toHop, nganhToHop);
        } else if (isVsat(pt)) {
            diemThgxt = buildVsatResult(request, result, pt, toHop, nganhToHop);
        } else {
            diemThgxt = buildThptResult(request, result, pt, toHop, nganhToHop);
        }

        // Điểm ưu tiên — hàm tái sử dụng cho mọi phương thức
        Map<String, Object> diemUuTienMap = tinhDiemUuTien(result, diemThgxt);
        BigDecimal diemCong = (BigDecimal) diemUuTienMap.get("diemCong");

        // Điểm xét tuyển = clamp30(ĐTHGXT + Điểm cộng)
        BigDecimal diemThxt = (BigDecimal) result.get("diemThxt");
        BigDecimal diemXetTuyen = clamp30(diemThxt.add(diemCong));
        result.put("diemCong", diemCong);
        result.put("diemXetTuyen", diemXetTuyen);
        result.put("diemUuTienMap", diemUuTienMap);

        evaluateKetQua(result, diemXetTuyen, nganh);
        return result;
    }

    // === Công thức quy đổi nội suy tuyến tính ===
    private BigDecimal quyDoi(BangQuyDoi bqd, BigDecimal diemGoc) {
        if (bqd == null || diemGoc == null) return null;
        BigDecimal den = bqd.getDiemDen().subtract(bqd.getDiemTu());
        if (den.compareTo(ZERO) == 0) return null;
        BigDecimal tu = diemGoc.subtract(bqd.getDiemTu());
        BigDecimal qdDen = bqd.getDiemQuydoiDen().subtract(bqd.getDiemQuydoiTu());
        return bqd.getDiemQuydoiTu()
                .add(tu.multiply(qdDen).divide(den, SCALE, RoundingMode.HALF_UP))
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal clamp30(BigDecimal d) {
        if (d == null) return ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal v = d.setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal min = ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal max = THIRTY.setScale(SCALE, RoundingMode.HALF_UP);
        if (v.compareTo(max) > 0) return max;
        if (v.compareTo(min) < 0) return min;
        return v;
    }

    private BigDecimal parseDiem(String raw) {
        if (isBlank(raw)) return null;
        try {
            return new BigDecimal(raw.replace(",", ".").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatDiem(BigDecimal d) {
        if (d == null) return "–";
        return d.setScale(3, RoundingMode.HALF_UP).toPlainString();
    }

    private boolean isDgnl(PhuongThuc pt) {
        if (pt == null) return false;
        String ma = norm(pt.getMaPhuongthuc());
        String ten = norm(pt.getTenPhuongthuc());
        return ma.contains("DGNL") || ma.contains("PT2")
                || ten.contains("DANHGIANANGLUC") || ten.contains("DGNL");
    }

    private boolean isVsat(PhuongThuc pt) {
        if (pt == null) return false;
        String ma = norm(pt.getMaPhuongthuc());
        String ten = norm(pt.getTenPhuongthuc());
        return ma.contains("VSAT") || ma.contains("PT3")
                || ten.contains("VANSAT") || ten.contains("VSAT");
    }

    private boolean isThpt(PhuongThuc pt) {
        if (pt == null) return false;
        return !isDgnl(pt) && !isVsat(pt);
    }

    private boolean isAllowedPhuongThuc(PhuongThuc pt) {
        return isDgnl(pt) || isVsat(pt) || isThpt(pt);
    }

    private NganhToHop findNganhToHop(String id) {
        if (isBlank(id)) return null;
        return xetTuyenService.findAllNganhToHop().stream()
                .filter(n -> n.getNganhTohopId().toString().equals(id))
                .findFirst().orElse(null);
    }

    private PhuongThuc findPhuongThuc(String id) {
        if (isBlank(id)) return null;
        return xetTuyenService.findAllPhuongThuc().stream()
                .filter(p -> p.getPhuongthucId().toString().equals(id))
                .findFirst().orElse(null);
    }

    private DoiTuongUutien findDoiTuong(String id) {
        if (isBlank(id)) return null;
        return doiTuongService.findAll().stream()
                .filter(d -> d.getDoituongId().toString().equals(id))
                .findFirst().orElse(null);
    }

    private KhuVucUutien findKhuVuc(String id) {
        if (isBlank(id)) return null;
        return khuVucService.findAll().stream()
                .filter(k -> k.getKhuvucId().toString().equals(id))
                .findFirst().orElse(null);
    }

    private List<NganhToHopDto> buildNganhToHopDtoList() {
        return xetTuyenService.findAllNganhToHop().stream()
                .filter(nth -> {
                    Nganh nganh = nth.getNganh();
                    if (nganh == null) return false;
                    List<NganhPhuongThuc> ptList = nganh.getDanhSachNganhPhuongThuc();
                    if (ptList == null || ptList.isEmpty()) return false;
                    return ptList.stream().anyMatch(npt ->
                            npt.getPhuongThuc() != null && isAllowedPhuongThuc(npt.getPhuongThuc()));
                })
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private NganhToHopDto convertToDto(NganhToHop nth) {
        List<NganhToHopDto.MonDto> monList = new ArrayList<>();
        if (nth.getDanhSachNganhToHopMon() != null) {
            for (NganhToHopMon nthm : nth.getDanhSachNganhToHopMon()) {
                if (nthm.getMon() != null) {
                    monList.add(new NganhToHopDto.MonDto(
                            nthm.getMon().getMonId(),
                            nthm.getMon().getMaMon(),
                            nthm.getMon().getTenMon(),
                            nthm.getHeSo() != null ? nthm.getHeSo().intValue() : 1
                    ));
                }
            }
        }
        return new NganhToHopDto(
                nth.getNganh().getNganhId(),
                nth.getNganhTohopId(),
                nth.getToHop().getMaTohop(),
                nth.getToHop().getTenTohop(),
                nth.getDoLech(),
                monList
        );
    }

    private ModelAndView reloadPage(Map<String, Object> ketQua) {
        List<NganhToHopDto> nthDtoList = buildNganhToHopDtoList();
        ModelAndView mav = pageWithData(nthDtoList);
        if (ketQua != null) mav.addObject("ketQua", ketQua);
        return mav;
    }

    private ModelAndView pageWithData(List<NganhToHopDto> nthDtoList) {
        List<PhuongThuc> allowedPtList = xetTuyenService.findActivePhuongThuc().stream()
                .filter(this::isAllowedPhuongThuc)
                .collect(Collectors.toList());

        Set<Integer> allowedNganhIds = nthDtoList.stream()
                .map(NganhToHopDto::getNganhId)
                .collect(Collectors.toSet());

        List<Nganh> allowedNganhList = xetTuyenService.findActiveNganh().stream()
                .filter(n -> allowedNganhIds.contains(n.getNganhId()))
                .collect(Collectors.toList());

        return viewResolver.view("tra-cuu-diem")
                .addObject("danhSachPhuongThuc", allowedPtList)
                .addObject("danhSachNganh", allowedNganhList)
                .addObject("danhSachNganhToHopDto", nthDtoList)
                .addObject("danhSachDoiTuong", doiTuongService.findAll())
                .addObject("danhSachKhuVuc", khuVucService.findAll())
                .addObject("currentPage", "tracuu")
                .addObject("pageTitle", "Tra cứu điểm xét tuyển");
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String reqParam(HttpServletRequest r, String name) {
        String v = r.getParameter(name);
        return (v != null) ? v.trim() : null;
    }

    private String norm(String s) {
        if (s == null) return "";
        return s.trim().toUpperCase(Locale.ROOT).replace("Đ", "D").replaceAll("\\s+", "");
    }
}
