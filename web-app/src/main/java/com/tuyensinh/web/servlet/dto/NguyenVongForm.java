package com.tuyensinh.web.servlet.dto;

import com.tuyensinh.entity.Nganh;
import com.tuyensinh.entity.NganhToHop;
import com.tuyensinh.entity.PhuongThuc;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NguyenVongForm {

    private final String nganhIdStr;
    private final String nganhTohopIdStr;
    private final String phuongthucIdStr;

    public NguyenVongForm(HttpServletRequest request) {
        this.nganhIdStr = trim(request.getParameter("nganhId"));
        this.nganhTohopIdStr = trim(request.getParameter("nganhTohopId"));
        this.phuongthucIdStr = trim(request.getParameter("phuongthucId"));
    }

    public Optional<String> validate() {
        if (isEmpty(nganhIdStr) || isEmpty(nganhTohopIdStr) || isEmpty(phuongthucIdStr)) {
            return Optional.of("Vui lòng điền đầy đủ thông tin.");
        }
        return Optional.empty();
    }

    public Nganh findNganh(List<Nganh> danhSachNganh) {
        if (isEmpty(nganhIdStr)) return null;
        try {
            Integer id = Integer.parseInt(nganhIdStr);
            return danhSachNganh.stream()
                    .filter(n -> n.getNganhId().equals(id))
                    .findFirst()
                    .orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public NganhToHop findNganhToHop(List<NganhToHop> danhSach) {
        if (isEmpty(nganhTohopIdStr)) return null;
        try {
            Integer id = Integer.parseInt(nganhTohopIdStr);
            return danhSach.stream()
                    .filter(n -> n.getNganhTohopId().equals(id))
                    .findFirst()
                    .orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public PhuongThuc findPhuongThuc(List<PhuongThuc> danhSach) {
        if (isEmpty(phuongthucIdStr)) return null;
        try {
            Short id = Short.parseShort(phuongthucIdStr);
            return danhSach.stream()
                    .filter(p -> p.getPhuongthucId().equals(id))
                    .findFirst()
                    .orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isEmpty(String v) { return v == null || v.trim().isEmpty(); }
    private String trim(String v) { return v != null ? v.trim() : null; }
}
