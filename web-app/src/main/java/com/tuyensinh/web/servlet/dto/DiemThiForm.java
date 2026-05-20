package com.tuyensinh.web.servlet.dto;

import com.tuyensinh.entity.DiemThi;
import com.tuyensinh.entity.DiemThiChiTiet;
import com.tuyensinh.entity.Mon;
import com.tuyensinh.entity.PhuongThuc;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DiemThiForm {

    private final String phuongthucIdStr;
    private final String namTuyensinhStr;
    private final String sobaodanh;
    private final String ghiChu;
    private final List<MonDiem> monDiems;
    private final HttpServletRequest request;

    public DiemThiForm(HttpServletRequest request, List<Mon> danhSachMon) {
        this.request = request;
        this.phuongthucIdStr = trim(request.getParameter("phuongthucId"));
        this.namTuyensinhStr = trim(request.getParameter("namTuyensinh"));
        this.sobaodanh = trim(request.getParameter("sobaodanh"));
        this.ghiChu = trim(request.getParameter("ghiChu"));
        this.monDiems = buildMonDiems(danhSachMon);
    }

    public String getPhuongthucIdStr() { return phuongthucIdStr; }
    public String getNamTuyensinhStr() { return namTuyensinhStr; }
    public String getSobaodanh() { return sobaodanh; }
    public String getGhiChu() { return ghiChu; }
    public List<MonDiem> getMonDiems() { return monDiems; }
    public boolean hasPhuongThuc() { return !isEmpty(phuongthucIdStr); }

    public Optional<String> validate() {
        if (!hasPhuongThuc()) {
            return Optional.of("Vui lòng chọn phương thức xét tuyển.");
        }
        return Optional.empty();
    }

    public Optional<String> validateWithPhuongThuc(PhuongThuc phuongThuc) {
        Optional<String> base = validate();
        if (base.isPresent()) return base;

        if (phuongThuc == null) {
            return Optional.of("Không tìm thấy phương thức xét tuyển.");
        }

        BigDecimal thangDiem = phuongThuc.getThangDiem();
        if (thangDiem == null) thangDiem = new BigDecimal("30");

        String ma = phuongThuc.getMaPhuongthuc();
        BigDecimal min = BigDecimal.ZERO;
        BigDecimal max;

        if (ma != null && (ma.contains("DGNL") || ma.contains("PT2"))) {
            max = new BigDecimal("1200");
        } else if (ma != null && (ma.contains("VSAT") || ma.contains("PT3"))) {
            max = new BigDecimal("150");
        } else if (ma != null && ma.contains("THPT")) {
            max = new BigDecimal("10");
        } else {
            max = thangDiem;
        }

        for (MonDiem md : monDiems) {
            if (!md.hasValue()) continue;
            BigDecimal d = md.getDiem();
            if (d == null) continue;

            if (d.compareTo(min) < 0 || d.compareTo(max) > 0) {
                return Optional.of("Điểm môn " + md.getMon().getMaMon()
                    + " phải từ " + min + " đến " + max + " (thang điểm "
                    + phuongThuc.getMaPhuongthuc() + " = " + thangDiem + ").");
            }
        }
        return Optional.empty();
    }

    public Short parseNamTuyensinh() {
        if (isEmpty(namTuyensinhStr)) return 2026;
        try {
            return Short.parseShort(namTuyensinhStr);
        } catch (NumberFormatException e) {
            return 2026;
        }
    }

    public DiemThi bindToEntity(PhuongThuc phuongThuc, Short namTuyensinh) {
        DiemThi diemThi = new DiemThi();
        diemThi.setPhuongThuc(phuongThuc);
        diemThi.setNamTuyensinh(namTuyensinh);
        diemThi.setSobaodanh(sobaodanh);
        diemThi.setGhiChu(ghiChu);
        diemThi.setDanhSachDiemChiTiet(new ArrayList<>());

        for (MonDiem md : monDiems) {
            if (md.hasValue()) {
                DiemThiChiTiet chiTiet = new DiemThiChiTiet();
                chiTiet.setDiemThi(diemThi);
                chiTiet.setMon(md.getMon());
                chiTiet.setDiemGoc(md.getDiem());
                chiTiet.setDiemQuydoi(md.getDiem());
                chiTiet.setDiemSudung(md.getDiem());
                diemThi.getDanhSachDiemChiTiet().add(chiTiet);
            }
        }
        return diemThi;
    }

    private List<MonDiem> buildMonDiems(List<Mon> danhSachMon) {
        List<MonDiem> result = new ArrayList<>();
        for (Mon mon : danhSachMon) {
            String diemStr = trim(request.getParameter("diem_" + mon.getMonId()));
            result.add(new MonDiem(mon, diemStr));
        }
        return result;
    }

    private boolean isEmpty(String v) { return v == null || v.trim().isEmpty(); }
    private String trim(String v) { return v != null ? v.trim() : null; }

    public static class MonDiem {
        private final Mon mon;
        private final BigDecimal diem;

        public MonDiem(Mon mon, String diemStr) {
            this.mon = mon;
            this.diem = parseDiem(diemStr);
        }

        public Mon getMon() { return mon; }
        public BigDecimal getDiem() { return diem; }
        public boolean hasValue() { return diem != null; }

        private BigDecimal parseDiem(String s) {
            if (s == null || s.trim().isEmpty()) return null;
            try {
                return new BigDecimal(s.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}
