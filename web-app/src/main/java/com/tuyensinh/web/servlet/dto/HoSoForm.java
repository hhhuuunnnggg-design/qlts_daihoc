package com.tuyensinh.web.servlet.dto;

import com.tuyensinh.entity.DoiTuongUutien;
import com.tuyensinh.entity.KhuVucUutien;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.service.DoiTuongService;
import com.tuyensinh.service.KhuVucService;
import com.tuyensinh.util.DateUtil;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Optional;

public class HoSoForm {

    private final String cccd;
    private final String ho;
    private final String ten;
    private final String ngaySinhStr;
    private final String gioiTinh;
    private final String dienThoai;
    private final String email;
    private final String noiSinh;
    private final String doituongIdStr;
    private final String khuvucIdStr;

    public HoSoForm(HttpServletRequest request) {
        this.cccd = trim(request.getParameter("cccd"));
        this.ho = trim(request.getParameter("ho"));
        this.ten = trim(request.getParameter("ten"));
        this.ngaySinhStr = trim(request.getParameter("ngaySinh"));
        this.gioiTinh = request.getParameter("gioiTinh");
        this.dienThoai = trim(request.getParameter("dienThoai"));
        this.email = trim(request.getParameter("email"));
        this.noiSinh = trim(request.getParameter("noiSinh"));
        this.doituongIdStr = trim(request.getParameter("doituongId"));
        this.khuvucIdStr = trim(request.getParameter("khuvucId"));
    }

    public Optional<String> validate() {
        if (isEmpty(cccd) || isEmpty(ho) || isEmpty(ten) || isEmpty(dienThoai) || isEmpty(email)) {
            return Optional.of("CCCD, họ, tên, điện thoại, email là bắt buộc.");
        }
        if (!cccd.matches("[0-9]{9,12}")) {
            return Optional.of("Số CCCD phải là 9-12 chữ số.");
        }
        return Optional.empty();
    }

    public void bindToEntity(ThiSinh thiSinh, DoiTuongService doiTuongService, KhuVucService khuVucService) {
        thiSinh.setCccd(cccd);
        thiSinh.setHo(ho);
        thiSinh.setTen(ten);

        if (!isEmpty(ngaySinhStr)) {
            LocalDate d = DateUtil.parseDate(ngaySinhStr);
            thiSinh.setNgaySinh(d);
        } else {
            thiSinh.setNgaySinh(null);
        }

        thiSinh.setGioiTinh(gioiTinh);
        thiSinh.setDienThoai(dienThoai);
        thiSinh.setEmail(email);
        thiSinh.setNoiSinh(noiSinh);

        thiSinh.setDoiTuongUutien(parseDoiTuong(doiTuongService));
        thiSinh.setKhuVucUutien(parseKhuVuc(khuVucService));
    }

    public String getCccd() { return cccd; }
    public String getHo() { return ho; }
    public String getTen() { return ten; }

    private DoiTuongUutien parseDoiTuong(DoiTuongService service) {
        if (isEmpty(doituongIdStr)) return null;
        try {
            return service.findById(Integer.parseInt(doituongIdStr));
        } catch (Exception e) {
            return null;
        }
    }

    private KhuVucUutien parseKhuVuc(KhuVucService service) {
        if (isEmpty(khuvucIdStr)) return null;
        try {
            return service.findById(Integer.parseInt(khuvucIdStr));
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isEmpty(String v) {
        return v == null || v.trim().isEmpty();
    }

    private String trim(String v) {
        return v != null ? v.trim() : null;
    }
}
