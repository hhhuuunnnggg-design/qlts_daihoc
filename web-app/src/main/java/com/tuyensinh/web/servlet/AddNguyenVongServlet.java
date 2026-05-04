package com.tuyensinh.web.servlet;

import com.tuyensinh.entity.Nganh;
import com.tuyensinh.entity.NguyenVong;
import com.tuyensinh.entity.PhuongThuc;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.service.ThiSinhService;
import com.tuyensinh.service.XetTuyenService;
import com.tuyensinh.web.servlet.base.ModelAndView;
import com.tuyensinh.web.servlet.dto.NguyenVongForm;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class AddNguyenVongServlet extends BaseServlet {

    private final ThiSinhService thiSinhService = new ThiSinhService();
    private final XetTuyenService xetTuyenService = new XetTuyenService();

    @Override
    protected ModelAndView handleGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ModelAndView auth = authController.requireLogin(request, response);
        if (auth != null) return auth;

        try {
            ThiSinh thiSinh = thiSinhService.findByNguoiDungId(
                authController.getCurrentUser(request).getNguoidungId()).orElse(null);

            if (thiSinh == null) {
                setMessage(request, "Không tìm thấy thông tin thí sinh.", "warning");
                return viewResolver.redirect("/profile");
            }

            List<NguyenVong> existingNguyenVong = xetTuyenService.findNguyenVongByThiSinh(thiSinh.getThisinhId());

            return viewResolver.view("add-nguyenvong-form")
                .addObject("thiSinh", thiSinh)
                .addObject("danhSachNganh", xetTuyenService.findActiveNganh())
                .addObject("danhSachPhuongThuc", xetTuyenService.findActivePhuongThuc())
                .addObject("danhSachNganhToHop", xetTuyenService.findAllNganhToHop())
                .addObject("soNguyenVongHienTai", existingNguyenVong.size());

        } catch (Exception e) {
            setMessage(request, "Đã xảy ra lỗi: " + e.getMessage(), "danger");
            return viewResolver.redirect("/dashboard");
        }
    }

    @Override
    protected ModelAndView handlePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ModelAndView auth = authController.requireLogin(request, response);
        if (auth != null) return auth;

        try {
            ThiSinh thiSinh = thiSinhService.findByNguoiDungId(
                authController.getCurrentUser(request).getNguoidungId()).orElse(null);

            if (thiSinh == null) {
                setMessage(request, "Không tìm thấy thông tin thí sinh.", "danger");
                return viewResolver.redirect("/add-nguyenvong");
            }

            List<NguyenVong> existingNguyenVong = xetTuyenService.findNguyenVongByThiSinh(thiSinh.getThisinhId());
            if (existingNguyenVong.size() >= 5) {
                setMessage(request, "Bạn đã đăng ký tối đa 5 nguyện vọng.", "warning");
                return viewResolver.redirect("/nguyenvong");
            }

            NguyenVongForm form = new NguyenVongForm(request);
            if (form.validate().isPresent()) {
                setMessage(request, form.validate().get(), "danger");
                return viewResolver.redirect("/add-nguyenvong");
            }

            List<Nganh> danhSachNganh = xetTuyenService.findActiveNganh();
            Nganh nganh = form.findNganh(danhSachNganh);
            if (nganh == null) {
                setMessage(request, "Không tìm thấy ngành học.", "danger");
                return viewResolver.redirect("/add-nguyenvong");
            }

            List<com.tuyensinh.entity.NganhToHop> nganhToHops = xetTuyenService.findNganhToHopByNganh(nganh.getNganhId());
            com.tuyensinh.entity.NganhToHop nganhToHop = form.findNganhToHop(nganhToHops);
            if (nganhToHop == null) {
                setMessage(request, "Không tìm thấy tổ hợp môn.", "danger");
                return viewResolver.redirect("/add-nguyenvong");
            }

            List<PhuongThuc> danhSachPhuongThuc = xetTuyenService.findActivePhuongThuc();
            PhuongThuc phuongThuc = form.findPhuongThuc(danhSachPhuongThuc);
            if (phuongThuc == null) {
                setMessage(request, "Không tìm thấy phương thức xét tuyển.", "danger");
                return viewResolver.redirect("/add-nguyenvong");
            }

            boolean daTonTai = existingNguyenVong.stream().anyMatch(nv ->
                    nv.getNganh().getNganhId().equals(nganh.getNganhId()) &&
                    nv.getNganhToHop().getNganhTohopId().equals(nganhToHop.getNganhTohopId()) &&
                    nv.getPhuongThuc().getPhuongthucId().equals(phuongThuc.getPhuongthucId()));

            if (daTonTai) {
                setMessage(request, "Nguyện vọng này đã tồn tại.", "warning");
                return viewResolver.redirect("/nguyenvong");
            }

            NguyenVong nguyenVong = new NguyenVong();
            nguyenVong.setThiSinh(thiSinh);
            nguyenVong.setNganh(nganh);
            nguyenVong.setNganhToHop(nganhToHop);
            nguyenVong.setPhuongThuc(phuongThuc);
            nguyenVong.setThuTu(existingNguyenVong.size() + 1);
            nguyenVong.setKetQua(NguyenVong.KetQua.CHO_XET);

            xetTuyenService.saveNguyenVong(nguyenVong);
            setMessage(request, "Đăng ký nguyện vọng thành công!", "success");
            return viewResolver.redirect("/nguyenvong");

        } catch (Exception e) {
            setMessage(request, "Đã xảy ra lỗi khi đăng ký nguyện vọng: " + e.getMessage(), "danger");
            return viewResolver.redirect("/add-nguyenvong");
        }
    }
}
